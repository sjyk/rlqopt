package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.preopt.InitRewrite;
import edu.berkeley.riselab.rlqopt.preopt.PreOptimizationRewrite;
import java.util.List;

// the main planner class
public class Planner {

  List<PreOptimizationRewrite> preopt;
  List<InitRewrite> init;

  public Planner(List<PreOptimizationRewrite> preopt, List<InitRewrite> init) {

    this.preopt = preopt;
    this.init = init;
  }

  private Operator initialize(Operator nominal) {

    // long now = System.currentTimeMillis();

    for (PreOptimizationRewrite p : preopt) nominal = p.apply(nominal);

    for (InitRewrite p : init) nominal = p.apply(nominal);

    // System.out.println("Init duration: " + (System.currentTimeMillis() - now) + " ms");

    return nominal;
  }

  public Operator plan(Operator nominal) {

    nominal = initialize(nominal);

    return nominal;
  }
}
