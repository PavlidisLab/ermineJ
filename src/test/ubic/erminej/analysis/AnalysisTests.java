/*
 * The ermineJ project
 *
 * Copyright (c) 2013 University of British Columbia
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
package ubic.erminej.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipInputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import ubic.basecode.util.StatusStderr;
import ubic.erminej.Analyzer;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder.GeneScoreMethod;
import ubic.erminej.SettingsHolder.Method;
import ubic.erminej.SettingsHolder.MultiElementHandling;
import ubic.erminej.data.GeneAnnotationParser;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetTerms;
import ubic.erminej.data.TestGeneAnnotations;

/**
 * @author Paul
 */
public class AnalysisTests {

    protected static GeneAnnotations annotations = null;
    protected static GeneScores scores = null;
    protected static Settings s = null;

    @BeforeClass
    public static void setup() throws Exception {
        s = new Settings( false );
        s.setGeneScoreThreshold( 0.0015 );
        s.setMinClassSize( 20 );
        s.setMaxClassSize( 200 );
        s.setScoreCol( 2 );
        s.setDataCol( 2 );
        //  s.setUseMultifunctionalityCorrection( true );
        s.setDoLog( true );
        s.setBigIsBetter( false );
        s.setUseBiologicalProcess( true );
        s.setUseCellularComponent( true );
        s.setUseMolecularFunction( true );
        s.setUseUserDefined( false );
        s.setLoadUserDefined( false );
        s.setGeneRepTreatment( MultiElementHandling.MEAN );
        s.setIterations( 200 );
        s.setScoreFile( AbstractPvalGeneratorTest.class.getResource( "/data/one-way-anova-parsed.txt" ).getFile() );
        s.setAnnotFile( AbstractPvalGeneratorTest.class.getResource( "/data/HG-U95A.an.txt" ).getFile() );
        s.setClassFile( AbstractPvalGeneratorTest.class.getResource( "/data/go_daily-termdb.rdf-xml.zip" ).getFile() );
        s.setRawFile( AbstractPvalGeneratorTest.class.getResource( "/data/melanoma_and_sarcomaMAS5.txt" ).getFile() );

        ZipInputStream z = new ZipInputStream(
                TestGeneAnnotations.class.getResourceAsStream( "/data/go_daily-termdb.rdf-xml.zip" ) );
        z.getNextEntry();
        GeneSetTerms gon = new GeneSetTerms( z, false );
        z.close();

        GeneAnnotationParser p = new GeneAnnotationParser( gon );
        InputStream ism = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/HG-U95A.an.txt" );
        annotations = p.read( ism, Format.DEFAULT, s );
        ism.close();

        InputStream is = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/one-way-anova-parsed.txt" );
        scores = new GeneScores( is, s, null, annotations );
        is.close();
    }

    @Test
    public void corrTest() {
        s.setClassScoreMethod( Method.CORR );
        Analyzer a = new Analyzer( s, new StatusStderr(), annotations );
        a.run();
        Collection<GeneSetPvalRun> results = a.getLatestResults();
        assertEquals( 1, results.size() );
        GeneSetPvalRun result = results.iterator().next();
        assertNotNull( result.getGeneData() );
    }

    @Test
    public void gsrTest() {
        s.setClassScoreMethod( Method.GSR );
        s.setGeneSetResamplingScoreMethod( GeneScoreMethod.MEAN );
        s.setMaxClassSize( 100 );
        s.setMinClassSize( 10 );
        Analyzer a = new Analyzer( s, new StatusStderr(), annotations );
        a.run();
        Collection<GeneSetPvalRun> results = a.getLatestResults();
        assertEquals( 1, results.size() );
        GeneSetPvalRun result = results.iterator().next();
        assertNotNull( result.getGeneData() );
    }

    @Test
    public void oraTest() {
        s.setClassScoreMethod( Method.ORA );
        Analyzer a = new Analyzer( s, new StatusStderr(), annotations );
        a.run();
    }

    @Test
    public void preReTest() {
        s.setClassScoreMethod( Method.GSR );
        s.setGeneSetResamplingScoreMethod( GeneScoreMethod.PRECISIONRECALL );
        s.setMaxClassSize( 100 );
        s.setMinClassSize( 10 );
        Analyzer a = new Analyzer( s, new StatusStderr(), annotations );
        a.run();
        Collection<GeneSetPvalRun> results = a.getLatestResults();
        assertEquals( 1, results.size() );
        GeneSetPvalRun result = results.iterator().next();
        assertNotNull( result.getGeneData() );
    }

    @Test
    public void rocTest() {
        s.setClassScoreMethod( Method.ROC );
        Analyzer a = new Analyzer( s, new StatusStderr(), annotations );
        a.run();
        Collection<GeneSetPvalRun> results = a.getLatestResults();
        assertEquals( 1, results.size() );
        GeneSetPvalRun result = results.iterator().next();
        assertNotNull( result.getGeneData() );
    }
}
