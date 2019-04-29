package org.knowceans.lda;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.print.Doc;

public class LdaPrediction {

  static double[][] varGamma;
  static LdaModel model;
  static int numDocs;
  static Document[] train;
  static Document[] validation;
  static Document[] test;
  static Document[] random;
  static String targetName;

  public static void init(String[] args) {
    importModel(args[0]);
    importData(args[1]);
    targetName = args[2];
//    checkTestInTrain(args[1]);
  }

  public static void importModel(String root) {
    model = new LdaModel(root);
    try {
      BufferedReader br = new BufferedReader(new FileReader(root + ".gamma"));
      String line;
      line = br.readLine();
      numDocs = Integer.parseInt(line);
      varGamma = new double[numDocs][model.getNumTopics()];
      for (int i=0; i<numDocs; i++) {
        line = br.readLine();
        String[] fields = line.trim().split(" ");
        for (int j=0; j<fields.length; j++) {
          varGamma[i][j] = Double.parseDouble(fields[j]);
        }
      }
      br.close();
//            System.out.println(numDocs);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void importData(String str) {
    try {
      validation = new Document[numDocs];
      test = new Document[numDocs];
      random = new Document[numDocs];
      BufferedReader br = new BufferedReader(new FileReader(str + "_validation.dat"));
      String line;
      int i = 0;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split(" ");
        int length = Integer.parseInt(fields[0]);
        Document d = new Document(length);
        for (int n = 0; n < length; n++) {
          String[] numbers = fields[n + 1].split(":");
          int word = Integer.parseInt(numbers[0]);
          int count = (int) Float.parseFloat(numbers[1]);
          d.setWord(n, word);
          d.setCount(n, count);
        }
        validation[i] = d;
        i++;
      }
      br.close();

      i = 0;
      br = new BufferedReader(new FileReader(str + "_test.dat"));
      while ((line = br.readLine()) != null) {
        test[i] = new Document(1);
        test[i].setWord(0, Integer.parseInt(line));
        test[i].setCount(0, 1);
        i++;
      }
      br.close();

      i = 0;
      br = new BufferedReader(new FileReader(str + "_random.dat"));
      while ((line = br.readLine()) != null) {
        random[i] = new Document(1);
        random[i].setCount(0, 1);
        random[i].setWord(0, Integer.parseInt(line));
        i++;
      }
      br.close();

//            System.out.println(numDocs);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void predict() {
    Document[] target;
    if (targetName.equals("validation")) {
      target = validation;
    } else if (targetName.equals("random")) {
      target = random;
    } else {
      target = test;
    }
    int trueCount = 0;
    double top = 0;
    List<IDSorter> gammaDoc;
    IDSorter[] predictWords = new IDSorter[model.getNumTerms()];
    for (int w=0; w<model.getNumTerms(); w++) {
      predictWords[w] = new IDSorter(w, 0);
    }
    double sumRank = 0;
    for (int d=0; d<numDocs; d++) {
      gammaDoc = new ArrayList<>();
      for (int t=0; t<model.getNumTopics(); t++) {
        if (varGamma[d][t] > 1) {
          gammaDoc.add(new IDSorter(t, varGamma[d][t]));
        }
      }
      double wordSum;
      for (int w=0; w<model.getNumTerms(); w++) {
        wordSum = 0;
        for (IDSorter gamma : gammaDoc) {
          wordSum += gamma.getWeight() * model.getClassWord()[gamma.getID()][w];
        }
        predictWords[w].set(w, wordSum);
      }
      Arrays.sort(predictWords);
      top += checkTop(predictWords, 1000, target[d]);
//      boolean check = compareTestAndRandom(predictWords, d);
//      System.out.println(check);
//      if (check) {
//        trueCount++;
//      }
//      sumRank += ranking(predictWords, target[d]);
//      System.out.println(predictWords[0].getID()+ " " + predictWords[0].getWeight());
//      break;
    }
//    System.out.println("average: "+ sumRank / numDocs);
//    System.out.println("true: "+ trueCount);
//    System.out.println("true: "+ (double) trueCount / numDocs);
    System.out.println("top: "+ top);
    System.out.println("num doc: "+ numDocs);
  }

  public static double checkTop(IDSorter[] predictWords, int numTop, Document doc) {
    List<Integer> docWords = new ArrayList<>();
    for (int w=0; w<doc.getLength(); w++) {
      docWords.add(doc.getWord(w));
    }

    int count = 0;
    for (int i=0; i<numTop; i++) {
      if (docWords.contains(predictWords[i].getID())) {
        count++;
      }
    }

//    System.out.println((double) count / doc.getLength());
    return (double) count / doc.getLength();
  }

  public static double ranking(IDSorter[] predictWords, Document doc) {
    double sumRank = 0;
    for (int word : doc.getWords()) {
      int rank = predictWords.length;
      for (int i=0; i<predictWords.length; i++) {
        if (word == predictWords[i].getID()) {
          rank = i + 1;
//          System.out.println(word + " " +  rank);
          break;
        }
      }
      sumRank += rank;
    }
    System.out.println(sumRank / doc.getLength());
    return sumRank / doc.getLength();
  }

  public static boolean compareTestAndRandom(IDSorter[] predictWords, int d) {
    for (IDSorter word : predictWords) {
      if (test[d].getWord(0) == word.getID()) {
        return true;
      } else if (random[d].getWord(0) == word.getID()) {
        return false;
      }
    }
    return false;
  }

  public static void checkTestInTrain(String str) {
    try {
      int count = 0;
      BufferedReader br = new BufferedReader(new FileReader(str + "_validation.dat"));
      String line;
      int d = 0;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split(" ");
        int length = Integer.parseInt(fields[0]);
        List<Integer> words = new ArrayList<>();
        for (int n = 0; n < length; n++) {
          String[] numbers = fields[n + 1].split(":");
          int word = Integer.parseInt(numbers[0]);
          words.add(word);
        }
        if (words.contains(test[d].getWord(0))) {
          count++;
          System.out.print("true ");
        }
        System.out.println(test[d].getWord(0));
        d++;
      }
      System.out.println("A: " + count);
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
