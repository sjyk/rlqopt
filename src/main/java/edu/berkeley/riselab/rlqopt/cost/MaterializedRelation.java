package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.LinkedList;

public class MaterializedRelation {

  private LinkedList<Attribute> schema;
  private LinkedList<LinkedList<String>> data;
  private Relation relation;

  public MaterializedRelation(LinkedList<Attribute> schema, LinkedList<LinkedList<String>> data) {
    this.schema = schema;
    this.data = data;
  }

  public MaterializedRelation(Relation r, LinkedList<LinkedList<String>> data) {
    this.schema = r.attributesList();
    this.data = data;
    this.relation = r;
  }

  public MaterializedRelation copy() {
    LinkedList<Attribute> schemaCpy = (LinkedList<Attribute>) schema.clone();
    LinkedList<LinkedList<String>> dataCpy = (LinkedList<LinkedList<String>>) data.clone();
    return new MaterializedRelation(schemaCpy, dataCpy);
  }

  public int count() {
    return data.size();
  }

  public MaterializedRelation select(Expression e) {

    MaterializedRelation result = copy();

    String value = null;
    Attribute attr;
    int index;
    int index2 = -1;

    if (e.children.get(0).noop != null) {
      value = e.children.get(1).op;
      index = schema.indexOf(e.children.get(0).noop);
    } else if (e.children.get(1).noop != null) {
      value = e.children.get(0).op;
      index = schema.indexOf(e.children.get(1).noop);
    } else {
      index2 = schema.indexOf(e.children.get(1).noop);
      index = schema.indexOf(e.children.get(0).noop);
    }

    LinkedList<LinkedList<String>> filteredData = new LinkedList();

    if (e.op.equals(Expression.EQUALS)) {

      for (LinkedList<String> record : data) {

        if (value == null) {
          if (record.get(index).equals(record.get(index2))) filteredData.add(record);
        } else {

          if (record.get(index).equals(value)) filteredData.add(record);
        }
      }
    }

    result.data = filteredData;

    return result;
  }

  public MaterializedRelation project(ExpressionList e) {

    MaterializedRelation result = copy();
    LinkedList<Attribute> attrs = e.getAllVisibleAttributes();
    LinkedList<LinkedList<String>> dataCpy = new LinkedList();

    for (LinkedList<String> record : this.data) {
      LinkedList<String> recordCpy = new LinkedList();

      for (int i = 0; i < schema.size(); i++)
        if (attrs.contains(schema.get(i))) recordCpy.add(record.get(i));

      dataCpy.add(recordCpy);
    }

    return new MaterializedRelation(attrs, dataCpy);
  }

  public MaterializedRelation cartesian(MaterializedRelation other) {

    MaterializedRelation result = copy();
    LinkedList<Attribute> attrs = new LinkedList();
    attrs.addAll(result.schema);
    attrs.addAll(other.schema);

    LinkedList<LinkedList<String>> dataCpy = new LinkedList();

    for (LinkedList<String> recordR : this.data) {
      for (LinkedList<String> recordL : other.data) {
        LinkedList<String> record = new LinkedList();
        record.addAll(recordR);
        record.addAll(recordL);
        dataCpy.add(record);
      }
    }

    return new MaterializedRelation(attrs, dataCpy);
  }

  public String toString() {
    String result = "";

    for (Attribute attr : schema) result += attr + "\t|";

    result += "\n";

    for (LinkedList<String> record : data) {
      for (String attr : record) result += attr + "\t|";

      result += "\n";
    }

    return result;
  }
}
