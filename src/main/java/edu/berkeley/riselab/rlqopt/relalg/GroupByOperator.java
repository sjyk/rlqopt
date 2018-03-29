package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.OperatorException;

//implements a group by operator
public class GroupByOperator extends Operator {

	public GroupByOperator(OperatorParameters params, 
							Operator...source) throws OperatorException{
		super(params, source);
	}

	//override
	public boolean isValid(OperatorParameters params, 
						   Operator...source){

		if (source.length != 1 && params.secondary_expression != null)
			return false;

		return true;
	}


}