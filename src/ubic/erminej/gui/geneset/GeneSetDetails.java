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
package ubic.erminej.gui.geneset;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;

/**
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetDetails {
    protected static final Log log = LogFactory.getLog( GeneSetDetails.class );
    private String classID;
    private String className;
    private GeneAnnotations geneData;
    private Settings settings;
    private final StatusViewer callerStatusViewer;

    public GeneSetDetails( StatusViewer callerStatusViewer, GONames goData, GeneAnnotations geneData,
            Settings settings, String classID ) {
        this.callerStatusViewer = callerStatusViewer;
        this.classID = classID;

        this.geneData = geneData;
        if ( settings == null ) {
            try {
                log.debug( "No settings, reading them in" );
                settings = new Settings();
                if ( settings == null ) throw new NullPointerException( "No settings!" );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } else {
            this.settings = settings;
        }
        this.className = goData.getNameForId( classID );
    }

    public void show( String runName, GeneSetResult res, GeneScores geneScores ) throws IOException,
            IllegalStateException {

        Collection probeIDs = null;
        Map pvals = new HashMap();

        if ( geneData == null ) {
            // user will be prompted.
            log.warn( "No gene data found" );
        } else {
            probeIDs = geneData.getGeneSetProbes( classID );
            if ( probeIDs == null || probeIDs.size() == 0 ) {
                log.info( "Information about gene set " + classID + " is not available" );
            }
        }

        if ( geneScores == null ) {
            geneScores = tryToGetGeneScores( geneScores );
        }

        if ( geneScores != null ) {
            getGeneScoresForGeneSet( geneScores, probeIDs, pvals );
        }

        if ( probeIDs == null ) {
            log.warn( "Class data retrieval error for " + className + "( no probes )" );
        }

        // create the details frame
        JGeneSetFrame f = new JGeneSetFrame( className, callerStatusViewer, new ArrayList( probeIDs ), pvals, geneData,
                settings );

        String title = getTitle( runName, res, probeIDs );
        f.setTitle( title );
        f.setVisible( true );

    }

    /**
     * @param geneScores
     * @param probeIDs
     * @param pvals
     */
    private void getGeneScoresForGeneSet( GeneScores geneScores, Collection probeIDs, Map pvals ) {
        if ( probeIDs == null ) return;
        for ( Iterator iter = probeIDs.iterator(); iter.hasNext(); ) {
            String probeID = ( String ) iter.next();

            if ( !geneScores.getProbeToScoreMap().containsKey( probeID ) ) {
                pvals.put( probeID, new Double( Double.NaN ) );
                continue;
            }

            Double pvalue;
            if ( geneScores == null ) {
                pvalue = new Double( Double.NaN );
            } else {
                if ( settings.getDoLog() == true ) {
                    double negLogPval = ( ( Double ) geneScores.getProbeToScoreMap().get( probeID ) ).doubleValue();
                    pvalue = new Double( Math.pow( 10.0, -negLogPval ) );
                } else {
                    pvalue = ( Double ) geneScores.getProbeToScoreMap().get( probeID );
                }
            }
            pvals.put( probeID, pvalue );
        }
    }

    /**
     * @param geneScores
     * @return
     */
    private GeneScores tryToGetGeneScores( GeneScores geneScores ) throws IOException, IllegalStateException {
        assert settings != null : "Null settings.";
        String scoreFile = settings.getScoreFile();
        if ( StringUtils.isNotBlank( scoreFile ) ) {
            GeneScores localReader = new GeneScores( scoreFile, settings, null, this.geneData );
            geneScores = localReader;
            log.debug( "Getting gene scores from " + scoreFile );
        }
        return geneScores;
    }

    /**
     * @param res
     * @param nf
     * @param probeIDs
     * @return
     */
    private String getTitle( String runName, GeneSetResult res, Collection probeIDs ) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 8 );
        String title = className + " (" + probeIDs.size() + " items ";
        if ( runName != null ) title = title + runName + " ";
        if ( res != null ) title = title + " p = " + nf.format( res.getPvalue() );
        title = title + ")";
        return title;
    }

    /**
     * Show when there is no run information available.
     */
    public void show() throws IOException, IllegalStateException {
        this.show( null, null, null );
    }

}