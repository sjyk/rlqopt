package edu.berkeley.riselab.rlqopt;

/**
 * OperatorParameters 
 **/
public class OperatorParameters{

	public ExpressionList expression;
	public ExpressionList secondary_expression;

	//every parameter at the least has a source relation
	public OperatorParameters(ExpressionList expression){

		this.expression = expression;
		this.secondary_expression = null;
	}

	//every parameter at the least has a source relation
	public OperatorParameters(ExpressionList expression, ExpressionList secondary_expression){

		this.expression = expression;
		this.secondary_expression = secondary_expression;
	}

}