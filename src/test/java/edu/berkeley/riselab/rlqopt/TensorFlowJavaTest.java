package edu.berkeley.riselab.rlqopt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import junit.framework.TestCase;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.Tensors;

// To make this run, need to run <rlqopt>/download_tf_jni.sh, then
// set java.library.path to point to that resulting "jni" directory.
// IntelliJ will be much easier: add the resulting "jni" folder to Project Settings -> Libraries ->
// "+" -> "jni".

// https://www.tensorflow.org/versions/master/install/install_java
public class TensorFlowJavaTest extends TestCase {

  public static void main(String[] args) throws Exception {
    try (Graph g = new Graph()) {
      final String value = "Hello from " + TensorFlow.version();

      // Construct the computation graph with a single operation, a constant
      // named "MyConst" with a value "value".
      try (Tensor t = Tensor.create(value.getBytes("UTF-8"))) {
        // The Java API doesn't yet include convenience functions for adding operations.
        g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
      }

      // Execute the "MyConst" operation in a Session.
      try (Session s = new Session(g);
          Tensor output = s.runner().fetch("MyConst").run().get(0)) {
        System.out.println(new String(output.bytesValue(), "UTF-8"));
      }
    }
  }

  public void testPredictExpressionLearning() {
    // Test data & checkpoint here are trained on the following dataset under expr_learning/data:
    //     5rel-10numAttrs-24totalAttrs-1000maxTblSize-100buckets-3000-1526336271
    float[][] featVec =
        new float[][] {
          // Non-whitened.  True label 654.
          {
            10.0f, 1000.0f, 1000.0f, 1000.0f, 100.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 676.0f
          },
          // Non-whitened.  True label 885.
          {
            10.f, 1000.f, 1000.f, 1000.f, 100.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f,
            0.f, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f,
            1.f, 0.f, 862.f
          },
          // Non-whitened.  True label 1.
          {
            10.0f, 1000.0f, 1000.0f, 1000.0f, 100.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 179.0f
          }
        };
    float[][] predicted = new float[featVec.length][1];

    Tensor<Float> floatTensor = Tensors.create(featVec);

    System.out.println("input shape: " + Arrays.toString(floatTensor.shape()));
    System.out.println("input: " + Arrays.toString(featVec[0]));

    String dir = System.getProperty("user.dir") + "/expr_learning/";
    String graphPath = dir + "trained_nowhitening_nodropout_20180517/" + "frozen_graph.pb";

    byte[] graphDef = readAllBytesOrExit(Paths.get(graphPath));
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef);
      try (Session sess = new Session(g)) {

        Tensor<Float> output =
            sess.runner()
                .feed("IteratorGetNext_1", 0, floatTensor)
                .fetch("dense_2/BiasAdd", 0)
                .run()
                .get(0)
                .expect(Float.class);

        System.out.println("output shape: " + Arrays.toString(output.shape()));
        output.copyTo(predicted);
        for (int i = 0; i < predicted.length; ++i) {
          System.out.println("output " + i + ": " + Arrays.toString(predicted[i]));
        }
        assertEquals(658.90814, predicted[0][0], 1e-5);
        assertEquals(885.26526, predicted[1][0], 1e-5);
        assertEquals(1.703435, predicted[2][0], 1e-5);
      }
    }
  }

  private static byte[] readAllBytesOrExit(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      System.err.println("Failed to read [" + path + "]: " + e.getMessage());
      System.exit(1);
    }
    return null;
  }
}
