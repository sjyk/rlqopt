package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.CostModel;
import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class TrainingDataPoint {

  public Operator[] oplist;
  public Double cost;

  public TrainingDataPoint(Operator[] oplist, Double cost) {

    this.oplist = oplist;
    this.cost = cost;
  }

  public String toString() {
    return Arrays.toString(oplist) + " => " + cost;
  }

  private HashMap<Attribute, Double> calculateBaseCardinality(Database db, CostModel c) {

    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    HashMap<Attribute, Double> rtn = new HashMap();

    for (Attribute a : allAttributes) {
      Relation r = a.relation;

      OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
      TableAccessOperator scan_r;

      try {
        scan_r = new TableAccessOperator(scan_params);
      } catch (OperatorException ex) {
        return null;
      }

      rtn.put(a, new Double(c.estimate(scan_r).resultCardinality + 0.0));
    }

    return rtn;
  }

  public Double[] featurize(Database db, CostModel c) {

    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    HashMap<Attribute, Double> cardMap = calculateBaseCardinality(db, c);

    int n = allAttributes.size();

    Double[] vector = new Double[n * 2 + 1];
    for (int i = 0; i < n * 2; i++) vector[i] = 0.0;

    for (Attribute a : oplist[0].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a)] = cardMap.get(a) / c.estimate(oplist[0]).resultCardinality;
    }

    for (Attribute a : oplist[1].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a) + n] =
          cardMap.get(a) / c.estimate(oplist[0]).resultCardinality;
    }

    vector[2 * n] = cost;

    return vector;
  }

  public INDArray featurizeND4j(Database db, CostModel c) {
    Double[] vector = featurize(db, c);
    int p = vector.length;

    float[] xBuffer = new float[p - 1];

    for (int ind = 0; ind < vector.length - 1; ind++) xBuffer[ind] = vector[ind].floatValue();

    return Nd4j.create(xBuffer, new int[] {1, p - 1});
  }
}
