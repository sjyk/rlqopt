package edu.berkeley.riselab.rlqopt;

import java.util.LinkedList;

public class Database extends LinkedList<Relation> {

  public Database(Relation... args) {

    super();

    // initialize with the input list
    for (Relation arg : args) this.add(arg);
  }

  public LinkedList<Attribute> getAllAttributes() {
    LinkedList<Attribute> allAttributes = new LinkedList();
    for (Relation r : this) allAttributes.addAll(r.attributes());
    return allAttributes;
  }

  public int getNumAttributes() {
    return getAllAttributes().size();
  }

  public boolean containsName(String relation) {

    for (Relation relObj : this) {

      if (relObj.name == null) continue;

      if (relObj.name.equalsIgnoreCase(relation)) return true;
    }

    return false;
  }

  public Relation getByName(String relation) {

    for (Relation relObj : this) {

      if (relObj.name == null) continue;

      // System.out.println("b:"+ relObj.name);

      if (relObj.name.equalsIgnoreCase(relation)) return relObj;
    }

    return null;
  }

  public Attribute getAttributeByFQName(String fullyQualified) {
    String[] comps = fullyQualified.toLowerCase().split(".");
    Relation r = getByName(comps[0]);

    if (r == null) return null;

    return r.get(comps[1]);
  }

  public Attribute getAttributeByName(String unqualified) {

    for (Relation relObj : this) {
      if (relObj.get(unqualified) != null) return relObj.get(unqualified.toLowerCase());
    }

    return null;
  }
}
