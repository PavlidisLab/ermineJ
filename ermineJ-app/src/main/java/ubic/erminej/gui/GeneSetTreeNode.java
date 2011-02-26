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
package ubic.erminej.gui;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import ubic.basecode.bio.GOEntry;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;

/**
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreeNode extends DefaultMutableTreeNode {
    /**
     * 
     */
    private static final long serialVersionUID = 7725460771978972950L;
    private boolean hasGoodChild = false;
    private boolean hasUsableChild = false;

    public GeneSetTreeNode( DirectedGraphNode<String, GOEntry> root ) {
        super( root );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<GeneSetTreeNode> breadthFirstEnumeration() {
        return super.breadthFirstEnumeration();
    }

    /**
     * @return Returns the hasGoodChild.
     */
    public boolean hasGoodChild() {
        return this.hasGoodChild;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirectedGraphNode<String, GOEntry> getUserObject() {
        return ( DirectedGraphNode<String, GOEntry> ) super.getUserObject();
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
