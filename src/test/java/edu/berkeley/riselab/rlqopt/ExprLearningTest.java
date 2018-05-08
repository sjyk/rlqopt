package edu.berkeley.riselab.rlqopt;

import edu.berkeley.riselab.rlqopt.cost.Cost;
import edu.berkeley.riselab.rlqopt.cost.CostModel;
import edu.berkeley.riselab.rlqopt.cost.TableStatisticsModel;
import edu.berkeley.riselab.rlqopt.workload.DatasetGenerator;
import edu.berkeley.riselab.rlqopt.workload.WorkloadGeneratorEasy;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

public class ExprLearningTest extends TestCase {

  // Assume op is one of "=", ">", ">=", "<", "<=" for now.
  private List<String> allOps = Arrays.asList("equals", "gt", "gte", "lt", "lte");

  // Featurize "SELECT * FROM R WHERE R.<attr> <op> <numeric literal>" into
  //    [database description] [expr description]
  // where the database description is
  //    [ relation 1's cardinality ] ... [relation N's card.]
  // and the expression description is
  //    [ 1-hot of "R.<attr>" ] [ 1-hot of op type ] [ the literal ]
  private double[] featurizeExpression(
      Database db, CostModel c, Operator select, TableStatisticsModel stats)
      throws OperatorException {
    final int numRelations = db.size();
    List<Attribute> allAttributes = db.getAllAttributes();
    final int allAttrDim = allAttributes.size();
    final int allOpDim = allOps.size();
    int featureDim = numRelations + allAttrDim + allOpDim + 1;
    double[] featVec = new double[featureDim];
    Arrays.fill(featVec, 0.0);
    Expression expr = select.params.expression.getFirst();

    // Db desc.
    int cnt = 0;
    for (Relation r : db) {
      Cost cost = c.estimate(r.scan());
      featVec[cnt] = cost.resultCardinality;
      assert cost.resultCardinality == stats.tableAccessOperator(r.scan()).resultCardinality;
      ++cnt;
    }

    // Expr desc.
    // TODO: should check "select" is of the above form.
    // Attr.
    Attribute attr = expr.getVisibleAttributes().getFirst();
    featVec[allAttributes.indexOf(attr) + numRelations] = 1.0;
    // Op.
    featVec[allOps.indexOf(expr.op) + numRelations + allAttrDim] = 1.0;
    // Literal.
    featVec[featureDim - 1] = Double.valueOf(expr.children.getLast().toString());
    return featVec;
  }

  private void writeToFile(
      String pathname, List<double[]> features, List<Double> labels, int start, int len) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(pathname));

      for (int i = start; i < start + len; ++i) {
        double[] feat = features.get(i);
        for (double num : feat) {
          writer.write(String.valueOf(num));
          writer.write(',');
        }
        writer.write(String.valueOf(labels.get(i)));
        writer.write('\n');
        ++i;
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void testGenerateExpressions() throws OperatorException {
    // Config.
    final int numRelations = 5;
    final int numAttrs = 10;
    final int maxTableSize = 100;
    final int numHistogramBuckets = 100;
    final int numTraining = 1000;
    final int numTest = 2000;
    final int numExamples = numTraining + numTest;

    DatasetGenerator d =
        new DatasetGenerator(numRelations, numAttrs, maxTableSize, numHistogramBuckets);
    WorkloadGeneratorEasy workload = new WorkloadGeneratorEasy(d);
    Database db = workload.getDatabase();
    CostModel c = workload.getStatsModel();
    TableStatisticsModel statsModel = workload.getStatsModel();
    System.out.println("All attrs in db: " + db.getAllAttributes().toString());

    final int totalAttrsInDb = db.getAllAttributes().size();
    final String desc =
        numRelations
            + "rel-"
            + numAttrs
            + "numAttrs-"
            + totalAttrsInDb
            + "totalAttrs-"
            + maxTableSize
            + "maxTblSize-"
            + numHistogramBuckets
            + "buckets-"
            + numExamples;
    final String trainFile = "data/train-" + desc + ".csv";
    final String testFile = "data/test-" + desc + ".csv";

    List<double[]> features = new ArrayList<>(numExamples);
    List<Double> labels = new ArrayList<>(numExamples);

    // Generate all.
    for (int i = 0; i < numExamples; ++i) {
      // SELECT * FROM R WHERE R.attr = int_literal
      Operator select = workload.generateSingleSelection();
      double[] feat = featurizeExpression(db, c, select, statsModel);
      double label = c.estimate(select).resultCardinality;

      features.add(feat);
      labels.add(label);
    }

    // Each line in csv: feat vec; label.
    writeToFile(trainFile, features, labels, 0, numTraining);
    writeToFile(testFile, features, labels, numTraining, numTest);
  }
}
