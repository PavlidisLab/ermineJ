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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ubic.basecode.util.StatusStderr;
import ubic.erminej.Settings;

/**
 * @author pavlidis
 */
public class TestGeneAnnotations {

    private static GeneSetTerms goNames;

    @BeforeClass
    public static void before() {
        try (InputStream i = new GZIPInputStream( TestGOParser.class.getResourceAsStream( "/data/goslim_generic.obo.txt.gz" ) )) {
            goNames = new GeneSetTerms( i );
            i.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    private GeneAnnotations ga;
    private List<Gene> geneIds;
    private List<Collection<GeneSetTerm>> goIds;
    private InputStream imb;

    private List<String> elements;

    private Settings settings;

    /*
     * @see TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {

        elements = new ArrayList<>();
        elements.add( "a" );
        elements.add( "b" );
        elements.add( "c" );
        geneIds = new ArrayList<>();
        Gene agene = new Gene( "aGene" );
        Gene bgene = new Gene( "bGene" );
        Gene cgene = new Gene( "cGene" );
        agene.addElement( new Element( "a" ) );
        bgene.addElement( new Element( "b" ) );
        cgene.addElement( new Element( "c" ) );

        geneIds.add( agene );
        geneIds.add( bgene );
        geneIds.add( cgene );
        goIds = new ArrayList<>();
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

        imb = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.sample-goidddelimittest.txt" );

        try (InputStream im = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.sample.txt" )) {

            GeneAnnotationParser p = new GeneAnnotationParser( goNames );
            settings = new Settings( false );
            settings.setUseUserDefined( false );
            settings.setLoadUserDefined( false ); // important for test accuracy.
            ga = p.readDefault( im, null, settings, false );
            ga.setMessenger( new StatusStderr() );
        }

    }

    @Test
    public void testAddGeneSet() {
        List<Gene> newGeneSet = new ArrayList<>();
        Gene g = new Gene( "LARS2" );
        g.addElement( new Element( "34764_at" ) );
        g.addElement( new Element( "32636_f_at" ) );
        newGeneSet.add( g );
        GeneSetTerm term = new GeneSetTerm( "Foo" );
        ga.addGeneSet( term, newGeneSet );
        assertTrue( ga.hasGeneSet( term ) );
        assertTrue( ga.findGeneSet( "Foo" ) != null );

    }

    @Test
    public void testConstructPruned() {
        Set<Element> keepers = new HashSet<>();
        keepers.add( ga.findElement( "36949_at" ) );
        keepers.add( ga.findElement( "41208_at" ) );
        keepers.add( ga.findElement( "34764_at" ) );
        keepers.add( ga.findElement( "33338_at" ) );
        GeneAnnotations pruned = new GeneAnnotations( ga, keepers );
        assertEquals( 4, pruned.numGenes() );
        assertEquals( 4, pruned.numProbes() );
        assertEquals( 6, pruned.numGeneSets() ); // not checked by hand.
    }

    @Test
    public void testGeneAnnotationsApiA() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, new StatusStderr() );
        int actualValue = val.numGenes();
        int expectedValue = 3;
        assertEquals( expectedValue, actualValue );
    }

    @Test
    public void testGeneAnnotationsApiB() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, new StatusStderr() );
        int actualValue = val.numElementsForGene( val.findGene( "aGene" ) );
        int expectedValue = 1;
        assertEquals( expectedValue, actualValue );
    }

    @Test
    public void testGeneAnnotationsApiC() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, new StatusStderr() );
        Collection<GeneSetTerm> geneSets = val.findGene( "aGene" ).getGeneSets();
        int actualValue = geneSets.size();
        int expectedValue = 2;
        assertEquals( expectedValue, actualValue );
    }

    @Test
    public void testGetParents() {
        GeneSetTerm t = ga.findTerm( "GO:0031410" );
        GeneSetTerm p = ga.findTerm( "GO:0043226" );

        assertNotNull( t );
        assertNotNull( p );

        Collection<GeneSetTerm> pa = goNames.getAllParents( t );

        assertTrue( pa.contains( p ) );
    }

    @Test
    public void testGoNames() {

        //     GO:0008150: biological_process
        //        GO:0006810: transport
        //            GO:0006913: nucleocytoplasmic transport
        //            GO:0007034: vacuolar transport
        //            GO:0015031: protein transport
        //                GO:0006605: protein targeting
        //            GO:0016192: vesicle-mediated transport
        //            GO:0030705: cytoskeleton-dependent intracellular transport
        //            GO:0055085: transmembrane transport

        GeneSetTerm c = goNames.get( "GO:0006810" );
        assertNotNull( c );
        assertEquals( "biological_process", c.getAspect() );
        GeneSetTerm term = new GeneSetTerm( "GO:0015031" );
        Set<GeneSetTerm> children = goNames.getChildren( term );
        assertEquals( 1, children.size() );
        assertTrue( children.contains( new GeneSetTerm( "GO:0006605" ) ) );

        Set<GeneSetTerm> allC = goNames.getAllChildren( new GeneSetTerm( "GO:0006810" ) );
        assertEquals( 7, allC.size() );

        assertTrue( allC.contains( new GeneSetTerm( "GO:0030705" ) ) );

        Collection<GeneSetTerm> parents = goNames.getParents( term );
        assertEquals( 1, parents.size() );
        assertTrue( parents.contains( new GeneSetTerm( "GO:0006810" ) ) );
        assertEquals(
                "The directed movement of substances (such as macromolecules, small molecules, ions) "
                        + "or cellular components (such as complexes and organelles) into, out of or within a cell, or between cells, "
                        + "or within a multicellular organism by means of some agent such as a transporter, pore or motor protein.",
                c.getDefinition() );
    }

    @Test
    public void testReadCommaDelimited() throws Exception {
        GeneAnnotationParser p = new GeneAnnotationParser( goNames );
        GeneAnnotations g = p.readDefault( imb, null, settings, false );

        Element probe = g.findElement( "32304_at" );
        assertEquals( "PRKCA", probe.getGene().getSymbol() );

        int expectedValue = 5; // not checked by hand.
        int actualValue = probe.getGeneSets().size();
        assertEquals( expectedValue, actualValue );
    }

    @Test
    public void testReadDescription() {
        String actualValue = ga.findElement( "32304_at" ).getDescription();
        String expectedValue = "protein kinase C, alpha";
        assertEquals( expectedValue, actualValue );
    }

    @Test
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

    @Test
    public void testRemoveAspect() {
        GeneSetMapTools.removeAspect( ga, goNames, null, "cellular_component" );
        assertEquals( 0, ga.getGeneSetElements( new GeneSetTerm( "GO:0005739" ) ).size() );
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
        assertEquals( 2, ga.getGeneSetElements( geneset ).size() );
    }

    @Test
    public void testSimple() throws Exception {
        try (InputStream i = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.simpletest.txt" )) {
            GeneAnnotationParser p = new GeneAnnotationParser( goNames );
            GeneAnnotations r = p.readDefault( i, null, settings, true );
            assertEquals( 9, r.getGenes().size() );

            Gene g = r.findGene( "TAH1" );
            assertNotNull( g );
            assertTrue( g.getGeneSets().contains( new GeneSetTerm( "GO:0005737" ) ) );
        }
    }

    /**
     * Test the 'minimum.geneset.size' feature. @
     */
    @Test
    public void testSimpleMinSize() throws Exception {
        try (InputStream i = TestGeneAnnotations.class.getResourceAsStream( "/data/geneAnnotation.simpletest.txt" )) {
            GeneAnnotationParser p = new GeneAnnotationParser( goNames );
            settings.setProperty( "minimum.geneset.size", 1 );

            GeneAnnotations r = p.readDefault( i, null, settings, true );
            assertEquals( 9, r.getGenes().size() );
        }

    }

    @Test
    public void testToTableModel() {
        GeneAnnotations val = new GeneAnnotations( geneIds, goIds, null );
        TableModel actualValue = val.toTableModel();
        assertEquals( 3, actualValue.getColumnCount() );
    }

    /**
     * Check that the GO Terms parsed match ...
     *
     * @param gotr
     * @param id
     */
    private void check( String[] gotr, String id ) {
        Collection<GeneSetTerm> geneSets = ga.findElement( id ).getGeneSets();

        Set<GeneSetTerm> re = new HashSet<>();
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
                                + " in the geneAnnotations, but not found for the element." );
                    }
                }
            }
            assertTrue( "Element is missing annotation: " + t, geneSets.contains( t ) );
        }
    }
}