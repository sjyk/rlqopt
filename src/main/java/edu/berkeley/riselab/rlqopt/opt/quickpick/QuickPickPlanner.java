package edu.berkeley.riselab.rlqopt.opt.quickpick;

import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.preopt.CascadedSelect;
import edu.berkeley.riselab.rlqopt.preopt.CorrespondAttributes;
import edu.berkeley.riselab.rlqopt.preopt.EagerSelectProject;
import edu.berkeley.riselab.rlqopt.preopt.FlattenJoin;
import java.util.LinkedList;

public class QuickPickPlanner extends Planner {

  public QuickPickPlanner(int numTrajectories) {
    super(new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
    // this.preopt.add(new ExposeProjection());
    this.preopt.add(new CascadedSelect());
    this.preopt.add(new CorrespondAttributes());
    this.preopt.add(new FlattenJoin());
    this.init.add(new EagerSelectProject());
    this.planners.add(new QuickPick(numTrajectories));
    this.setPlannerName("quickpick-" + numTrajectories);
  }
}
