package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Created :09/02/02
  Revision History: $Id$
  Description:Parses the file of the form
  chip_id pval

 The values are stored in a HashTable
                                                                                                                                                            
*******************************************************************************/



import scores.class_score.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;
     

/*****************************************************************************************/
 public class Pval_parse { 
/*****************************************************************************************/

     private String[] chip_id = null;
     private double[] pval = null;
     private int num_pvals;
     private static Map chip_pval_map;
     private double log10 = Math.log(10);
     //private gene_pval[] gene_list = null;

     //--------------------------------------------------< main >--------//

     public static void main (String[] args) {
	 Pval_parse t = new Pval_parse(args[0]);
     }
     

     //--------------------------------------------< readMyFile >--------//

     /*****************************************************************************************/
     /* creates the probe -> pval mapping.
     /*****************************************************************************************/
     public Pval_parse(String filename) {
	 this(filename, 1, true);
     }

     public Pval_parse(String filename, int column, boolean dolog) {
	 String aLine = null;
	 int count = 0;
	 //read in file

	 File infile = new File(filename);
	 if (!infile.exists() || !infile.canRead()) {
	     System.err.println("Could not read " + filename);
	 }

	 if (column < 1) {
	     System.err.println("Illegal column number " + column + ", must be greater or equal to 1");
	 } else {
	     System.err.println("Reading gene scores from column " + column);
	 }
	 
	 try { 
	     FileInputStream fis = new FileInputStream(filename);
	     BufferedInputStream bis = new BufferedInputStream(fis);
	     BufferedReader      dis = new BufferedReader(new InputStreamReader(bis));
	     Double[] doubleArray =null;
	     String row;
	     String col;
	     Vector rows = new Vector();
	     Vector cols = null;
	     chip_pval_map = new LinkedHashMap();
	     int colnumber =0;
	     // loop through rows
	     while((row = dis.readLine())!= null)
		 {  
		     StringTokenizer st = new StringTokenizer(row, "\t");
		     
		     // create a new Vector for each row's columns
		     cols = new Vector();
		     
		     // loop through columns
		     while (st.hasMoreTokens()) 
			 {
			     cols.add(st.nextToken());
			 }
		     // add the column Vector to the rows Vector
		     rows.add(cols);
		 }
	     
	     dis.close();
	     chip_id = new String[rows.size()-1];
	     pval = new double[rows.size()-1];
	     doubleArray =new Double[rows.size()-1];
	     for (int i=1; i < rows.size();i++)
		 {
		     
		     String name = (String)(((Vector)(rows.elementAt(i))).elementAt(0));

		     if (name.matches("AFFX.*")) { // todo: put this rule somewhere else
			 System.err.println("Skipping probe in pval file: " + name);
			 continue;
		     }
		     chip_id[i-1] = name;

		     pval[i-1] = Double.parseDouble((String)(((Vector)(rows.elementAt(i))).elementAt(column)));

		     //		     System.err.println("p " + pval[i-1]);

		     // Do not add probes whose pvals cannot be logged.
		     if (pval[i-1] <= 0 && dolog) {
			 System.err.println("Warning: Cannot take log of non-positive value for " + name  +  " (" + pval[i-1] + ") from gene score file: Setting to 10e-12.");
			 //			 continue;
			 pval[i-1] = 10e-12;
		     }

		     if (dolog) {
			 pval[i-1]= -(Math.log(pval[i-1])/log10); // Make -log base 10.
		     }

		     doubleArray[i-1] = new Double(pval[i-1]);
		     chip_pval_map.put(chip_id[i-1],doubleArray[i-1]);	      // put key, value.
		 }
	     
	     num_pvals = Array.getLength(pval);
	     if (num_pvals <= 0) {
		 System.err.println("No pvalues found in the file!");
		 System.exit(1);
	     } else {
		 System.err.println("Found " + num_pvals + " pvals in the file");
	     }
             
	 } catch (IOException e) { 
	     // catch possible io errors from readLine()
	     System.out.println(" IOException error!");
	     e.printStackTrace();
	 }

     } //
     



/*****************************************************************************************/
     public String[] get_chip_ids(){
/*****************************************************************************************/
	 return chip_id;
     }


/*****************************************************************************************/
      public double[] get_pval(){
	 return pval;
     }
/*****************************************************************************************/


/*****************************************************************************************/
     public int get_numpvals(){
/*****************************************************************************************/
	 return num_pvals;
     }
   


/*****************************************************************************************/  
     public Map get_map() {
/*****************************************************************************************/
	 return chip_pval_map;
     }
     
/*****************************************************************************************/  
     //public gene_pval[] get_gene_list() {
/*****************************************************************************************/
	// return gene_list;
     //}



/*****************************************************************************************/
     public double get_value_map(String chip_id) {
/*****************************************************************************************/
	 double value=0.0;
	 
	 if (chip_pval_map.get(chip_id)!=null){ 
	    value = Double.parseDouble((chip_pval_map.get(chip_id)).toString());
	 }
	 
	 return value;
     }
     

 } // end of class

