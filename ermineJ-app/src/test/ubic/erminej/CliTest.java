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

import java.io.File;

import org.apache.commons.lang.StringUtils;

import junit.framework.TestCase;

/**
 * Tests of command line - mostly just parsing, actual analysis not always run.
 * 
 * @author paul
 * @version $Id$
 */
public class CliTest extends TestCase {

    private String ermineJHome = "";

    private String basePath = "";

    private String gofile;

    @Override
    public final void setUp() {
        ermineJHome = System.getenv( "ERMINEJ_DEV_HOME" );
        if ( StringUtils.isBlank( ermineJHome ) ) {
            throw new IllegalStateException( "Please set ERMINEJ_DEV_HOME" );
        }

        String[] path = new String[] { "ermineJ-app", "src", "test", "data" };

        gofile = ermineJHome
                + File.separator
                + StringUtils.join( new String[] { "ermineJ-app", "data", "go_daily-termdb.rdf-xml.zip" },
                        File.separator );

        basePath = ermineJHome + File.separator + StringUtils.join( path, File.separator );

        if ( !( new File( gofile ) ).canRead() ) {
            throw new IllegalStateException( "Could not locate GO file" );
        }

    }

    public final void testCliA() throws Exception {
        classScoreCMD cmd = new classScoreCMD();
        assertTrue( !cmd.run( new String[] {} ) );
    }

    /**
     * Correlation analysis
     * 
     * @throws Exception
     */
    public final void testCliB() throws Exception {
        classScoreCMD cmd = new classScoreCMD();
        boolean okay = cmd.processCommandLine( "foo", new String[] { "-a",
                basePath + File.separator + "HG-U95A.an.txt", "-n", "2", "-c", gofile, "-r",
                basePath + File.separator + "melanoma_and_sarcomaMAS5.txt" } );
        assertTrue( okay );
    }

    /**
     * @throws Exception
     */
    public final void testCliC() throws Exception {
        classScoreCMD cmd = new classScoreCMD();
        boolean okay = cmd.run( new String[] { "-a", "foo", "-n", "2" } );
        assertTrue( !okay );
    }

    /**
     * Regular analysis
     * 
     * @throws Exception
     */
    public final void testCliD() throws Exception {
        classScoreCMD cmd = new classScoreCMD();
        boolean okay = cmd.run( new String[] { "-a", basePath + File.separator + "HG-U95A.an.txt", "-n", "1", "-c",
                gofile, "-s", basePath + File.separator + "one-way-anova-parsed.txt", "-x", "10" } );
        assertTrue( okay );
    }

    /**
     * Correlation analysis, but also pass gene scores
     * 
     * @throws Exception
     */
    public final void testCliE() throws Exception {
        classScoreCMD cmd = new classScoreCMD();
        boolean okay = cmd.processCommandLine( "foo", new String[] { "-a",
                basePath + File.separator + "HG-U95A.an.txt", "-n", "2", "-c", gofile, "-r",
                basePath + File.separator + "melanoma_and_sarcomaMAS5.txt", "-s",
                basePath + File.separator + "one-way-anova-parsed.txt" } );
        assertTrue( okay );
    }

}
