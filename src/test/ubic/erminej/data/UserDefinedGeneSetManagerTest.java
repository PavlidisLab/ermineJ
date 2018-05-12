/*
 * The ermineJ project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.data.UserDefinedGeneSetManager.GeneSetFileFormat;

/**
 * @author paul
 * @version $Id$
 */
public class UserDefinedGeneSetManagerTest {

    GeneAnnotations geneAnnots;

    UserDefinedGeneSetManager manager;

    @Before
    public void setUp() throws Exception {

        InputStream ism = UserDefinedGeneSetManagerTest.class.getResourceAsStream( "/data/HG-U95A.an.txt" );
        InputStream is = this.getClass().getResourceAsStream( "/data/go_daily-termdb.rdf-sample2.xml" );

        GeneSetTerms gonames = new GeneSetTerms( is, false );
        GeneAnnotationParser p = new GeneAnnotationParser( gonames );
        geneAnnots = p.read( ism, Format.DEFAULT, new Settings() );

        SettingsHolder settings = new Settings();

        manager = new UserDefinedGeneSetManager( geneAnnots, settings, null );

    }

    @Test
    public final void testKegg() throws Exception {

        String filePath = new File( this.getClass().getResource( "/data/genesets/kegg.txt" ).toURI() )
                .getAbsolutePath();
        Collection<GeneSet> keggsets = manager.loadUserGeneSetFile( filePath );
        assertEquals( 186, keggsets.size() );
        for ( GeneSet geneSet : keggsets ) {
            assertEquals( filePath, geneSet.getSourceFile() );
            assertEquals( GeneSetFileFormat.LINE_BASED, geneSet.getFormat() );
        }
    }

    @Test
    public final void testMulti() throws Exception {
        Collection<GeneSet> sets = manager.loadUserGeneSetFile( this.getClass().getResourceAsStream(
                "/data/genesets/my.test-classes.txt" ) );
        assertEquals( 3, sets.size() );
        for ( GeneSet geneSet : sets ) {
            assertEquals( GeneSetFileFormat.DEFAULT, geneSet.getFormat() );
            assertTrue( geneSet.getId().contains( "my" ) );
            assertTrue( "Got: " + geneSet.getName(), geneSet.getName().contains( "test" ) );
            for ( Element p : geneSet.getProbes() ) {
                assertTrue( p.getName().endsWith( "_at" ) );
            }
        }
    }

    @Test
    public final void testSingle() throws Exception {
        Collection<GeneSet> sets = manager.loadUserGeneSetFile( this.getClass().getResourceAsStream(
                "/data/genesets/GO-0004994-class.txt" ) );
        assertEquals( 1, sets.size() );
        GeneSet s = sets.iterator().next();
        assertEquals( "GO:0004994", s.getId() );
        assertEquals( "Got: " + s.getName(), "somatostatin receptor activity", s.getName() );
        assertEquals( 7, s.getProbes().size() ); // six lines but we pull in another via the gene symbol.
        assertEquals( 6, s.getGenes().size() );
    }

}
