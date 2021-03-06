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
package ubic.erminej;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ubic.basecode.util.ConfigUtils;
import ubic.basecode.util.FileTools;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.data.GeneSetTerm;

/**
 * Basically a wrapper around a Commons Configuration object.
 *
 * @author Paul Pavlidis (total rewrite)
 * @author Kiran Keshav
 * @author Homin Lee
 * @author Will Braynen
 */
public class Settings extends SettingsHolder {

    /*
     * URLS for web services used by the software. Use String.format to replace the %s with the desired platform short
     * name.
     */
    public static final String ANNOTATION_FILE_FETCH_RESTURL = "https://gemma.msl.ubc.ca/rest/v2/platforms/%s/annotations";

    // public static final String ANNOTATION_FILE_FETCH_RESTURL =
    // "http://localhost:8080/Gemma/rest/v2/platforms/?offset=0&limit=100000";
    public static final String ANNOTATION_FILE_LIST_RESTURL = "https://gemma.msl.ubc.ca/rest/v2/platforms/?offset=0&limit=100000";

    public static final String HELPURL = "http://erminej.msl.ubc.ca/help";

    /**
     * Settings that we need to write to analysis results files. Other settings are not needed there (like window sizes,
     * etc.)
     */
    protected static final String[] ANALYSIS_SETTINGS = new String[] { GENE_SCORE_THRESHOLD_KEY, QUANTILE_CONFIG_NAME,
            GENE_SET_RESAMPLING_SCORE_METHOD, MAX_CLASS_SIZE, MIN_CLASS_SIZE, RAW_FILE_CONFIG_NAME, SCORE_FILE,
            SCORE_COL, MTC_CONFIG_NAME, ITERATIONS, CLASS_FILE, BIG_IS_BETTER, DO_LOG, GENE_REP_TREATMENT,
            ALWAYS_USE_EMPIRICAL, ANNOT_FILE, ANNOT_FORMAT, CLASS_SCORE_METHOD,
            USE_MOL_FUNC, USE_BIOL_PROC, USE_CELL_COMP, USE_USER_DEFINED_GROUPS,
            CUSTOM_GENESET_FILES, VERSIONPARAM, SEED };
    //  USE_MULTIFUNCTIONALITY_CORRECTION,
    // FILTER_NONSPECIFIC,

    /**
     * Header for the config file.
     */
    protected static final String HEADER = "Configuration file for ermineJ."
            + "Do not delete this file if you want your ermineJ settings to stay across sessions.\nFor more information see http://erminej.msl.ubc.ca/";

    /**
     * Part of the distribution, where defaults can be read from. If it is absent, hard-coded defaults are used.
     */
    protected static final String USERGUI_DEFAULT_PROPERTIES = "ermineJdefault.properties";

    /**
     * Filename for settings.
     */
    protected static final String USERGUI_PROPERTIES = "ermineJ.properties";
    private static final Log log = LogFactory.getLog( Settings.class );

    /**
     * Write a configuration to the given file - but just settings relevant to analysis (not window locations, for
     * example).
     *
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param fileName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static void writeAnalysisSettings( SettingsHolder settings, String fileName ) throws IOException {

        Writer out = null;

        try {
            if ( fileName == null ) {
                out = new BufferedWriter( new PrintWriter( System.out ) );
            } else {
                out = new BufferedWriter( new FileWriter( fileName ) );
            }

            writeAnalysisSettings( settings, out );
        } catch ( IOException e ) {
            throw e;
        } finally {
            if ( out != null ) out.close();
        }
    }

    /**
     * Write a configuration to the given output - but just settings relevant to analysis (not window locations, for
     * example).
     *
     * @param settings a {@link ubic.erminej.SettingsHolder} object.
     * @param out a {@link java.io.Writer} object.
     * @throws java.io.IOException if any.
     */
    public static void writeAnalysisSettings( SettingsHolder settings, Writer out ) throws IOException {
        PropertiesConfiguration configToWrite = settings.getConfig();
        for ( String propertyName : ANALYSIS_SETTINGS ) {
            if ( configToWrite.getProperty( propertyName ) == null ) {
                if ( log.isDebugEnabled() ) log.debug( "No property " + propertyName + ", skipping" );
                continue;
            }

            /*
             * Don't print out irrelevant settings
             */
            if ( propertyName.equals( GENE_SCORE_THRESHOLD_KEY ) && !settings.getClassScoreMethod().equals( Method.ORA ) ) {
                continue;
            } else if ( !settings.getClassScoreMethod().equals( Method.GSR ) ) {
                if ( propertyName.equals( ITERATIONS ) ) {
                    continue;
                }

                if ( propertyName.equals( GENE_SET_RESAMPLING_SCORE_METHOD ) ) {
                    continue;
                }

                if ( propertyName.equals( ALWAYS_USE_EMPIRICAL ) ) {
                    continue;
                }
            }

            out.write( propertyName + " = " );
            out.write( StringEscapeUtils.escapeJava( configToWrite.getProperty( propertyName ).toString() ) );
            out.write( "\n" );
            if ( log.isDebugEnabled() )
                log.debug( "Writing " + propertyName + "=" + configToWrite.getProperty( propertyName ).toString() );
        }
    }

    /**
     * Create the settings, reading them from a file to be determined by the constructor.
     *
     * @throws java.io.IOException if there are problems setting up the configuration.
     */
    public Settings() throws IOException {
        this( true );
    }

    /**
     * <p>
     * Constructor for Settings.
     * </p>
     *
     * @param readFromFile if true, the user's config will be read in. If false, a blank (default) configuration will be
     *        initialized.
     * @throws java.io.IOException if any.
     */
    public Settings( boolean readFromFile ) throws IOException {
        super();
        if ( readFromFile ) {
            loadOrCreateInitialConfig();
        } else {
            this.configBuilder = new FileBasedConfigurationBuilder<>(
                    PropertiesConfiguration.class );
            try {
                this.config = configBuilder.getConfiguration();
            } catch ( ConfigurationException e ) {
                throw new IOException( e );
            }
        }
        initUserDirectories();
    }

    /**
     * Creates settings object from a copy. Note that in this situation, autoSave is FALSE.
     *
     * @param settingsToCopy a {@link ubic.erminej.SettingsHolder} object.
     */
    public Settings( SettingsHolder settingsToCopy ) {
        this.configBuilder = new FileBasedConfigurationBuilder<>( PropertiesConfiguration.class );
        try {
            this.config = configBuilder.getConfiguration();
        } catch ( ConfigurationException e ) {
            throw new RuntimeException( e );
        }
        PropertiesConfiguration oldConfig = settingsToCopy.getConfig();
        for ( Iterator<String> iter = oldConfig.getKeys(); iter.hasNext(); ) {
            String key = iter.next();
            Object value = oldConfig.getProperty( key );
            this.config.setProperty( key, value );
        }
    }

    /**
     * Create a Settings object from the header of a results file or from a regular configuration file - autosave will
     * not be set so they cannot be changed.
     *
     * @param configurationFile a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.apache.commons.configuration.ConfigurationException if any.
     */
    public Settings( String configurationFile ) throws ConfigurationException, IOException {
        this.configBuilder = ConfigUtils.getConfigBuilder( configurationFile );
        this.config = configBuilder.getConfiguration();
        initUserDirectories();
    }

    /**
     * Autosave will not be set.
     *
     * @param resource a {@link java.net.URL} object.
     * @throws java.io.IOException if any.
     * @throws org.apache.commons.configuration.ConfigurationException if any.
     */
    public Settings( URL resource ) throws ConfigurationException, IOException {
        this.configBuilder = ConfigUtils.getConfigBuilder( resource );
        this.config = configBuilder.getConfiguration();
        initUserDirectories();
    }

    /** {@inheritDoc} */
    @Override
    public Format getAnnotFormat() {
        String storedValue = config.getString( ANNOT_FORMAT, Format.DEFAULT.toString() );

        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );
            switch ( oldVal ) {
                case 0:
                    setAnnotFormat( Format.DEFAULT );
                    break;
                case 1:
                    setAnnotFormat( Format.AFFYCSV );
                    break;
                case 2:
                    setAnnotFormat( Format.AGILENT );
                    break;
                case 3:
                    setAnnotFormat( Format.SIMPLE );
                    break;
                default:
                    throw new IllegalStateException( "Format could not be identified: " + storedValue );
            }
            storedValue = config.getString( ANNOT_FORMAT, Format.DEFAULT.toString() );
        }

        return Format.valueOf( storedValue );
    }

    /** {@inheritDoc} */
    @Override
    public Settings.Method getClassScoreMethod() {
        String storedValue = config.getString( CLASS_SCORE_METHOD, SettingsHolder.Method.ORA.toString() );

        // backwards compatibility
        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );
            switch ( oldVal ) {
                case 0:
                    setClassScoreMethod( SettingsHolder.Method.ORA );
                    break;
                case 1:
                    setClassScoreMethod( SettingsHolder.Method.GSR );
                    break;
                case 2:
                    setClassScoreMethod( SettingsHolder.Method.CORR );
                    break;
                case 3:
                    setClassScoreMethod( SettingsHolder.Method.ROC );
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            storedValue = config.getString( CLASS_SCORE_METHOD, SettingsHolder.Method.ORA.toString() );

        }

        return SettingsHolder.Method.valueOf( storedValue );
    }

    /** {@inheritDoc} */
    @Override
    public PropertiesConfiguration getConfig() {
        if ( config == null ) loadOrCreateInitialConfig();
        if ( config == null ) {
            throw new IllegalStateException( "Unable to initialize configuration" );
        }

        return config;
    }

    /** {@inheritDoc} */
    @Override
    public MultiElementHandling getGeneRepTreatment() {
        String storedValue = config.getString( GENE_REP_TREATMENT, MultiElementHandling.BEST.toString() );

        // backwards compatibility
        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );
            switch ( oldVal ) {
                case 1:
                    setGeneRepTreatment( SettingsHolder.MultiElementHandling.BEST );
                    break;
                case 2:
                    setGeneRepTreatment( SettingsHolder.MultiElementHandling.MEAN );
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            storedValue = config.getString( GENE_REP_TREATMENT, MultiElementHandling.BEST.toString() );
        }

        return SettingsHolder.MultiElementHandling.valueOf( storedValue );
    }

    /*
     * (non-Javadoc)
     *
     * @see ubic.erminej.SettingsHolder#getGeneScoreThreshold()
     */
    /** {@inheritDoc} */
    @Override
    public double getGeneScoreThreshold() {

        // use the new key; delete the old one
        if ( config.containsKey( GENE_SCORE_THRESHOLD_KEY ) ) {
            config.getDouble( GENE_SCORE_THRESHOLD_KEY, 0.001 );
            if ( config.containsKey( GENE_SCORE_THRESHOLD_LEGACY_KEY ) ) {
                config.clearProperty( GENE_SCORE_THRESHOLD_LEGACY_KEY );

            }
        } else if ( config.containsKey( GENE_SCORE_THRESHOLD_LEGACY_KEY ) ) {
            config.setProperty( GENE_SCORE_THRESHOLD_KEY, config.getDouble( GENE_SCORE_THRESHOLD_LEGACY_KEY, 0.001 ) );
            config.clearProperty( GENE_SCORE_THRESHOLD_LEGACY_KEY );
        }

        return config.getDouble( GENE_SCORE_THRESHOLD_KEY, 0.001 );
    }

    /** {@inheritDoc} */
    @Override
    public GeneScoreMethod getGeneSetResamplingScoreMethod() {
        String storedValue = config.getString( GENE_SET_RESAMPLING_SCORE_METHOD, GeneScoreMethod.MEAN.toString() );

        // backwards compatibility
        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );

            switch ( oldVal ) {
                case 0:
                    setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.MEAN );
                    break;
                case 1:
                    setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.QUANTILE );
                    break;
                case 2:
                    setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.MEAN_ABOVE_QUANTILE );
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            storedValue = config.getString( GENE_SET_RESAMPLING_SCORE_METHOD, GeneScoreMethod.MEAN.toString() );
        }

        return SettingsHolder.GeneScoreMethod.valueOf( storedValue );
    }

    /**
     * <p>
     * getSettingsHolder.
     * </p>
     *
     * @return a {@link ubic.erminej.SettingsHolder} object.
     */
    public SettingsHolder getSettingsHolder() {
        return new SettingsHolder( config );
    }

    /**
     * <p>
     * isAutoSaving.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isAutoSaving() {
        return this.configBuilder.isAutoSave();
    }

    /**
     * <p>
     * setAlwaysUseEmpirical.
     * </p>
     *
     * @param b a boolean.
     */
    public void setAlwaysUseEmpirical( boolean b ) {
        this.config.setProperty( ALWAYS_USE_EMPIRICAL, b );
    }

    /**
     * <p>
     * setAnnotFile.
     * </p>
     *
     * @param val a {@link java.lang.String} object.
     */
    public void setAnnotFile( String val ) {
        this.config.setProperty( ANNOT_FILE, val );
    }

    /**
     * <p>
     * setAnnotFormat.
     * </p>
     *
     * @param arg a {@link ubic.erminej.data.GeneAnnotationParser.Format} object.
     */
    public void setAnnotFormat( Format arg ) {
        this.config.setProperty( ANNOT_FORMAT, arg.toString() );
    }

    /**
     * <p>
     * setBigIsBetter.
     * </p>
     *
     * @param b a boolean.
     */
    public void setBigIsBetter( boolean b ) {
        this.config.setProperty( BIG_IS_BETTER, b );
    }

    /**
     * <p>
     * setClassFile.
     * </p>
     *
     * @param val This is the GO XML file.
     */
    public void setClassFile( String val ) {
        this.config.setProperty( CLASS_FILE, val );
    }

    /**
     * <p>
     * setClassScoreMethod.
     * </p>
     *
     * @param val a Settings.Method object.
     */
    public void setClassScoreMethod( Settings.Method val ) {
        this.config.setProperty( CLASS_SCORE_METHOD, val.toString() );
    }

    /**
     * <p>
     * setCustomGeneSetDirectory.
     * </p>
     *
     * @param val a {@link java.lang.String} object.
     */
    public void setCustomGeneSetDirectory( String val ) {
        this.config.setProperty( CUSTOM_GENE_SET_DIRECTORY_PROPERTY, val );
    }

    /**
     * <p>
     * setDataCol.
     * </p>
     *
     * @param val a int.
     */
    public void setDataCol( int val ) {
        log.debug( "Setting data start column to " + val );
        this.config.setProperty( DATA_COL, val );
    }

    /**
     * <p>
     * setDataDirectory.
     * </p>
     *
     * @param val a {@link java.lang.String} object.
     */
    public void setDataDirectory( String val ) {
        this.config.setProperty( DATA_DIRECTORY, val );
    }

    /**
     * <p>
     * setDoLog.
     * </p>
     *
     * @param val a boolean.
     */
    public void setDoLog( boolean val ) {
        this.config.setProperty( DO_LOG, new Boolean( val ) );
    }

    /**
     * <p>
     * setGeneRepTreatment.
     * </p>
     *
     * @param val a MultiElementHandling object.
     */
    public void setGeneRepTreatment( MultiElementHandling val ) {
        this.config.setProperty( GENE_REP_TREATMENT, val.toString() );
    }

    /**
     * Only applies to ORA
     *
     * @param val a double.
     */
    public void setGeneScoreThreshold( double val ) {
        log.debug( "gene score threshold set to " + val );
        this.config.setProperty( GENE_SCORE_THRESHOLD_KEY, val );
    }

    /**
     * Set the method used to compute how values are combined (GSR method only).
     *
     * @param val a Settings.GeneScoreMethod object.
     */
    public void setGeneSetResamplingScoreMethod( Settings.GeneScoreMethod val ) {
        this.config.setProperty( GENE_SET_RESAMPLING_SCORE_METHOD, val.toString() );
    }

    /**
     * <p>
     * setGeneUrlBase.
     * </p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public void setGeneUrlBase( String url ) {
        this.config.setProperty( GENE_URL_BASE, url );
    }

    /**
     * Mostly used for testing
     *
     * @param goldStandardFile a {@link java.lang.String} object.
     */
    public void setGoldStandardFile( String goldStandardFile ) {
        this.config.setProperty( GOLD_STANDARD_FILE, goldStandardFile );
    }

    /**
     * <p>
     * setIterations.
     * </p>
     *
     * @param val a int.
     */
    public void setIterations( int val ) {
        this.config.setProperty( ITERATIONS, val );
    }

    /**
     * <p>
     * setMaxClassSize.
     * </p>
     *
     * @param val a int.
     */
    public void setMaxClassSize( int val ) {
        this.config.setProperty( MAX_CLASS_SIZE, val );
    }

    /**
     * <p>
     * setMinClassSize.
     * </p>
     *
     * @param val a int.
     */
    public void setMinClassSize( int val ) {
        this.config.setProperty( MIN_CLASS_SIZE, val );
    }

    /**
     * <p>
     * setMtc.
     * </p>
     *
     * @param mtc The mtc to set.
     */
    public void setMtc( MultiTestCorrMethod mtc ) {
        this.config.setProperty( MTC_CONFIG_NAME, mtc.toString() );
    }

    /**
     * Mostly used for testing
     *
     * @param outputFile a {@link java.lang.String} object.
     */
    public void setOutputFile( String outputFile ) {
        this.config.setProperty( OUTPUT_FILE, outputFile );
    }

    /**
     * <p>
     * setPrefFile.
     * </p>
     *
     * @param val a {@link java.lang.String} object.
     */
    public void setPrefFile( String val ) {
        this.config.setProperty( PREFERENCES_FILE_NAME, val );
    }

    /**
     * Set an arbitrary property. Handy for 'ad hoc' configuration parameters only used by specific classes.
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     */
    public void setProperty( String key, Object value ) {
        log.debug( "Setting property: " + key + " = " + value );
        this.getConfig().setProperty( key, value );
    }

    /**
     * <p>
     * setQuantile.
     * </p>
     *
     * @param val a int.
     */
    public void setQuantile( int val ) {
        this.config.setProperty( QUANTILE_CONFIG_NAME, val );
    }

    /**
     * <p>
     * setSaveAllGenesInOutput.
     * </p>
     *
     * @param saveAllGenes a boolean.
     */
    public void setSaveAllGenesInOutput( boolean saveAllGenes ) {
        config.setProperty( SAVE_ALL_GENES_IN_OUTPUT, saveAllGenes );

    }

    /**
     * Which column of the input score file has the scores. 2 means the first column after the row names.
     *
     * @param val a int.
     */
    public void setScoreCol( int val ) {
        log.debug( "Setting score start column to " + val );
        this.config.setProperty( SCORE_COL, val );
    }

    /**
     * @param seed
     */
    public void setRandomSeed( Long seed ) {
        this.config.setProperty( SEED, seed );
    }

    /**
     * <p>
     * setSelectedCustomGeneSets.
     * </p>
     *
     * @param addedClasses a {@link java.util.Collection} object.
     */
    public void setSelectedCustomGeneSets( Collection<GeneSetTerm> addedClasses ) {
        Collection<String> addedClassesIds = new HashSet<>();
        for ( GeneSetTerm t : addedClasses )
            addedClassesIds.add( t.getId() );
        this.config.setProperty( SELECTED_CUSTOM_GENESETS, addedClassesIds );
    }

    /**
     * <p>
     * setTester.
     * </p>
     *
     * @param isTester a boolean.
     */
    public void setTester( boolean isTester ) {
        this.config.setProperty( IS_TESTER, isTester );
    }

    /**
     * <p>
     * setUseBiologicalProcess.
     * </p>
     *
     * @param useBiologicalProcess The useBiologicalProcess to set.
     */
    public void setUseBiologicalProcess( boolean useBiologicalProcess ) {
        this.config.setProperty( USE_BIOL_PROC, new Boolean( useBiologicalProcess ) );
    }

    /**
     * <p>
     * setUseCellularComponent.
     * </p>
     *
     * @param useCellularComponent The useCellularComponent to set.
     */
    public void setUseCellularComponent( boolean useCellularComponent ) {
        this.config.setProperty( USE_CELL_COMP, useCellularComponent );
    }

    /**
     * <p>
     * setUseMolecularFunction.
     * </p>
     *
     * @param useMolecularFunction The useMolecularFunction to set.
     */
    public void setUseMolecularFunction( boolean useMolecularFunction ) {
        this.config.setProperty( USE_MOL_FUNC, useMolecularFunction );
    }

    /**
     * <p>
     * setUseMultifunctionalityCorrection.
     * </p>
     *
     * @param b a boolean.
     * @deprecated as this can always be true
     */
    public void setUseMultifunctionalityCorrection( boolean b ) {
        this.config.setProperty( USE_MULTIFUNCTIONALITY_CORRECTION, b );
    }

    /**
     * <p>
     * setUserGeneSetDirectory.
     * </p>
     *
     * @param dir The dir to set.
     */
    public void setUserGeneSetDirectory( String dir ) {
        this.config.setProperty( CUSTOM_GENE_SET_DIRECTORY_PROPERTY, dir );
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.config.toString();
    }

    /**
     * Intended to be used for saving results to the header of an output file, or when using the commnad line option
     * "saveconfig"
     *
     * @param fileName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public void writeAnalysisSettings( String fileName ) throws IOException {
        Writer out;
        if ( fileName == null ) {
            log.debug( "Output to STDOUT" );
            out = new BufferedWriter( new PrintWriter( System.out ) );
        } else {
            log.debug( "output " + fileName );
            log.debug( "Saving configuration to " + fileName );
            out = new BufferedWriter( new FileWriter( fileName ) );
        }

        for ( String propertyName : ANALYSIS_SETTINGS ) {

            if ( !this.loadUserDefined() && propertyName.equals( CUSTOM_GENESET_FILES ) ) {
                continue;
            }

            String value = "";
            if ( this.getProperty( propertyName ) == null ) {
                value = getDefault( propertyName ).toString();
            } else {
                value = config.getProperty( propertyName ).toString();
            }

            out.write( propertyName + " = " );
            out.write( StringEscapeUtils.escapeJava( value ) );
            out.write( "\n" );
        }
        out.close();
    }

    /**
     * Save the preferences to disk, if necessary, to the DEFAULT location (e.g. ermineJ.properties).
     */
    public void writePrefs() {
        if ( configBuilder.isAutoSave() ) return;
        try {
            log.debug( "Saving configuration to default location." );
            configBuilder.getFileHandler().setFile( this.getSettingsFilePath() );
            configBuilder.save();
        } catch ( ConfigurationException e ) {
            log.error( e, e );
        }
    }

    /**
     *
     */
    private void configLogging() {
        Logger logger = LogManager.getRootLogger();
        FileAppender appender = ( FileAppender ) logger.getAppender( "F" );

        try {
            File logFile = this.getLogFile();
            assert logFile != null;

            /*
             * FIXME for some reason, with the command line the appender is null.
             */
            if ( appender == null ) {
                appender = new FileAppender( new PatternLayout( "%p: %m%n" ), logFile.getAbsolutePath(), false );
                logger.addAppender( appender );
            } else {
                appender.setFile( logFile.getAbsolutePath() );
            }
            appender.activateOptions();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

    }

    /**
     * @return
     * @throws ConfigurationException
     */
    private PropertiesConfiguration getDefaultConfig() throws ConfigurationException {
        URL defaultConfigFileLocation = this.getClass().getResource( "/ubic/erminej/" + USERGUI_DEFAULT_PROPERTIES );

        if ( defaultConfigFileLocation == null ) {
            throw new ConfigurationException( "Defaults not found either!" );
        }

        log.info( "Found defaults at " + defaultConfigFileLocation );
        PropertiesConfiguration defaultConfig = ConfigUtils.loadConfig( defaultConfigFileLocation );
        return defaultConfig;
    }

    /**
     * @return
     */
    private File getSettingsFilePath() {
        File newConfigFile = new File( System.getProperty( "user.home" ) + System.getProperty( "file.separator" )
                + USERGUI_PROPERTIES );
        return newConfigFile;
    }

    /**
     * @throws IOException
     */
    private void initCustomClassesDirectory() throws IOException {
        String customGeneSetDirectoryName = null;
        if ( getCustomGeneSetDirectory() != null )
            customGeneSetDirectoryName = getCustomGeneSetDirectory();
        else
            customGeneSetDirectoryName = getDefaultUserClassesDirPath();

        if ( !FileTools.testDir( customGeneSetDirectoryName ) ) {
            log.info( "Creating " + customGeneSetDirectoryName );

            File customGeneSetDirectoryFile = new File( customGeneSetDirectoryName );

            if ( !customGeneSetDirectoryFile.exists() ) {
                log.debug( "does not exist" );
                if ( !new File( customGeneSetDirectoryName ).mkdir() ) {
                    throw new IOException( "Could not create a data directory at " + customGeneSetDirectoryName );
                }
            }
        }

        this.setCustomGeneSetDirectory( customGeneSetDirectoryName );
        log.debug( "Custom gene sets directory is " + customGeneSetDirectoryName );
    }

    /**
     * @throws IOException
     */
    private void initUserDirectories() throws IOException {
        String dataDirName = getDefaultUserDataDirPath();
        if ( !FileTools.testDir( dataDirName ) ) {
            log.info( "Creating " + dataDirName );

            File dataDirFile = new File( dataDirName );

            if ( !dataDirFile.exists() && !new File( dataDirName ).mkdir() ) {
                throw new IOException( "Could not create a data directory at " + dataDirName );
            }
        }
        this.setDataDirectory( dataDirName );

        log.info( "Data directory is " + this.getDataDirectory() );
        initCustomClassesDirectory();

    }

    /**
     *
     */
    private void loadOrCreateInitialConfig() {

        File newConfigFile = getSettingsFilePath();
        if ( !newConfigFile.exists() ) {
            try {
                FileTools.touch( newConfigFile );
            } catch ( IOException e ) {
                throw new RuntimeException( "Could not initialize the configuration file: " + newConfigFile
                        + "; please make sure the directory is writeable" );
            }
        }

        try {
            URL configFileLocation = ConfigUtils.locate( USERGUI_PROPERTIES );
            if ( configFileLocation == null ) throw new ConfigurationException( "Doesn't exist" );

            this.configBuilder = ConfigUtils.getConfigBuilder( configFileLocation );
            this.config = configBuilder.getConfiguration();
            // make sure defaults are defined.
            PropertiesConfiguration defaultConfig = getDefaultConfig();
            for ( Iterator<String> it = defaultConfig.getKeys(); it.hasNext(); ) {
                String key = it.next();
                if ( config.getProperty( key ) == null ) {
                    log.info( "Setting default for: " + key + "=" + defaultConfig.getProperty( key ) );
                    config.setProperty( key, defaultConfig.getProperty( key ) );
                }
            }

            log.info( "Got configuration from " + configFileLocation );
        } catch ( ConfigurationException e ) {
            try {
                log.info( "User properties file doesn't exist, creating new one from defaults" );
                PropertiesConfiguration defaultConfig = getDefaultConfig();

                this.configBuilder = ConfigUtils.getConfigBuilder( newConfigFile );
                this.config = configBuilder.getConfiguration();
                Iterator<?> keys = defaultConfig.getKeys();
                for ( ; keys.hasNext(); ) {
                    String k = ( String ) keys.next();
                    this.config.addProperty( k, defaultConfig.getProperty( k ) );
                }

                log.info( "Saved the new configuration in " + configBuilder.getFileHandler().getPath() );

            } catch ( ConfigurationException e1 ) {
                log.error( "Failed to initialize the configuration file, falling back: " + e1.getMessage() );
                try {
                    this.configBuilder = ConfigUtils.getConfigBuilder( newConfigFile );
                    this.config = configBuilder.getConfiguration();
                } catch ( ConfigurationException e2 ) {
                    throw new RuntimeException( "Completely failed to get configuration (" + newConfigFile + "): "
                            + e2.getMessage() );
                }
                this.configBuilder.getFileHandler().setPath( newConfigFile.getAbsolutePath() );
            }
        }

        if ( this.config != null ) this.config.setHeader( HEADER );

        if ( this.config != null ) this.configBuilder.setAutoSave( true );

        configLogging();

        logLocale();

        assert this.configBuilder != null;
    }

    /**
     * print out information about user's setup.
     */
    private void logLocale() {
        try {
            log.info( "Log file is " + this.getLogFile() );
        } catch ( IOException e1 ) {
            log.error( "Could not identify log file!" );
        }
        try {
            log.info( "System information:" );
            log.info( "    User country: " + System.getProperty( "user.country" ) );
            log.info( "    User language: " + System.getProperty( "user.language" ) );
            log.info( "    User home directory: " + System.getProperty( "user.home" ) );
            log.info( "    User working directory: " + System.getProperty( "user.dir" ) );
            log.info( "    Java version: " + System.getProperty( "java.runtime.version" ) );
            log.info( "    OS arch: " + System.getProperty( "os.arch" ) );
            log.info( "    OS name: " + System.getProperty( "os.name" ) );
            log.info( "    File encoding: " + System.getProperty( "file.encoding" ) );
        } catch ( SecurityException e ) {
            log.info( "Unable to get system information due to security restriction: " + e.getMessage() );
        }
    }

}
