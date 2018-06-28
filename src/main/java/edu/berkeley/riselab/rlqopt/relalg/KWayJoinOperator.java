package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.DummyOperator;
import edu.berkeley.riselab.rlqopt.Expression;
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

  public String toSQLString() {
    String className = this.getClass().getSimpleName();

    String prefix = "SELECT * FROM ";

    for (Operator o : source) {
      prefix += o.toSQLString() + " , ";
    }

    prefix = prefix.substring(0, prefix.length() - 3);

    prefix += " WHERE ";

    for (Expression e : params.expression) prefix += e.toSQLString() + " AND ";

    prefix = prefix.substring(0, prefix.length() - 4);

    return "(" + prefix + ") as " + className + hashCode();
  }
}
