package edu.berkeley.riselab.rlqopt.cost;

import edu.berkeley.riselab.rlqopt.Expression;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Histogram {

  protected double[][] histogramBuckets;

  private final int LOW = 0;
  private final int HIGH = 1;
  private final int COUNT = 2;

  private double min;
  private double max;

  protected int buckets;
  private int z;
  private int distinctCount;

  private String dtype;
  // TODO: make this a Set instead of a List?
  public List<String> seenStrings;

  public Histogram(int buckets, double min, double max, int distinctCount, String dtype) {
    this.buckets = buckets;
    this.min = min;
    this.max = max;

    this.histogramBuckets = new double[buckets][3];
    this.z = 0;
    this.distinctCount = distinctCount;
    this.dtype = dtype;
    if (dtype.equals("string")) {
      seenStrings = new ArrayList<>();
    }

    initialize();
  }

  // String histogram.  Min and max depend on an internal hashing param.
  public Histogram(int buckets, int distinctCount, String dtype) {
    this(buckets, -STRING_HASH_BUCKETS, STRING_HASH_BUCKETS, distinctCount, dtype);
    assert isStringColumn();
  }

  public boolean isStringColumn() {
    return dtype.equals("string");
  }

  /** Store a string. */
  public void putString(String val) {
    assert isStringColumn();
    put(featurizeStringLiteral(val));
    seenStrings.add(val);
  }

  /** Sample, uniformly at random with occurrences considered, a string from the seen ones. */
  public String sampleString(Random random) {
    assert isStringColumn();
    assert (!seenStrings.isEmpty());
    return seenStrings.get(random.nextInt(seenStrings.size()));
  }

  private static final int STRING_HASH_BUCKETS = 10000;

  public static double featurizeStringLiteral(String literal) {
    return literal.hashCode() % STRING_HASH_BUCKETS;
  }

  private double featurizeLiteral(String literal) {
    if (dtype.equals("string")) {
      return featurizeStringLiteral(literal);
    }
    return Double.parseDouble(literal); // Numeric, date.
  }

  public Histogram copy() {
    Histogram result = new Histogram(buckets, min, max, distinctCount, dtype);
    result.z = this.z;
    // Buckets.
    double[][] newBuckets = new double[buckets][3];
    double step = (max - min) / buckets;
    for (int i = 0; i < buckets; i++) {
      newBuckets[i][LOW] = min + step * i;
      newBuckets[i][HIGH] = min + step * (i + 1);
      newBuckets[i][COUNT] = histogramBuckets[i][COUNT];
    }
    result.histogramBuckets = newBuckets;
    // Seen strings.
    if (seenStrings != null) {
      result.seenStrings.addAll(seenStrings);
    }
    return result;
  }

  private void initialize() {

    double step = (max - min) / buckets;

    for (int i = 0; i < buckets; i++) {
      histogramBuckets[i][LOW] = min + step * i;
      histogramBuckets[i][HIGH] = min + step * (i + 1);
      histogramBuckets[i][COUNT] = 0.0;
    }
  }

  private void reset() {
    for (int i = 0; i < buckets; i++) {
      histogramBuckets[i][COUNT] = 0.0;
    }
    z = 0;
    distinctCount = 0;
  }

  private int val2index(double value) {
    double step = (max - min) / buckets;
    return Math.max(Math.min((int) ((value - min) / step), buckets - 1), 0);
  }

  public String dtype() {
    return dtype;
  }

  public void put(double value) {
    histogramBuckets[val2index(value)][COUNT]++;
    this.z++;
  }

  public double max() {
    return histogramBuckets[buckets - 1][HIGH];
  }

  public double min() {
    return histogramBuckets[0][LOW];
  }

  public int getDistinctCount() {
    return distinctCount;
  }

  public int getCount() {
    return z;
  }

  /** Handles "e" which is (1) a selection, and (2) a selection on a numeric column. */
  public Histogram filter(Expression e) {
    String literal = e.children.get(1).op;
    Histogram result = copy();

    double value = featurizeLiteral(literal);

    int index = val2index(value);

    if (e.op.equals(Expression.EQUALS)) {

      double[] tmp = result.histogramBuckets[index].clone();

      result.reset();

      result.histogramBuckets[index] = tmp;

      // TODO: 6/3/18 why +1 here?  Returning 0 seems sensible.
      result.z = (int) (tmp[COUNT]) + 1;

      result.distinctCount = (result.z > 0) ? 1 : 0;
    } else if (e.op.equals(Expression.LESS_THAN) || e.op.equals(Expression.LESS_THAN_EQUALS)) {

      double sum = 0.0;
      double tot = result.z + 0.0;
      for (int i = index + 1; i < buckets; i++) {
        result.z -= result.histogramBuckets[i][COUNT];
        sum += result.histogramBuckets[i][COUNT];
        result.histogramBuckets[i][COUNT] = 0;
      }
      result.distinctCount *= (1.0 - sum / tot);
      result.z = Math.max(result.z, 1);
    } else if (e.op.equals(Expression.GREATER_THAN)
        || e.op.equals(Expression.GREATER_THAN_EQUALS)) {

      double sum = 0.0;
      double tot = result.z + 0.0;
      for (int i = 0; i < index; i++) {
        result.z -= result.histogramBuckets[i][COUNT];
        sum += result.histogramBuckets[i][COUNT];
        result.histogramBuckets[i][COUNT] = 0;
      }
      result.distinctCount *= (1.0 - sum / tot);
      result.z = Math.max(result.z, 1);
    }

    return result;
  }

  public Histogram scale(double count) {
    Histogram result = copy();
    result.z *= count;
    result.distinctCount *= count;

    for (int i = 0; i < buckets; i++) result.histogramBuckets[i][COUNT] *= count;

    result.z = Math.max(result.z, 1);

    return result;
  }

  public Histogram merge(Histogram other) {

    Histogram result = copy();

    // System.out.println("bb");

    for (int i = 0; i < buckets; i++) {
      for (int j = 0; j < buckets; j++) {

        double mp = result.histogramBuckets[i][LOW] * 0.5 + result.histogramBuckets[i][HIGH] * 0.5;

        // System.out.println(mp + " " + other.histogramBuckets[j][LOW] + " " +
        // other.histogramBuckets[j][HIGH]);

        if (mp >= other.histogramBuckets[j][LOW] && mp <= other.histogramBuckets[j][HIGH]) {
          // System.out.println("a");
          double init = result.histogramBuckets[i][COUNT];
          double scaling = (other.distinctCount + 0.0) / other.buckets;

          result.histogramBuckets[i][COUNT] *= (other.histogramBuckets[i][COUNT] / scaling);

          result.z += (result.histogramBuckets[i][COUNT] - init);
        }
      }
    }

    return result;
  }

  public String toString() {
    LinkedList<Double> out = new LinkedList();
    for (int i = 0; i < buckets; i++) out.add(histogramBuckets[i][COUNT]);
    return out.toString();
  }
}
