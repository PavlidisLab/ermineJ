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
package ubic.erminej.gui.geneset.details;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.FastRowAccessDoubleMatrix;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.Probe;
import ubic.erminej.gui.util.GuiUtil;

/**
 * Combination of scores and data for a gene set, for visualization
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetDetails {
    protected static final Log log = LogFactory.getLog( GeneSetDetails.class );

    private GeneSetTerm classID;

    private GeneSetResult result; // TODO show current result in the title.
    private GeneAnnotations geneData;

    private Settings settings;
    private StatusViewer callerStatusViewer = new StatusStderr();
    private Collection<Probe> probes = new HashSet<Probe>();
    private Map<Probe, Double> probeScores = new HashMap<Probe, Double>();

    private DoubleMatrix<Probe, String> dataMatrix = null;

    /**
     * Show without any results.
     * 
     * @param classID
     * @param geneData
     * @param callerStatusViewer
     */
    public GeneSetDetails( GeneSetTerm classID, GeneAnnotations geneData, StatusViewer callerStatusViewer ) {
        if ( callerStatusViewer != null ) this.callerStatusViewer = callerStatusViewer;
        this.classID = classID;
        this.geneData = geneData;
        this.probes = geneData.getGeneSetProbes( classID );
    }

    /**
     * The data matrix will be read in based on the settings, or can be changed later.
     * 
     * @param classID
     * @param result - optional
     * @param geneData
     * @param settings - optional, if not supplied will be read in
     * @param geneScores - optional, if not supplied will be read in based on settings.
     * @param statusViewer
     */
    public GeneSetDetails( GeneSetTerm classID, GeneSetResult result, GeneAnnotations geneData, Settings settings,
            GeneScores geneScores, StatusViewer statusViewer ) {

        this( classID, geneData, statusViewer );

        if ( settings == null ) {
            log.debug( "No settings, reading them in" );
            try {
                this.settings = new Settings();
            } catch ( IOException e ) {
                GuiUtil.error( "Problem accessing settings for the details view: " + e
                        + ", see the logs for more details." );
                log.error( e, e );
                return;
            }
        } else {
            this.settings = settings;
        }

        if ( geneScores != null ) {
            this.probes = geneScores.getGeneAnnots().getGeneSetProbes( classID );
        }

        this.result = result;

        assert classID != null;
        assert result == null || result.getGeneSetId().equals( classID );

        if ( probes == null || probes.isEmpty() ) {
            log.warn( "Empty gene set " + classID );
            return;
        }

        GeneScores gsToUse = geneScores;

        if ( gsToUse == null ) {
            gsToUse = tryToGetGeneScores();
        }

        this.probes = geneData.getGeneSetProbes( classID );

        if ( gsToUse != null ) {
            initGeneScores( gsToUse );
        }

        initMatrix();

    }

    public GeneSetTerm getClassID() {
        return classID;
    }

    public DoubleMatrix<Probe, String> getDataMatrix() {
        return dataMatrix;
    }

    public GeneAnnotations getGeneData() {
        return geneData;
    }

    public Collection<Probe> getProbes() {
        return Collections.unmodifiableCollection( probes );
    }

    public Map<Probe, Double> getProbeScores() {
        return Collections.unmodifiableMap( probeScores );
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * @param res
     * @param nf
     * @param probeIDs
     * @return
     */
    public String getTitle( String runName, GeneSetResult res ) {
        String title = this.classID.getId() + " - " + StringUtils.abbreviate( this.classID.getName(), 50 ) + " ("
                + this.probes.size() + " items ";
        if ( runName != null ) title = title + runName + " ";
        if ( res != null ) title = title + " p = " + String.format( "%.4g", res.getPvalue() );
        title = title + ")";
        return title;
    }

    /**
     * Update or set the data matrix source. You only need to use this if you want to replace the matrix with one that
     * is not in the Settings (this is called during construction)
     * 
     * @param filename
     */
    public void loadDataMatrix( String filename ) {
        try {
            Map<String, Probe> probeNames = new HashMap<String, Probe>();
            for ( Probe p : probes ) {
                probeNames.put( p.getName(), p );
            }

            DoubleMatrixReader matrixReader = new DoubleMatrixReader();

            DoubleMatrix<String, String> omatrix = matrixReader.read( filename, probeNames.keySet(), settings
                    .getDataCol() );

            this.dataMatrix = new FastRowAccessDoubleMatrix<Probe, String>( omatrix.asArray() );
            dataMatrix.setColumnNames( omatrix.getColNames() );
            int i = 0;
            for ( String r : omatrix.getRowNames() ) {
                dataMatrix.setRowName( probeNames.get( r ), i );
                i++;
            }

        } catch ( IOException e ) {
            GuiUtil.error( "Error loading data from file " + filename + "\n"
                    + "Please make sure this file exists and the filename and directory path are correct,\n"
                    + "and that it is a valid raw data file (tab-delimited).\n" );
        }
    }

    /**
     * You only need to use this if you want to replace the matrix.
     * 
     * @param dm
     */
    public void setDataMatrix( DoubleMatrix<Probe, String> dm ) {
        /* FIXME check that probes match; if they don't not actually any harm, but won't display, no warning */
        this.dataMatrix = dm;
    }

    /**
     * @param geneScores
     */
    private void initGeneScores( GeneScores geneScores ) {
        probeScores = new HashMap<Probe, Double>();
        if ( probes == null || probes.isEmpty() ) return;

        assert geneScores != null;
        for ( Probe probeID : probes ) {

            if ( !geneScores.getProbeToScoreMap().containsKey( probeID ) ) {
                continue;
            }

            Double pvalue;

            if ( geneScores.isNegativeLog10Transformed() ) {
                double negLogPval = geneScores.getProbeToScoreMap().get( probeID );
                pvalue = new Double( Math.pow( 10.0, -negLogPval ) );
            } else {
                pvalue = geneScores.getProbeToScoreMap().get( probeID );
            }

            probeScores.put( probeID, pvalue );
        }
    }

    private void initMatrix() {

        log.debug( "Raw data file is " + settings.getRawDataFileName() );

        String filename = settings.getRawDataFileName();

        if ( !StringUtils.isBlank( filename ) && ( new File( filename ) ).canRead() ) {
            loadDataMatrix( filename );

        } else {
            GuiUtil.error( "The data file \"" + filename + "\" was not readable." + "\n"
                    + "Please make sure this file exists and the filename and directory path are correct,\n"
                    + "and that it is a valid raw data file (tab-delimited).\n" );
        }
    }

    /**
     * @param geneScores
     * @return
     */
    private GeneScores tryToGetGeneScores() throws IllegalStateException {
        assert settings != null : "Null settings.";
        String scoreFile = settings.getScoreFile();
        if ( StringUtils.isNotBlank( scoreFile ) ) {
            try {
                callerStatusViewer.showStatus( "Getting gene scores from " + scoreFile );
                return new GeneScores( scoreFile, settings, null, this.geneData );
            } catch ( Exception e ) {
                callerStatusViewer.showError( scoreFile + " is not readable: " + e );
            }
        }
        return null;
    }

    /**
     * Switch out the gene scores.
     * 
     * @param scores
     */
    public void setGeneScores( GeneScores scores ) {
        initGeneScores( scores );
    }

}