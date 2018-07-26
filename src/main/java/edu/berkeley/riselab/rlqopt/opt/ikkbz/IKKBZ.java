package edu.berkeley.riselab.rlqopt.opt.ikkbz;

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
import javafx.util.Pair;

// this implements one transformation
// of the plan match
public class IKKBZ extends PlanningModule {

  boolean resetPerSession;

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
        //System.out.println(superL + " " + subL);
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

  
  public HashMap<Operator, LinkedList<Pair<Operator, Expression>>> queryGraph(Operator in){

    HashMap<Operator, LinkedList<Pair<Operator, Expression>>> rtn = new HashMap();

    for (Operator i : in.source) {
      LinkedList<Pair<Operator, Expression>> edges = new LinkedList();
      for (Operator j : in.source) {

         if (i == j) continue;

         Expression e = findJoinExpression(in.params.expression, i, j);

         if (e != null) edges.add(new Pair(j,e));
      }
      rtn.put(i, edges);
    }

    return rtn;
  
  }

  
  public Operator highestRankedMerge(HashMap<Operator, LinkedList<Pair<Operator, Expression>>> qg, Operator start, CostModel c) throws OperatorException{
      Operator max = null;
      double maxRank = Double.MAX_VALUE;

      for(Pair<Operator, Expression> edgeTuple: qg.get(start)){

        Operator edge = edgeTuple.getKey();
        Expression e = edgeTuple.getValue();

        //System.out.println(edgeTuple + " " + !qg.containsKey(edge));

        //if (!qg.containsKey(edge)) continue;

        long cardinalityRight = c.estimate(edge).resultCardinality;
        long cardinalityLeft = c.estimate(start).resultCardinality;

        OperatorParameters params = new OperatorParameters(e.getExpressionList());
        JoinOperator cjv = new JoinOperator(params, start, edge);

        double selectivity = (c.estimate(cjv).resultCardinality+0.)/(cardinalityRight);

        double rank = (selectivity*cardinalityRight - 1)/ (selectivity*cardinalityRight);

        if (rank < maxRank){
          maxRank = rank;
          max = bestPhysicalOperator(edge, start, e, c);   
        }

      }
      return max;
  }

  public Operator bestPhysicalOperator(Operator i, Operator j, Expression e, CostModel c) throws OperatorException{ 
      OperatorParameters params = new OperatorParameters(e.getExpressionList());
      double cost = Double.MAX_VALUE;
      Operator best = null;
      for (Operator cjv : JoinOperator.allValidPhysicalJoins(params, i, j)) {

          double opCost = c.estimate(cjv).operatorIOcost;

          if (opCost < cost) {
            cost = opCost;
            best = cjv;
          }

      }
      return best;
  }


  public void updateQueryGraph(HashMap<Operator, LinkedList<Pair<Operator, Expression>>> qg, Operator newJoin){
    
    /*System.out.println( "^^" + newJoin.source.get(1) + " \n\n +++ ");
    for(Operator o: qg.keySet())
      System.out.println(o);*/

    Operator opl = newJoin.source.get(0);
    Operator opr = newJoin.source.get(1);

    LinkedList<Pair<Operator, Expression>> lEdges = qg.get(opl);
    LinkedList<Pair<Operator, Expression>> rEdges = qg.get(opr);
    lEdges.addAll(rEdges);

    qg.remove(opl);
    qg.remove(opr);
    qg.put(newJoin, lEdges);

    for (Operator key: qg.keySet())
    {
      LinkedList<Pair<Operator,Expression>> edges = qg.get(key);
      LinkedList<Pair<Operator,Expression>> copy = new LinkedList(edges);

      for (Pair<Operator,Expression> edge: copy)
      {
        if (edges.equals(lEdges) && edge.getKey().equals(opl))
        {
          edges.remove(edge);
          continue;
        }

        if (edge.getKey().equals(opl) || edge.getKey().equals(opr))
        {
          edges.add(new Pair(newJoin, edge.getValue()));
          edges.remove(edge);
        }

      }

    }

  }

  public Operator rankOrderJoin(Operator in, Operator root, CostModel c) {

    HashMap<Operator, LinkedList<Pair<Operator, Expression>>> qg = queryGraph(in);
    for (int i = 0; i < in.source.size(); i++) {
      try {
        Operator join = highestRankedMerge(qg,root,c);

        if (join == null)
          return (Operator) root;

        updateQueryGraph(qg, join);
        
        root = join;

      } catch (OperatorException opex) {
        continue;
      }
    }
    return (Operator) root;
  }


  public Operator reorderJoin(Operator in, CostModel c) {

    Operator best = null;
    double cost = Double.MAX_VALUE; 

    for (Operator child : in.source) {
      Operator result = rankOrderJoin(in, child, c);
      double childCost = c.estimate(result).operatorIOcost;

      if(childCost < cost)
      {
        best = result;
        cost = childCost;
      }
      
    }

    return best;
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
