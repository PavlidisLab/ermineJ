package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Parses the file of the form
  GoID Biological Name

 The values are stored in a HashTable
                                                                                                                                                            
*******************************************************************************/



import scores.class_score.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;
     
/*****************************************************************************************/
 public class GoName_parse { 
/*****************************************************************************************/

     private static Map go_name_map;



     public static void main (String[] args) {
	 GoName_parse val= new GoName_parse(args[0]);
	 val.print(go_name_map);
     }






/*****************************************************************************************/
     public GoName_parse(String filename)
	 /*****************************************************************************************/
     {
	 String aLine = null;
	 //read in file 	
	 try { 
	     FileInputStream fis = new FileInputStream(filename);
	     BufferedInputStream bis = new BufferedInputStream(fis);
	     BufferedReader      dis = new BufferedReader(new InputStreamReader(bis));
	     go_name_map = new LinkedHashMap();
	     String row;
	     
	     
	     while((row = dis.readLine())!= null)
		 {  
		     //tokenize file by tab character
		     StringTokenizer st = new StringTokenizer(row, "\t");
		     String go_id =  st.nextToken();
		     String go_name = st.nextToken();
		     go_name_map.put(go_id,go_name);
		 }
	     dis.close();
	     
	     
	 } catch (IOException e) { 
	     // catch possible io errors from readLine()
	     System.out.println(" IOException error!");
	     e.printStackTrace();
	 }
	 
     }


/*****************************************************************************************/     
     public Map get_GoName_map() {

/*****************************************************************************************/
	 return go_name_map;
     }



/*****************************************************************************************/
     public String get_GoName_value_map(String go_ID) {

/*****************************************************************************************/
	 return (String)(go_name_map.get(go_ID));
     }

  




  /*****************************************************************************************/
     public void print(Map m) 
/*****************************************************************************************/
    {
	Collection entries = m.entrySet();
	Iterator it = entries.iterator();
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
	}
    }





 }
