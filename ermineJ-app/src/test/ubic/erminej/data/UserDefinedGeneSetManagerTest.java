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

import java.io.InputStream;
import java.util.Collection;

import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotationParser.Format;

import junit.framework.TestCase;

/**
 * @author paul
 * @version $Id$
 */
public class UserDefinedGeneSetManagerTest extends TestCase {

    static boolean needInit = true;

    @Override
    public void setUp() throws Exception {
        if ( needInit ) {
            InputStream ism = UserDefinedGeneSetManagerTest.class.getResourceAsStream( "/data/HG-U95A.an.txt" );
            InputStream is = this.getClass().getResourceAsStream( "/data/go_daily-termdb.rdf-sample2.xml" );

            GeneSetTerms gonames = new GeneSetTerms( is );
            GeneAnnotationParser p = new GeneAnnotationParser( gonames );
            GeneAnnotations g = p.read( ism, Format.DEFAULT );

            Settings settings = new Settings();

            UserDefinedGeneSetManager.init( g, settings );
            needInit = false;
        }
    }

    public final void testKegg() throws Exception {
        Collection<GeneSet> keggsets = UserDefinedGeneSetManager.loadUserGeneSetFile( this.getClass()
                .getResourceAsStream( "/data/genesets/kegg.txt" ) );
        assertEquals( 186, keggsets.size() );
    }

    public final void testMulti() throws Exception {
        Collection<GeneSet> sets = UserDefinedGeneSetManager.loadUserGeneSetFile( this.getClass().getResourceAsStream(
                "/data/genesets/my.test-classes.txt" ) );
        assertEquals( 3, sets.size() );
        for ( GeneSet geneSet : sets ) {
            assertTrue( geneSet.getId().contains( "my" ) );
            assertTrue( geneSet.getName().contains( "test" ) );
            for ( Probe p : geneSet.getProbes() ) {
                assertTrue( p.getName().endsWith( "_at" ) );
            }
        }
    }

    public final void testSingle() throws Exception {
        Collection<GeneSet> sets = UserDefinedGeneSetManager.loadUserGeneSetFile( this.getClass().getResourceAsStream(
                "/data/genesets/GO-0004994-class.txt" ) );
        assertEquals( 1, sets.size() );
        GeneSet s = sets.iterator().next();
        assertEquals( "GO:0004994", s.getId() );
        assertEquals( "somatostatin receptor activity", s.getName() );
        assertEquals( 7, s.getProbes().size() ); // six lines but we pull in another via the gene symbol.
        assertEquals( 6, s.getGenes().size() );
    }

    // public final void testNoGO() throws Exception {
    //
    // }
    //
    // public final void testUpdateCustom() throws Exception {
    //
    // }

}