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
  public Double cost = 0.0;
  public Double gcost = 0.0;
  public Double size = 0.0;

  private final boolean selectivityScaling = true;
  private final boolean queryGraphFeatures = true;


  public TrainingDataPoint(Operator[] oplist, Double cost) {

    this.oplist = oplist;
    this.cost = cost;
  }

  public TrainingDataPoint(Operator[] oplist, Double cost, Double greedyCost, Double size) {

    this.oplist = oplist;
    this.cost = cost;
    this.size = size;
    this.gcost = greedyCost;
  }

  public String toString() {
    return Arrays.toString(oplist) + " => " + cost;
  }

  private HashMap<Attribute, Double> calculateSelCardinality(Database db, Operator in, CostModel c) {

    HashMap<Attribute, Long> selCard = new HashMap<>();

    for (Operator op: in.source)
    {
      long cardinality = c.estimate(op).resultCardinality;
      for (Attribute attr: op.getVisibleAttributes())
        selCard.put(attr, cardinality);
    }

    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    HashMap<Attribute, Double> rtn = new HashMap<>();

    for (Attribute a : allAttributes) {

      if (selCard.containsKey(a))
        rtn.put(a, (selCard.get(a) + 0.0)/c.cardinality(a));
    }

    return rtn;
  }

  public Double[] featurize(Database db, CostModel c) {

    LinkedList<Attribute> allAttributes = db.getAllAttributes();
    HashMap<Attribute, Double> cardMap = calculateSelCardinality(db, oplist[3], c);

    int n = allAttributes.size();

    Double[] vector = new Double[n * 3 + 4];
    for (int i = 0; i < n * 3; i++) vector[i] = 0.0;

    for (Attribute a : oplist[0].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a)] = 1.0;

    }

    for (Attribute a : oplist[1].getVisibleAttributes()) {

      vector[allAttributes.indexOf(a) + n] = 1.0;
    }


    if (queryGraphFeatures){
      for (Attribute a : oplist[3].getVisibleAttributes()) {

      if (selectivityScaling)
        vector[allAttributes.indexOf(a) + 2 * n] = cardMap.get(a);
      else
        vector[allAttributes.indexOf(a) + 2 * n] = 1.0;

      }
    }


    vector[3 * n] =  size;

    vector[3 * n + 1] = gcost;

    vector[3 * n + 2] = 0.0;

    vector[3 * n + 3] = cost;

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
