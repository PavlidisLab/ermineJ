package util;
import java.io.*; 
import java.util.*;

/**
  Matrix functions for storing a file and for creating an empty double[][] object.   Created :09/02/02

  @author Shahmil Merchant
  @version $Id$

*/
public class Matrix extends HashMap // encapsulates a matrix
{
    private int rows, cols;
    private double M[][];
    //row_name and col_name can be made local variables within constructor
    private Vector row_name;
    private static Map row_map;//contains a map of each row and elements in the row
  
    /**
     */
    public Matrix(int tRows, int tCols, double T[][]) //requires dims and 2D array
	//should be run from command line
    {
        M = new double[tRows][tCols];
        rows = tRows;
        cols = tCols;
        
        for(int i=0; i<rows; i++)
            for(int j=0; j<cols; j++)
                M[i][j] = T[i][j];
    }
    


    /**
     */
    public  Matrix(String filename)  //requires filename as input
     {
	 String aLine = null;
	 int count = 0;
	
	 try { 
	     FileInputStream fis = new FileInputStream(filename);
	     BufferedInputStream bis = new BufferedInputStream(fis);
	     BufferedReader      dis = new BufferedReader(new InputStreamReader(bis));
	     int maxWidth =0;
	     int counter =0;
	     int test =0;
 	     String row;
 	     String col;
 	     String rec = null;
	     Vector rows_ = new Vector();
	     Vector cols_ = null;
	     Vector rowlist = null;//contains the list of elements for a particular gene
	     Vector rrow = new Vector();//contains a vector **indirect way to pass elements 
	     row_name = new Vector();//contains a listing names of the first row
	     row_map = new LinkedHashMap(); //contains a map of each gene to the entire row
	    

	     // loop through rows
	   
	     while((row = dis.readLine())!= null)
		 {  		     
		   
		     StringTokenizer st = new StringTokenizer(row, "\t");
		     //for first row 
		       if (test==0){
			    while (st.hasMoreTokens()) 
			 {  
			     rrow.add(st.nextToken());
			 }
			    for (Enumeration e = rrow.elements(); e.hasMoreElements();)
			 {
			    String myString = (String) e.nextElement();
			    row_name.addElement(myString);
			 } 
			    
			 test++;
			 continue;
		     }
		     
		     // create a new Vector for each row's columns
		     cols_ = new Vector();
		     rowlist = new Vector();
		     // loop through columns
		     while (st.hasMoreTokens()) 
			 {  
			     cols_.add(st.nextToken());
			 }
		     counter = 0;
		     for (Enumeration e = cols_.elements(); e.hasMoreElements();)
			 { 
			     String myString = (String) e.nextElement();
			    
			     if (counter ==0) {
				 rec = myString;
				 counter ++;
				 continue;
			     } else {
				  rowlist.addElement(myString);
			
				 // print(rowmap);
				 counter ++;
			     }
			 }
		     	 row_map.put(rec,rowlist);
		     // add the column Vector to the rows   Vector
		     rows_.add(cols_);
		     test++;
		    

		 }
	     //minus 1 to remove column of gene names
	      maxWidth=((Vector)rows_.elementAt(0)).size() -1 ;
	     dis.close();
	     //	     print(row_map);
	     //use the following because double[] is not an object ..hence convert double[] t Object[] and then set value of Matrix
	      Object[][] os= new Object[rows_.size()][maxWidth];
	       M= new double[rows_.size()][maxWidth];
	     for(int i=0;i<rows_.size();i++) {
		 for(int j=1;j < ((Vector)rows_.elementAt(i)).size(); j++) {
		    os[i][j-1] = ((Vector)rows_.elementAt(i)).elementAt(j);
		    M[i][j-1]=Double.parseDouble(os[i][j-1].toString());
		 }
	     }
	     
	     rows = rows_.size();
	     cols= maxWidth;
	     rows_ = null;
	     cols_ = null;
	     rowlist = null;
	     rrow = null; 
	     os = null;
	 } catch (IOException e) { 
	     // catch possible io errors from readLine()
	     System.out.println(" IOException error!");
	     e.printStackTrace();
	 }

	 
     } // end of IOException
    


    /**
     */
    public Matrix(int tRows,int tCols)  //requires number of rows and columns as //input
    {
	  M = new double[tRows][tCols];
	  rows = tRows;
	  cols = tCols;
    }


    /**
     */
    public  void allocate_Matrix()

    {
	BufferedReader d  = new BufferedReader(new InputStreamReader(System.in));
  	for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
		//		System.err.print("Element " + i + ", " + j + "> ");
		//                System.err.flush();
		try {
		    M[i][j] = Double.parseDouble(d.readLine());
		} catch (IOException e) { 
		    // catch possible io errors from readLine()
		    System.out.println(" IOException error!");
		    e.printStackTrace();
		}
            }
        }
	
    }
    
    /**
     */
    public int get_num_rows() 
    {
	return rows;
    }


    
    /**
     */
    public int get_num_cols() 
    {
	return cols;
    }




    /**
     */
    public String formatted_get_matrix()
    {
	String string = "";
	for(int i=0;i<rows;i++){
             for(int j=0; j<cols; j++){
		 string += M[i][j] + "   ";
	     }
	}
        
        return string;            
    }
    

    /**
     */
    public String get_matrix()
    {
	String string = "";
	for(int i=0;i<rows;i++){
	    for(int j=0; j<cols; j++){
		string += M[i][j] + "\t";
	    }
        }
        
        return string;            
    }


    /**
     */
    public double[] get_ith_row(int i)
    {
	return M[i];
    }



    /**
     */
    public double get_matrix_val(int i,int j)

    {
	try {
	    double answer = 0.0;
	    answer = M[i][j];
	    return answer;
	} catch (ArrayIndexOutOfBoundsException exception ) {
	    System.out.println("get_matrix_val::Cannot access " + i + ", " + j + " in that matrix! Matrix has only " + rows + " rows and " + cols + " columns");
	    exception.printStackTrace();
	    System.exit(1);
	}
	return 0.0;
    }


    /*
     */
    public void set_matrix_val(int i,int j,double value)
    {
	try {
	    M[i][j] = value;
	} catch (ArrayIndexOutOfBoundsException exception ) {
	    System.out.println("set_matrix_val::Cannot access " + i + ", " + j + " in that matrix! Matrix has only " + rows + " rows and " + cols + " columns");
	    exception.printStackTrace();
	    System.exit(1);
	}
    }


    /**
     */
    public void increment_matrix_val(int i, int j)
    {
	try {
	    M[i][j] = M[i][j] + 1.0;
	} catch (ArrayIndexOutOfBoundsException exception ) {
	    System.out.println("increment_matrix_val::Cannot access " + i + ", " + j + " in that matrix! Matrix has only " + rows + " rows and " + cols + " columns");
	    exception.printStackTrace();
	    System.exit(1);
	}
    }


    public double get_row_sum(int row)
    {
	double answer = 0.0;
	for (int i = 0; i < cols; i++) {
	    answer += get_matrix_val(row, i);
	}
	return answer;
    }

    public double get_col_sum(int col)
    {
	double answer = 0.0;
	for (int i = 0; i < rows; i++) {
	    answer += get_matrix_val(i, col);
	}
	return answer;
    }

    /**
     */
    public double[][] get_matrix_double()
    {

	return M;
    }


    

 
    /**
     */
    public Vector get_row_names() 

    {
	return row_name;
    }


 
    /**
     */
     public Map get_row_Hash() 
    {
	return row_map;
    }
    

    /*****************************************************************************************/
    /*****************************************************************************************/
    public void addto_row_map(String key_value,Vector value_row)
    {
	row_map.put(key_value,value_row);
	return;
    }
    
    /*
     */
    public void removefrom_row_map(String key_value) 
    {
	row_map.remove(key_value);
	return;
    }



    /**
     */
    public Vector retrieveONEfrom_row_map(String key_value) 
    {
	return (Vector)(row_map.get(key_value));
    }



    /**
     */
    public static Map retrievefrom_row_map(Vector key_values) 
    {
	Map innerMap=new LinkedHashMap();
	for (Enumeration e = key_values.elements(); e.hasMoreElements();)
	    { 
		String myString = (String) e.nextElement();
		innerMap.put(myString,row_map.get(myString));
	    }
	return innerMap;
	
    }
    
    
    
    /**
     */
  public void print(Map m) 
    {
	Collection entries = m.entrySet();
	Iterator it = entries.iterator();
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    System.out.println("Key = " + e.getKey() + ", Value = " + e.getValue());
	}
    }

    public static void main(String args[]) 
    {
	/*
        int rows;
        int cols;
	double[][] M;
	int n=0;
     	BufferedReader d  = new BufferedReader(new InputStreamReader(System.in));
        


	
        System.out.print("\nPlease enter number of matrix rows: ");
        System.out.flush();
        rows = Integer.parseInt(d.readLine());
        System.out.print("\nPlease enter number of matrix cols: ");
        System.out.flush();
        cols = Integer.parseInt(d.readLine());
	M = new double[rows][cols];

	for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                System.out.print("Element "+i+", "+j+"> ");
                System.out.flush();
                M[i][j] = Double.parseDouble(d.readLine());
            }
	    }
	 

	
        Matrix matrix = new Matrix(args[0]);
	System.out.println(matrix.get_num_rows());
	System.out.println(matrix.get_num_cols());
	for (int i = 0;i<matrix.get_num_rows();i++){
	    for (int j = 0;j<matrix.get_num_cols();j++){
		System.out.print(matrix.get_matrix_val(i,j) + "\t");
	    }
	    System.out.println();
	}
	*/
       
    }
    
}



