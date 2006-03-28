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
package ubic.erminej.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.CancellationException;

/**
 * @author pavlidis
 * @version $Id$
 */
public abstract class AbstractLongTask {

    private static Log log = LogFactory.getLog( AbstractLongTask.class.getName() );

    /**
     * 
     */
    public void ifInterruptedStop() {
        // log.debug( "Checking if " + Thread.currentThread().getName() + " has been interrupted" );
        Thread.yield(); // let another thread have some time perhaps to stop this one.
        if ( Thread.currentThread().isInterrupted() ) {
            log.debug( "Interrupted, throwing CancellationException" );
            throw new CancellationException( "Cancelled" );
        }
    }

}
