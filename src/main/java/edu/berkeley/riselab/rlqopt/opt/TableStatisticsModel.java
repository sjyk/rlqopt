package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

public class TableStatisticsModel extends HashMap<Integer, LinkedList<AttributeStatistics>>
    implements CostModel {

  private double defaultSelectivity = 0.1;

  public TableStatisticsModel() {
    super();
  }

  public void putStats(Attribute a, AttributeStatistics s) {

    if (this.containsKey(a)) {
      LinkedList<AttributeStatistics> hist = this.get(a);
      hist.add(s);
    } else {

      LinkedList<AttributeStatistics> hist = new LinkedList();
      hist.add(s);
      this.put(a.hashCode(), hist);
    }
  }

  private long cardinalityEstimate(Relation r) {

    return cardinalityEstimate(r.attributes());
  }

  private long cardinalityEstimate(Iterable<Attribute> list) {
    long card = 0;
    for (Attribute a : list) {
      long sum = 0;

      if (containsKey(a.hashCode())) {
        for (AttributeStatistics s : get(a.hashCode())) sum += s.cardinality;
      }

      // System.out.println(a + " : " + sum);
      // for(Attribute b: this.keySet())
      //  System.out.println(b);

      card = Math.max(card, sum);
    }

    return card;
  }

  public Cost tableAccessOperator(Operator in) {

    ExpressionList el = in.params.expression;
    LinkedList<Attribute> al = el.getAllVisibleAttributes();
    long estimate = cardinalityEstimate(al);
    return new Cost(estimate, estimate, 0);
  }

  public Cost projectOperator(Operator in, Cost costIn) {
    return new Cost(costIn.resultCardinality, costIn.resultCardinality, 0);
  }

  public Cost groupByOperator(Operator in, Cost costIn) {

    LinkedList<Attribute> al = in.params.secondary_expression.getAllVisibleAttributes();
    long estimate = cardinalityEstimate(al);

    return new Cost(costIn.resultCardinality, estimate, 0);
  }


  public Cost selectOperator(Operator in, Cost costIn) {

    double rf = estimateReductionFactor(in.params.expression.get(0));
    return new Cost(costIn.resultCardinality, (long) (rf*costIn.resultCardinality), 0);
   
  }


  public double estimateReductionFactor(Expression e){

      if (e.op.equals(Expression.NOT))
        return 1.0 - estimateReductionFactor(e.children.get(0));
      else if (e.op.equals(Expression.AND))
        return estimateReductionFactor(e.children.get(0))*estimateReductionFactor(e.children.get(1));
      else if (e.op.equals(Expression.OR))
        return estimateReductionFactor(e.children.get(0)) + estimateReductionFactor(e.children.get(1));
      else {

          HashSet<Attribute> attrSet = e.getVisibleAttributeSet();
          Iterator<Attribute> attrSetIter = attrSet.iterator(); 

          if (attrSet.size() == 1){

            Attribute a = attrSetIter.next();

            try{

              double rf = 0;
              for (AttributeStatistics astats: get(a.hashCode()))
                  rf += astats.estimateReductionFactor(e);

               return rf;
            }
            catch(Exception ex){return defaultSelectivity;}

          }
          else if (attrSet.size() == 2) {

            Attribute a1 = attrSetIter.next();
            Attribute a2 = attrSetIter.next();

            try{
               double rf = estimateReductionFactorDouble(e,get(a1.hashCode()).get(0),get(a2.hashCode()).get(0));
               return rf;
            }
            catch(Exception ex){return defaultSelectivity;}

          }
          else
            return defaultSelectivity;

      }

  }

  private double estimateReductionFactorDouble(Expression e, AttributeStatistics a1, AttributeStatistics a2) throws CannotEstimateException {

      if (e.op.equals(Expression.EQUALS)) {
        
        return clip10(1.0 / Math.max(a1.distinctValues,a2.distinctValues));

      }
      else if (e.op.equals(Expression.GREATER_THAN) || e.op.equals(Expression.GREATER_THAN_EQUALS)){

        return 1.0;
      }
      else if (e.op.equals(Expression.LESS_THAN) || e.op.equals(Expression.LESS_THAN_EQUALS)){

        return 1.0;
      }
      else
        throw new CannotEstimateException(e);    

  }

  private double clip10(double val) {
    return Math.max(Math.min(val, 1.0), 0.0);
  }


  public Cost joinOperator(Cost l, Cost r) {

    long cartesian = l.resultCardinality * r.resultCardinality;
    long result = (long) (cartesian / Math.max(l.resultCardinality, r.resultCardinality));

    return new Cost(2 * l.operatorIOcost + 2 * r.operatorIOcost, result, 0);
  }

  public Cost estimate(Operator in) {

    Cost runningCost = new Cost(0, 0, 0);

    if (in instanceof TableAccessOperator) return tableAccessOperator(in);

    if (in instanceof ProjectOperator)
      return projectOperator(in, estimate(in.source.get(0))).plus(estimate(in.source.get(0)));

    if (in instanceof SelectOperator)
      return selectOperator(in, estimate(in.source.get(0))).plus(estimate(in.source.get(0)));

    if (in instanceof GroupByOperator)
      return groupByOperator(in, estimate(in.source.get(0))).plus(estimate(in.source.get(0)));

    if (in instanceof JoinOperator)
      return joinOperator(estimate(in.source.get(0)), estimate(in.source.get(1)))
          .plus(estimate(in.source.get(0)))
          .plus(estimate(in.source.get(1)));

    return runningCost;
  }
}
