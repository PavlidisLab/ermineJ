package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:calculates the raw pvals using a background distribution

                                                                                                                                                            
*******************************************************************************/
import scores.class_score.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/*****************************************************************************************/
public class class_pvals {
/*****************************************************************************************/
    private histogram hist ;
    private Map probe_go;
    private Map go_probe;
    private Map go_name;
    //    private Map ug_name;
    private Map probe_ug;
    private Map ug_pval_map;
    private exp_class_scores probe_pval;
    private GoName_parse goName;
    private Ug_Parse ugName;
    private double user_pvalue;
    private String dest_file;
    private boolean weight_on = true;
    private boolean dolog = true;

    // command line arguments in the following way
    // pval_file,affy_go_file,Go_name_file,destination_file,ug_file,method,groupMethod,class_max_size,class_min_size,number of runs,quantile, p-value, weightcheck
    public static void main (String[] args) {
	class_pvals test = new class_pvals(args[0],args[1],args[2],args[3],args[4],args[5],args[6],Integer.parseInt(args[7]),Integer.parseInt(args[8]),Integer.parseInt(args[9]),Integer.parseInt(args[10]), Double.parseDouble(args[11]),args[12], Integer.parseInt(args[13]), args[14]);
	test.class_pval_generator();
    }

    /*****************************************************************************************/
    /*****************************************************************************************/
    public class_pvals(String probe_pvalfile, String affy_gofile, String go_namefile, String destination_file, String ug_file, String method, String groupMethod, int class_max_size, int class_min_size,int number_of_runs,int quantile, double pval, String wt_check, int pvalcolumn, String dolog_check ) {

	affy_go_Parse affy_go = new affy_go_Parse(affy_gofile);//parses affy file. Yields map of probe->go
	goName = new GoName_parse(go_namefile); // parse go name file
	
	probe_go = new LinkedHashMap();
	go_probe = new LinkedHashMap();
	go_name = new LinkedHashMap();
	//	ug_name = new LinkedHashMap(); // not used anywhere?
	probe_ug = new LinkedHashMap();
	ug_pval_map = new LinkedHashMap();

	probe_go = affy_go.get_affy_map(); //probe go map
	go_probe = affy_go.get_go_map(); //go probe map
	go_name = goName.get_GoName_map(); //go name map
	//	ug_name = ugName.get_chip_map(); //ug map (chip_repeat_val) todo: this isn't used anywhere.??
	user_pvalue = -(Math.log(pval)/Math.log(10));//user defined pval (cutoff)
	weight_on =(Boolean.valueOf(wt_check)).booleanValue();
	dolog = (Boolean.valueOf(dolog_check)).booleanValue();

	hist = new histogram();
	probe_pval = new exp_class_scores(probe_pvalfile, wt_check, method, pvalcolumn, dolog);

	probe_pval.set_class_max_size(class_max_size);
	probe_pval.set_class_min_size(class_min_size);
	probe_pval.set_number_of_runs(number_of_runs);
	probe_pval.set_quantile(quantile);

	ugName = new Ug_Parse(ug_file, probe_pval.get_map()); //parse ug file. yields map of probe->replicates. Probes which have no replicates are not listed.
	ugName.chip_repeat(); // this IS used.
	probe_ug = ugName.get_chip_ug_map(); //ug chip map  (chip_ug_map -- map of probes to unigene
	
	
	if(weight_on)
	    probe_pval.set_input_pvals(ugName.get_ug_chip_map(), groupMethod); // this initializes the ug_pval_map.

	ug_pval_map = probe_pval.get_ug_pval_map(); // the ug_pval_map
						    // is empty if
						    // weight_on is
						    // false.

	probe_pval.set_range(Stats.meanOfTop2(probe_pval.get_in_pvals()));
	dest_file=destination_file;

	System.out.println("Read in the files and parameters");

	//calculate random classes
	hist = probe_pval.random_class_generator();
    }
    

    /*****************************************************************************************/
    // use the objects created from constructor
    /*****************************************************************************************/
    public void class_pval_generator()
    {
	int N1 = 0;
	int N2 = 0;  //the size of hypergeometric distrbution's two catagories based on user_pval
	Map chips = probe_pval.get_map(); // probe->pval map.

	// ug_pval_map is actually empty unless weight_on is true. This means
	// that we have to ensure that class_pval_generator is only called after
	// set_input_pvals is called in exp_class_scores
	Collection inp_entries;
	Map input_rank_map;
	if (weight_on) {
	    inp_entries = ug_pval_map.entrySet();
	    input_rank_map = Stats.rankOf(ug_pval_map);
	} else {
	    inp_entries = chips.entrySet();
	    input_rank_map =  Stats.rankOf(chips);
	}

	// calculate N1 and N2
	Iterator itr = inp_entries.iterator();
	while(itr.hasNext()){
	   Map.Entry m = (Map.Entry)itr.next();	
	   double ugval = Double.parseDouble((m.getValue()).toString());
	   if(ugval >= user_pvalue)
	      N1++;
	   else
	      N2++;
	}
	
	String method = probe_pval.get_method();
	double minPval = 0.5 / (double)probe_pval.get_number_of_runs(); // the best possible score.

	int inputSize = input_rank_map.size();
	
	Collection entries = go_probe.entrySet(); // go -> probe map. Entries are the class names.
	Iterator it = entries.iterator();
	//to store each class number
	Map class_list = new LinkedHashMap();
	Matrix M= new Matrix((probe_pval.get_class_max_size()-probe_pval.get_class_min_size() +1),hist.get_number_of_bins());
	M = hist.get_matrix();

	class_list=hist.get_matrix_map(); // list of all class sizes.
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(dest_file, false));

	    // headings
	    out.write("class" + "\t" + "size" + "\t" + "raw score" + "\t" + "pval " + "\t" + "virtual_size" + "\t" + "hyper pval" + "\t" + "aroc rate" + "\t" + "rocpval" + "\n");

	    // get each class values at a time and iterate through each value and calulate
	    while(it.hasNext()) { // for each class.
		Map.Entry e = (Map.Entry)it.next();
		String class_name = (String)e.getKey();
		//		System.err.println(class_name);

		ArrayList values =(ArrayList)e.getValue();  // items in the class.
		Iterator I = values.iterator();
		
		//variables for claculations
		Map record = new HashMap(); // to record those unigenes that have been used
		Map target_ranks = new HashMap();
		double[] ugPvalArr = new double[values.size()];
		int above_pval_counter = 0;
		int below_pval_counter = 0;
		double weight =0.0;
		double raw_score=0.0;
		double total =0.0;
		int n1 = 0;
		int n2 = 0;      //inputs for hypergeometric distribution
		
		//variables for outputs
		int size =0;
		int v_size =0;
		double pval=0.0;
		double rawscore=0.0;
		double hyper_pval = -1.0;
		double area_under_roc = 0.0;
		double roc_pval = 0.0;

		while(I.hasNext()){ // foreach item in the class.
		    String element = (String)I.next();  // probe id
		    if (element !=null){
			if(chips.containsKey(element)){ // if it is in the data set. pp: todo This seems inconsistent with the choice of inp_entries?
			    size++;
			    
			    if (weight_on == true) { //routine for weights
				/*
				if(method.equals("MEAN_METHOD")){
				    ArrayList chip_list = new ArrayList();
				    chip_list = ugName.get_chip_value_map(element); // list of repeat members.


				    if (chip_list == null) {
					// probably this is because there are no replicates. todo: this is a bug work around.
					weight = 1;
				    } else if (chip_list.isEmpty()) {
					weight = 1;
				    } else {
					weight = 1/(double)(chip_list.size() + 1 ); // weight per member of this group. todo: BUG This is not the right size if the data set is not complete.
        			    }
				    

				    
				    raw_score =  probe_pval.get_value_map(element); // raw score of the probe, not of the class.
				    //get value from map and calcualte total
				    if(Double.toString(raw_score) !=null) {
					total +=  raw_score*weight;
				    }

				    // debug: test class.
				    if (class_name.equals("GO:0006783")) {
					//	System.err.println(element + "\t" + weight + "\t" +  raw_score + "\t" +  total );
				    }

				}*/

        			//compute pval for every unigene class
				Object ugpval = ug_pval_map.get(probe_ug.get(element)); // probe -> ug
				if (ugpval != null) {  
				    if(!record.containsKey(probe_ug.get(element))){ // if we haven't done this probe already.
					record.put(probe_ug.get(element), null); // mark it as done.
					ugPvalArr[v_size] = Double.parseDouble(ugpval.toString()); // pval of one unigene in the class. This is only used by the quantile methods.
					
					total += ugPvalArr[v_size];
					
					if(ugPvalArr[v_size] >= user_pvalue) { // part of the hypergeometic calc.
					    n1++;
					} else {
					    n2++;
					}
					v_size++; // this is used in any case.
				    }
				}
				
				//for aroc
				Object ranking = input_rank_map.get(probe_ug.get(element)); // rank of this probe.
				if (ranking!=null){
				    target_ranks.put(ranking, null); // ranks of items in this class.
				}
			    } else {//no weights
				if(method.equals("MEAN_METHOD")){
				    raw_score = probe_pval.get_value_map(element);
				    //get value from map and calcualte total
				    if(Double.toString(raw_score) != null){
					total += raw_score;
				    } 
				}
				
				//compute pval for every GO class
				Object pbpval = chips.get(element);
				if(pbpval != null){
				    double pb_pvalue = Double.parseDouble(pbpval.toString()); //pval of one unigene in the class
				    if(pb_pvalue >= user_pvalue){
					n2++;
				    } else {
					n1++;
				    }
				}
				//for roc
				Object ranking = input_rank_map.get(element);
				if(ranking!=null){
				    target_ranks.put(ranking, null);
				}
			    } // weights...
			}
		    } // end of null check
		} // end of while I has next. - over items in the class.
		
		int in_size = weight_on? v_size : size;
		
		if(in_size < probe_pval.get_class_min_size() || in_size > probe_pval.get_class_max_size()) {
		    //		    System.err.println("Skipping class size " + in_size);
		    continue;
		}

		int binnum = 0; // I put this here so it remains in scope for error messages following this block. pp
		
		if(in_size != 0) {

		    if(method.equals("MEAN_METHOD"))
			rawscore=total/in_size;
		    
		    else if( method.equals("QUANTILE_METHOD") ) {
			double fract = (double)probe_pval.get_quantile()/100.0;
			int index = (int)Math.floor( fract*in_size );
			double[] pvalArr = weight_on ? ugPvalArr : probe_pval.get_pvals();   // **wrong when weight_on == false ** todo -- PP figure out what this means.
			rawscore =  Stats.calculate_quantile(index,pvalArr,in_size);            	
		    } else if (method.equals("MEAN_ABOVE_QUANTILE_METHOD")) {
			double fract = (double)probe_pval.get_quantile()/100.0;
			int index = (int)Math.floor( fract*in_size );
			double[] pvalArr = weight_on ? ugPvalArr : probe_pval.get_pvals();   
			rawscore =  Stats.calculate_mean_above_quantile(index,pvalArr,in_size);            	
		    } else {
		    }

		    if (rawscore < hist.get_hist_range() && rawscore > hist.get_hist_min() ) {

 			int row =  hist.class_index(in_size, probe_pval.get_class_min_size());
			binnum = (int)Math.floor((rawscore - hist.get_hist_min()) / (double)hist.get_bin_size());

			if (binnum < 0) 
			    binnum = 0;

			if (binnum > (hist.get_hist_range() - hist.get_hist_min())/hist.get_bin_size()) // todo: calculate this only once.
			    binnum = (int)Math.floor(hist.get_hist_range()/hist.get_bin_size());

			// todo: the need for this check is indicative of a problem
			if (row > M.get_num_rows() - 1 || binnum > M.get_num_cols() - 1) {
			    System.err.println("Warning, a rawscore yielded a bin number which was out of the range: Classname: " + class_name + " row: " + row + " bin number: " + binnum);
			    continue;
			}

			pval = M.get_matrix_val(row, binnum);

						if (class_name.equals("GO:0006783")) {
						    //						    System.err.println(class_name + ": " + "binnum: " + binnum + " pval: " + pval + " row: " + row + " rawscore: " + rawscore + " class size: " + v_size + " bin size" + (double)hist.get_bin_size() + " hist min: " + hist.get_hist_min() );
						}


		    } else {
			System.err.println("Warning, a raw score (" + rawscore + ", " + class_name + ") out of the histogram range was encountered ");
			pval = -1.0;
		    }
		    
		    area_under_roc = Stats.arocRate(inputSize, target_ranks);
		    roc_pval = Stats.rocpval(target_ranks, area_under_roc); 
		    hyper_pval = Stats.hyperPval(N1, n1, N2, n2);
		}
		
		if(pval == 0.0)
		    pval = minPval;
		
		if (Double.isNaN(pval))
		    System.err.println("Warning, a pvalue was not a number (raw score = " + rawscore + ", virtual class size = " + in_size + ", " + "binnum: " + binnum + ", " + class_name +  ")");

		String classname = goName.get_GoName_value_map(class_name);
		String fixnamea;
		String name;
		if (classname != null) {
		    fixnamea = classname.replace(' ', '_'); // make the format compatible with the perl scripts Paul wrote.
		    name = fixnamea.replace(':', '-'); // todo: figure out why this doesn't work.
		} else {
		    name = "";
		}

		if (weight_on == false) {
		    v_size = size;
		}
		//todo: make column order compatible with the perl scripts.
		out.write(name +"_" + class_name + "" + "\t" + size + "\t" + rawscore + "\t" + pval + "\t" + v_size + "\t" + hyper_pval + "\t" + area_under_roc + "\t" + roc_pval +"\n");

	    } // end of while it has next (classes)
	    out.close();
	} catch (IOException e) {
	    System.err.println("There was an IO error");
	    ; // todo: do something
	}
	System.err.println("inputSize is "+ inputSize);
    }
}

