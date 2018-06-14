package edu.berkeley.riselab.rlqopt;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.UUID;
import java.util.Arrays;

/** Main data structure for tables, just a wrapper around a HashSet */
public class Relation extends LinkedList<String> {

  public String name;

  public LinkedList<Integer> types;

  private LinkedList<Integer> keys;

  public Relation(String... args) {

    super();

    this.name = "R"+UUID.randomUUID().toString().replace("-","").substring(0,4);

    // initialize with the input list
    for (String arg : args) this.add(arg.toLowerCase());
  }


  public Relation(String [] args, int [] types, int [] keys) {

    super();

    this.name = "R"+UUID.randomUUID().toString().replace("-","").substring(0,4);

    this.types = new LinkedList();
    this.keys = new LinkedList();

    // initialize with the input list
    for (String arg : args) this.add(arg.toLowerCase());

    for (int arg : types) this.types.add(arg);

    for (int arg : keys) this.keys.add(arg);

  }

  public Attribute get(String exp) {

    String attr = null;
    if (exp.contains(".")) {
      String[] comps = exp.split("\\.");
      attr = comps[1].toLowerCase();

      // System.out.println(attr + " " + comps[0] + " " + this.contains(attr));

      if (!this.name.equalsIgnoreCase(comps[0])) return null;

      if (!this.contains(attr)) return null;

    } else {
      attr = exp.toLowerCase();
      if (!this.contains(attr)) return null;
    }

    if (this.types == null || this.keys == null)
      return new Attribute(this, attr);

    int index = this.indexOf(attr);
    boolean isKey = this.keys.contains(index);

    return new Attribute(this, attr, isKey, this.types.get(index));
  }

  public HashSet<Attribute> attributes() {

    HashSet<Attribute> attrs = new HashSet();

    for (String s : this) attrs.add(get(s));

    return attrs;
  }

  public LinkedList<Attribute> attributesList() {

    LinkedList<Attribute> attrs = new LinkedList();

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

  public int hashCode() {
    return this.toString().hashCode();
  }

  public String toString(){
    return this.name;
  }

  public boolean equals(Object obj) {
    return (this.hashCode() == obj.hashCode());
  }

}
