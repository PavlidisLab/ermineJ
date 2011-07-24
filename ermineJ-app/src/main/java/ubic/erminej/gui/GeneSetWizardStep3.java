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
import java.awt.Font;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import ubic.erminej.data.GeneSet;
import ubic.erminej.data.Probe;

/**
 * @author Homin K Lee
 * @version $Id$
 */
public class GeneSetWizardStep3 extends WizardStep {

    private static final long serialVersionUID = -7269554461600183571L;
    private GeneSet newGeneSet = null;
    private JTextField classIDTF = null;
    private JTextArea classDescTA = null;

    public GeneSetWizardStep3( GeneSetWizard wiz ) {
        super( wiz );
        newGeneSet = wiz.getNewGeneSet();
        this.jbInit();

        wiz.clearStatus();
    }

    // Component initialization
    @Override
    protected void jbInit() {
        this.setLayout( new BorderLayout() );
        JPanel step3Panel = new JPanel();
        step3Panel.setLayout( new BorderLayout() );

        JPanel idPanel = new JPanel();
        idPanel.setPreferredSize( new Dimension( 500, 51 ) );

        JLabel classIDL = new JLabel( "Gene Set name: " );
        classIDTF = new JTextField();
        classIDTF.setPreferredSize( new Dimension( 240, 19 ) );
        classIDTF.setBorder( BorderFactory.createLoweredBevelBorder() );

        idPanel.add( classIDL );
        idPanel.add( classIDTF );

        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setPreferredSize( new Dimension( 500, 180 ) );
        JLabel classDescL = new JLabel( "Description: " );
        classDescL.setRequestFocusEnabled( true );

        classDescTA = new JTextArea();
        classDescTA.setLineWrap( true );
        classDescTA.setBorder( BorderFactory.createLoweredBevelBorder() );
        classDescTA.setPreferredSize( new Dimension( 500, 180 ) );
        Font oldFont = classDescTA.getFont();
        Font newFont = new Font( oldFont.getFontName(), oldFont.getStyle(), 11 /* points */);
        classDescTA.setFont( newFont );

        descriptionPanel.add( classDescL );
        descriptionPanel.add( classDescTA );

        step3Panel.add( idPanel, BorderLayout.NORTH );
        step3Panel.add( descriptionPanel, BorderLayout.SOUTH );

        this.addHelp( "<html><b>Choose or edit the gene set identifier and description.</b><br>"
                + "The custom gene set will automatically be saved on your system"
                + " to be used again in future analyses." );
        this.addMain( step3Panel );
    }

    @Override
    public boolean isReady() {

        /*
         * If this is a user-defined group they are modifying, let them do what they want. Otherwise, if it's a GO
         * group, they have to change the id.
         */

        return StringUtils.isNotBlank( classIDTF.getText() );
    }

    public String getGeneSetId() {
        return classIDTF.getText();
    }

    public String getGeneSetName() {
        return classDescTA.getText();
    }

    /**
     * 
     */
    public void update( Collection<Probe> probesToUse ) {

        newGeneSet = ( ( GeneSetWizard ) owner ).getNewGeneSet();
        if ( newGeneSet != null ) {
            classIDTF.setText( newGeneSet.getId() );
            classDescTA.setText( newGeneSet.getName() );
        }

    }

    /**
     * @param b
     */
    public void setIdFieldEnabled( boolean b ) {
        this.classIDTF.setEnabled( b );
        this.classIDTF.setEditable( b );
        this.classDescTA.setEditable( b );
        this.classDescTA.setEnabled( b );
    }

}