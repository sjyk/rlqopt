package edu.berkeley.riselab.rlqopt.opt;

import java.util.HashMap;
import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Expression;

public class TableStatisticsModel extends HashMap<Attribute, Long> implements CostModel {

	public TableStatisticsModel(){
		super();
	}


	private Cost estimateTableScan(Operator in){
		long max = 0;
		for (Expression e: in.params.expression){

			if (get(e.noop) > max)
				max = get(e.noop);
		
		}

		return new Cost(max, max,0);
	}

	private Cost estimateProject(Operator in, Cost inCost){
		return new Cost(inCost.resultCardinality, inCost.resultCardinality,0);
	}

	private Cost estimateSelect(Operator in, Cost inCost){



		return new Cost(inCost.resultCardinality, inCost.resultCardinality,0);
	}

	private Cost estimateJoin(Operator s1, Operator s2, long cost){
		return new Cost(0,0,0);
	}


	public Cost estimate(Operator in){

		return new Cost(0,0,0);

	}

}