package classScore;
import util.*;
import java.io.*;
import java.util.*;
import java.text.*;

/**

  Data structure to store class scoring information about
  a class. (experiment scores only)

  @author Paul Pavlidis
  @version $Id$
                                                                                                                                                            
*/
public class classresult implements Comparable {
    private String class_id = null;
    private String class_name = null;
    private double pvalue = 1.0;
    private double score = 0.0;
    private double hyperpval = 1.0;
    private int hypercut = 0; // how many genes make the pvalue threshold; this is n1 in the calculations
    private double rocpval = 1.0;
    private double aroc = 0.5;
    private int size = 0;
    private int effective_size = 0;
    private double pvalue_corr = 0.0;
    private double hyperpval_corr = 0.0;
    private double rocpval_corr = 0.0;
    private LinkedHashMap identicalTo = null; // Defines classes this one is identical to in terms of members.
    private DecimalFormat nf;

    public classresult () {
	this(null, null, 0, 0, 0.0, 1.0, 1.0, 0.5, 1.0);
    }

    public classresult(String id, String class_name, int size, int effsize) {
	this();
	this.setnames(id, class_name);
	this.setsizes(size, effsize);
    }
    
    public classresult ( String id, 
			 String class_name, 
			 int size, 
			 int effective_size, 
			 double score, 
			 double pvalue,  
			 double hyperpval, 
			 double aroc, 
			 double rocpval) {
	this.class_id = id;
	this.class_name = class_name;
	this.pvalue = pvalue;
	this.score = score;
	this.hyperpval = hyperpval;
	this.rocpval = rocpval;
	this.aroc = aroc;
	this.size = size;
	this.effective_size = effective_size;

	nf = new DecimalFormat();
	nf.setMaximumFractionDigits(8);
	nf.setMinimumFractionDigits(3);
    }

    public void print (BufferedWriter out) {
	this.print(out, "");
   }

    public void print (BufferedWriter out, String extracolumns) {
	try {
	    
	    String fixnamea;
	    String cleanname;
	    if (class_name != null) {
		fixnamea = class_name.replace(' ', '_'); // make the format compatible with the perl scripts Paul wrote.
		cleanname = fixnamea.replace(':', '-'); // todo: figure out why this doesn't work.
	    } else {
		cleanname = "";
	    }
	    out.write(cleanname +"_" + 
		      class_id + "" + "\t" + size + "\t" + 
		      nf.format(score) + "\t" + nf.format(pvalue) + "\t" + 
		      effective_size + "\t" + hypercut + "\t" + nf.format(hyperpval) + "\t" + 
		      nf.format(aroc) + "\t" + nf.format(rocpval) + "\t" + 
		      nf.format(pvalue_corr) + extracolumns + "\n");
	} catch (IOException e) {
	    System.err.println("There was an IO error" + e);
	}
    }

   
    public void print_headings (BufferedWriter out) {
	this.print_headings(out, "");
    }


    public void print_headings (BufferedWriter out, String extracolumns) {
	try {
	    out.write("Class" + "\tsize" + "\tscore" + 
		      "\tscore pval" + "\teffective_size" + 
		      "\tN over pval cut\thyper pval" + "\tAROC" + "\tAROCpval" + 
		      "\tCorrected_pvalue" + extracolumns + "\n");
	} catch (IOException e) {
	    System.err.println("There was an IO error" + e);
	}
    }

    public void setnames (String id, String name) {
	this.class_id = id;
	this.class_name = name;
    }

    public void setsizes (int size, int effsize) {
	this.size = size;
	this.effective_size = effsize;
    }

    public void setscore (double ascore) {
	score = ascore;
    }

    public void setpval (double apvalue) {
	pvalue = apvalue;
    }

    public void sethyperp (double ahyperp) {
	hyperpval = ahyperp;
    }

    public void sethypercut (int ahypercut) {
	hypercut = ahypercut;
    }


    public void setaroc (double aroc) {
	this.aroc = aroc;
    }

    public void setarocp (double arocp) {
	this.rocpval = arocp;
    }

    public void setpvalue_corr (double a) {
	pvalue_corr = a;
    }

    public void sethyperpval_corr (double a) {
	hyperpval_corr = a;
    }

    public void setrocpval_corr (double a) {
	rocpval_corr = a;
    }

    public String toString() {
	return "I'm a classresult";
    }

    public String get_class_id() {
	return class_id;
    }

    public double get_pvalue() {
	return pvalue;
    }

    public double get_score() {
	return score;
    }

    public int get_effsize() {
	return effective_size;
    }



    /**
       
    Default comparator for this class: sorts by the pvalue.
    
    */
    public int compareTo(Object ob) {
	classresult other = (classresult)ob;
	if (this.pvalue>other.pvalue)
	    return 1;
	else if(this.pvalue < other.pvalue)
	    return -1;
	else
	    return 0;
    }

}
