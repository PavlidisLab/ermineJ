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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import ubic.erminej.data.Gene;
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
    private String[] columnNames = { "Element", "Gene", "Description" };
    private List<Probe> pl;

    public ProbeTableModel( Collection<Probe> probesToUse ) {
        this.setProbes( probesToUse );
        fireTableDataChanged();
    }

    public ProbeTableModel( GeneAnnotations geneData ) {
        pl = new ArrayList<Probe>( geneData.getProbes() );
        fireTableDataChanged();
    }

    public int getProbeCount() {
        return pl.size();
    }

    public int getGeneCount() {
        Set<Gene> g = new HashSet<Gene>();
        for ( Probe p : pl ) {
            g.addAll( p.getGenes() );
        }
        return g.size();
    }

    public void addProbes( Collection<Probe> probelist ) {
        for ( Probe probe : probelist ) {
            if ( !pl.contains( probe ) ) pl.add( probe );
        }
        fireTableDataChanged();
    }

    public void removeProbes( Collection<Probe> probelist ) {
        for ( Probe probe : probelist ) {
            if ( pl.contains( probe ) ) pl.remove( probe );
        }
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName( int i ) {
        return columnNames[i];
    }

    public List<Probe> getProbes() {
        return pl;
    }

    @Override
    public int getRowCount() {
        return pl.size();
    }

    @Override
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

    public void setProbes( Collection<Probe> probesToUse ) {
        pl = new ArrayList<Probe>( probesToUse );
        this.fireTableDataChanged();
    }

    public void addProbe( Probe p ) {
        if ( !pl.contains( p ) ) pl.add( p );
        fireTableDataChanged();
    }

    public void removeProbe( Probe p ) {
        if ( pl.contains( p ) ) pl.remove( p );
        fireTableDataChanged();
    }
}