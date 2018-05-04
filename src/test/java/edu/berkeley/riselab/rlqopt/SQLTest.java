package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.relalg.SQL2RelAlg;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.calcite.sql.parser.SqlParseException;

/** Unit test for simple App. */
public class SQLTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public SQLTest() {
    super("Test of the Relational Algebra Suite");
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(SQLTest.class);
  }

  public void test1() throws SqlParseException {
    Relation bear = new Relation("a", "b", "c");
    bear.name = "bear";

    Relation fred = new Relation("c", "d", "e");
    fred.name = "fred";

    Relation dog = new Relation("q", "l", "t");
    dog.name = "dog";

    Database db = new Database(bear, fred, dog);

    // System.out.println(bear);

    SQL2RelAlg s = new SQL2RelAlg(db);
    // s.convert("select a from bear join fred on bear.l = fred.b");
    // s.convert("select a from bear where a = 7 AND b = 4 AND  c = 1");
    // s.convert("select a from bear where bear.a = 7");
    System.out.println(
        s.convert(
            "select sum(bear.a) from bear join fred on bear.a = fred.c AND bear.c = fred.c join dog on bear.a = dog.a  where bear.a > 7 AND bear.a = 4 group by bear.b").toSQLString());
  }
}
