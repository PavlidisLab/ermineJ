/*
 * The basecode project
 * 
 * Copyright (c) 2005 University of British Columbia
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ubic.erminej.data;

/**
 * Represents a GO term (most commonly) or other way of grouping genes.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTerm implements Comparable<GeneSetTerm> {

    private String aspect;

    private String oldName;

    private String oldDefinition;

    private static String NO_DEFINITION_AVAILABLE = "[No definition available]";

    protected static String NO_NAME_AVAILABLE = "[No term available]";

    private String id = null;

    private String name = NO_NAME_AVAILABLE;

    private String definition = NO_DEFINITION_AVAILABLE;

    private boolean isUserDefined = false;

    /**
     * @param id
     */
    public GeneSetTerm( String id ) {
        this.id = id;
    }

    public GeneSetTerm( String id, String name ) {
        this( id );
        this.name = name;
    }

    /**
     * @param id
     * @param name
     * @param def
     */
    public GeneSetTerm( String id, String name, String def ) {
        this( id, name );
        this.definition = def;
    }

    @Override
    public int compareTo( GeneSetTerm o ) {
        return this.getId().compareTo( o.getId() );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        GeneSetTerm other = ( GeneSetTerm ) obj;
        if ( id == null ) {
            if ( other.id != null ) return false;
        } else if ( !id.equals( other.id ) ) return false;
        return true;
    }

    public String getAspect() {
        return aspect;
    }

    /**
     * @return
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        return result;
    }

    public boolean isUserDefined() {
        return isUserDefined;
    }

    public void reset() {
        if ( this.oldName != null ) this.setName( this.oldName );
        if ( this.oldDefinition != null ) this.setDefinition( oldDefinition );
    }

    public void setAspect( String aspect ) {
        this.aspect = aspect;
    }

    public void setDefinition( String d ) {
        this.oldDefinition = this.getDefinition();
        this.definition = d;
    }

    public void setName( String n ) {
        this.oldName = this.getName();
        this.name = n;
    }

    public void setUserDefined( boolean isUserDefined ) {
        this.isUserDefined = isUserDefined;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }

    /**
     * @return true if this represents an aspect (molecular_function etc.)
     */
    public boolean isAspect() {
        if ( this.aspect == null ) return false;
        return this.aspect == "Root" || this.aspect.equals( this.name ) || this.aspect.equals( this.id );
    }

}
