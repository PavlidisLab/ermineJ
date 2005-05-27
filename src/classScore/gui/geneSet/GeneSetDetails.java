package classScore.gui.geneSet;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.util.StatusViewer;
import classScore.Settings;
import classScore.data.GeneScores;
import classScore.data.GeneSetResult;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetDetails {
    protected static final Log log = LogFactory.getLog( GeneSetDetails.class );
    private String classID;
    private String className;
    private GONames goData;
    private GeneAnnotations geneData;
    private Settings settings;
    private final StatusViewer callerStatusViewer;

    public GeneSetDetails( StatusViewer callerStatusViewer, GONames goData, GeneAnnotations geneData,
            Settings settings, String classID ) {
        this.callerStatusViewer = callerStatusViewer;
        this.classID = classID;
        this.goData = goData;
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

    public void show( String runName, GeneSetResult res, GeneScores geneScores ) {

        Map classToProbe = null;
        Collection probeIDs = null;
        Map pvals = new HashMap();

        if ( geneData == null ) {
            // user will be prompted.
            log.warn( "No gene data found" );
        } else {
            classToProbe = geneData.getGeneSetToProbeMap();
            if ( !classToProbe.containsKey( classID ) )
                log.info( "Information about gene set " + classID + " is not available" );
            probeIDs = ( Collection ) classToProbe.get( classID );
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
        JGeneSetFrame f = new JGeneSetFrame( callerStatusViewer, new ArrayList( probeIDs ), pvals, geneData, settings );

        String title = getTitle( runName, res, probeIDs );
        f.setTitle( title );
        f.show();

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

            if ( !geneScores.getProbeToPvalMap().containsKey( probeID ) ) {
                pvals.put( probeID, new Double( Double.NaN ) );
                continue;
            }

            Double pvalue;
            if ( geneScores == null ) {
                pvalue = new Double( Double.NaN );
            } else {
                if ( settings.getDoLog() == true ) {
                    double negLogPval = ( ( Double ) geneScores.getProbeToPvalMap().get( probeID ) ).doubleValue();
                    pvalue = new Double( Math.pow( 10.0, -negLogPval ) );
                } else {
                    pvalue = ( Double ) geneScores.getProbeToPvalMap().get( probeID );
                }
            }
            pvals.put( probeID, pvalue );
        }
    }

    /**
     * @param geneScores
     * @return
     */
    private GeneScores tryToGetGeneScores( GeneScores geneScores ) {
        assert settings != null : "Null settings.";
        String scoreFile = settings.getScoreFile();
        if ( scoreFile != null ) {
            try {
                GeneScores localReader = new GeneScores( scoreFile, settings, null, geneData.getGeneToProbeList(),
                        geneData.getProbeToGeneMap() );
                geneScores = localReader;
                log.debug( "Getting gene scores from " + scoreFile );
            } catch ( IOException e ) {
                log.error( e, e );
                return null;
            }
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
    public void show() {
        this.show( null, null, null );
    }

}
