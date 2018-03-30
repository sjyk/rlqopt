package edu.berkeley.riselab.rlqopt;

import java.util.LinkedList;

/**
 * An expression is a tree with a operator and 
 * a sequence of other expressions
 */
public class ExpressionList extends LinkedList<Expression>{


	//create it explicitly named
	public ExpressionList(Expression ... args){
		
		super();

		for (Expression e: args)
			this.add(e);
	}

	//noop loading an attribute
	public ExpressionList(Relation r){
		
		super();

		for (String attr: r)
			this.add(r.get(attr).getExpression());

	}

	public LinkedList<Attribute> getAllVisibleAttributes(){

		LinkedList<Attribute> visibleAttrs = new LinkedList<Attribute>();
		for (Expression e: this)
			visibleAttrs.addAll(e.getVisibleAttributes());
		
		return visibleAttrs;

	}

}