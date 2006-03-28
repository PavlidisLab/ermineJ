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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.basecode.gui.GuiUtil;
import ubic.basecode.gui.ScrollingTextAreaDialog;
import ubic.basecode.gui.StatusJlabel;
import ubic.erminej.AnalysisThread;
import ubic.erminej.GeneSetPvalRun;
import ubic.erminej.Settings;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.UserDefinedGeneSetManager;

/**
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @author Will Braynen
 * @version $Id$
 */
public class GeneSetScoreFrame extends JFrame {
    private static Log log = LogFactory.getLog( GeneSetScoreFrame.class.getName() );
    /**
     * 
     */
    private static final int START_HEIGHT = 330;
    /**
     * 
     */
    private static final int START_WIDTH = 830;
    /**
     * 
     */
    private static final int STARTING_OVERALL_WIDTH = 830;

    private static final String MAINWINDOWHEIGHT = "mainview.WindowHeight";
    private static final String MAINWINDOWWIDTH = "mainview.WindowWidth";
    private static final String MAINWINDOWPOSITIONX = "mainview.WindowXPosition";
    private static final String MAINWINDOWPOSITIONY = "mainview.WindowYPosition";

    private JMenuItem aboutMenuItem = new JMenuItem();
    private JMenu analysisMenu = new JMenu();
    private AnalysisThread athread;
    private JMenuItem cancelAnalysisMenuItem = new JMenuItem();
    private JMenu classMenu = new JMenu();
    private int currentResultSet;
    private JMenuItem defineClassMenuItem = new JMenuItem();
    private final JFileChooser fc = new JFileChooser();
    private JMenu fileMenu = new JMenu();
    private JMenuItem findClassMenuItem = new JMenuItem();
    private FindDialog findByGeneDialog = null;
    private FindDialog findByNameDialog = null;
    private JMenuItem findGeneMenuItem = new JMenuItem();
    private GeneAnnotations geneData = null;
    private Map geneDataSets;
    private Map geneScoreSets;
    private GONames goData;
    private JMenu helpMenu = new JMenu();
    private JMenuItem helpMenuItem = new JMenuItem();
    private HelpHelper hh;
    private JLabel jLabelStatus = new JLabel();
    private JMenuBar jMenuBar1 = new JMenuBar();
    private JPanel jPanelStatus = new JPanel();
    private JMenuItem loadAnalysisMenuItem = new JMenuItem();
    private JPanel loadingPanel = new JPanel();
    private JMenuItem logMenuItem = new JMenuItem();
    private JLabel logoLabel;
    private JPanel mainPanel = ( JPanel ) this.getContentPane();
    private JMenuItem modClassMenuItem = new JMenuItem();
    private GeneSetTablePanel oPanel;
    private JPanel progInPanel = new JPanel();
    private JPanel progressPanel;
    private JMenuItem quitMenuItem = new JMenuItem();
    private Map rawDataSets;
    private List results = new LinkedList();
    private JMenuItem runAnalysisMenuItem = new JMenuItem();
    private JMenu runViewMenu = new JMenu();
    private JMenuItem saveAnalysisMenuItem = new JMenuItem();
    private Settings settings;
    private boolean showingUserGeneSets = false;
    private JMenuItem showUsersMenuItem = new JMenuItem();
    private StatusViewer statusMessenger;
    private JTabbedPane tabs = new JTabbedPane();
    private GeneSetTreePanel treePanel;
    private Collection userOverwrittenGeneSets;
    JProgressBar progressBar = new JProgressBar();
    private JMenuItem reloadGeneSetsMenuItem = new JMenuItem();
    private JMenuItem switchDataFileMenuItem = new JMenuItem();
    private JMenuItem switchGeneScoreFileMenuItem = new JMenuItem();
    private boolean cancelled = false;

    /**
     * @throws IOException
     */
    public GeneSetScoreFrame() throws IOException {
        settings = new Settings();
        jbInit();
        hh = new HelpHelper();
        hh.initHelp( helpMenuItem );
        this.userOverwrittenGeneSets = new HashSet();
    }

    public void addedNewGeneSet() {
        oPanel.addedNewGeneSet();
        treePanel.addedNewGeneSet();
        refreshShowUserGeneSetState();
    }

    public void addResult( GeneSetPvalRun result ) {
        if ( result == null || result.getResults().size() == 0 ) return;
        result.setName( "Run " + ( results.size() + 1 ) );
        results.add( result );
        this.updateRunViewMenu();
        oPanel.addRun();
        treePanel.addRun();
        athread = null;
    }

    /**
     * Designate a gene set as altered by the user. This is used for GO gene sets that are modified.
     * 
     * @param id
     */
    public void addUserOverwritten( String id ) {
        this.userOverwrittenGeneSets.add( id );
    }

    /**
     * @param classID
     */
    public void deleteUserGeneSet( String classID ) {
        deleteUserGeneSetFile( classID );
        treePanel.removeNode( classID );
        this.userOverwrittenGeneSets.remove( classID );
        refreshShowUserGeneSetState();
    }

    protected void deleteUserGeneSetFile( String classID ) {
        UserDefinedGeneSetManager ngs = new UserDefinedGeneSetManager( geneData, settings, classID );
        if ( ngs.deleteUserGeneSet() && this.statusMessenger != null ) {
            statusMessenger.showStatus( "Permanantly deleted " + classID );
        } else {
            GuiUtil.error( "Could not delete file for " + classID + ". Please delete the file manually from "
                    + settings.getUserGeneSetDirectory() );
        }
    }

    /**
     * 
     */
    private void refreshShowUserGeneSetState() {
        if ( this.showingUserGeneSets ) {
            geneData.setSelectedSets( goData.getUserDefinedGeneSets() );
        } else {
            geneData.resetSelectedSets();
        }
    }

    public void disableMenusForAnalysis() {
        defineClassMenuItem.setEnabled( false );
        modClassMenuItem.setEnabled( false );
        runAnalysisMenuItem.setEnabled( false );
        loadAnalysisMenuItem.setEnabled( false );
        saveAnalysisMenuItem.setEnabled( false );
        cancelAnalysisMenuItem.setEnabled( true );
    }

    public void disableMenusForLoad() {
        fileMenu.setEnabled( false );
        classMenu.setEnabled( false );
        analysisMenu.setEnabled( false );
        helpMenu.setEnabled( false );
    }

    public void enableMenusForAnalysis() {
        defineClassMenuItem.setEnabled( true );
        modClassMenuItem.setEnabled( true );
        runAnalysisMenuItem.setEnabled( true );
        loadAnalysisMenuItem.setEnabled( true );
        maybeEnableRunViewMenu();
        if ( results.size() > 0 ) saveAnalysisMenuItem.setEnabled( true );
        cancelAnalysisMenuItem.setEnabled( false );
    }

    /**
     * @param classID
     */
    public void findGeneSetInTree( String classID ) {
        if ( !treePanel.expandToGeneSet( classID ) ) return;
        this.tabs.setSelectedIndex( 1 );
        this.maybeEnableRunViewMenu();
    }

    /**
     * @return
     */
    public int getCurrentResultSet() {
        return this.currentResultSet;
    }

    /**
     * @return Returns the geneDataSets.
     */
    public Map getGeneDataSets() {
        return geneDataSets;
    }

    /**
     * @return Returns the oPanel.
     */
    public GeneSetTablePanel getOPanel() {
        return oPanel;
    }

    /**
     * Get the original, "fresh" gene annotation data.
     * 
     * @return
     */
    public GeneAnnotations getOriginalGeneData() {
        return ( GeneAnnotations ) geneDataSets.get( new Integer( "original".hashCode() ) );
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
     * @return Returns the treePanel.
     */
    public GeneSetTreePanel getTreePanel() {
        return this.treePanel;
    }

    /**
     * Called by the startupDialog.
     */
    public void initialize() {
        try {
            mainPanel.add( progressPanel, BorderLayout.CENTER );

            rawDataSets = new HashMap();
            geneDataSets = new HashMap();
            geneScoreSets = new HashMap();

            readDataFilesForStartup();

            statusMessenger.showStatus( "Done with setup" );

            enableMenusOnStart();

            mainPanel.remove( progressPanel );
            mainPanel.add( tabs, BorderLayout.CENTER );
            statusMessenger.showStatus( "Ready." );

        } catch ( IllegalArgumentException e ) {
            GuiUtil.error( "Error during initialization: " + e
                    + "\nTry again.\nIf this problem persists, please contact the software developer. " );
        }
        treePanel.initialize( goData, geneData );
        loadUserGeneSets();

        oPanel.addInitialData( goData );
        statusMessenger.showStatus( "Done with initialization." );
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

        this.athread = new AnalysisThread( loadSettings, statusMessenger, goData, geneDataSets, rawDataSets,
                geneScoreSets, loadFile );
        athread.run();
        log.debug( "Waiting" );
        addResult( athread.getLatestResults() );
        log.debug( "done" );
        enableMenusForAnalysis();
    }

    public void readDataFilesForStartup() {

        updateProgress( 10 );
        statusMessenger.showStatus( "Reading GO descriptions " + settings.getClassFile() );
        assert settings.getClassFile() != null;
        try {
            goData = new GONames( settings.getClassFile() );
        } catch ( SAXException e ) {
            GuiUtil.error( "Gene Ontology file format is incorrect. " + "\nPlease check that it is a valid XML file. "
                    + "\nIf this problem persists, please contact the software developer. " );
        } catch ( IOException e ) {
            GuiUtil.error( "GO reading error during initialization: " + e.getMessage()
                    + "\nCheck the file format.\nIf this problem persists, please contact the software developer. " );
        }

        updateProgress( 70 );
        if ( Thread.currentThread().isInterrupted() ) return;

        statusMessenger.showStatus( "Reading gene annotations from " + settings.getAnnotFile() );

        try {
            geneData = new GeneAnnotations( settings.getAnnotFile(), statusMessenger, goData, settings.getAnnotFormat() );
        } catch ( IOException e ) {
            GuiUtil.error( "Gene annotation reading error during initialization: " + e.getMessage()
                    + "\nCheck the file format.\nIf this problem persists, please contact the software developer. " );
        }

        geneDataSets.put( new Integer( "original".hashCode() ), geneData );

        updateProgress( 90 );
        if ( Thread.currentThread().isInterrupted() ) return;

        statusMessenger.showStatus( "Reading user-defined gene sets from directory "
                + settings.getUserGeneSetDirectory() );

        // end slow part.

        if ( geneData.getGeneSets().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no gene set information. "
                    + "Check that the file format is correct.\n" );
        }

        if ( geneData.getGenes().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no probes. "
                    + "Check that the file format is correct.\n" );
        }
    }

    /**
     * 
     */
    protected void loadUserGeneSets() {
        UserDefinedGeneSetManager loader = new UserDefinedGeneSetManager( geneData, settings, "" );
        this.userOverwrittenGeneSets = loader.loadUserGeneSets( this.goData, this.statusMessenger );
        for ( Iterator iter = goData.getUserDefinedGeneSets().iterator(); iter.hasNext(); ) {
            String id = ( String ) iter.next();
            treePanel.addNode( id, goData.getNameForId( id ) );
        }
    }

    /**
     * @param classID
     */
    public void restoreUserGeneSet( String classID ) {
        userOverwrittenGeneSets.remove( classID );
        treePanel.removeUserDefinedNode( classID );
        oPanel.addedNewGeneSet();
        treePanel.addedNewGeneSet();
        refreshShowUserGeneSetState();
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
            GeneSetPvalRun element = ( GeneSetPvalRun ) results.get( i );
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

    /**
     * @param a String
     */
    public void showStatus( String a ) {
        jLabelStatus.setText( a );
    }

    public void startAnalysis( Settings runSettings ) {
        disableMenusForAnalysis();
        this.cancelled = false;
        this.athread = new AnalysisThread( runSettings, statusMessenger, goData, geneDataSets, rawDataSets,
                geneScoreSets );
        log.debug( "Starting analysis thread" );
        try {
            athread.start();
        } catch ( Exception e ) {
            GuiUtil
                    .error( "There was an unexpected error during analysis.\nSee the log file for details.\nThe summary message was:\n"
                            + e.getMessage() );
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
    private void checkForReasonableResults( GeneSetPvalRun results1 ) {
        if ( !athread.isFinishedNormally() ) return;
        int numZeroPvalues = 0;
        if ( results1 == null || results1.getResults() == null ) {
            GuiUtil.error( "There was an error during analysis - there were no valid results. Please check the logs." );
        }
        int numPvalues = results1.getResults().size();
        int numUnityPvalue = 0;
        for ( Iterator itr = results1.getResults().values().iterator(); itr.hasNext(); ) {
            GeneSetResult result = ( GeneSetResult ) itr.next();
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

    public void updateProgress( int val ) {
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

    private void enableMenusOnStart() {
        fileMenu.setEnabled( true );
        classMenu.setEnabled( true );
        analysisMenu.setEnabled( true );
        runViewMenu.setEnabled( false );
        helpMenu.setEnabled( true );
    }

    /**
     * Called by the constructor .
     */
    private void jbInit() {
        this.setDefaultCloseOperation( EXIT_ON_CLOSE );
        this.setJMenuBar( jMenuBar1 );
        this.setSize( new Dimension( 886, 450 ) );

        this.readPrefs();

        this.setTitle( "ErmineJ" );
        this.setIconImage( new ImageIcon( this.getClass().getResource( "resources/logoIcon64.gif" ) ).getImage() );
        mainPanel.setLayout( new BorderLayout() );
        mainPanel.setPreferredSize( new Dimension( 1000, 600 ) );
        mainPanel.setInputVerifier( null );
        progInPanel.setBackground( Color.white );
        progInPanel.setPreferredSize( new Dimension( 800, 26 ) );
        loadingPanel.setBackground( Color.white );
        loadingPanel.setForeground( Color.black );
        loadingPanel.setPreferredSize( new Dimension( 800, 200 ) );

        setupMenus();

        // initialization panel (replaced by main panel when done)
        logoLabel = new JLabel();
        logoLabel.setIcon( new ImageIcon( GeneSetScoreFrame.class.getResource( "resources/logo1small.gif" ) ) );

        progressPanel = new JPanel();
        progressPanel.setLayout( new FlowLayout() );

        JLabel label = new JLabel( "Please wait while the files are loaded in." );
        label.setPreferredSize( new Dimension( 500, 30 ) );
        label.setHorizontalTextPosition( SwingConstants.CENTER );
        label.setLabelFor( progressBar );
        label.setAlignmentX( ( float ) 0.0 );
        label.setHorizontalAlignment( SwingConstants.CENTER );

        progressBar.setPreferredSize( new Dimension( 300, 16 ) );
        progressBar.setIndeterminate( true );
        progressPanel.setBackground( Color.white );

        progressPanel.add( logoLabel );
        progressPanel.add( loadingPanel, null );
        loadingPanel.add( label, null );
        loadingPanel.add( progInPanel, null );
        progInPanel.add( progressBar, null );

        // main panel
        oPanel = new GeneSetTablePanel( this, results, settings );
        oPanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );
        treePanel = new GeneSetTreePanel( this, results, settings );
        treePanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );

        tabs.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );
        tabs.addMouseListener( new MouseAdapter() {
            public void mouseReleased( MouseEvent e ) {
                maybeEnableRunViewMenu();
            }
        } );
        tabs.addTab( "Table", oPanel );
        tabs.addTab( "Tree", treePanel );

        this.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                writePrefs();
            }
        } );

        // controls

        // status bar
        // File logFile = settings.getLogFile();
        jPanelStatus.setLayout( new BorderLayout() );
        jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
        jPanelStatus.setPreferredSize( new Dimension( STARTING_OVERALL_WIDTH, 33 ) );
        jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        jLabelStatus.setBorder( BorderFactory.createEmptyBorder( 5, 5, 10, 10 ) );
        jLabelStatus.setPreferredSize( new Dimension( 800, 19 ) );
        jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );

        jPanelStatus.add( jLabelStatus, BorderLayout.WEST );
        showStatus( "This window is not usable until you confirm the startup settings in the dialog box." );
        // StatusViewer s = new StatusJlabel( jLabelStatus );
        // this.statusMessenger = new StatusFileLogger( logFile.getAbsolutePath(), s );
        this.statusMessenger = new StatusJlabel( jLabelStatus );
        mainPanel.add( jPanelStatus, BorderLayout.SOUTH );
        oPanel.setMessenger( this.statusMessenger );
        treePanel.setMessenger( this.statusMessenger );
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
     * 
     */
    protected void writePrefs() {
        settings.getConfig().setProperty( MAINWINDOWWIDTH, String.valueOf( this.getWidth() ) );
        settings.getConfig().setProperty( MAINWINDOWHEIGHT, String.valueOf( this.getHeight() ) );
        settings.getConfig().setProperty( MAINWINDOWPOSITIONX, new Double( this.getLocation().getX() ) );
        settings.getConfig().setProperty( MAINWINDOWPOSITIONY, new Double( this.getLocation().getY() ) );
    }

    /**
     * 
     */
    protected void maybeEnableRunViewMenu() {
        if ( results.size() > 1 && tabs.getSelectedIndex() == 1 ) {
            runViewMenu.setEnabled( true );
        } else {
            runViewMenu.setEnabled( false );
        }
    }

    /**
     * 
     */
    private void setupMenus() {
        // menu stuff
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

        reloadGeneSetsMenuItem.setText( "Reload user-defined gene sets" );
        reloadGeneSetsMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                loadUserGeneSets();
                addedNewGeneSet();
            }
        } );
        reloadGeneSetsMenuItem.setMnemonic( 'E' );
        reloadGeneSetsMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E, InputEvent.CTRL_MASK ) );

        showUsersMenuItem.setText( "Show user-defined gene sets" );
        showUsersMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                showUserMenuItemActionPerformed();
            }
        } );
        showUsersMenuItem.setMnemonic( 'U' );
        showUsersMenuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_U, InputEvent.CTRL_MASK ) );

        classMenu.add( defineClassMenuItem );
        classMenu.add( modClassMenuItem );
        classMenu.add( findClassMenuItem );
        classMenu.add( findGeneMenuItem );
        classMenu.add( reloadGeneSetsMenuItem );
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

        analysisMenu.add( runAnalysisMenuItem );
        analysisMenu.add( cancelAnalysisMenuItem );
        analysisMenu.add( loadAnalysisMenuItem );
        analysisMenu.add( saveAnalysisMenuItem );
        analysisMenu.add( switchDataFileMenuItem );
        analysisMenu.add( switchGeneScoreFileMenuItem );

        helpMenu.setText( "Help" );
        helpMenu.setMnemonic( 'H' );
        helpMenuItem.setText( "Help Topics" );
        helpMenuItem.setMnemonic( 'T' );

        logMenuItem.setMnemonic( 'L' );
        logMenuItem.setText( "View log" );
        logMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                showLogs();
            }
        } );

        aboutMenuItem.setText( "About ErmineJ" );
        aboutMenuItem.setMnemonic( 'A' );
        aboutMenuItem.addActionListener( new GeneSetScoreFrame_aboutMenuItem_actionAdapter( this ) );
        helpMenu.add( helpMenuItem );
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
    protected void switchGeneScoreFile() {
        JFileChooser fchooser = new JFileChooser( settings.getGeneScoreFileDirectory() );
        fchooser.setDialogTitle( "Choose the gene score file or cancel." );
        int yesno = fchooser.showDialog( this, "Open" );

        if ( yesno == JFileChooser.APPROVE_OPTION ) {
            settings.setScoreFile( fchooser.getSelectedFile().getAbsolutePath() );
            geneData.resetSelectedProbes();
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
    protected void showLogs() {
        StringBuffer bif = new StringBuffer();
        try {
            BufferedReader fis = new BufferedReader( new FileReader( settings.getLogFile() ) );
            String line;
            while ( ( line = fis.readLine() ) != null ) {
                bif.append( line );
                bif.append( "\n" );
            }
            fis.close();
            ScrollingTextAreaDialog b = new ScrollingTextAreaDialog( this, "ErmineJ Log", true );
            b.setText( bif.toString() );
            b.setSize( new Dimension( 350, 500 ) );
            b.setResizable( true );
            b.setLocation( GuiUtil.chooseChildLocation( b, this ) );
            b.pack();
            b.validate();
            b.setVisible( true );
        } catch ( FileNotFoundException e ) {
            GuiUtil.error( "The log file could not be found" );
        } catch ( IOException e ) {
            GuiUtil.error( "There was an error reading the log file" );
        }
    }

    protected void showUserMenuItemActionPerformed() {
        if ( showingUserGeneSets ) {
            geneData.resetSelectedSets();
            showingUserGeneSets = false;
        } else {
            geneData.setSelectedSets( goData.getUserDefinedGeneSets() );
            showingUserGeneSets = true;
        }
        this.oPanel.resetView();
        this.treePanel.resetView();
        statusMessenger.showStatus( geneData.selectedSets() + " matching gene sets found." );
    }

    /**
     * 
     */
    protected void updateRunViewMenu() {
        log.debug( "Updating runViewMenu" );
        runViewMenu.removeAll();
        for ( Iterator iter = this.results.iterator(); iter.hasNext(); ) {
            GeneSetPvalRun resultSet = ( GeneSetPvalRun ) iter.next();
            String name = resultSet.getName();
            log.debug( "Adding " + name );
            JMenuItem newSet = new JMenuItem();
            newSet.setIcon( new ImageIcon( this.getClass().getResource( "resources/noCheckBox.gif" ) ) );
            newSet.addActionListener( new RunSet_Choose_ActionAdapter( this ) );
            newSet.setText( name );
            this.runViewMenu.add( newSet );
        }
        if ( runViewMenu.getItemCount() > 0 ) {
            runViewMenu.getItem( runViewMenu.getItemCount() - 1 ).setIcon(
                    new ImageIcon( this.getClass().getResource( "resources/checkBox.gif" ) ) );
        }
        runViewMenu.revalidate();
    }

    /**
     * Determine if a gene set was overwritten by the user-define gene sets on startup.
     * 
     * @param geneSetId
     * @return
     */
    public boolean userOverWrote( String geneSetId ) {
        return userOverwrittenGeneSets != null && userOverwrittenGeneSets.contains( geneSetId );
    }

    void aboutMenuItem_actionPerformed() {
        new AboutBox( this );
    }

    void defineClassMenuItem_actionPerformed() {
        GeneSetWizard cwiz = new GeneSetWizard( this, geneData, goData, true );
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
        this.cancelled = true;
        showStatus( "Ready" );

    }

    void findClassMenuItem_actionPerformed() {
        if ( findByNameDialog == null ) findByNameDialog = new FindDialog( this, geneData, goData );
        findByNameDialog.show();
    }

    void findGeneMenuItem_actionPerformed() {
        if ( findByGeneDialog == null ) findByGeneDialog = new FindByGeneDialog( this, geneData, goData );
        findByGeneDialog.show();
    }

    void loadAnalysisMenuItem_actionPerformed() {
        LoadDialog lgsd = new LoadDialog( this );
        lgsd.showDialog();
    }

    void modClassMenuItem_actionPerformed() {
        GeneSetWizard cwiz = new GeneSetWizard( this, geneData, goData, false );
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
        AnalysisWizard awiz = new AnalysisWizard( this, geneDataSets, goData );
        awiz.showWizard();
    }

    void saveAnalysisMenuItem_actionPerformed() {
        if ( results.size() == 0 ) {
            statusMessenger.showError( "There are no runs to save" );
            return;
        }
        SaveWizard swiz = new SaveWizard( this, results, goData );
        swiz.showWizard();
    }

}

// //////////////////////////////////////////////////////////////////////////////
/* end class */

class GeneSetScoreFrame_aboutMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_aboutMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.aboutMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.doCancel();
    }
}

class GeneSetScoreFrame_defineClassMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_defineClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.defineClassMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_findClassMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_findClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.findClassMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_findGeneMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_findGeneMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.findGeneMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.loadAnalysisMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_modClassMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_modClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.modClassMenuItem_actionPerformed();
    }
}

class GeneSetScoreFrame_quitMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_quitMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.quitMenuItem_actionPerformed( e );
    }
}

class GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.runAnalysisMenuItem_actionPerformed();
    }

}

class GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.saveAnalysisMenuItem_actionPerformed();
    }
}

class RunSet_Choose_ActionAdapter implements java.awt.event.ActionListener {
    private static Log log = LogFactory.getLog( RunSet_Choose_ActionAdapter.class.getName() );
    GeneSetScoreFrame adaptee;

    /**
     * @param adaptee
     */
    public RunSet_Choose_ActionAdapter( GeneSetScoreFrame adaptee ) {
        super();
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        JMenuItem source = ( JMenuItem ) e.getSource();
        source.setIcon( new ImageIcon( this.getClass().getResource( "resources/checkBox.gif" ) ) );
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
                jmi.setIcon( new ImageIcon( this.getClass().getResource( "resources/noCheckBox.gif" ) ) );
            }
        }
    }
}
