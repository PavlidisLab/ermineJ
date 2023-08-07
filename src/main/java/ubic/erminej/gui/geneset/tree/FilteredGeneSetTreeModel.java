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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

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

    private boolean filterBySize = true;

    private boolean filterBySignificance = false;

    private GeneSetPvalRun results;

    private GeneAnnotations annots;

    private Collection<GeneSetTerm> selectedTerms = new HashSet<>();

    /**
     * <p>Constructor for FilteredGeneSetTreeModel.</p>
     *
     * @param annots a {@link ubic.erminej.data.GeneAnnotations} object.
     * @param toWrap a {@link javax.swing.tree.TreeModel} object.
     */
    public FilteredGeneSetTreeModel( GeneAnnotations annots, TreeModel toWrap ) {
        super( ( TreeNode ) toWrap.getRoot() );
        this.annots = annots;
    }

    /*
     * (non-Javadoc)
     *
     * Filtering criteria implemented here.
     *
     * @see javax.swing.tree.DefaultTreeModel#getChild(java.lang.Object, int)
     */
    /** {@inheritDoc} */
    @Override
    public Object getChild( Object parent, int index ) {
        Enumeration<TreeNode> children = ( ( GeneSetTreeNode ) parent ).children();

        int i = 0;
        while ( children.hasMoreElements() ) {
            GeneSetTreeNode node = ( GeneSetTreeNode ) children.nextElement();
            GeneSetTerm term = node.getTerm();

            if ( filterBySize && !term.isAspect() && annots.getGeneSetGenes( term ).size() == 0 ) {
                continue;
            }

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

            if ( !this.selectedTerms.isEmpty() && !term.isAspect() && !selectedTerms.contains( term )
                    && !node.hasSelectedChild() ) {
                continue;
            }

            if ( i == index ) {
                return node;
            }
            i++;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount( Object parent ) {
        Enumeration<TreeNode> children = ( ( GeneSetTreeNode ) parent ).children();

        // System.err.println( this.selectedTerms.size() );

        int i = 0;
        while ( children.hasMoreElements() ) {
            GeneSetTreeNode node = ( GeneSetTreeNode ) children.nextElement();
            GeneSetTerm term = node.getTerm();

            if ( filterBySize && !term.isAspect() && annots.getGeneSetGenes( term ).size() == 0 ) {
                continue;
            }

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

            if ( !this.selectedTerms.isEmpty() && !term.isAspect() && !selectedTerms.contains( term )
                    && !node.hasSelectedChild() ) {
                continue;
            }

            i++;
        }
        return i;
    }

    /**
     * <p>isFilterBySignificance.</p>
     *
     * @return a boolean.
     */
    public boolean isFilterBySignificance() {
        return filterBySignificance;
    }

    /**
     * <p>isFilterBySize.</p>
     *
     * @return a boolean.
     */
    public boolean isFilterBySize() {
        return filterBySize;
    }

    /**
     * <p>removeResults.</p>
     */
    public void removeResults() {
        this.results = null;
    }

    /**
     * <p>Setter for the field <code>filterBySignificance</code>.</p>
     *
     * @param filterBySignificance a boolean.
     */
    public void setFilterBySignificance( boolean filterBySignificance ) {
        this.filterBySignificance = filterBySignificance;
    }

    /**
     * <p>Setter for the field <code>filterBySize</code>.</p>
     *
     * @param filterBySize a boolean.
     */
    public void setFilterBySize( boolean filterBySize ) {
        this.filterBySize = filterBySize;
    }

    /**
     * <p>setFilterSelectedTerms.</p>
     *
     * @param selectedTerms a {@link java.util.Collection} object.
     */
    public void setFilterSelectedTerms( Collection<GeneSetTerm> selectedTerms ) {
        // System.err.println( selectedTerms.size() + " terms selected" );
        this.selectedTerms = selectedTerms;
    }

    /**
     * <p>Setter for the field <code>results</code>.</p>
     *
     * @param results a {@link ubic.erminej.analysis.GeneSetPvalRun} object.
     */
    public void setResults( GeneSetPvalRun results ) {
        this.results = results;
    }

}
