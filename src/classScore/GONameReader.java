package classScore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
  Description:Parses the file of the form GoID Biological Name. This
  is generic in that it doesn't have to be a GO, could be any class
  scheme. The values are stored in a HashTable.  Created :09/02/02
  @author Shahmil Merchant, Paul Pavlidis
  @version $Id$
 */
public class GONameReader {

   /**
     Contains the map of class id -> text description.
    */
   private static Map go_name_map;

   /**
     @param filename <code>String</code> The tab-delimited file
     containing class to name mappings. First column is the class
     id, second is a description that will be used int program
     output.
    */
   public GONameReader(String filename) throws IllegalArgumentException,
           IOException {
      String aLine = null;
      String go_id = "";
      String go_name = "";
      //read in file
      File infile = new File(filename);
      if (!infile.exists() || !infile.canRead()) {
         System.err.println("Could not read " + filename);
         throw new IllegalArgumentException("Could not read " + filename);
      }

      go_name_map = new LinkedHashMap();

      FileInputStream fis = new FileInputStream(filename);
      BufferedInputStream bis = new BufferedInputStream(fis);
      BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
      String row;

      while ((row = dis.readLine()) != null) {
         //tokenize file by tab character
         StringTokenizer st = new StringTokenizer(row, "\t");
         go_id = st.nextToken().intern();
         if (go_id.equals("Gene_Ontology")) { // note: this should no longer occur in our data files.
            continue;
         }

         go_name = st.nextToken().intern();
         go_name_map.put(go_id, go_name);
      }
      dis.close();

   }

   /**
    */
   public Map get_GoName_map() {
      return go_name_map;
   }

   /**
    */
   public String get_GoName_value_map(String go_ID) {
      return (String) (go_name_map.get(go_ID));
   }

   /**
    */
   public void print(Map m) {
      Collection entries = m.entrySet();
      Iterator it = entries.iterator();
      while (it.hasNext()) {
         Map.Entry e = (Map.Entry) it.next();
         System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
      }
   }

   public void addClass(String id, String name) {
      go_name_map.put(id, name);
   }

   public void modifyClass(String id, String name) {
      go_name_map.put(id, name);
   }
}
