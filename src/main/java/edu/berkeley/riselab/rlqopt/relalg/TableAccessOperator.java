package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.OperatorException;

//implements a project operator
public class TableAccessOperator extends Operator {

	public TableAccessOperator(OperatorParameters params) throws OperatorException {
		super(params);
	}

	//override
	public boolean isValid(OperatorParameters params, 
						   Operator...source){

		if (source.length != 0)
			return false;

		return true;

	}


}