/*
 * The baseCode project
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import ubic.basecode.util.StatusStderr;
import ubic.erminej.Settings;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneSetTerm;

import junit.framework.TestCase;

/**
 * @author pavlidis
 * @version $Id$
 */
public class TestGeneAnnotations extends TestCase {

    private InputStream is;
    private InputStream ia;
    private InputStream imb;
    private List<String> probes;
    private List<Gene> geneIds;
    private List<Collection<GeneSetTerm>> goIds;
    private static GeneSetTerms goNames;

    private GeneAnnotations ga;
    private Settings settings;

    static {

        try {
            ZipInputStream z = new ZipInputStream(
                    TestGeneAnnotations.class.getResourceAsStream( "/data/go_daily-termdb.rdf-xml.zip" ) );
            z.getNextEntry();
            goNames = new GeneSetTerms( z );
            z.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( SAXException e ) {
            e.printStackTrace();
        }

    }

    public void testAddGeneSet() {
        List<Gene> newGeneSet = new ArrayList<Gene>();
        Gene g = new Gene( "LARS2" );
        g.addProbe( new Probe( "34764_at" ) );
        g.addProbe( new Probe( "32636_f_at" ) );
        newGeneSet.add( g );
        GeneSetTerm term = new GeneSetTerm( "Foo" );
        ga.addGeneSet( term, newGeneSet );
        assertTrue( ga.hasGeneSet( term ) );
        assertTrue( ga.findGeneSet( "Foo" ) != null );

    }

    public void testConstructPruned() {
        Set<Probe> keepers = new HashSet<Probe>();
        keepers.add( ga.findProbe( "36949_at" ) );
        keepers.add( ga.findProbe( "41208_at" ) );
        keepers.add( ga.findProbe( "34764_at" ) );
        keepers.add( ga.findProbe( "33338_at" ) );
        GeneAnnotations pruned = new GeneAnnotations( ga, keepers );
        assertEquals( 4, pruned.numGenes() );
        assertEquals( 4, pruned.numProbes() );
        assertEquals( 59, pruned.numGeneSets() ); // not checked by hand.
    }

    public void testGeneAnnotationsApiA() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, new StatusStderr() );
        int actualValue = val.numGenes();
        int expectedValue = 3;
        assertEquals( expectedValue, actualValue );
    }

    public void testGeneAnnotationsApiB() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, new StatusStderr() );
        int actualValue = val.numProbesForGene( val.findGene( "aGene" ) );
        int expectedValue = 1;
        assertEquals( expectedValue, actualValue );
    }

    public void testGeneAnnotationsApiC() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, new StatusStderr() );
        Collection<GeneSetTerm> geneSets = val.findGene( "aGene" ).getGeneSets();
        int actualValue = geneSets.size();
        int expectedValue = 2;
        assertEquals( expectedValue, actualValue );
    }

    public void testGetParents() {
        GeneSetTerm t = ga.findTerm( "GO:0042246" );
        GeneSetTerm p = ga.findTerm( "GO:0048589" );

        assertNotNull( t );
        assertNotNull( p );

        Collection<GeneSetTerm> pa = goNames.getAllParents( t );

        assertTrue( pa.contains( p ) );
    }

    public void testGoNames() {
        // http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=GO:0005739 - "mitochondrion"
        GeneSetTerm cellComp = goNames.get( "GO:0005739" );
        assertNotNull( cellComp );
        assertEquals( "cellular_component", cellComp.getAspect() );
        GeneSetTerm term = new GeneSetTerm( "GO:0005739" );
        Set<GeneSetTerm> children = goNames.getChildren( term );
        assertEquals( 3, children.size() );
        assertTrue( children.contains( new GeneSetTerm( "GO:0016007" ) ) );

        Set<GeneSetTerm> allC = goNames.getAllChildren( term );
        assertEquals( 78, allC.size() ); // have not checked this by hand, but mitochondrion part has a lot.

        assertTrue( allC.contains( new GeneSetTerm( "GO:0005751" ) ) ); // few steps down.

        Collection<GeneSetTerm> parents = goNames.getParents( term );
        assertEquals( 2, parents.size() );
        assertTrue( parents.contains( new GeneSetTerm( "GO:0043231" ) ) );
        assertEquals( "A semiautonomous, self replicating organelle that occurs"
                + " in varying numbers, shapes, and sizes in the cytoplasm"
                + " of virtually all eukaryotic cells. It is notably the site of tissue respiration.",
                cellComp.getDefinition() );
    }

    public final void testReadAffyCsv() throws Exception {
        GeneAnnotationParser p = new GeneAnnotationParser( goNames, null );
        GeneAnnotations g = p.readAffyCsv( is, null, settings );
        Collection<GeneSetTerm> geneSets = g.getGeneSetTerms();
        assertTrue( geneSets.size() > 0 );
    }

    /**
     * Updated format @
     */
    public final void testReadAffyCsv2() throws Exception {
        // second affytest
        GZIPInputStream isa = new GZIPInputStream(
                TestGeneAnnotations.class
                        .getResourceAsStream( "/data/MoGene-1_0-st-v1.na31.mm9.transcript.sample.txt.gz" ) );
        GeneAnnotationParser p = new GeneAnnotationParser( goNames, null );
        GeneAnnotations g = p.readAffyCsv( isa, null, settings );
        Collection<GeneSetTerm> geneSets = g.getGeneSetTerms();
        assertTrue( geneSets.size() > 0 );
    }

    public final void testReadAffyCsv3() throws Exception {
        // second affytest
        InputStream isa = TestGeneAnnotations.class.getResourceAsStream( "/data/HG-U95A.affy.2011format.sample.csv" );
        GeneAnnotationParser p = new GeneAnnotationParser( goNames, null );
        GeneAnnotations g = p.readAffyCsv( isa, null, settings );
        Collection<GeneSetTerm> geneSets = g.getGeneSetTerms();
        assertTrue( geneSets.size() > 0 );

        // note that many terms in the file get pruned or collapsed.
        GeneSetTerm findTerm = g.findTerm( "GO:0044428" );
        assertNotNull( findTerm );

        GeneSetTerm term = new GeneSetTerm( "GO:0044428" );
        assertTrue( geneSets.contains( term ) );
        Collection<Gene> geneSetGenes = g.getGeneSetGenes( term );
        assertTrue( geneSetGenes.size() > 0 );
    }

    public void testReadAgilent() throws Exception {
        GeneAnnotationParser p = new GeneAnnotationParser( goNames );
        GeneAnnotations g = p.readAgilent( ia, null, settings );
        int actualValue = g.findProbe( "A_52_P311491" ).getGeneSets().size();
        assertEquals( 12, actualValue ); // not checked by hand.
    }

    public void testReadAgilent2012() throws Exception {
        GeneAnnotationParser p = new GeneAnnotationParser( goNames );
        InputStream a = TestGeneAnnotations.class.getResourceAsStream( "/data/agilentannots.test2012.txt" );
        GeneAnnotations g = p.readAgilent( a, null, settings );
        assertEquals( 37, g.getProbes().size() );
        assertEquals( 30, g.getGenes().size() ); // not checked by hand.
        Probe testp = g.findProbe( "A_84_P22735" );
        assertNotNull( testp );
        int actualValue = testp.getGeneSets().size();
        assertEquals( 23, actualValue ); // not checked by hand.
    }

    public void testReadCommaDelimited() throws Exception {
        GeneAnnotationParser p = new GeneAnnotationParser( goNames );
        GeneAnnotations g = p.readDefault( imb, null, settings, false );

        Probe probe = g.findProbe( "32304_at" );
        assertEquals( "PRKCA", probe.getGene().getSymbol() );

        int expectedValue = 113; // not checked by hand.
        int actualValue = probe.getGeneSets().size();
        assertEquals( expectedValue, actualValue );
    }

    public void testReadDescription() {
        String actualValue = ga.findProbe( "32304_at" ).getDescription();
        String expectedValue = "protein kinase C, alpha";
        assertEquals( expectedValue, actualValue );
    }

    public void testReadPipeDelimited() {

        String[] gotr = new String[] { "GO:0030856" };
        String id = "FOOFAKE_at";

        check( gotr, id );

        // ANOTHER TEST
        // // terms for 32304_at from the source file geneAnnotation.sample.txt, removing:
        // GO:0000074: obsolete.
        // GO:0007242 - has canonical id GO:0035556, which is not picked up.
        gotr = StringUtils.split( "GO:0000188|GO:0004672|GO:0004674|GO:0004682|GO:0004691|GO:0004697|"
                + "GO:0004698|GO:0004713|GO:0005509|GO:0005515|GO:0005524|GO:0005624|GO:0005634|"
                + "GO:0005737|GO:0005739|GO:0006468|GO:0006469|GO:0006937|GO:0007166|"
                + "GO:0008624|GO:0008629|GO:0016740|GO:0019992|GO:0030593|GO:0046325|GO:0046627|"
                + "GO:0050729|GO:0050730|GO:0050930", '|' );

        assertEquals( 29, gotr.length );

        check( gotr, "32304_at" );

    }

    public void testRemoveAspect() {
        GeneSetMapTools.removeAspect( ga, goNames, null, "cellular_component" );
        assertEquals( 0, ga.getGeneSetProbes( new GeneSetTerm( "GO:0005739" ) ).size() );
    }

    public void testRemoveBySize() {
        assertEquals( 259, ga.getGeneSetTerms().size() ); // not checked by hand.
        GeneSetMapTools.removeBySize( ga, null, 2, 5 );
        assertEquals( 196, ga.getGeneSetTerms().size() ); // not checked by hand
    }

    public void testSelectSetsByGene() {
        Collection<GeneSetTerm> selectedSets = ga.findSetsByGene( "LARS2" ); // from geneAnnotation.sample.txt
        assertEquals( "34764_at", ga.findGene( "LARS2" ).getProbes().iterator().next().getName() );
        GeneSetTerm geneset = new GeneSetTerm( "GO:0005739" );
        assertTrue( ga.hasGeneSet( geneset ) );
        assertEquals( 2, ga.numGenesInGeneSet( geneset ) );
        assertEquals( 59, selectedSets.size() ); // not checked by hand
        assertTrue( selectedSets.contains( geneset ) );
        assertEquals( 2, ga.getGeneSetProbes( geneset ).size() );
    }

    public void testToTableModel() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, null );
        TableModel actualValue = val.toTableModel();
        assertEquals( 3, actualValue.getColumnCount() );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        probes = new ArrayList<String>();
        probes.add( "a" );
        probes.add( "b" );
        probes.add( "c" );
        geneIds = new ArrayList<Gene>();
        Gene agene = new Gene( "aGene" );
        Gene bgene = new Gene( "bGene" );
        Gene cgene = new Gene( "cGene" );
        agene.addProbe( new Probe( "a" ) );
        bgene.addProbe( new Probe( "b" ) );
        cgene.addProbe( new Probe( "c" ) );

        geneIds.add( agene );
        geneIds.add( bgene );
        geneIds.add( cgene );
        goIds = new ArrayList<Collection<GeneSetTerm>>();
        goIds.add( new HashSet<GeneSetTerm>() );
        goIds.add( new HashSet<GeneSetTerm>() );
        goIds.add( new HashSet<GeneSetTerm>() );

        goIds.get( 0 ).add( new GeneSetTerm( "1" ) );
        goIds.get( 1 ).add( new GeneSetTerm( "1" ) );
        goIds.get( 2 ).add( new GeneSetTerm( "1" ) );
        goIds.get( 0 ).add( new GeneSetTerm( "2" ) );
        goIds.get( 1 ).add( new GeneSetTerm( "2" ) );
        for ( Collection<GeneSetTerm> gs : goIds ) {
            for ( GeneSetTerm g : gs )
                g.setAspect( "User-defined" );
        }

        is = TestGeneAnnotations.class.getResourceAsStream( "/data/HG-U133_Plus_2_annot_sample.csv" );
        imb = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.sample-goidddelimittest.txt" );
        ia = TestGeneAnnotations.class.getResourceAsStream( "/data/agilentannots.test.txt" );

        InputStream im = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.sample.txt" );

        GeneAnnotationParser p = new GeneAnnotationParser( goNames );
        settings = new Settings( false );
        settings.setUseUserDefined( false );
        settings.setLoadUserDefined( false ); // important for test accuracy.
        ga = p.readDefault( im, null, settings, false );
        ga.setMessenger( new StatusStderr() );

    }

    public void testSimple() throws Exception {
        InputStream i = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.simpletest.txt" );
        GeneAnnotationParser p = new GeneAnnotationParser( goNames );
        GeneAnnotations r = p.readDefault( i, null, settings, true );
        assertEquals( 9, r.getGenes().size() );

        Gene g = r.findGene( "TAH1" );
        assertNotNull( g );
        assertTrue( g.getGeneSets().contains( new GeneSetTerm( "GO:0005737" ) ) );
    }

    /**
     * Test the 'minimum.geneset.size' feature. @
     */
    public void testSimpleMinSize() throws Exception {
        InputStream i = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.simpletest.txt" );
        GeneAnnotationParser p = new GeneAnnotationParser( goNames );
        settings.setProperty( "minimum.geneset.size", 1 );

        GeneAnnotations r = p.readDefault( i, null, settings, true );
        assertEquals( 9, r.getGenes().size() );

    }

    public void testSimpleOldGo() throws Exception {
        InputStream i = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.simpletest.txt" );

        InputStream z = new GZIPInputStream(
                TestGeneAnnotations.class.getResourceAsStream( "/data/go_200109-termdb.xml.gz" ) );
        GeneSetTerms go = new GeneSetTerms( z, true );

        for ( GeneSetTerm g : go.getTerms() ) {
            assertNotNull( g.toString(), g.getAspect() );
        }

        GeneAnnotationParser p = new GeneAnnotationParser( go );
        GeneAnnotations r = p.readDefault( i, null, settings, true );
        assertEquals( 9, r.getGenes().size() );

        Gene g = r.findGene( "TAH1" );
        assertNotNull( g );
        assertTrue( g.getGeneSets().contains( new GeneSetTerm( "GO:0005737" ) ) );
    }

    /**
     * Check that the GO Terms parsed match ...
     * 
     * @param gotr
     * @param id
     */
    private void check( String[] gotr, String id ) {
        Collection<GeneSetTerm> geneSets = ga.findProbe( id ).getGeneSets();

        Set<GeneSetTerm> re = new HashSet<GeneSetTerm>();
        for ( String g : gotr ) {
            GeneSetTerm t = ga.findTerm( g );

            if ( t == null ) continue;
            if ( ga.hasGeneSet( t ) ) {
                if ( t.getDefinition().startsWith( "OBSOLETE" ) ) continue;
                if ( ga.getGeneSet( t ).size() >= ga.getMinimumGeneSetSize() ) re.add( t );
            }

            Collection<GeneSetTerm> allParents = ga.getGeneSetTermsHolder().getAllParents( t );

            for ( GeneSetTerm geneSetTerm : allParents ) {
                if ( geneSetTerm.getDefinition().startsWith( "OBSOLETE" ) ) continue;
                if ( ga.hasGeneSet( geneSetTerm ) && ga.getGeneSet( geneSetTerm ).size() >= ga.getMinimumGeneSetSize() ) {
                    re.add( geneSetTerm );
                }
            }

        }

        assertEquals( re.size(), geneSets.size() );
        for ( GeneSetTerm t : re ) {
            if ( !geneSets.contains( t ) ) {
                Set<GeneSetTerm> allChildren = ga.getGeneSetTermsHolder().getAllChildren( t );
                for ( String s : gotr ) {
                    if ( allChildren.contains( new GeneSetTerm( s ) ) ) {
                        System.err.println( t + " is a parent of listed term " + s
                                + " in the geneAnnotations, but not found for the probe." );
                    }
                }
            }
            assertTrue( "Probe is missing annotation: " + t, geneSets.contains( t ) );
        }
    }
}