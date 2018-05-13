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

/**
 * Represents a GO term (most commonly) or other way of grouping genes.
 *
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTerm implements Comparable<GeneSetTerm> {

    /** Constant <code>NO_NAME_AVAILABLE="[No term available]"</code> */
    protected static String NO_NAME_AVAILABLE = "[No term available]";

    private static String NO_DEFINITION_AVAILABLE = "[No definition available]";

    private String aspect;

    private String definition = NO_DEFINITION_AVAILABLE;

    private String id = null;

    private boolean isUserDefined = false;

    private String name = NO_NAME_AVAILABLE;

    private String oldDefinition;

    private String oldName;

    /**
     * <p>
     * Constructor for GeneSetTerm.
     * </p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public GeneSetTerm( String id ) {
        this.id = id;
    }

    /**
     * <p>
     * Constructor for GeneSetTerm.
     * </p>
     *
     * @param id a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     */
    public GeneSetTerm( String id, String name ) {
        this( id );
        this.name = name;
    }

    /**
     * <p>
     * Constructor for GeneSetTerm.
     * </p>
     *
     * @param id a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @param def a {@link java.lang.String} object.
     */
    public GeneSetTerm( String id, String name, String def ) {
        this( id, name );
        this.definition = def;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo( GeneSetTerm o ) {
        return this.getId().compareTo( o.getId() );
    }

    /** {@inheritDoc} */
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

    /**
     * <p>
     * Getter for the field <code>aspect</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAspect() {
        return aspect;
    }

    /**
     * <p>
     * Getter for the field <code>definition</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * <p>
     * Getter for the field <code>id</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
        return id;
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        return result;
    }

    /**
     * <p>
     * isAspect.
     * </p>
     *
     * @return true if this represents an aspect (molecular_function etc.)
     */
    public boolean isAspect() {
        if ( this.aspect == null ) return false;
        return this.aspect == "Root" || this.aspect.equals( this.name ) || this.aspect.equals( this.id );
    }

    /**
     * <p>
     * isUserDefined.
     * </p>
     *
     * @return a boolean.
     */
    public boolean isUserDefined() {
        return isUserDefined;
    }

    /**
     * <p>
     * reset.
     * </p>
     */
    public void reset() {
        if ( this.oldName != null ) this.setName( this.oldName );
        if ( this.oldDefinition != null ) this.setDefinition( oldDefinition );
    }

    /**
     * <p>
     * Setter for the field <code>aspect</code>.
     * </p>
     *
     * @param aspect a {@link java.lang.String} object.
     */
    public void setAspect( String aspect ) {
        if ( this.aspect == null )
            this.aspect = aspect;
        else if ( this.aspect.equals( aspect ) )
            return;
        else
            throw new IllegalArgumentException( "Attempt to change aspect of " + this.getId() );
    }

    /**
     * <p>
     * Setter for the field <code>definition</code>.
     * </p>
     *
     * @param d a {@link java.lang.String} object.
     */
    public void setDefinition( String d ) {
        this.oldDefinition = this.getDefinition();
        this.definition = d;
    }

    /**
     * Only use during creation
     * 
     * @param id
     */
    public void setId( String id ) {
        this.id = id;
    }

    /**
     * <p>
     * Setter for the field <code>name</code>.
     * </p>
     *
     * @param n a {@link java.lang.String} object.
     */
    public void setName( String n ) {
        this.oldName = this.getName();
        this.name = n;
    }

    /**
     * <p>
     * setUserDefined.
     * </p>
     *
     * @param isUserDefined a boolean.
     */
    public void setUserDefined( boolean isUserDefined ) {
        this.isUserDefined = isUserDefined;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return id + ": " + name;
    }

}
