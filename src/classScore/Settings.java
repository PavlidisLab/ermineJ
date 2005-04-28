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
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.util.FileTools;

/**
 * <hr>
 * FIXME use commons configuration throughout.
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Kiran Keshav
 * @author Homin Lee
 * @author Will Braynen
 * @version $Id$
 */

public class Settings {

    /**
     * 
     */

    private static final Log log = LogFactory.getLog( Settings.class );
    private static final String USERGUI_PROPERTIES = "ermineJ.properties";
    private static final String USERGUI_DEFAULT_PROPERTIES = "ermineJdefault.properties";
    private PropertiesConfiguration config;

    private Properties properties;
    private String pref_file = "";
    private String classFile = "";
    private String annotFile = "";
    private String rawFile = "";
    private String dataFolder = "";
    private String classFolder = "";
    private String scoreFile = "";
    private String outputFile = "";
    private String goldStandardFile = ""; // for testing;

    private int maxClassSize = 100;
    private int minClassSize = 8;
    private int iterations = 10000;
    private int scorecol = 2;
    private int geneRepTreatment = BEST_PVAL;
    private int rawScoreMethod = MEAN_METHOD;
    private int analysisMethod = RESAMP;
    private int quantile = 50;
    private int mtc = BENJAMINIHOCHBERG; // multiple test correction
    private boolean doLog = true;
    private double pValThreshold = 0.001;
    private boolean alwaysUseEmpirical = false;
    private boolean bigIsBetter = false;
    private boolean isTester = false; // set to true if this is running in the test framework.

    public static final int BEST_PVAL = 1;
    public static final int MEAN_PVAL = 2;

    public static final int MEAN_METHOD = 0;
    public static final int QUANTILE_METHOD = 1;
    public static final int MEAN_ABOVE_QUANTILE_METHOD = 2;

    public static final int ORA = 0;
    public static final int RESAMP = 1;
    public static final int CORR = 2;
    public static final int ROC = 3;
    public static final int TTEST = 4;
    public static final int KS = 5;
    public static final String GENE_URL_BASE = "gene.url.base";
    public static final int BONFERONNI = 0;
    public static final int WESTFALLYOUNG = 1;
    public static final int BENJAMINIHOCHBERG = 2;

    public Settings() throws IOException {
        this( "" );
    }

    public Settings( URL resource ) {
        properties = new Properties();
        initConfig();
        File fi = new File( resource.getFile() );
        pref_file = fi.getAbsolutePath();

        if ( fi.canRead() ) {
            try {
                InputStream f = new FileInputStream( pref_file );
                read( f );
            } catch ( IOException e ) {
                System.err.println( "Couldn't read from the file" );
                System.exit( 0 );
            }
        }
    }

    /**
     * 
     */
    private void initConfig() {
        try {
            URL configFileLocation = ConfigurationUtils.locate( USERGUI_PROPERTIES );
            if ( configFileLocation == null ) throw new ConfigurationException( "Doesn't exist" );

            this.config = new PropertiesConfiguration( configFileLocation );
            this.config.setAutoSave(true);
            log.debug( "Got configuration " + ConfigurationUtils.toString( this.config ) );
        } catch ( ConfigurationException e ) {

            try {
                log.warn( "User properties file doesn't exist, creating new one from defaults" );
                config = new PropertiesConfiguration( USERGUI_DEFAULT_PROPERTIES );
              //  config.save( USERGUI_PROPERTIES );
            } catch ( ConfigurationException e1 ) {
                e1.printStackTrace();
            }
        }
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

    /**
     * Creates settings object
     * 
     * @param filename name of preferences file to read
     * @throws IOException
     */
    public Settings( String filename ) throws IOException {
        initConfig();
        properties = new Properties();
        if ( dataFolder == null && !this.determineDataDirectory() ) {
            return;
        }

        pref_file = filename;
        classFolder = new String( dataFolder + System.getProperty( "file.separator" ) + "genesets" );

        if ( !FileTools.testDir( classFolder ) ) {
            if ( !new File( classFolder ).mkdir() ) {
                throw new IOException( "Could not create the class directory at " + classFolder );
            }
        }

        // make a new file if it was empty.
        if ( pref_file.compareTo( "" ) == 0 )
            pref_file = dataFolder + System.getProperty( "file.separator" ) + "ClassScore.preferences";

        // read the file if we can.
        File fi = new File( pref_file );
        if ( fi.canRead() ) {
            try {
                InputStream f = new FileInputStream( pref_file );
                read( f );
            } catch ( IOException e ) {
                System.err.println( "Couldn't read from the file" );
            }
        }

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
        dataFolder = settings.getDataFolder();
        classFolder = settings.getClassFolder();
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
        pref_file = settings.getPrefFile();
        mtc = settings.getMtc();
        bigIsBetter = settings.getBigIsBetter();
        isTester = settings.isTester();
        properties = new Properties();
    }

    public void writePrefs() throws IOException {
        this.writePrefs( pref_file );
    }

    /**
     * Writes setting values to file.
     */
    public void writePrefs( String fileName ) throws IOException {
//        try {
//            this.config.save();
//        } catch ( ConfigurationException e ) {
//            e.printStackTrace();
//        }
        if ( fileName == null || fileName.length() == 0 ) {
            return;
        }
        properties.setProperty( "scoreFile", scoreFile );
        properties.setProperty( "classFile", classFile );
        properties.setProperty( "annotFile", annotFile );
        properties.setProperty( "rawFile", rawFile );
        properties.setProperty( "goldStandardFile", goldStandardFile );
        properties.setProperty( "outputFile", outputFile );
        properties.setProperty( "dataFolder", dataFolder );
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
        OutputStream f = new FileOutputStream( fileName );
        properties.store( f, "" );
        f.close();
    }

    public String toString() {
        return properties.toString();
    }

    /**
     * Figure out where the data directory should go.
     * 
     * @return
     */
    public boolean determineDataDirectory() {
        dataFolder = System.getProperty( "user.dir" ); // directory from which we are running the software. This is not
        // platform independent so we fall back on the user home directory.

        dataFolder = dataFolder.substring( 0, dataFolder.lastIndexOf( System.getProperty( "file.separator" ) ) ); // up
        // one
        // level.

        dataFolder = dataFolder + System.getProperty( "file.separator" ) + "ermineJ.data";

        if ( !FileTools.testDir( dataFolder ) ) {
            dataFolder = System.getProperty( "user.home" ) + System.getProperty( "file.separator" ) + "ermineJ.data";

            if ( !FileTools.testDir( dataFolder ) ) {

                // try to make it in the user's home directory.
                return ( new File( dataFolder ) ).mkdir();
            }
        }

        return true;
    }

    /**
     * Returns setting values.
     */
    public String getClassFile() {
        return classFile;
    }

    public String getAnnotFile() {
        return annotFile;
    }

    public String getRawFile() {
        return rawFile;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public String getClassFolder() {
        return classFolder;
    }

    public String getScoreFile() {
        return scoreFile;
    }

    public int getMaxClassSize() {
        return maxClassSize;
    }

    public int getMinClassSize() {
        return minClassSize;
    }

    public int getIterations() {
        return iterations;
    }

    public int getScorecol() {
        return scorecol;
    }

    public int getGeneRepTreatment() {
        return geneRepTreatment;
    }

    public int getRawScoreMethod() {
        return rawScoreMethod;
    }

    public int getAnalysisMethod() {
        return analysisMethod;
    }

    public int getQuantile() {
        return quantile;
    }

    public boolean getDoLog() {
        return doLog;
    }

    public double getPValThreshold() {
        return pValThreshold;
    }

    public String getPrefFile() {
        return pref_file;
    }

    /**
     * Sets setting values.
     */
    public void setClassFile( String val ) {
        classFile = val;
    }

    public void setAnnotFile( String val ) {
        annotFile = val;
    }

    public void setRawFile( String val ) {
        rawFile = val;
    }

    public void setDataFolder( String val ) {
        dataFolder = val;
    }

    public void setClassFolder( String val ) {
        classFolder = val;
    }

    public void setScoreFile( String val ) {
        scoreFile = val;
    }

    public void setMaxClassSize( int val ) {
        maxClassSize = val;
    }

    public void setMinClassSize( int val ) {
        minClassSize = val;
    }

    public void setIterations( int val ) {
        iterations = val;
    }

    public void setScorecol( int val ) {
        scorecol = val;
    }

    public void setGeneRepTreatment( int val ) {
        geneRepTreatment = val;
    }

    public void setRawScoreMethod( int val ) {
        rawScoreMethod = val;
    }

    public void setAnalysisMethod( int val ) {
        analysisMethod = val;
    }

    public void setQuantile( int val ) {
        quantile = val;
    }

    public void setDoLog( boolean val ) {
        doLog = val;
    }

    public void setPValThreshold( double val ) {
        pValThreshold = val;
    }

    public void setPrefFile( String val ) {
        pref_file = val;
    }

    public boolean getUseWeights() {
        if ( geneRepTreatment == MEAN_PVAL || geneRepTreatment == BEST_PVAL ) return true;

        return false;
    }

    public String getGroupMethodString() {
        if ( geneRepTreatment == MEAN_PVAL )
            return "MEAN_PVAL";
        else if ( geneRepTreatment == BEST_PVAL )
            return "BEST_PVAL";
        else
            return "MEAN_PVAL"; // dummy. It won't be used.
    }

    public int getGroupMethod() {
        return geneRepTreatment;
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
    public boolean getUseLog() {
        return doLog;
    }

    /**
     * @return
     */
    public boolean getAlwaysUseEmpirical() {
        return alwaysUseEmpirical;
    }

    /**
     * @param b
     */
    public void setAlwaysUseEmpirical( boolean b ) {
        alwaysUseEmpirical = b;
    }

    /**
     * @return Returns the mtc.
     */
    public int getMtc() {
        return mtc;
    }

    /**
     * @param mtc The mtc to set.
     */
    public void setMtc( int mtc ) {
        this.mtc = mtc;
    }

    /**
     * @return
     */
    public boolean getBigIsBetter() {
        return bigIsBetter;
    }

    /**
     * @param b
     */
    public void setBigIsBetter( boolean b ) {
        bigIsBetter = b;
    }

    public boolean isTester() {
        return isTester;
    }

    public void setTester( boolean isTester ) {
        this.isTester = isTester;
    }

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Mostly used for testing
     * 
     * @param outputFile
     */
    public void setOutputFile( String outputFile ) {
        this.outputFile = outputFile;
    }

    /**
     * Mostly used for testing
     * 
     * @return
     */
    public String getGoldStandardFile() {
        return goldStandardFile;
    }

    /**
     * Mostly used for testing
     * 
     * @param goldStandardFile
     */
    public void setGoldStandardFile( String goldStandardFile ) {
        this.goldStandardFile = goldStandardFile;
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

            if ( properties.containsKey( "dataFolder" ) ) this.dataFolder = properties.getProperty( "dataFolder" );

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

            // System.err.println(this);
        } catch ( IOException ex ) {
            System.err.println( "Could not find preferences file. Will probably attempt to create a new one." );
        }
    }

}