package classScore;
import util.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.text.*;

/**
  Main class to make 'experiment score' pvalues. Includes multiple
  test correction.   Created :09/02/02

  @author Shahmil Merchant; Paul Pavlidis (major changes)
  @version $Id$

*/
public class class_pvals {
    private histogram hist ;
    private Map classToProbe;
    private Map goNames;
    private Map probe_group;
    private exp_class_scores probe_pval;
    private GoName_parse goName;
    private Group_Parse groupName;
    private double user_pvalue;
    private String dest_file;
    private ClassMap probe_class;
    private boolean weight_on = true;
    private boolean dolog = true;
    private LinkedHashMap results = null;
    private Vector sortedclasses = null;
    private Map effective_sizes = null;
    private Map actual_sizes = null;
    private Map target_ranks;
    private Map record;
    private int inputSize;
    private int numOverThreshold = 0; // number of genes over the threshold
    private int numUnderThreshold = 0; // number of genes below the threshold
    private NumberFormat nf = NumberFormat.getInstance();


    /**
     */
    public class_pvals(String probe_pvalfile,
		       String probe_classfile,
		       String goNamesfile,
		       String destination_file,
		       String group_file,
		       String method,
		       String groupMethod,
		       int class_max_size,
		       int class_min_size,
		       int number_of_runs,
		       int quantile,
		       double pval,
		       String wt_check,
		       int pvalcolumn,
		       String dolog_check,
		       String mtc_method) {

	nf.setMaximumFractionDigits(8);

	// user flags and constants:
	user_pvalue = -(Math.log(pval)/Math.log(10));// user defined pval (cutoff) for hypergeometric todo: this should NOT be here. What if the cutoff isn't a pvalue. See pvalue parse.
	weight_on =(Boolean.valueOf(wt_check)).booleanValue();
	dolog = (Boolean.valueOf(dolog_check)).booleanValue();

	classToProbe = new LinkedHashMap();


	effective_sizes = new HashMap();
	actual_sizes = new HashMap();
	dest_file = destination_file;
	target_ranks = new HashMap(); // will hold ranks of items in a class.
	record = new HashMap(); // scratch space to record those probes that have been seen when iterating over a class.

	goName = new GoName_parse(goNamesfile); // parse go name file

	probe_pval = new exp_class_scores(probe_pvalfile, wt_check, method, pvalcolumn, dolog, class_max_size, class_min_size, number_of_runs, quantile);

	groupName = new Group_Parse(group_file, probe_pval.get_map()); // parse group file. Yields map of probe->replicates.
	probe_group = groupName.get_probe_group_map(); // map of probes to groups

	if(weight_on)
	    probe_pval.set_input_pvals(groupName.get_group_probe_map(), groupMethod); // this initializes the group_pval_map, Calculates the ave/best pvalue for each group

	probe_class = new ClassMap(probe_classfile, probe_pval.get_map() ); // parses affy->classes file. Yields map of go->probes
	classToProbe = probe_class.get_class_map(); // this is the map of go->probes
	System.err.println("Read in classes and identified redundant classes");


	System.out.println("Read in the files and parameters");

	// Calculate random classes. todo: what a mess. This histogram should be held by the class that originated it.
	hist = probe_pval.generateNullDistribution();
	System.err.println(hist.toString());

	// Initialize the results data structure.
	results = new LinkedHashMap();

	// get the class sizes.
	class_sizes();

	Collection inp_entries;
	Map input_rank_map;
	if (weight_on) {
	    inp_entries = probe_pval.get_group_pval_map().entrySet();
	    input_rank_map = Stats.rankOf(probe_pval.get_group_pval_map());
	} else {
	    inp_entries = probe_pval.get_map().entrySet();
	    input_rank_map =  Stats.rankOf(probe_pval.get_map());
	}

	inputSize = input_rank_map.size(); // how many pvalues. This is constant under permutations of the data
	hgSizes(inp_entries); // get numOverThreshold and numUnderThreshold. Constant under permutations of the data.

	System.err.println("input size=" +  inputSize + " numOverThreshold=" + numOverThreshold + " numUnderThreshold=" + numUnderThreshold + " " );//+  + "" + foo + "" + foo + "" + foo + "" + foo );

	// calculate the actual class scores and correct sorting.
	class_pval_generator(probe_pval.get_group_pval_map(), probe_pval.get_map(), input_rank_map);
	sortResults();

	if (mtc_method.equals("bon")) {
	    correct_pvals(); // no arg: bonferroni. integer arg: w-y, int trials. Double arg: FDR
	} else if (mtc_method.equals("bh")) {
	    correct_pvals(0.05);
	} else if (mtc_method.equals("wy")) {
	    correct_pvals(10000);
	}

	// all done:
	// print the results
	class_pval_print(true);
    }

    /**
       This is stripped-down version of scoreClass. We use this when
       doing permutations, it is much faster.
     */
    public double classPvalue(String class_name, Map group_pval_map, Map probesToPvals, Map input_rank_map) {

	double pval = 0.0;
	double rawscore = 0.0;
	ArrayList values = (ArrayList)classToProbe.get(class_name);
	Iterator classit = values.iterator();

	int in_size = (int)((Integer)effective_sizes.get(class_name)).intValue(); // effective size of this class.
	if (in_size < probe_pval.get_class_min_size() || in_size > probe_pval.get_class_max_size() ) {
	    return -1.0;
	}

	double[] groupPvalArr = new double[in_size]; // store pvalues for items in the class.

	record.clear();
	target_ranks.clear();

	int v_size = 0;

	// foreach item in the class.
	// todo: see if this loop can be optimized. Probably. It's important when we are doing random trials that this go fast.
	while(classit.hasNext()) {
	    String probe = (String)classit.next();  // probe id

	    if (probesToPvals.containsKey(probe)){ // if it is in the data set. This is invariant under permutations.

		if (weight_on == true) {
		    Double grouppval = (Double)group_pval_map.get(probe_group.get(probe)); // probe -> group
		    if(!record.containsKey(probe_group.get(probe))) { // if we haven't done this probe already.
			record.put(probe_group.get(probe), null); // mark it as done.
			groupPvalArr[v_size] = grouppval.doubleValue();
			v_size++;
		    }

		} else {
		    System.err.println("Sorry, you can't use this without weights");
		    System.exit(1);
		}
	    } // if in data set
	} // end of while over items in the class.

	// get raw score and pvalue.
	rawscore = probe_pval.calc_rawscore(groupPvalArr, in_size);
	pval = scoreToPval(in_size, rawscore);

	if (pval < 0) {
	    System.err.println("Warning, a rawscore yielded an invalid pvalue: Classname: " + class_name);
	}
	return pval;
    }


    /**
     * Get results for one class, based on class id. The other
     * arguments are things that are not constant under permutations
     * of the data.
     *
     * @param class_name a <code>String</code> value
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     * @param input_rank_map a <code>Map</code> value
     * @return a <code>classresult</code> value
     */
    public classresult scoreClass (String class_name, Map group_pval_map, Map probesToPvals, Map input_rank_map) {
	//inputs for hypergeometric distribution
	int successes = 0; // number of genes in this class which are above the threshold
	int failures = 0; // number of genes in this calss which are below the threshold

	//variables for outputs
	double pval = 0.0;
	double rawscore = 0.0;
	double hyper_pval = -1.0;
	double area_under_roc = 0.0;
	double roc_pval = 0.0;

	int in_size = (int)((Integer)effective_sizes.get(class_name)).intValue(); // effective size of this class.
	if (in_size < probe_pval.get_class_min_size() || in_size > probe_pval.get_class_max_size() ) {
	    return null;
	}

	ArrayList values = (ArrayList)classToProbe.get(class_name);
	Iterator classit = values.iterator();
	double[] groupPvalArr = new double[in_size]; // store pvalues for items in the class.

	record.clear();
	target_ranks.clear();
	Object ranking = null;

	int v_size = 0;

	// foreach item in the class.
	// todo: see if this loop can be optimized. Probably. It's important when we are doing random trials that this go fast.
	while(classit.hasNext()) {

	    String probe = (String)classit.next();  // probe id

	    if (probesToPvals.containsKey(probe)){ // if it is in the data set. This is invariant under permutations.

		if (weight_on == true) {
		    Double grouppval = (Double)group_pval_map.get(probe_group.get(probe)); // probe -> group
		    if(!record.containsKey(probe_group.get(probe))) { // if we haven't done this probe already.
			record.put(probe_group.get(probe), null); // mark it as done.
			groupPvalArr[v_size] = grouppval.doubleValue();

			//  (hypergeometric) if the user_pval is met by this probe, we count it
			if(groupPvalArr[v_size] >= user_pvalue) {
			    successes++; // successs.
			} else {
			    failures++; // failure.
			}
			v_size++;

			//for aroc.
			ranking = input_rank_map.get(probe_group.get(probe)); // rank of this probe group.
			if (ranking!=null){
			    target_ranks.put(ranking, null); // ranks of items in this class.
			}
		    }

		} else {// no weights
		    Double pbpval = (Double)probesToPvals.get(probe); // pvalue for this probe. This will not be null if things have been done correctly so far. This is the only place we need the raw pvalue for a probe.
		    groupPvalArr[v_size] = pbpval.doubleValue();
		    v_size++;

		    // hypergeometric pval info.
		    if(pbpval.doubleValue() >= user_pvalue) {
			successes++; // successs.
		    } else {
			failures++; // failure.
		    }

		    //for roc. Only difference from with weights is that we don't use probe_group.get()
		    ranking = input_rank_map.get(probe);
		    if(ranking!=null){
			target_ranks.put(ranking, null);
		    }

		}
	    } // if in data set
	} // end of while over items in the class.

	// get raw score and pvalue.
	rawscore = probe_pval.calc_rawscore(groupPvalArr, in_size);
	pval = scoreToPval(in_size, rawscore);

	if (pval < 0) {
	    System.err.println("Warning, a rawscore yielded an invalid pvalue: Classname: " + class_name);
	}

	// our 'alternative' scoring methods. First, using the AROC.
	area_under_roc = Stats.arocRate(inputSize, target_ranks);
	roc_pval = Stats.rocpval(target_ranks.size(), area_under_roc);

	// Hypergeometric p value calculation. Using the binomial
	// approximation.  Only look at over-represented
	// genes. Identify these by seeing if the observed is greater
	// than the expected. Otherwise, set the hypergeometric pvalue
	// to be 1.0 (we can change this behavior if desired)
	double pos_prob = (double)in_size /(double)inputSize;
	double expected = (double)numOverThreshold * pos_prob;
	if (successes < expected || pos_prob == 0.0 ) { // fewer than expected, or we didn't/cant get anything.
	    hyper_pval = 1.0 - (double)(SpecFunc.binomialCumProb(successes, numOverThreshold, pos_prob));
	} else {
	    //	hyper_pval = Stats.cumHyperGeometric(numOverThreshold, successes, numUnderThreshold, failures); // using exact, but it runs out of precision too quickly.

	    // successes=number of genes in class which meet criteria
	    // (successes); numOverThreshold= number of genes which
	    // meet criteria (trials); pos_prob: fractional size of
	    // class wrt data size.
	    hyper_pval = SpecFunc.binomialCumProb(successes, numOverThreshold, pos_prob);
	}

	//	System.err.println(class_name + "(" + goName.get_GoName_value_map(class_name) + ") - base prob: " + pos_prob + " successes: " + successes + " failures: " + failures + " Trials: " + numOverThreshold + " Nontrials: " + numUnderThreshold + " H: " + hyper_pval);

	// set up the return object.
	classresult res = new classresult(class_name, goName.get_GoName_value_map(class_name), (int)((Integer)actual_sizes.get(class_name)).intValue(), in_size);
	res.setscore(rawscore);
	res.setpval(pval);
	res.sethypercut(successes);
	res.sethyperp(hyper_pval);
	res.setaroc(area_under_roc);
	res.setarocp(roc_pval);
	return res;

    } /* scoreClass */


    /**
     * Generate a complete set of class results. The arguments are not
     constant under pemutations. The second is only needed for the
     aroc method. This is to be used only for the 'real' data since
     it modifies 'results',
     * @param group_pval_map a <code>Map</code> value
     * @param probesToPvals a <code>Map</code> value
     * @param input_rank_map a <code>Map</code> value
     */
    public void class_pval_generator (Map group_pval_map, Map probesToPvals, Map input_rank_map)
    {
	Collection entries = classToProbe.entrySet(); // go -> probe map. Entries are the class names.
	Iterator it = entries.iterator(); // the classes.

	// For each class.
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    String class_name = (String)e.getKey();
	    classresult res = scoreClass(class_name, group_pval_map, probesToPvals, input_rank_map);
	    if (res != null)
		results.put(class_name, res);
	}
    } /* class_pval_generator */


    /**

    Same thing as class_pval_generator, but returns a collection of
    scores (pvalues) (see below) instead of adding them to the results
    object. This is used to get class pvalues for permutation
    analysis.

    */
    public HashMap class_v_pval_generator(Map group_pval_map, Map probesToPvals, Map input_rank_map)
    {
	Collection entries = classToProbe.entrySet(); // go -> probe map. Entries are the class names.
	Iterator it = entries.iterator(); // the classes.
	//	Vector results = new Vector();
	HashMap results = new HashMap();

	// For each class.
	while(it.hasNext()) {
	    Map.Entry e = (Map.Entry)it.next();
	    String class_name = (String)e.getKey();
	    double pval = classPvalue(class_name, group_pval_map, probesToPvals, input_rank_map);

	    if (pval >= 0.0)
		results.put(class_name, new Double(pval) );

	}
	return results;
    }


    /**
       convert a raw score into a pvalue, based on random background distribution
    */
    public double scoreToPval (int in_size, double rawscore) {
	double pval = hist.get_val(in_size, rawscore);

	if (Double.isNaN(pval))
	    System.err.println("Warning, a pvalue was not a number: raw score = " + rawscore );

	return pval;
    } /* scoreToPval */



    /**
       Sorted order of the class results - all this has to hold is the class names.
    */
    private void sortResults () {
	sortedclasses = new Vector(results.entrySet().size());
	Collection k = results.values();
	Vector l = new Vector();
	l.addAll(k);
	Collections.sort(l);
	for ( Iterator it = l.iterator(); it.hasNext(); ) {
	    sortedclasses.add(((classresult)it.next()).get_class_id());
	}
    } /* sortResults */


    /**
       Print the results
    */
    public void class_pval_print() {
	this.class_pval_print(false);
    }


    /**
       Print the results
       @param sort Sort the results so the best class (by score pvalue) is listed first.
    */
    public void class_pval_print (boolean sort)
    {
	System.err.println("Beginning output");
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(dest_file, false));
	    boolean first = true;
	    classresult res = null;
	    if (sort) {
		for (Iterator it=sortedclasses.iterator(); it.hasNext(); ) {
		    res = (classresult)results.get(it.next());
		    if (first) {
			first = false;
			res.print_headings(out, "\tSame as:\tSimilar to:");
		    }
		    //		    res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
		    res.print(out, format_redundant_and_similar(res.get_class_id()) );
		}
	    } else {
		for (Iterator it=results.entrySet().iterator(); it.hasNext(); ) {
		    res = (classresult)it.next();
		    if (first) {
			first = false;
			res.print_headings(out, "\tSame as:\tSimilar to:");
		    }
		    res.print(out, format_redundant_and_similar(res.get_class_id()) );
		    //		    res.print(out, "\t" + probe_class.getRedundanciesString(res.get_class_id()));
		}
	    }
	    out.close();
	} catch (IOException e) {
	    System.err.println("There was an IO error while printing the results: " + e);
	}
    }


    /**
       Set up the string the way I want it.
     */
    private String format_redundant_and_similar (String classid) {
	ArrayList redund = probe_class.getRedundancies(classid);
	String return_value = "";

	if (redund != null) {
	    Iterator it = redund.iterator();
	    while (it.hasNext()) {
		String nextid = (String)it.next();
		String prefix;
		return_value = return_value + nextid + "|" +  goName.get_GoName_value_map(nextid) + ", ";
	    }
	}

	return_value = return_value + "\t";

	ArrayList similar = probe_class.getSimilarities(classid);

	if (similar != null) {
	    Iterator it = similar.iterator();
	    while (it.hasNext()) {
		String nextid = (String)it.next();
		String prefix;
		return_value = return_value + nextid + "|" +  goName.get_GoName_value_map(nextid) + ", ";
	    }
	    return "\t" + return_value;
	}

	return return_value;

    }


    /**
       Calculate class sizes for all classes - both effective and actual size
    */
    public void class_sizes () {
	Collection entries = classToProbe.entrySet(); // go -> probe map. Entries are the class names.
	Iterator it = entries.iterator();
	Map probetopval = probe_pval.get_map(); // probe->pval map. We do not use the pvalues here, just a list of probes.
	int size;
	int v_size;

	while(it.hasNext()) { // for each class.
	    Map.Entry e = (Map.Entry)it.next(); // next class.
	    String class_name = (String)e.getKey();  // id of the class (GO:XXXXXX)
	    ArrayList values = (ArrayList)e.getValue();  // items in the class.
	    Iterator I = values.iterator();
	    double grouppval;

	    record.clear();
	    size = 0;
	    v_size = 0;

	    while(I.hasNext()){ // foreach item in the class.
		String probe = (String)I.next();

		if (probe != null) {
		    if (probetopval.containsKey(probe)){ // if it is in the data set
			size++;

			if (weight_on) { //routine for weights
			    // compute pval for every replicate group
			    if (probe_pval.get_group_pval_map().containsKey(probe_group.get(probe)) && !record.containsKey(probe_group.get(probe))) { // if we haven't done this probe already.
				record.put(probe_group.get(probe), null); // mark it as done for this class.
				v_size++; // this is used in any case.
			    }
			}
		    }
		} // end of null check
	    } // end of while over items in the class.


	    if (!weight_on)
		v_size = size;

	    effective_sizes.put(class_name, new Integer(v_size));
	    actual_sizes.put(class_name, new Integer(size));
	}

	System.err.println("Got class sizes");
    } /* class sizes */


    /**
       Calculate numOverThreshold and numUnderThreshold for hypergeometric distribution. This
       is a constant under permutations, but depends on weights.
       @param inp_entries The pvalues for the probes (no weights) or groups (weights)
    */
    private void hgSizes (Collection inp_entries) {

	Iterator itr = inp_entries.iterator();
	while(itr.hasNext()) {
	    Map.Entry m = (Map.Entry)itr.next();
	    double groupval = Double.parseDouble((m.getValue()).toString());

	    if(groupval >= user_pvalue)
		numOverThreshold++;
	    else
		numUnderThreshold++;
	}
	System.err.println(numOverThreshold + " genes are above the threshold " + user_pvalue);
    }


    /**

    Bonferroni correction of class pvalues.

    */
    private void correct_pvals() {
	int numclasses = sortedclasses.size();
	double corrected_p;
	for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) {
	    String nextclass = (String)it.next();
	    classresult res = (classresult)results.get(nextclass);
	    double actual_p = res.get_pvalue();
	    corrected_p = actual_p * numclasses;
	    if (corrected_p > 1.0) {
		corrected_p = 1.0;
	    }

	    res.setpvalue_corr(corrected_p);
	}
    }

    /**

    Benjamini-Hochberg correction of pvalues. This puts values of 1 or
    0 int the corrected p, indicating whether the FDR has been met by
    a particular pvalue.

    */
    private void correct_pvals(double fdr) {
	int numclasses = sortedclasses.size();
	int n = numclasses;
	boolean threshpassed = false;

	Collections.reverse(sortedclasses); // start from the worst class.
	double corrected_p;
	for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) {
	    String nextclass = (String)it.next();
	    classresult res = (classresult)results.get(nextclass);
	    double actual_p = res.get_pvalue();

	    double thresh = fdr * n / numclasses;

	    if (actual_p < thresh || threshpassed) {
		res.setpvalue_corr(1.0);
		threshpassed = true;
	    } else {
		res.setpvalue_corr(0.0);
	    }

	    n--;
	}
	Collections.reverse(sortedclasses); // put it back.
    }



    /**
       Westfall-Young pvalue correction. Based on algorithm 2.8, pg 66
       of 'resampling-based multiple testing'.

       0. Sort the pvalues for the real data (assume worst pvalue is first)
       1. Make an array of count variables, one for each class, intialize to zero.
       loop: (n=10,000).
       2. Generate class pvalues for randomized values (see above);
       3. Iterate over this in the same order as the actual order.
       4. Define successive minima: (q is the trial; p is real, already ranked)
       a. qk = pk (class with worst pvalue)
       b. qk-1 = min (qk, pk-1)
       ...
       5. at each step a.... if qi <= pi, count_i++
       end loop.
       6. p_i* = count_i/n
       7. enforce monotonicity by using successive maximization.
       @param trials  How many random trials to do. According to W-Y, it should be >=10,000.
    */
    private void correct_pvals(int trials)
    {

	int[] counts = new int[sortedclasses.size()];
	for (int i=0; i< sortedclasses.size(); i++) {
	    counts[i] = 0;
	}

	Collections.reverse(sortedclasses); // start from the worst class.
	HashMap permscores;

	boolean verbose = false;

	for (int i=0; i < trials; i++) {
	    //	    System.err.println("Trial: " + i );

	    Map scgroup_pval_map = probe_pval.get_group_pval_map(true); // shuffle the association of pvalues to genes.

	    // shuffle. Stupidity: this is a different permutation
	    // than the group one. If we are using weights, it DOES
	    // NOT MATTER - it doesn't even have to be shuffled (it is
	    // used only to check for presence of a probe in the data
	    // set). If we are not using weights, it only affects the
	    // hypergeometric pvalues. (todo: add correction for those
	    // values) So we don't even bother shuffling it.
	    Map scprobepvalmap =  probe_pval.get_map();

	    // Just for AROC:
	    Map scinput_rank_map;
	    if (weight_on) {
		scinput_rank_map = Stats.rankOf(scgroup_pval_map);
	    } else {
		scinput_rank_map = Stats.rankOf(scprobepvalmap);
	    }

	    /// permscores contains a list of the p values for the shuffled data.
	    permscores = class_v_pval_generator(scgroup_pval_map, scprobepvalmap, scinput_rank_map); // end of step 1.

	    int j = 0;
	    double permp = 0.0;
	    Double m = new Double(1.0);
	    //	    Double m = (Double)permscores.get(j); // first sim value (for worst class in real data)
	    double q = m.doubleValue(); // pvalue for the previous permutation, initialized here.
	    double qprev = q;
	    double actual_p = 0.0;
	    String nextclass = "";

	    // successive minima of step 2, pg 66. Also does step 3.
	    for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) { // going in the correct order for the 'real' data, starting from the worst class.

		nextclass = (String)it.next();

		classresult res = (classresult)results.get(nextclass);
		actual_p = res.get_pvalue(); // pvalue for this class on real data.

		m = (Double)permscores.get(nextclass);
		permp = m.doubleValue(); // randomized pvalue for this class.

		q = Math.min(qprev, permp); // The best values for
					    // permp for this trial
					    // bubbles up. The way
					    // this works is that if
					    // two classes are highly
					    // correlated, their
					    // permuted pvalues will
					    // tend to be the
					    // same. Then, whatever
					    // decision is made here
					    // will tend to be the
					    // same decision made for
					    // the next (correlated
					    // class). That is how the
					    // resulting corrected p
					    // values for correlated
					    // classes are correlated.

		/* step 3 */
		if (q <= actual_p) { // for bad classes, this will often be true. Otherwise we see it less.
		    counts[j]++;
		}


		/* the following tests two classes which are very similar. Their permutation p values should be correlated */
		/*		if (nextclass.equals("GO:0006956")) {
		    System.err.print("\tGO:0006956\t" +   nf.format(permp) + "\n");
		}
		if (nextclass.equals("GO:0006958")) {
		    System.err.print("\tGO:0006958\t" + nf.format(permp));
		}
		*/


		if (verbose && j == sortedclasses.size() - 1) // monitor what happens to the best class.
		    System.err.println("Sim " + i + " class# " + j + " " + nextclass +  " size=" + res.get_effsize() + " q=" + nf.format(q) + " qprev=" + nf.format(qprev) + " pperm=" + nf.format(permp) + " actp=" + nf.format(actual_p) + " countj=" + counts[j] + " currentp=" + (double)counts[j]/(i+1));

		j++;
		qprev = q;
	    }

	    if (0 == i % 100) {
		System.err.println(i + " Westfall-Young trials, " + (trials - i) + " to go.");
	    }

	}

	Collections.reverse(sortedclasses); // now the best class is first.

	int j = sortedclasses.size() - 1; // index of the best class (last one tested above).
	double corrected_p = counts[sortedclasses.size() - 1]/trials; // pvalue for the best class.
	double previous_p = corrected_p;

	// Step 4 and enforce monotonicity, pg 67 (step 5)
	for (Iterator it = sortedclasses.iterator(); it.hasNext(); ) { // starting from the best class.
	    classresult res = (classresult)results.get((String)it.next());
	    corrected_p = Math.max((double)counts[j] / (double)trials, previous_p); // first iteration, these are the same.

	    if (verbose) // print the counts for each class.
		System.err.println(j + " " + counts[j] + " " + trials + " " + corrected_p );

	    res.setpvalue_corr(corrected_p);
	    previous_p = corrected_p;
	    j--;
	}
    }


    public static void main (String[] args) {
	class_pvals test = new class_pvals(args[0],args[1],args[2],args[3],args[4],args[5],args[6],
					   Integer.parseInt(args[7]),Integer.parseInt(args[8]),Integer.parseInt(args[9]),
					   Integer.parseInt(args[10]), Double.parseDouble(args[11]),args[12],
					   Integer.parseInt(args[13]), args[14], args[15]);
    }


} /* class_pvals */

