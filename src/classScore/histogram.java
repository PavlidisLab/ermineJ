package classScore;
import util.*;
import java.util.*;
//import java.util.Locale.*;
import java.lang.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;


/**
  Stores information relevent to a histogram.   Created :09/02/02

  @author Shahmil Merchant
  @version $Id$

*/
public class histogram {

    private int min_class_size = 0;
    private double bin_size = 0.002; // todo: set this automatically, so there are always a reasonable # of bins.
    private double hist_min = 0.0; // todo: this is never set anywhere.
    private double hist_max = 5.0;
    private int number_of_bins = 0;
    private int number_of_runs = 0;
    private int row = 0;
    private int column = 0;
    private Matrix M = null; // holds the actual histograms.
    private Map list;
    private double minPval; // the smallest possible pvalue: used when a requested score is out of the top of the range.


    /**
     */
    public histogram() {
	;
    }


    /**
     */
    public histogram(int number_of_class, int min_class_size, int number_of_runs, double max) {
	this.hist_max = max;
	this.hist_min = this.hist_min; // todo: fix this so it does something.
	this.min_class_size = min_class_size;
	set_number_of_runs(number_of_runs);
	set_number_of_bins();
	M = new Matrix(number_of_class, number_of_bins + 1 );
	list = new HashMap();
    }


    /**
     */
    public void set_number_of_bins()  
    {
	number_of_bins =(int)((hist_max - hist_min)/(double)bin_size);
	if (number_of_bins == 0) {
	    System.err.println("Error: Histogram had no bins.");
	    System.exit(1);
	}
    }


    /**
     */
    public void set_number_of_runs(int runs)
    {
	number_of_runs = runs;
	minPval = 0.5 / (double)number_of_runs; // the best possible pvalue for a class.
	System.err.println("Minimum pvalue will be " + minPval + ", " + number_of_runs + " runs.");
    }

    
    /**

     Update the count for one bin.

     */
    public void update(int row, double value) {

	int thebin = (int)Math.floor((value - hist_min)/(double)bin_size);

	// make sure we're in the range
	if (thebin < 0) {
	    thebin = 0;
	}

	if (thebin > number_of_bins - 1) { // this shouldn't happen since we make sure there enough bins. Might give slight speedup.
	    System.err.println("Warning, last bin exceeded!");
	    thebin = number_of_bins - 1;
	}
	M.increment_matrix_val(row, thebin);
    }


    /**

      Convert a raw histogram to a cdf.

    */
    public void tocdf(int number_of_class, int class_min_size) {

	// todo: get rid of this if we don't use it???
	for (int s = class_min_size; s < (number_of_class + class_min_size - 1); s++){
	    String number = Integer.toString(s);
	    list.put(number, null);
	}
	
	for (int i = 0; i < M.get_num_rows(); i++ ) { // for each histogram (class size)
	    double total = M.get_row_sum(i);

	    //	    System.err.println(total + " trials observed.");
	    double sum = 0.0; 
	    for (int j = M.get_num_cols() - 1; j >= 0; j--) { // for each bin in this histogram.
		sum += M.get_matrix_val(i,j) / total;
		M.set_matrix_val(i, j, sum);
	    }
	}
	System.err.println("Made cdf");

	// debug
	//	Stats statistics = new Stats();
	//	showArray.show(M.get_ith_row(105), M.get_num_cols());
	//	showArray.show(M.get_ith_row(20), M.get_num_cols());
	//	showArray.show(M.get_ith_row(40), M.get_num_cols());
    }



    /**
       
     */
    public double get_bin_size()

    {
	return bin_size;
    }


    /**
     */
    public double get_hist_min()
    {
	return hist_min;
    }


    /**
     */
    public double get_hist_max()
    {
	return hist_max;
    }


    /**
     */
    public int get_number_of_bins()

    {
	return number_of_bins;
    }

    public int get_number_of_histograms() {
	return M.get_num_rows();
    }


    /**
     */
    public int get_number_of_runs()
	
    {
	return number_of_runs;
    }


    /** todo: this should be disallowed.
     */
    public Matrix get_matrix()
    {
	return M;
    }


    /**
     */
    public Map get_matrix_map()
    {
	return list;
    }


    public int get_min_class_size() {
	return min_class_size;
    }

    public String toString () {
	return "There are " + number_of_bins + " bins in the histogram. The maximum possible value is " + hist_max + ", the minimum is " + hist_min + "." + " Min class is " + min_class_size + ".";
    }


    /**
     */
    public int class_index(int class_size, int min_class_size)
    {
	//get corresponding index for each class size
	return class_size - min_class_size;
    }


    /**
     */
    public double get_val(int class_size, double rawscore) {
	if ( rawscore > hist_max  || rawscore < hist_min ) { // sanity check.
	    System.err.println("Warning, a rawscore yielded a bin number which was out of the range: " + rawscore);
	    return -1.0;
	} else {
	    int row = this.class_index( class_size, min_class_size);
	    int binnum = (int)Math.floor((rawscore - hist_min) / bin_size);

	    if (binnum < 0) 
		binnum = 0;
	    
	    if (binnum > number_of_bins - 1)
		binnum = number_of_bins - 1;
	    
	    return this.get_pval(row, binnum);
	}
    }

    /*****************************************************************************************/
    /*****************************************************************************************/
    public double get_pval(int row, int binnum) {
	double pval = M.get_matrix_val(row, binnum);
	if (pval == 0.0)
	    return minPval;
	else 
	    return pval;
    }

}

