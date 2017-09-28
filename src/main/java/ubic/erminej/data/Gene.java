/*
 * The baseCode project
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

/**
 * Represents a Gene entity.
 *
 * @author paul
 * @version $Id$
 */
public class Gene implements Comparable<Gene> {

    private Collection<Element> activeElements = new HashSet<>();

    private Long gemmaID = null;

    private Collection<GeneSetTerm> geneSets = new HashSet<>();

    private String name = "[No Name]";

    private Integer ncbiId = null;

    private Integer ncbiTaxonId = null;

    private Collection<Element> elements = new HashSet<>();

    private final String symbol;

    /**
     * <p>
     * Constructor for Gene.
     * </p>
     *
     * @param symbol a {@link java.lang.String} object.
     */
    public Gene( String symbol ) {
        this( symbol, null );
    }

    /**
     * <p>
     * Constructor for Gene.
     * </p>
     *
     * @param symbol a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     */
    public Gene( String symbol, String name ) {
        super();
        this.symbol = symbol;
        this.name = name;
    }

    /**
     * Updates membership (for elements too).
     *
     * @param t a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return true if it was added; false if it was already there.
     */
    public boolean addGeneSet( GeneSetTerm t ) {
        assert t != null;

        if ( this.geneSets.contains( t ) ) return false;

        this.geneSets.add( t );
        for ( Element p : elements ) {
            p.addToGeneSet( t );
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo( Gene o ) {
        return this.symbol.compareTo( o.getSymbol() );
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Gene other = ( Gene ) obj;

        if ( this.ncbiId != null && other.ncbiId != null ) {
            return this.ncbiId.equals( other.ncbiId );
        }

        return symbol.equals( other.symbol );
    }

    /**
     * <p>
     * getActiveProbes.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Element> getActiveProbes() {
        return Collections.unmodifiableCollection( activeElements );
    }

    /**
     * <p>
     * Getter for the field <code>gemmaID</code>.
     * </p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getGemmaID() {
        return gemmaID;
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
     * Getter for the field <code>ncbiId</code>.
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNcbiId() {
        return ncbiId;
    }

    /**
     * <p>
     * Getter for the field <code>ncbiTaxonId</code>.
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNcbiTaxonId() {
        return ncbiTaxonId;
    }

    /**
     * <p>
     * getProbes.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Element> getProbes() {
        return Collections.unmodifiableCollection( elements );
    }

    /**
     * <p>
     * Getter for the field <code>symbol</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSymbol() {
        return symbol;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        if ( ncbiId != null ) {
            return ncbiId.hashCode();
        }
        return symbol.hashCode();
    }

    /**
     * <p>
     * removeGeneSet.
     * </p>
     *
     * @param t a {@link ubic.erminej.data.GeneSetTerm} object.
     * @return true if it was there to be removed.
     */
    public boolean removeGeneSet( GeneSetTerm t ) {
        assert t != null;
        for ( Element p : this.elements ) {
            p.removeGeneSet( t );
        }
        return this.geneSets.remove( t );
    }

    /**
     * <p>
     * Setter for the field <code>activeElements</code>.
     * </p>
     *
     * @param activeProbes a {@link java.util.Collection} object.
     */
    public void setActiveElements( Collection<Element> activeProbes ) {
        this.activeElements = activeProbes;
    }

    /**
     * <p>
     * Setter for the field <code>elements</code>.
     * </p>
     *
     * @param elements a {@link java.util.Collection} object.
     */
    public void setElements( Collection<Element> elements ) {
        this.elements = elements;
    }

    /**
     * <p>
     * setGemmaId.
     * </p>
     *
     * @param id a {@link java.lang.Long} object.
     */
    public void setGemmaId( Long id ) {
        this.gemmaID = id;
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

    /**
     * <p>
     * Setter for the field <code>ncbiId</code>.
     * </p>
     *
     * @param ncbiId a {@link java.lang.Integer} object.
     */
    public void setNcbiId( Integer ncbiId ) {
        this.ncbiId = ncbiId;
    }

    /**
     * <p>
     * Setter for the field <code>ncbiTaxonId</code>.
     * </p>
     *
     * @param ncbiTaxonId a {@link java.lang.Integer} object.
     */
    public void setNcbiTaxonId( Integer ncbiTaxonId ) {
        this.ncbiTaxonId = ncbiTaxonId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Gene [" + symbol + ", " + name + "]";
    }

    /**
     * <p>
     * addElement.
     * </p>
     *
     * @param probe a {@link ubic.erminej.data.Element} object.
     */
    protected void addElement( Element probe ) {
        this.elements.add( probe );
        probe.addGene( this ); // have to be careful here not to cause stack overflow
        for ( GeneSetTerm gs : this.geneSets ) {
            probe.addToGeneSet( gs );
        }
    }
}
