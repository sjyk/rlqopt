package edu.berkeley.riselab.rlqopt.opt.learning;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;

public class DataNormalizer {

  private static NormalizerStandardize normalizer = null;
  private static INDArray labelsMean;
  private static INDArray labelsStd;

  /** In-place, returns the updated data. */
  public static INDArray transformFeature(INDArray data) {
    return data;
    //    assert normalizer != null;
    //    normalizer.transform(data);
    //    return data;
  }

  public static void normalize(DataSet dataSet) {
    INDArray labels = dataSet.getLabels();

    labelsMean = labels.mean(0);
    labelsStd = labels.std(0);
    labelsStd.addi(1e-6);

    labels.subi(labelsMean);
    labels.divi(labelsStd);
    dataSet.setLabels(labels);

    //    assert normalizer == null;
    //    normalizer = new NormalizerStandardize();
    //     Transform the labels (costs) too.
    //    normalizer.fitLabel(true);
    //    normalizer.fit(dataSet);
    //    normalizer.transform(dataSet);
  }

  /** In-place, returns the updated data. */
  public static INDArray revertLabel(INDArray data) {
    return data.muli(labelsStd).addi(labelsMean);
    //    assert normalizer != null;
    //    normalizer.revertLabels(data);
    //    return data;
  }
}
