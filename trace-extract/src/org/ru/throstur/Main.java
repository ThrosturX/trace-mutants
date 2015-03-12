package org.ru.throstur;

import javax.management.RuntimeErrorException;
import java.io.*;
import java.util.ArrayDeque;
import java.util.Scanner;

public class Main {

  /**
   * Trace extraction and test case generation program
   *
   * @param args [0]: file to read graph from (.aut format)
   */
  public static void main(String[] args) throws Exception {
    TraceGraph tg;

    String fileInput = null;

    for (String arg : args) {
      // first unnamed parameter is filename
      if (!arg.startsWith("-") && fileInput == null) {
        fileInput = arg;
      }
    }

    int traces = 50;

    tg = constructGraph(fileInput);

    try {
      traces = Integer.parseInt(args[1]);
    } catch (Exception ex) {
      System.err.println("Using default value of 50");
    }

    String output = "";
    for (int i = 0; i < traces; i++) {
      ArrayDeque<String> trace = tg.generateTrace();
      while (!trace.isEmpty()) {
        output += (trace.poll() + "-");
      }
      output += "\n";
    }

    try {
      File file = new File(args[0].substring(0, args[0].indexOf('.')) + ".traces");
      if (!file.exists()) {
        file.createNewFile();
      } else {
        System.err.println("WARNING: File already exists!");
      }
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(output);
      bw.close();
    } catch (IOException ex) {
      System.err.println("Foo! IOException happened... Using stdout: ");
      System.out.print(output);
    }

  }

  private static TraceGraph constructGraph(String autfile) throws Exception {
    TraceGraph tg = new TraceGraph();
    FileInputStream fstream;
    try {
      fstream = new FileInputStream(autfile);
      BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
      String line;

      // we don't care about the first line
      line = br.readLine();

      while ((line = br.readLine()) != null) {
        String[] tokens = line.replaceAll("(?!::)\\p{P}", "").split(" ");
        int from = Integer.parseInt(tokens[0]);
        int dest = Integer.parseInt(tokens[2]);
        if (from == dest) {
          continue;
        }
        tg.addEdge(from, dest, tokens[1]);
      }

    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
      throw ex;
    }
    return tg;
  }
}
