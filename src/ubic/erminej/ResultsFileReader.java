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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ubic.basecode.util.StatusViewer;

import ubic.erminej.data.GeneSetResult;

/**
 * @author Paul Pavlidis
 * @author Homin Lee
 * @version $Id$
 */
public class ResultsFileReader {

    private Map results;

    public ResultsFileReader( String filename, StatusViewer messenger ) throws NumberFormatException, IOException {
        results = new LinkedHashMap();

        File infile = new File( filename );
        if ( !infile.exists() || !infile.canRead() ) {
            throw new IOException( "Could not read " + filename );
        }

        if ( infile.length() == 0 ) {
            throw new IOException( "File has zero length" );
        }

        BufferedReader dis = new BufferedReader( new InputStreamReader( new BufferedInputStream( new FileInputStream(
                filename ) ) ) );

        messenger.showStatus( "Loading analysis..." );
        String line;
        while ( ( line = dis.readLine() ) != null ) {
            StringTokenizer st = new StringTokenizer( line, "\t" );
            String firstword = st.nextToken();
            if ( firstword.compareTo( "!" ) == 0 ) {
                String className = st.nextToken();
                String classId = st.nextToken();
                int size = Integer.parseInt( st.nextToken() );
                int effsize = Integer.parseInt( st.nextToken() );
                double score = Double.parseDouble( st.nextToken() );
                double pval = Double.parseDouble( st.nextToken() );
                GeneSetResult c = new GeneSetResult( classId, className, size, effsize, score, pval );
                results.put( classId, c );
            }
        }
        dis.close();
        messenger.showStatus( results.size() + " class results read from file" );
    }

    public Map getResults() {
        return results;
    }

}