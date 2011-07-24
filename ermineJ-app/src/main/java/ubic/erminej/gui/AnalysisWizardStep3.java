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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;
import ubic.erminej.data.GeneSetTerm;

/**
 * Step to handle adding custom gene sets to the analysis.
 * 
 * @author pavlidis
 * @author Homin Lee
 * @version $Id$
 */
public class AnalysisWizardStep3 extends WizardStep {
    private static final long serialVersionUID = -7777686534611702796L;

    private AnalysisWizardStep3_CustomClassList customClasses;
    private AbstractTableModel ccTableModel;
    private JTable customClassTable;

    private JLabel countLabel;

    private final GeneSetScoreFrame callingframe;

    private GeneAnnotations geneAnnots = null;

    public AnalysisWizardStep3( AnalysisWizard wiz, GeneSetScoreFrame callingframe, GeneAnnotations geneAnnots ) {
        super( wiz );
        this.callingframe = callingframe;
        this.jbInit();
        this.geneAnnots = geneAnnots;
        wiz.clearStatus();
        makeLeftTable();
    }

    // Component initialization
    @Override
    protected void jbInit() {

        this.setLayout( new BorderLayout() );
        JPanel step3Panel;
        JPanel jPanel10 = new JPanel();

        step3Panel = new JPanel();
        step3Panel.setLayout( new BorderLayout() );

        countLabel = new JLabel();
        countLabel.setForeground( Color.black );
        countLabel.setPreferredSize( new Dimension( 500, 15 ) );
        countLabel.setText( "Number of Classes: 0" );

        customClassTable = new JTable();
        customClassTable.getTableHeader().setReorderingAllowed( false );
        customClassTable.setPreferredScrollableViewportSize( new Dimension( 500, 150 ) );
        customClassTable.setAutoCreateRowSorter( true );
        customClassTable.getTableHeader().addMouseListener( new MouseAdapter() {
            @Override
            public void mouseEntered( MouseEvent e ) {
                setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                setCursor( Cursor.getDefaultCursor() );
            }
        } );

        final JScrollPane customClassScrollPane;
        customClassScrollPane = new JScrollPane( customClassTable );
        // customClassScrollPane.setPreferredSize( new Dimension( 500, 250 ) );

        jPanel10.add( customClassScrollPane, null );
        step3Panel.add( jPanel10, BorderLayout.NORTH );
        step3Panel.add( countLabel, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Custom gene sets</b><br>" + "These are the added groups available for analysis. "
                + "If you have not defined any custom gene sets, the table will be blank. " );
        this.addMain( step3Panel );
    }

    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * Create the left-hand table for the wizard, which contains the set of user-defined gene sets available.
     */
    void makeLeftTable() {

        Set<GeneSetTerm> userDefinedGeneSets = geneAnnots.getUserDefined();

        if ( userDefinedGeneSets == null || userDefinedGeneSets.size() == 0 ) {
            log.debug( "Null or no user-defined gene sets" );
            return;
        }
        log.debug( userDefinedGeneSets.size() + " user-defined gene sets available" );

        customClasses = new AnalysisWizardStep3_CustomClassList(); // I really don't think we need this extra class.
        for ( GeneSetTerm id : userDefinedGeneSets ) {
            if ( callingframe.userOverWrote( id ) ) continue;
            GeneSet geneSet = this.geneAnnots.getGeneSet( id );
            customClasses.add( geneSet );
        }
        ccTableModel = customClasses.toTableModel();
        customClassTable.setModel( ccTableModel );
        customClassTable.setAutoCreateRowSorter( true );
        countLabel.setText( "Number of Classes: " + customClassTable.getRowCount() );

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

class AnalysisWizardStep3_CustomClassList extends ArrayList<GeneSet> {

    private static final long serialVersionUID = 6273798227862995265L;

    public AbstractTableModel toTableModel() {
        return new AbstractTableModel() {
            /**
             * 
             */
            private static final long serialVersionUID = -7948657716603757137L;
            private String[] columnNames = { "ID", "Description", "Members" };

            @Override
            public String getColumnName( int i ) {
                return columnNames[i];
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public int getRowCount() {
                int windowrows = 6;
                int extra = 1;
                if ( size() < windowrows ) {
                    extra = windowrows - size();
                }
                return size() + extra;
            }

            public Object getValueAt( int i, int j ) {
                if ( i < size() ) {
                    GeneSet cinfo = get( i );
                    switch ( j ) {
                        case 0:
                            return cinfo.getId();
                        case 1:
                            return cinfo.getTerm().getName();
                        case 2: {
                            Collection<Gene> members = cinfo.getGenes();
                            return ( Integer.toString( members.size() ) + " " + ( cinfo.isGenes() ? "genes" : "probes" ) );
                        }
                        default:
                            return null;
                    }
                }
                return null;

            }
        };
    }
}
