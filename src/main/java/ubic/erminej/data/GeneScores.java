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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.CancellationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.list.DoubleArrayList;
import ubic.basecode.dataStructure.matrix.MatrixUtil;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;

/**
 * Parse and store probe-&gt;score associations. The values are stored in a Map probeToPvalMap. This is used to see what
 * elements are int the data set, as well as the score for each probe.
 *
 * @author Shahmil Merchant
 * @author Paul Pavlidis
 */
public class GeneScores {

    private static final Log log = LogFactory.getLog( GeneScores.class );

    private static final String PROBE_IGNORE_REGEX = "AFFX.*";

    private static final double SMALL = 10e-16;

    /**
     * Refers to the _original_ scores.
     */
    private boolean biggerIsBetter = false;

    private Map<Gene, Double> geneToScoreMap;

    private Settings.MultiElementHandling gpMethod = SettingsHolder.MultiElementHandling.BEST;

    /**
     * Refers to what was done to the original scores. The scores stored here are negative-logged if this is true.
     */
    private boolean logTransform = true;

    private StatusViewer messenger = new StatusStderr();

    final private GeneAnnotations originalGeneAnnots;

    private Map<Element, Double> probeToScoreMap;

    private String scoreColumnName = "";

    /**
     * Constructor for the case when we are just taking in a hit list. The scores for all non-hit genes are set such
     * that the appropriate threshold will be zero. Note that no background set can be defined!
     *
     * @param identifiers a {@link java.util.Collection} object.
     * @param settings a {@link ubic.erminej.Settings} object.
     * @param m a {@link ubic.basecode.util.StatusViewer} object.
     * @param geneAnnotations a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param outputFileName if provided, also save this, and update the settings.
     */
    public GeneScores( Collection<String> identifiers, Settings settings, StatusViewer m,
            GeneAnnotations geneAnnotations, String outputFileName ) {

        if ( geneAnnotations == null ) {
            throw new IllegalStateException( "Annotations were null" ); // test situations?
        }

        this.originalGeneAnnots = geneAnnotations;
        if ( m != null ) this.messenger = m;
        this.init( settings );
        makeScores( identifiers, settings );

        if ( StringUtils.isNotBlank( outputFileName ) ) {
            File f = new File( outputFileName );
            try {
                this.write( f );
            } catch ( IOException e ) {
                throw new RuntimeException( "Failed to save the scores to " + outputFileName );
            }
            settings.setScoreFile( f.getAbsolutePath() );
            settings.setScoreCol( 2 );
        }

        // definitely have to do this now.
        settings.setGeneScoreThreshold( 0.0 );
        if ( settings.getDoLog() ) {
            settings.setGeneScoreThreshold( 1.0 ); // becomes 0.
        }

    }

    /**
     * Create a copy of source that contains only the elements given.
     *
     * @param source a {@link ubic.erminej.data.GeneScores} object.
     * @param geneAnnots - the original gene annotation set, the elements here will be used as a starting point
     */
    public GeneScores( GeneScores source, GeneAnnotations geneAnnots ) {
        if ( source.messenger != null ) this.messenger = source.messenger;

        this.originalGeneAnnots = geneAnnots;
        this.biggerIsBetter = source.biggerIsBetter;
        this.logTransform = source.logTransform;
        this.gpMethod = source.gpMethod;

        this.init();
        Collection<Element> elements = geneAnnots.getProbes();
        for ( Element p : elements ) {
            Double s = source.getProbeToScoreMap().get( p );
            if ( s == null ) {
                throw new IllegalArgumentException( "Element given that wasn't in the source: " + p );
            }
            this.probeToScoreMap.put( p, s );
        }

        setUpGeneToScoreMap();
    }

    /**
     * <p>
     * Constructor for GeneScores.
     * </p>
     *
     * @param is - input stream
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param geneAnnotations Source (original) geneannotation set.
     * @throws java.io.IOException if any.
     * @param m a {@link ubic.basecode.util.StatusViewer} object.
     */
    public GeneScores( InputStream is, SettingsHolder settings, StatusViewer m, GeneAnnotations geneAnnotations )
            throws IOException {

        // used only in tests.
        if ( geneAnnotations == null ) {
            throw new IllegalArgumentException( "Annotations cannot be null" );
        }
        this.originalGeneAnnots = geneAnnotations;
        this.init( settings );
        if ( m != null ) this.messenger = m;
        read( is, settings.getScoreCol() );
    }

    /**
     * Constructor designed for use when input is not a file.
     *
     * @param elements List of Strings.
     * @param scores List of java.lang.Doubles containing the scores for each probe.
     * @param geneAnnots Source (original) geneannotation set.
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     */
    public GeneScores( List<String> elements, List<Double> scores, GeneAnnotations geneAnnots, SettingsHolder settings ) {

        this.originalGeneAnnots = geneAnnots;

        if ( elements.size() != scores.size() ) {
            throw new IllegalArgumentException( "Elements and scores must be equal in number" );
        }
        if ( elements.size() == 0 ) {
            throw new IllegalArgumentException( "No elements" );
        }

        this.init( settings );
        boolean invalidLog = false;
        boolean invalidNumber = false;
        int numElementsKept = 0;
        int numRepeatedElements = 0;
        int numBadNumberStrings = 0;
        Collection<String> unknownElements = new HashSet<>();
        Collection<String> unannotatedElements = new HashSet<>();

        for ( int i = 0; i < elements.size(); i++ ) {
            String ps = elements.get( i );
            Double value = scores.get( i );

            // only keep elements that are in our array platform.
            Element probe = geneAnnots.findElement( ps );
            if ( probe == null ) {
                unknownElements.add( ps );
                continue;
            }

            if ( probe.getGeneSets().isEmpty() ) {
                /*
                 * Important. We're ignoring elements that don't have any terms.
                 */
                unannotatedElements.add( ps );
                continue;
            }

            if ( probe.getName().matches( PROBE_IGNORE_REGEX ) ) {
                continue;
            }

            double score = value.doubleValue();

            if ( Double.isNaN( score ) ) {
                numBadNumberStrings++;
                continue;
            }

            // Fudge when pvalues are zero.
            if ( settings.getDoLog() && score <= 0.0 ) {
                invalidLog = true;
                score = SMALL;
            }

            if ( settings.getDoLog() ) {
                score = -Math.log10( score );
            }

            /* we're done... */
            numElementsKept++;
            if ( probeToScoreMap.containsKey( probe ) ) {
                log.warn( "Repeated identifier: " + probe + ", keeping original value." );
                numRepeatedElements++;
            } else {
                probeToScoreMap.put( probe, new Double( score ) );
            }
        }

        reportProblems( invalidLog, unknownElements, unannotatedElements, invalidNumber, "", numElementsKept,
                numRepeatedElements, numBadNumberStrings, elements.size() );
        setUpGeneToScoreMap();

    }

    /**
     * <p>
     * Constructor for GeneScores.
     * </p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param geneAnnots Source (original) geneannotation set.
     * @throws java.io.IOException if any.
     * @param m a {@link ubic.basecode.util.StatusViewer} object.
     */
    public GeneScores( String filename, SettingsHolder settings, StatusViewer m, GeneAnnotations geneAnnots )
            throws IOException {
        if ( StringUtils.isBlank( filename ) ) {
            throw new IllegalArgumentException( "Filename for gene scores can't be blank" );
        }
        this.originalGeneAnnots = geneAnnots;
        if ( m != null ) {
            this.messenger = m;
            m.showStatus( "Reading scores from " + filename );
        }
        this.init( settings );

        FileTools.checkPathIsReadableFile( filename );
        InputStream is = FileTools.getInputStreamFromPlainOrCompressedFile( filename );
        read( is, settings.getScoreCol() );
        is.close();

        // Sanity check.

        // GeneAnnotations annots = this.getPrunedGeneAnnotations();
        // for ( GeneSet gs : annots.getGeneSets() ) {
        // assert this.getGeneToScoreMap().keySet().containsAll( gs.getGenes() );
        // assert annots.getGeneSetGenes( gs.getTerm() ).containsAll( gs.getGenes() );
        // }

    }

    /**
     * Note that these will already be log-transformed, if that was requested by the user.
     *
     * @return an array of {@link java.lang.Double} objects.
     */
    public Double[] getGeneScores() {
        return this.geneToScoreMap.values().toArray( new Double[] {} );
    }

    /**
     * Get the gene scores for just the genes in the set indicated.
     *
     * @param geneSetTerm a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return an array of {@link java.lang.Double} objects.
     */
    public Double[] getGeneSetScores( GeneSetTerm geneSetTerm ) {
        DoubleArrayList p = new DoubleArrayList();
        for ( Gene g : this.getPrunedGeneAnnotations().getGeneSetGenes( geneSetTerm ) ) {
            if ( geneToScoreMap.containsKey( g ) ) p.add( geneToScoreMap.get( g ) );
        }
        return ArrayUtils.toObject( MatrixUtil.fromList( p ).toArray() );
    }

    /**
     * Note that these values will already be log tranformed if that was requested.
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Gene, Double> getGeneToScoreMap() {
        return geneToScoreMap;
    }

    /**
     * <p>
     * Getter for the field <code>geneToScoreMap</code>.
     * </p>
     *
     * @param shuffle Whether the map should be scrambled first. If so, then groups are randomly associated with scores,
     *        but the actual values are the same. This is used for resampling multiple test correction.
     * @return Map of groups of genes to scores (which will have been -log-transformed already, if requested)
     */
    public Map<Gene, Double> getGeneToScoreMap( boolean shuffle ) {
        if ( shuffle ) {
            Map<Gene, Double> scrambled_map = new LinkedHashMap<>();
            Set<Gene> keys = geneToScoreMap.keySet();
            Iterator<Gene> it = keys.iterator();

            Collection<Double> values = geneToScoreMap.values();
            List<Double> valvec = new Vector<>( values );
            Collections.shuffle( valvec );

            // randomly associate keys and values
            int i = 0;
            while ( it.hasNext() ) {
                scrambled_map.put( it.next(), valvec.get( i ) );
                i++;
            }
            return scrambled_map;

        }
        return geneToScoreMap;

    }

    /**
     * <p>
     * getNumElementsUsed.
     * </p>
     *
     * @return a int.
     */
    public int getNumElementsUsed() {
        return probeToScoreMap.size();
    }

    /**
     * <p>
     * getNumGenesUsed.
     * </p>
     *
     * @return a int.
     */
    public int getNumGenesUsed() {
        return geneToScoreMap.size();
    }

    /**
     * Note that these values will already be log-transformed if that was requested.
     *
     * @return an array of {@link java.lang.Double} objects.
     */
    public Double[] getProbeScores() {
        return this.probeToScoreMap.values().toArray( new Double[] {} );
    }

    /**
     * Note that these values will already be log-transformed if that was requested
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Element, Double> getProbeToScoreMap() {
        return probeToScoreMap;
    }

    /**
     * <p>
     * getPrunedGeneAnnotations.
     * </p>
     *
     * @return a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public GeneAnnotations getPrunedGeneAnnotations() {
        // lightweight except for first time.
        if ( this.probeToScoreMap.isEmpty() ) return this.originalGeneAnnots;
        GeneAnnotations subClone = originalGeneAnnots.subClone( this.probeToScoreMap.keySet() );
        assert this.probeToScoreMap.keySet().containsAll( subClone.getProbes() );
        assert subClone.getProbes().containsAll( this.probeToScoreMap.keySet() );
        return subClone;
    }

    /**
     * <p>
     * getRankedGenes.
     * </p>
     *
     * @return list of genes in order of their scores, where the <em>first</em> gene is the 'best'. If 'big is better',
     *         genes with large scores will be given first. If smaller is better (pvalues) and the data are -log
     *         transformed (usual), then the gene that had the smallest pvalue will be first.
     */
    public List<Gene> getRankedGenes() {

        assert this.geneToScoreMap.keySet().containsAll( this.getPrunedGeneAnnotations().getGenes() );
        assert this.getPrunedGeneAnnotations().getGenes().containsAll( this.geneToScoreMap.keySet() );

        final boolean flip = originalGeneAnnots.getSettings().upperTail();
        TreeMap<Gene, Double> m = new TreeMap<>( new Comparator<Gene>() {

            @Override
            public int compare( Gene o1, Gene o2 ) {

                if ( o1.equals( o2 ) ) return 0;

                double d1 = geneToScoreMap.get( o1 );
                double d2 = geneToScoreMap.get( o2 );

                if ( d1 == d2 ) return o1.compareTo( o2 );

                if ( flip ) {
                    if ( d1 > d2 ) return -1;
                    return 1;
                }

                if ( d1 > d2 ) return 1;
                return -1;

            }
        } );

        m.putAll( geneToScoreMap );

        assert m.size() == this.geneToScoreMap.size();
        assert m.keySet().containsAll( this.geneToScoreMap.keySet() ); // !!

        return Collections.unmodifiableList( new ArrayList<>( m.keySet() ) );
    }

    /**
     * Might not be available.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getScoreColumnName() {
        return scoreColumnName;
    }

    /**
     * <p>
     * getValueMap.
     * </p>
     *
     * @param probe_id a {@link java.lang.String} object.
     * @return a double.
     */
    public double getValueMap( String probe_id ) {
        double value = 0.0;

        if ( probeToScoreMap.get( probe_id ) != null ) {
            value = Double.parseDouble( ( probeToScoreMap.get( probe_id ) ).toString() );
        }

        return value;
    }

    /**
     * <p>
     * isNegativeLog10Transformed.
     * </p>
     *
     * @return true if these scores were transformed via -log_10(x) when they were read in (according to the settings)
     */
    public boolean isNegativeLog10Transformed() {
        return logTransform;
    }

    /**
     * <p>
     * numGenesAboveThreshold.
     * </p>
     *
     * @param geneScoreThreshold a double.
     * @return a int.
     */
    public int numGenesAboveThreshold( double geneScoreThreshold ) {
        int count = 0;

        double t = geneScoreThreshold;
        if ( isNegativeLog10Transformed() ) {
            t = -Math.log10( geneScoreThreshold );
        }

        boolean rankLargeScoresBest = rankLargeScoresBest();

        for ( Gene g : this.geneToScoreMap.keySet() ) {

            if ( rankLargeScoresBest ) {
                if ( this.geneToScoreMap.get( g ) > t ) {
                    count++;
                }
            } else {
                if ( this.geneToScoreMap.get( g ) < t ) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * <p>
     * rankLargeScoresBest.
     * </p>
     *
     * @see Settings#upperTail
     * @return true if the values returned by methods such as getGeneToScoreMap are returning values which should be
     *         treated as "big better". This will be true in the following (common) cases based on the settings the user
     *         made:
     *         <ul>
     *         <li>The scores were -log transformed, and small values are better (e.g., input probabilities)
     *         <li>The scores were not -log transformed, and big values were better in the original input.
     *         </ul>
     */
    public boolean rankLargeScoresBest() {
        // The first case is the common one, if input is pvalues.
        return ( logTransform && !biggerIsBetter ) || ( !logTransform && biggerIsBetter );
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        boolean r = this.logTransform;

        for ( Element probe : probeToScoreMap.keySet() ) {
            double score = probeToScoreMap.get( probe );

            if ( r ) {
                score = Math.pow( 10, -score );
            }
            buf.append( String.format( "%s\t%.3g\n", probe.getName(), score ) );
        }
        return buf.toString();
    }

    /**
     * Write the gene scores to a file. Note: does not affect the setting's value of scoreFile.
     *
     * @param f path to write to
     * @throws java.io.IOException if any.
     */
    public void write( File f ) throws IOException {
        BufferedWriter w = new BufferedWriter( new FileWriter( f ) );
        w.write( "# Gene score file generated by ErmineJ. If created from a quicklist, the scores will be dummy values.\n" );
        w.write( "Element\tScore\n" );
        w.write( this.toString() );
        w.close();
    }

    private void init() {
        this.geneToScoreMap = new LinkedHashMap<>( 1000 );
        this.probeToScoreMap = new LinkedHashMap<>( 1000 );
    }

    private void init( SettingsHolder settings ) {
        init();

        this.biggerIsBetter = settings.getBigIsBetter();
        this.logTransform = settings.getDoLog();
        this.gpMethod = settings.getGeneRepTreatment();

    }

    /**
     * @param identifiers
     * @param settings
     */
    private void makeScores( Collection<String> identifiers, SettingsHolder settings ) {

        boolean warned = false;

        Double hitScore = -1.0;
        Double missScore = 1.0;
        if ( settings.getDoLog() ) {
            hitScore = 0.1; // becomes 1
            missScore = 10.0; // becomes -1
            if ( settings.getBigIsBetter() ) {
                hitScore = 10.0;
                missScore = 0.1;
            }
        } else if ( settings.getBigIsBetter() ) {
            hitScore = 1.0;
            missScore = -1.0;
        }

        if ( settings.getDoLog() ) {
            hitScore = -Math.log10( hitScore );
            missScore = -Math.log10( missScore );
        }

        int i = 0;
        Set<String> unMatched = new HashSet<>();
        // Set<String> unAnnotated = new HashSet<String>();

        for ( String idr : identifiers ) {

            String id = StringUtils.strip( idr );

            Element p = originalGeneAnnots.findElement( id );

            Gene g;
            if ( p == null ) {

                /*
                 * Try parsing as a gene.
                 */
                g = originalGeneAnnots.findGene( id );

                if ( g == null ) {
                    unMatched.add( id );
                    continue;
                }

                p = g.getProbes().iterator().next();

                if ( p == null ) {
                    continue; // shouldn't happen.
                }
            } else {
                g = p.getGene();
            }

            // don't exclude unannotated genes - we don't want to count them as negatives.
            // if ( g.getGeneSets().isEmpty() ) {
            // unAnnotated.add( id );
            // continue;
            // }

            if ( probeToScoreMap.containsKey( p ) ) {
                if ( !warned ) {
                    messenger.showStatus( "Repeated identifier: " + id + ", keeping original value." );
                    warned = true;
                }
            } else {
                probeToScoreMap.put( p, hitScore );
            }
            if ( ++i % 100 == 0 ) {
                messenger.showStatus( "Parsed " + i + " ..." );
            }

        }

        if ( probeToScoreMap.isEmpty() ) {
            throw new IllegalArgumentException( "None of the identifiers were recognized" );
        }

        if ( probeToScoreMap.size() == 1 ) {
            throw new IllegalArgumentException( "Hit list must have at least two recognized items" );
        }

        // if ( !unAnnotated.isEmpty() ) {
        // messenger.showStatus( unAnnotated.size() + " of the genes in the list were unannotated" );
        // }

        if ( !unMatched.isEmpty() ) {
            messenger.showStatus( "Some identifiers were not in the annotations. Examples: "
                    + StringUtils.join(
                            ArrayUtils.subarray( unMatched.toArray(), 0, Math.max( unMatched.size(), 20 ) ), " " ) );
        }

        messenger.showStatus( probeToScoreMap.size() + " genes/elements were recognized." );

        /*
         * Now add dummy scores for the non-entered
         */
        for ( Element p : originalGeneAnnots.getProbes() ) {
            if ( !probeToScoreMap.containsKey( p ) ) {
                probeToScoreMap.put( p, missScore );
            }
        }

        setUpGeneToScoreMap();
    }

    /**
     * @param is
     * @param scoreCol
     * @throws IOException
     */
    private void read( InputStream is, int scoreCol ) throws IOException {
        assert originalGeneAnnots != null;
        if ( scoreCol < 2 ) {
            throw new IllegalArgumentException( "Illegal column number " + scoreCol + ", must be greater than 1" );
        }

        if ( messenger != null ) {
            messenger.showProgress( "Reading gene scores from column " + scoreCol );
        }

        BufferedReader dis = new BufferedReader( new InputStreamReader( new BufferedInputStream( is ) ) );
        String row;
        boolean invalidLog = false;
        boolean invalidNumber = false;
        String badNumberString = "";
        int scoreColumnIndex = scoreCol - 1;
        int numElementsKept = 0;
        int numRepeatedElements = 0;
        int numMissingValues = 0;

        String heading = dis.readLine();
        String[] headings = heading.split( "\t" );
        if ( headings.length >= scoreCol ) {
            // this is not essential information.
            this.scoreColumnName = headings[scoreColumnIndex];
        }

        Collection<String> unknownElements = new HashSet<>();
        Collection<String> unannotatedElements = new HashSet<>();

        boolean warned = false;

        int totalElements = 0;
        while ( ( row = dis.readLine() ) != null ) {
            String[] fields = row.split( "\t" );

            // ignore rows that have insufficient columns.
            if ( fields.length < scoreCol ) {
                continue;
            }

            String elementId = StringUtils.strip( fields[0] );

            if ( elementId.matches( PROBE_IGNORE_REGEX ) ) {
                if ( !warned ) {
                    messenger.showStatus( "Skipping element in score file: " + elementId
                            + " (further warnings suppressed)" );
                    warned = true;
                }
                continue;
            }

            // number of rows ... less those skipped above.
            totalElements++;

            // only keep elements that have annotations.

            Element p = originalGeneAnnots.findElement( elementId );

            if ( p == null ) {
                // Probably just means there are no annotations at all.
                unknownElements.add( elementId );
                continue;
            }

            if ( p.getGeneSets().isEmpty() ) {
                unannotatedElements.add( elementId );
                continue;
            }

            double score = Double.NaN;
            try {
                score = Double.parseDouble( fields[scoreColumnIndex] );
            } catch ( NumberFormatException e ) {
                /* the first line can be a header; we ignore it if it looks bad */
                if ( probeToScoreMap.size() > 0 ) {
                    invalidNumber = true;
                    badNumberString = fields[scoreColumnIndex];
                    numMissingValues++;
                    continue;
                }
            }

            // Fudge when pvalues are zero.
            if ( logTransform && score <= 0.0 ) {
                invalidLog = true;
                score = SMALL;
            }

            if ( logTransform ) {
                score = -Math.log10( score );
            }

            /* we're done... */
            numElementsKept++;

            if ( probeToScoreMap.containsKey( p ) ) {
                if ( !warned ) {
                    messenger.showStatus( "Repeated identifier: " + elementId + ", keeping original value." );
                    warned = true;
                }
                numRepeatedElements++;
            } else {
                probeToScoreMap.put( p, score );
            }

            if ( numElementsKept % 100 == 0 && Thread.currentThread().isInterrupted() ) {
                dis.close();
                throw new CancellationException();
            }

        }
        dis.close();
        messenger.clear();
        reportProblems( invalidLog, unknownElements, unannotatedElements, invalidNumber, badNumberString,
                numElementsKept, numRepeatedElements, numMissingValues, totalElements );

        setUpGeneToScoreMap();

    }

    /**
     * @param invalidLog
     * @param unannotatedElements
     * @param unknownProbe
     * @param invalidNumber
     * @param badNumberString
     * @param numElementsKept
     * @param numBadNumberStrings
     * @param totalElements
     */
    private void reportProblems( boolean invalidLog, Collection<String> unknownElements,
            Collection<String> unannotatedElements, boolean invalidNumber, String badNumberString, int numElementsKept,
            int numRepeatedElements, int numBadNumberStrings, int totalElements ) {
        if ( invalidNumber ) {
            messenger.showWarning( "Non-numeric gene scores(s) " + " (e.g. '" + badNumberString + "') "
                    + " found for input file. These are set to missing (" + numBadNumberStrings + " missing)" );
        }

        if ( invalidLog ) {
            messenger
                    .showWarning( "Warning: There were attempts to take the log of non-positive values. These are set to "
                            + SMALL );
        }

        if ( unknownElements.size() > 0 ) {

            /*
             * Elements which have absolutely no annotations or gene assigned will be missed entirely. So this needn't
             * be a scary message
             */
            messenger.showStatus( probeToScoreMap.size()
                    + " ("
                    + String.format( "%.2f", 100.00 * probeToScoreMap.size()
                            / ( probeToScoreMap.size() + unknownElements.size() ) )
                    + "%) of the scores were usable (others may not have genes in the annotations?)" );
        }

        if ( !unannotatedElements.isEmpty() ) {
            /*
             * This is in addition to those which have no gene (listed as unknownElements)
             */
            messenger.showWarning( unannotatedElements.size()
                    + " elements in your gene score file had no gene sets and were ignored." );
        }

        if ( numRepeatedElements > 0 ) {
            messenger
                    .showWarning( numRepeatedElements
                            + " identifiers in your gene score file were repeats. Only the first occurrence encountered was kept in each case." );
        }

        if ( numElementsKept == 0 || probeToScoreMap.isEmpty() ) {
            messenger
                    .showError( "No usable gene scores found. Please check you have selected the right column, that the file has"
                            + " the correct plain text format and"
                            + " that it corresponds to the gene annotation file you selected." );
        } else if ( messenger != null ) {
            // messenger.showStatus( "Found " + probeToScoreMap.size() + " usable scores in the file" );
        }
    }

    /**
     * Each gene is assigned a score, built from the values for the elements for that genes; either BEST or MEAN as
     * selected by the user.
     */
    private void setUpGeneToScoreMap() {

        // just used to get gene->probe maps.
        Collection<Gene> genes = originalGeneAnnots.getGenes();

        assert genes.size() > 0;

        int usable = 0;
        int notUsable = 0;
        for ( Gene gene : genes ) {

            /*
             * elements in this group according to the array platform.
             */
            Collection<Element> elements = gene.getProbes();

            double geneScore = 0.0;

            // Analyze all elements in this 'group' (pointing to the same gene)
            int usableElementsForGene = 0;
            for ( Element probe : elements ) {

                if ( !probeToScoreMap.containsKey( probe ) ) {
                    continue;
                }

                // these values are already log transformed if the user selected that option.
                double score = probeToScoreMap.get( probe );

                switch ( gpMethod ) {
                    case MEAN: {
                        geneScore += score;
                        break;
                    }
                    case BEST: {
                        if ( usableElementsForGene == 0 ) {
                            geneScore = score;
                        } else {

                            if ( rankLargeScoresBest() ) {
                                geneScore = Math.max( score, geneScore );
                            } else {
                                geneScore = Math.min( score, geneScore );
                            }
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException( "Illegal selection for groups score method." );
                    }
                }
                usableElementsForGene++;
            } // end iter over elements for genes.

            if ( usableElementsForGene > 0 ) {
                if ( gpMethod.equals( SettingsHolder.MultiElementHandling.MEAN ) ) {
                    geneScore /= usableElementsForGene; // take the mean
                }
                geneToScoreMap.put( gene, geneScore );
                usable++;
            } else {
                notUsable++;
            }
        } // end of iter over genes.

        if ( usable == 0 ) {
            // this is okay, if we're trying to show the class despite there being no results.
            return;
        }

        if ( notUsable > 0 && usable / ( double ) notUsable < 0.1 ) {
            messenger.showWarning( "Usable scores for only " + usable + " distinct genes found ("
                    + String.format( "%.2f", 100.0 * usable / ( usable + notUsable ) ) + "%)" );
        } else {
            messenger.showStatus( "Usable scores for " + usable + " distinct genes found ("
                    + String.format( "%.2f", 100.0 * usable / ( usable + notUsable ) ) + "%)" );
        }
    }
} // end of class
