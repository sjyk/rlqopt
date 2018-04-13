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
}
