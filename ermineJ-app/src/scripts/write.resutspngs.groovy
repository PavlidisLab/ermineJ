#!/usr/bin/groovy
/*
 * Demo script showing how to load an analysis and save pngs of the top 10 results.
 */
package ubic.erminej.script
import ubic.basecode.graphics.ColorMap;
import ubic.erminej.*
import ubic.erminej.analysis.*
import ubic.erminej.data.*
import ubic.erminej.gui.util.GeneSetDetailsImageWriter;

import ubic.erminej.util.*

loadFile = "myresults.erminej.txt"
settings = new Settings( loadFile );
goData = new GeneSetTerms( settings.classFile )
parser = new GeneAnnotationParser( goData, null )
geneData = parser.read( settings.annotFile, settings.annotFormat, settings )

gs = new GeneScores(settings.scoreFile, settings, null, geneData)

athread = new Analyzer( settings, null, geneData, loadFile );
results = athread.loadAnalysis();

result = results[0]

rlist = result.getResults().values()
rlist.sort()

rlist.eachWithIndex { it, i ->
    if (i > 10) {
        return
    }

    details = new GeneSetDetails(it.geneSetTerm, it, result.geneData, settings, gs, null)
    GeneSetDetailsImageWriter.writePng( details, result.name + "." + (1+i) + ".png", ColorMap.BLACKBODY_COLORMAP, true, false, true )
}


