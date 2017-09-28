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

    private final static DecimalFormat nf = new DecimalFormat();

    private final static DecimalFormat exp = new DecimalFormat( "0.###E00" );
    static {
        nf.setMaximumFractionDigits( 8 );
        nf.setMinimumFractionDigits( 3 );
    }
    protected GeneSetTerm geneSetTerm = null;
    private double pvalue = 1.0;
    private double score = 0.0;
    private int numGenes = 0;

    private double correctedPvalue = 1.0;
    private Double multifunctionalityRank = 0.5;

    private int rank = -12345;

    private double mfCorrectedPvalue = -1.0;

    private double mfCorrectedFdr = -1.0;

    private SettingsHolder settings;

    private Integer multifunctionalityCorrectedRankDelta = null;
    private int numProbes = 0;
    private double relativeRank = 1.0;

    /**
     * <p>
     * Constructor for GeneSetResult.
     * </p>
     */
    public GeneSetResult() {
        this( null, 0, 0, 0.0, 1.0, 1.0, null );
    }

    /**
     * <p>
     * Constructor for GeneSetResult.
     * </p>
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @param numProbes a int.
     * @param numGenes a int.
     * @param score a double.
     * @param pvalue a double.
     * @param correctedPvalue a double.
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
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
     * <p>
     * Constructor for GeneSetResult.
     * </p>
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @param numProbes (in set)
     * @param numGenes (in set)
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     */
    public GeneSetResult( GeneSetTerm id, int numProbes, int numGenes, SettingsHolder settings ) {
        this();
        assert id != null;
        this.geneSetTerm = id;
        this.settings = settings;
        this.setSizes( numProbes, numGenes );
    }

    /**
     * {@inheritDoc}
     *
     * Default comparator for this class: sorts by the pvalue.
     */
    @Override
    public int compareTo( GeneSetResult other ) {

        if ( this instanceof EmptyGeneSetResult && other instanceof EmptyGeneSetResult ) {
            // I don't think this will happen.
            return this.geneSetTerm.compareTo( other.geneSetTerm );
        }

        if ( other == null ) {
            return -1;
        }
        if ( other instanceof EmptyGeneSetResult ) {
            return -1;
        }
        if ( this.equals( other ) ) {
            return 0;
        }

        if ( this.pvalue > other.pvalue ) {
            return 1;
        } else if ( this.pvalue < other.pvalue ) {
            return -1;
        } else {
            // break ties alphabetically.
            if ( this.geneSetTerm == null ) {
                return 0; // stuck, but this shouldn't happen.
            }
            return this.geneSetTerm.compareTo( other.geneSetTerm );
        }
    }

    /** {@inheritDoc} */
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

    /**
     * <p>
     * Getter for the field <code>correctedPvalue</code>.
     * </p>
     *
     * @return a double.
     */
    public double getCorrectedPvalue() {
        return correctedPvalue;
    }

    /**
     * <p>
     * getGeneSetId.
     * </p>
     *
     * @return a {@link ubic.erminej.data.GeneSetTerm} object.
     */
    public GeneSetTerm getGeneSetId() {
        return this.geneSetTerm;
    }

    /**
     * <p>
     * Getter for the field <code>mfCorrectedFdr</code>.
     * </p>
     *
     * @return a double.
     */
    public double getMfCorrectedFdr() {
        return mfCorrectedFdr;
    }

    /**
     * <p>
     * Getter for the field <code>mfCorrectedPvalue</code>.
     * </p>
     *
     * @return a double.
     */
    public double getMfCorrectedPvalue() {
        return mfCorrectedPvalue;
    }

    /**
     * <p>
     * Getter for the field <code>multifunctionalityCorrectedRankDelta</code>.
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getMultifunctionalityCorrectedRankDelta() {
        return multifunctionalityCorrectedRankDelta;
    }

    /**
     * <p>
     * Getter for the field <code>multifunctionalityRank</code>.
     * </p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public Double getMultifunctionalityRank() {
        return multifunctionalityRank;
    }

    /**
     * <p>
     * Getter for the field <code>numGenes</code>.
     * </p>
     *
     * @return a int.
     */
    public int getNumGenes() {
        return numGenes;
    }

    /**
     * <p>
     * Getter for the field <code>numProbes</code>.
     * </p>
     *
     * @return int
     */
    public int getNumProbes() {
        return numProbes;
    }

    /**
     * <p>
     * Getter for the field <code>pvalue</code>.
     * </p>
     *
     * @return a double.
     */
    public double getPvalue() {
        return pvalue;
    }

    /**
     * Low numbers are better ranks.
     *
     * @return a int.
     */
    public int getRank() {
        return rank;
    }

    /**
     * <p>
     * Getter for the field <code>relativeRank</code>.
     * </p>
     *
     * @return a double.
     */
    public double getRelativeRank() {
        return relativeRank;
    }

    /**
     * <p>
     * Getter for the field <code>score</code>.
     * </p>
     *
     * @return a double.
     */
    public double getScore() {
        return score;
    }

    /**
     * <p>
     * Getter for the field <code>settings</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.SettingsHolder} object.
     */
    public SettingsHolder getSettings() {
        return settings;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( geneSetTerm == null ) ? 0 : geneSetTerm.hashCode() );
        return result;
    }

    /**
     * <p>
     * print.
     * </p>
     *
     * @param out a {@link java.io.Writer} object.
     * @throws java.io.IOException if any.
     */
    public void print( Writer out ) throws IOException {
        this.print( out, "" );
    }

    /**
     * The format is:
     *
     * <pre>
     * Gene set name
     * Gene set ID (e.g. GO:xxxxx)
     * Number of elements
     * Number of genes
     * Score
     * Pvalue
     * Multiple-testing corrected pvalue (FDR)
     * Multifunctionality-corrected pvalue
     * Multifunctionality-corrected pvalue, multiple-test corrected (FDR)
     * Multifunctionality of the gene set (relative rank, 1 is most multifunctional)
     * -- One more extra columns such as the redundant gene sets and the gene symbols of the members.
     * </pre>
     *
     * @param out a {@link java.io.Writer} object.
     * @param extracolumns a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public void print( Writer out, String extracolumns ) throws IOException {

        out.write( "!\t" + geneSetTerm.getName() + "\t" + geneSetTerm.getId() + "\t" + numProbes + "\t" + numGenes
                + "\t" + nf.format( score ) + "\t" + formatPvalue( pvalue ) + "\t" + formatPvalue( correctedPvalue )
                + "\t" + formatPvalue( mfCorrectedPvalue ) + "\t" + formatPvalue( mfCorrectedFdr ) + "\t"
                + String.format( "%.3g", this.multifunctionalityRank ) + extracolumns + "\n" );
    }

    /**
     * <p>
     * printHeadings.
     * </p>
     *
     * @param out a {@link java.io.Writer} object.
     * @throws java.io.IOException if any.
     */
    public void printHeadings( Writer out ) throws IOException {
        this.printHeadings( out, "" );
    }

    /**
     * <p>
     * printHeadings.
     * </p>
     *
     * @param out a {@link java.io.Writer} object.
     * @param extracolumns a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public void printHeadings( Writer out, String extracolumns ) throws IOException {
        out.write( "#\n#!" );
        out.write( "\tName\tID\tNumProbes\tNumGenes\tRawScore\tPval"
                + "\tCorrectedPvalue\tMFPvalue\tCorrectedMFPvalue\tMultifunctionality" + extracolumns + "\n" );
    }

    /**
     * <p>
     * Setter for the field <code>correctedPvalue</code>.
     * </p>
     *
     * @param a a double.
     */
    public void setCorrectedPvalue( double a ) {
        correctedPvalue = a;
    }

    /**
     * <p>
     * Setter for the field <code>mfCorrectedFdr</code>.
     * </p>
     *
     * @param mfCorrectedFdr a double.
     */
    public void setMfCorrectedFdr( double mfCorrectedFdr ) {
        this.mfCorrectedFdr = mfCorrectedFdr;
    }

    /**
     * <p>
     * Setter for the field <code>mfCorrectedPvalue</code>.
     * </p>
     *
     * @param mfCorrectedPvalue a double.
     */
    public void setMfCorrectedPvalue( double mfCorrectedPvalue ) {
        this.mfCorrectedPvalue = mfCorrectedPvalue;
    }

    /**
     * Set how much this result changes in rank when multifunctionality correction is applied. Positive values mean a
     * lot of change.
     *
     * @param multifunctionalityCorrectedRankDelta a {@link java.lang.Integer} object.
     */
    public void setMultifunctionalityCorrectedRankDelta( Integer multifunctionalityCorrectedRankDelta ) {
        this.multifunctionalityCorrectedRankDelta = multifunctionalityCorrectedRankDelta;
    }

    /**
     * <p>
     * Setter for the field <code>multifunctionalityRank</code>.
     * </p>
     *
     * @param rank relative, where 1 is most multifunctional
     */
    public void setMultifunctionalityRank( Double rank ) {
        this.multifunctionalityRank = rank;
    }

    /**
     * <p>
     * setPValue.
     * </p>
     *
     * @param apvalue a double.
     */
    public void setPValue( double apvalue ) {
        pvalue = apvalue;
    }

    /**
     * Where 1 is the best.
     *
     * @param n a int.
     */
    public void setRank( int n ) {
        rank = n;
    }

    /**
     * Where 0 is the best and 1 is the worst.
     *
     * @param i a double.
     */
    public void setRelativeRank( double i ) {
        if ( i < 0.0 || i > 1.0 ) {
            throw new IllegalArgumentException( "Relative rank must be in [0,1]" );
        }
        relativeRank = i;
    }

    /**
     * <p>
     * Setter for the field <code>score</code>.
     * </p>
     *
     * @param ascore a double.
     */
    public void setScore( double ascore ) {
        score = ascore;
    }

    /**
     * <p>
     * setSizes.
     * </p>
     *
     * @param size a int.
     * @param effsize a int.
     */
    public void setSizes( int size, int effsize ) {
        this.numProbes = size;
        this.numGenes = effsize;
    }

    /**
     * @return
     */
    private String formatPvalue( double p ) {
        if ( p < 0.0 ) {
            return "";
        }
        return p < 10e-3 ? exp.format( p ) : nf.format( p );
    }

}
