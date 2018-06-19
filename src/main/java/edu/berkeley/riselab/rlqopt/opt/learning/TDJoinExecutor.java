package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.opt.PlanningModule;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

// this implements one transformation
// of the plan match, discount
public class TDJoinExecutor implements PlanningModule {

  boolean resetPerSession;
  Random rand;
  double alpha;
  LinkedList<TrainingDataPoint> trainingData;
  LinkedList<TrainingDataPoint> localData;
  MultiLayerNetwork net;
  Database db;

  BaselineLeftDeep lfdb;

  public TDJoinExecutor(Database db) {

    this.rand = new Random();
    this.alpha = alpha;
    this.db = db;
    trainingData = new LinkedList();

    lfdb = new BaselineLeftDeep();
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

    /*if (in.source.size() == 2) {
      try {
        OperatorParameters params = new OperatorParameters(in.params.expression);
        return new JoinOperator(params, in.source.get(0), in.source.get(1));
      } catch (OperatorException opex) {
        return in;
      }
    }*/

    HashSet<Operator> relations = new HashSet();

    localData = new LinkedList();

    for (Operator child : in.source) {
      relations.add(child);
    }

    // System.out.println(costMap);

    for (int i = 0; i < in.source.size() - 1; i++) {
      try {
        relations = TDMerge(relations, c, in);

      } catch (OperatorException opex) {
        continue;
      }
    }

    Operator rtn = (Operator) relations.toArray()[0];
    double cost = c.estimate(rtn).operatorIOcost;

    if (!(rtn instanceof JoinOperator) || relations.size() > 1)
      System.out.println("___!!!!___" + relations);

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

  private Operator getRemainingOperators(HashSet<Operator> relations, Operator in)
      throws OperatorException {
    Operator[] relArray = relations.toArray(new Operator[relations.size()]);
    OperatorParameters params = new OperatorParameters(in.params.expression);
    return new KWayJoinOperator(params, relArray);
  }

  public HashSet<Operator> TDMerge(HashSet<Operator> relations, CostModel c, Operator in)
      throws OperatorException {

    double minCost = Double.MAX_VALUE;
    Operator[] pairToJoin = new Operator[3];
    HashSet<Operator> rtn = (HashSet) relations.clone();

    // for all pairs of operators
    for (Operator i : relations) {

      for (Operator j : relations) {

        // don't join with self
        if (i == j) continue;

        Expression e = findJoinExpression(in.params.expression, i, j);

        Operator cjv;

        if (e == null) {
          // OperatorParameters params = new OperatorParameters(new ExpressionList());
          // cjv = new CartesianOperator(params, i, j);
          continue;

        } else {
          OperatorParameters params = new OperatorParameters(e.getExpressionList());
          cjv = new JoinOperator(params, i, j);
        }

        // exploration
        Operator[] currentPair = new Operator[4];
        currentPair[0] = i;
        currentPair[1] = j;
        currentPair[2] = cjv;
        currentPair[3] = in.copy();

        double cost;

        if (net != null) {
//          System.out.println("********************");
//          System.exit(1);
//            assert false;
          TrainingDataPoint tpd =
              new TrainingDataPoint(
                  currentPair,
                  0f,
                0f,
                  (float) relations.size());
          INDArray input = tpd.featurizeND4j(db, c);
          INDArray out = net.output(input, false);
          cost = out.getDouble(0);

          //remove
//          HashSet<Operator> local = (HashSet) rtn.clone();
//          local.remove(i);
//          local.remove(j);
//          local.add(cjv);
//          Operator baseline = lfdb.reorderJoin(getRemainingOperators(local, in), c);
//
//          System.out.println(input + " : " + cost + " " + Math.log(c.estimate(baseline).operatorIOcost));


        } else {
//          assert false;
          cost = c.estimate(cjv).operatorIOcost;
        }

        //if (Double.isNaN(cost)) cost = c.estimate(cjv).operatorIOcost;

        if (cost < minCost) {
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

    return rtn;
  }

  public LinkedList<TrainingDataPoint> getTrainingData() {
    return trainingData;
  }
}
