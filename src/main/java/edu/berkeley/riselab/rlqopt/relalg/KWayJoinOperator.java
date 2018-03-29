package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.OperatorException;

//implements a project operator
public class KWayJoinOperator extends Operator {

	public KWayJoinOperator(OperatorParameters params, Operator...source) throws OperatorException {
		super(params, source);
	}

	//override
	public boolean isValid(OperatorParameters params, 
						   Operator...source){

		return true;

	}


}