package edu.berkeley.riselab.rlqopt.opt;

import edu.berkeley.riselab.rlqopt.Expression;
import java.util.HashSet;
import edu.berkeley.riselab.rlqopt.Attribute;

public class AttributeStatistics {

  public long distinctValues;
  public long cardinality;
  public long minVal;
  public long maxVal;
  public boolean isNumber;

  public AttributeStatistics(long distinctValues, long cardinality) {

    this.isNumber = false;
    this.distinctValues = distinctValues;
    this.cardinality = cardinality;

  }

  public AttributeStatistics(long distinctValues, long cardinality, long minVal, long maxVal) {

    this.isNumber = true;
    this.distinctValues = distinctValues;
    this.cardinality = cardinality;
    this.minVal = minVal;
    this.maxVal = maxVal;
  }

  private double clip10(double val) {
    return Math.max(Math.min(val, 1.0), 0.0);
  }

  // returns error if you can't estimate
  public double estimateReductionFactor(Expression e) throws CannotEstimateException {
 
        HashSet<Attribute> attrSet = e.getVisibleAttributeSet();

        if (attrSet.size() == 1)
          return estimateReductionFactorSingle(e);
        else
          throw new CannotEstimateException(e);

  }

  private double estimateReductionFactorSingle(Expression e) throws CannotEstimateException{

      if (e.op.equals(Expression.EQUALS)) {
        
        return clip10(1.0 / distinctValues);

      }
      else if (e.op.equals(Expression.GREATER_THAN) || e.op.equals(Expression.GREATER_THAN_EQUALS)){
          
        Expression child = e.children.get(1);
        double val = Double.parseDouble(child.op);

        return clip10((maxVal - val) / (maxVal - minVal));
      }
      else if (e.op.equals(Expression.LESS_THAN) || e.op.equals(Expression.LESS_THAN_EQUALS)){

        Expression child = e.children.get(1);
        double val = Double.parseDouble(child.op);

        return clip10((val - minVal) / (maxVal - minVal));
      }
      else
        throw new CannotEstimateException(e);

  }


}
