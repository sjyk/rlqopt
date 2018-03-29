package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.OperatorException;

//implements a union operator
public class UnionOperator extends Operator {

	public UnionOperator(OperatorParameters params, Operator...source) throws OperatorException{
		super(params, source);
	}

	//override
	public boolean isValid(OperatorParameters params, 
						   Operator...source){

		if (source.length != 2)
			return false;

		return true;

	}


}