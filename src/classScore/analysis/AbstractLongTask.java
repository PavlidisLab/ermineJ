package classScore.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.util.CancellationException;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class AbstractLongTask {

    private static Log log = LogFactory.getLog( AbstractLongTask.class.getName() );

    /**
     * 
     */
    public void ifInterruptedStop() {
//        log.debug( "Checking if " + Thread.currentThread().getName() + " has been interrupted" );
        Thread.yield(); // let another thread have some time perhaps to stop this one.
        if ( Thread.currentThread().isInterrupted() ) {
            log.debug( "Interrupted, throwing CancellationException" );
            throw new CancellationException( "Cancelled" );
        }
    }

}
