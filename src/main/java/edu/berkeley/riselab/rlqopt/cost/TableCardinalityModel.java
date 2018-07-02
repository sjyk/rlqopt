package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class TableCardinalityModel implements CostModel {

  private double defaultSelectivity = 0.1;
  private long availableMemory = (long) 1e7;
  private HashMap<Relation, Long> cardinality;

  public TableCardinalityModel(HashMap<Relation, Long> cardinality) {
    this.cardinality = cardinality;
  }

  public TableCardinalityModel(Database db, String filename) {
    try {

      Scanner scanner = new Scanner(new File(filename));
      cardinality = new HashMap();

      while (scanner.hasNext()) {
        String[] line = scanner.nextLine().trim().split(":");
        Relation r = db.getByName(line[0]);
        long r_cardinality = Long.parseLong(line[1]);
        cardinality.put(r, r_cardinality);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Cost tableAccessOperator(Operator in) {

    HashSet<Relation> rels = in.getVisibleRelations();
    Relation rel = null;
    for (Relation iter : rels) rel = iter;

    long count = cardinality.get(rel);
    return new Cost(count, count, 0);
  }

  public Cost projectOperator(Operator in, Cost costIn) {
    return new Cost(costIn.resultCardinality, costIn.resultCardinality, 0);
  }

  public Cost groupByOperator(Operator in, Cost costIn) {
    return new Cost(costIn.resultCardinality, costIn.resultCardinality, 0);
  }

  public Cost selectOperator(Operator in, Cost costIn) {
    long count = (long) (costIn.resultCardinality * defaultSelectivity);
    return new Cost(costIn.resultCardinality, count, 0);
  }

  public Cost joinOperator(Operator in, Cost l, Cost r) {

    long countr = r.resultCardinality;
    long countl = l.resultCardinality;

    JoinOperator jop = (JoinOperator) in;

    // System.out.println(jop.getJoinType());

    long iocost =
        ((availableMemory > countr) && (availableMemory > countr))
            ? countl + countr
            : countl + countl * countr;

    // System.out.println((availableMemory > countr) && (availableMemory > countr));

    switch (jop.getJoinType()) {
      case JoinOperator.IE:
        return new Cost(iocost, countl * countr, 0);
      case JoinOperator.NN:
        return new Cost(iocost, countl * countr, 0);
      case JoinOperator.NK:
        return new Cost(countl, Math.max(countl, countr), 0);
      case JoinOperator.KN:
        return new Cost(countr, countr, 0);
      case JoinOperator.KK:
        return new Cost(countl, Math.min(countl, countr), 0);
    }

    return new Cost(countl + countl * countr, countl * countr, 0);
  }

  public Cost cartesianOperator(Operator in, Cost l, Cost r) {

    return new Cost(
        l.resultCardinality * r.resultCardinality, l.resultCardinality * r.resultCardinality, 0);
  }

  private Cost doEstimate(Operator in) {
    if (in instanceof TableAccessOperator) return tableAccessOperator(in);

    if (in instanceof ProjectOperator)
      return projectOperator(in, doEstimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof SelectOperator)
      return selectOperator(in, doEstimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof GroupByOperator)
      return groupByOperator(in, estimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));

    if (in instanceof JoinOperator) {
      Cost left = doEstimate(in.source.get(0));
      Cost right = doEstimate(in.source.get(1));

      return joinOperator(in, left, right).plus(left).plus(right);
    }

    if (in instanceof CartesianOperator)
      return cartesianOperator(in, doEstimate(in.source.get(0)), doEstimate(in.source.get(1)))
          .plus(doEstimate(in.source.get(0)))
          .plus(doEstimate(in.source.get(1)));

    return new Cost(0, 0, 0);
  }

  public Cost estimate(Operator in) {
    Cost runningCost = doEstimate(in);
    runningCost.operatorIOcost += runningCost.resultCardinality;

    if (runningCost.operatorIOcost < 0) runningCost.operatorIOcost = Long.MAX_VALUE;

    if (runningCost.operatorCPUcost < 0) runningCost.operatorCPUcost = Long.MAX_VALUE;

    if (runningCost.resultCardinality < 0) runningCost.resultCardinality = Long.MAX_VALUE;

    return runningCost;
  }
}
