package org.ru.throstur;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TraceGraph tg = new TraceGraph();

        FileInputStream fstream;
        try {
            fstream = new FileInputStream(args[0]);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String line;

            // we don't care about the first line
            line = br.readLine();

            while((line = br.readLine()) != null) {
                String[] tokens = line.replaceAll("(?!::)\\p{P}", "").split(" ");
                int from = Integer.parseInt(tokens[0]);
                int dest = Integer.parseInt(tokens[2]);
                if (from == dest) {
                    continue;
                }
                tg.addEdge(from, dest, tokens[1]);
            }

        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            return;
        }

        int iterations = 5;

        try {
            iterations = Integer.parseInt(args[1]);
        } catch (Exception ex) {
            System.err.println("Using default value of 5");
        }

        for (int i=0; i < iterations; i++) {
            ArrayDeque<String> trace = tg.generateTrace();
            while (!trace.isEmpty()) {
                System.out.print(trace.poll() + "-");
            }
            System.out.print("\n");
        }



    }
}
