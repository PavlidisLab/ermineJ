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

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Data structure to store class scoring information about a class.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetResult implements Comparable {
    private String class_id = null;
    private String class_name = null;
    private double pvalue = 1.0;
    private double score = 0.0;
    private int size = 0;
    private int effective_size = 0;
    private double pvalue_corr = 0.0;
    private int rank;

    public GeneSetResult() {
        this( null, null, 0, 0, 0.0, 1.0 );
    }

    /**
     * @param id
     * @param name
     * @param size
     * @param effectiveSize
     */
    public GeneSetResult( String id, String name, int size, int effectiveSize ) {
        this();
        this.setNames( id, name );
        this.setSizes( size, effectiveSize );
    }

    /**
     * @param id
     * @param name
     * @param size
     * @param effectiveSize
     * @param score
     * @param pvalue
     */
    public GeneSetResult( String id, String name, int size, int effectiveSize, double score, double pvalue ) {
        this.class_id = id;
        this.class_name = name;
        this.pvalue = pvalue;
        this.score = score;
        this.size = size;
        this.effective_size = effectiveSize;

    }

    public void print( BufferedWriter out ) throws IOException {
        this.print( out, "" );
    }

    public void print( BufferedWriter out, String extracolumns ) throws IOException {
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits( 8 );
        nf.setMinimumFractionDigits( 3 );

        DecimalFormat exp = new DecimalFormat( "0.###E00" );
        out.write( "!\t" + class_name + "\t" + class_id + "\t" + size + "\t" + effective_size + "\t"
                + nf.format( score ) + "\t" + ( pvalue < 10e-3 ? exp.format( pvalue ) : nf.format( pvalue ) ) + "\t"
                + ( pvalue_corr < 10e-3 ? exp.format( pvalue_corr ) : nf.format( pvalue_corr ) ) + extracolumns
                + "\n" );
    }

    public void printHeadings( BufferedWriter out ) throws IOException {
        this.printHeadings( out, "" );
    }

    public void printHeadings( BufferedWriter out, String extracolumns ) throws IOException {
        out.write( "#\n#!" );
        out.write( "\tName" + "\tID" + "\tProbes" + "\tNumGenes" + "\tRawScore" + "\tPval" +
        // "\tN over pval cut\tORA pval+"
                /* + "\tAROC" + "\tAROCpval" */
                "\tCorrectedPvalue" + extracolumns + "\n" );
    }

    public void setNames( String id, String name ) {
        this.class_id = id;
        this.class_name = name;
    }

    public void setSizes( int size, int effsize ) {
        this.size = size;
        this.effective_size = effsize;
    }

    public void setScore( double ascore ) {
        score = ascore;
    }

    public void setPValue( double apvalue ) {
        pvalue = apvalue;
    }

    public void setCorrectedPvalue( double a ) {
        pvalue_corr = a;
    }

    public String getGeneSetId() {
        return class_id;
    }

    public String getGeneSetName() {
        return this.class_name;
    }

    public double getPvalue() {
        return pvalue;
    }

    public double getScore() {
        return score;
    }

    public int getEffectiveSize() {
        return effective_size;
    }

    public int getRank() {
        return rank;
    }

    public void setRank( int n ) {
        rank = n;
    }

    public double getCorrectedPvalue() {
        return pvalue_corr;
    }

    /**
     * @return int
     */
    public int getSize() {
        return size;
    }

    /**
     * Default comparator for this class: sorts by the pvalue.
     * 
     * @param ob Object
     * @return int
     */
    public int compareTo( Object ob ) {
        GeneSetResult other = ( GeneSetResult ) ob;
        if ( this.pvalue > other.pvalue ) {
            return 1;
        } else if ( this.pvalue < other.pvalue ) {
            return -1;
        } else {
            return 0;
        }
    }

}