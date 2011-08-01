/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.gui.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

import ubic.basecode.dataStructure.matrix.StringMatrix;
import ubic.basecode.io.reader.StringMatrixReader;

/**
 * Uses a dialog box (FIXME: this could just extend JDialog)
 * 
 * @author paul
 * @version $Id$
 */
public class MatrixPreviewer {

    /**
     * @param table
     * @param columnNames
     */
    public static void previewMatrix( Window parent, List<Object[]> table, Object[] columnNames ) {
        Object[][] tab = new Object[table.size()][];
        for ( int i = 0; i < tab.length; i++ ) {
            tab[i] = table.get( i );
        }

        previewMatrix( parent, tab, columnNames );
    }

    /**
     * @param parent
     * @param tab
     * @param columnNames
     */
    private static void previewMatrix( Window parent, Object[][] tab, Object[] columnNames ) {
        TableModel tableModel = new DefaultTableModel( tab, columnNames );

        final JDialog previewPanel = new JDialog( parent );
        Container content = previewPanel.getContentPane();
        previewPanel.setTitle( "Preview of file" );
        previewPanel.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        previewPanel.setFocusable( true );

        JTable tableView = new JTable( tableModel );
        JScrollPane scrollPane = new JScrollPane( tableView );
        tableView.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        content.add( scrollPane, BorderLayout.CENTER );

        JButton closeButton = new JButton( "Close" );

        closeButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                previewPanel.dispose();
            }
        } );

        JPanel p2 = new JPanel();

        p2.add( closeButton );
        content.add( p2, BorderLayout.SOUTH );

        previewPanel.pack();
        previewPanel.setSize( 400, 300 );
        previewPanel.setModal( true );

        GuiUtil.centerContainer( previewPanel );
        previewPanel.setVisible( true );
        previewPanel.requestFocus();
        previewPanel.requestFocusInWindow();
    }

    public static void previewMatrix( Window x, String fileName, Integer startCol ) throws IOException {
        StringMatrixReader r = new StringMatrixReader();
        StringMatrix<String, String> test;

        test = r.read( fileName, 100, startCol );
        previewMatrix( x, test );

    }

    /**
     * @param w
     * @param matrix
     */
    public static void previewMatrix( Window w, StringMatrix<String, String> matrix ) {

        Object[][] mat = new Object[matrix.rows()][matrix.columns()];
        Object[] headings = new Object[matrix.columns()];

        for ( int i = 0; i < matrix.rows(); i++ ) {
            for ( int j = 0; j < Math.min( 20 /* MAX COLUMNS */, matrix.columns() ); j++ ) {
                if ( i == 0 ) {
                    headings[j] = ( j + 1 ) + ": " + StringUtils.abbreviate( matrix.getColName( j ), 75 );
                }
                mat[i][j] = matrix.get( i, j );
            }
        }

        previewMatrix( w, mat, headings );

    }
}