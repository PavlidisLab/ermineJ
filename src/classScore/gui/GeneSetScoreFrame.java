package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
import javax.swing.JTree;
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
    private JMenu helpMenu = new JMenu();
    private JMenuItem helpMenuItem = new JMenuItem();
    private JMenuItem aboutMenuItem = new JMenuItem();
    private final JFileChooser fc = new JFileChooser();
    private JTabbedPane tabs = new JTabbedPane();
    private JPanel progressPanel;
    private JPanel progInPanel = new JPanel();
    JProgressBar progressBar = new JProgressBar();
    private OutputPanel oPanel;

    private JLabel jLabelStatus = new JLabel();
    private JPanel jPanelStatus = new JPanel();

    private Settings settings;
    private StatusViewer statusMessenger;
    private GONames goData;
    private GeneAnnotations geneData = null;
    private LinkedList results = new LinkedList();

    private Map geneDataSets;
    private Map rawDataSets;
    private Map geneScoreSets;

    private JLabel logoLabel;

    private AnalysisThread athread;
    JPanel loadingPanel = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    private GoTreePanel treePanel;

    private HelpHelper hh;

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

    /* init */
    private void jbInit() {
        this.setDefaultCloseOperation( EXIT_ON_CLOSE );
        this.setJMenuBar( jMenuBar1 );
        this.setSize( new Dimension( 886, 450 ) );
        this.setTitle( "ErmineJ" );
        BorderLayout borderLayout1 = new BorderLayout();
        mainPanel.setLayout( borderLayout1 );
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
        progressPanel.setLayout( flowLayout1 );

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
        oPanel = new OutputPanel( this, results, settings );
       
        oPanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );
        treePanel = new GoTreePanel( this, results );
        treePanel.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );

        tabs.setPreferredSize( new Dimension( START_WIDTH, START_HEIGHT ) );
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
        jMenuBar1.add( helpMenu );
    }

    private void enableMenusOnStart() {
        fileMenu.setEnabled( true );
        classMenu.setEnabled( true );
        analysisMenu.setEnabled( true );
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

        if ( results.size() > 0 ) {
            saveAnalysisMenuItem.setEnabled( true );
        }

        cancelAnalysisMenuItem.setEnabled( false );
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

    public void run() {

        try {
            updateProgress( 10 );
            statusMessenger.setStatus( "Reading GO descriptions " + settings.getClassFile() );
            goData = new GONames( settings.getClassFile() );

            updateProgress( 70 );
            if ( Thread.currentThread().isInterrupted() ) return;

            statusMessenger.setStatus( "Reading gene annotations from " + settings.getAnnotFile() );
            geneData = new GeneAnnotations( settings.getAnnotFile(), statusMessenger, goData, settings.getAnnotFormat() );

            updateProgress( 100 );
            if ( Thread.currentThread().isInterrupted() ) return;

            // end slow part.

            if ( geneData.getGeneSetToProbeMap().size() == 0 ) {
                throw new IllegalArgumentException( "The gene annotation file contains no gene set information. "
                        + "Check that the file format is correct.\n" );
            }

            if ( geneData.getGeneToProbeList().size() == 0 ) {
                throw new IllegalArgumentException( "The gene annotation file contains no probes. "
                        + "Check that the file format is correct.\n" );
            }

            geneDataSets.put( new Integer( "original".hashCode() ), geneData );

        } catch ( SAXException e ) {
            GuiUtil.error( "Gene Ontology file format is incorrect. " + "\nPlease check that it is a valid XML file. "
                    + "\nIf this problem persists, please contact the software developer. " + "\nPress OK to quit." );
            System.exit( 1 ); // FIXME - go back to the start.
        } catch ( IOException e ) {
            GuiUtil.error( "File reading or writing error during initialization: " + e.getMessage()
                    + "\nIf this problem persists, please contact the software developer. " + "\nPress OK to quit.", e );
            System.exit( 1 ); // FIXME - go back to the start
        } catch ( IllegalArgumentException e ) {
            GuiUtil.error( "Error during initialization: " + e
                    + "\nIf this problem persists, please contact the software developer. " + "\nPress OK to quit." );
            System.exit( 1 ); // FIXME - go back to the start
        }

    }

    public void initialize() {
        try {
            mainPanel.add( progressPanel, BorderLayout.CENTER );

            rawDataSets = new HashMap();
            geneDataSets = new HashMap();
            geneScoreSets = new HashMap();

            run();

            statusMessenger.setStatus( "Done with setup" );
            enableMenusOnStart();

            treePanel.initialize( goData );

            mainPanel.remove( progressPanel );
            mainPanel.add( tabs, BorderLayout.CENTER );

            // mainPanel.add( oPanel, BorderLayout.CENTER );
            statusMessenger.setStatus( "Ready." );

        } catch ( Exception e ) {
            e.printStackTrace();
        }
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
        results.add( result );
        oPanel.addRun();
    }

    public void startAnalysis( Settings runSettings ) {
        disableMenusForAnalysis();

        assert athread == null : "Analysis running already!     ";
        this.athread = new AnalysisThread( runSettings, statusMessenger, goData, geneDataSets, rawDataSets,
                geneScoreSets );
        log.debug( "Starting analysis thread" );
        athread.run();
        log.debug( "Waiting" );
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
    }

    /**
     * @return Returns the oPanel.
     */
    public OutputPanel getOPanel() {
        return oPanel;
    }

    /**
     * @return Returns the geneDataSets.
     */
    public Map getGeneDataSets() {
        return geneDataSets;
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

class GeneSetScoreFrame_aboutMenuItem_actionAdapter implements java.awt.event.ActionListener {
    GeneSetScoreFrame adaptee;

    GeneSetScoreFrame_aboutMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.aboutMenuItem_actionPerformed();
    }
}
