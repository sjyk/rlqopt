package edu.berkeley.riselab.rlqopt.opt.learning;

import java.util.LinkedList;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.CostModel;
import edu.berkeley.riselab.rlqopt.opt.learning.LearningPlanner;
import java.io.*;
import java.util.Arrays;
import edu.berkeley.riselab.rlqopt.Operator;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

public class TrainingDataGenerator {

	LinkedList<Relation> allRelations;
	String output;
	CostModel c;
	LearningPlanner planner;

	public TrainingDataGenerator(LinkedList<Relation> allRelations, String output, CostModel c, LearningPlanner planner)
	{
		this.output = output;
		this.allRelations = allRelations;
		this.c = c;
		this.planner = planner;

	}

	public void generateFile(LinkedList<Operator> workload, int t){

		try{
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));

		for (int i=0; i< t; i++)
			for(Operator query: workload)
				planner.plan(query, c);

		for (TrainingDataPoint tr : planner.getTrainingData()) 
		{
			writer.write(Arrays.toString(tr.featurize(allRelations, c)).replace("[","").replace("]","")+"\n");
		}

    	writer.close();
    	}
    	catch(IOException ex){};

	}

	public DataSet generateDataSet(LinkedList<Operator> workload, int t){

		for (int i=0; i< t; i++)
			for(Operator query: workload)
				planner.plan(query, c);

		LinkedList<INDArray>  trainingExamples = new LinkedList();
		LinkedList<INDArray>  reward = new LinkedList();

		int p = 0;

		for (TrainingDataPoint tr : planner.getTrainingData()) 
		{
			Double [] vector = tr.featurize(allRelations, c);
			p = vector.length;

			float [] xBuffer = new float[p-1];
			float [] yBuffer = new float[1];

			trainingExamples.add(Nd4j.create(xBuffer,new int[]{1,p-1}));						
			reward.add(Nd4j.create(yBuffer,new int[]{1,1}));
		}

		int n = trainingExamples.size();

		return new DataSet(Nd4j.create(trainingExamples, new int []{n,p-1}), Nd4j.create(reward, new int []{n,1}));

	}

	public void generateFile(Operator query, int t){

		LinkedList<Operator> workload = new LinkedList();
		workload.add(query);
		generateFile(workload,t);
	}


}