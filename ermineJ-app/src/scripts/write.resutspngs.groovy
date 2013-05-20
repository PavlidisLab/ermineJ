#!/usr/bin/groovy
/*
 * Demo script showing how to load an analysis and save pngs of the top 10 results.
 */
package ubic.erminej.script
import ubic.basecode.graphics.ColorMap;
import ubic.erminej.*
import ubic.erminej.analysis.*
import ubic.erminej.data.*
import ubic.erminej.util.*

loadFile = args[0]
assert loadFile != null

settings = new Settings( loadFile );
assert settings != null

assert settings.classFile != null

goData = new GeneSetTerms( settings.classFile + "", settings )
parser = new GeneAnnotationParser( goData, null )
geneData = parser.read( settings.annotFile, settings.annotFormat, settings )

gs = new GeneScores(settings.scoreFile, settings, null, geneData)

athread = new Analyzer( settings, null, geneData, loadFile );
results = athread.loadAnalysis();

assert results != null && results.size > 0, "No results were loaded from " + loadFile

result = results.asList()[0]

rlist = result.getResults().values()
rlist.sort()

rlist.eachWithIndex {  it, i ->
    if (i > 10) {
        return
    }

    details = new GeneSetDetails(it.geneSetTerm, it, result.geneData, settings, gs, null)
    GeneSetDetailsImageWriter.writePng( details, result.name + "." + (1+i) + ".png", ColorMap.BLACKBODY_COLORMAP, true, false, true )
}

