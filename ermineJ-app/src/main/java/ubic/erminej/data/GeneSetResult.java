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
package ubic.erminej.data;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;

/**
 * Data structure to store class scoring information about a class. This also knows how to print itself out.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetResult implements Comparable<GeneSetResult> {

    GeneSetTerm geneSetTerm;

    String runName = "";
    private double pvalue = 1.0;
    private double score = 0.0;
    private int numGenes = 0;
    private double correctedPvalue = 0.0;
    private double multifunctionality = 0.5;
    private int rank;

    private int numProbes;

    public GeneSetResult() {
        this( null, 0, 0, 0.0, 1.0, 1.0 );
    }

    /**
     * @param id
     * @param numProbes (in set)
     * @param numGenes (in set)
     */
    public GeneSetResult( GeneSetTerm id, int numProbes, int numGenes ) {
        this();
        this.geneSetTerm = id;
        this.setSizes( numProbes, numGenes );
    }

    /**
     * @param id
     * @param name
     * @param numProbes
     * @param numGenes
     * @param score
     * @param pvalue
     */
    public GeneSetResult( GeneSetTerm id, int numProbes, int numGenes, double score, double pvalue,
            double correctedPvalue ) {
        this.geneSetTerm = id;
        this.pvalue = pvalue;
        this.score = score;
        this.numProbes = numProbes;
        this.numGenes = numGenes;
        this.correctedPvalue = correctedPvalue;
    }

    /**
     * @param auc
     */
    public void setMultifunctionality( double auc ) {
        this.multifunctionality = auc;
    }

    public void print( Writer out ) throws IOException {
        this.print( out, "" );
    }

    /**
     * @param out
     * @param extracolumns
     * @throws IOException
     */
    public void print( Writer out, String extracolumns ) throws IOException {
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits( 8 );
        nf.setMinimumFractionDigits( 3 );

        DecimalFormat exp = new DecimalFormat( "0.###E00" );
        out.write( "!\t" + geneSetTerm.getName() + "\t" + geneSetTerm.getId() + "\t" + numProbes + "\t" + numGenes
                + "\t" + nf.format( score ) + "\t" + ( pvalue < 10e-3 ? exp.format( pvalue ) : nf.format( pvalue ) )
                + "\t" + ( correctedPvalue < 10e-3 ? exp.format( correctedPvalue ) : nf.format( correctedPvalue ) )
                + "\t" + String.format( "%.2f", this.multifunctionality ) + extracolumns + "\n" );
    }

    public void printHeadings( Writer out ) throws IOException {
        this.printHeadings( out, "" );
    }

    /**
     * @param out
     * @param extracolumns
     * @throws IOException
     */
    public void printHeadings( Writer out, String extracolumns ) throws IOException {
        out.write( "#\n#!" );
        out.write( "\tName\tID\tProbes\tNumGenes\tRawScore\tPval" + "\tCorrectedPvalue\tMultifuncBias" + extracolumns
                + "\n" );
    }

    public void setSizes( int size, int effsize ) {
        this.numProbes = size;
        this.numGenes = effsize;
    }

    public void setScore( double ascore ) {
        score = ascore;
    }

    public void setPValue( double apvalue ) {
        pvalue = apvalue;
    }

    public void setCorrectedPvalue( double a ) {
        correctedPvalue = a;
    }

    public GeneSetTerm getGeneSetId() {
        return this.geneSetTerm;
    }

    public double getPvalue() {
        return pvalue;
    }

    public double getScore() {
        return score;
    }

    /**
     * FIXME this is just the number of genes.
     * 
     * @return
     */
    public int getNumGenes() {
        return numGenes;
    }

    public int getRank() {
        return rank;
    }

    public void setRank( int n ) {
        rank = n;
    }

    public double getCorrectedPvalue() {
        return correctedPvalue;
    }

    /**
     * @return int
     */
    public int getNumProbes() {
        return numProbes;
    }

    /**
     * Default comparator for this class: sorts by the pvalue.
     * 
     * @param ob Object
     * @return int
     */
    public int compareTo( GeneSetResult other ) {

        if ( other == null ) return 1;

        if ( this.pvalue > other.pvalue ) {
            return 1;
        } else if ( this.pvalue < other.pvalue ) {
            return -1;
        } else {
            return 0;
        }
    }

}