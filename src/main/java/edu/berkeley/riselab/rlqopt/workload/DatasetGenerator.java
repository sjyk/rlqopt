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

    //System.out.println("test");

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

    

    if(type == NUMBER){
      ArrayList<Double> weights = generateGaussWeights(distinctIntegerKeys);

      for (int i = 0; i < size; i++)
        h.put(sample(distinctIntegerKeys, weights)+ 0.0);
    }
    else{

      for (int i = 0; i < size; i++)
        h.put(rand.nextInt(distinctKeys.size()) + 0.0);

    }

    return h;
  }

  private ArrayList<Double> generateGaussWeights(ArrayList<Integer> nums){

    double sum = 0.0;
    for(Integer i: nums)
      sum += i + 0.0;

    double mean = sum / nums.size();
    double std = maxTableSize/4.0;

    ArrayList<Double> unnormalizedWeights = new ArrayList(nums.size());
    ArrayList<Double> weights = new ArrayList(nums.size());

    double z = 0.0;
    for(Integer i: nums)
    {
      double x = Math.exp(- Math.pow( (i-mean) , 2 )/std);
      z += x;
      unnormalizedWeights.add(x);
    }

    for(Double x: unnormalizedWeights)
      weights.add(x/z);

    return weights;
  }


  private Integer sample(ArrayList<Integer> nums, ArrayList<Double> weights){

    Random rand = new Random();
    double randomWeight = rand.nextDouble();

    for (int i=0; i < nums.size(); i++){
      randomWeight = randomWeight - weights.get(i);

      if (randomWeight <= 0)
        return nums.get(i);
    }
    
    return nums.get(nums.size() - 1);

  }


  private String randomString() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private int randomNumber() {
    return new Random().nextInt(maxTableSize);
  }

  private String randomDate() {
    return randomNumber() + "";
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

  private int sampleOrderOfMagnitude(int maxSize, int minSize, double decay){

    Random rand = new Random();
    while (maxSize >= minSize){ //hyperparam
      
      if(rand.nextDouble() > decay)
        break;

      maxSize /= 10;
    }

    return Math.max(maxSize,minSize);

  }


  public HashMap<Attribute, Histogram> generateData(Relation r) {

    HashMap<Attribute, Histogram> data = new HashMap();

    boolean primary = true;
    Random rand = new Random();

    int tableSize = sampleOrderOfMagnitude(maxTableSize, 10, 0.5); //maxTableSize / divisors[rand.nextInt(5)] + 1;

    //System.out.println(tableSize);

    for (String attr : r) {
      int ind = Integer.parseInt(attr);
      int type = attrTypes.get(ind);

      if ((type == STRING) && primary) {
        data.put(r.get(attr), generateColumn(tableSize, tableSize, type));
        primary = false;
      } else if (type == STRING) {

        int distinct = sampleOrderOfMagnitude(tableSize, 1, 0.5);

        data.put(
            r.get(attr),
            generateColumn(tableSize, distinct, type));
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
