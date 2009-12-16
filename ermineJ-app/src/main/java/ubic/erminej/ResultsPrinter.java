/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.bio.geneset.GeneSetMapTools;
import ubic.erminej.data.GeneSetResult;

/**
 * @author pavlidis
 * @version $Id$
 */
public class ResultsPrinter {
    private static Log log = LogFactory.getLog( ResultsPrinter.class.getName() );
    protected String destFile;
    protected List<String> sortedclasses;
    protected Map<String, GeneSetResult> results;
    protected GONames goName;
    protected GeneAnnotations geneData;
    private final boolean saveAllGeneNames;

    /**
     * @param destFile output file name
     * @param run Analysis run to be saved
     * @param goName GO information
     * @param saveAllGeneNames Whether the output should include all the genes
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

        if ( results == null ) {
            log.warn( "No results to print" );
            return;
        }

        if ( sortedclasses == null ) {
            log.warn( "No genes sets" );
            return;
        }

        Writer out;
        if ( destFile == null ) {
            log.debug( "Writing results to STDOUT" );
            out = new BufferedWriter( new PrintWriter( System.out ) );
        } else {
            log.info( "Writing results to " + destFile );
            out = new BufferedWriter( new FileWriter( destFile, true ) ); // APPENDING
        }
        boolean first = true;
        GeneSetResult res = null;
        if ( sort ) {
            // in order of best score.
            for ( Iterator<String> it = sortedclasses.iterator(); it.hasNext(); ) {
                res = results.get( it.next() );
                if ( first ) {
                    first = false;
                    res.printHeadings( out, "\tSame as" + "\tGeneMembers" );
                }
                // res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
                res.print( out, "\t" + formatRedundantAndSimilar( res.getGeneSetId() ) + "\t"
                        + ( this.saveAllGeneNames ? formatGeneNames( res.getGeneSetId() ) : "" ) + "\t" );
            }
        } else {
            // output them in natural order. This is useful for testing.
            List<String> c = new ArrayList<String>( results.keySet() );
            Collections.sort( c );
            for ( Iterator<String> it = c.iterator(); it.hasNext(); ) {
                res = results.get( it.next() );
                if ( first ) {
                    first = false;
                    res.printHeadings( out, "\tSame as" + "\tGenesMembers" );
                }
                res.print( out, "\t" + formatRedundantAndSimilar( res.getGeneSetId() ) + "\t"
                        + ( this.saveAllGeneNames ? formatGeneNames( res.getGeneSetId() ) : "" ) + "\t" );
                // res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
            }
        }
        out.close();

    }

    /**
     * @param destFile
     */
    public void setDestFile( String destFile ) {
        this.destFile = destFile;
    }

    /**
     * @param className
     * @return
     */
    private String formatGeneNames( String className ) {
        if ( className == null ) return "";
        Collection<String> genes = this.geneData.getActiveGeneSetGenes( className );
        if ( genes == null || genes.size() == 0 ) return "";
        List<String> sortedGenes = new ArrayList<String>( genes );
        Collections.sort( sortedGenes );
        StringBuffer buf = new StringBuffer();
        for ( Iterator<String> iter = sortedGenes.iterator(); iter.hasNext(); ) {
            String gene = iter.next();
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
        Collection<String> redund = GeneSetMapTools.getRedundancies( classid, geneData.geneSetToRedundantMap() );
        String return_value = "";
        if ( redund != null ) {
            Iterator<String> it = redund.iterator();
            while ( it.hasNext() ) {
                String nextid = it.next();
                return_value = return_value + nextid + "|" + goName.getNameForId( nextid ) + ", ";
            }
        }

        return return_value;

    }

}