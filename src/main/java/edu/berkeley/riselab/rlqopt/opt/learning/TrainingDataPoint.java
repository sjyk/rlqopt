package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.CostModel;
import java.util.Arrays;
import java.util.LinkedList;

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

  public Double[] featurize(LinkedList<Relation> allRelations, CostModel c) {

    LinkedList<Attribute> allAttributes = new LinkedList();
    for (Relation r : allRelations) allAttributes.addAll(r.attributes());

    int n = allAttributes.size();

    Double[] vector = new Double[n * 2 + 1];
    for (int i = 0; i < n * 2; i++) vector[i] = 0.0;

    for (Attribute a : oplist[0].getVisibleAttributes()) vector[allAttributes.indexOf(a)] = 1.0;

    for (Attribute a : oplist[1].getVisibleAttributes()) vector[allAttributes.indexOf(a) + n] = 1.0;

    vector[2*n] = cost/allRelations.size();

    return vector;
  }
}
