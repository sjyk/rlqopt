package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.CostModel;
import edu.berkeley.riselab.rlqopt.opt.PlanningModule;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

// this implements one transformation
// of the plan match, discount
public class TDJoinSampler implements PlanningModule {

  boolean resetPerSession;
  Random rand;
  double alpha;
  LinkedList<TrainingDataPoint> trainingData;
  LinkedList<TrainingDataPoint> localData;

  public TDJoinSampler(double alpha) {

    this.rand = new Random();
    this.alpha = alpha;
    trainingData = new LinkedList();
  }

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

    HashSet<Operator> relations = new HashSet();

    localData = new LinkedList();

    for (Operator child : in.source) {
      relations.add(child);
    }

    // System.out.println(costMap);

    for (int i = 0; i < in.source.size() - 1; i++) {
      try {
        relations = TDMerge(relations, c, in);

        // System.out.println(relations.size());

      } catch (OperatorException opex) {
        continue;
      }
    }

    Operator rtn = (Operator) relations.toArray()[0];
    double cost = c.estimate(rtn).operatorIOcost;

    /*if (rtn instanceof CartesianOperator)
      System.out.println("C:" + cost);
    else
      System.out.println("J:" + cost);*/

    for (TrainingDataPoint t : localData) {
      t.cost = cost;
      trainingData.add(t);
    }

    return rtn;
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

  private Operator[] randomJoin(HashSet<Operator> relations, Operator in) throws OperatorException {
    Operator[] pairToJoin = new Operator[3];

    Operator[] relArray = relations.toArray(new Operator[relations.size()]);

    int ind1 = rand.nextInt(relArray.length);
    int ind2 = rand.nextInt(relArray.length);

    while (ind1 == ind2) ind2 = rand.nextInt(relArray.length);

    Operator cjv = getJoinOperator(relArray[ind1], relArray[ind2], in);
    pairToJoin[0] = relArray[ind1];
    pairToJoin[1] = relArray[ind2];
    pairToJoin[2] = cjv;
    return pairToJoin;
  }

  private Operator getJoinOperator(Operator i, Operator j, Operator in) throws OperatorException {

    Expression e = findJoinExpression(in.params.expression, i, j);

    Operator cjv;

    if (e == null) {
      OperatorParameters params = new OperatorParameters(new ExpressionList());
      cjv = new CartesianOperator(params, i, j);

    } else {
      OperatorParameters params = new OperatorParameters(e.getExpressionList());
      cjv = new JoinOperator(params, i, j);
    }

    return cjv;
  }

  public HashSet<Operator> TDMerge(HashSet<Operator> relations, CostModel c, Operator in)
      throws OperatorException {

    double minCost = Double.MAX_VALUE;
    Operator[] pairToJoin = new Operator[3];
    HashSet<Operator> rtn = (HashSet) relations.clone();

    boolean egreedy = false;
    if (rand.nextDouble() < alpha) {
      egreedy = true;
      pairToJoin = randomJoin(relations, in);
    }

    // for all pairs of operators
    for (Operator i : relations) {

      for (Operator j : relations) {

        // don't join with self
        if (i == j) continue;

        Operator cjv = getJoinOperator(i, j, in);

        // exploration
        // System.out.println(rand.nextGaussian());
        double cost = c.estimate(cjv).operatorIOcost;

        if ((cost < minCost) && !egreedy) {
          minCost = cost;
          pairToJoin[0] = i;
          pairToJoin[1] = j;
          pairToJoin[2] = cjv;
        }
      }
    }

    rtn.remove(pairToJoin[0]);
    rtn.remove(pairToJoin[1]);
    rtn.add(pairToJoin[2]);

    localData.add(new TrainingDataPoint(pairToJoin, new Double(0)));

    return rtn;
  }

  public LinkedList<TrainingDataPoint> getTrainingData() {
    return trainingData;
  }
}
