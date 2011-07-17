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
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
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

    private static final Log log = LogFactory.getLog( Settings.class );

    /**
     * Which gene set scoring method to use.
     */
    public enum Method {
        ORA, ROC, GSR, CORR
    }

    /**
     * What to do when there are multiple values for a gene.
     */
    public enum MultiProbeHandling {
        BEST, MEAN
    }

    /**
     * How to correct for multiple tests.
     */
    public enum MultiTestCorrMethod {
        BONFERONNI, WESTFALLYOUNG, BENJAMINIHOCHBERG
    }

    /**
     * For the gene set resampling method, how are scores computed for the group?
     */
    public enum GeneScoreMethod {
        MEAN, MEAN_ABOVE_QUANTILE, QUANTILE
    }

    public static final String GENE_URL_BASE = "gene.url.base";

    /*
     * Strings used in the config file
     */
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
    private static final String MAX_CLASS_SIZE = "maxClassSize";
    private static final String MIN_CLASS_SIZE = "minClassSize";
    private static final String MTC_CONFIG_NAME = "mtc";
    private static final String USE_MULTIFUNCTIONALITY_CORRECTION = "multifuncCorr";
    private static final String OUTPUT_FILE = "outputFile";
    private static final String GENE_SCORE_THRESHOLD = "pValThreshold"; // TODO deprecate this string
    private static final String PREFERENCES_FILE_NAME = "preferencesFileName";
    private static final String QUANTILE_CONFIG_NAME = "quantile";
    private static final String RAW_FILE_CONFIG_NAME = "rawFile";
    private static final String GENE_SET_RESAMPLING_SCORE_METHOD = "rawScoreMethod";
    private static final String SCORE_COL = "scoreCol";
    private static final String SCORE_FILE = "scoreFile";
    private static final String SELECTED_CUSTOM_GENESETS = "selectedCustomGeneSets";
    private static final String FILTER_NONSPECIFIC = "filterNonSpecific";
    private static final String USE_BIOL_PROC = "useGOBiologicalProcess";
    private static final String USE_MOL_FUNC = "useGOMolecularFunction";
    private static final String USE_CELL_COMP = "useGOCellularComponent";
    /**
     * Settings that we need to write to analysis results files. Other settings are not needed there (like window sizes,
     * etc.)
     */
    protected static final String[] ANALYSIS_SETTINGS = new String[] { GENE_SCORE_THRESHOLD, QUANTILE_CONFIG_NAME,
            GENE_SET_RESAMPLING_SCORE_METHOD, MAX_CLASS_SIZE, MIN_CLASS_SIZE, RAW_FILE_CONFIG_NAME, SCORE_FILE, SCORE_COL,
            MTC_CONFIG_NAME, ITERATIONS, CLASS_FILE, BIG_IS_BETTER, DO_LOG, GENE_REP_TREATMENT, ALWAYS_USE_EMPIRICAL,
            ANNOT_FILE, ANNOT_FORMAT, CLASS_SCORE_METHOD, FILTER_NONSPECIFIC, USE_MULTIFUNCTIONALITY_CORRECTION,
            USE_MOL_FUNC, USE_BIOL_PROC, USE_CELL_COMP };

    /**
     * Part of the distribution, where defaults can be read from. If it is absent, hard-coded defaults are used.
     */
    private static final String USERGUI_DEFAULT_PROPERTIES = "ermineJdefault.properties";

    /**
     * Filename for settings.
     */
    private static final String USERGUI_PROPERTIES = "ermineJ.properties";

    /**
     * Header for the config file.
     */
    private static final String HEADER = "Configuration file for ermineJ."
            + "Do not delete this file if you want your ermineJ settings to stay across sessions.\nFor more information see http://www.chibi.ubc.ca/ermineJ/";

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
     * Create the settings, reading them from a file to be determined by the constructor.
     */
    public Settings() {
        this( true );
    }

    public Settings( boolean readFromFile ) {
        if ( readFromFile ) {
            initConfig();
        } else {
            this.config = new PropertiesConfiguration();
        }
    }

    /**
     * Creates settings object from a copy. Note that in this situation, autoSave is FALSE.
     * 
     * @param settings - settings object to copy
     */
    @SuppressWarnings("unchecked")
    public Settings( Settings settingsToCopy ) {
        this.config = new PropertiesConfiguration();
        PropertiesConfiguration oldConfig = settingsToCopy.getConfig();
        for ( Iterator<String> iter = oldConfig.getKeys(); iter.hasNext(); ) {
            String key = iter.next();
            Object value = oldConfig.getProperty( key );
            this.config.setProperty( key, value );
        }
    }

    /**
     * Create a Settings object from the header of a results file - autosave will not be set!
     * 
     * @param resultsFile
     */
    public Settings( String resultsFile ) throws ConfigurationException {
        this.config = new PropertiesConfiguration( resultsFile );
    }

    /**
     * Autosave will not be set.
     * 
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
        return config.getBoolean( ALWAYS_USE_EMPIRICAL, false );

    }

    public String getAnnotFile() {
        return config.getString( ANNOT_FILE );
    }

    /**
     * @return
     */
    public int getAnnotFormat() {
        return config.getInteger( ANNOT_FORMAT, GeneAnnotations.DEFAULT );
    }

    /**
     * @return
     */
    public boolean getBigIsBetter() {
        return config.getBoolean( BIG_IS_BETTER, false );
    }

    /**
     * Returns setting values.
     */
    public String getClassFile() {
        return config.getString( CLASS_FILE );
    }

    /**
     * @return
     */
    public Settings.Method getClassScoreMethod() {
        String storedValue = config.getString( CLASS_SCORE_METHOD, Settings.Method.ORA.toString() );

        // backwards compatibility
        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );
            switch ( oldVal ) {
                case 0:
                    setClassScoreMethod( Settings.Method.ORA );
                    break;
                case 1:
                    setClassScoreMethod( Settings.Method.GSR );
                    break;
                case 2:
                    setClassScoreMethod( Settings.Method.CORR );
                    break;
                case 3:
                    setClassScoreMethod( Settings.Method.ROC );
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            storedValue = config.getString( CLASS_SCORE_METHOD, Settings.Method.ORA.toString() );

        }

        return Settings.Method.valueOf( storedValue );
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

    public String getCustomGeneSetDirectory() {
        return ( String ) this.config.getProperty( "classFolder" );
    }

    public String getDataDirectory() {
        return config.getString( DATA_DIRECTORY );

    }

    /**
     * @return
     */
    public boolean getDoLog() {
        return config.getBoolean( DO_LOG, true );
    }

    /**
     * @return
     */
    public boolean getFilterNonSpecific() {
        return config.getBoolean( FILTER_NONSPECIFIC );
    }

    /**
     * @return
     */
    public MultiProbeHandling getGeneRepTreatment() {
        String storedValue = config.getString( GENE_REP_TREATMENT, MultiProbeHandling.BEST.toString() );

        // backwards compatibility
        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );
            switch ( oldVal ) {
                case 1:
                    setGeneRepTreatment( Settings.MultiProbeHandling.BEST );
                    break;
                case 2:
                    setGeneRepTreatment( Settings.MultiProbeHandling.MEAN );
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            storedValue = config.getString( GENE_REP_TREATMENT, MultiProbeHandling.BEST.toString() );
        }

        return Settings.MultiProbeHandling.valueOf( storedValue );
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

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getGoldStandardFile() {
        return config.getString( GOLD_STANDARD_FILE );
    }

    /**
     * @return
     */
    public int getIterations() {
        return config.getInteger( ITERATIONS, 10000 );
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
        return config.getInteger( MAX_CLASS_SIZE, 100 );
    }

    public int getMinClassSize() {
        return config.getInteger( MIN_CLASS_SIZE, 5 );
    }

    /**
     * @return Returns the mtc.
     */
    public MultiTestCorrMethod getMtc() {
        return MultiTestCorrMethod.valueOf( config.getString( MTC_CONFIG_NAME, MultiTestCorrMethod.BENJAMINIHOCHBERG
                .toString() ) );
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

    /**
     * Value used for ORA
     * 
     * @return
     */
    public double getGeneScoreThreshold() {
        return config.getDouble( GENE_SCORE_THRESHOLD, 0.001 );
    }

    public int getQuantile() {
        return config.getInteger( QUANTILE_CONFIG_NAME, 50 );
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

    public String getRawDataFileName() {
        return config.getString( RAW_FILE_CONFIG_NAME );
    }

    /**
     * @return the method to be used to combine scores. This is only relevant for the gene set resampling method.
     */
    public GeneScoreMethod getGeneSetResamplingScoreMethod() {
        String storedValue = config.getString( GENE_SET_RESAMPLING_SCORE_METHOD, GeneScoreMethod.MEAN.toString() );

        // backwards compatibility
        if ( NumberUtils.isDigits( storedValue ) ) {
            int oldVal = Integer.parseInt( storedValue );

            switch ( oldVal ) {
                case 0:
                    setGeneSetResamplingScoreMethod( Settings.GeneScoreMethod.MEAN );
                    break;
                case 1:
                    setGeneSetResamplingScoreMethod( Settings.GeneScoreMethod.QUANTILE );
                    break;
                case 2:
                    setGeneSetResamplingScoreMethod( Settings.GeneScoreMethod.MEAN_ABOVE_QUANTILE );
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            storedValue = config.getString( GENE_SET_RESAMPLING_SCORE_METHOD, GeneScoreMethod.MEAN.toString() );
        }

        return Settings.GeneScoreMethod.valueOf( storedValue );
    }

    public int getScoreCol() {
        return config.getInteger( SCORE_COL, 2 );
    }

    public String getScoreFile() {
        return config.getString( SCORE_FILE );
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getSelectedCustomGeneSets() {
        return config.getList( SELECTED_CUSTOM_GENESETS, null );
    }

    /**
     * @return Returns the useBiologicalProcess.
     */
    public boolean getUseBiologicalProcess() {
        return config.getBoolean( "useBiologicalProcess", true );

    }

    /**
     * @return Returns the useCellularComponent.
     */
    public boolean getUseCellularComponent() {
        return config.getBoolean( "useCellularComponent", true );

    }

    /**
     * @return
     */
    public boolean getUseLog() {
        return config.getBoolean( DO_LOG, true );
    }

    /**
     * @return Returns the useMolecularFunction.
     */
    public boolean getUseMolecularFunction() {
        return config.getBoolean( "useMolecularFunction", true );

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

    /**
     * @return true if multiple values for a gene should be combined, or whether each probe should be treated
     *         independently regardless
     * @see getGeneRepTreatment for setting of how the combination occurs.
     */
    public boolean getUseWeights() {
        if ( this.getGeneRepTreatment().equals( MultiProbeHandling.MEAN )
                || this.getGeneRepTreatment().equals( MultiProbeHandling.BEST ) ) return true;
        return false;
    }

    public boolean isTester() {
        return config.getBoolean( IS_TESTER, false );
    }

    /**
     * @return true if multifunctionality corrections should be applied, if possible.
     */
    public boolean isUseMultifunctionalityCorrection() {
        return config.getBoolean( USE_MULTIFUNCTIONALITY_CORRECTION, false );
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
        this.config.setProperty( "classFolder", val );
    }

    public void setDataDirectory( String val ) {
        this.config.setProperty( DATA_DIRECTORY, val );
    }

    public void setDirectories() throws IOException {
        createDataDirectory();
        createCustomGeneSetDirectory();
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

    /**
     * Only applies to ORA
     * 
     * @param val
     */
    public void setGeneScoreThreshold( double val ) {
        log.debug( "gene score threshold set to " + val );
        this.config.setProperty( GENE_SCORE_THRESHOLD, val );
    }

    public void setQuantile( int val ) {
        this.config.setProperty( QUANTILE_CONFIG_NAME, val );
    }

    public void setRawFile( String val ) {
        this.config.setProperty( RAW_FILE_CONFIG_NAME, val );
    }

    /**
     * Set the method used to compute how values are combined (GSR method only).
     * 
     * @param val
     */
    public void setGeneSetResamplingScoreMethod( Settings.GeneScoreMethod val ) {
        this.config.setProperty( GENE_SET_RESAMPLING_SCORE_METHOD, val.toString() );
    }

    public void setScoreCol( int val ) {
        log.debug( "Setting score columns to " + val );
        this.config.setProperty( SCORE_COL, val );
    }

    public void setScoreFile( String val ) {
        this.config.setProperty( SCORE_FILE, val );
    }

    public void setSelectedCustomGeneSets( Collection<String> selectedSets ) {
        this.config.setProperty( SELECTED_CUSTOM_GENESETS, selectedSets );
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

    public void setUseMultifunctionalityCorrection( boolean b ) {
        this.config.setProperty( USE_MULTIFUNCTIONALITY_CORRECTION, b );
    }

    /**
     * @param dir The dir to set.
     */
    public void setUserGeneSetDirectory( String dir ) {
        this.config.setProperty( "userGeneSetDirectory", dir );
    }

    @Override
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
        return this.getDoLog() && !this.getBigIsBetter() || !this.getDoLog() && this.getBigIsBetter();
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

    /**
     * 
     */
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
     * 
     */
    private void initConfig() {

        logLocale();

        try {
            URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
            if ( configFileLocation == null ) throw new ConfigurationException( "Doesn't exist" );

            this.config = new PropertiesConfiguration( configFileLocation );
            log.info( "Got configuration from " + configFileLocation );
        } catch ( ConfigurationException e ) {
            try {
                log.info( "User properties file doesn't exist, creating new one from defaults" );
                URL defaultConfigFileLocation = ConfigurationUtils.locate( USERGUI_DEFAULT_PROPERTIES );

                if ( defaultConfigFileLocation == null )
                    throw new ConfigurationException( "Defaults not found either!" );

                log.info( "Found defaults at " + defaultConfigFileLocation );
                this.config = new PropertiesConfiguration( USERGUI_DEFAULT_PROPERTIES );
                // File tempLocation = new File( config.getPath() ); // why are we doing this?
                // this.config.save(); // make sure the temporary file exists.
                File newConfigFile = new File( System.getProperty( "user.home" )
                        + System.getProperty( "file.separator" ) + USERGUI_PROPERTIES );

                this.config = new PropertiesConfiguration( USERGUI_PROPERTIES );
                this.config.setPath( newConfigFile.getAbsolutePath() );
                // this.config.save( newConfigFile ); // copy over to where they should be.
                // URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
                log.info( "Saved the new configuration in " + config.getPath() );
                // if ( !tempLocation.delete() ) {
                // log.error( "Could not delete temporary configuration file from " + tempLocation.getAbsolutePath()
                // + ", please delete it manually" );
                // log.error( tempLocation.getAbsoluteFile() + ": Exists=" + tempLocation.exists() );
                // log.error( tempLocation.getAbsoluteFile() + ": Can write=" + tempLocation.canWrite() );
                // } else {
                // log.debug( "Deleted temporary config file from " + tempLocation.getAbsolutePath() );
                // }

            } catch ( ConfigurationException e1 ) {
                log.error( "Filed to initialize the configuration file: " + e1, e1 );
            }
        }

        if ( this.config != null ) this.config.setHeader( HEADER );

        if ( this.config != null ) this.config.setAutoSave( true );
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
     * @return
     */
    private String userHomeDataDirectoryName() {
        String dataDirName = System.getProperty( "user.home" ) + System.getProperty( "file.separator" )
                + "ermineJ.data";
        return dataDirName;
    }

}