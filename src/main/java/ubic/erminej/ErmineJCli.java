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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.UIManager;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.SettingsHolder.GeneScoreMethod;
import ubic.erminej.SettingsHolder.Method;
import ubic.erminej.SettingsHolder.MultiElementHandling;
import ubic.erminej.SettingsHolder.MultiTestCorrMethod;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotationParser;
import ubic.erminej.data.GeneAnnotationParser.Format;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetTerms;

/**
 * Run ermineJ from the command line (or fire up the GUI).
 *
 * @author  Paul Pavlidis
 * @author  keshav
 */
public class ErmineJCli {

    private static final String FOOTER = "ermineJ, Copyright (c) 2006-2021 University of British Columbia.\nFor more help go to https://erminej.msl.ubc.ca/";
    private static final String HEADER = "Options:";

    private static Log log = LogFactory.getLog( ErmineJCli.class );

    /**
     * <p>
     * main.
     * </p>
     *
     * @param  args                an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main( String[] args ) throws Exception {
        ErmineJCli cmd = new ErmineJCli();
        try {
            cmd.run( args );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    protected GeneAnnotations geneData;

    protected GeneSetTerms goData;
    protected Settings settings;
    protected StatusViewer statusMessenger;
    private File batchFile;
    private CommandLine commandLine;
    private Options options = new Options();

    private boolean saveAllGenes = false;

    // protected Map<String, DoubleMatrix<Probe, String>> rawDataSets;
    // protected Map<String, GeneScores> geneScoreSets;
    private String saveFileName = null;

    private boolean useCommandLineInterface = true;

    /**
     * <p>
     * Constructor for ErmineJCli.
     * </p>
     */
    public ErmineJCli() {
        try {
            settings = new Settings( false );
        } catch ( IOException e ) {
            log.debug( e, e );
            log.fatal( "There was an error with the configuration: " + e );
            System.exit( 1 );
        }
        // rawDataSets = new HashMap<String, DoubleMatrix<Probe, String>>();
        // geneScoreSets = new HashMap<String, GeneScores>();
        this.buildOptions();
    }

    /**
     * <p>
     * Getter for the field <code>saveFileName</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSaveFileName() {
        return saveFileName;
    }

    /**
     * <p>
     * Getter for the field <code>settings</code>.
     * </p>
     *
     * @return a {@link ubic.erminej.SettingsHolder} object.
     */
    public SettingsHolder getSettings() {
        return settings;
    }

    /**
     * <p>
     * Getter for the field <code>statusMessenger</code>.
     * </p>
     *
     * @return a {@link ubic.basecode.util.StatusViewer} object.
     */
    public StatusViewer getStatusMessenger() {
        return statusMessenger;
    }

    /**
     * <p>
     * isSaveAllGenes.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isSaveAllGenes() {
        return saveAllGenes;
    }

    /**
     * <p>
     * isUseCommandLineInterface.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isUseCommandLineInterface() {
        return useCommandLineInterface;
    }

    /**
     * <p>
     * analyze.
     * </p>
     *
     * @throws java.lang.IllegalArgumentException
     * @throws java.io.IOException                if any.
     * @return                                    a {@link ubic.erminej.analysis.GeneSetPvalRun} object.
     */
    protected GeneSetPvalRun analyze() throws IOException {
        statusMessenger.showProgress( "Starting analysis" );
        GeneSetPvalRun runResult = new GeneSetPvalRun( settings, geneData, statusMessenger );
        return runResult;
    }

    /**
     * <p>
     * batchAnalyze.
     * </p>
     *
     * @throws java.io.IOException if any.
     */
    protected void batchAnalyze() throws IOException {
        Collection<GeneSetPvalRun> results = new HashSet<>();

        if ( settings.getClassScoreMethod().equals( Method.CORR ) ) {
            throw new IllegalArgumentException(
                    "Batch analysis is only supported for gene score methods; option CORR not applicable" );
        }

        Collection<File> scoreFiles = readScoreFileList();
        statusMessenger.showProgress( "Batch processing " + scoreFiles.size() + " files" );

        for ( File file : scoreFiles ) {

            if ( !file.canRead() ) {
                statusMessenger.showError( "Could not read score file: " + file );
                continue;

            }

            settings.setScoreFile( file.getAbsolutePath() );
            statusMessenger.showProgress( "Starting analysis of " + file );
            GeneSetPvalRun runResult = new GeneSetPvalRun( settings, geneData, statusMessenger );
            results.add( runResult );
            String outputFile = file.getAbsolutePath().replaceAll( "\\.txt$", "" ) + ".erminej.txt";
            statusMessenger.showProgress( "Writing results to " + outputFile );
            ResultsPrinter.write( outputFile, runResult, isSaveAllGenes() );
        }
    }

    /**
     * <p>
     * initialize.
     * </p>
     *
     * @see ubic.erminej.gui.MainFrame#readDataFilesForStartup
     */
    protected void initialize() {
        try {
            statusMessenger = new StatusStderr();
            statusMessenger.showProgress( "Reading GO descriptions from " + settings.getClassFile() );

            goData = new GeneSetTerms( settings.getClassFile(), settings );
            GeneAnnotationParser parser = new GeneAnnotationParser( goData, statusMessenger );

            statusMessenger.showProgress( "Reading gene annotations from " + settings.getAnnotFile() );

            geneData = parser.read( settings.getAnnotFile(), settings.getAnnotFormat(), settings );

            statusMessenger.showProgress( "Initializing gene class mapping" );
            statusMessenger.showProgress( "Done with setup" );
            statusMessenger.showStatus( "Ready." );
        } catch ( IOException e ) {
            statusMessenger.showStatus( "File reading or writing error during initialization: " + e.getMessage()
                    + "\nIf this problem persists, please contact the software developer. " + "\nPress OK to quit." );
            System.exit( 1 );
        }

        statusMessenger.showStatus( "Done with initialization." );
    }

    /**
     * <p>
     * printHelp.
     * </p>
     *
     * @param command The name of the command as used at the command line.
     */
    protected void printHelp( String command ) {
        HelpFormatter h = new HelpFormatter();
        h.printHelp( 100, "$ERMINEJ_HOME/bin/ermineJ.sh ", HEADER, options, FOOTER, true );
    }

    /**
     * <p>
     * processCommandLine.
     * </p>
     *
     * @param  commandName         a {@link java.lang.String} object.
     * @param  args                an array of {@link java.lang.String} objects.
     * @return                     a boolean.
     * @throws java.lang.Exception if any.
     */
    protected final boolean processCommandLine( String commandName, String[] args ) throws Exception {
        /* COMMAND LINE PARSER STAGE */
        BasicParser parser = new BasicParser();

        if ( args == null ) {
            printHelp( commandName );
            return false;
        }

        try {
            commandLine = parser.parse( options, args );
        } catch ( ParseException e ) {
            if ( e instanceof MissingOptionException ) {
                System.out.println( "Required option(s) were not supplied: " + e.getMessage() );
            } else if ( e instanceof AlreadySelectedException ) {
                System.out.println( "The option(s) " + e.getMessage() + " were already selected" );
            } else if ( e instanceof MissingArgumentException ) {
                System.out.println( "Missing argument: " + e.getMessage() );
            } else if ( e instanceof UnrecognizedOptionException ) {
                System.out.println( "Unrecognized option: " + e.getMessage() );
            } else {
                e.printStackTrace();
            }

            printHelp( commandName );

            return false;
        }

        return processOptions();
    }

    /**
     * <p>
     * run.
     * </p>
     *
     * @param  args                an array of {@link java.lang.String} objects.
     * @throws IOException         if any.
     * @return                     a boolean.
     * @throws java.lang.Exception if any.
     */
    protected boolean run( String[] args ) throws Exception {

        boolean okay = processCommandLine( "ermineJ", args );

        if ( !okay ) {
            return false;
        }

        if ( isUseCommandLineInterface() ) {
            initialize();

            if ( batchFile != null ) {
                batchAnalyze();
            } else {

                try {
                    GeneSetPvalRun result = analyze();
                    ResultsPrinter.write( getSaveFileName(), result, isSaveAllGenes() );
                } catch ( Exception e ) {
                    getStatusMessenger().showStatus( "Error During analysis:" + e );
                    e.printStackTrace();
                    return false;
                }
            }

        } else {
            try {
                UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            } catch ( Exception e ) {
                e.printStackTrace();
                return false;
            }
            @SuppressWarnings("unused")
            ErmineJGui classScoreGUI = new ErmineJGui( settings );
        }
        return true;
    }

    @SuppressWarnings("static-access")
    private void buildOptions() {

        OptionBuilder.withLongOpt( "help" );
        OptionBuilder.withDescription( "Print this message" );
        options.addOption( OptionBuilder.create( 'h' ) );

        OptionBuilder
                .withLongOpt( "config" );
        OptionBuilder
                .hasArg();
        OptionBuilder
                .withDescription(
                        "Configuration file to use (saves typing); additional options given on the command line override those in the file."
                                + " If you don't use this option, no configuration file will be used." );
        OptionBuilder
                .withArgName( "config file" );
        options.addOption( OptionBuilder.create( 'C' ) );

        OptionBuilder.withDescription( "Launch the GUI." );
        OptionBuilder.withLongOpt( "gui" );
        options.addOption( OptionBuilder.create( 'G' ) );

        OptionBuilder.hasArg();
        OptionBuilder
                .withDescription( "Annotation file to be used [required unless using GUI]" );
        OptionBuilder.withLongOpt( "annots" );
        OptionBuilder
                .withArgName( "file" );
        options.addOption( OptionBuilder.create( 'a' ) );

        //        OptionBuilder.withLongOpt( "affy" );
        //        OptionBuilder.withDescription( "Annotation file is in Affymetrix format" );
        //        options.addOption( OptionBuilder
        //                .create( 'A' ) );

        OptionBuilder.withDescription(
                "Sets 'big is better' option for gene scores to true [default = "
                        + SettingsHolder.getDefault( SettingsHolder.BIG_IS_BETTER ) + "]" );
        options.addOption( OptionBuilder
                .create( 'b' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withLongOpt( "classFile" );
        OptionBuilder
                .withDescription( "Gene set ('class') file, e.g. GO file [OBO format; required unless using GUI]" );
        OptionBuilder
                .withArgName( "file" );
        options.addOption( OptionBuilder.create( 'c' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withDescription( "Column index for scores in input file (default: 2)" );
        OptionBuilder
                .withLongOpt( "scoreCol" );
        OptionBuilder.withArgName( "integer" );
        options.addOption( OptionBuilder.create( 'e' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "directory" );
        OptionBuilder
                .withDescription( "Data directory; default is your ermineJ.data directory" );
        options.addOption( OptionBuilder.create( 'd' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "directory" );
        OptionBuilder
                .withDescription( "Directory where custom gene set are located" );
        options.addOption( OptionBuilder.create( 'f' ) );

        //        OptionBuilder.withLongOpt( "filterNonSpecific" );
        //        OptionBuilder
        //                .withDescription( "Filter out non-specific elements (default annotation format only), default=true" );
        //        options.addOption( OptionBuilder
        //                .create( 'F' ) );

        OptionBuilder
                .hasArg();
        OptionBuilder
                .withArgName( "BEST|MEAN" );
        OptionBuilder
                .withLongOpt( "reps" );
        OptionBuilder
                .withDescription(
                        "What to do when genes have multiple scores"
                                + " in input file (due to multiple elements per gene): BEST = best of replicates; MEAN = mean of replicates; default="
                                + SettingsHolder.getDefault( SettingsHolder.GENE_REP_TREATMENT ) );
        options.addOption( OptionBuilder
                .create( 'g' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withLongOpt( "iters" );
        OptionBuilder
                .withDescription( "Number of iterations (GSR and CORR methods only)" );
        OptionBuilder.withArgName( "iterations" );
        options.addOption( OptionBuilder
                .create( 'i' ) );

        OptionBuilder
                .withDescription(
                        "Output should include gene symbols for all gene sets (default=don't include symbols)" );
        OptionBuilder
                .withLongOpt( "genesOut" );
        options.addOption( OptionBuilder.create( 'j' ) );

        OptionBuilder
                .withLongOpt( "logTrans" );
        OptionBuilder
                .withDescription(
                        "Log transform the scores (and change sign; recommended for p-values), default=don't transform" );
        options.addOption( OptionBuilder
                .create( 'l' ) );

        OptionBuilder
                .hasArg();
        OptionBuilder
                .withDescription(
                        "Method for computing raw class statistics (used for test=GSR only): "
                                + SettingsHolder.GeneScoreMethod.MEAN + " (mean),  "
                                + SettingsHolder.GeneScoreMethod.QUANTILE + " (quantile), or  "
                                + SettingsHolder.GeneScoreMethod.MEAN_ABOVE_QUANTILE + " (mean above quantile), or "
                                + SettingsHolder.GeneScoreMethod.PRECISIONRECALL
                                + " (area under the precision-recall curve); default="
                                + SettingsHolder.getDefault( SettingsHolder.GENE_SET_RESAMPLING_SCORE_METHOD ) );
        OptionBuilder
                .withLongOpt( "stats" );
        OptionBuilder.withArgName( "option" );
        options.addOption( OptionBuilder.create( 'm' ) );

        OptionBuilder
                .hasArg();
        OptionBuilder
                .withDescription(
                        "Method for computing gene set significance:  " + SettingsHolder.Method.ORA + " (ORA),  "
                                + SettingsHolder.Method.GSR
                                + " (resampling of gene scores; use with -m to choose algorithm),  "
                                + SettingsHolder.Method.CORR + " (profile correlation),  " + SettingsHolder.Method.ROC
                                + " (ROC)" );
        OptionBuilder
                .withLongOpt( "test" );
        OptionBuilder.withArgName( "value" );
        options.addOption( OptionBuilder.create( 'n' ) );

        OptionBuilder.hasArg();
        OptionBuilder
                .withDescription( "Batch process score files from a list, one per line. Incompatible with -o, -s, -G" );
        OptionBuilder
                .withArgName( "scoreFileList" );
        options.addOption( OptionBuilder.create( "batch" ) );

        //    OptionBuilder.withDescription( "Disable multifunctionality correction (default: on)" );
        /*
         * The intention is that this would be on
         */
        //        options.addOption( OptionBuilder
        //                .create( "nomf" ) );

        OptionBuilder.hasArg();
        OptionBuilder
                .withDescription( "Output file name; if omitted, results are written to standard out" );
        OptionBuilder
                .withArgName( "output file" );
        OptionBuilder.withLongOpt( "output" );
        options.addOption( OptionBuilder.create( 'o' ) );

        OptionBuilder
                .withDescription( "quantile to use, only used for 'MEAN_ABOVE_QUANTILE', default=50 (median)" );
        OptionBuilder
                .withArgName( "quantile" );
        OptionBuilder.withLongOpt( "quantile" );
        OptionBuilder.hasArg();
        options.addOption( OptionBuilder.create( 'q' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withLongOpt( "rawData" );
        OptionBuilder
                .withDescription( "Raw data file, only needed for profile correlation analysis" );
        OptionBuilder
                .withArgName( "data file" );
        options.addOption( OptionBuilder.create( 'r' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withLongOpt( "scoreFile" );
        OptionBuilder
                .withDescription( "Score file, required for all but profile correlation method" );
        OptionBuilder
                .withArgName( "score file" );
        options.addOption( OptionBuilder.create( 's' ) );

        OptionBuilder
                .hasArg();
        OptionBuilder
                .withLongOpt( "threshold" );
        OptionBuilder
                .withDescription(
                        "Score threshold, only used for ORA; default = "
                                + SettingsHolder.getDefault( SettingsHolder.GENE_SCORE_THRESHOLD_KEY ) );
        OptionBuilder
                .withArgName( "threshold" );
        options.addOption( OptionBuilder.create( 't' ) );

        OptionBuilder
                .hasArg();
        OptionBuilder
                .withDescription(
                        "Sets the minimum class size; default = "
                                + SettingsHolder.getDefault( SettingsHolder.MIN_CLASS_SIZE ) );
        OptionBuilder
                .withArgName( "minClassSize" );
        OptionBuilder.withLongOpt( "minClassSize" );
        options.addOption( OptionBuilder.create( 'y' ) );

        OptionBuilder
                .hasArg();
        OptionBuilder
                .withDescription(
                        "Sets the maximum class size; default = "
                                + SettingsHolder.getDefault( SettingsHolder.MAX_CLASS_SIZE ) );
        OptionBuilder
                .withArgName( "maxClassSize" );
        OptionBuilder.withLongOpt( "maxClassSize" );
        options.addOption( OptionBuilder.create( 'x' ) );

        OptionBuilder.hasArg();
        OptionBuilder.withLongOpt( "saveconfig" );
        OptionBuilder
                .withDescription( "Save preferences in the specified file" );
        OptionBuilder.withArgName( "file" );
        options.addOption( OptionBuilder.create( 'S' ) );

        OptionBuilder
                .hasArg();
        OptionBuilder
                .withDescription(
                        "Multiple test correction method: " + SettingsHolder.MultiTestCorrMethod.FWE
                                + " = Bonferroni FWE, "
                                // + SettingsHolder.MultiTestCorrMethod.WESTFALLYOUNG
                                //  + " = Westfall-Young (slow), "
                                + SettingsHolder.MultiTestCorrMethod.FDR
                                + " = Benjamini-Hochberg FDR [default]" );
        OptionBuilder
                .withLongOpt( "mtc" );
        OptionBuilder.withArgName( "value" );
        options.addOption( OptionBuilder
                .create( 'M' ) );

        options.addOption( OptionBuilder.hasArg().withDescription( "GO aspects to include: B, C, M; "
                + "for example for Biological Process only use B; to add Cellular Component use BC (Default: BCM = all )" )
                .withArgName( "selections" ).create( "aspects" ) );

        options.addOption(
                OptionBuilder.hasArg().withArgName( "value" ).withDescription( "Seed for random number generation (integer)" ).create( "seed" ) );

    }

    /**
     * @return true if everything is okay
     */
    private boolean processOptions() throws IOException {

        if ( commandLine.hasOption( 'h' ) ) {
            showHelp();
            return false;
        }

        if ( commandLine.hasOption( "batch" ) ) {
            if ( commandLine.hasOption( 'G' ) || commandLine.hasOption( "s" ) || commandLine.hasOption( "o" ) ) {
                System.err.println( "Cannot combine --batch with certain other options" );
                showHelp();
                return false;
            }

            String batchFileOption = commandLine.getOptionValue( "batch" );
            batchFile = new File( batchFileOption );
            if ( !batchFile.canRead() ) {
                System.err.println( "Cannot read from batch file " + batchFile.getAbsolutePath() );
                showHelp();
                return false;
            }

        }

        if ( commandLine.hasOption( 'G' ) ) {
            useCommandLineInterface = false;

            /*
             * GUI: Read the default configuration file for the user.
             */
            settings = new Settings();

            /*
             * Don't process any more options, start the gui
             */
            System.err.println( "Ignoring further command line options and starting the GUI" );
            return true;

        }

        String arg;

        /*
         * We only read the config file if 1) it is specified by the user or 2) the user is starting the GUI. In either
         * case, command line options can override.
         */
        if ( commandLine.hasOption( 'C' ) ) {
            arg = commandLine.getOptionValue( 'C' );
            if ( FileTools.testFile( arg ) ) {
                try {
                    settings = new Settings( arg );
                    System.err.println( "Initializing configuration from " + arg );
                } catch ( ConfigurationException e ) {
                    System.err.println( "Invalid config file name (" + arg + ")" );
                    showHelp();
                    return false;
                }
            } else {
                System.err.println( "Could not open configuration file: " + arg );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'A' ) ) {
            settings.setAnnotFormat( Format.AFFYCSV );
        }
        if ( commandLine.hasOption( 'a' ) ) {
            arg = commandLine.getOptionValue( 'a' );
            if ( FileTools.testFile( arg ) )
                settings.setAnnotFile( new File( arg ).getAbsolutePath() );
            else {
                System.err.println( "Invalid annotation file name (-a " + arg + ")" );
                showHelp();
                return false;
            }
        } else if ( !commandLine.hasOption( 'G' ) && !commandLine.hasOption( 'C' ) ) { // using GUI or config file?
            System.err.println( "Annotation file name (-a) is required or must be supplied in config file" );
            showHelp();
            return false;
        }

        settings.setBigIsBetter( commandLine.hasOption( 'b' ) );

        if ( commandLine.hasOption( 'c' ) ) {
            arg = commandLine.getOptionValue( 'c' );
            if ( FileTools.testFile( arg ) )
                settings.setClassFile( new File( arg ).getAbsolutePath() );
            else {
                System.err.println( "Invalid gene set definition file name (-c " + arg + ")" );
                showHelp();
                return false;
            }
        } else if ( !commandLine.hasOption( 'G' ) && !commandLine.hasOption( 'C' ) ) { // using GUI config file?
            System.err.println( "Gene set definition file (-c) is required" );
            showHelp();
            return false;
        }

        if ( commandLine.hasOption( 'd' ) ) {
            arg = commandLine.getOptionValue( 'd' );
            if ( FileTools.testDir( arg ) )
                settings.setDataDirectory( new File( arg ).getAbsolutePath() );
            else {
                System.err.println( "Invalid path for data folder (-d " + arg + ")" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'e' ) ) {
            arg = commandLine.getOptionValue( 'e' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg >= 2 ) {
                    settings.setScoreCol( intarg );
                } else {
                    System.err.println( "Invalid score column (-e " + intarg + "), must be a value 2 or higher" );
                    showHelp();
                    return false;
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Invalid score column (-e " + arg + "), must be an integer" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'f' ) ) {
            arg = commandLine.getOptionValue( 'f' );
            if ( !FileTools.testDir( arg ) ) new File( arg ).mkdir();

            settings.setCustomGeneSetDirectory( new File( arg ).getAbsolutePath() );
            log.debug( settings.getCustomGeneSetDirectory() );
        }
        if ( commandLine.hasOption( 'g' ) ) {
            arg = commandLine.getOptionValue( 'g' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg == 1 || intarg == 2 ) {
                    // Backwards compatibility

                    switch ( intarg ) {
                        case 1:
                            settings.setGeneRepTreatment( SettingsHolder.MultiElementHandling.BEST );
                            break;
                        case 2:
                            settings.setGeneRepTreatment( SettingsHolder.MultiElementHandling.MEAN );
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }

                    System.err
                            .println( "Please consider switching to the new command line style for this option (BEST or MEAN)" );
                } else {
                    throw new IllegalArgumentException();
                }
            } catch ( NumberFormatException e ) {
                MultiElementHandling val = SettingsHolder.MultiElementHandling.valueOf( arg );
                settings.setGeneRepTreatment( val );
            } catch ( Exception e ) {
                System.err.println( "Gene rep treatment must be either BEST or MEAN (-g)" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'i' ) ) {
            arg = commandLine.getOptionValue( 'i' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg > 0 )
                    settings.setIterations( intarg );
                else {
                    System.err.println( "Iterations must be greater than 0 (-i)" );
                    showHelp();
                    return false;
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Iterations must be a number" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'j' ) ) {
            log.info( "Gene symbols for each term will be output" );
            this.saveAllGenes = true;

        }
        if ( commandLine.hasOption( 'k' ) ) {
            arg = commandLine.getOptionValue( 'k' );
        }

        if ( commandLine.hasOption( 'l' ) ) {
            settings.setDoLog( true );
        } else {
            settings.setDoLog( false );
        }

        if ( commandLine.hasOption( 'm' ) ) {
            arg = commandLine.getOptionValue( 'm' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg == 0 || intarg == 1 || intarg == 2 ) {
                    // backwards compatibility

                    switch ( intarg ) {
                        case 0:
                            settings.setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.MEAN );
                            break;
                        case 1:
                            settings.setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.QUANTILE );
                            break;
                        case 2:
                            settings.setGeneSetResamplingScoreMethod( SettingsHolder.GeneScoreMethod.MEAN_ABOVE_QUANTILE );
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                    System.err
                            .println(
                                    "Please consider switching to the new command line style for this option (MEAN, QUANTILE, MEAN_ABOVE_QUANTILE or PRECISIONRECALL)" );
                } else {
                    throw new IllegalArgumentException();
                }
            } catch ( NumberFormatException e ) {
                GeneScoreMethod opt = SettingsHolder.GeneScoreMethod.valueOf( arg );
                settings.setGeneSetResamplingScoreMethod( opt );
            } catch ( Exception e ) {
                System.err
                        .println( "Raw score method must be set to MEAN, QUANTILE, MEAN_ABOVE_QUANTILE or PRECISIONRECALL" );
                showHelp();
                return false;
            }
        }

        //  settings.setFilterNonSpecific( commandLine.hasOption( 'F' ) );

        if ( commandLine.hasOption( 'M' ) ) {
            arg = commandLine.getOptionValue( 'M' );
            try {
                int mtc = Integer.parseInt( arg );
                if ( mtc == 0 || mtc == 1 || mtc == 2 ) {
                    switch ( mtc ) {
                        case 0:
                            settings.setMtc( SettingsHolder.MultiTestCorrMethod.FWE );
                            break;
                        case 1:
                            throw new IllegalArgumentException( "Westfall-Young correction is no longer supported" );
                            //                            settings.setMtc( SettingsHolder.MultiTestCorrMethod.WESTFALLYOUNG );
                            //                            break;
                        case 2:
                            settings.setMtc( SettingsHolder.MultiTestCorrMethod.FDR );
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }

                    System.err
                            .println(
                                    "Please consider switching to the new command line style for this option (FDR or FWE)" );
                } else {
                    System.err.println( "Multiple test correction must be FDR (Benjamini-Hochberg) or FWE (Bonferroni)" );
                    showHelp();
                    return false;
                }
            } catch ( NumberFormatException e ) {
                MultiTestCorrMethod mtcmethod = SettingsHolder.MultiTestCorrMethod.valueOf( arg );
                settings.setMtc( mtcmethod );
            } catch ( Exception e ) {
                System.err.println( "Multiple test correction must be FDR (Benjamini-Hochberg) or FWE (Bonferroni)" );
                showHelp();
                return false;
            }
        }

        if ( commandLine.hasOption( 'n' ) ) {
            arg = commandLine.getOptionValue( 'n' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg == 0 || intarg == 1 || intarg == 2 || intarg == 3 ) {
                    // backwards compatibility
                    switch ( intarg ) {
                        case 0:
                            settings.setClassScoreMethod( SettingsHolder.Method.ORA );
                            break;
                        case 1:
                            settings.setClassScoreMethod( SettingsHolder.Method.GSR );
                            break;
                        case 2:
                            settings.setClassScoreMethod( SettingsHolder.Method.CORR );
                            break;
                        case 3:
                            settings.setClassScoreMethod( SettingsHolder.Method.ROC );
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }

                    System.err
                            .println( "Please consider switching to the new command line style for this option (ORA, GSR, ROC or CORR)" );
                } else {
                    throw new IllegalArgumentException();
                }
            } catch ( NumberFormatException e ) {
                Method method = SettingsHolder.Method.valueOf( arg );
                settings.setClassScoreMethod( method );
            } catch ( Exception e ) {
                System.err.println( "Analysis method must be set to ORA, GSR, ROC or CORR" );
                showHelp();
                return false;
            }

            //            if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.ORA ) && commandLine.hasOption( "nomf" ) ) {
            //                settings.setUseMultifunctionalityCorrection( false );
            //            } else {
            //                settings.setUseMultifunctionalityCorrection( true );
            //            }
        }

        if ( commandLine.hasOption( 'o' ) ) {
            arg = commandLine.getOptionValue( 'o' );
            saveFileName = arg;
        } else {
            saveFileName = null;
        }

        if ( commandLine.hasOption( 'q' ) ) {
            arg = commandLine.getOptionValue( 'q' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg >= 0 && intarg <= 100 )
                    settings.setQuantile( intarg );
                else {
                    System.err.println( "Quantile must be between 0 and 100 (-q)" );
                    showHelp();
                    return false;
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Quantile must be between 0 and 100 (-q)" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'r' ) ) {
            arg = commandLine.getOptionValue( 'r' );
            if ( FileTools.testFile( arg ) )
                settings.setRawFile( arg );
            else {
                System.err.println( "Invalid raw file name (-r " + arg + ")" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 's' ) ) {
            arg = commandLine.getOptionValue( 's' );
            if ( FileTools.testFile( arg ) )
                settings.setScoreFile( new File( arg ).getAbsolutePath() );
            else {
                System.err.println( "Invalid score file name (-s " + arg + ")" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 't' ) ) {
            arg = commandLine.getOptionValue( 't' );
            try {
                double doublearg = Double.parseDouble( arg );
                settings.setGeneScoreThreshold( doublearg );
            } catch ( NumberFormatException e ) {
                System.err.println( "The threshold must be a number" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'u' ) ) {
            arg = commandLine.getOptionValue( 'u' );
        }
        if ( commandLine.hasOption( 'v' ) ) {
            arg = commandLine.getOptionValue( 'v' );
        }
        if ( commandLine.hasOption( 'x' ) ) {
            arg = commandLine.getOptionValue( 'x' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg > 1 )
                    settings.setMaxClassSize( intarg );
                else {
                    System.err.println( "The maximum class size must be greater than 1 (-x)" );
                    showHelp();
                    return false;
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "The maximum class size must be greater than 1 (-x)" );
                showHelp();
                return false;
            }
        }
        if ( commandLine.hasOption( 'y' ) ) {
            arg = commandLine.getOptionValue( 'y' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg > 0 )
                    settings.setMinClassSize( intarg );
                else {
                    System.err.println( "The minimum class size must be greater than 0 (-y)" );
                    showHelp();
                    return false;
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "The minimum class size must be greater than 0 (-y)" );
                showHelp();
                return false;
            }
        }

        if ( commandLine.hasOption( "aspects" ) ) {
            arg = commandLine.getOptionValue( "aspects" );
            if ( !arg.matches( "[BCM]{1,3}" ) ) {
                System.err.println(
                        "Valid choices for aspect are any of"
                                + " B (Biological Process), C (Cellular Component) or M ( Molecular function) or a combination" );
                showHelp();
                return false;
            }

            settings.setUseBiologicalProcess( true );
            settings.setUseMolecularFunction( true );
            settings.setUseCellularComponent( true );

            if ( !arg.contains( "B" ) ) {
                settings.setUseBiologicalProcess( false );
            }
            if ( !arg.contains( "M" ) ) {
                settings.setUseMolecularFunction( false );
            }
            if ( !arg.contains( "C" ) ) {
                settings.setUseCellularComponent( false );
            }
        }

        if ( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR )
                && settings.getRawDataFileName() == null ) {
            System.err.println( "You must supply a raw data file if you are using the correlation method" );
            showHelp();
            return false;
        }

        if ( !( settings.getClassScoreMethod().equals( SettingsHolder.Method.CORR ) )
                && settings.getScoreFile() == null ) {
            System.err.println( "You must supply a gene score file if you are not using the correlation method" );
            showHelp();
            return false;
        }

        if ( commandLine.hasOption( "seed" ) ) {
            arg = commandLine.getOptionValue( "seed" );
            try {
                Long seed = Long.parseLong( arg );
                settings.setRandomSeed( seed );
            } catch ( Exception e ) {
                System.err.println( "Seed must be an integer value" );
                showHelp();
                return false;
            }
        }

        if ( commandLine.hasOption( 'S' ) ) {
            // saveconfig
            arg = commandLine.getOptionValue( 'S' );
            settings.writeAnalysisSettings( arg );
        }

        return true;
    }

    /**
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Collection<File> readScoreFileList() throws FileNotFoundException, IOException {
        Collection<File> scoreFiles = new ArrayList<>();

        try (BufferedReader scoreFileListReader = new BufferedReader( new FileReader( batchFile ) )) {

            while ( scoreFileListReader.ready() ) {
                String rawLine = scoreFileListReader.readLine();
                String fileName = StringUtils.strip( rawLine );
                File f = new File( fileName );
                scoreFiles.add( f );

            }
            scoreFileListReader.close();
            return scoreFiles;
        } catch ( Exception e ) {
            throw ( e );
        }
    }

    private void showHelp() {
        printHelp( "ermineJ" );
    }
}
