package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

// this implements one transformation
// of the plan match
public class BaselineLeftDeep {

  boolean resetPerSession;

  public BaselineLeftDeep() {}

  private LinkedList<Attribute>[] getLeftRightAttributes(Expression e) {

    LinkedList<Attribute> allAttributes = e.getVisibleAttributes();
    HashMap<Relation, LinkedList<Attribute>> leftAndRight = new HashMap();

    for (Attribute a : allAttributes) {
      Relation attrRel = a.relation;
      if (!leftAndRight.containsKey(attrRel)) {
        leftAndRight.put(attrRel, new LinkedList());
      }

      LinkedList<Attribute> split = leftAndRight.get(attrRel);
      split.add(a);
    }

    LinkedList<Attribute>[] rtn = new LinkedList[2];

    int count = 0;
    for (Relation r : leftAndRight.keySet()) {
      rtn[count] = leftAndRight.get(r);
      count++;
    }

    return rtn;
  }

  private boolean isSubList(LinkedList<Attribute> superL, LinkedList<Attribute> subL) {

    for (Attribute a : subL) {
      if (!superL.contains(a)) {
        // System.out.println(superL + " " + subL);
        return false;
      }
    }

    return true;
  }

  private Operator greatest(
      HashMap<HashSet<Operator>, Operator> map,
      CostModel c,
      HashSet<Operator> key,
      Operator newOp) {
    if (!map.containsKey(key)) return newOp;
    else {
      Operator oldOp = map.get(key);
      double oldOpCost = c.estimate(oldOp).operatorIOcost;
      double newOpCost = c.estimate(newOp).operatorIOcost;

      if (newOpCost < oldOpCost) return newOp;
      else return oldOp;
    }
  }

  // get all the visible attributes

  // takes an operator returns an equivalent operator

  public Operator apply(Operator in, CostModel c) {

    LinkedList<Operator> newChildren = new LinkedList();

    for (Operator child : in.source) newChildren.add(apply(child, c));

    in.source = newChildren;

    if (in instanceof KWayJoinOperator) return reorderJoin(in, c);
    else return in;
  }

  public Operator reorderJoin(Operator in, CostModel c) {

    HashMap<HashSet<Operator>, Operator> costMap = new HashMap();

    for (Operator child : in.source) {
      HashSet<Operator> singleton = new HashSet();
      singleton.add(child);
      costMap.put(singleton, child);
    }

    // System.out.println(costMap);

    for (int i = 0; i < in.source.size() - 1; i++) {
      try {
        costMap = dynamicProgram(costMap, c, in);
      } catch (OperatorException opex) {
        continue;
      }
    }

    return (Operator) costMap.values().toArray()[0];
  }

  private HashMap<HashSet<Operator>, Operator> dynamicProgram(
      HashMap<HashSet<Operator>, Operator> costMap, CostModel c, Operator in)
      throws OperatorException {

    HashMap<HashSet<Operator>, Operator> result = new HashMap();

    for (HashSet<Operator> key : costMap.keySet()) {
      Operator val = costMap.get(key);
      LinkedList<Attribute> joinedAttributes = val.getVisibleAttributes();

      for (Operator child : in.source) {
        LinkedList<Attribute> newAttributes = child.getVisibleAttributes();

        for (Expression e : in.params.expression) {
          LinkedList<Attribute>[] leftRight = getLeftRightAttributes(e);

          LinkedList<Attribute> left = leftRight[0];
          LinkedList<Attribute> right = leftRight[1];

          if (!(isSubList(newAttributes, left) || isSubList(newAttributes, right))) continue;

          if (isSubList(joinedAttributes, left) && isSubList(joinedAttributes, right)) {
            continue;
          } else if (isSubList(joinedAttributes, left) && !key.contains(child)) {
            OperatorParameters params = new OperatorParameters(e.getExpressionList());
            JoinOperator cjv = new JoinOperator(params, val, child);

            HashSet<Operator> cloneset = (HashSet) key.clone();
            cloneset.add(child);
            result.put(cloneset, greatest(result, c, cloneset, cjv));

            // System.out.println(e);

            break;

          } else if (isSubList(joinedAttributes, right) && !key.contains(child)) {
            OperatorParameters params = new OperatorParameters(e.getExpressionList());
            JoinOperator cjv = new JoinOperator(params, child, val);
            HashSet<Operator> cloneset = (HashSet) key.clone();
            cloneset.add(child);
            result.put(cloneset, greatest(result, c, cloneset, cjv));

            // System.out.println(e);

            break;
          } else {
            continue;
          }
        }
      }
    }

    return result;
  }
}
