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
import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;
import ubic.erminej.analysis.GeneSetSizeComputer;
import ubic.erminej.analysis.OraPvalGenerator;
import ubic.erminej.data.GeneScores;

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
    protected GONames gon = null;
    protected GeneSetSizeComputer sizeComputer = null;

    @Override
    protected void setUp() throws Exception {
        ism = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/test.an.txt" );
        is = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/test.scores.txt" );
        isi = AbstractPvalGeneratorTest.class.getResourceAsStream( "/data/go_test_termdb.xml" );

        if ( ism == null || is == null || isi == null ) throw new IOException();

        s = new Settings();
        s.setGeneScoreThreshold( 0.015 );
        s.setMinClassSize( 2 );
        s.setScoreCol( 2 );
        s.setDoLog( true );
        s.setBigIsBetter( false );
        s.setUseBiologicalProcess( true );
        s.setUseCellularComponent( true );
        s.setUseMolecularFunction( true );

        annotations = new GeneAnnotations( ism, null, null, null );

        assertTrue( annotations.getGenes().size() > 0 );
        assertTrue( annotations.getGeneSets().size() > 0 );

        scores = new GeneScores( is, s, null, annotations );

        assertNotNull( scores.getGeneScores() );
        assertNotNull( scores.getProbeToScoreMap() );

        gon = new GONames( isi );
        sizeComputer = new GeneSetSizeComputer( scores.getProbeToScoreMap().keySet(), annotations, scores, true );

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        isi.close();
        ism.close();
        is.close();
        super.tearDown();
    }

}