/*
 * The ermineJ project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScore.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.math.DescriptiveWithMissing;
import baseCode.math.MatrixStats;
import baseCode.util.StringUtil;
import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class CorrelationPvalGenerator extends AbstractGeneSetPvalGenerator {

    private double histRange = 0;
    private DenseDoubleMatrix2DNamed data = null;
    private Histogram hist;
    private Map probeToGeneMap;
    private int geneRepTreatment;
    private int cacheHits = 0;
    private int tests = 0;
    private double[][] dataAsRawMatrix;
    private double[][] selfSquaredMatrix;
    private boolean[][] nanStatusMatrix;

    /**
     * @return Returns the tests.
     */
    public int getTests() {
        return this.tests;
    }

    /**
     * @param tests The tests to set.
     */
    public void setTests( int tests ) {
        this.tests = tests;
    }

    /**
     * @return Returns the usedCache.
     */
    public int getCacheHits() {
        return this.cacheHits;
    }

    /**
     * @param usedCache The usedCache to set.
     */
    public void setCacheHits( int usedCache ) {
        this.cacheHits = usedCache;
    }

    public CorrelationPvalGenerator( Settings settings, GeneAnnotations a, GeneSetSizeComputer csc, GONames gon,
            DenseDoubleMatrix2DNamed data ) {
        super( settings, a, csc, gon );
        this.data = data;

        dataAsRawMatrix = new double[data.rows()][];
        for ( int j = 0; j < data.rows(); j++ ) {
            double[] rowValues = data.getRow( j );
            dataAsRawMatrix[j] = rowValues;
        }
        selfSquaredMatrix = MatrixStats.selfSquaredMatrix( dataAsRawMatrix );
        nanStatusMatrix = MatrixStats.nanStatusMatrix( dataAsRawMatrix );
    }

    public GeneSetResult classPval( String geneSetName ) {
        if ( !super.checkAspect( geneSetName ) ) return null;
        int effSize = ( ( Integer ) effectiveSizes.get( geneSetName ) ).intValue();
        if ( effSize < settings.getMinClassSize() || effSize > settings.getMaxClassSize() ) {
            return null;
        }

        Collection probesInSet = geneAnnots.getGeneSetProbes( geneSetName );

        /*
         * Iterate over the probes to get pairwise correlations.; we do this in a list so we can do each comparison just
         * once.
         */
        double sumCorrel = 0.0;
        double nummeas = 0;
        Map values = new HashMap();
        List probeList = new ArrayList( probesInSet );

        for ( int i = probeList.size() - 1; i >= 0; i-- ) {
            String probei = ( String ) probeList.get( i );
            int iIndex = data.getRowIndexByName( probei );
            String genei = ( String ) probeToGeneMap.get( probei );
            double[] irow = dataAsRawMatrix[iIndex];
            int numProbesForGeneI = geneAnnots.numProbesForGene( genei );
            boolean multipleProbesI = numProbesForGeneI > 1;

            for ( int j = i - 1; j >= 0; j-- ) {
                String probej = ( String ) probeList.get( j );
                int jIndex = data.getRowIndexByName( probej );
                String genej = ( String ) probeToGeneMap.get( probej );

                if ( genei.equals( genej ) ) {
                    continue; // always ignore self-comparisons.
                }
                int numProbesForGeneJ = geneAnnots.numProbesForGene( genej );
                double[] jrow = dataAsRawMatrix[jIndex];
                double corr = Math.abs( DescriptiveWithMissing.correlation( irow, jrow, selfSquaredMatrix[iIndex],
                        selfSquaredMatrix[jIndex], nanStatusMatrix[iIndex], nanStatusMatrix[jIndex] ) );
                tests++;

                if ( multipleProbesI || numProbesForGeneJ > 1 ) { // do we even need to bother?
                    if ( geneRepTreatment == Settings.BEST_PVAL ) {
                        Object key = StringUtil.twoStringHashKey( genei, genej );
                        if ( !values.containsKey( key ) || ( ( Double ) values.get( key ) ).doubleValue() < corr ) {
                            values.put( key, new Double( corr ) );
                        }
                    } else if ( geneRepTreatment == Settings.MEAN_PVAL ) {
                        double weight = 1.0 / ( ( double ) numProbesForGeneJ * ( double ) numProbesForGeneI );
                        corr *= weight;
                        sumCorrel += corr;
                        nummeas += weight;
                    } else {
                        throw new UnsupportedOperationException( "Unsupported replicate treatment method "
                                + geneRepTreatment );
                    }
                } else {
                    sumCorrel += corr;
                    nummeas++;
                    if ( geneRepTreatment == Settings.BEST_PVAL ) {
                        Object key = StringUtil.twoStringHashKey( genei, genej );
                        values.put( key, new Double( corr ) );
                    }
                }
            }
        }

        if ( geneRepTreatment == Settings.BEST_PVAL ) {
            sumCorrel = 0.0;
            nummeas = 0;
            for ( Iterator iter = values.values().iterator(); iter.hasNext(); ) {
                sumCorrel += ( ( Double ) iter.next() ).doubleValue();
                nummeas++;
            }
        }

        double geneSetMeanCorrel = sumCorrel / nummeas;

        GeneSetResult result = new GeneSetResult( geneSetName, goName.getNameForId( geneSetName ),
                ( ( Integer ) actualSizes.get( geneSetName ) ).intValue(), effSize );
        result.setScore( geneSetMeanCorrel );
        result.setPValue( hist.getValue( effSize, geneSetMeanCorrel, true ) ); // always upper tail.
        return result;
    }

    /**
     * @param name
     * @return
     */
    public boolean containsRow( String name ) {
        return data.containsRowName( name );
    }

    /**
     * @return
     */
    public DenseDoubleMatrix2DNamed getData() {
        return data;
    }

    /**
     * Note that we don't worry about replicates here - it would slow things down too much.
     * 
     * @param correls
     * @return
     */
    public double geneSetMeanCorrel( DenseDoubleMatrix2DNamed correls ) {
        int classSize = correls.rows();

        double avecorrel = 0.0;
        int nummeas = 0;
        for ( int i = 0; i < classSize; i++ ) {
            for ( int j = i + 1; j < classSize; j++ ) {
                avecorrel += Math.abs( correls.getQuick( i, j ) );
                nummeas++;
            }
        }
        return avecorrel / nummeas;
    }

    /**
     */
    public void set_range( double range ) {
        histRange = range;
    }

    /**
     */
    public double get_range() {
        return histRange;
    }

    /**
     * @param hist
     */
    public void setHistogram( Histogram hist ) {
        this.hist = hist;
    }

    /**
     * @param probeToGeneMap
     */
    public void setProbeToGeneMap( Map probeToGeneMap ) {
        this.probeToGeneMap = probeToGeneMap;
    }

    /**
     * @param geneRepTreatment
     */
    public void setGeneRepTreatment( int geneRepTreatment ) {
        this.geneRepTreatment = geneRepTreatment;
    }

}