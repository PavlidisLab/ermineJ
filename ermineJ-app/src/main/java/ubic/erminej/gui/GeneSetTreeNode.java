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

import javax.swing.tree.DefaultMutableTreeNode;

import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.erminej.data.GeneSetTerm;

/**
 * @author pavlidis
 * @version $Id$
 */
public class GeneSetTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    private boolean hasSignificantChild = false;

    public GeneSetTreeNode( DirectedGraphNode<String, GeneSetTerm> root ) {
        super( root );
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public GeneSetTerm getTerm() {
        return ( ( DirectedGraphNode<String, GeneSetTerm> ) super.getUserObject() ).getItem();
    }

    /**
     * @return true if one ore more children of this node should be displayed as 'significant'.
     */
    public boolean hasSignificantChild() {
        return this.hasSignificantChild;
    }

    public void setHasSignificantChild( boolean b ) {
        this.hasSignificantChild = b;
    }

    @Override
    public String toString() {
        return this.getTerm().toString();
    }

}
