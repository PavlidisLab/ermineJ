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
    * @param settings Settings
    * @param quantile int
    * @param messenger classScoreStatus
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public InitialMaps(Settings settings, classScoreStatus messenger) throws
           IllegalArgumentException, IOException {
      readFiles(settings, messenger);
      GeneGroupReader gn = setupClasses(messenger);
      messenger.setStatus("Initializing gene score mapping");
      probePvalMapper.setInputPvals(gn.get_group_probe_map(),settings.getGroupMethod()); // this initializes the group_pval_map, Calculates the ave/best pvalue for each group
   }

   private void readFiles(Settings settings, classScoreStatus messenger
                          ) throws IllegalArgumentException, IOException {
      messenger.setStatus("Reading GO descriptions " + settings.getClassFile());
      goName = new GONames(settings.getClassFile()); // parse go name file
      messenger.setStatus("Reading gene scores from " + settings.getScoreFile());
      probePvalMapper = new expClassScore(settings);
      messenger.setStatus("Reading gene annotations from " + settings.getAnnotFile());
      geneData = new GeneAnnotations(settings.getAnnotFile(), probePvalMapper.get_map());
   }

   public GeneGroupReader setupClasses(classScoreStatus messenger) {
      GeneGroupReader groupName = new GeneGroupReader(geneData.getGeneToProbeList(),
              geneData.getProbeToGeneMap()); // parse group file. Yields map of probe->replicates.
      probeGroups = groupName.get_probe_group_map(); // map of probes to groups
      messenger.setStatus("Initializing gene class mapping");
      ClassMap probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                              geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      probeToClassMap.hackClassToProbeMap();
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      System.err.println("Hacked classToProbe has size: " + classToProbe.size());
      sortGeneSets(classToProbe);
      System.err.println("Sorted classes has size: " + sortedclasses.size());
      messenger.setStatus("Done with setup");
      return groupName;
   }

   private void sortGeneSets(Map geneSetToProbe) {
      sortedclasses = new Vector(geneSetToProbe.entrySet().size());
      System.err.println("classes has size: " + geneSetToProbe.entrySet().size());
      Set keys = geneSetToProbe.keySet();
      System.err.println("keyshas size: " + keys.size());
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
      sortGeneSets(classToProbe);
   }

   public void modifyClass(String id, String desc, ArrayList probes) {
      System.err.println("modifying " + id + " to setupmap");
      geneData.modifyClass(id, probes);
      goName.modifyClass(id, desc);
      ClassMap probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                              geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      sortGeneSets(classToProbe);
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
