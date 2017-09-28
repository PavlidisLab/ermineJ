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
package ubic.erminej.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.math.DescriptiveWithMissing;
import ubic.basecode.math.MatrixStats;
import ubic.basecode.util.StatusViewer;
import ubic.basecode.util.StringUtil;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.Element;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Histogram;

/**
 * <p>
 * CorrelationPvalGenerator class.
 * </p>
 *
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class CorrelationPvalGenerator extends AbstractGeneSetPvalGenerator {

    private double histRange = 0;
    private DoubleMatrix<Element, String> data = null;
    private Histogram hist;
    private Settings.MultiElementHandling geneRepTreatment = Settings.MultiElementHandling.BEST;
    private int cacheHits = 0;
    private int tests = 0;
    private boolean[][] nanStatusMatrix;
    private double[][] selfSquaredMatrix;

    /**
     * <p>
     * Constructor for CorrelationPvalGenerator.
     * </p>
     *
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param geneAnnots a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param rawData a {@link ubic.basecode.dataStructure.matrix.DoubleMatrix} object.
     * @param hist a {@link ubic.erminej.data.Histogram} object.
     * @param messenger a {@link ubic.basecode.util.StatusViewer} object.
     */
    public CorrelationPvalGenerator( SettingsHolder settings, GeneAnnotations geneAnnots,
            DoubleMatrix<Element, String> rawData, Histogram hist, StatusViewer messenger ) {
        super( settings, geneAnnots, null, messenger );

        this.geneAnnots = geneAnnots;
        this.data = rawData;

        nanStatusMatrix = MatrixStats.nanStatusMatrix( data.asArray() );
        selfSquaredMatrix = MatrixStats.selfSquaredMatrix( data.asArray() );

        setHistogram( hist );
        setGeneRepTreatment( settings.getGeneRepTreatment() );
        setClassMaxSize( settings.getMaxClassSize() );
        setClassMinSize( settings.getMinClassSize() );

    }

    /**
     * <p>
     * classPval.
     * </p>
     *
     * @param geneSetName a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a {@link ubic.erminej.data.GeneSetResult} object.
     */
    public GeneSetResult classPval( GeneSetTerm geneSetName ) {
        if ( !super.checkAspectAndRedundancy( geneSetName ) ) return null;
        int effSize = numGenesInSet( geneSetName );
        if ( effSize < settings.getMinClassSize() || effSize > settings.getMaxClassSize() ) {
            return null;
        }

        Collection<Element> elementsInSet = geneAnnots.getGeneSetElements( geneSetName );

        /*
         * Iterate over the elements to get pairwise correlations.; we do this in a list so we can do each comparison
         * just once.
         */
        double sumCorrel = 0.0;
        double nummeas = 0;
        Map<Long, Double> values = new HashMap<>();
        List<Element> elementList = new ArrayList<>( elementsInSet );

        for ( int i = elementList.size() - 1; i >= 0; i-- ) {
            Element elementi = elementList.get( i );

            if ( !data.containsRowName( elementi ) ) {
                continue;
            }

            int iIndex = data.getRowIndexByName( elementi );
            Gene genei = elementi.getGene();
            double[] irow = data.getRow( iIndex );
            int numElementsForGeneI = geneAnnots.numElementsForGene( genei );
            boolean multipleElementsI = numElementsForGeneI > 1;

            for ( int j = i - 1; j >= 0; j-- ) {

                Element elementj = elementList.get( j );

                if ( !data.containsRowName( elementj ) ) {
                    continue;
                }

                int jIndex = data.getRowIndexByName( elementj );
                Gene genej = elementj.getGene();

                if ( genei.equals( genej ) ) {
                    continue; // always ignore self-comparisons.
                }
                int numElementsForGeneJ = geneAnnots.numElementsForGene( genej );
                double[] jrow = data.getRow( jIndex );
                double corr = Math.abs( DescriptiveWithMissing.correlation( irow, jrow, selfSquaredMatrix[iIndex],
                        selfSquaredMatrix[jIndex], nanStatusMatrix[iIndex], nanStatusMatrix[jIndex] ) );
                tests++;

                if ( multipleElementsI || numElementsForGeneJ > 1 ) { // do we even need to bother?
                    if ( geneRepTreatment.equals( Settings.MultiElementHandling.BEST ) ) {
                        Long key = StringUtil.twoStringHashKey( genei.getSymbol(), genej.getSymbol() );
                        if ( !values.containsKey( key ) || values.get( key ) < corr ) {
                            values.put( key, new Double( corr ) );
                        }
                    } else if ( geneRepTreatment.equals( Settings.MultiElementHandling.MEAN ) ) {
                        double weight = 1.0 / ( ( double ) numElementsForGeneJ * ( double ) numElementsForGeneI );
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
                    if ( geneRepTreatment.equals( Settings.MultiElementHandling.BEST ) ) {
                        Long key = StringUtil.twoStringHashKey( genei.getSymbol(), genej.getSymbol() );
                        values.put( key, new Double( corr ) );
                    }
                }
            }
        }

        if ( geneRepTreatment.equals( Settings.MultiElementHandling.BEST ) ) {
            sumCorrel = 0.0;
            nummeas = 0;
            for ( Iterator<Double> iter = values.values().iterator(); iter.hasNext(); ) {
                sumCorrel += iter.next();
                nummeas++;
            }
        }

        double geneSetMeanCorrel = sumCorrel / nummeas;

        GeneSetResult result = new GeneSetResult( geneSetName, numElementsInSet( geneSetName ),
                numGenesInSet( geneSetName ), settings );
        result.setScore( geneSetMeanCorrel );
        result.setPValue( hist.getValue( effSize, geneSetMeanCorrel, true ) ); // always upper tail.
        return result;
    }

    /**
     * <p>
     * containsRow.
     * </p>
     *
     * @param name a {@link ubic.erminej.data.Element} object.
     * @return a boolean.
     */
    public boolean containsRow( Element name ) {
        return data.containsRowName( name );
    }

    /** {@inheritDoc} */
    @Override
    public Map<GeneSetTerm, GeneSetResult> generateGeneSetResults() {
        Map<GeneSetTerm, GeneSetResult> results = new HashMap<>();

        int count = 0;
        setTests( 0 );
        setCacheHits( 0 );

        this.numGenesUsed = geneAnnots.numGenes(); // is this going to be right?

        for ( Iterator<GeneSetTerm> iter = geneAnnots.getGeneSetTerms().iterator(); iter.hasNext(); ) {
            ifInterruptedStop();

            GeneSetTerm geneSetName = iter.next();
            GeneSetResult res = classPval( geneSetName );
            if ( res != null ) {
                results.put( geneSetName, res );
            }
            count++;
            if ( count % 100 == 0 ) {
                getMessenger().showProgress( count + " gene sets analyzed" );
            }
        }
        if ( results.isEmpty() ) return results;
        GeneSetPvalRun.populateRanks( results );

        // log.debug( "Tests: " + getTests() );
        // log.debug( "Cache hits: " + getCacheHits() );
        return results;
    }

    /**
     * Note that we don't worry about replicates here - it would slow things down too much.
     *
     * @param correls a {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix} object.
     * @return a double.
     */
    public double geneSetMeanCorrel( DenseDoubleMatrix<String, String> correls ) {
        int classSize = correls.rows();

        double avecorrel = 0.0;
        int nummeas = 0;
        for ( int i = 0; i < classSize; i++ ) {
            for ( int j = i + 1; j < classSize; j++ ) {
                avecorrel += Math.abs( correls.get( i, j ) );
                nummeas++;
            }
        }
        return avecorrel / nummeas;
    }

    /**
     * <p>
     * get_range.
     * </p>
     *
     * @return a double.
     */
    public double get_range() {
        return histRange;
    }

    /**
     * <p>
     * Getter for the field <code>cacheHits</code>.
     * </p>
     *
     * @return Returns the usedCache.
     */
    public int getCacheHits() {
        return this.cacheHits;
    }

    /**
     * <p>
     * Getter for the field <code>data</code>.
     * </p>
     *
     * @return a {@link ubic.basecode.dataStructure.matrix.DoubleMatrix} object.
     */
    public DoubleMatrix<Element, String> getData() {
        return data;
    }

    /**
     * <p>
     * Getter for the field <code>tests</code>.
     * </p>
     *
     * @return Returns the tests.
     */
    public int getTests() {
        return this.tests;
    }

    /**
     * <p>
     * set_range.
     * </p>
     *
     * @param range a double.
     */
    public void set_range( double range ) {
        histRange = range;
    }

    /**
     * <p>
     * Setter for the field <code>cacheHits</code>.
     * </p>
     *
     * @param usedCache The usedCache to set.
     */
    public void setCacheHits( int usedCache ) {
        this.cacheHits = usedCache;
    }

    /**
     * <p>
     * Setter for the field <code>geneRepTreatment</code>.
     * </p>
     *
     * @param geneRepTreatment a ubic.erminej.Settings$MultiElementHandling object.
     */
    public void setGeneRepTreatment( Settings.MultiElementHandling geneRepTreatment ) {
        this.geneRepTreatment = geneRepTreatment;
    }

    /**
     * <p>
     * setHistogram.
     * </p>
     *
     * @param hist a {@link ubic.erminej.data.Histogram} object.
     */
    public void setHistogram( Histogram hist ) {
        this.hist = hist;
    }

    /**
     * <p>
     * Setter for the field <code>tests</code>.
     * </p>
     *
     * @param tests The tests to set.
     */
    public void setTests( int tests ) {
        this.tests = tests;
    }

}
