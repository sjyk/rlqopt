package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.relalg.SelectOperator;

/**
 * Transforms Select(a AND b AND ...) into a series of singleton selects, Select(a, Select(b, ...)).
 *
 * <p>This could be useful when the attributes reference different relations; after the
 * transformation, the filters have the opportunity to be pushed down.
 */
public class CascadedSelect implements PreOptimizationRewrite {

  public CascadedSelect() {}

  public Operator apply(Operator in) {

    try {

      if (in instanceof SelectOperator) {
        Expression predicate = in.params.expression.get(0);
        Operator source = in.source.get(0);

        if (predicate.op.equals(Expression.AND)) {

          for (Expression e : predicate.children) {

            OperatorParameters param = new OperatorParameters(e.getExpressionList());
            source = new SelectOperator(param, source);
          }

          return source;

        } else {
          return in;
        }
      }

    } catch (OperatorException e) {
      return in;
    }

    in.source = Utils.map(in.source, this::apply);
    return in;
  }
}
