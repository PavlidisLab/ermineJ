package util;
import java.io.*; 
import java.util.*;
import java.math.*;
import java.lang.*;
//import java.lang.reflect.*;
//import java.security.*;

/**

  Calculation of various functions and statistical distributions.
  @author Paul Pavlidis
  @version $Id$
   
 */
public class SpecFunc {

    public SpecFunc() {}


    /** 
	@param xx Value we want the ln(gamma(x)) function evaluted for.
	@return ln(gamma(xx))
	
     */
    public static double gammaln(double xx) {
	double x, y, tmp, ser;
	double[] cof = {76.18009172947146, -86.50532032941677, 24.0140982408391, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5};
	int j;
	y=x=xx;
	tmp = x + 5.5;
	tmp -= (x + 0.5)*Math.log(tmp);
	ser = 1.000000000190015;
	for (j=0; j<=5; j++)
	    ser += cof[j]/++y;

	return -tmp+Math.log(2.5066282746310005*ser/x);
    }
  

    /**
     * Used by betai: Evaluates continued fraction for incomplete beta
     * function by modified Lentz's method.
     *
     * From Numerical Recipes in C.
     */
    public static  double betacf  (double a, double b, double x)
    {
	int MAXIT = 200;
	double EPS = 3.0e-7;
	double FPMIN = 1.0e-30;
	
	int m, m2;
	double aa, c, d, del, h, qab, qam, qap;
	
	// These q's will be used in factors that occur in the coefficients.
	qab = a + b;
	qap = a + 1.0;
	qam = a - 1.0;
	
	// First step of Lentz's method.
	c = 1.0;
	d = 1.0 - (qab * (x / qap));
	
	if (Math.abs(d) < FPMIN) d = FPMIN;
	d = 1.0 / d;
	h = d;
	
	for (m = 1; m <= MAXIT; m++) {
	    m2 = 2*m;
	    aa= m * (b - m) * x / ((qam + m2) * (a + m2));
	    
	    // One step (the even one) of the recurrence.
	    d = 1.0 + aa * d;
	    if (Math.abs(d) < FPMIN) d = FPMIN;
	    c = 1.0 + aa / c;
	    if (Math.abs(c) < FPMIN) c = FPMIN;
	    d = 1.0 / d;
	    h *= d * c;
	    aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
	    
	    // Next step of the recurrence (the odd one).
	    d = 1.0 + aa * d;
	    if (Math.abs(d) < FPMIN) d = FPMIN;
	    c = 1.0 + aa / c;
	    if (Math.abs(c) < FPMIN) c = FPMIN;
	    d = 1.0 / d;
	    del = d * c;
	    h *= del;
	    
	    // Are we done?
	    if (Math.abs(del - 1.0) < EPS) break;
	}
	if (m > MAXIT)
	    System.err.println("a (" + a + ") or b (" + b + ") too big, or MAXIT (" + MAXIT + ") too small in betacf");

	return(h);
    }
    
  
    /**
       Returns the incomplete beta function I_X(a,b).
       
       From Numerical Recipes in C.
    */
    public static  double betai	(double a, double b, double x)
    {
	double bt;
	
	if (x < 0.0 || x > 1.0) {
	    System.err.println("Bad x (" + x + ") in routine betai.");
	    System.exit(1);
	}
	
	if (x == 0.0 || x == 1.0) {
	    bt = 0.0;
	} else {
	    bt = Math.exp(gammaln(a + b) 
		     - gammaln(a)
		     - gammaln(b) 
		     + (a * Math.log(x))
		     + (b * Math.log(1.0 - x)));
	}
	
	if (x < (a + 1.0)/(a + b + 2.0)) {
	    return(bt * betacf(a, b, x) / a);
	} // else
	return(1.0 - (bt * betacf(b, a, 1.0 - x) / b));
    }
    
    
    /**
       ln(n!)
    */
    private static  double lnfact
	(int n) 
    {
	if (n<0) {
	    System.err.println("Attempt to measure lnfact of a negative value: " + n);
	    System.exit(1);
	}
	if (n<=1)
	    return 0.0;
	else
	    return gammaln(n+1.0);
    }
    
    
    /**
       Binomial coefficient
    */
    public static double binomial_coeff	(int n, int k) 
    {
	return Math.floor(0.5+Math.exp(lnfact(n)-lnfact(k)-lnfact(n-k)));
    }


    /**
      Binomial distribution. This function gives the same results as R's dbinom.
    */
    public static double binomial_prob (int successes, int trials, double p)
    {
	return binomial_coeff(trials, successes) * Math.pow(p, successes)*Math.pow(1.0 - p, trials - successes);
    }


     /**
      Cumulative Binomial distribution for n up to k., upper
      tail. This function gives the same results as R's pbinom, upper tail.
    */
    public static double binomialCumProb (int successes, int trials, double p)
    {
	int i;
	double pval = 0.0;

	for (i = successes + 1; i <= trials; i++) {
	    pval += binomial_prob(i, trials, p);
	}
	return pval;
    }

    
    /** 
	The tail of the normal distribution.
	@param z
	@return Area under one tail of the normal distribution.
    */
    public static double normdist (double z) {
	if (z < 0.0) 
	    z = -z;

	if (z == 0.0)
	    return 0.5;
	
	return  1.0 - 0.5*erfc(-z/Math.sqrt(2.0));
    }

    /**
       Cumulative error function.
     */
    public static double erfc(double a)
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
    
    
    /**
       Error function
    */
    public static double erf(double x)
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


}
