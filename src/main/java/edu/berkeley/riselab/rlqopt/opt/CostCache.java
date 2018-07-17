package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import java.util.HashMap;
import java.util.Map;

/** A cache for cost model outputs; write-once, never-modified. */
public class CostCache {

  private static final boolean disableCaching = true;

  private Map<String, Double> costCache = new HashMap<>();
  private Map<String, Long> cardCache = new HashMap<>();

  // Cache hit statistics for debugging.  Only for IO estimate.
  private int numHits = 0;
  private int numTotal = 0;

  public void report() {
    System.out.println("num hits " + numHits + " total " + numTotal);
  }

  public double getOrComputeIOEstimate(Operator op, CostModel c, String plannerName) {
    if (disableCaching) {
      return (double) c.estimate(op, plannerName).operatorIOcost;
    }

    ++numTotal;
    String opHash = op.toString();

    double cost;
    if (!costCache.containsKey(opHash)) {
      cost = (double) c.estimate(op, plannerName).operatorIOcost;
      costCache.put(opHash, cost);
    } else {
      cost = costCache.get(opHash);
      ++numHits;
    }

    return cost;
  }

  public double getOrComputeCardinality(Operator op, CostModel c, String plannerName) {
    if (disableCaching) {
      return c.estimate(op, plannerName).resultCardinality;
    }

    String opHash = op.toString();

    Long cost = cardCache.get(opHash);

    if (cost == null) {
      cost = c.estimate(op, plannerName).resultCardinality;
      cardCache.put(opHash, cost);
    }
    return cost;
  }
}
