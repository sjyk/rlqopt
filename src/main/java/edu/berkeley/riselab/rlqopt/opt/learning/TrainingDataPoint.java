package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.cost.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class TrainingDataPoint {

  public Operator[] oplist;
  public float cost = 0.0f;
  public float gcost = 0.0f;
  public float size = 0.0f;

  private static boolean selectivityScaling = true;
  private static boolean queryGraphFeatures = true;

  static {
    if (System.getProperty("selScaling") != null) {
      selectivityScaling = Boolean.valueOf(System.getProperty("selScaling"));
    }
    if (System.getProperty("queryGraph") != null) {
      queryGraphFeatures = Boolean.valueOf(System.getProperty("queryGraph"));
    }
    System.out.println("selectivityScaling = " + selectivityScaling);
    System.out.println("queryGraphFeatures = " + queryGraphFeatures);
  }

  public TrainingDataPoint(Operator[] oplist, float cost) {
    this.oplist = oplist;
    this.cost = cost;
  }

  public TrainingDataPoint(Operator[] oplist, float cost, float greedyCost, float size) {
    this.oplist = oplist;
    this.cost = cost;
    this.size = size;
    this.gcost = greedyCost;
  }

  public String toString() {
    return Arrays.toString(oplist) + " => " + cost;
  }

  private HashMap<Attribute, Float> calculateSelCardinality(Database db, Operator in, CostModel c) {
    HashMap<Attribute, Long> selCard = new HashMap<>();
    for (Operator op : in.source) {
      long cardinality = c.estimate(op).resultCardinality;

      for (Attribute attr : op.getVisibleAttributes()) selCard.put(attr, cardinality);
    }

    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    HashMap<Attribute, Float> rtn = new HashMap<>();

    for (Attribute a : allAttributes) {

      if (selCard.containsKey(a)) rtn.put(a, (float) selCard.get(a) / c.cardinality(a));
    }

    return rtn;
  }

  public float[] featurize(Database db, CostModel c) {
    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    HashMap<Attribute, Float> cardMap = new HashMap();

    if (selectivityScaling) cardMap = calculateSelCardinality(db, oplist[3], c);

    int n = allAttributes.size();

    float[] vector = new float[n * 3 + 4];
    for (int i = 0; i < n * 3; i++) vector[i] = 0.0f;

    for (Attribute a : oplist[0].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a)] = 1.0f;
    }

    for (Attribute a : oplist[1].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a) + n] = 1.0f;
    }

    if (queryGraphFeatures) {
      for (Attribute a : oplist[3].getVisibleAttributes()) {

        if (selectivityScaling) vector[allAttributes.indexOf(a) + 2 * n] = cardMap.get(a);
        else vector[allAttributes.indexOf(a) + 2 * n] = 1.0f;
      }
    }

    vector[3 * n] = size;

    vector[3 * n + 1] = gcost;

    vector[3 * n + 2] = 0.0f;

    vector[3 * n + 3] = cost;

    return vector;
  }

  public INDArray featurizeND4j(Database db, CostModel c) {
    float[] vector = featurize(db, c);
    int p = vector.length;
    float[] xBuffer = Arrays.copyOf(vector, p - 1);
    return Nd4j.create(xBuffer, new int[] {1, p - 1});
  }
}
