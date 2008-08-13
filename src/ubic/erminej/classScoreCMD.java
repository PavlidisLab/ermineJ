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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.dataStructure.matrix.DoubleMatrixNamed;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.UserDefinedGeneSetManager;

/**
 * Run ermineJ from the command line (or fire up the GUI).
 * 
 * @author Paul Pavlidis
 * @author keshav
 * @version $Id$
 */
public class classScoreCMD {

    private static final String HEADER = "Options:";
    private static final String FOOTER = "ermineJ, Copyright (c) 2006-2007 University of British Columbia.";

    private static Log log = LogFactory.getLog( classScoreCMD.class );

    public static void main( String[] args ) {
        try {
            classScoreCMD cmd = new classScoreCMD();
            cmd.processCommandLine( "ermineJ", args );
            // options( args );
            cmd.getSettings().setDirectories();
            if ( cmd.isUseCommandLineInterface() ) {
                cmd.initialize();
                try {
                    GeneSetPvalRun result = cmd.analyze();
                    cmd.getSettings().writeAnalysisSettings( cmd.getSaveFileName() );
                    ResultsPrinter rp = new ResultsPrinter( cmd.getSaveFileName(), result, cmd.getGoData(), cmd
                            .isSaveAllGenes() );
                    rp.printResults( true );
                } catch ( Exception e ) {
                    cmd.getStatusMessenger().showStatus( "Error During analysis:" + e );
                    e.printStackTrace();
                }
            } else {
                try {
                    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
                new classScoreGUI();
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    protected Settings settings;
    protected StatusViewer statusMessenger;
    protected GONames goData;
    protected GeneAnnotations geneData;
    protected Map<Integer, GeneAnnotations> geneDataSets;
    protected Map<String, DoubleMatrixNamed<String, String>> rawDataSets;
    protected Map<String, GeneScores> geneScoreSets;
    private String saveFileName = null;
    private boolean useCommandLineInterface = true;

    private boolean saveAllGenes = false;

    private Options options = new Options();

    private CommandLine commandLine;

    public classScoreCMD() throws IOException {
        settings = new Settings();
        rawDataSets = new HashMap<String, DoubleMatrixNamed<String, String>>();
        geneDataSets = new HashMap<Integer, GeneAnnotations>();
        geneScoreSets = new HashMap<String, GeneScores>();
        this.buildOptions();
    }

    public GONames getGoData() {
        return goData;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public Settings getSettings() {
        return settings;
    }

    public StatusViewer getStatusMessenger() {
        return statusMessenger;
    }

    public boolean isSaveAllGenes() {
        return saveAllGenes;
    }

    public boolean isUseCommandLineInterface() {
        return useCommandLineInterface;
    }

    /**
     * @throws IllegalArgumentException
     * @return
     * @throws IOException
     */
    protected GeneSetPvalRun analyze() throws IOException {
        DoubleMatrixNamed<String, String> rawData = null;
        if ( settings.getClassScoreMethod() == Settings.CORR ) {
            if ( rawDataSets.containsKey( settings.getRawDataFileName() ) ) {
                statusMessenger.showStatus( "Raw data are in memory" );
                rawData = rawDataSets.get( settings.getRawDataFileName() );
            } else {
                statusMessenger.showStatus( "Reading raw data from file " + settings.getRawDataFileName() );
                DoubleMatrixReader r = new DoubleMatrixReader();
                rawData = r.read( settings.getRawDataFileName() );
                rawDataSets.put( settings.getRawDataFileName(), rawData );
            }
        }

        GeneScores geneScores;
        if ( geneScoreSets.containsKey( settings.getScoreFile() ) ) {
            statusMessenger.showStatus( "Gene Scores are in memory" );
            geneScores = geneScoreSets.get( settings.getScoreFile() );
        } else {
            statusMessenger.showStatus( "Reading gene scores from file " + settings.getScoreFile() );
            geneScores = new GeneScores( settings.getScoreFile(), settings, statusMessenger, geneData );
            geneScoreSets.put( settings.getScoreFile(), geneScores );
        }

        if ( !settings.getScoreFile().equals( "" ) && geneScores == null ) {
            statusMessenger.showStatus( "Didn't get geneScores" );
        }

        Set<String> activeProbes = null;
        if ( rawData != null && geneScores != null ) { // favor the geneScores
            // list.
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        } else if ( rawData == null && geneScores != null ) {
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        } else if ( rawData != null && geneScores == null ) {
            activeProbes = new HashSet<String>( rawData.getRowNames() );
        }

        boolean needToMakeNewGeneData = true;
        for ( Iterator<Integer> it = geneDataSets.keySet().iterator(); it.hasNext(); ) {
            GeneAnnotations test = geneDataSets.get( it.next() );

            if ( test.getProbeToGeneMap().keySet().equals( activeProbes ) ) {
                geneData = test;
                needToMakeNewGeneData = false;
                break;
            }

        }

        if ( needToMakeNewGeneData ) {
            geneData = new GeneAnnotations( geneData, activeProbes );
            geneDataSets.put( new Integer( geneData.hashCode() ), geneData );
        }

        /* do work */
        statusMessenger.showStatus( "Starting analysis..." );
        GeneSetPvalRun runResult = new GeneSetPvalRun( activeProbes, settings, geneData, rawData, goData, geneScores,
                statusMessenger, "command" );
        return runResult;
    }

    /**
     * @see ubic.erminej.gui.GeneSetScoreFrame.readDataFilesForStartup
     */
    protected void initialize() {
        try {
            statusMessenger = new StatusStderr();
            statusMessenger.showStatus( "Reading GO descriptions from " + settings.getClassFile() );

            goData = new GONames( settings.getClassFile() );

            statusMessenger.showStatus( "Reading gene annotations from " + settings.getAnnotFile() );
            if ( settings.getAnnotFormat() == 1 ) {
                geneData = new GeneAnnotations( settings.getAnnotFile(), statusMessenger, goData,
                        GeneAnnotations.AFFYCSV );
            } else {
                geneData = new GeneAnnotations( settings.getAnnotFile(), statusMessenger, goData );
                // TODO add agilent support ... can we tell the type of file by the suffix?
            }

            statusMessenger.showStatus( "Initializing gene class mapping" );
            geneDataSets.put( new Integer( "original".hashCode() ), geneData );
            statusMessenger.showStatus( "Done with setup" );
            statusMessenger.showStatus( "Ready." );
        } catch ( IOException e ) {
            statusMessenger.showStatus( "File reading or writing error during initialization: " + e.getMessage()
                    + "\nIf this problem persists, please contact the software developer. " + "\nPress OK to quit." );
            System.exit( 1 );
        } catch ( SAXException e ) {
            statusMessenger.showStatus( "Gene Ontology file format is incorrect. "
                    + "\nPlease check that it is a valid XML file. "
                    + "\nIf this problem persists, please contact the software developer. " + "\nPress OK to quit." );
            System.exit( 1 );
        }

        // need to load user-defined sets.
        UserDefinedGeneSetManager loader = new UserDefinedGeneSetManager( geneData, settings, "" );
        loader.loadUserGeneSets( this.goData, this.statusMessenger );

        statusMessenger.showStatus( "Done with initialization." );
    }

    /**
     * @param command The name of the command as used at the command line.
     */
    protected void printHelp( String command ) {
        HelpFormatter h = new HelpFormatter();
        h.printHelp( command + " [options]", HEADER, options, FOOTER );
    }

    protected final void processCommandLine( String commandName, String[] args ) {
        /* COMMAND LINE PARSER STAGE */
        BasicParser parser = new BasicParser();

        if ( args == null ) {
            printHelp( commandName );
            System.exit( 0 );
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

            System.exit( 0 );
        }

        processOptions();

    }

    private void buildOptions() {

        options.addOption( OptionBuilder.withLongOpt( "help" ).create( 'h' ) );

        options
                .addOption( OptionBuilder
                        .withLongOpt( "config" )
                        .hasArg()
                        .withDescription(
                                "Configuration file to use (saves typing); additional options given on the command line override those in the file." )
                        .withArgName( "config file" ).create( 'C' ) );

        options.addOption( OptionBuilder.withDescription( "Launch the GUI." ).withLongOpt( "gui" ).create( 'G' ) );

        options.addOption( OptionBuilder.hasArg().withDescription(
                "Annotation file to be used [required unless using GUI]" ).withLongOpt( "annots" ).withArgName( "file" )
                .create( 'a' ) );

        options.addOption( OptionBuilder.withLongOpt( "affy" ).withDescription( "Affymetrix annotation file format" )
                .create( 'A' ) );

        options.addOption( OptionBuilder.withDescription(
                "Sets 'big is better' option for gene scores to true [default =" + settings.getBigIsBetter() + "]" )
                .create( 'b' ) );

        options.addOption( OptionBuilder.hasArg().withLongOpt( "classFile" ).withDescription(
                "Gene set ('class') file, e.g. GO XML file [required unless using GUI]" ).withArgName( "file" ).create(
                'c' ) );

        options.addOption( OptionBuilder.hasArg().withDescription( "Column for scores in input file" ).withLongOpt(
                "scoreCol" ).withArgName( "integer" ).create( 'e' ) );

        options.addOption( OptionBuilder.hasArg().withArgName( "directory" ).withDescription( "Data directory" )
                .create( 'd' ) );

        options.addOption( OptionBuilder.hasArg().withArgName( "directory" ).withDescription(
                "Directory where custom gene set are located" ).create( 'f' ) );

        options
                .addOption( OptionBuilder
                        .withArgName( "value" )
                        .withLongOpt( "reps" )
                        .withDescription(
                                "What to do when genes have multiple scores in input file (due to multiple probes per gene): 1 = best of replicates; 2 = mean of replicates; " )
                        .create( 'g' ) );

        options.addOption( OptionBuilder.hasArg().withLongOpt( "iters" ).withDescription(
                "Number of iterations (for iterative methods only" ).withArgName( "integer" ).create( 'i' ) );

        options.addOption( OptionBuilder.withDescription(
                "Output should include gene symbols for all gene sets (default=don't include symbols)" ).withLongOpt(
                "genesOut" ).create( 'j' ) );

        options.addOption( OptionBuilder.withLongOpt( "logTrans" ).withDescription(
                "Log transform the scores [recommended for p-values]" ).create( 'l' ) );

        options.addOption( OptionBuilder.hasArg().withDescription(
                "Method for computing raw class statistics: " + Settings.MEAN_METHOD + " (mean),  "
                        + Settings.QUANTILE_METHOD + " (quantile), or  " + Settings.MEAN_ABOVE_QUANTILE_METHOD
                        + " (mean above quantile)." ).withLongOpt( "stats" ).withArgName( "value" ).create( 'm' ) );

        options.addOption( OptionBuilder.hasArg().withDescription(
                "Method for computing gene set significance:  " + Settings.ORA + " (ORA),  " + Settings.RESAMP
                        + " (resampling of gene scores),  " + Settings.CORR + " (profile correlation),  "
                        + Settings.ROC + " (ROC)" ).withLongOpt( "test" ).withArgName( "value" ).create( 'n' ) );

        options.addOption( OptionBuilder.hasArg().withDescription(
                "Output file name; if omitted, results are written to standard out" ).withArgName( "file" )
                .withLongOpt( "output" ).create( 'o' ) );

        options.addOption( OptionBuilder.withDescription( "quantile to use" ).withArgName( "integer" ).withLongOpt(
                "quantile" ).hasArg().create( 'q' ) );

        options.addOption( OptionBuilder.hasArg().withLongOpt( "rawData" ).withDescription(
                "Raw data file, only needed for profile correlation analysis." ).withArgName( "file" ).create( 'r' ) );

        options.addOption( OptionBuilder.hasArg().withLongOpt( "scoreFile" ).withDescription(
                "Score file, required for all but profile correlation method" ).withArgName( "file" ).create( 's' ) );

        options.addOption( OptionBuilder.hasArg().withLongOpt( "threshold" ).withDescription(
                "Score threshold, only used for ORA" ).withArgName( "value" ).create( 't' ) );

        options.addOption( OptionBuilder.hasArg().withDescription( "Sets the minimum class size" ).withArgName(
                "integer" ).withLongOpt( "minClassSize" ).create( 'y' ) );

        options.addOption( OptionBuilder.hasArg().withDescription( "Sets the maximum class size" ).withArgName(
                "integer" ).withLongOpt( "maxClassSize" ).create( 'x' ) );

        options.addOption( OptionBuilder.hasArg().withLongOpt( "saveconfig" ).withDescription(
                "Save preferences in the specified file" ).withArgName( "file" ).create( 'S' ) );

        options.addOption( OptionBuilder.withLongOpt( "save" ).withDescription( "Save settings to the selected file" )
                .withArgName( "file" ).create( 'S' ) );

        options.addOption( OptionBuilder.hasArg().withDescription(
                "Multiple test correction method: " + Settings.BONFERONNI + " = Bonferonni FWE, "
                        + Settings.WESTFALLYOUNG + " = Westfall-Young (slow), " + Settings.BENJAMINIHOCHBERG
                        + " = Benjamini-Hochberg FDR [default]" ).withLongOpt( "mtc" ).withArgName( "value" ).create(
                'M' ) );

    }

    private void processOptions() {

        if ( commandLine.hasOption( 'h' ) ) {
            showHelpAndExit();
        }

        String arg;
        if ( commandLine.hasOption( 'C' ) ) {
            arg = commandLine.getOptionValue( 'C' );
            if ( FileTools.testFile( arg ) ) {
                try {
                    settings = new Settings( arg );
                    System.err.println( "Initializing configuration from " + arg );
                } catch ( ConfigurationException e ) {
                    System.err.println( "Invalid config file name (-C " + arg + ")" );
                    showHelpAndExit();
                }
            }

        }

        if ( commandLine.hasOption( 'A' ) ) {
            settings.setAnnotFormat( "Affy CSV" );
        }
        if ( commandLine.hasOption( 'a' ) ) {
            arg = commandLine.getOptionValue( 'a' );
            if ( FileTools.testFile( arg ) )
                settings.setAnnotFile( arg );
            else {
                System.err.println( "Invalid annotation file name (-a " + arg + ")" );
                showHelpAndExit();
            }
        } else if ( !commandLine.hasOption( 'G' ) && !commandLine.hasOption( 'C' ) ) { // using GUI or config file?
            System.err.println( "Annotation file name (-a) is required or must be supplied in config file" );
            showHelpAndExit();
        }

        if ( commandLine.hasOption( 'b' ) ) {
            arg = commandLine.getOptionValue( 'b' );
        }
        if ( commandLine.hasOption( 'c' ) ) {
            arg = commandLine.getOptionValue( 'c' );
            if ( FileTools.testFile( arg ) )
                settings.setClassFile( arg );
            else {
                System.err.println( "Invalid gene set definition file name (-c " + arg + ")" );
                showHelpAndExit();
            }
        } else if ( !commandLine.hasOption( 'G' ) && !commandLine.hasOption( 'C' ) ) { // using GUI config file?
            System.err.println( "Gene set definition file (-c) is required" );
            showHelpAndExit();
        }

        if ( commandLine.hasOption( 'd' ) ) {
            arg = commandLine.getOptionValue( 'd' );
            if ( FileTools.testDir( arg ) )
                settings.setDataDirectory( arg );
            else {
                System.err.println( "Invalid path for data folder (-d " + arg + ")" );
                showHelpAndExit();
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
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Invalid score column (-e " + arg + "), must be an integer" );
                showHelpAndExit();
            }
        }
        if ( commandLine.hasOption( 'f' ) ) {
            arg = commandLine.getOptionValue( 'f' );
            if ( !FileTools.testDir( arg ) ) new File( arg ).mkdir();

            settings.setCustomGeneSetDirectory( arg );
            log.debug( settings.getCustomGeneSetDirectory() );
        }
        if ( commandLine.hasOption( 'g' ) ) {
            arg = commandLine.getOptionValue( 'g' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg == 1 || intarg == 2 )
                    settings.setScoreCol( intarg );
                else {
                    System.err.println( "Gene rep treatment must be either " + "1 (BEST_PVAL) or 2 (MEAN_PVAL) (-g)" );
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Gene rep treatment must be either " + "1 (BEST_PVAL) or 2 (MEAN_PVAL) (-g)" );
                showHelpAndExit();
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
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Iterations must be greater than 0 (-i)" );
                showHelpAndExit();
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
            int intarg = Integer.parseInt( arg );
            if ( intarg == 0 || intarg == 1 || intarg == 2 )
                settings.setRawScoreMethod( intarg );
            else {
                System.err.println( "Raw score method must be set to 0 (MEAN_METHOD), "
                        + "1 (QUANTILE_METHOD), or 2 (MEAN_ABOVE_QUANTILE_METHOD) (-m)" );
                showHelpAndExit();
            }
        }
        if ( commandLine.hasOption( 'M' ) ) {
            arg = commandLine.getOptionValue( 'M' );
            int mtc = Integer.parseInt( arg );
            settings.setMtc( mtc );
        }
        if ( commandLine.hasOption( 'n' ) ) {
            arg = commandLine.getOptionValue( 'n' );
            try {
                int intarg = Integer.parseInt( arg );
                if ( intarg == 0 || intarg == 1 || intarg == 2 || intarg == 3 )
                    settings.setClassScoreMethod( intarg );
                else {
                    System.err.println( "Analysis method must be set to 0 (ORA), 1 (RESAMP), "
                            + "2 (CORR), or 3 (ROC) (-n)" );
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Analysis method must be set to 0 (ORA), 1 (RESAMP), "
                        + "2 (CORR), or 3 (ROC) (-n)" );
                showHelpAndExit();
            }
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
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "Quantile must be between 0 and 100 (-q)" );
                showHelpAndExit();
            }
        }
        if ( commandLine.hasOption( 'r' ) ) {
            arg = commandLine.getOptionValue( 'r' );
            if ( FileTools.testFile( arg ) )
                settings.setRawFile( arg );
            else {
                System.err.println( "Invalid raw file name (-r " + arg + ")" );
                showHelpAndExit();
            }
        }
        if ( commandLine.hasOption( 's' ) ) {
            arg = commandLine.getOptionValue( 's' );
            if ( FileTools.testFile( arg ) )
                settings.setScoreFile( arg );
            else {
                System.err.println( "Invalid score file name (-s " + arg + ")" );
                showHelpAndExit();
            }
        }
        if ( commandLine.hasOption( 't' ) ) {
            arg = commandLine.getOptionValue( 't' );
            try {
                double doublearg = Double.parseDouble( arg );
                if ( doublearg >= 0 && doublearg <= 1 )
                    settings.setPValThreshold( doublearg );
                else {
                    System.err.println( "The p value threshold must be between 0 and 1 (-x)" );
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "The p value threshold must be between 0 and 1 (-x)" );
                showHelpAndExit();
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
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "The maximum class size must be greater than 1 (-x)" );
                showHelpAndExit();
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
                    showHelpAndExit();
                }
            } catch ( NumberFormatException e ) {
                System.err.println( "The minimum class size must be greater than 0 (-y)" );
                showHelpAndExit();
            }
        }
        if ( commandLine.hasOption( 'G' ) ) {
            useCommandLineInterface = false;
        }
    }

    private void showHelpAndExit() {
        printHelp( "ermineJ" );
        System.exit( 0 );
    }
}
