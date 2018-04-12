package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.preopt.CascadedSelect;
import edu.berkeley.riselab.rlqopt.preopt.EagerSelectProject;
import edu.berkeley.riselab.rlqopt.preopt.ExposeProjection;
import edu.berkeley.riselab.rlqopt.preopt.FlattenJoin;
import edu.berkeley.riselab.rlqopt.preopt.InitRewrite;
import edu.berkeley.riselab.rlqopt.preopt.PreOptimizationRewrite;
import edu.berkeley.riselab.rlqopt.relalg.GroupByOperator;
import edu.berkeley.riselab.rlqopt.relalg.JoinOperator;
import edu.berkeley.riselab.rlqopt.relalg.SelectOperator;
import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Unit test for simple App. */
public class PreoptTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public PreoptTest() {
    super("Test of the Relational Algebra Suite");
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(PreoptTest.class);
  }

  /** */
  public void testJoinFlattening() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Relation s = new Relation("b", "c", "d");
    Relation t = new Relation("e", "d");
    Relation q = new Relation("a", "f", "g");

    JoinOperator j1 = new JoinOperator(createNaturalJoin(r, s), createScan(r), createScan(s));
    JoinOperator j2 = new JoinOperator(createNaturalJoin(s, t), createScan(t), j1);
    JoinOperator j3 = new JoinOperator(createNaturalJoin(r, q), createScan(q), j2);

    FlattenJoin f = new FlattenJoin();

    assertEquals(
        "KWayJoinOperator([TableAccessOperator([R302.a, R302.f, R302.g]), TableAccessOperator([R201.d, R201.e]), TableAccessOperator([R294.a, R294.b, R294.c]), TableAccessOperator([R297.b, R297.c, R297.d])])",
        f.apply(j3).toString());
    assertEquals(
        "[equals([R294.a, R302.a]), equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]",
        f.apply(j3).params.expression.toString());
  }

  public void testGroupByProjection() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Operator scan = createScan(r);

    ExpressionList ea = r.get("a").getExpression().getExpressionList();
    ExpressionList agg = new ExpressionList(new Expression("sum", r.get("b").getExpression()));
    OperatorParameters gb_params = new OperatorParameters(agg, ea);

    // SELECT SUM(b)
    // FROM R
    // GROUP BY a
    Operator gb = new GroupByOperator(gb_params, scan);
    assertEquals("GroupByOperator([TableAccessOperator([R294.a, R294.b, R294.c])])", gb.toString());
    assertEquals("[R294.a, R294.b, R294.c]", gb.source.get(0).params.expression.toString());
    assertEquals("[sum([R294.b])]", gb.params.expression.toString());
    assertEquals("[R294.a]", gb.params.secondary_expression.toString());

    // Insert a projection that should prune away attribute "c".
    ExposeProjection f = new ExposeProjection();
    Operator transformed = f.apply(gb);

    assertEquals(
        "GroupByOperator([ProjectOperator([TableAccessOperator([R294.a, R294.b, R294.c])])])",
        transformed.toString());
    assertEquals("[R294.b, R294.a]", transformed.source.get(0).params.expression.toString());
    assertEquals("[sum([R294.b])]", transformed.params.expression.toString());
    assertEquals("[R294.a]", transformed.params.secondary_expression.toString());
  }

  public void testCascadedSelect() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Operator scan = createScan(r);

    Expression e =
        new Expression(
            Expression.AND,
            new Expression(Expression.EQUALS, r.get("a").getExpression(), new Expression("1")),
            new Expression(Expression.EQUALS, r.get("b").getExpression(), new Expression("2")));

    ExpressionList el = e.getExpressionList();
    OperatorParameters params = new OperatorParameters(el);

    Operator sel = new SelectOperator(params, scan);
    assertEquals("SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])])", sel.toString());
    assertEquals(
        "[and([equals([R294.a, 1([])]), equals([R294.b, 2([])])])]",
        sel.params.expression.toString());

    CascadedSelect f = new CascadedSelect();
    Operator transformed = f.apply(sel);
    assertEquals(
        "SelectOperator([SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])])])",
        transformed.toString());
    assertEquals("[equals([R294.b, 2([])])]", transformed.params.expression.toString());
    assertEquals(
        "[equals([R294.a, 1([])])]", transformed.source.get(0).params.expression.toString());
  }

  public void testEagerSelectProject1() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Relation s = new Relation("b", "c", "d");
    Relation t = new Relation("e", "d");
    Relation q = new Relation("a", "f", "g");

    JoinOperator j1 = new JoinOperator(createNaturalJoin(r, s), createScan(r), createScan(s));
    JoinOperator j2 = new JoinOperator(createNaturalJoin(s, t), createScan(t), j1);
    JoinOperator j3 = new JoinOperator(createNaturalJoin(r, q), createScan(q), j2);
    // JoinOperator([
    //   TableAccessOperator([R302.a, R302.f, R302.g]),
    //   JoinOperator([
    //     TableAccessOperator([R201.d, R201.e]),
    //     JoinOperator([
    //       TableAccessOperator([R294.a, R294.b, R294.c]),       // R.
    //       TableAccessOperator([R297.b, R297.c, R297.d])])])])  // S.
    //    System.out.println(j3.toString());

    Expression e =
        new Expression(
            Expression.AND,
            new Expression(Expression.EQUALS, r.get("a").getExpression(), new Expression("1")),
            new Expression(Expression.EQUALS, s.get("b").getExpression(), new Expression("2")));

    OperatorParameters gb_params = new OperatorParameters(e.getExpressionList());
    // SELECT *
    // FROM R, S, T, Q
    // WHERE R.a = 1 AND S.b = 2 AND <join conditions>
    Operator sel = new SelectOperator(gb_params, j3);

    // Break "Select(a = 1, b = 2)" into two singleton Selects.
    CascadedSelect o1 = new CascadedSelect();
    // Push down the two singleton Selects.
    EagerSelectProject o2 = new EagerSelectProject();

    // JoinOperator([
    //   TableAccessOperator([R302.a, R302.f, R302.g]),
    //   JoinOperator([
    //     TableAccessOperator([R201.d, R201.e]),
    //     JoinOperator([
    //       SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])]),       // R.a = 1
    //       SelectOperator([TableAccessOperator([R297.b, R297.c, R297.d])])])])])  // S.b = 2
    Operator transformed = o2.apply(o1.apply(sel));
    assertEquals(
        "JoinOperator([TableAccessOperator([R302.a, R302.f, R302.g]), JoinOperator([TableAccessOperator([R201.d, R201.e]), JoinOperator([SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])]), SelectOperator([TableAccessOperator([R297.b, R297.c, R297.d])])])])])",
        transformed.toString());
  }

  public void testEagerSelectProject2() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Relation s = new Relation("b", "c", "d");
    Relation t = new Relation("e", "d");
    Relation q = new Relation("a", "f", "g");

    JoinOperator j1 = new JoinOperator(createNaturalJoin(r, s), createScan(r), createScan(s));
    JoinOperator j2 = new JoinOperator(createNaturalJoin(s, t), createScan(t), j1);
    JoinOperator j3 = new JoinOperator(createNaturalJoin(r, q), createScan(q), j2);

    Expression e =
        new Expression(
            Expression.AND,
            new Expression(Expression.EQUALS, r.get("a").getExpression(), new Expression("1")),
            new Expression(Expression.EQUALS, s.get("b").getExpression(), new Expression("2")));

    OperatorParameters sel_params = new OperatorParameters(e.getExpressionList());
    Operator sel = new SelectOperator(sel_params, j3);

    ExpressionList ea = q.get("g").getExpression().getExpressionList();
    ExpressionList agg = new ExpressionList(new Expression("sum", q.get("f").getExpression()));
    OperatorParameters gb_params = new OperatorParameters(agg, ea);

    // SELECT SUM(Q.f)
    // FROM R, S, T, Q
    // WHERE R.a = 1 AND S.b = 2 AND <join conditions>
    // GROUP BY Q.g
    Operator gb = new GroupByOperator(gb_params, sel);

    CascadedSelect o1 = new CascadedSelect();
    ExposeProjection o2 = new ExposeProjection();
    EagerSelectProject o3 = new EagerSelectProject();

    // Cascaded Select -> Expose Projection
    // GroupByOperator([
    //   ProjectOperator([  // Only references Q.f, Q.g.
    //     SelectOperator([
    //       SelectOperator([
    //         JoinOperator([
    //           TableAccessOperator([R302.a, R302.f, R302.g]),
    //           JoinOperator([
    //             TableAccessOperator([R201.d, R201.e]),
    //             JoinOperator([
    //               TableAccessOperator([R294.a, R294.b, R294.c]),
    //               TableAccessOperator([R297.b, R297.c, R297.d])])])])])])])])
    //    System.out.println(o2.apply(o1.apply(gb)).toString());

    // Cascaded Select -> Expose Projection -> Eager Select Project
    // GroupByOperator([
    //   JoinOperator([
    //     ProjectOperator([TableAccessOperator([R302.a, R302.f, R302.g])]),  //  f, g
    //     JoinOperator([
    //       TableAccessOperator([R201.d, R201.e]),
    //       JoinOperator([
    //         SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])]),
    //         SelectOperator([TableAccessOperator([R297.b, R297.c, R297.d])])])])])])
    Operator transformed = o3.apply(o2.apply(o1.apply(gb)));
    System.out.println(transformed);
    System.out.println(transformed.source.get(0).source.get(0).params.toString());
    assertEquals(
        "GroupByOperator([JoinOperator([ProjectOperator([TableAccessOperator([R302.a, R302.f, R302.g])]), JoinOperator([TableAccessOperator([R201.d, R201.e]), JoinOperator([SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])]), SelectOperator([TableAccessOperator([R297.b, R297.c, R297.d])])])])])])",
        transformed.toString());
  }

  public void testPlannerInit() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Relation s = new Relation("b", "c", "d");
    Relation t = new Relation("e", "d");
    Relation q = new Relation("a", "f", "g");

    JoinOperator j1 = new JoinOperator(createNaturalJoin(r, s), createScan(r), createScan(s));
    JoinOperator j2 = new JoinOperator(createNaturalJoin(s, t), createScan(t), j1);
    JoinOperator j3 = new JoinOperator(createNaturalJoin(r, q), createScan(q), j2);

    Expression e =
        new Expression(
            Expression.AND,
            new Expression(Expression.EQUALS, r.get("a").getExpression(), new Expression("1")),
            new Expression(Expression.EQUALS, s.get("b").getExpression(), new Expression("2")));

    OperatorParameters sel_params = new OperatorParameters(e.getExpressionList());
    Operator sel = new SelectOperator(sel_params, j3);

    ExpressionList ea = q.get("g").getExpression().getExpressionList();
    ExpressionList agg = new ExpressionList(new Expression("sum", q.get("f").getExpression()));
    OperatorParameters gb_params = new OperatorParameters(agg, ea);

    Operator gb = new GroupByOperator(gb_params, sel);
    String originalPlan = gb.toString();

    List<PreOptimizationRewrite> l1 =
        Arrays.asList(new CascadedSelect(), new ExposeProjection(), new FlattenJoin());
    List<InitRewrite> l2 = Collections.singletonList(new EagerSelectProject());

    Planner p = new Planner(l1, l2, new LinkedList());
    p.plan(gb, null);

    // NOTE(zongheng): currently the transformation rules are side-effectful, so we save the
    // original plan's string representation for comparison.
    //    assertTrue(!originalPlan.equals(planned.toString()));
  }

  private Operator createScan(Relation r) throws OperatorException {
    OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
    return new TableAccessOperator(scan_params);
  }

  /** Conjunct all attributes that "r" and "s" have in common. */
  private OperatorParameters createNaturalJoin(Relation r, Relation s) {
    Expression conjunction = null;

    for (Expression er : r.getExpressionList()) {

      for (Expression es : s.getExpressionList()) {

        if (er.noop.attribute.equals(es.noop.attribute)) {
          Expression clause = new Expression(Expression.EQUALS, er, es);

          if (conjunction == null) {
            conjunction = clause;
          } else {
            conjunction = new Expression(Expression.AND, clause, conjunction);
          }
        }
      }
    }

    return new OperatorParameters(conjunction.getExpressionList());
  }
}
