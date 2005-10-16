/*
 * The ermineJ project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import baseCode.util.StatusViewer;
import classScore.data.GeneSetResult;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
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