package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import baseCode.gui.WizardStep;
import classScore.Settings;
import classScore.data.GeneAnnotations;
import classScore.data.NewGeneSet;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 *
 * @author Homin K Lee
 * @version $Id$
 */

public class GeneSetWizardStep3 extends WizardStep {

   GeneSetWizard wiz;
   Settings settings;
   GeneAnnotations geneData;
   NewGeneSet newGeneSet;
   boolean makenew;

   JLabel classIDFinal;
   JLabel classDescL;
   JTextField classIDTF;
   JTextArea classDescTA;
   JTable finalTable;

   String origID;

   public GeneSetWizardStep3( GeneSetWizard wiz, Settings settings,
         GeneAnnotations geneData, NewGeneSet newGeneSet, boolean makenew ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      this.geneData = geneData;
      this.newGeneSet = newGeneSet;
      this.makenew = makenew;
      AbstractTableModel finalTableModel = newGeneSet.toTableModel( true );
      finalTable.setModel( finalTableModel );
      wiz.clearStatus();
   }

   //Component initialization
   protected void jbInit() {
      BorderLayout borderLayout4 = new BorderLayout();
      this.setLayout( borderLayout4 );
      JPanel step3Panel = new JPanel();
      BorderLayout borderLayout1 = new BorderLayout();
      step3Panel.setLayout( borderLayout1 );

      JPanel ncIDPanel = new JPanel();
      ncIDPanel.setPreferredSize( new Dimension( 128, 51 ) );
      JLabel classIDL = new JLabel( "New Class ID: " );
      classIDTF = new JTextField();
      //classIDTF.addKeyListener( new classIDlistener( this ) );
      classIDTF.setPreferredSize( new Dimension( 100, 19 ) );
      classIDTF.setBorder( BorderFactory.createLoweredBevelBorder() );
      classIDTF.setToolTipText( "New Class ID" );
      ncIDPanel.add( classIDL );
      ncIDPanel.add( classIDTF );

      JPanel ncInfo1Panel = new JPanel();
      ncInfo1Panel.setPreferredSize( new Dimension( 165, 240 ) );
      JPanel ncDescPanel = new JPanel();
      ncDescPanel.setPreferredSize( new Dimension( 165, 180 ) );
      classDescL = new JLabel( "New gene set ID: " );
      classDescL.setRequestFocusEnabled( true );
      classDescL.setText( "New gene set Description: " );
      classDescTA = new JTextArea();
      classDescTA.setToolTipText( "New gene set ID" );

      classDescTA.setLineWrap( true );
      JScrollPane classDTAScroll = new JScrollPane( classDescTA );
      classDTAScroll.setBorder( BorderFactory.createLoweredBevelBorder() );
      classDTAScroll.setPreferredSize( new Dimension( 160, 140 ) );
      ncDescPanel.add( classDescL );
      ncDescPanel.add( classDTAScroll, null );
      ncInfo1Panel.add( ncIDPanel, null );
      ncInfo1Panel.add( ncDescPanel, null );

      JPanel ncInfo2Panel = new JPanel();
      BorderLayout borderLayout2 = new BorderLayout();
      ncInfo2Panel.setLayout( borderLayout2 );
      ncInfo2Panel.setPreferredSize( new Dimension( 220, 240 ) );
      classIDFinal = new JLabel( "New Gene set ID: " );
      classIDFinal.setText( "No Gene set Name" );
      classIDFinal.setRequestFocusEnabled( true );
      finalTable = new JTable();
      finalTable.getTableHeader().setReorderingAllowed( false );
      JScrollPane finalScrollPane = new JScrollPane( finalTable );
      finalScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
      ncInfo2Panel.add( classIDFinal, BorderLayout.NORTH );
      ncInfo2Panel.add( finalScrollPane, BorderLayout.CENTER );

      step3Panel.add( ncInfo1Panel, BorderLayout.WEST );
      step3Panel.add( ncInfo2Panel, BorderLayout.CENTER );

      this.addHelp( "<html><b>Choose a new gene set identifier and description.</b><br>"
            + "The custom gene set will automatically be saved to your hard drive" +
                  "to be used again in future analyses." );
      this.addMain( step3Panel );
   }

   public boolean isReady() {
      return true;
   }

   public void nameNewGeneSet(){
      newGeneSet.setId(classIDTF.getText());
      newGeneSet.setDesc(classDescTA.getText());
      if(newGeneSet.modified() && origID.compareTo(newGeneSet.getId())!=0)
      {
         newGeneSet.setModified(false);
      }
   }

   public void update() {
      classIDTF.setText( newGeneSet.getId() );
      classDescTA.setText( newGeneSet.getDesc() );
      if ( newGeneSet.getId().compareTo( "" ) != 0 ) {
         classIDFinal.setText( newGeneSet.getId() );
      }
      if(newGeneSet.modified())
         origID = newGeneSet.getId();
   }

}
