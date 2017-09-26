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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.erminej.Settings;
import ubic.erminej.gui.file.AnnotationListFrame;
import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;
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

    /**
     * Show a list of available annotation files.
     */
    public ArrayDesignValueObject pickAnnotation() {

        List<ArrayDesignValueObject> designs = fetchPlatformList();

        AnnotationListFrame f = new AnnotationListFrame( designs );

        return f.getSelected();
    }

    /**
     * @return
     */
    private List<ArrayDesignValueObject> fetchPlatformList() {
        List<ArrayDesignValueObject> designs = null;

        FutureTask<List<ArrayDesignValueObject>> future = new FutureTask<List<ArrayDesignValueObject>>( new Callable<List<ArrayDesignValueObject>>() {
            @Override
            public List<ArrayDesignValueObject> call() throws Exception {
                return fetchList();
            }

        } );

        Executors.newSingleThreadExecutor().execute( future );
        try {
            StopWatch timer = new StopWatch();
            timer.start();
            while ( !future.isDone() ) {
                try {
                    Thread.sleep( 2000 );
                    log.info( "Waiting for response ..." );
                } catch ( InterruptedException ie ) {
                    throw new IOException( "Fetching platforms interrupted" );
                }
                if ( timer.getTime() > 20000 ) {
                    throw new IOException( "Fetching platforms timed out" );
                }
            }

            List<ArrayDesignValueObject> list = future.get();
            if ( !list.isEmpty() ) {
                designs = list;
            } else {
                throw new IOException( "Got no platforms" );
            }
        } catch ( ExecutionException e ) {
            throw new RuntimeException( "Fetching platforms failed: " + e.getMessage(), e );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( "Fetching platforms failed: " + e.getMessage(), e );
        } catch ( IOException e ) {
            throw new RuntimeException( "Fetching platforms failed: " + e.getMessage(), e );
        }

        log.info( designs.size() + " designs read" );
        return designs;
    }

    /**
     * Get the list of available annotations
     * 
     * @return
     * @throws IOException
     */
    public List<ArrayDesignValueObject> fetchList() throws IOException {
        try {
            String url = Settings.ANNOTATION_FILE_LIST_RESTURL;
            assert url != null;
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

        JSONArray recs = ( ( JSONArray ) o.get( "data" ) );

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
            public JSONValue toJSON( Object aPojo ) {
                return null; // not needed
            }
        } );

        JSONMapper.addHelper( new SimpleMapperHelper() {

            @Override
            public Class getHelpedClass() {
                return AuditEventType.class;
            }

            @Override
            public Object toJava( JSONValue aValue, Class aRequestedClass ) throws MapperException {
                return null;
            }

            @Override
            public JSONValue toJSON( Object aPojo ) {
                return null; // not needed
            }
        } );

        try {
            for ( int i = 0; i < recs.size(); i++ ) {
                JSONValue val = recs.get( i );
                ArrayDesignValueObject java = ( ArrayDesignValueObject ) JSONMapper.toJava( val, ArrayDesignValueObject.class );
                result.add( java );
            }

        } catch ( MapperException e ) {
            throw new RuntimeException( e );
        }

        return result;
    }

}
