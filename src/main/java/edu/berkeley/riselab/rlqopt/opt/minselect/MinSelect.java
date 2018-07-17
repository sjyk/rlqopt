package edu.berkeley.riselab.rlqopt.opt.minselect;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.opt.CostCache;
import edu.berkeley.riselab.rlqopt.opt.PlanningModule;
import edu.berkeley.riselab.rlqopt.relalg.JoinOperator;
import edu.berkeley.riselab.rlqopt.relalg.KWayJoinOperator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MinSelect extends PlanningModule {

  private CostCache costCache = new CostCache();

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

  // get all the visible attributes

  // takes an operator returns an equivalent operator

  public Operator apply(Operator in, CostModel c) {

    LinkedList<Operator> newChildren = new LinkedList();

    for (Operator child : in.source) newChildren.add(apply(child, c));

    in.source = newChildren;

    try {
      if (in instanceof KWayJoinOperator) return reorderJoin(in, c);
      else return in;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public Operator reorderJoin(Operator in, CostModel c) throws OperatorException {
    Operator currPlan = null;

    HashSet<Operator> relations = new HashSet();
    for (Operator child : in.source) {
      relations.add(child);
    }

    while (!relations.isEmpty()) {
      // Invariant: reduce set size by 1.

      double minSelectivity = Double.MAX_VALUE;
      Operator bestRel = null;

      if (currPlan == null) {
        for (Operator baseRel : relations) {
          double card = costCache.getOrComputeCardinality(baseRel, c, this.name);
          if (card < minSelectivity) {
            currPlan = baseRel;
            bestRel = baseRel;
            minSelectivity = card;
          }
        }
      } else {
        for (Operator baseRel : relations) {
          Expression e = findJoinExpression(in.params.expression, currPlan, baseRel);
          if (e == null) continue;

          OperatorParameters params = new OperatorParameters(e.getExpressionList());
          for (Operator newJoin : JoinOperator.allValidPhysicalJoins(params, currPlan, baseRel)) {
            double card =
                costCache.getOrComputeCardinality(newJoin, c, this.name)
                    / (costCache.getOrComputeCardinality(currPlan, c, this.name)
                        * costCache.getOrComputeCardinality(baseRel, c, this.name));

            if (card < minSelectivity) {
              currPlan = newJoin;
              bestRel = baseRel;
              minSelectivity = card;
            }
          }
        }
      }
      relations.remove(bestRel);
    }

    return currPlan;
  }

  private Expression findJoinExpression(ExpressionList e, Operator i, Operator j) {

    LinkedList<Attribute> leftAttributes = i.getVisibleAttributes();
    LinkedList<Attribute> rightAttributes = j.getVisibleAttributes();

    for (Expression child : e) {

      LinkedList<Attribute>[] leftRight = getLeftRightAttributes(child);
      LinkedList<Attribute> lefte = leftRight[0];
      LinkedList<Attribute> righte = leftRight[1];

      if (isSubList(leftAttributes, lefte) && isSubList(rightAttributes, righte)) return child;

      if (isSubList(leftAttributes, righte) && isSubList(rightAttributes, lefte)) return child;
    }

    return null;
  }
}
