package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;

// implements a select operator
public class SelectOperator extends Operator {

  public SelectOperator(OperatorParameters params, Operator... source) throws OperatorException {
    super(params, source);
  }

  // override
  public boolean isValid(OperatorParameters params, Operator... source) {

    if (source.length != 1) return false;

    return true;
  }

  public String toSQLString() {
    String className = this.getClass().getSimpleName();

    String prefix = "SELECT * FROM ";

    for (Operator o : source) {
      prefix += o.toSQLString();
    }

    prefix = prefix.substring(0, prefix.length() - 3);

    prefix += " WHERE ";

    for (Expression e : params.expression) prefix += e.toSQLString() + " AND ";

    prefix = prefix.substring(0, prefix.length() - 4);

    return "(" + prefix + ") as " + className + hashCode();
  }
}
