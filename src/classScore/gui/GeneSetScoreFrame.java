package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.xml.sax.SAXException;

import baseCode.gui.GuiUtil;
import baseCode.gui.StatusJlabel;
import baseCode.util.StatusViewer;
import classScore.AnalysisThread;
import classScore.Settings;
import classScore.classPvalRun;
import classScore.data.GONames;
import classScore.data.GeneAnnotations;
import classScore.data.GeneSetMapTools;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Homin Lee
 * @author Paul Pavlidis
 * @version $Id$
 * @todo All input of custom classes, identified either by probe id or official gene name.
 *
 */

public class GeneSetScoreFrame
    extends JFrame {
   final boolean CONSOLE_WINDOW = false;
   JPanel mainPanel = ( JPanel )this.getContentPane();
   JMenuBar jMenuBar1 = new JMenuBar();
   JMenu fileMenu = new JMenu();
   JMenuItem quitMenuItem = new JMenuItem();
   JMenu classMenu = new JMenu();
   JMenuItem defineClassMenuItem = new JMenuItem();
   JMenuItem modClassMenuItem = new JMenuItem();
   JMenuItem findClassMenuItem = new JMenuItem();
   JMenu analysisMenu = new JMenu();
   JMenuItem runAnalysisMenuItem = new JMenuItem();
   JMenuItem cancelAnalysisMenuItem = new JMenuItem();
   JMenuItem loadAnalysisMenuItem = new JMenuItem();
   JMenuItem saveAnalysisMenuItem = new JMenuItem();
   JMenu helpMenu = new JMenu();
   JMenuItem helpMenuItem = new JMenuItem();
   JMenuItem aboutMenuItem = new JMenuItem();

   JPanel progressPanel;
   JPanel progInPanel = new JPanel();
   JProgressBar progressBar = new JProgressBar();
   OutputPanel oPanel;


   JLabel jLabelStatus = new JLabel();
   JPanel jPanelStatus = new JPanel();

   Settings settings;
   StatusViewer statusMessenger;
   GONames goData;
   GeneAnnotations geneData;
   LinkedList results = new LinkedList();

   Map geneDataSets;
   Map rawDataSets;
   Map geneScoreSets;

   AnalysisThread athread=new AnalysisThread();

   public GeneSetScoreFrame() {
      try {
         jbInit();
         settings = new Settings();
         StartupDialog sdlog = new StartupDialog( this );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   /* init */
   private void jbInit() throws Exception {
      if ( CONSOLE_WINDOW ) {
         ConsoleWindow.init();
      }
      this.setDefaultCloseOperation( EXIT_ON_CLOSE );
      this.setJMenuBar( jMenuBar1 );
      this.setSize( new Dimension( 886, 450 ) );
      this.setTitle( "Functional Class Scoring" );
      BorderLayout borderLayout1 = new BorderLayout();
      mainPanel.setLayout(borderLayout1);
      mainPanel.setPreferredSize( new Dimension( 1000, 600 ) );
      mainPanel.setInputVerifier( null );

      //menu stuff
      fileMenu.setText( "File" );
      fileMenu.setMnemonic( 'F' );
      quitMenuItem.setText( "Quit" );
      quitMenuItem.addActionListener( new
                                      GeneSetScoreFrame_quitMenuItem_actionAdapter( this ) );
      quitMenuItem.setMnemonic( 'Q' );
      quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,InputEvent.CTRL_MASK));
      fileMenu.add( quitMenuItem );
      classMenu.setText( "Classes" );
      classMenu.setMnemonic( 'C' );
      classMenu.setEnabled(false);
      defineClassMenuItem.setText( "Define New Class" );
      defineClassMenuItem.addActionListener( new
                                             GeneSetScoreFrame_defineClassMenuItem_actionAdapter( this ) );
      defineClassMenuItem.setMnemonic( 'D' );
      defineClassMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_MASK));
      modClassMenuItem.setText( "Modify Class" );
      modClassMenuItem.addActionListener( new
                                          GeneSetScoreFrame_modClassMenuItem_actionAdapter( this ) );
      modClassMenuItem.setMnemonic( 'M' );
      findClassMenuItem.setText( "Find Class" );
      findClassMenuItem.addActionListener( new
                                          GeneSetScoreFrame_findClassMenuItem_actionAdapter( this ) );
      findClassMenuItem.setMnemonic( 'F' );
      findClassMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK));
      classMenu.add( defineClassMenuItem );
      classMenu.add( modClassMenuItem );
      classMenu.add( findClassMenuItem );
      analysisMenu.setText( "Analysis" );
      analysisMenu.setMnemonic( 'A' );
      analysisMenu.setEnabled(false);
      runAnalysisMenuItem.setText( "Run Analysis" );
      runAnalysisMenuItem.addActionListener( new
                                             GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( this ) );
      runAnalysisMenuItem.setMnemonic( 'R' );
      runAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK));
      cancelAnalysisMenuItem.setText( "Cancel Analysis" );
      cancelAnalysisMenuItem.addActionListener( new
                                             GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( this ) );
      cancelAnalysisMenuItem.setMnemonic( 'C' );
      cancelAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_MASK));

      loadAnalysisMenuItem.setText( "Load Analysis" );
      loadAnalysisMenuItem.addActionListener( new
                                              GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( this ) );
      loadAnalysisMenuItem.setMnemonic( 'L' );
      loadAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK));
      saveAnalysisMenuItem.setText( "Save Analysis" );
      saveAnalysisMenuItem.addActionListener( new
                                              GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( this ) );
      saveAnalysisMenuItem.setMnemonic( 'S' );
      saveAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
      analysisMenu.add( runAnalysisMenuItem );
      analysisMenu.add( cancelAnalysisMenuItem );
      analysisMenu.add( loadAnalysisMenuItem );
      analysisMenu.add( saveAnalysisMenuItem );
      helpMenu.setText( "Help" );
      helpMenu.setMnemonic( 'H' );
      helpMenuItem.setText( "Help Topics" );
      helpMenuItem.setMnemonic( 'T' );
      helpMenuItem.addActionListener( new
                                      GeneSetScoreFrame_helpMenuItem_actionAdapter( this ) );
      aboutMenuItem.setText( "About ErmineJ" );
      aboutMenuItem.setMnemonic( 'A' );
      aboutMenuItem.addActionListener( new
                                       GeneSetScoreFrame_aboutMenuItem_actionAdapter( this ) );
      helpMenu.add( helpMenuItem );
      helpMenu.add( aboutMenuItem );
      jMenuBar1.add( fileMenu );
      jMenuBar1.add( classMenu );
      jMenuBar1.add( analysisMenu );
      jMenuBar1.add( helpMenu );

      //initialization panel (replaced by main panel when done)
      progressPanel = new JPanel();
      progressPanel.setPreferredSize( new Dimension( 830, 330 ) );
      GridBagLayout gridBagLayout1 = new GridBagLayout();
      progressPanel.setLayout(gridBagLayout1);
      progInPanel.setPreferredSize(new Dimension(350, 100));
      JLabel label= new JLabel("Please wait while the files are loaded in.");
      label.setPreferredSize(new Dimension(195, 30));
      progressBar.setPreferredSize(new Dimension(300, 16));
      progressBar.setIndeterminate(true);
      progInPanel.add(label, null);
      progInPanel.add(progressBar, null);
      progressPanel.add(progInPanel,     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(114, 268, 87, 268), 0, 0));

      //main panel
      oPanel = new OutputPanel( this, results );
      oPanel.setPreferredSize( new Dimension( 830, 330 ) );

      //controls

      //status bar
      jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
      jPanelStatus.setPreferredSize( new Dimension( 830, 33 ) );
      jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
      jLabelStatus.setPreferredSize(new Dimension(800, 19) );
      jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
      jLabelStatus.setText( "Status" );
      jPanelStatus.add( jLabelStatus, null );
      showStatus( "Please see 'About this software' for license information." );
      statusMessenger = new StatusJlabel( jLabelStatus );

      //mainPanel.add( oPanel, BorderLayout.CENTER );
      mainPanel.add( progressPanel, BorderLayout.CENTER );
      mainPanel.add( jPanelStatus, BorderLayout.SOUTH );
   }

   private void enableMenusOnStart()
   {
      classMenu.setEnabled( true );
      analysisMenu.setEnabled( true );
      helpMenu.setEnabled(true);
   }

   public void disableMenusForAnalysis()
   {
      defineClassMenuItem.setEnabled(false);
      modClassMenuItem.setEnabled(false);
      runAnalysisMenuItem.setEnabled(false);
      loadAnalysisMenuItem.setEnabled(false);
      saveAnalysisMenuItem.setEnabled(false);
   }

   public void enableMenusForAnalysis()
   {
      defineClassMenuItem.setEnabled(true);
      modClassMenuItem.setEnabled(true);
      runAnalysisMenuItem.setEnabled(true);
      loadAnalysisMenuItem.setEnabled(true);
      saveAnalysisMenuItem.setEnabled(true);
   }

   public void initialize() {
      try {
         rawDataSets = new HashMap();
         geneDataSets = new HashMap();
         geneScoreSets = new HashMap();

         statusMessenger.setStatus("Reading GO descriptions " + settings.getClassFile());
         goData = new GONames(settings.getClassFile()); // parse go name file
         statusMessenger.setStatus("Reading gene annotations from " + settings.getAnnotFile());
         geneData = new GeneAnnotations(settings.getAnnotFile());
         statusMessenger.setStatus( "Initializing gene class mapping" );
         GeneSetMapTools.collapseClasses(geneData);
         geneData.sortGeneSets();

         geneDataSets.put(new Integer("original".hashCode()) , geneData);

         statusMessenger.setStatus("Done with setup");
         enableMenusOnStart();
         mainPanel.remove( progressPanel );
         mainPanel.add( oPanel, BorderLayout.CENTER );
         statusMessenger.setStatus("Ready.");
      }
      catch ( IllegalArgumentException e ) {
         GuiUtil.error( e, "During initialization. Press OK to quit." );
         System.exit(0);
      }
      catch ( IOException e ) {
         GuiUtil.error( e, "File reading or writing. Press OK to quit." );
         System.exit(0);
      } catch ( SAXException e ) {
         GuiUtil.error( "Gene Ontology file format is incorrect. Please check that it is a valid XML file.  Press OK to quit." );
         System.exit(0);
      }
      oPanel.addInitialData( geneData, goData );
      statusMessenger.setStatus("Done with initialization.");
   }

   /**
    *
    * @param a
    */
   private void showStatus( String a ) {
      jLabelStatus.setText( a );
   }

   /**
    *
    */
   private void clearStatus() {
      jLabelStatus.setText( "" );
   }

   /**
    *
    */

   void quitMenuItem_actionPerformed( ActionEvent e ) {
      System.exit( 0 );
   }

   void defineClassMenuItem_actionPerformed( ActionEvent e ) {
      GeneSetWizard cwiz = new GeneSetWizard(this, geneData, goData, true);
      cwiz.showWizard();
   }

   void modClassMenuItem_actionPerformed( ActionEvent e ) {
      GeneSetWizard cwiz = new GeneSetWizard(this, geneData, goData, false);
      cwiz.showWizard();
   }

   void findClassMenuItem_actionPerformed( ActionEvent e ) {
  //    FindDialog fdlog = new FindDialog( this );
   }

   void runAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      AnalysisWizard awiz = new AnalysisWizard(this, geneDataSets, goData);
      awiz.showWizard();
   }

   void cancelAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      athread.cancelAnalysisThread();
      showStatus( "Ready" );
   }

   void loadAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      LoadDialog sdlog = new LoadDialog( this );
   }

   void saveAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      SaveWizard swiz = new SaveWizard( this, results, goData );
      swiz.showWizard();
   }

   void helpMenuItem_actionPerformed( ActionEvent e ) {
   }

   void aboutMenuItem_actionPerformed( ActionEvent e ) {
      AboutBox dlg = new AboutBox( this );
   }

   public Settings getSettings() {
      return settings;
   }

   public void setSettings(Settings settings) {
      this.settings=settings;
   }

   public StatusViewer getStatusMessenger(){
      return statusMessenger;
   }

   public void addResult(classPvalRun result)
   {
      results.add( result );
      oPanel.addRun();  // this line should come after results.add() or else you'll get errors
   }

   public void startAnalysis(Settings runSettings)
   {
      disableMenusForAnalysis();
      athread.startAnalysisThread(this,runSettings,statusMessenger,goData,geneDataSets, rawDataSets, geneScoreSets);
   }

   public void loadAnalysis(String loadFile)
   {
      disableMenusForAnalysis();
      Settings loadSettings = new Settings(loadFile);
      athread.loadAnalysisThread(this,loadSettings,statusMessenger,goData,geneDataSets, rawDataSets, geneScoreSets,loadFile);
   }
}

/* end class */

class GeneSetScoreFrame_quitMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_quitMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.quitMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_defineClassMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_defineClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.defineClassMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_modClassMenuItem_actionAdapter
    implements java.awt.event.
    ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_modClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.modClassMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_findClassMenuItem_actionAdapter
    implements java.awt.event.
    ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_findClassMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.findClassMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_runAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.runAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_cancelAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.cancelAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_loadAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.loadAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_saveAnalysisMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.saveAnalysisMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_helpMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_helpMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.helpMenuItem_actionPerformed( e );
   }
}

class GeneSetScoreFrame_aboutMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   GeneSetScoreFrame adaptee;

   GeneSetScoreFrame_aboutMenuItem_actionAdapter( GeneSetScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.aboutMenuItem_actionPerformed( e );
   }
}


