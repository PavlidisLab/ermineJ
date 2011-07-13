/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej.data;

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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;
import ubic.erminej.Settings;

/**
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class UserDefinedGeneSetManager {

    private static Log log = LogFactory.getLog( UserDefinedGeneSetManager.class.getName() );
    private static final String USERGENESET_SUFFIX = "-class.txt";

    private static Settings settings;
    public static GeneAnnotations geneData;

    private static Map<String, UserDefinedGeneSet> userGeneSets = new HashMap<String, UserDefinedGeneSet>();

    private static GONames goData;

    /**
     * @param goData
     */
    public static void addGeneSet( UserDefinedGeneSet set ) {
        String id = set.getId();
        userGeneSets.put( id, set );
        geneData.addGeneSet( id, set.getProbes() );
        goData.addGeneSet( id, set.getDesc() );
    }

    /**
     * Delete a user-defined gene set from disk. Note: this doesn't work if there are multiple gene groups in the file.
     * <p>
     * This doesn't handle removing it from the maps. The caller has to do that.
     * 
     * @param ngs
     */
    public static boolean deleteUserGeneSet( String id ) {
        String classFile = getUserGeneSetFileForName( id );
        File file = new File( classFile );

        if ( !file.exists() ) {
            log.error( file.getAbsoluteFile() + " does not exist" );
        }
        if ( !file.canWrite() ) {
            log.error( "Cannot delete " + file.getAbsoluteFile() );
        }

        log.debug( "Deleting " + file.getAbsolutePath() );
        try {
            return file.delete();
        } catch ( Exception e ) {
            log.error( e, e );
            return false;
        }

    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public static Map<String, Object> getGeneSetFileInfo( String file ) throws IOException {
        Map<String, Object> cinfo = new HashMap<String, Object>();
        FileTools.checkPathIsReadableFile( file );
        FileInputStream fis = new FileInputStream( file );
        BufferedInputStream bis = new BufferedInputStream( fis );
        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        String row;
        Collection<String> members = new ArrayList<String>();
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

    /**
     * @param dir
     * @param className
     * @return
     */
    public static String getUserGeneSetFileForName( String id ) {
        String classFile = settings.getUserGeneSetDirectory() + System.getProperty( "file.separator" )
                + cleanGeneSetName( id ) + USERGENESET_SUFFIX;
        return classFile;
    }

    /**
     * Read in a list of genes or probe ids from a file. The list of genes is unadorned, one per row.
     * 
     * @param fileName
     * @return FIXME This does not have an ID stored!!
     * @throws IOException
     */
    public static UserDefinedGeneSet loadPlainGeneList( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );
        String row;
        Collection<String> genesOrProbes = new ArrayList<String>();
        while ( ( row = dis.readLine() ) != null ) {
            if ( row.length() == 0 ) continue;
            genesOrProbes.add( row );
        }
        dis.close();

        List<String> probes = convertToProbes( genesOrProbes );
        UserDefinedGeneSet result = new UserDefinedGeneSet();
        result.setProbes( probes );
        return result;
    }

    /**
     * Add user-defined gene set(s) to the GeneData. The format is:
     * <ol>
     * <li>Rows starting with "#" are ignored as comments.
     * <li>The type of gene set {gene|probe}
     * <li>The identifier for the gene set, e.g, "My gene set"
     * <li>The description for the gene set, e.g, "Genes I like"
     * <li>Any number of rows containing gene or probe identifiers.
     * <li>A row starting with "===" delimits multiple gene sets in one file.
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
    public static Collection<UserDefinedGeneSet> loadUserGeneSet( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );

        Collection<UserDefinedGeneSet> result = new HashSet<UserDefinedGeneSet>();

        while ( dis.ready() ) {
            UserDefinedGeneSet newSet = readOneSet( dis );
            result.add( newSet );
        }
        dis.close();

        return result;
    }

    /**
     * Load the user-defined gene sets.
     */
    public static Collection<String> loadUserGeneSets( StatusViewer statusMessenger ) {
        Collection<String> userOverwrittenGeneSets = new HashSet<String>();

        File userGeneSetDir = new File( settings.getUserGeneSetDirectory() );
        if ( !userGeneSetDir.exists() ) {
            statusMessenger.showError( "No user-define gene set directory found, none will be loaded" );
            return userOverwrittenGeneSets;
        }

        String[] classFiles = userGeneSetDir.list();
        int numLoaded = 0;
        for ( int i = 0; i < classFiles.length; i++ ) {

            String classFile = classFiles[i];
            if ( StringUtils.isEmpty( classFile ) ) {
                continue;
            }

            String classFilePath = null;
            try {
                classFilePath = userGeneSetDir + System.getProperty( "file.separator" ) + classFile;
                log.debug( "Loading " + classFilePath );
                Collection<UserDefinedGeneSet> loadedSets = loadUserGeneSet( classFilePath );

                numLoaded += loadedSets.size();
                for ( UserDefinedGeneSet set : loadedSets ) {
                    String id = set.getId();
                    if ( isExistingGeneSet( id ) ) {
                        log.debug( "User-defined gene set overriding " + id );
                        modifyGeneSet( set );
                        userOverwrittenGeneSets.add( id );
                    } else {
                        userGeneSets.put( id, set );
                        geneData.addGeneSet( id, set.getProbes() );
                        goData.addGeneSet( id, set.getDesc() );
                    }
                }

            } catch ( IOException e ) {
                if ( statusMessenger != null ) {
                    // This error will be shown if there are files that don't fit the format.
                    statusMessenger.showError( "Could not load user-defined class from " + classFilePath );
                }
            }

        }
        if ( statusMessenger != null && numLoaded > 0 )
            statusMessenger.showStatus( "Loaded " + numLoaded + " customized gene sets from " + classFiles.length
                    + " files." );
        return userOverwrittenGeneSets;
    }

    /**
     * Redefine a class. The "real" version of the class is modified to look like this one.
     * 
     * @param set
     */
    public static void modifyGeneSet( UserDefinedGeneSet set ) {
        log.debug( "Modifying " + set.getId() );
        geneData.modifyGeneSet( set.getId(), set.getProbes() );
        goData.modifyGeneSet( set.getId(), set.getDesc() );
    }

    /**
     * Write a gene set to disk, in the directory set in the preferences.
     * 
     * @param type
     * @throws IOException
     */
    public static void saveGeneSet( UserDefinedGeneSet set ) throws IOException {
        String cleanedDescription = set.getDesc().replace( '\n', ' ' );
        String filetype = ( set.isGenes() ) ? "probe" : "gene";
        BufferedWriter out = new BufferedWriter( new FileWriter( getUserGeneSetFileForName( set.getId() ), false ) );
        out.write( filetype + "\n" );
        out.write( set.getId() + "\n" );
        out.write( cleanedDescription + "\n" );
        if ( set.isGenes() ) {
            for ( String g : set.getGenes() ) {
                out.write( g + "\n" );
            }
        } else {
            for ( String p : set.getProbes() ) {
                out.write( p + "\n" );
            }
        }
        out.close();
    }

    /**
     * @return
     */
    private static String cleanGeneSetName( String id ) {
        String fileid = id.replaceAll( ":", "-" );
        fileid = fileid.replaceAll( "\\s+", "_" );
        return fileid;
    }

    /**
     * @param genesOrProbeNames OR probe ids.
     * @return
     */
    private static List<String> convertToProbes( Collection<String> genesOrProbeNames ) {
        List<String> probeSet = new ArrayList<String>();
        int ignored = 0;
        for ( String identifier : genesOrProbeNames ) {
            if ( geneData.getGeneProbeList( identifier ) != null ) {
                probeSet.addAll( geneData.getGeneProbeList( identifier ) );
                log.debug( "Gene " + identifier + " recognized." );
            } else if ( geneData.getProbeGeneName( identifier ) != null ) {
                log.debug( "Probe " + identifier + " recognized" );
                probeSet.add( identifier ); // it's actually a probe
            } else {
                log.debug( "Gene or probe " + identifier + " not found in the array design" );
                ignored++;
            }
        }
        if ( ignored > 0 ) {
            log.info( ignored + " items skipped because they are not in the current platform." );
        }
        return probeSet;
    }

    /**
     * @param ngs
     * @return
     */
    private static boolean isExistingGeneSet( String id1 ) {
        return geneData.hasGeneSet( id1 );
    }

    /**
     * @param dis
     * @return
     * @throws IOException
     */
    private static UserDefinedGeneSet readOneSet( BufferedReader dis ) throws IOException {
        String type = "";
        boolean hasUnknownProbes = false;
        boolean isGenes = true;
        String row = null;
        Collection<String> genes = new ArrayList<String>();
        UserDefinedGeneSet newSet = new UserDefinedGeneSet();
        while ( ( row = dis.readLine() ) != null ) {

            if ( row.startsWith( "#" ) ) {
                continue; // comment line, always ignored.
            }

            if ( row.startsWith( "===" ) ) {
                break;
            }

            if ( StringUtils.isBlank( type ) ) {
                type = row;
                if ( type.equalsIgnoreCase( "probe" ) ) {
                    isGenes = false;
                } else if ( type.equalsIgnoreCase( "gene" ) ) {
                    isGenes = true;
                } else {
                    throw new IOException( "Unknown data type. Each set must start with 'probe' or 'gene'" );
                }
                newSet.setIsGenes( isGenes );
                continue;
            }

            if ( StringUtils.isBlank( newSet.getId() ) ) {
                newSet.setId( row );
                continue;
            }

            if ( StringUtils.isBlank( newSet.getDesc() ) ) {
                newSet.setDesc( row );
                continue;
            }

            if ( isGenes ) {
                genes.add( row );
            } else {
                if ( geneData.getProbeGeneName( row ) == null ) {
                    hasUnknownProbes = true;
                } else {
                    newSet.getProbes().add( row );
                }
            }
        }
        assert newSet.getId() != null;

        if ( isGenes ) {
            newSet.setGenes( genes );
            List<String> probes = convertToProbes( genes );
            newSet.setProbes( probes );
        }
        if ( hasUnknownProbes ) {
            log.info( "Some probes not found in array design" );
        } else if ( newSet.getProbes().isEmpty() ) {
            log.info( "No probes for " + newSet.getId() + " match current platform" );
        }
        return newSet;
    }

    /**
     * @param fileName
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static BufferedReader setUpToLoad( String fileName ) throws IOException, FileNotFoundException {
        FileTools.checkPathIsReadableFile( fileName );
        FileInputStream fis = new FileInputStream( fileName );
        BufferedInputStream bis = new BufferedInputStream( fis );
        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        return dis;
    }

    protected UserDefinedGeneSetManager() {

    }

    public static void init( GeneAnnotations gd, GONames goD, Settings set ) {
        if ( goData != null ) {
            throw new IllegalStateException( "You should only call init once" );
        }
        goData = goD;
        geneData = gd;
        settings = set;
    }

    public UserDefinedGeneSet get( String id ) {
        return userGeneSets.get( id );
    }

    public String getAspect( String id ) {
        if ( !userGeneSets.containsKey( id ) ) {
            throw new IllegalArgumentException( "No such group defined" );
        }
        return userGeneSets.get( id ).getAspect();
    }

    public String getDefinition( String id ) {
        if ( !userGeneSets.containsKey( id ) ) {
            throw new IllegalArgumentException( "No such group defined" );
        }
        return userGeneSets.get( id ).getDefinition();
    }

    public String getDescription( String id ) {
        if ( !userGeneSets.containsKey( id ) ) {
            throw new IllegalArgumentException( "No such group defined" );
        }
        return userGeneSets.get( id ).getDesc();
    }

    /**
     * @param id1
     * @param goData
     * @return
     */
    public static Map<String, Object> getGeneSetInfo( String id1 ) {
        Map<String, Object> cinfo = new HashMap<String, Object>();
        cinfo.put( "type", "probe" );
        cinfo.put( "id", id1 );
        cinfo.put( "desc", goData.getNameForId( id1 ) );
        cinfo.put( "aspect", goData.getAspectForId( id1 ) );
        cinfo.put( "definition", goData.getDefinitionForId( id1 ) );
        Collection<String> members = geneData.getClassToProbes( id1 );
        cinfo.put( "members", members );
        return cinfo;
    }

    public Collection<String> getProbes( String id ) {
        if ( !userGeneSets.containsKey( id ) ) {
            throw new IllegalArgumentException( "No such group defined" );
        }
        return userGeneSets.get( id ).getProbes();
    }
}