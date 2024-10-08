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
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import ubic.erminej.gui.util.WizardStep;

/**
 * Get ID and description.
 *
 * @author Homin K Lee
 * @version $Id$
 */
public class GeneSetWizardStep3 extends WizardStep {

    private static final long serialVersionUID = -7269554461600183571L;
    private JTextField classIDTF = null;
    private JTextArea classDescTA = null;

    /**
     * <p>
     * Constructor for GeneSetWizardStep3.
     * </p>
     *
     * @param wiz a {@link ubic.erminej.gui.geneset.edit.GeneSetWizard} object.
     */
    public GeneSetWizardStep3( GeneSetWizard wiz ) {
        super( wiz );
        this.jbInit();
        wiz.clearStatus();
    }

    /**
     * <p>
     * getGeneSetId.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGeneSetId() {
        return classIDTF.getText();
    }

    /**
     * <p>
     * getGeneSetName.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGeneSetName() {
        return classDescTA.getText();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReady() {
        return StringUtils.isNotBlank( classIDTF.getText() );
    }

    /**
     * <p>
     * setDescText.
     * </p>
     *
     * @param t a {@link java.lang.String} object.
     */
    public void setDescText( String t ) {
        classDescTA.setText( t );
    }

    /**
     * <p>
     * setIdFieldEnabled.
     * </p>
     *
     * @param b a boolean.
     */
    public void setIdFieldEnabled( boolean b ) {
        this.classIDTF.setEnabled( b );
        this.classIDTF.setEditable( b );
        this.classDescTA.setEditable( b );
        this.classDescTA.setEnabled( b );
    }

    /**
     * <p>
     * setIdText.
     * </p>
     *
     * @param t a {@link java.lang.String} object.
     */
    public void setIdText( String t ) {
        classIDTF.setText( t );
    }

    // Component initialization
    /** {@inheritDoc} */
    @Override
    protected void jbInit() {
        this.setLayout( new BorderLayout() );
        JPanel step3Panel = new JPanel();
        step3Panel.setLayout( new BorderLayout() );

        JPanel idPanel = new JPanel();
        idPanel.setPreferredSize( new Dimension( 500, 51 ) );

        JLabel classIDL = new JLabel( "Gene Set name: " );
        classIDTF = new JTextField( 20 );
        classIDTF.setMinimumSize( new Dimension( 240, 19 ) );

        idPanel.add( classIDL );
        idPanel.add( classIDTF );

        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setPreferredSize( new Dimension( 500, 180 ) );
        JLabel classDescL = new JLabel( "Description" );

        classDescTA = new JTextArea();
        classDescTA.setLineWrap( true );
        classDescTA.setBorder( BorderFactory.createLoweredBevelBorder() );
        classDescTA.setPreferredSize( new Dimension( 500, 180 ) );
        Font oldFont = classDescTA.getFont();
        Font newFont = new Font( oldFont.getFontName(), oldFont.getStyle(), 11 /* points */ );
        classDescTA.setFont( newFont );

        descriptionPanel.add( classDescL );
        descriptionPanel.add( classDescTA );

        step3Panel.add( idPanel, BorderLayout.NORTH );
        step3Panel.add( descriptionPanel, BorderLayout.CENTER );

        this.addHelp( "<html><b>Choose or edit the gene set identifier and optional description.</b><br>"
                + "The custom gene set will automatically be saved on your system"
                + " to be used again in future analyses." );
        this.addMain( step3Panel );
    }

}
