package edu.berkeley.riselab.rlqopt.workload;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class WorkloadGeneratorEasy extends WorkloadGenerator {

  Database db;
  Random rand;
  TableStatisticsModel ts;

  public WorkloadGeneratorEasy(DatasetGenerator ds) {

    db = ds.getDatabase();
    ts = ds.getStats();
    rand = new Random(1234);
  }

  public TableStatisticsModel getStatsModel() {
    return ts;
  }

  public Database getDatabase() {
    return db;
  }

  public Expression generateSelection(Relation r) {
    HashSet<Attribute> allAttributes = r.attributes();
    Attribute[] attributeArray = allAttributes.toArray(new Attribute[allAttributes.size()]);
    Attribute randAttribute = attributeArray[rand.nextInt(attributeArray.length)];
    Histogram atStats = ts.get(randAttribute);
    int randomEquality = rand.nextInt((int) atStats.max());
    return new Expression(
        Expression.EQUALS, randAttribute.getExpression(), new Expression(randomEquality + ""));
  }

  public Operator generateSelectOp(Operator r) throws OperatorException {
    LinkedList<Attribute> allAttributes = r.getVisibleAttributes();
    Attribute[] attributeArray = allAttributes.toArray(new Attribute[allAttributes.size()]);
    Attribute randAttribute = attributeArray[rand.nextInt(attributeArray.length)];
    Histogram atStats = ts.get(randAttribute);
    int randomEquality = rand.nextInt((int) atStats.max());

    int type = rand.nextInt(3);
    Expression e;

    switch (type) {
      case 0:
        e =
            new Expression(
                Expression.EQUALS,
                randAttribute.getExpression(),
                new Expression(randomEquality + ""));
        break;

      case 1:
        e =
            new Expression(
                Expression.GREATER_THAN,
                randAttribute.getExpression(),
                new Expression(randomEquality + ""));
        break;

      default:
        e =
            new Expression(
                Expression.LESS_THAN,
                randAttribute.getExpression(),
                new Expression(randomEquality + ""));
    }

    OperatorParameters params = new OperatorParameters(e.getExpressionList());
    return new SelectOperator(params, r);
  }

  private Operator createScan(Relation r) throws OperatorException {
    OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
    TableAccessOperator scan_r = new TableAccessOperator(scan_params);
    return scan_r;
  }

  public Operator generateSingleSelection() throws OperatorException {
    int size = db.size();
    Relation r = db.get(rand.nextInt(size));
    Expression e = generateSelection(r);
    OperatorParameters params = new OperatorParameters(e.getExpressionList());
    return new SelectOperator(params, createScan(r));
  }

  public OperatorParameters generateGroupBy(Operator in) {
    LinkedList<Attribute> allAttributes = in.getVisibleAttributes();
    Attribute[] attributeArray = allAttributes.toArray(new Attribute[allAttributes.size()]);
    Attribute randAttribute1 = attributeArray[rand.nextInt(attributeArray.length)];
    Attribute randAttribute2 = attributeArray[rand.nextInt(attributeArray.length)];
    ExpressionList ea = randAttribute1.getExpression().getExpressionList();
    ExpressionList agg = new ExpressionList(new Expression("sum", randAttribute2.getExpression()));
    return new OperatorParameters(agg, ea);
  }

  public Operator generateSingleGroupBy() throws OperatorException {
    int size = db.size();
    Relation r = db.get(rand.nextInt(size));
    Operator rop = createScan(r);
    OperatorParameters params = generateGroupBy(rop);
    return new GroupByOperator(params, rop);
  }

  public Operator generateSelGroupBy() throws OperatorException {
    int size = db.size();
    Operator rop = generateSingleSelection();
    OperatorParameters params = generateGroupBy(rop);
    return new GroupByOperator(params, rop);
  }

  private OperatorParameters createNaturalJoin(Relation r, Relation s) {
    Expression conjunction = null;

    for (Expression er : r.getExpressionList()) {

      for (Expression es : s.getExpressionList()) {

        if (er.noop.attribute.equals(es.noop.attribute)) {

          Expression clause = new Expression(Expression.EQUALS, er, es);

          if (conjunction == null) conjunction = clause;
          else conjunction = new Expression(Expression.AND, clause, conjunction);
        }
      }
    }

    if (conjunction == null) return null;

    return new OperatorParameters(conjunction.getExpressionList());
  }

  // fix cartesian product bug
  public Operator generateJoin() throws OperatorException {
    HashSet<Relation> tablesToJoin = new HashSet();
    int tablesToJoinCnt = Math.max(2, rand.nextInt(db.size()));

    for (int i = 0; i < tablesToJoinCnt; i++) {
      tablesToJoin.add(db.get(rand.nextInt(db.size())));
    }

    if (tablesToJoin.size() < 2) tablesToJoin.add(db.get(0));

    Relation prev = null;
    Operator rtn = null;

    ArrayList<Relation> tempList = new ArrayList(tablesToJoin.size());
    for (Relation r : tablesToJoin) tempList.add(r);

    Collections.shuffle(tempList);

    for (Relation r : tempList) {
      if (prev == null) {
        rtn = createScan(r);
        prev = r;
      } else {
        OperatorParameters params = createNaturalJoin(r, prev);

        if (params == null || tempList.size() < 3) return null;
        else rtn = new JoinOperator(params, createScan(r), rtn);
      }
    }

    return rtn;
  }

  public Operator generateJoinSel() throws OperatorException {
    return generateSelectOp(generateJoin());
  }

  public Operator generateJoinSelGb() throws OperatorException {
    Operator in = generateSelectOp(generateJoin());
    OperatorParameters params = generateGroupBy(in);
    return new GroupByOperator(params, in);
  }

  public LinkedList<Operator> generateWorkload(int n) throws OperatorException {

    LinkedList<Operator> workload = new LinkedList();
    for (int i = 0; i < n; i++) {
      // int k = rand.nextInt(2);
      // if (k == 0)
      Operator o = generateJoin();

      if (o == null) continue;

      workload.add(o);
      // else if (k == 1) workload.add(generateJoinSel());
      // else if (k == 2) workload.add(generateJoinSelGb());
    }

    return workload;
  }
}
