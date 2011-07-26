/*
 * The ermineJ project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.erminej.gui.geneset.tree;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import ubic.erminej.analysis.GeneSetPvalRun;
import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetResult;
import ubic.erminej.data.GeneSetTerm;
import ubic.erminej.gui.geneset.GeneSetPanel;

/**
 * Deals with showing filtered views of the gene set tree.
 * 
 * @author paul
 * @version $Id$
 */
public class FilteredGeneSetTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;

    boolean filterBySize = true;
    boolean filterBySignificance = false;
    boolean filterByRedundancy = false;

    public boolean isFilterByRedundancy() {
        return filterByRedundancy;
    }

    public void setFilterByRedundancy( boolean filterByRedundancy ) {
        this.filterByRedundancy = filterByRedundancy;
    }

    private GeneSetPvalRun results;

    public boolean isFilterBySize() {
        return filterBySize;
    }

    public void setFilterBySize( boolean filterBySize ) {
        this.filterBySize = filterBySize;
    }

    public boolean isFilterBySignificance() {
        return filterBySignificance;
    }

    public void setFilterBySignificance( boolean filterBySignificance ) {
        this.filterBySignificance = filterBySignificance;
    }

    public void setResults( GeneSetPvalRun results ) {
        this.results = results;
    }

    /*
     * (non-Javadoc)
     * 
     * Filtering criteria implemented here.
     * 
     * @see javax.swing.tree.DefaultTreeModel#getChild(java.lang.Object, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getChild( Object parent, int index ) {
        Enumeration children = ( ( GeneSetTreeNode ) parent ).children();

        int i = 0;
        while ( children.hasMoreElements() ) {
            GeneSetTreeNode node = ( GeneSetTreeNode ) children.nextElement();
            GeneSetTerm term = node.getTerm();

            if ( filterBySignificance && this.results != null ) {
                GeneSetResult geneSetResult = results.getResults().get( term );
                //
                if ( geneSetResult != null ) {
                    if ( geneSetResult.getCorrectedPvalue() >= GeneSetPanel.FDR_THRESHOLD_FOR_FILTER ) {
                        continue;
                    }
                } else if ( !node.hasSignificantChild() ) {
                    continue;
                }

            }
            if ( filterBySize && !term.isAspect() && annots.getGeneSetGenes( term ).size() == 0 ) {
                continue;
            } else if ( filterByRedundancy && annots.skipDueToRedundancy( term ) ) {
                continue;
            }

            if ( i == index ) {
                return node;
            }
            i++;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getChildCount( Object parent ) {
        Enumeration children = ( ( GeneSetTreeNode ) parent ).children();

        int i = 0;
        while ( children.hasMoreElements() ) {
            GeneSetTreeNode node = ( GeneSetTreeNode ) children.nextElement();
            GeneSetTerm term = node.getTerm();

            if ( filterBySignificance && this.results != null ) {
                GeneSetResult geneSetResult = results.getResults().get( term );

                if ( geneSetResult != null ) {
                    if ( geneSetResult.getCorrectedPvalue() >= GeneSetPanel.FDR_THRESHOLD_FOR_FILTER ) {
                        continue;
                    }
                } else if ( !node.hasSignificantChild() ) {
                    continue;
                }

            }
            if ( filterBySize && !term.isAspect() && annots.getGeneSetGenes( term ).size() == 0 ) {
                continue;
            }
            if ( filterByRedundancy && annots.skipDueToRedundancy( term ) ) {
                continue;
            }
            i++;
        }
        return i;
    }

    private GeneAnnotations annots;

    public FilteredGeneSetTreeModel( GeneAnnotations annots, TreeModel toWrap ) {
        super( ( TreeNode ) toWrap.getRoot() );
        this.annots = annots;
    }

}
