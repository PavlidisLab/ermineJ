package classScore;

import java.io.*;
import java.util.*;
import classScore.data.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ResultsFileReader {

   private Map results;

   public ResultsFileReader(String filename) {
      results = new LinkedHashMap();

      File infile = new File(filename);
      if (!infile.exists() || !infile.canRead()) {
         System.err.println("Could not read " + filename);
      }

      try {
         FileInputStream fis = new FileInputStream(filename);

         BufferedInputStream bis = new BufferedInputStream(fis);
         BufferedReader dis = new BufferedReader(new InputStreamReader(bis));

         String line;
         line = dis.readLine(); // ditch the header.
         while ( (line = dis.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, "\t");

            String classNameMunged = st.nextToken();
            String className = st.nextToken();
            String classId = st.nextToken();
            int size = Integer.parseInt(st.nextToken());
            int effsize = Integer.parseInt(st.nextToken());
            double score = Double.parseDouble(st.nextToken());
            double pval = Double.parseDouble(st.nextToken());
            int hypercut = Integer.parseInt(st.nextToken());
            double hyperpval = Double.parseDouble(st.nextToken());

            GeneSetResult c = new GeneSetResult(classId, className, size, effsize, score, pval,
                                            hyperpval, 0.5, 1.0);
            results.put(classId, c);

         }

      }
      catch (FileNotFoundException ex) {

      }
      catch (IOException ex) {
      }
      System.err.println(results.size() + " class results read from file");
   }

   public Map getResults() {
      return results;
   }

}
