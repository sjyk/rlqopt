package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.cost.*;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

public class TrainingDataGenerator {

  Database db;
  String output;
  CostModel c;
  TrainingPlanner planner;
  double scaling;

  public TrainingDataGenerator(Database db, String output, CostModel c, TrainingPlanner planner) {
    this.output = output;
    this.db = db;
    this.c = c;
    this.planner = planner;
    // this.scaling = scaling;
  }

  public void generateFile(LinkedList<Operator> workload, int t) {

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(output));

      for (int i = 0; i < t; i++) for (Operator query : workload) planner.plan(query, c);

      for (TrainingDataPoint tr : planner.getTrainingData()) {
        writer.write(Arrays.toString(tr.featurize(db, c)).replace("[", "").replace("]", "") + "\n");
      }

      writer.close();
    } catch (IOException ex) {
    }
    ;
  }

  public DataSet generateDataSet(LinkedList<Operator> workload, int t) {

    for (int i = 0; i < t; i++) {
      for (Operator query : workload) {
        System.out.println(query);
        planner.plan(query, c);
      }
    }

    LinkedList<INDArray> trainingExamples = new LinkedList();
    LinkedList<INDArray> reward = new LinkedList();

    int p = 0;

    for (TrainingDataPoint tr : planner.getTrainingData()) {
      Double[] vector = tr.featurize(db, c);
      p = vector.length;

      float[] xBuffer = new float[p - 1];

      for (int ind = 0; ind < vector.length - 1; ind++) xBuffer[ind] = vector[ind].floatValue();

      float[] yBuffer = new float[1];

      if (Double.isInfinite(vector[vector.length - 1].floatValue())) continue;

      // System.out.println("--" + Math.log(vector[vector.length - 1].floatValue()) + "," +
      // vector[vector.length - 1].floatValue());
      yBuffer[0] = (float) (Math.log(vector[vector.length - 1].floatValue())); // todo fix scaling
      // System.out.println(yBuffer[0]);

      trainingExamples.add(Nd4j.create(xBuffer, new int[] {1, p - 1}));
      reward.add(Nd4j.create(yBuffer, new int[] {1, 1}));
    }

    int n = trainingExamples.size();

    return new DataSet(
        Nd4j.create(trainingExamples, new int[] {n, p - 1}), Nd4j.create(reward, new int[] {n, 1}));
  }

  public DataSetIterator generateDataSetIterator(LinkedList<Operator> workload, int t) {

    DataSet dataSet = generateDataSet(workload, t);
    List<DataSet> listDs = dataSet.asList();
    return new ListDataSetIterator(listDs, 1000); // todo hyperparameter
  }

  public DataSetIterator generateDataSetIterator(Operator query, int t) {
    LinkedList<Operator> workload = new LinkedList();
    workload.add(query);
    return generateDataSetIterator(workload, t);
  }

  public void generateFile(Operator query, int t) {
    LinkedList<Operator> workload = new LinkedList();
    workload.add(query);
    generateFile(workload, t);
  }
}
