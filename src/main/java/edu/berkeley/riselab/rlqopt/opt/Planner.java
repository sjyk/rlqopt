package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.preopt.InitRewrite;
import edu.berkeley.riselab.rlqopt.preopt.PreOptimizationRewrite;
import java.util.List;

// the main planner class
public class Planner {

  protected List<PreOptimizationRewrite> preopt;
  protected List<InitRewrite> init;
  protected List<PlanningModule> planners;
  String name;

  PlanningStatistics planStats;

  public Planner(
      List<PreOptimizationRewrite> preopt, List<InitRewrite> init, List<PlanningModule> planners) {

    this.preopt = preopt;
    this.init = init;
    this.planners = planners;
  }

  private Operator initialize(Operator nominal) {

    long now = System.nanoTime();

    for (PreOptimizationRewrite p : preopt) nominal = p.apply(nominal);

    for (InitRewrite p : init) nominal = p.apply(nominal);

    planStats.preprocessing = System.nanoTime() - now;

    return nominal;
  }

  public Operator plan(Operator nominal, CostModel c) {
    return this.plan(nominal, c, c);
  }

  public Operator plan(Operator nominal, CostModel internal, CostModel actual) {

    this.planStats = new PlanningStatistics();
    planStats.name = this.name;

    // Operator nominal = in.clone();

    if (actual != null) planStats.initialCost = actual.estimate(nominal).operatorIOcost;

    nominal = initialize(nominal);

    long now = System.nanoTime();

    for (PlanningModule p : planners) nominal = p.apply(nominal, internal);

    planStats.planning = System.nanoTime() - now;

    if (actual != null) planStats.finalCost = actual.estimate(nominal).operatorIOcost;

    return nominal;
  }

  public PlanningStatistics getLastPlanStats() {
    return planStats;
  }

  protected void setPlannerName(String name) {
    this.name = name;
  }
}
