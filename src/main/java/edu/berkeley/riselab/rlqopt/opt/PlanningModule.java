package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.cost.CostModel;

public abstract class PlanningModule {

  /** Propagated by the encapsulating Planner. */
  public String name = null;

  // takes an operator returns an equivalent operator
  public abstract Operator apply(Operator in, CostModel c);
}
