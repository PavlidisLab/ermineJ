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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * @author paul
 * @version $Id$
 */
public class UserDefinedGeneSet {
    private String aspect;
    private String definition;
    private String desc = "";
    private String id = "";
    private boolean modified = false;
    private List<String> probes = new ArrayList<String>();
    private Collection<String> genes = new HashSet<String>();
    private boolean isGenes; // how was it originally represented.

    // public UserDefinedGeneSet( String id, String aspect, String definition, String description, boolean modified,
    // Collection<String> probes ) {
    // this.id = id;
    // this.aspect = aspect;
    // this.definition = definition;
    // this.desc = description;
    // this.modified = modified;
    // this.probes = probes;
    // }

    public UserDefinedGeneSet() {
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        UserDefinedGeneSet other = ( UserDefinedGeneSet ) obj;
        if ( aspect == null ) {
            if ( other.aspect != null ) return false;
        } else if ( !aspect.equals( other.aspect ) ) return false;
        if ( definition == null ) {
            if ( other.definition != null ) return false;
        } else if ( !definition.equals( other.definition ) ) return false;
        if ( desc == null ) {
            if ( other.desc != null ) return false;
        } else if ( !desc.equals( other.desc ) ) return false;
        if ( id == null ) {
            if ( other.id != null ) return false;
        } else if ( !id.equals( other.id ) ) return false;
        if ( modified != other.modified ) return false;
        if ( probes == null ) {
            if ( other.probes != null ) return false;
        } else {
            if ( other.probes.size() != this.probes.size() ) {
                return false;
            }
            for ( String p : other.probes ) {
                if ( !this.probes.contains( p ) ) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getAspect() {
        return aspect;
    }

    public String getDefinition() {
        return definition;
    }

    public String getDesc() {
        return desc;
    }

    public Collection<String> getGenes() {
        return genes;
    }

    public String getId() {
        return id;
    }

    public Collection<String> getProbes() {
        return probes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( aspect == null ) ? 0 : aspect.hashCode() );
        result = prime * result + ( ( definition == null ) ? 0 : definition.hashCode() );
        result = prime * result + ( ( desc == null ) ? 0 : desc.hashCode() );
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        result = prime * result + ( modified ? 1231 : 1237 );
        for ( String p : probes ) {
            result = prime * result + p.hashCode();
        }

        return result;
    }

    public boolean isGenes() {
        return isGenes;
    }

    public boolean isModified() {
        return modified;
    }

    public void setAspect( String aspect ) {
        this.aspect = aspect;
    }

    public void setDefinition( String definition ) {
        this.definition = definition;
    }

    public void setDesc( String desc ) {
        this.desc = desc;
    }

    public void setGenes( Collection<String> genes ) {
        this.genes = genes;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public void setIsGenes( boolean isGenes ) {
        this.isGenes = isGenes;
    }

    public void setModified( boolean modified ) {
        this.modified = modified;
    }

    public void setProbes( List<String> probes ) {
        this.probes = probes;
    }

    public AbstractTableModel toTableModel( boolean editable ) {
        final boolean finalized = editable;

        return new AbstractTableModel() {
            /**
             * 
             */
            private static final long serialVersionUID = -1738460714695777126L;
            private String[] columnNames = { "Probe", "Gene", "Description" };

            public int getColumnCount() {
                return 3;
            }

            @Override
            public String getColumnName( int i ) {
                return columnNames[i];
            }

            public int getRowCount() {
                int windowrows;
                if ( finalized ) {
                    windowrows = 16;
                } else {
                    windowrows = 13;
                }
                int extra = 1;
                if ( probes.size() < windowrows ) {
                    extra = windowrows - probes.size();
                }
                return probes.size() + extra;
            }

            public Object getValueAt( int r, int c ) {
                if ( r < probes.size() ) {
                    String probeid = probes.get( r );
                    switch ( c ) {
                        case 0:
                            return probeid;
                        case 1:
                            return UserDefinedGeneSetManager.geneData.getProbeGeneName( probeid );
                        case 2:
                            return UserDefinedGeneSetManager.geneData.getProbeDescription( probeid );
                        default:
                            return null;
                    }
                }
                return null;
            }

            @Override
            public boolean isCellEditable( int r, int c ) {
                if ( !finalized && ( c == 0 || c == 1 ) ) {
                    return true;
                }
                return false;
            }
        };
    }

}
