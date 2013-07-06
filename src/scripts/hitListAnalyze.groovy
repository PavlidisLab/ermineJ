#!/usr/bin/groovy
/*
 * Demo script showing how to run an analysis of hit lists, save results.
 */
package ubic.erminej.script
import ubic.erminej.*
import ubic.erminej.analysis.*
import ubic.erminej.data.*

/*
 * Set things up.
 */
config = new Settings(true)
config.useMolecularFunction = false
config.useBiologicalProcess = true
config.useCellularComponent = false
config.useUserDefined = true
config.classScoreMethod = "ORA"
config.geneScoreThreshold = 1.0  // this is a peculiarity of the hitlist style
config.doLog = true
config.bigIsBetter = false

goData = new GeneSetTerms( "go_daily-termdb.rdf-xml.gz", config)
parser = new GeneAnnotationParser( goData, null )
geneData = parser.read( "Generic_human.an.txt", "DEFAULT", config )

// Assume file contains a list of lists, one per line, with list name in first field.
// The list name is just ignored by the gene parser so I didn't bother removing it.
file = new File("myset.txt")
file.eachLine{ line ->
    f = line.tokenize('\t')
    name = f[0]
    println(name + " " + (f.size - 1) + " genes ...")

    try {

        /*
         * Create a GeneScores object from the list. We also save it to a file, so that the analysis can be loaded back in.
         */
        gs = new GeneScores(f, config, null, geneData, name + ".quicklist.txt")

        // do the analysis
        results = new GeneSetPvalRun(config, gs)

        ResultsPrinter.write(name + ".erminej.txt", results, false)
    } catch (e ) {
        println(name + " FAILED: " + e)
    }
}


