package classScore;

import java.io.*;
import java.util.*;
import javax.swing.table.*;

/**
  Makes the initial maps based on the input files. Created :05/27/04
  @@author Shahmil Merchant; Paul Pavlidis (major changes)
  @@version $Id$
 */
public class SetupMaps {
   public GONameReader goName;
   public expClassScore probePvalMapper;
   public GeneDataReader geneData = null;
   public Map probeGroups;
   public ClassMap probeToClassMap;
   public Map classToProbe;
   private GeneGroupReader groupName;
   private boolean weight_on = true;
   private boolean dolog = true;
   private Vector sortedclasses = null;

   /**
    */
   public SetupMaps(String probePvalFile,
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
                       classScoreStatus messenger)
          throws IllegalArgumentException, IOException{

      // user flags and constants:
      dolog = (Boolean.valueOf(dolog_check)).booleanValue();

      messenger.setStatus("Reading GO descriptions");
      goName = new GONameReader(goNamesfile); // parse go name file

      messenger.setStatus("Reading gene scores");
      probePvalMapper = new expClassScore(probePvalFile, useWeights, method,
                                          pvalcolumn, dolog, classMaxSize,
                                          classMinSize, numberOfRuns,
                                          quantile);

      messenger.setStatus("Reading gene data file");
      geneData = new GeneDataReader(probe_annotfile, probePvalMapper.get_map());
      groupName = new GeneGroupReader(geneData.getGroupProbeList(),
                                      geneData.getProbeGroupMap()); // parse group file. Yields map of probe->replicates.
      probeGroups = groupName.get_probe_group_map(); // map of probes to groups

      messenger.setStatus("Initializing gene score mapping");

      //  if (weight_on) {
      probePvalMapper.setInputPvals(groupName.get_group_probe_map(),
                                    groupMethod); // this initializes the group_pval_map, Calculates the ave/best pvalue for each group
      //  }

      messenger.setStatus("Initializing gene class mapping");

      //   messenger.setStatus("Reading in GO class membership");
      probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                     geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      sortClasses();
      messenger.setStatus("Done with setup");
   }

   private void sortClasses()
   {
      sortedclasses = new Vector(classToProbe.entrySet().size());
      Set keys = classToProbe.keySet();
      Vector l = new Vector();
      l.addAll(keys);
      Collections.sort(l);
      Iterator it = l.iterator();
      while (it.hasNext())
      {
         sortedclasses.add(it.next());
      }
   }

   public static TableModel toBlankTableModel() {
      return new AbstractTableModel()
      {
         private String[] columnNames = {"Name", "Description","# of Probes","# of Genes"};

         public String getColumnName(int i) {
            return columnNames[i];
         }

         public int getColumnCount() {
            return columnNames.length;
         }

         public int getRowCount() {
            return 30;
         }

         public Object getValueAt(int i, int j)
         {
            return "";
         }
      };
   }

   public TableModel toTableModel()
   {
      return new AbstractTableModel()
      {
         private String[] columnNames = {"Name", "Description","# of Probes","# of Genes"};

         public String getColumnName(int i) { return columnNames[i]; }

         public int getColumnCount() { return columnNames.length; }

         public int getRowCount() { return sortedclasses.size(); }

         public Object getValueAt(int i, int j)
         {
            String classid = (String) sortedclasses.get(i);
            String classdesc = (String) goName.get_GoName_map().get(classid);

            switch (j)
            {
               case 0:
                  return classid;
               case 1:
                  return classdesc;
               case 2:
               {
                  int numprobes = 0;
                  if(classToProbe.containsKey(classid))
                     numprobes = ((ArrayList)classToProbe.get(classid)).size();
                  return Integer.toString(numprobes);
               }
               case 3:
               {
                  HashSet genes = new HashSet();
                  ArrayList probes=(ArrayList)classToProbe.get(classid);
                  Iterator probe_it=probes.iterator();
                  while(probe_it.hasNext())
                  {
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

   public void addClass(String id, String desc, ArrayList probes)
   {
      System.err.println("adding " + id + " to setupmap");
      geneData.addClass(id, probes);
      goName.addClass(id,desc);
      probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                     geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      sortClasses();
   }

   public void modifyClass(String id, String desc, ArrayList probes)
   {
      System.err.println("modifying " + id + " to setupmap");
      geneData.modifyClass(id, probes);
      goName.modifyClass(id,desc);
      probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                     geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      sortClasses();
   }
}
