package classScore;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.table.*;
import util.*;

/**
  Makes the initial maps based on the input files. Created :05/27/04
  @author Shahmil Merchant; Paul Pavlidis (major changes)
  @version $Id$
 */
public class setupMaps {
   public GONameReader goName;
   public expClassScore probePvalMapper;
   public GeneDataReader geneData = null;
   public Map probeGroups;
   public ClassMap probeToClassMap;
   public Map classToProbe;
   private GeneGroupReader groupName;
   private boolean weight_on = true;
   private boolean dolog = true;

   /**
    */
   public setupMaps(String probePvalFile,
                       String probe_annotfile,
                       String goNamesfile,
                       String method,
                       String groupMethod,
                       int classMaxSize,
                       int classMinSize,
                       int numberOfRuns,
                       int quantile,
                       String useWeights,
                       int pvalcolumn,
                       String dolog_check,
                       classScoreStatus messenger)
          throws IllegalArgumentException, IOException{

      // user flags and constants:
      dolog = (Boolean.valueOf(dolog_check)).booleanValue();

      messenger.setStatus("Reading GO descriptions");
      goName = new GONameReader(goNamesfile); // parse go name file

      messenger.setStatus("Reading gene scores");
      probePvalMapper = new expClassScore(probePvalFile, useWeights, method,
                                          pvalcolumn, dolog, classMaxSize,
                                          classMinSize, numberOfRuns,
                                          quantile);

      messenger.setStatus("Reading gene data file");
      geneData = new GeneDataReader(probe_annotfile, probePvalMapper.get_map());
      groupName = new GeneGroupReader(geneData.getGroupProbeList(),
                                      geneData.getProbeGroupMap()); // parse group file. Yields map of probe->replicates.
      probeGroups = groupName.get_probe_group_map(); // map of probes to groups

      messenger.setStatus("Initializing gene score mapping");

      //  if (weight_on) {
      probePvalMapper.setInputPvals(groupName.get_group_probe_map(),
                                    groupMethod); // this initializes the group_pval_map, Calculates the ave/best pvalue for each group
      //  }

      messenger.setStatus("Initializing gene class mapping");

      //   messenger.setStatus("Reading in GO class membership");
      probeToClassMap = new ClassMap(geneData.getProbeToClassMap(),
                                     geneData.getClassToProbeMap()); // parses affy->classes file. Yields map of go->probes
      classToProbe = probeToClassMap.getClassToProbeMap(); // this is the map of go->probes
      messenger.setStatus("Done with setup");
   }
}
