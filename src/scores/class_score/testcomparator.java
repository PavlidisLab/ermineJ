package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Created :09/02/02
  Revision History: $Id$
  Description:implements comparator used in Stats                                                                                                                                     
*******************************************************************************/



import scores.class_score.*;
import java.util.*;


/*****************************************************************************************/
public class testcomparator implements Comparator { 
    /*****************************************************************************************/
  
    
    /*****************************************************************************************/
    /* calculate a particular quantile of an array. Quantile must be a value from 0 to 100. */
    public int compare(Object elem1, Object elem2) {
	/*****************************************************************************************/
	double num1 = Double.parseDouble(elem1.toString());
	double num2 = Double.parseDouble(elem2.toString());
	if (num1 < num2) {
	    return(-1);
	} else if (num1 > num2) {
	    return(1);
	}
	return(0);
    }
}
