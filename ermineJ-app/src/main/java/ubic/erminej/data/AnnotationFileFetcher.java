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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.SettingsHolder;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;
import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import com.sdicons.json.mapper.helper.SimpleMapperHelper;
import com.sdicons.json.mapper.helper.impl.DateMapper;
import com.sdicons.json.model.JSONArray;
import com.sdicons.json.model.JSONInteger;
import com.sdicons.json.model.JSONObject;
import com.sdicons.json.model.JSONString;
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;

/**
 * Assistance in getting gene annotation files.
 * 
 * @author paul
 * @version $Id$
 */
public class AnnotationFileFetcher {

    private static Log log = LogFactory.getLog( AnnotationFileFetcher.class );

    private SettingsHolder settingsHolder;

    public AnnotationFileFetcher( SettingsHolder settingsHolder ) {
        super();
        this.settingsHolder = settingsHolder;
    }

    /**
     * Show a list of available annotation files.
     */
    public ArrayDesignValueObject pickAnnotation() throws IOException {
        List<ArrayDesignValueObject> i = fetchList();
        log.info( i.size() + " designs read" );

        AnnotationListFrame f = new AnnotationListFrame( i );

        return f.getSelected();
    }

    /**
     * Get the list of available annotations
     * 
     * @return
     * @throws IOException
     */
    public List<ArrayDesignValueObject> fetchList() throws IOException {

        try {
            // defined in ermineJdefault.properties.
            String url = settingsHolder.getStringProperty( "annotation.file.list.rest.url" );
            if ( url == null ) {
                log.warn( "There was no valid setting for the URL to fetch the annotation file list" );
                throw new IOException( "There was no valid setting for the URL to fetch the annotation file list" );
            }
            URL toBeGotten = new URL( url );
            InputStream is = toBeGotten.openStream();
            JSONParser parser = new JSONParser( is );
            JSONValue v = parser.nextValue();
            return convert( v );

        } catch ( RecognitionException e ) {
            throw new IOException( e );
        } catch ( TokenStreamException e ) {
            throw new IOException( e );
        }
    }

    /**
     * @param v
     * @return
     */
    protected List<ArrayDesignValueObject> convert( JSONValue v ) {

        List<ArrayDesignValueObject> result = new ArrayList<ArrayDesignValueObject>();
        JSONObject o = ( JSONObject ) v;

        JSONArray recs = ( ( JSONArray ) o.get( "records" ) );
        JSONMapper.addHelper( new SimpleMapperHelper() {

            @Override
            public Class getHelpedClass() {
                return Date.class;
            }

            @Override
            public Object toJava( JSONValue aValue, Class aRequestedClass ) throws MapperException {
                if ( aValue.isInteger() ) {
                    return new Date( ( ( JSONInteger ) aValue ).getValue().longValue() );
                }
                return DateMapper.fromISO8601( ( ( JSONString ) aValue ).getValue().trim() );
            }

            @Override
            public JSONValue toJSON( Object aPojo ) throws MapperException {
                return null; // not needed
            }
        } );
        try {
            for ( int i = 0; i < recs.size(); i++ ) {
                JSONValue val = recs.get( i );
                ArrayDesignValueObject java = ( ArrayDesignValueObject ) JSONMapper.toJava( val,
                        ArrayDesignValueObject.class );
                result.add( java );
            }

        } catch ( MapperException e ) {
            throw new RuntimeException( e );
        }

        return result;
    }

}
