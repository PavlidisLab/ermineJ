package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Create background distribution of class correlation where the class correlation is the average pairwise correlation between the vectors in the class
   
*******************************************************************************/






import scores.class_score.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;


/*****************************************************************************************/
// input format will be data_file,class_max_size,class_min_size,number_of_runs,range
public class corr_class_scores {
/*****************************************************************************************/
    private Matrix data =null;
    private int class_max_size = 100;
    private int number_of_runs = 10000;
    private double hist_range =0;
    private int class_min_size =2;
    
   /*****************************************************************************************/ 
    public corr_class_scores(String filename_data)
/*****************************************************************************************/
    {
	data = new Matrix(filename_data);
	
    }

/*****************************************************************************************/
 public histogram random_class_generator()
/*****************************************************************************************/
    {
	int i,j,k;
	int count =0;
	int[] randomnums = null;
	double avecorrel=0.0;
	Stats statistics = new Stats();
	histogram hist= new histogram();
	//create matrix object from probe data file size
	Matrix correls = new Matrix(data.get_num_rows(),data.get_num_rows());
	int number_of_class =class_max_size - class_min_size +1;
	Matrix mat = new Matrix(number_of_class,number_of_runs);
	
	statistics.correl_matrix(data,correls);
	
	for (i=class_min_size; i<=class_max_size; i++) {
	    randomnums = new int[i];
	    for (j=0; j<number_of_runs; j++) {
		avecorrel=0.0;
		statistics.chooserandom_2(randomnums, data.get_num_rows(), i);
		avecorrel = classcorrel(randomnums,correls.get_matrix_double(), i);
		mat.set_matrix_val(count,j,avecorrel);
	    }
	    count++;
	}
	//takes the the matrix having the random values for a particular class and gets its corresponding histogram
	hist.convert_hist(number_of_class,class_min_size,number_of_runs,mat,hist_range);
	return hist;
			
    }



/*****************************************************************************************/
public double classcorrel(int[] randomnums, double[][] correls, int classsize) {/*****************************************************************************************/
    //calculate average correlation
  double avecorrel;
  int i,j, nummeas;
  avecorrel = 0;
  nummeas = 0;
  for (i=0; i < classsize; i++) {
    for (j=i+1; j < classsize; j++) {
      avecorrel +=  Math.abs(correls[ randomnums[i] ][ randomnums[j] ]);
      nummeas++;
    }
  }
  return avecorrel / (double)nummeas ;
}


/*****************************************************************************************/
    public Map get_data_map()
/*****************************************************************************************/
    {
	return data.get_row_Hash();
    }


/*****************************************************************************************/
 public Vector get_data_chip_map(String chip)
/*****************************************************************************************/
    {
	return data.retrieveONEfrom_row_map(chip);
    }

/*****************************************************************************************/
	public void set_class_max_size(int value)
/*****************************************************************************************/
	    {
		class_max_size = value;
	    }
	


/*****************************************************************************************/
	public void set_class_min_size(int value)
/*****************************************************************************************/
	    {
		class_min_size = value;
	    }
	


/*****************************************************************************************/
	public void set_number_of_runs(int value)
/*****************************************************************************************/
	    {
		number_of_runs = value;
	    }


/*****************************************************************************************/
    	public int get_class_max_size()
/*****************************************************************************************/
	    {
		return class_max_size;
	    }


/*****************************************************************************************/
	public void set_range(double range)
/*****************************************************************************************/
	    {
		hist_range =range;
	    }


/*****************************************************************************************/
	public double get_range()
/*****************************************************************************************/
	    {
		return hist_range;
	    }



/*****************************************************************************************/
    	public int get_class_min_size()
/*****************************************************************************************/
	    {
		return class_min_size;
	    }

	







 public static void main (String[] args) {
	histogram t = new histogram(); 
	Matrix M = null;
	corr_class_scores test = new corr_class_scores(args[0]);
	test.set_class_max_size(Integer.parseInt(args[1]));
	test.set_class_min_size(Integer.parseInt(args[2]));
	test.set_number_of_runs(Integer.parseInt(args[3]));
	test.set_range(Double.parseDouble(args[4]));
	t=test.random_class_generator();
	
	M= new Matrix((t.get_matrix()).get_num_rows(),(t.get_matrix()).get_num_cols());
	M=t.get_matrix();
	double total =0.0;
	for(int i=0;i<M.get_num_rows();i++){
	    total=0.0;
	    for(int j=1;j<M.get_num_cols();j++){
		System.out.print(M.get_matrix_val(i,j) + "\t");
		total=total + M.get_matrix_val(i,j);
	    }
	    System.out.println("total\t" + total);
	    System.out.println("");
	}
	
 }



}
