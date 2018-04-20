package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;
import java.util.LinkedList;

// this implements one transformation
// of the plan
public interface Trainable {

  // takes an operator returns an equivalent operator
  public void train(LinkedList<Operator> training);
}
