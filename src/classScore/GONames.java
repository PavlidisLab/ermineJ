package classScore;

import java.io.IOException;
import java.util.*;

import baseCode.dataStructure.reader.MapReader;
/**
 * Gets geneclass names from tab-delimited file of the form GoID[tab]Biological Name. This
 *  is generic in that it doesn't have to be a GO, could be any class
 * scheme. The values are stored in a HashTable.  Created 09/02/02
 * <p>Copyright (c) 2004 Columbia University
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class GONames {

   private static Map goNameMap;
   private Set newGeneSets = new HashSet();

   /**
    *
    * @param filename <code>String</code> The tab-delimited file containing
    *   class to name mappings. First column is the class id, second is a
    *   description that will be used int program output.
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public GONames(String filename) throws IllegalArgumentException,
           IOException {

      if (filename == null || filename.length() == 0) {
         throw new IllegalArgumentException("Invalid filename or no filename was given");
      }
      MapReader m = new MapReader();
      goNameMap = m.read(filename, true);
   }

   /**
    *
    * @return Map
    */
   public Map getMap() {
      return goNameMap;
   }

   /**
    *
    * @param go_ID String
    * @return String
    */
   public String getNameForId(String go_ID) {
      return (String) (goNameMap.get(go_ID));
   }

   /**
    *
    * @param m Map
    */
   public void toString(Map m) {
      Collection entries = m.entrySet();
      Iterator it = entries.iterator();
      while (it.hasNext()) {
         Map.Entry e = (Map.Entry) it.next();
         System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
      }
   }

   /**
    *
    * @param id String
    * @param name String
    */
   public void addClass(String id, String name) {
      goNameMap.put(id, name);
      newGeneSets.add(id);
   }

   /**
    *
    * @param id String
    * @param name String
    */
   public void modifyClass(String id, String name) {
      goNameMap.put(id, name);
      newGeneSets.add(id);
   }

   public boolean newSet(String id) { return newGeneSets.contains(id); }
   public Set getNewGeneSets() { return newGeneSets; }
}
