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
 * Data structure to store class scoring information about a class.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GeneSetResult implements Comparable<GeneSetResult> {
    private String classId = null;
    private String clasName = null;
    private double pvalue = 1.0;
    private double score = 0.0;
    private int size = 0;
    private int effectiveSize = 0;
    private double correctedPvalue = 0.0;
    private double multifunctionality = 0.5;
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
        this.classId = id;
        this.clasName = name;
        this.pvalue = pvalue;
        this.score = score;
        this.size = size;
        this.effectiveSize = effectiveSize;
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
        out.write( "!\t" + clasName + "\t" + classId + "\t" + size + "\t" + effectiveSize + "\t" + nf.format( score )
                + "\t" + ( pvalue < 10e-3 ? exp.format( pvalue ) : nf.format( pvalue ) ) + "\t"
                + ( correctedPvalue < 10e-3 ? exp.format( correctedPvalue ) : nf.format( correctedPvalue ) )
                + String.format( "%.2f", this.multifunctionality ) + extracolumns + "\n" );
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

    public void setNames( String id, String name ) {
        this.classId = id;
        this.clasName = name;
    }

    public void setSizes( int size, int effsize ) {
        this.size = size;
        this.effectiveSize = effsize;
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

    public String getGeneSetId() {
        return classId;
    }

    public String getGeneSetName() {
        return this.clasName;
    }

    public double getPvalue() {
        return pvalue;
    }

    public double getScore() {
        return score;
    }

    public int getEffectiveSize() {
        return effectiveSize;
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
    public int getSize() {
        return size;
    }

    /**
     * Default comparator for this class: sorts by the pvalue.
     * 
     * @param ob Object
     * @return int
     */
    public int compareTo( GeneSetResult other ) {
        if ( this.pvalue > other.pvalue ) {
            return 1;
        } else if ( this.pvalue < other.pvalue ) {
            return -1;
        } else {
            return 0;
        }
    }

}