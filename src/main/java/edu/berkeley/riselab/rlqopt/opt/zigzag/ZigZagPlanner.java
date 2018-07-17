package edu.berkeley.riselab.rlqopt.opt.zigzag;

import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.preopt.CascadedSelect;
import edu.berkeley.riselab.rlqopt.preopt.CorrespondAttributes;
import edu.berkeley.riselab.rlqopt.preopt.EagerSelectProject;
import edu.berkeley.riselab.rlqopt.preopt.FlattenJoin;
import java.util.LinkedList;

// the main planner class
public class ZigZagPlanner extends Planner {

  public ZigZagPlanner() {
    super(new LinkedList(), new LinkedList(), new LinkedList());
    // this.preopt.add(new ExposeProjection());
    this.preopt.add(new CascadedSelect());
    this.preopt.add(new CorrespondAttributes());
    this.preopt.add(new FlattenJoin());
    this.init.add(new EagerSelectProject());
    this.planners.add(new ZigZagEnumerator());
    this.setPlannerName("zigzag");
    // Hack.
    ((ZigZagEnumerator) (this.planners.get(0))).lfdb.name = this.getPlannerName();
  }
}
