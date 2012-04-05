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

import ubic.erminej.SettingsHolder;

/**
 * Data structure to store class scoring information about a class. This also knows how to print itself out.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetResult implements Comparable<GeneSetResult> {

    protected GeneSetTerm geneSetTerm = null;

    private double pvalue = 1.0;
    private double score = 0.0;
    private int numGenes = 0;
    private double correctedPvalue = 1.0;
    private double multifunctionality = 0.5;
    private int rank = -12345;

    private SettingsHolder settings;

    public SettingsHolder getSettings() {
        return settings;
    }

    private int multifunctionalityCorrectedRankDelta = -12345;

    private int numProbes = 0;

    private double relativeRank = 1.0;

    public GeneSetResult() {
        this( null, 0, 0, 0.0, 1.0, 1.0, null );
    }

    /**
     * @param id
     * @param numProbes (in set)
     * @param numGenes (in set)
     */
    public GeneSetResult( GeneSetTerm id, int numProbes, int numGenes, SettingsHolder settings ) {
        this();
        this.geneSetTerm = id;
        this.settings = settings;
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
            double correctedPvalue, SettingsHolder settings ) {
        this.geneSetTerm = id;
        this.pvalue = pvalue;
        this.score = score;
        this.numProbes = numProbes;
        this.numGenes = numGenes;
        this.correctedPvalue = correctedPvalue;
        this.settings = settings;
    }

    /**
     * Default comparator for this class: sorts by the pvalue.
     * 
     * @param ob Object
     * @return int
     */
    public int compareTo( GeneSetResult other ) {

        if ( other == null ) return -1;

        if ( other instanceof EmptyGeneSetResult ) return -1;

        if ( this.equals( other ) ) return 0;

        if ( this.pvalue > other.pvalue ) {
            return 1;
        } else if ( this.pvalue < other.pvalue ) {
            return -1;
        } else {
            // break ties alphabetically.
            return this.geneSetTerm.compareTo( other.geneSetTerm );
        }
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        GeneSetResult other = ( GeneSetResult ) obj;
        if ( geneSetTerm == null ) {
            if ( other.geneSetTerm != null ) return false;
        } else if ( !geneSetTerm.equals( other.geneSetTerm ) ) return false;
        return true;
    }

    public double getCorrectedPvalue() {
        return correctedPvalue;
    }

    public GeneSetTerm getGeneSetId() {
        return this.geneSetTerm;
    }

    public int getMultifunctionalityCorrectedRankDelta() {
        return multifunctionalityCorrectedRankDelta;
    }

    /**
     * @return
     */
    public int getNumGenes() {
        return numGenes;
    }

    /**
     * @return int
     */
    public int getNumProbes() {
        return numProbes;
    }

    public double getPvalue() {
        return pvalue;
    }

    /**
     * Low numbers are better ranks.
     * 
     * @return
     */
    public int getRank() {
        return rank;
    }

    public double getRelativeRank() {
        return relativeRank;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( geneSetTerm == null ) ? 0 : geneSetTerm.hashCode() );
        return result;
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
                + "\t" + ( this.multifunctionalityCorrectedRankDelta ) + "\t"
                + String.format( "%.3g", this.multifunctionality ) + extracolumns + "\n" );
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
        out.write( "\tName\tID\tProbes\tNumGenes\tRawScore\tPval"
                + "\tCorrectedPvalue\tMultifuncSensitivity\tMultifuncBias" + extracolumns + "\n" );
    }

    public void setCorrectedPvalue( double a ) {
        correctedPvalue = a;
    }

    /**
     * @param auc
     */
    public void setMultifunctionality( double mf ) {
        this.multifunctionality = mf;
    }

    /**
     * Set how much this result changes in rank when multifunctionality correction is applied. Positive values mean a
     * lot of change.
     * 
     * @param multifunctionalityCorrectedRankDelta
     */
    public void setMultifunctionalityCorrectedRankDelta( int multifunctionalityCorrectedRankDelta ) {
        this.multifunctionalityCorrectedRankDelta = multifunctionalityCorrectedRankDelta;
    }

    public void setPValue( double apvalue ) {
        pvalue = apvalue;
    }

    /**
     * Where 1 is the best.
     * 
     * @param n
     */
    public void setRank( int n ) {
        rank = n;
    }

    /**
     * Where 0 is the best and 1 is the worst.
     * 
     * @param i
     */
    public void setRelativeRank( double i ) {
        if ( i < 0.0 || i > 1.0 ) {
            throw new IllegalArgumentException( "Relative rank must be in [0,1]" );
        }
        relativeRank = i;
    }

    public void setScore( double ascore ) {
        score = ascore;
    }

    public void setSizes( int size, int effsize ) {
        this.numProbes = size;
        this.numGenes = effsize;
    }

}