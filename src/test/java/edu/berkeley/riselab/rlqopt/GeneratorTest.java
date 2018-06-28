package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import edu.berkeley.riselab.rlqopt.workload.*;
import java.util.Arrays;
import java.util.LinkedList;
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

    String[] cols = {"id", "name", "salary"};
    int[] types = {Attribute.NUMBER, Attribute.STRING, Attribute.NUMBER};
    int[] keys = {0};

    Relation r = new Relation(cols, types, keys);

    String[][] table = {
      {"1", "Peter Parker", "10"}, {"2", "John Doe", "10"}, {"3", "Jane Doe", "10"}
    };
    LinkedList<LinkedList<String>> data = arrayToTable(table);
    MaterializedRelation m = new MaterializedRelation(r.attributesList(), data);

    Expression ea = r.get("id").getExpression();
    Expression eb = new Expression("2");
    Expression equals = new Expression(Expression.EQUALS, eb, ea);
    System.out.println(m);
    System.out.println(m.select(equals));
    System.out.println(m.project(ea.getExpressionList()));
    System.out.println(m.cartesian(m));
  }

  private LinkedList<LinkedList<String>> arrayToTable(String[][] table) {

    LinkedList<LinkedList<String>> result = new LinkedList();
    for (int i = 0; i < table.length; i++) {

      LinkedList<String> record = new LinkedList();

      for (String attr : Arrays.asList(table[i])) {
        record.add(attr);
      }

      result.add(record);
    }

    return result;
  }
}
