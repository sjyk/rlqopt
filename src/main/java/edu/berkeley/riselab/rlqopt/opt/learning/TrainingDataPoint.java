package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.cost.*;
import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
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

  private HashMap<Attribute, Float> calculateBaseCardinality(Database db, CostModel c) {

    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    HashMap<Attribute, Float> rtn = new HashMap<>();

    for (Attribute a : allAttributes) {
      Relation r = a.relation;

      OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
      TableAccessOperator scan_r;

      try {
        scan_r = new TableAccessOperator(scan_params);
      } catch (OperatorException ex) {
        return null;
      }

      rtn.put(a, (float) c.estimate(scan_r).resultCardinality);
    }

    return rtn;
  }

  public float[] featurize(Database db, CostModel c) {

    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    //HashMap<Attribute, Double> cardMap = calculateBaseCardinality(db, c);

    int n = allAttributes.size();

    float[] vector = new float[n * 3 + 3];
    for (int i = 0; i < n * 3; i++) vector[i] = 0.0f;

    for (Attribute a : oplist[0].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a)] = 1.0f;
      // Math.log(cardMap.get(a) / c.estimate(oplist[0]).resultCardinality);
    }

    for (Attribute a : oplist[1].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a) + n] = 1.0f;
      // Math.log(cardMap.get(a) / c.estimate(oplist[1]).resultCardinality);
    }

    // System.out.println(oplist[3].getVisibleAttributes() + " " + oplist[3]);

    for (Attribute a : oplist[3].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a) + 2 * n] = 1.0f;
    }

    vector[3 * n] = size;

    vector[3 * n + 1] = gcost;

    vector[3 * n + 2] = cost;

    return vector;
  }

  public INDArray featurizeND4j(Database db, CostModel c) {
    float[] vector = featurize(db, c);
    int p = vector.length;

    float[] xBuffer = new float[p - 1];

    for (int ind = 0; ind < vector.length - 1; ind++) xBuffer[ind] = vector[ind];

    return Nd4j.create(xBuffer, new int[] {1, p - 1});
  }
}
