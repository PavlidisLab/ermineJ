package classScore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GeneAnnotations;
import baseCode.util.FileTools;

/**
 * <hr>
 * FIXME use commons configuration throughout (or as much as possible).
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Kiran Keshav
 * @author Homin Lee
 * @author Will Braynen
 * @version $Id$
 */

public class Settings {

    /*
     * Multiple test correction methods
     */
    public static final int WESTFALLYOUNG = 1;
    public static final int BENJAMINIHOCHBERG = 2;
    public static final int BONFERONNI = 0;

    public static final int CORR = 2;
    public static final int BEST_PVAL = 1;
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

    /**
     * 
     */

    private static final Log log = LogFactory.getLog( Settings.class );
    private static final String USERGUI_DEFAULT_PROPERTIES = "ermineJdefault.properties";
    private static final String USERGUI_PROPERTIES = "ermineJ.properties";
    private boolean alwaysUseEmpirical = false;
    private int analysisMethod = RESAMP;
    private String annotFile = "";
    private int annotFormat = GeneAnnotations.DEFAULT;
    private boolean bigIsBetter = false;
    private String classFile = "";
    private String classFolder = "";
    private PropertiesConfiguration config;
    private String dataDirectory = null;
    private boolean doLog = true;
    private int geneRepTreatment = BEST_PVAL;

    private String goldStandardFile = ""; // for testing;
    private boolean isTester = false; // set to true if this is running in the test framework.

    private int iterations = 10000;
    private int maxClassSize = 100;
    private int minClassSize = 8;

    private int mtc = BENJAMINIHOCHBERG; // multiple test correction
    private String outputFile = "";
    private String preferencesFileName = "";
    private Properties properties;
    private double pValThreshold = 0.001;
    private int quantile = 50;
    private String rawFile = "";
    private int rawScoreMethod = MEAN_METHOD;
    private int scorecol = 2;
    private String scoreFile = "";

    private boolean useBiologicalProcess = true;
    private boolean useCellularComponent = true;
    private boolean useMolecularFunction = true;

    public Settings() throws IOException {
        this( "" );
    }

    /**
     * Creates settings object
     * 
     * @param settings - settings object to copy
     */
    public Settings( Settings settings ) {
        initConfig();
        classFile = settings.getClassFile();
        annotFile = settings.getAnnotFile();
        rawFile = settings.getRawFile();
        goldStandardFile = settings.getGoldStandardFile();
        outputFile = settings.getOutputFile();
        dataDirectory = settings.getDataDirectory();
        classFolder = settings.getUserGeneSetDirectory();
        scoreFile = settings.getScoreFile();
        maxClassSize = settings.getMaxClassSize();
        minClassSize = settings.getMinClassSize();
        iterations = settings.getIterations();
        scorecol = settings.getScorecol();
        geneRepTreatment = settings.getGeneRepTreatment();
        rawScoreMethod = settings.getRawScoreMethod();
        analysisMethod = settings.getAnalysisMethod();
        quantile = settings.getQuantile();
        doLog = settings.getDoLog();
        pValThreshold = settings.getPValThreshold();
        alwaysUseEmpirical = settings.getAlwaysUseEmpirical();
        preferencesFileName = settings.getPrefFile();
        mtc = settings.getMtc();
        bigIsBetter = settings.getBigIsBetter();
        isTester = settings.isTester();
        annotFormat = settings.getAnnotFormat();
        properties = new Properties();

    }

    /**
     * @return
     */
    public int getAnnotFormat() {
        return this.annotFormat;
    }

    /**
     * Creates settings object
     * 
     * @param filename name of preferences file to read
     * @throws IOException
     */
    public Settings( String filename ) throws IOException {
        initConfig();
        properties = new Properties();
        if ( dataDirectory == null && !this.determineDataDirectory() ) {
            log.info( "Can't find data directory, using default settings" );
            return;
        }

        preferencesFileName = filename;
        createCustomGeneSetDirectory();

        // make a new file if it was empty.
        if ( preferencesFileName.compareTo( "" ) == 0 ) {
            preferencesFileName = dataDirectory + System.getProperty( "file.separator" ) + "ermineJ.preferences";
            log.debug( "Determined preferences file " + preferencesFileName );
        }

        // read the file if we can.
        File fi = new File( preferencesFileName );
        if ( fi.canRead() ) {
            try {
                log.info( "Reading settings from " + preferencesFileName );
                InputStream f = new FileInputStream( preferencesFileName );
                read( f );
            } catch ( IOException e ) {
                log.error( "Couldn't read from the file " + preferencesFileName );
            }
        }

    }

    /**
     * @throws IOException
     */
    private void createCustomGeneSetDirectory() throws IOException {
        classFolder = new String( dataDirectory + System.getProperty( "file.separator" ) + "genesets" );
        if ( !FileTools.testDir( classFolder ) ) {
            log.info( "Creating custom class folder at " + classFolder );
            if ( !new File( classFolder ).mkdir() ) {
                throw new IOException( "Could not create the class directory at " + classFolder );
            }
        }
        log.debug( "Custom gene sets directory is " + classFolder );
    }

    public Settings( URL resource ) {
        properties = new Properties();
        initConfig();
        File fi = new File( resource.getFile() );
        preferencesFileName = fi.getAbsolutePath();

        if ( fi.canRead() ) {
            try {
                InputStream f = new FileInputStream( preferencesFileName );
                read( f );
            } catch ( IOException e ) {
                log.error( "Couldn't read from the file " + preferencesFileName );
            }
        }
    }

    /**
     * Figure out where the data directory should go.
     * 
     * @return
     */
    public boolean determineDataDirectory() {
        dataDirectory = System.getProperty( "user.dir" );
        dataDirectory = dataDirectory
                .substring( 0, dataDirectory.lastIndexOf( System.getProperty( "file.separator" ) ) );

        dataDirectory = dataDirectory + System.getProperty( "file.separator" ) + "ermineJ.data";

        if ( !FileTools.testDir( dataDirectory ) ) {
            dataDirectory = System.getProperty( "user.home" ) + System.getProperty( "file.separator" ) + "ermineJ.data";

            if ( !FileTools.testDir( dataDirectory ) ) {
                log.info( "Creating data directory " + dataDirectory );
                // try to make it in the user's home directory.
                return ( new File( dataDirectory ) ).mkdir();
            }
        }
        log.info( "Data directory is " + dataDirectory );
        return true;
    }

    /**
     * @return
     */
    public boolean getAlwaysUseEmpirical() {
        return alwaysUseEmpirical;
    }

    public int getAnalysisMethod() {
        return analysisMethod;
    }

    public String getAnnotFile() {
        return annotFile;
    }

    /**
     * @return
     */
    public boolean getBigIsBetter() {
        return bigIsBetter;
    }

    /**
     * Returns setting values.
     */
    public String getClassFile() {
        return classFile;
    }

    public String getUserGeneSetDirectory() {
        return classFolder;
    }

    public int getClassScoreMethod() {
        return rawScoreMethod;

    }

    public String getClassScoreMethodString() {
        if ( rawScoreMethod == MEAN_METHOD ) {
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
        return dataDirectory;
    }

    public boolean getDoLog() {
        return doLog;
    }

    public int getGeneRepTreatment() {
        return geneRepTreatment;
    }

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getGoldStandardFile() {
        return goldStandardFile;
    }

    public int getGroupMethod() {
        return geneRepTreatment;
    }

    public String getGroupMethodString() {
        if ( geneRepTreatment == MEAN_PVAL )
            return "MEAN_PVAL";
        else if ( geneRepTreatment == BEST_PVAL )
            return "BEST_PVAL";
        else
            return "MEAN_PVAL"; // dummy. It won't be used.
    }

    public int getIterations() {
        return iterations;
    }

    public int getMaxClassSize() {
        return maxClassSize;
    }

    public int getMinClassSize() {
        return minClassSize;
    }

    /**
     * @return Returns the mtc.
     */
    public int getMtc() {
        return mtc;
    }

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getOutputFile() {
        return outputFile;
    }

    public String getPrefFile() {
        return preferencesFileName;
    }

    public double getPValThreshold() {
        return pValThreshold;
    }

    public int getQuantile() {
        return quantile;
    }

    public String getRawFile() {
        return rawFile;
    }

    public int getRawScoreMethod() {
        return rawScoreMethod;
    }

    public int getScorecol() {
        return scorecol;
    }

    public String getScoreFile() {
        return scoreFile;
    }

    /**
     * @return
     */
    public boolean getUseLog() {
        return doLog;
    }

    public boolean getUseWeights() {
        if ( geneRepTreatment == MEAN_PVAL || geneRepTreatment == BEST_PVAL ) return true;

        return false;
    }

    public boolean isTester() {
        return isTester;
    }

    /**
     * @param b
     */
    public void setAlwaysUseEmpirical( boolean b ) {
        alwaysUseEmpirical = b;
    }

    public void setAnalysisMethod( int val ) {
        analysisMethod = val;
    }

    public void setAnnotFile( String val ) {
        annotFile = val;
    }

    /**
     * @param arg
     */
    public void setAnnotFormat( String arg ) {
        if ( arg.equalsIgnoreCase( "affy" ) || arg.equalsIgnoreCase( "Affy CSV" ) ) { // fixme, this is hard to
            // maintain.
            this.annotFormat = GeneAnnotations.AFFYCSV;
        } else {
            this.annotFormat = GeneAnnotations.DEFAULT;
        }

    }

    /**
     * @param b
     */
    public void setBigIsBetter( boolean b ) {
        bigIsBetter = b;
    }

    /**
     * Sets setting values.
     */
    public void setClassFile( String val ) {
        classFile = val;
    }

    public void setClassFolder( String val ) {
        classFolder = val;
    }

    public void setDataDirectory( String val ) {
        dataDirectory = val;
    }

    public void setDoLog( boolean val ) {
        doLog = val;
    }

    public void setGeneRepTreatment( int val ) {
        geneRepTreatment = val;
    }

    /**
     * Mostly used for testing
     * 
     * @param goldStandardFile
     */
    public void setGoldStandardFile( String goldStandardFile ) {
        this.goldStandardFile = goldStandardFile;
    }

    public void setIterations( int val ) {
        iterations = val;
    }

    public void setMaxClassSize( int val ) {
        maxClassSize = val;
    }

    public void setMinClassSize( int val ) {
        minClassSize = val;
    }

    /**
     * @param mtc The mtc to set.
     */
    public void setMtc( int mtc ) {
        this.mtc = mtc;
    }

    /**
     * Mostly used for testing
     * 
     * @param outputFile
     */
    public void setOutputFile( String outputFile ) {
        this.outputFile = outputFile;
    }

    public void setPrefFile( String val ) {
        preferencesFileName = val;
    }

    public void setPValThreshold( double val ) {
        pValThreshold = val;
    }

    public void setQuantile( int val ) {
        quantile = val;
    }

    public void setRawFile( String val ) {
        rawFile = val;
    }

    public void setRawScoreMethod( int val ) {
        rawScoreMethod = val;
    }

    public void setScorecol( int val ) {
        scorecol = val;
    }

    public void setScoreFile( String val ) {
        scoreFile = val;
    }

    public void setTester( boolean isTester ) {
        this.isTester = isTester;
    }

    public String toString() {
        return properties.toString();
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
        return ( doLog && !bigIsBetter ) || ( !doLog && bigIsBetter );
    }

    public void writePrefs() throws IOException {
        this.writePrefs( preferencesFileName );
    }

    /**
     * Writes setting values to file.
     */
    public void writePrefs( String fileName ) throws IOException {
        // try {
        // this.config.save();
        // } catch ( ConfigurationException e ) {
        // e.printStackTrace();
        // }
        if ( fileName == null || fileName.length() == 0 ) {
            return;
        }
        properties.setProperty( "scoreFile", scoreFile );
        properties.setProperty( "classFile", classFile );
        properties.setProperty( "annotFile", annotFile );
        properties.setProperty( "rawFile", rawFile );
        properties.setProperty( "goldStandardFile", goldStandardFile );
        properties.setProperty( "outputFile", outputFile );
        properties.setProperty( "dataFolder", dataDirectory );
        properties.setProperty( "classFolder", classFolder );
        properties.setProperty( "maxClassSize", String.valueOf( maxClassSize ) );
        properties.setProperty( "minClassSize", String.valueOf( minClassSize ) );
        properties.setProperty( "iterations", String.valueOf( iterations ) );
        properties.setProperty( "scorecol", String.valueOf( scorecol ) );
        properties.setProperty( "geneRepTreatment", String.valueOf( geneRepTreatment ) );
        properties.setProperty( "rawScoreMethod", String.valueOf( rawScoreMethod ) );
        properties.setProperty( "mtc", String.valueOf( mtc ) );
        properties.setProperty( "analysisMethod", String.valueOf( analysisMethod ) );
        properties.setProperty( "quantile", String.valueOf( quantile ) );
        properties.setProperty( "doLog", String.valueOf( doLog ) );
        properties.setProperty( "bigIsBetter", String.valueOf( bigIsBetter ) );
        properties.setProperty( "pValThreshold", String.valueOf( pValThreshold ) );
        properties.setProperty( "useEmpirical", String.valueOf( alwaysUseEmpirical ) );
        properties.setProperty( "isTester", String.valueOf( isTester ) );
        properties.setProperty( "annotFormat", String.valueOf( annotFormat ) );
        OutputStream f = new FileOutputStream( fileName );
        properties.store( f, "" );
        f.close();
    }

    /**
     * 
     */
    private void initConfig() {
        try {
            URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
            if ( configFileLocation == null ) throw new ConfigurationException( "Doesn't exist" );

            this.config = new PropertiesConfiguration( configFileLocation );
            this.config.setAutoSave( true );
            log.debug( "Got configuration " + ConfigurationUtils.toString( this.config ) );
        } catch ( ConfigurationException e ) {

            try {
                log.info( "User properties file doesn't exist, creating new one from defaults" );
                URL defaultConfigFileLocation = ConfigurationUtils.locate( USERGUI_DEFAULT_PROPERTIES );

                if ( defaultConfigFileLocation == null )
                    throw new ConfigurationException( "Defaults not found either!" );

                log.info( "Found defaults at " + defaultConfigFileLocation );
                config = new PropertiesConfiguration( USERGUI_DEFAULT_PROPERTIES );
                config.save( USERGUI_PROPERTIES ); // copy over to where they should be.
                URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
                log.info( "Saved the new configuration in " + configFileLocation );

            } catch ( ConfigurationException e1 ) {
                e1.printStackTrace();
            }
        }
    }

    private void read( InputStream s ) {

        try {
            properties.load( s );
            s.close();

            if ( properties.containsKey( "isTester" ) )
                this.isTester = Boolean.valueOf( properties.getProperty( "isTester" ) ).booleanValue();

            if ( properties.containsKey( "scoreFile" ) ) {
                if ( isTester ) {
                    this.scoreFile = Settings.class.getResource( properties.getProperty( "scoreFile" ) ).getFile();
                } else {
                    this.scoreFile = properties.getProperty( "scoreFile" );
                }
            }

            if ( properties.containsKey( "classFile" ) ) {
                if ( isTester ) {
                    this.classFile = Settings.class.getResource( properties.getProperty( "classFile" ) ).getFile();
                } else {
                    this.classFile = properties.getProperty( "classFile" );
                }
            }

            if ( properties.containsKey( "annotFile" ) ) {
                if ( isTester ) {
                    this.annotFile = Settings.class.getResource( properties.getProperty( "annotFile" ) ).getFile();
                } else {
                    this.annotFile = properties.getProperty( "annotFile" );
                }
            }

            if ( properties.containsKey( "rawFile" ) ) {
                if ( isTester ) {
                    this.rawFile = Settings.class.getResource( properties.getProperty( "rawFile" ) ).getFile();
                } else {
                    this.rawFile = properties.getProperty( "rawFile" );
                }
            }

            if ( properties.containsKey( "goldStandardFile" ) ) {
                if ( isTester ) {
                    String goldStandardFileName = properties.getProperty( "goldStandardFile" );

                    String path = Settings.class.getResource( properties.getProperty( "rawFile" ) ).getPath();

                    path = path.substring( 0, path.lastIndexOf( "/data" ) );

                    this.goldStandardFile = path + goldStandardFileName;
                    System.err.println( "Gold standard is or will be in " + goldStandardFile );
                } else {
                    this.goldStandardFile = properties.getProperty( "goldStandardFile" );
                }
            }

            if ( properties.containsKey( "dataFolder" ) ) this.dataDirectory = properties.getProperty( "dataFolder" );

            if ( properties.containsKey( "classFolder" ) ) this.classFolder = properties.getProperty( "classFolder" );

            if ( properties.containsKey( "maxClassSize" ) )
                this.maxClassSize = Integer.valueOf( properties.getProperty( "maxClassSize" ) ).intValue();

            if ( properties.containsKey( "minClassSize" ) )
                this.minClassSize = Integer.valueOf( properties.getProperty( "minClassSize" ) ).intValue();

            if ( properties.containsKey( "iterations" ) )
                this.iterations = Integer.valueOf( properties.getProperty( "iterations" ) ).intValue();

            if ( properties.containsKey( "scorecol" ) )
                this.scorecol = Integer.valueOf( properties.getProperty( "scorecol" ) ).intValue();

            if ( properties.containsKey( "geneRepTreatment" ) )
                this.geneRepTreatment = Integer.valueOf( properties.getProperty( "geneRepTreatment" ) ).intValue();

            if ( properties.containsKey( "rawScoreMethod" ) )
                this.rawScoreMethod = Integer.valueOf( properties.getProperty( "rawScoreMethod" ) ).intValue();

            if ( properties.containsKey( "analysisMethod" ) )
                this.analysisMethod = Integer.valueOf( properties.getProperty( "analysisMethod" ) ).intValue();

            if ( properties.containsKey( "quantile" ) )
                this.quantile = Integer.valueOf( properties.getProperty( "quantile" ) ).intValue();

            if ( properties.containsKey( "doLog" ) )
                this.doLog = Boolean.valueOf( properties.getProperty( "doLog" ) ).booleanValue();

            if ( properties.containsKey( "pValThreshold" ) )
                this.pValThreshold = Double.valueOf( properties.getProperty( "pValThreshold" ) ).doubleValue();

            if ( properties.containsKey( "useEmpirical" ) )
                this.alwaysUseEmpirical = Boolean.valueOf( properties.getProperty( "useEmpirical" ) ).booleanValue();

            if ( properties.containsKey( "mtc" ) )
                this.mtc = Integer.valueOf( properties.getProperty( "mtc" ) ).intValue();

            if ( properties.containsKey( "bigIsBetter" ) )
                this.bigIsBetter = Boolean.valueOf( properties.getProperty( "bigIsBetter" ) ).booleanValue();

            if ( properties.containsKey( "annotFormat" ) )
                this.annotFormat = Integer.valueOf( properties.getProperty( "annotFormat" ) ).intValue();

            // System.err.println(this);
        } catch ( IOException ex ) {
            System.err.println( "Could not find preferences file. Will probably attempt to create a new one." );
        }
    }

    /**
     * @return Returns the useBiologicalProcess.
     */
    public boolean getUseBiologicalProcess() {
        return this.useBiologicalProcess;
    }

    /**
     * @param useBiologicalProcess The useBiologicalProcess to set.
     */
    public void setUseBiologicalProcess( boolean useBiologicalProcess ) {
        this.config.setProperty( "useBiologicalProcess", new Boolean( useBiologicalProcess ) );
        this.useBiologicalProcess = useBiologicalProcess;
    }

    /**
     * @return Returns the useCellularComponent.
     */
    public boolean getUseCellularComponent() {
        return this.useCellularComponent;
    }

    /**
     * @param useCellularComponent The useCellularComponent to set.
     */
    public void setUseCellularComponent( boolean useCellularComponent ) {
        this.config.setProperty( "useCellularComponent", new Boolean( useCellularComponent ) );
        this.useCellularComponent = useCellularComponent;
    }

    /**
     * @return Returns the useMolecularFunction.
     */
    public boolean getUseMolecularFunction() {
        return this.useMolecularFunction;
    }

    /**
     * @param useMolecularFunction The useMolecularFunction to set.
     */
    public void setUseMolecularFunction( boolean useMolecularFunction ) {
        this.config.setProperty( "useMolecularFunction", new Boolean( useMolecularFunction ) );
        this.useMolecularFunction = useMolecularFunction;
    }

}