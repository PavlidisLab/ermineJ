package classScore;

import java.util.*;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Institution:: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class ResultsPrinter {

   protected String dest_file;
   protected Vector sortedclasses;
   protected Map results;
   protected GONameReader goName;
   private ClassMap probeToClassMap;

   public ResultsPrinter(String dest_file, Vector sortedclasses, Map results, GONameReader goName, ClassMap probeToClassMap) {
      this.dest_file = dest_file;
      this.sortedclasses = sortedclasses;
      this.results = results;
      this.goName = goName;
      this.probeToClassMap = probeToClassMap;
   }


   /**
      Print the results
    */
   public void printResults() {
      this.printResults(false);
   }


   /**
        Print the results
        @param sort Sort the results so the best class (by score pvalue) is listed first.
      */
     public void printResults(boolean sort) {
        System.err.println("Beginning output");
        try {
           BufferedWriter out = new BufferedWriter(new FileWriter(dest_file, false));
           boolean first = true;
           classresult res = null;
           if (sort) {
              for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) {
                 res = (classresult) results.get(it.next());
                 if (first) {
                    first = false;
                    res.print_headings(out, "\tSame as:\tSimilar to:");
                 }
                 //		    res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
                 res.print(out, format_redundant_and_similar(res.getClassId()));
              }
           } else {
              for (Iterator it = results.entrySet().iterator(); it.hasNext(); ) {
                 res = (classresult) it.next();
                 if (first) {
                    first = false;
                    res.print_headings(out, "\tSame as:\tSimilar to:");
                 }
                 res.print(out, format_redundant_and_similar(res.getClassId()));
                 //		    res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
              }
           }
           out.close();
        } catch (IOException e) {
           System.err.println(
                   "There was an IO error while printing the results: " +
                   e);
        }
     }

     /**
      * Set up the string the way I want it.
      *
      * @param classid String
      * @return String
      */
     private String format_redundant_and_similar(String classid) {
        ArrayList redund = probeToClassMap.getRedundancies(classid);
        String return_value = "";

        if (redund != null) {
           Iterator it = redund.iterator();
           while (it.hasNext()) {
              String nextid = (String) it.next();
              String prefix;
              return_value = return_value + nextid + "|" +
                             goName.get_GoName_value_map(nextid) + ", ";
           }
        }

        return_value = return_value + "\t";

        ArrayList similar = probeToClassMap.getSimilarities(classid);

        if (similar != null) {
           Iterator it = similar.iterator();
           while (it.hasNext()) {
              String nextid = (String) it.next();
              String prefix;
              return_value = return_value + nextid + "|" +
                             goName.get_GoName_value_map(nextid) + ", ";
           }
           return "\t" + return_value;
        }

        return return_value;

     }


}
