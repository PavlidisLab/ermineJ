package classScoreTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.util.RegressionTesting;
import classScore.GeneSetPvalRun;
import classScore.ResultsPrinter;
import classScore.Settings;
import classScore.classScoreCMD;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class classScoreTester extends classScoreCMD {
    private static Log log = LogFactory.getLog( classScoreTester.class.getName() );
    private static final double TOLERANCE = 0.001;

    // find settings in the data directory; read each one, run analyses on them.
    public classScoreTester() throws IOException {
        super();

        BufferedReader dis = new BufferedReader( new InputStreamReader( classScoreTester.class
                .getResourceAsStream( "/data/test.configs.txt" ) ) );
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
                    ResultsPrinter rp = new ResultsPrinter( saveFileName, result, goData, false );
                    rp.printResults( false ); // don't sort.
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( ConfigurationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main( String[] args ) {
        try {
            new classScoreTester();
        } catch ( IOException e ) {
            e.printStackTrace();
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
                            System.err.println( "Diff (numeric): " + da + " != " + db + " at line " + lineNum );
                        return false;

                    } catch ( Exception e ) {
                        System.err.println( "Diff: " + sa + " != " + sb + " at line " + lineNum + ", token " + tokNum );
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