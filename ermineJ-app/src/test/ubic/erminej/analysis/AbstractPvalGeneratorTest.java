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
package ubic.erminej.analysis;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder.MultiProbeHandling;
import ubic.erminej.data.GeneAnnotationParser;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSetTerms;

/**
 * @author pavlidis
 * @version $Id$
 */
public abstract class AbstractPvalGeneratorTest extends TestCase {
    protected OraPvalGenerator test = null;
    protected GeneAnnotations annotations = null;
    protected GeneScores scores = null;
    protected InputStream is = null;
    protected InputStream ism = null;
    protected InputStream isi = null;
    protected Settings s = null;
    protected GeneSetTerms gon = null;

    @Override
    protected void setUp() throws Exception {
        ism = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/test.an.txt" );
        is = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/test.scores.txt" );
        isi = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/go-termdb-test.xml" );

        if ( ism == null || is == null || isi == null ) throw new IOException();

        s = new Settings( false );
        s.setGeneScoreThreshold( 0.015 );
        s.setMinClassSize( 2 );
        s.setMaxClassSize( 200 );
        s.setScoreCol( 2 );
        s.setUseMultifunctionalityCorrection( false );
        s.setDoLog( true );
        s.setBigIsBetter( false );
        s.setUseBiologicalProcess( true );
        s.setUseCellularComponent( true );
        s.setUseMolecularFunction( true );
        s.setUseUserDefined( false );
        s.setLoadUserDefined( false );
        s.setGeneRepTreatment( MultiProbeHandling.MEAN );
        s.setScoreFile( AbstractPvalGeneratorTest.class.getResource( "/data/test.scores.txt" ).getFile() );
        s.setAnnotFile( AbstractPvalGeneratorTest.class.getResource( "/data/test.an.txt" ).getFile() );
        s.setClassFile( AbstractPvalGeneratorTest.class.getResource( "/data/go-termdb-test.xml" ).getFile() );

        gon = new GeneSetTerms( isi );
        GeneAnnotationParser p = new GeneAnnotationParser( gon );
        annotations = p.read( ism, Format.DEFAULT, s );

        assertTrue( annotations.getGenes().size() > 0 );
        assertTrue( annotations.getGeneSetTerms().size() > 0 );

        for ( GeneSetTerm t : annotations.getGeneSetTerms() ) {
            assertNotNull( t.getAspect() );
        }

        scores = new GeneScores( is, s, null, annotations );

        assertNotNull( scores.getGeneScores() );
        assertNotNull( scores.getProbeToScoreMap() );

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        isi.close();
        ism.close();
        is.close();
        s.setLoadUserDefined( true );
        super.tearDown();
    }

}