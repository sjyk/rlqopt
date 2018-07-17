package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.cost.InMemoryCostModel;
import edu.berkeley.riselab.rlqopt.opt.bushy.PostgresBushyPlanner;
import edu.berkeley.riselab.rlqopt.opt.volcano.VolcanoPlanner;
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
    InMemoryCostModel tm = new InMemoryCostModel(db, "imdb_tables.txt");

    // System.out.println(db);

    // Scanner scanner = new Scanner(System.in);

    SQL2RelAlg s = new SQL2RelAlg(db);

    String query =
        "SELECT MIN(mi.info) AS release_date, MIN(miidx.info) AS rating, MIN(t.title) AS german_movie FROM company_name AS cn, company_type AS ct, info_type AS it, info_type AS it2, kind_type AS kt, movie_companies AS mc, movie_info AS mi, movie_info_idx AS miidx, title AS t WHERE cn.country_code ='[de]' AND ct.kind ='production companies' AND it.info ='rating' AND it2.info ='release dates' AND kt.kind ='movie' AND mi.movie_id = t.id AND it2.id = mi.info_type_id AND kt.id = t.kind_id AND mc.movie_id = t.id AND cn.id = mc.company_id AND ct.id = mc.company_type_id AND miidx.movie_id = t.id AND it.id = miidx.info_type_id AND mi.movie_id = miidx.movie_id AND mi.movie_id = mc.movie_id AND miidx.movie_id = mc.movie_id"; // scanner.nextLine();

    Operator nominal = s.convert(query);
    VolcanoPlanner p = new VolcanoPlanner();
    System.out.println(SQL2RelAlg.prettyPrint(p.plan(nominal, tm).toSQLString()));
    System.out.println(p.getLastPlanStats());

    Operator nominal2 = s.convert(query);
    PostgresBushyPlanner p2 = new PostgresBushyPlanner();
    System.out.println(SQL2RelAlg.prettyPrint(p2.plan(nominal2, tm).toSQLString()));
    System.out.println(p2.getLastPlanStats());

    // System.out.println(p.getLastPlanStats());

    // System.out.println(.getKeys());

    /*System.out.println("===Input===");
    System.out.println(query);
    System.out.println("===Output===");
    System.out.println(SQL2RelAlg.prettyPrint(s.convert(query).toSQLString()));*/
  }
}
