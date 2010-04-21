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
    @Override
    protected void setUp() throws Exception {

        super.setUp();

        test = new OraPvalGenerator( s, annotations, sizeComputer, 2, 18, gon, 20 );
        test.setGlobalMissingAspectTreatedAsUsable( true );

    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        test = null;
    }

    public void testClassPval2() {
        // phyper(lower.tail=F, 1,4,16,2) + dhyper(1, 4, 16, 2);

        double expectedReturn = 0.36842;

        GeneSetResult r = test.classPval( "GO:2", scores.getGeneToPvalMap(), scores.getProbeToScoreMap() );
        assertNotNull( r );
        double actualReturn = r.getPvalue();

        assertEquals( expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval1() {
        double expectedReturn = 0.0;

        GeneSetResult r = test.classPval( "GO:1", scores.getGeneToPvalMap(), scores.getProbeToScoreMap() );

        assertNotNull( r );
        double actualReturn = r.getPvalue();

        assertEquals( expectedReturn, actualReturn, 0.0001 );
    }

    public void testClassPval3() {
        double expectedReturn = 0.7631579;

        GeneSetResult r = test.classPval( "GO:3", scores.getGeneToPvalMap(), scores.getProbeToScoreMap() );
        assertNotNull( r );
        double actualReturn = r.getPvalue();

        // phyper(lower.tail=F, 1,10,10,2) + dhyper(1, 10,10, 2)

        assertEquals( expectedReturn, actualReturn, 0.0001 );
    }

}