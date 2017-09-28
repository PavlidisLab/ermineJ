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
package ubic.erminej;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.RegressionTesting;
import ubic.erminej.analysis.GeneSetPvalRun;

/**
 * Not complete! Non functional as it stands.
 *
 * @author pavlidis
 * @version $Id$
 */
public class classScoreTester extends ErmineJCli {
    private static Log log = LogFactory.getLog( classScoreTester.class.getName() );
    private static final double TOLERANCE = 0.001;

    @SuppressWarnings("unused")
    public static void main( String[] args ) {
        new classScoreTester();
    }

    // find settings in the data directory; read each one, run analyses on them.
    public classScoreTester() {
        super();

        BufferedReader dis = new BufferedReader( new InputStreamReader(
                classScoreTester.class.getResourceAsStream( "/data/test.configs.txt" ) ) );
        String configFileName = "";

        try {
            while ( ( configFileName = dis.readLine() ) != null ) {
                boolean doTest = true;

                if ( configFileName.startsWith( "#" ) ) continue;
                settings = new Settings( classScoreTester.class.getResource( configFileName ) );
                log.info( "Running: " + configFileName );

                String saveFileName = File.createTempFile( "ermineJ.tmp", ".txt" ).getAbsolutePath();
                log.info( "Results will be written to " + saveFileName );

                String goldStandardFileName = settings.getGoldStandardFile();
                if ( !new File( goldStandardFileName ).canRead() ) {
                    saveFileName = goldStandardFileName;
                    doTest = false;
                    log.error( "Could not read from " + goldStandardFileName );
                    log.info( "Creating test output" );
                }

                initialize();

                try {
                    GeneSetPvalRun result = analyze();
                    ResultsPrinter.write( saveFileName, result, false );
                } catch ( Exception e ) {
                    statusMessenger.showStatus( "Error During analysis: " + e );
                    e.printStackTrace();
                }

                if ( doTest ) {
                    String gs = RegressionTesting.readTestResultFromFile( goldStandardFileName );
                    String te = RegressionTesting.readTestResultFromFile( saveFileName );

                    // System.err.println( gs );
                    // System.err.println( te );

                    if ( compareResults( gs, te ) ) {
                        log.info( "Passed test" );
                    } else {
                        log.error( "Failed test" );
                    }
                } else {
                    log.info( "Skipping test, output instead" );
                }

            }
            dis.close();
        } catch ( IOException e ) {
            log.fatal( e, e );
        } catch ( ConfigurationException e ) {
            log.fatal( e, e );
        }

    }

    private boolean compareResults( String a, String b ) throws IOException {
        BufferedReader ina = new BufferedReader( new StringReader( a ) );
        BufferedReader inb = new BufferedReader( new StringReader( b ) );

        String linea;
        String lineb;
        int lineNum = 0;
        while ( ( linea = ina.readLine() ) != null && ( lineb = inb.readLine() ) != null ) {
            StringTokenizer toka = new StringTokenizer( linea, "\t" );
            StringTokenizer tokb = new StringTokenizer( lineb, "\t" );
            String sa = null;
            String sb = null;

            while ( toka.hasMoreTokens() || tokb.hasMoreTokens() ) {
                int tokNum = 0;
                if ( toka.hasMoreTokens() ) {
                    sa = toka.nextToken();
                }

                if ( tokb.hasMoreTokens() ) {
                    sb = tokb.nextToken();
                }

                if ( sa == null || sb == null ) return false;

                if ( !sa.equals( sb ) ) {
                    // try parsing doubles, maybe they are close enough.
                    try {
                        double da = Double.parseDouble( sa );
                        double db = Double.parseDouble( sb );

                        if ( Math.abs( da - db ) > TOLERANCE )
                            log.error( "Diff (numeric): " + da + " != " + db + " at line " + lineNum );
                        return false;

                    } catch ( Exception e ) {
                        log.error( "Diff: " + sa + " != " + sb + " at line " + lineNum + ", token " + tokNum );
                        return false;
                    }

                }
                tokNum++;
            }

            lineNum++;

        }
        return true;
    }
}