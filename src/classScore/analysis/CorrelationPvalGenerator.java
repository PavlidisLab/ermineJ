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
import baseCode.util.CancellationException;
import cern.colt.list.DoubleArrayList;
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
    private Map cache;
    private int usedCache = 0;
    private int tests = 0;

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
    public int getUsedCache() {
        return this.usedCache;
    }

    /**
     * @param usedCache The usedCache to set.
     */
    public void setUsedCache( int usedCache ) {
        this.usedCache = usedCache;
    }

    public CorrelationPvalGenerator( Settings settings, GeneAnnotations a, GeneSetSizeComputer csc, GONames gon,
            DenseDoubleMatrix2DNamed data ) {
        super( settings, a, csc, gon );
        this.data = data;
        cache = new HashMap();
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
        double avecorrel = 0.0;
        double nummeas = 0;
        Map values = new HashMap();
        int b = 0;
        List probeList = new ArrayList( probesInSet );

        for ( int i = probeList.size() - 1; i >= 0; i-- ) {
            String probei = ( String ) probeList.get( i );
            String genei = ( String ) probeToGeneMap.get( probei );
            DoubleArrayList irow = new DoubleArrayList( data.getRowByName( probei ) );
            int numProbesForGene = geneAnnots.getGeneProbeList( genei ).size();

            for ( int j = i - 1; j >= 0; j-- ) {
                String probej = ( String ) probeList.get( j );
                String genej = ( String ) probeToGeneMap.get( probej );

                if ( genei.equals( genej ) ) {
                    continue; // always ignore self-comparisons.
                }

                DoubleArrayList jrow = new DoubleArrayList( data.getRowByName( probej ) );
                double corr = Math.abs( DescriptiveWithMissing.correlation( irow, jrow ) );
                tests++;

                if ( geneRepTreatment == Settings.BEST_PVAL ) {
                    String key = genei + "____" + genej;
                    if ( !values.containsKey( key ) || ( ( Double ) values.get( key ) ).doubleValue() < corr ) {
                        values.put( ( key ), new Double( corr ) );
                    }
                } else if ( geneRepTreatment == Settings.MEAN_PVAL ) {
                    double weight = weight = 1.0 / ( geneAnnots.getGeneProbeList( genej ).size() * numProbesForGene );
                    corr *= weight;
                    avecorrel += corr;
                    nummeas += weight;
                } else {
                    throw new UnsupportedOperationException( "Unsupported replicate treatment method "
                            + geneRepTreatment );
                }
                b++;
                if ( b % 100 == 0 ) {
                    ifInterruptedStop();
                    try {
                        Thread.sleep( 5 );
                    } catch ( InterruptedException ex ) {
                        log.debug( "Interrupted" );
                        throw new CancellationException( "Interrupted" );
                    }
                }
            }
        }

        if ( geneRepTreatment == Settings.BEST_PVAL ) {
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