/*
 * The ermineJ project
 * 
 * Copyright (c) 2006-2011 University of British Columbia
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusViewer;

import ubic.erminej.Settings;

/**
 * This is designed to work as a singleton in the scope of a running ErmineJ instance.
 * 
 * @author Homin K Lee
 * @author Paul Pavlidis
 * @version $Id$
 */
public class UserDefinedGeneSetManager {

    private static Log log = LogFactory.getLog( UserDefinedGeneSetManager.class.getName() );
    private static final String USERGENESET_SUFFIX = "-class.txt";

    private static Settings settings;
    public static GeneAnnotations geneData;

    private static Map<GeneSetTerm, GeneSet> userGeneSets = new HashMap<GeneSetTerm, GeneSet>();

    /**
     * Delete a user-defined gene set from disk. Note: this doesn't work if there are multiple gene groups in the file.
     * <p>
     * This doesn't handle removing it from the maps. The caller has to do that.
     * 
     * @param ngs
     */
    public static boolean deleteUserGeneSet( GeneSetTerm id ) {
        String classFile = getUserGeneSetFileForName( id.getId() );
        File file = new File( classFile );

        if ( !file.exists() ) {
            log.error( file.getAbsoluteFile() + " does not exist" );
            return false;
        }
        if ( !file.canWrite() ) {
            log.error( "Cannot delete " + file.getAbsoluteFile() + ": file is not editable" );
            return false;
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

    public static void init( GeneAnnotations gd, Settings set ) {
        // assert geneData == null : "You should only call init once";
        geneData = gd;
        settings = set;
    }

    /**
     * Read in a list of genes or probe ids from a file. The list of genes is unadorned, one per row.
     * 
     * @param fileName
     * @return incomplete gene set. The caller has to arrange for this to be finished.
     * @throws IOException
     */
    public static GeneSet loadPlainGeneList( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );
        String row;
        Collection<Gene> genes = new ArrayList<Gene>();
        while ( ( row = dis.readLine() ) != null ) {
            if ( row.length() == 0 ) continue;

            Gene g = geneData.findGene( row );
            if ( g == null ) {
                Probe p = geneData.findProbe( row );
                if ( p == null ) {
                    log.warn( "Could not identify " + row ); // maybe we should add it.
                    continue;
                }
                g = p.getGene();
            }

            genes.add( g );
        }
        dis.close();

        GeneSet result = new GeneSet();
        result.setGenes( genes );
        result.setSourceFile( fileName );
        result.setUserDefined( true );
        result.getTerm().setAspect( GeneSetTerms.USER_DEFINED );
        return result;
    }

    /**
     * Add user-defined gene set(s) to the GeneData.
     * <ul>
     * <li>Rows starting with "#" are ignored as comments.</li>
     * <li>A row starting with "===" delimits multiple gene sets in one file.</li>
     * </ul>
     * The format of a group is:
     * <ol>
     * <li>The first line in a group: The type of gene set {gene|probe}</li>
     * <li>Second line: The identifier for the gene set, e.g, "My gene set"; tab characters should be avoided in this
     * line to avoid confusion with the other supported format</li>
     * <li>Third line: The description for the gene set, e.g, "Genes I like"; tab characters should be avoided in this
     * line to avoid confusion with the other format</li>
     * <li>Any number of rows containing gene or probe identifiers.</li>
     * </ol>
     * Alternatively, a tab-delimited file can be provided with one group per row, with the following format:
     * <ol>
     * <li>A name for the group (e.g., KEGG identifier)</li>
     * <li>A description for the group (can be blank but must present)</li>
     * <li>The remaining fields are interpreted as gene symbols</li>
     * <li>Lines starting with "#" are ignored as comments.</li>
     * <li>Lines starting with "===" are ignored.</li>
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
    public static Collection<GeneSet> loadUserGeneSetFile( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );

        Collection<GeneSet> result = new HashSet<GeneSet>();

        while ( dis.ready() ) {
            GeneSet newSet = readOneSet( dis );
            if ( newSet == null ) {
                log.warn( "Set was not read from " + fileName );
                continue;
            }
            result.add( newSet );
        }
        dis.close();

        for ( GeneSet set : result ) {
            set.setSourceFile( fileName );
        }

        return result;
    }

    /**
     * Load the user-defined gene sets.
     */
    public static Collection<GeneSetTerm> loadUserGeneSets( StatusViewer statusMessenger ) {
        Collection<GeneSetTerm> userOverwrittenGeneSets = new HashSet<GeneSetTerm>();

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
                Collection<GeneSet> loadedSets = loadUserGeneSetFile( classFilePath );

                numLoaded += loadedSets.size();
                for ( GeneSet set : loadedSets ) {
                    GeneSetTerm id = set.getTerm();
                    if ( isExistingGeneSet( id ) ) {
                        log.debug( "User-defined gene set overriding " + id );
                        modifyGeneSet( set );
                        userOverwrittenGeneSets.add( id );
                    } else {
                        userGeneSets.put( id, set );
                        geneData.addGeneSet( id, set.getGenes() );
                    }
                }

            } catch ( IOException e ) {
                if ( statusMessenger != null ) {
                    // This error will be shown if there are files that don't fit the format.
                    statusMessenger
                            .showError( "Could not load gene sets from " + classFilePath + ": " + e.getMessage() );
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
    public static void modifyGeneSet( GeneSet set ) {
        log.debug( "Modifying or adding " + set.getId() );
        geneData.modifyGeneSet( set.getTerm(), set.getProbes() );
    }

    /**
     * Write a gene set to disk, in the directory set in the preferences.
     * 
     * @param type
     * @throws IOException
     */
    public static void saveGeneSet( GeneSet setToSave ) throws IOException {

        String fileName = null;
        if ( StringUtils.isNotBlank( setToSave.getSourceFile() ) ) {
            fileName = setToSave.getSourceFile();
        } else {
            fileName = getUserGeneSetFileForName( setToSave.getId() );
        }

        /*
         * FIXME we need to determine a clearer policy for what happens if you modify a set.
         */

        /*
         * FIXME this assume the set cam in the ermineJ native format!!
         */

        /*
         * Handle case of multiple groups per file. We re-write it, clobber the file.
         */
        if ( ( new File( fileName ) ).canRead() ) {
            Collection<GeneSet> sets = loadUserGeneSetFile( fileName );

            BufferedWriter out = new BufferedWriter( new FileWriter( fileName, false ) );
            for ( GeneSet s : sets ) {
                if ( s.getId().equals( setToSave.getId() ) ) {
                    writeSet( setToSave, out );
                } else {
                    writeSet( s, out );
                }
            }
            out.close();
        } else {
            BufferedWriter out = new BufferedWriter( new FileWriter( fileName, false ) );
            writeSet( setToSave, out );
            out.close();
        }
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
     * @param ngs
     * @return
     */
    private static boolean isExistingGeneSet( GeneSetTerm id1 ) {
        return geneData.hasGeneSet( id1 );
    }

    /**
     * @param dis
     * @return
     * @throws IOException
     */
    private static GeneSet readOneSet( BufferedReader dis ) throws IOException {
        String type = null;
        boolean hasUnknownProbes = false;
        boolean isGenes = true;
        String row = null;
        GeneSet newSet = null;

        while ( ( row = dis.readLine() ) != null ) {

            if ( row.startsWith( "#" ) ) {
                continue; // comment line, always ignored.
            }

            if ( row.startsWith( "===" ) ) {
                // reset.
                type = null;
                isGenes = true;
                hasUnknownProbes = false;
                break;
            }

            String[] fields = StringUtils.split( row, '\t' );
            if ( fields.length == 0 ) {
                continue;
            }

            if ( fields.length > 2 ) {
                /*
                 * We assume there is one record per row.
                 */
                newSet = new GeneSet( new GeneSetTerm( fields[0], fields[1] ) );
                newSet.setIsGenes( true );
                newSet.setUserDefined( true );
                for ( int i = 2; i < fields.length; i++ ) {
                    Gene g = geneData.findGene( fields[i] );
                    if ( g != null ) newSet.addGene( g );
                }
                break;
            }

            // start a new multi-line record.

            // first line of group: tells us the type of record.
            if ( StringUtils.isBlank( type ) ) {
                type = row;
                if ( type.equalsIgnoreCase( "probe" ) ) {
                    isGenes = false;
                } else if ( type.equalsIgnoreCase( "gene" ) ) {
                    isGenes = true;
                } else {
                    throw new IOException( "Unknown data type. Each set must start with 'probe' or 'gene'" );
                }
                continue;
            }

            GeneSetTerm term = null;

            // second line of group: id
            if ( newSet == null ) {
                term = new GeneSetTerm( row );
                newSet = new GeneSet( term );
                newSet.setUserDefined( true );
                newSet.setIsGenes( isGenes );
                continue;
            }

            // third line of group: name
            if ( StringUtils.isBlank( newSet.getTerm().getName() )
                    || newSet.getName().equals( GeneSetTerm.NO_NAME_AVAILABLE ) ) {
                newSet.getTerm().setName( row );
                continue;
            }

            // read the genes.
            if ( isGenes ) {
                Gene gene = geneData.findGene( row );
                if ( gene == null ) {
                    hasUnknownProbes = true;
                    log.warn( "Gene " + row + " not recognized" );
                    continue;
                }
                newSet.addGene( gene );
            } else {
                Probe probe = geneData.findProbe( row );
                if ( probe == null ) {
                    hasUnknownProbes = true;
                } else {
                    newSet.addGene( probe.getGene() );
                }
            }
        }

        if ( newSet == null ) {
            return newSet;
        }

        if ( hasUnknownProbes ) {
            /*
             * We could add these probes (and genes). This would release users from having to have the annotation file,
             * but I don't think it's that big of a deal.
             */
            log.info( "Some probes or genes in the custom sets not recognized" );
        } else if ( newSet.getProbes().isEmpty() ) {
            log.info( "No probes for " + newSet.getId() + " match current platform" );
        }

        newSet.getTerm().setAspect( GeneSetTerms.USER_DEFINED );

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

    /**
     * Write a set using "ermineJ native" format (not the tab-delimited one)
     * 
     * @param set
     * @param out
     * @throws IOException
     */
    private static void writeSet( GeneSet set, BufferedWriter out ) throws IOException {
        String cleanedDescription = set.getTerm().getName().replace( '\n', ' ' );
        String filetype = ( set.isGenes() ) ? "gene" : "probe";
        out.write( filetype + "\n" );
        out.write( set.getId() + "\n" );
        out.write( cleanedDescription + "\n" );
        if ( set.isGenes() ) {
            for ( Gene g : set.getGenes() ) {
                out.write( g.getSymbol() + "\n" );
            }
        } else {
            for ( Probe p : set.getProbes() ) {
                out.write( p.getName() + "\n" );
            }
        }
    }

    /**
     * For testing only -- does NOT set the file name since we don't know it.
     * 
     * @param is
     * @return
     * @throws IOException
     */
    protected static Collection<GeneSet> loadUserGeneSetFile( InputStream is ) throws IOException {
        BufferedReader dis = new BufferedReader( new InputStreamReader( is ) );
        Collection<GeneSet> result = new HashSet<GeneSet>();

        while ( dis.ready() ) {
            GeneSet newSet = readOneSet( dis );
            if ( newSet == null ) {
                log.warn( "Set was not read" );
                continue;
            }
            result.add( newSet );
        }
        dis.close();
        return result;
    }

    protected UserDefinedGeneSetManager() {

    }

    public GeneSet get( String id ) {
        return userGeneSets.get( id );
    }

    public static void addGeneSet( GeneSet set ) {
        userGeneSets.put( set.getTerm(), set );
        geneData.addGeneSet( set );

    }

}