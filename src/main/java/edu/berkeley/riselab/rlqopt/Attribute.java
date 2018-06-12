package edu.berkeley.riselab.rlqopt;

/** A wrapper class for a relation and named column pair */
public class Attribute {

  public Relation relation;
  public String attribute;
  public boolean isKey;
  private int type;

  public final static int NUMBER = 0;
  public final static int STRING = 1;
  public final static int DATE = 2;

  // create it explicitly named
  public Attribute(Relation r, String attr) {
    this.relation = r;
    this.attribute = attr;
    this.isKey = false;
    this.type = STRING;

  }

  public Attribute(Relation r, String attr, boolean isKey, int type) {
    this.relation = r;
    this.attribute = attr;
    this.isKey = isKey;
    this.type = type;
  }

  public Attribute(Relation r, String attr, boolean isKey) {
    this.relation = r;
    this.attribute = attr;
    this.isKey = isKey;
    this.type = STRING;
  }

  // implicitly named
  public Attribute(String attr) {
    this.relation = null;
    this.attribute = attr;
    this.isKey = false;
    this.type = STRING;
  }

  public Expression getExpression() {
    return new Expression(this);
  }

  public String toString() {
    if (relation.name == null) return "R" + relation.hashCode() + "." + attribute + modifiers();
    else return relation.name + "." + attribute + modifiers();
  }

  public String modifiers(){
    return "["+type+"]"+ ((this.isKey) ? "*" : "");
  }

  public int hashCode() {
    return (relation.toString() + attribute.toString()).hashCode();
  }

  public boolean equals(Object obj) {

    if (!(obj instanceof Attribute)) return false;

    Attribute other = (Attribute) obj;

    return (relation.hashCode() == other.relation.hashCode())
        && (attribute.equals(other.attribute));
  }
}
