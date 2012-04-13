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
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;

import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneAnnotationParser.Format;

/**
 * Basically a wrapper around a Commons Configuration object.
 * 
 * @author Paul Pavlidis (total rewrite)
 * @author Kiran Keshav
 * @author Homin Lee
 * @author Will Braynen
 * @version $Id$
 */
public class Settings extends SettingsHolder {

    private static final String ERMINE_J_LOG_FILE_NAME = "ermineJ.log";

    private static final Log log = LogFactory.getLog( Settings.class );

    /**
     * Settings that we need to write to analysis results files. Other settings are not needed there (like window sizes,
     * etc.)
     */
    protected static final String[] ANALYSIS_SETTINGS = new String[] { GENE_SCORE_THRESHOLD_KEY, QUANTILE_CONFIG_NAME,
            GENE_SET_RESAMPLING_SCORE_METHOD, MAX_CLASS_SIZE, MIN_CLASS_SIZE, RAW_FILE_CONFIG_NAME, SCORE_FILE,
            SCORE_COL, MTC_CONFIG_NAME, ITERATIONS, CLASS_FILE, BIG_IS_BETTER, DO_LOG, GENE_REP_TREATMENT,
            ALWAYS_USE_EMPIRICAL, ANNOT_FILE, ANNOT_FORMAT, CLASS_SCORE_METHOD, FILTER_NONSPECIFIC,
            USE_MULTIFUNCTIONALITY_CORRECTION, USE_MOL_FUNC, USE_BIOL_PROC, USE_CELL_COMP, USE_USER_DEFINED_GROUPS,
            CUSTOM_GENESET_FILES };

    /**
     * Part of the distribution, where defaults can be read from. If it is absent, hard-coded defaults are used.
     */
    protected static final String USERGUI_DEFAULT_PROPERTIES = "ermineJdefault.properties";

    /**
     * Filename for settings.
     */
    protected static final String USERGUI_PROPERTIES = "ermineJ.properties";

    /**
     * Header for the config file.
     */
    protected static final String HEADER = "Configuration file for ermineJ."
            + "Do not delete this file if you want your ermineJ settings to stay across sessions.\nFor more information see http://www.chibi.ubc.ca/ermineJ/";

    /**
     * Write a configuration to the given file - but just settings relevant to analysis (not window locations, for
     * example).
     * 
     * @param settings
     * @param fileName
     * @throws IOException
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
     * @param settings
     * @param out
     * @throws IOException
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
     * 
     */
    private File logFile;

    /**
     * Create the settings, reading them from a file to be determined by the constructor.
     * 
     * @throws IOException if there are problems setting up the configuration.
     */
    public Settings() throws IOException {
        this( true );
    }

    /**
     * @param readFromFile if true, the user's config will be read in. If false, a blank (default) configuration will be
     *        initialized.
     * @throws IOException
     */
    public Settings( boolean readFromFile ) throws IOException {
        super();
        if ( readFromFile ) {
            loadOrCreateInitialConfig();
        } else {
            this.config = new PropertiesConfiguration();
        }
        initUserDirectories();
    }

    /**
     * Creates settings object from a copy. Note that in this situation, autoSave is FALSE.
     * 
     * @param settings - settings object to copy
     */
    public Settings( SettingsHolder settingsToCopy ) {
        this.config = new PropertiesConfiguration();
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
     * @param configurationFile
     * @throws IOException
     */
    public Settings( String configurationFile ) throws ConfigurationException, IOException {
        this.config = new PropertiesConfiguration( configurationFile );
        initUserDirectories();
    }

    /**
     * Autosave will not be set.
     * 
     * @param resource
     * @throws IOException
     */
    public Settings( URL resource ) throws ConfigurationException, IOException {
        this.config = new PropertiesConfiguration( resource );
        initUserDirectories();
    }

    /**
     * @return
     */
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
                default:
                    throw new IllegalStateException();
            }
            storedValue = config.getString( ANNOT_FORMAT, Format.DEFAULT.toString() );
        }

        return Format.valueOf( storedValue );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.erminej.SettingsHolder#getGeneScoreThreshold()
     */
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

    /**
     * @return
     */
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

    /**
     * @return
     */
    @Override
    public PropertiesConfiguration getConfig() {
        if ( config == null ) loadOrCreateInitialConfig();
        if ( config == null ) {
            throw new IllegalStateException( "Unable to initialize configuration" );
        }
        return config;
    }

    /**
     * @return
     */
    @Override
    public MultiProbeHandling getGeneRepTreatment() {
        String storedValue = config.getString( GENE_REP_TREATMENT, MultiProbeHandling.BEST.toString() );

        // backwards compatibility
        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );
            switch ( oldVal ) {
                case 1:
                    setGeneRepTreatment( SettingsHolder.MultiProbeHandling.BEST );
                    break;
                case 2:
                    setGeneRepTreatment( SettingsHolder.MultiProbeHandling.MEAN );
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            storedValue = config.getString( GENE_REP_TREATMENT, MultiProbeHandling.BEST.toString() );
        }

        return SettingsHolder.MultiProbeHandling.valueOf( storedValue );
    }

    /**
     * @return the method to be used to combine scores. This is only relevant for the gene set resampling method.
     */
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
     * Determine where to put the log file.
     * 
     * @return
     */
    public File getLogFile() {
        // rotating logs: not implemented.
        if ( logFile == null ) {
            logFile = new File( System.getProperty( "user.home" ) + System.getProperty( "file.separator" )
                    + ERMINE_J_LOG_FILE_NAME );
            return logFile;
        }
        return logFile;
    }

    public SettingsHolder getSettingsHolder() {
        return new SettingsHolder( config );
    }

    public boolean isAutoSaving() {
        return this.config.isAutoSave();
    }

    /**
     * @param b
     */
    public void setAlwaysUseEmpirical( boolean b ) {
        this.config.setProperty( ALWAYS_USE_EMPIRICAL, b );
    }

    public void setAnnotFile( String val ) {
        this.config.setProperty( ANNOT_FILE, val );
    }

    /**
     * @param arg
     */
    public void setAnnotFormat( Format arg ) {
        this.config.setProperty( ANNOT_FORMAT, arg.toString() );
    }

    /**
     * @param b
     */
    public void setBigIsBetter( boolean b ) {
        this.config.setProperty( BIG_IS_BETTER, b );
    }

    /**
     * Sets setting values.
     */
    public void setClassFile( String val ) {
        this.config.setProperty( CLASS_FILE, val );
    }

    public void setClassScoreMethod( Settings.Method val ) {
        this.config.setProperty( CLASS_SCORE_METHOD, val.toString() );
    }

    public void setCustomGeneSetDirectory( String val ) {
        this.config.setProperty( CUSTOM_GENE_SET_DIRECTORY_PROPERTY, val );
    }

    public void setDataCol( int val ) {
        log.debug( "Setting data start column to " + val );
        this.config.setProperty( DATA_COL, val );
    }

    public void setDataDirectory( String val ) {
        this.config.setProperty( DATA_DIRECTORY, val );
    }

    public void setDoLog( boolean val ) {
        this.config.setProperty( DO_LOG, new Boolean( val ) );
    }

    public void setFilterNonSpecific( boolean val ) {
        this.config.setProperty( FILTER_NONSPECIFIC, new Boolean( val ) );
    }

    public void setGeneRepTreatment( MultiProbeHandling val ) {
        this.config.setProperty( GENE_REP_TREATMENT, val.toString() );
    }

    /**
     * Only applies to ORA
     * 
     * @param val
     */
    public void setGeneScoreThreshold( double val ) {
        log.debug( "gene score threshold set to " + val );
        this.config.setProperty( GENE_SCORE_THRESHOLD_KEY, val );
    }

    /**
     * Set the method used to compute how values are combined (GSR method only).
     * 
     * @param val
     */
    public void setGeneSetResamplingScoreMethod( Settings.GeneScoreMethod val ) {
        this.config.setProperty( GENE_SET_RESAMPLING_SCORE_METHOD, val.toString() );
    }

    /**
     * @param url
     */
    public void setGeneUrlBase( String url ) {
        this.config.setProperty( GENE_URL_BASE, url );
    }

    /**
     * Mostly used for testing
     * 
     * @param goldStandardFile
     */
    public void setGoldStandardFile( String goldStandardFile ) {
        this.config.setProperty( GOLD_STANDARD_FILE, goldStandardFile );
    }

    public void setIterations( int val ) {
        this.config.setProperty( ITERATIONS, val );
    }

    public void setMaxClassSize( int val ) {
        this.config.setProperty( MAX_CLASS_SIZE, val );
    }

    public void setMinClassSize( int val ) {
        this.config.setProperty( MIN_CLASS_SIZE, val );
    }

    /**
     * @param mtc The mtc to set.
     */
    public void setMtc( MultiTestCorrMethod mtc ) {
        this.config.setProperty( MTC_CONFIG_NAME, mtc.toString() );
    }

    /**
     * Mostly used for testing
     * 
     * @param outputFile
     */
    public void setOutputFile( String outputFile ) {
        this.config.setProperty( OUTPUT_FILE, outputFile );
    }

    public void setPrefFile( String val ) {
        this.config.setProperty( PREFERENCES_FILE_NAME, val );
    }

    /**
     * Set an arbitrary property. Handy for 'ad hoc' configuration parameters only used by specific classes.
     * 
     * @param key
     * @param value
     */
    public void setProperty( String key, Object value ) {
        log.debug( "Setting property: " + key + " = " + value );
        this.getConfig().setProperty( key, value );
    }

    public void setQuantile( int val ) {
        this.config.setProperty( QUANTILE_CONFIG_NAME, val );
    }

    public void setSaveAllGenesInOutput( boolean saveAllGenes ) {
        config.setProperty( SAVE_ALL_GENES_IN_OUTPUT, saveAllGenes );

    }

    public void setScoreCol( int val ) {
        log.debug( "Setting score start column to " + val );
        this.config.setProperty( SCORE_COL, val );
    }

    public void setSelectedCustomGeneSets( Collection<GeneSetTerm> addedClasses ) {
        Collection<String> addedClassesIds = new HashSet<String>();
        for ( GeneSetTerm t : addedClasses )
            addedClassesIds.add( t.getId() );
        this.config.setProperty( SELECTED_CUSTOM_GENESETS, addedClassesIds );
    }

    public void setTester( boolean isTester ) {
        this.config.setProperty( IS_TESTER, isTester );
    }

    /**
     * @param useBiologicalProcess The useBiologicalProcess to set.
     */
    public void setUseBiologicalProcess( boolean useBiologicalProcess ) {
        this.config.setProperty( USE_BIOL_PROC, new Boolean( useBiologicalProcess ) );
    }

    /**
     * @param useCellularComponent The useCellularComponent to set.
     */
    public void setUseCellularComponent( boolean useCellularComponent ) {
        this.config.setProperty( USE_CELL_COMP, useCellularComponent );
    }

    /**
     * @param useMolecularFunction The useMolecularFunction to set.
     */
    public void setUseMolecularFunction( boolean useMolecularFunction ) {
        this.config.setProperty( USE_MOL_FUNC, useMolecularFunction );
    }

    /**
     * @param b
     */
    public void setUseMultifunctionalityCorrection( boolean b ) {
        this.config.setProperty( USE_MULTIFUNCTIONALITY_CORRECTION, b );
    }

    /**
     * @param dir The dir to set.
     */
    public void setUserGeneSetDirectory( String dir ) {
        this.config.setProperty( CUSTOM_GENE_SET_DIRECTORY_PROPERTY, dir );
    }

    @Override
    public String toString() {
        return this.config.toString();
    }

    /**
     * Intended to be used for saving results to the header of an output file.
     * 
     * @param fileName
     * @throws IOException
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
            if ( this.getProperty( propertyName ) == null ) {
                /*
                 * This happens if we are using the defaults.
                 */
                log.debug( "No property " + propertyName + ", skipping" );
                continue;
            }

            out.write( propertyName + " = " );
            out.write( StringEscapeUtils.escapeJava( config.getProperty( propertyName ).toString() ) );
            out.write( "\n" );
        }
        out.close();
    }

    /**
     * Save the preferences to disk, if necessary, to the DEFAULT location (e.g. ermineJ.properties).
     */
    public void writePrefs() {
        if ( config.isAutoSave() ) return;
        try {
            log.debug( "Saving configuration to default location." );
            config.save( this.getSettingsFilePath() );
        } catch ( ConfigurationException e ) {
            log.error( e, e );
        }
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

        logLocale();

        File newConfigFile = getSettingsFilePath();

        try {
            URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
            if ( configFileLocation == null ) throw new ConfigurationException( "Doesn't exist" );

            this.config = new PropertiesConfiguration( configFileLocation );

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

                this.config = new PropertiesConfiguration( newConfigFile );
                this.config.setPath( newConfigFile.getAbsolutePath() );
                Iterator<?> keys = defaultConfig.getKeys();
                for ( ; keys.hasNext(); ) {
                    String k = ( String ) keys.next();
                    this.config.addProperty( k, defaultConfig.getProperty( k ) );
                }

                log.info( "Saved the new configuration in " + config.getPath() );

            } catch ( ConfigurationException e1 ) {
                log.error( "Failed to initialize the configuration file, falling back: " + e1.getMessage() );
                try {
                    this.config = new PropertiesConfiguration( newConfigFile );
                } catch ( ConfigurationException e2 ) {
                    throw new RuntimeException( "Completely failed to get configuration" );
                }
                this.config.setPath( newConfigFile.getAbsolutePath() );
            }
        }

        if ( this.config != null ) this.config.setHeader( HEADER );

        if ( this.config != null ) this.config.setAutoSave( true );
    }

    /**
     * @return
     * @throws ConfigurationException
     */
    private PropertiesConfiguration getDefaultConfig() throws ConfigurationException {
        URL defaultConfigFileLocation = this.getClass().getResource( "/ubic/erminej/" + USERGUI_DEFAULT_PROPERTIES );

        // URL defaultConfigFileLocation = ConfigurationUtils.locate( USERGUI_DEFAULT_PROPERTIES );

        if ( defaultConfigFileLocation == null ) {
            throw new ConfigurationException( "Defaults not found either!" );
        }

        log.info( "Found defaults at " + defaultConfigFileLocation );
        PropertiesConfiguration defaultConfig = new PropertiesConfiguration( defaultConfigFileLocation );
        return defaultConfig;
    }

    /**
     * 
     */
    private void logLocale() {
        log.info( "System information:" );
        log.info( "    User country: " + System.getProperty( "user.country" ) );
        log.info( "    User language: " + System.getProperty( "user.language" ) );
        log.info( "    User home directory: " + System.getProperty( "user.home" ) );
        log.info( "    User working directory: " + System.getProperty( "user.dir" ) );
        log.info( "    Java version: " + System.getProperty( "java.runtime.version" ) );
        log.info( "    OS arch: " + System.getProperty( "os.arch" ) );
        log.info( "    OS version: " + System.getProperty( "os.name" ) );
        log.info( "    File encoding: " + System.getProperty( "file.encoding" ) );
    }

}