package classScore;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.UIManager;

import org.xml.sax.SAXException;

import classScore.data.GeneScoreReader;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.io.reader.DoubleMatrixReader;
import baseCode.util.FileTools;
import baseCode.util.StatusStderr;
import baseCode.util.StatusViewer;

/**
 * Main for command line
 * <p>
 * Copyright (c) 2003 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */

public class classScoreCMD {

	private Settings settings;

	private StatusViewer statusMessenger;

	private GONames goData;

	private GeneAnnotations geneData;

	private LinkedList results = new LinkedList();

	private Map geneDataSets;

	private Map rawDataSets;

	private Map geneScoreSets;

	private String saveFileName = null;//"C:\\Documents and Settings\\hkl7\\ermineJ.data\\outout.txt";

	private boolean commandline=true;
	
	public classScoreCMD(String[] args) {
		settings = new Settings();
		options(args);
		if(commandline) {
			initialize();
		try {
			GeneSetPvalRun result = analyze();
			ResultsPrinter rp = new ResultsPrinter(saveFileName, result, goData);
			rp.printResults(true);
		} catch (Exception e) {
			statusMessenger.setStatus("Error During analysis" + e);
		}}
		else
		{
			try {
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			new classScoreGUI();
		}
	}

	public static void main(String[] args) {
		new classScoreCMD(args);
	}

	private void options(String[] args) {
		options(args,false);
	}
	
	private void options(String[] args, boolean configged) {
		if(args.length==0)
			showHelp();
		LongOpt[] longopts = new LongOpt[4];
		StringBuffer sb = new StringBuffer();
		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longopts[1] = new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'C');
		longopts[2] = new LongOpt("gui", LongOpt.NO_ARGUMENT, null, 'G');		
		longopts[3] = new LongOpt("save", LongOpt.NO_ARGUMENT, null, 'S');
		Getopt g = new Getopt("classScoreCMD", args,
				"a:c:d:e:f:g:hi:l:m:n:o:q:r:s:t:x:y:CGS:",longopts);
		int c;
		String arg;
		int intarg;
		double doublearg;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'a': //annotfile
				arg = g.getOptarg();
				if (FileTools.testFile(arg))
					settings.setAnnotFile(arg);
				else {
					System.err.println("Invalid annotation file name (-a)");
					showHelp();
				}
				break;
			case 'c': //classfile
				arg = g.getOptarg();
				if (FileTools.testFile(arg))
					settings.setClassFile(arg);
				else {
					System.err.println("Invalid class file name (-c)");
					showHelp();
				}
				break;
			case 'd': //datafolder
				arg = g.getOptarg();
				if (FileTools.testDir(arg))
					settings.setDataFolder(arg);
				else {
					System.err.println("Invalid path for data folder (-d)");
					showHelp();
				}
				break;
			case 'e': //scorecol
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg >= 0)
						settings.setScorecol(intarg);
					else {
						System.err.println("Invalid score column (-e)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err.println("Invalid score column (-e)");
					showHelp();
				}
				break;
			case 'f': //classfolder
				arg = g.getOptarg();
				if (FileTools.testDir(arg))
					settings.setClassFolder(arg);
				else {
					System.err.println("Invalid path for class folder (-f)");
					showHelp();
				}
				break;
			case 'g': //gene rep treatment
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg == 1 || intarg == 2)
						settings.setScorecol(intarg);
					else {
						System.err.println("Gene rep treatment must be either "
								+ "1 (BEST_PVAL) or 2 (MEAN_PVAL) (-g)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err.println("Gene rep treatment must be either "
							+ "1 (BEST_PVAL) or 2 (MEAN_PVAL) (-g)");
					showHelp();
				}
				break;
			case 'h': //iterations
				showHelp();
				break;
			case 'i': //iterations
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg > 0)
						settings.setIterations(intarg);
					else {
						System.err
								.println("Iterations must be greater than 0 (-i)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err
							.println("Iterations must be greater than 0 (-i)");
					showHelp();
				}
				break;
			case 'l': //dolog
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg == 0)
						settings.setDoLog(false);
					else if (intarg == 1)
						settings.setDoLog(true);
					else {
						System.err
								.println("Do Log must be set to 0 (false) or 1 (true) (-l)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err
							.println("Do Log must be set to 0 (false) or 1 (true) (-l)");
					showHelp();
				}
				break;
			case 'm': //rawscoremethod
				arg = g.getOptarg();
				intarg = Integer.parseInt(arg);
				if (intarg == 0 || intarg == 1 || intarg == 2)
					settings.setRawScoreMethod(intarg);
				else {
					System.err
							.println("Raw score method must be set to 0 (MEAN_METHOD), "
									+ "1 (QUANTILE_METHOD), or 2 (MEAN_ABOVE_QUANTILE_METHOD) (-m)");
					showHelp();
				}
				break;
			case 'n': //analysis method
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg == 0 || intarg == 1 || intarg == 2)
						settings.setAnalysisMethod(intarg);
					else {
						System.err
								.println("Analysis method must be set to 0 (ORA), 1 (RESAMP), "
										+ "2 (CORR), or 3 (ROC) (-n)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err
							.println("Analysis method must be set to 0 (ORA), 1 (RESAMP), "
									+ "2 (CORR), or 3 (ROC) (-n)");
					showHelp();
				}

				break;
			case 'o': //output file
				arg = g.getOptarg();
				saveFileName=arg;
				break;
			case 'q': //quantile
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg >= 0 && intarg <= 100)
						settings.setQuantile(intarg);
					else {
						System.err
								.println("Quantile must be between 0 and 100 (-q)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err
							.println("Quantile must be between 0 and 100 (-q)");
					showHelp();
				}

				break;
			case 'r': //rawfile
				arg = g.getOptarg();
				if (FileTools.testFile(arg))
					settings.setRawFile(arg);
				else {
					System.err.println("Invalid raw file name (-r)");
					showHelp();
				}
				break;
			case 's': //scorefile
				arg = g.getOptarg();
				if (FileTools.testFile(arg))
					settings.setScoreFile(arg);
				else {
					System.err.println("Invalid score file name (-s)");
					showHelp();
				}
				break;
			case 't': //pval threshold
				arg = g.getOptarg();
				try {
					doublearg = Double.parseDouble(arg);
					if (doublearg >= 0 && doublearg <= 1)
						settings.setPValThreshold(doublearg);
					else {
						System.err
								.println("The p value threshold must be between 0 and 1 (-x)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err
							.println("The p value threshold must be between 0 and 1 (-x)");
					showHelp();
				}
				break;
			case 'x': //max class size
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg > 1)
						settings.setMaxClassSize(intarg);
					else {
						System.err
								.println("The maximum class size must be greater than 1 (-x)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err
							.println("The maximum class size must be greater than 1 (-x)");
					showHelp();
				}
				break;
			case 'y': //min class size
				arg = g.getOptarg();
				try {
					intarg = Integer.parseInt(arg);
					if (intarg > 0)
						settings.setMinClassSize(intarg);
					else {
						System.err
								.println("The minimum class size must be greater than 0 (-y)");
						showHelp();
					}
				} catch (NumberFormatException e) {
					System.err
							.println("The minimum class size must be greater than 0 (-y)");
					showHelp();
				}
				break;
			case 'C': //configfile
				if(!configged)
				{
					arg = g.getOptarg();
					if (FileTools.testFile(arg)) {
						settings=new Settings(arg);
						options(args,true);
						break;
					}
					else {
						System.err.println("Invalid config file name (-C)");
						showHelp();
					}
				}
				break;
			case 'G': //GUI
				commandline=false;
				break;
			case 'S': //save pref file
				arg = g.getOptarg();
				settings.setPrefFile(arg);
				break;
			case '?':
				showHelp();
			default:
				showHelp();
			}
		}
		try {
			settings.writePrefs();
		} catch (IOException ex) {
			System.err.print("Could not write preferences to a file.");
		}
	}

	private void showHelp()
	{
		System.out.print("OPTIONS\n" +
				"\tThe following options are supported:\n\n" +
				"\t-a file ...\n" +
				"\t\tSets the annotation file to be used.\n\n" +
				"\t-c file ...\n" +
				"\t\tSets the class file to be used.\n\n" +
				"\t-d dir ...\n" +
				"\t\tSets the data folder to be used.\n\n" +
				"\t-e int ...\n" +
				"\t\tSets the column in the score file to be used for scores.\n\n" +
				"\t-f die ...\n" +
				"\t\tSets the class folder to be used.\n\n" +
				"\t-g int ...\n" +
				"\t\tSets the gene replicant treatment: 1 (BEST_PVAL) or 2 (MEAN_PVAL).\n\n" +
				"\t-h or --help\n"+
				"\t\tShows help.\n\n" +
				"\t-i int ...\n" +
				"\t\tSets the number of iterations.\n\n" +
				"\t-l {0/1} ...\n" +
				"\t\tSets whether or not to take logs.\n\n" +
				"\t-m int ...\n" +
				"\t\tSets the raw score method: 0 (MEAN_METHOD), 1 (QUANTILE_METHOD), or 2 (MEAN_ABOVE_QUANTILE_METHOD).\n\n" +
				"\t-n int ...\n" +
				"\t\tSets the analysis method: 0 (ORA), 1 (RESAMPLING), 2 (CORRELATION), or 3 (ROC)\n\n" +
				"\t-o file ...\n" +
				"\t\tSets the output file.\n\n" +
				"\t-q int ...\n" +
				"\t\tSets the quantile.\n\n" +
				"\t-r file ...\n" +
				"\t\tSets the raw file to be used.\n\n" +
				"\t-s file ...\n" +
				"\t\tSets the score file to be used.\n\n" +
				"\t-t double ...\n" +
				"\t\tSets the pvalue threshold.\n\n" +
				"\t-x maximum class size ...\n" +
				"\t\tSets the maximum class size.\n\n" +
				"\t-y minimum class size ...\n" +
				"\t\tSets the minimum class size.\n\n" +
				"\t-C file ... or --config file ...\n" +
				"\t\tSets the configuration file to be used.\n\n" +
				"\t-G or --gui\n" +
				"\t\tLaunch the GUI.\n\n" +
				"\t-S file ... or --save file ...\n" +
				"\t\tSave preferences in the specified file.\n\n");
		System.exit(0);
	}
	
	private void initialize() {
		try {
			statusMessenger = new StatusStderr();
			rawDataSets = new HashMap();
			geneDataSets = new HashMap();
			geneScoreSets = new HashMap();

			statusMessenger.setStatus("Reading GO descriptions "
					+ settings.getClassFile());
			goData = new GONames(settings.getClassFile()); // parse go name file

			statusMessenger.setStatus("Reading gene annotations from "
					+ settings.getAnnotFile());
			geneData = new GeneAnnotations(settings.getAnnotFile(),
					statusMessenger);
			statusMessenger.setStatus("Initializing gene class mapping");
			geneDataSets.put(new Integer("original".hashCode()), geneData);
			statusMessenger.setStatus("Done with setup");
			statusMessenger.setStatus("Ready.");
		} catch (IOException e) {
			statusMessenger
					.setStatus("File reading or writing error during initialization: "
							+ e.getMessage()
							+ "\nIf this problem persists, please contact the software developer. "
							+ "\nPress OK to quit.");
			System.exit(1);
		} catch (SAXException e) {
			statusMessenger
					.setStatus("Gene Ontology file format is incorrect. "
							+ "\nPlease check that it is a valid XML file. "
							+ "\nIf this problem persists, please contact the software developer. "
							+ "\nPress OK to quit.");
			System.exit(1);
		}
		statusMessenger.setStatus("Done with initialization.");
	}

	GeneSetPvalRun analyze() throws IOException {
		DenseDoubleMatrix2DNamed rawData = null;
		if (settings.getAnalysisMethod() == Settings.CORR) {
			if (rawDataSets.containsKey(settings.getRawFile())) {
				statusMessenger.setStatus("Raw data are in memory");
				rawData = (DenseDoubleMatrix2DNamed) rawDataSets.get(settings
						.getRawFile());
			} else {
				statusMessenger.setStatus("Reading raw data from file "
						+ settings.getRawFile());
				DoubleMatrixReader r = new DoubleMatrixReader();
				rawData = (DenseDoubleMatrix2DNamed) r.read(settings
						.getRawFile());
				rawDataSets.put(settings.getRawFile(), rawData);
			}
		}

		GeneScoreReader geneScores;
		if (geneScoreSets.containsKey(settings.getScoreFile())) {
			statusMessenger.setStatus("Gene Scores are in memory");
			geneScores = (GeneScoreReader) geneScoreSets.get(settings
					.getScoreFile());
		} else {
			statusMessenger.setStatus("Reading gene scores from file "
					+ settings.getScoreFile());
			geneScores = new GeneScoreReader(settings.getScoreFile(), settings,
					statusMessenger, geneData.getGeneToProbeList());
			geneScoreSets.put(settings.getScoreFile(), geneScores);
		}

		if (!settings.getScoreFile().equals("") && geneScores == null) {
			statusMessenger.setStatus("Didn't get geneScores");
		}

		// todo need logic to choose which source of probes to use.
		Set activeProbes = null;
		if (rawData != null && geneScores != null) { // favor the geneScores
			// list.
			activeProbes = geneScores.getProbeToPvalMap().keySet();
		} else if (rawData == null && geneScores != null) {
			activeProbes = geneScores.getProbeToPvalMap().keySet();
		} else if (rawData != null && geneScores == null) {
			activeProbes = new HashSet(rawData.getRowNames());
		}

		boolean needToMakeNewGeneData = true;
		for (Iterator it = geneDataSets.keySet().iterator(); it.hasNext();) {
			GeneAnnotations test = (GeneAnnotations) geneDataSets
					.get(it.next());

			if (test.getProbeToGeneMap().keySet().equals(activeProbes)) {
				geneData = test;
				needToMakeNewGeneData = false;
				break;
			}

		}

		if (needToMakeNewGeneData) {
			geneData = new GeneAnnotations(geneData, activeProbes);
			geneDataSets.put(new Integer(geneData.hashCode()), geneData);
		}

		/* do work */
		statusMessenger.setStatus("Starting analysis...");
		GeneSetPvalRun runResult = new GeneSetPvalRun(activeProbes, settings,
				geneData, rawData, goData, geneScores, statusMessenger,
				"command");
		return runResult;
	}
}

