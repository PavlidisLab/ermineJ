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
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.StopWatch;
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

    private static final int MAX_WARNINGS = 1;

    private StatusViewer statusMessenger = new StatusStderr();

    private Settings settings;

    private int numTimesWarnedOfProblems = 0;

    /**
     * The original
     */
    private GeneAnnotations geneData;

    public UserDefinedGeneSetManager( GeneAnnotations annots, Settings settings, StatusViewer messenger ) {
        if ( messenger != null ) this.statusMessenger = messenger;
        assert settings != null;
        init( annots, settings );
    }

    /**
     * Delete a user-defined gene set from disk and from the annotations in memory.
     * <p>
     * 
     * @param ngs
     */
    public boolean deleteUserGeneSet( GeneSetTerm termToDelete ) {
        GeneSet geneSet = geneData.getGeneSet( termToDelete );

        String classFile = geneSet.getSourceFile();

        if ( StringUtils.isBlank( geneSet.getSourceFile() ) ) {
            classFile = getUserGeneSetFileForName( termToDelete.getId() );
        }

        File file = new File( classFile );

        if ( !file.exists() ) {
            statusMessenger.showError( file.getAbsoluteFile() + " does not exist" );
            return false;
        }
        if ( !file.canWrite() ) {
            statusMessenger.showError( "Cannot delete " + file.getAbsoluteFile() + ": file is not editable" );
            return false;
        }

        File backup = null;
        boolean okToDeleteBackup = true;

        try {

            Collection<GeneSet> sets = loadUserGeneSetFile( classFile );

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
     * Read in a list of genes or probe ids from a file. The list of genes is unadorned, one per row.
     * 
     * @param fileName
     * @return incomplete gene set. The caller has to arrange for this to be finished.
     * @throws IOException
     */
    public GeneSet loadPlainGeneList( String fileName ) throws IOException {
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

        if ( genes.isEmpty() && numTimesWarnedOfProblems < MAX_WARNINGS ) {
            statusMessenger.showError( "None of the items in your file matched the current annotations." );
            numTimesWarnedOfProblems++;
            return null;
        }

        if ( probesNotFound && numTimesWarnedOfProblems < MAX_WARNINGS ) {
            statusMessenger
                    .showError( "At least one gene in your set had no matching identifiers in the current annotations." );
            numTimesWarnedOfProblems++;
        }

        GeneSet result = new GeneSet();
        result.setGenes( genes );
        result.setSourceFile( null ); // if we rewrite it, it will be a new file.
        result.setUserDefined( true );
        result.getTerm().setAspect( GeneSetTerms.USER_DEFINED );
        return result;
    }

    /**
     * Write a gene set to disk, in the directory set in the preferences. Use for update or create.
     * 
     * @param type
     * @throws IOException
     */
    public void saveGeneSet( GeneSet setToSave ) throws IOException {
        if ( !setToSave.isUserDefined() ) {
            throw new IllegalArgumentException( "Only user-defined sets can be saved, attempted to change "
                    + setToSave.getId() );
        }

        if ( geneData.hasGeneSet( setToSave.getTerm() ) ) {
            this.statusMessenger.showStatus( "Updating " + setToSave );
        }

        String fileName = null;
        if ( StringUtils.isNotBlank( setToSave.getSourceFile() ) ) {
            fileName = setToSave.getSourceFile(); // reuse it.
        } else {
            // make a new file
            fileName = getUserGeneSetFileForName( setToSave.getId() );
        }

        /*
         * Handle case of multiple groups per file. We re-write it, clobber the file.
         */
        if ( ( new File( fileName ) ).canRead() ) {
            Collection<GeneSet> sets = loadUserGeneSetFile( fileName );

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
    private String cleanGeneSetName( String id ) {
        String fileid = id.replaceAll( ":", "-" );
        fileid = fileid.replaceAll( "\\s+", "_" );
        return fileid;
    }

    /**
     * @param dir
     * @param className
     * @return
     */
    private String getUserGeneSetFileForName( String id ) {
        String classFile = settings.getUserGeneSetDirectory() + System.getProperty( "file.separator" )
                + cleanGeneSetName( id ) + USERGENESET_SUFFIX;
        return classFile;
    }

    /**
     * Usually this would be called only once per application run. The annotations kept here are the 'canonical' ones.
     * 
     * @param gd
     * @param set
     */
    private void init( GeneAnnotations gd, Settings set ) {
        geneData = gd;
        settings = set;
        loadUserGeneSets();
    }

    /**
     * Open file for writing, add header.
     * 
     * @param classFile
     * @return
     * @throws IOException
     */
    private Writer initOutputFile( String classFile ) throws IOException {
        Writer out = new BufferedWriter( new FileWriter( classFile, false ) );
        out.write( "# Saved by ErmineJ " + new Date() + " \n" );
        return out;
    }

    /**
     * @param ngs
     * @return
     */
    private boolean isExistingGeneSet( GeneSetTerm id1 ) {
        return geneData.hasGeneSet( id1 );
    }

    /**
     * Load the user-defined gene sets.
     */
    private void loadUserGeneSets() {

        StopWatch timer = new StopWatch();
        timer.start();

        File userGeneSetDir = new File( settings.getUserGeneSetDirectory() );
        if ( !userGeneSetDir.exists() ) {
            statusMessenger.showError( "No custom gene set directory found, none will be loaded - looked for "
                    + settings.getUserGeneSetDirectory() );
            return;
        }

        String[] classFiles = userGeneSetDir.list();

        if ( classFiles.length == 0 ) {
            statusMessenger.showStatus( "No gene sets found in " + settings.getUserGeneSetDirectory() );
            return;
        }

        int numLoaded = 0;

        for ( int i = 0; i < classFiles.length; i++ ) {

            String classFile = classFiles[i];
            if ( StringUtils.isEmpty( classFile ) ) {
                continue;
            }

            String classFilePath = userGeneSetDir + System.getProperty( "file.separator" ) + classFile;
            statusMessenger.showStatus( "Loading gene sets from: " + classFilePath );

            try {

                Collection<GeneSet> loadedSets = loadUserGeneSetFile( classFilePath );
                numLoaded += loadedSets.size();

                if ( !loadedSets.isEmpty() ) {
                    Collection<String> customGeneSetFiles = settings.getCustomGeneSetFiles();
                    customGeneSetFiles.add( classFilePath );
                    settings.setCustomGeneSetFiles( customGeneSetFiles );
                }

                for ( GeneSet set : loadedSets ) {
                    GeneSetTerm id = set.getTerm();

                    if ( set.getGenes().isEmpty() ) {
                        statusMessenger.showError( "Gene set has no genes matching annotations (" + id + ")" );
                    } else if ( isExistingGeneSet( id ) ) {
                        statusMessenger.showError( "Gene set IDs must be unique, please rename it (" + id + ")" );
                    } else {
                        geneData.addGeneSet( set.getTerm(), set.getGenes(), classFile );
                    }
                }

            } catch ( IOException e ) {
                // This error will be shown if there are files that don't fit the format.
                statusMessenger.showError( "Could not load gene sets from " + classFilePath + ": " + e.getMessage() );

            }

            if ( numLoaded > 0 && numLoaded % 100 == 0 ) {
                statusMessenger.showStatus( "Loading custom sets: " + numLoaded );
            }

            statusMessenger.clear();
        }

        if ( statusMessenger != null && numLoaded > 0 )
            statusMessenger.showStatus( "Loaded " + numLoaded + " customized gene sets from " + classFiles.length
                    + " files." );
        log.info( "Load user sets: " + timer.getTime() + "ms" );
    }

    /**
     * @param dis
     * @return
     * @throws IOException
     */
    private GeneSet readOneSet( BufferedReader dis ) throws IOException {
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
                 * We assume there is one record per row; includes MolSigDB (which also distributes a representation of
                 * KEGG) gene symbol files.
                 */
                String geneSetID = fields[0];
                String geneSetName = fields[1];

                /*
                 * Pretty-up the name (use ID if it's better)
                 */
                if ( geneSetName.contains( "broadinstitute.org" ) ) {
                    geneSetName = WordUtils.capitalize( StringUtils.lowerCase( geneSetID ) ).replaceAll( "_", " " );
                } else {
                    // Future: add additional case-specific cleanups. Worse case: blank.
                }

                newSet = new GeneSet( new GeneSetTerm( geneSetID, geneSetName ) );
                newSet.setIsGenes( true );
                newSet.setUserDefined( true );
                for ( int i = 2; i < fields.length; i++ ) {
                    String symbol = fields[i];
                    if ( StringUtils.isBlank( symbol ) ) continue;

                    if ( symbol.contains( "///" ) ) {
                        // MolSigDb has this; Let's skip, in agreement with GSEA:
                        // "This symbol indicates ambiguous mapping according to the Affymentrix conventions and serves
                        // as a field separator when a probe set id corresponds to several gene symbols. /// may
                        // appear in some gene sets curated form Affymetrix (NetAffx) annotation data. GSEA ignores such
                        // genes."
                        continue;
                    }

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
                    statusMessenger.showError( "Unknown data type '" + type
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
                    log.warn( "Gene " + row + " not recognized, skipping" );
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
        } // end of iteration over lines

        if ( newSet == null ) {
            return null;
        }

        if ( hasUnknownProbes ) {
            /*
             * We could add these probes (and genes). This would release users from having to have the annotation file,
             * but I don't think it's that big of a deal.
             */
            if ( numTimesWarnedOfProblems < MAX_WARNINGS ) {
                statusMessenger.showError( "Some genes in the custom sets not recognized; further warnings suppressed" );
                numTimesWarnedOfProblems++;
            }
            // } else if ( newSet != null && newSet.getProbes().isEmpty() ) {
        } else if ( newSet.getProbes().size() < 2 ) {
            if ( numTimesWarnedOfProblems < MAX_WARNINGS ) {
                statusMessenger.showError( "No genes for " + newSet.getId()
                        + " match current annotations; further warnings suppressed" );
                numTimesWarnedOfProblems++;
            }
            return null;
        }

        newSet.getTerm().setAspect( GeneSetTerms.USER_DEFINED );

        return newSet;
    }

    private boolean restoreBackup( File originalFile, File backup ) {
        return backup.renameTo( originalFile );
    }

    private File saveBackup( Collection<GeneSet> sets ) throws IOException {
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
    private BufferedReader setUpToLoad( String fileName ) throws IOException, FileNotFoundException {
        FileTools.checkPathIsReadableFile( fileName );
        FileInputStream fis = new FileInputStream( fileName );
        BufferedInputStream bis = new BufferedInputStream( fis );
        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        return dis;
    }

    /**
     * The set will be written in the format set (geneSet.getFormat())
     * 
     * @param geneeSet
     * @param out
     * @throws IOException
     */
    private void writeSet( GeneSet geneeSet, Writer out ) throws IOException {
        String cleanedDescription = geneeSet.getTerm().getName().replaceAll( "[\r\n\t]+", " " );
        String filetype = ( geneeSet.isGenes() ) ? "gene" : "probe";

        if ( geneeSet.getFormat() == GeneSetFileFormat.DEFAULT ) {
            out.write( "# " + geneeSet + "\n" );
            out.write( filetype + "\n" );
            out.write( geneeSet.getId() + "\n" );
            out.write( cleanedDescription + "\n" );
            if ( geneeSet.isGenes() ) {
                for ( Gene g : geneeSet.getGenes() ) {
                    out.write( g.getSymbol() + "\n" );
                }
            } else {
                for ( Probe p : geneeSet.getProbes() ) {
                    out.write( p.getName() + "\n" );
                }
            }
            out.write( "====\n" );
        } else {
            out.write( geneeSet.getId() + "\t" + geneeSet.getName() );
            for ( Gene g : geneeSet.getGenes() ) {
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
    protected Collection<GeneSet> loadUserGeneSetFile( InputStream is ) throws IOException {
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
     * Alternatively, a tab-delimited file can be provided with one group per row, with the following format (which it
     * turns out MolSigDB uses):
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
    Collection<GeneSet> loadUserGeneSetFile( String fileName ) throws IOException {
        BufferedReader dis = setUpToLoad( fileName );

        Collection<GeneSet> result = new HashSet<GeneSet>();

        while ( dis.ready() ) {
            GeneSet newSet = readOneSet( dis );

            if ( newSet == null ) {
                continue;
            }
            newSet.setSourceFile( fileName );
            result.add( newSet );
        }
        dis.close();

        return result;
    }

}