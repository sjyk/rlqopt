package edu.berkeley.riselab.rlqopt.workload;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.Histogram;
import edu.berkeley.riselab.rlqopt.cost.TableStatisticsModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DatasetGenerator {

  public static final int NUMBER = 0;
  public static final int STRING = 1;
  public static final int DATE = 2;

  private Random random = new Random();

  private int numAttributes;
  private int maxTableSize;
  private int histogramRes;

  private LinkedList<Integer> attrTypes;

  private Database db;
  private TableStatisticsModel ts;

  private Set<String> validStringColumnSpecs = new HashSet<>(Arrays.asList("uuid", "us_state"));
  private String stringColumnSpec = "uuid";

  public DatasetGenerator(
      int numRelations,
      int numAttributes,
      int maxTableSize,
      int histogramRes,
      String stringColumnSpec) {

    this.numAttributes = numAttributes;
    attrTypes = generateAttributes();
    this.maxTableSize = maxTableSize;
    this.histogramRes = histogramRes;
    assert validStringColumnSpecs.contains(stringColumnSpec);
    this.stringColumnSpec = stringColumnSpec;

    db = new Database();
    ts = new TableStatisticsModel();

    for (int i = 0; i < numRelations; i++) {
      Relation r = generateRelation();
      db.add(r);
      Map<Attribute, Histogram> map = generateData(r);
      ts.putAll(map);
    }
  }

  public DatasetGenerator(int numRelations, int numAttributes, int maxTableSize, int histogramRes) {
    this(numRelations, numAttributes, maxTableSize, histogramRes, "uuid");
  }

  private Histogram generateColumn(int size, int distinct, int type) {
    Random rand = random;
    Histogram h;

    switch (type) {
      case NUMBER:
        h = new Histogram(histogramRes, 0, maxTableSize, distinct, "number");
        break;
      case STRING:
        h = new Histogram(histogramRes, distinct, "string");
        break;
      case DATE:
        h = new Histogram(histogramRes, 0, maxTableSize, distinct, "date");
        break;
      default:
        return null;
    }

    ArrayList<String> distinctKeys = new ArrayList<>(distinct);
    ArrayList<Integer> distinctIntegerKeys = new ArrayList<>(distinct);
    ArrayList<String> result = new ArrayList<>(size);

    for (int i = 0; i < distinct; i++) {
      switch (type) {
        case NUMBER:
          int num = randomNumber();
          distinctKeys.add(num + "");
          distinctIntegerKeys.add(num);
          break;
        case STRING:
          if (stringColumnSpec.equals("us_state")) {
            // If the alphabet (e.g., US states) has size < "distinct", currently this generator
            // will repeat elements in a round-robin fashion, so they are not really distinct.  All
            // elements including possible repeats will then be Gaussian-weighted.
            String randString = allUsStates.get(i % allUsStates.size());
            distinctKeys.add(randString);
            distinctIntegerKeys.add(i);
          } else {
            // Draw a string from the alphabet specified by stringColumnSpec.
            String randString = randomString();
            distinctKeys.add(randString);
            distinctIntegerKeys.add(i);
          }
          break;
        case DATE:
          distinctKeys.add(randomDate());
          break;
      }
    }

    if (type == NUMBER) {
      ArrayList<Double> weights = generateGaussWeights(distinctIntegerKeys);

      for (int i = 0; i < size; i++) h.put(sample(distinctIntegerKeys, weights) + 0.0);
    } else if (type == STRING) {
      //      System.out.println("distinctIntegerKeys: " +
      // Arrays.toString(distinctIntegerKeys.toArray()));
      //      System.out.println("distinctKeys: " + Arrays.toString(distinctKeys.toArray()));

      // We want "size" strings from the "distinctKeys" alphabet, that is Gaussian-weighted.
      // Index i gets assigned weights gaussian_pdf(i).
      ArrayList<Double> weights = generateGaussWeights(distinctIntegerKeys);
      for (int i = 0; i < size; i++) {
        int sampledIndex = sample(distinctIntegerKeys, weights);
        String sampledString = distinctKeys.get(sampledIndex);
        h.putString(sampledString);
      }
    } else {
      for (int i = 0; i < size; i++) h.put(rand.nextInt(distinctKeys.size()) + 0.0);
    }

    return h;
  }

  private ArrayList<Double> generateGaussWeights(ArrayList<Integer> nums) {
    double sum = 0.0;
    for (Integer i : nums) sum += i;

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
    assert nums.size() == weights.size();
    Random rand = random;
    double randomWeight = rand.nextDouble();

    for (int i = 0; i < nums.size(); i++) {
      randomWeight = randomWeight - weights.get(i);

      if (randomWeight <= 0) return nums.get(i);
    }

    return nums.get(nums.size() - 1);
  }

  private static List<String> allUsStates =
      Arrays.asList(
          "Alabama",
          "Alaska",
          "Arizona",
          "Arkansas",
          "California",
          "Colorado",
          "Connecticut",
          "Delaware",
          "Florida",
          "Georgia",
          "Hawaii",
          "Idaho",
          "Illinois",
          "Indiana",
          "Iowa",
          "Kansas",
          "Kentucky",
          "Louisiana",
          "Maine",
          "Maryland",
          "Massachusetts",
          "Michigan",
          "Minnesota",
          "Mississippi",
          "Missouri",
          "Montana",
          "Nebraska",
          "Nevada",
          "New Hampshire",
          "New Jersey",
          "New Mexico",
          "New York",
          "North Carolina",
          "North Dakota",
          "Ohio",
          "Oklahoma",
          "Oregon",
          "Pennsylvania",
          "Rhode Island",
          "South Carolina",
          "South Dakota",
          "Tennessee",
          "Texas",
          "Utah",
          "Vermont",
          "Virginia",
          "Washington",
          "West Virginia",
          "Wisconsin",
          "Wyoming");

  private String randomString() {
    if (stringColumnSpec.equals("us_state")) {
      int size = allUsStates.size();
      return allUsStates.get(random.nextInt(size));
    }
    return UUID.randomUUID().toString().replace("-", "");
  }

  private int randomNumber() {
    return random.nextInt(maxTableSize);
  }

  private String randomDate() {
    return randomNumber() + "";
  }

  private LinkedList<Integer> generateAttributes() {
    Random rand = random;

    LinkedList<Integer> attrs = new LinkedList();

    for (int i = 0; i < numAttributes; i++) {
      int type = rand.nextInt(3);
      attrs.add(type);
    }

    return attrs;
  }

  public Relation generateRelation() {

    Random rand = random;

    int numAttributesInRel = rand.nextInt(numAttributes) + 2;

    HashSet<String> attributes = new HashSet();

    for (int j = 0; j < numAttributesInRel; j++) {
      attributes.add(String.valueOf(rand.nextInt(numAttributes)));
    }

    return new Relation(attributes.toArray(new String[attributes.size()]));
  }

  private int sampleOrderOfMagnitude(int maxSize, int minSize, double decay) {

    Random rand = random;
    while (maxSize >= minSize) { // hyperparam

      if (rand.nextDouble() > decay) break;

      maxSize /= 10;
    }

    return Math.max(maxSize, minSize);
  }

  public HashMap<Attribute, Histogram> generateData(Relation r) {

    HashMap<Attribute, Histogram> data = new HashMap();

    boolean primary = true;
    Random rand = random;

    int tableSize =
        sampleOrderOfMagnitude(
            maxTableSize, 10, 0.5); // maxTableSize / divisors[rand.nextInt(5)] + 1;

    for (String attr : r) {
      int ind = Integer.parseInt(attr);
      int type = attrTypes.get(ind);

      if ((type == STRING) && primary) {
        Histogram hist = generateColumn(tableSize, tableSize, type);
        data.put(r.get(attr), hist);
        primary = false;
      } else if (type == STRING) {
        Histogram hist = generateColumn(tableSize, tableSize, type);
        data.put(r.get(attr), hist);
      } else {
        data.put(r.get(attr), generateColumn(tableSize, tableSize, type));
      }
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
