package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Has mathematical functions 
                                                                                                                                                            
*******************************************************************************/




import scores.class_score.*;
import java.io.*; 
import java.util.*;
import java.math.*;
import java.lang.*;
import java.lang.reflect.*;
import java.security.*;




/*****************************************************************************************/
public class Stats implements Cloneable,ConstantStuff// mathematical calculations
/*****************************************************************************************/
{
    
    private Random generator = new Random(System.currentTimeMillis());
    public Stats() {}

/*****************************************************************************************/
    public Object clone() {
/*****************************************************************************************/
	Object o =null;
	try {
	    o=super.clone();
	} catch (CloneNotSupportedException e) {}
	return o;
    }



/*****************************************************************************************/
/* calculate the sum of the elements of an array */
    public static double sum(double[] x)
/*****************************************************************************************/
    {
	int length=Array.getLength(x);
	double total =0.0;
	int i;
	for (i=0; i<length; i++) {
	    total += x[i];
	}
	return total;
    }

/*****************************************************************************************/
/* calculate the mean of an array's elements*/ 
    public static double mean(double[] x)
/*****************************************************************************************/
    {
	int length=Array.getLength(x);
	double sum=0.0;
	int i;
	for (i=0; i<length; i++) {
	    sum += x[i];
	}
	if (length == 0.0) {
	    return 0.0; 
	} else {
	    return sum / length; 
	}	
    }
    

/*****************************************************************************************/
    /* calculate the sum of squared deviation from the mean of an array */
    public static double ssq (double[] x,double mean)
/*****************************************************************************************/
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
    

/*****************************************************************************************/
    /* calculate the standard deviation of an array */
    public static double stdev (double[] m,double mean, double ssqs) {
/*****************************************************************************************/
	int length=Array.getLength(m);
	if (length < 2) {
	    return -1.0;
	}
	return Math.sqrt(ssqs / (length - 1));
    }
    
  
/*****************************************************************************************/  
    /* a faster algorithm for calculating the standard deviation */
    public static double ssq_fast (double[] x) {
/*****************************************************************************************/
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
    

/*****************************************************************************************/ 
/* calculate the pearson correlation of two arrays */
public static double pearson_correlation (double[] x, double[] y)
/*****************************************************************************************/
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
    


    /*****************************************************************************************/
    /* choose n random integers from 0 to max without repeating */
    /*****************************************************************************************/
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
    
    /*****************************************************************************************/
    // create double[] ug_pval 
    /*****************************************************************************************/
    /*    
	  public double[] ugPvalGenerator(Map ugProbeMap, Map probePvalMap){
	  Collection ugEntries = ugProbeMap.entrySet();
	  Iterator ugMapItr = ugEntries.iterator();
	  double[] ug_pval_temp = new double[ugProbeMap.size()];
	  int counter = 0;
	  while(ugMapItr.hasNext()){
	  Map.Entry ugTuple = (Map.Entry)ugMapItr.next();
	  ArrayList probes = (ArrayList)ugTuple.getValue();
	  Iterator pbItr = probes.iterator();
	  //System.out.println("pos = "+pos+"   probes.size() = "+probes.size());
	  boolean ugFlag=true;
	  while(pbItr.hasNext()){
	  Object key = probePvalMap.get(pbItr.next());
	  if(key != null){	
	  String pbPval = key.toString();    	        
	  //System.out.println("pbPval = "+pbPval);
	  ug_pval_temp[counter] += Math.pow(10, -1*Double.parseDouble(pbPval));
	  }else{
	  ugFlag = false;
	  //ug_pval_map.put(ugTuple.getKey(), new Double(1.0));
	  break;
	  }
	  }
	  if(ugFlag){
	  ug_pval_temp[counter] /= probes.size();            //take the mean
	  ug_pval_temp[counter] = -(Math.log(ug_pval_temp[counter])/Math.log(10));   //transform to -log (base 10) value
	  //System.out.println("pval["+pos+"] = "+ug_pval[pos]);	    
	  //ug_pval_map.put(ugTuple.getKey(), new Double(ug_pval_temp[counter]));
	  counter++;
	  }
	  }//end of while   
	  ug_pval = new double[counter];   // counter = the number of unigene_id that actually appears in pval file
	  for(int i=0; i<counter; i++){
	  ug_pval[i] = ug_pval_temp[i];	
	  }
	  return ug_pval;
	  }    
    */    
    
    /*****************************************************************************************/
    /* same as chooserandom, but with replacement */
    /*****************************************************************************************/
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
    

    /*****************************************************************************************/
    /* calculate the means of a matrix's rows. */
    public void matrix_row_means (Matrix M, double[] means) {
	/*****************************************************************************************/
	int i;
	for (i=0; i<M.get_num_rows(); i++) {
	    means[i] = mean(M.get_ith_row(i));
	}
    } /* matrix_row_sums */   
    
    
    /*****************************************************************************************/
    /* calculate the sums of a matrix's rows. */
    public void matrix_row_sums (Matrix M,double[] sums) {
	/*****************************************************************************************/
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
    
    
    /*****************************************************************************************/   
    /* faster algorithm for calculating the sum of squares of each row of a matrix */
    /*****************************************************************************************/
    public void matrix_row_ssqs_fast (Matrix M,double[] ssqs) {
	int i;
	for (i = 0; i<M.get_num_rows(); i++) {
	    ssqs[i] = ssq_fast(M.get_ith_row(i));
	}
    } /* matrix_row_ssqs */

    
    /*****************************************************************************************/
    /*****************************************************************************************/
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
    
    
    /*****************************************************************************************/
    /*****************************************************************************************/
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
	}else{
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

    
    
    /*****************************************************************************************/
    public static double calculate_quantile_1 (int quantile, double[] random_class, int size)
	/*****************************************************************************************/
    {
	int[] temp = new int[size];
	int[] count = new int[25000];
	double returnvalue = 0.0;
	double fract = (double)quantile/100.0;
	int index = (int)Math.ceil(fract*(size-1));
	
	if (fract > 1.0 || fract < 0.0) {
	    System.out.println("Quantile is illegal\n");
	}
	//for (int i=0; i<Math.min(10,random_class.length); i++)
	//  System.out.println("rdm  " + random_class[i]);
	
	/* make a O(n) sorting algorithm, return the Median. */
	for (int i=0; i<size; i++){
	    temp[i] = (int)(1000*random_class[i]);
	}
	
	for (int i=0; i<size; i++){
	    count[temp[i]]++;
	}
	
	for (int i=0; i<25000; i++){
	    index -= count[i];
	    if(index <= 0){
		//    System.out.println("medium p_val= " + (i/1000.00));
		return(i/1000.00);          
	    }
	}
  return 0.0;
    }
    
    /*****************************************************************************************/
    public static double calculate_quantile_2 (int quantile, double[] random_class, int size)
	/*****************************************************************************************/
    {
	double[] temp = new double[size];
	double returnvalue = 0.0;
	double fract = (double)quantile/100.0;
	int index = (int)Math.ceil(fract*(size-1));
	
	if (fract > 1.0 || fract < 0.0) {
	    System.out.println("Quantile is illegal\n");
	}
	
	/* make a sorted copy of the array, return the index value. */
	temp = random_class;
	Arrays.sort(temp);  
	returnvalue = random_class[index];
	return(returnvalue);
    }
  


    /*****************************************************************************************/
    /* calculate the mean of the values above a particular quantile of an
       array.  Quantile must be a value from 0 to 100.*/
    public static double calculate_mean_above_quantile (int quantile, double[] random_class, int size) 
	/*****************************************************************************************/
    {
	double[] temp = new double[size];
	double median;
	double returnvalue = 0.0;
	int k = 0;
	/* make a sorted copy of the array, return the index value. */
	temp = random_class;
	median = calculate_quantile(quantile, random_class, size);
    
	for (int i=0; i<size; i++) {
	    if(temp[i] >= median){
		returnvalue += temp[i];
		k++;
	    }
	}
	return(returnvalue/k);
    }

    /*****************************************************************************************/
    /* calculate the mean of the values above a particular quantile of an
       array.  Quantile must be a value from 0 to 100.*/
    public static double calculate_mean_above_quantile_2 (int quantile, double[] random_class, int size) 
	/*****************************************************************************************/
    {
	double[] temp = new double[size];
	double returnvalue = 0.0;
	int i,k;
	double fract = (double)quantile/100.0;
	int index = (int)Math.floor(fract*size);
	/* make a sorted copy of the array, return the index value. */
	temp = random_class;
	Arrays.sort(temp);
	k=0;
	for (i=index; i<size; i++) {
	    returnvalue += temp[i];
	    k++;
	}
	return(returnvalue/k);
    }
    
    
    /*****************************************************************************************/
    // method to calculate pval of hypergeometric distribution
    /*****************************************************************************************/  
    public static double hyperPval (int N1, int n1, int N2, int n2) // todo: make this use binomial approx when needed.
    {
	return (n_choose_n(N1,n1)/n_choose_n(N1+N2,n1+n2))*n_choose_n(N2,n2);
    }
    

    public static double n_choose_n (int N, int n)
    {
	double total = 1;
	for(int i=0; i<n; i++){
	    total *= ((N-i)/(double)(i+1));
	}
    return total;
    }


    /**
     * Calculates the ranking of each gene.
     * @return A LinkedHashMap keys=gene ids, values= rank of the gene.
     */
    public static LinkedHashMap rankOf(Map m){
	int counter = 0;
	LinkedHashMap result = new LinkedHashMap();
	gene_pval[] pvalArray = new gene_pval[m.size()];
	Collection entries = m.entrySet();
	Iterator itr = entries.iterator();

	/* put the pvalues into an array of objects which contain the
	 * pvalue and the gene id */
	while(itr.hasNext()) {
	    Map.Entry tuple = (Map.Entry)itr.next();
	    String key = (tuple.getKey()).toString();
	    double val = Double.parseDouble((tuple.getValue()).toString());
	    pvalArray[counter] = new gene_pval(key, val);
	    counter ++;
	}
	
	/* sort it */
	Arrays.sort(pvalArray); 
	
	/* put the sorted items back into a hashmap with the rank */
	for(int i=0; i<m.size(); i++){
	    result.put(pvalArray[i].getId(), new Integer(m.size()-i));
	}
	
	return result;
    }

    
    /*****************************************************************************************/
    // method to calculate area under ROC                                           tt6
    /*****************************************************************************************/  
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
  
    
    public static double rocpval(Map ranks, double aroc)
    {
	double result = 0.0;
	double logstdev = 0.0;
	int size = ranks.size();
	double stdev = Math.exp(-0.5 * (Math.log(size) + 1));
	double z = (aroc - 0.5)/stdev;
	
	//	System.err.println("Size: " + size + " Stdev: " + stdev + " aroc: " + aroc + " z: " + z);

	result = 1.0 - 0.5*erfc(-z/Math.sqrt(2.0));
	
	return result;
    }
    
    static private double erfc(double a)
	throws ArithmeticException { 
	double x,y,z,p,q;
	
	double P[] = {
	    2.46196981473530512524E-10,
	    5.64189564831068821977E-1,
	    7.46321056442269912687E0,
	    4.86371970985681366614E1,
	    1.96520832956077098242E2,
	    5.26445194995477358631E2,
	    9.34528527171957607540E2,
	    1.02755188689515710272E3,
	    5.57535335369399327526E2
	};
	double Q[] = {
	    //1.0
	    1.32281951154744992508E1,
	    8.67072140885989742329E1,
	    3.54937778887819891062E2,
	    9.75708501743205489753E2,
	    1.82390916687909736289E3,
	    2.24633760818710981792E3,
	    1.65666309194161350182E3,
	    5.57535340817727675546E2
	};
	
	double R[] = {
	    5.64189583547755073984E-1,
	    1.27536670759978104416E0,
	    5.01905042251180477414E0,
	    6.16021097993053585195E0,
	    7.40974269950448939160E0,
	    2.97886665372100240670E0
	};
	double S[] = {
	    //1.00000000000000000000E0, 
	    2.26052863220117276590E0,
	    9.39603524938001434673E0,
	    1.20489539808096656605E1,
	    1.70814450747565897222E1,
	    9.60896809063285878198E0,
	    3.36907645100081516050E0
	};
	
	if( a < 0.0 )   x = -a;
	else            x = a;
    
	if( x < 1.0 )   return 1.0 - erf(a);
	
	z = -a * a;
	
	if( z < -700 ) {
	    if( a < 0 )  return( 2.0 );
	    else         return( 0.0 );
	}
	
	z = Math.exp(z);
	
	if( x < 8.0 ) {
	    p = polevl( x, P, 8 );
	    q = p1evl( x, Q, 8 );
	} else {
	    p = polevl( x, R, 5 );
	    q = p1evl( x, S, 6 );
	}
	
	y = (z * p)/q;
	
	if( a < 0 ) y = 2.0 - y;
	
	if( y == 0.0 ) {
	    if( a < 0 ) return 2.0;
	    else        return( 0.0 );
	}
	
	return y;
    }
    
    
    static public double erf(double x)
                       throws ArithmeticException { 
	double y, z;
	double T[] = {
	    9.60497373987051638749E0,
	    9.00260197203842689217E1,
	    2.23200534594684319226E3,
	    7.00332514112805075473E3,
	    5.55923013010394962768E4
                    };
	double U[] = {
	    //1.00000000000000000000E0,
	    3.35617141647503099647E1,
	    5.21357949780152679795E2,
	    4.59432382970980127987E3,
	    2.26290000613890934246E4,
	    4.92673942608635921086E4
	};
	
	if( Math.abs(x) > 1.0 ) return( 1.0 - erfc(x) );
	z = x * x;
	y = x * polevl( z, T, 4 ) / p1evl( z, U, 5 );
	return y;
    }



    static  private double polevl( double x, double coef[], int N )
	throws ArithmeticException {
	
	double ans;
	ans = coef[0];
	for(int i=1; i<=N; i++) { ans = ans*x+coef[i]; }
	return ans;
    }
    
    static  private double p1evl( double x, double coef[], int N )
	throws ArithmeticException {
	
	double ans;
	ans = x + coef[0];
	for(int i=1; i<N; i++) { ans = ans*x+coef[i]; }
	return ans;
    }
    

    /*****************************************************************************************/
    // return mean of top 2 elements in array, for histogram range setting            tt12
    /*****************************************************************************************/  
    
    static double meanOfTop2(double[] inArray){
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
  
  
    /******************************************************************************************/
    //  get file path
    /******************************************************************************************/
    
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
    
    
    /*****************************************************************************************/
    /* Select a random number from a normal distribution with mean zero
       and variance 1*/
    /*****************************************************************************************/
    public double randomGaussian () {
	return generator.nextGaussian();
    }
    
    public static void main (String[] args) {
	

    }
    
} // end of class

/*****************************************************************************************/
/* Record the execution time between start() and stop() for testing                */
/*****************************************************************************************/

class timeCounter{
    static long tempTime;
    
    public static void start(){	
    	tempTime = System.currentTimeMillis();
    }
    
    public static void stop(){	
    	System.out.println("Excution time: " + (System.currentTimeMillis()-tempTime));
    }
}

/*****************************************************************************************/
/* Show the elements in array for testing                */
/*****************************************************************************************/

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
