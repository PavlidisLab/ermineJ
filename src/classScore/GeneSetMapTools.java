package classScore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
   Parses the file of the form
   <pre>probe_id   Classes1|Classes2|Classes3|.....</pre>
   Stores it in the HashTable called probeToClassMap
   Stores the reverse also int classToProbeMap in the form
   <pre>Classes1   probe1,probe2....</pre>
   @author Shahmil Merchant, Paul Pavlidis (major changes)
   @version $Id$
 */
public class GeneSetMapTools {
//   private static Map classToProbeMap; //stores Classes->probe map

   // the following are filled in by other functions.
//   private static Map classesToRedundantMap; // stores classes->classes which are the same.
//   private static Map classesToSimilarMap; // stores classes->classes which are similar (and therefore not considered directly)

   /**
    */

   /**
      Remove classes which are too similar to some other
      class. Classes which have fractionSameThreshold of a larger
      class will be ignored. This doesn't know which classes are
      relevant to the data, so it does not work perfectly.
      The algorithm is: for each class, compare it to all other
      classes. If any class encountered is nearly the same as the
      query class, the smaller of the two classes is deleted and the
      query continues with the class that is left. We iterate until
      no changes are made (actually, the stopping criterion will need
      some work - we don't want to consolidate endlessly.)
      Typically what happens is one class will be contained in
      another.
    */
   public static void ignoreSimilar( double fractionSameThreshold, Map classToProbeMap ) {
      Set entries = classToProbeMap.keySet();
      Iterator it = entries.iterator();
      String queryClassId = "";
      String targetClassId = "";
      int sizeQuery = 0;
      int sizeTarget = 0;
      Map classesToSimilarMap = new LinkedHashMap();
      HashMap seenit = new HashMap();
      ArrayList deleteUs = new ArrayList();

      System.out.println( "...Highly (" + fractionSameThreshold * 100 +
                          "%)  similar classes are being removed..." );

      while ( it.hasNext() ) {
         queryClassId = ( String ) it.next();
         ArrayList queryClass = ( ArrayList ) classToProbeMap.get( queryClassId );

         sizeQuery = queryClass.size();
         if ( sizeQuery > 250 || sizeQuery < 5 ) {
            continue;
         }

         Iterator itb = entries.iterator();
         while ( itb.hasNext() ) {
            targetClassId = ( String ) itb.next();

            /* skip self comparisons and also symmetric comparisons. The latter half  */
            if ( seenit.containsKey( targetClassId ) ||
                 targetClassId.equals( queryClassId ) ) {
               continue;
            }

            ArrayList targetClass = ( ArrayList ) classToProbeMap.get(
                targetClassId );

            sizeTarget = targetClass.size();
            if ( sizeTarget > 250 || sizeTarget < 2 ) {
               continue;
            }

            //		System.out.println("Comparing " + targetClassId + " to " + queryClassId );

            if ( sizeTarget < sizeQuery ) {

               if ( ( double ) sizeTarget / sizeQuery < fractionSameThreshold ) {
                  continue;
               }

               if ( areSimilarClasses( sizeQuery, sizeTarget, queryClass,
                                       targetClass,
                                       fractionSameThreshold ) ) {
                  deleteUs.add( targetClassId );

                  //			System.out.println(targetClassId + " and " + queryClassId + " are similar (target is smaller and will be ignored)");

                  if ( !classesToSimilarMap.containsKey( queryClassId ) ) {
                     classesToSimilarMap.put( queryClassId, new ArrayList() );
                  }
                  if ( !classesToSimilarMap.containsKey( targetClassId ) ) {
                     classesToSimilarMap.put( targetClassId, new ArrayList() );

                  }
                  ( ( ArrayList ) classesToSimilarMap.get( queryClassId ) ).add(
                      targetClassId );
                  ( ( ArrayList ) classesToSimilarMap.get( targetClassId ) ).add(
                      queryClassId );

               }

            } else {

               if ( ( double ) sizeQuery / sizeTarget < fractionSameThreshold ) {
                  continue;
               }

               if ( areSimilarClasses( sizeTarget, sizeQuery, targetClass,
                                       queryClass,
                                       fractionSameThreshold ) ) {

                  //			System.out.println(targetClassId + " and " + queryClassId + " are similar (query is smaller and will be ignored)");

                  queryClassId = targetClassId;
                  queryClass = targetClass;
                  deleteUs.add( queryClassId );

                  if ( !classesToSimilarMap.containsKey( targetClassId ) ) {
                     classesToSimilarMap.put( targetClassId, new ArrayList() );
                  }
                  if ( !classesToSimilarMap.containsKey( queryClassId ) ) {
                     classesToSimilarMap.put( queryClassId, new ArrayList() );

                  }
                  ( ( ArrayList ) classesToSimilarMap.get( queryClassId ) ).add(
                      targetClassId );
                  ( ( ArrayList ) classesToSimilarMap.get( targetClassId ) ).add(
                      queryClassId );

                  break; // cant/dont test this query any more!
               }

            }

         }
         /* inner while */
         seenit.put( queryClassId, new Boolean( true ) );
      }
      /* end while ... */

      /* remove the ones we don't want to keep */
      Iterator itrd = deleteUs.iterator();
      while ( itrd.hasNext() ) {
         String deleteMe = ( String ) itrd.next();
         classToProbeMap.remove( deleteMe );
      }

      System.out.println( "There are now " + classToProbeMap.size() +
                          " classes represented on the chip (" + deleteUs.size() +
                          " were ignored)" );
   }

   /* ignoreSimilar */

   /**
      Helper function for ignoreSimilar.
    */
   private static boolean areSimilarClasses( int biggersize, int smallersize,
                                             ArrayList biggerClass,
                                             ArrayList smallerClass,
                                             double fractionSameThreshold ) {

      if ( biggersize < smallersize ) {
         System.err.println( "Invalid sizes" );
         System.exit( 1 );
      }

      /* iterate over the members of the larger class. If the member
       * is not in the smaller class, increment a counter. If this
       * count goes over the maximum allowed missing, return
       * false. */
      int maxmissing = ( int ) ( ( 1.0 - fractionSameThreshold ) *
                                 ( double ) biggersize );

      int notin = 0;
      Iterator ita = biggerClass.iterator();
      while ( ita.hasNext() ) {
         String probe = ( String ) ita.next();

         if ( !smallerClass.contains( probe ) ) { // using arraylists here searches are not optimally fast.
            notin++;

         }
         if ( notin > maxmissing ) {
            return false;
         }
      }

      /* return true is the count is high enough */
      return true;

   }

   /**
      Identify classes which are identical to others. This isn't
      superfast, because it doesn't know which classes are actually
      relevant in the data.
    */
   public static void collapseClasses(Map classToProbeMap) {
      LinkedHashMap seenClasses = new LinkedHashMap();
      LinkedHashMap sigs = new LinkedHashMap();
      Map classesToRedundantMap = new LinkedHashMap();
      ArrayList sortedList = null;
      Set entries = classToProbeMap.keySet();
      Iterator it = entries.iterator();
      String signature = "";
      String classId = "";
      HashMap seenit = new HashMap();

      System.out.println( "There are " + entries.size() +
                          " classes represented on the chip (of any size). Redundant classes are being removed..." );

      // sort each arraylist in for each go and create a string that is a signature for this class.
      int ignored = 0;
      while ( it.hasNext() ) {
         classId = ( String ) it.next();
         ArrayList classMembers = ( ArrayList ) classToProbeMap.get( classId );

         // skip classes that are huge. It's too slow
         // otherwise. This is a total heuristic. Note that this
         // doesn't mean the class won't get analyzed, it just
         // means we don't bother looking for redundancies. Big
         // classes are less likely to be identical to others,
         // anyway. In tests, the range shown below has no effect
         // on the results, but it _could_ matter.
         if ( classMembers.size() > 250 || classMembers.size() < 2 ) {
            ignored++;
            continue;
         }

         Collections.sort( classMembers );
         signature = "";
         Iterator classit = classMembers.iterator();
         seenit.clear();
         while ( classit.hasNext() ) {
            String probeid = ( String ) classit.next();
            if ( !seenit.containsKey( probeid ) ) {
               signature = signature + "__" + probeid;
               seenit.put( probeid, new Boolean( true ) );
            }
         }
         sigs.put( classId, signature );
      }

      // look at the signatures for repeats.
      entries = sigs.keySet();
      Iterator nit = entries.iterator();
      while ( nit.hasNext() ) {
         classId = ( String ) nit.next();
         signature = ( String ) sigs.get( classId );

         // if the signature has already been seen, add it to the redundant list, and remove this class from the classToProbeMap.
         if ( seenClasses.containsKey( signature ) ) {
            if ( !classesToRedundantMap.containsKey( seenClasses.get( signature ) ) ) {
               classesToRedundantMap.put( seenClasses.get( signature ),
                                          new ArrayList() );

            }
            ( ( ArrayList ) classesToRedundantMap.get( seenClasses.get( signature ) ) ).
                add( classId );
            classToProbeMap.remove( classId );
            //		System.err.println(classId + " is the same as an existing class, " + seenClasses.get(signature));
         } else {
            // add string to hash
            seenClasses.put( signature, classId );
         }
      }

      /*
            Set classes = classToProbeMap.keySet();
            it = classes.iterator();
            while (it.hasNext()) {
               Object considered_class = it.next();
               if(((ArrayList)classToProbeMap.get(considered_class)).size()<5)
                  classToProbeMap.remove(considered_class);
            }
       */
      System.out.println( "There are now " + classToProbeMap.size() +
                          " classes represented on the chip (" + ignored +
                          " were ignored)" );
   }

   /**
    */
   public ArrayList getRedundancies( String classId, Map classesToRedundantMap ) {
      if ( classesToRedundantMap != null &&
           classesToRedundantMap.containsKey( classId ) ) {
         return ( ArrayList ) classesToRedundantMap.get( classId );
      } else {
         return null;
      }
   }

   /**
    */
   public ArrayList getSimilarities( String classId, Map classesToSimilarMap ) {
      if ( classesToSimilarMap != null &&
           classesToSimilarMap.containsKey( classId ) ) {
         return ( ArrayList ) classesToSimilarMap.get( classId );
      } else {
         return null;
      }
   }

   /**
    */
   public String getRedundanciesString( String classId, Map classesToRedundantMap ) {
      if ( classesToRedundantMap != null &&
           classesToRedundantMap.containsKey( classId ) ) {
         ArrayList redundant = ( ArrayList ) classesToRedundantMap.get( classId );
         Iterator it = redundant.iterator();
         String returnValue = "";
         while ( it.hasNext() ) {
            returnValue = returnValue + ", " + it.next();
         }
         return returnValue;
      } else {
         return "";
      }
   }

   /**
    */
//   public Map getClassToProbeMap() {
//      return classToProbeMap;
//   }
//
//   /**
//    */
//   public ArrayList getClassValueMap( String class_id ) {
//      return ( ArrayList ) ( classToProbeMap.get( class_id ) );
//   }

   /**
    */
   public void print( Map m ) {
      //print the entire map
      Collection entries = m.entrySet();
      Iterator it = entries.iterator();
      while ( it.hasNext() ) {
         Map.Entry e = ( Map.Entry ) it.next();
         System.out.println( "Key = " + e.getKey() + ", Value = " + e.getValue() );
      }
   }

   public static void hackGeneSetToProbeMap(Map classToProbeMap) {
      int min = 14;
      int max = 15;
      Set keys = classToProbeMap.keySet();
      HashSet removekeys = new HashSet();
      Iterator it = keys.iterator();
      while ( it.hasNext() ) {
         String geneclass = ( String ) it.next();
         int size = ( ( ArrayList ) classToProbeMap.get( geneclass ) ).size();
         if ( size < min || size > max ) {
            removekeys.add( geneclass );
         }
      }
      Iterator rit = removekeys.iterator();
      while ( rit.hasNext() ) {
         String geneclass = ( String ) rit.next();
         classToProbeMap.remove( geneclass );
      }
   }

} // end of class
