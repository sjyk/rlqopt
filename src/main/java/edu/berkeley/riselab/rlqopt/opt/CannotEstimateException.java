package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Expression;

/**
 * OperatorException class- this class defines an abstract relational operator
 **/
public class CannotEstimateException extends Exception
{
	//TODO more error handling
	public CannotEstimateException(Expression e){

		super("Cannot estimate:" + e.toString());
	}
}