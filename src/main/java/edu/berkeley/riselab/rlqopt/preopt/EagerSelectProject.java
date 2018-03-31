package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.relalg.ProjectOperator;
import edu.berkeley.riselab.rlqopt.relalg.SelectOperator;
import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
import java.util.LinkedList;
import java.util.List;

public class EagerSelectProject implements InitRewrite {

  private List<Operator> gatherSelectProject(Operator in) {
    return Utils.filterRecursive(
        in, op -> (op instanceof SelectOperator || op instanceof ProjectOperator));
  }

  private Operator removeSelectProject(Operator in) {
    if (in instanceof SelectOperator || in instanceof ProjectOperator) {
      return removeSelectProject(in.source.get(0));
    }
    in.source = Utils.map(in.source, this::removeSelectProject);
    return in;
  }

  private boolean eligible(Operator in, Operator probe) {
    if (!(in instanceof TableAccessOperator)
        || (!((probe instanceof SelectOperator) || (probe instanceof ProjectOperator)))) {
      return false;
    }

    LinkedList<Attribute> s_attrList = in.params.expression.getAllVisibleAttributes();
    LinkedList<Attribute> t_attrList = probe.params.expression.getAllVisibleAttributes();

    for (Attribute s : s_attrList) {
      for (Attribute t : t_attrList) {
        if (!s.relation.equals(t.relation)) return false;
      }
    }

    return true;
  }

  private Operator eagerEligible(Operator in, List<Operator> probe) {
    if (!(in instanceof TableAccessOperator)) {
      return in;
    }

    Operator prev = in;
    for (Operator p : probe) {
      if (eligible(in, p)) {
        p.source.clear();
        p.source.add(prev);
        prev = p;
      }
    }

    return prev;
  }

  private Operator applyRecurse(Operator rtn, List<Operator> probes) {
    if (rtn instanceof TableAccessOperator) return eagerEligible(rtn, probes);
    LinkedList<Operator> children = new LinkedList<>();
    for (Operator child : rtn.source) children.add(applyRecurse(child, probes));
    rtn.source = children;
    return rtn;
  }

  public Operator apply(Operator in) {
    List<Operator> probes = gatherSelectProject(in);
    Operator rtn = removeSelectProject(in);
    return applyRecurse(rtn, probes);
  }
}
