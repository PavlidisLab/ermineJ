package classScore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
  Makes the initial maps based on the input files. Created :05/27/04
  @author Shahmil Merchant; Paul Pavlidis (major changes)
  @version $Id$
 * @todo remove redundant readers
 */
public class InitialMaps {
   public GONames goName;
   public GeneAnnotations geneData = null;
   public Map probeGroups;
   public Map classToProbe;
   public expClassScore probePvalMapper;
   private Vector sortedclasses = null;


   public GONames getGoName() {
      return goName;
   }

   public Map getProbeGroups() {
      return probeGroups;
   }

   public Map getClassToProbe() {
      return classToProbe;
   }

   public expClassScore getProbePvalMapper() {
      return probePvalMapper;
   }

   /**
    *
    * @param probe_annotfile String
    * @param goNamesfile String
    * @param messenger classScoreStatus
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public InitialMaps(String probe_annotfile, String goNamesfile,
                      classScoreStatus messenger) throws
           IllegalArgumentException, IOException {
      readFiles(probe_annotfile, goNamesfile, messenger);
      setupClasses(messenger);
   }

   /**
    *
    * @param probePvalFile String
    * @param probe_annotfile String
    * @param goNamesfile String
    * @param method String
    * @param groupMethod String
    * @param classMaxSize int
    * @param classMinSize int
    * @param numberOfRuns int
    * @param quantile int
    * @param useWeights String
    * @param pvalcolumn int
    * @param dolog_check String
    * @param messenger classScoreStatus
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public InitialMaps(String probePvalFile,
                      String probe_annotfile,
                      String goNamesfile,
                      String method,
                      String groupMethod,
                      int classMaxSize,
                      int classMinSize,
                      int numberOfRuns,
                      int quantile,
                      String useWeights,
                      int pvalcolumn,
                      String dolog_check,
                      classScoreStatus messenger) throws
           IllegalArgumentException, IOException {
      readFiles(probe_annotfile, goNamesfile, messenger, probePvalFile,
                method, classMaxSize, classMinSize, numberOfRuns, quantile,
                useWeights, pvalcolumn, dolog_check);
      GeneGroupReader gn = setupClasses(messenger);
      messenger.setStatus("Initializing gene score mapping");
      probePvalMapper.setInputPvals(gn.get_group_probe_map(),
                                    groupMethod); // this initializes the group_pval_map, Calculates the ave/best pvalue for each group
   }

   private void readFiles(String probe_annotfile, String goNamesfile,
                          classScoreStatus messenger) throws
           IllegalArgumentException, IOException {
      messenger.setStatus("Reading GO descriptions from " + goNamesfile);
      goName = new GONames(goNamesfile); // parse go name file
      messenger.setStatus("Reading gene annotations from " + probe_annotfile);
      geneData = new GeneAnnotations(probe_annotfile);
   }

   private void readFiles(String probe_annotfile, String goNamesfile,
                          classScoreStatus messenger, String probePvalFile,
                          String method, int classMaxSize, int classMinSize,
                          int numberOfRuns, int quantile,
                          String useWeights, int pvalcolumn,
                          String dolog_check
                          ) throws IllegalArgumentException, IOException {
      messenger.setStatus("Reading GO descriptions " + goNamesfile);
      goName = new GONames(goNamesfile); // parse go name file
      messenger.setStatus("Reading gene scores from " + probePvalFile);
      boolean dolog = (Boolean.valueOf(dolog_check)).booleanValue();
      probePvalMapper = new expClassScore(probePvalFile, useWeights, method,
                                          pvalcolumn, dolog, classMaxSize,
                                          classMinSize, numberOfRuns,
                                          quantile);
      messenger.setStatus("Reading gene annotations from " + probe_annotfile);
      geneData = new GeneAnnotations(probe_annotfile, probePvalMapper.get_map());
   }

   private GeneGroupReader setupClasses(classScoreStatus messenger) {
      GeneGroupReader groupName = new GeneGroupReader(geneData.getGeneToProbeList(),
              geneData.getProbeToGeneMap()); // parse group file. Yields map of probe->replicates.
      probeGroups = groupName.get_probe_group_map(); // map of probes to groups
      messenger.setStatus("Initializing gene class mapping");
      ClassMap probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                              geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      probeToClassMap.hackClassToProbeMap();
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      System.err.println("Hacked classToProbe has size: " + classToProbe.size());
      sortClasses();
      messenger.setStatus("Done with setup");
      return groupName;
   }

   private void sortClasses() {
      sortedclasses = new Vector(classToProbe.entrySet().size());
      Set keys = classToProbe.keySet();
      Vector l = new Vector();
      l.addAll(keys);
      Collections.sort(l);
      Iterator it = l.iterator();
      while (it.hasNext()) {
         sortedclasses.add(it.next());
      }
   }

   public static TableModel toBlankTableModel() {
      return new AbstractTableModel() {
         private String[] columnNames = {"Name", "Description", "# of Probes",
                                        "# of Genes"};

         public String getColumnName(int i) {
            return columnNames[i];
         }

         public int getColumnCount() {
            return columnNames.length;
         }

         public int getRowCount() {
            return 30;
         }

         public Object getValueAt(int i, int j) {
            return "";
         }
      };
   }

   public TableModel toTableModel() {
      return new AbstractTableModel() {
         private String[] columnNames = {"Name", "Description", "# of Probes",
                                        "# of Genes"};

         public String getColumnName(int i) {return columnNames[i];
         }

         public int getColumnCount() {return columnNames.length;
         }

         public int getRowCount() {return sortedclasses.size();
         }

         public Object getValueAt(int i, int j) {
            String classid = (String) sortedclasses.get(i);

            switch (j) {
            case 0:
               return classid;
            case 1:
               return goName.getNameForId(classid);
            case 2: {
               int numprobes = 0;
               if (classToProbe.containsKey(classid)) {
                  numprobes = ((ArrayList) classToProbe.get(classid)).size();
               }
               return Integer.toString(numprobes);
            }
            case 3: {
               HashSet genes = new HashSet();
               ArrayList probes = (ArrayList) classToProbe.get(classid);
               Iterator probe_it = probes.iterator();
               while (probe_it.hasNext()) {
                  genes.add(geneData.getProbeGeneName((String) probe_it.next()));
               }
               int numgenes = genes.size();
               return Integer.toString(numgenes);
            }
            default:
               return "";
            }
         }
      };
   };

   public void addClass(String id, String desc, ArrayList probes) {
      System.err.println("adding " + id + " to setupmap");
      geneData.addClass(id, probes);
      goName.addClass(id, desc);
      ClassMap probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                              geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      sortClasses();
   }

   public void modifyClass(String id, String desc, ArrayList probes) {
      System.err.println("modifying " + id + " to setupmap");
      geneData.modifyClass(id, probes);
      goName.modifyClass(id, desc);
      ClassMap probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                              geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      sortClasses();
   }

   public int numClasses() {
      return sortedclasses.size();
   }

   public String getClass(int i) {
      return (String) sortedclasses.get(i);
   }

   public String getClassDesc(String id) {
      return (String) goName.getNameForId(
           id);
   }

   public int numProbes(String id) {
      return ((ArrayList) classToProbe.get(id)).
           size();
   }

   public int numGenes(String id) {
      HashSet genes = new HashSet();
      ArrayList probes = (ArrayList) classToProbe.get(id);
      Iterator probe_it = probes.iterator();
      while (probe_it.hasNext()) {
         genes.add(geneData.getProbeGeneName((String) probe_it.next()));
      }
      return genes.size();
   }
}
