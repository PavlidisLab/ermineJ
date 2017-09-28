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

    private Collection<Gene> genes = new HashSet<>();

    private Collection<GeneSetTerm> geneSets = new HashSet<>();

    /**
     * <p>
     * Constructor for Element.
     * </p>
     *
     * @param elementId a {@link java.lang.String} object.
     */
    public Element( String elementId ) {
        this( elementId, null );
    }

    /**
     * <p>
     * Constructor for Element.
     * </p>
     *
     * @param name a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     */
    public Element( String name, String description ) {
        this( name, description, null );
    }

    /**
     * <p>
     * Constructor for Element.
     * </p>
     *
     * @param name a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     * @param genes a {@link java.util.Collection} object.
     */
    public Element( String name, String description, Collection<Gene> genes ) {
        super();
        this.name = name;
        this.description = description;
        if ( genes != null ) {
            this.genes = genes;
        }
    }

    /**
     * <p>
     * addToGeneSet.
     * </p>
     *
     * @param geneSet a {@link ubic.erminej.data.GeneSetTerm} object.
     */
    public void addToGeneSet( GeneSetTerm geneSet ) {
        this.geneSets.add( geneSet );
    }

    /** {@inheritDoc} */
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

    /**
     * <p>
     * Getter for the field <code>description</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * getGene.
     * </p>
     *
     * @return a {@link ubic.erminej.data.Gene} object.
     */
    public Gene getGene() {
        if ( genes.isEmpty() ) return null;
        return genes.iterator().next();
    }

    /**
     * <p>
     * Getter for the field <code>genes</code>.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Gene> getGenes() {
        return Collections.unmodifiableCollection( genes );
    }

    /**
     * <p>
     * Getter for the field <code>geneSets</code>.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<GeneSetTerm> getGeneSets() {
        return Collections.unmodifiableCollection( geneSets );
    }

    /**
     * <p>
     * Getter for the field <code>name</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * hasAnnots.
     * </p>
     *
     * @return a boolean.
     */
    public boolean hasAnnots() {
        return !this.geneSets.isEmpty();
    }

    /** {@inheritDoc} */
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
     * @param g a {@link ubic.erminej.data.Gene} object.
     */
    public void setGene( Gene g ) {
        this.genes.clear();
        this.genes.add( g );
    }

    /**
     * <p>
     * Setter for the field <code>genes</code>.
     * </p>
     *
     * @param genes a {@link java.util.Collection} object.
     */
    public void setGenes( Collection<Gene> genes ) {
        this.genes = genes;
    }

    /**
     * <p>
     * Setter for the field <code>geneSets</code>.
     * </p>
     *
     * @param geneSets a {@link java.util.Collection} object.
     */
    public void setGeneSets( Collection<GeneSetTerm> geneSets ) {
        this.geneSets = geneSets;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Element [name=" + name + ", " + getGene() + "]";
    }

    /**
     * <p>
     * addGene.
     * </p>
     *
     * @param g a {@link ubic.erminej.data.Gene} object.
     */
    protected void addGene( Gene g ) {
        // a little bit dangerous
        this.genes.add( g );
    }

    /**
     * <p>
     * removeGeneSet.
     * </p>
     *
     * @param t a {@link ubic.erminej.data.GeneSetTerm} object.
     */
    protected void removeGeneSet( GeneSetTerm t ) {
        this.geneSets.remove( t );
    }

}
