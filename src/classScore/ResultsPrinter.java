package classScore;

import java.util.*;
import java.io.*;
import classScore.data.*;

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
   protected GONames goName;
   protected GeneAnnotations geneData;

   public ResultsPrinter(String dest_file, Vector sortedclasses, Map results, GONames goName) {
      this.dest_file = dest_file;
      this.sortedclasses = sortedclasses;
      this.results = results;
      this.goName = goName;
   }

   public ResultsPrinter(String dest_file, classPvalRun run, GONames goName) {
      this.dest_file = dest_file;
      this.sortedclasses = run.getSortedClasses();
      this.results = run.getResults();
      this.geneData = run.getGeneData();
      this.goName = goName;
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
           BufferedWriter out = new BufferedWriter(new FileWriter(dest_file, true));
           boolean first = true;
           GeneSetResult res = null;
           if (sort) {
              for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) {
                 res = (GeneSetResult) results.get(it.next());
                 if (first) {
                    first = false;
                    res.print_headings(out, "\tSame as:\tSimilar to:");
                 }
                 //		    res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
                 res.print(out, format_redundant_and_similar(res.getClassId()));
              }
           } else {
              for (Iterator it = results.entrySet().iterator(); it.hasNext(); ) {
                 System.err.println(it.next().getClass().toString());
                 res = (GeneSetResult) it.next();
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
        ArrayList redund = GeneSetMapTools.getRedundancies(classid,geneData.getClassesToRedundantMap());    //commented just to compile (Homin)
        String return_value = "";
        if (redund != null) {
           Iterator it = redund.iterator();
           while (it.hasNext()) {
              String nextid = (String) it.next();
              String prefix;
              return_value = return_value + nextid + "|" +
                             goName.getNameForId(nextid) + ", ";
           }
        }
        return_value = return_value + "\t";
/*
        ArrayList similar = probeToClassMap.getSimilarities(classid);           //commented just to compile (Homin)

        if (similar != null) {
           Iterator it = similar.iterator();
           while (it.hasNext()) {
              String nextid = (String) it.next();
              String prefix;
              return_value = return_value + nextid + "|" +
                             goName.getNameForId(nextid) + ", ";
           }
           return "\t" + return_value;
        }
*/
        return return_value;

     }


}
