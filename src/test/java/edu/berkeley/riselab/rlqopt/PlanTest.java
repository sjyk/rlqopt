package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.opt.AttributeStatistics;
import edu.berkeley.riselab.rlqopt.opt.CannotEstimateException;
import edu.berkeley.riselab.rlqopt.opt.Cost;
import edu.berkeley.riselab.rlqopt.opt.TableStatisticsModel;
import edu.berkeley.riselab.rlqopt.opt.LeftDeepJoinReorder;
import edu.berkeley.riselab.rlqopt.preopt.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Unit test for simple App. */
public class PlanTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public PlanTest() {
    super("Test of the Relational Algebra Suite");
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(PlanTest.class);
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

    FlattenJoin f = new FlattenJoin();
    LeftDeepJoinReorder l = new LeftDeepJoinReorder(false);
    Operator optimized = f.apply(j3);

    System.out.println(ts.estimate(l.apply(optimized, ts)));
    System.out.println(ts.estimate(j3));


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
