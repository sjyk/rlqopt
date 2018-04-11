package edu.berkeley.riselab.rlqopt.opt.learning;

import java.util.LinkedList;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.opt.CostModel;
import edu.berkeley.riselab.rlqopt.opt.learning.LearningPlanner;
import java.io.*;
import java.util.Arrays;
import edu.berkeley.riselab.rlqopt.Operator;

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

	public void generate(LinkedList<Operator> workload, int t){

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

	public void generate(Operator query, int t){

		LinkedList<Operator> workload = new LinkedList();
		workload.add(query);
		generate(workload,t);
	}


}