package scores.class_score;
import java.util.*;

/** 
    Simple data structure used by Stats.rankOf().
    @author Edward Chen
    $Id$
 */
public class geneNpval implements Comparable{
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
