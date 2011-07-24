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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.geneset.SimpleGeneSetListTableModel;

/**
 * Step to choose a gene set to modify from the full list.
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class GeneSetWizardStep1A extends WizardStep {

    private static final long serialVersionUID = -1L;
    private GeneAnnotations geneData = null;
    private JTable oldClassTable = null;
    private JTextField searchTextField = null;

    public GeneSetWizardStep1A( GeneSetWizard wiz, GeneAnnotations geneData, Collection<GeneSet> geneSets ) {
        super( wiz );
        this.jbInit();
        this.geneData = geneData;
        wiz.clearStatus();
        populateTables( geneSets );
    }

    public GeneSetWizardStep1A( GeneSetWizard wiz, GeneAnnotations geneData ) {
        super( wiz );
        this.jbInit();
        this.geneData = geneData;
        wiz.clearStatus();
        populateTables( null );
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
        JScrollPane oldClassScrollPane = new JScrollPane( oldClassTable );
        oldClassScrollPane.setPreferredSize( new Dimension( 250, 230 ) );

        step1MPanel.setPreferredSize( new Dimension( 250, 250 ) );
        step1MPanel.add( oldClassScrollPane, BorderLayout.CENTER );

        JPanel searchPanel = new JPanel();

        JButton searchButton = new JButton();
        searchButton.setText( "Find" );
        searchButton.addActionListener( new GeneSetWizardStep1A_searchButton_actionAdapter( this ) );

        searchPanel.add( searchButton );

        searchTextField = new JTextField();
        searchTextField.setPreferredSize( new Dimension( 80, 19 ) );
        searchTextField.addActionListener( new ActionListener() {
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

    @Override
    public boolean isReady() {
        int n = oldClassTable.getSelectedRowCount();
        if ( n < 1 ) {
            showError( "You must pick a gene set to be modified." );
            return false;
        }
        int row = oldClassTable.getSelectedRow();
        String id = ( String ) oldClassTable.getValueAt( row, 0 );

        GeneSet geneSet = geneData.findGeneSet( id );

        assert geneSet != null;

        ( ( GeneSetWizard ) this.owner ).setOldGeneSet( geneSet );

        /*
         * Clone the old gene set.
         */
        GeneSetTerm oldGeneSetTerm = geneSet.getTerm();

        // if ( !oldGeneSetTerm.isUserDefined() ) {
        // throw new IllegalArgumentException( "You can only modify your own gene sets" ); // for now
        // }

        // clone the set.
        GeneSetTerm updatedGeneSetTerm = new GeneSetTerm( oldGeneSetTerm.getId(), oldGeneSetTerm.getName(),
                oldGeneSetTerm.getDefinition() );
        updatedGeneSetTerm.setAspect( oldGeneSetTerm.getAspect() );
        GeneSet updatedGeneSet = new GeneSet( updatedGeneSetTerm );
        updatedGeneSet.addGenes( geneSet.getGenes() );

        ( ( GeneSetWizard ) this.owner ).setNewGeneSet( updatedGeneSet );

        log.info( "Editing a copy of  " + updatedGeneSet );

        /*
         * Now we're ready to edit the copy.
         */

        return true;

    }

    private void populateTables( Collection<GeneSet> geneSets ) {
        SimpleGeneSetListTableModel model;
        if ( geneSets == null || geneSets.isEmpty() ) {
            model = new SimpleGeneSetListTableModel( geneData );
        } else {
            model = new SimpleGeneSetListTableModel( geneSets );
        }

        oldClassTable.setAutoCreateRowSorter( true );
        oldClassTable.setModel( model );
        oldClassTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 30 );
        oldClassTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 30 );
        oldClassTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 30 );
        oldClassTable.revalidate();

        showStatus( "Available sets: " + geneData.numGeneSets() );
    }

    public void find() {
        String searchOn = searchTextField.getText();
        Collection<GeneSetTerm> terms;
        if ( searchOn.equals( "" ) ) {
            terms = geneData.getActiveGeneSets();
        } else {
            terms = geneData.findSetsByName( searchOn );
        }
        populateTables( geneData.getGeneSets( terms ) );
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

    public void actionPerformed( ActionEvent e ) {
        adaptee.find();

    }

}
