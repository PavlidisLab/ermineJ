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

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;

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

            GeneAnnotations g = new GeneAnnotations( ism, null, null, null );

            InputStream is = this.getClass().getResourceAsStream( "/data/go_test_termdb.xml" );

            GONames gonames = new GONames( is );

            Settings settings = new Settings();

            UserDefinedGeneSetManager.init( g, gonames, settings );
            needInit = false;
        }
    }

    public final void testKegg() throws Exception {
        Collection<UserDefinedGeneSet> keggsets = UserDefinedGeneSetManager.loadUserGeneSetFile( this.getClass()
                .getResourceAsStream( "/data/genesets/kegg.txt" ) );
        assertEquals( 186, keggsets.size() );
    }

    public final void testMulti() throws Exception {

    }

    public final void testSingle() throws Exception {

    }

    public final void testNoGO() throws Exception {

    }

    public final void testUpdateCustom() throws Exception {

    }

}
