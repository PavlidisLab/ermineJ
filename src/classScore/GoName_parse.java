package classScore;

import util.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;

/**
  Description:Parses the file of the form GoID Biological Name. This
  is generic in that it doesn't have to be a GO, could be any class
  scheme. The values are stored in a HashTable.  Created :09/02/02
  @author Shahmil Merchant, Paul Pavlidis
  @version $Id$
 */
public class GoName_parse {

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
  public GoName_parse(String filename) {
    String aLine = null;
    String go_id = "";
    String go_name = "";
    //read in file
    File infile = new File(filename);
    if (!infile.exists() || !infile.canRead()) {
      System.err.println("Could not read " + filename);
    }

    go_name_map = new LinkedHashMap();

    try {
      FileInputStream fis = new FileInputStream(filename);
      BufferedInputStream bis = new BufferedInputStream(fis);
      BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
      String row;

      while ( (row = dis.readLine()) != null) {
        //tokenize file by tab character
        StringTokenizer st = new StringTokenizer(row, "\t");
        go_id = st.nextToken();
        if (go_id.equals("Gene_Ontology")) { // note: this should no longer occur in our data files.
          continue;
        }

        go_name = st.nextToken();
        go_name_map.put(go_id, go_name);
      }
      dis.close();

    }
    catch (IOException e) {
      // catch possible io errors from readLine()
      System.err.println(" IOException error!");
      e.printStackTrace();
    }
    catch (NoSuchElementException e) {
      System.err.println(" No such element error! GOID:" + go_id + " NAME:" +
                         go_name);
      e.printStackTrace();
    }

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

}
