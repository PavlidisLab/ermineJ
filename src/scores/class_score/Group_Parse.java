package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant, Paul Pavlidis (major changes)
  Created :09/02/02
  Revision History: $Id$
  Description:Parses the file of the form
   probe GROUP_ID

and then stores the temporary reverse map of the form
UD_ID probe1,probe2,probe3

// todo? is the following actually done? (or, to the point, used at all?
then stores the values for each GROUP_ID in a hash map
probe1->probe2,probe3
probe2->probe3,probe1
probe3->probe1,probe2

where each group represent items in the same unigene cluster.
*******************************************************************************/
import scores.class_score.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;


/**
 */
public class Group_Parse { 
    
    private static Map probe_group_map; 
    private static Map group_probe_list; 
    private Set group; 
    

    /**
       Note that this does not store anything for probes which do not
       have a unigene cluster.
       @param String filename: name of the tab-delimited file that has
       the group map, columns probe GROUP_ID. GROUP_ID is the same for
       probes which are from the same group (roughly, group==gene)
       @param Map probesFromList: Just a list of what probes are in the file, so we skip any that aren't in there.
     */
    public Group_Parse(String filename, Map probesInData)
    {
	String aLine = null;
	int count = 0;
	//	probes = probesFromList;
	group_probe_list = new HashMap();
	probe_group_map = new LinkedHashMap();
	    
	//	probe_repeat_val = new HashMap();	  // todo : used?

	File infile = new File(filename);
	if (!infile.exists() || !infile.canRead()) {
	    System.err.println("Could not read " + filename);
	}

	try { 
	    FileInputStream fis = new FileInputStream(filename);
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
	    String row;

	    //	    group = new TreeSet();

	    //read in file
	    while((row = dis.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(row, "\t");
		//		if (st.countTokens() > 1){ // i.e., if there is a group id. This is no longer valid: there is always a group id. Otherwise, the probe gets skipped.
		String probe = st.nextToken();

		if (probesInData.get(probe) != null) { // only do probes that are in our data set.
		    String group = st.nextToken();
		    probe_group_map.put(probe, group); // probe -> group
		    //store list of unique group ids
		    //			group.add(group); // not used.
		    
		    // create the list if need be.
		    if (group_probe_list.get(group) == null) {
			group_probe_list.put(group, new ArrayList());
		    }
		    ((ArrayList)group_probe_list.get(group)).add(probe);
		}
		    //		}
	    }
	    dis.close();
	} catch (IOException e) { 
	    // catch possible io errors from readLine()
	    System.out.println(" IOException error!");
	    e.printStackTrace();
	}
	//	probe_repeat(probesInData); // unravel the map we just made // no longer needed, done above.
    } 
    

    /**
     */
    public Map get_group_probe_map() {
	return group_probe_list;
    } 
    
   /**
     */
    public Map get_probe_group_map() {
	return probe_group_map;
    }
   
    
    /**
     */
    public void print(Map m) //no,nono. todo.
    {
	Collection entries = m.entrySet();
	Iterator it = entries.iterator();
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
	}
    }

    
    public static void main (String[] args) {
	//Group_Parse fi = new Group_Parse(args[0]);
	//fi.print(probe_group_map);
	//	fi.probe_repeat();
	//	fi.print(probe_repeat_val);
    }
    

    
    /**
       Make a map of group to members. Todo: This function is no
       longer needed - not used???
    */
    public void probe_repeat()
    {
	Iterator it = group.iterator();
	Set myprobe = new TreeSet();

	//store map of group with list of probes intially with value zero
	while (it.hasNext()) {
	    String element = (String)it.next();
	    group_probe_list.put(element,null);
	}
	
	Set pairEntries = probe_group_map.entrySet();
	Map.Entry nxt = null;
	Iterator it1 = pairEntries.iterator();

	//iterate over hashtable and store all probes for a particular group 
	while(it1.hasNext()) {
	    nxt = (Map.Entry)it1.next();
	    String group = (String)nxt.getValue(); // group
	    String currprobe = (String)nxt.getKey(); // probe

	    //	    myprobe.add(currprobe); //list of all the gene id , unique. Is this necessary any longer?

	    // create the list if need be.
	    if (group_probe_list.get(group) == null) {
		group_probe_list.put(group, new ArrayList());
	    }
	    ((ArrayList)group_probe_list.get(group)).add(currprobe);
	}   

	// the following is not used at all.???
	/*
	Iterator probeit = myprobe.iterator();
 	while (probeit.hasNext()) {
	    String ele = (String)probeit.next();
	    	    if(probesInData.containsKey(ele)) // only add those occur in gene_pval file
			probe_repeat_val.put(ele,null);
	}
	
	//iterate over earlier table values
 	Set pairEntriess = group_probe_list.entrySet();
	Map.Entry nxts = null;
	Iterator it11 = pairEntriess.iterator();
	while(it11.hasNext()){
	    nxts = (Map.Entry)it11.next();
	    ArrayList val = (ArrayList)nxts.getValue();
	    
	    Iterator I = val.iterator();
	    while(I.hasNext()){
		String valx = (String)I.next();
		// create a new list if a probe has another probe with the same GROUP ...hence list to be stored in Hashtable as values
		List l1 = (List)probe_repeat_val.get(valx);
		if (l1 == null)
		    l1 = new ArrayList();
		
		Iterator arry = val.iterator();
		//add all values of probes with the same GROUP to a hashtable with key equal to one of the GROUP id's probe 
		while(arry.hasNext()){
		    String arr = (String)arry.next();
		    if(probesInData.containsKey(arr) && !arr.equals(valx))  //only add those occur in gene_pval file
			l1.add(arr);
		}
		probe_repeat_val.put(valx,l1);
	    }
	}
	*/
    }	
    
    
    
    /**
       todo: is this used? 
     */
    //    public Map get_repeat_val_map() {
    //	return probe_repeat_val;
    //    }
    
    

   /**
     */
    //    public ArrayList get_probe_value_map(String probe_id) {
    //	return (ArrayList)(probe_repeat_val.get(probe_id));
    //    }
    
    
    /**
     */
    //    public ArrayList get_group_probe_value_map(String probe_id) {
    //	 return (ArrayList)(group_probe_list.get(probe_id));
    //    }     
 

    
 } // end of class


