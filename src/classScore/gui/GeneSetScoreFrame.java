package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.GuiUtil;
import baseCode.gui.StatusJlabel;
import baseCode.util.FileTools;
import baseCode.util.StatusViewer;
import classScore.AnalysisThread;
import classScore.GeneSetPvalRun;
import classScore.Settings;
import classScore.data.UserDefinedGeneSetManager;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
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
    private JPanel mainPanel = ( JPanel ) this.getContentPane();
    private JMenuBar jMenuBar1 = new JMenuBar();
    private JMenu fileMenu = new JMenu();
    private JMenuItem quitMenuItem = new JMenuItem();
    private JMenu classMenu = new JMenu();
    private JMenuItem defineClassMenuItem = new JMenuItem();
    private JMenuItem modClassMenuItem = new JMenuItem();
    private JMenuItem findClassMenuItem = new JMenuItem();
    private JMenuItem findGeneMenuItem = new JMenuItem();
    private JMenu analysisMenu = new JMenu();
    private JMenuItem runAnalysisMenuItem = new JMenuItem();
    private JMenuItem cancelAnalysisMenuItem = new JMenuItem();
    private JMenuItem loadAnalysisMenuItem = new JMenuItem();
    private JMenuItem saveAnalysisMenuItem = new JMenuItem();
    private JMenu runViewMenu = new JMenu();
    private JMenu helpMenu = new JMenu();
    private JMenuItem helpMenuItem = new JMenuItem();
    private JMenuItem aboutMenuItem = new JMenuItem();
    private final JFileChooser fc = new JFileChooser();
    private JTabbedPane tabs = new JTabbedPane();
    private JPanel progressPanel;
    private JPanel progInPanel = new JPanel();
    JProgressBar progressBar = new JProgressBar();
    private GeneSetTablePanel oPanel;

    private JLabel jLabelStatus = new JLabel();
    private JPanel jPanelStatus = new JPanel();

    private Settings settings;
    private StatusViewer statusMessenger;
    private GONames goData;
    private GeneAnnotations geneData = null;
    private List results = new LinkedList();

    private Map geneDataSets;
    private Map rawDataSets;
    private Map geneScoreSets;

    private JLabel logoLabel;

    private AnalysisThread athread;
    private JPanel loadingPanel = new JPanel();
    private GeneSetTreePanel treePanel;

    private HelpHelper hh;
    private int currentResultSet;

    public GeneSetScoreFrame() throws IOException {
        settings = new Settings();
        jbInit();
        hh = new HelpHelper();
        hh.initHelp( helpMenuItem );
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
     * Called by the constructor .
     */
    private void jbInit() {
        this.setDefaultCloseOperation( EXIT_ON_CLOSE );
        this.setJMenuBar( jMenuBar1 );
        this.setSize( new Dimension( 886, 450 ) );
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
        progressBar.setIndeterminate( false );
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
        tabs.addMouseListener( new MouseListener() {

            public void mouseClicked( MouseEvent e ) {
            }

            public void mouseEntered( MouseEvent e ) {
            }

            public void mouseExited( MouseEvent e ) {
            }

            public void mousePressed( MouseEvent e ) {
            }

            public void mouseReleased( MouseEvent e ) {
                maybeEnableRunViewMenu();
            }
        } );
        tabs.addTab( "Table", oPanel );
        tabs.addTab( "Tree", treePanel );

        // controls

        // status bar
        jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
        jPanelStatus.setPreferredSize( new Dimension( STARTING_OVERALL_WIDTH, 33 ) );
        jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        jLabelStatus.setPreferredSize( new Dimension( 800, 19 ) );
        jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
        jPanelStatus.add( jLabelStatus, null );
        showStatus( "This window is not usable until you confirm the startup settings in the dialog box." );
        statusMessenger = new StatusJlabel( jLabelStatus );
        mainPanel.add( jPanelStatus, BorderLayout.SOUTH );
        oPanel.setMessenger( this.statusMessenger );
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
        ;

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

        this.runViewMenu.setText( "Results" );
        runViewMenu.setMnemonic( 'R' );
        runViewMenu.setEnabled( false );

        classMenu.add( defineClassMenuItem );
        classMenu.add( modClassMenuItem );
        classMenu.add( findClassMenuItem );
        classMenu.add( findGeneMenuItem );

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
        analysisMenu.add( runAnalysisMenuItem );
        analysisMenu.add( cancelAnalysisMenuItem );
        analysisMenu.add( loadAnalysisMenuItem );
        analysisMenu.add( saveAnalysisMenuItem );

        // resultsSetMenu = new JMenu();
        // resultsSetMenu.setMnemonic('R');
        // resultsSetMenu.setEnabled(false);

        helpMenu.setText( "Help" );
        helpMenu.setMnemonic( 'H' );
        helpMenuItem.setText( "Help Topics" );
        helpMenuItem.setMnemonic( 'T' );
        aboutMenuItem.setText( "About ErmineJ" );
        aboutMenuItem.setMnemonic( 'A' );
        aboutMenuItem.addActionListener( new GeneSetScoreFrame_aboutMenuItem_actionAdapter( this ) );
        helpMenu.add( helpMenuItem );
        helpMenu.add( aboutMenuItem );
        jMenuBar1.add( fileMenu );
        jMenuBar1.add( classMenu );
        jMenuBar1.add( analysisMenu );
        jMenuBar1.add( runViewMenu );
        jMenuBar1.add( helpMenu );
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

    private void enableMenusOnStart() {
        fileMenu.setEnabled( true );
        classMenu.setEnabled( true );
        analysisMenu.setEnabled( true );
        runViewMenu.setEnabled( false );
        helpMenu.setEnabled( true );
    }

    public void disableMenusForLoad() {
        fileMenu.setEnabled( false );
        classMenu.setEnabled( false );
        analysisMenu.setEnabled( false );
        helpMenu.setEnabled( false );
    }

    public void disableMenusForAnalysis() {
        defineClassMenuItem.setEnabled( false );
        modClassMenuItem.setEnabled( false );
        runAnalysisMenuItem.setEnabled( false );
        loadAnalysisMenuItem.setEnabled( false );
        saveAnalysisMenuItem.setEnabled( false );
        cancelAnalysisMenuItem.setEnabled( true );
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
     * 
     */
    private void maybeEnableRunViewMenu() {
        if ( results.size() > 1 && tabs.getSelectedIndex() == 1 ) {
            runViewMenu.setEnabled( true );
        } else {
            runViewMenu.setEnabled( false );
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

    public void readDataFilesForStartup() {

        updateProgress( 10 );
        statusMessenger.setStatus( "Reading GO descriptions " + settings.getClassFile() );

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

        statusMessenger.setStatus( "Reading gene annotations from " + settings.getAnnotFile() );

        try {
            geneData = new GeneAnnotations( settings.getAnnotFile(), statusMessenger, goData, settings.getAnnotFormat() );
        } catch ( IOException e ) {
            GuiUtil.error( "Gene annotation reading error during initialization: " + e.getMessage()
                    + "\nCheck the file format.\nIf this problem persists, please contact the software developer. " );
        }

        geneDataSets.put( new Integer( "original".hashCode() ), geneData );

        updateProgress( 90 );
        if ( Thread.currentThread().isInterrupted() ) return;

        statusMessenger.setStatus( "Reading user-defined gene sets from directory "
                + settings.getUserGeneSetDirectory() );

        loadUserGeneSets();

        // end slow part.

        if ( geneData.getGeneSetToProbeMap().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no gene set information. "
                    + "Check that the file format is correct.\n" );
        }

        if ( geneData.getGeneToProbeList().size() == 0 ) {
            throw new IllegalArgumentException( "The gene annotation file contains no probes. "
                    + "Check that the file format is correct.\n" );
        }

    }

    /**
     * Load the user-defined gene sets.
     */
    private void loadUserGeneSets() {
        File dir = new File( settings.getUserGeneSetDirectory() );
        if ( dir.exists() ) {
            String[] classFiles = dir.list();
            for ( int i = 0; i < classFiles.length; i++ ) {
                String classFile = classFiles[i];
                UserDefinedGeneSetManager ngs = new UserDefinedGeneSetManager( geneData, settings, null );
                try {
                    classFile = settings.getUserGeneSetDirectory() + System.getProperty( "file.separator" ) + classFile;
                    log.debug( "Loading " + classFile );
                    ngs.loadUserGeneSet( classFile );
                    ngs.addToMaps( goData );
                } catch ( IOException e ) {
                    statusMessenger.setError( "Could not load user-defined class from " + classFile );
                }
            }
        }
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

            statusMessenger.setStatus( "Done with setup" );

            enableMenusOnStart();

            mainPanel.remove( progressPanel );
            mainPanel.add( tabs, BorderLayout.CENTER );
            statusMessenger.setStatus( "Ready." );

        } catch ( IllegalArgumentException e ) {
            GuiUtil.error( "Error during initialization: " + e
                    + "\nTry again.\nIf this problem persists, please contact the software developer. " );
        }
        treePanel.initialize( goData, geneData );
        oPanel.addInitialData( goData );
        statusMessenger.setStatus( "Done with initialization." );
    }

    /**
     * @param a String
     */
    public void showStatus( String a ) {
        jLabelStatus.setText( a );
    }

    /**
     *
     */
    private void clearStatus() {
        jLabelStatus.setText( "" );
    }

    /**
     * @param e ActionEvent
     */
    void quitMenuItem_actionPerformed( ActionEvent e ) {
        System.exit( 0 );
    }

    void defineClassMenuItem_actionPerformed() {
        GeneSetWizard cwiz = new GeneSetWizard( this, geneData, goData, true );
        cwiz.showWizard();
    }

    void modClassMenuItem_actionPerformed() {
        GeneSetWizard cwiz = new GeneSetWizard( this, geneData, goData, false );
        cwiz.showWizard();
    }

    void findClassMenuItem_actionPerformed() {
        new FindDialog( this, geneData, goData );
    }

    void findGeneMenuItem_actionPerformed() {
        new FindByGeneDialog( this, geneData, goData );
    }

    void runAnalysisMenuItem_actionPerformed() {
        AnalysisWizard awiz = new AnalysisWizard( this, geneDataSets, goData );
        awiz.showWizard();
    }

    void doCancel() {
        log.debug( "Got cancel" );
        assert athread != null : "Attempt to cancel a null analysis thread";
        athread.interrupt();
        athread.setStop( true );
        enableMenusForAnalysis();
        showStatus( "Ready" );
    }

    void loadAnalysisMenuItem_actionPerformed() {
        LoadDialog lgsd = new LoadDialog( this );
        lgsd.showDialog();
    }

    void saveAnalysisMenuItem_actionPerformed() {
        if ( results.size() == 0 ) {
            statusMessenger.setError( "There are no runs to save" );
            return;
        }
        SaveWizard swiz = new SaveWizard( this, results, goData );
        swiz.showWizard();
    }

    void aboutMenuItem_actionPerformed() {
        new AboutBox( this );
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings( Settings settings ) {
        this.settings = settings;
    }

    public StatusViewer getStatusMessenger() {
        return statusMessenger;
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

    public void startAnalysis( Settings runSettings ) {
        disableMenusForAnalysis();
        this.athread = new AnalysisThread( runSettings, statusMessenger, goData, geneDataSets, rawDataSets,
                geneScoreSets );
        log.debug( "Starting analysis thread" );
        athread.run();
        log.debug( "Waiting..." );
        addResult( athread.getLatestResults() );
        log.debug( "done" );
        enableMenusForAnalysis();
    }

    public void loadAnalysis( String loadFile ) throws IOException {
        disableMenusForAnalysis();
        Settings loadSettings = new Settings( loadFile );
        if ( !checkValid( loadSettings ) ) {
            GuiUtil.error( "Loading of the analysis cannot proceed without the file information." );
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

    /**
     * @param loadSettings
     */
    private boolean checkValid( Settings loadSettings ) {

        String file;

        file = checkFile( loadSettings.getRawFile() );
        if ( file == null ) return false;
        loadSettings.setRawFile( file );

        file = checkFile( loadSettings.getScoreFile() );
        if ( file == null ) return false;
        loadSettings.setScoreFile( file );

        return true;
    }

    /**
     * Check whether a file exists, and if not, prompt the user to enter one. The path is returned.
     * 
     * @param file
     * @return If the user doesn't locate the file, return null, otherwise the path to the file.
     */
    private String checkFile( String file ) {
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

    public void addedNewGeneSet() {
        oPanel.addedNewGeneSet();
        treePanel.addedNewGeneSet();
    }

    /**
     * @return Returns the oPanel.
     */
    public GeneSetTablePanel getOPanel() {
        return oPanel;
    }

    /**
     * @return Returns the geneDataSets.
     */
    public Map getGeneDataSets() {
        return geneDataSets;
    }

    /**
     * @return
     */
    public int getCurrentResultSet() {
        return this.currentResultSet;
    }

    /**
     * @param classID
     */
    public void findGeneSetInTree( String classID ) {
        this.tabs.setSelectedIndex( 1 );
        treePanel.expandToGeneSet( classID );
    }

    /**
     * @return Returns the treePanel.
     */
    public GeneSetTreePanel getTreePanel() {
        return this.treePanel;
    }

    /**
     * @param classID
     */
    public void deleteUserGeneSet( String classID ) {
        UserDefinedGeneSetManager ngs = new UserDefinedGeneSetManager( geneData, settings, classID );
        if ( ngs.deleteUserGeneSet( classID ) && this.statusMessenger != null ) {
            statusMessenger.setStatus( "Permanantly deleted " + classID );
        } else {
            GuiUtil.error( "Could not delete file for " + classID + ". Please delete the file manually from "
                    + settings.getUserGeneSetDirectory() );
        }
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

    /**
     * @return
     */
    public JMenu getRunViewMenu() {
        return this.runViewMenu;
    }

    /**
     * @param runIndex
     */
    public void setCurrentResultSet( int runIndex ) {
        this.currentResultSet = runIndex;
        treePanel.fireResultsChanged();
    }
}

// //////////////////////////////////////////////////////////////////////////////
/* end class */

class GeneSetScoreFrame_quitMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_quitMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.quitMenuItem_actionPerformed( e );
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

class GeneSetScoreFrame_modClassMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_modClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.modClassMenuItem_actionPerformed();
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

class GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.runAnalysisMenuItem_actionPerformed();
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

class GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.loadAnalysisMenuItem_actionPerformed();
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

class GeneSetScoreFrame_aboutMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_aboutMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.aboutMenuItem_actionPerformed();
    }
}
