package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.opt.PlanningStatistics;
import edu.berkeley.riselab.rlqopt.opt.Trainable;
import edu.berkeley.riselab.rlqopt.workload.WorkloadGenerator;
import java.util.LinkedList;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

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
    setPlannerName("learning");
  }

  public RLQOpt(WorkloadGenerator w, String trainingDataPath) {
    this(w);
    tgen =
        new TrainingDataGenerator(
            w.getDatabase(), "output.csv", w.getStatsModel(), trainer, trainingDataPath);
  }

  public void train(LinkedList<Operator> training) {
    DataSet dataSet = tgen.loadDataSet();
    DataSetIterator iter;

    if (dataSet != null) {
      // Dataset loaded from file.
      // We need to make sure the newly loaded dataset is standardized.
      DataNormalizer.normalize(dataSet);
      iter = tgen.generateDataSetIterator(dataSet);
    } else {
      // Generate dataset from scratch.
      iter = tgen.generateDataSetIterator(training, 1);
    }

    net = model.train(iter);
    learner.setNetwork(net);
  }

  public Operator plan(Operator nominal, CostModel internal, CostModel actual) {
    return learner.plan(nominal, internal, actual);
  }

  public PlanningStatistics getLastPlanStats() {
    return learner.getLastPlanStats();
  }
}
