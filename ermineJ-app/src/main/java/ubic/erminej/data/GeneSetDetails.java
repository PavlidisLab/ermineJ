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
package ubic.erminej.data;

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
import ubic.erminej.SettingsHolder;
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

    private GeneSetResult result;
    private GeneAnnotations geneData;

    private Settings settings;
    private StatusViewer callerStatusViewer = new StatusStderr();
    private Collection<Probe> probes = new HashSet<Probe>();
    private Map<Probe, Double> probeScores = new HashMap<Probe, Double>();

    private DoubleMatrix<Probe, String> dataMatrix = null;

    private GeneScores geneScores;

    private int numGenes = 0;

    private SettingsHolder runSettings;

    /**
     * Show without any results.
     * 
     * @param classID
     * @param geneData Should be the pruned set, if appropriate
     * @param callerStatusViewer
     */
    public GeneSetDetails( GeneSetTerm classID, GeneAnnotations geneData, StatusViewer callerStatusViewer ) {
        if ( callerStatusViewer != null ) this.callerStatusViewer = callerStatusViewer;
        this.classID = classID;
        this.geneData = geneData;
        this.probes = geneData.getGeneSetProbes( classID );
        this.numGenes = geneData.getGeneSetGenes( classID ).size();
    }

    /**
     * The data matrix will be read in based on the settings, or can be changed later.
     * 
     * @param classID
     * @param result - optional
     * @param geneData Should be the pruned set, if appropriate
     * @param settings - optional, if not supplied will be read in
     * @param geneScores - optional, if not supplied will be read in based on settings.
     * @param statusViewer
     */
    public GeneSetDetails( GeneSetTerm classID, GeneSetResult result, GeneAnnotations geneData, Settings settings,
            GeneScores geneScores, StatusViewer statusViewer ) {

        this( classID, geneData, statusViewer );

        if ( result != null ) this.runSettings = result.getSettings();

        if ( settings == null ) {
            log.debug( "No settings, reading them in" );
            try {
                this.settings = new Settings();
            } catch ( IOException e ) {
                GuiUtil.error( "Problem accessing settings for the details view:\n" + e.getMessage() );
                return;
            }
        } else {
            this.settings = settings;
        }

        this.probes = geneData.getGeneSetProbes( classID );

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

        // sanity
        if ( this.geneScores != null ) {
            assert this.geneData != null;
            assert this.geneScores.getGeneToScoreMap() != null;
            assert !this.geneScores.getGeneToScoreMap().keySet().isEmpty();
            assert this.geneData.getGeneSetGenes( this.classID ) != null;
            assert this.geneScores.getGeneToScoreMap().keySet()
                    .containsAll( this.geneData.getGeneSetGenes( this.classID ) );
        }

    }

    public GeneSetTerm getClassID() {
        return classID;
    }

    public DoubleMatrix<Probe, String> getDataMatrix() {
        return dataMatrix;
    }

    /**
     * @return
     */
    public GeneAnnotations getGeneData() {
        return geneData;
    }

    /**
     * @return
     */
    public Collection<Probe> getProbes() {
        return Collections.unmodifiableCollection( probes );
    }

    /**
     * Just for the probes in this set.
     * 
     * @return
     */
    public Map<Probe, Double> getProbeScores() {
        return Collections.unmodifiableMap( probeScores );
    }

    /**
     * @return the settings used for analysis of this gene set (will be null if we aren't looking at a result)
     */
    public SettingsHolder getRunSettings() {
        return runSettings;
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * The full set of GeneScores from which this was derived.
     * 
     * @return
     */
    public GeneScores getSourceGeneScores() {
        return geneScores;
    }

    /**
     * @param res
     * @param nf
     * @param probeIDs
     * @return
     */
    public String getTitle() {
        String title = this.classID.getId() + " - " + StringUtils.abbreviate( this.classID.getName(), 50 ) + " ("
                + numGenes + " genes)";

        if ( this.result != null ) {
            title = title + " p = " + String.format( "%.2g", result.getPvalue() );
        }

        SettingsHolder s = getSettingsToUse();

        /* this gets kind of long, this information needs to be available somewhere */

        if ( dataMatrix != null ) {
            String rawDataSource = new File( s.getRawDataFileName() ).getName();
            title += " Profiles: " + rawDataSource;
        }

        if ( probeScores != null && !probeScores.isEmpty() ) {
            if ( StringUtils.isNotBlank( s.getScoreFile() ) ) {
                String scoreSource = new File( s.getScoreFile() ).getName();
                title += " Scores: " + scoreSource;
            }
            title += " col: " + s.getScoreCol() + " " + this.geneScores.getScoreColumnName();

        }

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
            SettingsHolder s = getSettingsToUse();

            DoubleMatrixReader matrixReader = new DoubleMatrixReader();

            DoubleMatrix<String, String> omatrix = matrixReader.read( filename, probeNames.keySet(), s.getDataCol() );

            this.dataMatrix = new FastRowAccessDoubleMatrix<Probe, String>( omatrix.asArray() );
            dataMatrix.setColumnNames( omatrix.getColNames() );
            int i = 0;
            for ( String r : omatrix.getRowNames() ) {
                dataMatrix.setRowName( probeNames.get( r ), i );
                i++;
            }

        } catch ( IOException e ) {
            GuiUtil.error( "Could not load data from " + filename, e );
        }
    }

    /**
     * You only need to use this if you want to replace the matrix.
     * 
     * @param dm
     */
    public void setDataMatrix( DoubleMatrix<Probe, String> dm ) {
        int numNotKnown = 0;
        for ( Probe p : dm.getRowNames() ) {
            if ( !geneData.hasProbe( p ) ) {
                numNotKnown++;
            }
        }

        if ( numNotKnown > 0 ) {

            if ( numNotKnown == dm.rows() ) {
                callerStatusViewer.showError( "The selected matrix has no probes that match the current annotations" );
                throw new IllegalArgumentException(
                        "The selected matrix has no probes that match the current annotations" );
            }

            callerStatusViewer.showWarning( numNotKnown
                    + " of the probes in the data matrix don't match the current annotations" );

        }

        this.dataMatrix = dm;
    }

    /**
     * Switch out the gene scores.
     * 
     * @param scores
     */
    public void setGeneScores( GeneScores scores ) {
        initGeneScores( scores );
    }

    /**
     * @return
     */
    private SettingsHolder getSettingsToUse() {
        return runSettings == null ? settings : runSettings;
    }

    /**
     * @param geneScores
     */
    private void initGeneScores( GeneScores gs ) {
        this.geneScores = gs;
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
        SettingsHolder s = getSettingsToUse();
        String filename = s.getRawDataFileName();

        if ( StringUtils.isBlank( filename ) ) {
            callerStatusViewer.showWarning( "Data file for heatmap is not defined" );
        } else if ( !( new File( filename ) ).canRead() ) {
            GuiUtil.error( "The data file \"" + filename + "\" was not readable." + "\n"
                    + "Please make sure this file exists and the filename and directory path are correct,\n"
                    + "and that it is a valid raw data file (tab-delimited).\n" );
        } else {
            loadDataMatrix( filename );

        }
    }

    /**
     * @param geneScores
     * @return
     */
    private GeneScores tryToGetGeneScores() throws IllegalStateException {
        assert settings != null : "Null settings.";
        SettingsHolder s = getSettingsToUse();

        String scoreFile = s.getScoreFile();

        if ( StringUtils.isNotBlank( scoreFile ) ) {

            File f = new File( scoreFile );
            if ( !f.canRead() ) {
                /*
                 * This can happen if things have moved. Let's ignore the setting in the future.
                 */
                callerStatusViewer.showWarning( "Gene scores were not readable from  " + scoreFile + "; ignoring" );
                settings.setScoreFile( null );
                return null;
            }

            try {
                callerStatusViewer.showStatus( "Getting gene scores from " + scoreFile );
                GeneScores scores = new GeneScores( scoreFile, s, null, this.geneData );
                if ( scores.getProbeToScoreMap().isEmpty() ) {
                    return null;
                }
                return scores;
            } catch ( Exception e ) {
                // just in case
                callerStatusViewer.showError( scoreFile + " is not readable: " + e );
            }
        }
        return null;
    }

}