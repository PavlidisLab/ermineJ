package classScore.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import baseCode.gui.WizardStep;

/**
 * <p>
 * Copyright: Copyright (c) 2003-2004 Columbia University
 * @author Homin Lee
 * @version $Id$
 */
public class SaveWizardStep1 extends WizardStep {
   SaveWizard wiz;
   LinkedList rundata;
   JPanel runPanel;
   JComboBox runComboBox;
   JLabel runLabel;
   BorderLayout borderLayout;
   boolean runs_exist;

   public SaveWizardStep1( SaveWizard wiz, LinkedList rundata ) {
      super( wiz );
      this.wiz = wiz;
      this.rundata = rundata;
      showChoices();
      wiz.clearStatus();
   }

   //Component initialization
   protected void jbInit() throws Exception {
      runPanel = new JPanel();
      borderLayout = new BorderLayout();
      runPanel.setLayout( borderLayout );
      JPanel topPanel = new JPanel();
      runLabel = new JLabel();
      runLabel.setText( "Choose the analysis to save:" );
      topPanel.add( runLabel );
      JPanel centerPanel = new JPanel();
      runComboBox = new JComboBox();
      runComboBox
            .addActionListener( new SaveWizardStep1_runComboBox_actionAdapter(
                  this ) );
      centerPanel.add( runComboBox );
      runPanel.add( topPanel, BorderLayout.NORTH );
      runPanel.add( centerPanel, BorderLayout.CENTER );

      this.addHelp( "<html><b>You may save " +
            "the results of an analysis in a file.</b><br>" + "This file" +
                  " can be used in other software (e.g. Excel) or loaded" +
                  " back into this application to be viewed later." );
      this.addMain( runPanel );
   }

   public boolean isReady() {
      return true;
   }

   void showChoices() {
      if ( rundata == null || rundata.size() < 1 ) {
         runComboBox.addItem( "No runs available to save" );
         runs_exist = false;
      } else {
         runs_exist = true;
         for ( int i = 0; i < rundata.size(); i++ ) {
            runComboBox.insertItemAt( "Run " + ( i + 1 ), i );
         }
         runComboBox.setSelectedIndex( 0 );
      }
   }

   public int getSelectedRunNum() {
      return runComboBox.getSelectedIndex();
   }

   public boolean runsExist() {
      return runs_exist;
   }

   void runComboBox_actionPerformed( ActionEvent e ) {
      wiz.selectRun( runComboBox.getSelectedIndex() );
   }

}

class SaveWizardStep1_runComboBox_actionAdapter implements
      java.awt.event.ActionListener {
   SaveWizardStep1 adaptee;

   SaveWizardStep1_runComboBox_actionAdapter( SaveWizardStep1 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.runComboBox_actionPerformed( e );
   }
}