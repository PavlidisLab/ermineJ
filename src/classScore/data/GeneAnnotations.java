package classScore.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Reads tab-delimited file to create maps of probes to classes, classes to probes, probes to genes, genes to probes.
 * <p>
 * Maintains the following important data structures, all derived from the input file:
 * 
 * <pre>
 *           probe-&gt;Classes -- each value is a Set of the Classes that a probe belongs to.
 *           Classes-&gt;probe -- each value is a Set of the probes that belong to a class
 *           probe-&gt;gene -- each value is the gene name corresponding to the probe.
 *           gene-&gt;list of probes -- each value is a list of probes corresponding to a gene
 *           probe-&gt;description -- each value is a text description of the probe (actually...of the gene)
 *
 * </pre>
 * 
 * <p>
 * Copyright (c) 2004 Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @author Shamhil Merchant
 * @author Homin Lee
 * @version $Id$
 */

public class GeneAnnotations {
   private Map probeToClassMap; //stores probe->Classes map
   private Map classToProbeMap; //stores Classes->probes map
   private Map probeToGeneName;
   private Map probeToDescription;
   private Map geneToProbeList;
   private Map classToGeneMap; //stores Classes->genes map. use to get a list
   // of classes.
   private Set probes;
   private Vector sortedGeneSets;
   private Map classesToRedundantMap;
   private Vector selectedProbes;
   private Vector selectedSets;

   /**
    * This is for creating GeneAnnotations by reading from a file
    * 
    * @param filename String
    * @param probes Map only include these probes
    * @throws IOException
    */
   public GeneAnnotations( String filename, Set probes ) throws IOException {
      if ( probes != null ) {
         this.probes = probes;
      }
      probeToClassMap = new LinkedHashMap();
      classToProbeMap = new LinkedHashMap();
      probeToGeneName = new HashMap();
      probeToDescription = new HashMap();
      geneToProbeList = new HashMap();
      classesToRedundantMap = new HashMap();
      this.readFile( filename );
      classToGeneMap = makeClassToGeneMap();
      GeneSetMapTools.collapseClasses(this);
      prune( 2, 1000 );
      resetSelectedProbes();
      resetSelectedSets();
      sortGeneSets();
   }

   /**
    * This is for creating GeneAnnotations by pruning a copy
    * 
    * @param geneData GeneAnnotations copy to prune from
    * @param probes Map only include these probes
    */
   public GeneAnnotations( GeneAnnotations geneData, Set probes ) {
      this.probes = probes;
      probeToClassMap = new LinkedHashMap( geneData.probeToClassMap );
      classToProbeMap = new LinkedHashMap( geneData.classToProbeMap );
      probeToGeneName = new HashMap( geneData.probeToGeneName );
      probeToDescription = new HashMap( geneData.probeToDescription );
      geneToProbeList = new HashMap( geneData.geneToProbeList );
      Vector probeList = new Vector( geneData.getProbeToGeneMap().keySet() ); // pp
      // changed
      // from
      // getting
      // the
      // probelist.
      classesToRedundantMap = new HashMap( geneData.classesToRedundantMap );

      Iterator it = probeList.iterator();
      while ( it.hasNext() ) {
         String probe = ( String ) it.next();
         if ( !probes.contains( probe ) ) { // remove probes not in data set.
            removeProbeFromMaps(probe);
         }
      }
      classToGeneMap = makeClassToGeneMap();
      prune( 2, 1000 );
      resetSelectedProbes();
      resetSelectedSets();
   }

   /**
    * @param filename String
    * @throws IOException
    */
   public GeneAnnotations( String filename ) throws IOException {
      this( filename, null );
   }

   /**
    * @return mapping of classes to genes.
    */
   private Map makeClassToGeneMap() {
      Map map = new HashMap();
      Iterator it = classToProbeMap.keySet().iterator();
      while ( it.hasNext() ) {
         String id = ( String ) it.next();
         HashSet genes = new HashSet();
         ArrayList probes = ( ArrayList ) classToProbeMap.get( id );
         Iterator probe_it = probes.iterator();
         while ( probe_it.hasNext() ) {
            genes.add( probeToGeneName.get( probe_it.next() ) );
         }
         map.put( id, genes );
      }
      return map;
   }

   //read in file
   private void readFile( String filename ) throws IOException {
      File infile = new File( filename );
      if ( !infile.exists() || !infile.canRead() ) {
         throw new IOException( "Could not read from " + filename );
      }

      FileInputStream fis = new FileInputStream( filename );

      BufferedInputStream bis = new BufferedInputStream( fis );
      BufferedReader dis = new BufferedReader( new InputStreamReader( bis ) );

      ArrayList probeIds = new ArrayList();
      String classIds = null;

      // loop through rows. Makes hash map of probes to go, and map of go to
      // probes.
      String line = "";
      while ( ( line = dis.readLine() ) != null ) {
         if ( line.startsWith( "#" ) ) continue;
         StringTokenizer st = new StringTokenizer( line, "\t" );

         String probe = st.nextToken().intern();
         if ( probes == null || probes.contains( probe ) ) { // only do probes
            // we have in the
            // data set.

            /* read gene name */
            String group = st.nextToken().intern();
            probeToGeneName.put( probe.intern(), group.intern() );

            // create the list if need be.
            if ( geneToProbeList.get( group ) == null ) {
               geneToProbeList.put( group.intern(), new ArrayList() );
            }
            ( ( ArrayList ) geneToProbeList.get( group ) ).add( probe.intern() );

            probeIds.add( probe );
            probeToClassMap.put( probe.intern(), new ArrayList() );

            /* read gene description */
            if ( st.hasMoreTokens() ) {
               String description = st.nextToken().intern();
               if ( !description.startsWith( "GO:" ) ) { // this happens when
                  // there is no
                  // desription and we
                  // skip to the GO
                  // terms.
                  probeToDescription.put( probe.intern(), description.intern() );
               } else {
                  probeToDescription.put( probe.intern(), "[No description]" );
               }
            } else {
               probeToDescription.put( probe.intern(), "[No description]" );
            }

            if ( st.hasMoreTokens() ) {
               classIds = st.nextToken();

               //another tokenizer is required since the ClassesID's are
               // seperated by the | character
               StringTokenizer st1 = new StringTokenizer( classIds, "|" );
               while ( st1.hasMoreTokens() ) {
                  String go = st1.nextToken().intern();

                  // add this go to the probe->go map.
                  ( ( ArrayList ) probeToClassMap.get( probe ) ).add( go );

                  // add this probe this go->probe map.
                  if ( !classToProbeMap.containsKey( go ) ) {
                     classToProbeMap.put( go, new ArrayList() );
                  }
                  ( ( ArrayList ) classToProbeMap.get( go ) ).add( probe );
               }
            }
         }
      }

      /* Fill in the genegroupreader and the classmap */
      dis.close();
      resetSelectedProbes();
   }

   // remove classes that have too few members
   // todo this doesn't affect the tree representation of the genesets.
   private void prune( int lowThreshold, int highThreshold ) {

      Set removeUs = new HashSet();
      for ( Iterator it = classToProbeMap.keySet().iterator(); it.hasNext(); ) {
         String id = ( String ) it.next();
         if ( numProbes( id ) < lowThreshold || numGenes( id ) < lowThreshold
               || numProbes( id ) > highThreshold
               || numGenes( id ) > highThreshold ) {
            removeUs.add( id );
         }
      }

      for ( Iterator it = removeUs.iterator(); it.hasNext(); ) {
         String id = ( String ) it.next();
         removeClassFromMaps(id);
      }

      sortGeneSets();
   }

   private void removeProbeFromMaps( String probe ) {
      if ( probeToGeneName.containsKey( probe ) ) {
         String gene = ( String ) probeToGeneName.get( probe );
         probeToGeneName.remove( probe );
         if ( geneToProbeList.containsKey( gene ) )
            ( ( ArrayList ) geneToProbeList.get( gene ) )
                .remove( probe );
      }
      if ( probeToClassMap.containsKey( probe ) ) {
         Iterator cit = ( ( ArrayList ) probeToClassMap.get( probe ) )
             .iterator();
         while ( cit.hasNext() ) {
            String geneSet = ( String ) cit.next();
            if ( classToProbeMap.containsKey( geneSet ) )
               ( ( ArrayList ) classToProbeMap.get( geneSet ) )
                   .remove( probe );
         }
         probeToClassMap.remove( probe );
      }
      if ( probeToDescription.containsKey( probe ) )
         probeToDescription.remove( probe );
   }

   public void removeClassFromMaps( String id ) {
      if ( classToProbeMap.containsKey( id ) ) {
         for ( Iterator pit = ( ( ArrayList ) classToProbeMap.get( id ) ).iterator();
               pit.hasNext(); ) {
            String probe = ( String ) pit.next();
            if ( probeToClassMap.containsKey( probe ) )
               if ( ( ( ArrayList ) probeToClassMap.get( probe ) ).contains( id ) )
                  ( ( ArrayList ) probeToClassMap.get( probe ) ).remove( id );
         }
         classToProbeMap.remove( id );
      }
      if ( classToGeneMap.containsKey( id ) ) classToGeneMap.remove( id );
      if ( classesToRedundantMap.containsKey( id ) )
         classesToRedundantMap.remove( id );
   }

   /**
    * @return Map
    */
   public Map getProbeToGeneMap() {
      return probeToGeneName;
   }

   /**
    * @return Map
    */
   public Map getGeneToProbeList() {
      return geneToProbeList;
   }

   /**
    * @return Map
    */
   public Map getClassToProbeMap() {
      return classToProbeMap;
   }

   /**
    * @param id String class id
    * @return ArrayList list of probes in class
    */
   public ArrayList getClassToProbes( String id ) {
      return ( ArrayList ) classToProbeMap.get( id );
   }

   /**
    *  
    */
   public void sortGeneSets() {
      sortedGeneSets = new Vector( classToProbeMap.entrySet().size() );
      Set keys = classToProbeMap.keySet();
      Vector l = new Vector();
      l.addAll( keys );
      Collections.sort( l );
      Iterator it = l.iterator();
      while ( it.hasNext() ) {
         sortedGeneSets.add( it.next() );
      }
   }

   /**
    * @return Map
    */
   public Map getProbeToClassMap() {
      return probeToClassMap;
   }

   /**
    * @return Map
    */
   public Map getClassesToRedundantMap() {
      return classesToRedundantMap;
   }

   /**
    * Get the gene that a probe belongs to.
    * 
    * @param p String
    * @return String
    */
   public String getProbeGeneName( String p ) {
      return ( String ) probeToGeneName.get( p );
   }

   /**
    * Get the description for a gene.
    * 
    * @param p String
    * @return String
    */
   public String getProbeDescription( String p ) {
      return ( String ) probeToDescription.get( p );
   }

   /**
    * Get a list of the probes that correspond to a particular gene.
    * 
    * @param g String a gene name
    * @return ArrayList list of the probes for gene g
    */
   public ArrayList getGeneProbeList( String g ) {
      return ( ArrayList ) geneToProbeList.get( g );
   }

   public int numClasses() {
      return sortedGeneSets.size();
   }

   public String getClass( int i ) {
      return ( String ) sortedGeneSets.get( i );
   }

   /**
    * Get the number of probes in a class
    * 
    * @param id String a class id
    * @return int number of probes in the class
    */
   public int numProbes( String id ) {
      if ( !classToProbeMap.containsKey( id ) ) {
         return 0;
      }

      return ( ( ArrayList ) classToProbeMap.get( id ) ).size();
   }

   /**
    * Returns true if the class is in the classToProbe map
    * 
    * @param id String a class id
    * @return boolean
    */
   public boolean classExists( String id ) {
      return classToProbeMap.containsKey( id );
   }

   public int numGenes( String id ) {
      if ( !classToGeneMap.containsKey( id ) ) {
         return 0;
      }
      
      return ( ( HashSet ) classToGeneMap.get( id ) ).size();
   }

   /**
    * Add a class
    * 
    * @param id String class to be added
    * @param probes ArrayList user-defined list of members.
    */
   public void addClass( String id, ArrayList probes ) {
      classToProbeMap.put( id, probes );

      Iterator probe_it = probes.iterator();
      while ( probe_it.hasNext() ) {
         String probe = new String( ( String ) probe_it.next() );
         ( ( ArrayList ) probeToClassMap.get( probe ) ).add( id );
      }

      Set genes = new HashSet();
      Iterator probe_it2 = probes.iterator();
      while ( probe_it2.hasNext() ) {
         genes.add( probeToGeneName.get( probe_it2.next() ) );
      }
      classToGeneMap.put( id, genes );
      resetSelectedSets();
   }

   /**
    * Redefine a class.
    * 
    * @param classId String class to be modified
    * @param probes ArrayList current user-defined list of members. The "real" version of the class is modified to look
    *        like this one.
    */
   public void modifyClass( String classId, ArrayList probes ) {
      ArrayList orig_probes = ( ArrayList ) classToProbeMap.get( classId );
      Iterator orig_probe_it = orig_probes.iterator();
      while ( orig_probe_it.hasNext() ) {
         String orig_probe = new String( ( String ) orig_probe_it.next() );
         if ( !probes.contains( orig_probe ) ) {
            Set ptc = new HashSet( ( Collection ) probeToClassMap
                  .get( orig_probe ) );
            ptc.remove( classId );
            probeToClassMap.remove( orig_probe );
            probeToClassMap.put( orig_probe, new ArrayList( ptc ) );
         }
      }
      Iterator probe_it = probes.iterator();
      while ( probe_it.hasNext() ) {
         String probe = ( String ) probe_it.next();
         if ( !orig_probes.contains( probe ) ) {
            ( ( ArrayList ) probeToClassMap.get( probe ) ).add( classId );
         }
      }
      classToProbeMap.put( classId, probes );
      resetSelectedSets();
   }

   /**
    * @return
    */
   public TableModel toTableModel() {
      return new AbstractTableModel() {
         private String[] columnNames = { "Probe", "Gene", "Description" };

         public String getColumnName( int i ) {
            return columnNames[i];
         }

         public int getColumnCount() {
            return 3;
         }

         public int getRowCount() {
            return selectedProbes.size();
         }

         public Object getValueAt( int i, int j ) {

            String probeid = ( String ) selectedProbes.get( i );
            switch ( j ) {
               case 0:
                  return probeid;
               case 1:
                  return getProbeGeneName( probeid );
               case 2:
                  return getProbeDescription( probeid );
               default:
                  return null;
            }
         }

      };
   }

   /**
    * Create a selected probes list based on a search string.
    * 
    * @param searchOn A string to be searched.
    */
   public void selectProbes( String searchOn ) {

      String searchOnUp = searchOn.toUpperCase();
      resetSelectedProbes();
      Set removeUs = new HashSet();
      for ( Iterator it = probeToGeneName.keySet().iterator(); it.hasNext(); ) {
         String probe = ( String ) it.next();

         String candidate = ( ( String ) probeToGeneName.get( ( probe ) ) )
               .toUpperCase();

         // look in descriptions.
         String candidateD = ( ( String ) probeToDescription.get( ( probe ) ) )
               .toUpperCase();

         if ( !candidate.startsWith( searchOnUp )
               && candidateD.indexOf( searchOnUp ) < 0 ) {
            removeUs.add( probe );
         }

      }

      for ( Iterator it = removeUs.iterator(); it.hasNext(); ) {
         selectedProbes.remove( it.next() );
      }
   }

   /**
    * Set the selected gene set to be the entire set.
    */
   public void resetSelectedProbes() {
      selectedProbes = new Vector( probeToGeneName.keySet() );
   }

   /**
    * @return the list of selected probes.
    */
   public List getSelectedProbes() {
      return selectedProbes;
   }

   /**
    * @return the number of probes currently on the 'selected' list.
    */
   public int selectedProbes() {
      return selectedProbes.size();
   }

   /**
    * @param searchOn
    * @param goData
    */
   public void selectSets( String searchOn, GONames goData ) {

      String searchOnUp = searchOn.toUpperCase();
      resetSelectedSets();
      Set removeUs = new HashSet();
      for ( Iterator it = classToProbeMap.keySet().iterator(); it.hasNext(); ) {
         String candidate = ( String ) it.next();

         // look in the name too
         String candidateN = goData.getNameForId( candidate ).toUpperCase();

         if ( !candidate.toUpperCase().startsWith( searchOnUp )
               && candidateN.indexOf( searchOnUp ) < 0 ) {
            removeUs.add( candidate );
         }
      }

      for ( Iterator it = removeUs.iterator(); it.hasNext(); ) {
         selectedSets.remove( it.next() );
      }
   }

   /**
    * Set the selected gene set to be the entire set.
    */
   public void resetSelectedSets() {
      selectedSets = new Vector( classToProbeMap.keySet() );
   }

   /**
    * @return list of selected sets.
    */
   public List getSelectedSets() {
      return selectedSets;
   }

   /**
    * @return the number of sets currently on the 'selected' list.
    */
   public int selectedSets() {
      return selectedSets.size();
   }

}