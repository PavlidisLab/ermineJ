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

import ubic.basecode.bio.geneset.GONames;
import ubic.basecode.bio.geneset.GeneAnnotations;

/**
 * @author pavlidis
 * @version $Id$
 */
public class FindByGeneDialog extends FindDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 5573937842473360863L;

    /**
     * @param callingframe
     * @param geneData
     * @param goData
     */
    public FindByGeneDialog( GeneSetScoreFrame callingframe, GeneAnnotations geneData, GONames goData ) {
        super( callingframe, geneData, goData );
        this.setTitle( "Find Gene Set using a gene or probe symbol" );
    }

    @Override
    void findActionPerformed() {
        String searchOn = searchTextField.getText();
        statusMessenger.showStatus( "Searching '" + searchOn + "'" );

        if ( searchOn.equals( "" ) ) {
            geneData.resetSelectedSets();
        } else {
            geneData.selectSetsByGene( searchOn );
        }

        statusMessenger.showStatus( geneData.selectedSets() + " matching gene sets found." );
        resetViews();

    }

}
