package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.opt.*;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.preopt.*;
import edu.berkeley.riselab.rlqopt.workload.WorkloadGenerator;
import java.util.LinkedList;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

// the main planner class
public class RLQOpt extends Planner implements Trainable {

  TrainingPlanner trainer;
  LearningPlanner learner;
  MultiLayerNetwork net;
  TrainingDataGenerator tgen;
  ModelTrainer model;

  public RLQOpt(WorkloadGenerator w) {

    super(new LinkedList(), new LinkedList(), new LinkedList());

    trainer = new TrainingPlanner();
    learner = new LearningPlanner(w.getDatabase());
    tgen = new TrainingDataGenerator(w.getDatabase(), "output.csv", w.getStatsModel(), trainer);
    model = new ModelTrainer(w.getDatabase());
  }

  public void train(LinkedList<Operator> training) {
    net = model.train(tgen.generateDataSetIterator(training, 1));
    learner.setNetwork(net);
  }

  public Operator plan(Operator nominal, CostModel internal, CostModel actual) {
    return learner.plan(nominal, internal, actual);
  }

  public PlanningStatistics getLastPlanStats() {
    return learner.getLastPlanStats();
  }
}
