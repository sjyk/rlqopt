package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.opt.AttributeStatistics;
import edu.berkeley.riselab.rlqopt.opt.CannotEstimateException;
import edu.berkeley.riselab.rlqopt.opt.Cost;
import edu.berkeley.riselab.rlqopt.opt.TableStatisticsModel;
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
    AttributeStatistics a = new AttributeStatistics(10, 0, 10);
    // Operator scan = createScan(r);

    Expression e1 =
        new Expression(Expression.EQUALS, r.get("a").getExpression(), new Expression("1"));

    assertEquals(0.1, a.estimateReductionFactor(e1), 1e-6);

    Expression e2 = new Expression(Expression.NOT, e1);

    assertEquals(0.9, a.estimateReductionFactor(e2), 1e-6);

    Expression e3 = new Expression(Expression.OR, e1, e2);

    assertEquals(1.0, a.estimateReductionFactor(e3), 1e-6);

    Expression e4 =
        new Expression(
            Expression.GREATER_THAN_EQUALS, r.get("a").getExpression(), new Expression("3"));
    assertEquals(0.7, a.estimateReductionFactor(e4), 1e-6);

    // System.out.println(a.estimateReductionFactor(e));
    // assertEquals(f.apply(j3).params.expression.toString(), "[equals([R294.a, R302.a]),
    // equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]");
  }

  public void testStatisticalCostEstimates1() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    AttributeStatistics a = new AttributeStatistics(10, 0, 10);
    AttributeStatistics b = new AttributeStatistics(16, 0, 10);
    AttributeStatistics c = new AttributeStatistics(19, 0, 10);

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
    AttributeStatistics a = new AttributeStatistics(10, 0, 10);
    AttributeStatistics b = new AttributeStatistics(16, 0, 10);
    AttributeStatistics c = new AttributeStatistics(19, 0, 10);

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
    AttributeStatistics a = new AttributeStatistics(10, 0, 10);
    AttributeStatistics b = new AttributeStatistics(16, 0, 10);
    AttributeStatistics c = new AttributeStatistics(19, 0, 10);

    TableStatisticsModel t = new TableStatisticsModel();
    t.putStats(r.get("a"), a);
    t.putStats(r.get("b"), b);
    t.putStats(r.get("c"), c);

    Expression e =
        new Expression(
            Expression.GREATER_THAN_EQUALS, r.get("a").getExpression(), new Expression("2"));

    ExpressionList el = e.getExpressionList();
    OperatorParameters params = new OperatorParameters(el);

    Operator sel = new SelectOperator(params, createScan(r));

    System.out.println(t.estimate(sel));

    // assertEquals(t.estimate(gb), new Cost(38,10,0));

  }

  private Operator createScan(Relation r) throws OperatorException {
    OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
    TableAccessOperator scan_r = new TableAccessOperator(scan_params);
    return scan_r;
  }
}
