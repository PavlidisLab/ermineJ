/*
 * The ermineJ project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScore.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import baseCode.util.FileTools;
import baseCode.util.StatusViewer;
import classScore.Settings;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class UserDefinedGeneSetManager {

    private static Log log = LogFactory.getLog( UserDefinedGeneSetManager.class.getName() );
    private static final String USERGENESET_SUFFIX = "-class.txt";
    private String aspect;
    private String definition;
    private String desc = "";
    private String id = "";
    private boolean modifiedGS = false;
    private Settings settings;
    protected GeneAnnotations geneData;
    List probes;

    public UserDefinedGeneSetManager( GeneAnnotations geneData, Settings settings, String geneSetId ) {
        this.geneData = geneData;
        this.settings = settings;
        this.id = geneSetId;
        probes = new ArrayList();
    }

    public void addGeneSet( GONames goData ) {
        geneData.addClass( id, this.getProbes() );
        goData.addGeneSet( id, desc );
        // geneData.sortGeneSets();
    }

    public void clear() {
        id = "";
        desc = "";
        probes.clear();
    }

    public boolean compare( UserDefinedGeneSetManager comparee ) {

        if ( !comparee.getId().equals( id ) ) return false;

        if ( !comparee.getDesc().equals( desc ) ) return false;

        if ( !comparee.getDefinition().equals( definition ) ) return false;

        if ( !comparee.getAspect().equals( aspect ) ) return false;

        Collection probesInSet = comparee.getProbes();
        if ( probesInSet.size() != probesInSet.size() ) return false;

        for ( Iterator it = probesInSet.iterator(); it.hasNext(); ) {
            String probe = ( String ) it.next();
            if ( !probesInSet.contains( probe ) ) return false;
        }

        return true;
    }

    /**
     * Delete a user-defined gene set from disk.
     * <p>
     * This doesn't handle removing it from the maps. The caller has to do that.
     * 
     * @param ngs
     */
    public boolean deleteUserGeneSet() {
        String classFile = this.getUserGeneSetFileForName();
        File file = new File( classFile );
        log.debug( "Deleting " + file.getAbsolutePath() );
        if ( !file.exists() ) {
            log.error( file.getAbsoluteFile() + " does not exist" );
        }
        if ( !file.canWrite() ) {
            log.error( "Cannot delete " + file.getAbsoluteFile() );
        }
        try {
            return file.delete();
        } catch ( Exception e ) {
            log.error( e, e );
            return false;
        }

    }

    /**
     * @return Returns the aspect.
     */
    public String getAspect() {
        return this.aspect;
    }

    /**
     * @return Returns the definition.
     */
    public String getDefinition() {
        return this.definition;
    }

    public String getDesc() {
        return desc;
    }

    public Map getGeneSetInfo( String id, GONames goData ) {
        Map cinfo = new HashMap();
        cinfo.put( "type", "probe" );
        cinfo.put( "id", id );
        cinfo.put( "desc", goData.getNameForId( id ) );
        cinfo.put( "aspect", goData.getAspectForId( id ) );
        cinfo.put( "definition", goData.getDefinitionForId( id ) );
        Collection members = geneData.getClassToProbes( id );
        cinfo.put( "members", members );
        return cinfo;
    }

    public String getId() {
        return id;
    }

    public List getProbes() {
        return probes;
    }

    /**
     * @param dir
     * @param className
     * @return
     */
    public String getUserGeneSetFileForName() {
        String classFile = settings.getUserGeneSetDirectory() + System.getProperty( "file.separator" )
                + this.cleanGeneSetName() + USERGENESET_SUFFIX;
        return classFile;
    }

    public boolean isModified() {
        return modifiedGS;
    }

    /**
     * Read in a list of genes from a file. The list of genes is unadorned, one per row.
     * 
     * @param fileName
     * @throws IOException
     */
    public void loadPlainGeneList( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );
        String row;
        Collection genes = new ArrayList();
        while ( ( row = dis.readLine() ) != null ) {
            if ( row.length() == 0 ) continue;
            genes.add( row );
        }
        dis.close();
        int ignored = 0;
        fillInProbes( genes, ignored );
    }

    /**
     * Add a user-defined gene set to the GeneData. The format is:
     * <ol>
     * <li>The type of gene set {gene|probe}
     * <li>The identifier for the gene set, e.g, "My gene set"
     * <li>The description for the gene set, e.g, "Genes I like"
     * <li>Any number of rows containing gene or probe identifiers.
     * </ol>
     * <p>
     * Probes which aren't found on the currently active array design are ignored, but any probes that match identifiers
     * with ones on the current array design are used to build as much of the gene set as possible. It is conceivable
     * that this behavior is not desirable.
     * <p>
     * This overwrites any attributes this instance may have alreadhy had for a gene set (id, description)
     * 
     * @param file which stores the probes or genes.
     * @return true if some probes were read in which are on the current array design.
     * @throws IOException
     */
    public boolean loadUserGeneSet( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );
        Collection genes = new ArrayList();
        String type = "";
        boolean hasUnknownProbes = false;
        boolean isGenes = true;
        String row;
        this.probes = new ArrayList();
        while ( ( row = dis.readLine() ) != null ) {
            if ( type.length() == 0 ) {
                type = row;
                if ( type.equalsIgnoreCase( "probe" ) ) {
                    isGenes = false;
                } else if ( type.equalsIgnoreCase( "gene" ) ) {
                    isGenes = true;
                } else {
                    throw new IOException( "Unknown data type. File must start with 'probe' or 'gene'" );
                }
            } else if ( id.length() == 0 ) {
                this.id = row;
            } else if ( desc.length() == 0 ) {
                this.desc = row;
            } else {
                if ( !isGenes ) {
                    if ( geneData.getProbeGeneName( row ) == null ) {
                        hasUnknownProbes = true;
                    } else {
                        this.probes.add( row );
                    }
                } else {
                    genes.add( row );
                }
            }
        }
        dis.close();
        int ignored = 0;
        if ( isGenes ) {
            fillInProbes( genes, ignored );
        }
        if ( hasUnknownProbes ) {
            log.info( "Some probes not found in array design" );
        }
        if ( this.probes.size() == 0 ) {
            log.info( "No probes in " + fileName + " match current array design" );
            return false;
        }
        return true;
    }

    /**
     * Load the user-defined gene sets.
     */
    public Collection loadUserGeneSets( GONames goData, StatusViewer statusMessenger ) {
        Collection userOverwrittenGeneSets = new HashSet();
        File dir = new File( settings.getUserGeneSetDirectory() );
        if ( dir.exists() ) {
            String[] classFiles = dir.list();
            for ( int i = 0; i < classFiles.length; i++ ) {
                String classFile = classFiles[i];

                try {
                    classFile = settings.getUserGeneSetDirectory() + System.getProperty( "file.separator" ) + classFile;
                    log.debug( "Loading " + classFile );
                    boolean gotSomeProbes = loadUserGeneSet( classFile );
                    if ( gotSomeProbes ) {
                        log.debug( "Read " + this.probes.size() + " probes for " + getId() + " (" + getDesc() + ")" );
                        if ( isExistingGeneSet( getId() ) ) {
                            log.debug( "User-defined gene set overriding " + getId() );
                            modifyGeneSet( goData );
                            userOverwrittenGeneSets.add( getId() );
                        } else {
                            addGeneSet( goData );
                        }
                    }
                } catch ( IOException e ) {
                    if ( statusMessenger != null )
                        statusMessenger.showError( "Could not load user-defined class from " + classFile );
                }
            }
            if ( statusMessenger != null )
                statusMessenger.showStatus( "Loaded " + classFiles.length + " customized gene sets." );
        }
        return userOverwrittenGeneSets;
    }

    /**
     * Redefine a class. The "real" version of the class is modified to look like this one.
     */
    public void modifyGeneSet( GONames goData ) {
        log.debug( "Modifying " + id );
        geneData.modifyGeneSet( id, probes );
        goData.modifyGeneSet( id, desc );
    }

    /**
     * Write a gene set to disk, in the directory set in the preferences.
     * 
     * @param type
     * @throws IOException
     */
    public void saveGeneSet( int type ) throws IOException {
        String cleanedDescription = desc.replace( '\n', ' ' );
        String filetype = ( type == 0 ) ? "probe" : "gene";
        BufferedWriter out = new BufferedWriter( new FileWriter( getUserGeneSetFileForName(), false ) );
        out.write( filetype + "\n" );
        out.write( id + "\n" );
        out.write( cleanedDescription + "\n" );
        for ( Iterator it = probes.iterator(); it.hasNext(); ) {
            out.write( ( String ) it.next() + "\n" );
        }
        out.close();
    }

    /**
     * @param aspectForId
     */
    public void setAspect( String aspectForId ) {
        this.aspect = aspectForId;

    }

    /**
     * @param definitionForId
     */
    public void setDefinition( String definitionForId ) {
        this.definition = definitionForId;

    }

    public void setDesc( String val ) {
        desc = val;
    }

    public void setId( String val ) {
        id = val;
    }

    public void setModified( boolean val ) {
        modifiedGS = val;
    }

    public void setProbes( List val ) {
        probes = val;
    }

    public AbstractTableModel toTableModel( boolean editable ) {
        final boolean finalized = editable;

        return new AbstractTableModel() {
            private String[] columnNames = { "Probe", "Gene", "Description" };

            public int getColumnCount() {
                return 3;
            }

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
                    String probeid = ( String ) probes.get( r );
                    switch ( c ) {
                        case 0:
                            return probeid;
                        case 1:
                            return geneData.getProbeGeneName( probeid );
                        case 2:
                            return geneData.getProbeDescription( probeid );
                        default:
                            return null;
                    }
                }
                return null;
            }

            public boolean isCellEditable( int r, int c ) {
                if ( !finalized && ( c == 0 || c == 1 ) ) {
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * @return
     */
    private String cleanGeneSetName() {
        String fileid = id.replaceAll( ":", "-" );
        fileid = fileid.replaceAll( "\\s+", "_" );
        return fileid;
    }

    /**
     * @param genes
     * @param ignored
     * @return
     */
    private void fillInProbes( Collection genes, int ignored ) {
        Set probeSet = new HashSet();
        for ( Iterator it = genes.iterator(); it.hasNext(); ) {
            String gene = ( ( String ) it.next() ).toUpperCase();
            if ( geneData.getGeneProbeList( gene ) != null ) {
                probeSet.addAll( geneData.getGeneProbeList( gene ) );
            } else {
                log.info( "Gene " + gene + " not found in the array design" );
                ignored++;
            }
        }
        probes = new ArrayList( probeSet );
        if ( ignored > 0 ) {
            log.info( ignored + " items skipped because they are not in the array design." );
        }
    }

    /**
     * @param ngs
     * @return
     */
    private boolean isExistingGeneSet( String id ) {
        return geneData.getGeneSetToProbeMap().containsKey( id );
    }

    /**
     * @param fileName
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private BufferedReader setUpToLoad( String fileName ) throws IOException, FileNotFoundException {
        clear();
        FileTools.checkPathIsReadableFile( fileName );
        FileInputStream fis = new FileInputStream( fileName );
        BufferedInputStream bis = new BufferedInputStream( fis );
        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        return dis;
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static Map getGeneSetFileInfo( String file ) throws IOException {
        Map cinfo = new HashMap();
        FileTools.checkPathIsReadableFile( file );
        FileInputStream fis = new FileInputStream( file );
        BufferedInputStream bis = new BufferedInputStream( fis );
        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        String row;
        Collection members = new ArrayList();
        while ( ( row = dis.readLine() ) != null ) {
            if ( !cinfo.containsKey( "type" ) ) {
                cinfo.put( "type", row );
            } else if ( !cinfo.containsKey( "id" ) ) {
                cinfo.put( "id", row );
            } else if ( !cinfo.containsKey( "desc" ) ) {
                cinfo.put( "desc", row );
            } else {
                members.add( row );
            }
        }
        cinfo.put( "members", members );
        dis.close();

        return cinfo;
    }
}