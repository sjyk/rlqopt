package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;

// implements a project operator
public class IndexJoinOperator extends JoinOperator {

  public IndexJoinOperator(OperatorParameters params, Operator... source) throws OperatorException {
    super(params, source);
  }

  // override
  public boolean isValid(OperatorParameters params, Operator... source) {

    if (source.length != 2 || getJoinType(params,source) != JoinOperator.KN || source[0].getVisibleRelations().size() != 1) return false;

    return true;
  }

  public boolean isValid() {

    return isValid(this.params, this.source.toArray(new Operator[this.source.size()]));
  }
}
