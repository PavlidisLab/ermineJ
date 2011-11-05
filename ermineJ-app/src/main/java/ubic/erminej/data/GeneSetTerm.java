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
