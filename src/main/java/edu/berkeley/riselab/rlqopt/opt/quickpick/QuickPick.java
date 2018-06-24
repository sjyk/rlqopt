package edu.berkeley.riselab.rlqopt.opt.quickpick;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.opt.PlanningModule;
import edu.berkeley.riselab.rlqopt.relalg.JoinOperator;
import edu.berkeley.riselab.rlqopt.relalg.KWayJoinOperator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.calcite.util.Pair;

public class QuickPick implements PlanningModule {

  private int numTrajectories;
  private Random random;

  public QuickPick(int numTrajectories) {
    this.numTrajectories = numTrajectories;
    this.random = new Random(1234);
  }

  private LinkedList<Attribute>[] getLeftRightAttributes(Expression e) {
    LinkedList<Attribute> allAttributes = e.getVisibleAttributes();
    HashMap<Relation, LinkedList<Attribute>> leftAndRight = new HashMap<>();

    for (Attribute a : allAttributes) {
      Relation attrRel = a.relation;
      if (!leftAndRight.containsKey(attrRel)) {
        leftAndRight.put(attrRel, new LinkedList<>());
      }

      LinkedList<Attribute> split = leftAndRight.get(attrRel);
      split.add(a);
    }

    LinkedList<Attribute>[] rtn = new LinkedList[2];
    rtn[0] = new LinkedList();
    rtn[1] = new LinkedList();

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

  public Operator apply(Operator in, CostModel c) {
    LinkedList<Operator> newChildren = new LinkedList<>();
    for (Operator child : in.source) newChildren.add(apply(child, c));
    in.source = newChildren;
    try {
      if (in instanceof KWayJoinOperator) return quickPick(in, c);
      else return in;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private Expression findJoinExpression(ExpressionList e, Operator i, Operator j) {
    LinkedList<Attribute> leftAttributes = i.getVisibleAttributes();
    LinkedList<Attribute> rightAttributes = j.getVisibleAttributes();

    for (Expression child : e) {

      LinkedList<Attribute>[] leftRight = getLeftRightAttributes(child);
      LinkedList<Attribute> lefte = leftRight[0];
      LinkedList<Attribute> righte = leftRight[1];

      if (isSubList(leftAttributes, lefte) && isSubList(rightAttributes, righte)) return child;
    }

    return null;
  }

  /** Implements the QuickPick algorithm. */
  private Operator quickPick(Operator in, CostModel c) throws OperatorException {
    double bestCost = Double.MAX_VALUE;
    Operator bestPlan = null;
    for (int i = 0; i < this.numTrajectories; ++i) {
      List<Operator> relations = new ArrayList<>(in.source);
      Pair<Operator, Double> joinAndCost = rollout(relations, c, in);
      System.out.println("QuickPick: collected trajectory " + i);
      double cost = joinAndCost.right;
      if (bestPlan == null || cost < bestCost) {
        bestPlan = joinAndCost.left;
        bestCost = cost;
      }
    }
    return bestPlan;
  }

  private Pair<Operator, Double> rollout(List<Operator> relations, CostModel c, Operator in)
      throws OperatorException {
    if (relations.size() == 1) {
      Operator finalJoin = (Operator) (relations.toArray()[0]);
      double cost = (double) c.estimate(finalJoin).operatorIOcost;
      return new Pair<>(finalJoin, cost);
    }
    // Pick a random & valid edge (i, j) to join.
    int n = relations.size();
    Operator randomJoin, left, right;
    int i, j;
    while (true) {
      i = random.nextInt(n);
      j = random.nextInt(n);
      left = relations.get(i);
      right = relations.get(j);

      // No self-join.
      if (i == j) continue;
      Expression e = findJoinExpression(in.params.expression, left, right);
      // Has to have a valid edge (i, j).
      if (e == null) continue;

      OperatorParameters params = new OperatorParameters(e.getExpressionList());
      randomJoin = new JoinOperator(params, left, right);
      break;
    }
    relations.remove(left);
    relations.remove(right);
    relations.add(randomJoin);
    return rollout(relations, c, in);
  }
}
