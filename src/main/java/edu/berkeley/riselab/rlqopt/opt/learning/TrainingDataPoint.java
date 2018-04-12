package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.CostModel;
import java.util.Arrays;
import java.util.LinkedList;

import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

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

  public Double[] featurize(Database db, CostModel c) {

    LinkedList<Attribute> allAttributes = db.getAllAttributes();

    int n = allAttributes.size();

    Double[] vector = new Double[n * 2 + 1];
    for (int i = 0; i < n * 2; i++) vector[i] = 0.0;

    for (Attribute a : oplist[0].getVisibleAttributes()) vector[allAttributes.indexOf(a)] = 1.0;

    for (Attribute a : oplist[1].getVisibleAttributes()) vector[allAttributes.indexOf(a) + n] = 1.0;

    vector[2*n] = cost/db.size();

    return vector;
  }

  public INDArray featurizeND4j(Database db, CostModel c) {
    Double [] vector = featurize(db, c);
    int p = vector.length;

    float [] xBuffer = new float[p-1];

    for (int ind=0; ind<vector.length - 1; ind++)
        xBuffer[ind] = vector[ind].floatValue();

    return Nd4j.create(xBuffer,new int[]{1,p-1});
  }

}
