package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Database;
import java.io.*;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class ModelTrainer {

  MultiLayerNetwork net;

  public ModelTrainer(Database db) {
    int numInput = db.getNumAttributes() * 3 + 1;

    int numOutputs = 1;
    int nHidden = 128;
    double learningRate = 1e-1;

    this.net =
        new MultiLayerNetwork(
            new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.25))
                .list()
                .layer(
                    0,
                    new DenseLayer.Builder()
                        .nIn(numInput)
                        .nOut(nHidden)
                        .activation(Activation.SIGMOID)
                        .build())
                .layer(
                    1,
                    new OutputLayer.Builder(LossFunctions.LossFunction.L1)
                        .activation(Activation.IDENTITY)
                        .nIn(nHidden)
                        .nOut(numOutputs)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build());
  }

  public MultiLayerNetwork train(DataSetIterator iterator) {

    net.init();

    // Train the network on the full data set, and evaluate in periodically
    for (int i = 0; i < 50000; i++) {
      iterator.reset();
      net.fit(iterator);
    }

    return net;
  }
}
