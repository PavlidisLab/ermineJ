package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:stores constant variables 

                                                                                                                                                            
*******************************************************************************/
import scores.class_score.*;
import java.util.*;


/*****************************************************************************************/
public interface ConstantStuff
/*****************************************************************************************/
{
    public static final long IA = 16807;
    public static final long IM = 2147483647;
    public static final double AM = (1.0/IM);
    public static final long IQ = 127773;
    public static final long IR = 2836;
    public static final int NTAB = 32;
    public static final double NDIV = (1+(IM-1)/NTAB);
    public static final double EPS = 1.2e-7;
    public static final double RNMX = (1.0 - EPS);
    public static final double LNTEN = Math.log(10);

}
