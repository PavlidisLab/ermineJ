package classScore.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xml.sax.SAXException;

import baseCode.dataStructure.OntologyEntry;
import baseCode.dataStructure.graph.DirectedGraph;
import baseCode.dataStructure.graph.DirectedGraphNode;
import baseCode.xml.GOParser;

/**
 * Gets geneclass names from tab-delimited file of the form GoID[tab]Biological Name. This is generic in that it doesn't
 * have to be a GO, could be any class scheme. The values are stored in a HashTable. Created 09/02/02
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Shahmil Merchant, Paul Pavlidis
 * @version $Id$
 */
public class GONames {

   private static Map goNameMap;
   private Set newGeneSets = new HashSet();
   private GOParser parser;

   /**
    * 
    * @param filename <code>String</code> The XML file containing class to name mappings. First column is the class
    *        id, second is a description that will be used int program output.
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public GONames( String filename ) throws SAXException, IOException {

      if ( filename == null || filename.length() == 0 ) {
         throw new IllegalArgumentException(
               "Invalid filename or no filename was given" );
      }

      InputStream i = new FileInputStream( filename );
      parser = new GOParser( i );

      //     MapReader m = new MapReader();
      //    goNameMap = m.read( filename, true );

      goNameMap = parser.getGONameMap();

   }

   /**
    * Get the graph representation of the GO hierarchy. This can be used to support JTree representations.
    * 
    * @return
    */
   public DirectedGraph getGraph() {
      return parser.getGraph();
   }

   /**
    * 
    * @param id
    * @return a Set containing the ids of geneSets which are immediately below the selected one in the hierarchy.
    */
   public Set getChildren( String id ) {
      Set returnVal = new HashSet();
      Set children = ( ( DirectedGraphNode ) getGraph().get( id ) )
            .getChildNodes();
      for ( Iterator it = children.iterator(); it.hasNext(); ) {
         DirectedGraphNode child = ( DirectedGraphNode ) it.next();
         String childKey = ( ( OntologyEntry ) child.getItem() ).getId();
         returnVal.add( childKey );
      }
      return returnVal;
   }

   /**
    * 
    * @param id
    * @return a Set containing the ids of geneSets which are immediately above the selected one in the hierarchy.
    */
   public Set getParents( String id ) {
      Set returnVal = new HashSet();
      Set parents = ( ( DirectedGraphNode ) getGraph().get( id ) )
            .getParentNodes();
      for ( Iterator it = parents.iterator(); it.hasNext(); ) {
         DirectedGraphNode parent = ( DirectedGraphNode ) it.next();
         String parentKey = ( ( OntologyEntry ) parent.getItem() ).getId();
         returnVal.add( parentKey );
      }
      return returnVal;
   }

   /**
    * Get the Map representation of the GO id - name associations.
    * 
    * @return Map
    */
   public Map getMap() {
      return goNameMap;
   }

   /**
    * 
    * @param go_ID String
    * @return String
    */
   public String getNameForId( String go_ID ) {
      String name = ( String ) ( goNameMap.get( go_ID ) );
      if ( name == null ) {
         return "<no description available>";
      }
      return name;
   }

   /**
    * 
    * @param m Map
    */
   public void toString( Map m ) {
      Collection entries = m.entrySet();
      Iterator it = entries.iterator();
      while ( it.hasNext() ) {
         Map.Entry e = ( Map.Entry ) it.next();
         System.out.println( "Key = " + e.getKey() + ", Value = "
               + e.getValue() );
      }
   }

   /**
    * 
    * @param id String
    * @param name String
    * @todo this should modify the tree representation too.
    */
   public void addClass( String id, String name ) {
      goNameMap.put( id, name );
      newGeneSets.add( id );
   }

   /**
    * 
    * @param id String
    * @param name String
    * @todo this should modify the tree representation too.
    */
   public void modifyClass( String id, String name ) {
      goNameMap.put( id, name );
      newGeneSets.add( id );
   }

   public boolean newSet( String id ) {
      return newGeneSets.contains( id );
   }

   public Set getNewGeneSets() {
      return newGeneSets;
   }
}