package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;

import java.util.LinkedList;
import java.util.HashSet;

// implements a project operator
public class JoinOperator extends Operator {

  public final static int NN = 0;
  public final static int KN = 1;
  public final static  int NK = 2;
  public final static int KK = 3;
  public final static int IE = 4;

  public JoinOperator(OperatorParameters params, Operator... source) throws OperatorException {
    super(params, source);
  }

  // override
  public boolean isValid(OperatorParameters params, Operator... source) {

    if (source.length != 2) return false;

    return true;
  }


  public int getJoinType(){

    if (this.params.expression.get(0).isEquality()){

        HashSet<Attribute> leftKeys = source.get(0).getKeys();
        HashSet<Attribute> rightKeys = source.get(1).getKeys();
        LinkedList<Attribute> visibleAttrs = this.params.expression.get(0).getVisibleAttributes();

        for(Attribute attr: visibleAttrs)
        {
          if (leftKeys.contains(attr))
            leftKeys.remove(attr);

          if (rightKeys.contains(attr))
            rightKeys.remove(attr);
        }
      if (leftKeys.size() == 0 && rightKeys.size() > 0)
        return KN;

      if (leftKeys.size() > 0 && rightKeys.size() == 0)
        return NK;

      if (leftKeys.size() == 0 && rightKeys.size() == 0)
        return KK;

      return NN;

    }

    return IE;

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
