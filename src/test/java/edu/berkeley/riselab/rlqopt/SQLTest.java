package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.relalg.SQL2RelAlg;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.calcite.sql.parser.SqlParseException;

import java.util.Scanner;

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
    Relation sales = new Relation("sku", "emp_id", "amount");
    sales.name = "sales";

    Relation products = new Relation("sku", "price", "comission");
    products.name = "products";

    Relation employees = new Relation("emp_id", "name", "salary");
    employees.name = "employees";

    Database db = new Database(sales, products, employees);

    System.out.println(db);
    //Scanner scanner = new Scanner(System.in);

    SQL2RelAlg s = new SQL2RelAlg(db);
    
    String query = "SELECT * FROM sales join products on sales.sku = products.sku join employees on  sales.emp_id = employees.emp_id";//scanner.nextLine();

    System.out.println("===Input===");
    System.out.println(query);
    System.out.println("===Output===");
    System.out.println(SQL2RelAlg.prettyPrint(s.convert(query).toSQLString()));
  }
}
