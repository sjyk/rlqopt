package edu.berkeley.riselab.rlqopt.opt.postgres;

import edu.berkeley.riselab.rlqopt.opt.*;
import edu.berkeley.riselab.rlqopt.preopt.*;
import java.util.LinkedList;

// the main planner class
public class PostgresPlanner extends Planner {

  public PostgresPlanner() {
    super(new LinkedList(), new LinkedList(), new LinkedList());
    this.preopt.add(new ExposeProjection());
    this.preopt.add(new CascadedSelect());
    this.preopt.add(new FlattenJoin());
    this.init.add(new EagerSelectProject());
    this.planners.add(new LeftDeepJoinReorder());
    this.setPlannerName("postgres");
  }
}
