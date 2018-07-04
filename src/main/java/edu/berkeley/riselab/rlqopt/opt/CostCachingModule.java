package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import java.util.HashMap;
import java.util.Map;

/** A mix-in class that caches IO cost estimates. Cache is write-once, never-modified. */
public interface CostCachingModule {

  Map<String, Double> costCache = new HashMap<>();

  default double getOrComputeIOEstimate(Operator op, CostModel c) {
    if (op == null) {
      return c.estimate(null).operatorIOcost;
    }
    String opHash = op.toString();
    
    Double cost = null; //costCache.get(opHash); (disable)

    if (cost == null) {
      cost = (double) c.estimate(op).operatorIOcost;
      costCache.put(opHash, cost);
    }
    return cost;
  }
}
