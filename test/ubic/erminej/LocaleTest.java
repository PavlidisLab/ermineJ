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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author keshav
 * @version $Id$
 */
public class LocaleTest extends TestCase {
    Log log = LogFactory.getLog( this.getClass() );

    public void readFile( InputStream is ) throws IOException, ParseException {
        NumberFormat numberFormat = NumberFormat.getInstance( Locale.FRANCE );

        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
        br.readLine();// discard header
        String row;
        while ( ( row = br.readLine() ) != null ) {
            String[] vals = row.split( "\t" );
            Number num = numberFormat.parse( vals[1] );
            log.debug( num );
        }
    }

    public void testLocale() {
        double num = 100.01;
        Locale locale_us = new Locale( "US" );
        String fNum_us = NumberFormat.getNumberInstance( locale_us ).format( num );
        log.debug( "USA: " + fNum_us );

        Locale locale_fr = new Locale( "FR" );
        String fNum_fr = NumberFormat.getNumberInstance( locale_fr ).format( num );
        log.debug( "France: " + fNum_fr );

        Locale locale_ge = new Locale( "DE" );
        String fNum_ge = NumberFormat.getNumberInstance( locale_ge ).format( num );
        log.debug( "Germany: " + fNum_ge );
    }

    public void testLocaleParse() {
        String filename = "c:/java/apps/eclipse_workspace/ermineJ/test/data/test.scores.euro.txt";
        InputStream is;
        try {
            is = new FileInputStream(
                    new File( filename ) );

            readFile( is );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
