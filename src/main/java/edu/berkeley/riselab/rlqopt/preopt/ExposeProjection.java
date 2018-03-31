package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.relalg.GroupByOperator;
import edu.berkeley.riselab.rlqopt.relalg.ProjectOperator;
import java.util.LinkedList;

/** Inserts projections that prune away attributes not referenced by a "group by". */
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

        in.source.remove(0);
        in.source.add(projected);
      } else return in;

    } catch (OperatorException e) {
      return in;
    }

    in.source = Utils.map(in.source, this::apply);
    return in;
  }
}
