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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ubic.erminej.data.UserDefinedGeneSetManager.GeneSetFileFormat;

/**
 * A group of genes, such as those defined by GO annotations or KEGG pathways.
 * 
 * @author paul
 * @version $Id$
 */
public class GeneSet {
    private GeneSetTerm term;

    private Set<Gene> genes = new HashSet<Gene>();

    /**
     * Gene sets that have the exact same members.
     */
    private Set<GeneSet> redundantGroups = new HashSet<GeneSet>();

    // How was it originally represented. This affects how things are stored on disk. Genes is better.
    private boolean isGenes = true;

    private String sourceFile;

    private Set<Element> elements = new HashSet<Element>();

    private GeneSetFileFormat format = GeneSetFileFormat.DEFAULT;

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
        this.isGenes = true;
        addGenes( items );
    }

    /**
     * Add the gene (and its elements)
     * 
     * @param g
     */
    public void addGene( Gene g ) {
        assert g != null;

        this.genes.add( g );

        // make sure the gene and probe have the term referenced.... this is just in case!
        assert this.term != null;
        g.addGeneSet( this.term );
        for ( Element p : g.getProbes() ) {
            p.addToGeneSet( this.term );
            this.elements.add( p );
        }

        if ( !redundantGroups.isEmpty() ) {
            // this would be a programming error.
            throw new IllegalStateException( "Illegal attempt to add gene (" + g.getSymbol() + ") to a set ("
                    + this.getId() + ") that already has redundancy computed: has " + redundantGroups.size()
                    + " redundancies." );
        }
    }

    public void addGenes( Collection<Gene> gs ) {
        for ( Gene g : gs ) {
            this.addGene( g );
        }
    }

    public void addRedundantGroup( GeneSet redundant ) {
        if ( this.equals( redundant ) ) {
            return;
        }
        this.redundantGroups.add( redundant );
    }

    public void clearGenes() {
        genes = new HashSet<Gene>();
        elements = new HashSet<Element>();
        clearRedundancy();
    }

    public void clearRedundancy() {
        this.redundantGroups.clear();
    }

    public void clearRedundancy( GeneSet toclear ) {
        this.redundantGroups.remove( toclear );
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
     * @return the file format that this group came in (only applies for User-defined groups)
     */
    public GeneSetFileFormat getFormat() {
        return format;
    }

    /**
     * Do not modify this collection directly! Use 'addGene'.
     * 
     * @return
     */
    public Set<Gene> getGenes() {
        return Collections.unmodifiableSet( genes );
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
     * FIXME this should return only the *active* Elements? - those which have Scores.
     * 
     * @return
     */
    public Set<Element> getProbes() {
        if ( this.elements.isEmpty() ) {
            for ( Gene g : genes ) {
                elements.addAll( g.getProbes() );
            }
        }
        return Collections.unmodifiableSet( this.elements );
    }

    /**
     * Can not modify this collection directly; use addRedundantGroup instead.
     * 
     * @return
     */
    public Set<GeneSet> getRedundantGroups() {
        return Collections.unmodifiableSet( redundantGroups );
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

    public boolean hasRedundancy() {
        return !redundantGroups.isEmpty();
    }

    public boolean isGenes() {
        return isGenes;
    }

    public boolean isUserDefined() {
        return term.isUserDefined();
    }

    public void setFormat( GeneSetFileFormat f ) {
        this.format = f;

    }

    public void setGenes( boolean isGenes ) {
        this.isGenes = isGenes;
    }

    public void setGenes( Collection<Gene> genes ) {
        if ( genes == null ) {
            throw new IllegalArgumentException( "Genes was null" );
        }
        this.genes.clear();
        for ( Gene gene : genes ) {
            this.addGene( gene );
        }
    }

    public void setIsGenes( boolean isGenes ) {
        this.isGenes = isGenes;
    }

    public void setRedundantGroups( Set<GeneSet> redundantGroups ) {
        this.redundantGroups = redundantGroups;
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
        if ( term == null ) return;
        this.term.setUserDefined( isUserDefined );
    }

    /**
     * @return how many genes there are.
     */
    public int size() {
        return genes.size();
    }

    @Override
    public String toString() {
        return term.getId() + " - " + StringUtils.abbreviate( term.getName(), 40 ) + " (" + this.getGenes().size()
                + " genes)";
    }
}
