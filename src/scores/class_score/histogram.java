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
    private double bin_size = 0.01;
    private double hist_min = 0.0;
    private double hist_max = 5.0;
    private int number_of_bins =0;
    private int number_of_runs = 0;
    private int row =0;
    private int column = 0;
    private Matrix M = null;
    private Map list;
   

/*****************************************************************************************/
    public histogram(){}
/*****************************************************************************************/




/*****************************************************************************************/
    public void set_class_size(int size)
/*****************************************************************************************/
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
public void set_hist_min(double histmin)
/*****************************************************************************************/
    {
	hist_min = histmin;
    }


/*****************************************************************************************/
  public void set_number_of_bins()  
/*****************************************************************************************/     
    {
	number_of_bins =(int)((hist_max-hist_min)/(double)bin_size);
    }


/*****************************************************************************************/
  public void set_number_of_runs(int runs)
/*****************************************************************************************/
    {
	number_of_runs = runs;
    }

  
/*****************************************************************************************/
    public void set_val(int row,int column,double value)
/*****************************************************************************************/
    {
	M.set_matrix_val(row,column,value);

    }


/*****************************************************************************************/
    public void convert_hist(int number_of_class,int class_min_size,int number_of_runs,Matrix raw_scores,double range){
/*****************************************************************************************/
    
	set_hist_range(range);
	set_number_of_bins();
	
	M = new Matrix(number_of_class,number_of_bins+1);
	row= number_of_class;
	column =number_of_bins+1;
	int classes = class_min_size;
	double[] bin = new double[number_of_bins];
	long[] count = new long[number_of_bins];
	double v = 0;
	list = new HashMap();
	//list =M.get_row_Hash();
	//initialise bin values
	for (int i=0;i<number_of_bins;i++){ 
	bin[i]=v;
	v+=bin_size;
	}
	//store classes in a map
	int s=0;
	for (s=class_min_size;s<(number_of_class +class_min_size -1);s++){
	    String number = Integer.toString(s);
	    list.put(number,null);
	}

	for(int i =0;i<raw_scores.get_num_rows();i++)
	    {
		//set count value to zero for each bin for each histogram when appropriately called by each class size 
		for (s =0;s<number_of_bins;s++)
		    count[s]=0;
		//		System.out.println("class_size " + classes);
		for (int j=0;j<raw_scores.get_num_cols();j++)
		    {
			
			double value = raw_scores.get_matrix_val(i,j);
			//get each value from matrix and check for range and then increment count correspondingly
			for (int k =0;k<number_of_bins;k++)
			    {
				if( k==(number_of_bins -1)){
				    if (value>=bin[k])
					count[k]+=1;
				    break;
				} else {
				    if(value>=bin[k] && value<bin[k+1])
					{
					    count[k]+=1;
					    break;
					}
				}
			    }
		    }
		//M.set_matrix_val(i,0,classes);
		double sum =0.0;
		
		for (int l =number_of_bins -1;l>=0;l--)
		    {
			//calcualte cdf and also store values in map
			sum+=count[l];

			double convert=(sum/(double)number_of_runs);

			List lis = (List) list.get(Integer.toString(classes));
			if (lis ==null){
			      list.put(Integer.toString(classes),lis= new ArrayList());
			      lis.add(Double.valueOf(Double.toString(convert)));
			} else {
			    list.put(Integer.toString(classes),lis);
			    lis.add(Double.valueOf(Double.toString(convert)));
			}
			M.set_matrix_val(i,l,convert);
			//System.out.println(convert);
		    }
		classes = classes +1;
	    }

    }


/*****************************************************************************************/
  public int get_class_size()
/*****************************************************************************************/
    {
	return class_size;
    }


/*****************************************************************************************/
  public double get_bin_size()
/*****************************************************************************************/
    {
	return bin_size;
    }


/*****************************************************************************************/
  public double get_hist_min()
/*****************************************************************************************/
    {
	return hist_min;
    }


/*****************************************************************************************/
    public double get_hist_range()
/*****************************************************************************************/
    {
	return hist_max;
    }


/*****************************************************************************************/
  public int get_number_of_bins()
/*****************************************************************************************/
    {
	return number_of_bins;
    }


/*****************************************************************************************/
  public int get_number_of_runs()
/*****************************************************************************************/
    {
	return number_of_runs;
    }


/*****************************************************************************************/
  public Matrix get_matrix()
/*****************************************************************************************/
    {
	return M;
    }


/*****************************************************************************************/
 public Map get_matrix_map()
/*****************************************************************************************/
    {
	return list;
    }


/*****************************************************************************************/
    public int class_index(int class_size)
/*****************************************************************************************/
    {
	//get corresponding index for each class size 
	int value=0;
	for(int i=0;i<M.get_num_rows();i++){
	    if (M.get_matrix_val(i,0) == class_size){
		value =i;
		break;
	    }
	}
	return value;
    }

  

}
