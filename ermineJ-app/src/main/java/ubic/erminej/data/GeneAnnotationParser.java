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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.FileTools;
import ubic.basecode.util.StatusStderr;
import ubic.basecode.util.StatusViewer;
import ubic.basecode.util.StringUtil;
import ubic.erminej.SettingsHolder;

/**
 * * Reads tab-delimited file to create maps of probes to classes, classes to probes, probes to genes, genes to probes.
 * 
 * @author paul
 * @version $Id$
 */
public class GeneAnnotationParser {

    private static final int LINES_READ_UPDATE_FREQ = 2500;

    public enum Format {
        DEFAULT, AFFYCSV, AGILENT, SIMPLE
    }

    private static Log log = LogFactory.getLog( GeneAnnotationParser.class.getName() );

    /**
     * String used to indicate a gene has no description associated with it.
     */
    private static final String NO_DESCRIPTION = "[No description]";

    private boolean filterNonSpecific = false;

    private GeneSetTerms geneSetTerms;

    private StatusViewer messenger = new StatusStderr();

    private static Pattern pipePattern = Pattern.compile( "\\s*[\\s\\|,]\\s*" );

    public GeneAnnotationParser( GeneSetTerms geneSets ) {
        this.geneSetTerms = geneSets;
    }

    public GeneAnnotationParser( GeneSetTerms geneSets, StatusViewer messenger ) {
        this( geneSets );
        if ( messenger != null ) this.messenger = messenger;
    }

    /**
     * @param i
     * @param format
     * @param settings
     * @return
     * @throws IOException
     */
    public GeneAnnotations read( InputStream i, Format format, SettingsHolder settings ) throws IOException {
        warned = false;
        if ( i == null ) {
            throw new IOException( "Inputstream was null" );
        }

        if ( i.available() == 0 ) {
            throw new IOException( "No bytes to read from the annotation file." );
        }

        GeneAnnotations result = null;
        switch ( format ) {
            case DEFAULT:
                result = this.readDefault( i, settings, false );
                break;
            case AFFYCSV:
                result = this.readAffyCsv( i, settings );
                break;
            case AGILENT:
                result = this.readAgilent( i, settings );
                break;
            case SIMPLE:
                result = this.readDefault( i, settings, true );
            default:
                throw new IllegalStateException();
        }

        return result;
    }

    /**
     * Fixme, all this needs is the settings.
     * 
     * @param fileName
     * @param format
     * @param settings
     * @return
     * @throws IOException
     */
    public GeneAnnotations read( String fileName, Format format, SettingsHolder settings ) throws IOException {
        InputStream i = FileTools.getInputStreamFromPlainOrCompressedFile( fileName );
        return this.read( i, format, settings );
    }

    /**
     * @param classIds
     */
    private Collection<GeneSetTerm> extractPipeDelimitedGoIds( String classIds ) {
        String[] classIdAry = pipePattern.split( classIds );

        Collection<GeneSetTerm> result = new HashSet<GeneSetTerm>();
        if ( classIdAry.length == 0 ) return result;
        for ( String go : classIdAry ) {

            GeneSetTerm goterm = this.geneSetTerms.get( go );
            if ( goterm == null ) {
                continue;
            }

            result.add( goterm );
        }
        return result;
    }

    /**
     * @param go
     * @return
     */
    private String padGoTerm( String go ) {
        String goPadded = go;
        if ( !goPadded.startsWith( "GO:" ) ) {
            int needZeros = 7 - goPadded.length();
            for ( int j = 0; j < needZeros; j++ ) {
                goPadded = "0" + goPadded;
            }
            goPadded = "GO:" + goPadded;
        }
        return goPadded;
    }

    boolean warned = false;

    /**
     * @param pat
     * @param goi
     * @return the GeneSetTerm (from the canonical list provided to the constructor)
     */
    private GeneSetTerm parseGoTerm( Pattern pat, String goi ) {
        Matcher mat = pat.matcher( goi );
        if ( !mat.find() ) {
            return null;
        }
        int start = mat.start();
        int end = mat.end();
        String go = goi.substring( start, end );

        go = padGoTerm( go );

        assert go.matches( "GO:[0-9]{7}" ) : "Trying to fix up : " + goi;

        GeneSetTerm goterm = this.geneSetTerms.get( go );

        if ( goterm == null ) {
            log.warn( "GO term " + go + " not recognized" );
            if ( messenger != null && !warned ) {
                messenger.showStatus( "GO term " + go
                        + " not recognized in the annotation file; further warnings suppressed" );
                warned = true;
            }
            return null;
        }
        // log.info( goterm );
        return goterm;

    }

    /**
     * @param bis
     */
    private GeneAnnotations readAffyCsv( InputStream bis, SettingsHolder settings ) throws IOException {
        return this.readAffyCsv( bis, null, settings );
    }

    private GeneAnnotations readAgilent( InputStream bis, SettingsHolder settings ) throws IOException {
        return this.readAgilent( bis, null, settings );
    }

    /**
     * @param bis
     * @param settings
     * @param simple
     * @return
     * @throws IOException
     */
    private GeneAnnotations readDefault( InputStream bis, SettingsHolder settings, boolean simple ) throws IOException {
        return this.readDefault( bis, null, settings, simple );
    }

    /**
     * Parse affy formatted files (CSV or tabbed should be okay)
     * 
     * @param bis
     * @param object
     */
    protected GeneAnnotations readAffyCsv( InputStream bis, Set<String> activeGenes, SettingsHolder settings )
            throws IOException {
        if ( bis == null ) {
            throw new IOException( "Inputstream was null" );
        }
        warned = false;
        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        String classIds = null;
        Map<String, Gene> genes = new HashMap<String, Gene>();

        /*
         * Skip comment lines (new format)
         */
        String line = "";
        while ( ( line = dis.readLine() ) != null ) {
            /* line is blank, or starts with "#" , keep reading */
            if ( !StringUtils.isBlank( line ) && !line.matches( "^#.+" ) ) {
                break;
            }
        }

        String header = line;

        if ( StringUtils.isBlank( header ) ) {
            throw new IOException( "File had no header" );
        }

        int numFields = ParserHelper.getAffyNumFields( header );
        int probeIndex = ParserHelper.getAffyProbeIndex( header );
        int goBpIndex = ParserHelper.getAffyBpIndex( header );
        int goCcIndex = ParserHelper.getAffyCcIndex( header );
        int goMfIndex = ParserHelper.getAffyMfIndex( header );
        int geneNameIndex = ParserHelper.getAffyGeneNameIndex( header );
        int geneSymbolIndex = ParserHelper.getAffyGeneSymbolIndex( header );

        int alternateGeneSymbolIndex = ParserHelper.getAffyAlternateGeneSymbolIndex( header );

        if ( probeIndex < 0 ) {
            throw new IllegalStateException( "Invalid AFFY file format: could not find the probe set id column" );
        }
        if ( geneNameIndex < 0 ) {
            throw new IllegalStateException( "Invalid AFFY file format: could not find the gene name column" );
        }
        if ( geneSymbolIndex < 0 ) {
            throw new IllegalStateException( "Invalid AFFY file format: could not find the gene symbol column" );
        }

        if ( goBpIndex < 0 ) {
            throw new IllegalStateException( "Invalid AFFY file format: No biological process data were found" );
        } else if ( goCcIndex < 0 ) {
            throw new IllegalStateException( "Invalid AFFY file format: No cellular component data were found" );
        } else if ( goMfIndex < 0 ) {
            throw new IllegalStateException( "Invalid AFFY file format: No molecular function data were found" );
        }

        log.debug( "Read header" );

        assert ( numFields > probeIndex + 1 && numFields > geneSymbolIndex + 1 );

        /*
         * This pattern is designed to work with old or new formats (old was not padded to 7 digits and lacks "GO:").
         */
        Pattern pat = Pattern.compile( "(GO:)?[0-9]{1,7}$" );

        // loop through rows. Makes hash map of probes to go, and map of go to
        // probes.
        int n = 0;

        log.debug( "File opened okay, parsing Affy annotation file" );

        while ( ( line = dis.readLine() ) != null ) {

            /*
             * New files are tabbed...
             */
            String[] fields = null;

            if ( line.matches( ".+\t.+" ) ) {
                fields = StringUtils.splitPreserveAllTokens( line, '\t' );
            } else {
                fields = StringUtil.csvSplit( line );
            }

            if ( fields.length < probeIndex + 1 || fields.length < geneSymbolIndex + 1 ) {
                continue; // skip lines that don't meet criteria.
            }

            String probeId = fields[probeIndex];

            if ( probeId.matches( "AFFX.*" ) ) {
                continue;
            }

            String geneSymbol = fields[geneSymbolIndex];

            if ( StringUtils.isBlank( probeId ) || probeId.equals( "---" ) ) {
                throw new IllegalStateException( "Probe name was missing or invalid at line " + n
                        + "; it is possible the file format is not readable; contact the developers." );
            }

            if ( StringUtils.isBlank( geneSymbol ) || geneSymbol.equals( "---" ) ) {
                geneSymbol = fields[alternateGeneSymbolIndex];
                if ( StringUtils.isBlank( geneSymbol ) || geneSymbol.equals( "---" ) ) {
                    continue;
                }
            }

            if ( activeGenes != null && !activeGenes.contains( geneSymbol ) ) {
                continue;
            }

            /* read gene description */

            String description = fields[geneNameIndex];
            if ( description.equals( "---" ) ) description = NO_DESCRIPTION;

            Probe probe = new Probe( probeId, description );
            Gene gene;
            if ( genes.containsKey( geneSymbol ) ) {
                gene = genes.get( geneSymbol );
            } else {
                gene = new Gene( geneSymbol, description );
                genes.put( geneSymbol, gene );
            }
            gene.addProbe( probe );
            probe.setGene( gene );

            classIds = " // " + fields[goBpIndex] + " // " + fields[goMfIndex] + " // " + fields[goCcIndex];
            String[] goinfo = classIds.split( "/+" );
            for ( String goi : goinfo ) {
                GeneSetTerm goTerm = parseGoTerm( pat, StringUtils.strip( goi ) );

                if ( goTerm == null ) continue;
                // log.info( gene + " " + goTerm );
                gene.addGeneSet( goTerm );
                probe.addToGeneSet( goTerm ); // redundant
            }

            if ( messenger != null && n % LINES_READ_UPDATE_FREQ == 0 ) {
                messenger.showProgress( "Read " + n + " probes" );
                try {
                    Thread.sleep( 10 );
                } catch ( InterruptedException e ) {
                    dis.close();
                    throw new RuntimeException( "Interrupted" );
                }
            }
            n++;
        }

        dis.close();

        if ( genes.isEmpty() ) {
            throw new IllegalStateException( "There were no genes found in the annotation file." );
        }

        GeneAnnotations result = new GeneAnnotations( genes.values(), geneSetTerms, settings, messenger );

        if ( result.numProbes() == 0 ) {
            throw new IllegalArgumentException(
                    "The gene annotations had invalid information. Please check the format." );
        }

        return result;
    } // AFFY CSV

    /**
     * @param bis
     * @param settings
     * @throws IOException
     */
    protected GeneAnnotations readAgilent( InputStream bis, Set<String> activeGenes, SettingsHolder settings )
            throws IOException {
        if ( bis == null ) {
            throw new IOException( "Inputstream was null" );
        }
        warned = false;
        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        String classIds = null;
        Map<String, Gene> genes = new HashMap<String, Gene>();

        String header = dis.readLine();
        int numFields = ParserHelper.getAgilentNumFields( header );
        int probeIndex = ParserHelper.getAgilentProbeIndex( header );
        int goIndex = ParserHelper.getAgilentGoIndex( header );
        int geneNameIndex = ParserHelper.getAgilentGeneNameIndex( header );
        int geneSymbolIndex = ParserHelper.getAgilentGeneSymbolIndex( header );

        assert ( numFields > probeIndex + 1 && numFields > geneSymbolIndex + 1 );
        Pattern pat = Pattern.compile( "[0-9]+" );

        int n = 0;
        String line = "";
        while ( ( line = dis.readLine() ) != null ) {

            String[] fields = StringUtils.splitPreserveAllTokens( line, '\t' );
            if ( fields.length < probeIndex + 1 || fields.length < geneSymbolIndex + 1 ) {
                continue; // skip lines that don't meet criteria.
            }

            String probeId = fields[probeIndex];
            String geneSymbol = fields[geneSymbolIndex];
            String geneName = fields[geneNameIndex];

            Probe probe = new Probe( probeId, geneName );

            Gene gene;
            if ( genes.containsKey( geneSymbol ) ) {
                gene = genes.get( geneSymbol );
            } else {
                gene = new Gene( geneSymbol, geneName );
                genes.put( geneSymbol, gene );
            }
            gene.addProbe( probe );
            probe.setGene( gene );

            if ( activeGenes != null && !activeGenes.contains( gene ) ) {
                genes.remove( gene.getSymbol() );
                continue;
            }

            if ( fields.length < goIndex + 1 ) {
                continue;
            }

            classIds = fields[goIndex];

            if ( StringUtils.isNotBlank( classIds ) ) {
                String[] goinfo = classIds.split( "\\|" );
                for ( String element : goinfo ) {
                    String goi = element.intern();
                    GeneSetTerm term = parseGoTerm( pat, goi );
                    if ( term == null ) continue;
                    gene.addGeneSet( term );
                    probe.addToGeneSet( term ); // redundant
                }

            }

            if ( messenger != null && n % LINES_READ_UPDATE_FREQ == 0 ) {
                messenger.showProgress( "Read " + n + " probes" );
                try {
                    Thread.sleep( 10 );
                } catch ( InterruptedException e ) {
                    dis.close();
                    throw new RuntimeException( "Interrupted" );
                }
            }
            n++;
        }

        /* Fill in the genegroupreader and the classmap */
        dis.close();

        if ( genes.isEmpty() ) {
            throw new IllegalStateException( "There were no genes found in the annotation file." );
        }

        GeneAnnotations result = new GeneAnnotations( genes.values(), geneSetTerms, settings, messenger );

        if ( result.numProbes() == 0 ) {
            throw new IllegalArgumentException(
                    "The gene annotations had invalid information. Please check the format." );
        }

        return result;
    } // Agilent

    /**
     * Main default reading method. Because we want to be tolerant of file format variation, headers are not treated
     * differently than other rows. This extra "gene" can be counted in things like rankings, so we do try to avoid it
     * if we can, but doesn't directly effect most calculations.
     * 
     * @param bis
     * @param genes
     * @param settings
     * @param simple If true, assume two-column format with GO terms pipe-delimited in column 2, and only one gene per
     *        row (no ABC|ABC2 stuff)
     * @throws IOException
     */
    public GeneAnnotations readDefault( InputStream bis, Collection<Gene> activeGenes, SettingsHolder settings,
            boolean simple ) throws IOException {

        BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );
        Map<String, Gene> genes = new HashMap<String, Gene>();
        warned = false;
        int n = 0;
        String line = "";
        while ( ( line = dis.readLine() ) != null ) {

            // check for things that don't look like data, or other skippable things.
            if ( line.startsWith( "#" ) ) continue;
            if ( n == 0 && ( line.toLowerCase().startsWith( "probe" ) || line.toLowerCase().startsWith( "gene" ) ) ) {
                ++n;
                continue;
            }

            String[] tokens = line.split( "\t" );
            int numTokens = tokens.length;
            if ( numTokens < 2 ) continue;
            String geneName = "";
            String probeId = "";
            String description = NO_DESCRIPTION;
            if ( simple ) {
                geneName = tokens[0];
                probeId = geneName;
            } else {
                probeId = tokens[0];
                if ( probeId.matches( "AFFX.*" ) ) {
                    continue;
                }
                geneName = tokens[1];

                // correctly deal with things like Nasp|Nasp or Nasp|Nasp2
                if ( geneName.matches( ".+?[\\|,].+?" ) ) {
                    String[] multig = geneName.split( "[\\|,]" );
                    Collection<String> multigenes = new HashSet<String>();
                    for ( String g : multig ) {
                        // log.debug( g + " found in " + geneName );
                        multigenes.add( g );
                    }

                    if ( multigenes.size() > 1 && filterNonSpecific ) {
                        continue;
                    }

                    geneName = multigenes.iterator().next();
                }
                /* read gene description */
                if ( numTokens >= 3 ) {
                    description = tokens[2];
                } else {
                    description = NO_DESCRIPTION;
                }
            }

            Probe probe = new Probe( probeId, description );

            Gene gene;
            if ( genes.containsKey( geneName ) ) {
                gene = genes.get( geneName );
            } else {
                gene = new Gene( geneName, description );
                genes.put( geneName, gene );
            }

            gene.addProbe( probe );
            probe.setGene( gene );

            if ( activeGenes != null && !activeGenes.contains( gene ) ) {
                genes.remove( gene.getSymbol() );
                continue;
            }

            /* read GO data */
            if ( simple ) {
                String classIds = tokens[1];
                Collection<GeneSetTerm> goTerms = extractPipeDelimitedGoIds( classIds );
                for ( GeneSetTerm term : goTerms ) {
                    gene.addGeneSet( term );
                    probe.addToGeneSet( term );
                }
            } else {
                if ( numTokens >= 4 ) {
                    String classIds = tokens[3];
                    Collection<GeneSetTerm> goTerms = extractPipeDelimitedGoIds( classIds );

                    for ( GeneSetTerm term : goTerms ) {
                        gene.addGeneSet( term );
                        probe.addToGeneSet( term );
                    }
                }

                if ( numTokens >= 6 ) {
                    // Additional columns are ignored. However, new annotation files have the Gemma and NCBI gene ids.
                    String ncbiID = tokens[5];
                    try {
                        gene.setNcbiId( Integer.parseInt( ncbiID ) );
                    } catch ( NumberFormatException e ) {
                        // no big deal
                    }
                }
            }

            if ( messenger != null && ++n % LINES_READ_UPDATE_FREQ == 0 ) {
                messenger.showProgress( "Read " + n + " probes" );
                try {
                    Thread.sleep( 20 );
                } catch ( InterruptedException e ) {
                    dis.close();
                    throw new CancellationException();
                }
            }
        }
        dis.close();
        if ( genes.isEmpty() ) {
            throw new IllegalStateException( "There were no genes found in the annotation file." );
        }

        GeneAnnotations result = new GeneAnnotations( genes.values(), geneSetTerms, settings, messenger );

        return result;
    }

    public void setFilterNonSpecific( boolean b ) {
        this.filterNonSpecific = b;
    }
}

class ParserHelper {
    private static final String AFFY_FIELD_SEPARATION_REGEX = "[,\t]";

    /**
     * @param limit
     * @param header
     * @param pattern
     */
    public static int findField( String header, String sep, String pattern ) {
        String[] fields = header.split( sep );
        if ( fields == null || fields.length == 0 ) throw new IllegalArgumentException( "No header!" );
        for ( int i = 0; i < fields.length; i++ ) {
            if ( fields[i].replaceAll( "\"", "" ).matches( pattern ) ) {
                return i;
            }
        }
        return -1;
    }

    public static int getAffyAlternateGeneSymbolIndex( String header ) {

        String[] alternates = new String[] { "Transcript ID", "Transcript ID(Array Design)", "UniGene ID", "swissprot",
                "unigene" };
        for ( String pattern : alternates ) {
            int i = findField( header, AFFY_FIELD_SEPARATION_REGEX, pattern );
            if ( i >= 0 ) return i;
        }
        return -1;
    }

    /**
     * @throws IOException
     * @param header
     * @return
     */
    public static int getAffyBpIndex( String header ) throws IOException {
        String pattern = "(Gene Ontology Biological Process|GO_biological_process)";
        return findField( header, AFFY_FIELD_SEPARATION_REGEX, pattern );
    }

    /**
     * @throws IOException
     * @param header
     * @return
     */
    public static int getAffyCcIndex( String header ) throws IOException {
        String pattern = "(Gene Ontology Cellular Component|GO_cellular_component)";
        return findField( header, AFFY_FIELD_SEPARATION_REGEX, pattern );
    }

    /**
     * @throws IOException
     * @param header
     * @return
     */
    public static int getAffyGeneNameIndex( String header ) throws IOException {
        String pattern = "(Gene Title|gene_assignment)";
        return findField( header, AFFY_FIELD_SEPARATION_REGEX, pattern );
    }

    /**
     * @throws IOException
     * @param header
     * @return
     */
    public static int getAffyGeneSymbolIndex( String header ) throws IOException {
        String pattern = "(Gene Symbol|gene_assignment)";
        return findField( header, AFFY_FIELD_SEPARATION_REGEX, pattern );
    }

    /**
     * @throws IOException
     * @param header
     * @return
     */
    public static int getAffyMfIndex( String header ) throws IOException {
        String pattern = "(Gene Ontology Molecular Function|GO_molecular_function)";
        return findField( header, AFFY_FIELD_SEPARATION_REGEX, pattern );
    }

    /**
     * @param header
     * @return
     */
    public static int getAffyNumFields( String header ) {

        String[] fields = header.split( AFFY_FIELD_SEPARATION_REGEX );
        return fields.length;
    }

    /**
     * @throws IOException
     * @param header
     * @return
     */
    public static int getAffyProbeIndex( String header ) throws IOException {
        String pattern = "(Probe Set ID|probeset_id)";
        return findField( header, AFFY_FIELD_SEPARATION_REGEX, pattern );
    }

    /**
     * @param header
     * @return
     */
    public static int getAgilentGeneNameIndex( String header ) {
        String pattern = "GeneName";
        return findField( header, "\t", pattern );
    }

    /**
     * @param header
     * @return
     */
    public static int getAgilentGeneSymbolIndex( String header ) {
        String pattern = "GeneSymbol";
        return findField( header, "\t", pattern );
    }

    /**
     * @param header
     * @return
     */
    public static int getAgilentGoIndex( String header ) {
        String pattern = "GO";
        return findField( header, "\t", pattern );
    }

    /**
     * @param header
     * @return
     */
    public static int getAgilentNumFields( String header ) {
        String[] fields = header.split( "\t" );
        return fields.length;
    }

    /**
     * @param header
     * @return
     */
    public static int getAgilentProbeIndex( String header ) {
        String pattern = "ProbeID";
        return findField( header, "\t", pattern );
    }

}
