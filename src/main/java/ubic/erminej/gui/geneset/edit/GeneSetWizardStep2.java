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

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import ubic.erminej.data.Element;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.gui.geneset.ProbeTableModel;
import ubic.erminej.gui.util.WizardStep;

/**
 * Step to add/remove genes from a gene set.
 *
 * @author Homin K Lee
 * @version $Id$
 */
public class GeneSetWizardStep2 extends WizardStep {

    private static final long serialVersionUID = 1L;

    private final static int COL0WIDTH = 80;

    private final static int COL1WIDTH = 80;

    private final static int COL2WIDTH = 200;

    private GeneAnnotations geneData = null;

    private JTable probeTable = null;

    private JTable newClassTable = null;

    private ProbeTableModel ncTableModel = null;
    private JTextField searchTextField = null;
    private ProbeTableModel sourceProbeModel;

    JLabel jLabel2 = new JLabel();

    /**
     * <p>
     * Constructor for GeneSetWizardStep2.
     * </p>
     *
     * @param wiz a {@link ubic.erminej.gui.geneset.edit.GeneSetWizard} object.
     * @param geneData a {@link ubic.erminej.data.GeneAnnotations} object.
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
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Element> getProbes() {
        return this.ncTableModel.getProbes();
    }

    /** {@inheritDoc} */
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

    /**
     * <p>
     * updateCountLabel.
     * </p>
     */
    public void updateCountLabel() {
        if ( ncTableModel.getProbeCount() != ncTableModel.getGeneCount() ) {
            showStatus( ncTableModel.getGeneCount() + " genes selected [" + ncTableModel.getProbeCount() + " elements]" );
        } else {
            showStatus( ncTableModel.getGeneCount() + " genes selected" );

        }
    }

    /**
     * @param gene
     */
    void addGeneToSet( Gene gene ) {
        Collection<Element> probelist = gene.getProbes();

        if ( probelist.size() == 0 ) {
            showError( "No elements for gene " + gene );
            return;
        }

        log.debug( "Got " + probelist.size() + " new elements to add" );
        ncTableModel.addProbes( probelist );
        ncTableModel.fireTableDataChanged();

        sourceProbeModel.removeProbes( probelist );
        sourceProbeModel.fireTableDataChanged();

        updateCountLabel();
    }

    void addProbesFromLeftTableToRight() {
        int[] rows = probeTable.getSelectedRows();
        log.debug( rows.length + " rows selected" );

        Collection<Element> elements = new HashSet<>();
        for ( int i = 0; i < rows.length; i++ ) {
            String probe = ( String ) probeTable.getValueAt( rows[i], 0 );
            log.debug( "Got probe: " + probe );
            Element p = geneData.findElement( probe );
            if ( p != null ) elements.add( p );

        }

        ncTableModel.addProbes( elements );
        sourceProbeModel.removeProbes( elements );
        updateCountLabel();
    }

    void deleteProbesFromRightTable() {
        int[] rows = newClassTable.getSelectedRows();
        Collection<Element> elements = new HashSet<>();
        for ( int i = 0; i < rows.length; i++ ) {
            String probe = ( String ) newClassTable.getValueAt( rows[i], 0 );
            log.debug( "Removing " + probe );
            Element p = geneData.findElement( probe );

            // remove all of the elements for the gene, not just the selected one (otherwise doesn't make much sense).
            if ( p != null ) {
                for ( Gene g : p.getGenes() ) {
                    elements.addAll( g.getProbes() );
                }
            }

        }
        ncTableModel.removeProbes( elements );
        sourceProbeModel.addProbes( elements );
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

        Element p = geneData.findElement( newProbe );
        if ( p == null ) {
            showError( "Element" + newProbe + " does not exist." );
            return;
        }
        Gene g = p.getGene();
        this.addGeneToSet( g );

    }

    void find() {
        String searchOn = searchTextField.getText();

        Collection<Element> leftHandProbes = new HashSet<>();
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

    // Component initialization
    /** {@inheritDoc} */
    @Override
    protected void jbInit() {
        this.setLayout( new BorderLayout() );
        JPanel topPanel = new JPanel();
        // countLabel = new JLabel();
        JLabel jLabel1 = new JLabel();

        jLabel1.setMinimumSize( new Dimension( 250, 19 ) );
        jLabel1.setText( "All available genes (or elements)" );
        jLabel2.setMinimumSize( new Dimension( 250, 19 ) );
        jLabel2.setText( "Gene set members" );
        showStatus( "0 genes selected" );
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
        GroupLayout gl = new GroupLayout( bottomPanel );
        bottomPanel.setLayout( gl );
        bottomPanel.setBorder( BorderFactory.createEmptyBorder( 5, 15, 5, 15 ) );
        bottomPanel.setMinimumSize( new Dimension( 200, 30 ) );

        JButton searchButton = new JButton();
        searchButton.setText( "Find" );
        searchButton.addActionListener( new GeneSetWizardStep2_searchButton_actionAdapter( this ) );

        searchTextField = new JTextField();
        searchTextField.setMinimumSize( new Dimension( 50, 19 ) );
        searchTextField.setToolTipText( "Enter search terms for genes here" );
        searchTextField.getInsets().set( 5, 8, 5, 15 );
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

        gl.setHorizontalGroup( gl.createSequentialGroup().addComponent( searchButton ).addGap( 3 )
                .addComponent( searchTextField ).addGap( 15 ).addComponent( addButton ).addComponent( deleteButton ) );

        gl.setVerticalGroup( gl.createParallelGroup().addComponent( searchButton ).addComponent( searchTextField )
                .addComponent( addButton ).addComponent( deleteButton ) );

        bottomPanel.add( searchButton );
        bottomPanel.add( searchTextField );
        bottomPanel.add( addButton );
        bottomPanel.add( deleteButton );

        step2Panel.add( topPanel, BorderLayout.NORTH );
        step2Panel.add( centerPanel, BorderLayout.CENTER );
        step2Panel.add( bottomPanel, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Set up the gene set</b><br>"
                + "Add or remove elements/genes using the buttons below the table. "
                + "The list of all possible available elements is provided at the left. "
                + "The list of elements/genes that are in the gene set is given at right. "
                + "To find a specific gene use the 'find' tool. "
                + "If you don't want to make any changes, press 'cancel'." );
        this.addMain( step2Panel );
    }

    /**
     * <p>
     * setStartingSet.
     * </p>
     *
     * @param genes a {@link java.util.Collection} object.
     */
    protected void setStartingSet( Collection<Gene> genes ) {
        for ( Gene gene : genes ) {
            this.ncTableModel.addProbes( gene.getProbes() );
            this.sourceProbeModel.removeProbes( gene.getProbes() );
        }

    }

    /**
     * <p>
     * setStartingSet.
     * </p>
     *
     * @param original a {@link ubic.erminej.data.GeneSet} object.
     */
    protected void setStartingSet( GeneSet original ) {
        assert jLabel2 != null;
        jLabel2.setText( original.toString() );
        this.ncTableModel.setProbes( original.getProbes() );
        this.sourceProbeModel.removeProbes( original.getProbes() );
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

        ncTableModel = new ProbeTableModel( new HashSet<Element>() );
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
        showStatus( "Available elements: " + geneData.numProbes() );
    }

}

class GeneSetWizardStep2_addButton_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_addButton_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.addProbesFromLeftTableToRight();
    }
}

class GeneSetWizardStep2_delete_actionPerformed_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_delete_actionPerformed_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.deleteProbesFromRightTable();
    }
}

class GeneSetWizardStep2_editorGeneAdaptor implements CellEditorListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_editorGeneAdaptor( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
        /** {@inheritDoc} */
    }

    @Override
    public void editingCanceled( ChangeEvent e ) {
        editingCanceled( e );
        /** {@inheritDoc} */
    }

    @Override
    public void editingStopped( ChangeEvent e ) {
        adaptee.editorGene_actionPerformed( e );
    }
}

class GeneSetWizardStep2_editorProbeAdaptor implements CellEditorListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_editorProbeAdaptor( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void editingCanceled( ChangeEvent e ) {
        editingCanceled( e );
    }

    /** {@inheritDoc} */
    @Override
    public void editingStopped( ChangeEvent e ) {
        adaptee.editorProbe_actionPerformed( e );
    }
}

// hitting enter in search also activates it.

/**
 * <p>
 * Constructor for GeneSetWizardStep2_searchButton_actionAdapter.
 * </p>
 *
 * @param adaptee a {@link ubic.erminej.gui.geneset.edit.GeneSetWizardStep2} object.
 */
class GeneSetWizardStep2_searchButton_actionAdapter implements ActionListener {
    GeneSetWizardStep2 adaptee;

    public GeneSetWizardStep2_searchButton_actionAdapter( GeneSetWizardStep2 adaptee ) {
        /** {@inheritDoc} */
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.searchButton_actionPerformed_adapter();
        /**
         * <p>
         * Constructor for GeneSetWizardStep2_searchText_actionAdapter.
         * </p>
         *
         * @param adaptee a {@link ubic.erminej.gui.geneset.edit.GeneSetWizardStep2} object.
         */
    }

}

/** {@inheritDoc} */
// respond to typing in the search field. - incremental search could go here.

class GeneSetWizardStep2_searchText_actionAdapter implements ActionListener {
    GeneSetWizardStep2 adaptee;

    /**
     * <p>
     * Constructor for GeneSetWizardStep2_searchText_keyAdapter.
     * </p>
     *
     * @param adaptee a {@link ubic.erminej.gui.geneset.edit.GeneSetWizardStep2} object.
     */
    public GeneSetWizardStep2_searchText_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.searchButton_actionPerformed_adapter();
    }
    /** {@inheritDoc} */
}

// respond to search request.

/** {@inheritDoc} */
class GeneSetWizardStep2_searchText_keyAdapter implements KeyListener {

    GeneSetWizardStep2 adaptee;

    public GeneSetWizardStep2_searchText_keyAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void keyPressed( KeyEvent e ) {
    }

    @Override
    public void keyReleased( KeyEvent e ) {
    }

    @Override
    public void keyTyped( KeyEvent e ) {
    }

}

class GeneSetWizardStep2_searchTextField_actionAdapter implements java.awt.event.ActionListener {
    GeneSetWizardStep2 adaptee;

    GeneSetWizardStep2_searchTextField_actionAdapter( GeneSetWizardStep2 adaptee ) {
        this.adaptee = adaptee;
        /** {@inheritDoc} */
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.searchTextField_actionPerformed();
    }
}
