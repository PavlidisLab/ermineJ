package classScore;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Vector;
import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo All input of custom classes, identified either by probe id or official gene name.
 */

public class classScoreFrame
    extends JFrame {

   final boolean CONSOLE_WINDOW = false;

   JPanel mainPanel = ( JPanel )this.getContentPane();

   JMenuBar jMenuBar1 = new JMenuBar();
   JMenu classMenu = new JMenu();
   JMenuItem defineClassMenuItem = new JMenuItem();
   JMenuItem modClassMenuItem = new JMenuItem();
   JMenuItem loadClassMenuItem = new JMenuItem();
   JMenu analysisMenu = new JMenu();
   JMenuItem runAnalysisMenuItem = new JMenuItem();
   JMenuItem loadAnalysisMenuItem = new JMenuItem();
   JMenuItem saveAnalysisMenuItem = new JMenuItem();

   JTabbedPane jTabbedPane1 = new JTabbedPane();
   ClassPanel cPanel;
   ResultPanel resultpanel;
   OutputPanel oPanel;

   JPanel jPanelMainControls = new JPanel();
   JButton jButtonAbout = new JButton();
   JButton jButtonCancel = new JButton();
   JButton jButtonLoadResults = new JButton();
   JButton jButtonQuit = new JButton();

   JLabel jLabelStatus = new JLabel();
   JPanel jPanelStatus = new JPanel();

   Thread runner;
   boolean done = false;
   boolean loadResults = false;
   int runnum = 0;
   InitialMaps imaps;
   boolean initialized = false;
   classScoreStatus statusMessenger;
   Vector results = new Vector();

   Settings settings;

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
      mainPanel.setMaximumSize( new Dimension( 2000, 2000 ) );
      mainPanel.setMinimumSize( new Dimension( 2000, 2000 ) );
      mainPanel.setPreferredSize( new Dimension( 1000, 600 ) );
      mainPanel.setInputVerifier( null );

      //menu stuff
      classMenu.setText( "Classes" );
      classMenu.setMnemonic( 'C' );
      defineClassMenuItem.setText( "Define New Class" );
      defineClassMenuItem.addActionListener( new
                                             classScoreFrame_defineClassMenuItem_actionAdapter( this ) );
      defineClassMenuItem.setMnemonic( 'D' );
      modClassMenuItem.setText( "Modify Class" );
      modClassMenuItem.addActionListener( new
                                          classScoreFrame_modClassMenuItem_actionAdapter( this ) );
      modClassMenuItem.setMnemonic( 'M' );
      loadClassMenuItem.setText( "Load Class Information" );
      loadClassMenuItem.addActionListener( new
                                           classScoreFrame_loadClassMenuItem_actionAdapter( this ) );
      loadClassMenuItem.setMnemonic( 'L' );
      classMenu.add( loadClassMenuItem );
      classMenu.add( defineClassMenuItem );
      classMenu.add( modClassMenuItem );
      analysisMenu.setText( "Analysis" );
      analysisMenu.setMnemonic( 'A' );
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

      jTabbedPane1.setMaximumSize( new Dimension( 32767, 32767 ) );
      jTabbedPane1.setMinimumSize( new Dimension( 300, 530 ) );
      jTabbedPane1.setPreferredSize( new Dimension( 830, 330 ) );
      cPanel = new ClassPanel( this );
      oPanel = new OutputPanel( results );
      //cPanel.setModel(InitialMaps.toBlankTableModel());
      jTabbedPane1.addTab( "oPanel", oPanel );

      jPanelMainControls.setPreferredSize( new Dimension( 830, 35 ) );
      jButtonLoadResults.setToolTipText(
          "Click to load an existing results file from disk" );
      jButtonLoadResults.setActionCommand( "jButtonLoad" );
      jButtonLoadResults.setText( "Load Results" );
      jButtonLoadResults.addActionListener( new
                                            classScoreFrame_jButtonLoadResults_actionAdapter( this ) );
      jButtonAbout.setToolTipText( "Please click here!" );
      jButtonAbout.setText( "About the software" );
      jButtonAbout.addActionListener( new
                                      classScoreFrame_jButtonAbout_actionAdapter( this ) );
      jButtonQuit.setText( "Quit Program" );
      jButtonQuit.addActionListener( new
                                     classScoreFrame_jButtonQuit_actionAdapter( this ) );
      jButtonCancel.setToolTipText( "Cancel the current run" );
      jButtonCancel.setText( "Stop" );
      jButtonCancel.addActionListener( new
                                       classScoreFrame_jButtonCancel_actionAdapter( this ) );
      jPanelMainControls.add( jButtonQuit, null );
      jPanelMainControls.add( jButtonLoadResults, null );
      jPanelMainControls.add( jButtonCancel, null );
      jPanelMainControls.add( jButtonAbout, null );

      jPanelStatus.setBorder( BorderFactory.createEtchedBorder() );
      jPanelStatus.setPreferredSize( new Dimension( 830, 33 ) );
      jLabelStatus.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
      jLabelStatus.setPreferredSize( new Dimension( 500, 19 ) );
      jLabelStatus.setHorizontalAlignment( SwingConstants.LEFT );
      jLabelStatus.setText( "Status" );
      jPanelStatus.add( jLabelStatus, null );
      showStatus( "Please see 'About this software' for license information." );
      statusMessenger = new classScoreStatus( jLabelStatus );

      mainPanel.add( jTabbedPane1, BorderLayout.NORTH );
      mainPanel.add( jPanelMainControls, BorderLayout.CENTER );
      mainPanel.add( jPanelStatus, BorderLayout.SOUTH );
   }

   void jButtonLoad_actionPerformed( ActionEvent e ) {
      /*
       final double oraThresh = Double.parseDouble(jTextFieldPValueThreshold.
                                                        getText());
            final String useWeights = getUseWeights();

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

            writePrefs();

            showStatus("Running");

            classScoreStatus m = new classScoreStatus(jLabelStatus);

            class runthread
                extends Thread {
               classScoreStatus m;

               public runthread(classScoreStatus m) {
                  this.m = m;
               }

               public void run() {
                  try {
                     populate_class_list(m);
                     classPvalRun results = new classPvalRun(imaps,
                         jTextFieldOutPutFileName.getText(),
                         oraThresh,
                         useWeights,
                         "bh", m, loadResults);

                     ResultPanel r = new ResultPanel(results);
                     //r.setTitle(jTextFieldOutPutFileName.getText());
                     //        r.addClassDetailsListener(class_details_action_listener);
                     r.setModel(results.toTableModel());
                     //r.show();
                     //jTabbedPane1.addTab(jTextFieldOutPutFileName.getText(),r);
                     runnum++;
       jTabbedPane1.addTab("Run " + Integer.toString(runnum),runAnalysisMenuItem);
                     cPanel.setModel(imaps.toTableModel());
                  }
                  catch (IllegalArgumentException e) {
                     error(e, "During class score calculation");
                  }
                  catch (IOException e) {
                     error(e, "File reading or writing");
                  }
                  showStatus("Done");
                  done = true;
                  loadResults = false;
               }
            };

            runner = new runthread(m);
            runner.start();
       */
   }

   void initialize() {
      try {
         imaps = new InitialMaps(
             settings.getAnnotFile(),
             settings.getClassFile(),
             statusMessenger );
      }
      catch ( IllegalArgumentException e ) {
         error( e, "During initialization" );
      }
      catch ( IOException e ) {
         error( e, "File reading or writing" );
      }
      //cPanel.setModel(imaps.toTableModel());
      oPanel.addInitialClassData( imaps );
      initialized = true;
   }

   public void analyze( Settings settings, String classScoreMethod, String groupMethod,
                        String useWeights, String takeLog,
                        classScoreStatus messenger, String outputfile ) {
      try {
         if ( !initialized ) {
            initialize();
         }
         InitialMaps runmaps = new InitialMaps( settings,
                                                classScoreMethod, groupMethod,
                                                50, useWeights, takeLog,
                                                messenger );

         //cPanel.setModel(imaps.toTableModel());
         System.err.println( "DONE with RUNMAPS" );
         classPvalRun runResult = new classPvalRun( settings, runmaps, outputfile,
             useWeights, "bh", messenger,
             loadResults );

         System.err.println( "DONE with CLASSPVALRUN" );

         resultpanel = new ResultPanel( runResult, settings );
         resultpanel.setModel( runResult.toTableModel() );
         runnum++;
         jTabbedPane1.addTab( "Run " + Integer.toString( runnum ), resultpanel );
         //oPanel.addRunData( runResult.getResults() );
         oPanel.addRun();
         results.add( runResult );
      }
      catch ( IllegalArgumentException e ) {
         error( e, "During class score calculation" );
      }
      catch ( IOException e ) {
         error( e, "File reading or writing" );
      }
   }

   /* quit */
   void jButtonQuit_actionPerformed( ActionEvent e ) {
      System.exit( 0 );
   }

   void jButtonCancel_actionPerformed( ActionEvent e ) {

      if ( runner != null ) {
         runner.stop();
      }
      try {
         Thread.sleep( 200 );
      }
      catch ( InterruptedException ex ) {
         Thread.currentThread().interrupt();
      }
      showStatus( "Ready" );
      System.err.println( "Ready" );
   }

   /**
    *
    * @param e
    * @param message
    */
   public void error( Exception e, String message ) {
      showStatus( "Error: " + message + " (" + e.toString() + ")" );
      JOptionPane.showMessageDialog( null,
                                     "Error: " + message + "\n" + e.toString() +
                                     "\n" + e.getStackTrace() );
   }

   /**
    *
    * @param comp
    * @return
    */
   public String getString( JComboBox comp ) {

      String selectedPath = ( String ) comp.getSelectedItem();
      if ( selectedPath == null ||
           !selectedPath.equals( comp.getEditor().getItem() ) ) {
         selectedPath = ( String ) comp.getEditor().getItem();
      }
      return selectedPath;
   }

   /**
    *
    * @param inFilename
    * @return
    */
   public Vector Reader( String inFilename ) {
      File file = new File( inFilename );

      showStatus( "Reading " + inFilename );

      if ( file.exists() && file.isFile() && file.canRead() ) {

         Vector fileList = new Vector();
         try {
            FileInputStream fis = new FileInputStream( inFilename );
            BufferedInputStream bis = new BufferedInputStream( fis );
            BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
            while ( dis.ready() ) {
               String line = dis.readLine();
               fileList.add( line );
               //         System.err.println(line);
            }
            dis.close();
         }
         catch ( IOException e ) {
            // catch possible io errors from readLine()
            error( e, "Reading preferences." );
         }

         clearStatus();
         return fileList;
      } else {
         return null;
      }
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
    * @param outFilename
    * @param names
    */
   public void comboWriter( String outFilename, String names ) {
      try {
         BufferedWriter out = new BufferedWriter( new FileWriter( outFilename, false ) );
         showStatus( "Writing preferences to " + outFilename );
         out.write( names + "\n" );
         out.close();
      }
      catch ( IOException e ) {
         error( e, "Writing preferences" );
      }
      clearStatus();
   }

   /**
    *
    * @param in
    * @return
    */
   private String getCanonical( String in ) {
      if ( in == null || in.length() == 0 ) {
         return in;
      }
      File outFile = new File( in );
      try {
         return outFile.getCanonicalPath();
      }
      catch ( Exception e ) {
         error( e, "Getting path for preferences file" );
         return null;
      }
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
    * @param e
    */
   void jButtonLoadResults_actionPerformed( ActionEvent e ) {
      loadResults = true;
      this.jButtonLoad_actionPerformed( e );
   }

   /**
    *
    */

   void defineClassMenuItem_actionPerformed( ActionEvent e ) {
      makeModClassFrame( true, "" );
   }

   void modClassMenuItem_actionPerformed( ActionEvent e ) {
      makeModClassFrame( false, "" );
   }

   public void makeModClassFrame( boolean makenew, String classid ) {
      if ( !initialized ) {
         initialize();
      }
      modClassFrame modframe = new modClassFrame( makenew, imaps, this.cPanel,
                                                  settings.getDataFolder(), classid );
      showDialog( modframe );
   }

   void loadClassMenuItem_actionPerformed( ActionEvent e ) {
      showStatus( "Loading" );
      class runthread
          extends Thread {
         public runthread() {}

         public void run() {
            initialize();
            showStatus( "Loaded" );
         }
      };
      runner = new runthread();
      runner.start();
   }

   void runAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      AnalysisWizard awiz = new AnalysisWizard(this);
      awiz.showWizard();
//      showDialog( awiz );
   }

   void loadAnalysisMenuItem_actionPerformed( ActionEvent e ) {
      //oPanel.addColumn("new col");
   }

   void saveAnalysisMenuItem_actionPerformed( ActionEvent e ) {
//      SaveWizard swiz = new SaveWizard( this, ( Vector ) oPanel.getAllRunData() );
      SaveWizard swiz = new SaveWizard( this, results );
      showDialog( swiz );
   }

   void showDialog( JDialog j ) {
      Dimension dlgSize = j.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      j.setLocation( ( frmSize.width - dlgSize.width ) / 2 + loc.x,
                     ( frmSize.height - dlgSize.height ) / 2 + loc.y );
      j.pack();
      j.show();
   }

   public Settings getSettings() {
      return settings;
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

class classScoreFrame_jButtonLoadResults_actionAdapter
    implements java.awt.
    event.ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_jButtonLoadResults_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.jButtonLoadResults_actionPerformed( e );
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

class classScoreFrame_loadClassMenuItem_actionAdapter
    implements java.awt.event.
    ActionListener {
   classScoreFrame adaptee;

   classScoreFrame_loadClassMenuItem_actionAdapter( classScoreFrame adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.loadClassMenuItem_actionPerformed( e );
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
