package edu.berkeley.riselab.rlqopt;

import java.util.HashSet;
import java.util.LinkedList;

/** An expression is a tree with a operator and a sequence of other expressions */
public class Expression {

  // todo check syntax
  public static final String EQUALS = "equals";
  public static final String GREATER_THAN = "gt";
  public static final String GREATER_THAN_EQUALS = "gte";
  public static final String LESS_THAN = "lt";
  public static final String LESS_THAN_EQUALS = "lte";
  public static final String NOT = "not";
  public static final String AND = "and";
  public static final String OR = "or";

  public String op;
  public LinkedList<Expression> children;
  public Attribute noop;

  // create it explicitly named
  public Expression(String op, Expression... args) {
    this.op = op;
    this.children = new LinkedList<Expression>();

    for (Expression e : args) this.children.add(e);
  }

  // deep copy constructor
  public Expression(Expression e) {
    this.op = e.op;

    if (e.children == null) {
      this.children = null;
      this.noop = e.noop;
    } else {

      this.children = new LinkedList<Expression>();

      for (Expression child : e.children) this.children.add(new Expression(child));
    }
  }

  // noop loading an attribute
  public Expression(Attribute a) {
    this.op = null;
    this.children = null;
    this.noop = a;
  }

  public ExpressionList getExpressionList() {
    return new ExpressionList(this);
  }

  public LinkedList<Attribute> getVisibleAttributes() {
    LinkedList<Attribute> visibleAttrs = new LinkedList<Attribute>();

    // base case
    if (this.op == null) {
      visibleAttrs.add(this.noop);
      return visibleAttrs;
    }

    for (Expression e : children) visibleAttrs.addAll(e.getVisibleAttributes());

    return visibleAttrs;
  }

  public HashSet<Attribute> getVisibleAttributeSet() {
    HashSet<Attribute> visibleAttrs = new HashSet<Attribute>();

    // base case
    if (this.op == null) {
      visibleAttrs.add(this.noop);
      return visibleAttrs;
    }

    for (Expression e : children) visibleAttrs.addAll(e.getVisibleAttributes());

    return visibleAttrs;
  }

  private boolean isEquality(Expression e) {

    if (e.op.equals(NOT)) return false;
    else if (e.op.equals(OR)) return false;
    else if (e.op.equals(Expression.GREATER_THAN) || e.op.equals(Expression.GREATER_THAN_EQUALS)) {
      return false;
    } else if (e.op.equals(Expression.LESS_THAN) || e.op.equals(Expression.LESS_THAN_EQUALS)) {
      return false;
    } else if (e.op.equals(AND))
      return isEquality(e.children.get(0)) && isEquality(e.children.get(1));
    else return true;
  }

  public boolean isEquality() {
    return isEquality(this);
  }

  public String toString() {

    if (this.op == null) return this.noop.toString();

    return this.op + "(" + children.toString() + ")";
  }

  public boolean isUDF() {
    return !(op.equals(EQUALS)
        || op.equals(AND)
        || op.equals(NOT)
        || op.equals(OR)
        || op.equals(GREATER_THAN)
        || op.equals(GREATER_THAN_EQUALS)
        || op.equals(LESS_THAN)
        || op.equals(LESS_THAN_EQUALS));
  }

  public boolean isLiteral() {
    return (children.size() == 0);
  }
}
