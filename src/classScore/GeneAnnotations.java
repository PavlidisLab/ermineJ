package classScore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *  Reads tab-delimited file to create maps of probes to classes, classes to probes, probes to genes, genes to probes.
 *<p>
 * Maintains the following important data structures, all derived from the input file:
 *
 * <pre>
 * probe->Classes -- each value is a Set of the Classes that a probe belongs to.
 * Classes->probe -- each value is a Set of the probes that belong to a class
 * probe->gene -- each value is the gene name corresponding to the probe.
 * gene->list of probes -- each value is a list of probes corresponding to a gene
 * probe->description -- each value is a text description of the probe (actually...of the gene)
 *</pre>
 *
 * <p>Copyright (c) 2004 Columbia University</p>
 * @author Paul Pavlidis, Shamhil Merchant, Homin Lee
 * @version $Id$
 */

public class GeneAnnotations {
   private Map probeToClassMap; //stores probe->Classes map
   private Map classToProbeMap; //stores Classes->probe map
   private Map probeToGeneName;
   private Map probeToDescription;
   private Map geneToProbeList;
   private Vector probeList;
   private Map probes; /** @todo this should be a Set? */
   private Vector sortedGeneSets;

   /**
    *
    * @param filename String
    * @param probes Map
    * @throws IOException
    */
   public GeneAnnotations(String filename, Map probes) throws IOException {
      if (probes != null) {
         this.probes = probes;
      }
      probeToClassMap = new LinkedHashMap();
      classToProbeMap = new LinkedHashMap();
      probeToGeneName = new HashMap();
      probeToDescription = new HashMap();
      geneToProbeList = new HashMap();
      this.readFile(filename);
   }

   /**
    *
    * @param filename String
    * @throws IOException
    */
   public GeneAnnotations(String filename) throws IOException {
      this(filename, null);
   }

//read in file
   private void readFile(String filename) throws IOException {
      File infile = new File(filename);
      if (!infile.exists() || !infile.canRead()) {
         throw new IOException("Could not read from " + filename);
      }

      FileInputStream fis = new FileInputStream(filename);

      BufferedInputStream bis = new BufferedInputStream(fis);
      BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

      ArrayList probeIds = new ArrayList();
      String classIds = null;

      // loop through rows. Makes hash map of probes to go, and map of go to probes.
      String line = "";
      while ((line = dis.readLine()) != null) {
         StringTokenizer st = new StringTokenizer(line, "\t");

         String probe = st.nextToken().intern();
         if (probes == null || probes.containsKey(probe)) { // only do probes we have in the data set.

            /* read gene name */
            String group = st.nextToken().intern();
            probeToGeneName.put(probe.intern(), group.intern());

            // create the list if need be.
            if (geneToProbeList.get(group) == null) {
               geneToProbeList.put(group.intern(), new ArrayList());
            }
            ((ArrayList) geneToProbeList.get(group)).add(probe.intern());

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
                  String go = st1.nextToken().intern();

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
      probeList = new Vector(probeToGeneName.keySet());
   }

   /**
    *
    * @return Map
    */
   public Map getProbeToGeneMap() {
      return probeToGeneName;
   }

   /**
    *
    * @return Map
    */
   public Map getGeneToProbeList() {
      return geneToProbeList;
   }

   /**
    *
    * @return Map
    */
   public Map getClassToProbeMap() {
      return classToProbeMap;
   }

   /**
    *
    * @param Map
    */
   public void sortGeneSets() {
      sortedGeneSets = new Vector(classToProbeMap.entrySet().size());
      Set keys = classToProbeMap.keySet();
      Vector l = new Vector();
      l.addAll(keys);
      Collections.sort(l);
      Iterator it = l.iterator();
      while (it.hasNext()) {
         sortedGeneSets.add(it.next());
      }
   }

   /**
    *
    * @return Map
    */
   public Map getProbeToClassMap() {
      return probeToClassMap;
   }

   /**
    * Get the gene that a probe belongs to.
    * @param p String
    * @return String
    */
   public String getProbeGeneName(String p) {
      return (String) probeToGeneName.get(p);
   }

   /**
    * Get the description for a gene.
    * @param p String
    * @return String
    */
   public String getProbeDescription(String p) {
      return (String) probeToDescription.get(p);
   }

   /**
    * Get a list of the probes that correspond to a particular gene.
    * @param g String a gene name
    * @return ArrayList list of the probes for gene g
    */
   public ArrayList getGeneProbeList(String g) {
      return (ArrayList) geneToProbeList.get(g);
   }

   public int numClasses() {
      return sortedGeneSets.size();
   }

   public String getClass(int i) {
      return (String) sortedGeneSets.get(i);
   }

   public int numProbes(String id) {
      return ((ArrayList) classToProbeMap.get(id)).size();
   }

   public int numGenes(String id) {
      HashSet genes = new HashSet();
      ArrayList probes = (ArrayList) classToProbeMap.get(id);
      Iterator probe_it = probes.iterator();
      while (probe_it.hasNext()) {
         genes.add(probeToGeneName.get((String) probe_it.next()));
      }
      return genes.size();
   }

   /**
    * Find out if a class contains a given probe
    * @param className String
    * @return boolean
    */
   public boolean classContainsProbe(String className) {
      return classToProbeMap.containsKey(className);
   }

   /**
    * Add a class
    * @param id String class to be added
    * @param probes ArrayList user-defined list of members.
    */
   public void addClass(String id, ArrayList probes) {
      classToProbeMap.put(id, probes);

      Iterator probe_it = probes.iterator();
      while (probe_it.hasNext()) {
         String probe = new String((String) probe_it.next());
         ((ArrayList) probeToClassMap.get(probe)).add(id);
      }
   }

   /**
    * Redefine a class.
    * @param id String class to be modified
    * @param probes ArrayList current user-defined list of members.
    * The "real" version of the class is modified to look like this one.
    */
   public void modifyClass(String classId, ArrayList probes) {
      ArrayList orig_probes = (ArrayList) classToProbeMap.get(classId);
      Iterator orig_probe_it = orig_probes.iterator();
      while (orig_probe_it.hasNext()) {
         String orig_probe = new String((String) orig_probe_it.next());
         if (!probes.contains(orig_probe)) {
            HashSet ptc = new HashSet((Collection) probeToClassMap.get(
                    orig_probe));
            ptc.remove(classId);
            probeToClassMap.remove(orig_probe);
            probeToClassMap.put(orig_probe, new ArrayList((Collection) ptc));
         }
      }
      Iterator probe_it = probes.iterator();
      while (probe_it.hasNext()) {
         String probe = (String) probe_it.next();
         if (!orig_probes.contains(probe)) {
            ((ArrayList) probeToClassMap.get(probe)).add(classId);
         }
      }
      classToProbeMap.put(classId, probes);
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
            return getProbeToGeneMap().size();
         }

         public Object getValueAt(int i, int j) {
            //  Collections.sort(probe_list);
            String probeid = (String) probeList.get(i);
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
