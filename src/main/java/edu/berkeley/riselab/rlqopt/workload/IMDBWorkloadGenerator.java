package edu.berkeley.riselab.rlqopt.workload;

import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import edu.berkeley.riselab.rlqopt.relalg.SQL2RelAlg;
import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

public class IMDBWorkloadGenerator extends WorkloadGenerator {

  private Database db;
  private CostModel tm;
  private String workloadDir;
  private SQL2RelAlg sqlParser;

  public static final int MEMORY = 0;
  public static final int DISK = 1;
  public static final int MATERIALIZATION = 2;

  public IMDBWorkloadGenerator(String dbschema, String tablestats, String workloadDir, int cm) {
    super();
    db = new Database(dbschema);

    switch (cm) {
      case MEMORY:
        tm = new InMemoryCostModel(db, tablestats);
      case DISK:
        tm = new DiskCostModel(db, tablestats);
    }
    this.workloadDir = workloadDir;
    sqlParser = new SQL2RelAlg(db);
  }

  public Database getDatabase() {
    return db;
  }

  public CostModel getStatsModel() {
    return tm;
  }

  public CostModel getNoisyStatsModel() {
    return getStatsModel();
  }

  public LinkedList<Operator> generateWorkload(int n) throws OperatorException {

    LinkedList<Operator> rtn = new LinkedList();

    // String query =
    //  "SELECT MIN(mi.info) AS release_date, MIN(miidx.info) AS rating, MIN(t.title) AS
    // german_movie FROM company_name AS cn, company_type AS ct, info_type AS it, info_type AS it2,
    // kind_type AS kt, movie_companies AS mc, movie_info AS mi, movie_info_idx AS miidx, title AS t
    // WHERE cn.country_code ='[de]' AND ct.kind ='production companies' AND it.info ='rating' AND
    // it2.info ='release dates' AND kt.kind ='movie' AND mi.movie_id = t.id AND it2.id =
    // mi.info_type_id AND kt.id = t.kind_id AND mc.movie_id = t.id AND cn.id = mc.company_id AND
    // ct.id = mc.company_type_id AND miidx.movie_id = t.id AND it.id = miidx.info_type_id AND
    // mi.movie_id = miidx.movie_id AND mi.movie_id = mc.movie_id AND miidx.movie_id = mc.movie_id";
    // // scanner.nextLine();

    try {

      Scanner scanner = new Scanner(new File(workloadDir));

      while (scanner.hasNextLine() && n > 0) {
        String query = scanner.nextLine().replace(";", "").replace("!=", "<>");
        Operator nominal = sqlParser.convert(query);
        rtn.add(nominal);
        n--;
      }

    } catch (Exception e) {

      e.printStackTrace();
    }
    ;

    // System.out.println(rtn.size());

    return rtn;
  }

  public LinkedList<Operator> copyWorkload(LinkedList<Operator> w) throws OperatorException {

    LinkedList<Operator> workload = new LinkedList();

    for (Operator op : w) workload.add(op.copy());

    return workload;
  }
}
