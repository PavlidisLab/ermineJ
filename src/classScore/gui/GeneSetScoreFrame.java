package classScore.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import baseCode.gui.*;
import classScore.*;
import classScore.data.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo All input of custom classes, identified either by probe id or official gene name.
 * @todo (6/23/04 Homin) probePvalMapper includes too many probes (ones that don't have pvals).
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
   GeneSetScoreStatus statusMessenger;
   GONames goData;
   GeneAnnotations geneData;
   Vector results = new Vector();

   AnalysisThread athread=new AnalysisThread();
   //javax.swing.Timer initMonitor;

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
      mainPanel.setPreferredSize( new Dimension( 1000, 600 ) );
      mainPanel.setInputVerifier( null );

      //menu stuff
      fileMenu.setText( "File" );
      fileMenu.setMnemonic( 'F' );
      fileMenu.setEnabled(false);
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
      modClassMenuItem.setText( "Modify Class" );
      modClassMenuItem.addActionListener( new
                                          GeneSetScoreFrame_modClassMenuItem_actionAdapter( this ) );
      modClassMenuItem.setMnemonic( 'M' );
      classMenu.add( defineClassMenuItem );
      classMenu.add( modClassMenuItem );
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
      helpMenu.setEnabled(false);
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
      jLabelStatus.setPreferredSize( new Dimension( 500, 19 ) );
      jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
      jLabelStatus.setText( "Status" );
      jPanelStatus.add( jLabelStatus, null );
      showStatus( "Please see 'About this software' for license information." );
      statusMessenger = new GeneSetScoreStatus( jLabelStatus );

      mainPanel.add( progressPanel, BorderLayout.NORTH );
      mainPanel.add( jPanelStatus, BorderLayout.SOUTH );
   }

   private void enableMenus()
   {
      fileMenu.setEnabled(true);
      classMenu.setEnabled( true );
      analysisMenu.setEnabled( true );
      helpMenu.setEnabled(true);
   }

   public void initialize() {
      try {
         statusMessenger.setStatus("Reading GO descriptions " + settings.getClassFile());
         goData = new GONames(settings.getClassFile()); // parse go name file
         statusMessenger.setStatus("Reading gene annotations from " + settings.getAnnotFile());
         geneData = new GeneAnnotations(settings.getAnnotFile());
         statusMessenger.setStatus( "Initializing gene class mapping" );
         GeneSetMapTools.collapseClasses(geneData.getClassToProbeMap());
         geneData.sortGeneSets();
         statusMessenger.setStatus("Done with setup");
         enableMenus();
         mainPanel.remove( progressPanel );
         mainPanel.add( oPanel, BorderLayout.NORTH );
         statusMessenger.setStatus("Ready.");
      }
      catch ( IllegalArgumentException e ) {
         GuiUtil.error( e, "During initialization" );
      }
      catch ( IOException e ) {
         GuiUtil.error( e, "File reading or writing" );
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

   void runAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      AnalysisWizard awiz = new AnalysisWizard(this,geneData,goData);
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
      SaveWizard swiz = new SaveWizard( this, results );
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

   public GeneSetScoreStatus getStatusMessenger(){
      return statusMessenger;
   }

   public void addResult(classPvalRun result)
   {
      results.add( result );
      oPanel.addRun();  // this line should come after results.add() or else you'll get errors
   }

   public void startAnalysis(Settings runSettings)
   {
      athread.startAnalysisThread(this,runSettings,statusMessenger,goData,geneData);
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

