package edu.berkeley.riselab.rlqopt.workload;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.AttributeStatistics;
import edu.berkeley.riselab.rlqopt.opt.TableStatisticsModel;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public abstract class WorkloadGenerator {


  public WorkloadGenerator() {

  }

  public abstract Database getDatabase();

  public abstract TableStatisticsModel getStatsModel();


  public abstract LinkedList<Operator> generateWorkload(int n) throws OperatorException;


  public LinkedList<Operator> copyWorkload(LinkedList<Operator> w) throws OperatorException {

    LinkedList<Operator> workload = new LinkedList();

    for (Operator op : w) workload.add(op.copy());

    return workload;
  }

  

}
