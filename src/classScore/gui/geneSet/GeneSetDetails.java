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

import classScore.Settings;
import classScore.data.GeneScoreReader;
import classScore.data.GeneSetResult;
import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

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

    public GeneSetDetails( GONames goData, GeneAnnotations geneData, Settings settings, String classID ) {
        this.classID = classID;
        this.goData = goData;
        this.geneData = geneData;
        if ( settings == null ) {
            try {
                settings = new Settings();
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            this.settings = settings;
        }
        this.className = goData.getNameForId( classID );
    }

    public void show( GeneSetResult res, GeneScoreReader geneScores ) {

        Map classToProbe = null;
        Collection probeIDs = null;
        Map pvals = new HashMap();

        if ( geneData == null ) {
            // FIXME what we should do: prompt the user to enter some; if they don't have any, they can just skip it.
            log.warn( "No gene data found" );
        } else {
            classToProbe = geneData.getGeneSetToProbeMap();
            probeIDs = ( Collection ) classToProbe.get( classID );
        }

        if ( geneScores == null ) {
            geneScores = tryToGetGeneScores( geneScores );
        }

        getGeneScoresForGeneSet( geneScores, probeIDs, pvals );

        if ( probeIDs == null ) {
            throw new IllegalStateException( "Class data retrieval error for " + className );
        }

        // create the details frame
        JGeneSetFrame f = new JGeneSetFrame( new ArrayList( probeIDs ), pvals, geneData, settings );

        String title = getTitle( res, probeIDs );
        f.setTitle( title );
        f.show();

    }

    /**
     * @param geneScores
     * @param probeIDs
     * @param pvals
     */
    private void getGeneScoresForGeneSet( GeneScoreReader geneScores, Collection probeIDs, Map pvals ) {
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
    private GeneScoreReader tryToGetGeneScores( GeneScoreReader geneScores ) {
        assert settings != null : "Null settings.";
        String scoreFile = settings.getScoreFile();
        if ( scoreFile != null ) {
            try {
                GeneScoreReader localReader = new GeneScoreReader( scoreFile, settings, null, geneData
                        .getGeneToProbeList(), geneData.getProbeToGeneMap() );
                geneScores = localReader;
            } catch ( IOException e ) {
                log.error( e, e );
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
    private String getTitle( GeneSetResult res, Collection probeIDs ) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 8 );
        String title = className + " (" + probeIDs.size() + " items  ";
        if ( res != null ) title = title + "p = " + nf.format( res.getPvalue() );
        title = title + ")";
        return title;
    }

    public void show() {
        this.show( null, null );
    }

}
