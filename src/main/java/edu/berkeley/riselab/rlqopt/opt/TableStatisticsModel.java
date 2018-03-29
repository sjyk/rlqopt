package edu.berkeley.riselab.rlqopt.opt;

import java.util.HashMap;
import edu.berkeley.riselab.rlqopt.Attribute;

public class TableStatisticsModel extends HashMap<Attribute, Long> implements CostModel {

	public TableStatisticsModel(){
		super();
	}


	private long estimateTableScan(Operator in){
		long max = 0;
		for (Expression e: in.params.expression){

			if (get(e.noop) > max)
				max = get(e.noop);
		
		}

		return max;
	}

	private long estimateProject(Operator in, long cost){
		return cost;
	}

	private long estimateJoin(Operator in, long cost){
		return cost;
	}


	public long estimate(Operator in){

		return 0;

	}

}