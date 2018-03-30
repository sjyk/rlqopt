package edu.berkeley.riselab.rlqopt.opt;

import java.util.HashMap;
import java.util.LinkedList;
import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Expression;

public class TableStatisticsModel extends HashMap<Attribute, LinkedList<AttributeStatistics>> implements CostModel {

	public TableStatisticsModel(){
		super();
	}

	public void putStats(Attribute a, AttributeStatistics s){

		if (this.containsKey(a))
		{
			LinkedList<AttributeStatistics> hist = this.get(a);
			hist.add(s);
		}
		else{

			LinkedList<AttributeStatistics> hist = new LinkedList();
			hist.add(s);
			this.put(a, hist);
		}
	}


	private long cardinalityEstimate(Relation r){

	}



	public Cost estimate(Operator in){

		return new Cost(0,0,0);

	}

}