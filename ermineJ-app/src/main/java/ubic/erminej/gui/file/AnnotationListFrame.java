/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ubic.erminej.gui.file;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;

/**
 * Shows list of available annotation files for download.
 * 
 * @author paul
 * @version $Id$
 */
public class AnnotationListFrame extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final int COL0WIDTH = 80;

    private static final int COL1WIDTH = 180;

    private static final int COL2WIDTH = 60;

    // private static final int COL3WIDTH = 30;
    private final List<ArrayDesignValueObject> arrays;

    private ArrayDesignValueObject selected;

    public ArrayDesignValueObject getSelected() {
        return selected;
    }

    public AnnotationListFrame( List<ArrayDesignValueObject> a ) {
        super();

        this.arrays = new ArrayList<ArrayDesignValueObject>();

        for ( ArrayDesignValueObject v : a ) {
            if ( v.getHasAnnotationFile() ) this.arrays.add( v );
        }

        // initially sort by short name.
        Collections.sort( arrays, new Comparator<ArrayDesignValueObject>() {
            @Override
            public int compare( ArrayDesignValueObject o1, ArrayDesignValueObject o2 ) {
                return o1.getShortName().compareTo( o2.getShortName() );
            }
        } );

        this.setModal( true );
        final JTable t = new JTable();
        setTitle( "Available annotation files" );
        t.getTableHeader().setVisible( true );
        t.setModel( new ADTableModel( arrays ) );
        t.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        t.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        t.setAutoCreateRowSorter( true );
        t.getColumnModel().getColumn( 0 ).setPreferredWidth( COL0WIDTH );
        t.getColumnModel().getColumn( 1 ).setPreferredWidth( COL1WIDTH );
        t.getColumnModel().getColumn( 2 ).setPreferredWidth( COL2WIDTH );
        // t.getColumnModel().getColumn( 3 ).setPreferredWidth( COL3WIDTH );
        t.getTableHeader().setReorderingAllowed( false );

        JScrollPane scrollPane = new JScrollPane( t );
        scrollPane.setPreferredSize( new Dimension( 550, 450 ) );

        Container contentPane = this.getContentPane();
        contentPane.setLayout( new BorderLayout() );
        contentPane.add( scrollPane, BorderLayout.CENTER );
        JPanel bottomPanel = new JPanel();
        bottomPanel.setPreferredSize( new Dimension( 300, 50 ) );
        JButton selectButton = new JButton();
        selectButton.setSelected( false );
        selectButton.setText( "Select" );
        selectButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                int selectedRow = t.getSelectedRow();
                if ( selectedRow < 0 ) return;
                Object v = t.getValueAt( selectedRow, 0 );
                for ( int j = 0; j < arrays.size(); j++ ) {
                    if ( arrays.get( j ).getShortName().equals( v ) ) {
                        selected = arrays.get( j );
                        if ( selected != null /* && selected.getHasAnnotationFile() */) dispose();

                        // show alert?
                    }
                }
            }
        } );
        JButton cancelButton = new JButton();
        cancelButton.setSelected( false );
        cancelButton.setText( "Cancel" );
        cancelButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        } );

        bottomPanel.add( selectButton );

        bottomPanel.add( cancelButton, null );
        // f.add( topPanel, BorderLayout.NORTH );
        contentPane.add( bottomPanel, BorderLayout.SOUTH );

        this.pack();
        this.setVisible( true );

    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        // TODO Auto-generated method stub

    }

}

class ADTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    // private final String[] columnNames = { "Short Name", "Name", "Taxon", "Annots. Avail." };
    private final String[] columnNames = { "Short Name", "Name", "Taxon" };
    List<ArrayDesignValueObject> designs;

    public ADTableModel( List<ArrayDesignValueObject> designs ) {
        super();

        this.designs = designs;
    }

    @Override
    public String getColumnName( int column ) {
        return columnNames[column];
    }

    @Override
    public int getRowCount() {
        return designs.size();
    }

    @Override
    public int getColumnCount() {
        return 3; // 4 if we have the 'has annotation file' field.
    }

    @Override
    public Object getValueAt( int rowIndex, int columnIndex ) {
        switch ( columnIndex ) {
            case 0:
                return designs.get( rowIndex ).getShortName();

            case 1:
                return designs.get( rowIndex ).getName();

            case 2:
                return designs.get( rowIndex ).getTaxon();
                // case 3:
                // return designs.get( rowIndex ).getHasAnnotationFile();
            default:
                return "";
        }
    }

}
