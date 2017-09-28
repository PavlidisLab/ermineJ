/*
 * The ermineJ project
 *
 * Copyright (c) 2010 University of British Columbia
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
package ubic.erminej;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of command line - mostly just parsing, actual analysis not always run.
 *
 * @author paul
 * @version $Id$
 */
public class CliTest {

    private String basePath = "";

    private String output = "";

    private String gofile;

    @Before
    public final void setUp() throws Exception {
        URL go = this.getClass().getResource( "/data/go_daily-termdb.rdf-xml.zip" );
        File f = new File( go.toURI() );
        gofile = f.getAbsolutePath();
        basePath = f.getParentFile().getAbsolutePath();

        if ( !( new File( gofile ) ).canRead() ) {
            throw new IllegalStateException( "Could not locate GO file" );
        }

        output = File.createTempFile( "ermineJtest.", ".tmp" ).getAbsolutePath();

    }

    @After
    public final void tearDown() throws Exception {
        try {
            new File( output ).delete();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    @Test
    public final void testCliA() throws Exception {
        ErmineJCli cmd = new ErmineJCli();
        assertTrue( !cmd.run( new String[] {} ) );
    }

    /**
     * Correlation analysis
     *
     * @throws Exception
     */
    @Test
    public final void testCliB() throws Exception {
        ErmineJCli cmd = new ErmineJCli();
        boolean okay = cmd.processCommandLine( "foo", new String[] { "-a",
                basePath + File.separator + "HG-U95A.an.txt", "-n", "2", "-c", gofile, "-r",
                basePath + File.separator + "melanoma_and_sarcomaMAS5.txt" } );
        assertTrue( okay );
    }

    /**
     * @throws Exception
     */
    @Test
    public final void testCliC() throws Exception {
        ErmineJCli cmd = new ErmineJCli();
        boolean okay = cmd.run( new String[] { "-a", "foo", "-n", "2" } );
        assertTrue( !okay );
    }

    /**
     * Regular analysis
     *
     * @throws Exception
     */
    @Test
    public final void testCliD() throws Exception {
        ErmineJCli cmd = new ErmineJCli();
        boolean okay = cmd.run( new String[] { "-a", basePath + File.separator + "HG-U95A.an.txt", "-n", "1", "-c",
                gofile, "-s", basePath + File.separator + "one-way-anova-parsed.txt", "-x", "10", "-o", output } );
        assertTrue( okay );

        File file = new File( output );

        BufferedReader f = new BufferedReader( new FileReader( file ) );

        String line = "";
        boolean found = false;
        // TODO do better test of output.
        while ( ( line = f.readLine() ) != null ) {
            // System.err.println( line );
            if ( line.contains( "GO:0008061" ) ) {
                assertTrue( line.contains( "0.3039054" ) );
                found = true;
            }
        }
        assertTrue( found );

    }

    /**
     * Correlation analysis, but also pass gene scores
     *
     * @throws Exception
     */
    @Test
    public final void testCliE() throws Exception {
        ErmineJCli cmd = new ErmineJCli();
        boolean okay = cmd.processCommandLine( "foo", new String[] { "-a",
                basePath + File.separator + "HG-U95A.an.txt", "-n", "2", "-c", gofile, "-r",
                basePath + File.separator + "melanoma_and_sarcomaMAS5.txt", "-s",
                basePath + File.separator + "one-way-anova-parsed.txt" } );
        assertTrue( okay );
    }

}
