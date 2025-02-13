/*
 * The ermineJ project
 *
 * Copyright (c) 2011-2021 University of British Columbia
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
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotationParser.Format;

/**
 * @author paul
 */
public class TestMultifunctionality {

    @Test
    public void testMf1() throws Exception {
        Settings settings = new Settings();
        settings.setDoLog( false );
        settings.setLoadUserDefined( false );
        settings.setMaxClassSize( 100 );
        settings.setMinClassSize( 10 );
        settings.setBigIsBetter( true );
        /*
         * JG was provided with this file for cross-checking.
         */
        try (InputStream is = TestMultifunctionality.class
                .getResourceAsStream( "/data/multfunc.annot.testfile.withoutdups.txt" )) {

            ZipInputStream z = new ZipInputStream(
                    TestMultifunctionality.class.getResourceAsStream( "/data/go_daily-termdb.rdf-xml.zip" ) );
            z.getNextEntry();
            GeneSetTerms geneSets = new GeneSetTerms( z, true );

            assertEquals( 32508, geneSets.getGeneSets().size() ); // rechecked (includes the roots)

            assertNotNull( geneSets.getGraph().getRoot() );

            GeneAnnotationParser p = new GeneAnnotationParser( geneSets );

            GeneAnnotations ga = p.read( is, Format.DEFAULT, settings );

            assertEquals( 211, ga.getGeneSets().size() ); // jesse confirms

            assertEquals( 81, ga.getGenes().size() ); // jesse confirms

            Multifunctionality mf = ga.getMultifunctionality();

            assertEquals( "Wrong number of genes in multifunctionality", 81, mf.getNumGenes() ); // Jesse confirmed.

            assertEquals( "Wrong number of GO terms for gene", 80, mf.getNumGoTerms( new Gene( "PAX8" ) ) ); // Jesse
            // confirmed.

            // System.err.println( "-------------------------" );
            // for ( GeneSetTerm gs : ga.findGene( "PAX8" ).getGeneSets() ) {
            // System.err.println( gs.getId() );
            // }
            // System.err.println( "-------------------------" );

            // it's the second-most multifunctional (rank = 1 by our reckoning)
            assertEquals( 1.0, mf.getRawGeneMultifunctionalityRank( new Gene( "PAX8" ) ), 0.00001 ); // Jesse confirmed.

            assertEquals( 1.0 - 2.0 / 81.0, mf.getMultifunctionalityRank( new Gene( "PAX8" ) ), 0.01 );

            assertEquals( 0.23961, mf.getMultifunctionalityScore( new Gene( "PAX8" ) ), 0.001 ); // Jesse confirmed it is
                                                                                                 // 0.2396

            // from Jesse's results.
            assertEquals( 0.038061, mf.getMultifunctionalityScore( new Gene( "VMD2L2" ) ), 0.001 );
            assertEquals( 0, mf.getMultifunctionalityScore( new Gene( "ARMCX4" ) ), 0.0001 );
            assertEquals( 0.015758, mf.getMultifunctionalityScore( new Gene( "CRYZL1" ) ), 0.0001 );
            assertEquals( 0.0067805, mf.getMultifunctionalityScore( new Gene( "LOC201158" ) ), 0.0001 );
            assertEquals( 0.13531, mf.getMultifunctionalityScore( new Gene( "CCL5" ) ), 0.0001 );

            // actual rank should be 124. I checked this in R with (see test_mf.roc_scores.gillis.txt)
            // cbind(test_mf.roc_scores.gillis,rank(-test_mf.roc_scores.gillis[,2]))
            // assertEquals( 1.0 - 124.0 / 211.0, mf.getGOTermMultifunctionalityRank( new GeneSetTerm( "GO:0005634" ) ),
            // 0.001 );

            // more tests from Jesse's output.
            assertEquals( 0.8552036, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0005634" ) ), 0.001 );
            assertEquals( 0.86056, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0001882" ) ), 0.001 );
            assertEquals( 0.78394, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0019538" ) ), 0.001 );
            assertEquals( 0.53797, mf.getGOTermMultifunctionality( new GeneSetTerm( "GO:0044421" ) ), 0.001 );

            List<Gene> li = new ArrayList<>();
            li.add( new Gene( "EYA3" ) );
            li.add( new Gene( "EPHB3" ) );
            li.add( new Gene( "MAPK1" ) );
            li.add( new Gene( "PTPN21" ) );
            li.add( new Gene( "CYP2A6" ) );
            li.add( new Gene( "CCL5" ) );
            li.add( new Gene( "GSC" ) );
            li.add( new Gene( "PAX8" ) );
            li.add( new Gene( "THRA" ) );
            li.add( new Gene( "PXK" ) );
            li.add( new Gene( "GUCA1A" ) );
            li.add( new Gene( "DDR1" ) );
            li.add( new Gene( "RFC2" ) );
            li.add( new Gene( "HSPA6" ) );
            li.add( new Gene( "LOC201158" ) );
            li.add( new Gene( "SLC39A5" ) );
            li.add( new Gene( "ALG10" ) );
            li.add( new Gene( "UBE1L" ) );
            li.add( new Gene( "C6orf199" ) );
            li.add( new Gene( "foonotagene" ) );
            assertEquals( 0.707017, mf.correlationWithGeneMultifunctionality( li ), 0.001 ); // not checked by hand.

            Map<Gene, Double> geneToScoreMap = new LinkedHashMap<>();
            int i = li.size();
            for ( Gene g : li ) {
                // these are 'already log-transformed'.
                geneToScoreMap.put( g, ( double ) i-- );
            }
            Map<Gene, Double> adjustedScores = mf.adjustScores( geneToScoreMap, false, true );

            // for ( Gene g : li ) {
            // System.err.println( adjustedScores.get( g ) );
            // System.err.println( String.format( "%s\t%.4g\t%.4g", g, mf.getMultifunctionalityRank( g ),
            // geneToScoreMap.get( g ) ) );
            // }

            // mfscorestest <-
            // read.delim("C:/Users/paul/dev/eclipseworkspace/ermineJ/ermineJ-app/src/test/data/mfscorestest.txt")
            // cat(residuals(lm(mfscorestest$GeneScore ~mfscorestest$MFRank , weights =1/c(1:20)) ), sep=",")
            // cat(rstudent(lm(mfscorestest$GeneScore ~mfscorestest$MFRank , weights =1/c(1:20)) ), sep=",")

            // here are the plain old residuals.
            // double[] expectedAdjustedScores = new double[] { 3.033775, 1.427687, -0.1784013, 2.153857, 0.3980867,
            // -2.268042, 1.578209, -5.480219, -4.360137, -2.329696, -4.240055, -8.87413, -8.661954, -3.603526,
            // 0.848813, -7.723608, -0.5450989, -5.785262, -4.66518, 6.451675 };
            // stduentized
            // double[] expectedAdjustedScoresStudentized = new double[] { 2.801387, 0.7126326, -0.06987865, 0.7145039,
            // 0.1154752, -0.6070027, 0.3899923, -1.318726, -0.9556866, -0.475843, -0.8333633, -1.788133, -1.649778,
            // -0.6296883, 0.1493991, -1.29153, -0.0898998, -0.9070473, -0.7163117, 1.228161 };

            /*
             * Square root.
             */
            // mfscorestest <-
            // read.delim("C:/Users/paul/dev/eclipseworkspace/ermineJ/ermineJ-app/src/test/data/mfscorestest.txt")
            // cat(residuals(lm(mfscorestest$GeneScore ~mfscorestest$MFRank , weights =1/sqrt(c(1:20))) ), sep=",")
            // cat(rstudent(lm(mfscorestest$GeneScore ~mfscorestest$MFRank , weights =1/sqrt(c(1:20))) ), sep=",")
            // plain residuals with square-root weighting.
            // double[] expectedAdjustedScores =
            // {4.631408,3.10432,1.577231,3.475151,1.817891,-0.6310691,2.583505,-3.685246,-2.841503,-1.206061,-2.99776,-7.158158,-7.103981,-2.835229,0.9064338,-6.678972,-0.5664777,-5.253963,-4.410221,5.127282}

            // studentized.
            double[] expectedAdjustedScoresStudentized = { 2.292787, 1.146938, 0.507715, 1.032061, 0.4980303, -0.1650407,
                    0.6509431, -0.921432, -0.6719097, -0.2733478, -0.6696485, -1.685027, -1.618964, -0.5984784, 0.1954458,
                    -1.414458, -0.1186639, -1.07232, -0.8935964, 1.3674652 };

            for ( int j = 0; j < expectedAdjustedScoresStudentized.length; j++ ) {
                assertEquals( expectedAdjustedScoresStudentized[j], adjustedScores.get( li.get( j ) ), 0.01 );
            }
            settings.setLoadUserDefined( true );
        }
    }
}