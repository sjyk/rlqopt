package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.HashMap;

public class TableStatisticsModel extends HistogramRelation implements CostModel {

  private double defaultSelectivity = 0.1;
  private int availableMemory;

  public TableStatisticsModel(HashMap<Attribute, Histogram> data) {
    super(data);
    this.availableMemory = 50;
  }

  public TableStatisticsModel() {
    super(new HashMap());
    this.availableMemory = 50;
  }

  public Cost tableAccessOperator(Operator in) {
    int count = HistogramOperations.eval(this, in).count();
    return new Cost(count, count, 0);
  }

  public Cost projectOperator(Operator in, Cost costIn) {
    // int count = HistogramOperations.eval(this, in);
    return new Cost(costIn.resultCardinality, costIn.resultCardinality, 0);
  }

  public Cost groupByOperator(Operator in, Cost costIn) {
    int count = HistogramOperations.eval(this, in).count();
    return new Cost(costIn.resultCardinality, count, 0);
  }

  public Cost selectOperator(Operator in, Cost costIn) {
    int count = HistogramOperations.eval(this, in).count();
    return new Cost(costIn.resultCardinality, count, 0);
  }

  public Cost joinOperator(Operator in, Cost l, Cost r) {

    int count = HistogramOperations.eval(this, in).count();
    int countr = HistogramOperations.eval(this, in.source.get(0)).count();
    int countl = HistogramOperations.eval(this, in.source.get(1)).count();

    // System.out.println((availableMemory-2));
    return new Cost(countr + countl * countr / (availableMemory - 2), count, 0);
  }

  public Cost cartesianOperator(Operator in, Cost l, Cost r) {

    int count = HistogramOperations.eval(this, in).count();

    return new Cost(l.resultCardinality * r.resultCardinality, count, 0);
  }

  private Cost doEstimate(Operator in) {

    Cost runningCost = new Cost(0, 0, 0);

    if (in instanceof TableAccessOperator) return tableAccessOperator(in);

    if (in instanceof ProjectOperator)
      return projectOperator(in, doEstimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof SelectOperator)
      return selectOperator(in, doEstimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof GroupByOperator)
      return groupByOperator(in, estimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof JoinOperator)
      return joinOperator(in, doEstimate(in.source.get(0)), doEstimate(in.source.get(1)))
          .plus(doEstimate(in.source.get(0)))
          .plus(doEstimate(in.source.get(1)));

    if (in instanceof CartesianOperator)
      return cartesianOperator(in, doEstimate(in.source.get(0)), doEstimate(in.source.get(1)))
          .plus(doEstimate(in.source.get(0)))
          .plus(doEstimate(in.source.get(1)));

    return runningCost;
  }

  public Cost estimate(Operator in) {
    Cost runningCost = new Cost(0, 0, 0);
    runningCost = doEstimate(in);

    if (runningCost.operatorIOcost < 0) runningCost.operatorIOcost = Long.MAX_VALUE;

    if (runningCost.operatorCPUcost < 0) runningCost.operatorCPUcost = Long.MAX_VALUE;

    if (runningCost.resultCardinality < 0) runningCost.resultCardinality = Long.MAX_VALUE;

    return runningCost;
  }
}
