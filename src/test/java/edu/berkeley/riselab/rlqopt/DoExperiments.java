package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.experiments.Experiment;
import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.opt.bushy.PostgresBushyPlanner;
import edu.berkeley.riselab.rlqopt.opt.learning.RLQOpt;
import edu.berkeley.riselab.rlqopt.opt.nopt.NoPlanner;
import edu.berkeley.riselab.rlqopt.opt.postgres.PostgresPlanner;
import edu.berkeley.riselab.rlqopt.opt.quickpick.QuickPickPlanner;
import edu.berkeley.riselab.rlqopt.opt.volcano.VolcanoPlanner;
import edu.berkeley.riselab.rlqopt.workload.DatasetGenerator;
import edu.berkeley.riselab.rlqopt.workload.IMDBWorkloadGenerator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.calcite.sql.parser.SqlParseException;

/** Unit test for simple App. */
public class DoExperiments extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public DoExperiments() {
    super("Test of the Relational Algebra Suite");
  }

  /** @return the suite of tests being tested */
  public static Test suite() {
    return new TestSuite(DoExperiments.class);
  }

  private void printSorted(Map<Planner, Double> map) {
    Map<String, Double> treeMap = new TreeMap<>();
    for (Map.Entry<Planner, Double> entry : map.entrySet()) {
      treeMap.put(entry.getKey().toString(), entry.getValue());
    }
    System.out.println(treeMap);
  }

  /** Write or append to a .csv file. */
  private void writeLatencyCsv(
      String csvPath, DatasetGenerator d, List<Planner> planners, Map<Planner, Double> latencies) {

    List<String> plannerNames =
        planners.stream().map(Planner::getPlannerName).collect(Collectors.toList());
    String allPlanners = String.join(",", plannerNames);
    System.out.println("allPlanners = " + allPlanners);
    String schema = "num_relations,num_total_attributes," + allPlanners + "\n";

    try {
      if (!Files.exists(Paths.get(csvPath))) {
        Files.write(
            Paths.get(csvPath),
            schema.getBytes("utf8"),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
      }
      StringBuilder sb = new StringBuilder();
      sb.append(d.numRelations());
      sb.append(',');
      sb.append(d.numTotalAttributes());
      sb.append(',');
      for (int i = 0; i < planners.size(); ++i) {
        sb.append(latencies.get(planners.get(i)));
        if (i != planners.size() - 1) sb.append(',');
      }
      sb.append('\n');

      Files.write(Paths.get(csvPath), sb.toString().getBytes("utf8"), StandardOpenOption.APPEND);
    } catch (Exception exception) {
      System.out.println(exception.toString());
    }
  }

  /*public void test1() throws OperatorException {
    int numRels = 9;
    if (System.getProperty("numRels") != null) {
      numRels = Integer.valueOf(System.getProperty("numRels"));
    }

    DatasetGenerator d = new DatasetGenerator(numRels, 12, 1000, 100);
    WorkloadGeneratorEasy workload = new WorkloadGeneratorEasy(d);

    LinkedList<Planner> planners = new LinkedList<>();
    planners.add(new NoPlanner());
    planners.add(new RLQOpt(workload));
    planners.add(new PostgresBushyPlanner());
    planners.add(new PostgresPlanner());
    planners.add(new VolcanoPlanner());
    planners.add(new QuickPickPlanner(100));

    Experiment e = new Experiment(workload, 1000, 1000, planners);
    e.run();

    d.describe();
    System.out.print("Improvement: ");
    printSorted(e.getBaselineImprovement());
    System.out.print("Planning latency: ");
    Map<Planner, Double> latencies = e.getBaselineLatency();
    printSorted(latencies);

    writeLatencyCsv("planning_latencies.csv", d, planners, latencies);
  }*/

  public void test2() throws OperatorException, SqlParseException {
    IMDBWorkloadGenerator workload =
        new IMDBWorkloadGenerator(
            "schematext.sql", "imdb_tables.txt", "join-order-benchmark/queries/queries.sql");

    LinkedList<Planner> planners = new LinkedList<>();
    planners.add(new NoPlanner());
    planners.add(new RLQOpt(workload));
    planners.add(new PostgresBushyPlanner());
    planners.add(new PostgresPlanner());
    planners.add(new VolcanoPlanner());
    planners.add(new QuickPickPlanner(100));

    Experiment e = new Experiment(workload, 15, 15, planners);
    e.train();
    e.run();

    System.out.print("Improvement: ");
    printSorted(e.getBaselineImprovement());
    System.out.print("Planning latency: ");
    Map<Planner, Double> latencies = e.getBaselineLatency();
    printSorted(latencies);
  }
}
