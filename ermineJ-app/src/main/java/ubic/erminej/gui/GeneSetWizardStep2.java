/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
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
package ubic.erminej.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.data.UserDefinedGeneSetManager;
import ubic.erminej.gui.table.TableSorter;

/**
 * Step to add/remove probes/genes from a gene set.
 * 
 * @author Homin K Lee
 * @version $Id$
 */

public class GeneSetWizardStep2 extends WizardStep {

    /**
     * 
     */
    private static final long serialVersionUID = 4023050874708943617L;
    private GeneAnnotations geneData = null;
    private JTable probeTable = null;
    private JTable newClassTable = null;
    private AbstractTableModel ncTableModel = null;
    private UserDefinedGeneSetManager newGeneSet = null;
    private JTextField searchTextField = null;

    private final static int COL0WIDTH = 80;
    private final static int COL1WIDTH = 80;
    private final static int COL2WIDTH = 200;

    public GeneSetWizardStep2( GeneSetWizard wiz, GeneAnnotations geneData, UserDefinedGeneSetManager newGeneSet ) {
        super( wiz );
        this.jbInit();
        this.geneData = geneData;
        this.newGeneSet = newGeneSet;
        wiz.clearStatus();
        geneData.resetSelectedProbes();
        populateTables();
    }

    // Component initialization
    @Override
    protected void jbInit() {
        this.setLayout( new BorderLayout() );
        JPanel topPanel = new JPanel();
        // countLabel = new JLabel();
        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        jLabel1.setPreferredSize( new Dimension( 250, 15 ) );
        jLabel1.setText( "All available probes and genes" );
        jLabel2.setPreferredSize( new Dimension( 250, 15 ) );
        jLabel2.setText( "Gene set members" );
        showStatus( "Number of Probes selected: 0" );
        topPanel.add( jLabel1, null );
        topPanel.add( jLabel2, null );

        final JPanel step2Panel = new JPanel();
        step2Panel.setLayout( new BorderLayout() );

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout( new GridLayout() );

        probeTable = new JTable();
        probeTable.getTableHeader().setReorderingAllowed( false );

        JScrollPane probeScrollPane = new JScrollPane( probeTable );
        probeScrollPane.setPreferredSize( new Dimension( 250, 150 ) );

        newClassTable = new JTable();
        newClassTable.getTableHeader().setReorderingAllowed( false );

        JScrollPane newClassScrollPane = new JScrollPane( newClassTable );
        newClassScrollPane.setPreferredSize( new Dimension( 250, 150 ) );

        centerPanel.add( probeScrollPane, null );
        centerPanel.add( newClassScrollPane, null );

        JPanel bottomPanel = new JPanel();
        bottomPanel.setPreferredSize( new Dimension( 300, 50 ) );

        JButton searchButton = new JButton();
        searchButton.setText( "Find" );
        searchButton.addActionListener( new GeneSetWizardStep2_searchButton_actionAdapter( this ) );

        searchTextField = new JTextField();
        searchTextField.setPreferredSize( new Dimension( 80, 19 ) );
        searchTextField.addKeyListener( new GeneSetWizardStep2_searchText_keyAdapter( this ) );
        searchTextField.addActionListener( new GeneSetWizardStep2_searchTextField_actionAdapter( this ) );
        JButton addButton = new JButton();
        addButton.setSelected( false );
        addButton.setText( "Add >" );
        addButton.addActionListener( new GeneSetWizardStep2_addButton_actionAdapter( this ) );
        JButton deleteButton = new JButton();
        deleteButton.setSelected( false );
        deleteButton.setText( "Delete" );
        deleteButton.addActionListener( new GeneSetWizardStep2_delete_actionPerformed_actionAdapter( this ) );

        bottomPanel.add( searchButton );
        bottomPanel.add( searchTextField );
        bottomPanel.add( addButton, null );
        bottomPanel.add( deleteButton, null );
        step2Panel.add( topPanel, BorderLayout.NORTH );
        step2Panel.add( centerPanel, BorderLayout.CENTER );
        step2Panel.add( bottomPanel, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Set up the gene set</b><br>"
                + "Add or remove probes/genes using the buttons below the table. "
                + "The list of all possible available probes is provided at the left. "
                + "The list of probes/genes that are in the gene set is given at right. "
                + "To find a specific gene use the 'find' tool. "
                + "If you don't want to make any changes, press 'cancel'." );
        this.addMain( step2Panel );
    }

    @Override
    public boolean isReady() {
        if ( newGeneSet.getProbes().size() == 0 ) {
            return false;
        }

        return true;
    }

    void delete_actionPerformed() {
        int[] rows = newClassTable.getSelectedRows();
        for ( int i = 0; i < rows.length; i++ ) {
            Object probe = newClassTable.getValueAt( rows[i] - i, 0 );
            log.debug( "Removing " + probe );
            newGeneSet.getProbes().remove( probe );
        }
        ncTableModel.fireTableDataChanged();
        updateCountLabel();
    }

    void addButton_actionPerformed() {
        int[] rows = probeTable.getSelectedRows();
        log.debug( rows.length + " rows selected" );
        for ( int i = 0; i < rows.length; i++ ) {
            String probe = ( String ) probeTable.getValueAt( rows[i], 0 );
            log.debug( "Got probe: " + probe );
            String newGene;
            if ( ( newGene = geneData.getProbeGeneName( probe ) ) != null ) {
                log.debug( "Adding " + newGene );
                this.addGene( newGene );
            }
        }
        Set<String> noDupes = new HashSet<String>( newGeneSet.getProbes() );
        newGeneSet.getProbes().clear();
        newGeneSet.getProbes().addAll( noDupes );
        ncTableModel.fireTableDataChanged();
        updateCountLabel();
    }

    void editorProbe_actionPerformed( ChangeEvent e ) {
        String newProbe = ( String ) ( ( DefaultCellEditor ) e.getSource() ).getCellEditorValue();
        String newGene;
        if ( ( newGene = geneData.getProbeGeneName( newProbe ) ) != null ) {
            this.addGene( newGene );
        } else {
            showError( "Probe " + newProbe + " does not exist." );
        }
    }

    void editorGene_actionPerformed( ChangeEvent e ) {
        String newGene = ( String ) ( ( DefaultCellEditor ) e.getSource() ).getCellEditorValue();
        this.addGene( newGene );
    }

    /**
     * @param gene
     */
    void addGene( String gene ) {
        Collection<String> probelist = geneData.getGeneProbeList( gene );
        if ( probelist == null ) {
            showError( "Gene " + gene + " does not exist." );
            return;
        }

        if ( probelist.size() == 0 ) {
            showError( "No probes for gene " + gene );
            return;
        }

        log.debug( "Got " + probelist.size() + " new probes to add" );
        newGeneSet.getProbes().addAll( probelist );
        ncTableModel.fireTableDataChanged();
        updateCountLabel();
    }

    public void updateCountLabel() {
        showStatus( "Number of Probes selected: " + newGeneSet.getProbes().size() );
    }

    private void populateTables() {
        ProbeTableModel model = new ProbeTableModel( geneData );
        TableSorter sorter = new TableSorter( model );
        probeTable.setModel( sorter );
        sorter.setTableHeader( probeTable.getTableHeader() );
        probeTable.getColumnModel().getColumn( 0 ).setPreferredWidth( COL0WIDTH );
        probeTable.getColumnModel().getColumn( 1 ).setPreferredWidth( COL1WIDTH );
        probeTable.getColumnModel().getColumn( 2 ).setPreferredWidth( COL2WIDTH );

        sorter.getTableHeader().addMouseListener( new MouseAdapter() {
            @Override
            public void mouseEntered( MouseEvent e ) {
                getParent().setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                getParent().setCursor( Cursor.getDefaultCursor() );
            }
        } );

        probeTable.revalidate();

        ncTableModel = newGeneSet.toTableModel( false );
        TableSorter anotherSorter = new TableSorter( ncTableModel );
        newClassTable.setModel( anotherSorter );
        anotherSorter.setTableHeader( newClassTable.getTableHeader() );
        anotherSorter.getTableHeader().addMouseListener( new MouseAdapter() {
            @Override
            public void mouseEntered( MouseEvent e ) {
                getParent().setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                getParent().setCursor( Cursor.getDefaultCursor() );
            }
        } );
        newClassTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 40 );
        newClassTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 40 );
        newClassTable.revalidate();
        showStatus( "Available probes: " + geneData.numSelectedProbes() );
    }

    /**
     * do a search.
     */
    public void searchButton_actionPerformed_adapter() {
        find();
    }

    void searchTextField_actionPerformed() {
        find();
    }

    void find() {
        String searchOn = searchTextField.getText();

        if ( searchOn.equals( "" ) ) {
            geneData.resetSelectedProbes();
        } else {
            geneData.selectProbesBySearch( searchOn );
        }
        populateTables();
    }
}

class GeneSetWizardStep2_delete_actionPerformed_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_delete_actionPerformed_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.delete_actionPerformed();
    }
}

class GeneSetWizardStep2_addButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_addButton_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.addButton_actionPerformed();
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

// hitting enter in search also activates it.

class GeneSetWizardStep2_searchText_actionAdapter implements ActionListener {
    GeneSetWizardStep2 adaptee;

    public GeneSetWizardStep2_searchText_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.searchButton_actionPerformed_adapter();
    }
}

// respond to typing in the search field. - incremental search could go here.

class GeneSetWizardStep2_searchText_keyAdapter implements KeyListener {

    GeneSetWizardStep2 adaptee;

    public GeneSetWizardStep2_searchText_keyAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void keyPressed( KeyEvent e ) {
    }

    public void keyReleased( KeyEvent e ) {
    }

    public void keyTyped( KeyEvent e ) {
    }

}

class ProbeTableModel extends AbstractTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = -4672326703011127415L;
    GeneAnnotations geneData;
    private String[] columnNames = { "Probe", "Gene", "Description" };

    public ProbeTableModel( GeneAnnotations geneData ) {
        this.geneData = geneData;
    }

    @Override
    public String getColumnName( int i ) {
        return columnNames[i];
    }

    public int getColumnCount() {
        return 3;
    }

    public int getRowCount() {
        return geneData.getSelectedProbes().size();
    }

    public Object getValueAt( int i, int j ) {

        String probeid = geneData.getSelectedProbes().get( i );
        switch ( j ) {
            case 0:
                return probeid;
            case 1:
                return geneData.getProbeGeneName( probeid );
            case 2:
                return geneData.getProbeDescription( probeid );
            default:
                return null;
        }
    }
}

// respond to search request.

class GeneSetWizardStep2_searchButton_actionAdapter implements ActionListener {
    GeneSetWizardStep2 adaptee;

    public GeneSetWizardStep2_searchButton_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.searchButton_actionPerformed_adapter();
    }

}

class GeneSetWizardStep2_searchTextField_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_searchTextField_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.searchTextField_actionPerformed();
    }
}