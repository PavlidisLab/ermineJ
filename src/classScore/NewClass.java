package classScore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NewClass {
   modClassFrame outerframe;
   String id;
   String desc;
   ArrayList probes;

   public NewClass(modClassFrame outerframe) {
      this.outerframe = outerframe;
      id = new String();
      desc = new String();
      probes = new ArrayList();
   }

   public void clear() {
      id = "";
      desc = "";
      probes.clear();
   }

   public AbstractTableModel toTableModel(boolean editable) {
      final boolean finalized = editable;

      return new AbstractTableModel() {

         private String[] columnNames = {"Probe", "Gene", "Description"};

         public String getColumnName(int i) {return columnNames[i];
         }

         public int getRowCount() {
            int windowrows;
            if (finalized) {
               windowrows = 11;
            } else {
               windowrows = 8;
            }
            int extra = 1;
            if (probes.size() < windowrows) {
               extra = windowrows - probes.size();
            }
            return probes.size() + extra;
         }

         public int getColumnCount() {
            return 3;
         }

         public Object getValueAt(int r, int c) {
            if (r < probes.size()) {
               String probeid = (String) probes.get(r);
               GeneDataReader geneData = outerframe.imaps.geneData;
               switch (c) {
               case 0:
                  return probeid;
               case 1:
                  return geneData.getProbeGeneName(probeid);
               case 2:
                  return geneData.getProbeDescription(probeid);
               default:
                  return "";
               }
            } else {
               return "";
            }
         }

         public boolean isCellEditable(int r, int c) {
            if (!finalized && (c == 0 || c == 1)) {
               return true;
            } else {
               return false;
            }
         }
      };
   }

   public void loadClassFile(String file) {
      clear();
      File infile = new File(file);
      if (!infile.exists() || !infile.canRead()) {
         outerframe.error("Could not find file: " + file);
      } else {
         try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
            String row;
            ArrayList genes = new ArrayList();
            String type = new String("");
            int filetype = 0;
            while ((row = dis.readLine()) != null) {
               if (type.compareTo("") == 0) {
                  type = row;
                  if (type.compareTo("probe") == 0) {
                     filetype = 0;
                  } else if (type.compareTo("gene") == 0) {
                     filetype = 1;
                  }
               } else if (id.compareTo("") == 0) {
                  id = row;
               } else if (desc.compareTo("") == 0) {
                  desc = row;
               } else {
                  if (filetype == 0) {
                     probes.add(row);
                  } else if (filetype == 1) {
                     genes.add(row);
                  }
               }
            }
            dis.close();
            if (filetype == 1) {
               HashSet probeSet = new HashSet();
               for (Iterator it = genes.iterator(); it.hasNext(); ) {
                  probeSet.addAll(outerframe.imaps.geneData.getGeneProbeList((
                          String) it.next()));
               }
               probes = new ArrayList(probeSet);
            }
         } catch (IOException ioe) {
            outerframe.error("Could not find file: " + ioe);
         }
      }

   }

   public void saveClass(String folder, int type) {
      try {
         String fileid = id.replace(':', '-');
         String filedesc = desc.replace('\n', ' ');
         String filetype = (type == 0) ? "probe" : "gene";
         BufferedWriter out = new BufferedWriter(new FileWriter(folder +
                 "classes" + File.separatorChar + fileid + "-class.txt", false));
         out.write(filetype + "\n");
         out.write(id + "\n");
         out.write(filedesc + "\n");
         for (Iterator it = probes.iterator(); it.hasNext(); ) {
            out.write((String) it.next() + "\n");
         }
         out.close();
      } catch (IOException e) {
         System.err.println(
                 "There was an IO error while printing the results: " + e);
      }
   }

   public static HashMap getClassFileInfo(String file) {
      HashMap cinfo = new HashMap();
      File infile = new File(file);
      if (!infile.exists() || !infile.canRead()) {
         System.err.println("Could not find file: " + file);
      } else {
         try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
            String row;
            ArrayList members = new ArrayList();
            while ((row = dis.readLine()) != null) {
               if (!cinfo.containsKey("type")) {
                  cinfo.put("type", row);
               } else if (!cinfo.containsKey("id")) {
                  cinfo.put("id", row);
               } else if (!cinfo.containsKey("desc")) {
                  cinfo.put("desc", row);
               } else {
                  members.add(row);
               }
            }
            cinfo.put("members", members);
            dis.close();
         } catch (IOException ioe) {
            System.err.println("Could not find file: " + ioe);
         }
      }
      return cinfo;
   }
}
