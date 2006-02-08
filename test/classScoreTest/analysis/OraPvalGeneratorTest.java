/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package classScoreTest.analysis;

import classScore.analysis.OraPvalGenerator;
import classScore.data.GeneSetResult;

/**
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

        GeneSetResult r = test.classPval( "GO:2", gsr.getGeneToPvalMap(), gsr.getProbeToScoreMap() );

        if ( r == null ) {
            assertTrue( false );
        }

        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval1() {
        double expectedReturn = 0.0;

        GeneSetResult r = test.classPval( "GO:1", gsr.getGeneToPvalMap(), gsr.getProbeToScoreMap() );

        if ( r == null ) {
            assertTrue( false );
        }

        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval3() {
        double expectedReturn = 0.4642857;

        GeneSetResult r = test.classPval( "GO:3", gsr.getGeneToPvalMap(), gsr.getProbeToScoreMap() );

        if ( r == null ) {
            assertTrue( false );
        }

        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

}