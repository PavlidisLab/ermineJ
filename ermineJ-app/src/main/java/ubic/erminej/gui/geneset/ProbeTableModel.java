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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.Probe;

/**
 * Simple tabular representation of probes.
 * 
 * @author paul
 * @version $Id$
 */
public class ProbeTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -1L;
    private String[] columnNames = { "Probe", "Gene", "Description" };
    List<Probe> pl;

    public ProbeTableModel( GeneAnnotations geneData ) {
        pl = new ArrayList<Probe>( geneData.getProbes() );
    }

    public void setProbes( Collection<Probe> probesToUse ) {
        pl = new ArrayList<Probe>( probesToUse );
        this.fireTableDataChanged();
    }

    public ProbeTableModel( Collection<Probe> probesToUse ) {
        this.setProbes( probesToUse );
    }

    @Override
    public String getColumnName( int i ) {
        return columnNames[i];
    }

    public int getColumnCount() {
        return 3;
    }

    public int getRowCount() {
        return pl.size();
    }

    public Object getValueAt( int i, int j ) {

        Probe probeid = pl.get( i );
        switch ( j ) {
            case 0:
                return probeid.getName();
            case 1:
                return probeid.getGene().getSymbol();
            case 2:
                return probeid.getGene().getName();
            default:
                return null;
        }
    }
}