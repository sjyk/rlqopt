package edu.berkeley.riselab.rlqopt.experiments;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.Planner;
import edu.berkeley.riselab.rlqopt.opt.PlanningStatistics;
import edu.berkeley.riselab.rlqopt.opt.Trainable;
import edu.berkeley.riselab.rlqopt.opt.learning.RLQOpt;
import edu.berkeley.riselab.rlqopt.workload.WorkloadGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Experiment {

  WorkloadGenerator workload;
  int numTraining;
  int numTest;
  List<Planner> planners;
  Planner baseline;
  HashMap<Planner, LinkedList<PlanningStatistics>> finalCost;

  LinkedList<Operator> trainWorkload;
  // If non-zero, train on templates with join graphs <= this number.  On testing, test on queries
  // with graphs > this number.  Set to 0 to disable.
  final int kWayJoinThreshold = 0;

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
    if (trainWorkload == null) {
      LinkedList<Operator> training = workload.generateWorkload(numTraining);
      LinkedList<Operator> trainingCpy = workload.copyWorkload(training);
      List<Operator> toRemove = new ArrayList<>();
      for (Operator trainOp : trainingCpy) {
        if (kWayJoinThreshold != 0 && trainOp.source.size() > kWayJoinThreshold) {
          System.out.println(
              "kWayJoinThreshold "
                  + kWayJoinThreshold
                  + "; removing query with larger graph size "
                  + trainOp.source.size());
          toRemove.add(trainOp);
        }
      }
      trainingCpy.removeAll(toRemove);
      // Count # unique relations.
      Set<Relation> coveredRelations = new HashSet<>();
      for (Operator op : trainingCpy) coveredRelations.addAll(op.getVisibleRelations());
      System.out.println("Covered relations " + coveredRelations.size() + ": " + coveredRelations);
      System.out.println("Training on " + trainingCpy.size() + " queries");

      trainWorkload = trainingCpy;
    }

    for (Planner p : this.planners) {
      if (p instanceof Trainable) {
        //          System.out.println(
        //              "*** "
        //                  + trainOp.toString().hashCode()
        //                  + "; "
        //                  + trainOp.source.size()
        //                  + "; "
        //                  + trainOp);
        ((Trainable) p).train(trainWorkload);
      }
    }
  }

  private void maybePrintJoinSize(List<Operator> workload) {
    boolean doPrint = false;
    if (doPrint) {
      System.out.println("Per query join size:");
      int i = 0;
      for (Operator o : workload) {
        System.out.printf("q%d\t%d\n", i, o.getVisibleRelations().size());
        ++i;
      }
    }
  }

  public void run() throws OperatorException {

    LinkedList<Operator> test = workload.generateWorkload(numTest);
    maybePrintJoinSize(test);

    System.out.println("test.size " + test.size() + " train size " + trainWorkload.size());
    if (kWayJoinThreshold != 0) {
      List<Operator> toRemove = new ArrayList<>();
      for (Operator op : test) {
        if (op.source.size() <= kWayJoinThreshold) {
          //        if (op.source.size() < 12) {
          toRemove.add(op);
        }
      }
      test.removeAll(toRemove);
    }
    System.out.println("Testing on " + test.size() + " queries");

    for (Planner p : this.planners) {

      for (Operator op : workload.copyWorkload(test)) {

        //        if (p == baseline) {
        //          p.plan(op.copy(), workload.getStatsModel(),
        // workload.getStatsModel()).toSQLString();
        //        } else {
        //          p.plan(op.copy(), workload.getNoisyStatsModel(), workload.getStatsModel());
        //        }
        System.out.println("Evaluating " + p + ": opt is using noisy");
        p.plan(op.copy(), workload.getNoisyStatsModel(), workload.getStatsModel());

        System.out.println(
            "+++,q"
                + op.hashCode()
                + ","
                + op.getVisibleRelations().size() // # of relations in the query graph.
                + ","
                + p
                + ","
                + p.getLastPlanStats().finalCost);

        //        System.out.println(
        //            op
        //                + "+++---\n"
        //                + p.plan(op.copy(), workload.getStatsModel(), workload.getStatsModel()));

        LinkedList<PlanningStatistics> stats = finalCost.get(p);
        stats.add(p.getLastPlanStats());
        finalCost.put(p, stats);
      }
    }
  }

  public Map<Planner, Double> getBaselineImprovement(int dropFirst) {

    HashMap<Planner, Double> rtn = new HashMap();

    for (Planner p : this.planners) {
      LinkedList<PlanningStatistics> stats = finalCost.get(p);
      int n = stats.size();

      double sum = 0.0;
      int nt = 0;

      for (int i = dropFirst; i < n; i++) {

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

  public Map<Planner, List<Double>> getPerQueryImprovement() {
    Map<Planner, List<Double>> rtn = new HashMap<>();
    for (Planner p : this.planners) {
      LinkedList<PlanningStatistics> stats = finalCost.get(p);
      List<Double> perQueryCost = new LinkedList<>();
      for (PlanningStatistics queryStats : stats) {
        // Math.log() because getBaselineImprovement() does so.
        perQueryCost.add(Math.log(queryStats.finalCost));
      }
      rtn.put(p, perQueryCost);
    }
    return rtn;
  }

  public Map<Planner, List<Double>> getPerQueryLatency() {
    Map<Planner, List<Double>> rtn = new HashMap<>();
    for (Planner p : this.planners) {
      LinkedList<PlanningStatistics> stats = finalCost.get(p);
      List<Double> perQueryCost = new LinkedList<>();
      for (PlanningStatistics queryStats : stats) {
        // Math.log() because getBaselineImprovement() does so.
        perQueryCost.add(((double) queryStats.planning / 1e6));
      }
      rtn.put(p, perQueryCost);
    }
    return rtn;
  }
}
