package edu.berkeley.riselab.rlqopt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Database extends LinkedList<Relation> {

  private final String CREATE_TABLE = "create table";

  public Database(Relation... args) {

    super();

    // initialize with the input list
    for (Relation arg : args) this.add(arg);
  }

  public Database(String filename) {

    super();

    loadFromSchemaFile(filename);
  }

  private void loadFromSchemaFile(String filename) {

    try {
      Scanner scanner = new Scanner(new File(filename));

      boolean readStart = false;
      int index = 0;

      ArrayList<String> names = new ArrayList();
      ArrayList<Integer> types = new ArrayList();
      ArrayList<Integer> keys = new ArrayList();
      String relationName = "";

      while (scanner.hasNextLine()) {

        String line = scanner.nextLine();

        if (line.toLowerCase().contains(CREATE_TABLE)) {
          // System.out.println("Table: " + parseTableName(line));
          readStart = true;
          relationName = parseTableName(line);
        }

        if (readStart && line.contains(";")) {
          readStart = false;

          Relation r =
              new Relation(
                  names.toArray(new String[names.size()]),
                  types.toArray(new Integer[types.size()]),
                  keys.toArray(new Integer[keys.size()]));
          r.name = relationName;

          this.add(r);

          index = 0;
          names = new ArrayList();
          types = new ArrayList();
          keys = new ArrayList();
        }

        if (readStart && (!line.contains(";"))) {
          names.add(parseAttrName(line));
          types.add(parseAttrType(line));

          if (parseAttrKey(line)) keys.add(index);
          index += 1;
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private String parseTableName(String in) {
    int index = in.toLowerCase().indexOf(CREATE_TABLE) + CREATE_TABLE.length();
    int endIndex = in.toLowerCase().indexOf("(");
    return in.substring(index, endIndex).trim();
  }

  private String parseAttrName(String in) {
    String name = in.trim().split(" ")[0];
    return name;
  }

  private boolean parseAttrKey(String in) {
    return in.toLowerCase().contains("primary");
  }

  private int parseAttrType(String in) {
    String name = in.trim().split(" ")[1];

    if (name.contains("integer")) return Attribute.NUMBER;

    return Attribute.STRING;
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
