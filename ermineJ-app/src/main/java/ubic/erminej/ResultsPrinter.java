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

import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;

/**
 * @author pavlidis
 * @version $Id$
 */
public class ResultsPrinter {
    private static Log log = LogFactory.getLog( ResultsPrinter.class.getName() );
    protected String destFile;
    private final boolean saveAllGeneNames;
    private GeneSetPvalRun resultRun;
    private GeneAnnotations geneData;

    /**
     * @param destFile output file name
     * @param run Analysis run to be saved
     * @param goName GO information
     * @param saveAllGeneNames Whether the output should include all the genes
     */
    public ResultsPrinter( String destFile, GeneSetPvalRun run, boolean saveAllGeneNames ) {
        this.destFile = destFile;
        this.saveAllGeneNames = saveAllGeneNames;
        this.resultRun = run;
        this.geneData = resultRun.getGeneData();

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

        if ( resultRun == null ) {
            log.warn( "No results to print" );
            return;
        }

        Map<GeneSetTerm, GeneSetResult> results = this.resultRun.getResults();

        Writer out = getDesintation();

        boolean first = true;
        if ( sort ) {

            List<GeneSetResult> sortedResults = new ArrayList<GeneSetResult>( results.values() );
            Collections.sort( sortedResults );

            for ( GeneSetResult res : sortedResults ) {
                if ( first ) {
                    first = false;
                    res.printHeadings( out, "\tSame as" + "\tGeneMembers" );
                }
                print( out, res );
            }
        } else {
            // output them in alphabetical order. This is useful for testing.
            List<GeneSetTerm> c = new ArrayList<GeneSetTerm>( results.keySet() );
            Collections.sort( c );

            for ( GeneSetTerm t : c ) {
                GeneSetResult res = results.get( t );
                if ( first ) {
                    first = false;
                    res.printHeadings( out, "\tSame as" + "\tGenesMembers" );
                }
                print( out, res );
            }
        }
        out.close();

    }

    private void print( Writer out, GeneSetResult res ) throws IOException {
        res.print( out, "\t" + formatRedundantAndSimilar( res.getGeneSetId() ) + "\t"
                + ( this.saveAllGeneNames ? formatGeneNames( res.getGeneSetId() ) : "" ) + "\t" );
    }

    private Writer getDesintation() throws IOException {
        Writer out;
        if ( destFile == null ) {
            log.debug( "Writing results to STDOUT" );
            out = new BufferedWriter( new PrintWriter( System.out ) );
        } else {
            log.info( "Writing results to " + destFile );
            out = new BufferedWriter( new FileWriter( destFile, true ) ); // APPENDING
        }
        return out;
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
    private String formatGeneNames( GeneSetTerm className ) {
        if ( className == null ) return "";
        Collection<Gene> genes = this.geneData.getGeneSetGenes( className );
        if ( genes == null || genes.size() == 0 ) return "";
        List<Gene> sortedGenes = new ArrayList<Gene>( genes );
        Collections.sort( sortedGenes );
        StringBuffer buf = new StringBuffer();
        for ( Iterator<Gene> iter = sortedGenes.iterator(); iter.hasNext(); ) {
            Gene gene = iter.next();
            buf.append( gene.getSymbol() + "|" );
        }
        return buf.toString();
    }

    /**
     * Set up the string the way I want it.
     * 
     * @param classid String
     * @return String
     */
    private String formatRedundantAndSimilar( GeneSetTerm classid ) {
        Collection<GeneSet> redund = geneData.findGeneSet( classid ).getRedundantGroups();
        String return_value = "";
        if ( redund.isEmpty() ) {
            return return_value;
        }

        for ( GeneSet nextid : redund ) {
            return_value = return_value + nextid.getId() + "|" + nextid.getName() + ", ";
        }

        return return_value;

    }

}