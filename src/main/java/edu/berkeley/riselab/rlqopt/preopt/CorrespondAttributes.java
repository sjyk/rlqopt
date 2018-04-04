package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.relalg.SelectOperator;
import java.util.HashSet;
import java.util.LinkedList;

/** When attributes are meant to be used in a natural join, corresponds attrs */
public class CorrespondAttributes implements PreOptimizationRewrite {

  LinkedList<Relation> otherRelations;

  public CorrespondAttributes(Relation... args) {

    this.otherRelations = new LinkedList();

    for (Relation r : args) otherRelations.add(r);

    // System.out.println(otherRelations);

  }

  public Operator apply(Operator in) {

    try {

      if (in instanceof SelectOperator) {

        Expression predicate = in.params.expression.get(0);
        HashSet<Attribute> attrset = predicate.getVisibleAttributeSet();
        Operator source = in;

        if ((attrset.size() == 1)
            && (predicate.op.equals(Expression.EQUALS)
                || predicate.op.equals(Expression.GREATER_THAN)
                || predicate.op.equals(Expression.GREATER_THAN_EQUALS)
                || predicate.op.equals(Expression.LESS_THAN)
                || predicate.op.equals(Expression.LESS_THAN_EQUALS))) {

          for (Relation r : otherRelations) {
            Attribute a = predicate.children.get(0).noop;

            if (a.relation == r) continue;

            Attribute ap = r.get(a.attribute);

            if (ap == null) continue;

            Expression ep = new Expression(predicate);
            ep.children.get(0).noop = ap;

            OperatorParameters param = new OperatorParameters(ep.getExpressionList());
            source = new SelectOperator(param, source);
          }

          return source;

        } else return in;
      }
    } catch (OperatorException e) {
      return in;
    }

    in.source = Utils.map(in.source, this::apply);
    return in;
  }
}
