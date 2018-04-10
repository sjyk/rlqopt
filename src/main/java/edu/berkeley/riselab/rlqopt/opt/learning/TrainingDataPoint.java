  package edu.berkeley.riselab.rlqopt.opt.learning;
  import edu.berkeley.riselab.rlqopt.Operator;
  import java.util.Arrays;

  public class TrainingDataPoint{

    public Operator [] oplist;
    public Double cost;

    public TrainingDataPoint(Operator [] oplist, Double cost){

      this.oplist = oplist;
      this.cost = cost;

    }

    public String toString(){
    	return Arrays.toString(oplist) + " => " + cost;
    }

    public Double [] featurize(LinkedList<Relation> allRelations, CostModel c){

    } 

  }