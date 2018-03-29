package edu.berkeley.riselab.rlqopt;

/**
 * OperatorException class- this class defines an abstract relational operator
 **/
public class OperatorException extends Exception
{
	//TODO more error handling
	public OperatorException(Operator...source){

		super("Invalid Syntax at:" + source.toString());
	}
}