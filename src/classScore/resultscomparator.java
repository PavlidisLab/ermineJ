package classScore;

import java.util.Comparator;

import classScore.data.GeneSetResult;

class resultscomparator
    implements Comparator {
   public int compare(Object o1, Object o2) {
      GeneSetResult s1 = (GeneSetResult) o1;
      GeneSetResult s2 = (GeneSetResult) o2;
      return s1.compareTo(s2);
   }

   public boolean equals(Object o) {
      return compare(this, o) == 0;
   }
}
