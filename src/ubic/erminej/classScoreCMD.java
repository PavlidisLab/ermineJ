/*
 * The ermineJ project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package ubic.erminej;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.UIManager;

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
 * Main for command line
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class classScoreCMD {
    private static Log log = LogFactory.getLog( classScoreCMD.class );

    protected Settings settings;
    protected StatusViewer statusMessenger;
    protected GONames goData;
    protected GeneAnnotations geneData;
    protected Map geneDataSets;
    protected Map rawDataSets;
    protected Map geneScoreSets;
    private String saveFileName = null;// "C:\\Documents and Settings\\hkl7\\ermineJ.data\\outout.txt";
    private boolean commandline = true;
    private boolean saveAllGenes = false;

    public classScoreCMD() throws IOException {
        settings = new Settings();
        rawDataSets = new HashMap();
        geneDataSets = new HashMap();
        geneScoreSets = new HashMap();
    }

    public classScoreCMD( String[] args ) throws IOException {
        this();
        options( args );
        settings.setDirectories();
        if ( commandline ) {
            initialize();
            try {
                GeneSetPvalRun result = analyze();
                settings.writeAnalysisSettings( saveFileName );
                ResultsPrinter rp = new ResultsPrinter( saveFileName, result, goData, saveAllGenes );
                rp.printResults( true );
            } catch ( Exception e ) {
                statusMessenger.showStatus( "Error During analysis:" + e );
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
    }

    public static void main( String[] args ) {
        try {
            new classScoreCMD( args );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void options( String[] args ) {
        options( args, false );
    }

    private void options( String[] args, boolean configged ) {
        if ( args.length == 0 ) showHelp();
        LongOpt[] longopts = new LongOpt[5];
        longopts[0] = new LongOpt( "help", LongOpt.NO_ARGUMENT, null, 'h' );
        longopts[1] = new LongOpt( "config", LongOpt.REQUIRED_ARGUMENT, null, 'C' );
        longopts[2] = new LongOpt( "gui", LongOpt.NO_ARGUMENT, null, 'G' );
        longopts[3] = new LongOpt( "save", LongOpt.NO_ARGUMENT, null, 'S' );
        longopts[4] = new LongOpt( "mtc", LongOpt.REQUIRED_ARGUMENT, null, 'M' );
        Getopt g = new Getopt( "classScoreCMD", args, "Aa:bc:d:e:f:g:hi:jl:m:n:o:q:r:s:t:x:y:CGS:M:", longopts );
        int c;
        String arg;
        int intarg;
        double doublearg;
        while ( ( c = g.getopt() ) != -1 ) {
            switch ( c ) {
                case 'a': // annotfile
                    arg = g.getOptarg();
                    if ( FileTools.testFile( arg ) )
                        settings.setAnnotFile( arg );
                    else {
                        System.err.println( "Invalid annotation file name (-a " + arg + ")" );
                        showHelp();
                    }
                    break;
                case 'A': // affymetrix format
                    settings.setAnnotFormat( "Affy CSV" );
                    break;
                case 'b': // bigger is better
                    settings.setBigIsBetter( true );
                    break;
                case 'c': // classfile
                    arg = g.getOptarg();
                    if ( FileTools.testFile( arg ) )
                        settings.setClassFile( arg );
                    else {
                        System.err.println( "Invalid class file name (-c " + arg + ")" );
                        showHelp();
                    }
                    break;
                case 'd': // datafolder
                    arg = g.getOptarg();
                    if ( FileTools.testDir( arg ) )
                        settings.setDataDirectory( arg );
                    else {
                        System.err.println( "Invalid path for data folder (-d " + arg + ")" );
                        showHelp();
                    }
                    break;
                case 'e': // scorecol
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg >= 0 )
                            settings.setScoreCol( intarg );
                        else {
                            System.err.println( "Invalid score column (-e " + intarg + ")" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "Invalid score column (-e " + arg + ")" );
                        showHelp();
                    }
                    break;
                case 'f': // classfolder
                    arg = g.getOptarg();
                    if ( !FileTools.testDir( arg ) ) new File( arg ).mkdir();

                    settings.setCustomGeneSetDirectory( arg );
                    log.debug( settings.getCustomGeneSetDirectory() );
                    break;
                case 'g': // gene rep treatment
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg == 1 || intarg == 2 )
                            settings.setScoreCol( intarg );
                        else {
                            System.err.println( "Gene rep treatment must be either "
                                    + "1 (BEST_PVAL) or 2 (MEAN_PVAL) (-g)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "Gene rep treatment must be either "
                                + "1 (BEST_PVAL) or 2 (MEAN_PVAL) (-g)" );
                        showHelp();
                    }
                    break;
                case 'h': // iterations
                    showHelp();
                    break;
                case 'i': // iterations
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg > 0 )
                            settings.setIterations( intarg );
                        else {
                            System.err.println( "Iterations must be greater than 0 (-i)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "Iterations must be greater than 0 (-i)" );
                        showHelp();
                    }
                    break;
                case 'j': // save all genes
                    this.saveAllGenes = true;
                    break;
                case 'l': // dolog
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg == 0 )
                            settings.setDoLog( false );
                        else if ( intarg == 1 )
                            settings.setDoLog( true );
                        else {
                            System.err.println( "Do Log must be set to 0 (false) or 1 (true) (-l)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "Do Log must be set to 0 (false) or 1 (true) (-l)" );
                        showHelp();
                    }
                    break;
                case 'm': // rawscoremethod
                    arg = g.getOptarg();
                    intarg = Integer.parseInt( arg );
                    if ( intarg == 0 || intarg == 1 || intarg == 2 )
                        settings.setRawScoreMethod( intarg );
                    else {
                        System.err.println( "Raw score method must be set to 0 (MEAN_METHOD), "
                                + "1 (QUANTILE_METHOD), or 2 (MEAN_ABOVE_QUANTILE_METHOD) (-m)" );
                        showHelp();
                    }
                    break;
                case 'n': // analysis method
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg == 0 || intarg == 1 || intarg == 2 || intarg == 3 )
                            settings.setClassScoreMethod( intarg );
                        else {
                            System.err.println( "Analysis method must be set to 0 (ORA), 1 (RESAMP), "
                                    + "2 (CORR), or 3 (ROC) (-n)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "Analysis method must be set to 0 (ORA), 1 (RESAMP), "
                                + "2 (CORR), or 3 (ROC) (-n)" );
                        showHelp();
                    }

                    break;
                case 'o': // output file
                    arg = g.getOptarg();
                    saveFileName = arg;
                    break;
                case 'q': // quantile
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg >= 0 && intarg <= 100 )
                            settings.setQuantile( intarg );
                        else {
                            System.err.println( "Quantile must be between 0 and 100 (-q)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "Quantile must be between 0 and 100 (-q)" );
                        showHelp();
                    }

                    break;
                case 'r': // rawfile
                    arg = g.getOptarg();
                    if ( FileTools.testFile( arg ) )
                        settings.setRawFile( arg );
                    else {
                        System.err.println( "Invalid raw file name (-r " + arg + ")" );
                        showHelp();
                    }
                    break;
                case 's': // scorefile
                    arg = g.getOptarg();
                    if ( FileTools.testFile( arg ) )
                        settings.setScoreFile( arg );
                    else {
                        System.err.println( "Invalid score file name (-s " + arg + ")" );
                        showHelp();
                    }
                    break;
                case 't': // pval threshold
                    arg = g.getOptarg();
                    try {
                        doublearg = Double.parseDouble( arg );
                        if ( doublearg >= 0 && doublearg <= 1 )
                            settings.setPValThreshold( doublearg );
                        else {
                            System.err.println( "The p value threshold must be between 0 and 1 (-x)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "The p value threshold must be between 0 and 1 (-x)" );
                        showHelp();
                    }
                    break;
                case 'x': // max class size
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg > 1 )
                            settings.setMaxClassSize( intarg );
                        else {
                            System.err.println( "The maximum class size must be greater than 1 (-x)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "The maximum class size must be greater than 1 (-x)" );
                        showHelp();
                    }
                    break;
                case 'y': // min class size
                    arg = g.getOptarg();
                    try {
                        intarg = Integer.parseInt( arg );
                        if ( intarg > 0 )
                            settings.setMinClassSize( intarg );
                        else {
                            System.err.println( "The minimum class size must be greater than 0 (-y)" );
                            showHelp();
                        }
                    } catch ( NumberFormatException e ) {
                        System.err.println( "The minimum class size must be greater than 0 (-y)" );
                        showHelp();
                    }
                    break;
                case 'C': // configfile
                    if ( !configged ) {
                        arg = g.getOptarg();
                        if ( FileTools.testFile( arg ) ) {
                            try {
                                settings = new Settings( arg );
                            } catch ( ConfigurationException e ) {
                                e.printStackTrace();
                            }
                            options( args, true );
                            break;
                        }

                        System.err.println( "Invalid config file name (-C " + arg + ")" );
                        showHelp();

                    }
                    break;
                case 'M': // multiple test correction
                    arg = g.getOptarg();
                    int mtc = Integer.parseInt( arg );
                    settings.setMtc( mtc );
                    System.err.println( "Got " + mtc );
                    break;
                case 'G': // GUI
                    commandline = false;
                    break;
                case 'S': // save pref file
                    arg = g.getOptarg();
                    settings.setPrefFile( arg );
                    break;
                case '?':
                    showHelp();
                default:
                    showHelp();
            }
        }
        // try {
        // settings.writePrefs();
        // } catch ( ConfigurationException ex ) {
        // System.err.print( "Could not write preferences to a file." );
        // }
    }

    private void showHelp() {
        System.out.print( "OPTIONS\n" + "\tThe following options are supported:\n\n"
                + "\t-a file ...\n\t\tSets the annotation file to be used [required].\n\n"
                + "\t-A ...\n\t\tAnnotation file is in Affymetrix format.\n\n"
                + "\t-c file ...\n\t\tSets the class file to be used (e.g., go_200406-termdb.xml) [required] \n\n"
                + "\t-d dir ...\n\t\tSets the data folder to be used.\n\n"
                + "\t-e int ...\n\t\tSets the column in the score file to be used for scores.\n\n" + "\t-f die ...\n"
                + "\t\tSets the class folder to be used.\n\n"
                + "\t-g int ...\n\t\tSets the gene replicant treatment:  " + Settings.BEST_PVAL
                + " (best gene score used) or  " + Settings.MEAN_PVAL + " (mean gene score used).\n\n"
                + "\t-b Sets 'big is better' option for gene scores (default is " + settings.getBigIsBetter()
                + ").\n\n" + "\t-h or --help\n\t\tShows help.\n\n"
                + "\t-i int ...\n\t\tSets the number of iterations.\n\n"
                + "\t-j\n\t\tOutput should include gene symbols for all gene sets (default=don't include symbols).\n\n"
                + "\t-l {0/1} ...\n" + "\t\tSets whether or not to take logs (default is " + settings.getDoLog()
                + ").\n\n" + "\t-m int ...\n" + "\t\tSets the raw score method:  " + Settings.MEAN_METHOD
                + " (mean),  " + Settings.QUANTILE_METHOD + " (quantile), or  " + Settings.MEAN_ABOVE_QUANTILE_METHOD
                + " (mean above quantile).\n\n" + "\t-n int ...\n" + "\t\tSets the analysis method:  " + Settings.ORA
                + " (ORA),  " + Settings.RESAMP + " (resampling of gene scores),  " + Settings.CORR
                + " (profile correlation),  " + Settings.ROC + " (ROC)\n\n"
                + "\t-o file ...\n\t\tSets the output file.\n\n" + "\t-q int ...\n\t\tSets the quantile.\n\n"
                + "\t-r file ...\n\t\tSets the raw file to be used.[required for correlation method]\n\n"
                + "\t-s file ...\n\t\tSets the score file to be used.[required for gene score-based methods]\n\n"
                + "\t-t double ...\n\t\tSets the pvalue threshold.\n\n"
                + "\t-x maximum class size ...\n\t\tSets the maximum class size.\n\n"
                + "\t-y minimum class size ...\n\t\tSets the minimum class size.\n\n"
                + "\t-M method or --mtc method\n\t\tSets the multiple test correction method: " + Settings.BONFERONNI
                + " = Bonferonni,  " + Settings.WESTFALLYOUNG + " = Westfall-Young (slow),  "
                + Settings.BENJAMINIHOCHBERG + " =  Benjamini-Hochberg (default)\n\n"
                + "\t-C file ... or --config file ...\n\t\tSets the configuration file to be used.\n\n"
                + "\t-G or --gui\n" + "\t\tLaunch the GUI.\n\n"
                + "\t-S file ... or --save file ...\n\t\tSave preferences in the specified file.\n\n" );
        System.exit( 0 );
    }

    protected void initialize() {
        try {
            statusMessenger = new StatusStderr();
            statusMessenger.showStatus( "Reading GO descriptions from " + settings.getClassFile() );

            goData = new GONames( settings.getClassFile() );

            statusMessenger.showStatus( "Reading gene annotations from " + settings.getAnnotFile() );
            geneData = new GeneAnnotations( settings.getAnnotFile(), statusMessenger, goData );
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
        Collection userOverwrittenGeneSets = loader.loadUserGeneSets( this.goData, this.statusMessenger );

        statusMessenger.showStatus( "Done with initialization." );
    }

    /**
     * @throws IllegalArgumentException
     * @return
     * @throws IOException
     */
    protected GeneSetPvalRun analyze() throws IOException {
        DoubleMatrixNamed rawData = null;
        if ( settings.getClassScoreMethod() == Settings.CORR ) {
            if ( rawDataSets.containsKey( settings.getRawDataFileName() ) ) {
                statusMessenger.showStatus( "Raw data are in memory" );
                rawData = ( DoubleMatrixNamed ) rawDataSets.get( settings.getRawDataFileName() );
            } else {
                statusMessenger.showStatus( "Reading raw data from file " + settings.getRawDataFileName() );
                DoubleMatrixReader r = new DoubleMatrixReader();
                rawData = ( DoubleMatrixNamed ) r.read( settings.getRawDataFileName() );
                rawDataSets.put( settings.getRawDataFileName(), rawData );
            }
        }

        GeneScores geneScores;
        if ( geneScoreSets.containsKey( settings.getScoreFile() ) ) {
            statusMessenger.showStatus( "Gene Scores are in memory" );
            geneScores = ( GeneScores ) geneScoreSets.get( settings.getScoreFile() );
        } else {
            statusMessenger.showStatus( "Reading gene scores from file " + settings.getScoreFile() );
            geneScores = new GeneScores( settings.getScoreFile(), settings, statusMessenger, geneData );
            geneScoreSets.put( settings.getScoreFile(), geneScores );
        }

        if ( !settings.getScoreFile().equals( "" ) && geneScores == null ) {
            statusMessenger.showStatus( "Didn't get geneScores" );
        }

        Set activeProbes = null;
        if ( rawData != null && geneScores != null ) { // favor the geneScores
            // list.
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        } else if ( rawData == null && geneScores != null ) {
            activeProbes = geneScores.getProbeToScoreMap().keySet();
        } else if ( rawData != null && geneScores == null ) {
            activeProbes = new HashSet( rawData.getRowNames() );
        }

        boolean needToMakeNewGeneData = true;
        for ( Iterator it = geneDataSets.keySet().iterator(); it.hasNext(); ) {
            GeneAnnotations test = ( GeneAnnotations ) geneDataSets.get( it.next() );

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
}
