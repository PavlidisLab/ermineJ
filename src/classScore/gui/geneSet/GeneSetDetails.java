package classScore.gui.geneSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    private String classID;
    private String className;
    private GONames goData;
    private GeneAnnotations geneData;
    private Settings settings;

    public GeneSetDetails( GONames goData, GeneAnnotations geneData, Settings settings, String classID ) {
        this.classID = classID;
        this.goData = goData;
        this.geneData = geneData;
        this.settings = settings;
        this.className = goData.getNameForId( classID );
    }

    public void show( GeneSetResult res, GeneScoreReader geneScores ) {
        Map classToProbe = geneData.getGeneSetToProbeMap();

        Collection probeIDs = ( Collection ) classToProbe.get( classID );
        Map pvals = new HashMap();

        for ( Iterator iter = probeIDs.iterator(); iter.hasNext(); ) {
            String probeID = ( String ) iter.next();

            Double pvalue;
            if ( geneScores == null ) {
                pvalue = new Double( Double.NaN );
            } else {
                if ( geneScores.getSettings().getDoLog() == true ) {
                    pvalue = new Double( Math.pow( 10.0, -( ( Double ) geneScores.getProbeToPvalMap().get( probeID ) )
                            .doubleValue() ) );
                } else {
                    pvalue = ( Double ) geneScores.getProbeToPvalMap().get( probeID );
                }
            }
            pvals.put( probeID, pvalue );
        }

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
        return title;
    }

    public void show() {
        this.show( null, null );
    }

}
