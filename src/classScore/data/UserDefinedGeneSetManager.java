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
    protected GeneAnnotations geneData;
    private String id = "";
    private String desc = "";
    List probes;
    private boolean modifiedGS = false;
    private Settings settings;
    private static final String USERGENESET_SUFFIX = "-class.txt";

    public UserDefinedGeneSetManager( GeneAnnotations geneData, Settings settings, String geneSetId ) {
        this.geneData = geneData;
        this.settings = settings;
        this.id = geneSetId;
        probes = new ArrayList();
    }

    public int compare( UserDefinedGeneSetManager comparee ) {
        int idcomp = comparee.getId().compareTo( id );
        if ( idcomp != 0 ) return idcomp;
        int desccomp = comparee.getDesc().compareTo( desc );
        if ( idcomp != 0 ) return desccomp;
        Collection probes2 = comparee.getProbes();
        if ( probes.size() < probes2.size() )
            return -1;
        else if ( probes.size() > probes2.size() )
            return 1;
        else {
            for ( Iterator it = probes2.iterator(); it.hasNext(); ) {
                String probe2 = ( String ) it.next();
                if ( !probes.contains( probe2 ) ) return -1;
            }
        }
        return 0;
    }

    public void setModified( boolean val ) {
        modifiedGS = val;
    }

    public boolean modified() {
        return modifiedGS;
    }

    public void clear() {
        id = "";
        desc = "";
        probes.clear();
    }

    public AbstractTableModel toTableModel( boolean editable ) {
        final boolean finalized = editable;

        return new AbstractTableModel() {
            private String[] columnNames = { "Probe", "Gene", "Description" };

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

            public int getColumnCount() {
                return 3;
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
     * Add a user-defined gene set to the GeneData. The format is:
     * <ol>
     * <li>The type of gene set {gene|probe}
     * <li>The identifier for the gene set, e.g, "My gene set"
     * <li>The description for the gene set, e.g, "Genes I like"
     * <li>Any number of rows containing gene or probe identifiers.
     * </ol>
     * 
     * @param file which stores the probes or genes.
     * @throws IOException
     */
    public void loadUserGeneSet( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );
        Collection genes = new ArrayList();
        String type = "";

        boolean isGenes = true;
        String row;
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
                id = row;
            } else if ( desc.length() == 0 ) {
                desc = row;
            } else {
                if ( !isGenes ) {
                    if ( geneData.getProbeGeneName( row ) == null )
                        log.info( "Probe " + row + " not found in array design" );
                    probes.add( row );
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

    }

    /**
     * Write a gene set to disk, in the directory set in the preferences.
     * 
     * @param type
     * @throws IOException
     */
    public void saveGeneSet( int type ) throws IOException {
        String cleanedId = cleanGeneSetName();
        String cleanedDescription = desc.replace( '\n', ' ' );
        String filetype = ( type == 0 ) ? "probe" : "gene";
        BufferedWriter out = new BufferedWriter( new FileWriter( getUserGeneSetFileForName( cleanedId ), false ) );
        out.write( filetype + "\n" );
        out.write( id + "\n" );
        out.write( cleanedDescription + "\n" );
        for ( Iterator it = probes.iterator(); it.hasNext(); ) {
            out.write( ( String ) it.next() + "\n" );
        }
        out.close();
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

    public void addToMaps( GONames goData ) {
        geneData.addClass( id, this.getProbes() );
        goData.addClass( id, desc );
        // geneData.sortGeneSets();
    }

    /**
     * Redefine a class. The "real" version of the class is modified to look like this one.
     */
    public void modifyClass( GONames goData ) {
        geneData.modifyClass( id, probes );
        goData.modifyClass( id, desc );
    }

    public void setId( String val ) {
        id = val;
    }

    public void setDesc( String val ) {
        desc = val;
    }

    public void setProbes( List val ) {
        probes = val;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public List getProbes() {
        return probes;
    }

    /**
     * @param dir
     * @param className
     * @return
     */
    public String getUserGeneSetFileForName( String className ) {
        String classFile = settings.getUserGeneSetDirectory() + System.getProperty( "file.separator" )
                + this.cleanGeneSetName() + USERGENESET_SUFFIX;
        return classFile;
    }

    /**
     * Delete a user-defined gene set from disk.
     * <p>
     * FIXME this doesn't handle removing it from the maps.
     * 
     * @param ngs
     */
    public boolean deleteUserGeneSet( String classID ) {
        String classFile = this.getUserGeneSetFileForName( classID );
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
}