package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.opt.*;
import edu.berkeley.riselab.rlqopt.preopt.*;
import java.util.LinkedList;
import java.util.HashMap;
import edu.berkeley.riselab.rlqopt.Operator;

// the main planner class
public class LearningPlanner extends Planner {

  public LearningPlanner() {
    super(new LinkedList(), new LinkedList(), new LinkedList());
    //this.preopt.add(new ExposeProjection()); 
    this.preopt.add(new CascadedSelect());
    this.preopt.add(new CorrespondAttributes());
    this.preopt.add(new FlattenJoin());
    this.init.add(new EagerSelectProject());
    this.planners.add(new TDJoinSampler(10000));
    this.setPlannerName("learning");
  }

  public LinkedList<TrainingDataPoint> getTrainingData(){
    return ((TDJoinSampler) this.planners.get(0)).getTrainingData();
  }

}
