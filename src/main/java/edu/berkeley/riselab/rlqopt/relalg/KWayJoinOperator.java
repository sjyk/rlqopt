package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.DummyOperator;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;

// implements a project operator
public class KWayJoinOperator extends DummyOperator {

  public KWayJoinOperator(OperatorParameters params, Operator... source) throws OperatorException {
    super(params, source);
  }

  // override
  public boolean isValid(OperatorParameters params, Operator... source) {

    return true;
  }

}
