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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;

/**
 * Maintains gene annotations. Be clear on the distinction between a GeneSetTerm (just a term) and a GeneSet (the set of
 * genes associated with a GeneSetTerm)
 * <p>
 * This is initialized by providing a set of 'probes' with 'gene' and 'geneset' associations. When ErmineJ is started,
 * the annotation file provided determines the set of probes/genes that are available.
 * <p>
 * At later stages, when the user's analysis results are read in, they might not have all the probes/genes used. A copy
 * of the GeneAnnotations should be made that references just the subset of probes/genes that are relevant.
 * 
 * @author Paul Pavlidis
 * @author Shamhil Merchant
 * @author Homin Lee
 * @version $Id$
 * @version Extensively rewritten for 2.2 (PP)
 */
public class GeneAnnotations {

    /**
     * Whether to filter out probes that hit more than one gene
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
    private static final int PRACTICAL_MAXIMUM_GENESET_SIZE = 5000;

    private Multifunctionality multifunctionality;

    /**
     * This includes all gene set terms, including ones which are redundant.
     */
    private Map<GeneSetTerm, GeneSet> geneSets = new HashMap<GeneSetTerm, GeneSet>();

    /**
     * A backup
     */
    private Map<GeneSetTerm, GeneSet> oldGeneSets = new HashMap<GeneSetTerm, GeneSet>();

    private Collection<Probe> probes = new HashSet<Probe>();

    private Collection<Gene> genes = new HashSet<Gene>();

    private GeneSetTerms geneSetTerms;

    private StatusViewer messenger = new StatusStderr();

    private Map<String, Probe> probeNameMap = new HashMap<String, Probe>();

    private Map<String, Gene> geneSymbolMap = new HashMap<String, Gene>();

    /**
     * Gene sets taht are redundant and (semi-arbitrarily) will be ignored.
     */
    private Collection<GeneSetTerm> skipDueToRedundancy = new HashSet<GeneSetTerm>();

    /**
     * @param genes
     * @param geneSetTerms
     * @param messenger
     */
    public GeneAnnotations( Collection<Gene> genes, GeneSetTerms geneSetTerms, StatusViewer messenger ) {
        if ( messenger != null ) this.messenger = messenger;
        this.probes = new HashSet<Probe>();
        assert !genes.isEmpty();
        this.genes.addAll( genes );
        this.geneSetTerms = geneSetTerms;

        for ( Gene gene : this.genes ) {
            Collection<Probe> geneProbes = gene.getProbes();
            this.probes.addAll( geneProbes );
            this.geneSymbolMap.put( gene.getSymbol(), gene );
            for ( Probe p : geneProbes ) {
                this.probeNameMap.put( p.getName(), p );
            }
        }

        setUp();
    }

    /**
     * Create a new annotation set based on an existing one, for selected probes, retaining all probes including those
     * which have no annotations. See the related contructor GeneAnnotations( GeneAnnotations start, Collection<Probe>
     * probes, boolean pruneUnannotated ).
     * 
     * @param start
     * @param probes
     */
    public GeneAnnotations( GeneAnnotations start, Collection<Probe> probes ) {
        this( start, probes, false );
    }

    boolean allowModification = true;

    /**
     * Create a new annotation set based on an existing one, for selected probes, optionally removing unannotated
     * probes. This involves making new GeneSets, but the GeneSetTerms, Genes and Probes themselves are 'reused'.
     * Multifunctionality is recomputed based on the restricted set.
     * 
     * @param start
     * @param probes which must be a subset of those in start (others will be ignored)
     * @param pruneUnannotated if true, probes lacking annotations are removed. This then becomes unmutable in the sense
     *        that adding annotations is not allowed. This constructor should generally be used
     */
    public GeneAnnotations( GeneAnnotations start, Collection<Probe> probes, boolean pruneUnannotated ) {
        if ( messenger != null ) this.messenger = start.messenger;

        if ( probes.size() > start.numProbes() ) {
            messenger.showError( "The new analysis has more probes than the original, any extras will be ignored" );
        }

        this.geneSetTerms = start.geneSetTerms;

        for ( Probe p : probes ) {

            if ( !start.getProbes().contains( p ) ) {
                log.warn( "Probe not in original" );
                continue;
            }

            if ( p.getGeneSets().isEmpty() && pruneUnannotated ) {
                continue;
            }

            this.probes.add( p );

            for ( Gene g : p.getGenes() ) {
                this.geneSymbolMap.put( g.getSymbol(), g );
                this.genes.add( g );
            }
            this.probeNameMap.put( p.getName(), p );
        }
        messenger.showStatus( "Cloning a reduced annotation set for " + this.probes.size() + " probes (out of "
                + start.getProbes().size() + ")" );

        setUp( start );

        this.allowModification = pruneUnannotated;
    }

    /**
     * Constructor designed for use when a file is not the immediate input of the data.
     * 
     * @param probes A List of probes
     * @param geneSymbols A List of gene symbols (e.g., ACTB), corresponding to the probes (in the same order)
     * @param geneNames A List of gene names (e.g., "Actin"), corresponding to the probes (in the same order). This can
     *        be null.
     * @param goTerms A List of Collections of Strings corresponding to the GO terms for each probe.
     * @throws IllegaArgumentException if any of the required arguments are null, don't have sizes that match, etc.
     */
    public GeneAnnotations( List<Gene> geneSymbols, List<Collection<GeneSetTerm>> goTerms, StatusViewer m ) {
        checkValidData( geneSymbols, goTerms );
        if ( m != null ) this.messenger = m;

        Collection<GeneSetTerm> allTerms = new HashSet<GeneSetTerm>();
        for ( Collection<GeneSetTerm> geneSetTerm : goTerms ) {
            allTerms.addAll( geneSetTerm );
        }
        this.geneSetTerms = new GeneSetTerms( allTerms );

        for ( int i = 0; i < geneSymbols.size(); i++ ) {
            Gene gene = geneSymbols.get( i );
            Collection<Probe> geneProbes = gene.getProbes();
            this.genes.add( gene );
            this.probes.addAll( geneProbes );
            this.geneSymbolMap.put( gene.getSymbol(), gene );
            for ( Probe p : geneProbes ) {
                this.probeNameMap.put( p.getName(), p );
            }

            Collection<GeneSetTerm> terms = goTerms.get( i );

            this.addAnnotation( gene, terms );

            if ( i > 0 && i % 2000 == 0 ) {
                this.messenger.showStatus( "Loaded info for " + i + " genes" );
                try {
                    Thread.sleep( 10 );
                } catch ( InterruptedException e ) {
                    throw new RuntimeException( "Interrupted" );
                }
            }
        }

        this.messenger.showStatus( "Loaded info for " + this.genes.size() + " genes" );

        this.setUp();
    }

    /**
     * @param genes
     * @param goTerms
     */
    public GeneAnnotations( List<String> probes, List<String> genes, List<Collection<String>> goTerms ) {
        Collection<GeneSetTerm> terms = new HashSet<GeneSetTerm>();
        for ( int i = 0; i < genes.size(); i++ ) {
            String g = genes.get( i );
            Gene ge = new Gene( g );
            ge.addProbe( new Probe( probes.get( i ) ) );
            this.genes.add( ge );

            for ( Collection<String> gss : goTerms ) {
                for ( String gs : gss ) {
                    GeneSetTerm term = new GeneSetTerm( gs, gs );
                    term.setAspect( "Unknown" );
                    terms.add( term );
                    ge.addGeneSet( term );
                }
            }
            this.genes.add( ge );

        }

        this.geneSetTerms = new GeneSetTerms( terms );

        for ( Gene gene : this.genes ) {
            Collection<Probe> geneProbes = gene.getProbes();
            this.probes.addAll( geneProbes );
            this.geneSymbolMap.put( gene.getSymbol(), gene );
            for ( Probe p : geneProbes ) {
                this.probeNameMap.put( p.getName(), p );
            }
        }

        setUp();

    }

    public void addAnnotation( Gene gene, Collection<GeneSetTerm> terms ) {
        for ( GeneSetTerm t : terms ) {
            assert t != null;
            gene.addGeneSet( t );
        }
    }

    /**
     * @param set
     */
    public void addGeneSet( GeneSet set ) {
        assert allowModification;
        this.addGeneSet( set.getTerm(), set.getGenes() );
    }

    /**
     * @param geneSetId
     * @param gs
     */
    public void addGeneSet( GeneSetTerm geneSetId, Collection<Gene> gs ) {
        assert allowModification;
        for ( Gene g : gs ) {
            if ( !this.hasGene( g ) ) {
                log.warn( "Adding previously unseen gene " + g ); // which is somewhat useless.
                this.genes.add( g );
                this.geneSymbolMap.put( g.getSymbol(), g );
                this.probes.addAll( g.getProbes() );
            }
        }

        geneSetId.setUserDefined( true );
        this.geneSetTerms.addUserDefinedTerm( geneSetId );

        GeneSet newSet = new GeneSet( geneSetId, gs );
        newSet.setUserDefined( true );

        geneSets.put( geneSetId, newSet );

        // redundancyCheck(); // Maybe not. Even if the user's set is redundant, they want to see it.

        this.multifunctionality.setStale( true );

        log.debug( "Added new gene set: " + gs.size() + " genes in gene set " + geneSetId );
    }

    /**
     * Add a new gene set. Used to set up user-defined gene sets.
     * 
     * @param id String class to be added
     * @param probesForNew collection of members.
     */
    public void addSet( GeneSetTerm geneSetId, Collection<Probe> probesForNew ) {

        assert allowModification;

        if ( probesForNew.isEmpty() ) {
            log.debug( "No probes to add for " + geneSetId );
            return;
        }

        for ( Probe p : probesForNew ) {
            if ( !hasProbe( p ) ) {
                log.warn( "Adding new probe : " + p );
                this.probes.add( p );
                this.probeNameMap.put( p.getName(), p );
            }
        }

        Collection<Gene> gs = new HashSet<Gene>();
        for ( Probe p : probesForNew ) {
            p.addToGeneSet( geneSetId );
            gs.addAll( p.getGenes() );
        }

        this.addGeneSet( geneSetId, gs );

    }

    /**
     * Remove a gene set (class) from all the maps that reference it. This basically completely removes the class, and
     * it cannot be restored unless there is a backup. If it is user-defined it is deleted entirely from the
     * GeneSetTerms tree.
     * 
     * @param id
     */
    public void deleteGeneSet( GeneSetTerm id ) {

        assert allowModification;

        // deals with probes.
        for ( Gene g : getGeneSetGenes( id ) ) {
            g.removeGeneSet( id );
        }

        geneSets.remove( id );

        if ( id.isUserDefined() ) geneSetTerms.removeUserDefined( id );

        oldGeneSets.remove( id );
        skipDueToRedundancy.remove( id );

    }

    public Gene findGene( String symbol ) {
        return this.geneSymbolMap.get( symbol );
    }

    public GeneSet findGeneSet( GeneSetTerm term ) {
        if ( term == null ) return null;
        return this.geneSets.get( term );
    }

    public GeneSet findGeneSet( String name ) {
        GeneSetTerm term = this.findTerm( name );
        if ( term == null ) return null;
        return findGeneSet( term );
    }

    public Probe findProbe( String probe ) {
        return this.probeNameMap.get( probe );
    }

    /**
     * Create a selected probes list based on a search string.
     * 
     * @param searchOn A string to be searched.
     */
    public Collection<Probe> findProbes( String searchOn ) {

        String searchOnUp = searchOn.toUpperCase();
        Set<Probe> results = new HashSet<Probe>();
        for ( Probe probe : probes ) {
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
     * @param searchOn
     */
    public Collection<GeneSetTerm> findSetsByGene( String searchOn ) {

        String searchOnUp = searchOn.toUpperCase();
        Set<GeneSetTerm> result = new HashSet<GeneSetTerm>();

        for ( GeneSetTerm candidateGeneSet : geneSets.keySet() ) {
            boolean found = false;
            Collection<Probe> p = geneSets.get( candidateGeneSet ).getProbes();

            for ( Probe candidate : p ) {
                if ( candidate.getName().toUpperCase().startsWith( searchOnUp ) ) {
                    found = true;
                    log.debug( "Found " + candidate + " in " + candidateGeneSet );
                    break;
                }
            }

            if ( found ) {
                result.add( candidateGeneSet );
                continue;
            }

            Collection<Gene> g = this.getGeneSetGenes( candidateGeneSet );
            for ( Gene candidate : g ) {
                if ( candidate.getSymbol().toUpperCase().startsWith( searchOnUp ) ) {
                    found = true;
                    log.debug( "Found " + candidate + " in " + candidateGeneSet );
                    break;
                }
            }

            if ( found ) {
                result.add( candidateGeneSet );
                continue;
            }

        }

        return result;
    }

    /**
     * @param searchOn
     */
    public Collection<GeneSetTerm> findSetsByName( String searchOn ) {

        String searchOnUp = searchOn.toUpperCase();
        Set<GeneSetTerm> result = new HashSet<GeneSetTerm>();
        for ( GeneSetTerm term : geneSets.keySet() ) {
            String candidateN = term.getName().toUpperCase();
            if ( candidateN.toUpperCase().startsWith( searchOnUp ) || term.getId().equals( searchOn ) ) {
                result.add( term );
            }
        }
        return result;
    }

    /**
     * @param id
     * @return
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
     * Get a collection of all (active) gene sets -- ones which have at least one probe and which are not marked as
     * redundant. Use for analysis.
     * 
     * @return
     */
    public Collection<GeneSetTerm> getActiveGeneSets() {
        Collection<GeneSetTerm> result = new HashSet<GeneSetTerm>();
        for ( GeneSetTerm gst : this.geneSets.keySet() ) {
            if ( this.getGeneSetProbes( gst ).size() > 1 ) result.add( gst );
        }
        removeRedundant( result );
        return result;
    }

    /**
     * @return
     */
    public Collection<GeneSet> getAllGeneSets() {
        Collection<GeneSet> res = new HashSet<GeneSet>();
        res.addAll( this.geneSets.values() );
        return res;
    }

    /**
     * Get all gene sets, including ones that might be empty or redundant. Use for displaying lists that can then be
     * filtered.
     * 
     * @return
     */
    public Collection<GeneSetTerm> getAllTerms() {
        return this.geneSetTerms.getGeneSets();
    }

    /**
     * @param id
     * @return immediate children of the term
     */
    public Set<GeneSetTerm> getChildren( GeneSetTerm id ) {
        return geneSetTerms.getChildren( id );
    }

    /**
     * Return all genes, including those that are 'inactive'.
     * 
     * @return
     */
    public Collection<Gene> getGenes() {
        Collection<Gene> finalList = new HashSet<Gene>();
        for ( Probe p : probes ) {
            finalList.addAll( p.getGenes() );
        }
        return finalList;
    }

    public GeneSet getGeneSet( GeneSetTerm classid ) {
        return this.geneSets.get( classid );
    }

    /**
     * @param goset
     * @return set of genes in the given gene set (if any), based on the currently active probes
     */
    public Collection<Gene> getGeneSetGenes( GeneSetTerm goset ) {
        GeneSet geneSet = this.geneSets.get( goset );
        if ( geneSet == null ) {
            /*
             * This is a little bit misleading
             */
            return new HashSet<Gene>();
        }
        return geneSet.getGenes();
    }

    /**
     * @param geneSetId
     * @return active probes for the given gene set
     */
    public Collection<Probe> getGeneSetProbes( GeneSetTerm geneSetId ) {
        GeneSet geneSet = this.geneSets.get( geneSetId );
        if ( geneSet == null ) return new HashSet<Probe>();
        return geneSet.getProbes();
    }

    /**
     * @param terms
     * @return
     */
    public Collection<GeneSet> getGeneSets( Collection<GeneSetTerm> terms ) {
        Collection<GeneSet> res = new HashSet<GeneSet>();
        for ( GeneSetTerm t : terms ) {
            if ( this.geneSets.containsKey( t ) ) {
                res.add( geneSets.get( t ) );
            }
        }
        return res;
    }

    public Multifunctionality getMultifunctionality() {
        return multifunctionality;
    }

    /**
     * @return the list of probes.
     */
    public Collection<Probe> getProbes() {
        return this.probes;
    }

    /**
     * @return the set of gene set terms which are skipped due to redundancy.
     */
    public Collection<GeneSetTerm> getRedundant() {
        return skipDueToRedundancy;
    }

    /**
     * Get the gene sets that are user-defined.
     * 
     * @return
     */
    public Set<GeneSetTerm> getUserDefined() {
        Set<GeneSetTerm> result = new HashSet<GeneSetTerm>();
        for ( GeneSetTerm term : geneSets.keySet() ) {
            GeneSet gs = geneSets.get( term );
            if ( gs.isUserDefined() ) {
                result.add( term );
            }
        }
        return result;
    }

    public boolean hasGene( Gene g ) {
        return this.genes.contains( g );
    }

    /**
     * @param id
     * @return
     */
    public boolean hasGeneSet( GeneSetTerm id ) {
        return this.geneSets.containsKey( id );
    }

    /**
     * @param probeId
     * @return
     */
    public boolean hasProbe( Probe probeId ) {
        return this.probes.contains( probeId );
    }

    /**
     * Redefine a class.
     * 
     * @param classId String class to be modified
     * @param probesForNew Collection current user-defined list of members. The gene set is recreated to look like this
     *        one.
     */
    public void modifyGeneSet( GeneSetTerm classId, Collection<Probe> probesForNew ) {
        assert allowModification;
        if ( !geneSets.containsKey( classId ) ) {
            log.warn( "No such class to modify: " + classId );
            return;
        }

        // // FIXME have the gene set save this internally?
        // log.debug( "Saving backup version of " + classId + ", replacing with new version that has "
        // + probesForNew.size() + " probes." );
        // oldGeneSets.put( classId, geneSets.get( classId ) );
        // addSet( classId, probesForNew );
    }

    /**
     * Compute how many genes have Gene set annotations.
     * 
     * @return
     */
    public int numAnnotatedGenes() {
        int count = 0;
        for ( Gene gene : genes ) {
            Collection<GeneSetTerm> element = gene.getGeneSets();
            if ( element.size() > 0 ) {
                count++;
            }
        }
        return count;
    }

    /**
     * How many genes are currently available
     */
    public int numGenes() {
        return this.getGenes().size();
    }

    /**
     * Get the number of gene sets currently available.
     * 
     * @return
     */
    public int numGeneSets() {
        return this.getActiveGeneSets().size();
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
     * @return
     */
    public int numProbes() {
        return probes.size();
    }

    /**
     * Get how many probes point to the same gene. This is like the old "numReplicates".
     * 
     * @param g
     * @return
     */
    public int numProbesForGene( Gene g ) {
        return g.getProbes().size();
    }

    /**
     * Get the number of probes in a gene set, identified by id.
     * 
     * @param id String a class id
     * @return int number of probes in the class
     */
    public int numProbesInGeneSet( GeneSetTerm id ) {
        if ( !geneSets.containsKey( id ) ) {
            return 0;
        }
        GeneSet geneSet = geneSets.get( id );
        return geneSet.getProbes().size();
    }

    /**
     * Print out the gene annotations in the same format we got them in, but if the gene sets have been modified, this
     * will be reflected.
     * 
     * @param out
     * @throws IOException
     */
    public void print( Writer out ) throws IOException {
        out.write( "Probe\tSymbol\tName\tGeneSets\n" );
        out.flush();
        for ( Probe probe : probes ) {
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
     * Restore the previous version of a gene set. If no previous version is found, then nothing is done.
     * 
     * @param id
     */
    public void restoreGeneSet( GeneSetTerm id ) {
        if ( !oldGeneSets.containsKey( id ) ) return;
        log.info( "Restoring " + id );
        deleteGeneSet( id );
        addGeneSet( id, oldGeneSets.get( id ).getGenes() );
    }

    /**
     * Make the selection the user-defined sets only.
     */
    public Collection<GeneSetTerm> selectUserDefined() {

        Set<GeneSetTerm> result = new HashSet<GeneSetTerm>();
        for ( GeneSetTerm term : geneSets.keySet() ) {
            if ( term.isUserDefined() ) {
                result.add( term );
            }
        }

        return result;
    }

    /**
     * @param m
     */
    public void setMessenger( StatusViewer m ) {
        if ( m == null ) return;
        this.messenger = m;
    }

    /**
     * @param id
     * @return true if this set is redundant with another set; it should not be used in analysis.
     */
    public boolean skipDueToRedundancy( GeneSetTerm id ) {
        return this.skipDueToRedundancy.contains( id );
    }

    /**
     * @return
     * @deprecated
     */
    public TableModel toTableModel() {
        final List<Probe> pL = new ArrayList<Probe>( probes );
        return new AbstractTableModel() {
            private static final long serialVersionUID = 1L;
            private String[] columnNames = { "Probe", "Gene", "Description" };

            public int getColumnCount() {
                return 3;
            }

            @Override
            public String getColumnName( int i ) {
                return columnNames[i];
            }

            public int getRowCount() {
                return probes.size();
            }

            public Object getValueAt( int i, int j ) {
                Probe probeid = pL.get( i );
                switch ( j ) {
                    case 0:
                        return probeid;
                    case 1:
                        return probeid.getGene().getSymbol();
                    case 2:
                        return probeid.getDescription();
                    default:
                        return null;
                }
            }

        };
    }

    /**
     * Recompute all the things that depend on how many active probes or genes there are.
     */
    public void updateActiveItems() {

        // when a probe is made active, that automatically propagates to the genes. But gene sets need to be updated.
        for ( GeneSetTerm gs : this.getActiveGeneSets() ) {
            gs.reset();
        }

    }

    /**
     * Add the parents of each term to the association for each gene.
     * 
     * @param ga
     * @param goNames
     */
    private void addParents() {

        if ( messenger != null ) {
            messenger.showStatus( "Adding parent terms gene sets " );
        }
        Map<Gene, Collection<GeneSetTerm>> toBeAdded = new HashMap<Gene, Collection<GeneSetTerm>>();
        Map<GeneSetTerm, Collection<GeneSetTerm>> parentCache = new HashMap<GeneSetTerm, Collection<GeneSetTerm>>();
        int count = 0;
        for ( Gene gene : genes ) {

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

            if ( ++count % 1000 == 0 && messenger != null ) {
                messenger.showStatus( count + " genes examined for term parents ..." );
            }
        }
        for ( Gene g : toBeAdded.keySet() ) {
            Collection<GeneSetTerm> parents = toBeAdded.get( g );
            addAnnotation( g, parents );
        }

        if ( messenger != null ) {
            messenger.showStatus( "Added parents for all terms " );
        }
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
     */
    private void formGeneSets() {
        this.geneSets = new HashMap<GeneSetTerm, GeneSet>();
        for ( Gene g : this.genes ) {
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
     * Remove classes that have too few members, or which are obsolete. These are not removed from the GO tree
     * 
     * @param lowThreshold
     * @param highThreshold
     */
    private void prune( int lowThreshold, int highThreshold ) {

        if ( this.geneSets.isEmpty() ) {
            throw new IllegalStateException( "There are no gene sets" );
        }

        Set<GeneSetTerm> removeUs = new HashSet<GeneSetTerm>();
        int obsoleteRemoved = 0;
        int tooBigRemoved = 0;
        int tooSmallRemoved = 0;
        for ( GeneSetTerm id : geneSets.keySet() ) {

            if ( id.getAspect() == null || id.getDefinition().startsWith( "OBSOLETE" ) ) {
                obsoleteRemoved++;
                removeUs.add( id );
            } else {
                int numP = numProbesInGeneSet( id );
                int numG = numGenesInGeneSet( id );
                if ( numP < lowThreshold || numG < lowThreshold ) {
                    tooSmallRemoved++;
                    removeUs.add( id );
                } else if ( numP > highThreshold || numG > highThreshold ) {
                    tooBigRemoved++;
                    removeUs.add( id );
                }
            }
        }

        if ( !removeUs.isEmpty() ) {
            this.messenger.showStatus( removeUs.size() + " removed: obsolete (" + obsoleteRemoved + "), too small ("
                    + tooSmallRemoved + ") or too big (" + tooBigRemoved + ") terms pruned." );
        }

        for ( GeneSetTerm id : removeUs ) {
            deleteGeneSet( id ); // gone forever.
        }

        if ( this.geneSets.isEmpty() ) {
            throw new IllegalStateException(
                    "All gene sets were removed due to being too small, too big, or obsolete (" + obsoleteRemoved
                            + ") size range=" + lowThreshold + " - " + highThreshold
                            + ". Your annotation file may contain too few GO terms." );
        }

    }

    /**
     * Identify classes which are absoluely identical to others. This isn't superfast. This should be called after
     * adding parents.
     */
    private void redundancyCheck() {

        messenger.showStatus( "There are " + numGeneSets()
                + " gene sets in the annotations (of any size). Checking for redundancy ..." );

        Collection<GeneSet> checked = new HashSet<GeneSet>();
        int i = 0;
        int numToSkip = 0;
        for ( GeneSet gs1 : this.geneSets.values() ) {
            gs: for ( GeneSet gs2 : this.geneSets.values() ) {
                if ( gs1.equals( gs2 ) || checked.contains( gs2 ) ) continue;

                Collection<Gene> genes1 = gs1.getGenes();
                Collection<Gene> genes2 = gs2.getGenes();

                if ( genes1.size() != genes2.size() ) continue; // not identical.

                for ( Gene g1 : genes1 ) {
                    if ( !genes2.contains( g1 ) ) continue gs; // not redundant.
                }

                gs1.getRedundantGroups().add( gs2 );
                gs2.getRedundantGroups().add( gs1 );

                if ( gs1.isSkipDueToRedundancy() || gs2.isSkipDueToRedundancy() ) {
                    continue;
                }

                /*
                 * Choose one to mark as skippable; favor keeping the child term.
                 */
                if ( this.geneSetTerms.isParent( gs1.getTerm(), gs2.getTerm() ) ) {
                    gs1.setSkipDueToRedundancy( true );
                    this.skipDueToRedundancy.add( gs1.getTerm() );
                } else {
                    gs2.setSkipDueToRedundancy( true );
                    this.skipDueToRedundancy.add( gs2.getTerm() );
                }
                numToSkip++;

            }
            checked.add( gs1 );

            if ( ++i % 500 == 0 ) {
                messenger.showStatus( checked.size() + " sets checked for redundancy, " + numToSkip + " found ..." );
            }

        }

        messenger.showStatus( numToSkip + " gene sets will be ignored in analysis due to redundancy" );

    }

    /**
     * Used during subcloning of annotations. Make use of the fact that if two sets are redundant before, they will
     * still be redundant even if we remove probes (as long as there are any probes left).
     * 
     * @param start the clone source
     */
    private void redundancyCheck( GeneAnnotations start ) {

        for ( GeneSet gs1 : this.geneSets.values() ) {
            GeneSet originalGeneSet = start.getGeneSet( gs1.getTerm() );

            // we can't have _more_ gene sets, but sometimes something funn can happen - GO:0001775
            if ( originalGeneSet == null ) {
                log.warn( gs1.getTerm() + " missing from source" );
                continue;
            }

            if ( originalGeneSet.isSkipDueToRedundancy() ) {
                gs1.setSkipDueToRedundancy( true );
                this.skipDueToRedundancy.add( gs1.getTerm() );
            }

            for ( GeneSet redund : originalGeneSet.getRedundantGroups() ) {
                gs1.getRedundantGroups().add( this.getGeneSet( redund.getTerm() ) );
            }

        }
    }

    /**
     * Modify a collection of terms so there are none flagged as "skip due to redundancy"
     * 
     * @param terms
     * @return true if any terms were removed.
     */
    private boolean removeRedundant( Collection<GeneSetTerm> terms ) {
        return terms.removeAll( skipDueToRedundancy );
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

        addParents();

        formGeneSets();

        assert !this.geneSets.isEmpty();

        prune( ABSOLUTE_MINIMUM_GENESET_SIZE, PRACTICAL_MAXIMUM_GENESET_SIZE );

        redundancyCheck();

        this.multifunctionality = new Multifunctionality( this );

    }

    /**
     * Less intensive setup for when we are cloning a starting point.
     * 
     * @param start
     */
    private void setUp( GeneAnnotations start ) {

        formGeneSets();

        assert !this.geneSets.isEmpty();
        prune( ABSOLUTE_MINIMUM_GENESET_SIZE, PRACTICAL_MAXIMUM_GENESET_SIZE );

        redundancyCheck( start );

        this.multifunctionality = new Multifunctionality( this );
    }

}

class ClassSizeComparator implements Comparator<GeneSet>, Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
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

// used for the comparator.
