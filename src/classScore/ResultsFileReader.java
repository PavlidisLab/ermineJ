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