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

  private Random random;

  public void describe() {
    int numRelations = db.size();
    System.out.print("Num relations: " + numRelations);
    System.out.print("; #Attrs: ");
    for (Relation r : db) {
        System.out.print(r.size() + " ");
    }
    int totalNumAttrs = db.getAllAttributes().size();
    System.out.println("; Total #attrs: "+ totalNumAttrs);
  }

  public DatasetGenerator(int numRelations, int numAttributes, int maxTableSize, int histogramRes) {
    // Change this seed to get different runs.
    this.random = new Random(1234);

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
    ts.setJoinReductionFactors(getJoinReductionFactors(db));
  }

  private String int2bin(int x, int len) {
    String binCode = Integer.toBinaryString(x);
    while (binCode.length() < len) binCode = "0" + binCode;

    return binCode;
  }

  private HashMap<HashSet<Relation>, Double> getJoinReductionFactors(
      LinkedList<Relation> allRelations) {
    HashMap<HashSet<Relation>, Double> reductions = new HashMap();

    int maxCode = (int) Math.pow(2, allRelations.size());

    // System.out.println(maxCode);

    Relation[] relationArray = allRelations.toArray(new Relation[allRelations.size()]);

    for (int code = 0; code <= maxCode; code++) {
      String binCode = int2bin(code, allRelations.size());

      HashSet<Relation> incidentRelations = new HashSet();
      HashSet<Relation> prefix = new HashSet();
      ArrayList<Integer> incidentIndexes = new ArrayList();

      for (int i = 0; i < binCode.length(); i++) {
        if (binCode.charAt(i) == '1') {
          incidentRelations.add(relationArray[i]);
          incidentIndexes.add(i);
        }
      }

      for (int i = 0; i < binCode.length() && prefix.size() < incidentRelations.size() - 1; i++) {
        if (binCode.charAt(i) == '1') prefix.add(relationArray[i]);
      }

      if (reductions.containsKey(incidentRelations)) continue;

      // neighbor boost to add some systematic characteristic to the costs.
      double boost = 1.0;
      for (int i = 0; i < incidentIndexes.size() - 1 && incidentIndexes.size() > 1; i++) {
        if (Math.abs(incidentIndexes.get(i) - incidentIndexes.get(i + 1)) == 1)
          boost = boost / (maxTableSize * maxTableSize); // Math.pow(10.0, incidentIndexes.size());
      }

      if (!reductions.containsKey(prefix) || prefix.size() == 0) {
        reductions.put(incidentRelations, 1.0);
        System.out.println(
            prefix
                + " => "
                + reductions.get(prefix)
                + " xx "
                + incidentRelations
                + " "
                + reductions.size());
      } else {

        Double prefixReduction = reductions.get(prefix);

        System.out.println(
            prefix
                + " => "
                + prefixReduction
                + " xx "
                + incidentRelations
                + " "
                + reductions.size());

        reductions.put(incidentRelations, prefixReduction * (random.nextDouble()) * boost);
      }
    }

    return reductions;
  }

  private Histogram generateColumn(int size, int distinct, int type) {

    // System.out.println("test");

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
    ArrayList<Integer> distinctIntegerKeys = new ArrayList(distinct);
    ArrayList<String> result = new ArrayList(size);

    for (int i = 0; i < distinct; i++) {
      switch (type) {
        case NUMBER:
          int num = randomNumber();
          distinctKeys.add(num + "");
          distinctIntegerKeys.add(num);
          break;
        case STRING:
          distinctKeys.add(randomString());
          break;
        case DATE:
          distinctKeys.add(randomDate());
          break;
      }
    }

    if (type == NUMBER) {
      ArrayList<Double> weights = generateGaussWeights(distinctIntegerKeys);

      for (int i = 0; i < size; i++) h.put(sample(distinctIntegerKeys, weights) + 0.0);
    } else {

      for (int i = 0; i < size; i++) h.put(random.nextInt(distinctKeys.size()) + 0.0);
    }

    return h;
  }

  private ArrayList<Double> generateGaussWeights(ArrayList<Integer> nums) {

    double sum = 0.0;
    for (Integer i : nums) sum += i + 0.0;

    double mean = sum / nums.size();
    double std = maxTableSize / 4.0;

    ArrayList<Double> unnormalizedWeights = new ArrayList(nums.size());
    ArrayList<Double> weights = new ArrayList(nums.size());

    double z = 0.0;
    for (Integer i : nums) {
      double x = Math.exp(-Math.pow((i - mean), 2) / std);
      z += x;
      unnormalizedWeights.add(x);
    }

    for (Double x : unnormalizedWeights) weights.add(x / z);

    return weights;
  }

  private Integer sample(ArrayList<Integer> nums, ArrayList<Double> weights) {

    double randomWeight = random.nextDouble();

    for (int i = 0; i < nums.size(); i++) {
      randomWeight = randomWeight - weights.get(i);

      if (randomWeight <= 0) return nums.get(i);
    }

    return nums.get(nums.size() - 1);
  }

  private String randomString() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private int randomNumber() {
    return random.nextInt(maxTableSize);
  }

  private String randomDate() {
    return randomNumber() + "";
  }

  private LinkedList<Integer> generateAttributes() {
    LinkedList<Integer> attrs = new LinkedList();

    for (int i = 0; i < numAttributes; i++) {
      int type = random.nextInt(3);
      attrs.add(type);
    }

    return attrs;
  }

  public Relation generateRelation() {
    int numAttributesInRel = random.nextInt(numAttributes) + 2;

    HashSet<String> attributes = new HashSet();

    for (int j = 0; j < numAttributesInRel; j++) {
      attributes.add(random.nextInt(numAttributes) + "");
    }

    return new Relation(attributes.toArray(new String[attributes.size()]));
  }

  private int sampleOrderOfMagnitude(int maxSize, int minSize, double decay) {
    while (maxSize >= minSize) { // hyperparam

      if (random.nextDouble() > decay) break;

      maxSize /= 10;
    }

    return Math.max(maxSize, minSize);
  }

  public HashMap<Attribute, Histogram> generateData(Relation r) {

    HashMap<Attribute, Histogram> data = new HashMap();

    // boolean primary = true;
    int tableSize =
        sampleOrderOfMagnitude(
            maxTableSize, 10, 0.25); // maxTableSize / divisors[rand.nextInt(5)] + 1;

    // System.out.println(tableSize);

    for (String attr : r) {
      int ind = Integer.parseInt(attr);
      int type = attrTypes.get(ind);
      Attribute attrObj = r.get(attr);

      if (attrObj.isKey) {
        data.put(attrObj, generateColumn(tableSize, tableSize, type));
      } else if (type == STRING) {

        int distinct = sampleOrderOfMagnitude(tableSize, 1, 0.25);

        data.put(attrObj, generateColumn(tableSize, distinct, type));
      }

      data.put(attrObj, generateColumn(tableSize, tableSize, type));
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
