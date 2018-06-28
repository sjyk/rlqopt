package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;

// implements a group by operator
public class GroupByOperator extends Operator {

  public GroupByOperator(OperatorParameters params, Operator... source) throws OperatorException {
    super(params, source);
  }

  // override
  public boolean isValid(OperatorParameters params, Operator... source) {

    if (source.length != 1 && params.secondary_expression != null) return false;

    return true;
  }

  public String toSQLString() {
    String className = this.getClass().getSimpleName();

    String prefix = "SELECT " + params.expression.get(0).toString() + " FROM ";

    for (Operator o : source) {
      prefix += o.toSQLString();
    }

    prefix = prefix.substring(0, prefix.length() - 3);

    prefix += " GROUP BY ";

    for (Expression e : params.secondary_expression) prefix += e.toSQLString() + " , ";

    prefix = prefix.substring(0, prefix.length() - 3);

    return "(" + prefix + ") as " + className + hashCode();
  }
}
