package classScoreTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.StringTokenizer;

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

   private static final double TOLERANCE = 0.001;

   // find settings in the data directory; read each one, run analyses on them.
   public classScoreTester() {
      super();

      BufferedReader dis = new BufferedReader( new InputStreamReader(
            classScoreTester.class
                  .getResourceAsStream( "/data/test.configs.txt" ) ) );
      String configFileName = "";

      try {
         while ( ( configFileName = dis.readLine() ) != null ) {
            boolean doTest = true;

            if ( configFileName.startsWith( "#" ) ) continue;
            settings = new Settings( classScoreTester.class
                  .getResource( configFileName ) );
            System.err.println( "Running: " + configFileName );

            String saveFileName = File.createTempFile( "ermineJ.tmp", ".txt" )
                  .getAbsolutePath();
            System.err.println( "Results will be written to " + saveFileName );

            String goldStandardFileName = settings.getGoldStandardFile();
            if ( !new File( goldStandardFileName ).canRead() ) {
               saveFileName = goldStandardFileName;
               doTest = false;
               System.err.println( "Could not read from "
                     + goldStandardFileName );
               System.err.println( "Creating test output" );
            }

            initialize();

            try {
               GeneSetPvalRun result = analyze();
               ResultsPrinter rp = new ResultsPrinter( saveFileName, result,
                     goData );
               rp.printResults( false ); // don't sort.
            } catch ( Exception e ) {
               statusMessenger.setStatus( "Error During analysis: " + e );
               e.printStackTrace();
            }

            if ( doTest ) {
               String gs = RegressionTesting
                     .readTestResultFromFile( goldStandardFileName );
               String te = RegressionTesting
                     .readTestResultFromFile( saveFileName );

               //    System.err.println( gs );
               //     System.err.println( te );

               if ( compareResults( gs, te ) ) {
                  System.err.println( "Passed test" );
               } else {
                  System.err.println( "Failed test" );
               }
            } else {
               System.err.println( "Skipping test, output instead" );
            }

         }
         dis.close();
      } catch ( IOException e ) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   public static void main( String[] args ) {
      new classScoreTester();
   }

   private boolean compareResults( String a, String b ) throws IOException {
      BufferedReader ina = new BufferedReader( new StringReader( a ) );
      BufferedReader inb = new BufferedReader( new StringReader( b ) );

      String linea;
      String lineb;
      int lineNum = 0;
      while ( ( linea = ina.readLine() ) != null
            && ( lineb = inb.readLine() ) != null ) {
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
                        System.err.println( "Diff (numeric): " + da + " != "
                              + db + " at line " + lineNum );
                  return false;

               } catch ( Exception e ) {
                  System.err.println( "Diff: " + sa + " != " + sb + " at line "
                        + lineNum + ", token " + tokNum );
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