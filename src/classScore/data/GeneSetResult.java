package classScore.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

/**
  Data structure to store class scoring information about
  a class. (experiment scores only)

  @author Paul Pavlidis
  @version $Id$

 */
public class GeneSetResult implements Comparable {
   private String class_id = null;
   private String class_name = null;
   private double pvalue = 1.0;
   private double score = 0.0;
 //  private double hyperpval = 1.0;
   private int hypercut = 0; // how many genes make the pvalue threshold; this is n1 in the calculations
//   private double rocpval = 1.0;
 //  private double aroc = 0.5;
   private int size = 0;
   private int effective_size = 0;
   private double pvalue_corr = 0.0;
 //  private double hyperpval_corr = 0.0;
 //  private double rocpval_corr = 0.0;
   private LinkedHashMap identicalTo = null; // Defines classes this one is identical to in terms of members.
   private DecimalFormat nf;
   private int rank;

   public GeneSetResult() {
      this(null, null, 0, 0, 0.0, 1.0, 1.0, 0.5, 1.0);
   }

   public GeneSetResult(String id, String class_name, int size, int effsize) {
      this();
      this.setnames(id, class_name);
      this.setsizes(size, effsize);
   }

   public GeneSetResult(String id,
                      String class_name,
                      int size,
                      int effective_size,
                      double score,
                      double pvalue,
                      double hyperpval,
                      double aroc,
                      double rocpval) {
      this.class_id = id;
      this.class_name = class_name;
      this.pvalue = pvalue;
      this.score = score;
 //     this.hyperpval = hyperpval;
    //  this.rocpval = rocpval;
   //   this.aroc = aroc;
      this.size = size;
      this.effective_size = effective_size;

      nf = new DecimalFormat();
      nf.setMaximumFractionDigits(8);
      nf.setMinimumFractionDigits(3);
   }

   public void print(BufferedWriter out) throws IOException {
      this.print(out, "");
   }

   public void print(BufferedWriter out, String extracolumns) throws
           IOException {

      String fixnamea;
      String cleanname;
      if (class_name != null) {
         fixnamea = class_name.replace(' ', '_'); // make the format compatible with the perl scripts Paul wrote.
         cleanname = fixnamea.replace(':', '-'); // todo: figure out why this doesn't work.
      } else {
         cleanname = "";
      }
      out.write(cleanname + "_" +
                class_id + "" + "\t" + class_name + "\t" + class_id + "\t" +
                size + "\t" +
                effective_size + "\t" +
                nf.format(score) + "\t" + nf.format(pvalue)
                 + "\t" +
                nf.format(pvalue_corr) + "\t" + extracolumns + "\n");
   }

   public void print_headings(BufferedWriter out) throws IOException {
      this.print_headings(out, "");
   }

   public void print_headings(BufferedWriter out, String extracolumns) throws
           IOException {
      out.write("Class" + "\tClass Name" + "\tClass ID" + "\tsize" +
                "\teffective_size" +
                "\traw score" +
                "\tresamp pval" +
                "\tN over pval cut\tORA pval"
                /* + "\tAROC" + "\tAROCpval"  */
                +
                "\tCorrected_pvalue" + extracolumns + "\n");
   }

   public void setnames(String id, String name) {
      this.class_id = id;
      this.class_name = name;
   }

   public void setsizes(int size, int effsize) {
      this.size = size;
      this.effective_size = effsize;
   }

   public void setScore(double ascore) {
      score = ascore;
   }

   public void setPValue(double apvalue) {
      pvalue = apvalue;
   }


   public void setCorrectedPvalue(double a) {
      pvalue_corr = a;
   }


   public String toString() {
      return "I'm a classresult";
   }

   public String getClassId() {
      return class_id;
   }

   public String getClassName() {
      return this.class_name;
   }

   public double getPvalue() {
      return pvalue;
   }

   public double getScore() {
      return score;
   }

   public int getEffectiveSize() {
      return effective_size;
   }

   public int getRank() {
      return rank;
   }

   public void setRank(int n) {
      rank = n;
   }

   public double getPvalue_corr() {
      return pvalue_corr;
   }

   /**
    *
    * @return int
    */
   public int getSize() {
      return size;
   }

   /**
    * Default comparator for this class: sorts by the pvalue.
    *
    * @param ob Object
    * @return int
    */
   public int compareTo(Object ob) {
      GeneSetResult other = (GeneSetResult) ob;
      if (this.pvalue > other.pvalue) {
         return 1;
      } else if (this.pvalue < other.pvalue) {
         return -1;
      } else {
         return 0;
      }
   }

}
