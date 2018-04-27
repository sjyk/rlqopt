package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Operator;

public interface CostModel {

  public Cost estimate(Operator in);
}
