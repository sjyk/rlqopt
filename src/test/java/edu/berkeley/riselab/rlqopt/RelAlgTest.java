package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.relalg.JoinOperator;
import edu.berkeley.riselab.rlqopt.relalg.ProjectOperator;
import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Unit test for simple App. */
public class RelAlgTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public RelAlgTest() {
    super("Test of the Relational Algebra Suite");
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(RelAlgTest.class);
  }

  /** */
  public void testRelAlgComposition() throws OperatorException {
    Relation s = new Relation("a", "b", "c");

    OperatorParameters scan_params = new OperatorParameters(s.getExpressionList());
    TableAccessOperator r = new TableAccessOperator(scan_params);

    assertEquals(r.toString(), "TableAccessOperator([R294.a, R294.b, R294.c])");

    OperatorParameters select_params = new OperatorParameters(s.getExpressionList());
    ProjectOperator sel = new ProjectOperator(select_params, r);

    assertEquals(
        sel.toString(), "ProjectOperator([TableAccessOperator([R294.a, R294.b, R294.c])])");

    OperatorParameters join_params =
        new OperatorParameters(
            new Expression(
                    Expression.EQUALS, s.get("a").getExpression(), s.get("b").getExpression())
                .getExpressionList());
    JoinOperator j = new JoinOperator(join_params, sel, r);
    // System.out.println(j);
    assertEquals(
        j.toString(),
        "JoinOperator([ProjectOperator([TableAccessOperator([R294.a, R294.b, R294.c])]), TableAccessOperator([R294.a, R294.b, R294.c])])");
  }

  public void testExpressionComposition() throws OperatorException {
    Relation s = new Relation("a", "b", "c");
    Attribute a = s.get("a");
    assertEquals(a.toString(), "R294.a");

    Expression ea = a.getExpression();
    Expression eb = s.get("b").getExpression();
    Expression equals = new Expression(Expression.EQUALS, ea, eb);
    assertEquals(equals.toString(), "equals([R294.a, R294.b])");
  }

  public void testExpressionAttribute() throws OperatorException {
    Relation s = new Relation("a", "b", "c");
    Attribute a = s.get("a");
    assertEquals(a.toString(), "R294.a");

    Expression ea = a.getExpression();
    Expression eb = s.get("b").getExpression();
    System.out.println(eb.getVisibleAttributes());
    // fix
    // assertEquals(a.toString(), "R294.a");
  }

  public void testOperatorAttribute() throws OperatorException {
    Relation s = new Relation("a", "b", "c");

    OperatorParameters scan_params = new OperatorParameters(s.getExpressionList());
    TableAccessOperator r = new TableAccessOperator(scan_params);
    OperatorParameters select_params = new OperatorParameters(s.getExpressionList());
    ProjectOperator sel = new ProjectOperator(select_params, r);
    OperatorParameters join_params =
        new OperatorParameters(
            new Expression(
                    Expression.EQUALS, s.get("a").getExpression(), s.get("b").getExpression())
                .getExpressionList());
    JoinOperator j = new JoinOperator(join_params, sel, r);
    System.out.println(j.getVisibleAttributes());
    // fix
    // assertEquals(, "JoinOperator([ProjectOperator([TableAccessOperator([R294.a, R294.b,
    // R294.c])]), TableAccessOperator([R294.a, R294.b, R294.c])])");
  }
}
