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
package ubic.erminej.gui.geneset;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSet;

/**
 * Simpleified table model for listing gene sets (e.g. in a picker situation)
 * 
 * @author paul
 * @version $Id$
 */
public class SimpleGeneSetListTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private List<String> columnNames = new Vector<String>();
    private NumberFormat nf = NumberFormat.getInstance();
    private List<GeneSet> geneSets = new ArrayList<GeneSet>();

    /**
     * Offer up only the given gene sets
     * 
     * @param geneDat
     * @param geneSets
     */
    public SimpleGeneSetListTableModel( Collection<GeneSet> geneSets ) {
        nf.setMaximumFractionDigits( 3 );
        columnNames.add( "Name" );
        columnNames.add( "Description" );
        columnNames.add( "# of Probes" );
        columnNames.add( "# of Genes" );
        this.geneSets.addAll( geneSets );
    }

    /**
     * Offer up all gene sets.
     * 
     * @param geneDat
     */
    public SimpleGeneSetListTableModel( GeneAnnotations geneDat ) {
        this.geneSets.addAll( geneDat.getAllGeneSets() );
        nf.setMaximumFractionDigits( 3 );
        columnNames.add( "Name" );
        columnNames.add( "Description" );
        columnNames.add( "# of Probes" );
        columnNames.add( "# of Genes" );
    }

    @Override
    public String getColumnName( int i ) {
        return columnNames.get( i );
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        return this.geneSets.size();
    }

    public Object getValueAt( int i, int j ) {

        GeneSet geneSet = geneSets.get( i );
        switch ( j ) {
            case 0:
                return geneSet.getId();
            case 1:
                return geneSet.getName();
            case 2:
                return new Integer( geneSet.getProbes().size() );
            case 3:
                return new Integer( geneSet.getGenes().size() );
            default:
                return "";
        }
    }

}
