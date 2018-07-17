package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;

// implements a project operator
public class HashJoinOperator extends JoinOperator {

  public HashJoinOperator(OperatorParameters params, Operator... source) throws OperatorException {
    super(params, source);
  }
}
