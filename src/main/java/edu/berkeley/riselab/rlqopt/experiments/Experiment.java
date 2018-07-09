package edu.berkeley.riselab.rlqopt.experiments;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.opt.PlanningStatistics;
import edu.berkeley.riselab.rlqopt.opt.Trainable;
import edu.berkeley.riselab.rlqopt.workload.WorkloadGenerator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Experiment {

  WorkloadGenerator workload;
  int numTraining;
  int numTest;
  List<Planner> planners;
  Planner baseline;
  HashMap<Planner, LinkedList<PlanningStatistics>> finalCost;

  public Experiment(
      WorkloadGenerator workload, int numTraining, int numTest, List<Planner> planners) {

    this.workload = workload;
    this.numTraining = numTraining;
    this.numTest = numTest;
    this.planners = planners;
    this.baseline = planners.get(0);

    finalCost = new HashMap();
    for (Planner p : planners) finalCost.put(p, new LinkedList());
  }

  public void train() throws OperatorException {
    LinkedList<Operator> training = workload.generateWorkload(numTraining);
    for (Planner p : this.planners) {
      if (p instanceof Trainable) {
        LinkedList<Operator> trainingCpy = workload.copyWorkload(training);
        ((Trainable) p).train(trainingCpy);
      }
    }
  }

  public void run() throws OperatorException {

    LinkedList<Operator> test = workload.generateWorkload(numTest);

    for (Planner p : this.planners) {

      for (Operator op : workload.copyWorkload(test)) {

        if (p == baseline) {
          p.plan(op.copy(), workload.getStatsModel(), workload.getStatsModel()).toSQLString();
        } else {
          p.plan(op.copy(), workload.getNoisyStatsModel(), workload.getStatsModel());
        }

        System.out.println("+++,q" + op.hashCode() + "," + op.source.size() +","+ p + "," + p.getLastPlanStats().finalCost);

        LinkedList<PlanningStatistics> stats = finalCost.get(p);
        stats.add(p.getLastPlanStats());
        finalCost.put(p, stats);
      }
    }
  }

  public Map<Planner, Double> getBaselineImprovement() {

    HashMap<Planner, Double> rtn = new HashMap();

    for (Planner p : this.planners) {
      LinkedList<PlanningStatistics> stats = finalCost.get(p);
      LinkedList<PlanningStatistics> statsB = finalCost.get(this.baseline);
      int n = stats.size();

      double sum = 0.0;
      int nt = 0;

      for (int i = 0; i < n; i++) {

        // if (stats.get(i).finalCost == stats.get(i).initialCost)
        //  continue;

        double cost = (double) Math.log(stats.get(i).finalCost);
        // ( - statsB.get(i).finalCost) / statsB.get(i).finalCost;

        // if (statsB.get(i).finalCost == 0) sum += 0.0;
        sum += cost;

        nt++;
      }

      rtn.put(p, sum / nt);
    }
    return rtn;
  }

  public HashMap<Planner, Double> getBaselineLatency() {

    HashMap<Planner, Double> rtn = new HashMap();

    for (Planner p : this.planners) {
      LinkedList<PlanningStatistics> stats = finalCost.get(p);
      LinkedList<PlanningStatistics> statsB = finalCost.get(this.baseline);
      int n = stats.size();

      // System.out.println(stats);

      double sum = 0.0;

      for (int i = 0; i < n; i++) {
        double cost = ((double) stats.get(i).planning / 1e6);
        sum += cost;
      }

      rtn.put(p, sum / n);
    }

    return rtn;
  }
}
