package edu.berkeley.riselab.rlqopt;

import java.util.LinkedList;

/** Convenience class to represent a list of expressions. */
public class ExpressionList extends LinkedList<Expression> {

  // create it explicitly named
  public ExpressionList(Expression... args) {

    super();

    for (Expression e : args) this.add(e);
  }

  public ExpressionList(LinkedList<Expression> args) {

    super();

    for (Expression e : args) this.add(e);
  }

  // noop loading an attribute
  public ExpressionList(Relation r) {

    super();

    for (String attr : r) this.add(r.get(attr).getExpression());
  }

  public LinkedList<Attribute> getAllVisibleAttributes(LinkedList<Attribute> visibleAttrs) {
    for (Expression e : this) e.getVisibleAttributes(visibleAttrs);
    return visibleAttrs;
  }

  public LinkedList<Attribute> getAllVisibleAttributes() {
    LinkedList<Attribute> visibleAttrs = new LinkedList<>();
    return getAllVisibleAttributes(visibleAttrs);
  }
}
