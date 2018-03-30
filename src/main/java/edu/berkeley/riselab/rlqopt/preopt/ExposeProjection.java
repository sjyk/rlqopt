package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.LinkedList;

public class ExposeProjection implements PreOptimizationRewrite {

  public ExposeProjection() {}

  public Operator apply(Operator in) {

    try {

      if (in instanceof GroupByOperator) {
        LinkedList<Attribute> agg = in.params.expression.getAllVisibleAttributes();
        agg.addAll(in.params.secondary_expression.getAllVisibleAttributes());

        ExpressionList projList = new ExpressionList();

        for (Attribute a : agg) projList.add(a.getExpression());

        OperatorParameters op = new OperatorParameters(projList);
        Operator projected = new ProjectOperator(op, in.source.get(0));

        in.source.pop();
        in.source.add(projected);
      } else return in;

    } catch (OperatorException e) {
      return in;
    }

    LinkedList<Operator> children = new LinkedList();

    for (Operator child : in.source) children.add(apply(child));

    in.source = children;

    return in;
  }
}
