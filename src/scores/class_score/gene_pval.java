package scores.class_score;

import scores.class_score.*;
import java.util.*;


public class gene_pval implements Comparable{
  private String gene_id;
  private double pval;
  
  public gene_pval(String id, double pv){
    gene_id = id;
    pval = pv;    	
  }
  
  public int compareTo(Object ob){
    gene_pval other = (gene_pval)ob;
    
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