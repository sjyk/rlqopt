package edu.berkeley.riselab.rlqopt.cost;

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

  public Cost(double operatorIOcost, double resultCardinality, double operatorCPUcost) {
    this.operatorIOcost = (long) operatorIOcost;
    this.resultCardinality = (long) resultCardinality;
    this.operatorCPUcost = (long) operatorCPUcost;
  }

  public Cost plus(Cost other) {
    return new Cost(
        this.operatorIOcost + other.operatorIOcost,
        this.resultCardinality,
        other.operatorCPUcost + this.operatorCPUcost);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Cost)) return false;

    Cost other = (Cost) obj;

    return (other.operatorCPUcost == operatorCPUcost)
        && (other.operatorIOcost == operatorIOcost)
        && (other.resultCardinality == resultCardinality);
  }

  public String toString() {
    return "Cost: {IO:"
        + operatorIOcost
        + ", CPU: "
        + operatorCPUcost
        + ", Cardinality: "
        + resultCardinality
        + "}";
  }
}
