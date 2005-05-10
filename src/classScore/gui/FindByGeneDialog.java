package classScore.gui;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;

/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004-2005 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public class FindByGeneDialog extends FindDialog {

    /**
     * @param callingframe
     * @param geneData
     * @param goData
     */
    public FindByGeneDialog( GeneSetScoreFrame callingframe, GeneAnnotations geneData, GONames goData ) {
        super( callingframe, geneData, goData );
        this.setTitle( "Find Gene Set using a gene or probe symbol" );
    }
    
    void findActionPerformed() {
        String searchOn = searchTextField.getText();
        statusMessenger.setStatus( "Searching '" + searchOn + "'" );

        if ( searchOn.equals( "" ) ) {
            geneData.resetSelectedSets();
        } else {
            geneData.selectSetsByGene( searchOn );
        }

        statusMessenger.setStatus( geneData.selectedSets() + " matching gene sets found." );

        callingframe.getOPanel().resetTable();

    }

}
