package edu.berkeley.riselab.rlqopt.opt;

public class Cost {

  public long operatorIOcost;
  public long operatorCPUcost;
  public long resultCardinality;

  public Cost(long operatorIOcost, long resultCardinality) {
    this.operatorIOcost = operatorIOcost;
    this.resultCardinality = resultCardinality;
    this.operatorCPUcost = 0;
  }

  public Cost(long operatorIOcost, long resultCardinality, long operatorCPUcost) {
    this.operatorIOcost = operatorIOcost;
    this.resultCardinality = resultCardinality;
    this.operatorCPUcost = operatorCPUcost;
  }

  public String toString() {
    return "Cost: {IO:"
        + operatorIOcost
        + ", CPU"
        + operatorCPUcost
        + ", Cardinality: "
        + resultCardinality
        + "}";
  }
}
