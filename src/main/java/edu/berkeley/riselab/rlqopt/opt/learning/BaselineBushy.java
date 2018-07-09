package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.opt.CostCachingModule;
import edu.berkeley.riselab.rlqopt.relalg.JoinOperator;
import edu.berkeley.riselab.rlqopt.relalg.KWayJoinOperator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

// this implements one transformation
// of the plan match
public class BaselineBushy implements CostCachingModule {

  boolean resetPerSession;

  public BaselineBushy() {}

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
      double oldOpCost = getOrComputeIOEstimate(oldOp, c);
      double newOpCost = getOrComputeIOEstimate(newOp, c);

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

    Operator argMax = null;
    for (int i = 0; i < in.source.size(); i++) {
      try {

        costMap = dynamicProgram(costMap, c, in);
        argMax = baseCase(costMap, in);

      } catch (OperatorException opex) {
        continue;
      }
    }

    return argMax; // (Operator) costMap.values().toArray()[0];
  }

  private Operator baseCase(HashMap<HashSet<Operator>, Operator> costMap, Operator in) {

    for (HashSet<Operator> opList : costMap.keySet()) {

      HashSet<Operator> childOps = new HashSet();

      for (Operator child : in.source) childOps.add(child);

      if (childOps.equals(opList)) return costMap.get(opList);
    }

    return null;
  }

  private HashMap<HashSet<Operator>, Operator> dynamicProgram(
      HashMap<HashSet<Operator>, Operator> costMap, CostModel c, Operator in)
      throws OperatorException {

    HashMap<HashSet<Operator>, Operator> result = new HashMap(costMap);

    for (HashSet<Operator> key : costMap.keySet()) {
      Operator op1 = costMap.get(key);
      LinkedList<Attribute> joinedAttributes1 = op1.getVisibleAttributes();

      for (HashSet<Operator> key2 : costMap.keySet()) {
        Operator op2 = costMap.get(key2);
        LinkedList<Attribute> joinedAttributes2 = op2.getVisibleAttributes();

        HashSet<Operator> intersection = new HashSet(key);
        intersection.retainAll(key2);

        HashSet<Operator> union = new HashSet(key);
        union.addAll(key2);

        // figure out if this is an eligible pair
        if (intersection.size() > 0) continue;

        for (Expression e : in.params.expression) {
          LinkedList<Attribute>[] leftRight = getLeftRightAttributes(e);

          LinkedList<Attribute> left = leftRight[0];
          LinkedList<Attribute> right = leftRight[1];

          if (!(isSubList(joinedAttributes2, left) || isSubList(joinedAttributes2, right)))
            continue;

          if (!(isSubList(joinedAttributes1, left) || isSubList(joinedAttributes1, right)))
            continue;

          if ((isSubList(joinedAttributes1, left) && isSubList(joinedAttributes2, right))
              || (isSubList(joinedAttributes2, left) && isSubList(joinedAttributes1, right))) {
            OperatorParameters params = new OperatorParameters(e.getExpressionList());
            
            JoinOperator cjv = new JoinOperator(params, op1, op2);
            result.put(union, greatest(result, c, union, cjv));

            break;
          }
        }
      }
    }
    return result;
  }
}
