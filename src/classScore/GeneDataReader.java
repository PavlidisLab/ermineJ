package classScore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *  Reads tab-delimited file to create maps of probes to classes, classes to probes, probes to genes, genes to probes.
 * <p>Copyright (c) 2004 Columbia University</p>
 * @author Paul Pavlidis, Shamhil Merchant, Homin Lee
 * @version 1.0
 * @todo constructors should throw IOException.
 */

public class GeneDataReader {
   private Map probeToClassMap; //stores probe->Classes map
   private Map classToProbeMap; //stores Classes->probe map
   private Map probeToGeneName; // same as probeGroupMap
   private Map probeToDescription;
   private Map probeGroupMap;
   private Map groupProbeList;
//   private Set group;
   private String filename;
   private Map probes;
   private Vector probe_list;

   public GeneDataReader(String filename, Map probes) {
      this.filename = filename;
      this.probes = probes;
      probeToClassMap = new LinkedHashMap();
      classToProbeMap = new LinkedHashMap();
      probeToGeneName = new HashMap();
      probeGroupMap = probeToGeneName;
      probeToDescription = new HashMap();
      groupProbeList = new HashMap();
      try {
         this.readFile();
      } catch (IOException ex) {
      }
   }

   public GeneDataReader(String filename) {
      this.filename = filename;
      probeToClassMap = new LinkedHashMap();
      classToProbeMap = new LinkedHashMap();
      probeToGeneName = new HashMap();
      probeGroupMap = probeToGeneName;
      probeToDescription = new HashMap();
      groupProbeList = new HashMap();
      try {
         this.readFile();
      } catch (IOException ex) {
      }
   }

//read in file
   private void readFile() throws IOException {
      File infile = new File(filename);
      if (!infile.exists() || !infile.canRead()) {
         System.err.println("Could not read " + filename);
      }

      FileInputStream fis = new FileInputStream(filename);

      BufferedInputStream bis = new BufferedInputStream(fis);
      BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
   //   Vector rows = new Vector();
   //   Vector cols = null;

      ArrayList probeIds = new ArrayList();
      String classIds = null;
      String go = "";

      // loop through rows. Makes hash map of probes to go, and map of go to probes.
      String line = "";
      while ((line = dis.readLine()) != null) {
         //tokenize file by tab character
         StringTokenizer st = new StringTokenizer(line, "\t");

         // create a new Vector for each row's columns
         String probe = st.nextToken().intern();
         if (probes == null || probes.containsKey(probe)) { // only do probes we have in the data set.

            /* read gene name */
            String group = st.nextToken().intern();
            probeToGeneName.put(probe.intern(), group.intern());

            // create the list if need be.
            if (groupProbeList.get(group) == null) {
               groupProbeList.put(group.intern(), new ArrayList());
            }
            ((ArrayList) groupProbeList.get(group)).add(probe.intern());

            probeIds.add(probe);
            probeToClassMap.put(probe.intern(), new ArrayList());

            /* read gene description */
            if (st.hasMoreTokens()) {
               String description = st.nextToken().intern();
               if (!description.startsWith("GO:")) { // this happens when there is no desription and we skip to the GO terms.
                  probeToDescription.put(probe.intern(), description.intern());
               } else {
                  probeToDescription.put(probe.intern(), "[No description]");
               }
            } else {
               probeToDescription.put(probe.intern(), "[No description]");
            }

            if (st.hasMoreTokens()) {
               classIds = st.nextToken();

               //another tokenizer is required since the ClassesID's are seperated by the | character
               StringTokenizer st1 = new StringTokenizer(classIds, "|");
               while (st1.hasMoreTokens()) {
                  go = st1.nextToken().intern();

                  // add this go to the probe->go map.
                  ((ArrayList) probeToClassMap.get(probe)).add(go);

                  // add this probe this go->probe map.
                  if (!classToProbeMap.containsKey(go)) {
                     classToProbeMap.put(go, new ArrayList());
                  }
                  ((ArrayList) classToProbeMap.get(go)).add(probe);
               }
            }
         }
      }

      /* Fill in the genegroupreader and the classmap */
      dis.close();
      probe_list = new Vector(probeGroupMap.keySet());
   }

   public Map getProbeGroupMap() {
      return probeGroupMap;
   }

   public Map getGroupProbeList() {
      return groupProbeList;
   }

   public Map getClassToProbeMap() {
      return classToProbeMap;
   }

   public Map getProbeToClassMap() {
      return probeToClassMap;
   }

   public String getProbeGeneName(String p) {
      return (String) probeToGeneName.get(p);
   }

   public String getProbeDescription(String p) {
      return (String) probeToDescription.get(p);
   }

   public ArrayList getGeneProbeList(String g) {
      return (ArrayList) groupProbeList.get(g);
   }

   public boolean classToProbeMapContains(String className) {
      return classToProbeMap.containsKey(className);
   }

   public void addClass(String id, ArrayList probes) {
      classToProbeMap.put(id, probes);

      Iterator probe_it = probes.iterator();
      while (probe_it.hasNext()) {
         String probe = new String((String) probe_it.next());
         ((ArrayList) probeToClassMap.get(probe)).add(id);
      }
   }

   /**
    * Add a gene to a class.
    * @param id String
    * @param probes ArrayList
    */
   public void modifyClass(String id, ArrayList probes) {
      ArrayList orig_probes = (ArrayList) classToProbeMap.get(id);
      Iterator orig_probe_it = orig_probes.iterator();
      while (orig_probe_it.hasNext()) {
         String orig_probe = new String((String) orig_probe_it.next());
         if (!probes.contains(orig_probe)) {
            HashSet ptc = new HashSet((Collection) probeToClassMap.get(
                    orig_probe));
            ptc.remove(id);
            probeToClassMap.remove(orig_probe);
            probeToClassMap.put(orig_probe, new ArrayList((Collection) ptc));
         }
      }
      Iterator probe_it = probes.iterator();
      while (probe_it.hasNext()) {
         String probe = (String) probe_it.next();
         if (!orig_probes.contains(probe)) {
            ((ArrayList) probeToClassMap.get(probe)).add(id);
         }
      }
      classToProbeMap.put(id, probes);
   }

   public TableModel toTableModel() {
      return new AbstractTableModel() {
         private String[] columnNames = {
                                        "Probe", "Gene", "Description"};

         public String getColumnName(int i) {
            return columnNames[i];
         }

         public int getColumnCount() {
            return 3;
         }

         public int getRowCount() {
            return getProbeGroupMap().size();
         }

         public Object getValueAt(int i, int j) {
            //  Collections.sort(probe_list);
            String probeid = (String) probe_list.get(i);
            switch (j) {
            case 0:
               return probeid;
            case 1:
               return (String) getProbeGeneName(probeid);
            case 2:
               return (String) getProbeDescription(probeid);
            default:
               return "";
            }
         }

      };
   };
}
