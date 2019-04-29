package org.knowceans.example;

import org.knowceans.lda.LdaEstimate;

public class EstimateExample01 {
  public static void main(String[] args) {
    args = new String[8];
    args[0] = "est";
    args[1] = "0.1";
    args[2] = "50";
    args[3] = "D:/Research/Project/lda-c-master/settings.txt";
    args[4] = "D:/Research/Dataset/checkin/New folder/us_6_train.dat";
    args[5] = "seeded";
    args[6] = "abc1";
    args[7] = "D:/Research/Project/lda-c-master/example/ap/vocab.txt";

    LdaEstimate.estimate(args);
  }
}
