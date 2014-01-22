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

    private Collection<Element> activeElements = new HashSet<Element>();

    private Long gemmaID = null;

    private Collection<GeneSetTerm> geneSets = new HashSet<GeneSetTerm>();

    private String name = "[No Name]";

    private Integer ncbiId = null;

    private Integer ncbiTaxonId = null;

    private Collection<Element> elements = new HashSet<Element>();

    private final String symbol;

    public Gene( String symbol ) {
        this( symbol, null );
    }

    public Gene( String symbol, String name ) {
        super();
        this.symbol = symbol;
        this.name = name;
    }

    /**
     * Updates membership (for elements too).
     * 
     * @param t
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

    @Override
    public int compareTo( Gene o ) {
        return this.symbol.compareTo( o.getSymbol() );
    }

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

    public Collection<Element> getActiveProbes() {
        return Collections.unmodifiableCollection( activeElements );
    }

    public Long getGemmaID() {
        return gemmaID;
    }

    public Collection<GeneSetTerm> getGeneSets() {
        return Collections.unmodifiableCollection( geneSets );
    }

    public String getName() {
        return name;
    }

    public Integer getNcbiId() {
        return ncbiId;
    }

    public Integer getNcbiTaxonId() {
        return ncbiTaxonId;
    }

    public Collection<Element> getProbes() {
        return Collections.unmodifiableCollection( elements );
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public int hashCode() {
        if ( ncbiId != null ) {
            return ncbiId.hashCode();
        }
        return symbol.hashCode();
    }

    /**
     * @param t
     * @return true if it was there to be removed.
     */
    public boolean removeGeneSet( GeneSetTerm t ) {
        assert t != null;
        for ( Element p : this.elements ) {
            p.removeGeneSet( t );
        }
        return this.geneSets.remove( t );
    }

    public void setActiveElements( Collection<Element> activeProbes ) {
        this.activeElements = activeProbes;
    }

    public void setGemmaId( Long id ) {
        this.gemmaID = id;
    }

    public void setGeneSets( Collection<GeneSetTerm> geneSets ) {
        this.geneSets = geneSets;
    }

    public void setNcbiId( Integer ncbiId ) {
        this.ncbiId = ncbiId;
    }

    public void setNcbiTaxonId( Integer ncbiTaxonId ) {
        this.ncbiTaxonId = ncbiTaxonId;
    }

    public void setElements( Collection<Element> elements ) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "Gene [" + symbol + ", " + name + "]";
    }

    /**
     * @param probe
     */
    protected void addElement( Element probe ) {
        this.elements.add( probe );
        probe.addGene( this ); // have to be careful here not to cause stack overflow
        for ( GeneSetTerm gs : this.geneSets ) {
            probe.addToGeneSet( gs );
        }
    }
}
