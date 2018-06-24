package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.experiments.Experiment;
import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.opt.bushy.PostgresBushyPlanner;
import edu.berkeley.riselab.rlqopt.opt.learning.RLQOpt;
import edu.berkeley.riselab.rlqopt.opt.nopt.NoPlanner;
import edu.berkeley.riselab.rlqopt.opt.postgres.PostgresPlanner;
import edu.berkeley.riselab.rlqopt.opt.quickpick.QuickPickPlanner;
import edu.berkeley.riselab.rlqopt.opt.volcano.VolcanoPlanner;
import edu.berkeley.riselab.rlqopt.workload.DatasetGenerator;
import edu.berkeley.riselab.rlqopt.workload.WorkloadGeneratorEasy;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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

  public void test1() throws OperatorException {
    DatasetGenerator d = new DatasetGenerator(5, 12, 1000, 100);
    // DatasetGenerator d = new DatasetGenerator(3, 8, 1000, 100);
    WorkloadGeneratorEasy workload = new WorkloadGeneratorEasy(d);
    CostModel c = workload.getStatsModel();

    // System.out.println("here");

    LinkedList<Planner> planners = new LinkedList();
    planners.add(new NoPlanner());
//    planners.add(new RLQOpt(workload));
    planners.add(new PostgresBushyPlanner());
    planners.add(new PostgresPlanner());
    planners.add(new VolcanoPlanner());
    planners.add(new QuickPickPlanner(100));

    Experiment e = new Experiment(workload, 1000, 1000, planners);
    e.train();
    e.run();

    d.describe();
    System.out.print("Improvement: ");
    printSorted(e.getBaselineImprovement());
    System.out.print("Planning latency: ");
    printSorted(e.getBaselineLatency());
  }
}
