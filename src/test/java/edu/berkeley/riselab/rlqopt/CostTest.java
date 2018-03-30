package edu.berkeley.riselab.rlqopt;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.berkeley.riselab.rlqopt.*;
import edu.berkeley.riselab.rlqopt.relalg.*;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.OperatorException;

import edu.berkeley.riselab.rlqopt.preopt.*;
import edu.berkeley.riselab.rlqopt.opt.*;

import java.util.LinkedList;

/**
 * Unit test for simple App.
 */
public class CostTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CostTest ()
    {
        super("Test of the Relational Algebra Suite");
    }


    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CostTest.class );
    }



    public void testReductionFactors() throws CannotEstimateException
    {
        Relation r = new Relation("a","b","c");
        AttributeStatistics a = new AttributeStatistics(10,0,10);
        //Operator scan = createScan(r);

        Expression e1 = new Expression( Expression.EQUALS, 
                                      r.get("a").getExpression(), 
                                        new Expression("1")
                                      );

    
        assertEquals(0.1, a.estimateReductionFactor(e1), 1e-6);


        Expression e2 = new Expression( Expression.NOT, e1);

        assertEquals(0.9, a.estimateReductionFactor(e2), 1e-6);


        Expression e3 = new Expression( Expression.OR, e1, e2);

        assertEquals(1.0, a.estimateReductionFactor(e3), 1e-6);


        Expression e4 = new Expression( Expression.GREATER_THAN_EQUALS, 
                                      r.get("a").getExpression(), 
                                        new Expression("3")
                                      );
        assertEquals(0.7, a.estimateReductionFactor(e4), 1e-6);

        //System.out.println(a.estimateReductionFactor(e));
        //assertEquals(f.apply(j3).params.expression.toString(), "[equals([R294.a, R302.a]), equals([R297.d, R201.d]), and([equals([R294.c, R297.c]), equals([R294.b, R297.b])])]");
    }


}
