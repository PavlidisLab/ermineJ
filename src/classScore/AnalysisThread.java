package classScore;

import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 * @todo don't use stop. use interrupt instead, and stop the readers
 */

public class AnalysisThread {
   private volatile Thread athread;
   classScoreFrame csframe;
   Settings settings;
   classScoreStatus messenger;
   GONames goData;
   GeneAnnotations geneData;

   public AnalysisThread() { }

   public void startAnalysisThread(classScoreFrame csframe, Settings settings,
                                   classScoreStatus messenger,
                                   GONames goData, GeneAnnotations geneData )
       throws IllegalStateException{
      this.csframe = csframe;
      this.settings = settings;
      this.messenger = messenger;
      this.goData = goData;
      this.geneData = geneData; //this is the default geneData
      if ( athread != null )throw new IllegalStateException();
      athread = new Thread( new Runnable() {
         public void run() {
            doAnalysis();
         }
      } );
      athread.start();
   }

   private void doAnalysis() {
      try {
         expClassScore probePvalMapper = new expClassScore( settings );
         geneData = new GeneAnnotations(geneData, probePvalMapper.get_map()); //default replaced by new geneData
         GeneGroupReader groupName = new GeneGroupReader(
             geneData.getGeneToProbeList(), geneData.getProbeToGeneMap() ); // parse group file. Yields map of probe->replicates.
         probePvalMapper.setInputPvals( groupName.get_group_probe_map(),
                                        settings.getGroupMethod() ); // this initializes the group_pval_map, Calculates the ave/best pvalue for each group
         classPvalRun runResult = new classPvalRun( settings, geneData, goData,
             probePvalMapper, "", "bh", messenger, false );
         csframe.addResult( runResult );
         athread=null;
      }
      catch ( IOException ioe ) {
         //do something
      }
   }

   public void cancelAnalysisThread() {
      if ( athread != null )
      {
         athread.stop();
         //athread.interrupt();
         athread=null;
      }
   }

}
