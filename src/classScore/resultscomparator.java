package classScore;

import java.util.*;
import classScore.data.*;

class resultscomparator
    implements Comparator {
   public int compare(Object o1, Object o2) {
      classresult s1 = (classresult) o1;
      classresult s2 = (classresult) o2;
      return s1.compareTo(s2);
   }

   public boolean equals(Object o) {
      return compare(this, o) == 0;
   }
}
