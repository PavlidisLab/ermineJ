/*
 * The ermineJ project
 *
 * Copyright (c) 2006-2011 University of British Columbia
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
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.graph.DirectedGraph;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;

/**
 * Maintains gene annotations. Be clear on the distinction between a GeneSetTerm (just a term) and a GeneSet (the set of
 * genes associated with a GeneSetTerm)
 * <p>
 * This is initialized by providing a set of elements (e.g. 'elements') with 'gene' and 'geneset' associations. When
 * ErmineJ is started, the annotation file provided determines the set of elements/genes that are available.
 * <p>
 * At later stages, when the user's analysis results are read in, they might not have all the elements/genes used. A
 * copy
 * of the GeneAnnotations should be made that references just the subset of elements/genes that are relevant.
 *
 * @author Paul Pavlidis
 * @author Shamhil Merchant
 * @author Homin Lee
 * @version Extensively rewritten for 2.2 (PP)
 */
public class GeneAnnotations {

    // undocumented for now, allows more flexible use of API without restriction of value = 2.
    private static final String MINIMUM_GENESET_SIZE_PROPERTY = "minimum.geneset.size";

    /**
     * Whether to filter out elements that hit more than one gene
     */
    public static final boolean DEFAULT_FILTER_NONSPECIFIC = false;

    /**
     * The minimum size of a 'set' of genes. Seems reasonable, doesn't it?
     */
    private static final int ABSOLUTE_MINIMUM_GENESET_SIZE = 2;

    private static Log log = LogFactory.getLog( GeneAnnotations.class.getName() );

    /**
     * The maximum size of gene sets ever considered.
     */
    private static final int PRACTICAL_MAXIMUM_GENESET_SIZE = 50000;

    private int minimumGeneSetSize = ABSOLUTE_MINIMUM_GENESET_SIZE;

    private Multifunctionality multifunctionality;

    /**
     * This includes all gene set terms, including ones which are redundant.
     */
    private Map<GeneSetTerm, GeneSet> geneSets = new HashMap<>();

    private GeneSetTerms geneSetTerms;

    private StatusViewer messenger = new StatusStderr();

    private Map<String, Element> elements = new CaseInsensitiveMap<>();

    private Map<String, Gene> genes = new CaseInsensitiveMap<>();

    private UserDefinedGeneSetManager userDefinedGeneSetManager;

    /**
     * Used to protect instances
     */
    private boolean allowModification = true;

    private SettingsHolder settings;

    private Collection<GeneAnnotations> subClones = new HashSet<>();

    /**
     * <p>
     * Constructor for GeneAnnotations.
     * </p>
     *
     * @param genes a {@link java.util.Collection} object.
     * @param geneSetTerms a {@link ubic.erminej.data.GeneSetTerms} object.
     * @param messenger a {@link ubic.basecode.util.StatusViewer} object.
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     */
    public GeneAnnotations( Collection<Gene> genes, GeneSetTerms geneSetTerms, SettingsHolder settings,
            StatusViewer messenger ) {
        if ( messenger != null ) this.messenger = messenger;
        if ( genes.isEmpty() ) {
            throw new IllegalArgumentException( "There were no genes" );
        }
        this.geneSetTerms = geneSetTerms;
        this.settings = settings;

        for ( Gene gene : genes ) {
            Collection<Element> geneProbes = gene.getProbes();
            this.genes.put( gene.getSymbol(), gene );
            for ( Element p : geneProbes ) {
                this.elements.put( p.getName(), p );
            }
        }

        setUp();
    }

    /**
     * Create a <strong>read-only</strong> new annotation set based on an existing one, for selected elements, removing
     * elements with no annotations.
     *
     * @param start a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param elements a {@link java.util.Collection} object.
     * @see ubic.erminej.data.GeneAnnotations
     */
    public GeneAnnotations( GeneAnnotations start, Collection<Element> elements ) {
        this( start, elements, true );
    }

    /**
     * Create a <strong>read-only</strong> new annotation set based on an existing one, for selected elements,
     * optionally
     * removing unannotated elements. This involves making new GeneSets, but the GeneSetTerms, Genes and Probes
     * themselves
     * are 'reused'. Multifunctionality is recomputed based on the restricted set. However, you should use subClone() to
     * avoid excessive duplication of annotation sets.
     *
     * @param start a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param elements which must be a subset of those in start (others will be ignored)
     * @param pruneUnannotated if true (normally!), elements lacking annotations are removed. This then becomes
     *        unmutable
     *        in the sense that adding annotations is not allowed.
     */
    public GeneAnnotations( GeneAnnotations start, Collection<Element> elements, boolean pruneUnannotated ) {

        StopWatch timer = new StopWatch();
        timer.start();
        if ( messenger != null ) this.messenger = start.messenger;

        if ( elements.isEmpty() ) {
            throw new IllegalArgumentException( "No elements were selected." );
        }

        Set<Element> startProbes = new HashSet<>( start.getProbes() ); // this has to get made.

        messenger.showProgress( "Creating a subsetted annotation set for " + elements.size() + "/" + startProbes.size()
                + " elements)" );

        if ( elements.size() > start.numProbes() ) {
            messenger.showWarning( "The new analysis has more elements than the original, any extras will be ignored" );
        }

        this.geneSetTerms = start.geneSetTerms;
        this.settings = start.getSettings();

        /*
         * First select elements that have annotations at all.
         */
        for ( Element p : elements ) {

            if ( !startProbes.contains( p ) ) {
                log.warn( "Element not in original: " + p );
                continue;
            }

            if ( pruneUnannotated && !p.hasAnnots() ) {
                continue;
            }

            for ( Gene g : p.getGenes() ) {
                this.genes.put( g.getSymbol(), g );
            }
            this.elements.put( p.getName(), p );
        }

        setUp( start );

        this.allowModification = false;

        if ( timer.getTime() > 1000 ) {
            log.info( "Subclone: " + timer.getTime() + "ms" );
        }
    }

    /**
     * Constructor designed for use when a file is not the immediate input of the data.
     *
     * @param geneSymbols A List of gene symbols (e.g., ACTB), corresponding to the elements (in the same order)
     * @param goTerms A List of Collections of Strings corresponding to the GO terms for each probe.
     * @throws IllegalArgumentException if any of the required arguments are null, don't have sizes that match, etc.
     * @param m a {@link ubic.basecode.util.StatusViewer} object.
     */
    public GeneAnnotations( List<Gene> geneSymbols, List<Collection<GeneSetTerm>> goTerms, StatusViewer m ) {
        checkValidData( geneSymbols, goTerms );
        if ( m != null ) this.messenger = m;

        try {
            this.settings = new Settings( false );
        } catch ( IOException e1 ) {
            throw new RuntimeException( e1 );
        }

        Collection<GeneSetTerm> allTerms = new HashSet<>();
        for ( Collection<GeneSetTerm> geneSetTerm : goTerms ) {
            allTerms.addAll( geneSetTerm );
        }
        this.geneSetTerms = new GeneSetTerms( allTerms );

        for ( int i = 0; i < geneSymbols.size(); i++ ) {
            Gene gene = geneSymbols.get( i );
            Collection<Element> geneProbes = gene.getProbes();
            this.genes.put( gene.getSymbol(), gene );
            for ( Element p : geneProbes ) {
                this.elements.put( p.getName(), p );
            }

            Collection<GeneSetTerm> terms = goTerms.get( i );

            this.addAnnotation( gene, terms );

            if ( i > 0 && i % 2000 == 0 ) {
                this.messenger.showProgress( "Loaded info for " + i + " genes" );
                try {
                    Thread.sleep( 10 );
                } catch ( InterruptedException e ) {
                    throw new RuntimeException( "Interrupted" );
                }
            }
        }

        this.messenger.showProgress( "Loaded info for " + this.genes.size() + " genes" );

        this.setUp();
    }

    /**
     * <p>
     * Constructor for GeneAnnotations.
     * </p>
     *
     * @param genes a {@link java.util.List} object.
     * @param goTerms a {@link java.util.List} object.
     * @param elements a {@link java.util.List} object.
     */
    public GeneAnnotations( List<String> elements, List<String> genes, List<Collection<String>> goTerms ) {
        Collection<GeneSetTerm> terms = new HashSet<>();
        for ( int i = 0; i < genes.size(); i++ ) {
            String g = genes.get( i );
            Gene ge = new Gene( g );
            ge.addElement( new Element( elements.get( i ) ) );
            this.genes.put( ge.getSymbol(), ge );

            for ( Collection<String> gss : goTerms ) {
                for ( String gs : gss ) {
                    GeneSetTerm term = new GeneSetTerm( gs, gs );
                    term.setAspect( "Unknown" );
                    terms.add( term );
                    ge.addGeneSet( term );
                }
            }

        }

        this.geneSetTerms = new GeneSetTerms( terms );

        for ( Gene gene : this.genes.values() ) {
            Collection<Element> geneProbes = gene.getProbes();
            for ( Element p : geneProbes ) {
                this.elements.put( p.getName(), p );
            }
        }

        try {
            this.settings = new Settings( false );
        } catch ( IOException e1 ) {
            throw new RuntimeException( e1 );
        }

        setUp();

    }

    /**
     * <p>
     * addAnnotation.
     * </p>
     *
     * @param gene a {@link ubic.erminej.data.Gene} object.
     * @param terms a {@link java.util.Collection} object.
     * @return how many annotations were added (i.e., were not already there)
     */
    public int addAnnotation( Gene gene, Collection<GeneSetTerm> terms ) {
        int i = 0;
        for ( GeneSetTerm t : terms ) {
            assert t != null;
            if ( gene.addGeneSet( t ) ) i++;
        }
        return i;
    }

    /**
     * <p>
     * addGeneSet.
     * </p>
     *
     * @param set a {@link ubic.erminej.data.GeneSet} object.
     */
    public void addGeneSet( GeneSet set ) {
        checkModifiability();
        this.addGeneSet( set.getTerm(), set.getGenes() );
    }

    /**
     * <p>
     * addGeneSet.
     * </p>
     *
     * @param geneSetId a {@link ubic.erminej.data.GeneSetTerm} object.
     * @param gs a {@link java.util.Collection} object.
     */
    public void addGeneSet( GeneSetTerm geneSetId, Collection<Gene> gs ) {
        this.addGeneSet( geneSetId, gs, null );
    }

    /**
     * Add a user-defined gene set.
     *
     * @param geneSetId a {@link ubic.erminej.data.GeneSetTerm} object.
     * @param gs -- these should already be checked for compatibility (don't add new genes here, might be okay but
     *        considered undefined)
     * @param sourceFile used for tracking sources and later modification/persistence.
     * @return a {@link ubic.erminej.data.GeneSet} object.
     */
    public GeneSet addGeneSet( GeneSetTerm geneSetId, Collection<Gene> gs, String sourceFile ) {
        checkModifiability();

        if ( this.hasGeneSet( geneSetId ) ) {
            throw new IllegalArgumentException( "Don't add sets twice" );
        }

        if ( gs.isEmpty() ) {
            throw new IllegalArgumentException( "Could not create a gene set that contains no genes." );
        }

        geneSetId.setUserDefined( true );
        this.geneSetTerms.addUserDefinedTerm( geneSetId );

        GeneSet newSet = new GeneSet( geneSetId, gs );
        newSet.setSourceFile( sourceFile );
        newSet.setUserDefined( true );

        geneSets.put( geneSetId, newSet );

        if ( this.multifunctionality != null ) this.multifunctionality.setStale( true );

        if ( log.isDebugEnabled() )
            log.debug( "Added new gene set: " + gs.size() + " genes in gene set " + geneSetId );

        updateSubClones( newSet );

        return newSet;
    }

    /**
     * Remove a gene set (class) from all the maps that reference it. This basically completely removes the class, and
     * it cannot be restored unless there is a backup. If it is user-defined it is deleted entirely from the
     * GeneSetTerms tree.
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     */
    public void deleteGeneSet( GeneSetTerm id ) {

        checkModifiability();

        // deals with elements.
        for ( Gene g : getGeneSetGenes( id ) ) {
            g.removeGeneSet( id );
        }

        geneSets.remove( id );

        if ( id.isUserDefined() ) geneSetTerms.removeUserDefined( id );
    }

    /**
     * Remove a no-longer-needed subclone. This only removes the reference from this, if other objects maintain a
     * reference it will obviously not be freed.
     *
     * @param a a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public void deleteSubClone( GeneAnnotations a ) {
        log.info( "Deleting annotations" );
        boolean removed = this.subClones.remove( a );
        if ( !removed ) {
            log.warn( "Was not able to remove the annotations!" );
        }
    }

    /**
     * <p>
     * deleteUserGeneSet.
     * </p>
     *
     * @param classID a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a boolean.
     */
    public boolean deleteUserGeneSet( GeneSetTerm classID ) {
        if ( this.isReadOnly() ) throw new UnsupportedOperationException();
        return this.userDefinedGeneSetManager.deleteUserGeneSet( classID );
    }

    /**
     * Case insensitive
     *
     * @param probe a {@link java.lang.String} object.
     * @return a {@link ubic.erminej.data.Element} object.
     */
    public Element findElement( String probe ) {
        return this.elements.get( probe );
    }

    /**
     * Case insensitive
     *
     * @param symbol a {@link java.lang.String} object.
     * @return a {@link ubic.erminej.data.Gene} object.
     */
    public Gene findGene( String symbol ) {
        return this.genes.get( symbol );
    }

    /**
     * <p>
     * findGeneSet.
     * </p>
     *
     * @param term a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a {@link ubic.erminej.data.GeneSet} object.
     */
    public GeneSet findGeneSet( GeneSetTerm term ) {
        if ( term == null ) return null;
        return this.geneSets.get( term );
    }

    /**
     * <p>
     * findGeneSet.
     * </p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link ubic.erminej.data.GeneSet} object.
     */
    public GeneSet findGeneSet( String name ) {
        GeneSetTerm term = this.findTerm( name );
        if ( term == null ) return null;
        return findGeneSet( term );
    }

    /**
     * Create a selected elements list based on a search string.
     *
     * @param searchOn A string to be searched.
     * @return a {@link java.util.Set} object.
     */
    public Set<Element> findProbes( String searchOn ) {

        String searchOnUp = searchOn.toUpperCase();
        Set<Element> results = new HashSet<>();
        for ( Element probe : elements.values() ) {
            Gene candidate = probe.getGene();

            if ( candidate.getSymbol().toUpperCase().startsWith( searchOnUp )
                    || candidate.getName().toUpperCase().indexOf( searchOnUp ) >= 0 ) {
                results.add( probe );
            }
        }

        return results;
    }

    /**
     * Identify gene sets that contain a particular gene or probe.
     *
     * @param searchOn a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<GeneSetTerm> findSetsByGene( String searchOn ) {

        Set<GeneSetTerm> result = new HashSet<>();

        Set<Gene> g = findGenesByName( searchOn );
        for ( Gene gene : g ) {
            result.addAll( gene.getGeneSets() );
        }
        return result;
    }

    /**
     * <p>
     * findSetsByName.
     * </p>
     *
     * @param searchOn a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<GeneSetTerm> findSetsByName( String searchOn ) {
        String searchOnUp = searchOn.toUpperCase();
        Set<GeneSetTerm> result = new HashSet<>();
        for ( GeneSetTerm term : geneSets.keySet() ) {
            String candidateN = term.getName().toUpperCase();
            String candidateI = term.getId().toUpperCase();
            if ( candidateN.contains( searchOnUp ) || candidateI.startsWith( searchOnUp ) ) {
                result.add( term );
            }
        }
        return result;
    }

    /**
     * <p>
     * findTerm.
     * </p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link ubic.erminej.data.GeneSetTerm} object.
     */
    public GeneSetTerm findTerm( String id ) {
        return this.geneSetTerms.get( id );
    }

    /**
     * Returns true if the class is in the classToProbe map
     *
     * @param id String a class id
     * @return boolean
     */
    public boolean geneSetExists( String id ) {
        return this.findTerm( id ) != null;
    }

    /**
     * Get all gene sets, including ones that might be empty or redundant. Use for displaying lists that can then be
     * filtered.
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<GeneSetTerm> getAllTerms() {
        return Collections.unmodifiableSet( this.geneSetTerms.getGeneSets() );
    }

    /**
     * <p>
     * getChildren.
     * </p>
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return immediate children of the term
     */
    public Set<GeneSetTerm> getChildren( GeneSetTerm id ) {
        return geneSetTerms.getChildren( id );
    }

    /**
     * Return all genes, including those that are 'inactive'.
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Gene> getGenes() {
        Collection<Gene> finalList = new HashSet<>();
        for ( Element p : elements.values() ) {
            finalList.addAll( p.getGenes() );
        }
        return Collections.unmodifiableCollection( finalList );
    }

    /**
     * <p>
     * getGeneSet.
     * </p>
     *
     * @param classid a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a {@link ubic.erminej.data.GeneSet} object.
     */
    public GeneSet getGeneSet( GeneSetTerm classid ) {
        return this.geneSets.get( classid );
    }

    /**
     * <p>
     * getGeneSetElements.
     * </p>
     *
     * @param geneSetId a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return active elements for the given gene set
     */
    public Set<Element> getGeneSetElements( GeneSetTerm geneSetId ) {
        GeneSet geneSet = this.geneSets.get( geneSetId );
        if ( geneSet == null ) return Collections.unmodifiableSet( ( new HashSet<Element>() ) );
        return geneSet.getProbes(); // unmodifiable
    }

    /**
     * <p>
     * getGeneSetGenes.
     * </p>
     *
     * @param goset a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return set of genes in the given gene set (if any), based on the currently active elements
     */
    public Set<Gene> getGeneSetGenes( GeneSetTerm goset ) {
        GeneSet geneSet = this.geneSets.get( goset );
        if ( geneSet == null ) {
            /*
             * This is a little bit misleading/confusing -- asking for a non-existing geneSet could be an error (but
             * it's not at the moment!)
             */
            return Collections.unmodifiableSet( new HashSet<Gene>() );
        }
        return geneSet.getGenes(); // already unmodifiable.
    }

    /**
     * <p>
     * getGeneSetGraph.
     * </p>
     *
     * @return a {@link ubic.basecode.dataStructure.graph.DirectedGraph} object.
     */
    public DirectedGraph<String, GeneSetTerm> getGeneSetGraph() {
        return this.geneSetTerms.getGraph();
    }

    /**
     * <p>
     * Getter for the field <code>geneSets</code>.
     * </p>
     *
     * @return view of all gene sets, NOT including empty ones - at least minimumGeneSetSize genes (i.e., 2).
     */
    public Set<GeneSet> getGeneSets() {
        return Collections.unmodifiableSet( new HashSet<>( this.geneSets.values() ) );
    }

    /**
     * <p>
     * Getter for the field <code>geneSets</code>.
     * </p>
     *
     * @param terms a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<GeneSet> getGeneSets( Collection<GeneSetTerm> terms ) {
        Collection<GeneSet> res = new HashSet<>();
        for ( GeneSetTerm t : terms ) {
            if ( this.geneSets.containsKey( t ) ) {
                res.add( geneSets.get( t ) );
            }
        }
        return Collections.unmodifiableCollection( res );
    }

    /**
     * <p>
     * Getter for the field <code>geneSetTerms</code>.
     * </p>
     *
     * @return view of all gene set terms, NOT including empty ones - at least minimumGeneSetSize genes (i.e., 2).
     */
    public Set<GeneSetTerm> getGeneSetTerms() {
        Set<GeneSetTerm> res = new HashSet<>();
        res.addAll( this.geneSets.keySet() );
        return Collections.unmodifiableSet( res );
    }

    /**
     * <p>
     * getGeneSetTermsHolder.
     * </p>
     *
     * @return a {@link ubic.erminej.data.GeneSetTerms} object.
     */
    public GeneSetTerms getGeneSetTermsHolder() {
        // FIXME stupid method name .. conflicts with other.
        return this.geneSetTerms;
    }

    /**
     * <p>
     * Getter for the field <code>minimumGeneSetSize</code>.
     * </p>
     *
     * @return a int.
     */
    public int getMinimumGeneSetSize() {
        return minimumGeneSetSize;
    }

    /**
     * <p>
     * Getter for the field <code>multifunctionality</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.data.Multifunctionality} object.
     */
    public Multifunctionality getMultifunctionality() {
        return multifunctionality;
    }

    /**
     * <p>
     * getProbes.
     * </p>
     *
     * @return the list of elements.
     */
    public Set<Element> getProbes() {
        return Collections.unmodifiableSet( new HashSet<>( this.elements.values() ) );
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

    /**
     * <p>
     * getUserDefinedGeneSets.
     * </p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<GeneSet> getUserDefinedGeneSets() {
        Set<GeneSet> result = new HashSet<>();
        for ( GeneSetTerm term : geneSets.keySet() ) {
            if ( term.isUserDefined() ) {
                result.add( geneSets.get( term ) );
            }
        }
        return result;
    }

    /**
     * Get the gene sets that are user-defined.
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<GeneSetTerm> getUserDefinedTerms() {
        Set<GeneSetTerm> result = new HashSet<>();
        for ( GeneSetTerm term : geneSets.keySet() ) {
            if ( term.isUserDefined() ) {
                result.add( term );
            }
        }
        return Collections.unmodifiableSet( result );
    }

    /**
     * <p>
     * hasGene.
     * </p>
     *
     * @param g a {@link ubic.erminej.data.Gene} object.
     * @return a boolean.
     */
    public boolean hasGene( Gene g ) {
        return this.genes.values().contains( g );
    }

    /**
     * <p>
     * hasGeneSet.
     * </p>
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a boolean.
     */
    public boolean hasGeneSet( GeneSetTerm id ) {
        return this.geneSets.containsKey( id );
    }

    /**
     * <p>
     * hasProbe.
     * </p>
     *
     * @param elementId a {@link ubic.erminej.data.Element} object.
     * @return a boolean.
     */
    public boolean hasProbe( Element elementId ) {
        return this.elements.values().contains( elementId );
    }

    /**
     * Check if a group has any redundancies.
     *
     * @param id a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return a boolean.
     */
    public boolean hasRedundancy( GeneSetTerm id ) {
        GeneSet geneSet = this.getGeneSet( id );
        if ( geneSet == null ) return false;
        return !geneSet.getRedundantGroups().isEmpty();
    }

    /**
     * <p>
     * hasUsableAspect.
     * </p>
     *
     * @param geneSetTerm a {@link ubic.erminej.data.GeneSetTerm} object.
     * @param missingAspectTreatedAsUsable What to do if the aspect is missing. User-defined groups (which don't have an
     *        aspect) aren't affected by this
     * @return true if the gene set would be included in the analysis.
     */
    public boolean hasUsableAspect( GeneSetTerm geneSetTerm, boolean missingAspectTreatedAsUsable ) {
        String aspect = geneSetTerm.getAspect();

        /*
         * If there is no aspect, we don't use it, unless it's user-defined (though that should have an aspect ... )
         */
        if ( aspect == null && !geneSetTerm.isUserDefined() ) {
            return missingAspectTreatedAsUsable;
        }

        if ( aspect == null ) {
            return false;
        }

        if ( this.settings.getUseBiologicalProcess() && aspect.equalsIgnoreCase( "biological_process" ) ) {
            return true;
        } else if ( this.settings.getUseMolecularFunction() && aspect.equalsIgnoreCase( "molecular_function" ) ) {
            return true;
        } else if ( this.settings.getUseCellularComponent() && aspect.equalsIgnoreCase( "cellular_component" ) ) {
            return true;
        } else if ( this.settings.getUseUserDefined() && aspect.equalsIgnoreCase( GeneSetTerms.USER_DEFINED ) ) {
            // probably won't reach this?
            return true;
        }

        return false;
    }

    /**
     * Test whether this was constructed 'subcloning' style. This should usually be true in the context of analyses,
     * which focus on analyzed subsets of the annotated genes. I recommend testing this in your code to make sure you
     * aren't forgetting to do that step.
     *
     * @return a boolean.
     */
    public boolean isReadOnly() {
        return !this.allowModification;
    }

    /**
     * <p>
     * loadPlainGeneList.
     * </p>
     *
     * @param loadFile a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     * @throws java.io.IOException if any.
     */
    public Collection<Gene> loadPlainGeneList( String loadFile ) throws IOException {
        if ( this.isReadOnly() ) throw new UnsupportedOperationException();
        return this.userDefinedGeneSetManager.loadPlainGeneList( loadFile );
    }

    /**
     * Count how many gene sets will be analyzed given the user's current settings (min and max size and aspects)
     *
     * @return a int.
     */
    public int numActiveGeneSets() {
        int minSize = this.settings.getMinClassSize();
        int maxSize = this.settings.getMaxClassSize();
        return numActiveGeneSets( minSize, maxSize );
    }

    /**
     * Count how many gene sets will be analyzed given the user's current selected aspects and the provided minimum and
     * maximum sizes.
     *
     * @param minSize a int.
     * @param maxSize a int.
     * @return a int.
     */
    public int numActiveGeneSets( int minSize, int maxSize ) {
        int c = 0;
        for ( Entry<GeneSetTerm, GeneSet> e : this.geneSets.entrySet() ) {
            GeneSetTerm t = e.getKey();
            GeneSet geneSet = e.getValue();
            if ( geneSet.size() >= minSize && geneSet.size() <= maxSize && hasUsableAspect( t, false ) ) {
                c++;
            }
        }
        return c;
    }

    /**
     * Compute how many genes have Gene set annotations.
     *
     * @return a int.
     */
    public int numAnnotatedGenes() {
        int count = 0;
        for ( Gene gene : genes.values() ) {
            Collection<GeneSetTerm> element = gene.getGeneSets();
            if ( element.size() > 0 ) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get how many elements point to the same gene. This is like the old "numReplicates".
     *
     * @param g a {@link ubic.erminej.data.Gene} object.
     * @return a int.
     */
    public int numElementsForGene( Gene g ) {
        if ( g == null ) throw new IllegalArgumentException( "Gene cannot be null" );
        return g.getProbes().size();
    }

    /**
     * Get the number of elements in a gene set, identified by id.
     *
     * @param id String a class id
     * @return int number of elements in the class
     */
    public int numElementsInGeneSet( GeneSetTerm id ) {
        if ( !geneSets.containsKey( id ) ) {
            return 0;
        }
        GeneSet geneSet = geneSets.get( id );
        return geneSet.getProbes().size();
    }

    /**
     * How many genes are currently available
     *
     * @return a int.
     */
    public int numGenes() {
        return this.getGenes().size();
    }

    /**
     * Get the number of gene sets currently available.
     *
     * @return a int.
     */
    public int numGeneSets() {
        return this.getGeneSetTerms().size();
    }

    /**
     * Get the number of genes in a gene set, identified by id.
     *
     * @param id String a class id
     * @return int number of genes in the class
     */
    public int numGenesInGeneSet( GeneSetTerm id ) {
        if ( !geneSets.containsKey( id ) ) {
            return 0;
        }
        return getGeneSetGenes( id ).size();
    }

    /**
     * <p>
     * numProbes.
     * </p>
     *
     * @return a int.
     */
    public int numProbes() {
        return elements.size();
    }

    /**
     * Print out the gene annotations in the same format we got them in, but if the gene sets have been modified, this
     * will be reflected.
     *
     * @param out a {@link java.io.Writer} object.
     * @throws java.io.IOException if any.
     */
    public void print( Writer out ) throws IOException {
        out.write( "Probe\tSymbol\tName\tGeneSets\n" );
        out.flush();
        for ( Element probe : elements.values() ) {
            Gene gene = probe.getGene();
            String desc = probe.getDescription();
            out.write( probe + "\t" + gene.getSymbol() + "\t" + desc + "\t" );
            Collection<GeneSetTerm> gs = probe.getGeneSets();
            for ( GeneSetTerm element : gs ) {
                out.write( element.getId() + "|" );
            }
            out.write( "\n" );
        }
    }

    /**
     * Save changes to a user-defined gene set.
     *
     * @param toSave a {@link ubic.erminej.data.GeneSet} object.
     * @throws java.io.IOException if any.
     */
    public void saveGeneSet( GeneSet toSave ) throws IOException {
        if ( this.isReadOnly() ) throw new UnsupportedOperationException();
        assert toSave.isUserDefined();

        if ( userDefinedGeneSetManager == null ) {
            // can happen if the 'use user-defined' was false at startup.
            userDefinedGeneSetManager = new UserDefinedGeneSetManager( this, settings, this.messenger );
            settings.setUseUserDefined( true );
        }

        this.userDefinedGeneSetManager.saveGeneSet( toSave );
        if ( this.multifunctionality != null ) this.multifunctionality.setStale( true );

        refreshRedundancyCheck( toSave );
    }

    /**
     * Make the selection the user-defined sets only.
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<GeneSetTerm> selectUserDefined() {

        Set<GeneSetTerm> result = new HashSet<>();
        for ( GeneSetTerm term : geneSets.keySet() ) {
            if ( term.isUserDefined() ) {
                result.add( term );
            }
        }

        return result;
    }

    /**
     * <p>
     * Setter for the field <code>messenger</code>.
     * </p>
     *
     * @param m a {@link ubic.basecode.util.StatusViewer} object.
     */
    public void setMessenger( StatusViewer m ) {
        if ( m == null ) return;
        this.messenger = m;
    }

    /**
     * Create a new annotation set based on an existing one, for selected elements, removing elements with no
     * annotations.
     * Subclones should be treated as unmodifiable (though we don't enforce that strongly as it would greatly increase
     * memory requirements)
     * <p>
     * Note that this could be changed in the future to consider the annotation aspect (BP, MF etc) not just the
     * elements
     * to retain. See comment in the Multifunctionality javadoc.
     *
     * @param probesToRetain a {@link java.util.Collection} object.
     * @return a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public GeneAnnotations subClone( Collection<Element> probesToRetain ) {

        StopWatch timer = new StopWatch();
        timer.start();
        // messenger.showStatus( "Creating annotation set ..." );

        // perhaps there is no pruning to be done:
        if ( this.getProbes().size() == probesToRetain.size() && this.getProbes().containsAll( probesToRetain ) ) {
            // WARNING this is a modifiable version of the annotations, but copying is memory hog.
            // TODO create a lightweight unmodifiable wrapper.
            // log.info( "Subcloning not needed" );
            return this;
        }

        // use an existing subclone?
        for ( GeneAnnotations existingSubClone : subClones ) {
            Collection<Element> existingSubCloneProbes = new HashSet<>( existingSubClone.getProbes() );
            if ( existingSubCloneProbes.size() == probesToRetain.size()
                    && existingSubCloneProbes.containsAll( probesToRetain ) ) {
                // log.info( "Found a usable existing annotation set" );
                return existingSubClone;
            }
        }

        GeneAnnotations clone = new GeneAnnotations( this, probesToRetain );
        this.subClones.add( clone );
        this.messenger.clear();

        if ( timer.getTime() > 1000 ) log.info( "Subclone annotations: " + timer.getTime() + "ms" );
        return clone;
    }

    /**
     * <p>
     * toTableModel.
     * </p>
     *
     * @return a {@link javax.swing.table.TableModel} object.
     */
    public TableModel toTableModel() {

        // this is not actually used except for a test .. but it could be used.
        final List<Element> pL = new ArrayList<>( elements.values() );
        return new AbstractTableModel() {
            private static final long serialVersionUID = 1L;
            private String[] columnNames = { "Element", "Gene", "Description" };

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public String getColumnName( int i ) {
                return columnNames[i];
            }

            @Override
            public int getRowCount() {
                return elements.size();
            }

            @Override
            public Object getValueAt( int i, int j ) {
                Element elementId = pL.get( i );
                switch ( j ) {
                    case 0:
                        return elementId;
                    case 1:
                        return elementId.getGene().getSymbol();
                    case 2:
                        return elementId.getDescription();
                    default:
                        return null;
                }
            }

        };
    }

    /**
     * Add the parents of each term to the association for each gene.
     *
     * @param ga
     * @param goNames
     */
    private void addParents() {

        if ( messenger != null ) {
            messenger.showProgress( "Inferring annotations in graph" );
        }
        Map<Gene, Collection<GeneSetTerm>> toBeAdded = new HashMap<>();
        Map<GeneSetTerm, Collection<GeneSetTerm>> parentCache = new HashMap<>();
        int count = 0;
        int affectedGenes = 0;

        for ( Gene gene : genes.values() ) {

            Collection<GeneSetTerm> terms = gene.getGeneSets();

            for ( GeneSetTerm geneSet : terms ) {
                if ( geneSet == null ) {
                    log.warn( "Null geneset for  " + gene );
                    continue;
                }

                Collection<GeneSetTerm> parents;
                if ( parentCache.containsKey( geneSet ) ) {
                    parents = parentCache.get( geneSet );
                } else {
                    parents = getAllParents( geneSet );
                    parentCache.put( geneSet, parents );
                }

                setParentsToBeAdded( toBeAdded, gene, parents );

            }

            if ( ++count % 3000 == 0 && messenger != null ) {
                messenger.showProgress( count + " genes examined for term parents ..." );
            }
        }

        int numAnnotsAdded = 0;
        for ( Gene g : toBeAdded.keySet() ) {
            Collection<GeneSetTerm> parents = toBeAdded.get( g );
            int numAdded = addAnnotation( g, parents );
            numAnnotsAdded += numAdded;
            if ( numAdded > 0 ) {
                affectedGenes++;
            }
        }

        if ( messenger != null ) {
            messenger.showProgress( "Added " + numAnnotsAdded + " inferred annotations (affected " + affectedGenes
                    + "/" + genes.size() + " genes)" );
        }
    }

    /**
     * @throws IllegalStateException if modification is not allowed.
     */
    private void checkModifiability() {
        if ( isReadOnly() ) throw new IllegalStateException( "Attempt to modify a read-only annotation set" );
    }

    /**
     * @param geneSymbols
     * @param geneNames
     * @param goTerms
     */
    private void checkValidData( List<Gene> geneSymbols, List<Collection<GeneSetTerm>> goTerms ) {
        if ( geneSymbols == null || goTerms == null ) {
            throw new IllegalArgumentException( " gene symbols, GO terms and GO data must not be null" );
        }
        if ( geneSymbols.isEmpty() ) {
            throw new IllegalArgumentException( "Empty list" );
        }
    }

    /**
     * @param searchOn
     * @return
     */
    private Set<Gene> findGenesByName( String searchOn ) {

        Set<Gene> results = new HashSet<>();

        Gene g = this.findGene( searchOn );
        if ( g != null ) results.add( g );

        g = this.findGene( searchOn.toUpperCase() );
        if ( g != null ) results.add( g );

        g = this.findGene( searchOn.toLowerCase() );
        if ( g != null ) results.add( g );

        // possibly return?

        String searchOnUp = searchOn.toUpperCase();

        for ( Gene c : getGenes() ) {
            if ( c.getName().contains( searchOnUp ) ) {
                results.add( c );
            }
        }

        return results;
    }

    /**
     */
    private void formGeneSets() {
        this.geneSets = new HashMap<>();
        for ( Gene g : this.genes.values() ) {
            for ( GeneSetTerm term : g.getGeneSets() ) {
                assert term != null;
                if ( !geneSets.containsKey( term ) ) {
                    GeneSet geneSet = new GeneSet( term );
                    geneSet.setIsGenes( true );
                    geneSets.put( term, geneSet );
                }
                geneSets.get( term ).addGene( g );
            }
        }
    }

    private Collection<GeneSetTerm> getAllParents( GeneSetTerm geneSet ) {
        return this.geneSetTerms.getAllParents( geneSet );
    }

    /**
     *
     */
    private void maybeSetMinimumGenesetSize() {
        assert settings != null;

        // undocumented for now, allows more flexible use of API without restriction of value = 2.
        if ( StringUtils.isNotBlank( settings.getStringProperty( MINIMUM_GENESET_SIZE_PROPERTY ) ) ) {
            try {
                this.minimumGeneSetSize = Integer
                        .parseInt( settings.getStringProperty( MINIMUM_GENESET_SIZE_PROPERTY ) );
                if ( minimumGeneSetSize < 1 )
                    throw new IllegalArgumentException( "minimum.geneset.size must be at least 1 (was: "
                            + minimumGeneSetSize + ")" );
                log.info( "Minimum gene set size set to " + this.minimumGeneSetSize );
            } catch ( NumberFormatException e ) {
                throw new IllegalArgumentException( "minimum.geneset.size was not a valid integer value" );
            }
        }
    }

    /**
     * Remove classes that have too few members, or which are obsolete. These are not removed from the GO tree
     *
     * @param subCloning signals that we're making a copy of another annotation set for the purpose of analysis; ensures
     *        the original is not pruned.
     */
    private void prune( boolean subCloning ) {

        if ( this.geneSets.isEmpty() ) {
            throw new IllegalStateException( "There are no gene sets" );
        }

        Set<GeneSetTerm> removeUs = new HashSet<>();
        int obsoleteRemoved = 0;
        int tooBigRemoved = 0;
        int tooSmallRemoved = 0;
        int startCount = geneSets.size();
        for ( GeneSetTerm id : geneSets.keySet() ) {

            if ( id.getAspect() == null || id.getDefinition().startsWith( "OBSOLETE" ) ) { // special case ...
                obsoleteRemoved++;
                removeUs.add( id );
            } else {
                int numP = numElementsInGeneSet( id );
                int numG = numGenesInGeneSet( id );
                if ( numP < minimumGeneSetSize || numG < minimumGeneSetSize ) {
                    tooSmallRemoved++;
                    removeUs.add( id );
                } else if ( numP > PRACTICAL_MAXIMUM_GENESET_SIZE || numG > PRACTICAL_MAXIMUM_GENESET_SIZE ) {
                    tooBigRemoved++;
                    removeUs.add( id );
                }
            }
        }

        for ( GeneSetTerm id : removeUs ) {
            prune( id, subCloning );
        }

        if ( !removeUs.isEmpty() ) {
            this.messenger.showProgress( "Pruning: " + removeUs.size() + "/" + startCount + " sets removed: obsolete ("
                    + obsoleteRemoved + "), too small (" + tooSmallRemoved + ") or too big (" + tooBigRemoved
                    + ") terms pruned." );
        }

        if ( this.geneSets.isEmpty() ) {
            throw new IllegalStateException(
                    "All gene sets were removed due to being too small, too big, or obsolete; usable size range="
                            + this.minimumGeneSetSize + " - " + PRACTICAL_MAXIMUM_GENESET_SIZE
                            + ". Check the format of your file and the score column selection." );
        }

    }

    /**
     * @param id
     * @param subCloning
     */
    private void prune( GeneSetTerm id, boolean subCloning ) {
        if ( isReadOnly() ) throw new IllegalStateException( "Attempt to modify a ready-only annotation set" );

        // deals with elements.
        for ( Gene g : getGeneSetGenes( id ) ) {
            g.removeGeneSet( id );
        }

        geneSets.remove( id );

        // when subcloning do not remove it from the tree, this is not for display purposes.
        if ( !subCloning && id.isUserDefined() ) geneSetTerms.removeUserDefined( id );
    }

    /**
     * Identify classes which are absoluely identical to others. This should be called after adding parents.
     */
    private void redundancyCheck() {

        StopWatch timer = new StopWatch();
        timer.start();
        messenger.showProgress( "There are " + numGeneSets()
                + " gene sets in the annotations, checking for redundancy ..." );

        List<GeneSet> bySize = new ArrayList<>( this.geneSets.values() );
        Collections.sort( bySize, new Comparator<GeneSet>() {
            @Override
            public int compare( GeneSet o1, GeneSet o2 ) {
                if ( o1.getGenes().size() > o2.getGenes().size() ) return -1;
                if ( o1.getGenes().size() == o2.getGenes().size() ) return o1.getId().compareTo( o2.getId() );
                return 1;
            }
        } );

        int numRedundant = 0;
        for ( int i = 0; i < bySize.size(); i++ ) {

            GeneSet gs1 = bySize.get( i );
            Collection<Gene> genes1 = gs1.getGenes();

            gs: for ( int j = i + 1; j < bySize.size(); j++ ) {

                GeneSet gs2 = bySize.get( j );
                Collection<Gene> genes2 = gs2.getGenes();

                assert genes2.size() <= genes1.size();

                if ( genes2.size() < genes1.size() ) break;

                for ( Gene g1 : genes1 ) {
                    if ( !genes2.contains( g1 ) ) continue gs; // not redundant.
                }

                gs1.addRedundantGroup( gs2 );
                gs2.addRedundantGroup( gs1 );
            }
            if ( i > 0 && i % 2000 == 0 ) {
                messenger.showProgress( i + " sets checked for redundancy, " + numRedundant + " found ..." );
            }
            if ( gs1.hasRedundancy() ) numRedundant++;

        }

        messenger.showProgress( numRedundant + "/" + geneSets.size()
                + " gene sets are redundant with at least one other." );

        log.info( "Redundancy check: " + timer.getTime() + "ms" );
    }

    /**
     * Used during subcloning of annotations. Make use of the fact that if two sets are redundant before, they will
     * still be redundant even if we remove elements (as long as there are any elements left).
     *
     * @param start the clone source
     */
    private void redundancyCheck( GeneAnnotations start ) {

        for ( GeneSet gs1 : this.geneSets.values() ) {
            GeneSet originalGeneSet = start.getGeneSet( gs1.getTerm() );

            // we can't have _more_ gene sets, but sometimes something funny can happen - GO:0001775
            if ( originalGeneSet == null ) {
                log.warn( gs1.getTerm() + " missing from source" );
                continue;
            }

            for ( GeneSet redund : originalGeneSet.getRedundantGroups() ) {
                gs1.addRedundantGroup( this.getGeneSet( redund.getTerm() ) );
                // I do not have to do it the other way around, since I'm iterating over all of them
            }

        }
    }

    /**
     * Update the redundancy information for one gene set.
     *
     * @param toSave
     */
    private void refreshRedundancyCheck( GeneSet toSave ) {
        this.messenger.showProgress( "Updating redundancy information ..." );
        toSave.clearRedundancy(); // reset it completely.
        Collection<Gene> genes1 = toSave.getGenes();
        sets: for ( GeneSet gs2 : this.geneSets.values() ) {

            if ( toSave.equals( gs2 ) ) continue;

            Collection<Gene> genes2 = gs2.getGenes();

            if ( genes1.size() != genes2.size() ) {
                gs2.clearRedundancy( toSave );
                continue;
            } // not identical.

            for ( Gene g1 : genes1 ) {
                if ( !genes2.contains( g1 ) ) {
                    gs2.clearRedundancy( toSave );
                    continue sets;
                } // not redundant.
            }

            // If we get here, it means the groups are the same.
            toSave.addRedundantGroup( gs2 );
            gs2.addRedundantGroup( toSave );
        }

        this.messenger.clear();
    }

    /**
     * @param toBeAdded
     * @param gene
     * @param parents
     */
    private void setParentsToBeAdded( Map<Gene, Collection<GeneSetTerm>> toBeAdded, Gene gene,
            Collection<GeneSetTerm> parents ) {
        if ( parents.isEmpty() ) return;
        if ( !toBeAdded.containsKey( gene ) ) {
            toBeAdded.put( gene, new HashSet<GeneSetTerm>() );
        }
        toBeAdded.get( gene ).addAll( parents );

    }

    /**
     * Initialize the gene sets and other data structures that needs special handling before use.
     *
     * @param goNames
     */
    private void setUp() {

        StopWatch timer = new StopWatch();
        timer.start();
        /** {@inheritDoc} */

        if ( settings != null && settings.loadUserDefined() ) {
            userDefinedGeneSetManager = new UserDefinedGeneSetManager( this, settings, this.messenger );
        }

        /** {@inheritDoc} */
        addParents(); // <- 1s

        formGeneSets(); // 1s

        assert !this.geneSets.isEmpty();

        maybeSetMinimumGenesetSize();

        prune( false ); // / 100 ms

        redundancyCheck(); // <-- slow, 8s for 7700 terms

        this.multifunctionality = new Multifunctionality( this, this.messenger ); // < 1 s.

        log.info( "Total annotation setup: " + timer.getTime() + "ms" );

        this.messenger.clear();
    }

    /**
     * Less intensive setup for when we are cloning a starting point.
     *
     * @param start
     */
    private void setUp( GeneAnnotations start ) {

        formGeneSets(); // fast

        if ( this.geneSets.isEmpty() ) {
            throw new IllegalStateException( "No gene sets were formed." );
        }

        maybeSetMinimumGenesetSize();

        prune( true /* subcloning */ ); // fast

        redundancyCheck( start );// fast

        this.multifunctionality = new Multifunctionality( this, this.messenger ); // ~1s
    }

    /**
     * Add a new set to the subclones.
     *
     * @param newSet
     */
    private void updateSubClones( GeneSet newSet ) {
        for ( GeneAnnotations clone : this.subClones ) {
            if ( clone.hasGeneSet( newSet.getTerm() ) ) {
                throw new IllegalStateException( "Don't add sets to subclones that already have them!" );
            }
            clone.geneSetTerms.addUserDefinedTerm( newSet.getTerm() );
            clone.geneSets.put( newSet.getTerm(), newSet );
            if ( clone.multifunctionality != null ) clone.multifunctionality.setStale( true );
        }
    }

}

class CaseInsensitiveMap<V> extends HashMap<String, V> {

    /** {@inheritDoc} */
    @Override
    public V get( Object key ) {
        if ( !( key instanceof String ) ) return null;
        return super.get( ( ( String ) key ).toLowerCase() );
    }

    /** {@inheritDoc} */
    @Override
    public V put( String key, V value ) {
        return super.put( key.toLowerCase(), value );
    }
}

class ClassSizeComparator implements Comparator<GeneSet>, Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public int compare( GeneSet a, GeneSet b ) {

        int sizea = a.size();
        int sizeb = b.size();

        if ( sizea > sizeb ) {
            return 1;
        } else if ( sizeb < sizea ) {
            return -1;
        }

        return 0;
    }
}
