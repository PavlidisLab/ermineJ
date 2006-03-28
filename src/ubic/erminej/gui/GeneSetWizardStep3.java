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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import ubic.basecode.gui.WizardStep;
import ubic.erminej.data.UserDefinedGeneSetManager;

/**
 * @author Homin K Lee
 * @version $Id$
 */
public class GeneSetWizardStep3 extends WizardStep {

    private UserDefinedGeneSetManager newGeneSet = null;
    private JLabel classIDFinal = null;
    private JTextField classIDTF = null;
    private JTextArea classDescTA = null;
    private JTable finalTable = null;

    String origID = null;
    String origDesc = null;
    private final boolean makenew;

    public GeneSetWizardStep3( GeneSetWizard wiz, UserDefinedGeneSetManager newGeneSet, boolean makenew ) {
        super( wiz );
        this.makenew = makenew;
        this.newGeneSet = newGeneSet;
        this.jbInit();
        AbstractTableModel finalTableModel = newGeneSet.toTableModel( true );
        assert finalTableModel != null;
        assert finalTable != null;
        finalTable.setModel( finalTableModel );
        wiz.clearStatus();
    }

    // Component initialization
    protected void jbInit() {
        this.setLayout( new BorderLayout() );
        JPanel step3Panel = new JPanel();
        step3Panel.setLayout( new BorderLayout() );

        JPanel ncIDPanel = new JPanel();
        ncIDPanel.setPreferredSize( new Dimension( 128, 51 ) );
        JLabel classIDL = new JLabel( "New Gene Set ID: " );

        // classIDTF.addKeyListener( new classIDlistener( this ) );
        classIDTF = new JTextField();
        classIDTF.setPreferredSize( new Dimension( 120, 19 ) );
        classIDTF.setBorder( BorderFactory.createLoweredBevelBorder() );
        classIDTF.setToolTipText( "New Gene Set ID" );

        ncIDPanel.add( classIDL );
        ncIDPanel.add( classIDTF );

        JPanel ncInfo1Panel = new JPanel();
        ncInfo1Panel.setPreferredSize( new Dimension( 165, 240 ) );
        JPanel ncDescPanel = new JPanel();
        ncDescPanel.setPreferredSize( new Dimension( 165, 180 ) );
        JLabel classDescL = new JLabel( "New gene set ID: " );

        classDescL.setRequestFocusEnabled( true );
        classDescL.setText( "New gene set Description: " );
        classDescTA = new JTextArea();
        if ( !makenew ) {
            classDescTA.setText( newGeneSet.getId() );
        }
        classDescTA.setToolTipText( "New gene set description" );

        if ( makenew ) {
            classDescTA.setText( "Enter description" );
        } else {
            classDescTA.setText( newGeneSet.getDesc() );
        }
        classDescTA.setLineWrap( true );
        JScrollPane classDTAScroll = new JScrollPane( classDescTA );
        classDTAScroll.setBorder( BorderFactory.createLoweredBevelBorder() );
        classDTAScroll.setPreferredSize( new Dimension( 160, 140 ) );
        ncDescPanel.add( classDescL );
        ncDescPanel.add( classDTAScroll, null );
        ncInfo1Panel.add( ncIDPanel, null );
        ncInfo1Panel.add( ncDescPanel, null );

        JPanel ncInfo2Panel = new JPanel();
        ncInfo2Panel.setLayout( new BorderLayout() );
        ncInfo2Panel.setPreferredSize( new Dimension( 220, 240 ) );
        classIDFinal = new JLabel( "New Gene set ID: " );
        classIDFinal.setText( "No Gene set Name" );
        classIDFinal.setRequestFocusEnabled( true );

        finalTable = new JTable();
        finalTable.getTableHeader().setReorderingAllowed( false );
        JScrollPane finalScrollPane = new JScrollPane( finalTable );
        finalScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
        ncInfo2Panel.add( classIDFinal, BorderLayout.NORTH );
        ncInfo2Panel.add( finalScrollPane, BorderLayout.CENTER );

        step3Panel.add( ncInfo1Panel, BorderLayout.WEST );
        step3Panel.add( ncInfo2Panel, BorderLayout.CENTER );

        this.addHelp( "<html><b>Choose a new gene set identifier and description.</b><br>"
                + "The custom gene set will automatically be saved to your hard drive"
                + " to be used again in future analyses." );
        this.addMain( step3Panel );
    }

    public boolean isReady() {
        return true;
    }

    public void nameNewGeneSet() {
        assert newGeneSet != null;
        newGeneSet.setId( classIDTF.getText() );
        newGeneSet.setDesc( classDescTA.getText() );

        // assert origID != null;
        //
        // if ( !newGeneSet.modified() && origID.equals( newGeneSet.getId() ) ) {
        // newGeneSet.setModified( false );
        // }
    }

    public void update() {
        assert newGeneSet != null;
        classIDTF.setText( newGeneSet.getId() );
        classDescTA.setText( newGeneSet.getDesc() );
        if ( newGeneSet.getId().compareTo( "" ) != 0 ) {
            classIDFinal.setText( newGeneSet.getId() );
        }
        assert newGeneSet.getId() != null;
        if ( newGeneSet.isModified() ) origID = newGeneSet.getId();
    }

    public void setIdFieldEnabled( boolean b ) {
        this.classIDTF.setEnabled( b );
        this.classIDTF.setEditable( b );
        this.classDescTA.setEditable( b );
        this.classDescTA.setEnabled( b );
    }

}