package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:calculates a background distribution for class sscores derived from randomly selected individual gene scores

                                                                                                                                                            
*******************************************************************************/

import scores.class_score.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;


/** 
 *  input format will be
 *  pval_file,method,class_max_size,class_min_size,number_of_runs,quantile,range
 */
public class exp_class_scores {
    private double[] ug_pval_arr = null; 
    private double[] pvals = null;
    private Map ug_pval_map;
    private Map chip_pval;
    private String method = null;
    private boolean weight_on;
    private int class_max_size = 100;
    private int number_of_runs = 10000;
    private int quantile = 50;
    private double hist_range = 0;
    private double pvalue = 0;
    private int class_min_size = 2;
   
    
    /**  */
    public exp_class_scores(String filename_pval, String wt_check, String in_method)
    {
	Pval_parse parser = new Pval_parse(filename_pval); // makes the probe -> pval map.
	chip_pval = new LinkedHashMap();
	chip_pval = parser.get_map(); // reference to the probe -> pval map.
	ug_pval_map = new HashMap(); // this gets initialized by set_input_pvals
	weight_on = (Boolean.valueOf(wt_check)).booleanValue();
	pvals = parser.get_pval(); // array of pvalues.
	method = in_method;
    }


    /** 
     * Used for methods which require randomly sampling classes to
     * generate a null distribution of scores.
     * @return A histogram object containing a cdf that can be used to generate pvalues.
     */
    public histogram random_class_generator()
    {
	Stats statistics = new Stats();
	
	int i,j,k;

	int gene_length;
	double[] in_pval;
	if (weight_on) {
	    gene_length = Array.getLength(ug_pval_arr);
	    in_pval = ug_pval_arr;
	} else {
	    gene_length = Array.getLength(pvals);
	    in_pval = pvals;
	}

	//	System.err.println("There are " + gene_length + " pvalues to choose from randomly.");

	int number_of_class = class_max_size - class_min_size + 1;
	int[] random = null;
	double[] random_class = new double[gene_length];
	boolean[] recLog = new boolean[gene_length];  //recLog records the numbers been choosed

	histogram hist= new histogram(number_of_class, class_min_size, number_of_runs, hist_range);
	
	//check for method and accordingly generate values 
	for (i = class_min_size; i<class_max_size; i++)
	    //	    System.err.println("Running class size " + i);
	    { 
		random = new int[i];
		for (k =0 ; k< number_of_runs; k++)
		    {
			double total=0.0;
			statistics.chooserandom (random, recLog, gene_length, i);
			
			//			showArray.show(random, i);
			if (method.equals("MEAN_METHOD")) { 
			    for (j= 0; j<i; j++) {
				//System.out.println("random["+j+"]="+random[j]);
				total+=in_pval[random[j]];
			    }
			    total=total/(double)i;
			    
			}  else if (method.equals("QUANTILE_METHOD")) {
			    for (j=0;j<i;j++) {
				random_class[j] = in_pval[random[j]];
				//System.out.println("random_class ["+j+"]= "+random_class[j]+"    random[]= "+random[j]);
			    }
			    double fract = (double)quantile/100.0;
    			    int index = (int)Math.floor( fract*i );
			    total =  statistics.calculate_quantile(index,random_class,i);
			}  else if (method.equals("MEAN_ABOVE_QUANTILE_METHOD")){
			    for (j=0; j<i; j++) {
				random_class[j]= in_pval[random[j]];
			    }
			    double fract = (double)quantile/100.0;
    			    int index = (int)Math.floor( fract*i );			    
			    total = statistics.calculate_mean_above_quantile(index, random_class, i); 
			} else {
			    System.out.println("Invalid method entered");
			    System.exit(0);
			}
			//System.out.println(total);
			//System.out.flush();
			hist.update(i - class_min_size, total);
		    }		
	    }
	
	try { 
	    hist.tocdf(number_of_class, class_min_size);
	} catch(NullPointerException s) {
	    System.out.println("Null pointer Exception");
	    s.printStackTrace();
	} 
	return hist;
    }



    /**  */	
    public void set_class_max_size(int value)
    {
	class_max_size = value;
    }


    /**  */	
    public void set_class_min_size(int value)
    {
	class_min_size = value;
    }



    /**  */	
    public void set_number_of_runs(int value)
    {
	number_of_runs = value;
    }
    
	    
    /**  */	
    public int get_number_of_runs()
    {
	return number_of_runs;
    }	    


    /**  */
    public int get_class_max_size()
    {
	return class_max_size;
    }
    

    /**  */
    public void set_range(double range)
    {
	hist_range =range;
    }

	    
    /**  */
    public void set_pval(double pval)
    {
	pvalue =pval;
    }	    


    /**  */
    public double get_range()
    {
	return hist_range;
    }
	    
    /**  */
    /**  */
    public double get_pval()
    {
	return pvalue;
    }
	    
    /**  */
    public double[] get_pvals()
    {
	return pvals;
    }
	    
    /**  */
    public double[] get_in_pvals()
    {
	return weight_on? ug_pval_arr : pvals;	    	
    }
    
    /**  */
    public int get_class_min_size()
    {
	return class_min_size;
    }


    /**  */	
    public void set_quantile(int value)
    {
	quantile = value;
    }
	    
    /**  */	
    public int get_quantile()
    {
	return quantile;
    }	    

    /**
     * Each pvalue is adjusted to the mean of all the values in the
     * 'replicate group' */
    public void set_input_pvals(Map ugProbeMap)
    {
	Collection ugEntries = ugProbeMap.entrySet(); // map of ug to probes.
	Iterator ugMapItr = ugEntries.iterator();
	double[] ug_pval_temp = new double[ugProbeMap.size()];
	int counter = 0;
	
	while(ugMapItr.hasNext()){
	    Map.Entry ugTuple = (Map.Entry)ugMapItr.next();
	    ArrayList probes = (ArrayList)ugTuple.getValue(); // list of probes in this unigene
	    Iterator pbItr = probes.iterator();
	    int in_size = 0;
	    while(pbItr.hasNext()){
		Object key = chip_pval.get(pbItr.next());  // pvalue for the next probe in this unigene.
		if (key != null) {
		    String pbPval = key.toString();
		    //  ug_pval_temp[counter] += Math.pow(10, -1*Double.parseDouble(pbPval));
		    ug_pval_temp[counter] += Double.parseDouble(pbPval);
		    in_size++;
		}
	    }
	    if(in_size!=0) {
		ug_pval_temp[counter] /= in_size;     // take the mean
		//		ug_pval_temp[counter] = -(Math.log(ug_pval_temp[counter])/Math.log(10));   //transform to -log (base 10) value
		Object obb = ugTuple.getKey();
		Double dbb = new Double(ug_pval_temp[counter]);
		if(ugTuple.getKey()!=null){
		    ug_pval_map.put(obb, dbb);
		}
		counter++;
	    }
	} //end of while       

	double[] ug_pval = new double[counter]; // counter = the number of unigene_id that actually appears in pval file
	for(int i=0; i<counter; i++){
	    ug_pval[i] = ug_pval_temp[i];	
	}
	ug_pval_arr = ug_pval;
    }	    


    /**  */
    public Map get_ug_pval_map() {
	return ug_pval_map;
    }


    /**  */
    public Map get_map() {
	return chip_pval;
    }

    /**  */
    public String get_method() {
	return method;
    }
    

    /**  */	
    public double get_value_map(String chip_id) {
	 double value=0.0;
	 if (chip_pval.get(chip_id)!=null){ 
	    value= Double.parseDouble((chip_pval.get(chip_id)).toString());
	 }
	 return value;
    }

    /**  */
    public static void main (String[] args) {
	histogram hi = new histogram(); 
	Matrix M = null;
	exp_class_scores test = new exp_class_scores(args[0],args[1], args[2]);
	test.set_class_max_size(Integer.parseInt(args[3]));
	test.set_class_min_size(Integer.parseInt(args[4]));
	test.set_number_of_runs(Integer.parseInt(args[5]));
	test.set_quantile(Integer.parseInt(args[6]));
	test.set_range(Double.parseDouble(args[7]));
	hi=test.random_class_generator();
		/*	
	M= new Matrix((hi.get_matrix()).get_num_rows(),(hi.get_matrix()).get_num_cols());
	M=hi.get_matrix();
	double total =0.0;
	for(int i=0;i<M.get_num_rows();i++){
	    total=0.0;
	    for(int j=1;j<M.get_num_cols();j++){
		System.out.print(M.get_matrix_val(i,j) + "\t");
		total=total + M.get_matrix_val(i,j);
	    }
	    System.out.println("total\t" + total);
	    System.out.println("");
	}*/
	
	
    }
}
