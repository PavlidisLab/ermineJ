package classScore;

import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @todo All input of custom classes, identified either by probe id or official gene name.
 */

public class Settings {
   Properties properties;
   String pref_file;
   String classFile;
   String annotFile;
   String rawFile;
   String saveFolder;
   String scoreFile;
   int maxClassSize = 15;
   int minClassSize = 14;
   int iterations = 10;
   int scorecol = 2;
   boolean doLog = true;
   double pValThreshold = 0.001;

   /**
    * Creates settings object
    * @param file - name of preferences file to read
    */
   public Settings( String pref_file ) {
      this.pref_file = pref_file;
      classFile = "C:\\jbproject\\ermineJ\\data\\goNames.txt";
      annotFile = "C:\\jbproject\\ermineJ\\data\\HG-U95A.an.txt";
      rawFile = "C:\\jbproject\\ermineJ\\data\\melanoma_and_sarcomaMAS5.txt";
      saveFolder = "C:\\jbproject\\ermineJ\\data\\";
      scoreFile = "C:\\jbproject\\ermineJ\\data\\one-way-anova-parsed.txt";

      properties = new Properties();
      try {
         File fi = new File( pref_file );
         if ( fi.canRead() ) {
            InputStream f = new FileInputStream( pref_file );
            properties.load( f );
            if ( properties.containsKey( "scoreFile" ) )
               scoreFile = properties.getProperty( "scoreFile" );
            if ( properties.containsKey( "classFile" ) )
               classFile = properties.getProperty( "classFile" );
            if ( properties.containsKey( "annotFile" ) )
               annotFile = properties.getProperty( "annotFile" );
            if ( properties.containsKey( "rawFile" ) )
               rawFile = properties.getProperty( "rawFile" );
            if ( properties.containsKey( "saveFolder" ) )
               saveFolder = properties.getProperty( "saveFolder" );
            if ( properties.containsKey( "maxClassSize" ) )
               maxClassSize = Integer.valueOf( properties.getProperty( "maxClassSize" ) ).intValue();
            if ( properties.containsKey( "minClassSize" ) )
               minClassSize = Integer.valueOf( properties.getProperty( "minClassSize" ) ).intValue();
            if ( properties.containsKey( "iterations" ) )
               iterations = Integer.valueOf( properties.getProperty( "iterations" ) ).intValue();
            if ( properties.containsKey( "scorecol" ) )
               scorecol = Integer.valueOf( properties.getProperty( "scorecol" ) ).intValue();
            if ( properties.containsKey( "doLog" ) )
               doLog = Boolean.valueOf( properties.getProperty( "doLog" ) ).booleanValue();
            if ( properties.containsKey( "pValThreshold" ) )
               pValThreshold = Double.valueOf( properties.getProperty( "pValThreshold" ) ).doubleValue();
         }
      }
      catch ( IOException ex ) {
         System.err.println( "Could not find preferences file." ); // no big deal.
      }
   }

   /**
    * Writes setting values to file.
    */
   public void writePrefs() throws IOException
   {
      properties.setProperty("scoreFile", scoreFile);
      properties.setProperty("classFile", classFile);
      properties.setProperty("annotFile", annotFile);
      properties.setProperty("rawFile", rawFile);
      properties.setProperty("saveFolder", saveFolder);
      properties.setProperty("maxClassSize", String.valueOf(maxClassSize));
      properties.setProperty("minClassSize", String.valueOf(minClassSize));
      properties.setProperty("doLog", String.valueOf(doLog));
      properties.setProperty("pValThreshold", String.valueOf(pValThreshold));
      properties.setProperty("iterations", String.valueOf(iterations));
      properties.setProperty("scorecol", String.valueOf(scorecol));
      OutputStream f = new FileOutputStream(pref_file);
      properties.store(f, "");
   }

   /**
    * Returns setting values.
    */
   public String getClassFile() { return classFile; }
   public String getAnnotFile() { return annotFile; }
   public String getRawFile() { return rawFile; }
   public String getSaveFolder() { return saveFolder; }
   public String getScoreFile() { return scoreFile; }
   public int getMaxClassSize() { return maxClassSize; }
   public int getMinClassSize() { return minClassSize; }
   public int getIterations() { return iterations; }
   public int getScorecol() { return scorecol; }
   public boolean getDoLog() { return doLog; }
   public double getPValThreshold() { return pValThreshold; }

   /**
    * Sets setting values.
    */
   public void setClassFile(String val) {  classFile=val; }
   public void setAnnotFile(String val) {  annotFile=val; }
   public void setRawFile(String val) {  rawFile=val; }
   public void setSaveFolder(String val) {  saveFolder=val; }
   public void setScoreFile(String val) {  scoreFile=val; }
   public void setMaxClassSize(int val) {  maxClassSize=val; }
   public void setMinClassSize(int val) {  minClassSize=val; }
   public void setIterations(int  val) {  iterations=val; }
   public void setScorecol(int val) {  scorecol=val; }
   public void setDoLog(boolean val) {  doLog=val; }
   public void setPValThreshold(double val) {  pValThreshold=val; }
}
