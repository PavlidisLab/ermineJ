package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Parses the file of the form
   probe UG_ID

and then stores the temporary reverse map of the form
UD_ID probe1,probe2,probe3

then stores the values for each UG_ID in a hash map
probe1->probe2,probe3
probe2->probe3,probe1
probe3->probe1,probe2

where each group represent items in the same unigene cluster.
*******************************************************************************/



import scores.class_score.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;
     


/*****************************************************************************************/
/*****************************************************************************************/
public class Ug_Parse { 
    private static Map chip_ug_map;//map of chip ->ug..input file
    private static Map chip_repeat_val;//map of chips final weights file  todo: is this used?
    private static Map ug_chip_list;//map of ug->chips
    private Map chips;
    private Set ug;
    
    //--------------------------------------------------< main >--------//
    
    public static void main (String[] args) {
	//Ug_Parse fi = new Ug_Parse(args[0]);
	//fi.print(chip_ug_map);
	//	fi.chip_repeat();
	//	fi.print(chip_repeat_val);
    }
    
    
    //--------------------------------------------< readMyFile >--------//
    /*****************************************************************************************/
    /* Note that this does not store anything for probes which do not
     * have a unigene cluster. */
    /*****************************************************************************************/
    public Ug_Parse(String filename, Map chipsFromList)
    {
	String aLine = null;
	int count = 0;
	chips = chipsFromList;
	try { 
	    FileInputStream fis = new FileInputStream(filename);
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    BufferedReader      dis = new BufferedReader(new InputStreamReader(bis));
	    String row;
	    chip_ug_map = new LinkedHashMap();
	    ug = new TreeSet();
	    //read in file
	    while((row = dis.readLine())!= null)
		{  
		    StringTokenizer st = new StringTokenizer(row, "\t");
		    if (st.countTokens()>1){ // i.e., if there is a unigene id.
			String chip = st.nextToken();
			String uni = st.nextToken();
			chip_ug_map.put(chip,uni);
			//store list of unique ug ids
			ug.add(uni);
		    }
		}
	    dis.close();
	    
	} catch (IOException e) { 
	    // catch possible io errors from readLine()
	    System.out.println(" IOException error!");
	    e.printStackTrace();
	}
    } 
    
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public void chip_repeat()
    {
	ug_chip_list = new HashMap();
	chip_repeat_val = new HashMap();	 
	Iterator it = ug.iterator();
	Set chip = new TreeSet();
	//store map of ug with list of chips intially with value zero
	while (it.hasNext()) {
	    String element = (String)it.next();
	    ug_chip_list.put(element,null);
	}
	
	java.util.Set pairEntries = chip_ug_map.entrySet();
	java.util.Map.Entry nxt = null;
	Iterator it1 = pairEntries.iterator();
	//iterate over hashtable and store all chips for a particular ug 
	while(it1.hasNext()){
	    nxt = (Map.Entry)it1.next();
	    String val = (String)nxt.getValue();
	     
	    String ke = (String)nxt.getKey();
	    chip.add(ke);//list of all the gene id 

	    // create the list if need be.
	     List l = (List) ug_chip_list.get(val);
	     if (l == null)
		 l = new ArrayList();

	     ug_chip_list.put(val,l);
	     l.add(ke);	 
	}   
	 
	Iterator chipit = chip.iterator();
	while (chipit.hasNext()) {
	    String ele = (String)chipit.next();
	    if(chips.containsKey(ele))      //only add those occur in gene_pval file
	        chip_repeat_val.put(ele,null);
	}

	//iterate over earlier table values
	java.util.Set pairEntriess = ug_chip_list.entrySet();
	java.util.Map.Entry nxts = null;
	Iterator it11 = pairEntriess.iterator();
	while(it11.hasNext()){
	    nxts = (Map.Entry)it11.next();
	     ArrayList val = (ArrayList)nxts.getValue();
	     
	     Iterator I = val.iterator();
	     while(I.hasNext()){
		 String valx = (String)I.next();
		 //create a new list if a probe has another probe with the same UG ...hence list to be stored in Hashtable as values
		 List l1 = (List) chip_repeat_val.get(valx);
		 
		 if (l1 ==null){
		     chip_repeat_val.put(valx,l1= new ArrayList());
		     Iterator arry = val.iterator();
		     //add all values of chips with the same UG to a hashtable with key equal to one of the UG id's chip 
		     while(arry.hasNext()){
			 String arr = (String)arry.next();
			 if(chips.containsKey(arr))  //only add those occur in gene_pval file
			     if (!arr.equals(valx)){
			         l1.add(arr);
			     }
		     } 
		 } else {
		     Iterator arri = val.iterator();
		     while(arri.hasNext()){
			 String arr = (String)arri.next();
			 if(chips.containsKey(arr)) //only add those occur in gene_pval file
			     if (!arr.equals(valx)){
			         l1.add(arr);
			     }
		     } 
		     chip_repeat_val.put(valx,l1);
		 }
	     }
	}
    }	
    
    
    
    /*****************************************************************************************/     
    /* todo: is this used? */
    /*****************************************************************************************/
    public Map get_chip_map() {
	return chip_repeat_val;
    }
    
    
    /*****************************************************************************************/     
    /*****************************************************************************************/
    public Map get_ug_chip_map() {
	return ug_chip_list;
    } 
    
    /*****************************************************************************************/     
    /*****************************************************************************************/
    public Map get_chip_ug_map() {
	return chip_ug_map;
    }          
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public ArrayList get_chip_value_map(String chip_id) {
	return (ArrayList)(chip_repeat_val.get(chip_id));
    }
    
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public ArrayList get_ug_chip_value_map(String chip_id) {
	 return (ArrayList)(ug_chip_list.get(chip_id));
    }     
    
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public void print(Map m) 
    {
	Collection entries = m.entrySet();
	Iterator it = entries.iterator();
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
	}
    }
    
 } // end of class


