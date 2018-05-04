package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;

import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Relation;

// implements a project operator
public class TableAccessOperator extends Operator {

  public TableAccessOperator(OperatorParameters params, Operator... source)
      throws OperatorException {
    super(params);
  }

  public TableAccessOperator(OperatorParameters params) throws OperatorException {
    super(params);
  }

  // override
  public boolean isValid(OperatorParameters params, Operator... source) {

    if (source.length != 0) return false;

    return true;
  }

  public String toSQLString(){
      String className = this.getClass().getSimpleName();

      Expression e = params.expression.get(0);
      Relation r = e.noop.relation;
      
      return "(SELECT * FROM " + r.name + ") as " +className+hashCode();
  }

}
