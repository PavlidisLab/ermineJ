package classScore.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import baseCode.gui.*;
import classScore.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 */

public class GeneSetWizardStep1 extends WizardStep
{
   GeneSetWizard wiz;
   Settings settings;
   JButton browseButton;
   JTextField classFile;
   JFileChooser chooser;
   int inputMethod;

   public GeneSetWizardStep1( GeneSetWizard wiz, Settings settings ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      chooser = new JFileChooser();
      chooser.setCurrentDirectory( new File( settings.getDataFolder() ) );
      chooser.setDialogTitle("Choose Gene Set File");
   }

   //Component initialization
   protected void jbInit() {
      JPanel step1Panel = new JPanel();
      BorderLayout borderLayout1 = new BorderLayout();
      step1Panel.setLayout(borderLayout1);

      //top
      JPanel jPanel1 = new JPanel();
      JPanel jPanel7 = new JPanel(); //outer method choice
      GridBagLayout gridBagLayout4 = new GridBagLayout();
      jPanel7.setLayout( gridBagLayout4 );
      JLabel jLabel8 = new JLabel(); // 'choose method'
      jLabel8.setText( "Choose the method of data entry:" );
      jLabel8.setMaximumSize( new Dimension( 999, 15 ) );
      jLabel8.setMinimumSize( new Dimension( 259, 15 ) );
      jLabel8.setPreferredSize( new Dimension( 259, 15 ) );
      GridBagLayout gridBagLayout1 = new GridBagLayout();
      JPanel jPanel4 = new JPanel(); // holds radio buttons
      jPanel4.setBorder( BorderFactory.createEtchedBorder() );
      jPanel4.setLayout( gridBagLayout1 );
      JRadioButton fileInputButton = new JRadioButton();
      fileInputButton.setBackground( SystemColor.control );
      fileInputButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
      fileInputButton.setText( "File" );
      fileInputButton.addActionListener( new GeneSetWizardStep1_fileInputButton_actionAdapter( this ) );
      JRadioButton manInputButton = new JRadioButton( "Manual", true );
      manInputButton.setBackground( SystemColor.control );
      manInputButton.setMaximumSize( new Dimension( 91, 23 ) );
      manInputButton.addActionListener( new
                                        GeneSetWizardStep1_manInputButton_actionAdapter( this ) );
      manInputButton.setBorder( BorderFactory.createLineBorder( Color.black ) );
      ButtonGroup buttonGroup1 = new ButtonGroup();
      buttonGroup1.add( fileInputButton );
      buttonGroup1.add( manInputButton );
      JLabel jLabel4 = new JLabel();
      jLabel4.setText( "- File with gene symbols or probe ids" );
      JLabel jLabel5 = new JLabel();
      jLabel5.setText( "- Enter using lists" );
      jPanel4.add( jLabel5, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0
          , GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets( 0, 16, 8, 10 ),
          125, 10 ) );
      jPanel4.add( jLabel4, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0
          , GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets( 3, 16, 0, 10 ),
          30, 10 ) );
      jPanel4.add( manInputButton, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets( 0, 9, 8, 0 ), 8, 12 ) );
      jPanel4.add( fileInputButton, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets( 3, 9, 0, 0 ), 26, 12 ) );
      jPanel7.add( jLabel8, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0
          , GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets( 6, 21, 0, 74 ),
          0, 0 ) );
      jPanel7.add( jPanel4, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
          , GridBagConstraints.CENTER,
          GridBagConstraints.HORIZONTAL,
          new Insets( 6, 10, 12, 16 ), -1,
          8 ) );
      jPanel1.add(jPanel7, null);

      //bottom
      JPanel jPanel2 = new JPanel(); // holds file chooser
      jPanel2.setPreferredSize(new Dimension(354, 150));
      browseButton = new JButton();
      browseButton.setText( "Browse...." );
      browseButton.addActionListener( new GeneSetWizardStep1_browseButton_actionAdapter( this ) );
      browseButton.setEnabled( false );
      classFile = new JTextField();
      classFile.setEditable( false );
      classFile.setPreferredSize( new Dimension( 230, 19 ) );
      classFile.setToolTipText( "File containing class members" );
      classFile.setText( "File containing class members" );
      jPanel2.add( browseButton, null );
      jPanel2.add( classFile, null );

      step1Panel.add(jPanel1, BorderLayout.CENTER);
      step1Panel.add(jPanel2, BorderLayout.SOUTH);

      this.add( step1Panel );
   }

   public boolean isReady() {
      return true;
   }

   void manInputButton_actionPerformed( ActionEvent e ) {
      classFile.setEditable( false );
      classFile.setEnabled( false );
      browseButton.setEnabled( false );
      inputMethod = 0;
   }

   void fileInputButton_actionPerformed( ActionEvent e ) {
      classFile.setEditable( true );
      classFile.setEnabled( true );
      browseButton.setEnabled( true );
      inputMethod = 1;
   }

   void browseButton_actionPerformed( ActionEvent e ) {
      int result = chooser.showOpenDialog( this );
      if ( result == JFileChooser.APPROVE_OPTION ) {
         classFile.setText( chooser.getSelectedFile().toString() );
      }
   }

   public int getInputMethod()  { return inputMethod;  }
   public String getLoadFile() { return classFile.getText(); }
}

class GeneSetWizardStep1_manInputButton_actionAdapter implements java.awt.event.
        ActionListener {
   GeneSetWizardStep1 adaptee;

   GeneSetWizardStep1_manInputButton_actionAdapter(GeneSetWizardStep1 adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.manInputButton_actionPerformed(e);
   }
}

class GeneSetWizardStep1_fileInputButton_actionAdapter implements java.awt.event.
        ActionListener {
   GeneSetWizardStep1 adaptee;

   GeneSetWizardStep1_fileInputButton_actionAdapter(GeneSetWizardStep1 adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.fileInputButton_actionPerformed(e);
   }
}

class GeneSetWizardStep1_browseButton_actionAdapter implements java.awt.event.ActionListener {
   GeneSetWizardStep1 adaptee;

   GeneSetWizardStep1_browseButton_actionAdapter(GeneSetWizardStep1 adaptee) {
      this.adaptee = adaptee;
   }

   public void actionPerformed(ActionEvent e) {
      adaptee.browseButton_actionPerformed(e);
   }
}

