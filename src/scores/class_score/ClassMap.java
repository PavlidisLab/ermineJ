package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Created :09/02/02
  Revision History: $Id$
  Description:Parses the file of the form
    chip_id   Classes1|Classes2|Classes3|.....
    Stores it in the HashTable called probe_class_map
    Stores the reverse also int class_probe_map in the form 
    Classes1   chip1,chip2....
                                                                                                                                                            
*******************************************************************************/


import scores.class_score.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.charset.*;
     

/**
 */
public class ClassMap { 

     //stores probe->Classes map
     private static Map probe_class_map;
     //stores Classes->probe map
     private static Map class_probe_map;
     //store list of unique Go_IDS
     private Set Classes;

     /**
      */
     public ClassMap(String filename)
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
	     probe_class_map = new LinkedHashMap();
	     ArrayList chip_id = new ArrayList();
	     String go_ids = null;
	     //using set to prevent repeats
	     Classes = new TreeSet();
	     
	     // loop through rows
	     while((row = dis.readLine())!= null)
		 {  
		     //tokenize file by tab character
		     StringTokenizer st = new StringTokenizer(row, "\t");
		     
		     // create a new Vector for each row's columns
		     cols = new Vector();
		     //String pp = st.nextToken();
		     //System.out.println(pp);
		     //chip_id.add(pp);
		     chip_id.add(st.nextToken());
		     // assumption just 2 columns
		     if (st.hasMoreTokens()){
			 go_ids = st.nextToken();
			 //another tokenizer is required since the ClassesID's are seperated by the | character
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
	     
	     // for hash map of probe -> go
	     for (int i =0;i<rows.size();i++){
		 ArrayList go = new ArrayList();
		 for(int j=0;j < ((Vector)rows.elementAt(i)).size(); j++) {
		     String element_ij = (String)(((Vector)(rows.elementAt(i))).elementAt(j));
		     if (element_ij !=null){
			 go.add(element_ij);
			 //stores uniques list of GoIDs
			 Classes.add(element_ij);
		     }
		 }
		 probe_class_map.put(chip_id.get(i), go);
	     }   
			     
	 } catch (IOException e) { 
	     // catch possible io errors from readLine()
	     System.out.println("IOException error!");
	     e.printStackTrace();
	 }
	 
     } // end of readMyFile()
     
    
    
     /**
      */
     public Map get_probe_map() {
	 return probe_class_map;
     }


     /**
      */
     public ArrayList get_probe_value_map(String chip_id) {
	 return (ArrayList)(probe_class_map.get(chip_id));
     }

  
     /**
      */
     public Map get_class_map() {

	 ArrayList[] arry = new ArrayList[Classes.size()];
	 //iterators over  Set 
	 Iterator it = Classes.iterator();
	 class_probe_map = new LinkedHashMap();
	 //stores entrySet
	 java.util.Set pairEntries = probe_class_map.entrySet();
	 java.util.Map.Entry nxt = null;
	 //stores entires over pair values of Hashtable
	 Iterator it1 = pairEntries.iterator();
	 int length =Classes.size();
	 int x;
	 class_probe_map = new LinkedHashMap();
	 
	 // define a mulitmap using a list interface


         //create a hashtable with keys as the Classes elements and values null 
	 while (it.hasNext()) {
	     String element = (String)it.next();
	     class_probe_map.put(element,null);
	 }
	 
	 //iterate over each key value pair of probe_class_map
	 while(it1.hasNext()){
	     nxt = (Map.Entry)it1.next();
	     ArrayList arr= (ArrayList)nxt.getValue();
	     //since each key has a list of values define an interator to go over the values
	     Iterator I=arr.iterator();
	     while (I.hasNext()){
		 String val = (String)I.next();
		 
		 String ke = (String)nxt.getKey();
		 List l = (List) class_probe_map.get(val);
		 //to implement multimap using an ArrayList....get created only when the value 
		 //associated with a particular go_probe key is null and if not just add to the list 
		 if (l ==null){
		     class_probe_map.put(val,l= new ArrayList());
		     l.add(ke);
		 } else {
		     class_probe_map.put(val,l);
		     l.add(ke);
		 }
	     }
	 }

	 return class_probe_map;
     }
     

     /**


      */
     public ArrayList get_class_value_map(String go_id) {
	 return (ArrayList)(class_probe_map.get(go_id));
     }



     /**


      */
     public void print(Map m) 
     {
	 //print the entire map
	 Collection entries = m.entrySet();
	 Iterator it = entries.iterator();
	 while(it.hasNext()) {
	     Map.Entry e = (Map.Entry)it.next();
	     System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
	 }
     }


   
     public static void main (String[] args) {
	 ClassMap test = new ClassMap(args[0]); // creates the probe -> go map.
	 test.print(probe_class_map);
	 test.get_class_map(); // creates the go -> probe map
	 test.print(class_probe_map);
     }
 

 } // end of class

