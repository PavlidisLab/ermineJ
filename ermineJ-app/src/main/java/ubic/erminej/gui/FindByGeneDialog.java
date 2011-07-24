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

import java.util.Collection;

import ubic.erminej.data.GeneAnnotations;
import ubic.erminej.data.GeneSetTerm;

/**
 * @author pavlidis
 * @version $Id$
 */
public class FindByGeneDialog extends FindDialog {

    private static final long serialVersionUID = 5573937842473360863L;

    /**
     * @param callingframe
     * @param geneData
     * @param goData
     */
    public FindByGeneDialog( GeneSetScoreFrame callingframe, GeneAnnotations geneData ) {
        super( callingframe, geneData );
        this.setTitle( "Find Gene Set using a gene or probe symbol" );
    }

    @Override
    public void findActionPerformed() {
        String searchOn = searchTextField.getText();
        statusMessenger.showStatus( "Searching '" + searchOn + "'" );
        Collection<GeneSetTerm> geneSets;
        if ( searchOn.equals( "" ) ) {
            geneSets = geneData.getAllTerms();
        } else {
            geneSets = geneData.findSetsByGene( searchOn );
        }

        statusMessenger.showStatus( geneSets.size() + " matching gene sets found." );
        filterViews( geneSets );

    }

}
