package edu.berkeley.riselab.rlqopt.opt.ikkbz;

import edu.berkeley.riselab.rlqopt.opt.*;
import edu.berkeley.riselab.rlqopt.preopt.*;
import java.util.LinkedList;

// the main planner class
public class IKKBZPlanner extends Planner {

  public IKKBZPlanner() {
    super(new LinkedList(), new LinkedList(), new LinkedList());
    // this.preopt.add(new ExposeProjection());
    this.preopt.add(new CascadedSelect());
    this.preopt.add(new CorrespondAttributes());
    this.preopt.add(new FlattenJoin());
    this.init.add(new EagerSelectProject());
    this.planners.add(new IKKBZ());
    this.setPlannerName("ik-kbz");
  }
}
