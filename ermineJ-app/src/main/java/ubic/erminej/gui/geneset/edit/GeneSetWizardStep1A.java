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
package ubic.erminej.gui.geneset.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.geneset.SimpleGeneSetListTableModel;
import ubic.erminej.gui.util.WizardStep;

/**
 * Step to choose a gene set to modify from the a list.
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class GeneSetWizardStep1A extends WizardStep {

    private static final long serialVersionUID = -1L;
    private GeneAnnotations geneData = null;
    private JTable oldClassTable = null;
    private JTextField searchTextField = null;
    private GeneSet selectedGeneSet;

    /**
     * Only show the user's sets
     * 
     * @param wiz
     * @param geneData
     */
    public GeneSetWizardStep1A( GeneSetWizard wiz, GeneAnnotations geneData ) {
        super( wiz );
        this.jbInit();
        this.geneData = geneData;
        wiz.clearStatus();
        populateTables( null );
    }

    public GeneSetWizardStep1A( GeneSetWizard wiz, GeneAnnotations geneData, Collection<GeneSet> geneSets ) {
        super( wiz );
        this.jbInit();
        this.geneData = geneData;
        wiz.clearStatus();
        populateTables( geneSets );
    }

    public void find() {
        String searchOn = searchTextField.getText();
        Collection<GeneSetTerm> terms;
        if ( searchOn.equals( "" ) ) {
            terms = geneData.getUserDefinedTerms();
        } else {
            terms = geneData.findSetsByName( searchOn );
            terms.retainAll( geneData.getUserDefinedTerms() );
        }
        Collection<GeneSet> foundGeneSets = geneData.getGeneSets( terms );
        populateTables( foundGeneSets );

    }

    public GeneSet getSelectedGeneSet() {
        return selectedGeneSet;
    }

    @Override
    public boolean isReady() {
        int n = oldClassTable.getSelectedRowCount();
        if ( n < 1 ) {
            showError( "You must pick a gene set to be modified." );
            return false;
        }

        int row = oldClassTable.getSelectedRow();
        String id = ( String ) oldClassTable.getValueAt( row, 0 );

        this.selectedGeneSet = geneData.findGeneSet( id );
        assert selectedGeneSet != null;

        return true;

    }

    // Component initialization
    @Override
    protected void jbInit() {
        this.setLayout( new BorderLayout() );

        JPanel step1MPanel = new JPanel();
        step1MPanel.setLayout( new BorderLayout() );

        oldClassTable = new JTable();
        oldClassTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        oldClassTable.setPreferredScrollableViewportSize( new Dimension( 250, 150 ) );
        oldClassTable.getTableHeader().setReorderingAllowed( false );
        oldClassTable.getTableHeader().addMouseListener( new MouseAdapter() {
            @Override
            public void mouseEntered( MouseEvent e ) {
                getParent().setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                getParent().setCursor( Cursor.getDefaultCursor() );
            }
        } );

        JScrollPane oldClassScrollPane = new JScrollPane( oldClassTable );
        oldClassScrollPane.setPreferredSize( new Dimension( 250, 230 ) );

        step1MPanel.setPreferredSize( new Dimension( 250, 250 ) );
        step1MPanel.add( oldClassScrollPane, BorderLayout.CENTER );
        step1MPanel.setBackground( Color.BLUE );

        JPanel searchPanel = new JPanel();
        searchPanel.setMinimumSize( new Dimension( 150, 20 ) );

        JButton searchButton = new JButton();
        searchButton.setText( "Find" );
        searchButton.addActionListener( new GeneSetWizardStep1A_searchButton_actionAdapter( this ) );

        searchPanel.add( searchButton );

        searchTextField = new JTextField( 18 );
        searchTextField.setMinimumSize( new Dimension( 100, 19 ) );

        searchTextField.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                find();
            }
        } );

        searchPanel.add( searchTextField );

        step1MPanel.add( searchPanel, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Pick a gene set to modify.</b><br>"
                + "You will be asked to add or remove genes from this set in the next step." );
        this.addMain( step1MPanel );
    }

    /**
     * @param geneSets
     */
    private void populateTables( Collection<GeneSet> geneSets ) {
        SimpleGeneSetListTableModel model;
        if ( geneSets == null ) {
            model = new SimpleGeneSetListTableModel( geneData.getUserDefinedGeneSets() );
            showStatus( "Showing " + geneData.getUserDefinedGeneSets().size() + " user-defined sets" );

        } else {
            model = new SimpleGeneSetListTableModel( geneSets );
            showStatus( "Showing " + geneSets.size() + " user-defined sets" );
        }
        TableRowSorter<SimpleGeneSetListTableModel> sorter = new TableRowSorter<SimpleGeneSetListTableModel>( model );
        oldClassTable.setRowSorter( sorter );
        oldClassTable.setModel( model );
        oldClassTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 30 );
        oldClassTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 30 );
        oldClassTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 30 );
        oldClassTable.revalidate();

    }
}

/**
 * @author paul
 * @version $Id$
 */

class GeneSetWizardStep1A_searchButton_actionAdapter implements ActionListener {

    GeneSetWizardStep1A adaptee;

    public GeneSetWizardStep1A_searchButton_actionAdapter( GeneSetWizardStep1A adaptee ) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        adaptee.find();

    }

}
