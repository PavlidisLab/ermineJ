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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date; 
import java.util.HashSet; 

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusStderr;
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

    public enum GeneSetFileFormat {
        LINE_BASED, DEFAULT
    }

    private static Log log = LogFactory.getLog( UserDefinedGeneSetManager.class.getName() );

    private static final String USERGENESET_SUFFIX = "-class.txt";

    private static Settings settings;
    private static GeneAnnotations geneData;

    /**
     * Delete a user-defined gene set from disk and from the annotations in memory.
     * <p>
     * 
     * @param ngs
     */
    public static boolean deleteUserGeneSet( GeneSetTerm termToDelete, StatusViewer m ) {
        GeneSet geneSet = geneData.getGeneSet( termToDelete );

        String classFile = geneSet.getSourceFile();

        if ( StringUtils.isBlank( geneSet.getSourceFile() ) ) {
            classFile = getUserGeneSetFileForName( termToDelete.getId() );
        }

        File file = new File( classFile );

        if ( !file.exists() ) {
            m.showError( file.getAbsoluteFile() + " does not exist" );
            return false;
        }
        if ( !file.canWrite() ) {
            m.showError( "Cannot delete " + file.getAbsoluteFile() + ": file is not editable" );
            return false;
        }

        File backup = null;
        boolean okToDeleteBackup = true;

        try {

            Collection<GeneSet> sets = loadUserGeneSetFile( classFile, m );

            backup = saveBackup( sets );

            boolean success = false;
            if ( sets.size() > 1 ) {

                // have to rewrite the file, omitting this set.

                Writer out = initOutputFile( classFile );
                for ( GeneSet s : sets ) {
                    if ( !s.getId().equals( termToDelete.getId() ) ) {
                        writeSet( s, out );
                    }
                }
                out.close();
                success = true;
            } else {
                log.debug( "Deleting " + file.getAbsolutePath() );
                success = file.delete();
            }

            if ( success ) {
                geneData.deleteGeneSet( termToDelete );
            }

            return success;

        } catch ( Exception e ) {
            log.error( e, e );
            okToDeleteBackup = restoreBackup( new File( classFile ), backup );
            return false;
        } finally {
            if ( backup != null && okToDeleteBackup ) {
                log.info( "Removing backup from " + backup );
                backup.delete();
            }
        }

    }

    /**
     * Usually this would be called only once per application run. The annotations kept here are the 'canonical' ones.
     * 
     * @param gd
     * @param set
     */
    public static void init( GeneAnnotations gd, Settings set ) {
        if ( geneData != null ) {
            log.warn( "Replacing old annotations" );
        }

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
    public static GeneSet loadPlainGeneList( String fileName, StatusViewer m ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );
        String row;
        Collection<Gene> genes = new ArrayList<Gene>();

        boolean probesNotFound = false;
        while ( ( row = dis.readLine() ) != null ) {
            if ( row.length() == 0 ) continue;

            Gene g = geneData.findGene( row );
            if ( g == null ) {
                Probe p = geneData.findProbe( row );
                if ( p == null ) {
                    // log.warn( "Could not identify " + row ); // maybe we should add it.
                    probesNotFound = true;
                    continue;
                }
                g = p.getGene();
            }

            genes.add( g );
        }
        dis.close();

        if ( genes.isEmpty() ) {
            m.showError( "None of the items in your file matched the current annotations." );
            return null;
        }

        if ( probesNotFound ) {
            m.showError( "At least one gene in your set had no matching identifiers in the current annotations." );
        }

        GeneSet result = new GeneSet();
        result.setGenes( genes );
        result.setSourceFile( null ); // if we rewrite it, it will be a new file.
        result.setUserDefined( true );
        result.getTerm().setAspect( GeneSetTerms.USER_DEFINED );
        return result;
    }

    /**
     * Load the user-defined gene sets.
     */
    public static void loadUserGeneSets( StatusViewer statusMessenger ) {

        File userGeneSetDir = new File( settings.getUserGeneSetDirectory() );
        if ( !userGeneSetDir.exists() ) {
            statusMessenger.showError( "No cusotm gene set directory found, none will be loaded - looked for "
                    + settings.getUserGeneSetDirectory() );
            return;
        }

        String[] classFiles = userGeneSetDir.list();

        if ( classFiles.length == 0 ) {
            statusMessenger.showStatus( "No gene sets found in " + settings.getUserGeneSetDirectory() );
            return;
        }
        int timesWarned = 0;
        int maxWarnings = 3;
        int numLoaded = 0;
        Collection<GeneSet> newSets = new HashSet<GeneSet>();
        for ( int i = 0; i < classFiles.length; i++ ) {

            String classFile = classFiles[i];
            if ( StringUtils.isEmpty( classFile ) ) {
                continue;
            }

            String classFilePath = userGeneSetDir + System.getProperty( "file.separator" ) + classFile;
            statusMessenger.showStatus( "Loading gene sets from: " + classFilePath );

            try {

                Collection<GeneSet> loadedSets = loadUserGeneSetFile( classFilePath, statusMessenger );
                numLoaded += loadedSets.size();
                for ( GeneSet set : loadedSets ) {
                    GeneSetTerm id = set.getTerm();
                    if ( isExistingGeneSet( id ) ) {
                        statusMessenger.showError( "Cannot overwrite gene set, please rename it " + id );
                    } else {
                        newSets.add( set );
                    }
                }

            } catch ( IOException e ) {
                // This error will be shown if there are files that don't fit the format.
                statusMessenger.showError( "Could not load gene sets from " + classFilePath + ": " + e.getMessage() );

            }

            /*
             * Warn the user if any of their sets are redundant FIXME we don't store this in the 'redundant sets'
             */
            redundancyCheck( newSets );

            for ( GeneSet s : newSets ) {

                Collection<GeneSet> redundantGroups = s.getRedundantGroups();

                if ( !redundantGroups.isEmpty() && timesWarned < maxWarnings ) {
                    statusMessenger.showError( s.getId() + " is redundant with " + redundantGroups.size()
                            + " other sets (but it will be kept)" );
                    timesWarned++;
                    if ( timesWarned == maxWarnings ) {
                        statusMessenger.showError( "Further warnings about redundancy skipped" );
                    }
                }
            }

            // we can finally add them!
            for ( GeneSet s : newSets ) {
                geneData.addGeneSet( s.getTerm(), s.getGenes(), classFilePath );
            }

            statusMessenger.clear();

        }

        if ( statusMessenger != null && numLoaded > 0 )
            statusMessenger.showStatus( "Loaded " + numLoaded + " customized gene sets from " + classFiles.length
                    + " files." );
    }

    /**
     * Check whether the given gene set is redundant with any others (excluding itself, but including any that were
     * already considered to be redundant)
     * 
     * @param s
     */
    private static void redundancyCheck( Collection<GeneSet> sets ) {
        for ( GeneSet s : sets ) {
            Collection<Gene> genes2 = s.getGenes();

            for ( GeneSet gs1 : geneData.getAllGeneSets() ) {
                Collection<Gene> genes1 = gs1.getGenes();

                if ( gs1.equals( s ) ) continue; // doesn't count ... but this would be a mistake since we haven't added
                // it yet

                if ( genes1.size() != genes2.size() ) continue; // not identical.

                for ( Gene g1 : genes1 ) {
                    if ( !genes2.contains( g1 ) ) continue; // not redundant.
                }
                s.getRedundantGroups().add( s );
            }
        }
    }

    /**
     * Write a gene set to disk, in the directory set in the preferences.
     * 
     * @param type
     * @throws IOException
     */
    public static void saveGeneSet( GeneSet setToSave, StatusViewer m ) throws IOException {
        if ( !setToSave.isUserDefined() ) {
            throw new IllegalArgumentException( "Only user-defined sets can be saved, attempted to change "
                    + setToSave.getId() );
        }

        if ( geneData.hasGeneSet( setToSave.getTerm() ) ) {
            m.showStatus( "Updating " + setToSave );
        }

        String fileName = null;
        if ( StringUtils.isNotBlank( setToSave.getSourceFile() ) ) {
            fileName = setToSave.getSourceFile(); // reuse it.
        } else {
            // make a new file
            fileName = getUserGeneSetFileForName( setToSave.getId() );
        }

        /*
         * FIXME this assume the set came in the ermineJ native format!!
         */

        /*
         * Handle case of multiple groups per file. We re-write it, clobber the file.
         */
        if ( ( new File( fileName ) ).canRead() ) {
            Collection<GeneSet> sets = loadUserGeneSetFile( fileName, m );

            File backup = null;
            boolean okToDeleteBackup = true;

            try {
                backup = saveBackup( sets );

                Writer out = initOutputFile( fileName );
                for ( GeneSet s : sets ) {
                    if ( s.getId().equals( setToSave.getId() ) ) {
                        writeSet( setToSave, out );
                    } else {
                        writeSet( s, out );
                    }
                }
                out.close();
            } catch ( Exception e ) {
                if ( backup != null ) {
                    okToDeleteBackup = restoreBackup( new File( fileName ), backup );
                }
            } finally {
                if ( backup != null && okToDeleteBackup ) {
                    log.info( "Removing backup from " + backup );
                    backup.delete();
                }
            }
        } else {

            // make a new one in a file by itself.

            Writer out = initOutputFile( fileName );
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
     * @param dir
     * @param className
     * @return
     */
    private static String getUserGeneSetFileForName( String id ) {
        String classFile = settings.getUserGeneSetDirectory() + System.getProperty( "file.separator" )
                + cleanGeneSetName( id ) + USERGENESET_SUFFIX;
        return classFile;
    }

    /**
     * Open file for writing, add header.
     * 
     * @param classFile
     * @return
     * @throws IOException
     */
    private static Writer initOutputFile( String classFile ) throws IOException {
        Writer out = new BufferedWriter( new FileWriter( classFile, false ) );
        out.write( "# Saved by ErmineJ " + new Date() + " \n" );
        return out;
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
    private static GeneSet readOneSet( BufferedReader dis, StatusViewer m ) throws IOException {
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
                    String symbol = fields[i];
                    if ( StringUtils.isBlank( symbol ) ) continue;
                    Gene g = geneData.findGene( symbol );
                    if ( g != null ) newSet.addGene( g );
                }
                newSet.setFormat( GeneSetFileFormat.LINE_BASED );
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
                    m.showError( "Unknown data type '" + type
                            + "' for group; In this format each group must start with 'probe' or 'gene'" );
                    return null;
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
                newSet.setFormat( GeneSetFileFormat.DEFAULT );
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
            m.showError( "Some genes in the custom sets not recognized" );
        } else if ( newSet.getProbes().isEmpty() ) {
            m.showError( "No genes for " + newSet.getId() + " match current annotations" );
        }

        newSet.getTerm().setAspect( GeneSetTerms.USER_DEFINED );

        return newSet;
    }

    private static boolean restoreBackup( File originalFile, File backup ) {
        return backup.renameTo( originalFile );
    }

    private static File saveBackup( Collection<GeneSet> sets ) throws IOException {
        File backup = File.createTempFile( "ermineJ.set.backup.", ".txt" );
        Writer out = initOutputFile( backup.getAbsolutePath() );
        for ( GeneSet s : sets ) {
            writeSet( s, out );
        }
        out.close();
        return backup;
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
    private static void writeSet( GeneSet set, Writer out ) throws IOException {
        String cleanedDescription = set.getTerm().getName().replaceAll( "[\n\t]+", " " );
        String filetype = ( set.isGenes() ) ? "gene" : "probe";

        if ( set.getFormat() == GeneSetFileFormat.DEFAULT ) {
            out.write( "# " + set + "\n" );
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
            out.write( "====\n" );
        } else {
            out.write( set.getId() + "\t" + set.getName() );
            for ( Gene g : set.getGenes() ) {
                out.write( "\t" + g.getSymbol() );
            }
            out.write( "\n" );
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
            GeneSet newSet = readOneSet( dis, new StatusStderr() );
            if ( newSet == null ) {
                log.warn( "Set was not read" );
                continue;
            }
            result.add( newSet );
        }
        dis.close();
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
    static Collection<GeneSet> loadUserGeneSetFile( String fileName, StatusViewer m ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );

        Collection<GeneSet> result = new HashSet<GeneSet>();

        while ( dis.ready() ) {
            GeneSet newSet = readOneSet( dis, m );

            if ( newSet == null ) {
                m.showError( "Set was not read from " + fileName );
                continue;
            }
            newSet.setSourceFile( fileName );
            result.add( newSet );
        }
        dis.close();

        return result;
    }

    protected UserDefinedGeneSetManager() {

    }

}