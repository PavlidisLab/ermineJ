/*
 * The ermineJ project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;

import ubic.erminej.data.GeneAnnotationParser.Format;

/**
 * Holds settings for retrieval but doesn't allow changing them (with a couple of exceptions) or saving them. This is
 * basically a value object to use during analyses.
 *
 * @author paul
 * @version $Id: $Id
 */
public class SettingsHolder {

    /**
     * For the gene set resampling method, how are scores computed for the group?
     */
    public enum GeneScoreMethod {
        MEAN, MEAN_ABOVE_QUANTILE, PRECISIONRECALL, QUANTILE
    }

    /**
     * Which gene set scoring method to use. PREREC is a subtype of GSR.
     */
    public enum Method {
        CORR, GSR, ORA, ROC
    }

    /**
     * What to do when there are multiple values for a gene.
     */
    public enum MultiElementHandling {
        BEST, MEAN
    }

    /**
     * How to correct for multiple tests.
     */
    public enum MultiTestCorrMethod {
        FDR, FWE
    }

    /** Constant <code>defaults</code> */
    public static Map<String, Object> defaults = new HashMap<>();

    public static final String GEMMA_URL_BASE = "https://gemma.msl.ubc.ca/";

    /** Constant <code>GENE_URL_BASE="gene.url.base"</code> */
    public static final String GENE_URL_BASE = "gene.url.base";

    /*
     * Strings used in the config file
     */
    /** Constant <code>ALWAYS_USE_EMPIRICAL="alwaysUseEmpirical"</code> */
    protected static final String ALWAYS_USE_EMPIRICAL = "alwaysUseEmpirical";

    /** Constant <code>ANNOT_FILE="annotFile"</code> */
    protected static final String ANNOT_FILE = "annotFile";

    /** Constant <code>ANNOT_FORMAT="annotFormat"</code> */
    protected static final String ANNOT_FORMAT = "annotFormat";
    /** Constant <code>BIG_IS_BETTER="bigIsBetter"</code> */
    protected static final String BIG_IS_BETTER = "bigIsBetter";
    /** Constant <code>CLASS_FILE="classFile"</code> */
    protected static final String CLASS_FILE = "classFile";
    /** Constant <code>CLASS_SCORE_METHOD="classScoreMethod"</code> */
    protected static final String CLASS_SCORE_METHOD = "classScoreMethod";
    /** Constant <code>CUSTOM_GENE_SET_DIRECTORY_PROPERTY="classFolder"</code> */
    protected static final String CUSTOM_GENE_SET_DIRECTORY_PROPERTY = "classFolder";
    /** Constant <code>CUSTOM_GENESET_FILES="customGeneSetFiles"</code> */
    protected static final String CUSTOM_GENESET_FILES = "customGeneSetFiles";
    /** Constant <code>DATA_COL="dataCol"</code> */
    protected static final String DATA_COL = "dataCol"; // in data matrix, where the first data are.
    /** Constant <code>DATA_DIRECTORY="dataDirectory"</code> */
    protected static final String DATA_DIRECTORY = "dataDirectory";
    /** Constant <code>DEFAULT_CUSTOM_GENE_SET_DIR_NAME="genesets"</code> */
    protected static final String DEFAULT_CUSTOM_GENE_SET_DIR_NAME = "genesets";
    /** Constant <code>DEFAULT_USER_DATA_DIR_NAME="ermineJ.data"</code> */
    protected static final String DEFAULT_USER_DATA_DIR_NAME = "ermineJ.data";
    /** Constant <code>DO_LOG="doLog"</code> */
    protected static final String DO_LOG = "doLog";

    /** Constant <code>GENE_REP_TREATMENT="geneRepTreatment"</code> */
    protected static final String GENE_REP_TREATMENT = "geneRepTreatment";
    /** Constant <code>GENE_SCORE_THRESHOLD_KEY="scoreThreshold"</code> */
    protected static final String GENE_SCORE_THRESHOLD_KEY = "scoreThreshold";
    /** Constant <code>GENE_SCORE_THRESHOLD_LEGACY_KEY="pValThreshold"</code> */
    protected static final String GENE_SCORE_THRESHOLD_LEGACY_KEY = "pValThreshold";
    /** Constant <code>GENE_SET_RESAMPLING_SCORE_METHOD="rawScoreMethod"</code> */
    protected static final String GENE_SET_RESAMPLING_SCORE_METHOD = "rawScoreMethod";
    /** Constant <code>GOLD_STANDARD_FILE="goldStandardFile"</code> */
    protected static final String GOLD_STANDARD_FILE = "goldStandardFile";
    /** Constant <code>IS_TESTER="isTester"</code> */
    protected static final String IS_TESTER = "isTester";
    /** Constant <code>ITERATIONS="iterations"</code> */
    protected static final String ITERATIONS = "iterations";
    /** Constant <code>LOAD_USER_DEFINED_GENE_GROUPS="loadUserDefinedGeneGroups"</code> */
    protected static final String LOAD_USER_DEFINED_GENE_GROUPS = "loadUserDefinedGeneGroups";
    /** Constant <code>LOG_FILE="logFile"</code> */
    protected static final String LOG_FILE = "logFile";

    /** Constant <code>MAX_CLASS_SIZE="maxClassSize"</code> */
    protected static final String MAX_CLASS_SIZE = "maxClassSize";
    /** Constant <code>MIN_CLASS_SIZE="minClassSize"</code> */
    protected static final String MIN_CLASS_SIZE = "minClassSize";
    /** Constant <code>MTC_CONFIG_NAME="mtc"</code> */
    protected static final String MTC_CONFIG_NAME = "mtc";
    /** Constant <code>OUTPUT_FILE="outputFile"</code> */
    protected static final String OUTPUT_FILE = "outputFile";
    /** Constant <code>PREFERENCES_FILE_NAME="preferencesFileName"</code> */
    protected static final String PREFERENCES_FILE_NAME = "preferencesFileName";
    /** Constant <code>QUANTILE_CONFIG_NAME="quantile"</code> */
    protected static final String QUANTILE_CONFIG_NAME = "quantile";
    /** Constant <code>RAW_FILE_CONFIG_NAME="rawFile"</code> */
    protected static final String RAW_FILE_CONFIG_NAME = "rawFile";
    /** Constant <code>SAVE_ALL_GENES_IN_OUTPUT="saveAllGenesInOutput"</code> */
    protected static final String SAVE_ALL_GENES_IN_OUTPUT = "saveAllGenesInOutput";
    /** Constant <code>SEED="seed"</code> */
    protected static final String SEED = "seed";
    /** Constant <code>SCORE_COL="scoreCol"</code> */
    protected static final String SCORE_COL = "scoreCol";
    /** Constant <code>SCORE_FILE="scoreFile"</code> */
    protected static final String SCORE_FILE = "scoreFile";
    /** Constant <code>SELECTED_CUSTOM_GENESETS="selectedCustomGeneSets"</code> */
    protected static final String SELECTED_CUSTOM_GENESETS = "selectedCustomGeneSets";
    /** Constant <code>USE_BIOL_PROC="useGOBiologicalProcess"</code> */
    protected static final String USE_BIOL_PROC = "useGOBiologicalProcess";
    /** Constant <code>USE_CELL_COMP="useGOCellularComponent"</code> */
    protected static final String USE_CELL_COMP = "useGOCellularComponent";
    /** Constant <code>USE_MOL_FUNC="useGOMolecularFunction"</code> */
    protected static final String USE_MOL_FUNC = "useGOMolecularFunction";

    /** Constant <code>USE_USER_DEFINED_GROUPS="useUserDefinedGroups"</code> */
    protected static final String USE_USER_DEFINED_GROUPS = "useUserDefinedGroups";
    /** Constant <code>VERSIONPARAM="softwareVersion"</code> */
    protected static final String VERSIONPARAM = "softwareVersion";
    // note this is also listed in erminejdefault.properties.
    private static final String DEFAULT_GENE_URL_BASE = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=search&term=@@";

    private static final String ERMINE_J_LOG_FILE_NAME = "ermineJ.log";

    /** Constant <code>FILTER_NONSPECIFIC="filterNonSpecific"</code> */
    @Deprecated
    protected static final String FILTER_NONSPECIFIC = "filterNonSpecific";

    /** Constant <code>USE_MULTIFUNCTIONALITY_CORRECTION="multifuncCorr"</code> */
    @Deprecated
    protected static final String USE_MULTIFUNCTIONALITY_CORRECTION = "multifuncCorr";

    /**
     * Hard-coded in case of a failure to retrieve the actual version.
     */
    private static final String FALLBACK_VERSION = "3.0";

    private static String version = FALLBACK_VERSION;

    /*
     * Define default values.
     */
    static {
        defaults.put( QUANTILE_CONFIG_NAME, 50 );
        defaults.put( MIN_CLASS_SIZE, 20 );
        defaults.put( MAX_CLASS_SIZE, 200 );
        defaults.put( ITERATIONS, 1000 );
        defaults.put( GENE_SCORE_THRESHOLD_KEY, 0.001 );
        defaults.put( GENE_SET_RESAMPLING_SCORE_METHOD, GeneScoreMethod.MEAN.toString() );
        defaults.put( SettingsHolder.GENE_URL_BASE, DEFAULT_GENE_URL_BASE );
        defaults.put( GENE_REP_TREATMENT, MultiElementHandling.MEAN.toString() );
        defaults.put( FILTER_NONSPECIFIC, Boolean.TRUE );
        defaults.put( DO_LOG, Boolean.TRUE );
        defaults.put( CLASS_SCORE_METHOD, Settings.Method.ORA.toString() );
        defaults.put( DATA_DIRECTORY, getDefaultUserDataDirPath() );
        defaults.put( CUSTOM_GENE_SET_DIRECTORY_PROPERTY, getDefaultUserClassesDirPath() );
        defaults.put( ANNOT_FORMAT, Format.DEFAULT.toString() );
        defaults.put( BIG_IS_BETTER, Boolean.FALSE );
        defaults.put( USE_USER_DEFINED_GROUPS, Boolean.TRUE );
        defaults.put( USE_MULTIFUNCTIONALITY_CORRECTION, Boolean.TRUE );
        defaults.put( SAVE_ALL_GENES_IN_OUTPUT, Boolean.FALSE );
        defaults.put( MTC_CONFIG_NAME, MultiTestCorrMethod.FDR );
        defaults.put( USE_MOL_FUNC, Boolean.TRUE );
        defaults.put( USE_CELL_COMP, Boolean.TRUE );
        defaults.put( USE_BIOL_PROC, Boolean.TRUE );
        defaults.put( SCORE_COL, 2 );
        defaults.put( DATA_COL, 2 );
        defaults.put( VERSIONPARAM, version );
        defaults.put( LOG_FILE, defaults.get( DATA_DIRECTORY ) + File.separator + ERMINE_J_LOG_FILE_NAME );

    }

    /**
     * <p>
     * getDefault.
     * </p>
     *
     * @param key a {@link java.lang.String} object.
     * @return the default setting for the key, or null if there is no such setting.
     */
    public static Object getDefault( String key ) {
        if ( !defaults.containsKey( key ) ) {
            return "";
        }
        return defaults.get( key );
    }

    /**
     * <p>
     * Getter for the field <code>version</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getVersion() {
        return version;
    }

    /**
     * <p>
     * getDefaultUserClassesDirPath.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected static String getDefaultUserClassesDirPath() {
        return getDefaultUserDataDirPath() + File.separator + DEFAULT_CUSTOM_GENE_SET_DIR_NAME;
    }

    /**
     * <p>
     * getDefaultUserDataDirPath.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected static String getDefaultUserDataDirPath() {
        String dataDirName = System.getProperty( "user.home" ) + File.separator + DEFAULT_USER_DATA_DIR_NAME;
        return dataDirName;
    }

    protected PropertiesConfiguration config = null;

    protected FileBasedConfigurationBuilder<PropertiesConfiguration> configBuilder = null;

    /**
     * <p>
     * Constructor for SettingsHolder.
     * </p>
     *
     * @param oldConfig a {@link org.apache.commons.configuration.PropertiesConfiguration} object.
     */
    public SettingsHolder( PropertiesConfiguration oldConfig ) {
        this();
        this.configBuilder = new FileBasedConfigurationBuilder<>( PropertiesConfiguration.class );

        try {
            this.config = configBuilder.getConfiguration();
        } catch ( ConfigurationException e ) {
            throw new RuntimeException( e );
        }

        /*
         * Copy configuration over.
         */
        configBuilder.setParameters( new HashMap<String, Object>() );
        for ( Iterator<String> iter = oldConfig.getKeys(); iter.hasNext(); ) {
            String key = iter.next();
            Object value = oldConfig.getProperty( key );
            this.config.setProperty( key, value );
        }
    }

    /**
     * <p>
     * Constructor for SettingsHolder.
     * </p>
     */
    protected SettingsHolder() {

        try {
            InputStream resourceAsStream = getClass().getResourceAsStream( "/ubic/erminej/version" );
            BufferedReader r = new BufferedReader( new InputStreamReader( resourceAsStream ) );
            String v = r.readLine();
            r.close();
            version = v;
        } catch ( Exception e ) {
            // no big deal.
        }
    }

    /**
     * <p>
     * getAlwaysUseEmpirical.
     * </p>
     *
     * @return a boolean.
     */
    public boolean getAlwaysUseEmpirical() {
        return config.getBoolean( ALWAYS_USE_EMPIRICAL, false );

    }

    /**
     * <p>
     * getAnnotFile.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAnnotFile() {
        return config.getString( ANNOT_FILE );
    }

    /**
     * <p>
     * getAnnotFormat.
     * </p>
     *
     * @return a {@link ubic.erminej.data.GeneAnnotationParser.Format} object.
     */
    public Format getAnnotFormat() {
        String storedValue = config.getString( ANNOT_FORMAT, Format.DEFAULT.toString() );

        return Format.valueOf( storedValue );
    }

    /**
     * <p>
     * getBigIsBetter.
     * </p>
     *
     * @return a boolean.
     */
    public boolean getBigIsBetter() {
        return config.getBoolean( BIG_IS_BETTER, false );
    }

    /**
     * Returns setting values.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassFile() {
        assert config != null;
        return config.getString( CLASS_FILE );
    }

    /**
     * <p>
     * getClassScoreMethod.
     * </p>
     *
     * @return a Settings.Method object.
     */
    public Settings.Method getClassScoreMethod() {
        String storedValue = config.getString( CLASS_SCORE_METHOD, Settings.Method.ORA.toString() );

        return Settings.Method.valueOf( storedValue );
    }

    /**
     * A human-readable version of the name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassScoreMethodName() {
        if ( this.getClassScoreMethod().equals( Method.GSR )
                && this.getGeneSetResamplingScoreMethod().equals( GeneScoreMethod.PRECISIONRECALL ) ) {
            return "PRERE";
        }
        return this.getClassScoreMethod().toString();
    }

    /**
     * <p>
     * getCustomGeneSetDirectory.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCustomGeneSetDirectory() {
        return config.getString( CUSTOM_GENE_SET_DIRECTORY_PROPERTY, getDefaultUserClassesDirPath() );
    }

    /**
     * <p>
     * getCustomGeneSetFiles.
     * </p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getCustomGeneSetFiles() {
        return new HashSet<>( Arrays.asList( config.getStringArray( CUSTOM_GENESET_FILES ) ) );
    }

    /**
     * The first column in the data file that has data in it. This is numbered "naturally" so that 2 means the second
     * column of the file.
     *
     * @return a int.
     */
    public int getDataCol() {
        return config.getInteger( DATA_COL, ( Integer ) getDefault( DATA_COL ) );
    }

    /**
     * <p>
     * getDataDirectory.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDataDirectory() {
        return config.getString( DATA_DIRECTORY, getDefaultUserDataDirPath() );

    }

    /**
     * <p>
     * getDefaultGeneUrl.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultGeneUrl() {
        return DEFAULT_GENE_URL_BASE;
    }

    /**
     * <p>
     * getDoLog.
     * </p>
     *
     * @return a boolean.
     */
    public boolean getDoLog() {
        return config.getBoolean( DO_LOG, ( Boolean ) getDefault( DO_LOG ) );
    }

    /**
     * <p>
     * getGeneRepTreatment.
     * </p>
     *
     * @return a {@link ubic.erminej.SettingsHolder.MultiElementHandling} object.
     */
    public MultiElementHandling getGeneRepTreatment() {
        String storedValue = config.getString( GENE_REP_TREATMENT, MultiElementHandling.MEAN.toString() );
        return Settings.MultiElementHandling.valueOf( storedValue );
    }

    /**
     * <p>
     * getGeneScoreFileDirectory.
     * </p>
     *
     * @return the path to the last directory used for gene scores
     */
    public String getGeneScoreFileDirectory() {
        String gsf = this.getScoreFile();
        if ( gsf == null ) return getDataDirectory();

        File gsfFile = new File( gsf );
        return gsfFile.getParent() == null ? getDataDirectory() : gsfFile.getParent();
    }

    /**
     * Value used for ORA
     *
     * @return a double.
     */
    public double getGeneScoreThreshold() {
        return config.getDouble( GENE_SCORE_THRESHOLD_KEY, ( Double ) getDefault( GENE_SCORE_THRESHOLD_KEY ) );
    }

    /**
     * <p>
     * getGeneSetResamplingScoreMethod.
     * </p>
     *
     * @return the method to be used to combine scores. This is only relevant for the gene set resampling method.
     */
    public GeneScoreMethod getGeneSetResamplingScoreMethod() {
        String storedValue = config.getString( GENE_SET_RESAMPLING_SCORE_METHOD, GeneScoreMethod.MEAN.toString() );

        return Settings.GeneScoreMethod.valueOf( storedValue );
    }

    /**
     * <p>
     * getGeneUrlBase.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGeneUrlBase() {
        return config.getString( SettingsHolder.GENE_URL_BASE, DEFAULT_GENE_URL_BASE );
    }

    /**
     * Mostly used for testing
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGoldStandardFile() {
        return config.getString( GOLD_STANDARD_FILE );
    }

    /**
     * <p>
     * getIterations.
     * </p>
     *
     * @return a int.
     */
    public int getIterations() {
        return config.getInteger( ITERATIONS, 10000 );
    }

    /**
     * <p>
     * getLogFile.
     * </p>
     *
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File getLogFile() throws IOException {
        File f = new File( config.getString( LOG_FILE, ( String ) getDefault( LOG_FILE ) ) );
        if ( !f.getParentFile().isDirectory() || !f.getParentFile().canWrite() ) {

            f = File.createTempFile( "ermineJ", "log" );
            System.err.println( "Cannot write to log file: " + f.getAbsolutePath()
                    + ", logging will be to generic temporary file: " + f.getAbsolutePath() );

        }
        return f;
    }

    /**
     * <p>
     * getMaxClassSize.
     * </p>
     *
     * @return a int.
     */
    public int getMaxClassSize() {
        return config.getInteger( MAX_CLASS_SIZE, 100 );
    }

    /**
     * <p>
     * getMinClassSize.
     * </p>
     *
     * @return a int.
     */
    public int getMinClassSize() {
        return config.getInteger( MIN_CLASS_SIZE, 5 );
    }

    /**
     * <p>
     * getMtc.
     * </p>
     *
     * @return Returns the mtc.
     */
    public MultiTestCorrMethod getMtc() {
        return MultiTestCorrMethod.valueOf( config.getString( MTC_CONFIG_NAME,
                MultiTestCorrMethod.FDR.toString() ) );
    }

    /**
     * <p>
     * getQuantile.
     * </p>
     *
     * @return a int.
     */
    public int getQuantile() {
        return config.getInteger( QUANTILE_CONFIG_NAME, 50 );
    }

    /**
     * <p>
     * getRawDataFileDirectory.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRawDataFileDirectory() {
        String rdf = this.getRawDataFileName();
        if ( rdf == null ) return getDataDirectory();

        File rdfFile = new File( rdf );
        return rdfFile.getParent() == null ? getDataDirectory() : rdfFile.getParent();
    }

    /**
     * <p>
     * getRawDataFileName.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRawDataFileName() {
        return config.getString( RAW_FILE_CONFIG_NAME );
    }

    /**
     * <p>
     * getSaveAllGenesInOutput.
     * </p>
     *
     * @return true if outputs should include all genes genes in each gene set.
     */
    public boolean getSaveAllGenesInOutput() {
        return config.getBoolean( SAVE_ALL_GENES_IN_OUTPUT, false );
    }

    /**
     * <p>
     * getScoreCol.
     * </p>
     *
     * @return a int.
     */
    public int getScoreCol() {
        return config.getInteger( SCORE_COL, ( Integer ) getDefault( SCORE_COL ) );
    }

    /**
     * <p>
     * getScoreFile.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getScoreFile() {
        return config.getString( SCORE_FILE );
    }

    /**
     * get the random seed if it was set.
     * 
     * @return seed or null if it wasn't set.
     */
    public Long getSeed() {
        if ( config.containsKey( SEED ) ) {
            return config.getLong( SEED );
        }
        return null;
    }

    /**
     * <p>
     * getSelectedCustomGeneSets.
     * </p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getSelectedCustomGeneSets() {
        return new HashSet<>( Arrays.asList( config.getStringArray( SELECTED_CUSTOM_GENESETS ) ) );
    }

    /**
     * <p>
     * getStringProperty.
     * </p>
     *
     * @param propertyName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getStringProperty( String propertyName ) {
        Object prop = this.config.getProperty( propertyName );
        if ( prop == null ) return null;
        return prop.toString();
    }

    /**
     * <p>
     * getUseBiologicalProcess.
     * </p>
     *
     * @return Returns the useBiologicalProcess.
     */
    public boolean getUseBiologicalProcess() {
        return config.getBoolean( USE_BIOL_PROC, ( Boolean ) getDefault( USE_BIOL_PROC ) );

    }

    /**
     * <p>
     * getUseCellularComponent.
     * </p>
     *
     * @return Returns the useCellularComponent.
     */
    public boolean getUseCellularComponent() {
        return config.getBoolean( USE_CELL_COMP, ( Boolean ) getDefault( USE_CELL_COMP ) );

    }

    /**
     * <p>
     * getUseLog.
     * </p>
     *
     * @return a boolean.
     */
    public boolean getUseLog() {
        return config.getBoolean( DO_LOG, true );
    }

    // /**
    // * @return true if multiple values for a gene should be combined, or whether each probe should be treated
    // * independently regardless; basically this is always going to be true.
    // * @see getGeneRepTreatment for setting of how the combination occurs.
    // */
    // public boolean getUseWeights() {
    // if ( this.getGeneRepTreatment().equals( MultiProbeHandling.MEAN )
    // || this.getGeneRepTreatment().equals( MultiProbeHandling.BEST ) ) return true;
    // return false;
    // }

    /**
     * <p>
     * getUseMolecularFunction.
     * </p>
     *
     * @return Returns the useMolecularFunction.
     */
    public boolean getUseMolecularFunction() {
        return config.getBoolean( USE_MOL_FUNC, ( Boolean ) getDefault( USE_MOL_FUNC ) );

    }

    /**
     * Get the path to the directory where custom gene sets are stored (default should be like
     * ${HOME}/ermineJ.data/genesets). This path is not guaranteed to exist.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUserGeneSetDirectory() {
        String dir = config.getString( CUSTOM_GENE_SET_DIRECTORY_PROPERTY );
        return dir;
    }

    /**
     * <p>
     * getUseUserDefined.
     * </p>
     *
     * @return a boolean.
     */
    public boolean getUseUserDefined() {
        return config.getBoolean( USE_USER_DEFINED_GROUPS, ( Boolean ) getDefault( USE_USER_DEFINED_GROUPS ) );
    }

    /**
     * <p>
     * isTester.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isTester() {
        return config.getBoolean( IS_TESTER, false );
    }

    // primarily for testing.
    /**
     * <p>
     * loadUserDefined.
     * </p>
     *
     * @return a boolean.
     */
    public boolean loadUserDefined() {
        return this.config.getBoolean( LOAD_USER_DEFINED_GENE_GROUPS, true );
    }

    /**
     * <p>
     * setCustomGeneSetFiles.
     * </p>
     *
     * @param filePaths a {@link java.util.Collection} object.
     */
    public void setCustomGeneSetFiles( Collection<String> filePaths ) {
        this.config.setProperty( CUSTOM_GENESET_FILES, filePaths );
    }

    /**
     * <p>
     * setLoadUserDefined.
     * </p>
     *
     * @param b a boolean.
     */
    public void setLoadUserDefined( boolean b ) {
        this.config.setProperty( LOAD_USER_DEFINED_GENE_GROUPS, b );
    }

    /**
     * <p>
     * setRawFile.
     * </p>
     *
     * @param val a {@link java.lang.String} object.
     */
    public void setRawFile( String val ) {
        this.config.setProperty( RAW_FILE_CONFIG_NAME, val );
    }

    /**
     * <p>
     * setScoreFile.
     * </p>
     *
     * @param val a {@link java.lang.String} object.
     */
    public void setScoreFile( String val ) {
        this.config.setProperty( SCORE_FILE, val );
    }

    /**
     * <p>
     * setUseUserDefined.
     * </p>
     *
     * @param b a boolean.
     */
    public void setUseUserDefined( boolean b ) {
        this.config.setProperty( USE_USER_DEFINED_GROUPS, b );
        if ( b ) {
            this.config.setProperty( LOAD_USER_DEFINED_GENE_GROUPS, true );
        }
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

    //    /**
    //     * <p>
    //     * useMultifunctionalityCorrection.
    //     * </p>
    //     *
    //     * @return true if multifunctionality corrections should be applied, if possible.
    //     * @deprecated this should always be true
    //     */
    //    public boolean useMultifunctionalityCorrection() {
    //        /*
    //         * Note that the intention is that this always be true. The reason to turn it off would be for testing or
    //         * performance reasons.
    //         */
    //        return config.getBoolean( USE_MULTIFUNCTIONALITY_CORRECTION, true );
    //    }

    /**
     * <p>
     * Getter for the field <code>config</code>.
     * </p>
     *
     * @return a {@link org.apache.commons.configuration.PropertiesConfiguration} object.
     */
    protected PropertiesConfiguration getConfig() {
        return config;
    }

    /**
     * <p>
     * getProperty.
     * </p>
     *
     * @param propertyName a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    protected Object getProperty( String propertyName ) {
        return this.config.getProperty( propertyName );
    }

}
