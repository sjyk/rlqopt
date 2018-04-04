package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.opt.AttributeStatistics;
import edu.berkeley.riselab.rlqopt.opt.CannotEstimateException;
import edu.berkeley.riselab.rlqopt.opt.Cost;
import edu.berkeley.riselab.rlqopt.opt.TableStatisticsModel;
import edu.berkeley.riselab.rlqopt.preopt.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Unit test for simple App. */
public class CostTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public CostTest() {
    super("Test of the Relational Algebra Suite");
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(CostTest.class);
  }

  public void testReductionFactors() throws CannotEstimateException {
    Relation r = new Relation("a", "b", "c");
    AttributeStatistics a = new AttributeStatistics(10, 10, 0, 10);
    TableStatisticsModel t = new TableStatisticsModel();
    t.putStats(r.get("a"), a);
    // Operator scan = createScan(r);

    Expression e1 =
        new Expression(Expression.EQUALS, r.get("a").getExpression(), new Expression("1"));

    assertEquals(0.1, t.estimateReductionFactor(e1), 1e-6);

    Expression e2 = new Expression(Expression.NOT, e1);

    assertEquals(0.9, t.estimateReductionFactor(e2), 1e-6);

    Expression e3 = new Expression(Expression.OR, e1, e2);

    assertEquals(1.0, t.estimateReductionFactor(e3), 1e-6);

    Expression e4 =
        new Expression(
            Expression.GREATER_THAN_EQUALS, r.get("a").getExpression(), new Expression("3"));
    assertEquals(0.7, t.estimateReductionFactor(e4), 1e-6);

    // System.out.println(a.estimateReductionFactor(e));
    // assertEquals(f.apply(j3).params.expression.toString(), "[equals([R294.a, R302.a]),
    // equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]");
  }

  public void testStatisticalCostEstimates1() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    AttributeStatistics a = new AttributeStatistics(10, 10, 0, 10);
    AttributeStatistics b = new AttributeStatistics(10, 16, 0, 10);
    AttributeStatistics c = new AttributeStatistics(10, 19, 0, 10);

    TableStatisticsModel t = new TableStatisticsModel();
    t.putStats(r.get("a"), a);
    t.putStats(r.get("b"), b);
    t.putStats(r.get("c"), c);

    assertEquals(t.estimate(createScan(r)), new Cost(19, 19, 0));

    // System.out.println(a.estimateReductionFactor(e));
    // assertEquals(f.apply(j3).params.expression.toString(), "[equals([R294.a, R302.a]),
    // equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]");
  }

  public void testStatisticalCostEstimates2() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    AttributeStatistics a = new AttributeStatistics(10, 10, 0, 10);
    AttributeStatistics b = new AttributeStatistics(10, 16, 0, 10);
    AttributeStatistics c = new AttributeStatistics(10, 19, 0, 10);

    TableStatisticsModel t = new TableStatisticsModel();
    t.putStats(r.get("a"), a);
    t.putStats(r.get("b"), b);
    t.putStats(r.get("c"), c);

    ExpressionList ea = r.get("a").getExpression().getExpressionList();
    ExpressionList agg = new ExpressionList(new Expression("sum", r.get("b").getExpression()));
    OperatorParameters gb_params = new OperatorParameters(agg, ea);

    // SELECT SUM(b)
    // FROM R
    // GROUP BY a
    Operator gb = new GroupByOperator(gb_params, createScan(r));

    // System.out.println(t.estimate(gb));

    assertEquals(t.estimate(gb), new Cost(38, 10, 0));
  }

  public void testStatisticalCostEstimates3() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Relation s = new Relation("b", "c", "d");
    Relation t = new Relation("e", "d");
    Relation q = new Relation("a", "f", "g");

    /*   JoinOperator j1 = new JoinOperator(createNaturalJoin(r, q), createScan(r), createScan(q));
    JoinOperator j2 = new JoinOperator(createNaturalJoin(s, t), createScan(t), j1);
    JoinOperator j3 = new JoinOperator(createNaturalJoin(s, r), createScan(s), j2);*/

    JoinOperator j1 = new JoinOperator(createNaturalJoin(s, t), createScan(s), createScan(t));
    JoinOperator j2 = new JoinOperator(createNaturalJoin(r, s), createScan(r), j1);
    JoinOperator j3 = new JoinOperator(createNaturalJoin(r, q), createScan(q), j2);

    Expression ex =
        new Expression(
            Expression.AND,
            new Expression(Expression.EQUALS, r.get("a").getExpression(), new Expression("1")),
            new Expression(
                Expression.LESS_THAN_EQUALS, s.get("b").getExpression(), new Expression("2")));

    OperatorParameters sel_params = new OperatorParameters(ex.getExpressionList());
    Operator sel = new SelectOperator(sel_params, j3);

    AttributeStatistics ra = new AttributeStatistics(10, 1000, 0, 10);
    AttributeStatistics rb = new AttributeStatistics(100, 1000, 0, 100);
    AttributeStatistics rc = new AttributeStatistics(10, 1000, 0, 10);

    AttributeStatistics sb = new AttributeStatistics(100, 1600, 0, 100);
    AttributeStatistics sc = new AttributeStatistics(10, 1600, 0, 10);
    AttributeStatistics sd = new AttributeStatistics(10, 1600, 0, 10);

    AttributeStatistics te = new AttributeStatistics(10, 1900, 0, 10);
    AttributeStatistics td = new AttributeStatistics(10, 1900, 0, 10);

    AttributeStatistics qa = new AttributeStatistics(10, 4400, 0, 10);
    AttributeStatistics qf = new AttributeStatistics(10, 4400, 0, 10);
    AttributeStatistics qg = new AttributeStatistics(10, 4400, 0, 10);

    TableStatisticsModel ts = new TableStatisticsModel();
    ts.putStats(r.get("a"), ra);
    ts.putStats(r.get("b"), rb);
    ts.putStats(r.get("c"), rc);
    ts.putStats(s.get("b"), sb);
    ts.putStats(s.get("c"), sc);
    ts.putStats(s.get("d"), sd);
    ts.putStats(t.get("e"), te);
    ts.putStats(t.get("d"), td);
    ts.putStats(q.get("a"), qa);
    ts.putStats(q.get("f"), qf);
    ts.putStats(q.get("g"), qg);

    PreOptimizationRewrite o1 = new CascadedSelect();
    PreOptimizationRewrite o2 = new CorrespondAttributes(r, s, t, q);

    System.out.println(ts.estimate(o2.apply(o1.apply(sel))));

    InitRewrite o = new EagerSelectProject();
    System.out.println(ts.estimate(o.apply(o2.apply(o1.apply(sel)))));

    // System.out.println(ts.estimate(sel));

    // assertEquals(t.estimate(gb), new Cost(38,10,0));

  }

  private Operator createScan(Relation r) throws OperatorException {
    OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
    TableAccessOperator scan_r = new TableAccessOperator(scan_params);
    return scan_r;
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

    return new OperatorParameters(conjunction.getExpressionList());
  }
}
