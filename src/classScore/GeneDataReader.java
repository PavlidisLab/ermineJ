package classScore;

import java.io.*;
import java.util.*;
import javax.swing.table.*;

/**
 * <p>Title: </p>
 * <p>Description: Reads tab-delimited file. Replaces file reading functions of ClassMap and GeneGroupReader.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class GeneDataReader {
   private Map probeToClassMap; //stores probe->Classes map
   private Map classToProbeMap; //stores Classes->probe map
   private Map probeToGeneName; // same as probeGroupMap
   private Map probeToDescription;
   private Map probeGroupMap;
   private Map groupProbeList;
   private Set group;
   private String filename;
   private Map probes;

   public GeneDataReader(String filename, Map probes) {
      this.filename = filename;
      this.probes = probes;
      probeToClassMap = new LinkedHashMap();
      classToProbeMap = new LinkedHashMap();
//    probeGroupMap = new HashMap();
      probeToGeneName = new HashMap();
      probeGroupMap = probeToGeneName;
      probeToDescription = new HashMap();
      groupProbeList = new HashMap();
      this.readFile();
   }

//read in file
   private void readFile() {
      try {

         File infile = new File(filename);
         if (!infile.exists() || !infile.canRead()) {
            System.err.println("Could not read " + filename);
         }

         FileInputStream fis = new FileInputStream(filename);

         BufferedInputStream bis = new BufferedInputStream(fis);
         BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
         Vector rows = new Vector();
         Vector cols = null;

         ArrayList probeIds = new ArrayList();
         String classIds = null;
         String go = "";

         // loop through rows. Makes hash map of probes to go, and map of go to probes.
         String line = "";
         while ( (line = dis.readLine()) != null) {
            //tokenize file by tab character
            StringTokenizer st = new StringTokenizer(line, "\t");

            // create a new Vector for each row's columns
            String probe = st.nextToken();
            if (probes.containsKey(probe)) { // only do probes we have in the data set.

               /* read gene name */
               String group = st.nextToken();
               //        probeGroupMap.put(probe, group); // probe -> group
               probeToGeneName.put(probe, group); // redundant... todo: remove one of these.

               // create the list if need be.
               if (groupProbeList.get(group) == null) {
                  groupProbeList.put(group, new ArrayList());
               }
               ( (ArrayList) groupProbeList.get(group)).add(probe);

               probeIds.add(probe);
               probeToClassMap.put(probe, new ArrayList());

               /* read gene description */
               if (st.hasMoreTokens()) {
                  String description = st.nextToken();
                  probeToDescription.put(probe, description);
               }

               if (st.hasMoreTokens()) {
                  classIds = st.nextToken();

                  //another tokenizer is required since the ClassesID's are seperated by the | character
                  StringTokenizer st1 = new StringTokenizer(classIds, "|");
                  while (st1.hasMoreTokens()) {
                     go = st1.nextToken();

                     // add this go to the probe->go map.
                     ( (ArrayList) probeToClassMap.get(probe)).add(go);

                     // add this probe this go->probe map.
                     if (!classToProbeMap.containsKey(go)) {
                        classToProbeMap.put(go, new ArrayList());
                     }
                     ( (ArrayList) classToProbeMap.get(go)).add(probe);
                  }
               }
            }
         }

         /* Fill in the genegroupreader and the classmap */
         dis.close();
      }
      catch (IOException e) {
         // catch possible io errors from readLine()
         System.out.println("IOException error!");
         e.printStackTrace();
      }
      catch (RuntimeException e) {
         e.printStackTrace();
      }
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

   public void addClass(String id, ArrayList probes)
   {
      classToProbeMap.put(id,probes);

      Iterator probe_it=probes.iterator();
      while(probe_it.hasNext())
      {
         String probe = new String((String) probe_it.next());
         ((ArrayList)probeToClassMap.get(probe)).add(id);
      }
   }

   public void modifyClass(String id, ArrayList probes)
   {
      ArrayList orig_probes=(ArrayList)classToProbeMap.get(id);
      Iterator orig_probe_it=orig_probes.iterator();
      while(orig_probe_it.hasNext())
      {
         String orig_probe = new String((String) orig_probe_it.next());
         if(!probes.contains(orig_probe))
         {
            HashSet ptc = new HashSet((Collection)probeToClassMap.get(orig_probe));
            ptc.remove(id);
            probeToClassMap.remove(orig_probe);
            probeToClassMap.put(orig_probe,new ArrayList((Collection)ptc));
         }
      }
      Iterator probe_it=probes.iterator();
      while(probe_it.hasNext())
      {
         String probe = (String) probe_it.next();
         if(!orig_probes.contains(probe))
            ((ArrayList)probeToClassMap.get(probe)).add(id);
      }
      classToProbeMap.put(id,probes);
   }

   public TableModel toTableModel()
   {
      return new AbstractTableModel()
      {
         private String[] columnNames = {"Probe", "Gene", "Description"};

         public String getColumnName(int i) { return columnNames[i]; }

         public int getColumnCount() { return 3; }

         public int getRowCount() { return getProbeGroupMap().size(); }

         public Object getValueAt(int i, int j)
         {
            Map pgmap=getProbeGroupMap();
            Vector probe_list=new Vector(pgmap.keySet());
            Collections.sort(probe_list);
            String probeid = (String) probe_list.get(i);
            String probegroup = (String) getProbeGeneName(probeid);
            String description = (String) getProbeDescription(probeid);
            switch (j)
            {
               case 0:
                  return probeid;
               case 1:
                  return probegroup;
               case 2:
                  return description;
               default:
                   return "";
             }
         }

      };
   };


   public static void main(String[] args) {
   }
}
