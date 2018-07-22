package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.relalg.ProjectOperator;
import edu.berkeley.riselab.rlqopt.relalg.SelectOperator;
import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
import java.util.LinkedList;
import java.util.List;

/** Push Select and Project nodes down to their respective table scans. */
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

  /**
   * Returns true iff "in" is a table scan; "probe" is a project, or a select; all attributes that
   * the two operators refer to come from the same relation.
   *
   * <p>Intuitively, returns whether "probe" (a select or a project) solely refers to the "in"
   * relation.
   */
  private boolean eligible(Operator in, Operator probe) {
    if (!(in instanceof TableAccessOperator)
        || (!((probe instanceof SelectOperator) || (probe instanceof ProjectOperator)))) {
      return false;
    }

    LinkedList<Attribute> s_attrList = in.params.expression.getAllVisibleAttributes();
    LinkedList<Attribute> t_attrList = probe.params.expression.getAllVisibleAttributes();

    // System.out.println( + " " + t_attrList);

    /*for (Attribute s : s_attrList) {
      for (Attribute t : t_attrList) {
        if (!s.relation.equals(t.relation)) return false;
      }
    }*/
    return s_attrList.get(0).relation.equals(t_attrList.get(0).relation);
  }

  /**
   * Pushes "probes" (selects, projects) down to the "in" scan, if eligible. Returns an operator
   * with the pushed down selects/projects wrapped around the scan.
   */
  private Operator eagerEligible(Operator in, List<Operator> probes) {
    if (!(in instanceof TableAccessOperator)) {
      return in;
    }

    Operator prev = in;
    for (Operator p : probes) {
      if (eligible(in, p)) {
        p.source.clear();
        p.source.add(prev);
        prev = p;
      }
    }

    return prev;
  }

  /** Pushes "probes" down into any eligible table scan node under "rtn". */
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
