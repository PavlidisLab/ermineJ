package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant, Paul Pavlidis
  Created :09/02/02
  Revision History: $Id$
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
    private double[] group_pval_arr = null; // pvalues for groups.
    private double[] pvals = null; // pvalues for probes.
    private Map group_pval_map; // groups -> pvalues
    private Map probe_pval; // probes -> pval
    private String method = null;
    private boolean weight_on;
    private int class_max_size = 100;
    private int number_of_runs = 10000;
    private int quantile = 50;
    private double quantfract = 0.5;
    private double hist_max = 0; // todo: this should not be set here, should it? it is in the histogram object
    private int class_min_size = 2;
    private histogram hist = null;

    /** Can use when pvalue column is 1, and taking logs. */
    public exp_class_scores(String filename_pval, String wt_check, String in_method) {
	this(filename_pval, wt_check, in_method, 1, true);
    }
    

    /** Use defaults for most things.  */
    public exp_class_scores(String filename_pval, String wt_check, String in_method, int pvalcolumn, boolean dolog)
    {
	Pval_parse parser = new Pval_parse(filename_pval, pvalcolumn, dolog); // makes the probe -> pval map.
	probe_pval = new LinkedHashMap();
	probe_pval = parser.get_map(); // reference to the probe -> pval map.
	group_pval_map = new HashMap(); // this gets initialized by set_input_pvals
	weight_on = (Boolean.valueOf(wt_check)).booleanValue();
	hist = new histogram();
	pvals = parser.get_pval(); // array of pvalues.
	method = in_method;
    }


    /** Set everything.  */
    public exp_class_scores(String filename_pval, String wt_check, String in_method, int pvalcolumn, boolean dolog, int class_max_size, int class_min_size, int number_of_runs, int quantile)
    {
	this(filename_pval, wt_check, in_method, pvalcolumn, dolog);
	this.set_class_max_size(class_max_size);
	this.set_class_min_size(class_min_size);
	this.set_number_of_runs(number_of_runs);
	this.set_quantile(quantile);
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

	// do the right thing if we are using weights.
	if (weight_on) {
	    gene_length = Array.getLength(group_pval_arr);
	    in_pval = group_pval_arr;
	} else {
	    gene_length = Array.getLength(pvals);
	    in_pval = pvals;
	}

	if (gene_length <= 0) {
	    System.err.println("No pvalues!");
	    System.exit(1);
	}

	//	System.err.println("There are " + gene_length + " pvalues to choose from randomly.");

	int number_of_class = class_max_size - class_min_size + 1;
	int[] random = null;
	double[] random_class = new double[gene_length];
	boolean[] recLog = new boolean[gene_length];  //recLog records the numbers been choosed

	//	System.err.println(number_of_class + " " +  class_min_size + " " +   number_of_runs + " " +   hist_max);
	hist= new histogram(number_of_class, class_min_size, number_of_runs, hist_max);
	
	//check for method and accordingly generate values 
	for (i = class_min_size; i<=class_max_size; i++)
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
			    System.err.println("Invalid method entered: " + method); // todo: put this kind of check at the start of the program, not here?
			    System.exit(1);
			}
			//System.out.println(total);
			//System.out.flush();
			hist.update(i - class_min_size, total);
		    }		
	    }
	
	try { 
	    hist.tocdf(number_of_class, class_min_size);
	} catch(NullPointerException s) {
	    System.err.println("Null pointer Exception");
	    s.printStackTrace();
	} catch(ArrayIndexOutOfBoundsException s) {
	    System.err.println("ArrayIndexOutOfBoundsException");
	    s.printStackTrace();
	} 
	return hist;
    }



    /**  */	
    public void set_class_max_size(int value)
    {
	class_max_size = value;
    }


    /**  
    */	
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
	hist_max =range;
    }

	    
    /**  */
    public double get_range()
    {
	return hist_max;
    }
	    
	    
    /**  */
    public double[] get_pvals()
    {
	return pvals;
    }
	    
    /**  */
    public double[] get_in_pvals()
    {
	return weight_on? group_pval_arr : pvals;	    	
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
	quantfract = (double)quantile/100.0; 
    }
	    
    /**  */	
    public int get_quantile()
    {
	return quantile;
    }	    

    /**
     * Each pvalue is adjusted to the mean (or best) of all the values in the
     * 'replicate group' 
     */
    public void set_input_pvals(Map groupProbeMap, String gp_method)
    {
	Collection groupEntries = groupProbeMap.entrySet(); // map of ug to probes.
	Iterator groupMapItr = groupEntries.iterator();
	double[] group_pval_temp = new double[groupProbeMap.size()];
	int counter = 0;
	
	while(groupMapItr.hasNext()){
	    Map.Entry groupTuple = (Map.Entry)groupMapItr.next();
	    ArrayList probes = (ArrayList)groupTuple.getValue(); // list of probes in this unigene
	    Iterator pbItr = probes.iterator();
	    int in_size = 0;
	    while(pbItr.hasNext()) {
		Object key = probe_pval.get(pbItr.next());  // pvalue for the next probe in this unigene.
		if (key != null) {
		    String pbPval = key.toString();
		    if(gp_method.equals("MEAN_PVAL")){
		        group_pval_temp[counter] += Double.parseDouble(pbPval);
		    } else if(gp_method.equals("BEST_PVAL")){//use best method for group
		        group_pval_temp[counter] = Math.max(Double.parseDouble(pbPval),group_pval_temp[counter]);
		    } else {
			System.err.println("Illegal selection for groups score method. Valid choices are MEAN_PVAL and BEST_PVAL");
			System.exit(1);
		    }
		    in_size++;
		}
	    }
	    if(in_size!=0) {
	    	if(gp_method.equals("MEAN_PVAL"))
		    group_pval_temp[counter] /= in_size; // take the mean
		Object obb = groupTuple.getKey();
		Double dbb = new Double(group_pval_temp[counter]);
		if(groupTuple.getKey() != null){
		    group_pval_map.put(obb, dbb);
		}
		counter++;
	    }
	} //end of while       

	double[] group_pval = new double[counter]; // counter = the number of group_id that actually appears in pval file
	for(int i=0; i<counter; i++){
	    group_pval[i] = group_pval_temp[i];
	}
	group_pval_arr = group_pval;
    }


    /**  */
    public Map get_group_pval_map() {
	return group_pval_map;
    }

    /**  */
    public Map get_group_pval_map(boolean shuffle) {
	if (shuffle) {
	    Map scrambled_map = new LinkedHashMap();
	    Set keys = group_pval_map.keySet();
	    Iterator it = keys.iterator();
	    
	    Collection values = group_pval_map.values();
	    Vector valvec = new Vector(values);
	    Collections.shuffle(valvec);

	    // randomly associate keys and values
	    int i = 0;
	    while (it.hasNext()) {
		scrambled_map.put(it.next(), valvec.get(i));
		i++;
	    }
	    return scrambled_map;

	} else {
	    return group_pval_map;
	}
    }


    /**  */
    public Map get_map() {
	return probe_pval;
    }


    /**  */
    public Map get_map(boolean shuffle) {
	
	if (shuffle) {
	    Map scrambled_probe_pval_map = new LinkedHashMap();
	    
	    Set keys = probe_pval.keySet();
	    Iterator it = keys.iterator();
	    
	    Collection values = probe_pval.values();
	    Vector valvec = new Vector(values);
	    Collections.shuffle(valvec);
	    
	    // randomly associate keys and values
	    int i = 0;
	    while (it.hasNext()) {
		scrambled_probe_pval_map.put(it.next(), valvec.get(i));
		//		 System.err.println(it.next() + " " + valvec.get(i));
		i++;
	     }
	    return scrambled_probe_pval_map;
	    
	} else {
	    return probe_pval;
	}
     }


    /**  */
    public String get_method() {
	return method;
    }
    

    /**  */	
    public double get_value_map(String probe_id) {
	 double value=0.0;
	 if (probe_pval.get(probe_id)!=null){ 
	    value= Double.parseDouble((probe_pval.get(probe_id)).toString());
	 }
	 return value;
    }

    /**  

    Basic method to calculate the raw score, given an array of the gene scores for items in the class.

    */	
    public double calc_rawscore (double[] genevalues, int effsize) {
	
	if (method.equals("MEAN_METHOD"))
	    return Stats.mean(genevalues, effsize);
	else {
	    int index = (int)Math.floor( quantfract * effsize );
	    if ( method.equals("QUANTILE_METHOD") ) {
		return  Stats.calculate_quantile(index, genevalues, effsize);            	
	    } else if (method.equals("MEAN_ABOVE_QUANTILE_METHOD")) {
		return  Stats.calculate_mean_above_quantile(index, genevalues, effsize);            	
	    } else {
		System.err.println("Illegal method selected, somehow");
		return -1.0;
	    }
	}
    }


    /** */
    public histogram get_hist() {
	return hist;
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
