package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.experiments.Experiment;
import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.opt.bushy.PostgresBushyPlanner;
import edu.berkeley.riselab.rlqopt.opt.learning.RLQOpt;
import edu.berkeley.riselab.rlqopt.opt.nopt.NoPlanner;
import edu.berkeley.riselab.rlqopt.opt.postgres.PostgresPlanner;
import edu.berkeley.riselab.rlqopt.opt.volcano.VolcanoPlanner;
import edu.berkeley.riselab.rlqopt.relalg.*;
import edu.berkeley.riselab.rlqopt.workload.*;
import java.util.LinkedList;
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

  public void test1() throws OperatorException {

    DatasetGenerator d = new DatasetGenerator(5, 12, 100, 10);
    WorkloadGeneratorEasy workload = new WorkloadGeneratorEasy(d);
    CostModel c = workload.getStatsModel();

    LinkedList<Planner> planners = new LinkedList();
    planners.add(new NoPlanner());
    planners.add(new RLQOpt(workload));
    planners.add(new PostgresBushyPlanner());
    planners.add(new PostgresPlanner());
    planners.add(new VolcanoPlanner());


    Experiment e = new Experiment(workload, 500, 100, planners);
    e.train();
    e.run();
    System.out.println(e.getBaselineImprovement());
    System.out.println(e.getBaselineLatency());
  }
}
