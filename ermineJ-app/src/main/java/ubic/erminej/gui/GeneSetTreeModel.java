/*
 * The ermineJ project
 * 
 * Copyright (c) 2010 University of British Columbia
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

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * TODO Document Me
 * 
 * @author paul
 * @version $Id$
 */
public class GeneSetTreeModel extends AbstractTreeModel {

    protected TreeNode root;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    @Override
    public Object getChild( Object parent, int index ) {
        return ( ( TreeNode ) parent ).getChildAt( index );
    }

    public GeneSetTreeModel( TreeNode root ) {
        super();
        this.root = root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    @Override
    public int getChildCount( Object parent ) {
        return ( ( TreeNode ) parent ).getChildCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    @Override
    public Object getRoot() {
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf( Object node ) {
        return ( ( TreeNode ) node ).isLeaf();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    @Override
    public void valueForPathChanged( TreePath path, Object newValue ) {
        // MutableTreeNode aNode = ( MutableTreeNode ) path.getLastPathComponent();
        //
        // aNode.setUserObject( newValue );
        // nodeChanged( aNode );
        return;
    }

}
