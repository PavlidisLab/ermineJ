package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Stores information relevent to a histogram

                                                                                                                                                            
*******************************************************************************/

import scores.class_score.*;
import java.util.*;
import java.util.Locale.*;
import java.lang.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;


/*****************************************************************************************/
public class histogram {
/*****************************************************************************************/
    //default values
    private int class_size = 0;
    private double bin_size = 0.002;
    private double hist_min = 0.0;
    private double hist_max = 5.0;
    private int number_of_bins =0;
    private int number_of_runs = 0;
    private int row =0;
    private int column = 0;
    private Matrix M = null;
    private Map list;
   
/*****************************************************************************************/
    /*****************************************************************************************/
    public histogram(){}


    /*****************************************************************************************/
    /*****************************************************************************************/
    public histogram(int number_of_class,int class_min_size,int number_of_runs, double range) {
	set_hist_range(range);
	set_number_of_bins();
	M = new Matrix(number_of_class, number_of_bins + 1 );
	list = new HashMap();
	if (number_of_bins == 0) {
	    System.err.println("Error: Histogram had no bins (" + number_of_class + " classes, " + class_min_size + " min size, " + range + " range).");
	    System.exit(1);
	}
    }


/*****************************************************************************************/    
    /*****************************************************************************************/
    public void set_class_size(int size)
    {
	class_size = size;
    }


/*****************************************************************************************/
  public void set_bin_size(double binsize)
/*****************************************************************************************/
    {
	bin_size = binsize;
    }


/*****************************************************************************************/
  public void set_hist_range(double range)
/*****************************************************************************************/
    {
	hist_max = range;
    }


/*****************************************************************************************/
/*****************************************************************************************/
public void set_hist_min(double histmin)

    {
	hist_min = histmin;
    }


/*****************************************************************************************/
/*****************************************************************************************/     
  public void set_number_of_bins()  

    {
	number_of_bins =(int)((hist_max-hist_min)/(double)bin_size);
	System.err.println("There are " + number_of_bins + " bins in the histogram");
    }


/*****************************************************************************************/
/*****************************************************************************************/
  public void set_number_of_runs(int runs)

    {
	number_of_runs = runs;
    }

  
/*****************************************************************************************/
/*****************************************************************************************/
    public void set_val(int row,int column,double value)

    {
	M.set_matrix_val(row,column,value);

    }

    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public void update(int row, double value) {

	int thebin = (int)Math.floor((value - hist_min)/(double)bin_size);

	// make sure we're in the range.
	if (thebin < 0) {
	    thebin = 0;
	}

	if (thebin > number_of_bins - 1) { // this shouldn't happen since we make sure there enough bins. Might give slight speedup.
	    System.err.println("Warning, last bin exceeded!");
	    thebin = number_of_bins - 1;
	}
	M.increment_matrix_val(row, thebin);
    }


    /*****************************************************************************************/
    // convert a raw histogram to a cdf.
    /*****************************************************************************************/
    public void tocdf(int number_of_class, int class_min_size) {

	// todo: get rid of this if we don't use it.
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



    /*****************************************************************************************/
    /*****************************************************************************************/
    public int get_class_size()
    {
	return class_size;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public double get_bin_size()

    {
	return bin_size;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public double get_hist_min()
    {
	return hist_min;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public double get_hist_range()
    {
	return hist_max;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public int get_number_of_bins()

    {
	return number_of_bins;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public int get_number_of_runs()
	
    {
	return number_of_runs;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public Matrix get_matrix()
    {
	return M;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public Map get_matrix_map()
    {
	return list;
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    public int class_index(int class_size, int min_class_size)
    {
	//get corresponding index for each class size
	return class_size - min_class_size;
    }

}
