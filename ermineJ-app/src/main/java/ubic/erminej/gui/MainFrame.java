/*
 * The ermineJ project
 * 
 * Copyright (c) 2006-2011 University of British Columbia
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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import ubic.basecode.util.BrowserLauncher;
import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;
import ubic.erminej.AnalysisThread;
import ubic.erminej.ResultsPrinter;
import ubic.erminej.Settings;
import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotationParser;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneScores;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.data.GeneSetTerms;
import ubic.erminej.gui.analysis.AnalysisWizard;
import ubic.erminej.gui.analysis.MultiFuncDiagWindow;
import ubic.erminej.gui.file.DataFileFilter;
import ubic.erminej.gui.geneset.details.JGeneScoreFileChooser;
import ubic.erminej.gui.geneset.details.JRawFileChooser;
import ubic.erminej.gui.geneset.table.GeneSetTablePanel;
import ubic.erminej.gui.geneset.tree.GeneSetTreePanel;
import ubic.erminej.gui.geneset.wiz.GeneSetWizard;
import ubic.erminej.gui.util.GuiUtil;
import ubic.erminej.gui.util.ScrollingTextAreaDialog;
import ubic.erminej.gui.util.StatusJlabel;

/**
 * The main ErmineJ application GUI Sframe.
 * 
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @author Will Braynen
 * @version $Id$
 */
public class MainFrame extends JFrame {

    private final class ProjectFileFilter extends FileFilter {
        @Override
        public boolean accept( File pathname ) {
            if ( pathname.isDirectory() ) {
                return true;
            }
            return pathname.getPath().endsWith( ".project" );
        }

        @Override
        public String getDescription() {
            return "ErmineJ project files (*.project)";
        }
    }

    private static final String START_CARD = "START";

    private static final String TABS_CARD = "TABS";

    private static final String PROGRESS_CARD = "PROGRESS";

    public final static String RESOURCE_LOCATION = "/ubic/erminej/";

    private static Log log = LogFactory.getLog( MainFrame.class.getName() );

    private static final int START_HEIGHT = 630;

    private static final int START_WIDTH = 830;

    private static final int STARTING_OVERALL_WIDTH = 830;

    private static final String MAINWINDOWHEIGHT = "mainview.WindowHeight";
    private static final String MAINWINDOWWIDTH = "mainview.WindowWidth";
    private static final String MAINWINDOWPOSITIONX = "mainview.WindowXPosition";
    private static final String MAINWINDOWPOSITIONY = "mainview.WindowYPosition";

    private GeneAnnotations geneData = null; // original.

    private List<GeneSetPvalRun> results = new ArrayList<GeneSetPvalRun>();

    private Settings settings;

    private int currentResultSet = -1;

    private AnalysisThread athread; // Ideally this would be a local variable.

    private StatusViewer statusMessenger;
    private GeneSetTablePanel tablePanel;
    private GeneSetTreePanel treePanel;

    private JMenu analysisMenu = new JMenu();
    private JMenu diagnosticsMenu = new JMenu();
    private JMenu classMenu = new JMenu();

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

    private JPanel statusBarPanel;

    /**
     * @throws IOException
     */
    public MainFrame() throws IOException {
        settings = new Settings( true );
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
        // searchPanel.setEnabled( false );
    }

    /**
     * @param selectedTerms
     */
    public void filter( final Collection<GeneSetTerm> selectedTerms ) {

        // SwingWorker<Object, Object> w = new SwingWorker<Object, Object>() {
        // @Override
        // protected Object doInBackground() throws Exception {
        tablePanel.filter( selectedTerms );
        treePanel.filter( selectedTerms );
        // return null;
        // }
        // };
        // w.execute();

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
    public GeneSetPvalRun getCurrentResultSet() {
        if ( this.currentResultSet < 0 ) return null;
        return this.results.get( this.currentResultSet );
    }

    public int getCurrentResultSetIndex() {
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

    /**
     * @param loadFile
     * @throws IOException
     */
    public void loadAnalysis( String loadFile ) throws IOException {

        assert loadFile != null;

        disableMenusForAnalysis();

        // the settings used for the analysis, not the same as the application settings.
        Settings loadSettings;
        try {
            loadSettings = new Settings( loadFile );
        } catch ( ConfigurationException e ) {
            GuiUtil.error( "There was a problem loading the settings from:\n" + loadFile + "\n" + e.getMessage() );
            return;
        }

        if ( !checkValid( loadSettings ) ) {
            GuiUtil
                    .error( "There was a problem loading the analysis.\nFiles referred to in the analysis may have been moved or deleted." );
            return;
        }

        this.athread = new AnalysisThread( loadSettings, statusMessenger, geneData, loadFile );
        athread.run();
        log.debug( "Waiting" );

        Collection<GeneSetPvalRun> latestResults = athread.getLatestResults();
        for ( GeneSetPvalRun latestResult : latestResults ) {
            checkForReasonableResults( latestResult );
            if ( latestResult != null ) addResult( latestResult );
        }

        athread = null;

        log.debug( "done" );
        enableMenusForAnalysis();
    }

    /**
     * @param runIndex
     */
    public void setCurrentResultSetIndex( int runIndex ) {
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
                this.setCurrentResultSetIndex( i );
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

    /**
     * @param runSettings
     */
    public void startAnalysis( Settings runSettings ) {
        disableMenusForAnalysis();
        this.athread = new AnalysisThread( runSettings, statusMessenger, geneData );
        log.debug( "Starting analysis thread" );
        try {
            athread.start();
        } catch ( Exception e ) {
            GuiUtil.error( "There was an unexpected error during analysis.\n"
                    + "See the log file for details.\nThe summary message was:\n" + e.getMessage() );
            enableMenusForAnalysis();
        }
        log.debug( "Waiting..." );

        Collection<GeneSetPvalRun> latestResults = athread.getLatestResults();
        for ( GeneSetPvalRun latestResult : latestResults ) {
            checkForReasonableResults( latestResult );
            if ( latestResult != null ) addResult( latestResult );
            try {
                Thread.sleep( 100 );
            } catch ( InterruptedException e ) {
                // 
            }
        }
        athread = null;
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

        try {
            Thread.sleep( 100 );
        } catch ( InterruptedException e ) {
            // give the gui a chance to catch up; an attempt (in vain?) to avoid errors in logs like "
            // java.lang.ClassCastException:
            // ubic.erminej.data.EmptyGeneSetResult cannot be cast to java.lang.String at
            // ubic.erminej.gui.geneset.table.GeneSetTableCellRenderer.getTableCellRendererComponent(GeneSetTableModel.java:316)"
        }

        tablePanel.addRun();
        treePanel.addRun();
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

            // try to start them somewhere useful.
            JFileChooser fc = new JFileChooser( settings.getGeneScoreFileDirectory() );
            //
            // GuiUtil.error( "A file referred to in the results file\n(" + file
            // + ")\nwas not found at the listed path.\nIt may have been moved.\nYou will be prompted to"
            // + " enter the location" );
            fc.setDialogTitle( "Missing file: please locate " + file );
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
        if ( athread == null || !athread.isFinishedNormally() ) return;
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
            GuiUtil.error( "The results indicate that you may need to adjust your analysis settings.\n"
                    + "For example, make sure your setting for 'larger scores are better' is correct." );
        }

    }

    /**
     * @param loadSettings
     */
    private boolean checkValid( Settings loadSettings ) {

        /*
         * Strict validation is turned off. But I'm not sure we need this at all?
         */
        if ( !loadSettings.getAnnotFile().equals( this.settings.getAnnotFile() ) ) {
            this.statusMessenger
                    .showError( "The annotation file in the file doesn't match the one you have loaded, there could be problems." );
        }

        String file;

        file = checkFile( loadSettings.getRawDataFileName() ); // looks for the file.
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
        // searchPanel.setEnabled( true );
    }

    private int find( String searchOn, boolean searchGenes ) {
        Collection<GeneSetTerm> geneSets = new HashSet<GeneSetTerm>();

        if ( StringUtils.isBlank( searchOn ) ) {
            filter( new HashSet<GeneSetTerm>() );
            statusMessenger.showStatus( "Showing all gene sets" );
            return geneData.numGeneSets();
        }

        if ( searchGenes ) {
            geneSets = geneData.findSetsByGene( searchOn );
        } else {
            geneSets = geneData.findSetsByName( searchOn );
        }

        if ( geneSets.size() > 0 ) {
            filter( geneSets );
        }

        return geneSets.size();
    }

    /**
     * aFter the user has clicked "okay" for their initial setup.
     * 
     * @param
     */
    private void initialize( final JPanel cards, final String projectFile ) {
        ( ( CardLayout ) cards.getLayout() ).show( cards, PROGRESS_CARD );

        SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {

            @Override
            protected Object doInBackground() throws Exception {
                try {

                    tablePanel.setMessenger( statusMessenger );
                    treePanel.setMessenger( statusMessenger );
                    initializeAllData();

                    /*
                     * If we are loading a project;
                     */
                    if ( StringUtils.isNotBlank( projectFile ) ) loadAnalysis( projectFile );

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
     * Reinitialize everything
     */
    protected void initializeAllData() {
        readDataFilesForStartup(); // sets up geneData.
        treePanel.initialize( geneData );
        tablePanel.initialize( geneData );
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

        setupMenus();
        setupStatusBar();

        disableMenusForLoad();

        final JPanel cards = new JPanel( new CardLayout() );
        this.getContentPane().add( cards, BorderLayout.CENTER );

        final StartupPanel startupPanel = new StartupPanel( settings, this.statusMessenger );
        cards.add( startupPanel, START_CARD );

        JPanel progressPanel = setupProgressPanel();
        cards.add( progressPanel, PROGRESS_CARD );

        setupMainPanels();
        cards.add( tabs, TABS_CARD );

        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                initialize( cards, startupPanel.getProjectFileName() );
            }
        };

        startupPanel.addActionListener( actionListener );

        ( ( CardLayout ) cards.getLayout() ).show( cards, START_CARD );

        setIconImage( new ImageIcon( this.getClass().getResource( "/ubic/erminej/logoIcon64.gif" ) ).getImage() );

    }

    /**
     * Show the multifunctionality for the current selected annotation and gene scores.
     */
    private void multifunctionalityDiagnostics() {
        /*
         * Should allow user to pick.
         */
        GeneAnnotations ga = this.geneData;
        GeneScores gs = null;
        try {
            if ( this.getCurrentResultSetIndex() >= 0 && !this.results.isEmpty() ) {
                gs = new GeneScores( getCurrentResultSet().getSettings().getScoreFile(), settings, statusMessenger,
                        this.geneData );
                ga = gs.getGeneAnnots();
            } else if ( StringUtils.isNotBlank( settings.getScoreFile() ) ) {
                gs = new GeneScores( settings.getScoreFile(), settings, statusMessenger, this.geneData );
                ga = gs.getGeneAnnots();
            }
        } catch ( IOException e ) {
            GuiUtil.error( "Data for multifunctionality could not be read", e );
            return;
        }

        MultiFuncDiagWindow w = new MultiFuncDiagWindow( ga, gs );
        w.setSize( new Dimension( 500, 500 ) );
        GuiUtil.centerContainer( w );
        w.setVisible( true );
    }

    /**
     * Input the GO and annotation files.
     */
    private void readDataFilesForStartup() {

        StopWatch timer = new StopWatch();
        timer.start();
        updateProgress( 10 );

        statusMessenger.showStatus( "Reading GO hierarchy from: " + settings.getClassFile() );
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

        timer.split();
        log.info( "Load GO: " + timer.getSplitTime() + " ms" );

        updateProgress( 30 );

        statusMessenger.showStatus( "Reading gene annotations from " + settings.getAnnotFile() );

        try {
            GeneAnnotationParser parser = new GeneAnnotationParser( goData, statusMessenger );

            geneData = parser.read( settings.getAnnotFile(), settings.getAnnotFormat(), settings );

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

        assert geneData != null;
        log.info( "Setup done: " + timer.getTime() + " ms" );

        updateProgress( 90 );

        File annotF = new File( settings.getAnnotFile() );
        String fileName = annotF.getName();
        this.setTitle( "ErmineJ : " + fileName );

        // end slow(ish) part.

        if ( geneData == null || geneData.getGeneSetTerms() == null ) {
            throw new IllegalArgumentException( "The gene annotation file was not valid. "
                    + "Check that you have selected the correct format.\n" );
        }

        if ( geneData.getGeneSetTerms().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no usable gene set information. "
                    + "Check that the file format is correct or that you have annotations for enough genes.\n" );
        }

        if ( geneData.getGenes().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no probes. "
                    + "Check that the file format is correct.\n" );
        }

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
        tablePanel = new GeneSetTablePanel( this, settings );
        tablePanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );
        treePanel = new GeneSetTreePanel( this, settings );
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
        JMenuItem aboutMenuItem = new JMenuItem();

        fileMenu.setText( "File" );
        fileMenu.setMnemonic( 'F' );
        quitMenuItem.setText( "Quit" );
        quitMenuItem.addActionListener( new GeneSetScoreFrame_quitMenuItem_actionAdapter( this ) );
        quitMenuItem.setMnemonic( 'Q' );
        quitMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK ) );
        fileMenu.add( quitMenuItem );

        JMenuItem saveProjectMenuItem = new JMenuItem( "Save project ..." );
        JMenuItem loadProjectMenuItem = new JMenuItem( "Load project ..." );
        saveProjectMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                saveProject();
            }
        } );
        loadProjectMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                loadProject();
            }
        } );

        JMenuItem switchAnnotationFileMenuItem = new JMenuItem( "Switch annotation source ..." );
        switchAnnotationFileMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                switchAnnotations();
            }
        } );

        fileMenu.add( switchAnnotationFileMenuItem );
        fileMenu.add( saveProjectMenuItem );
        fileMenu.add( loadProjectMenuItem );

        classMenu.setText( "Gene Sets" );
        classMenu.setMnemonic( 'C' );
        classMenu.setEnabled( false );

        defineClassMenuItem.setText( "Define New Gene Set" );
        defineClassMenuItem.addActionListener( new GeneSetScoreFrame_defineClassMenuItem_actionAdapter( this ) );
        defineClassMenuItem.setMnemonic( 'D' );
        defineClassMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK ) );

        modClassMenuItem.setText( "View/Modify Gene Set" );
        modClassMenuItem.addActionListener( new GeneSetScoreFrame_modClassMenuItem_actionAdapter( this ) );
        modClassMenuItem.setMnemonic( 'M' );

        showUsersMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                showUserMenuItemActionPerformed( showUsersMenuItem.getState() );
            }
        } );
        showUsersMenuItem.setMnemonic( 'U' );
        showUsersMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_U, InputEvent.CTRL_MASK ) );

        classMenu.add( defineClassMenuItem );
        classMenu.add( modClassMenuItem );
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
        runAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK ) );
        cancelAnalysisMenuItem.setText( "Cancel Analysis" );
        cancelAnalysisMenuItem.setEnabled( false );
        cancelAnalysisMenuItem.addActionListener( new GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( this ) );
        cancelAnalysisMenuItem.setMnemonic( 'C' );
        cancelAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK ) );
        loadAnalysisMenuItem.setText( "Load Analysis" );
        loadAnalysisMenuItem.addActionListener( new GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( this ) );
        loadAnalysisMenuItem.setMnemonic( 'L' );
        loadAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK ) );
        saveAnalysisMenuItem.setText( "Save Analysis" );
        saveAnalysisMenuItem.addActionListener( new GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( this ) );
        saveAnalysisMenuItem.setMnemonic( 'S' );
        saveAnalysisMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ) );
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
     * 
     */
    protected void loadAnalysis() {
        JFileChooser chooser = new JFileChooser();
        String RESULTS_LOAD_LOCATION = "resultsLoadPath";
        chooser.setCurrentDirectory( new File( settings.getConfig().getString( RESULTS_LOAD_LOCATION,
                settings.getDataDirectory() ) ) );
        chooser.setDialogTitle( "Open saved analysis" );
        int result = chooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {

            settings.getConfig().setProperty( RESULTS_LOAD_LOCATION, chooser.getSelectedFile().getAbsolutePath() );
            final String path = chooser.getSelectedFile().getAbsolutePath();
            if ( FileTools.testFile( path ) ) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            loadAnalysis( path );
                        } catch ( Exception e1 ) {
                            GuiUtil.error( "There was an error:\n" + e1.getMessage() );
                        }
                    }
                }.start();
            } else {
                GuiUtil.error( "File is not readable." );
            }
        }

    }

    protected void switchAnnotations() {

        /*
         * User selects annotation file
         */
        JFileChooser annotFileChooser = new JFileChooser( settings.getDataDirectory() );
        annotFileChooser.setFileFilter( new DataFileFilter() );
        annotFileChooser.setDialogTitle( "Select annotation file" );
        int yesno = annotFileChooser.showDialog( this, "Open" );
        if ( yesno == JFileChooser.APPROVE_OPTION ) {

            File selectedFile = annotFileChooser.getSelectedFile();

            boolean newIsSameAsOld = selectedFile.getAbsolutePath().equals( settings.getAnnotFile() );
            if ( newIsSameAsOld ) {
                /*
                 * Alert the user -- they can reload if they want, or bail.
                 */
                int response = JOptionPane.showConfirmDialog( null, "The selected annotations"
                        + " are already loaded. Force reload?", "File already loaded", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE );
                if ( response == JOptionPane.CANCEL_OPTION ) return;
            }

            /*
             * FIXME If there are already analyses, we should prompt them to save, as it will mess them up? Need to test
             * behaviour.
             */
            if ( !this.results.isEmpty() ) {
                // int response = JOptionPane
                // .showConfirmDialog(
                // null,
                // "Your current results will be discarded when"
                // +
                // "the anotations load.\nYou can click Cancel and then save results you want to keep, or click OK to proceed.",
                // "Unsaved results will be lost",
                //
                // JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
                // if ( response == JOptionPane.CANCEL_OPTION ) return;
            }

            this.settings.setAnnotFile( selectedFile.getAbsolutePath() );

            /*
             * The settings for the geneScores and rawData should be blanked, if the annotation file is different.
             */
            if ( !newIsSameAsOld ) {
                settings.setScoreFile( null );
                settings.setRawFile( null );
            }

            SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {
                    initializeAllData();

                    return null;
                }
            };
            r.execute();
        }
    }

    protected void loadProject() {

        if ( !this.results.isEmpty() ) {
            int response = JOptionPane
                    .showConfirmDialog(
                            null,
                            "Your current results will be discarded when"
                                    + "the project loads.\nYou can click Cancel and then save results you want to keep, or click OK to proceed.",
                            "Unsaved results will be lost",

                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
            if ( response == JOptionPane.CANCEL_OPTION ) return;
        }

        /*
         * User selects project file
         */
        JFileChooser projectPathChooser = new JFileChooser( settings.getDataDirectory() );
        projectPathChooser.setFileFilter( new ProjectFileFilter() );
        projectPathChooser.setDialogTitle( "Open/Switch project" );
        int yesno = projectPathChooser.showDialog( this, "Open" );

        /*
         * Import it as the current settings - actually use the results import function?
         */
        if ( yesno == JFileChooser.APPROVE_OPTION ) {

            final String path = projectPathChooser.getSelectedFile().getAbsolutePath();

            try {
                Settings projectSettings = new Settings( path );

                this.settings = projectSettings;

                /*
                 * Note: those settings are not auto-saved, so the project is not modified.
                 */

                /*
                 * Rebuilt the application data structures, pretty much from scratch
                 */
                SwingWorker<Object, Object> r = new SwingWorker<Object, Object>() {

                    @Override
                    protected Object doInBackground() throws Exception {
                        initializeAllData(); // Perhaps skip if the files have not changed?
                        loadAnalysis( path );
                        resetSignificanceFilters();
                        return null;
                    }
                };
                r.execute();

            } catch ( IOException e ) {
                GuiUtil.error( "Error while loading the project: " + e.getMessage() );
                log.error( e, e );
                return;
            } catch ( ConfigurationException e ) {
                GuiUtil.error( "Error while loading the project: " + e.getMessage() );
                log.error( e, e );
                return;
            }
        }

    }

    /**
     * 
     */
    protected void resetSignificanceFilters() {
        this.treePanel.setHideInsignificant( false );
        this.treePanel.filter( true );
    }

    protected void saveProject() {

        /*
         * Prompt user for file name (and possibly the name of the project)
         */

        final JFileChooser projectPathChooser = new JFileChooser( settings.getDataDirectory() );

        projectPathChooser.setFileFilter( new ProjectFileFilter() );
        projectPathChooser.setDialogTitle( "Save the current project" );
        int yesno = projectPathChooser.showDialog( this, "Save" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {

            File selectedFile = projectPathChooser.getSelectedFile();

            if ( selectedFile.exists() ) {
                int response = JOptionPane.showConfirmDialog( null, "Overwrite existing file?", "Confirm Overwrite",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( response == JOptionPane.CANCEL_OPTION ) {
                    statusMessenger.showStatus( "Cancelled." );
                    return;
                }
            }

            if ( !selectedFile.getName().endsWith( ".project" ) ) {
                selectedFile = new File( selectedFile.getAbsolutePath() + ".project" );
            }

            String path = selectedFile.getAbsolutePath();

            /*
             * Write the file in ermineJ.data in *.project
             */
            try {

                ResultsPrinter.write( path, this.settings, this.results );
                this.statusMessenger.showStatus( "Saved to " + selectedFile.getAbsolutePath() );
            } catch ( IOException e ) {
                GuiUtil.error( "Could not save the project: " + e.getMessage() );
                log.error( e, e );
            }
        }

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
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout( new BoxLayout( progressPanel, BoxLayout.Y_AXIS ) );
        progressPanel.setBackground( Color.white );
        progressPanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );

        JLabel label = new JLabel( "Please wait while the files are loaded in." );
        label.setMaximumSize( new Dimension( 500, 30 ) );
        label.setLabelFor( progressBar );

        JPanel progressBarContainer = new JPanel();
        progressBarContainer.setLayout( new BoxLayout( progressBarContainer, BoxLayout.Y_AXIS ) );
        progressBarContainer.setPreferredSize( new Dimension( 500, 300 ) );
        //
        progressBarContainer.setBackground( Color.WHITE );

        progressBar.setIndeterminate( true );
        progressBar.setMaximumSize( new Dimension( 500, 20 ) );

        progressBarContainer.add( label );
        progressBarContainer.add( progressBar );

        progressPanel.add( logoPanel );
        progressPanel.add( progressBarContainer );

        return progressPanel;
    }

    private void setupStatusBar() {
        JLabel jLabelStatus = new JLabel();
        jLabelStatus.setPreferredSize( new Dimension( 800, 25 ) );

        statusBarPanel = new JPanel();
        GroupLayout gl = new GroupLayout( statusBarPanel );
        statusBarPanel.setLayout( gl );
        statusBarPanel.setBorder( BorderFactory.createEtchedBorder() );

        gl.setAutoCreateContainerGaps( true );
        gl.setAutoCreateGaps( true );

        final JTextField queryTextField = new JTextField();
        queryTextField.setMaximumSize( new Dimension( 140, 20 ) );
        final JCheckBox searchGenesChx = new JCheckBox( "Search genes" );

        // hitting enter while the search field has focus: search.
        queryTextField.addKeyListener( new KeyAdapter() {
            @Override
            public void keyReleased( final KeyEvent e ) {
                if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
                    statusMessenger.clear();
                    String queryString = ( ( JTextField ) e.getComponent() ).getText();
                    boolean searchByGene = searchGenesChx.isSelected();
                    int found = find( queryString, searchByGene );
                    if ( searchByGene ) {
                        if ( found == 0 ) {
                            statusMessenger.showError( "No gene sets contain genes matching query" );
                        } else {
                            statusMessenger.showStatus( found + " gene sets with matching genes" );
                        }
                    } else {
                        if ( found == 0 ) {
                            statusMessenger.showError( "No matching gene sets " );
                        } else {
                            statusMessenger.showStatus( found + " matching gene sets" );
                        }
                    }
                }
            }
        } );

        // Hitting ctrl-F: reset the search
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher( new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent( KeyEvent e ) {
                if ( e.getKeyCode() == KeyEvent.VK_F && e.isControlDown() ) {
                    /*
                     * Remove find filter
                     */
                    if ( StringUtils.isNotBlank( queryTextField.getText() ) ) {
                        queryTextField.setText( "" );
                        filter( new HashSet<GeneSetTerm>() );
                        statusMessenger.showStatus( "Showing all gene sets" );
                    }
                    queryTextField.requestFocusInWindow();
                }
                return false;
            }
        } );

        gl.setHorizontalGroup( gl.createSequentialGroup().addComponent( queryTextField ).addComponent( searchGenesChx )
                .addPreferredGap( ComponentPlacement.UNRELATED, 20, 20 ).addComponent( jLabelStatus ) );
        gl.setVerticalGroup( gl.createParallelGroup( Alignment.BASELINE ).addComponent( queryTextField ).addComponent(
                searchGenesChx ).addComponent( jLabelStatus ) );

        this.statusMessenger = new StatusJlabel( jLabelStatus );
        this.getContentPane().add( statusBarPanel, BorderLayout.SOUTH );

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
        JGeneScoreFileChooser fchooser = new JGeneScoreFileChooser( settings.getScoreFile(), settings.getScoreCol() );
        fchooser.setDialogTitle( "Choose the gene score file or cancel." );
        int yesno = fchooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            settings.setScoreFile( fchooser.getSelectedFile().getAbsolutePath() );
            statusMessenger.showStatus( "Score file set to " + settings.getScoreFile()
                    + ", reading values from column " + settings.getScoreCol() );
            settings.setScoreFile( fchooser.getSelectedFile().getAbsolutePath() );
            settings.setScoreCol( fchooser.getStartColumn() );
            statusMessenger.showStatus( "Score file set to " + settings.getScoreFile() );
        }

    }

    protected void switchRawDataFile() {
        JRawFileChooser fchooser = new JRawFileChooser( settings.getRawDataFileName(), settings.getDataCol() );
        fchooser.setDialogTitle( "Choose the expression data file or cancel." );
        int yesno = fchooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            settings.setRawFile( fchooser.getSelectedFile().getAbsolutePath() );
            settings.setDataCol( fchooser.getStartColumn() );
            statusMessenger.showStatus( "Data file set to " + settings.getScoreFile() );
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
        athread = null;
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

    void modClassMenuItem_actionPerformed() {
        GeneSetWizard cwiz = new GeneSetWizard( this, geneData, false );
        cwiz.showWizard();
    }

    /**
     * @param e ActionEvent
     */
    void quitMenuItem_actionPerformed( ActionEvent e ) {
        statusMessenger.clear();

        /*
         * This is appropriate if for example we loaded a project (which we do not change), but we might as well keep
         * the settings for next time.
         */
        settings.writePrefs();

        System.exit( 0 );
    }

    void runAnalysisMenuItem_actionPerformed() {
        AnalysisWizard awiz = new AnalysisWizard( this, geneData );
        awiz.showWizard();
    }

    /**
     * 
     */
    public void saveAnalysisAction() {
        if ( results.size() == 0 ) {
            statusMessenger.showError( "There are no runs to save" );
            return;
        }

        /*
         * 1. Pick the run and get other settings (latter needed even if only one result available)
         */
        SaveAnalysisDialog dialog = new SaveAnalysisDialog( this, this.settings, getCurrentResultSetIndex() );

        if ( dialog.wasCancelled() ) {
            this.statusMessenger.showStatus( "Save cancelled." );
            return;
        }

        int runNum = dialog.getSelectedRunNum();
        GeneSetPvalRun runToSave = this.results.get( runNum );
        boolean includeGenes = dialog.isSaveAllGenes();

        /*
         * 2. Pick the file.
         */
        JFileChooser chooser = new JFileChooser();
        chooser = new JFileChooser();
        chooser.setCurrentDirectory( new File( settings.getDataDirectory() ) );
        chooser.setApproveButtonText( "OK" );
        chooser.setDialogTitle( "Save Analysis As:" );
        chooser.setSelectedFile( new File( getCurrentResultSet().getName() + ".erminej.txt" ) );

        int result = chooser.showOpenDialog( this );

        if ( result == JFileChooser.APPROVE_OPTION ) {
            File f = new File( chooser.getSelectedFile().toString() );

            if ( f.exists() ) {
                int k = JOptionPane.showConfirmDialog( this, "That file exists. Overwrite?", "File exists",
                        JOptionPane.YES_NO_CANCEL_OPTION );
                if ( k != JOptionPane.YES_OPTION ) {
                    this.statusMessenger.showStatus( "Save cancelled." );
                    return;
                } // otherwise, bail.
            }

            /*
             * 3. Save.
             */

            try {
                String saveFileName = f.getAbsolutePath();
                ResultsPrinter.write( saveFileName, runToSave, includeGenes );
                this.statusMessenger.showStatus( "Saved" );
            } catch ( IOException ioe ) {
                GuiUtil.error( "Could not write results to the file. " + ioe );
            }
        } else {
            this.statusMessenger.showStatus( "Save cancelled." );
        }
    }

    /**
     * Remove a run from the list of results that are currently loaded.
     * 
     * @param runIndex
     */
    public void removeRun( int runIndex ) {

        /*
         * If possible, remove the annotations.
         */
        boolean canRemoveAnnots = true;
        GeneSetPvalRun runToRemove = results.get( runIndex );
        GeneAnnotations runAnnots = runToRemove.getGeneData();
        if ( results.size() > 1 ) {

            for ( GeneSetPvalRun r : results ) {
                if ( r == runToRemove ) continue;
                if ( r.getGeneData().equals( runAnnots ) ) {
                    // someone else is using it.
                    canRemoveAnnots = false;
                }
            }
        }

        if ( canRemoveAnnots ) {
            this.geneData.deleteSubClone( runAnnots );
        }

        results.remove( runIndex );
        setCurrentResultSetIndex( results.size() - 1 );

        this.treePanel.removeRun( runToRemove );
        this.tablePanel.removeRun( runToRemove );
    }

    public GeneSetPvalRun getResultSet( int runIndex ) {
        if ( runIndex < 0 || runIndex > this.results.size() - 1 ) {
            return null;
        }
        return this.results.get( runIndex );
    }

    public int getNumResultSets() {
        return this.results.size();
    }

    /**
     * @return unmodifiable list view.
     */
    public List<GeneSetPvalRun> getResultSets() {
        return Collections.unmodifiableList( this.results );
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

class GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    MainFrame adaptee;

    GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( MainFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.loadAnalysis();
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
        adaptee.saveAnalysisAction();
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
