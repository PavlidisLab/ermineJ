/***************************************************************************************
 command line interface for ermineJ program

 #command line arguments in the following way
  args[0]:pval_file, 
  args[1]:affy_go_file, 
  args[2]:Go_name_file, 
  args[3]:ug_file, 
  args[4]:destination_file, 
  args[5]:method, 
  args[6]:class_max_size, 
  args[7]:class_min_size, 
  args[8]:number of runs, 
  args[9]:quantile, 
  args[10]:p-value, 
  args[11]:weightcheck

  **notce: all "data files" and this "command line file" should be put uncer the directory ermineJ\java_proj\src

examples:
1. java ermine age.welch.pvals.highexpression.forerminej.txt MG-U74Av2.go.txt goNames.txt MG-U74Av2.ug.txt output-quantile-log2.txt QUANTILE_METHOD 100 4 10000 50 0.001 true
2. java ermine age.welch.pvals.highexpression.forerminej.txt MG-U74Av2.go.txt goNames.txt MG-U74Av2.ug.txt output-mean-log3.txt MEAN_METHOD 100 4 10000 50 0.0001 true

***************************************************************************************/


import scores.class_score.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class erminecmd{
    public static void main(String args[]){
	try {
	    String pbPvalFile = args[0];
	    String affyGoFile = args[1];
	    String goNameFile = args[2];
	    String ugFile = args[3];
	    String destinFile = args[4]; 
	    
	    pbPvalFile = getCanonical(pbPvalFile);
	    affyGoFile = getCanonical(affyGoFile);
	    destinFile = getCanonical(destinFile);
	    goNameFile = getCanonical(goNameFile);
	    ugFile = getCanonical(ugFile);

	    class_pvals test = new class_pvals(pbPvalFile, affyGoFile, goNameFile, destinFile, ugFile, args[5], args[6], Integer.parseInt(args[7]),Integer.parseInt(args[8]),Integer.parseInt(args[9]),Integer.parseInt(args[10]), Double.parseDouble(args[11]), args[12], Integer.parseInt(args[13]), args[14]);
	    test.class_pval_generator();       
	} catch (ArrayIndexOutOfBoundsException exception) {
	    System.err.println("You must enter 15 command line arguments: \nprobe_pvalfile\naffy_gofile\ngo_namefile\ngroups file\ndestination_file\nmethod\ngroups method\nmax class size\nmin class size\nnum runs\nquantile\npval\nwt_check\npvalcolumn\ndolog");
	}
    }

    protected static String getCanonical(String in) {
	if (in == null || in.length() == 0)
	    return in;
	File outFile = new File(in);
	try {
	    return outFile.getCanonicalPath();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
   }

}

