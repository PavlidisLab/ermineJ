/*
 * The ermineJ project
 *
 * Copyright (c) 2013 University of British Columbia
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
 * This is based on ArrayDesignValueObject from Gemma. We don't use a direct dependency because of incompatible java
 * versions, which made it more trouble that it was worth.
 *
 * @author Paul
 * @version $Id: $Id
 */
public class Platform implements java.io.Serializable, Comparable<Platform> {

    private static final long serialVersionUID = -8259245319391937522L;

    private String color;

    private String dateCached;

    private java.util.Date dateCreated;

    private String description;

    private Long designElementCount;

    private Long expressionExperimentCount;

    private boolean hasAnnotationFile;

    private Boolean hasBlatAssociations;

    private Boolean hasGeneAssociations;

    private Boolean hasSequenceAssociations;

    private Long id;

    private Boolean isMerged;

    private Boolean isMergee;

    private Boolean isSubsumed;

    private Boolean isSubsumer;

    private java.util.Date lastGeneMapping;

    private java.util.Date lastRepeatMask;

    private java.util.Date lastSequenceAnalysis;

    private java.util.Date lastSequenceUpdate;

    private String name;

    private Boolean needsAttention;

    private String numGenes;

    private String numProbeAlignments;

    private String numProbeSequences;

    private String numProbesToGenes;

    private String shortName;

    private String curationNote;

    private String taxon;

    private String technologyType;

    private Boolean troubled = false;

    private String troubleDetails = "(Details of trouble not populated)";

    private Boolean validated = false;

    /**
     * <p>
     * Constructor for Platform.
     * </p>
     */
    public Platform() {
    }

    /**
     * Copies constructor from other Platform
     *
     * @throws java.lang.NullPointerException if the argument is <code>null</code>
     * @param otherBean a {@link ubic.erminej.data.Platform} object.
     */
    public Platform( Platform otherBean ) {
        this( otherBean.getName(), otherBean.getShortName(), otherBean.getDesignElementCount(), otherBean.getTaxon(),
                otherBean.getExpressionExperimentCount(), otherBean.getHasSequenceAssociations(), otherBean
                        .getHasBlatAssociations(),
                otherBean.getHasGeneAssociations(), otherBean.getId(), otherBean
                        .getColor(),
                otherBean.getNumProbeSequences(), otherBean.getNumProbeAlignments(), otherBean
                        .getNumProbesToGenes(),
                otherBean.getNumGenes(), otherBean.getDateCached(), otherBean
                        .getLastSequenceUpdate(),
                otherBean.getLastSequenceAnalysis(), otherBean.getLastGeneMapping(),
                otherBean.getIsSubsumed(), otherBean.getIsSubsumer(), otherBean.getIsMerged(), otherBean.getIsMergee(),
                otherBean.getLastRepeatMask(), otherBean.getTroubled(), otherBean.getValidated(), otherBean
                        .getDateCreated(),
                otherBean.getDescription(), otherBean.getTechnologyType() );
    }

    /**
     * <p>
     * Constructor for Platform.
     * </p>
     *
     * @param name a {@link java.lang.String} object.
     * @param shortName a {@link java.lang.String} object.
     * @param designElementCount a {@link java.lang.Long} object.
     * @param taxon a {@link java.lang.String} object.
     * @param expressionExperimentCount a {@link java.lang.Long} object.
     * @param hasSequenceAssociations a {@link java.lang.Boolean} object.
     * @param hasBlatAssociations a {@link java.lang.Boolean} object.
     * @param hasGeneAssociations a {@link java.lang.Boolean} object.
     * @param id a {@link java.lang.Long} object.
     * @param color a {@link java.lang.String} object.
     * @param numProbeSequences a {@link java.lang.String} object.
     * @param numProbeAlignments a {@link java.lang.String} object.
     * @param numProbesToGenes a {@link java.lang.String} object.
     * @param numGenes a {@link java.lang.String} object.
     * @param dateCached a {@link java.lang.String} object.
     * @param lastSequenceUpdate a {@link java.util.Date} object.
     * @param lastSequenceAnalysis a {@link java.util.Date} object.
     * @param lastGeneMapping a {@link java.util.Date} object.
     * @param isSubsumed a {@link java.lang.Boolean} object.
     * @param isSubsumer a {@link java.lang.Boolean} object.
     * @param isMerged a {@link java.lang.Boolean} object.
     * @param isMergee a {@link java.lang.Boolean} object.
     * @param lastRepeatMask a {@link java.util.Date} object.
     * @param troubleEvent a boolean.
     * @param validationEvent a boolean.
     * @param dateCreated a {@link java.util.Date} object.
     * @param description a {@link java.lang.String} object.
     * @param technologyType a {@link java.lang.String} object.
     */
    public Platform( String name, String shortName, Long designElementCount, String taxon,
            Long expressionExperimentCount, Boolean hasSequenceAssociations, Boolean hasBlatAssociations,
            Boolean hasGeneAssociations, Long id, String color, String numProbeSequences, String numProbeAlignments,
            String numProbesToGenes, String numGenes, String dateCached, java.util.Date lastSequenceUpdate,
            java.util.Date lastSequenceAnalysis, java.util.Date lastGeneMapping, Boolean isSubsumed,
            Boolean isSubsumer, Boolean isMerged, Boolean isMergee, java.util.Date lastRepeatMask,
            boolean troubleEvent, boolean validationEvent, java.util.Date dateCreated, String description,
            String technologyType ) {
        this.name = name;
        this.shortName = shortName;
        this.designElementCount = designElementCount;
        this.taxon = taxon;
        this.expressionExperimentCount = expressionExperimentCount;
        this.hasSequenceAssociations = hasSequenceAssociations;
        this.hasBlatAssociations = hasBlatAssociations;
        this.hasGeneAssociations = hasGeneAssociations;
        this.id = id;
        this.color = color;
        this.numProbeSequences = numProbeSequences;
        this.numProbeAlignments = numProbeAlignments;
        this.numProbesToGenes = numProbesToGenes;
        this.numGenes = numGenes;
        this.dateCached = dateCached;
        this.lastSequenceUpdate = lastSequenceUpdate;
        this.lastSequenceAnalysis = lastSequenceAnalysis;
        this.lastGeneMapping = lastGeneMapping;
        this.isSubsumed = isSubsumed;
        this.isSubsumer = isSubsumer;
        this.isMerged = isMerged;
        this.isMergee = isMergee;
        this.lastRepeatMask = lastRepeatMask;
        this.troubled = troubleEvent;
        this.validated = validationEvent;
        this.dateCreated = dateCreated;
        this.description = description;
        this.technologyType = technologyType;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo( Platform arg0 ) {

        if ( arg0.getDateCreated() == null || this.getDateCreated() == null ) return 0;

        return arg0.getDateCreated().compareTo( this.getDateCreated() );

    }

    /**
     * Copies all properties from the argument value object into this value object.
     *
     * @param otherBean a {@link ubic.erminej.data.Platform} object.
     */
    public void copy( Platform otherBean ) {
        if ( otherBean != null ) {
            this.setName( otherBean.getName() );
            this.setShortName( otherBean.getShortName() );
            this.setDesignElementCount( otherBean.getDesignElementCount() );
            this.setTaxon( otherBean.getTaxon() );
            this.setExpressionExperimentCount( otherBean.getExpressionExperimentCount() );
            this.setHasSequenceAssociations( otherBean.getHasSequenceAssociations() );
            this.setHasBlatAssociations( otherBean.getHasBlatAssociations() );
            this.setHasGeneAssociations( otherBean.getHasGeneAssociations() );
            this.setId( otherBean.getId() );
            this.setColor( otherBean.getColor() );
            this.setNumProbeSequences( otherBean.getNumProbeSequences() );
            this.setNumProbeAlignments( otherBean.getNumProbeAlignments() );
            this.setNumProbesToGenes( otherBean.getNumProbesToGenes() );
            this.setNumGenes( otherBean.getNumGenes() );
            this.setDateCached( otherBean.getDateCached() );
            this.setLastSequenceUpdate( otherBean.getLastSequenceUpdate() );
            this.setLastSequenceAnalysis( otherBean.getLastSequenceAnalysis() );
            this.setLastGeneMapping( otherBean.getLastGeneMapping() );
            this.setIsSubsumed( otherBean.getIsSubsumed() );
            this.setIsSubsumer( otherBean.getIsSubsumer() );
            this.setIsMerged( otherBean.getIsMerged() );
            this.setIsMergee( otherBean.getIsMergee() );
            this.setLastRepeatMask( otherBean.getLastRepeatMask() );
            this.setTroubled( otherBean.getTroubled() );
            this.setValidated( otherBean.getValidated() );
            this.setDateCreated( otherBean.getDateCreated() );
            this.setDescription( otherBean.getDescription() );
            this.setTechnologyType( otherBean.getTechnologyType() );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see Object#equals(Object)
     */
    /** {@inheritDoc} */
    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Platform other = ( Platform ) obj;
        if ( id == null ) {
            if ( other.id != null ) return false;

            return id.equals( other.id );
        } else if ( !id.equals( other.id ) ) return false;
        if ( shortName == null ) {
            if ( other.shortName != null ) return false;
        } else if ( !shortName.equals( other.shortName ) ) return false;
        return true;
    }

    /**
     * <p>
     * Getter for the field <code>color</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getColor() {
        return this.color;
    }

    /**
     * <p>
     * Getter for the field <code>curationNote</code>.
     * </p>
     *
     * @return the curationNote
     */
    public String getCurationNote() {
        return curationNote;
    }

    /**
     * <p>
     * Getter for the field <code>dateCached</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDateCached() {
        return this.dateCached;
    }

    /**
     * <p>
     * The date the Array Design was created
     * </p>
     *
     * @return a {@link java.util.Date} object.
     */
    public java.util.Date getDateCreated() {
        return this.dateCreated;
    }

    /**
     * <p>
     * Getter for the field <code>description</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * <p>
     * Getter for the field <code>designElementCount</code>.
     * </p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getDesignElementCount() {
        return this.designElementCount;
    }

    /**
     * <p>
     * Getter for the field <code>expressionExperimentCount</code>.
     * </p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getExpressionExperimentCount() {
        return this.expressionExperimentCount;
    }

    /**
     * <p>
     * Getter for the field <code>hasAnnotationFile</code>.
     * </p>
     *
     * @return a boolean.
     */
    public boolean getHasAnnotationFile() {
        return hasAnnotationFile;
    }

    /**
     * <p>
     * Getter for the field <code>hasBlatAssociations</code>.
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getHasBlatAssociations() {
        return this.hasBlatAssociations;
    }

    /**
     * <p>
     * Getter for the field <code>hasGeneAssociations</code>.
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getHasGeneAssociations() {
        return this.hasGeneAssociations;
    }

    /**
     * <p>
     * Getter for the field <code>hasSequenceAssociations</code>.
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getHasSequenceAssociations() {
        return this.hasSequenceAssociations;
    }

    /**
     * <p>
     * Getter for the field <code>id</code>.
     * </p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * <p>
     * Indicates this array design is the merger of other array designs.
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getIsMerged() {
        return this.isMerged;
    }

    /**
     * <p>
     * Indicates that this array design has been merged into another.
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getIsMergee() {
        return this.isMergee;
    }

    /**
     * <p>
     * Indicate if this array design is subsumed by some other array design.
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getIsSubsumed() {
        return this.isSubsumed;
    }

    /**
     * <p>
     * Indicates if this array design subsumes some other array design(s)
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getIsSubsumer() {
        return this.isSubsumer;
    }

    /**
     * <p>
     * Getter for the field <code>lastGeneMapping</code>.
     * </p>
     *
     * @return a {@link java.util.Date} object.
     */
    public java.util.Date getLastGeneMapping() {
        return this.lastGeneMapping;
    }

    /**
     * <p>
     * Getter for the field <code>lastRepeatMask</code>.
     * </p>
     *
     * @return a {@link java.util.Date} object.
     */
    public java.util.Date getLastRepeatMask() {
        return this.lastRepeatMask;
    }

    /**
     * <p>
     * Getter for the field <code>lastSequenceAnalysis</code>.
     * </p>
     *
     * @return a {@link java.util.Date} object.
     */
    public java.util.Date getLastSequenceAnalysis() {
        return this.lastSequenceAnalysis;
    }

    /**
     * <p>
     * Getter for the field <code>lastSequenceUpdate</code>.
     * </p>
     *
     * @return a {@link java.util.Date} object.
     */
    public java.util.Date getLastSequenceUpdate() {
        return this.lastSequenceUpdate;
    }

    /**
     * <p>
     * Getter for the field <code>name</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>
     * Getter for the field <code>needsAttention</code>.
     * </p>
     *
     * @return the needsAttention
     */
    public Boolean getNeedsAttention() {
        return needsAttention;
    }

    /**
     * <p>
     * The number of unique genes that this array design maps to.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumGenes() {
        return this.numGenes;
    }

    /**
     * <p>
     * The number of elements that have BLAT alignments.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumProbeAlignments() {
        return this.numProbeAlignments;
    }

    /**
     * <p>
     * The number of elements that map to bioSequences.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumProbeSequences() {
        return this.numProbeSequences;
    }

    /**
     * <p>
     * The number of elements that map to genes. This count includes probe-aligned regions, predicted genes, and known
     * genes.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumProbesToGenes() {
        return this.numProbesToGenes;
    }

    /**
     * <p>
     * Getter for the field <code>shortName</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * <p>
     * Getter for the field <code>taxon</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTaxon() {
        return this.taxon;
    }

    /**
     * <p>
     * Getter for the field <code>technologyType</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTechnologyType() {
        return this.technologyType;
    }

    /**
     * <p>
     * Getter for the field <code>troubled</code>.
     * </p>
     *
     * @return the troubled
     */
    public Boolean getTroubled() {
        return troubled;
    }

    /**
     * <p>
     * Getter for the field <code>troubleDetails</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTroubleDetails() {
        return troubleDetails;
    }

    /**
     * The last uncleared TroubleEvent associated with this ArrayDesign.
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getTroubleEvent() {
        return this.troubled;
    }

    /**
     * <p>
     * Getter for the field <code>validated</code>.
     * </p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getValidated() {
        return validated;
    }

    /**
     * The last uncleared TroubleEvent associated with this ArrayDesign.
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getValidationEvent() {
        return this.validated;
    }

    /*
     * (non-Javadoc)
     *
     * @see Object#hashCode()
     */
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        if ( id == null ) {
            result = prime * result + ( ( shortName == null ) ? 0 : shortName.hashCode() );
        }
        return result;
    }

    /**
     * <p>
     * Setter for the field <code>color</code>.
     * </p>
     *
     * @param color a {@link java.lang.String} object.
     */
    public void setColor( String color ) {
        this.color = color;
    }

    /**
     * <p>
     * Setter for the field <code>curationNote</code>.
     * </p>
     *
     * @param curationNote the curationNote to set
     */
    public void setCurationNote( String curationNote ) {
        this.curationNote = curationNote;
    }

    /**
     * <p>
     * Setter for the field <code>dateCached</code>.
     * </p>
     *
     * @param dateCached a {@link java.lang.String} object.
     */
    public void setDateCached( String dateCached ) {
        this.dateCached = dateCached;
    }

    /**
     * <p>
     * Setter for the field <code>dateCreated</code>.
     * </p>
     *
     * @param dateCreated a {@link java.util.Date} object.
     */
    public void setDateCreated( java.util.Date dateCreated ) {
        this.dateCreated = dateCreated;
    }

    /**
     * <p>
     * Setter for the field <code>description</code>.
     * </p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription( String description ) {
        this.description = description;
    }

    /**
     * <p>
     * Setter for the field <code>designElementCount</code>.
     * </p>
     *
     * @param designElementCount a {@link java.lang.Long} object.
     */
    public void setDesignElementCount( Long designElementCount ) {
        this.designElementCount = designElementCount;
    }

    /**
     * <p>
     * Setter for the field <code>expressionExperimentCount</code>.
     * </p>
     *
     * @param expressionExperimentCount a {@link java.lang.Long} object.
     */
    public void setExpressionExperimentCount( Long expressionExperimentCount ) {
        this.expressionExperimentCount = expressionExperimentCount;
    }

    /**
     * <p>
     * Setter for the field <code>hasAnnotationFile</code>.
     * </p>
     *
     * @param b a boolean.
     */
    public void setHasAnnotationFile( boolean b ) {
        this.hasAnnotationFile = b;
    }

    /**
     * <p>
     * Setter for the field <code>hasBlatAssociations</code>.
     * </p>
     *
     * @param hasBlatAssociations a {@link java.lang.Boolean} object.
     */
    public void setHasBlatAssociations( Boolean hasBlatAssociations ) {
        this.hasBlatAssociations = hasBlatAssociations;
    }

    /**
     * <p>
     * Setter for the field <code>hasGeneAssociations</code>.
     * </p>
     *
     * @param hasGeneAssociations a {@link java.lang.Boolean} object.
     */
    public void setHasGeneAssociations( Boolean hasGeneAssociations ) {
        this.hasGeneAssociations = hasGeneAssociations;
    }

    /**
     * <p>
     * Setter for the field <code>hasSequenceAssociations</code>.
     * </p>
     *
     * @param hasSequenceAssociations a {@link java.lang.Boolean} object.
     */
    public void setHasSequenceAssociations( Boolean hasSequenceAssociations ) {
        this.hasSequenceAssociations = hasSequenceAssociations;
    }

    /**
     * <p>
     * Setter for the field <code>id</code>.
     * </p>
     *
     * @param id a {@link java.lang.Long} object.
     */
    public void setId( Long id ) {
        this.id = id;
    }

    /**
     * <p>
     * Setter for the field <code>isMerged</code>.
     * </p>
     *
     * @param isMerged a {@link java.lang.Boolean} object.
     */
    public void setIsMerged( Boolean isMerged ) {
        this.isMerged = isMerged;
    }

    /**
     * <p>
     * Setter for the field <code>isMergee</code>.
     * </p>
     *
     * @param isMergee a {@link java.lang.Boolean} object.
     */
    public void setIsMergee( Boolean isMergee ) {
        this.isMergee = isMergee;
    }

    /**
     * <p>
     * Setter for the field <code>isSubsumed</code>.
     * </p>
     *
     * @param isSubsumed a {@link java.lang.Boolean} object.
     */
    public void setIsSubsumed( Boolean isSubsumed ) {
        this.isSubsumed = isSubsumed;
    }

    /**
     * <p>
     * Setter for the field <code>isSubsumer</code>.
     * </p>
     *
     * @param isSubsumer a {@link java.lang.Boolean} object.
     */
    public void setIsSubsumer( Boolean isSubsumer ) {
        this.isSubsumer = isSubsumer;
    }

    /**
     * <p>
     * Setter for the field <code>lastGeneMapping</code>.
     * </p>
     *
     * @param lastGeneMapping a {@link java.util.Date} object.
     */
    public void setLastGeneMapping( java.util.Date lastGeneMapping ) {
        this.lastGeneMapping = lastGeneMapping;
    }

    /**
     * <p>
     * Setter for the field <code>lastRepeatMask</code>.
     * </p>
     *
     * @param lastRepeatMask a {@link java.util.Date} object.
     */
    public void setLastRepeatMask( java.util.Date lastRepeatMask ) {
        this.lastRepeatMask = lastRepeatMask;
    }

    /**
     * <p>
     * Setter for the field <code>lastSequenceAnalysis</code>.
     * </p>
     *
     * @param lastSequenceAnalysis a {@link java.util.Date} object.
     */
    public void setLastSequenceAnalysis( java.util.Date lastSequenceAnalysis ) {
        this.lastSequenceAnalysis = lastSequenceAnalysis;
    }

    /**
     * <p>
     * Setter for the field <code>lastSequenceUpdate</code>.
     * </p>
     *
     * @param lastSequenceUpdate a {@link java.util.Date} object.
     */
    public void setLastSequenceUpdate( java.util.Date lastSequenceUpdate ) {
        this.lastSequenceUpdate = lastSequenceUpdate;
    }

    /**
     * <p>
     * Setter for the field <code>name</code>.
     * </p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * <p>
     * Setter for the field <code>needsAttention</code>.
     * </p>
     *
     * @param needsAttention the needsAttention to set
     */
    public void setNeedsAttention( Boolean needsAttention ) {
        this.needsAttention = needsAttention;
    }

    /**
     * <p>
     * Setter for the field <code>numGenes</code>.
     * </p>
     *
     * @param numGenes a {@link java.lang.String} object.
     */
    public void setNumGenes( String numGenes ) {
        this.numGenes = numGenes;
    }

    /**
     * <p>
     * Setter for the field <code>numProbeAlignments</code>.
     * </p>
     *
     * @param numProbeAlignments a {@link java.lang.String} object.
     */
    public void setNumProbeAlignments( String numProbeAlignments ) {
        this.numProbeAlignments = numProbeAlignments;
    }

    /**
     * <p>
     * Setter for the field <code>numProbeSequences</code>.
     * </p>
     *
     * @param numProbeSequences a {@link java.lang.String} object.
     */
    public void setNumProbeSequences( String numProbeSequences ) {
        this.numProbeSequences = numProbeSequences;
    }

    /**
     * <p>
     * Setter for the field <code>numProbesToGenes</code>.
     * </p>
     *
     * @param numProbesToGenes a {@link java.lang.String} object.
     */
    public void setNumProbesToGenes( String numProbesToGenes ) {
        this.numProbesToGenes = numProbesToGenes;
    }

    /**
     * <p>
     * Setter for the field <code>shortName</code>.
     * </p>
     *
     * @param shortName a {@link java.lang.String} object.
     */
    public void setShortName( String shortName ) {
        this.shortName = shortName;
    }

    /**
     * <p>
     * Setter for the field <code>taxon</code>.
     * </p>
     *
     * @param taxon a {@link java.lang.String} object.
     */
    public void setTaxon( String taxon ) {
        this.taxon = taxon;
    }

    /**
     * <p>
     * Setter for the field <code>technologyType</code>.
     * </p>
     *
     * @param technologyType a {@link java.lang.String} object.
     */
    public void setTechnologyType( String technologyType ) {
        this.technologyType = technologyType;
    }

    /**
     * <p>
     * Setter for the field <code>troubled</code>.
     * </p>
     *
     * @param troubled the troubled to set
     */
    public void setTroubled( Boolean troubled ) {
        this.troubled = troubled;
    }

    /**
     * <p>
     * Setter for the field <code>troubleDetails</code>.
     * </p>
     *
     * @param troubleEvent a {@link java.lang.String} object.
     */
    public void setTroubleDetails( String troubleEvent ) {
        this.troubleDetails = troubleEvent;
    }

    /**
     * <p>
     * Setter for the field <code>validated</code>.
     * </p>
     *
     * @param validated a {@link java.lang.Boolean} object.
     */
    public void setValidated( Boolean validated ) {
        this.validated = validated;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getShortName();
    }

}
