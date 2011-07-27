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
package ubic.erminej.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.util.BrowserLauncher;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.AnalysisThread;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneAnnotationParser;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSetTerms;
import ubic.erminej.data.Probe;
import ubic.erminej.data.UserDefinedGeneSetManager;
import ubic.erminej.gui.analysis.AnalysisWizard;
import ubic.erminej.gui.analysis.MultiFuncDiagWindow;
import ubic.erminej.gui.geneset.table.GeneSetTablePanel;
import ubic.erminej.gui.geneset.tree.GeneSetTreePanel;
import ubic.erminej.gui.geneset.wiz.GeneSetWizard;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.ScrollingTextAreaDialog;

/**
 * The main ErmineJ application GUI Sframe.
 * 
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @author Will Braynen
 * @version $Id$
 */
public class MainFrame extends JFrame {

    private static final String START_CARD = "START";

    private static final String TABS_CARD = "TABS";

    private static final String PROGRESS_CARD = "PROGRESS";

    public final static String RESOURCE_LOCATION = "/ubic/erminej/";

    private static Log log = LogFactory.getLog( MainFrame.class.getName() );

    private static final int START_HEIGHT = 330;

    private static final int START_WIDTH = 830;

    private static final int STARTING_OVERALL_WIDTH = 830;

    private static final String MAINWINDOWHEIGHT = "mainview.WindowHeight";
    private static final String MAINWINDOWWIDTH = "mainview.WindowWidth";
    private static final String MAINWINDOWPOSITIONX = "mainview.WindowXPosition";
    private static final String MAINWINDOWPOSITIONY = "mainview.WindowYPosition";

    private GeneAnnotations geneData = null; // original.
    private Map<String, GeneScores> geneScoreSets = new HashMap<String, GeneScores>();

    private Map<String, DoubleMatrix<Probe, String>> rawDataSets = new HashMap<String, DoubleMatrix<Probe, String>>();
    private List<GeneSetPvalRun> results = new LinkedList<GeneSetPvalRun>();
    private Settings settings;

    private int currentResultSet = -1;
    private AnalysisThread athread;

    private StatusViewer statusMessenger;
    private GeneSetTablePanel tablePanel;
    private GeneSetTreePanel treePanel;

    private FindDialog findByGeneDialog = null;
    private FindDialog findByNameDialog = null;

    private JMenu analysisMenu = new JMenu();
    private JMenu diagnosticsMenu = new JMenu();
    private JMenu classMenu = new JMenu();
    private final JFileChooser fc = new JFileChooser();
    private JMenu fileMenu = new JMenu();
    private JMenu helpMenu = new JMenu();

    private JMenu runViewMenu = new JMenu();

    private JTabbedPane tabs = new JTabbedPane();
    private JProgressBar progressBar = new JProgressBar();

    private JMenuItem defineClassMenuItem = new JMenuItem();
    private JMenuItem cancelAnalysisMenuItem = new JMenuItem();
    private JMenuItem loadAnalysisMenuItem = new JMenuItem();
    private JMenuItem modClassMenuItem = new JMenuItem();
    private JMenuItem runAnalysisMenuItem = new JMenuItem();
    private JMenuItem saveAnalysisMenuItem = new JMenuItem();

    /**
     * @throws IOException
     */
    public MainFrame() throws IOException {
        settings = new Settings();
        jbInit();

    }

    public MainFrame( Settings settings ) {
        this.settings = settings;
        jbInit();
    }

    public void addedNewGeneSet( GeneSet newGeneSet ) {
        tablePanel.addedGeneSet( newGeneSet.getTerm() );
        treePanel.addedGeneSet( newGeneSet.getTerm() );
    }

    public void disableMenusForLoad() {
        fileMenu.setEnabled( false );
        classMenu.setEnabled( false );
        analysisMenu.setEnabled( false );
        helpMenu.setEnabled( false );
    }

    /**
     * @param selectedTerms
     */
    public void filter( Collection<GeneSetTerm> selectedTerms ) {
        this.tablePanel.filter( selectedTerms );
        // this.treePanel.filter( selectedTerms );
    }

    /**
     * @param classID
     */
    public void findGeneSetInTree( GeneSetTerm classID ) {
        if ( !treePanel.expandToGeneSet( classID ) ) return;
        this.tabs.setSelectedIndex( 1 );
        this.maybeEnableSomeMenus();
    }

    /**
     * @return
     */
    public int getCurrentResultSet() {
        return this.currentResultSet;
    }

    /**
     * Get the original, "fresh" gene annotation data.
     * 
     * @return
     */
    public GeneAnnotations getOriginalGeneData() {
        return geneData;
    }

    /**
     * @return
     */
    public JMenu getRunViewMenu() {
        return this.runViewMenu;
    }

    public Settings getSettings() {
        return settings;
    }

    public StatusViewer getStatusMessenger() {
        return statusMessenger;
    }

    /**
     * @return Returns the tablePanel.
     */
    public GeneSetTablePanel getTablePanel() {
        return tablePanel;
    }

    /**
     * @return Returns the treePanel.
     */
    public GeneSetTreePanel getTreePanel() {
        return this.treePanel;
    }

    public void loadAnalysis( String loadFile ) {
        disableMenusForAnalysis();
        Settings loadSettings;
        try {
            loadSettings = new Settings( loadFile );
        } catch ( ConfigurationException e ) {
            GuiUtil.error( "There was a problem loading the settings from the results file: " + e.getMessage() );
            return;
        }
        if ( !checkValid( loadSettings ) ) {
            GuiUtil
                    .error( "There was a problem loading the analysis.\nFiles referred to in the analysis may have been moved or deleted." );
            return;
        }

        this.athread = new AnalysisThread( loadSettings, statusMessenger, geneData, rawDataSets, geneScoreSets,
                loadFile );
        athread.run();
        log.debug( "Waiting" );
        addResult( athread.getLatestResults() );
        log.debug( "done" );
        enableMenusForAnalysis();
    }

    /**
     * @param runIndex
     */
    public void setCurrentResultSet( int runIndex ) {
        this.currentResultSet = runIndex;
        treePanel.fireResultsChanged();
    }

    /**
     * @param resultSetName
     */
    public void setCurrentResultSet( String resultSetName ) {
        for ( int i = 0; i < results.size(); i++ ) {
            GeneSetPvalRun element = results.get( i );
            if ( element.getName().equals( resultSetName ) ) {
                this.setCurrentResultSet( i );
            }
        }
    }

    public void setSettings( Settings settings ) {
        this.settings = settings;
    }

    /**
     * @param message
     * @param e Throwable
     */
    public void showError( String message, Throwable e ) {
        statusMessenger.showError( message, e );
    }

    public void startAnalysis( Settings runSettings ) {
        disableMenusForAnalysis();
        this.athread = new AnalysisThread( runSettings, statusMessenger, geneData, rawDataSets, geneScoreSets );
        log.debug( "Starting analysis thread" );
        try {
            athread.start();
        } catch ( Exception e ) {
            GuiUtil.error( "There was an unexpected error during analysis.\n"
                    + "See the log file for details.\nThe summary message was:\n" + e.getMessage() );
            enableMenusForAnalysis();
        }
        log.debug( "Waiting..." );

        GeneSetPvalRun latestResult = athread.getLatestResults();
        checkForReasonableResults( latestResult );
        if ( latestResult != null ) addResult( latestResult );

        enableMenusForAnalysis();
    }

    /**
     * 
     */
    public void updateRunViewMenu() {
        log.debug( "Updating runViewMenu" );
        runViewMenu.removeAll();
        for ( Iterator<GeneSetPvalRun> iter = this.results.iterator(); iter.hasNext(); ) {
            GeneSetPvalRun resultSet = iter.next();
            String name = resultSet.getName();
            log.debug( "Adding " + name );
            JMenuItem newSet = new JMenuItem();
            newSet.setIcon( new ImageIcon( this.getClass().getResource( RESOURCE_LOCATION + "noCheckBox.gif" ) ) );
            newSet.addActionListener( new RunSet_Choose_ActionAdapter( this ) );
            newSet.setText( name );
            this.runViewMenu.add( newSet );
        }
        if ( runViewMenu.getItemCount() > 0 ) {
            runViewMenu.getItem( runViewMenu.getItemCount() - 1 ).setIcon(
                    new ImageIcon( this.getClass().getResource( RESOURCE_LOCATION + "checkBox.gif" ) ) );
        }
        runViewMenu.revalidate();
    }

    private void addResult( GeneSetPvalRun result ) {
        if ( result == null || result.getResults().size() == 0 ) return;
        result.setName( "Run " + ( results.size() + 1 ) );
        results.add( result );
        this.updateRunViewMenu();
        tablePanel.addRun();
        treePanel.addRun();
        athread = null;
    }

    /**
     * Check whether a file exists, and if not, prompt the user to enter one. The path is returned.
     * 
     * @param file
     * @return If the user doesn't locate the file, return null, otherwise the path to the file.
     */
    private String checkFile( String file ) {
        if ( StringUtils.isBlank( file ) ) return null;
        log.info( "Seeking file '" + file + "'" );
        if ( !FileTools.testFile( file ) ) {
            GuiUtil.error( "A file referred to in the results\n(" + file
                    + ")\nwas not found at the listed path.\nIt may have been moved.\nYou will be prompted to"
                    + " enter the location." );
            fc.setDialogTitle( "Please locate " + file );
            fc.setDialogType( JFileChooser.OPEN_DIALOG );
            fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
            int result = fc.showOpenDialog( this );
            if ( result == JFileChooser.APPROVE_OPTION ) {
                File f = fc.getSelectedFile();
                return f.getAbsolutePath();
            }
            return null;
        }
        return file;
    }

    /**
     * 
     */
    private void checkForReasonableResults( GeneSetPvalRun results1 ) {
        if ( !athread.isFinishedNormally() ) return;
        int numZeroPvalues = 0;
        if ( results1 == null || results1.getResults() == null ) {
            GuiUtil.error( "There was an error during analysis - there were no valid results. Please check the logs." );
            return;
        }
        int numPvalues = results1.getResults().size();
        int numUnityPvalue = 0;
        for ( Iterator<GeneSetResult> itr = results1.getResults().values().iterator(); itr.hasNext(); ) {
            GeneSetResult result = itr.next();
            double p = result.getPvalue();
            if ( p == 0 ) {
                numZeroPvalues++;
            } else if ( p == 1.0 ) {
                numUnityPvalue++;
            }
        }

        if ( numZeroPvalues == numPvalues || numUnityPvalue == numPvalues ) {
            GuiUtil.error( "The results indicate that you may need to adjust your\nanalysis settings.\n"
                    + "For example, make sure your setting for 'larger scores are better' is correct." );
        }

    }

    /**
     * @param loadSettings
     */
    private boolean checkValid( Settings loadSettings ) {

        String file;

        file = checkFile( loadSettings.getRawDataFileName() );
        // if ( file == null ) {
        // return false;
        // }
        loadSettings.setRawFile( file );

        file = checkFile( loadSettings.getScoreFile() );
        // if ( file == null ) {
        // return false;
        // }
        loadSettings.setScoreFile( file );

        return true;
    }

    private void disableMenusForAnalysis() {
        defineClassMenuItem.setEnabled( false );
        modClassMenuItem.setEnabled( false );
        runAnalysisMenuItem.setEnabled( false );
        loadAnalysisMenuItem.setEnabled( false );
        saveAnalysisMenuItem.setEnabled( false );
        cancelAnalysisMenuItem.setEnabled( true );
    }

    private void enableMenusOnStart() {
        fileMenu.setEnabled( true );
        classMenu.setEnabled( true );
        analysisMenu.setEnabled( true );
        diagnosticsMenu.setEnabled( true );
        runViewMenu.setEnabled( false );
        helpMenu.setEnabled( true );
    }

    private void initialize( final JPanel cards ) {
        ( ( CardLayout ) cards.getLayout() ).show( cards, PROGRESS_CARD );

        SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    readDataFilesForStartup();
                    treePanel.initialize( geneData );
                    tablePanel.initialize( geneData );
                    ( ( CardLayout ) cards.getLayout() ).show( cards, TABS_CARD );
                    statusMessenger.showStatus( "Ready." );
                    enableMenusOnStart();
                    statusMessenger.showStatus( "Done with initialization." );
                } catch ( Exception e ) {
                    GuiUtil.error( "Error during initialization: " + e.getMessage() );
                    log.error( e, e );
                }
                return null;
            }

        };

        r.execute();

    }

    /**
     *  
     */
    private void jbInit() {

        // contained within this.contentPane().
        this.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                writePrefs();
            }
        } );

        this.setDefaultCloseOperation( EXIT_ON_CLOSE );

        this.setSize( new Dimension( 886, 450 ) );

        this.readPrefs();

        this.setTitle( "ErmineJ" );
        this.setIconImage( new ImageIcon( this.getClass().getResource( RESOURCE_LOCATION + "logoIcon64.gif" ) )
                .getImage() );
        this.getContentPane().setLayout( new BorderLayout() );
        this.getContentPane().setPreferredSize( new Dimension( 1000, 600 ) );
        // this.getContentPane().setInputVerifier( null );

        setupMenus();

        disableMenusForLoad();

        final JPanel cards = new JPanel( new CardLayout() );
        this.getContentPane().add( cards, BorderLayout.CENTER );

        final StartupPanel startupPanel = new StartupPanel( settings );
        cards.add( startupPanel, START_CARD );

        JPanel progressPanel = setupProgressPanel();
        cards.add( progressPanel, PROGRESS_CARD );

        setupMainPanels();
        cards.add( tabs, TABS_CARD );

        setupStatusBar();// have to do afeter the main panels are setup.

        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                initialize( cards );
            }
        };

        startupPanel.addActionListener( actionListener );

        ( ( CardLayout ) cards.getLayout() ).show( cards, START_CARD );

        setIconImage( new ImageIcon( this.getClass().getResource( "/ubic/erminej/logoIcon64.gif" ) ).getImage() );

        this.statusMessenger.showStatus( "Waiting for input" );
    }

    /**
     * 
     */
    private void multifunctionalityDiagnostics() {
        /*
         * Should allow user to pick?
         */
        GeneAnnotations ga = this.geneData;
        GeneScores gs = null;

        if ( this.getCurrentResultSet() >= 0 ) {
            ga = this.results.get( this.getCurrentResultSet() ).getGeneData();
            gs = this.results.get(this.getCurrentResultSet()).getGeneScores();
        }

        MultiFuncDiagWindow w = new MultiFuncDiagWindow( ga, gs  );
        w.setSize( new Dimension( 500, 500 ) );
        GuiUtil.centerContainer( w );
        w.setVisible( true );
    }

    /**
     * Input the GO and annotation files.
     */
    private void readDataFilesForStartup() {

        updateProgress( 10 );

        statusMessenger.showStatus( "Reading GO tree from: " + settings.getClassFile() );
        assert settings.getClassFile() != null;
        GeneSetTerms goData = null;
        try {
            goData = new GeneSetTerms( settings.getClassFile() );
        } catch ( SAXException e ) {
            GuiUtil.error( "Gene Ontology file format is incorrect. "
                    + "\nPlease check that it is a valid GO XML file." );
            log.error( e, e );
            return;
        } catch ( IOException e ) {
            GuiUtil.error( "Error during GO initialization: " + e.getMessage() );
            log.error( e, e );
            return;
        }

        updateProgress( 30 );

        statusMessenger.showStatus( "Reading gene annotations from " + settings.getAnnotFile() );

        try {
            GeneAnnotationParser parser = new GeneAnnotationParser( goData, statusMessenger );

            geneData = parser.read( settings.getAnnotFile(), settings.getAnnotFormat() );

        } catch ( IOException e ) {
            GuiUtil.error( "Gene annotation reading error during initialization: " + e.getMessage()
                    + "\nCheck the file format." );
            log.error( e, e );
            return;

        } catch ( Exception e ) {
            GuiUtil.error( "Gene annotation reading error during initialization: " + e.getMessage()
                    + "\nCheck the file format." );
            log.error( e, e );
            return;
        }

        updateProgress( 60 );

        statusMessenger.showStatus( "Reading user-defined gene sets from directory "
                + settings.getUserGeneSetDirectory() );

        // end slow(ish) part.

        if ( geneData == null || geneData.getActiveGeneSets() == null ) {
            throw new IllegalArgumentException( "The gene annotation file was not valid. "
                    + "Check that you have selected the correct format.\n" );
        }

        if ( geneData.getActiveGeneSets().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no gene set information. "
                    + "Check that the file format is correct.\n" );
        }

        if ( geneData.getGenes().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no probes. "
                    + "Check that the file format is correct.\n" );
        }

        assert geneData != null;
        UserDefinedGeneSetManager.init( geneData, settings );
        UserDefinedGeneSetManager.loadUserGeneSets( statusMessenger );
        updateProgress( 90 );
    }

    /**
     * 
     */
    private void readPrefs() {
        int width = STARTING_OVERALL_WIDTH;
        int height = START_HEIGHT;
        if ( settings == null ) {
            this.setSize( width, height );
            return;
        }

        if ( settings.getConfig() == null ) return;

        if ( settings.getConfig().containsKey( MAINWINDOWWIDTH ) ) {
            width = Integer.parseInt( settings.getConfig().getString( MAINWINDOWWIDTH ) );
            log.debug( "Got: " + width );
        }

        if ( settings.getConfig().containsKey( MAINWINDOWHEIGHT ) ) {
            height = Integer.parseInt( settings.getConfig().getString( MAINWINDOWHEIGHT ) );
            log.debug( "Got: " + height );
        }

        try {
            int startX = ( int ) settings.getConfig().getDouble( MAINWINDOWPOSITIONX );
            int startY = ( int ) settings.getConfig().getDouble( MAINWINDOWPOSITIONY );
            this.setLocation( new Point( startX, startY ) );
        } catch ( NoSuchElementException e ) {
            GuiUtil.centerContainer( this );
        }

        this.setSize( width, height );

    }

    /**
     * Tabs that have our table and tree.
     */
    private void setupMainPanels() {
        tablePanel = new GeneSetTablePanel( this, results, settings );
        tablePanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );
        treePanel = new GeneSetTreePanel( this, results, settings );
        treePanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );

        tabs.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );
        tabs.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseReleased( MouseEvent e ) {
                maybeEnableSomeMenus();
            }
        } );
        tabs.addTab( "Table", tablePanel );
        tabs.addTab( "Tree", treePanel );
    }

    /**
     * 
     */
    private void setupMenus() {
        JMenuBar jMenuBar1 = new JMenuBar();
        this.setJMenuBar( jMenuBar1 );
        final JCheckBoxMenuItem showUsersMenuItem = new JCheckBoxMenuItem( "Show user-defined gene sets", false );
        JMenuItem quitMenuItem = new JMenuItem();
        JMenuItem logMenuItem = new JMenuItem();
        JMenuItem geneAnnotsWebLinkMenuItem = new JMenuItem();

        JMenuItem findClassMenuItem = new JMenuItem();
        JMenuItem findGeneMenuItem = new JMenuItem();
        JMenuItem aboutMenuItem = new JMenuItem();

        fileMenu.setText( "File" );
        fileMenu.setMnemonic( 'F' );
        quitMenuItem.setText( "Quit" );
        quitMenuItem.addActionListener( new GeneSetScoreFrame_quitMenuItem_actionAdapter( this ) );
        quitMenuItem.setMnemonic( 'Q' );
        quitMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Q, InputEvent.CTRL_MASK ) );

        fileMenu.add( quitMenuItem );

        classMenu.setText( "Gene Sets" );
        classMenu.setMnemonic( 'C' );
        classMenu.setEnabled( false );

        defineClassMenuItem.setText( "Define New Gene Set" );
        defineClassMenuItem.addActionListener( new GeneSetScoreFrame_defineClassMenuItem_actionAdapter( this ) );
        defineClassMenuItem.setMnemonic( 'D' );
        defineClassMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_D, InputEvent.CTRL_MASK ) );

        modClassMenuItem.setText( "View/Modify Gene Set" );
        modClassMenuItem.addActionListener( new GeneSetScoreFrame_modClassMenuItem_actionAdapter( this ) );
        modClassMenuItem.setMnemonic( 'M' );

        findClassMenuItem.setText( "Find Gene Set" );
        findClassMenuItem.addActionListener( new GeneSetScoreFrame_findClassMenuItem_actionAdapter( this ) );
        findClassMenuItem.setMnemonic( 'F' );
        findClassMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F, InputEvent.CTRL_MASK ) );

        findGeneMenuItem.setText( "Find Gene sets by gene" );
        findGeneMenuItem.addActionListener( new GeneSetScoreFrame_findGeneMenuItem_actionAdapter( this ) );
        findGeneMenuItem.setMnemonic( 'G' );
        findGeneMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_G, InputEvent.CTRL_MASK ) );

        showUsersMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                showUserMenuItemActionPerformed( showUsersMenuItem.getState() );
            }
        } );
        showUsersMenuItem.setMnemonic( 'U' );
        showUsersMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_U, InputEvent.CTRL_MASK ) );

        classMenu.add( defineClassMenuItem );
        classMenu.add( modClassMenuItem );
        classMenu.add( findClassMenuItem );
        classMenu.add( findGeneMenuItem );
        classMenu.add( showUsersMenuItem );

        this.runViewMenu.setText( "Results" );
        runViewMenu.setMnemonic( 'R' );
        runViewMenu.setEnabled( false );

        analysisMenu.setText( "Analysis" );
        analysisMenu.setMnemonic( 'A' );
        analysisMenu.setEnabled( false );
        runAnalysisMenuItem.setText( "Run Analysis" );
        runAnalysisMenuItem.addActionListener( new GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( this ) );
        runAnalysisMenuItem.setMnemonic( 'R' );
        runAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_R, InputEvent.CTRL_MASK ) );
        cancelAnalysisMenuItem.setText( "Cancel Analysis" );
        cancelAnalysisMenuItem.setEnabled( false );
        cancelAnalysisMenuItem.addActionListener( new GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( this ) );
        cancelAnalysisMenuItem.setMnemonic( 'C' );
        cancelAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, InputEvent.CTRL_MASK ) );
        loadAnalysisMenuItem.setText( "Load Analysis" );
        loadAnalysisMenuItem.addActionListener( new GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( this ) );
        loadAnalysisMenuItem.setMnemonic( 'L' );
        loadAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_L, InputEvent.CTRL_MASK ) );
        saveAnalysisMenuItem.setText( "Save Analysis" );
        saveAnalysisMenuItem.addActionListener( new GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( this ) );
        saveAnalysisMenuItem.setMnemonic( 'S' );
        saveAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_MASK ) );
        saveAnalysisMenuItem.setEnabled( false ); // no runs to begin with.

        JMenuItem switchDataFileMenuItem = new JMenuItem();
        JMenuItem switchGeneScoreFileMenuItem = new JMenuItem();

        switchDataFileMenuItem.setActionCommand( "Set raw data file" );
        switchDataFileMenuItem.setText( "Set raw data file..." );
        switchDataFileMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                switchRawDataFile();
            }
        } );

        switchGeneScoreFileMenuItem.setActionCommand( "Set gene score file" );
        switchGeneScoreFileMenuItem.setText( "Set gene score file..." );
        switchGeneScoreFileMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                switchGeneScoreFile();
            }
        } );

        JMenuItem multifuncMenuItem = new JMenuItem( "Multifunctionality" );
        multifuncMenuItem.setToolTipText( "View diagnostics about gene multifunctionality." );
        multifuncMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                multifunctionalityDiagnostics();
            }
        } );

        analysisMenu.add( runAnalysisMenuItem );
        analysisMenu.add( cancelAnalysisMenuItem );
        analysisMenu.add( loadAnalysisMenuItem );
        analysisMenu.add( saveAnalysisMenuItem );
        analysisMenu.add( multifuncMenuItem );
        analysisMenu.add( switchDataFileMenuItem );
        analysisMenu.add( switchGeneScoreFileMenuItem );

        logMenuItem.setMnemonic( 'L' );
        logMenuItem.setText( "View log" );
        logMenuItem.setToolTipText( "Debugging information" );
        logMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                showLogs();
            }
        } );

        geneAnnotsWebLinkMenuItem.setText( "Get annotations" );
        geneAnnotsWebLinkMenuItem.setToolTipText( "Find annotation files on the ErmineJ web site" );
        geneAnnotsWebLinkMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try {
                    BrowserLauncher.openURL( "http://www.chibi.ubc.ca/microannots" );
                } catch ( Exception ex ) {
                    GuiUtil.error( "Could not open a web browser window to get annotations" );
                }
            }
        } );

        aboutMenuItem.setText( "About ErmineJ" );
        aboutMenuItem.setMnemonic( 'A' );
        aboutMenuItem.addActionListener( new GeneSetScoreFrame_aboutMenuItem_actionAdapter( this ) );

        JMenuItem helpMenuItem = new JMenuItem();

        helpMenu.setText( "Help" );
        helpMenu.setMnemonic( 'H' );
        helpMenuItem.setText( "Help Topics" );
        helpMenuItem.setMnemonic( 'T' );

        HelpHelper hh = new HelpHelper();
        hh.initHelp( helpMenuItem );

        helpMenu.add( helpMenuItem );
        helpMenu.add( geneAnnotsWebLinkMenuItem );
        helpMenu.add( aboutMenuItem );
        helpMenu.add( logMenuItem );

        jMenuBar1.add( fileMenu );
        jMenuBar1.add( classMenu );
        jMenuBar1.add( analysisMenu );
        jMenuBar1.add( runViewMenu );
        jMenuBar1.add( helpMenu );

    }

    /**
     * The panel that show up while the initial inputs are loaded.
     * 
     * @return
     */
    private JPanel setupProgressPanel() {
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon( new ImageIcon( MainFrame.class.getResource( RESOURCE_LOCATION + "logo1small.gif" ) ) );

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground( Color.WHITE );
        logoPanel.add( logoLabel );

        // big
        JPanel progressPanel = new JPanel( new BorderLayout() );
        progressPanel.setBackground( Color.white );
        progressPanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );

        JLabel label = new JLabel( "Please wait while the files are loaded in." );
        label.setMaximumSize( new Dimension( 500, 30 ) );
        label.setLabelFor( progressBar );

        JPanel progressBarContainer = new JPanel();
        progressBarContainer.setLayout( new BoxLayout( progressBarContainer, BoxLayout.Y_AXIS ) );
        progressBarContainer.setPreferredSize( new Dimension( 300, 36 ) );
        progressBarContainer.add( label );
        progressBarContainer.add( progressBar );
        progressBarContainer.setBorder( BorderFactory.createEmptyBorder( 20, 20, 20, 20 ) );
        progressBar.setIndeterminate( true );
        progressBar.setMaximumSize( new Dimension( 500, 25 ) );

        progressPanel.add( logoPanel, BorderLayout.NORTH );
        progressPanel.add( progressBarContainer, BorderLayout.CENTER );

        return progressPanel;
    }

    private void setupStatusBar() {
        JLabel jLabelStatus = new JLabel();
        JPanel jPanelStatus = new JPanel();

        jPanelStatus.setLayout( new BorderLayout() );
        jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
        jPanelStatus.setPreferredSize( new Dimension( STARTING_OVERALL_WIDTH, 33 ) );
        jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        jLabelStatus.setBorder( BorderFactory.createEmptyBorder( 5, 5, 10, 10 ) );
        jLabelStatus.setPreferredSize( new Dimension( 800, 19 ) );
        jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
        jPanelStatus.add( jLabelStatus, BorderLayout.WEST );
        this.statusMessenger = new StatusJlabel( jLabelStatus );
        this.getContentPane().add( jPanelStatus, BorderLayout.SOUTH );

        tablePanel.setMessenger( this.statusMessenger );
        treePanel.setMessenger( this.statusMessenger );
    }

    private void updateProgress( int val ) {
        final int value = val;

        if ( SwingUtilities.isEventDispatchThread() ) {
            progressBar.setValue( value );
        } else {

            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        progressBar.setValue( value );
                    }
                } );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            } catch ( InvocationTargetException e ) {
                e.printStackTrace();
            }
        }

    }

    protected void maybeEnableSomeMenus() {

        if ( results.size() > 1 && tabs.getSelectedIndex() == 1 ) {
            runViewMenu.setEnabled( true );
        } else {
            runViewMenu.setEnabled( false );
        }
    }

    /**
     * 
     */
    protected void showLogs() {
        StringBuffer bif = new StringBuffer();
        File logFile = settings.getLogFile();

        if ( logFile == null ) {
            GuiUtil.error( "Cannot locate log file" );
            return;
        }

        if ( !logFile.canRead() ) {
            GuiUtil.error( "Cannot read log file from " + logFile );
            return;
        }

        try {

            BufferedReader fis = new BufferedReader( new FileReader( logFile ) );
            String line;
            while ( ( line = fis.readLine() ) != null ) {
                bif.append( line );
                bif.append( "\n" );
            }
            fis.close();
            ScrollingTextAreaDialog b = new ScrollingTextAreaDialog( this, "ErmineJ Log", true );
            b.setText( "The log file is located at:\n" + logFile + "\n\nLog contents:\n" + bif.toString() );
            b.setEditable( false );
            b.setSize( new Dimension( 350, 500 ) );
            b.setResizable( true );
            b.setLocation( GuiUtil.chooseChildLocation( b, this ) );
            b.setCaretPosition( 0 );
            b.pack();
            b.validate();
            b.setVisible( true );
        } catch ( FileNotFoundException e ) {
            GuiUtil.error( "The log file could not be found: was looking in " + logFile );
            log.error( e );
        } catch ( IOException e ) {
            GuiUtil.error( "There was an error reading the log file from " + logFile );
            log.error( e );
        }
    }

    /**
     * @param b
     */
    protected void showUserMenuItemActionPerformed( boolean b ) {
        /*
         * I deem this filtering unnecessary for the tree, but it could be done.
         */
        this.tablePanel.filterByUserGeneSets( b );

        if ( b ) {
            statusMessenger.showStatus( this.tablePanel.getRowCount() + " custom gene sets shown" );
        } else {
            statusMessenger.showStatus( this.tablePanel.getRowCount() + " gene sets shown" );

        }
    }

    /**
     * 
     */
    protected void switchGeneScoreFile() {
        JFileChooser fchooser = new JFileChooser( settings.getGeneScoreFileDirectory() );
        fchooser.setDialogTitle( "Choose the gene score file or cancel." );
        int yesno = fchooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            settings.setScoreFile( fchooser.getSelectedFile().getAbsolutePath() );
        }

    }

    /**
     * 
     */
    protected void switchRawDataFile() {
        JFileChooser fchooser = new JFileChooser( settings.getRawDataFileDirectory() );
        fchooser.setDialogTitle( "Choose the expression data file or cancel." );
        int yesno = fchooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            settings.setRawFile( fchooser.getSelectedFile().getAbsolutePath() );
            settings.userSetRawFile( true );
        } else {
            settings.userSetRawFile( false );
        }
    }

    /**
     * 
     */
    protected void writePrefs() {
        settings.getConfig().setProperty( MAINWINDOWWIDTH, String.valueOf( this.getWidth() ) );
        settings.getConfig().setProperty( MAINWINDOWHEIGHT, String.valueOf( this.getHeight() ) );
        settings.getConfig().setProperty( MAINWINDOWPOSITIONX, new Double( this.getLocation().getX() ) );
        settings.getConfig().setProperty( MAINWINDOWPOSITIONY, new Double( this.getLocation().getY() ) );
    }

    void aboutMenuItem_actionPerformed() {
        new AboutBox( this );
    }

    void defineClassMenuItem_actionPerformed() {
        GeneSetWizard cwiz = new GeneSetWizard( this, geneData, true );
        cwiz.showWizard();
    }

    /**
     * Cancel the currently running analysis task.
     */
    void doCancel() {
        log.debug( "Got cancel in thread " + Thread.currentThread().getName() );

        assert athread != null : "Attempt to cancel a null analysis thread";
        athread.interrupt();

        try {
            Thread.sleep( 100 );
        } catch ( InterruptedException e ) {
        }

        athread.stopRunning( true );
        enableMenusForAnalysis();
        this.statusMessenger.showStatus( "Ready" );

    }

    void enableMenusForAnalysis() {
        defineClassMenuItem.setEnabled( true );
        modClassMenuItem.setEnabled( true );
        runAnalysisMenuItem.setEnabled( true );
        loadAnalysisMenuItem.setEnabled( true );
        maybeEnableSomeMenus();
        if ( results.size() > 0 ) saveAnalysisMenuItem.setEnabled( true );
        cancelAnalysisMenuItem.setEnabled( false );
    }

    void findClassMenuItem_actionPerformed() {
        if ( findByNameDialog == null ) findByNameDialog = new FindDialog( this, geneData );
        findByNameDialog.setVisible( true );
    }

    void findGeneMenuItem_actionPerformed() {
        if ( findByGeneDialog == null ) findByGeneDialog = new FindByGeneDialog( this, geneData );
        findByGeneDialog.setVisible( true );
    }

    void loadAnalysisMenuItem_actionPerformed() {
        LoadDialog lgsd = new LoadDialog( this );
        lgsd.showDialog();
    }

    void modClassMenuItem_actionPerformed() {
        GeneSetWizard cwiz = new GeneSetWizard( this, geneData, false );
        cwiz.showWizard();
    }

    /**
     * @param e ActionEvent
     */
    void quitMenuItem_actionPerformed( ActionEvent e ) {
        statusMessenger.clear();
        System.exit( 0 );
    }

    void runAnalysisMenuItem_actionPerformed() {
        AnalysisWizard awiz = new AnalysisWizard( this, geneData );
        awiz.showWizard();
    }

    void saveAnalysisMenuItem_actionPerformed() {
        if ( results.size() == 0 ) {
            statusMessenger.showError( "There are no runs to save" );
            return;
        }
        SaveWizard swiz = new SaveWizard( this, results );
        swiz.showWizard();
    }

}

// //////////////////////////////////////////////////////////////////////////////
/* end class */

class GeneSetScoreFrame_aboutMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_aboutMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.aboutMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.doCancel();
    }
}

class GeneSetScoreFrame_defineClassMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_defineClassMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.defineClassMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_findClassMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_findClassMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.findClassMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_findGeneMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_findGeneMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.findGeneMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.loadAnalysisMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_modClassMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_modClassMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.modClassMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_quitMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_quitMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.quitMenuItem_actionPerformed( e );
    }
}

class GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.runAnalysisMenuItem_actionPerformed();
    }

}

class GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.saveAnalysisMenuItem_actionPerformed();
    }
}

class RunSet_Choose_ActionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    /**
     * @param adaptee
     */
    public RunSet_Choose_ActionAdapter( MainFrame adaptee ) {
        super();
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        JMenuItem source = ( JMenuItem ) e.getSource();
        source.setIcon( new ImageIcon( this.getClass().getResource( "/ubic/erminej/checkBox.gif" ) ) );
        source.setSelected( true );
        String resultSetName = source.getText();
        clearOtherMenuItems( resultSetName );
        adaptee.setCurrentResultSet( resultSetName );
    }

    /**
     * clear icon for other menu items.
     * 
     * @param source
     * @param resultSetName
     */
    private void clearOtherMenuItems( String resultSetName ) {
        JMenu rvm = adaptee.getRunViewMenu();
        for ( int i = 0; i < rvm.getItemCount(); i++ ) {
            JMenuItem jmi = rvm.getItem( i );
            if ( !jmi.getText().equals( resultSetName ) ) {
                jmi.setIcon( new ImageIcon( this.getClass().getResource( "/ubic/erminej/noCheckBox.gif" ) ) );
            }
        }
    }
}
