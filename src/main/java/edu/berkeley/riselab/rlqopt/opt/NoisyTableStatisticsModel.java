package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.Random;

public class NoisyTableStatisticsModel extends TableStatisticsModel implements CostModel {

  double alpha;
  public NoisyTableStatisticsModel(double alpha, TableStatisticsModel t) {
    super();
    for (Attribute a : this.keySet()) super.put(a, this.get(a));

    this.alpha = alpha;
  }

  public double estimateReductionFactor(Expression e) {

    Random rand = new Random(e.toString().hashCode());

    return super.estimateReductionFactor(e) + alpha * (rand.nextDouble() - 0.5);
  }

  public double estimateJoinReductionFactor(Expression e) {

    Random rand = new Random(e.toString().hashCode());

    return super.estimateJoinReductionFactor(e) + alpha * (rand.nextDouble() - 0.5);
  }
}
