package classScore.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import baseCode.gui.GuiUtil;
import baseCode.gui.WizardStep;
import classScore.Settings;
import classScore.data.GONames;
import classScore.data.NewGeneSet;

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

public class AnalysisWizardStep3 extends WizardStep {
   AnalysisWizard wiz;
   Settings settings;
   GONames goData;
   AnalysisWizardStep3_CustomClassList customClasses;
   AbstractTableModel ccTableModel;
   JTable customClassTable;
   HashMap ccHash;
   AnalysisWizardStep3_CustomClassList addedClasses;
   HashMap acHash;
   JTable addedClassTable;
   AbstractTableModel acTableModel;
   JLabel countLabel;

   public AnalysisWizardStep3( AnalysisWizard wiz, Settings settings,
         GONames goData ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      this.goData = goData;
      makeLeftTable();
      makeRightTable();
   }

   //Component initialization
   protected void jbInit() {
      BorderLayout borderLayout = new BorderLayout();
      this.setLayout( borderLayout );
      JPanel step3Panel;
      JPanel jPanel10 = new JPanel();
      JScrollPane customClassScrollPane;
      JScrollPane addedClassScrollPane;
      JPanel jPanel9 = new JPanel();
      JButton addButton = new JButton();
      JButton deleteButton = new JButton();
      countLabel = new JLabel();
      BorderLayout borderLayout1 = new BorderLayout();

      step3Panel = new JPanel();
      step3Panel.setLayout( borderLayout1 );
      countLabel.setForeground( Color.black );
      countLabel.setText( "Number of Classes: 0" );
      customClassTable = new JTable();
      customClassTable.setPreferredScrollableViewportSize( new Dimension( 250,
            150 ) );
      customClassScrollPane = new JScrollPane( customClassTable );
      customClassScrollPane.setPreferredSize( new Dimension( 250, 150 ) );
      addedClassTable = new JTable();
      addedClassTable.setPreferredScrollableViewportSize( new Dimension( 250,
            150 ) );
      addedClassScrollPane = new JScrollPane( addedClassTable );
      addedClassScrollPane.setPreferredSize( new Dimension( 250, 150 ) );
      GridLayout gridLayout1 = new GridLayout();
      jPanel10.setLayout( gridLayout1 );
      JButton addAllButton = new JButton();
      addAllButton.setText( "Add All >" );
      addAllButton
            .addActionListener( new AnalysisWizardStep3_addAllButton_actionAdapter(
                  this ) );
      jPanel10.add( customClassScrollPane, null );
      jPanel10.add( addedClassScrollPane, null );
      jPanel9.setPreferredSize( new Dimension( 200, 50 ) );
      addButton.setSelected( false );
      addButton.setText( "Add >" );
      addButton
            .addActionListener( new AnalysisWizardStep3_addButton_actionAdapter(
                  this ) );
      deleteButton.setSelected( false );
      deleteButton.setText( "Delete" );
      deleteButton
            .addActionListener( new AnalysisWizardStep3_delete_actionPerformed_actionAdapter(
                  this ) );
      jPanel9.add( addButton, null );
      jPanel9.add( addAllButton, null );
      jPanel9.add( deleteButton, null );
      step3Panel.add( countLabel, BorderLayout.NORTH );
      step3Panel.add( jPanel10, BorderLayout.CENTER );
      step3Panel.add( jPanel9, BorderLayout.SOUTH );

      this
            .addHelp( "<html><b>Select custom classes to include in the analysis</b><br>"
                  + "If you have not defined any custom classes, the left-hand panel will be blank. " );
      this.addMain( step3Panel );
   }

   public boolean isReady() {
      return true;
   }

   void addButton_actionPerformed( ActionEvent e ) {
      int n = customClassTable.getSelectedRowCount();
      int[] rows = customClassTable.getSelectedRows();
      for ( int i = 0; i < n; i++ ) {
         String id = ( String ) customClassTable.getValueAt( rows[i], 0 );
         if ( id != null ) {
            HashMap cfi = ( HashMap ) ccHash.get( id );
            if ( !acHash.containsKey( cfi.get( "id" ) ) ) {
               addedClasses.add( cfi );
               acHash.put( cfi.get( "id" ), cfi );
            }
         }
      }
      acTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void addAllButton_actionPerformed( ActionEvent e ) {
      for ( int i = 0; i < ccTableModel.getRowCount(); i++ ) {
         String id = ( String ) customClassTable.getValueAt( i, 0 );
         if ( id != null ) {
            HashMap cfi = ( HashMap ) ccHash.get( id );
            if ( !acHash.containsKey( cfi.get( "id" ) ) ) {
               addedClasses.add( cfi );
               acHash.put( cfi.get( "id" ), cfi );
            }
         }
      }
      acTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void delete_actionPerformed( ActionEvent e ) {
      int n = addedClassTable.getSelectedRowCount();
      int[] rows = addedClassTable.getSelectedRows();
      for ( int i = 0; i < n; i++ ) {
         String id = ( String ) addedClassTable.getValueAt( rows[i] - i, 0 );
         System.err.println( id );
         if ( id != null ) {
            HashMap cfi = ( HashMap ) ccHash.get( id );
            acHash.remove( cfi.get( "id" ) );
            addedClasses.remove( cfi );
         }
      }
      acTableModel.fireTableDataChanged();
      updateCountLabel();
   }

   void updateCountLabel() {
      countLabel.setText( "Number of Classes: " + addedClasses.size() );
   }

   void makeLeftTable() {
      File dir = new File( settings.getClassFolder() );
      if ( dir.exists() ) {
         String[] classFiles = dir
               .list( new AnalysisWizardStep3_ClassFileFilter( "-class.txt" ) );
         customClasses = new AnalysisWizardStep3_CustomClassList();
         ccHash = new HashMap();
         for ( int i = 0; i < classFiles.length; i++ ) {
            File classFile = new File( dir.getPath(), classFiles[i] );
            HashMap cfi = NewGeneSet.getClassFileInfo( classFile
                  .getAbsolutePath() );
            customClasses.add( cfi );
            ccHash.put( cfi.get( "id" ), cfi );
         }
         ccTableModel = customClasses.toTableModel();
         customClassTable.setModel( ccTableModel );
      } else
         GuiUtil
               .error( "There is no 'genesets' folder in the 'data' directory" );
   }

   void makeRightTable() {
      addedClasses = new AnalysisWizardStep3_CustomClassList();
      acTableModel = addedClasses.toTableModel();
      addedClassTable.setModel( acTableModel );
      acHash = new HashMap();
   }

   public ArrayList getAddedClasses() {
      return addedClasses;
   }
}

class AnalysisWizardStep3_delete_actionPerformed_actionAdapter implements
      java.awt.event.ActionListener {
   AnalysisWizardStep3 adaptee;

   AnalysisWizardStep3_delete_actionPerformed_actionAdapter(
         AnalysisWizardStep3 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.delete_actionPerformed( e );
   }
}

class AnalysisWizardStep3_addButton_actionAdapter implements
      java.awt.event.ActionListener {
   AnalysisWizardStep3 adaptee;

   AnalysisWizardStep3_addButton_actionAdapter( AnalysisWizardStep3 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.addButton_actionPerformed( e );
   }
}

class AnalysisWizardStep3_addAllButton_actionAdapter implements
      java.awt.event.ActionListener {
   AnalysisWizardStep3 adaptee;

   AnalysisWizardStep3_addAllButton_actionAdapter( AnalysisWizardStep3 adaptee ) {
      this.adaptee = adaptee;
   }

   public void actionPerformed( ActionEvent e ) {
      adaptee.addAllButton_actionPerformed( e );
   }
}

class AnalysisWizardStep3_ClassFileFilter implements FilenameFilter {
   private String extension;

   public AnalysisWizardStep3_ClassFileFilter( String ext ) {
      extension = ext;
   }

   public boolean accept( File dir, String name ) {
      return name.endsWith( extension );
   }
}

class AnalysisWizardStep3_CustomClassList extends ArrayList {
   public AbstractTableModel toTableModel() {
      return new AbstractTableModel() {
         private String[] columnNames = { "ID", "Description", "Members" };

         public String getColumnName( int i ) {
            return columnNames[i];
         }

         public int getColumnCount() {
            return columnNames.length;
         }

         public int getRowCount() {
            int windowrows = 10;
            int extra = 1;
            if ( size() < windowrows ) {
               extra = windowrows - size();
            }
            return size() + extra;
         }

         public Object getValueAt( int i, int j ) {
            if ( i < size() ) {
               HashMap cinfo = ( HashMap ) get( i );
               switch ( j ) {
                  case 0:
                     return cinfo.get( "id" );
                  case 1:
                     return cinfo.get( "desc" );
                  case 2: {
                     String type = ( String ) cinfo.get( "type" );
                     ArrayList members = ( ArrayList ) cinfo.get( "members" );
                     return ( Integer.toString( members.size() ) + " " + type + "s" );
                  }
                  default:
                     return null;
               }
            }
            return null;

         }
      };
   };
}