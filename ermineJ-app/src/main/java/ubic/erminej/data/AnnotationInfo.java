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
 * Holds information on available array design annotation.
 * 
 * @author paul
 * @version $Id$
 */
public class AnnotationInfo {
    private String shortName;
    private Long id;
    private String name;

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    // String accession;
    private String taxon;
    private boolean hasAnnotations;

    public AnnotationInfo() {
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName( String shortName ) {
        this.shortName = shortName;
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getTaxon() {
        return taxon;
    }

    public void setTaxon( String taxon ) {
        this.taxon = taxon;
    }

    public boolean getHasAnnotations() {
        return hasAnnotations;
    }

    public void setHasAnnotations( boolean hasAnnotations ) {
        this.hasAnnotations = hasAnnotations;
    }

}