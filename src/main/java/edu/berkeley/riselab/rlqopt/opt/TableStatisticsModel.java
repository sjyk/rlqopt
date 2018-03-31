package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Relation;
import java.util.HashMap;
import java.util.LinkedList;

public class TableStatisticsModel extends HashMap<Attribute, LinkedList<AttributeStatistics>>
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
      this.put(a, hist);
    }
  }

  private long cardinalityEstimate(Relation r) {

    return cardinalityEstimate(r.attributes());
  }

  private long cardinalityEstimate(Iterable<Attribute> list) {

    long card = 0;
    for (Attribute a : list) {
      long sum = 0;
      if (containsKey(a)) {
        for (AttributeStatistics s : get(a)) sum += s.distinctValues;
      }

      card = Math.max(card, sum);
    }

    return card;
  }

  private long selectivityEstimate(Iterable<Attribute> list) {

    long card = 0;
    for (Attribute a : list) {
      long sum = 0;
      if (containsKey(a)) {
        for (AttributeStatistics s : get(a)) sum += s.distinctValues;
      }

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
    return new Cost(costIn.operatorIOcost, costIn.resultCardinality, 0);
  }

  public Cost groupByOperator(Operator in, Cost costIn) {

    LinkedList<Attribute> al = in.params.secondary_expression.getAllVisibleAttributes();
    long estimate = cardinalityEstimate(al);

    return new Cost(costIn.operatorIOcost, estimate, 0);
  }

  public Cost selectOperator(Operator in, Cost costIn) {

    LinkedList<Attribute> al = in.getVisibleAttributes();
    double reduction = 1.0;
    for (Attribute a : al) {

      double sum = 0;
      try {
        if (containsKey(a)) {
          LinkedList<AttributeStatistics> aslist = get(a);

          for (AttributeStatistics s : aslist)
            sum += s.estimateReductionFactor(in.params.expression.get(0));

          reduction *= (sum / aslist.size());
        } else {

          reduction = defaultSelectivity;
          break;
        }
      } catch (CannotEstimateException ex) {
        reduction = defaultSelectivity;
        break;
      }
    }

    return new Cost(costIn.operatorIOcost, (long) (costIn.resultCardinality * reduction), 0);
  }

  public Cost joinOperator(Operator lop, Operator rop, Cost l, Cost r) {

    long cartesian = l.resultCardinality * r.resultCardinality;
    long result = (long) (cartesian / Math.max(l.resultCardinality, r.resultCardinality));

    return new Cost(2 * l.operatorIOcost + 2 * r.operatorIOcost, result, 0);
  }

  public Cost estimate(Operator in) {

    Cost runningCost = new Cost(0, 0, 0);

    return runningCost;
  }
}
