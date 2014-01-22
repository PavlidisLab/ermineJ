/*
 * The baseCode project
 * 
 * Copyright (c) 2011 University of British Columbia
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

package ubic.erminej.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Represents an assayable item such as a microarray probe (or probeset) that can refer to more than one gene.
 * 
 * @author paul
 * @version $Id$
 */
public class Element {

    private String name;

    private String description;

    private Collection<Gene> genes = new HashSet<Gene>();

    private Collection<GeneSetTerm> geneSets = new HashSet<GeneSetTerm>();

    public Element( String elementId ) {
        this( elementId, null );
    }

    public Element( String name, String description ) {
        this( name, description, null );
    }

    public Element( String name, String description, Collection<Gene> genes ) {
        super();
        this.name = name;
        this.description = description;
        if ( genes != null ) {
            this.genes = genes;
        }
    }

    /**
     * @param geneSet
     */
    public void addToGeneSet( GeneSetTerm geneSet ) {
        this.geneSets.add( geneSet );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Element other = ( Element ) obj;
        if ( name == null ) {
            if ( other.name != null ) return false;
        } else if ( !name.equals( other.name ) ) return false;
        return true;
    }

    public String getDescription() {
        return description;
    }

    public Gene getGene() {
        if ( genes.isEmpty() ) return null;
        return genes.iterator().next();
    }

    public Collection<Gene> getGenes() {
        return Collections.unmodifiableCollection( genes );
    }

    public Collection<GeneSetTerm> getGeneSets() {
        return Collections.unmodifiableCollection( geneSets );
    }

    public String getName() {
        return name;
    }

    public boolean hasAnnots() {
        return !this.geneSets.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        return result;
    }

    /**
     * Assumes we only have one gene per probe.
     * 
     * @param g
     */
    public void setGene( Gene g ) {
        this.genes.clear();
        this.genes.add( g );
    }

    public void setGenes( Collection<Gene> genes ) {
        this.genes = genes;
    }

    public void setGeneSets( Collection<GeneSetTerm> geneSets ) {
        this.geneSets = geneSets;
    }

    @Override
    public String toString() {
        return "Element [name=" + name + ", " + getGene() + "]";
    }

    protected void addGene( Gene g ) {
        // a little bit dangerous
        this.genes.add( g );
    }

    protected void removeGeneSet( GeneSetTerm t ) {
        this.geneSets.remove( t );
    }

}
