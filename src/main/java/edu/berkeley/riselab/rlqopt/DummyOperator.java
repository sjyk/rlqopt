package edu.berkeley.riselab.rlqopt;

/** A DummyOperator is one that cannot be executed */
public abstract class DummyOperator extends Operator {

  /** An operator takes as input a number of source operators. Throws an OperatorException */
  public DummyOperator(OperatorParameters params, Operator... source) throws OperatorException {
    super(params, source);
  }
}
