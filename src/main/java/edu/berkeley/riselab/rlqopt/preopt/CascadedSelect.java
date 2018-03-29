package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.LinkedList;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.OperatorException;


public class CascadedSelect implements PreOptimizationRewrite{

	public CascadedSelect(){}

	public Operator apply(Operator in){

		try{

			if(in instanceof SelectOperator)
			{
				Expression predicate = in.params.expression.get(0);
				Operator source = in.source.get(0);

				if (predicate.op.equals(Expression.AND)){

					for (Expression e: predicate.children){

						OperatorParameters param = new OperatorParameters(e.getExpressionList());
						source = new SelectOperator(param, source);
					}

					return source;

				}
				else{
					return in;
				}


			}

			
		}
		catch(OperatorException e){
			return in;
		}
		
		LinkedList<Operator> children = new LinkedList();

	    for(Operator child: in.source) 
	    	children.add(apply(child));

	    in.source = children;

	    return in;
	}

}
