/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
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

import java.util.List;

import ubic.erminej.Settings;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;

import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;

import junit.framework.TestCase;

/**
 * @author paul
 * @version $Id$
 */
public class AnnotationFileFetcherTest extends TestCase {

    public void testConvert() throws Exception {
        AnnotationFileFetcher f = new AnnotationFileFetcher( new Settings() );
        final JSONParser lParser = new JSONParser(
                AnnotationFileFetcherTest.class.getResourceAsStream( "/data/arrayExample.json" ) );
        final JSONValue v = lParser.nextValue();

        List<ArrayDesignValueObject> converted = f.convert( v );

        assertEquals( 2, converted.size() );

        assertEquals( "GPL1355", converted.get( 1 ).getShortName() );
    }

}
