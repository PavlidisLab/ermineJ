package classScore.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import baseCode.dataStructure.graph.DirectedGraphNode;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreeNode extends DefaultMutableTreeNode {
    private boolean hasGoodChild = false;
    private boolean hasUsableChild = false;

    public GeneSetTreeNode( DirectedGraphNode root ) {
        super( root );
    }

    /**
     * @return Returns the hasGoodChild.
     */
    public boolean hasGoodChild() {
        return this.hasGoodChild;
    }

    /**
     * @param hasGoodChild The hasGoodChild to set.
     */
    public void setHasGoodChild( boolean hasGoodChild ) {
        this.hasGoodChild = hasGoodChild;
    }

    /**
     * @return Returns the hasUsableChild.
     */
    public boolean hasUsableChild() {
        return this.hasUsableChild;
    }

    /**
     * @param hasUsableChild The hasUsableChild to set.
     */
    public void setHasUsableChild( boolean hasUsableChild ) {
        this.hasUsableChild = hasUsableChild;
    }

}
