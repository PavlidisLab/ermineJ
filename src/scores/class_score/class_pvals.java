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
    private Map ug_name;
    private Map probe_ug;
    private Map ug_pval_map;
    private exp_class_scores probe_pval;
    private GoName_parse goName;
    private Ug_Parse ugName;
    private double user_pvalue;
    private String dest_file;
    private boolean weight_on = false;

    // command line arguments in the following way
    // pval_file,affy_go_file,Go_name_file,destination_file,ug_file,method,class_max_size,class_min_size,number of runs,quantile, p-value, weightcheck
  public static void main (String[] args) {
      class_pvals test = new class_pvals(args[0],args[1],args[2],args[3],args[4],args[5],Integer.parseInt(args[6]),Integer.parseInt(args[7]),Integer.parseInt(args[8]),Integer.parseInt(args[9]), Double.parseDouble(args[10]),args[11]);
      test.class_pval_generator();
     }
     


/*****************************************************************************************/
    public class_pvals(String probe_pvalfile,String affy_gofile,String go_namefile,String destination_file,String ug_file,String method,int class_max_size,int class_min_size,int number_of_runs,int quantile, double pval, String wt_check){
/*****************************************************************************************/

	affy_go_Parse  affy_go = new affy_go_Parse(affy_gofile);//parses affy file
	goName = new GoName_parse(go_namefile); //parse go name file
	ugName = new Ug_Parse(ug_file); //parse ug file
	probe_go = new LinkedHashMap();
	go_probe = new LinkedHashMap();
	go_name = new LinkedHashMap();
	ug_name = new LinkedHashMap();
	probe_ug = new LinkedHashMap();
	ug_pval_map = new LinkedHashMap();
	ugName.chip_repeat();
	probe_go =affy_go.get_affy_map();//probe go map
	go_probe =affy_go.get_go_map();//go probe map
	go_name = goName.get_GoName_map();//go name map
	ug_name = ugName.get_chip_map();//ug map
	probe_ug =ugName.get_chip_ug_map();//ug chip map
	user_pvalue = -(Math.log(pval)/Math.log(10));//user defined pval
	weight_on =(Boolean.valueOf(wt_check)).booleanValue();
	hist = new histogram();
	probe_pval = new exp_class_scores(probe_pvalfile, wt_check, method); //calculates histogram and parse pval file
	ug_pval_map = probe_pval.get_ug_pval_map();
	probe_pval.set_class_max_size(class_max_size);
	probe_pval.set_class_min_size(class_min_size);
	probe_pval.set_number_of_runs(number_of_runs);
	probe_pval.set_quantile(quantile);
	if(weight_on)
	    probe_pval.set_input_pvals(ugName.get_ug_chip_map());
	//probe_pval.set_range(range);
	probe_pval.set_range(Stats.meanOfTop2(probe_pval.get_in_pvals()));
	dest_file=destination_file;
	//calculate random classes
	hist = probe_pval.random_class_generator();
    }
    

/*****************************************************************************************/
    //use the objects created from constructor 
    public void class_pval_generator()
/*****************************************************************************************/
    {
	int N1 = 0;
	int N2 = 0;  //the size of hypergeometric distrbution's two catagories based on user_pval
	Map chips = probe_pval.get_map();
	// calculate N1 and N2
	Collection inp_entries = weight_on ? ug_pval_map.entrySet() : chips.entrySet();
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
	Map input_rank_map = weight_on ? Stats.rankOf(ug_pval_map) : Stats.rankOf(chips);
	int inputSize = input_rank_map.size();
	double minPval = 0.5 / (double)probe_pval.get_number_of_runs();
	
	Collection entries = go_probe.entrySet();
	Iterator it = entries.iterator();
	//to store each class number
	Map class_list = new LinkedHashMap();
	Matrix M= new Matrix((probe_pval.get_class_max_size()-probe_pval.get_class_min_size() +1),hist.get_number_of_bins());
	M= hist.get_matrix();

	class_list=hist.get_matrix_map();
 try {
        BufferedWriter out = new BufferedWriter(new FileWriter(dest_file, true));
	if (weight_on == false){
	   out.write("class" + "\t" + "size" + "\t" + "raw score" + "\t" + "pval " + "\t" + "hyper pval" + "\t" + "aroc rate" + "\t" +"\n");
	} else {
	   out.write("class" + "\t" + "size" + "\t" + "raw score" + "\t" + "pval " + "\t" + "virtual_size" + "\t" + "hyper pval" + "\t" + "aroc rate" + "\t" + "\n");
	}
	//get each class values at a time and iterate through each value and calulate
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    String class_name = (String)e.getKey();
	    ArrayList values =(ArrayList)e.getValue();	
	    Iterator I =values.iterator();
            
            //variables for claculations
            Map record = new HashMap();       // to record those unigenes that have been used
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
	    
	    while(I.hasNext()){
		String element = (String)I.next();  //store probe id
		//System.out.println("   element :" + element);
		if (element !=null){
		    if(chips.containsKey(element)){
		        size++;
		        
        		    if (weight_on == true) { //routine for weights
        		    	if(method.equals("MEAN_METHOD")){
        			    ArrayList chip_list = new ArrayList();
        			    chip_list=ugName.get_chip_value_map(element);
        			    
        			    if (chip_list.isEmpty()) {
        			        weight = 1;
        			    }else {
        			        weight = 1/(double)(chip_list.size() +1);
        			    }
        			    
        			    raw_score=0.0;
        			    raw_score = probe_pval.get_value_map(element);
        			    //get value from map and calcualte total
        			    if(Double.toString(raw_score) !=null){
        			        total +=  raw_score*weight;
        			    } 
        			}
        			//compute pval for every unigene class
        			Object ugpval = ug_pval_map.get(probe_ug.get(element));
        			if(ugpval != null){		        
        			    if(!record.containsKey(probe_ug.get(element))){
        			    	record.put(probe_ug.get(element), null);
        			        ugPvalArr[v_size] = Double.parseDouble(ugpval.toString()); //pval of one unigene in the class
        			        
        			        if(ugPvalArr[v_size] >= user_pvalue){
        			            n1++;	
        			        }else{
        			            n2++;
        			        }
        			        v_size++;
        			    }
        		        }
        		        
        		        //for aroc
        		        Object ranking = input_rank_map.get(probe_ug.get(element));
        		        if (ranking!=null){
        		           target_ranks.put(ranking, null);
        		        }
        		    } else {//no weights
        		        if(method.equals("MEAN_METHOD")){
        		            raw_score=0.0;
        		            raw_score = probe_pval.get_value_map(element);
        		            //get value from map and calcualte total
        		            if(Double.toString(raw_score) !=null){
        		                total +=  raw_score;
        		            } 
                        	}
                                //compute pval for every GO class
        			Object pbpval = chips.get(element);
        			if(pbpval != null){
        			    //System.out.println("Get it! " + element);
        			    double pb_pvalue = Double.parseDouble(pbpval.toString()); //pval of one unigene in the class
        			    if(pb_pvalue >= user_pvalue){
        			        n2++;
        			        //System.out.println("Get it! " + class_name + "     " + element + "  " + pb_pvalue);
        			    }else{
        			        n1++;
        			    }
        		        }//else{System.out.println("Not it! " + element);}
        		        //for roc
        		        Object ranking = input_rank_map.get(element);
        		        if(ranking!=null){
        		           //if(class_name.equals("GO:0000051"))
        		               //System.out.println("rank of "+ class_name + " on  element "+element+" =  " + Integer.parseInt(ranking.toString()));
        		            target_ranks.put(ranking, null);
        		        }
        		    }
        		}//end of null check
        	}
	    }
	    
	    int in_size = weight_on? v_size : size;
	    
	    if(in_size < probe_pval.get_class_min_size() || in_size > probe_pval.get_class_max_size())
	        continue;
	    
	    if(in_size != 0){
	       if(method.equals("MEAN_METHOD"))
                  rawscore=total/in_size;
                  
               area_under_roc = Stats.arocRate(inputSize, target_ranks);		    
               
               if(method.equals("QUANTILE_METHOD")){
		    double fract = (double)probe_pval.get_quantile()/100.0;
    		    int index = (int)Math.floor( fract*in_size );
    		    double[] pvalArr = weight_on ? ugPvalArr : probe_pval.get_pvals();   // **wrong when weight_on == false **
		    rawscore =  Stats.calculate_quantile(index,pvalArr,in_size);            	
	       }
               if (rawscore <hist.get_hist_range()) {
                   double[] class_row = new double[hist.get_number_of_bins()];
                 
                   if (weight_on == true) { 
                       class_row=M.get_ith_row(hist.class_index(v_size));  //(int)(Math.round(weight)))); 
                   } else {
               	       class_row=M.get_ith_row(hist.class_index(size));
                   }
                   int binnum = (int)Math.floor((rawscore - hist.get_hist_min()) / (double)hist.get_bin_size());
                   pval = class_row[binnum];
               } else {
               }
               
               hyper_pval = Stats.hyperPval(N1, n1, N2, n2);   //geometric distribution calculations
            }  
            if(pval == 0)
               pval = minPval;
              
            if (weight_on == true) { 
                out.write(goName.get_GoName_value_map(class_name) +"(" + class_name + ")" + "\t" + size + "\t" + rawscore + "\t" +pval + "\t" + v_size + "\t"+ hyper_pval + "\t" + area_under_roc + "\n");
                //out.write( size + "\t" + rawscore + "\t" +pval + "\t" + v_size + "\t"+ hyper_pval + "\t" + area_under_roc + "\t" + goName.get_GoName_value_map(class_name) + "\t" + "(" + class_name + ")" +"\n");
            } else {
                out.write(goName.get_GoName_value_map(class_name) +"(" + class_name + ")" + "\t" + size + "\t" + rawscore + "\t" +pval + "\t" + hyper_pval + "\t" + area_under_roc +"\n");
                //out.write(size + "\t" + rawscore + "\t" +pval + "\t" + hyper_pval + "\t" + area_under_roc + "\t" + goName.get_GoName_value_map(class_name) +"(" + class_name + ")" + "\n");
            }
            if(class_name.equals("GO:0000051"))System.out.println("N1=" + N1 + "  N2=" + N2 + "  n1=" + n1 + "  n2=" + n2);
	}
	out.close();
 } catch (IOException e) {
    }
	System.out.println("inputSize is "+ inputSize);
    }

}









