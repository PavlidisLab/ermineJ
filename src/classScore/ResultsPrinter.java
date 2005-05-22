package classScore;

import java.io.BufferedWriter;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.bio.geneset.GeneSetMapTools;
import classScore.data.GeneSetResult;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class ResultsPrinter {
    private static Log log = LogFactory.getLog( ResultsPrinter.class.getName() );
    protected String destFile;
    protected Vector sortedclasses;
    protected Map results;
    protected GONames goName;
    protected GeneAnnotations geneData;
    private final boolean saveAllGeneNames;

    /**
     * @param destFile
     * @param run
     * @param goName
     */
    public ResultsPrinter( String destFile, GeneSetPvalRun run, GONames goName, boolean saveAllGeneNames ) {
        this.destFile = destFile;
        this.saveAllGeneNames = saveAllGeneNames;
        this.sortedclasses = run.getSortedClasses();
        this.results = run.getResults();
        this.geneData = run.getGeneData();
        this.goName = goName;
        if ( saveAllGeneNames ) log.debug( "Will save all genes" );
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
                    res.printHeadings( out, "\tSame as\tSimilar to\tGenes" );
                }
                // res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
                res.print( out, formatRedundantAndSimilar( res.getClassId() )
                        + ( this.saveAllGeneNames ? formatGeneNames( res.getClassId() ) : "" ) );
            }
        } else {
            // output them in natural order. This is useful for testing.
            List c = new ArrayList( results.keySet() );
            Collections.sort( c );
            for ( Iterator it = c.iterator(); it.hasNext(); ) {
                res = ( GeneSetResult ) results.get( it.next() );
                if ( first ) {
                    first = false;
                    res.printHeadings( out, "\tSame as:\tSimilar to:" );
                }
                res.print( out, formatRedundantAndSimilar( res.getClassId() )
                        + ( this.saveAllGeneNames ? formatGeneNames( res.getClassId() ) : "" ) );
                // res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
            }
        }
        out.close();

    }

    /**
     * @param className
     * @return
     */
    private String formatGeneNames( String className ) {
        if ( className == null ) return "";
        Collection genes = ( Collection ) this.geneData.getGeneSetToGeneMap().get( className );
        if ( genes == null || genes.size() == 0 ) return "";
        StringBuffer buf = new StringBuffer();
        for ( Iterator iter = genes.iterator(); iter.hasNext(); ) {
            String gene = ( String ) iter.next();
            buf.append( gene + "|" );
        }
        return buf.toString();
    }

    /**
     * Set up the string the way I want it.
     * 
     * @param classid String
     * @return String
     */
    private String formatRedundantAndSimilar( String classid ) {
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