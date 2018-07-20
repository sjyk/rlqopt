package edu.berkeley.riselab.rlqopt.opt.learning;

import edu.berkeley.riselab.rlqopt.Database;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class ModelTrainer {

  MultiLayerNetwork net;

  public ModelTrainer(Database db) {
    int numInput = db.getNumAttributes() * 3 + 3;
    int numOutputs = 1;
    int nHidden = 128;

    this.net =
        new MultiLayerNetwork(
            new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.RELU_UNIFORM)
                .updater(new Adam())
                .list()
                .layer(
                    0,
                    new DenseLayer.Builder()
                        .nIn(numInput)
                        .nOut(nHidden)
                        .activation(Activation.RELU)
                        .build())
                .layer(
                    1,
                    new DenseLayer.Builder()
                        .nIn(nHidden)
                        .nOut(nHidden / 2)
                        .activation(Activation.RELU)
                        .build())
                .layer(
                    2,
                    new OutputLayer.Builder(LossFunctions.LossFunction.L1)
                        .activation(Activation.IDENTITY)
                        .nIn(nHidden / 2)
                        .nOut(numOutputs)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build());
  }

  public MultiLayerNetwork train(DataSetIterator iterator) {
    net.init();
    net.setListeners(new ScoreIterationListener(100));

    System.out.println(net.getUpdater());

    // Epochs.
    for (int i = 0; i < 10000; i++) {
      iterator.reset();
      net.fit(iterator);
    }

    return net;
  }
}
