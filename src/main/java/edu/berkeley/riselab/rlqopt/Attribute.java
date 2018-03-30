package edu.berkeley.riselab.rlqopt;

/** A wrapper class for a relation and named column pair */
public class Attribute {

  public Relation relation;
  public String attribute;

  // create it explicitly named
  public Attribute(Relation r, String attr) {
    this.relation = r;
    this.attribute = attr;
  }

  // implicitly named
  public Attribute(String attr) {
    this.relation = null;
    this.attribute = attr;
  }

  public Expression getExpression() {
    return new Expression(this);
  }

  public String toString() {
    return "R" + relation.hashCode() + "." + attribute;
  }
}
