package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
import java.util.HashSet;
import java.util.UUID;

/** Main data structure for tables, just a wrapper around a HashSet */
public class Relation extends HashSet<String> {

  public String name;

  public Relation(String... args) {

    super();

    this.name = "R"+UUID.randomUUID().toString().replace("-","").substring(0,4);

    // initialize with the input list
    for (String arg : args) this.add(arg.toLowerCase());
  }

  public Operator scan() throws OperatorException {
    OperatorParameters scan_params = new OperatorParameters(this.getExpressionList());
    return new TableAccessOperator(scan_params);
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
