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
package ubic.erminej.gui.geneset.tree;

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.erminej.data.GeneSetTerm;

/**
 * <p>
 * GeneSetTreeNode class.
 * </p>
 *
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    private static Comparator<GeneSetTreeNode> comparator = new Comparator<GeneSetTreeNode>() {

        @Override
        public int compare( GeneSetTreeNode o1, GeneSetTreeNode o2 ) {
            return o1.getTerm().getName().compareTo( o2.getTerm().getName() );
        }
    };

    private boolean hasSignificantChild = false;

    private boolean hasSelectedChild = false;

    /**
     * <p>
     * Constructor for GeneSetTreeNode.
     * </p>
     *
     * @param root a {@link ubic.basecode.dataStructure.graph.DirectedGraphNode} object.
     */
    public GeneSetTreeNode( DirectedGraphNode<String, GeneSetTerm> root ) {
        super( root );
    }

    /**
     * <p>
     * getTerm.
     * </p>
     *
     * @return the GeneSetTerm associated with this
     */
    @SuppressWarnings("unchecked")
    public GeneSetTerm getTerm() {
        return ( ( DirectedGraphNode<String, GeneSetTerm> ) super.getUserObject() ).getItem();
    }

    /**
     * If it's included in a filtered set.
     *
     * @return true if one or more of the children of this node should be displayed in a selection.
     */
    public boolean hasSelectedChild() {
        return hasSelectedChild;
    }

    /**
     * <p>
     * hasSignificantChild.
     * </p>
     *
     * @return true if one ore more children of this node should be displayed as 'significant'.
     */
    public boolean hasSignificantChild() {
        return this.hasSignificantChild;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void insert( MutableTreeNode newChild, int childIndex ) {
        super.insert( newChild, childIndex );
        Collections.sort( this.children, comparator );
    }

    /**
     * <p>
     * Setter for the field <code>hasSelectedChild</code>.
     * </p>
     *
     * @param b a boolean.
     */
    public void setHasSelectedChild( boolean b ) {
        this.hasSelectedChild = b;
    }

    /**
     * <p>
     * Setter for the field <code>hasSignificantChild</code>.
     * </p>
     *
     * @param b a boolean.
     */
    public void setHasSignificantChild( boolean b ) {
        this.hasSignificantChild = b;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getTerm().toString();
    }
}
