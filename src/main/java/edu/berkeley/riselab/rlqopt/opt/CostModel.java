package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Operator;

public interface CostModel {

	public Cost estimate(Operator in);

}