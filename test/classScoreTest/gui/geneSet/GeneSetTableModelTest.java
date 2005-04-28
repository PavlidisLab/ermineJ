package classScoreTest.gui.geneSet;

import classScore.gui.geneSet.GeneSetTableModel;
import junit.framework.TestCase;

/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004-2005 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTableModelTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // exercise the configuration.
    public void testGeneSetTableModel() {
        GeneSetTableModel foo = new GeneSetTableModel(null, null, null, null, null, null, null);
    }

}
