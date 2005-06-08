package classScore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.gui.WizardStep;
import baseCode.gui.table.TableSorter;
import classScore.data.UserDefinedGeneSetManager;

/**
 * Step to choose a gene set to modify.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin Lee
 * @version $Id$
 */
public class GeneSetWizardStep1A extends WizardStep {

    private GeneAnnotations geneData = null;;
    private GONames goData = null;;
    private UserDefinedGeneSetManager newGeneSet = null;;
    private UserDefinedGeneSetManager oldGeneSet = null;;
    private JTable oldClassTable = null;;
    private JTextField searchTextField = null;;

    public GeneSetWizardStep1A( GeneSetWizard wiz, GeneAnnotations geneData, GONames goData,
            UserDefinedGeneSetManager newGeneSet, UserDefinedGeneSetManager oldGeneSet ) {
        super( wiz );
        this.jbInit();
        this.geneData = geneData;
        this.goData = goData;
        this.newGeneSet = newGeneSet;
        this.oldGeneSet = oldGeneSet;
        wiz.clearStatus();
        populateTables();
    }

    // Component initialization
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
                find( e );
            }
        } );

        searchPanel.add( searchTextField );

        step1MPanel.add( searchPanel, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Pick a gene set to modify.</b><br>"
                + "You will be asked to add or remove genes from this set in the next step." );
        this.addMain( step1MPanel );
    }

    public boolean isReady() {
        int n = oldClassTable.getSelectedRowCount();
        if ( n < 1 ) {
            showError( "You must pick a gene set to be modified." );
            return false;
        }
        int row = oldClassTable.getSelectedRow();
        String id = ( String ) oldClassTable.getValueAt( row, 0 );
        String desc = ( String ) oldClassTable.getValueAt( row, 1 );
        newGeneSet.setId( id );
        newGeneSet.setDesc( desc );
        newGeneSet.setModified( true );
        if ( geneData.geneSetExists( id ) ) {
            newGeneSet.getProbes().addAll( geneData.getClassToProbes( id ) );
        }
        oldGeneSet.setId( id );
        oldGeneSet.setDesc( desc );
        oldGeneSet.setModified( true );
        if ( geneData.geneSetExists( id ) ) {
            oldGeneSet.getProbes().addAll( geneData.getClassToProbes( id ) );
        }
        return true;

    }

    private void populateTables() {
        ModClassTableModel model = new ModClassTableModel( geneData, goData );
        TableSorter sorter = new TableSorter( model );
        oldClassTable.setModel( sorter );
        sorter.setTableHeader( oldClassTable.getTableHeader() );
        oldClassTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 30 );
        oldClassTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 30 );
        oldClassTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 30 );
        oldClassTable.revalidate();

        showStatus( "Available sets: " + geneData.selectedSets() );
    }

    public void find( ActionEvent e ) {
        String searchOn = searchTextField.getText();

        if ( searchOn.equals( "" ) ) {
            geneData.resetSelectedSets();
        } else {
            geneData.selectSets( searchOn, goData );
        }
        populateTables();
    }

}

class ModClassTableModel extends AbstractTableModel {
    GeneAnnotations geneData;
    GONames goData;
    Vector columnNames = new Vector();
    private NumberFormat nf = NumberFormat.getInstance();

    public ModClassTableModel( GeneAnnotations geneData, GONames goData ) {
        this.geneData = geneData;
        this.goData = goData;
        nf.setMaximumFractionDigits( 8 );
        columnNames.add( "Name" );
        columnNames.add( "Description" );
        columnNames.add( "# of Probes" );
        columnNames.add( "# of Genes" );
    }

    public String getColumnName( int i ) {
        return ( String ) columnNames.get( i );
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        return geneData.getSelectedSets().size();
    }

    public Object getValueAt( int i, int j ) {

        String classid = ( String ) geneData.getSelectedSets().get( i );

        switch ( j ) {
            case 0:
                return classid;
            case 1:
                return goData.getNameForId( classid );
            case 2:
                return new Integer( geneData.numProbesInGeneSet( classid ) );
            case 3:
                return new Integer( geneData.numGenesInGeneSet( classid ) );
            default:
                return "";
        }
    }
}

class GeneSetWizardStep1A_searchButton_actionAdapter implements ActionListener {

    GeneSetWizardStep1A adaptee;

    public GeneSetWizardStep1A_searchButton_actionAdapter( GeneSetWizardStep1A adaptee ) {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e ) {
        adaptee.find( e );

    }

}
