package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.LinkedList;

public class EagerSelectProject implements InitRewrite {

  public EagerSelectProject() {}

  private Operator removeSelectProject(Operator in) {

    if (in instanceof TableAccessOperator) return in;

    if (in instanceof SelectOperator || in instanceof ProjectOperator) {
      return removeSelectProject(in.source.get(0));
    }

    LinkedList<Operator> children = new LinkedList();

    for (Operator child : in.source) children.add(removeSelectProject(child));

    in.source = children;

    return in;
  }

  private LinkedList<Operator> gatherSelectProject(Operator in) {

    LinkedList<Operator> operators = new LinkedList();

    if (in instanceof SelectOperator || in instanceof ProjectOperator) {
      operators.add(in);
    }

    for (Operator child : in.source) operators.addAll(gatherSelectProject(child));

    return operators;
  }

  private boolean eligible(Operator in, Operator probe) {

    if ((!(in instanceof TableAccessOperator))
        || (!((probe instanceof SelectOperator) || (probe instanceof ProjectOperator)))) {
      return false;
    }

    LinkedList<Attribute> s_attrList = in.params.expression.getAllVisibleAttributes();
    LinkedList<Attribute> t_attrList = probe.params.expression.getAllVisibleAttributes();

    for (Attribute s : s_attrList)
      for (Attribute t : t_attrList) if (!s.relation.equals(t.relation)) return false;

    return true;
  }

  private Operator eagerEligible(Operator in, LinkedList<Operator> probe) {

    if (!(in instanceof TableAccessOperator)) {
      return in;
    }

    Operator prev = in;
    for (Operator p : probe)
      if (eligible(in, p)) {

        p.source.clear();
        p.source.add(prev);
        prev = p;
      }

    return prev;
  }

  public Operator applyRecurse(Operator rtn, LinkedList<Operator> probes) {

    LinkedList<Operator> children = new LinkedList();

    if (rtn instanceof TableAccessOperator) return eagerEligible(rtn, probes);

    for (Operator child : rtn.source) children.add(applyRecurse(child, probes));

    rtn.source = children;

    return rtn;
  }

  public Operator apply(Operator in) {

    LinkedList<Operator> probes = gatherSelectProject(in);
    Operator rtn = removeSelectProject(in);
    return applyRecurse(rtn, probes);
  }
}
