package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Created :09/02/02
  Revision History: $Id$
  Description:calculates the raw average class correlations using a background d  istribution

                                                                                                                                                            
*******************************************************************************/
import scores.class_score.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
     
/*****************************************************************************************/
public class class_correls {
/*****************************************************************************************/
//histogram object stores background related information
    private histogram hist ;
    // stores information regarding random values
    private corr_class_scores probe_data;
    //stores probe->go Hashtable 
    private Map probe_gom;
    //stores go->probe Hashtable
    private Map go_probe;
    private String dest_file;
    private Map go_name;
    private GoName_parse goName;

    // command line arguments in the following way
    // data_file,probe_go_file,go_namefile,output_file,class_max_size,class_min_size,number of runs,hist range
  public static void main (String[] args) {
      class_correls test = new class_correls(args[0],args[1],args[2],args[3],Integer.parseInt(args[4]),Integer.parseInt(args[5]),Integer.parseInt(args[6]),Double.parseDouble(args[7]));
      test.class_correl_generator();
     }
     

/*****************************************************************************************/
    public class_correls(String probe_datafile,String probe_gofile,String go_namefile,String destination_file,int class_max_size,int class_min_size,int number_of_runs,double range){
/*****************************************************************************************/

	probe_data = new corr_class_scores(probe_datafile); // main data file

	ClassMap probe_go = new ClassMap(probe_gofile);//parses affy file
	goName = new GoName_parse(go_namefile); //parse go name file
	probe_gom = new LinkedHashMap();
	go_probe = new LinkedHashMap();
	go_name = new LinkedHashMap();
	probe_gom =probe_go.get_probe_map();//probe go map
	go_probe =probe_go.get_class_map();//go probe map
	go_name = goName.get_GoName_map();//go name map
	hist = new histogram();
	//set histogram parameters
	probe_data.set_class_max_size(class_max_size);
	probe_data.set_class_min_size(class_min_size);
	probe_data.set_number_of_runs(number_of_runs);
	probe_data.set_range(range);
	dest_file=destination_file;
	//calculate random classes
	hist = probe_data.random_class_generator();
    }

/*****************************************************************************************/
  //use the objects created from constructor 
    public void class_correl_generator()
/*****************************************************************************************/
    {
	Stats statistics = new Stats();
	Collection entries = go_probe.entrySet();
	Iterator it = entries.iterator();
	//to store each class number
	Map class_list = new LinkedHashMap();
	//stores Matrix of class histograms
	Matrix M= new Matrix((probe_data.get_class_max_size()-probe_data.get_class_min_size() +1),hist.get_number_of_bins());
	M= hist.get_matrix();
	//stores map of class sizes
	class_list=hist.get_matrix_map();
 try {
     BufferedWriter out = new BufferedWriter(new FileWriter(dest_file,true));
     out.write("class                                  " + "\t" + "size  " + "\t" + "raw score " + "\t" +"pval " + "\n");

     //	System.out.println("class  " + "\t" + "size  " + "\t" + "raw score " + "\t" +"pval ");	
	
	//iterate over each class
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    String class_name = (String)e.getKey();
	    ArrayList values =(ArrayList)e.getValue();	
	    Iterator I =values.iterator();
	    Iterator I1=values.iterator();
	    double total =0.0;
	    int size =0;
	    Vector raw_score=null;
	    Matrix V=null;
	    boolean check =false;
	    int index=0;
	    int mat_size=0;

	    //this calculation is done just in case the hashtable has no value for an element...hence we keep track of the number of elements with values and then create a Matrix to be used for correlation based on that
	    while(I1.hasNext()){
		String element = (String)I1.next();
		if ((probe_data.get_data_map()).containsKey(element) == true){
		    mat_size++;
		}
	    }
	    // to check if class size less than.....min class size ....cause the value that is used to send to avvecorrel has to be greater than 1 cause other wise the for loops cause the division of numeas by 0 resulting in value of NAN
	    if (mat_size >=probe_data.get_class_min_size()) { 
		while(I.hasNext()){
		    String element = (String)I.next();
		    //System.out.println("chip:\t" + element);
		    //System.out.flush();
		    //check if element exists in map
		    if (element !=null && ((probe_data.get_data_map()).containsKey(element) == true)){
			raw_score=new Vector();
			raw_score = (Vector)(probe_data.get_data_chip_map(element));
			if (raw_score.size() >0){
			    if (check ==false){
				//create a new Matrix so as to add each row of data for a particular probe in matrix 
				V=new Matrix(mat_size,raw_score.size());
				check =true;
			    }
			    //define an iteraor for the values over the probe in the date file
			    Iterator vec_val = raw_score.iterator();
			    int j=0;
			    
			    //store value in intermediate Matrix which is used to correlation calculation
			    while(vec_val.hasNext()){
				
				V.set_matrix_val(index,j,Double.parseDouble((String)vec_val.next()));
				j++;
			    }
			    index++;
			} 
		    }
		}
		
		if (V == null) {
		    continue;
		} else {
		    double avecorrel=0.0;
		    //calculate correlation
		    Matrix C= new Matrix(V.get_num_rows(),V.get_num_rows());
		    statistics.correl_matrix(V,C);
		    
		    avecorrel = classcorrel(C.get_matrix_double(),C.get_num_rows());
		    
		    size = mat_size;
		    double pval=0.0;
		    double rawscore=0.0;
		    if (class_list.containsKey(Integer.toString(size))==false) {
			//  System.out.println("Class size does not exist");
			
		    } else {
			//calcualte raw score and get corresponding value from histogram of that particular class
			rawscore=avecorrel/(double)size;
			if (rawscore < hist.get_hist_max()) {
			    double[] class_row = new double[hist.get_number_of_bins()];
			    class_row=M.get_ith_row(hist.class_index(size, probe_data.get_class_min_size()));
			    int binnum = (int)Math.floor((rawscore - hist.get_hist_min()) / (double)hist.get_bin_size());
			    
			    
			    pval = class_row[binnum];
			} else {
			    //    System.out.println("out of range");
			}
			if (rawscore>0){
			    out.write(goName.get_GoName_value_map(class_name) +"(" + class_name + ")" + "\t" + size + "\t" + rawscore + "\t" +pval + "\n");
			    //	 System.out.println(class_name + "\t" + size + "\t" + rawscore + "\t" +pval);
			}	
		    }
		}
	    } else {
		continue;
	    }
	}
	out.close();
 } catch (IOException e) {
 }
 
    }	
    
    
    /*****************************************************************************************/
    public double classcorrel(double[][] correls, int classsize) {
/*****************************************************************************************/
	//calculate average correlation
	double avecorrel;
	int i,j, nummeas;
	avecorrel = 0;
	nummeas = 0;
	for (i=0; i < classsize; i++) {
	    for (j=i+1; j < classsize; j++) {
		avecorrel +=  Math.abs(correls[i][j]);
		nummeas++;
	    }
	}

	return avecorrel / (double)nummeas ;
    }
    
}
