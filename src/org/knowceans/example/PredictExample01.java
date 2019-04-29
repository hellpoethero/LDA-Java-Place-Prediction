package org.knowceans.example;

import java.util.Date;
import org.knowceans.lda.LdaPrediction;

public class PredictExample01 {
  public static void main(String[] args) {
    args = new String[10];

    args[0] = "abc1/101"; // model
    args[1] = "D:/Research/Dataset/checkin/New folder/us_6"; // data
    args[2] = "random";

    LdaPrediction.init(args);
    Date s = new Date();
    LdaPrediction.predict();
    Date e = new Date();
    System.out.println(e.getTime()-s.getTime());
  }
}
