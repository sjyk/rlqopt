package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.opt.postgres.PostgresPlanner;
import edu.berkeley.riselab.rlqopt.relalg.*;
import edu.berkeley.riselab.rlqopt.workload.*;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Unit test for simple App. */
public class GeneratorTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public GeneratorTest() {
    super("Test of the Relational Algebra Suite");
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(GeneratorTest.class);
  }

  public void test1() throws OperatorException {

    DatasetGenerator d = new DatasetGenerator(5, 10, 10000, 100);
    WorkloadGeneratorEasy workload = new WorkloadGeneratorEasy(d);
    CostModel c = workload.getStatsModel();

    PostgresPlanner post = new PostgresPlanner();

    for (Operator q : workload.generateWorkload(100)) {
      post.plan(q, c);
      System.out.println(post.getLastPlanStats());
    }
  }

  public void test2() throws OperatorException {

    DatasetGenerator d = new DatasetGenerator(5, 10, 10000, 10);
    Relation r = d.generateRelation();
    HistogramRelation h = new HistogramRelation(d.generateData(r));
    System.out.println(h);
    System.out.println(h.count());

    Attribute a = r.first();

    Expression ea = a.getExpression();
    Expression eb = new Expression("3");
    Expression equals = new Expression(Expression.LESS_THAN, ea, eb);

    System.out.println(HistogramOperations.cartesian(h, h));
  }

  public void test3() throws OperatorException {

    DatasetGenerator d = new DatasetGenerator(5, 10, 10000, 10);
    Relation r = d.generateRelation();
    Relation s = d.generateRelation();

    HistogramRelation hr = new HistogramRelation(d.generateData(r));
    HistogramRelation hs = new HistogramRelation(d.generateData(s));
    HistogramRelation h = HistogramOperations.merge(hr, hs);

    OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
    TableAccessOperator scan_r = new TableAccessOperator(scan_params);

    OperatorParameters scan_paramss = new OperatorParameters(s.getExpressionList());
    TableAccessOperator scan_s = new TableAccessOperator(scan_paramss);

    OperatorParameters params = new OperatorParameters(new ExpressionList());
    CartesianOperator c = new CartesianOperator(params, scan_r, scan_s);

    System.out.println(HistogramOperations.eval(h, scan_s).count());
    System.out.println(HistogramOperations.eval(h, scan_r).count());
    System.out.println(HistogramOperations.eval(h, c).count());

    Iterator<Attribute> attrSetIter = r.attributes().iterator();
    Attribute a = attrSetIter.next();

    Expression ea = a.getExpression();
    Expression eb = new Expression("1");
    Expression equals = new Expression(Expression.LESS_THAN, ea, eb);
    OperatorParameters select_params = new OperatorParameters(equals.getExpressionList());
    SelectOperator sel_r = new SelectOperator(select_params, c);
    System.out.println(HistogramOperations.eval(h, sel_r).count());
  }
}
