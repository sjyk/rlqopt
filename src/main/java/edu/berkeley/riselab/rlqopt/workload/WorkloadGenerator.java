package edu.berkeley.riselab.rlqopt.workload;

import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.LinkedList;

public abstract class WorkloadGenerator {

  public WorkloadGenerator() {}

  public abstract Database getDatabase();

  public abstract TableStatisticsModel getStatsModel();

  public TableStatisticsModel getNoisyStatsModel() {
    return getStatsModel();
  }

  public abstract LinkedList<Operator> generateWorkload(int n) throws OperatorException;

  public LinkedList<Operator> copyWorkload(LinkedList<Operator> w) throws OperatorException {

    LinkedList<Operator> workload = new LinkedList();

    for (Operator op : w) workload.add(op.copy());

    return workload;
  }
}
