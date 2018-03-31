package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.relalg.JoinOperator;
import edu.berkeley.riselab.rlqopt.relalg.KWayJoinOperator;
import java.util.LinkedList;

public class FlattenJoin implements PreOptimizationRewrite {

  public FlattenJoin() {}

  private LinkedList<Operator> flattenRecurse(Operator in) {

    LinkedList<Operator> arguments = new LinkedList();

    if (!(in instanceof JoinOperator)) {
      arguments.add(in);
      return arguments;
    }

    for (Operator child : in.source) arguments.addAll(flattenRecurse(child));

    return arguments;
  }

  private ExpressionList flattenRecurseKey(Operator in) {

    ExpressionList keys = new ExpressionList();

    if (!(in instanceof JoinOperator)) {
      return keys;
    }

    keys.addAll(in.params.expression);

    for (Operator child : in.source) {
      keys.addAll(flattenRecurseKey(child));
    }

    return keys;
  }

  private Operator flatten(Operator in) throws OperatorException {

    if (!(in instanceof JoinOperator)) {
      return in;
    }

    LinkedList<Operator> kwayargs = flattenRecurse(in);
    OperatorParameters kwayjoinkeys = new OperatorParameters(flattenRecurseKey(in));

    return new KWayJoinOperator(kwayjoinkeys, kwayargs.toArray(new Operator[kwayargs.size()]));
  }

  public Operator apply(Operator in) {

    Operator flattened;

    try {
      flattened = flatten(in);
    } catch (OperatorException e) {
      return in;
    }

    flattened.source = Utils.map(flattened.source, this::apply);
    return flattened;
  }
}
