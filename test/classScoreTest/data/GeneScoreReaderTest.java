package classScoreTest.data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import baseCode.bio.geneset.GeneAnnotations;
import baseCodeTest.io.reader.TestStringMatrixReader;
import classScore.Settings;
import classScore.data.GeneScoreReader;
import junit.framework.TestCase;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneScoreReaderTest extends TestCase {
   InputStream is = null;
   InputStream ism = null;
   GeneScoreReader test = null;

   /*
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
      Settings s = new Settings();
   

      ism = GeneScoreReaderTest.class
      .getResourceAsStream( "/data/test.an.txt" );
      
      is = GeneScoreReaderTest.class
            .getResourceAsStream( "/data/test.scores.txt" );
      
      GeneAnnotations g = new GeneAnnotations(ism, null, null);
      Map gpm = g.getGeneSetToProbeMap();
      Map pgm = g.getProbeToGeneMap();
      
      test = new GeneScoreReader( is, s, null, gpm, pgm );
      super.setUp();
   }

   /*
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
      super.tearDown();
      ism.close();
      is.close();
   }

   public void testGet_probe_ids() {
      //TODO Implement get_probe_ids().
   }

   public void testGetPvalues() {
      //TODO Implement getPvalues().
   }

   public void testGet_numpvals() {
      //TODO Implement get_numpvals().
   }

   /*
    * Class under test for Map getGeneToPvalMap()
    */
   public void testGetGeneToPvalMap() {
      //TODO Implement getGeneToPvalMap().
   }

}