package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Parses the file of the form
    chip_id   GO1|GO2|GO3|.....
    Stores it in the HashTable called affy_go_map
    Stores the reverse also int go_affy_map in the form 
    GO1   chip1,chip2....
                                                                                                                                                            
*******************************************************************************/


import scores.class_score.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.charset.*;
     

/** lets see if <b>this</b> works*/


/*****************************************************************************************/
 public class affy_go_Parse { 

/*****************************************************************************************/
     //stores affy->GO map
     private static Map affy_go_map;
     //stores GO->affy map
     private static Map go_affy_map;
     //store list of unique Go_IDS
     private Set GO;
   
     public static void main (String[] args) {
	 affy_go_Parse test = new affy_go_Parse(args[0]); // creates the affy -> go map.
	 test.print(affy_go_map);
	 test.get_go_map(); // creates the go -> affy map
	 test.print(go_affy_map);
     }
     
     /*****************************************************************************************/
     /*****************************************************************************************/
     public affy_go_Parse(String filename)
     {
	 String aLine = null;
	 //read in file 	
	 try { 
	     FileInputStream fis = new FileInputStream(filename);
	     BufferedInputStream bis = new BufferedInputStream(fis);
	     BufferedReader      dis = new BufferedReader(new InputStreamReader(bis));
	     Double[] DoubleArray = null;
	     String row;
	     String col;
	     Vector rows = new Vector();
	     Vector cols = null;
	     affy_go_map = new LinkedHashMap();
	     ArrayList chip_id = new ArrayList();
	     String go_ids = null;
	     //using set to prevent repeats
	     GO = new TreeSet();
	     
	     // loop through rows
	     while((row = dis.readLine())!= null)
		 {  
		     //tokenize file by tab character
		     StringTokenizer st = new StringTokenizer(row, "\t");
		     
		     // create a new Vector for each row's columns
		     cols = new Vector();
		     
		     chip_id.add(st.nextToken());
		     // assumption just 2 columns
		     if (st.hasMoreTokens()){
			 go_ids = st.nextToken();
			 //another tokenizer is required since the GOID's are seperated by the | character
			 StringTokenizer st1 = new StringTokenizer(go_ids,"|");
			 while(st1.hasMoreTokens())
			     cols.add(st1.nextToken());
		     } else {
			 cols.add(null);
		     }
		     // add the column Vector to the rows Vector
		     rows.add(cols);
		 }
	     
	     dis.close();
	     
	     // for hash map of affy -> go
	     for (int i =0;i<rows.size();i++){
		 ArrayList go = new ArrayList();
		 for(int j=0;j < ((Vector)rows.elementAt(i)).size(); j++) {
		     String element_ij = (String)(((Vector)(rows.elementAt(i))).elementAt(j));
		     if (element_ij !=null){
			 go.add(element_ij);
			 //stores uniques list of GoIDs
			 GO.add(element_ij);
		     }
		 }
		 affy_go_map.put(chip_id.get(i), go);
	     }   
			     
	 } catch (IOException e) { 
	     // catch possible io errors from readLine()
	     System.out.println("IOException error!");
	     e.printStackTrace();
	 }
	 
     } // end of readMyFile()
     
    
    
     /*****************************************************************************************/     
     /*****************************************************************************************/
     public Map get_affy_map() {
	 return affy_go_map;
     }



     /*****************************************************************************************/
     /*****************************************************************************************/
     public ArrayList get_affy_value_map(String chip_id) {
	 return (ArrayList)(affy_go_map.get(chip_id));
     }

  
     /*****************************************************************************************/
     /*****************************************************************************************/
     public Map get_go_map() {

	 ArrayList[] arry = new ArrayList[GO.size()];
	 //iterators over  Set 
	 Iterator it = GO.iterator();
	 go_affy_map = new LinkedHashMap();
	 //stores entrySet
	 java.util.Set pairEntries = affy_go_map.entrySet();
	 java.util.Map.Entry nxt = null;
	 //stores entires over pair values of Hashtable
	 Iterator it1 = pairEntries.iterator();
	 int length =GO.size();
	 int x;
	 go_affy_map = new LinkedHashMap();
	 
	 // define a mulitmap using a list interface


         //create a hashtable with keys as the GO elements and values null 
	 while (it.hasNext()) {
	     String element = (String)it.next();
	     go_affy_map.put(element,null);
	 }
	 
	 //iterate over each key value pair of affy_go_map
	 while(it1.hasNext()){
	     nxt = (Map.Entry)it1.next();
	     ArrayList arr= (ArrayList)nxt.getValue();
	     //since each key has a list of values define an interator to go over the values
	     Iterator I=arr.iterator();
	     while (I.hasNext()){
		 String val = (String)I.next();
		 
		 String ke = (String)nxt.getKey();
		 List l = (List) go_affy_map.get(val);
		 //to implement multimap using an ArrayList....get created only when the value 
		 //associated with a particular go_affy key is null and if not just add to the list 
		 if (l ==null){
		     go_affy_map.put(val,l= new ArrayList());
		     l.add(ke);
		 } else {
		     go_affy_map.put(val,l);
		     l.add(ke);
		 }
	     }
	 }

	 return go_affy_map;
     }
     



     /*****************************************************************************************/
     /*****************************************************************************************/
     public ArrayList get_go_value_map(String go_id) {
	 return (ArrayList)(go_affy_map.get(go_id));
     }






/*****************************************************************************************/
 public void print(Map m) 
/*****************************************************************************************/
    {
	//print the entire map
	Collection entries = m.entrySet();
	Iterator it = entries.iterator();
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
	}
    }


 } // end of class

