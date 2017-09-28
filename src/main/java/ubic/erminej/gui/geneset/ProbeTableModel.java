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

import ubic.erminej.data.Element;
import ubic.erminej.data.Gene;
import ubic.erminej.data.GeneAnnotations;

/**
 * Simple tabular representation of elements.
 *
 * @author paul
 * @version $Id$
 */
public class ProbeTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -1L;
    private String[] columnNames = { "Element", "Gene", "Description" };
    private List<Element> pl;

    /**
     * <p>
     * Constructor for ProbeTableModel.
     * </p>
     *
     * @param probesToUse a {@link java.util.Collection} object.
     */
    public ProbeTableModel( Collection<Element> probesToUse ) {
        this.setProbes( probesToUse );
        fireTableDataChanged();
    }

    /**
     * <p>
     * Constructor for ProbeTableModel.
     * </p>
     *
     * @param geneData a {@link ubic.erminej.data.GeneAnnotations} object.
     */
    public ProbeTableModel( GeneAnnotations geneData ) {
        pl = new ArrayList<>( geneData.getProbes() );
        fireTableDataChanged();
    }

    /**
     * <p>
     * addProbe.
     * </p>
     *
     * @param p a {@link ubic.erminej.data.Element} object.
     */
    public void addProbe( Element p ) {
        if ( !pl.contains( p ) ) pl.add( p );
        fireTableDataChanged();
    }

    /**
     * <p>
     * addProbes.
     * </p>
     *
     * @param probelist a {@link java.util.Collection} object.
     */
    public void addProbes( Collection<Element> probelist ) {
        for ( Element probe : probelist ) {
            if ( !pl.contains( probe ) ) pl.add( probe );
        }
        fireTableDataChanged();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName( int i ) {
        return columnNames[i];
    }

    /**
     * <p>
     * getGeneCount.
     * </p>
     *
     * @return a int.
     */
    public int getGeneCount() {
        Set<Gene> g = new HashSet<>();
        for ( Element p : pl ) {
            g.addAll( p.getGenes() );
        }
        return g.size();
    }

    /**
     * <p>
     * getProbeCount.
     * </p>
     *
     * @return a int.
     */
    public int getProbeCount() {
        return pl.size();
    }

    /**
     * <p>
     * getProbes.
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Element> getProbes() {
        return pl;
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return pl.size();
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt( int i, int j ) {

        Element elementId = pl.get( i );
        switch ( j ) {
            case 0:
                return elementId.getName();
            case 1:
                return elementId.getGene().getSymbol();
            case 2:
                return elementId.getGene().getName();
            default:
                return null;
        }
    }

    /**
     * <p>
     * removeProbe.
     * </p>
     *
     * @param p a {@link ubic.erminej.data.Element} object.
     */
    public void removeProbe( Element p ) {
        if ( pl.contains( p ) ) pl.remove( p );
        fireTableDataChanged();
    }

    /**
     * <p>
     * removeProbes.
     * </p>
     *
     * @param probelist a {@link java.util.Collection} object.
     */
    public void removeProbes( Collection<Element> probelist ) {
        for ( Element probe : probelist ) {
            if ( pl.contains( probe ) ) pl.remove( probe );
        }
        fireTableDataChanged();
    }

    /**
     * <p>
     * setProbes.
     * </p>
     *
     * @param probesToUse a {@link java.util.Collection} object.
     */
    public void setProbes( Collection<Element> probesToUse ) {
        pl = new ArrayList<>( probesToUse );
        this.fireTableDataChanged();
    }
}
