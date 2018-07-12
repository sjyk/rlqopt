package edu.berkeley.riselab.rlqopt.opt.learning;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;

public class DataNormalizer {

  private static NormalizerStandardize normalizer = null;
  private static INDArray lablesMean;
  private static INDArray lablesStd;

  /** In-place, returns the updated data. */
  public static INDArray transformFeature(INDArray data) {
    return data;
    //    assert normalizer != null;
    //    normalizer.transform(data);
    //    return data;
  }

  public static void normalize(DataSet dataSet) {
    INDArray labels = dataSet.getLabels();
    lablesMean = labels.mean(0);
    lablesStd = labels.std(0);
    lablesStd.addi(1e-6);
    labels.subi(lablesMean).divi(lablesStd);
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
    return data.muli(lablesStd).addi(lablesMean);
    //    assert normalizer != null;
    //    normalizer.revertLabels(data);
    //    return data;
  }
}
