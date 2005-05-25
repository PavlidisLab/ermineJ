package classScore.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.math.DescriptiveWithMissing;
import baseCode.util.CancellationException;
import baseCode.util.StatusViewer;
import cern.colt.list.DoubleArrayList;
import classScore.Settings;
import classScore.data.GeneSetResult;
import classScore.data.Histogram;

/**
 * Calculates the raw average class correlations using a background distribution.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 * @version $Id$
 */
public class CorrelationsGeneSetPvalSeriesGenerator extends AbstractGeneSetPvalGenerator {

    protected static final Log log = LogFactory.getLog( CorrelationsGeneSetPvalSeriesGenerator.class );

    private Histogram hist;
    private CorrelationPvalGenerator probeCorrelData;
    // private Map probeToGeneSetMap; // stores probe->go Hashtable
    private Map geneSetToProbeMap; // stores go->probe Hashtable
    private Map results;
    private DenseDoubleMatrix2DNamed rawData;
    private Map probeToGeneMap;

    /**
     * @return
     */
    public Map getResults() {
        return results;
    }

    /**
     * @param settings
     * @param geneAnnots
     * @param csc
     * @param gon
     * @param rawData
     * @param hist
     */
    public CorrelationsGeneSetPvalSeriesGenerator( Settings settings, GeneAnnotations geneAnnots,
            GeneSetSizeComputer csc, GONames gon, DenseDoubleMatrix2DNamed rawData, Histogram hist ) {
        super( settings, geneAnnots, csc, gon );

        this.probeToGeneMap = geneAnnots.getProbeToGeneMap();
        this.probeCorrelData = new CorrelationPvalGenerator( settings, geneAnnots, csc, gon, rawData ); // main data
        // file
        this.geneAnnots = geneAnnots;
        // this.probeToGeneSetMap = geneAnnots.getProbeToClassMap();
        this.geneSetToProbeMap = geneAnnots.getGeneSetToProbeMap();

        this.hist = hist;
        this.rawData = rawData;
        probeCorrelData.set_class_max_size( settings.getMaxClassSize() );
        probeCorrelData.set_class_min_size( settings.getMinClassSize() );
        results = new HashMap();
    }

    /**
     * @param messenger
     * @todo make this faster. cache values?
     */
    public void geneSetCorrelationGenerator( StatusViewer messenger ) {
        // iterate over each class
        int count = 0;
        int setting = settings.getGeneRepTreatment();

        for ( Iterator it = geneSetToProbeMap.entrySet().iterator(); it.hasNext(); ) {
            if ( isInterrupted() ) {
                log.debug( "Canceling" );
                break;
            }
            Map.Entry e = ( Map.Entry ) it.next();
            Collection probesInSet = ( Collection ) e.getValue();
            String geneSetName = ( String ) e.getKey();

            if ( !super.checkAspect( geneSetName ) ) continue;

            int effSize = ( ( Integer ) effectiveSizes.get( geneSetName ) ).intValue();

            if ( effSize < probeCorrelData.getMinGeneSetSize() ) {
                continue; // then there is no hope.
            }

            // this calculation is done just in case the hashtable has no value
            // for an element...hence we keep track of the number of elements
            // with values and then create a Matrix to be used for correlation
            // based on that
            int classSize = 0;
            for ( Iterator mit = probesInSet.iterator(); mit.hasNext(); ) {
                String element = ( String ) mit.next();
                if ( probeCorrelData.containsRow( element ) ) {
                    classSize++;
                }
                if ( classSize > probeCorrelData.getMaxClassSize() ) {
                    break;
                }
            }

            // to check if class size is ok.
            if ( classSize < probeCorrelData.getMinGeneSetSize() || classSize > probeCorrelData.getMaxClassSize() ) {
                continue;
            }

            if ( count % 10 == 0 ) {
                messenger.showStatus( "Classes analyzed: " + count );
                try {
                    Thread.sleep( 5 );
                } catch ( InterruptedException ex ) {
                    log.debug( "Interrupted" );
                    throw new CancellationException();
                }
            }

            /*
             * Iterate over the probes to get pairwise correlations.; we do this in a list so we can do each comparison
             * just once.
             */
            double avecorrel = 0.0;
            double nummeas = 0;
            Map values = new HashMap();
            int b = 0;
            List probeList = new ArrayList( probesInSet );

            for ( int i = probeList.size() - 1; i >= 0; i-- ) {

                String probei = ( String ) probeList.get( i );
                String genei = ( String ) probeToGeneMap.get( probei );
                DoubleArrayList irow = new DoubleArrayList( rawData.getRowByName( probei ) );
                int numProbesForGene = geneAnnots.getGeneProbeList( genei ).size();

                for ( int j = i - 1; j >= 0; j-- ) {
                    String probej = ( String ) probeList.get( j );
                    String genej = ( String ) probeToGeneMap.get( probej );

                    if ( genei.equals( genej ) ) {
                        continue; // always ignore self-comparisons.
                    }

                    DoubleArrayList jrow = new DoubleArrayList( rawData.getRowByName( probej ) );

                    double corr = Math.abs( DescriptiveWithMissing.correlation( irow, jrow ) );

                    if ( setting == Settings.BEST_PVAL ) {
                        String key = genei + "___" + genej;
                        if ( !values.containsKey( key ) || ( ( Double ) values.get( key ) ).doubleValue() < corr ) {
                            values.put( ( key ), new Double( corr ) );
                        }
                    } else if ( setting == Settings.MEAN_PVAL ) {
                        double weight = weight = 1.0 / ( geneAnnots.getGeneProbeList( genej ).size() * numProbesForGene );
                        corr *= weight;
                        avecorrel += corr;
                        nummeas += weight;
                    } else {
                        throw new IllegalStateException( "Unknown method." );
                    }
                    b++;
                    if ( b % 500 == 0 ) {
                        try {
                            Thread.sleep( 5 );
                        } catch ( InterruptedException ex ) {
                            log.debug( "Interrupted" );
                            throw new RuntimeException( "Interrupted" );
                        }
                    }
                }
            }

            if ( setting == Settings.BEST_PVAL ) {
                avecorrel = 0.0;
                nummeas = 0;
                for ( Iterator iter = values.values().iterator(); iter.hasNext(); ) {
                    avecorrel += ( ( Double ) iter.next() ).doubleValue();
                    nummeas++;
                }
            }

            double geneSetMeanCorrel = avecorrel / nummeas;

            GeneSetResult result = new GeneSetResult( geneSetName, goName.getNameForId( geneSetName ),
                    ( ( Integer ) actualSizes.get( geneSetName ) ).intValue(), effSize );
            result.setScore( geneSetMeanCorrel );
            result.setPValue( hist.getValue( classSize, geneSetMeanCorrel, true ) ); // always upper tail.
            results.put( geneSetName, result );
            // log.debug(geneSetName + " " + hist.getValue( classSize, geneSetMeanCorrel ) + " " + classSize +
            // " " + geneSetMeanCorrel);
            count++;
        }
    }
}