package util;
import java.io.*; 
import java.util.*;
import java.math.*;
import java.lang.*;
import java.lang.reflect.*;


/**
   Mathematical functions for statistics and common matrix manipulations.
   Created 09/02/02

   @author Shahmil Merchant, Edward Chen, Paul Pavlidis (major changes)
   @version $Id$

   todo: get rid of constantstuff interface.
 
 */
public class Stats implements Cloneable, ConstantStuff

{
    
    private Random generator = new Random(System.currentTimeMillis());
    public Stats() {
    }

    /**
     */
    public Object clone () {

	Object o =null;
	try {
	    o=super.clone();
	} catch (CloneNotSupportedException e) {}
	return o;
    }


    /**
       calculate the sum of the elements of an array
    */
    public static double sum(double[] x)
    {
	int length=Array.getLength(x);
	double total =0.0;
	int i;
	for (i=0; i<length; i++) {
	    total += x[i];
	}
	return total;
    }

    /**
       Calculate the mean of an array's elements
    */
    public static double mean(double[] x)
    {
	int length=Array.getLength(x);
	double sum=0.0;
	int i;
	for (i=0; i<length; i++) {
	    sum += x[i];
	}
	if (length == 0) {
	    return 0.0; 
	} else {
	    return sum / length; 
	}	
    }
    
    /**
       special mean calculation where we use the effective size
    */
    public static double mean(double[] x, int eff_size) {
	int length=Array.getLength(x);
	double sum=0.0;
	int i;
	for (i=0; i<length; i++) {
	    sum += x[i];
	}
	if (length == 0 || eff_size == 0) {
	    return 0.0; 
	} else {
	    return sum / eff_size; 
	}
    }


    /**
       calculate the sum of squared deviation from the mean of an array
    */
    public static double ssq (double[] x,double mean)
    {
	int length=Array.getLength(x);
	double result= 0.0;
	double dev = 0.0;
	int i=0;
	for (i=0; i<length; i++) {
	    dev = x[i] - mean;
	    result += dev * dev;
	}
	return result;
    }
    

    /**
       calculate the standard deviation of an array
    */
    public static double stdev (double[] m,double mean, double ssqs) {
	int length=Array.getLength(m);
	if (length < 2) {
	    return -1.0;
	}
	return Math.sqrt(ssqs / (length - 1));
    }
    
  
    /**
       a faster algorithm for calculating the standard deviation 
    */
    public static double ssq_fast (double[] x) {
	int length=Array.getLength(x);
	double sumx=0.0;
	double sumxs=0.0;
	int i;
	for (i=0; i<length;i++) {
	    sumxs += x[i]*x[i];
	    sumx += x[i];
	}
	return (sumxs - sumx * sumx / length) / (length - 1);
    }
    

    /**
       Calculate the pearson correlation of two arrays
    */
    public static double pearson_correlation (double[] x, double[] y)
	
    {
	char length=(char)(Array.getLength(x));
	char j;
	double yt, xt;
	double syy, sxy, sxx, ay, ax;
	syy = 0.0;
	sxy = 0.0;
	sxx = 0.0;
	ay = 0.0;
	ax = 0.0;
	
	/* mean */
	
	for (j=0; j<length; j++) {
	    ax+=x[j];
	    ay+=y[j];
	}
	ax/=length;
	ay/=length;
	
	for (j=0;j<length;j++) {
	    xt=x[j]-ax;
	    yt=y[j]-ay;
	    sxx+=xt*xt;
	    syy+=yt*yt;
	    sxy+=xt*yt;
	}
	return sxy/Math.sqrt(sxx*syy);
    } /* pearson_correlation */
    
    

    /**
       choose n random integers from 0 to max without repeating 
    */
    public void chooserandom(int[] randomnums, boolean[] recLog, int max, int n) {
	int numgot;
	int i;
	int newnum;
	
	numgot = 0;

	while (numgot < n) { /* numgot is the index of the last gotten item */
	    newnum =generator.nextInt(max);
	    if (!recLog[newnum]) {
		randomnums[numgot] = newnum;
		recLog[newnum] = true;
		numgot++;
	    }
	}
	
	// reset all elements in recLog to false
	for(i=0; i<n; i++){
		recLog[randomnums[i]] = false;
	}
	
    }

    /*****************************************************************************************/
    /* choose n random integers from 0 to max without repeating */
    /*****************************************************************************************/
    public void chooserandom_1(int[] randomnums,  int max, int n) {

	int numgot;
	int i;
	int newnum;
	boolean[] recLog = new boolean[max];
	
	numgot = 0;

	while (numgot < n) { /* numgot is the index of the last gotten item */
	    newnum = generator.nextInt(max);
	    if (!recLog[newnum]) {
		randomnums[numgot] = newnum;
		recLog[newnum] = true;
		numgot++;
	    }
	}
    }
    
    /*****************************************************************************************/
    /* choose n random integers from 0 to max without repeating */
    /*****************************************************************************************/
    public void chooserandom_2(int[] randomnums, int max, int n) {
	int numgot;
	int i;
	int newnum;
	boolean repeat = false;
	
	numgot = 0;
	
	while (numgot < n) { /* numgot is the index of the last gotten item */
	    newnum =generator.nextInt(max);
	    repeat = false;
	    for (i=0; i<=numgot; i++) { /* check got for repeats */
		if( randomnums[i] == newnum) { /* it is a repeat */
		    repeat = true;
		    break;
		}
	    }
	    if (repeat == false ) {
		randomnums[numgot] = newnum;
		numgot++;
	    }
	}
    } 

    /**
       Same as chooserandom, but with replacement -- that is, repeats are allowed.
    */
    public void chooserandom_wrep(int[] randomnums, int max, int n) {
	int numgot;
	int newnum, i;
	int repeat;
	
	numgot = 0;
	for (i = 0 ; i<n; i++) {
	    newnum = (char)(generator.nextInt() % max);
	    repeat = 0;
	    randomnums[i] = newnum;
	    
	}
    } /* */
    

    /**
       Calculate the means of a matrix's rows.
    */
    public void matrix_row_means (Matrix M, double[] means) {
	int i;
	for (i=0; i<M.get_num_rows(); i++) {
	    means[i] = mean(M.get_ith_row(i));
	}
    } /* matrix_row_sums */   
    
    
    /*****************************************************************************************/
    /* calculate the sums of a matrix's rows. */
    /*****************************************************************************************/
    public void matrix_row_sums (Matrix M, double[] sums) {
	int i;
	for (i=0; i<M.get_num_rows(); i++) {
	    sums[i] = sum(M.get_ith_row(i));
	}
    } /* matrix_row_sums */
    
    
    /*****************************************************************************************/
    /* calculate the standard deviation of each row of a matrix */
    /*****************************************************************************************/
    public void matrix_row_stdevs (Matrix M, double[] means, double[] ssqs,double[] stdevs) {
	int i;
	for (i = 0; i<M.get_num_rows(); i++) {
	    stdevs[i] = stdev(M.get_ith_row(i), means[i], ssqs[i]);
	}
    }
    
    
    /*****************************************************************************************/
    /* calculate the sum of squares for each row of a matrix */
    /*****************************************************************************************/
    public void matrix_row_ssqs (Matrix M, double[] means,double[] ssqs) {
	int i;
	if (means == null) {
	    double[] my_means = new double[M.get_num_rows()];
	    matrix_row_means(M, my_means);
	    for (i = 0; i<M.get_num_rows(); i++) {
		ssqs[i] = ssq(M.get_ith_row(i), my_means[i]);
	    }
	    
	} else {
	    for (i = 0; i<M.get_num_rows(); i++) {
	  ssqs[i] = ssq(M.get_ith_row(i), means[i]);
	    }
	}
    } /* matrix_row_ssqs */
    
    
    /**
       Faster algorithm for calculating the sum of squares of each row of a matrix
    */
    public void matrix_row_ssqs_fast (Matrix M,double[] ssqs) {
	int i;
	for (i = 0; i<M.get_num_rows(); i++) {
	    ssqs[i] = ssq_fast(M.get_ith_row(i));
	}
    } /* matrix_row_ssqs */

    
    /**
       Calculate a correlation matrix for a matrix.
       @param M input matrix
       @param C output matrix
     */
    public void correl_matrix (Matrix M,Matrix C)
    {
	//int i=0, j=0, k=0;
	double sxx=0.0, sxy=0.0, ax=0.0;
	double[] means = new double[M.get_num_rows()];
	double[] sss = new double[M.get_num_rows()];
	double xt=0.0;
	sxy = 0.0;
	sxx = 0.0;
	ax = 0.0;

	/* calculate mean and sumsq for each row */
	for (int i=0; i<M.get_num_rows(); i++) {
	    ax = 0.0;
	    sxx = 0.0;
	    for (int j=0; j<M.get_num_cols(); j++) {
		ax+=M.get_matrix_val(i,j);
	    }
	    means[i] = (ax /M.get_num_cols());
	    
	    for (int j=0; j<M.get_num_cols(); j++) {
		xt = M.get_matrix_val(i,j) - means[i]; /* deviation from mean */
		sxx += xt*xt; /* sum of squared error */
	    }
	    sss[i]=sxx;
	}
	
	/* now for each vector, compare it to all other vectors, avoid repeating things */

	
	for (int i=0; i<M.get_num_rows(); i++) { 
	    int l=i;
	    for (int j=l; j<M.get_num_rows();j++) {
		if (j==i) {
		    C.set_matrix_val(i,j,1.0);
		} else {
		    sxy = 0.0;
		    for (int k=0; k<M.get_num_cols();k++) {
			sxy+=(M.get_matrix_val(i,k) - means[i])*(M.get_matrix_val(j,k) - means[j]);
		    }
		    C.set_matrix_val(i,j,sxy/(double)Math.sqrt(sss[i]*sss[j]));
		    C.set_matrix_val(j,i,C.get_matrix_val(i,j)); /* mirror image */
		}
	    }
	}
	sss=null;
	means=null;
    } /* correl_matrix */
    
    
    /**
       Given an array, calculate the quantile requested.
    
     */
    public static double calculate_quantile (int index, double[] random_class, int size)
	
    {
	double pivot = -1.0;
	if(index == 0){
	    double ans = random_class[0];
	    for(int i=1; i<size; i++)
		if(ans > random_class[i])
		    ans = random_class[i];
	    //System.out.println("indx0");
	    return ans;
	} else {
	    double[] temp = new double[size];
	    
	    for(int i=0; i<size; i++){
		temp[i] = random_class[i];
	      //System.out.println("temp["+i+"]= "+temp[i]);
	    }
	    try{
		pivot = temp[0];
		
		double[] smaller = new double[size];
		double[] bigger = new double[size];
		int itrSm = 0;
		int itrBg = 0;
		for(int i=1; i<size; i++){
		    if(temp[i] <= pivot){
			smaller[itrSm] = temp[i];
			itrSm++;	
		    }else if(temp[i] > pivot){	
			bigger[itrBg] = temp[i];
			itrBg++;	
		    }
		}
		if(itrSm > index){
		    //System.out.println("Small  pivot="+pivot+ "  sm= " + itrSm + "  bg= " + itrBg +"  index="+index);
		    return calculate_quantile(index, smaller, itrSm);
		}else if (itrSm < index){
		  //System.out.println("Big    pivot="+pivot+ "  sm= " + itrSm + "  bg= " + itrBg +"  index="+index);
		    return calculate_quantile(index-itrSm-1, bigger, itrBg);
		} else {
		    //System.out.println("DONE   pivot="+pivot+ "  sm= " + itrSm + "  bg= " + itrBg +"  index="+index);
		    return pivot;
		}
	    } catch(ArrayIndexOutOfBoundsException e){
		System.out.println("\n\nERROR:" + "  index="+index+ "  size="+size+ "  pivot="+pivot);
		for(int i=0; i<random_class.length; i++){
		    System.out.print("random_class["+i+"]= "+random_class[i]);
		    if (i<=size)
			System.out.println("   temp["+i+"]= "+temp[i]);
	      }
		return -1.0;
	    }
	}
    }

    
    


    /**
       calculate the mean of the values above a particular quantile of an
       array.  Quantile must be a value from 0 to 100.
       @param quantile A value from 0 to 100
       @param array Array for which we want to get the quantile.
       @param size The size of the array.
       
    */
    public static double calculate_mean_above_quantile (int quantile, double[] array, int size) 
    {
	double[] temp = new double[size];
	double median;
	double returnvalue = 0.0;
	int k = 0;

	temp = array;
	median = calculate_quantile(quantile, array, size);
    
	for (int i=0; i<size; i++) {
	    if(temp[i] >= median){
		returnvalue += temp[i];
		k++;
	    }
	}
	return(returnvalue/k);
    }

    
    
    /**
       Calculate pval of hypergeometric distribution
       @param N1 Number of positives in the data
       @param n1 Number of 'successes'
       @param N2 Number of negatives in the data
       @param n2 Number of 'failures'
    */
    public static double hyperPval (int N1, int n1, int N2, int n2)
    {
	//	return (n_choose_n(N1,n1)/n_choose_n(N1+N2,n1+n2))*n_choose_n(N2,n2);
	return SpecFunc.binomial_coeff(N1,n1)*SpecFunc.binomial_coeff(N2,n2)/SpecFunc.binomial_coeff(N1+N2,n1+n2);
    }
    

    /**
     * Calculates the ranking of each gene in the entire data set.
     * @param A map with keys strings, values doubles.
     * @return A LinkedHashMap keys=old keys, values=integer rank of the gene.
     */
    public static LinkedHashMap rankOf(Map m){
	int counter = 0;
	LinkedHashMap result = new LinkedHashMap();
	geneNpval[] pvalArray = new geneNpval[m.size()];
	Collection entries = m.entrySet();
	Iterator itr = entries.iterator();

	/* put the pvalues into an array of objects which contain the
	 * pvalue and the gene id */
	while(itr.hasNext()) {
	    Map.Entry tuple = (Map.Entry)itr.next();
	    String key = (String)tuple.getKey();
	    double val = (double)(((Double)tuple.getValue()).doubleValue());
	    pvalArray[counter] = new geneNpval(key, val);
	    counter++;
	}
	
	/* sort it */
	Arrays.sort(pvalArray); 
	
	/* put the sorted items back into a hashmap with the rank */
	for(int i=0; i<m.size(); i++){
	    result.put(pvalArray[i].getId(), new Integer(m.size()-i));
	}
	return result;
    }

    
    /**
       Calculate area under ROC
    */
    public static double arocRate(int totalSize, Map ranks)
    {
	int k = 0;
	int targetSize = ranks.size();
	if (targetSize == 0)
	    return 0.0;
	double result = 0.0;
	for(int i=1; i<= totalSize; i++){
	    if( ranks.containsKey(new Integer(i))){
		k++;
	    } else {
		result += k;
	    }
	    
	    if(k == targetSize){
		result += targetSize*(totalSize - i);
		break;	
	    }	
	}
	return result/(k*(totalSize-k));
    }
  
    /**

       For an AROC value, calculates a p value based on approximation
       for calculating the stanadard deviation.

       @param numpos How many positives are in the data.
       @param aroc The AROC
       @return The p value.
     */
    public static double rocpval(int numpos, double aroc)
    {
	double logstdev = 0.0;
	double stdev = Math.exp(-0.5 * (Math.log(numpos) + 1));
	double z = (aroc - 0.5)/stdev;
	
	/* We are only interested in the upper tails. */
	if (z < 0.0) {
	    z = 0.0;
	}
	
	//	System.err.println("Size: " + numpos + " Stdev: " + stdev + " aroc: " + aroc + " z: " + z);

	return SpecFunc.normdist(z);
    }
    

    /**
       return mean of top 2 elements in array, for histogram range setting            tt12
    */
    public static double meanOfTop2(double[] inArray){
	double max1 = 0;
	double max2 = 0;
	int pin = 0;
	
	if (inArray.length == 0) {
	    System.err.println("No values for meanofTop2!");
	    System.exit(1);
	}

	for(int i=0; i<inArray.length; i++){
	    if(max1 < inArray[i]){
		max1 = inArray[i];
		pin = i;
	    }
	}	
	for(int i=0; i<inArray.length; i++){
	    if(max2 < inArray[i] && i != pin)
		max2 = inArray[i];	
	}	
	return (max1+max2)/2;
    }
  
  
    /**
       get file path todo: why is this int the stats object?
    */
    public static String getCanonical(String in) {
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
    
    
    /**
       Select a random number from a normal distribution with mean zero
       and variance 1
    */
    public double randomGaussian () {
	return generator.nextGaussian();
    }
    


    /**

       main tests some functions.

     */
    public static void main (String[] args) {
	int a,b,c,d;
	
	a = 8;
	b = 3;
	c = 6;
	d = 2;
	double testhyper = hyperPval (a,b,c,d);
	System.err.println(testhyper);

	double testerfc = SpecFunc.erfc(1.0);
	double testerf = SpecFunc.erf(1.0);
	System.err.println(testerfc);
	System.err.println(testerf);
	System.err.println("one-tailed normal for " + 1.0 + ": " + SpecFunc.normdist(1.0));
	System.err.println("one-tailed normal for " + 2.0 + ": " + SpecFunc.normdist(2.0));
	System.err.println("one-tailed normal for " + 3.0 + ": " + SpecFunc.normdist(3.0));
	System.err.println("one-tailed normal for " + 4.0 + ": " + SpecFunc.normdist(4.0));
	System.err.println("one-tailed normal for " + 5.0 + ": " + SpecFunc.normdist(5.0));
    }
    
} // end of class


/**
   Record the execution time between start() and stop() for testing                
*/
class timeCounter {
    static long tempTime;
    
    public static void start(){	
    	tempTime = System.currentTimeMillis();
    }
    
    public static void stop(){	
    	System.out.println("Excution time: " + (System.currentTimeMillis()-tempTime));
    }
}

/**
 Show the elements in array for testing                
*/
class showArray{
    public static void show(int[] a, int n){	
    	for(int i=0; i<n; i++){
    	    System.out.println(i + "\t" + a[i]);	
    	}
	System.out.println("\n");
    }

    public static void show(double[] a, int n){	
    	for(int i=0; i<n; i++){
    	    System.out.println(i + "\t" + a[i]);	
    	}
	System.out.println("\n");
    }

}

/** 
    Simple data structure used by Stats.rankOf().
    @author Edward Chen
 */
class geneNpval implements Comparable{
    private String gene_id;
    private double pval;
    
    public geneNpval(String id, double pv){
	gene_id = id;
	pval = pv;	
    }
    
    public int compareTo(Object ob){
	geneNpval other = (geneNpval)ob;
	
	if(this.pval>other.pval)
	    return 1;
	else if(this.pval<other.pval)
	    return -1;
	else
	    return 0;
    }
    
    public String getId(){
	return gene_id;	
    }
    
    public double getPval(){
	return pval;  
    }
}
