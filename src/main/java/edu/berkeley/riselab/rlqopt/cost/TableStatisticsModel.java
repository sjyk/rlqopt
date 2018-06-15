package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.relalg.*;
import edu.berkeley.riselab.rlqopt.Relation;
import java.util.HashMap;
import java.util.HashSet;

public class TableStatisticsModel extends HistogramRelation implements CostModel {

  private double defaultSelectivity = 0.1;
  private int availableMemory;
  private HashMap<HashSet<Relation>, Double> joinReductionFactors;
  private HashMap<Operator, Cost> memoizeTable;

  public TableStatisticsModel(HashMap<Attribute, Histogram> data) {
    super(data);
    this.availableMemory = 1;
    joinReductionFactors = new HashMap();
    memoizeTable = new HashMap();
  }

  public TableStatisticsModel() {
    super(new HashMap());
    this.availableMemory = 1;
    joinReductionFactors = new HashMap();
    memoizeTable = new HashMap();
  }

  public void setJoinReductionFactors(HashMap<HashSet<Relation>, Double> jr){
    joinReductionFactors = jr;
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

    long countr = r.resultCardinality;
    long countl = l.resultCardinality;

    //int countrh = HistogramOperations.eval(this, in.source.get(0)).count();
    //int countlh = HistogramOperations.eval(this, in.source.get(1)).count();

    double reduction = 1.0;
    HashSet<Relation> relations = in.getVisibleRelations();

    if (joinReductionFactors.containsKey(relations))
      reduction = joinReductionFactors.get(relations);

    if (! joinReductionFactors.containsKey(relations))
      System.out.println(relations + " ///// " + joinReductionFactors);

    long count = (long)(countl*countr*reduction);

    return new Cost(countl + countl * countr, count, 0);
  }

  public Cost cartesianOperator(Operator in, Cost l, Cost r) {

    int count = HistogramOperations.eval(this, in).count();

    return new Cost(l.resultCardinality * r.resultCardinality, count, 0);
  }

  private Cost doEstimate(Operator in) {

//    Cost runningCost = new Cost(0, 0, 0);

    if (in instanceof TableAccessOperator) return tableAccessOperator(in);

    if (in instanceof ProjectOperator)
      return projectOperator(in, doEstimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof SelectOperator)
      return selectOperator(in, doEstimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof GroupByOperator)
      return groupByOperator(in, estimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof JoinOperator)
    {
        Cost left = doEstimate(in.source.get(0));
        Cost right = doEstimate(in.source.get(1));

        return joinOperator(in, left, right).plus(left).plus(right);
    }

    if (in instanceof CartesianOperator)
      return cartesianOperator(in, doEstimate(in.source.get(0)), doEstimate(in.source.get(1)))
          .plus(doEstimate(in.source.get(0)))
          .plus(doEstimate(in.source.get(1)));

    return new Cost(0,0,0);
  }

  public Cost estimate(Operator in) {
//    Cost runningCost = new Cost(0, 0, 0);
    Cost runningCost = doEstimate(in);
    runningCost.operatorIOcost += runningCost.resultCardinality;

    if (runningCost.operatorIOcost < 0) runningCost.operatorIOcost = Long.MAX_VALUE;

    if (runningCost.operatorCPUcost < 0) runningCost.operatorCPUcost = Long.MAX_VALUE;

    if (runningCost.resultCardinality < 0) runningCost.resultCardinality = Long.MAX_VALUE;

    return runningCost;
  }
}
