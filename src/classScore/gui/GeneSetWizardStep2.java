package classScore.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import baseCode.gui.*;
import classScore.data.*;
import classScore.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version $Id$
 */

public class GeneSetWizardStep2 extends WizardStep {

   GeneSetWizard wiz;
   GeneAnnotations geneData;
   JLabel countLabel;
   JTable probeTable;
   JTable newClassTable;
   AbstractTableModel ncTableModel;
   NewGeneSet newGeneSet;
   JTextField searchTextField;
   
   public GeneSetWizardStep2( GeneSetWizard wiz, GeneAnnotations geneData,
         NewGeneSet newGeneSet ) {
      super( wiz );
      this.wiz = wiz;
      this.geneData = geneData;
      this.newGeneSet = newGeneSet;
      populateTables();
   }

   //Component initialization
   protected void jbInit() {
      BorderLayout borderLayout2 = new BorderLayout();
      this.setLayout( borderLayout2 );
      JPanel step2Panel;

      JPanel topPanel = new JPanel();
      countLabel = new JLabel();
      countLabel.setText( "Number of Probes: 0" );
      topPanel.add( countLabel );

      step2Panel = new JPanel();
      BorderLayout borderLayout1 = new BorderLayout();
      step2Panel.setLayout( borderLayout1 );

      JPanel centerPanel = new JPanel();
      GridLayout gridLayout1 = new GridLayout();
      centerPanel.setLayout( gridLayout1 );
      JScrollPane probeScrollPane;
      JScrollPane newClassScrollPane;
      probeTable = new JTable();
      probeTable.getTableHeader().setReorderingAllowed( false );
      probeScrollPane = new JScrollPane( probeTable );
      probeScrollPane.setPreferredSize( new Dimension( 250, 150 ) );
      newClassTable = new JTable();
      newClassTable.getTableHeader().setReorderingAllowed( false );
      newClassScrollPane = new JScrollPane( newClassTable );
      newClassScrollPane.setPreferredSize( new Dimension( 250, 150 ) );
      centerPanel.add( probeScrollPane, null );
      centerPanel.add( newClassScrollPane, null );

      JPanel bottomPanel = new JPanel();
      bottomPanel.setPreferredSize( new Dimension( 300, 50 ) );

      JButton searchButton = new JButton();
      searchButton.setText( "Find" );
      searchButton
            .addActionListener( new GeneSetWizardStep2_searchButton_actionAdapter(
                  this ) );
      // JLabel searchLabel = new JLabel();
      //  searchLabel.setText("Find:");

      searchTextField = new JTextField();
      searchTextField.setPreferredSize( new Dimension( 60, 19 ) );
      searchTextField
            .addKeyListener( new GeneSetWizardStep2_searchText_keyAdapter( this ) );

      JButton addButton = new JButton();
      addButton.setSelected( false );
      addButton.setText( "Add >" );
      addButton
            .addActionListener( new GeneSetWizardStep2_addButton_actionAdapter(
                  this ) );
      JButton deleteButton = new JButton();
      deleteButton.setSelected( false );
      deleteButton.setText( "Delete" );
      deleteButton
            .addActionListener( new GeneSetWizardStep2_delete_actionPerformed_actionAdapter(
                  this ) );

      bottomPanel.add( searchButton );
      //    bottomPanel.add(searchLabel, null);
      bottomPanel.add( searchTextField );
      bottomPanel.add( addButton, null );
      bottomPanel.add( deleteButton, null );
      step2Panel.add( topPanel, BorderLayout.NORTH );
      step2Panel.add( centerPanel, BorderLayout.CENTER );
      step2Panel.add( bottomPanel, BorderLayout.SOUTH );

      this.addHelp( "<html><b>Set up the gene set</b><br>"
                  + "Add or remove probes/genes using the buttons below the table. " +
                        "To find a specific gene use the 'find' tool." );
      this.addMain( step2Panel );
   }

   public boolean isReady() {
      return true;
   }

   void delete_actionPerformed( ActionEvent e ) {
      int n = newClassTable.getSelectedRowCount();
      int[] rows = newClassTable.getSelectedRows();
      for ( int i = 0; i < n; i++ ) {
         newGeneSet.getProbes().remove(
               newClassTable.getValueAt( rows[i] - i, 0 ) );
      }
      int s = newGeneSet.getProbes().size();
      ncTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void addButton_actionPerformed( ActionEvent e ) {
      int n = probeTable.getSelectedRowCount();
      int[] rows = probeTable.getSelectedRows();
      for ( int i = 0; i < n; i++ ) {
         //newGeneSet.probes.add(probeTable.getValueAt(rows[i], 0)); (for just deleting probes)
         String newGene;
         if ( ( newGene = geneData.getProbeGeneName( ( String ) probeTable
               .getValueAt( rows[i], 0 ) ) ) != null ) {
            addGene( newGene );
         }
      }
      HashSet noDupes = new HashSet( newGeneSet.getProbes() );
      newGeneSet.getProbes().clear();
      newGeneSet.getProbes().addAll( noDupes );
      int s = newGeneSet.getProbes().size();
      ncTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void editorProbe_actionPerformed( ChangeEvent e ) {
      String newProbe = ( String ) ( ( DefaultCellEditor ) e.getSource() )
            .getCellEditorValue();
      String newGene;
      if ( ( newGene = geneData.getProbeGeneName( newProbe ) ) != null ) {
         addGene( newGene );
      } else {
         GuiUtil.error( "Probe " + newProbe + " does not exist." );
      }
   }

   void editorGene_actionPerformed( ChangeEvent e ) {
      String newGene = ( String ) ( ( DefaultCellEditor ) e.getSource() )
            .getCellEditorValue();
      addGene( newGene );
   }

   void addGene( String gene ) {
      ArrayList probelist = geneData.getGeneProbeList( gene );
      if ( probelist != null ) {
         newGeneSet.getProbes().addAll( probelist );
         int s = newGeneSet.getProbes().size();
         ncTableModel.fireTableDataChanged();
         updateCountLabel();
      } else {
         GuiUtil.error( "Gene " + gene + " does not exist." );
      }
   }

   public void updateCountLabel() {
      countLabel.setText( "Number of Probes: " + newGeneSet.getProbes().size() );
   }

   // todo shouldn't this use the TableSorter?
   private void populateTables() {
      SortFilterModel sorter = new SortFilterModel( geneData.toTableModel() );
      probeTable.setModel( sorter );
      probeTable.getTableHeader().addMouseListener( new MouseAdapter() {
         public void mouseClicked( MouseEvent event ) {
            int tableColumn = probeTable.columnAtPoint( event.getPoint() );
            int modelColumn = probeTable
                  .convertColumnIndexToModel( tableColumn );
            ( ( SortFilterModel ) probeTable.getModel() ).sort( modelColumn );
         }
      } );
      probeTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 40 );

      ncTableModel = newGeneSet.toTableModel( false );
      newClassTable.setModel( ncTableModel );
//      JTextField editProbe = new JTextField();
//      editProbe.setBorder( BorderFactory.createEmptyBorder() );
//      DefaultCellEditor editorProbe = new DefaultCellEditor( editProbe );
//      editorProbe
//            .addCellEditorListener( new GeneSetWizardStep2_editorProbeAdaptor(
//                  this ) );
//      newClassTable.getColumnModel().getColumn( 0 ).setCellEditor( editorProbe );
//      JTextField editGene = new JTextField();
//      editGene.setBorder( BorderFactory.createEmptyBorder() );
//      DefaultCellEditor editorGene = new DefaultCellEditor( editGene );
//      editorGene
//            .addCellEditorListener( new GeneSetWizardStep2_editorGeneAdaptor(
//                  this ) );
//      newClassTable.getColumnModel().getColumn( 1 ).setCellEditor( editorGene );
      newClassTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 40 );
   }

   /**
    * do a search.
    */
   public void searchButton_actionPerformed_adapter( ActionEvent e ) {
      // TODO Auto-generated method stub
      String searchOn = searchTextField.getText();
      
      // reset
      if (searchOn.equals("")) {
         geneData.resetSelected();
      } else {   
         geneData.select(searchOn);
      }
      populateTables();
      
   }
}

class GeneSetWizardStep2_delete_actionPerformed_actionAdapter implements
      java.awt.event.ActionListener {
   GeneSetWizardStep2 adaptee;

   GeneSetWizardStep2_delete_actionPerformed_actionAdapter(
         GeneSetWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.delete_actionPerformed( e );
   }
}

class GeneSetWizardStep2_addButton_actionAdapter implements
      java.awt.event.ActionListener {
   GeneSetWizardStep2 adaptee;

   GeneSetWizardStep2_addButton_actionAdapter( GeneSetWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.addButton_actionPerformed( e );
   }
}

class GeneSetWizardStep2_editorProbeAdaptor implements CellEditorListener {
   GeneSetWizardStep2 adaptee;

   GeneSetWizardStep2_editorProbeAdaptor( GeneSetWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void editingStopped( ChangeEvent e ) {
      adaptee.editorProbe_actionPerformed( e );
   }

   public void editingCanceled( ChangeEvent e ) {
      editingCanceled( e );
   }
}

class GeneSetWizardStep2_editorGeneAdaptor implements CellEditorListener {
   GeneSetWizardStep2 adaptee;

   GeneSetWizardStep2_editorGeneAdaptor( GeneSetWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }

   public void editingStopped( ChangeEvent e ) {
      adaptee.editorGene_actionPerformed( e );
   }

   public void editingCanceled( ChangeEvent e ) {
      editingCanceled( e );
   }
}

// respond to typing in the search field.
// todo incremental search trigger would go here.
class GeneSetWizardStep2_searchText_keyAdapter implements KeyListener {

   GeneSetWizardStep2 adaptee;
   
   public GeneSetWizardStep2_searchText_keyAdapter( GeneSetWizardStep2 adaptee ) {
      this.adaptee = adaptee;
      // TODO Auto-generated constructor stub
   }

   public void keyPressed( KeyEvent e ) {
      // TODO Auto-generated method stub
   }

   public void keyReleased( KeyEvent e ) {
      // TODO Auto-generated method stub
   }

   public void keyTyped( KeyEvent e ) {
      // TODO Auto-generated method stub
   }

}

// respond to search request.
class GeneSetWizardStep2_searchButton_actionAdapter implements ActionListener {
   GeneSetWizardStep2 adaptee;
   
   public GeneSetWizardStep2_searchButton_actionAdapter(
         GeneSetWizardStep2 adaptee ) {
      this.adaptee = adaptee;
   }
   
   public void actionPerformed( ActionEvent e ) {
      adaptee.searchButton_actionPerformed_adapter(e);
   }

}