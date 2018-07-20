package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.LinkedList;

public class DiskCostModel implements CostModel {

  private double defaultSelectivity = 0.1;
  private double memory = 1e6;
  private HashMap<Relation, Long> cardinality;
  private HashMap<HashSet<Relation>, Long> pairs;

  public DiskCostModel(HashMap<Relation, Long> cardinality) {
    this.cardinality = cardinality;
  }

  public DiskCostModel(Database db, String filename) {
    try {

      Scanner scanner = new Scanner(new File(filename + "/imdb_tables.txt"));
      cardinality = new HashMap();
      pairs = new HashMap();

      while (scanner.hasNext()) {
        String[] line = scanner.nextLine().trim().split(":");
        Relation r = db.getByName(line[0]);
        long r_cardinality = Long.parseLong(line[1]);
        cardinality.put(r, r_cardinality);
      }

      scanner.close();

      scanner = new Scanner(new File(filename + "/pairs.csv"));

      while (scanner.hasNext()) {
        String[] line = scanner.nextLine().split(",");

        String t1 = line[0].trim();
        Relation r = db.getByName(t1);

        String t2 = line[1].trim();
        Relation s = db.getByName(t2);

        HashSet<Relation> rels = new HashSet();
        rels.add(r);
        rels.add(s);

        long card = Long.parseLong(line[2].trim());

        pairs.put(rels, card);
      }

      scanner.close();

      // System.out.println(cardinality);
      // System.out.println(pairs);

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

  public long getJoinCardinality(Operator in, Cost l, Cost r) {
    long countl = l.resultCardinality;
    long countr = r.resultCardinality;

    Relation el = in.params.expression.get(0).children.get(0).noop.relation;
    Relation er = in.params.expression.get(0).children.get(1).noop.relation;
    HashSet<Relation> rels = new HashSet();
    rels.add(el);
    rels.add(er);
    long cartCard = cardinality.get(el) * cardinality.get(er);
    long pairCard = pairs.get(rels);
    double rf = (pairCard + 0.) / cartCard;

    JoinOperator jop = (JoinOperator) in;

    return (long) (rf * (countl * countr));
  }

  private boolean streamable(Operator in, Cost l){
    
    Operator leftOp = in.source.get(1);
    Operator rightOp = in.source.get(0);

    if (leftOp instanceof HashJoinOperator && l.resultCardinality < memory)
    {
       Operator build = leftOp.source.get(0);

       if (build.getVisibleRelations().size() != 1)
         return false;


       LinkedList<Attribute> expressionAttrs = leftOp.params.expression.getAllVisibleAttributes();
       LinkedList<Attribute> opAttrs = build.getVisibleAttributes();
       Attribute hashAttr = null;

       for (Attribute i: expressionAttrs)
       {  if (opAttrs.contains(i))
          {
            hashAttr = i;
            break;
          }
        }
       

       if (hashAttr != null && in.params.expression.getAllVisibleAttributes().contains(hashAttr))
         return true;
    }

    return false;

  }

  public Cost hashJoinOperator(Operator in, Cost l, Cost r) {

    long countl = l.resultCardinality;
    long countr = r.resultCardinality;

    long card = getJoinCardinality(in, l, r);

    long iocost = 0;

    if (! streamable(in, r) )
      iocost = 5*countr + 5*countl;

    return new Cost(iocost, card, 0);
  }

  //this is just a nested loop join
  public Cost indexJoinOperator(Operator in, Cost l, Cost r) {

    long countl = l.resultCardinality;
    long countr = r.resultCardinality;

    long card = getJoinCardinality(in, l, r);

    long iocost = countr + countl * countr;

    return new Cost(iocost, card, 0);
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
      return groupByOperator(in, doEstimate(in.source.get(0))).plus(doEstimate(in.source.get(0)));


    Cost left = doEstimate(in.source.get(0));
    Cost right = doEstimate(in.source.get(1));

    if (in instanceof HashJoinOperator) {
      JoinOperator jop = (JoinOperator) in;
      return hashJoinOperator(in, left, right).plus(left).plus(right);
    }

    if (in instanceof IndexJoinOperator) {
      JoinOperator jop = (JoinOperator) in;
      return indexJoinOperator(in, left, right).plus(right).plus(left);
    }

    if (in instanceof JoinOperator) {
      JoinOperator jop = (JoinOperator) in;
      return hashJoinOperator(in, left, right).plus(left).plus(right);
    }

    if (in instanceof CartesianOperator)
      return cartesianOperator(in, doEstimate(in.source.get(0)), doEstimate(in.source.get(1)))
          .plus(doEstimate(in.source.get(0)))
          .plus(doEstimate(in.source.get(1)));

    return new Cost(0, 0, 0);
  }

  public Cost estimate(Operator in) {

    Cost runningCost = doEstimate(in);

    if (runningCost.operatorIOcost < 0) runningCost.operatorIOcost = Long.MAX_VALUE;

    if (runningCost.operatorCPUcost < 0) runningCost.operatorCPUcost = Long.MAX_VALUE;

    if (runningCost.resultCardinality < 0) runningCost.resultCardinality = Long.MAX_VALUE;

    return runningCost;
  }
}