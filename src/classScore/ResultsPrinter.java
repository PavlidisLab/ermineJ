package classScore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.bio.geneset.GeneSetMapTools;
import classScore.data.GeneSetResult;

/**
 * 
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004-2005 Columbia University
 * @author pavlidis
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
    public ResultsPrinter( String destFile, Vector sortedclasses, Map results, GONames goName ) {
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
     * @throws IOException Print the results
     */
    public void printResults() throws IOException {
        this.printResults( false );
    }

    /**
     * Print the results
     * 
     * @param sort Sort the results so the best class (by score pvalue) is listed first.
     */
    public void printResults( boolean sort ) throws IOException {

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
                // res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
                res.print( out, format_redundant_and_similar( res.getClassId() ) );
            }
        } else {
            // output them in natural order. This is useful for testing.
            List c = new ArrayList( results.keySet() );
            Collections.sort( c );
            for ( Iterator it = c.iterator(); it.hasNext(); ) {
                res = ( GeneSetResult ) results.get( it.next() );
                if ( first ) {
                    first = false;
                    res.print_headings( out, "\tSame as:\tSimilar to:" );
                }
                res.print( out, format_redundant_and_similar( res.getClassId() ) );
                // res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
            }
        }
        out.close();

    }

    /**
     * Set up the string the way I want it.
     * 
     * @param classid String
     * @return String
     */
    private String format_redundant_and_similar( String classid ) {
        List redund = GeneSetMapTools.getRedundancies( classid, geneData.geneSetToRedundantMap() );
        String return_value = "";
        if ( redund != null ) {
            Iterator it = redund.iterator();
            while ( it.hasNext() ) {
                String nextid = ( String ) it.next();
                return_value = return_value + nextid + "|" + goName.getNameForId( nextid ) + ", ";
            }
        }
        return_value = return_value + "\t";
        return return_value;

    }

}