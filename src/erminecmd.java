import java.io.*;

import classScore.*;

/**
 Command line interface for ermineJ program.
 This needs major work!!!
 command line arguments in the following way
  args[0]:pval_file,
  args[1]:affy_go_file,
  args[2]:Go_name_file,
  args[3]:ug_file,
  args[4]:destination_file,
  args[5]:method,
  args[6]:class_max_size,
  args[7]:class_min_size,
  args[8]:number of runs,
  args[9]:quantile,
  args[10]:p-value,
  args[11]:weightcheck
  args[12]
  args[13]
  args[14]
 **notce: all "data files" and this "command line file" should be put uncer the directory ermineJ\java_proj\src
  examples:
  1. java ermine age.welch.pvals.highexpression.forerminej.txt MG-U74Av2.go.txt goNames.txt MG-U74Av2.ug.txt output-quantile-log2.txt QUANTILE_METHOD 100 4 10000 50 0.001 true 1 true
  2. java ermine age.welch.pvals.highexpression.forerminej.txt MG-U74Av2.go.txt goNames.txt MG-U74Av2.ug.txt output-mean-log3.txt MEAN_METHOD 100 4 10000 50 0.0001 true 1 true
  @author Edward Chen, Paul Pavlidis
  @version $Id$
 */

public class erminecmd {

  public static void main(String args[]) {

    try {

      String pbPvalFile = args[0];
      String affyGoFile = args[1];
      String goNameFile = args[2];
      String destinFile = args[3];
      pbPvalFile = getCanonical(pbPvalFile);
      affyGoFile = getCanonical(affyGoFile);
      destinFile = getCanonical(destinFile);
      goNameFile = getCanonical(goNameFile);
      classScoreStatus m = new classScoreStatus(null);
      System.err.println("P values from " + pbPvalFile);
      System.err.println("Probe annotations from " + affyGoFile);
      System.err.println("Output into " + destinFile);

      setupMaps smaps = new setupMaps(pbPvalFile, affyGoFile, goNameFile, // files
                                      args[4],args[5], // methods
                                      Integer.parseInt(args[6]), // max clas
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
                                            smaps.probeToClassMap,
                                            smaps.classToProbe,
                                            destinFile,                   // output file
                                            Double.parseDouble(args[10]), // pvalue
                                            args[11],                     // use weights
                                            args[14],                     // mtc method
                                            m, false);
    }
    catch (ArrayIndexOutOfBoundsException exception) { // this doesn't work ...
      System.err.println("You must enter 15 command line arguments: \nprobe_pvalfile\nannot file\ngo_namefile\ndestination_file\nmethod\ngroups method\nmax class size\nmin class size\nnum runs\nquantile\npval\nwt_check\npvalcolumn\ndolog\nmultiple test correction method (bon|bh|wy)");
    }
    catch (IOException e) {
      System.err.println("File reading/writing error");
      e.printStackTrace();
    }

  }

  protected static String getCanonical(String in) {

    if (in == null || in.length() == 0) {
      return in;
    }

    File outFile = new File(in);
    try {
      return outFile.getCanonicalPath();
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
