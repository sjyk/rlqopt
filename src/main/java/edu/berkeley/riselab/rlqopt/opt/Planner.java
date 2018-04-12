package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.preopt.InitRewrite;
import edu.berkeley.riselab.rlqopt.preopt.PreOptimizationRewrite;
import java.util.List;

// the main planner class
public class Planner {

  protected List<PreOptimizationRewrite> preopt;
  protected List<InitRewrite> init;
  protected List<PlanningModule> planners;

  PlanningStatistics planStats;

  public Planner(
      List<PreOptimizationRewrite> preopt, List<InitRewrite> init, List<PlanningModule> planners) {

    this.preopt = preopt;
    this.init = init;
    this.planners = planners;
    this.planStats = new PlanningStatistics();
  }

  private Operator initialize(Operator nominal) {

    long now = System.nanoTime();

    for (PreOptimizationRewrite p : preopt) nominal = p.apply(nominal);

    for (InitRewrite p : init) nominal = p.apply(nominal);

    planStats.preprocessing = System.nanoTime() - now;

    return nominal;
  }

  public Operator plan(Operator nominal, CostModel c) {

    //Operator nominal = in.clone();

    if (c != null) planStats.initialCost = c.estimate(nominal).operatorIOcost;

    nominal = initialize(nominal);

    long now = System.nanoTime();

    for (PlanningModule p : planners) nominal = p.apply(nominal, c);

    planStats.planning = System.nanoTime() - now;

    if (c != null) planStats.finalCost = c.estimate(nominal).operatorIOcost;

    return nominal;
  }

  public PlanningStatistics getLastPlanStats() {
    return planStats;
  }

  protected void setPlannerName(String name) {
    planStats.name = name;
  }

  private class PlanningStatistics {

    double preprocessing = 0;
    double planning = 0;
    double initialCost = 0;
    double finalCost;
    String name;

    public String toString() {
      return "{'name': "
          + name
          + ",\n"
          + "'preproc': "
          + (preprocessing / 1e6)
          + ",\n"
          + "'planning: '"
          + (planning / 1e6)
          + ",\n"
          + "'initial: '"
          + initialCost
          + ",\n"
          + "'final: '"
          + finalCost
          + "}";
    }
  }
}
