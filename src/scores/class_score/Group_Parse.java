package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Created :09/02/02
  Revision History: $Id$
  Description:Parses the file of the form
   probe GROUP_ID

and then stores the temporary reverse map of the form
UD_ID probe1,probe2,probe3

then stores the values for each GROUP_ID in a hash map
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
public class Group_Parse { 
    private static Map probe_group_map;//map of probe ->group..input file
    private static Map probe_repeat_val;//map of probes final weights file  todo: is this used?
    private static Map group_probe_list;//map of group->probes
    private Map probes;
    private Set group;
    
    //--------------------------------------------------< main >--------//
    
    public static void main (String[] args) {
	//Group_Parse fi = new Group_Parse(args[0]);
	//fi.print(probe_group_map);
	//	fi.probe_repeat();
	//	fi.print(probe_repeat_val);
    }
    
    
    /*****************************************************************************************/
    /* Note that this does not store anything for probes which do not
     * have a unigene cluster. */
    /*****************************************************************************************/
    public Group_Parse(String filename, Map probesFromList)
    {
	String aLine = null;
	int count = 0;
	probes = probesFromList;

	File infile = new File(filename);
	if (!infile.exists() || !infile.canRead()) {
	    System.err.println("Could not read " + filename);
	}

	try { 
	    FileInputStream fis = new FileInputStream(filename);
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    BufferedReader      dis = new BufferedReader(new InputStreamReader(bis));
	    String row;
	    probe_group_map = new LinkedHashMap();
	    group = new TreeSet();
	    //read in file
	    while((row = dis.readLine())!= null)
		{  
		    StringTokenizer st = new StringTokenizer(row, "\t");
		    if (st.countTokens()>1){ // i.e., if there is a unigene id.
			String probe = st.nextToken();
			String uni = st.nextToken();
			probe_group_map.put(probe,uni);
			//store list of unique group ids
			group.add(uni);
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
    public void probe_repeat()
    {
	group_probe_list = new HashMap();
	probe_repeat_val = new HashMap();	 
	Iterator it = group.iterator();
	Set probe = new TreeSet();
	//store map of group with list of probes intially with value zero
	while (it.hasNext()) {
	    String element = (String)it.next();
	    group_probe_list.put(element,null);
	}
	
	java.util.Set pairEntries = probe_group_map.entrySet();
	java.util.Map.Entry nxt = null;
	Iterator it1 = pairEntries.iterator();
	//iterate over hashtable and store all probes for a particular group 
	while(it1.hasNext()){
	    nxt = (Map.Entry)it1.next();
	    String val = (String)nxt.getValue();
	     
	    String ke = (String)nxt.getKey();
	    probe.add(ke);//list of all the gene id 

	    // create the list if need be.
	     List l = (List) group_probe_list.get(val);
	     if (l == null)
		 l = new ArrayList();

	     group_probe_list.put(val,l);
	     l.add(ke);	 
	}   
	 
	Iterator probeit = probe.iterator();
	while (probeit.hasNext()) {
	    String ele = (String)probeit.next();
	    if(probes.containsKey(ele))      //only add those occur in gene_pval file
	        probe_repeat_val.put(ele,null);
	}

	//iterate over earlier table values
	java.util.Set pairEntriess = group_probe_list.entrySet();
	java.util.Map.Entry nxts = null;
	Iterator it11 = pairEntriess.iterator();
	while(it11.hasNext()){
	    nxts = (Map.Entry)it11.next();
	     ArrayList val = (ArrayList)nxts.getValue();
	     
	     Iterator I = val.iterator();
	     while(I.hasNext()){
		 String valx = (String)I.next();
		 //create a new list if a probe has another probe with the same GROUP ...hence list to be stored in Hashtable as values
		 List l1 = (List) probe_repeat_val.get(valx);
		 
		 if (l1 ==null){
		     probe_repeat_val.put(valx,l1= new ArrayList());
		     Iterator arry = val.iterator();
		     //add all values of probes with the same GROUP to a hashtable with key equal to one of the GROUP id's probe 
		     while(arry.hasNext()){
			 String arr = (String)arry.next();
			 if(probes.containsKey(arr))  //only add those occur in gene_pval file
			     if (!arr.equals(valx)){
			         l1.add(arr);
			     }
		     } 
		 } else {
		     Iterator arri = val.iterator();
		     while(arri.hasNext()){
			 String arr = (String)arri.next();
			 if(probes.containsKey(arr)) //only add those occur in gene_pval file
			     if (!arr.equals(valx)){
			         l1.add(arr);
			     }
		     } 
		     probe_repeat_val.put(valx,l1);
		 }
	     }
	}
    }	
    
    
    
    /*****************************************************************************************/     
    /* todo: is this used? */
    /*****************************************************************************************/
    public Map get_probe_map() {
	return probe_repeat_val;
    }
    
    
    /*****************************************************************************************/     
    /*****************************************************************************************/
    public Map get_group_probe_map() {
	return group_probe_list;
    } 
    
    /*****************************************************************************************/     
    /*****************************************************************************************/
    public Map get_probe_group_map() {
	return probe_group_map;
    }          
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public ArrayList get_probe_value_map(String probe_id) {
	return (ArrayList)(probe_repeat_val.get(probe_id));
    }
    
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public ArrayList get_group_probe_value_map(String probe_id) {
	 return (ArrayList)(group_probe_list.get(probe_id));
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


