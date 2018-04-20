package edu.berkeley.riselab.rlqopt.opt.bushy;

import edu.berkeley.riselab.rlqopt.opt.*;
import edu.berkeley.riselab.rlqopt.preopt.*;
import java.util.LinkedList;

// the main planner class
public class PostgresBushyPlanner extends Planner {

  public PostgresBushyPlanner() {
    super(new LinkedList(), new LinkedList(), new LinkedList());
    // this.preopt.add(new ExposeProjection());
    this.preopt.add(new CascadedSelect());
    this.preopt.add(new CorrespondAttributes());
    this.preopt.add(new FlattenJoin());
    this.init.add(new EagerSelectProject());
    this.planners.add(new BushyJoinEnumerator());
    this.setPlannerName("postgres-bushy");
  }
}
