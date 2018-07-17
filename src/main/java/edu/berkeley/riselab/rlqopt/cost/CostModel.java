package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Operator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public interface CostModel {

  Cost estimate(Operator in);

  Map<String, Long> numEvaluatedPlans = new HashMap<>();

  /** Iff plannerName is non-null, increment #estimateCalled by 1 for that planner. */
  default Cost estimate(Operator in, String plannerName) {
    if (plannerName != null) {
      Long count = numEvaluatedPlans.get(plannerName);
      if (count != null) {
        ++count;
      } else {
        count = 1L;
      }
      numEvaluatedPlans.put(plannerName, count);
    }
    return estimate(in);
  }

  default void reportNumEvaluations() {
    System.out.print("# Plans evaluated by cost model: ");
    Map<String, Long> treeMap = new TreeMap<>();
    for (Map.Entry<String, Long> entry : numEvaluatedPlans.entrySet()) {
      treeMap.put(entry.getKey(), entry.getValue());
    }
    System.out.println(treeMap);
  }
}
