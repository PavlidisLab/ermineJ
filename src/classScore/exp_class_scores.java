package classScore;
import util.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;


/** 
    Calculates a background distribution for class sscores derived
    from randomly selected individual gene scores...and does other
    things.   Created 09/02/02.
    
    @author Shahmil Merchant, Paul Pavlidis
    @version $Id$
    
 
  */
public class exp_class_scores {
    private double[] group_pval_arr = null; // pvalues for groups.
    private double[] pvals = null; // pvalues for probes.
    private Map group_pval_map; // groups -> pvalues
    private Map probe_pval; // probes -> pval
    private boolean weight_on;
    private int class_max_size = 100;
    private int number_of_runs = 10000;
    private int quantile = 50;
    private int number_of_class = 0;
    private double quantfract = 0.5;
    private double hist_max = 0; // todo: this should not be set here, should it? it is in the histogram object
    private int class_min_size = 2;
    private histogram hist = null;
    private boolean logged = true;

    private int method;
    private static final int MEAN_METHOD = 0;
    private static final int QUANTILE_METHOD = 1;
    private static final int MEAN_ABOVE_QUANTILE_METHOD = 2;


    /** 
	Can use when pvalue column is 1, and taking logs, and defaults are ok.
    */
    public exp_class_scores(String filename_pval, String wt_check, String in_method) {
	this(filename_pval, wt_check, in_method, 1, true);
    }
    

    /** 
	Use defaults for most things.  
    */
    public exp_class_scores(String filename_pval, String wt_check, String in_method, int pvalcolumn, boolean dolog)
    {
	this(filename_pval, wt_check, in_method, pvalcolumn, dolog, 100, 2, 10000, 50);
    }


    /** 
	Set everything according to parameters.
	@param filename_pval File that contains the scores for each probe
	@param wt_check Whether weights should be used or not
	@param in_method The class scoring method: Mean, Quantile, etc.
	@param pvalcolumn Which column in the data file contains the scores we will use. The first column contains probe labels and is not counted.
	@param dolog Whether the log of the scores should be used. Use true when working with p-values
	@param class_max_size The largest class that will be considered. This refers to the apparent size.
	@param class_min_size The smallest  class that will be considered. This refers to the apparent size.
	@param number_of_runs How many random trials are done when generating background distributions.
	@param quantile A number from 1-100. This is ignored unless a quantile method is selected.
    */
    public exp_class_scores(String filename_pval, String wt_check, String in_method, int pvalcolumn, boolean dolog, int class_max_size, int class_min_size, int number_of_runs, int quantile)
    {
	this.set_class_max_size(class_max_size);
	this.class_min_size = class_min_size;
	this.number_of_runs = number_of_runs;
	this.set_quantile(quantile);
	this.weight_on = (Boolean.valueOf(wt_check)).booleanValue();
	this.set_method(in_method);

	if (class_max_size < class_min_size) {
	    System.err.println("Error:The maximum class size is smaller than the minimum.");
	    System.exit(1);
	}

	this.number_of_class = class_max_size - class_min_size + 1;
	this.logged = dolog;

	Pval_parse parser = new Pval_parse(filename_pval, pvalcolumn, dolog); // makes the probe -> pval map.
	pvals = parser.get_pval(); // array of pvalues.
	probe_pval = parser.get_map(); // reference to the probe -> pval map.
	group_pval_map = new HashMap(); // this gets initialized by set_input_pvals
    }


    /** 
     * Used for methods which require randomly sampling classes to
     * generate a null distribution of scores.
     * @return A histogram object containing a cdf that can be used to generate pvalues.
     */
    public histogram generateNullDistribution () 
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

	int[] random = null;
	double[] random_class = new double[gene_length];
	boolean[] recLog = new boolean[gene_length];  //recLog records the numbers been choosed

	//	System.err.println(number_of_class + " " +  class_min_size + " " +   number_of_runs + " " +   hist_max);

	
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
			//todo: this is a good place for a switch.
			if (method == MEAN_METHOD) { 
			    for (j= 0; j<i; j++) {
				//System.out.println("random["+j+"]="+random[j]);
				total+=in_pval[random[j]];
			    }
			    total=total/(double)i;
			}  else if (method == QUANTILE_METHOD) {
			    for (j=0;j<i;j++) {
				random_class[j] = in_pval[random[j]];
			    }
			    double fract = (double)quantile/100.0;
    			    int index = (int)Math.floor( fract*i );
			    total =  statistics.calculate_quantile(index,random_class,i);
			}  else if (method == MEAN_ABOVE_QUANTILE_METHOD){
			    for (j=0; j<i; j++) {
				random_class[j]= in_pval[random[j]];
			    }
			    double fract = (double)quantile/100.0;
    			    int index = (int)Math.floor( fract*i );			    
			    total = statistics.calculate_mean_above_quantile(index, random_class, i); 
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
    public void set_range()
    {
	hist_max = Stats.meanOfTop2( weight_on ? group_pval_arr : pvals );
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
	return weight_on ? group_pval_arr : pvals;	    	
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
     * @param Map groupProbeMap Map of groups to probes.
     * @param String gp_method Which method we use to calculate scores
     * for genes that occur more than once in the data set.
     */
    public void set_input_pvals(Map groupProbeMap, String gp_method)
    {
	Collection groupEntries = groupProbeMap.entrySet(); // map of groups -> probes in group
	Iterator groupMapItr = groupEntries.iterator();
	double[] group_pval_temp = new double[groupProbeMap.size()];
	int counter = 0;
	
	while(groupMapItr.hasNext()){
	    Map.Entry groupTuple = (Map.Entry)groupMapItr.next();
	    ArrayList probes = (ArrayList)groupTuple.getValue(); // list of probes in this group
	    Iterator pbItr = probes.iterator();
	    int in_size = 0;
	    while(pbItr.hasNext()) {
		Object key = probe_pval.get(pbItr.next()); // pvalue for the next probe in this group.
		if (key != null) {
		    String pbPval = key.toString();
		    if(gp_method.equals("MEAN_PVAL")){
		        group_pval_temp[counter] += Double.parseDouble(pbPval);
		    } else if(gp_method.equals("BEST_PVAL")){ 
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

	this.set_range(); // figure out the max pvalue possible.
	this.hist = new histogram( number_of_class, class_min_size, number_of_runs, hist_max );
    }


    /**  
	@return Map of groups to pvalues.
    */
    public Map get_group_pval_map() {
	return group_pval_map;
    }


    /** 
	@param shuffle Whether the map should be scrambled first. If
	so, then groups are randomly associated with scores, but the
	actual values are the same.
	
	@return Map of groups to pvalues.
    */
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
    //    public int get_method() {
    //	return method; // todo: make this return the appropriate string..not used anyway
    //}
    

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
	
	if (method == MEAN_METHOD)
	    return Stats.mean(genevalues, effsize);
	else {
	    int index = (int)Math.floor( quantfract * effsize );
	    if ( method == QUANTILE_METHOD ) {
		return  Stats.calculate_quantile(index, genevalues, effsize);            	
	    } else if (method == MEAN_ABOVE_QUANTILE_METHOD ) {
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

    /** */ 
    private void set_method(String meth) {
	if (meth.equals("MEAN_METHOD")) { 
	    method = MEAN_METHOD;
	}  else if (meth.equals("QUANTILE_METHOD")) {
	    method = QUANTILE_METHOD;
	}  else if (meth.equals("MEAN_ABOVE_QUANTILE_METHOD")){
	    method = MEAN_ABOVE_QUANTILE_METHOD;
	} else {
	    System.err.println("Invalid method entered: " + meth); // todo: put this kind of check at the start of the program, not here?
	    System.exit(1);
	}
    }



}
