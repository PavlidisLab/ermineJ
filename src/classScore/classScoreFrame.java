package classScore;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import baseCode.gui.*;

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

public class classScoreFrame
    extends JFrame {
   final boolean CONSOLE_WINDOW = false;
   JPanel mainPanel = ( JPanel )this.getContentPane();
   JMenuBar jMenuBar1 = new JMenuBar();
   JMenu classMenu = new JMenu();
   JMenuItem defineClassMenuItem = new JMenuItem();
   JMenuItem modClassMenuItem = new JMenuItem();
   JMenu analysisMenu = new JMenu();
   JMenuItem runAnalysisMenuItem = new JMenuItem();
   JMenuItem loadAnalysisMenuItem = new JMenuItem();
   JMenuItem saveAnalysisMenuItem = new JMenuItem();

   JPanel progressPanel;
   JPanel progInPanel = new JPanel();
   JProgressBar progressBar = new JProgressBar();
   OutputPanel oPanel;

   JPanel jPanelMainControls = new JPanel();
   JButton jButtonAbout = new JButton();
   JButton jButtonCancel = new JButton();
   JButton jButtonQuit = new JButton();

   JLabel jLabelStatus = new JLabel();
   JPanel jPanelStatus = new JPanel();

   Settings settings;
   classScoreStatus statusMessenger;
   GONames goData;
   GeneAnnotations geneData;
   Vector results = new Vector();

   AnalysisThread athread=new AnalysisThread();
   //javax.swing.Timer initMonitor;

   public classScoreFrame() {
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
      classMenu.setText( "Classes" );
      classMenu.setMnemonic( 'C' );
      classMenu.setEnabled(false);
      defineClassMenuItem.setText( "Define New Class" );
      defineClassMenuItem.addActionListener( new
                                             classScoreFrame_defineClassMenuItem_actionAdapter( this ) );
      defineClassMenuItem.setMnemonic( 'D' );
      modClassMenuItem.setText( "Modify Class" );
      modClassMenuItem.addActionListener( new
                                          classScoreFrame_modClassMenuItem_actionAdapter( this ) );
      modClassMenuItem.setMnemonic( 'M' );
      classMenu.add( defineClassMenuItem );
      classMenu.add( modClassMenuItem );
      analysisMenu.setText( "Analysis" );
      analysisMenu.setMnemonic( 'A' );
      analysisMenu.setEnabled(false);
      runAnalysisMenuItem.setText( "Run Analysis" );
      runAnalysisMenuItem.addActionListener( new
                                             classScoreFrame_runAnalysisMenuItem_actionAdapter( this ) );
      runAnalysisMenuItem.setMnemonic( 'R' );
      loadAnalysisMenuItem.setText( "Load Analysis" );
      loadAnalysisMenuItem.addActionListener( new
                                              classScoreFrame_loadAnalysisMenuItem_actionAdapter( this ) );
      loadAnalysisMenuItem.setMnemonic( 'L' );
      saveAnalysisMenuItem.setText( "Save Analysis" );
      saveAnalysisMenuItem.addActionListener( new
                                              classScoreFrame_saveAnalysisMenuItem_actionAdapter( this ) );
      saveAnalysisMenuItem.setMnemonic( 'S' );
      analysisMenu.add( runAnalysisMenuItem );
      analysisMenu.add( loadAnalysisMenuItem );
      analysisMenu.add( saveAnalysisMenuItem );
      jMenuBar1.add( classMenu );
      jMenuBar1.add( analysisMenu );

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
      /*
      initMonitor=new javax.swing.Timer(500, new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            int current = ithread.getInitCurrent();
            progressBar.setValue(current);
            if(current == ithread.getInitTarget())
               initMonitor.stop();
         }
      });
      */
      progInPanel.add(label, null);
      progInPanel.add(progressBar, null);
      progressPanel.add(progInPanel,     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(114, 268, 87, 268), 0, 0));

      //main panel
      oPanel = new OutputPanel( this, results );
      oPanel.setPreferredSize( new Dimension( 830, 330 ) );

      //controls
      jPanelMainControls.setPreferredSize( new Dimension( 830, 35 ) );
      jButtonAbout.setToolTipText( "Please click here!" );
      jButtonAbout.setText( "About the software" );
      jButtonAbout.setMnemonic('b');
      jButtonAbout.addActionListener( new
                                      classScoreFrame_jButtonAbout_actionAdapter( this ) );
      jButtonQuit.setText( "Quit Program" );
      jButtonQuit.setMnemonic('Q');
      jButtonQuit.addActionListener( new
                                     classScoreFrame_jButtonQuit_actionAdapter( this ) );
      jButtonCancel.setToolTipText( "Cancel the current run" );
      jButtonCancel.setText( "Stop" );
      jButtonCancel.setMnemonic('S');
      jButtonCancel.addActionListener( new
                                       classScoreFrame_jButtonCancel_actionAdapter( this ) );
      jPanelMainControls.add( jButtonQuit, null );
      jPanelMainControls.add( jButtonCancel, null );
      jPanelMainControls.add( jButtonAbout, null );

      //status bar
      jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
      jPanelStatus.setPreferredSize( new Dimension( 830, 33 ) );
      jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
      jLabelStatus.setPreferredSize( new Dimension( 500, 19 ) );
      jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
      jLabelStatus.setText( "Status" );
      jPanelStatus.add( jLabelStatus, null );
      showStatus( "Please see 'About this software' for license information." );
      statusMessenger = new classScoreStatus( jLabelStatus );

      mainPanel.add( progressPanel, BorderLayout.NORTH );
      mainPanel.add( jPanelMainControls, BorderLayout.CENTER );
      mainPanel.add( jPanelStatus, BorderLayout.SOUTH );
   }

   private void enableMenus()
   {
      classMenu.setEnabled( true );
      analysisMenu.setEnabled( true );
   }

   public void initialize() {
      try {
         statusMessenger.setStatus("Reading GO descriptions " + settings.getClassFile());
         goData = new GONames(settings.getClassFile()); // parse go name file
         statusMessenger.setStatus("Reading gene annotations from " + settings.getAnnotFile());
         geneData = new GeneAnnotations(settings.getAnnotFile());
         statusMessenger.setStatus( "Initializing gene class mapping" );
         GeneSetMapTools.collapseClasses(geneData.getClassToProbeMap());
         //GeneSetMapTools.hackGeneSetToProbeMap(geneData.getClassToProbeMap());
         //System.err.println("Hacked classToProbe has size: " + geneData.getClassToProbeMap().size());
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

   /* quit */
   void jButtonQuit_actionPerformed( ActionEvent e ) {
      System.exit( 0 );
   }

   void jButtonCancel_actionPerformed( ActionEvent e ) {
      athread.cancelAnalysisThread();
      showStatus( "Ready" );
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
    * About
    * @param e
    */
   void jButtonAbout_actionPerformed( ActionEvent e ) {
      classScoreFrameAboutBox dlg = new classScoreFrameAboutBox( this );
      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      dlg.setLocation( ( frmSize.width - dlgSize.width ) / 2 + loc.x,
                       ( frmSize.height - dlgSize.height ) / 2 + loc.y );
      dlg.setModal( true );
      dlg.pack();
      dlg.show();
   }

   /**
    *
    */

   void defineClassMenuItem_actionPerformed( ActionEvent e ) {
      ClassWizard cwiz = new ClassWizard(this, geneData, goData, true);
      cwiz.showWizard();
   }

   void modClassMenuItem_actionPerformed( ActionEvent e ) {
      ClassWizard cwiz = new ClassWizard(this, geneData, goData, false);
      cwiz.showWizard();
   }

   void runAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      AnalysisWizard awiz = new AnalysisWizard(this,geneData,goData);
      awiz.showWizard();
   }

   void loadAnalysisMenuItem_actionPerformed( ActionEvent e ) {
         /*
               boolean ok = true;
               ok = testfile(jTextFieldGeneScoreFile.getText());
               ok = ok && testfile(jTextFieldGONames.getText());
               ok = ok && testfile(jTextFieldProbeAnnot.getText());

               if (loadResults) {
                  ok = ok && testfile(jTextFieldOutPutFileName.getText());
               }

               if (!ok) {
                  return;
               }
                        populate_class_list(m);
                        classPvalRun results = new classPvalRun(imaps,
                            jTextFieldOutPutFileName.getText(),
                            oraThresh,
                            useWeights,
                            "bh", m, loadResults);
                        ResultPanel r = new ResultPanel(results);
                        r.setModel(results.toTableModel());
          */
   }

   void saveAnalysisMenuItem_actionPerformed( ActionEvent e ) {
//      SaveWizard swiz = new SaveWizard( this, ( Vector ) oPanel.getAllRunData() );
      SaveWizard swiz = new SaveWizard( this, results );
      swiz.showWizard();
   }

   public Settings getSettings() {
      return settings;
   }

   public classScoreStatus getStatusMessenger(){
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

class classScoreFrame_jButtonQuit_actionAdapter
    implements java.awt.event.
    ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonQuit_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.jButtonQuit_actionPerformed( e );
   }
}

class classScoreFrame_jButtonAbout_actionAdapter
    implements java.awt.event.
    ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonAbout_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.jButtonAbout_actionPerformed( e );
   }
}

class classScoreFrame_jButtonCancel_actionAdapter
    implements java.awt.event.
    ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonCancel_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.jButtonCancel_actionPerformed( e );
   }
}

class classScoreFrame_defineClassMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_defineClassMenuItem_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.defineClassMenuItem_actionPerformed( e );
   }
}

class classScoreFrame_modClassMenuItem_actionAdapter
    implements java.awt.event.
    ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_modClassMenuItem_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.modClassMenuItem_actionPerformed( e );
   }
}

class classScoreFrame_runAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_runAnalysisMenuItem_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.runAnalysisMenuItem_actionPerformed( e );
   }
}

class classScoreFrame_loadAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_loadAnalysisMenuItem_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.loadAnalysisMenuItem_actionPerformed( e );
   }
}

class classScoreFrame_saveAnalysisMenuItem_actionAdapter
    implements java.awt.
    event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_saveAnalysisMenuItem_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.saveAnalysisMenuItem_actionPerformed( e );
   }
}
