package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant, Paul Pavlidis (major changes)
  Created :09/02/02
  Revision History: $Id$
  Description:Parses the file of the form
    probe_id   Classes1|Classes2|Classes3|.....
    Stores it in the HashTable called probeToClassMap
    Stores the reverse also int classToProbeMap in the form 
    Classes1   probe1,probe2....
                                                                                                                                                            
*******************************************************************************/
import scores.class_score.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.charset.*;
     

/**
 */
public class ClassMap { 

    private static Map probeToClassMap;      //stores probe->Classes map
    private static Map classToProbeMap;      //stores Classes->probe map
    private static Map classesToRedundantMap; // stores classes->classes which are the same.

    /**
     */
    public ClassMap(String filename, Map probes)
    {
	String aLine = null;

	//read in file 	
	try { 
	    FileInputStream fis = new FileInputStream(filename);
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    BufferedReader      dis = new BufferedReader(new InputStreamReader(bis));
	    Double[] DoubleArray = null;
	    Vector rows = new Vector();
	    Vector cols = null;

	    probeToClassMap = new LinkedHashMap();
	    classToProbeMap = new LinkedHashMap();

	    ArrayList probe_id = new ArrayList();
	    String class_ids = null;
	    String probe = "";
	    String go = "";

	    //using set to prevent repeats
	    //	    Classes = new TreeSet();
	     
	    // loop through rows. Makes hash map of probes to go, and map of go to probes.
	    String line = "";
	    while(( line = dis.readLine() ) != null) {  
		//tokenize file by tab character
		StringTokenizer st = new StringTokenizer(line, "\t");
		
		// create a new Vector for each row's columns
		probe = st.nextToken();

		if (probes.containsKey(probe)) { // only do probes we have in the data set.
		    
		    probe_id.add(probe);
		    probeToClassMap.put(probe, new ArrayList());
		    
		    // assumption: just 2 columns
		    if (st.hasMoreTokens()) {
			class_ids = st.nextToken();
			
			//another tokenizer is required since the ClassesID's are seperated by the | character
			StringTokenizer st1 = new StringTokenizer(class_ids, "|");
			while(st1.hasMoreTokens()) {
			    go = st1.nextToken();
			    
			    // add this go to the probe->go map.
			    ((ArrayList)probeToClassMap.get(probe)).add(go);
			    
			    // add this probe this go->probe map.
			    if (! classToProbeMap.containsKey(go) ) {
				classToProbeMap.put(go, new ArrayList());
			    }
			    ((ArrayList)classToProbeMap.get(go)).add(probe);
			}
		    }
		}
	    }
	     
	    dis.close();

	} catch (IOException e) { 
	    // catch possible io errors from readLine()
	    System.out.println("IOException error!");
	    e.printStackTrace();
	}
	
	collapseClasses(); // identify redundant classes.
	
    } // end of readMyFile()
     


    /**
       Identify classes which are identical to others. This isn't
       superfast, because it doesn't know which classes are actually
       relevant in the data. So some very large classes will be looked
       at here.

    */
    private void collapseClasses() {
	LinkedHashMap seenClasses = new LinkedHashMap();
	LinkedHashMap sigs = new LinkedHashMap();
	classesToRedundantMap = new LinkedHashMap();
	ArrayList sortedList = null;
	Set entries = classToProbeMap.keySet();
	Iterator it = entries.iterator();
	String signature = "";
	String classId = "";

	System.err.println("There are " + entries.size() + " classes represented on the chip (of any size). Redundant classes are being removed...");

	// sort each arraylist in for each go and create a string that is a signature for this class. 
	int ignored = 0;
	while (it.hasNext()) {
	    classId = (String)it.next();
	    ArrayList classMembers = (ArrayList)classToProbeMap.get(classId);
	    
	    // skip classes that are huge. It's too slow
	    // otherwise. This is a total heuristic. Note that this
	    // doesn't mean the class won't get analyzed, it just
	    // means we don't bother looking for redundancies. Big
	    // classes are less likely to be identical to others,
	    // anyway. In tests, the range shown below has no effect
	    // on the results, but it _could_matter.
	    if (classMembers.size() > 200 || classMembers.size() < 2) {
		ignored++;
		continue;
	    }

	    Collections.sort(classMembers);
	    signature = "";
	    Iterator classit = classMembers.iterator();
	    while (classit.hasNext()) {
		signature = signature + "__" + (String)classit.next();
	    }
	    sigs.put(classId, signature);
	}
	
	// look at the signatures for repeats.
	entries = sigs.keySet();
	it = entries.iterator();
	while (it.hasNext() ) {
	    classId = (String)it.next();
	    signature = (String)sigs.get( classId );

	    // if the signature has already been seen, add it to the redundant list, and remove this class from the classToProbeMap.
	    if (seenClasses.containsKey(signature)) {
		if ( ! classesToRedundantMap.containsKey(seenClasses.get(signature)) )
		    classesToRedundantMap.put(seenClasses.get(signature), new ArrayList());

		((ArrayList)classesToRedundantMap.get(seenClasses.get(signature))).add(classId);
		classToProbeMap.remove(classId);
		//		System.err.println(classId + " is the same as an existing class, " + seenClasses.get(signature));
	    } else {
		// add string to hash
		seenClasses.put(signature, classId);
	    }
	}

	//	System.err.println("There are now " + classToProbeMap.size() + " classes represented on the chip (" + ignored + " were ignored)");

    }


    /**
     */
    public ArrayList getRedundancies(String classId) {
	if ( classesToRedundantMap.containsKey(classId) ) {
	    return (ArrayList)classesToRedundantMap.get(classId);
	} else {
	    return null;
	}
    }

    /**
     */
    public String  getRedundanciesString(String classId) {
	if ( classesToRedundantMap.containsKey(classId) ) {
	    ArrayList redundant =  (ArrayList)classesToRedundantMap.get(classId);
	    Iterator it = redundant.iterator();
	    String returnValue = "";
	    while (it.hasNext()) {
		returnValue = returnValue + ", " + it.next();
	    }
	    return returnValue;
	} else {
	    return "";
	}
    }

 
    /**
     */
    private ArrayList get_probe_value_map(String probe_id) {
	return (ArrayList)(probeToClassMap.get(probe_id));
    }

  
    /**
     */
    public Map get_class_map() {

	return classToProbeMap;
    }
     

    /**


    */
    public ArrayList get_class_value_map(String class_id) {
	return (ArrayList)(classToProbeMap.get(class_id));
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


} // end of class

