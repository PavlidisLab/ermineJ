package classScore;

import util.*;
import java.util.*;
//import java.util.Locale.*;
import java.lang.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;

/**
  Stores information relevent to a histogram.   Created :09/02/02
  @author Shahmil Merchant, Paul Pavlidis
  @version $Id$
 */
public class histogram {

  private int min_class_size = 0;
  private double bin_size = 0.002; // todo: set this automatically?, so there are always a reasonable # of bins.
  private double hist_min = 0.0; // todo: this is never set anywhere.
  private double hist_max = 5.0; // this gets adjusted if need be.
  private int number_of_bins = 0;
  private int number_of_runs = 0;
  private Matrix M = null; // holds the actual histograms. Each row is a histogram.
  private double minPval; // the smallest possible pvalue: used when a requested score is out of the top of the range.

  /**
   */
  public histogram() {
    ;
  }

  /**
   */
  public histogram(int number_of_class, int min_class_size, int number_of_runs,
                   double max) {
    this.hist_max = max;
    this.hist_min = this.hist_min; // todo: fix this so it does something.
    this.min_class_size = min_class_size;
    set_number_of_runs(number_of_runs);
    set_number_of_bins();

    if (number_of_class < 1) {
      System.err.println("Error: No classes.");
      System.exit(1);
    }

    M = new Matrix(number_of_class, number_of_bins + 1);
  }

  /**
   */
  public void set_number_of_bins() {
    number_of_bins = (int) ( (hist_max - hist_min) / (double) bin_size);
    if (number_of_bins < 1) {
      System.err.println("Error: Histogram had no bins or too few bins. (" +
                         number_of_bins + ")");
      System.exit(1);
    }
  }

  /**
   */
  public void set_number_of_runs(int runs) {
    number_of_runs = runs;
    minPval = 0.5 / (double) number_of_runs; // the best possible pvalue for a class.
    System.err.println("Minimum pvalue will be " + minPval + ", " +
                       number_of_runs + " runs.");
  }

  /**
   Update the count for one bin.
   */
  public void update(int row, double value) {

    int thebin = (int) Math.floor( (value - hist_min) / (double) bin_size);

    // make sure we're in the range
    if (thebin < 0) {
      thebin = 0;
    }

    if (thebin > number_of_bins - 1) { // this shouldn't happen since we make sure there enough bins.
      System.err.println("Warning, last bin exceeded!");
      thebin = number_of_bins - 1;
    }
    M.increment_matrix_val(row, thebin);
  }

  /**
    Convert a raw histogram to a cdf.
   */
  public void tocdf(int number_of_class, int class_min_size) {

    for (int i = 0; i < M.get_num_rows(); i++) { // for each histogram (class size)
      double total = M.get_row_sum(i);

      double sum = 0.0;
      for (int j = M.get_num_cols() - 1; j >= 0; j--) { // for each bin in this histogram. Go from right to avoid roundoff problems where it matters.
        sum += M.get_matrix_val(i, j) / total;
        M.set_matrix_val(i, j, sum);
      }
    }
    System.err.println("Made cdf");
  }

  /**
   */
  public double get_bin_size() {
    return bin_size;
  }

  /**
   */
  public double get_hist_min() {
    return hist_min;
  }

  /**
   */
  public double get_hist_max() {
    return hist_max;
  }

  /**
   */
  public int get_number_of_bins() {
    return number_of_bins;
  }

  public int get_number_of_histograms() {
    return M.get_num_rows();
  }

  /**
   */
  public int get_number_of_runs() {
    return number_of_runs;
  }

  /** todo: this should be disallowed.
   */
  public Matrix get_matrix() {
    return M;
  }

  public int get_min_class_size() {
    return min_class_size;
  }

  public String toString() {
    return "There are " + number_of_bins +
        " bins in the histogram. The maximum possible value is " + hist_max +
        ", the minimum is " + hist_min + "." + " Min class is " +
        min_class_size + ".";
  }

  /**
   */
  public int class_index(int class_size, int min_class_size) {
    //get corresponding index for each class size
    return class_size - min_class_size;
  }

  /**
   */
  public double get_val(int class_size, double rawscore) {
    if (rawscore > hist_max || rawscore < hist_min) { // sanity check.
      System.err.println(
          "Warning, a rawscore yielded a bin number which was out of the range: " +
          rawscore);
      return -1.0;
    }
    else {
      int row = this.class_index(class_size, min_class_size);
      int binnum = (int) Math.floor( (rawscore - hist_min) / bin_size);

      if (binnum < 0) {
        binnum = 0;

      }
      if (binnum > number_of_bins - 1) {
        binnum = number_of_bins - 1;

      }
      return this.get_pval(row, binnum);
    }
  }

  /**
     Print the histogram to stderr.
   */
  public void print() {
    /* print a heading */
    int stepsize = 20;
    System.err.print("heading:");
    for (int j = 0; j < M.get_num_cols(); j += stepsize) { // for each bin in this histogram.
      System.err.print("\t" + (hist_min + bin_size * j));
    }
    System.err.print("\n");

    for (int i = 0; i < M.get_num_rows(); i++) { // for each histogram (class size)
      System.err.print("row:");
      for (int j = 0; j < M.get_num_cols(); j += stepsize) { // for each bin in this histogram.
        System.err.print("\t" + M.get_matrix_val(i, j));
      }
      System.err.print("\n");
    }
    //	M.print();
  }

  /**
   */
  public double get_pval(int row, int binnum) {
    double pval = M.get_matrix_val(row, binnum);
    if (pval == 0.0) {
      return minPval;
    }
    else {
      return pval;
    }
  }

}
