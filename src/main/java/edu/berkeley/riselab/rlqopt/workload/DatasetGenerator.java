package edu.berkeley.riselab.rlqopt.workload;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.Histogram;
import edu.berkeley.riselab.rlqopt.cost.TableStatisticsModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

public class DatasetGenerator {

  private final int NUMBER = 0;
  private final int STRING = 1;
  private final int DATE = 2;

  private int numAttributes;
  private int maxTableSize;
  private int histogramRes;

  private LinkedList<Integer> attrTypes;

  private Database db;
  private TableStatisticsModel ts;

  public DatasetGenerator(int numRelations, int numAttributes, int maxTableSize, int histogramRes) {

    this.numAttributes = numAttributes;
    attrTypes = generateAttributes();
    this.maxTableSize = maxTableSize;
    this.histogramRes = histogramRes;

    db = new Database();
    ts = new TableStatisticsModel();

    for (int i = 0; i < numRelations; i++) {
      Relation r = generateRelation();
      db.add(r);
      ts.putAll(generateData(r));
    }
  }

  private Histogram generateColumn(int size, int distinct, int type) {

    Random rand = new Random();
    Histogram h;

    switch (type) {
      case NUMBER:
        h = new Histogram(histogramRes, 0, maxTableSize, distinct);
        break;
      case STRING:
        h = new Histogram(histogramRes, 0, distinct, distinct);
        break;
      case DATE:
        h = new Histogram(histogramRes, 0, maxTableSize, distinct);
        break;
      default:
        return null;
    }

    ArrayList<String> distinctKeys = new ArrayList(distinct);
    ArrayList<String> result = new ArrayList(size);

    for (int i = 0; i < distinct; i++) {
      switch (type) {
        case NUMBER:
          distinctKeys.add(randomNumber());
          break;
        case STRING:
          distinctKeys.add(randomString());
          break;
        case DATE:
          distinctKeys.add(randomDate());
          break;
      }
    }

    for (int i = 0; i < size; i++) {
      int index = rand.nextInt(distinctKeys.size());

      switch (type) {
        case STRING:
          h.put(index + 0.0);
          break;
        default:
          h.put(Integer.parseInt(distinctKeys.get(index)) + 0.0);
          break;
      }
    }

    return h;
  }

  private String randomString() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private String randomNumber() {
    return new Random().nextInt(maxTableSize) + "";
  }

  private String randomDate() {
    return randomNumber();
  }

  private LinkedList<Integer> generateAttributes() {
    Random rand = new Random();

    LinkedList<Integer> attrs = new LinkedList();

    for (int i = 0; i < numAttributes; i++) {
      int type = rand.nextInt(3);
      attrs.add(type);
    }

    return attrs;
  }

  public Relation generateRelation() {

    Random rand = new Random();

    int numAttributesInRel = rand.nextInt(numAttributes) + 2;

    HashSet<String> attributes = new HashSet();

    for (int j = 0; j < numAttributesInRel; j++) {
      attributes.add(rand.nextInt(numAttributes) + "");
    }

    return new Relation(attributes.toArray(new String[attributes.size()]));
  }

  public HashMap<Attribute, Histogram> generateData(Relation r) {

    HashMap<Attribute, Histogram> data = new HashMap();
    int[] distinct = new int[] {5, 500, 50000};
    int[] divisors = new int[] {1, 10, 100, 1000, 10000};

    boolean primary = true;
    Random rand = new Random();

    int tableSize = maxTableSize / divisors[rand.nextInt(5)] + 1;

    for (String attr : r) {
      int ind = Integer.parseInt(attr);
      int type = attrTypes.get(ind);

      if ((type == STRING) && primary) {
        data.put(r.get(attr), generateColumn(tableSize, tableSize, type));
        primary = false;
      } else if (type == STRING) {
        data.put(
            r.get(attr),
            generateColumn(tableSize, Math.min(tableSize, distinct[rand.nextInt(3)]), type));
      }

      data.put(r.get(attr), generateColumn(tableSize, tableSize, type));
    }

    return data;
  }

  public Database getDatabase() {
    return db;
  }

  public TableStatisticsModel getStats() {
    return ts;
  }
}
