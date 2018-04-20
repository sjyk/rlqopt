package edu.berkeley.riselab.rlqopt.opt;

public class PlanningStatistics {

  public double preprocessing = 0;
  public double planning = 0;
  public double initialCost = 0;
  public double finalCost;
  public String name;

  public String toString() {
    return "{'name': "
        + name
        + ",\n"
        + "'preproc': "
        + (preprocessing / 1e6)
        + ",\n"
        + "'planning: '"
        + (planning / 1e6)
        + ",\n"
        + "'initial: '"
        + initialCost
        + ",\n"
        + "'final: '"
        + finalCost
        + "}";
  }
}
