package classScore;
import util.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;


/**
  Description:Parses the file of the form:
   <pre>probe GROUP_ID</pre>

   and then stores the temporary reverse map of the form
   
   <pre>UD_ID probe1,probe2,probe3</pre>
   
   where each group represent items in the same unigene cluster.   Created :09/02/02
   
   @author Shahmil Merchant, Paul Pavlidis (major changes)
   @version $Id$
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
	group_probe_list = new HashMap();
	probe_group_map = new LinkedHashMap();
	    
	File infile = new File(filename);
	if (!infile.exists() || !infile.canRead()) {
	    System.err.println("Could not read " + filename);
	}

	try { 
	    FileInputStream fis = new FileInputStream(filename);
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
	    String row;

	    //read in file
	    while((row = dis.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(row, "\t");
		String probe = st.nextToken();

		if (probesInData.get(probe) != null) { // only do probes that are in our data set.
		    String group = st.nextToken();
		    probe_group_map.put(probe, group); // probe -> group
		    
		    // create the list if need be.
		    if (group_probe_list.get(group) == null) {
			group_probe_list.put(group, new ArrayList());
		    }
		    ((ArrayList)group_probe_list.get(group)).add(probe);
		}
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

	    // create the list if need be.
	    if (group_probe_list.get(group) == null) {
		group_probe_list.put(group, new ArrayList());
	    }
	    ((ArrayList)group_probe_list.get(group)).add(currprobe);
	}   
    }	
    
 } // end of class


