package classScore;

import java.io.*;
import java.net.*;
import java.util.*;
import baseCode.gui.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 * @todo
 */

public class Settings {
   private Properties properties;
   private String pref_file;
   private String classFile;
   private String annotFile;
   private String rawFile;
   private String dataFolder;
   private String classFolder;
   private String scoreFile;
   private int maxClassSize = 15;
   private int minClassSize = 14;
   private int iterations = 10;
   private int scorecol = 2;
   private  int geneRepTreatment = BEST_PVAL;
   private int rawScoreMethod = MEAN_METHOD;
   private int analysisMethod = RESAMP;
   private  int quantile = 50;
   private boolean doLog = true;
   private  double pValThreshold = 0.001;

   public static final int BEST_PVAL = 1;
   public static final int MEAN_PVAL = 2;
   public static final int MEAN_METHOD = 0;
   public static final int QUANTILE_METHOD = 1;
   public static final int ORA = 0;
   public static final int RESAMP = 1;
   public static final int CORR = 2;
   public static final int ROC = 3;
   
   /**
    * Westfall-Young resampling-based.
    * Caution, this is very computationally intensive.
    * Also not implemented for all analysis methods.
    */
   public static final int WY = 0;
   
   /**
    * Bonferroni
    */
   public static final int BON = 1;
   
   /**
    * Benjamini-Hochberg FDR
    */
   public static final int BH = 2;
   
   public Settings() {
      this("");
   }

   /**
    * Creates settings object
    * @param file - name of preferences file to read
    */
   public Settings(String filename) {
      pref_file=filename;
      dataFolder=(new File(Settings.class.getResource("Settings.class").getFile())).getPath();
      int end = dataFolder.lastIndexOf(File.separatorChar);
      dataFolder = dataFolder.substring(0, end + 1) + ".." + File.separator + ".." + File.separator + "data";
      try{
         dataFolder = URLDecoder.decode((new File(dataFolder).getCanonicalPath()), "ISO-8859-1");
      }
      catch ( IOException ex ) {
         GuiUtil.error( "Could not find data folder." ); // make a big deal...
         System.exit(1);
      }
      classFolder = new String(dataFolder + File.separator + "genesets");
      if(pref_file.compareTo("")==0)
         pref_file = dataFolder + File.separator + "ClassScore.preferences";
      classFile = dataFolder + File.separator + "goNames.txt";
      annotFile = dataFolder + File.separator + "HG-U95A.an.txt";
      rawFile = dataFolder + File.separator + "melanoma_and_sarcomaMAS5.txt";
      scoreFile = dataFolder + File.separator + "one-way-anova-parsed.txt";

      properties = new Properties();
      try {
         File fi = new File( pref_file );
         if ( fi.canRead() ) {
            InputStream f = new FileInputStream( pref_file );
            properties.load( f );
            f.close();
            if ( properties.containsKey( "scoreFile" ) )
               scoreFile = properties.getProperty( "scoreFile" );
            if ( properties.containsKey( "classFile" ) )
               classFile = properties.getProperty( "classFile" );
            if ( properties.containsKey( "annotFile" ) )
               annotFile = properties.getProperty( "annotFile" );
            if ( properties.containsKey( "rawFile" ) )
               rawFile = properties.getProperty( "rawFile" );
            if ( properties.containsKey( "dataFolder" ) )
               this.dataFolder = properties.getProperty( "dataFolder" );
            if ( properties.containsKey( "classFolder" ) )
               this.classFolder = properties.getProperty( "classFolder" );
            if ( properties.containsKey( "maxClassSize" ) )
               maxClassSize = Integer.valueOf( properties.getProperty( "maxClassSize" ) ).intValue();
            if ( properties.containsKey( "minClassSize" ) )
               minClassSize = Integer.valueOf( properties.getProperty( "minClassSize" ) ).intValue();
            if ( properties.containsKey( "iterations" ) )
               iterations = Integer.valueOf( properties.getProperty( "iterations" ) ).intValue();
            if ( properties.containsKey( "scorecol" ) )
               scorecol = Integer.valueOf( properties.getProperty( "scorecol" ) ).intValue();
            if ( properties.containsKey( "geneRepTreatment" ) )
               geneRepTreatment = Integer.valueOf( properties.getProperty( "geneRepTreatment" ) ).intValue();
            if ( properties.containsKey( "rawScoreMethod" ) )
               rawScoreMethod = Integer.valueOf( properties.getProperty( "rawScoreMethod" ) ).intValue();
            if ( properties.containsKey( "analysisMethod" ) )
               analysisMethod = Integer.valueOf( properties.getProperty( "analysisMethod" ) ).intValue();
            if ( properties.containsKey( "quantile" ) )
               quantile = Integer.valueOf( properties.getProperty( "quantile" ) ).intValue();
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
    * Creates settings object
    * @param settings - settings object to copy
    */
   public Settings( Settings settings) {
      classFile=settings.getClassFile();
      annotFile=settings.getAnnotFile();
      rawFile=settings.getRawFile();
      dataFolder=settings.getDataFolder();
      classFolder=settings.getClassFolder();
      scoreFile=settings.getScoreFile();
      maxClassSize=settings.getMaxClassSize();
      minClassSize=settings.getMinClassSize();
      iterations=settings.getIterations();
      scorecol=settings.getScorecol();
      geneRepTreatment=settings.getGeneRepTreatment();
      rawScoreMethod=settings.getRawScoreMethod();
      analysisMethod=settings.getAnalysisMethod();
      quantile=settings.getQuantile();
      doLog=settings.getDoLog();
      pValThreshold=settings.getPValThreshold();
      pref_file=settings.getPrefFile();
      properties = new Properties();
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
      properties.setProperty("dataFolder", dataFolder);
      properties.setProperty("classFolder", classFolder);
      properties.setProperty("maxClassSize", String.valueOf(maxClassSize));
      properties.setProperty("minClassSize", String.valueOf(minClassSize));
      properties.setProperty("iterations", String.valueOf(iterations));
      properties.setProperty("scorecol", String.valueOf(scorecol));
      properties.setProperty("geneRepTreatment", String.valueOf(geneRepTreatment));
      properties.setProperty("rawScoreMethod", String.valueOf(rawScoreMethod));
      properties.setProperty("analysisMethod", String.valueOf(analysisMethod));
      properties.setProperty("quantile", String.valueOf(quantile));
      properties.setProperty("doLog", String.valueOf(doLog));
      properties.setProperty("pValThreshold", String.valueOf(pValThreshold));
      OutputStream f = new FileOutputStream(pref_file);
      properties.store(f, "");
      f.close();
   }

   /**
    * Returns setting values.
    */
   public String getClassFile() { return classFile; }
   public String getAnnotFile() { return annotFile; }
   public String getRawFile() { return rawFile; }
   public String getDataFolder() { return dataFolder; }
   public String getClassFolder() { return classFolder; }
   public String getScoreFile() { return scoreFile; }
   public int getMaxClassSize() { return maxClassSize; }
   public int getMinClassSize() { return minClassSize; }
   public int getIterations() { return iterations; }
   public int getScorecol() { return scorecol; }
   public int getGeneRepTreatment() { return geneRepTreatment; }
   public int getRawScoreMethod() { return rawScoreMethod; }
   public int getAnalysisMethod() { return analysisMethod; }
   public int getQuantile() { return quantile; }
   public boolean getDoLog() { return doLog; }
   public double getPValThreshold() { return pValThreshold; }
   public String getPrefFile() { return pref_file; }

   /**
    * Sets setting values.
    */
   public void setClassFile(String val) {  classFile=val; }
   public void setAnnotFile(String val) {  annotFile=val; }
   public void setRawFile(String val) {  rawFile=val; }
   public void setDataFolder(String val) {  dataFolder=val; }
   public void setClassFolder(String val) {  classFolder=val; }
   public void setScoreFile(String val) {  scoreFile=val; }
   public void setMaxClassSize(int val) {  maxClassSize=val; }
   public void setMinClassSize(int val) {  minClassSize=val; }
   public void setIterations(int  val) {  iterations=val; }
   public void setScorecol(int val) {  scorecol=val; }
   public void setGeneRepTreatment(int val) { geneRepTreatment=val; }
   public void setRawScoreMethod(int val) { rawScoreMethod=val; }
   public void setAnalysisMethod(int val) { analysisMethod=val; }
   public void setQuantile(int val) {  quantile=val; }
   public void setDoLog(boolean val) {  doLog=val; }
   public void setPValThreshold(double val) {  pValThreshold=val; }
   public void setPrefFile(String val) { pref_file=val; }

   public boolean getUseWeights() {
      if (geneRepTreatment==MEAN_PVAL || geneRepTreatment==BEST_PVAL)
         return true;
      else
         return false;
   }

   public String getGroupMethod() {
      if (geneRepTreatment==MEAN_PVAL)
         return "MEAN_PVAL";
      else if (geneRepTreatment==BEST_PVAL)
         return "BEST_PVAL";
      else
         return "MEAN_PVAL"; // dummy. It won't be used.
   }

   public String getClassScoreMethod() {
      if (rawScoreMethod==MEAN_METHOD) {
         return "MEAN_METHOD";
      } else {
         return "QUANTILE_METHOD"; // note that quantile is hard-coded to be 50 for the gui.
      }
   }

   /**
    * @todo this should return a boolean.
    * @return
    */
   public boolean getUseLog() {
      return doLog;
   }

}

