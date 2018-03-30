package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Operator;

// this implements one transformation
// of the plan todo priority
public interface InitRewrite {

  // takes an operator returns an equivalent operator
  public Operator apply(Operator in);
}
