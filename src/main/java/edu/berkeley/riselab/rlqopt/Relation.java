package edu.berkeley.riselab.rlqopt;

import java.util.HashSet;

/** Main data structure for tables, just a wrapper around a HashSet */
public class Relation extends HashSet<String> {

  public Relation(String... args) {

    super();

    // initialize with the input list
    for (String arg : args) this.add(arg);
  }

  public Attribute get(String attr) {

    if (!this.contains(attr)) return null;

    return new Attribute(this, attr);
  }

  public HashSet<Attribute> attributes() {

    HashSet<Attribute> attrs = new HashSet();

    for (String s : this) attrs.add(get(s));

    return attrs;
  }

  public Attribute first() {

    for (String s : this) return get(s);

    return null;
  }

  public ExpressionList getExpressionList() {

    return new ExpressionList(this);
  }

  public boolean contains(Attribute attr) {

    if (attr.relation != null) return this.equals(attr.relation) && this.contains(attr.attribute);

    return this.contains(attr.attribute);
  }

  public boolean containsAll(HashSet<Attribute> attrs) {

    boolean rtn = true;

    for (Attribute attr : attrs) {
      if (!contains(attr)) return false;
    }

    return rtn;
  }
}
