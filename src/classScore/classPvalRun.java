package classScore;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import classScore.gui.geneSet.JDetailsFrame;
import classScore.analysis.ClassPvalSetGenerator;
import classScore.analysis.ClassSizeComputer;
import classScore.analysis.MultipleTestCorrector;
import baseCode.math.Rank;

/**
  Main class to make 'experiment score' pvalues. Includes multiple
  test correction.   Created :09/02/02
  @author Shahmil Merchant; Paul Pavlidis (major changes)
  @version $Id$
 * @todo set up way to do different types of analysis
 * @todo pass all the maps around in a container instead of as lots of parameters.
 */
public class classPvalRun {

   private GONames goName;
   private expClassScore probePvalMapper;
   private GeneAnnotations geneData;
   private Map probeGroups;
   private ClassMap probeToClassMap;
   private Map classToProbe;
   private histogram hist;
   private String dest_file;
   private boolean weight_on = true;
   private Map results = null;
   private Vector sortedclasses = null; // this holds the results.
   private int inputSize;
   private int numOverThreshold = 0; // number of genes over the threshold
   private int numUnderThreshold = 0; // number of genes below the threshold
   private NumberFormat nf = NumberFormat.getInstance();
   private boolean useUniform = false; // assume input values come from uniform distribution under null hypothesis.

   /**
    *
    * @param imaps InitialMaps
    * @param resultsFile String
    * @param pval double
    * @param useWeights String
    * @param mtc_method String
    * @param messenger classScoreStatus
    * @param loadResults boolean
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public classPvalRun(InitialMaps imaps,
                       String resultsFile,
                       double pval,
                       String useWeights,
                       String mtc_method,
                       classScoreStatus messenger,
                       boolean loadResults) throws
           IllegalArgumentException, IOException {
      initialize(imaps.goName,
                 imaps.probePvalMapper,
                 imaps.geneData,
                 imaps.probeGroups,
                 imaps.classToProbe,
                 resultsFile,
                 pval,
                 useWeights,
                 mtc_method,
                 messenger,
                 loadResults);
   }

   /**
    *
    * @param imaps InitialMaps
    * @param resultsFile String
    * @param pval double
    * @param useWeights String
    * @param mtc_method String
    * @param messenger classScoreStatus
    * @param loadResults boolean
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public classPvalRun(Settings settings,
                       InitialMaps imaps,
                       String resultsFile,
                       String useWeights,
                       String mtc_method,
                       classScoreStatus messenger,
                       boolean loadResults) throws
           IllegalArgumentException, IOException {
      initialize(imaps.goName,
                 imaps.probePvalMapper,
                 imaps.geneData,
                 imaps.probeGroups,
                 imaps.classToProbe,
                 resultsFile,
                 settings.getPValThreshold(),
                 useWeights,
                 mtc_method,
                 messenger,
                 loadResults);
   }

   /**
    *
    * @param gn GONameReader
    * @param ppm expClassScore
    * @param gd GeneDataReader
    * @param pgm Map
    * @param ctp Map
    * @param resultsFile String
    * @param pval double
    * @param useWeights String
    * @param mtc_method String
    * @param messenger classScoreStatus
    * @param loadResults boolean
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public classPvalRun(GONames gn,
                       expClassScore ppm,
                       GeneAnnotations gd,
                       Map pgm,
                       Map ctp,
                       String resultsFile,
                       double pval,
                       String useWeights,
                       String mtc_method,
                       classScoreStatus messenger,
                       boolean loadResults) throws IllegalArgumentException,
           IOException {
      initialize(gn, ppm, gd, pgm, ctp,
                 resultsFile, pval, useWeights, mtc_method,
                 messenger, loadResults);
   }

   /**
    *
    * @param gn GONameReader
    * @param ppm expClassScore
    * @param gd GeneDataReader
    * @param pgm Map
    * @param ctp Map
    * @param resultsFile String
    * @param pval double
    * @param useWeights String
    * @param mtc_method String
    * @param messenger classScoreStatus
    * @param loadResults boolean
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public void initialize(GONames gn,
                          expClassScore ppm,
                          GeneAnnotations gd,
                          Map pgm,
                          Map ctp,
                          String resultsFile,
                          double pval,
                          String useWeights,
                          String mtc_method,
                          classScoreStatus messenger,
                          boolean loadResults) throws
           IllegalArgumentException, IOException {

      goName = gn;
      probePvalMapper = ppm;
      geneData = gd;
      probeGroups = pgm;
      classToProbe = ctp;

      nf.setMaximumFractionDigits(8);

      // user flags and constants:
      //    user_pvalue = -(Math.log(pval) / Math.log(10)); // user defined pval (cutoff) for hypergeometric todo: this should NOT be here. What if the cutoff isn't a pvalue. See pvalue parse.
      weight_on = (Boolean.valueOf(useWeights)).booleanValue();
      dest_file = resultsFile;

      if (loadResults) {
         readResultsFromFile(resultsFile);
         sortResults();
      } else {

         // Calculate random classes. todo: what a mess. This histogram should be held by the class that originated it.
         if (!useUniform) {
            messenger.setStatus("Starting resampling");
            System.out.println("Starting resampling");
            hist = probePvalMapper.generateNullDistribution(messenger);
            messenger.setStatus("Finished resampling");
         }

//    messenger.setStatus(hist.toString());
         System.err.println("Hist to string: " + hist.toString());

         // Initialize the results data structure.
         results = new LinkedHashMap();

         // get the class sizes. /* todo use initmap */
         ClassSizeComputer csc = new ClassSizeComputer(probePvalMapper,
                 classToProbe, probeGroups,
                 weight_on);
         csc.getClassSizes();

         //    Collection inp_entries; // this is only used for printing.
         Map input_rank_map;
         if (weight_on) {
            //      inp_entries = probePvalMapper.get_group_pval_map().entrySet();
            input_rank_map = Rank.rankTransform(probePvalMapper.
                                                get_group_pval_map());
         } else {
            //        inp_entries = probePvalMapper.get_map().entrySet();
            input_rank_map = Rank.rankTransform(probePvalMapper.get_map());
         }

         inputSize = input_rank_map.size(); // how many pvalues. This is constant under permutations of the data

         // hgSizes(inp_entries); // get numOverThreshold and numUnderThreshold. Constant under permutations of the data.

         System.err.println("Input size=" + inputSize + " numOverThreshold=" +
                            numOverThreshold + " numUnderThreshold=" +
                            numUnderThreshold + " "); //+  + "" + foo + "" + foo + "" + foo + "" + foo );

         /* todo use initmap */
         ClassPvalSetGenerator pvg = new ClassPvalSetGenerator(classToProbe,
                 probeGroups, weight_on,
                 hist, probePvalMapper, csc, goName);

         // calculate the actual class scores and correct sorting. /** todo make this use initmap */
         pvg.classPvalGenerator(probePvalMapper.get_group_pval_map(),
                                probePvalMapper.get_map(),
                                input_rank_map);
         results = pvg.getResults();
         sortResults();

         messenger.setStatus("Multiple test correction");

         /** todo use initmap */
         MultipleTestCorrector mt = new MultipleTestCorrector(sortedclasses,
                 results,
                 probePvalMapper, weight_on, hist, probeGroups, classToProbe,
                 csc);
         if (mtc_method.equals("bon")) {
            mt.bonferroni(); // no arg: bonferroni. integer arg: w-y, int trials. Double arg: FDR
         } else if (mtc_method.equals("bh")) {
            mt.benjaminihochberg(0.05);
         } else if (mtc_method.equals("wy")) {
            mt.westfallyoung(10000);
         }

         messenger.setStatus("Beginning output");
         // all done:
         // print the results
         if (dest_file.compareTo("") != 0) {
            ResultsPrinter rpr = new ResultsPrinter(dest_file, sortedclasses,
                    results, goName, probeToClassMap);
         }
         //printResults(true);
      }

      //for table output
      for (int i = 0; i < sortedclasses.size(); i++) {
         ((classresult) results.get((String) sortedclasses.get(i))).setRank(i +
                 1);
      }

      messenger.setStatus("Done!");
   }
   /* Done! */

   /**
    Sorted order of the class results - all this has to hold is the class names.
    */
   private void sortResults() {
      sortedclasses = new Vector(results.entrySet().size());
      Collection k = results.values();
      Vector l = new Vector();
      l.addAll(k);
      Collections.sort(l);
      for (Iterator it = l.iterator(); it.hasNext(); ) {
         sortedclasses.add(((classresult) it.next()).getClassId());
      }
   }


   /**
    *
    * @return javax.swing.table.TableModel
    */
   public TableModel toTableModel() {
      return new AbstractTableModel() {

         private String[] columnNames = {
                                        "Rank", "GO Id", "Name", "Size",
                                        "Eff. size", "Score",
                                        "Class P value"};

         public String getColumnName(int i) {
            return columnNames[i];
         }

         public int getColumnCount() {
            return 7;
         }

         public int getRowCount() {
            return sortedclasses.size();
         }

         public Object getValueAt(int i, int j) {
            classresult res = (classresult) results.get((String) sortedclasses.
                    get(i));
            switch (j) {
            case 0:
               return new Integer(i + 1);
            case 1:
               return res.getClassId();
            case 2:
               return res.getClassName();
            case 3:
               return new Integer(res.getSize());
            case 4:
               return new Integer(res.getEffectiveSize());
            case 5:
               return new Double(nf.format(res.getScore()));
            case 6:
               return new Double(nf.format(res.getPvalue()));
            default:
               return "";
            }
         }
      };
   }

   /**
    * This should not really be here...
    *
    * @param index int
    * @param settings Settings
    */
   public void showDetails(int index, Settings settings) {
      final classresult res = (classresult) results.get((String) sortedclasses.
              get(
              index));
      String name = res.getClassName();
      final String id = res.getClassId();
      System.err.println(name);
      final ArrayList values = (ArrayList) classToProbe.get(id);

      final Map pvals = new HashMap();
      for (int i = 0, n = values.size(); i < n; i++) {
         Double pvalue = new Double(Math.pow(10.0,
                                             -probePvalMapper.getPval((
                 String) ((ArrayList) classToProbe.get(id)).get(i))));
         pvals.put((String) ((ArrayList) classToProbe.get(id)).get(i), pvalue);
      }

      if (values == null) {
         throw new RuntimeException("Class data retrieval error for " + name);
      }

      // create the details frame
      JDetailsFrame f = new JDetailsFrame(
         values, pvals, classToProbe, id, geneData, settings
         );
      f.setTitle(name + " (" + values.size() + " items)");
      f.show();
   }

   /* */
   private void readResultsFromFile(String destination_file) {
      ResultsFileReader f = new ResultsFileReader(destination_file);
      this.results = f.getResults();
   }

   /**
    *
    * @return Map the results
    */
   public Map getResults() {
      return results;
   }


   /*
      public static void main(String[] args) {

         // Check if args have been passed in.
         if (args.length < 14) {

            System.err.println("No filenames have been passed in.");
            System.err.println(
                    "If you are running this class as your main project " +
                    "class, you might want to run classScoreGUI instead.");
            System.exit(1);
         }

         classScoreStatus m = new classScoreStatus(null);
         try {
            InitialMaps smaps = new InitialMaps(
                    args[0], // pbPval file
                    args[1], // affy GO File
                    args[2], // GO name file
                    args[4], args[5], // methods
                    Integer.parseInt(args[6]), // max class
                    Integer.parseInt(args[7]), // min class
                    Integer.parseInt(args[8]), // numruns
                    Integer.parseInt(args[9]), // quantile
                    args[11], // use weights
                    Integer.parseInt(args[12]), // column
                    args[13], // takeLog
                    m);

            classPvalRun test = new classPvalRun(smaps.goName,
                                                 smaps.probePvalMapper,
                                                 smaps.geneData,
                                                 smaps.probeGroups,
                                                 smaps.classToProbe,
                                                 args[3], // output file
    Double.parseDouble(args[10]), // pvalue
                                                 args[11], // use weights
                                                 args[14], // mtc method
                                                 m, false);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
    */


}
