package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.preopt.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.LinkedList;
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
        f.apply(j3).toString(),
        "KWayJoinOperator([TableAccessOperator([R302.a, R302.f, R302.g]), TableAccessOperator([R201.d, R201.e]), TableAccessOperator([R294.a, R294.b, R294.c]), TableAccessOperator([R297.b, R297.c, R297.d])])");
    assertEquals(
        f.apply(j3).params.expression.toString(),
        "[equals([R294.a, R302.a]), equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]");
  }

  public void testGroupByProjection() throws OperatorException {
    Relation r = new Relation("a", "b", "c");
    Operator scan = createScan(r);

    ExpressionList ea = r.get("a").getExpression().getExpressionList();
    ExpressionList agg = new ExpressionList(new Expression("sum", r.get("b").getExpression()));
    OperatorParameters gb_params = new OperatorParameters(agg, ea);

    Operator gb = new GroupByOperator(gb_params, scan);

    ExposeProjection f = new ExposeProjection();

    assertEquals(f.apply(gb).source.get(0).params.expression.toString(), "[R294.b, R294.a]");
    // assertEquals(f.apply(j3).params.expression.toString(), "[equals([R294.a, R302.a]),
    // equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]");
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
    OperatorParameters gb_params = new OperatorParameters(el);

    Operator sel = new SelectOperator(gb_params, scan);

    CascadedSelect f = new CascadedSelect();
    assertEquals(
        f.apply(sel).toString(),
        "SelectOperator([SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])])])");

    // assertEquals(f.apply(j3).params.expression.toString(), "[equals([R294.a, R302.a]),
    // equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]");
  }

  public void testEagerSelectProject1() throws OperatorException {
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

    OperatorParameters gb_params = new OperatorParameters(e.getExpressionList());
    Operator sel = new SelectOperator(gb_params, j3);

    CascadedSelect o1 = new CascadedSelect();
    EagerSelectProject o2 = new EagerSelectProject();

    assertEquals(
        o2.apply(o1.apply(sel)).toString(),
        "JoinOperator([TableAccessOperator([R302.a, R302.f, R302.g]), JoinOperator([TableAccessOperator([R201.d, R201.e]), JoinOperator([SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])]), SelectOperator([TableAccessOperator([R297.b, R297.c, R297.d])])])])])");
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

    Operator gb = new GroupByOperator(gb_params, sel);

    CascadedSelect o1 = new CascadedSelect();
    ExposeProjection o2 = new ExposeProjection();
    EagerSelectProject o3 = new EagerSelectProject();

    assertEquals(
        o3.apply(o2.apply(o1.apply(gb))).toString(),
        "GroupByOperator([JoinOperator([ProjectOperator([TableAccessOperator([R302.a, R302.f, R302.g])]), JoinOperator([TableAccessOperator([R201.d, R201.e]), JoinOperator([SelectOperator([TableAccessOperator([R294.a, R294.b, R294.c])]), SelectOperator([TableAccessOperator([R297.b, R297.c, R297.d])])])])])])");
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

    PreOptimizationRewrite o1 = new CascadedSelect();
    PreOptimizationRewrite o2 = new ExposeProjection();
    PreOptimizationRewrite o3 = new FlattenJoin();

    LinkedList<PreOptimizationRewrite> l1 = new LinkedList();
    l1.add(o1);
    l1.add(o2);
    l1.add(o3);

    InitRewrite o4 = new EagerSelectProject();

    LinkedList<InitRewrite> l2 = new LinkedList();
    l2.add(o4);

    Planner p = new Planner(l1, l2);
    p.plan(gb);
  }

  private Operator createScan(Relation r) throws OperatorException {
    OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
    TableAccessOperator scan_r = new TableAccessOperator(scan_params);
    return scan_r;
  }

  private OperatorParameters createNaturalJoin(Relation r, Relation s) throws OperatorException {
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
