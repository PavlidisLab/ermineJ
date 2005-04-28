package classScoreTest.analysis;

import classScore.analysis.OraPvalGenerator;
import classScore.data.GeneSetResult;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class OraPvalGeneratorTest extends AbstractPvalGeneratorTest {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {

        super.setUp();

        test = new OraPvalGenerator( s, g, csc, 2, 6, gon, 8 );

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        test = null;
    }

    public void testClassPval2() {
        double expectedReturn = 0.1071429;

        GeneSetResult r = test.classPval( "GO:2", gsr.getGeneToPvalMap(), gsr.getProbeToPvalMap() );

        if ( r == null ) {
            assertTrue( false );
        }

        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval1() {
        double expectedReturn = 0.0;

        GeneSetResult r = test.classPval( "GO:1", gsr.getGeneToPvalMap(), gsr.getProbeToPvalMap() );

        if ( r == null ) {
            assertTrue( false );
        }

        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval3() {
        double expectedReturn = 0.4642857;

        GeneSetResult r = test.classPval( "GO:3", gsr.getGeneToPvalMap(), gsr.getProbeToPvalMap() );

        if ( r == null ) {
            assertTrue( false );
        }

        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

}