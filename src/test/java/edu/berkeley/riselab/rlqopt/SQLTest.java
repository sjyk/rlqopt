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
    Database db = new Database("schematext.sql");

    System.out.println(db);

    // Scanner scanner = new Scanner(System.in);

    SQL2RelAlg s = new SQL2RelAlg(db);

    String query =
        "SELECT MIN(chn.name) AS uncredited_voiced_character, MIN(t.title) AS russian_movie FROM char_name AS chn, cast_info AS ci, company_name AS cn, company_type AS ct, movie_companies AS mc, role_type AS rt, title AS t WHERE ci.note  like '%(voice)%' and ci.note like '%(uncredited)%' AND cn.country_code  = '[ru]' AND rt.role  = 'actor' AND t.production_year > 2005 AND t.id = mc.movie_id AND t.id = ci.movie_id AND ci.movie_id = mc.movie_id AND chn.id = ci.person_role_id AND rt.id = ci.role_id AND cn.id = mc.company_id AND ct.id = mc.company_type_id"; // scanner.nextLine();

    System.out.println("===Input===");
    System.out.println(query);
    System.out.println("===Output===");
    System.out.println(SQL2RelAlg.prettyPrint(s.convert(query).toSQLString()));
  }
}
