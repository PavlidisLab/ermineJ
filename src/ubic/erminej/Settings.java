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
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;

import ubic.basecode.bio.geneset.GeneAnnotations;

/**
 * Basically a wrapper around a Commons Configuration object.
 * 
 * @author Paul Pavlidis (total rewrite)
 * @author Kiran Keshav
 * @author Homin Lee
 * @author Will Braynen
 * @version $Id$
 */
public class Settings {
    public static final int BENJAMINIHOCHBERG = 2;

    public static final int BEST_PVAL = 1;
    public static final int BONFERONNI = 0;
    public static final int CORR = 2;
    public static final String GENE_URL_BASE = "gene.url.base";
    public static final int KS = 5;
    public static final int MEAN_ABOVE_QUANTILE_METHOD = 2;
    public static final int MEAN_METHOD = 0;
    public static final int MEAN_PVAL = 2;
    public static final int ORA = 0;
    public static final int QUANTILE_METHOD = 1;
    public static final int RESAMP = 1;
    public static final int ROC = 3;
    public static final int TTEST = 4;
    public static final int WESTFALLYOUNG = 1;
    private static final String ALWAYS_USE_EMPIRICAL = "alwaysUseEmpirical";
    private static final String ANNOT_FILE = "annotFile";
    private static final String ANNOT_FORMAT = "annotFormat";
    private static final String BIG_IS_BETTER = "bigIsBetter";
    private static final String CLASS_FILE = "classFile";
    private static final String CLASS_SCORE_METHOD = "classScoreMethod";
    private static final String DATA_DIRECTORY = "dataDirectory";
    private static final String DO_LOG = "doLog";
    private static final String GENE_REP_TREATMENT = "geneRepTreatment";
    private static final String GOLD_STANDARD_FILE = "goldStandardFile";
    private static final String IS_TESTER = "isTester";
    private static final String ITERATIONS = "iterations";
    private static final Log log = LogFactory.getLog( Settings.class );
    private static final String MAX_CLASS_SIZE = "maxClassSize";
    private static final String MIN_CLASS_SIZE = "minClassSize";
    private static final String MTC = "mtc";
    private static final String OUTPUT_FILE = "outputFile";
    private static final String P_VAL_THRESHOLD = "pValThreshold";
    private static final String PREFERENCES_FILE_NAME = "preferencesFileName";
    private static final String QUANTILE = "quantile";
    private static final String RAW_FILE = "rawFile";
    private static final String RAW_SCORE_METHOD = "rawScoreMethod";
    private static final String SCORE_COL = "scoreCol";
    private static final String SCORE_FILE = "scoreFile";
    private static final String SELECTED_CUSTOM_GENESETS = "selectedCustomGeneSets";
    /**
     * Part of the distribution, where defaults can be read from. If it is absent, hard-coded defaults are used.
     */
    private static final String USERGUI_DEFAULT_PROPERTIES = "ermineJdefault.properties";

    /**
     * Filename for settings.
     */
    private static final String USERGUI_PROPERTIES = "ermineJ.properties";

    /**
     * where everything is kept.
     */
    private PropertiesConfiguration config;

    private File logFile;

    /**
     * Indicates whether the user needs to be prompted for a data file.
     */
    private boolean userSetRawDataFile = true;

    /**
     * Settings that we need to write to analysis results files. Other settings are not needed there (like window sizes,
     * etc.)
     */
    protected static final String[] ANALYSIS_SETTINGS = new String[] { P_VAL_THRESHOLD, QUANTILE, RAW_SCORE_METHOD,
            MAX_CLASS_SIZE, MIN_CLASS_SIZE, RAW_FILE, SCORE_FILE, SCORE_COL, MTC, ITERATIONS, CLASS_FILE,
            BIG_IS_BETTER, DO_LOG, GENE_REP_TREATMENT, ALWAYS_USE_EMPIRICAL, ANNOT_FILE, ANNOT_FORMAT,
            CLASS_SCORE_METHOD };

    /**
     * Create the settings, reading them from a file to be determined by the constructor.
     * 
     * @throws IOException
     */
    public Settings() throws IOException {
        this( true );
    }

    public Settings( boolean readFromFile ) throws IOException {
        if ( readFromFile ) {
            initConfig();
            // createDataDirectory();
            // createCustomGeneSetDirectory();
        } else {
            this.config = new PropertiesConfiguration();
        }
    }

    public void setDirectories() throws IOException {
        createDataDirectory();
        createCustomGeneSetDirectory();
    }

    /**
     * Creates settings object from a copy. Note that in this situation, autoSave is FALSE.
     * 
     * @param settings - settings object to copy
     */
    public Settings( Settings settingsToCopy ) {
        this.config = new PropertiesConfiguration();
        PropertiesConfiguration oldConfig = settingsToCopy.getConfig();
        for ( Iterator iter = oldConfig.getKeys(); iter.hasNext(); ) {
            String key = ( String ) iter.next();
            Object value = oldConfig.getProperty( key );
            this.config.setProperty( key, value );
        }
    }

    /**
     * Create a Settings object from the header of a results file.
     * 
     * @param resultsFile
     */
    public Settings( String resultsFile ) throws ConfigurationException {
        this.config = new PropertiesConfiguration( resultsFile );

    }

    /**
     * @param resource
     */
    public Settings( URL resource ) throws ConfigurationException {
        this.config = new PropertiesConfiguration( resource );
    }

    /**
     * Figure out where the data directory should go.
     * 
     * @return
     */
    public boolean determineDataDirectory() {
        String dataDirectoryName = System.getProperty( "user.dir" );
        dataDirectoryName = dataDirectoryName.substring( 0, dataDirectoryName.lastIndexOf( System
                .getProperty( "file.separator" ) ) );

        dataDirectoryName = dataDirectoryName + System.getProperty( "file.separator" ) + "ermineJ.data";

        if ( !FileTools.testDir( dataDirectoryName ) ) {
            this.setDataDirectory( dataDirectoryName );

            if ( !FileTools.testDir( dataDirectoryName ) ) {
                log.info( "Creating data directory " + dataDirectoryName );
                return ( new File( dataDirectoryName ) ).mkdir();
            }
        }
        log.info( "Data directory is " + dataDirectoryName );
        this.setDataDirectory( dataDirectoryName );
        return true;
    }

    /**
     * @return
     */
    public boolean getAlwaysUseEmpirical() {
        return config.getBoolean( ALWAYS_USE_EMPIRICAL, new Boolean( false ) ).booleanValue();

    }

    public String getAnnotFile() {
        return config.getString( ANNOT_FILE );
    }

    /**
     * @return
     */
    public int getAnnotFormat() {
        return config.getInteger( ANNOT_FORMAT, new Integer( GeneAnnotations.DEFAULT ) ).intValue();
    }

    /**
     * @return
     */
    public boolean getBigIsBetter() {
        return config.getBoolean( BIG_IS_BETTER, new Boolean( false ) ).booleanValue();
    }

    /**
     * Returns setting values.
     */
    public String getClassFile() {
        return config.getString( CLASS_FILE );
    }

    public int getClassScoreMethod() {
        return config.getInteger( CLASS_SCORE_METHOD, new Integer( ORA ) ).intValue();
    }

    public String getClassScoreMethodString() {
        if ( this.getClassScoreMethod() == MEAN_METHOD ) {
            return "Mean";
        }
        return "Quantile"; // note that quantile is hard-coded to be 50 for the
        // gui.
    }

    /**
     * @return
     */
    public PropertiesConfiguration getConfig() {
        if ( config == null ) initConfig();
        if ( config == null ) {
            return null;
        }
        return config;
    }

    public String getDataDirectory() {
        return config.getString( DATA_DIRECTORY );

    }

    public boolean getDoLog() {
        return config.getBoolean( DO_LOG, new Boolean( true ) ).booleanValue();
    }

    public int getGeneRepTreatment() {
        return config.getInteger( GENE_REP_TREATMENT, new Integer( BEST_PVAL ) ).intValue();
    }

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getGoldStandardFile() {
        return config.getString( GOLD_STANDARD_FILE );
    }

    public String getGroupMethodString() {
        if ( this.getGeneRepTreatment() == MEAN_PVAL )
            return "MEAN_PVAL";
        else if ( this.getGeneRepTreatment() == BEST_PVAL )
            return "BEST_PVAL";
        else
            return "MEAN_PVAL"; // dummy. It won't be used.
    }

    public int getIterations() {
        return config.getInteger( ITERATIONS, new Integer( 10000 ) ).intValue();
    }

    /**
     * Determine where to put the log file.
     * 
     * @return
     */
    public File getLogFile() {
        if ( logFile == null ) {
            // Calendar c = Calendar.getInstance();
            // this.logFile = new File( this.getDataDirectory() + System.getProperty( "file.separator" ) + "log."
            // + c.get( Calendar.YEAR ) + c.get( Calendar.MONTH ) + c.get( Calendar.DATE ) + c.get( Calendar.HOUR )
            // + c.get( Calendar.MINUTE ) + c.get( Calendar.SECOND ) );
            // log.info( "Creating log file: " + logFile.getAbsolutePath() );
            logFile = new File( System.getProperty( "user.home" ) + System.getProperty( "file.separator" )
                    + "ermineJ.log" );
            return logFile;
        }
        return logFile;
    }

    public int getMaxClassSize() {
        return config.getInteger( MAX_CLASS_SIZE, new Integer( 100 ) ).intValue();
    }

    public int getMinClassSize() {
        return config.getInteger( MIN_CLASS_SIZE, new Integer( 5 ) ).intValue();
    }

    /**
     * @return Returns the mtc.
     */
    public int getMtc() {
        return config.getInteger( MTC, new Integer( BENJAMINIHOCHBERG ) ).intValue();
    }

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getOutputFile() {
        return config.getString( OUTPUT_FILE );

    }

    public String getPrefFile() {
        return config.getString( PREFERENCES_FILE_NAME );

    }

    public double getPValThreshold() {
        return config.getDouble( P_VAL_THRESHOLD, new Double( 0.001 ) ).doubleValue();
    }

    public int getQuantile() {
        return config.getInteger( QUANTILE, new Integer( 50 ) ).intValue();
    }

    public String getRawDataFileName() {
        return config.getString( RAW_FILE );
    }

    public int getRawScoreMethod() {
        return config.getInteger( RAW_SCORE_METHOD, new Integer( MEAN_METHOD ) ).intValue();
    }

    public int getScoreCol() {
        return config.getInteger( SCORE_COL, new Integer( 2 ) ).intValue();
    }

    public String getScoreFile() {
        return config.getString( SCORE_FILE );
    }

    /**
     * @return Returns the useBiologicalProcess.
     */
    public boolean getUseBiologicalProcess() {
        return config.getBoolean( "useBiologicalProcess", new Boolean( true ) ).booleanValue();

    }

    public Collection getSelectedCustomGeneSets() {
        return config.getList( SELECTED_CUSTOM_GENESETS, null );
    }

    /**
     * @return Returns the useCellularComponent.
     */
    public boolean getUseCellularComponent() {
        return config.getBoolean( "useCellularComponent", new Boolean( true ) ).booleanValue();

    }

    /**
     * @return
     */
    public boolean getUseLog() {
        return config.getBoolean( DO_LOG, new Boolean( true ) ).booleanValue();
    }

    /**
     * @return Returns the useMolecularFunction.
     */
    public boolean getUseMolecularFunction() {
        return config.getBoolean( "useMolecularFunction", new Boolean( true ) ).booleanValue();

    }

    public String getUserGeneSetDirectory() {
        String dir = config.getString( "classFolder" );
        if ( dir == null ) {
            dir = System.getProperty( "user.dir" );
            if ( dir == null ) {
                dir = System.getProperty( "user.home" );
                if ( dir == null ) {
                    throw new IllegalStateException( "Could not locate a user-defined gene set directory" );
                }
                this.setUserGeneSetDirectory( dir );
            } else {
                this.setUserGeneSetDirectory( dir );
            }
        }
        return dir;
    }

    /**
     * @return
     */
    public boolean getUserSetRawFile() {
        return this.userSetRawDataFile;
    }

    public boolean getUseWeights() {
        if ( this.getGeneRepTreatment() == MEAN_PVAL || this.getGeneRepTreatment() == BEST_PVAL ) return true;
        return false;
    }

    public boolean isTester() {
        return config.getBoolean( IS_TESTER, new Boolean( false ) ).booleanValue();
    }

    /**
     * @param b
     */
    public void setAlwaysUseEmpirical( boolean b ) {
        this.config.setProperty( ALWAYS_USE_EMPIRICAL, new Boolean( b ) );
    }

    public void setAnnotFile( String val ) {
        this.config.setProperty( ANNOT_FILE, val );
    }

    /**
     * @param arg
     */
    public void setAnnotFormat( String arg ) {
        if ( arg.equalsIgnoreCase( "affy" ) || arg.equalsIgnoreCase( "Affy CSV" ) ) {
            this.config.setProperty( ANNOT_FORMAT, new Integer( GeneAnnotations.AFFYCSV ) );
        } else if ( arg.equalsIgnoreCase( "agilent" ) ) {
            this.config.setProperty( ANNOT_FORMAT, new Integer( GeneAnnotations.AGILENT ) );
        } else {
            this.config.setProperty( ANNOT_FORMAT, new Integer( GeneAnnotations.DEFAULT ) );
        }

    }

    /**
     * @param b
     */
    public void setBigIsBetter( boolean b ) {
        this.config.setProperty( BIG_IS_BETTER, new Boolean( b ) );
    }

    /**
     * Sets setting values.
     */
    public void setClassFile( String val ) {
        this.config.setProperty( CLASS_FILE, val );
    }

    public void setClassScoreMethod( int val ) {
        this.config.setProperty( CLASS_SCORE_METHOD, new Integer( val ) );
    }

    public void setCustomGeneSetDirectory( String val ) {
        this.config.setProperty( "classFolder", val );
    }

    public String getCustomGeneSetDirectory() {
        return ( String ) this.config.getProperty( "classFolder" );
    }

    public void setDataDirectory( String val ) {
        this.config.setProperty( DATA_DIRECTORY, val );
    }

    public void setDoLog( boolean val ) {
        this.config.setProperty( DO_LOG, new Boolean( val ) );
    }

    public void setGeneRepTreatment( int val ) {
        this.config.setProperty( GENE_REP_TREATMENT, new Integer( val ) );
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
        this.config.setProperty( ITERATIONS, new Integer( val ) );
    }

    public void setMaxClassSize( int val ) {
        this.config.setProperty( MAX_CLASS_SIZE, new Integer( val ) );
    }

    public void setMinClassSize( int val ) {
        this.config.setProperty( MIN_CLASS_SIZE, new Integer( val ) );
    }

    /**
     * @param mtc The mtc to set.
     */
    public void setMtc( int mtc ) {
        this.config.setProperty( MTC, new Integer( mtc ) );
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

    public void setPValThreshold( double val ) {
        log.debug( "pvalue threshold set to " + val );
        this.config.setProperty( P_VAL_THRESHOLD, new Double( val ) );
    }

    public void setQuantile( int val ) {
        this.config.setProperty( QUANTILE, new Integer( val ) );
    }

    public void setRawFile( String val ) {
        this.config.setProperty( RAW_FILE, val );
    }

    public void setRawScoreMethod( int val ) {
        this.config.setProperty( RAW_SCORE_METHOD, new Integer( val ) );
    }

    public void setScoreCol( int val ) {
        this.config.setProperty( SCORE_COL, new Integer( val ) );
    }

    public void setScoreFile( String val ) {
        this.config.setProperty( SCORE_FILE, val );
    }

    public void setTester( boolean isTester ) {
        this.config.setProperty( IS_TESTER, new Boolean( isTester ) );
    }

    public void setSelectedCustomGeneSets( Collection selectedSets ) {
        this.config.setProperty( SELECTED_CUSTOM_GENESETS, selectedSets );
    }

    /**
     * @param useBiologicalProcess The useBiologicalProcess to set.
     */
    public void setUseBiologicalProcess( boolean useBiologicalProcess ) {
        this.config.setProperty( "useBiologicalProcess", new Boolean( useBiologicalProcess ) );
    }

    /**
     * @param useCellularComponent The useCellularComponent to set.
     */
    public void setUseCellularComponent( boolean useCellularComponent ) {
        this.config.setProperty( "useCellularComponent", new Boolean( useCellularComponent ) );
    }

    /**
     * @param useMolecularFunction The useMolecularFunction to set.
     */
    public void setUseMolecularFunction( boolean useMolecularFunction ) {
        this.config.setProperty( "useMolecularFunction", new Boolean( useMolecularFunction ) );
    }

    /**
     * @param dir The dir to set.
     */
    public void setUserGeneSetDirectory( String dir ) {
        this.config.setProperty( "userGeneSetDirectory", dir );
    }

    public String toString() {
        return this.config.toString();
    }

    /**
     * Determine whether we should be using the upper tails of our histograms. The "big is better" setting reflects the
     * original gene scores, not the log-transformed scores. Therefore if we are taking the log, and the user indicates
     * smaller values are better, then we do want to use the upper tail. If we're not taking the log, then we just
     * directly interpret what the user selected for 'bigIsBetter'.
     * 
     * @return true if we are using the "upper tail" of our distributions.
     */
    public boolean upperTail() {
        return ( this.getDoLog() && !this.getBigIsBetter() ) || ( !this.getDoLog() && this.getBigIsBetter() );
    }

    /**
     * @param setRawFile Set to false to indicate that the user wants to proceed without a data file.
     */
    public void userSetRawFile( boolean setRawFile ) {
        this.userSetRawDataFile = setRawFile;
    }

    /**
     * Intended to be used for saving results to the header of an output file.
     * 
     * @param fileName
     * @throws IOException
     */
    public void writeAnalysisSettings( String fileName ) throws IOException {
        log.debug( "output " + fileName );
        log.debug( "Saving configuration to " + fileName );
        BufferedWriter out = new BufferedWriter( new FileWriter( fileName ) );
        for ( int i = 0; i < ANALYSIS_SETTINGS.length; i++ ) {
            String propertyName = ANALYSIS_SETTINGS[i];

            if ( config.getProperty( propertyName ) == null ) {
                log.debug( "No property " + propertyName + ", skipping" );
                continue;
            }

            out.write( propertyName + " = " );
            out.write( StringEscapeUtils.escapeJava( config.getProperty( propertyName ).toString() ) );
            out.write( "\n" );
            log.debug( "Writing " + propertyName + "=" + config.getProperty( propertyName ).toString() );
        }
        out.close();
    }

    public void writePrefs() {
        if ( config.isAutoSave() ) return;
        try {
            log.debug( "Saving configuration" );
            config.save();
        } catch ( ConfigurationException e ) {
            log.error( e, e );
        }
    }

    /**
     * @throws IOException
     */
    private void createCustomGeneSetDirectory() throws IOException {

        String customGeneSetDirectoryName = null;
        if ( getCustomGeneSetDirectory() != null )
            customGeneSetDirectoryName = getCustomGeneSetDirectory();
        else
            customGeneSetDirectoryName = new String( this.userHomeDataDirectoryName() + File.separator + "genesets" );

        // if ( !FileTools.testDir( customGeneSetDirectoryName ) ) {
        // log.info( "Creating " + customGeneSetDirectoryName );
        //
        // File customGeneSetDirectoryFile = new File( customGeneSetDirectoryName );
        //
        // if ( !customGeneSetDirectoryFile.exists() ) {
        // log.debug( "does not exist" );
        // if ( !new File( customGeneSetDirectoryName ).mkdir() ) {
        // throw new IOException( "Could not create a data directory at " + customGeneSetDirectoryName );
        // }
        // }
        // }

        this.setCustomGeneSetDirectory( customGeneSetDirectoryName );
        log.debug( "Custom gene sets directory is " + customGeneSetDirectoryName );

    }

    /**
     * @throws IOException
     */
    private void createDataDirectory() throws IOException {
        String dataDirName = userHomeDataDirectoryName();
        if ( !FileTools.testDir( dataDirName ) ) {
            log.info( "Creating " + dataDirName );

            File dataDirFile = new File( dataDirName );

            if ( !dataDirFile.exists() ) {
                if ( !new File( dataDirName ).mkdir() ) {
                    throw new IOException( "Could not create a data directory at " + dataDirName );
                }
            }
        }
        this.setDataDirectory( dataDirName );
        log.info( "Data directory is " + this.getDataDirectory() );
    }

    /**
     * @return
     */
    private String userHomeDataDirectoryName() {
        String dataDirName = System.getProperty( "user.home" ) + System.getProperty( "file.separator" )
                + "ermineJ.data";
        return dataDirName;
    }

    /**
     * 
     */
    private void initConfig() {

        logLocale();

        try {
            URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
            if ( configFileLocation == null ) throw new ConfigurationException( "Doesn't exist" );

            this.config = new PropertiesConfiguration( configFileLocation );
            log.info( "Got configuration " + configFileLocation );
        } catch ( ConfigurationException e ) {
            try {
                log.info( "User properties file doesn't exist, creating new one from defaults" );
                URL defaultConfigFileLocation = ConfigurationUtils.locate( USERGUI_DEFAULT_PROPERTIES );

                if ( defaultConfigFileLocation == null )
                    throw new ConfigurationException( "Defaults not found either!" );

                log.info( "Found defaults at " + defaultConfigFileLocation );
                this.config = new PropertiesConfiguration( USERGUI_DEFAULT_PROPERTIES );
                File tempLocation = new File( config.getPath() );
                this.config.save(); // make sure the temporary file exists.
                File newConfigFile = new File( System.getProperty( "user.home" )
                        + System.getProperty( "file.separator" ) + USERGUI_PROPERTIES );

                this.config = new PropertiesConfiguration( tempLocation );
                this.config.setPath( newConfigFile.getAbsolutePath() );
                // this.config.save( newConfigFile ); // copy over to where they should be.
                // URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
                log.info( "Saved the new configuration in " + config.getPath() );
                if ( !tempLocation.delete() ) {
                    log.error( "Could not delete temporary configuration file from " + tempLocation.getAbsolutePath()
                            + ", please delete it manually" );
                    log.error( tempLocation.getAbsoluteFile() + ": Exists=" + tempLocation.exists() );
                    log.error( tempLocation.getAbsoluteFile() + ": Can write=" + tempLocation.canWrite() );
                } else {
                    log.debug( "Deleted temporary config file from " + tempLocation.getAbsolutePath() );
                }

            } catch ( ConfigurationException e1 ) {
                log.error( e1, e1 );
            }
        }
        this.config.setAutoSave( true );
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

    /**
     * @return
     */
    public String getRawDataFileDirectory() {
        String rdf = this.getRawDataFileName();
        if ( rdf == null ) return getDataDirectory();

        File rdfFile = new File( rdf );
        return rdfFile.getParent() == null ? getDataDirectory() : rdfFile.getParent();
    }

    /**
     * @return
     */
    public String getGeneScoreFileDirectory() {
        String gsf = this.getScoreFile();
        if ( gsf == null ) return getDataDirectory();

        File gsfFile = new File( gsf );
        return gsfFile.getParent() == null ? getDataDirectory() : gsfFile.getParent();
    }

}