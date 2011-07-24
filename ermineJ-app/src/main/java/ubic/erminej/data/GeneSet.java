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
package ubic.erminej.data;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

/**
 * @author paul
 * @version $Id$
 */
public class GeneSet {
    private GeneSetTerm term;

    private boolean modified = false;

    private Collection<Gene> genes = new HashSet<Gene>();

    /**
     * Gene sets that have the exact same members.
     */
    private Collection<GeneSet> redundantGroups = new HashSet<GeneSet>();

    // How was it originally represented. This affects how things are stored on disk. Genes is better.
    private boolean isGenes = true;

    private boolean skipDueToRedundancy = false;

    private boolean isUserDefined = false;

    private String sourceFile;

    private Collection<Probe> probes = new HashSet<Probe>();

    public GeneSet() {
    }

    /**
     * @param name
     */
    public GeneSet( GeneSetTerm name ) {
        this.term = name;
    }

    /**
     * @param name
     * @param items
     */
    public GeneSet( GeneSetTerm name, Collection<Gene> items ) {
        this( name );
        for ( Gene g : items )
            addGene( g );
    }

    /**
     * Add the gene (and its probes)
     * 
     * @param g
     */
    public void addGene( Gene g ) {
        assert g != null;

        this.genes.add( g );

        // make sure the gene and probe have the term referenced.... this is just in case!
        assert this.term != null;
        g.addGeneSet( this.term );
        for ( Probe p : g.getProbes() ) {
            p.addToGeneSet( this.term );
            this.probes.add( p );
        }

        assert redundantGroups.isEmpty() :
        // FIXME: the redundantGroups should be invalidated/checked?
        "Do not add genes to sets after redundantGroups has been populated!";
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        GeneSet other = ( GeneSet ) obj;
        if ( term == null ) {
            if ( other.term != null ) return false;
        } else if ( !term.equals( other.term ) ) return false;
        return true;
    }

    /**
     * Do not modify this collection directly! Use 'addGene'.
     * 
     * @return
     */
    public Collection<Gene> getGenes() {
        return genes;
    }

    /**
     * @return
     */
    public String getId() {
        return term.getId();
    }

    /**
     * @return
     */
    public String getName() {
        return term.getName();
    }

    /**
     * @return
     */
    public Collection<Probe> getProbes() {
        if ( this.probes.isEmpty() ) {
            for ( Gene g : genes ) {
                probes.addAll( g.getProbes() );
            }
        }
        return this.probes;
    }

    /**
     * @return
     */
    public Collection<GeneSet> getRedundantGroups() {
        return redundantGroups;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * @return Returns the name.
     */
    public GeneSetTerm getTerm() {
        return term;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( term == null ) ? 0 : term.hashCode() );
        return result;
    }

    public boolean isGenes() {
        return isGenes;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isSkipDueToRedundancy() {
        return skipDueToRedundancy;
    }

    public boolean isUserDefined() {
        return isUserDefined;
    }

    public void setGenes( boolean isGenes ) {
        this.isGenes = isGenes;
    }

    public void setGenes( Collection<Gene> genes ) {
        this.genes.clear();
        for ( Gene gene : genes ) {
            this.addGene( gene );
        }
    }

    public void setIsGenes( boolean isGenes ) {
        this.isGenes = isGenes;
    }

    public void setModified( boolean modified ) {
        this.modified = modified;
    }

    public void setRedundantGroups( Collection<GeneSet> redundantGroups ) {
        this.redundantGroups = redundantGroups;
    }

    public void setSkipDueToRedundancy( boolean skipDueToRedundancy ) {
        this.skipDueToRedundancy = skipDueToRedundancy;
    }

    public void setSourceFile( String fileName ) {
        this.sourceFile = fileName;
    }

    /**
     * @param name The name to set.
     */
    public void setTerm( GeneSetTerm name ) {
        this.term = name;
    }

    public void setUserDefined( boolean isUserDefined ) {
        this.isUserDefined = isUserDefined;
        this.term.setUserDefined( isUserDefined );
    }

    public int size() {
        return genes.size();
    }

    @Override
    public String toString() {
        return term.getId() + " - " + StringUtils.abbreviate( term.getName(), 40 ) + " (" + this.getGenes().size()
                + " genes)";
    }

    // /**
    // * Used while viewing/editing.
    // *
    // * @param editable
    // * @return
    // */
    // public ProbeTableModel toTableModel( boolean editable ) {
    // final boolean finalized = editable;
    //
    // final List<Probe> gpL = new ArrayList<Probe>( getProbes() );
    //
    // return new ProbeTableModel( gpL ) {
    //
    // private static final long serialVersionUID = -1L;
    // private String[] columnNames = { "Probe", "Gene", "Description" };
    //
    // @Override
    // public String getColumnName( int i ) {
    // return columnNames[i];
    // }
    //
    // @Override
    // public int getRowCount() {
    // int windowrows;
    // if ( finalized ) {
    // windowrows = 16;
    // } else {
    // windowrows = 13; // why? so it fits?
    // }
    // int extra = 1;
    // if ( getProbes().size() < windowrows ) {
    // extra = windowrows - getProbes().size();
    // }
    // return getProbes().size() + extra;
    // }
    //
    // @Override
    // public boolean isCellEditable( int r, int c ) {
    // if ( !finalized && ( c == 0 || c == 1 ) ) {
    // return true;
    // }
    // return false;
    // }
    // };
    // }

    public void addGenes( Collection<Gene> gs ) {
        for ( Gene g : gs ) {
            this.addGene( g );
        }
    }

    public void clearGenes() {
        genes = new HashSet<Gene>();
        probes = new HashSet<Probe>();
        assert this.redundantGroups.isEmpty(); // for now...
    }
}
