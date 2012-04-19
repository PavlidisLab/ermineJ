/*
 * The ermineJ project
 * 
 * Copyright (c) 2006-2011 University of British Columbia
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
package ubic.erminej.gui.geneset.edit;

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

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.Probe;
import ubic.erminej.gui.geneset.ProbeTableModel;
import ubic.erminej.gui.util.WizardStep;

/**
 * Step to add/remove probes/genes from a gene set.
 * 
 * @author Homin K Lee
 * @version $Id$
 */
public class GeneSetWizardStep2 extends WizardStep {

    private static final long serialVersionUID = 1L;

    private GeneAnnotations geneData = null;

    private JTable probeTable = null;

    private JTable newClassTable = null;

    private ProbeTableModel ncTableModel = null;

    private JTextField searchTextField = null;

    private ProbeTableModel sourceProbeModel;

    private final static int COL0WIDTH = 80;
    private final static int COL1WIDTH = 80;
    private final static int COL2WIDTH = 200;

    JLabel jLabel2 = new JLabel();

    /**
     * @param wiz
     * @param geneData
     */
    public GeneSetWizardStep2( GeneSetWizard wiz, GeneAnnotations geneData ) {
        super( wiz );
        this.jbInit();
        this.geneData = geneData;
        wiz.clearStatus();
        populateTables();
    }

    /**
     * Get the results of the user's picking.
     * 
     * @return
     */
    public Collection<Probe> getProbes() {
        return this.ncTableModel.getProbes();
    }

    @Override
    public boolean isReady() {
        return !getProbes().isEmpty();

    }

    /**
     * do a search.
     */
    public void searchButton_actionPerformed_adapter() {
        find();
    }

    public void updateCountLabel() {
        showStatus( "Number of Probes selected: " + ncTableModel.getProbeCount() + " [ " + ncTableModel.getGeneCount()
                + " genes]" );
    }

    // Component initialization
    @Override
    protected void jbInit() {
        this.setLayout( new BorderLayout() );
        JPanel topPanel = new JPanel();
        // countLabel = new JLabel();
        JLabel jLabel1 = new JLabel();

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

        /*
         * Left panel
         */
        probeTable = new JTable();
        probeTable.getTableHeader().setReorderingAllowed( false );

        JScrollPane probeScrollPane = new JScrollPane( probeTable );
        probeScrollPane.setPreferredSize( new Dimension( 250, 150 ) );

        /*
         * Right panel
         */
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

    protected void setStartingSet( GeneSet original ) {
        assert jLabel2 != null;
        jLabel2.setText( original.toString() );
        this.ncTableModel.setProbes( original.getProbes() );
        this.sourceProbeModel.removeProbes( original.getProbes() );
    }

    protected void setStartingSet( Collection<Gene> genes ) {
        for ( Gene gene : genes ) {
            this.ncTableModel.addProbes( gene.getProbes() );
            this.sourceProbeModel.removeProbes( gene.getProbes() );
        }

    }

    /**
     * @param gene
     */
    void addGeneToSet( Gene gene ) {
        Collection<Probe> probelist = gene.getProbes();

        if ( probelist.size() == 0 ) {
            showError( "No probes for gene " + gene );
            return;
        }

        log.debug( "Got " + probelist.size() + " new probes to add" );
        ncTableModel.addProbes( probelist );
        ncTableModel.fireTableDataChanged();

        sourceProbeModel.removeProbes( probelist );
        sourceProbeModel.fireTableDataChanged();

        updateCountLabel();
    }

    void addProbesFromLeftTableToRight() {
        int[] rows = probeTable.getSelectedRows();
        log.debug( rows.length + " rows selected" );

        Collection<Probe> probes = new HashSet<Probe>();
        for ( int i = 0; i < rows.length; i++ ) {
            String probe = ( String ) probeTable.getValueAt( rows[i], 0 );
            log.debug( "Got probe: " + probe );
            Probe p = geneData.findProbe( probe );
            if ( p != null ) probes.add( p );

        }

        ncTableModel.addProbes( probes );
        sourceProbeModel.removeProbes( probes );
        updateCountLabel();
    }

    void deleteProbesFromRightTable() {
        int[] rows = newClassTable.getSelectedRows();
        Collection<Probe> probes = new HashSet<Probe>();
        for ( int i = 0; i < rows.length; i++ ) {
            String probe = ( String ) newClassTable.getValueAt( rows[i], 0 );
            log.debug( "Removing " + probe );
            Probe p = geneData.findProbe( probe );

            // remove all of the probes for the gene, not just the selected one (otherwise doesn't make much sense).
            if ( p != null ) {
                for ( Gene g : p.getGenes() ) {
                    probes.addAll( g.getProbes() );
                }
            }

        }
        ncTableModel.removeProbes( probes );
        sourceProbeModel.addProbes( probes );
        updateCountLabel();
    }

    void editorGene_actionPerformed( ChangeEvent e ) {
        String newGene = ( String ) ( ( DefaultCellEditor ) e.getSource() ).getCellEditorValue();

        Gene g = geneData.findGene( newGene );
        if ( g == null ) {
            showError( "Gene " + newGene + " does not exist." );
            return;
        }

        this.addGeneToSet( g );
    }

    void editorProbe_actionPerformed( ChangeEvent e ) {
        String newProbe = ( String ) ( ( DefaultCellEditor ) e.getSource() ).getCellEditorValue();

        Probe p = geneData.findProbe( newProbe );
        if ( p == null ) {
            showError( "Probe " + newProbe + " does not exist." );
            return;
        }
        Gene g = p.getGene();
        this.addGeneToSet( g );

    }

    void find() {
        String searchOn = searchTextField.getText();

        Collection<Probe> leftHandProbes = new HashSet<Probe>();
        if ( searchOn.equals( "" ) ) {
            leftHandProbes = geneData.getProbes();
        } else {
            leftHandProbes = geneData.findProbes( searchOn );
        }
        sourceProbeModel.setProbes( leftHandProbes );
    }

    void searchTextField_actionPerformed() {
        find();
    }

    /**
     * 
     */
    private void populateTables() {

        sourceProbeModel = new ProbeTableModel( geneData );

        probeTable.setModel( sourceProbeModel );
        probeTable.setAutoCreateRowSorter( true );
        probeTable.getColumnModel().getColumn( 0 ).setPreferredWidth( COL0WIDTH );
        probeTable.getColumnModel().getColumn( 1 ).setPreferredWidth( COL1WIDTH );
        probeTable.getColumnModel().getColumn( 2 ).setPreferredWidth( COL2WIDTH );

        probeTable.getTableHeader().addMouseListener( new MouseAdapter() {
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

        ncTableModel = new ProbeTableModel( new HashSet<Probe>() );
        newClassTable.setModel( ncTableModel );
        newClassTable.setAutoCreateRowSorter( true );
        newClassTable.getTableHeader().addMouseListener( new MouseAdapter() {
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
        showStatus( "Available probes: " + geneData.numProbes() );
    }

}

class GeneSetWizardStep2_addButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_addButton_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.addProbesFromLeftTableToRight();
    }
}

class GeneSetWizardStep2_delete_actionPerformed_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_delete_actionPerformed_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.deleteProbesFromRightTable();
    }
}

class GeneSetWizardStep2_editorGeneAdaptor implements CellEditorListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_editorGeneAdaptor( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void editingCanceled( ChangeEvent e ) {
        editingCanceled( e );
    }

    public void editingStopped( ChangeEvent e ) {
        adaptee.editorGene_actionPerformed( e );
    }
}

class GeneSetWizardStep2_editorProbeAdaptor implements CellEditorListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_editorProbeAdaptor( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void editingCanceled( ChangeEvent e ) {
        editingCanceled( e );
    }

    public void editingStopped( ChangeEvent e ) {
        adaptee.editorProbe_actionPerformed( e );
    }
}

// hitting enter in search also activates it.

class GeneSetWizardStep2_searchButton_actionAdapter implements ActionListener {
    GeneSetWizardStep2 adaptee;

    public GeneSetWizardStep2_searchButton_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.searchButton_actionPerformed_adapter();
    }

}

// respond to typing in the search field. - incremental search could go here.

class GeneSetWizardStep2_searchText_actionAdapter implements ActionListener {
    GeneSetWizardStep2 adaptee;

    public GeneSetWizardStep2_searchText_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.searchButton_actionPerformed_adapter();
    }
}

// respond to search request.

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

class GeneSetWizardStep2_searchTextField_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_searchTextField_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.searchTextField_actionPerformed();
    }
}