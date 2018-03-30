
package edu.berkeley.riselab.rlqopt.opt;

import java.util.HashMap;
import edu.berkeley.riselab.rlqopt.Expression;


public class AttributeStatistics{

	public long distinctValues;
	public long minVal;
	public long maxVal;
	public boolean isNumber;

	public AttributeStatistics (long distinctValues){

		this.isNumber = false;
		this.distinctValues = distinctValues;

	}

	public AttributeStatistics (long distinctValues, long minVal, long maxVal){

		this.isNumber = true;
		this.distinctValues = distinctValues;
		this.minVal = minVal;
		this.maxVal = maxVal;

	}

	private double clip10(double val){
		return Math.max(Math.min(val, 1.0),0.0);
	}

	//returns error if you can't estimate
	public double estimateReductionFactor(Expression e) throws CannotEstimateException{

		if (e.op.equals(Expression.EQUALS)){
			return clip10(1.0/distinctValues);
		}
		else if (isNumber){

			if (e.op.equals(Expression.GREATER_THAN) || 
				e.op.equals(Expression.GREATER_THAN_EQUALS)){

				try{

					Expression child = e.children.get(1);

					if (child.isLiteral())
					{
						double val = Double.parseDouble(child.op);
						return clip10((maxVal-val)/(maxVal - minVal));
					}
					else{

						throw new CannotEstimateException(e);

					}
				}
				catch(Exception ex){ throw new CannotEstimateException(e); }
				
			}


			if (e.op.equals(Expression.LESS_THAN) || 
				e.op.equals(Expression.LESS_THAN_EQUALS)){

				try{

					Expression child = e.children.get(1);
					if (child.isLiteral())
					{
						double val = Double.parseDouble(child.op);
						return clip10((val-minVal)/(maxVal - minVal));
					}
					else{

						throw new CannotEstimateException(e);

					}
				}
				catch(Exception ex){ throw new CannotEstimateException(e); }

			}

		}

		if (e.op.equals(Expression.NOT)){

			return clip10(1.0 - estimateReductionFactor(e.children.get(0)));
		}
		else if (e.op.equals(Expression.OR)){
			double total = 0.0;
			for (Expression c : e.children)
				total += estimateReductionFactor(c);
			return clip10(total);
		}
		else if (e.op.equals(Expression.AND)){
			double total = 1.0;
			for (Expression c : e.children)
				total *= estimateReductionFactor(c);
			return clip10(total);
		}
		else{
			throw new CannotEstimateException(e);
		}

	}


}