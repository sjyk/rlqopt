package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.preopt.CascadedSelect;
import edu.berkeley.riselab.rlqopt.preopt.CorrespondAttributes;
import edu.berkeley.riselab.rlqopt.preopt.EagerSelectProject;
import edu.berkeley.riselab.rlqopt.preopt.FlattenJoin;
import java.util.LinkedList;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

// the main planner class
public class LearningPlanner extends Planner {

  public LearningPlanner(Database db) {
    super(new LinkedList(), new LinkedList(), new LinkedList());
    // this.preopt.add(new ExposeProjection());
    this.preopt.add(new CascadedSelect());
    this.preopt.add(new CorrespondAttributes());
    this.preopt.add(new FlattenJoin());
    this.init.add(new EagerSelectProject());
    this.planners.add(new TDJoinExecutor(db));
    this.setPlannerName("learning");
  }

  public void setNetwork(MultiLayerNetwork net) {
    ((TDJoinExecutor) this.planners.get(0)).net = net;
  }

  public void reportInferenceNumEvals() {
    System.out.println(
        "learning # net evals at inference: "
            + ((TDJoinExecutor) this.planners.get(0)).numNetEvals);
  }
}
