package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.relalg.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;


public class HistogramRelation extends HashMap<Attribute, Histogram> {


	public HistogramRelation(HashMap<Attribute, Histogram> data){
		for (Attribute a: data.keySet())
			this.put(a, data.get(a).copy());
	}

	//returns a copy of the histogram
	public Histogram get(Attribute a){
		if (! containsKey(a))
			return null;

		return super.get(a).copy();
	}

	public HistogramRelation copy(){
		HashMap<Attribute, Histogram> data = new HashMap<Attribute, Histogram>();
		for(Attribute a: this.keySet())
			data.put(a, get(a));
		return new HistogramRelation(data);
	}

	public int count(){
		int cnt = 0;
		for(Attribute a: this.keySet())
			cnt = Math.max(cnt, get(a).getCount());

		/*for(Attribute a: this.keySet())
		{
			/System.out.println(a + " " + get(a).getCount());
		}*/


		return Math.max(cnt,1);
	}

}