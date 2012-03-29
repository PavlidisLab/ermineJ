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
package ubic.erminej.analysis;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import junit.framework.TestCase;

import hep.aida.bin.QuantileBin1D;
import ubic.erminej.Settings;
import ubic.erminej.SettingsHolder;
import ubic.erminej.data.GeneAnnotationParser;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScoreReaderTest;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetTerms;

/**
 * @author paul
 * @version $Id$
 */
public class ScoreQuantileTest extends TestCase {

    private GeneScores test;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        SettingsHolder s = new Settings( false );

        InputStream is = GeneScoreReaderTest.class.getResourceAsStream( "/data/test.scores.txt" );

        InputStream ism = GeneScoreReaderTest.class.getResourceAsStream( "/data/test.an.txt" );

        GeneSetTerms geneSetTerms = new GeneSetTerms( GeneScoreReaderTest.class
                .getResourceAsStream( "/data/go-termdb-test.xml" ) );
        GeneAnnotationParser p = new GeneAnnotationParser( geneSetTerms );
        GeneAnnotations g = p.readDefault( ism, null, s );

        test = new GeneScores( is, s, null, g );
        super.setUp();
    }

    public final void testQ1() throws Exception {

        System.err.println( StringUtils.join( test.getProbeScores(), "," ) );

        QuantileBin1D q = ScoreQuantiles.computeQuantiles( new Settings( false ), test );
        double quantile = q.quantile( 0.5 );
        assertEquals( 0.698, quantile, 0.001 ); // median
        assertEquals( 0.04575, q.quantile( 0 ), 0.0001 ); // min
        assertEquals( 2, q.quantile( 1 ), 0.0001 ); // max

        double quantileInverse = q.quantileInverse( 2 );
        assertEquals( 1, quantileInverse, 0.001 );
        assertEquals( 0, q.quantileInverse( 0 ), 0.001 );
        assertEquals( 0.31, q.quantileInverse( 0.39794001 ), 0.01 );

    }

}
