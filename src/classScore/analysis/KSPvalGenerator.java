package classScore.analysis;

import baseCode.bio.geneset.GONames;
import baseCode.bio.geneset.GeneAnnotations;
import classScore.Settings;

/**
 * Use the Kolmogorov-Smirnov test to evaluate the ranking of the probes.
 * <hr>
 * <p>
 * Copyright (c) 2006 University of British Columbia
 * 
 * @author pavlidis
 * @version $Id$
 */
public class KSPvalGenerator extends AbstractGeneSetPvalGenerator {

    public KSPvalGenerator( Settings set, GeneAnnotations an, GeneSetSizeComputer csc, GONames gon ) {
        super( set, an, csc, gon );
    }
}
