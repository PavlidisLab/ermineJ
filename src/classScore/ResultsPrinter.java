package classScore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import classScore.data.GeneSetResult;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.bio.geneset.GeneSetMapTools;
import baseCode.gui.GuiUtil;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Institution:: Columbia University
 * </p>
 * 
 * @author Pavlidis
 * @version $Id$
 */

public class ResultsPrinter {

   protected String destFile;
   protected Vector sortedclasses;
   protected Map results;
   protected GONames goName;
   protected GeneAnnotations geneData;

   /**
    * @param destFile
    * @param sortedclasses
    * @param results
    * @param goName
    */
   public ResultsPrinter( String destFile, Vector sortedclasses, Map results,
         GONames goName ) {
      this.destFile = destFile;
      this.sortedclasses = sortedclasses;
      this.results = results;
      this.goName = goName;
   }

   /**
    * @param destFile
    * @param run
    * @param goName
    */
   public ResultsPrinter( String destFile, GeneSetPvalRun run, GONames goName ) {
      this.destFile = destFile;
      this.sortedclasses = run.getSortedClasses();
      this.results = run.getResults();
      this.geneData = run.getGeneData();
      this.goName = goName;
   }

   /**
    * @return
    */
   public String getDestFile() {
      return destFile;
   }

   /**
    * @param destFile
    */
   public void setDestFile( String destFile ) {
      this.destFile = destFile;
   }

   /**
    * Print the results
    */
   public void printResults() {
      this.printResults( false );
   }

   /**
    * Print the results
    * 
    * @param sort Sort the results so the best class (by score pvalue) is listed first.
    */
   public void printResults( boolean sort ) {
      try {
         BufferedWriter out;
         if ( destFile == null ) {
            out = new BufferedWriter( new PrintWriter( System.out ) );
         } else {
            out = new BufferedWriter( new FileWriter( destFile, true ) );
         }
         boolean first = true;
         GeneSetResult res = null;
         if ( sort ) {
            // in order of best score.
            for ( Iterator it = sortedclasses.iterator(); it.hasNext(); ) {
               res = ( GeneSetResult ) results.get( it.next() );
               if ( first ) {
                  first = false;
                  res.print_headings( out, "\tSame as:\tSimilar to:" );
               }
               //		    res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
               res
                     .print( out, format_redundant_and_similar( res
                           .getClassId() ) );
            }
         } else {
            // output them in natural order. This is useful for testing.
            List c = new ArrayList(results.keySet());
            Collections.sort(c);
            for ( Iterator it = c.iterator(); it.hasNext(); ) {
               res = ( GeneSetResult ) results.get( it.next() );
               if ( first ) {
                  first = false;
                  res.print_headings( out, "\tSame as:\tSimilar to:" );
               }
               res
                     .print( out, format_redundant_and_similar( res
                           .getClassId() ) );
               //		    res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
            }
         }
         out.close();
      } catch ( IOException e ) {
         GuiUtil
               // todo this shouldn't be here!
               .error( "Unable to write to file "
                     + destFile
                     + "\n"
                     + "Please make sure the named file is not open in another application.\n"
                     + "If this problem persists, please contact the software vendor." );
      }
   }

   /**
    * Set up the string the way I want it.
    * 
    * @param classid String
    * @return String
    */
   private String format_redundant_and_similar( String classid ) {
      ArrayList redund = GeneSetMapTools.getRedundancies( classid, geneData
            .geneSetToRedundantMap() ); //commented just to compile (Homin)
      String return_value = "";
      if ( redund != null ) {
         Iterator it = redund.iterator();
         while ( it.hasNext() ) {
            String nextid = ( String ) it.next();
            return_value = return_value + nextid + "|"
                  + goName.getNameForId( nextid ) + ", ";
         }
      }
      return_value = return_value + "\t";
      /*
       * ArrayList similar = probeToClassMap.getSimilarities(classid); //commented just to compile (Homin) if (similar !=
       * null) { Iterator it = similar.iterator(); while (it.hasNext()) { String nextid = (String) it.next(); String
       * prefix; return_value = return_value + nextid + "|" + goName.getNameForId(nextid) + ", "; } return "\t" +
       * return_value; }
       */
      return return_value;

   }

}