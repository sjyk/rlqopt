package edu.berkeley.riselab.rlqopt;

import java.util.HashSet;


/**
 * Main data structure for tables, just a wrapper around
 * a HashSet 
 */
public class Relation extends HashSet<String> {

	public Relation(String...args){
		
		super();

		//initialize with the input list
		for (String arg: args)
			this.add(arg);

	}

	public Attribute get(String attr){

		if (! this.contains(attr) )
			return null;

		return new Attribute(this, attr);

	}

	public ExpressionList getExpressionList(){

		return new ExpressionList(this);

	}


	public boolean contains(Attribute attr){

		if (attr.relation != null)
			return this.equals(attr.relation) &&  this.contains(attr.attribute);

		return this.contains(attr.attribute);
	
	}

}