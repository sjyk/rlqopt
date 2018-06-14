package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.Relation;
import edu.berkeley.riselab.rlqopt.relalg.*;
import java.util.HashMap;
import java.util.LinkedList;

public class HistogramOperations {

  public static HistogramRelation project(ExpressionList exp, HistogramRelation in) {
    LinkedList<Attribute> attrs = exp.getAllVisibleAttributes();
    HashMap<Attribute, Histogram> data = new HashMap<Attribute, Histogram>();
    for (Attribute a : attrs) data.put(a, in.get(a));
    return new HistogramRelation(data);
  }

  public static HistogramRelation groupBy(ExpressionList exp, HistogramRelation in) {
    LinkedList<Attribute> attrs = exp.getAllVisibleAttributes();
    HashMap<Attribute, Histogram> data = new HashMap<Attribute, Histogram>();
    for (Attribute a : attrs) data.put(a, in.get(a));
    return new HistogramRelation(data);
  }

  public static HistogramRelation select(Expression exp, HistogramRelation in) {
    LinkedList<Attribute> attrs = exp.getVisibleAttributes();
    Attribute a = attrs.get(0);
    HashMap<Attribute, Histogram> data = new HashMap<Attribute, Histogram>();
    Histogram selHist = in.get(a);
    Histogram selected = selHist.filter(exp);
    data.put(a, selected);

    double rf = (selected.getCount() + 0.0) / in.count();

    for (Attribute b : in.keySet()) {

      if (b != a) data.put(b, in.get(b).scale(rf));
    }

    return new HistogramRelation(data);
  }

  public static HistogramRelation cartesian(HistogramRelation left, HistogramRelation right) {
    HashMap<Attribute, Histogram> data = new HashMap<Attribute, Histogram>();

    int leftCount = left.count();
    int rightCount = right.count();

    for (Attribute a : left.keySet()) {
      Histogram leftHist = left.get(a);
      leftHist = leftHist.scale(rightCount);
      data.put(a, leftHist);
    }

    for (Attribute a : right.keySet()) {
      Histogram rightHist = right.get(a);
      rightHist = rightHist.scale(leftCount);
      data.put(a, rightHist);
    }

    return new HistogramRelation(data);
  }

  private static LinkedList<Attribute>[] getLeftRightAttributes(Expression e) {

    LinkedList<Attribute> allAttributes = e.getVisibleAttributes();
    HashMap<Relation, LinkedList<Attribute>> leftAndRight = new HashMap();

    for (Attribute a : allAttributes) {
      Relation attrRel = a.relation;
      if (!leftAndRight.containsKey(attrRel)) {
        leftAndRight.put(attrRel, new LinkedList());
      }

      LinkedList<Attribute> split = leftAndRight.get(attrRel);
      split.add(a);
    }

    LinkedList<Attribute>[] rtn = new LinkedList[2];

    int count = 0;
    for (Relation r : leftAndRight.keySet()) {
      rtn[count] = leftAndRight.get(r);
      count++;
    }

    return rtn;
  }

  public static HistogramRelation join(Expression e, HistogramRelation left, HistogramRelation right) {
    return cartesian(left, right);
  }

  public static HistogramRelation merge(HistogramRelation l, HistogramRelation r) {

    HashMap<Attribute, Histogram> data = new HashMap<Attribute, Histogram>();

    for (Attribute a : l.keySet()) data.put(a, l.get(a));

    for (Attribute a : r.keySet()) data.put(a, r.get(a));

    return new HistogramRelation(data);
  }

  public static HistogramRelation equalize(HistogramRelation h) {
    HashMap<Attribute, Histogram> data = new HashMap<Attribute, Histogram>();
    int countMin = Integer.MAX_VALUE;
    int countKeyMin = Integer.MAX_VALUE;

    for (Attribute a : h.keySet()) {
      //System.out.println(countMin);
      countMin = Math.min(h.get(a).getCount(), countMin);

      if (a.isKey)
        countKeyMin = Math.min(h.get(a).getCount(), countKeyMin);
    }

    int count = Math.min(countMin, countKeyMin);

    //System.out.println( "a:" + count);


    for (Attribute a : h.keySet()) {
      Histogram ahist = h.get(a);
      double scaling = (count + 0.0) / Math.max(ahist.getCount(), 1);
      Histogram scaledHist = ahist.scale(scaling);
      //System.out.println( "b:" + scaledHist.getCount());
      data.put(a, scaledHist );
    }

    

    return new HistogramRelation(data);
  }

  public static HistogramRelation eval(HistogramRelation hist, Operator op) {

    HistogramRelation h = hist.copy();

    if (op instanceof TableAccessOperator) return project(op.params.expression, h);
    else if (op instanceof SelectOperator)
      return select(op.params.expression.get(0), eval(h, op.source.get(0)));
    else if (op instanceof GroupByOperator)
      return project(op.params.secondary_expression, eval(h, op.source.get(0)));
    else if (op instanceof CartesianOperator)
      return cartesian(eval(h, op.source.get(0)), eval(h, op.source.get(1)));
    else if (op instanceof JoinOperator)
      return join(
          op.params.expression.get(0), eval(h, op.source.get(0)), eval(h, op.source.get(1)));
    else return h;
  }
}
