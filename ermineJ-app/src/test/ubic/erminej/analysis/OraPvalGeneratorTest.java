/*
 * The ermineJ project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.erminej.analysis;

import ubic.erminej.analysis.OraPvalGenerator;
import ubic.erminej.data.GeneSetResult;

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
        test.setGlobalMissingAspectTreatedAsUsable( true );

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


        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval1() {
        double expectedReturn = 0.0;

        GeneSetResult r = test.classPval( "GO:1", gsr.getGeneToPvalMap(), gsr.getProbeToScoreMap() );


        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval3() {
        double expectedReturn = 0.4642857;

        GeneSetResult r = test.classPval( "GO:3", gsr.getGeneToPvalMap(), gsr.getProbeToScoreMap() );


        double actualReturn = r.getPvalue();

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

}