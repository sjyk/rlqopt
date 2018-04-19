package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.relalg.GroupByOperator;
import edu.berkeley.riselab.rlqopt.relalg.ProjectOperator;
import edu.berkeley.riselab.rlqopt.relalg.TableAccessOperator;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.InvocationTargetException;

/** Operator class- this class defines an abstract relational operator */
public abstract class Operator {

  public List<Operator> source; // data structure that holds the source
  public OperatorParameters params;

  /** An operator takes as input a number of source operators. Throws an OperatorException */
  public Operator(OperatorParameters params, Operator... source) throws OperatorException {

    if (!isValid(params, source)) throw new OperatorException(source);

    // init the operator
    this.source = new LinkedList<Operator>();
    for (Operator o : source) this.source.add(o);

    this.params = params;
  }


  public LinkedList<Attribute> getVisibleAttributes() {

    LinkedList<Attribute> visibleAttrs = new LinkedList<Attribute>();

    if (this instanceof TableAccessOperator) {

      visibleAttrs.addAll(params.expression.getAllVisibleAttributes());
      return visibleAttrs;

    } else if (this instanceof ProjectOperator) {

      visibleAttrs.addAll(params.expression.getAllVisibleAttributes());
      return visibleAttrs;
    } else if (this instanceof GroupByOperator) {

      visibleAttrs.addAll(params.secondary_expression.getAllVisibleAttributes());
      return visibleAttrs;
    }

    for (Operator o : source) visibleAttrs.addAll(o.getVisibleAttributes());

    return visibleAttrs;
  }

  // Validates the inputs to the operator
  public abstract boolean isValid(OperatorParameters params, Operator... source);

  public Operator copy() throws OperatorException {

    Operator op = null;
     try{
          op = this.getClass().getDeclaredConstructor(OperatorParameters.class, Operator[].class).newInstance(this.params, this.source.toArray(new Operator [this.source.size()]));
     }
     catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (SecurityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (NoSuchMethodException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    op.source = new LinkedList();

     for (Operator child: this.source)
        op.source.add(child.copy());
     
     return op;
  }

  // override
  public String toString() {
    String className = this.getClass().getSimpleName();

    if (source.size() == 0) return className + "(" + params.expression + ")";
    else {
      return className + "(" + source.toString() + ")";
    }
  }
}
