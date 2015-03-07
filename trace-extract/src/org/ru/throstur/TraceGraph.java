package org.ru.throstur;

import java.util.*;

/**
 * Created by throstur on 3/6/15.
 */
public class TraceGraph {

    private class Edge {
        int id;
        String message;

        public Edge(int id, String message) {
            this.id = id;
            this.message = message;
        }
    }

    private static Random random = new Random();

    private int root;

    // THIS NODE -> (THAT NODE, MESSAGE)
    private HashMap<Integer, ArrayList<Edge>> edges;

    public TraceGraph() {
        root = 0;
        // Collection<Edge> group = new ArrayDeque<Edge>();
        edges = new HashMap<Integer, ArrayList<Edge>>();
    }

    public void addEdge(int from, int dest, String message) {
        Edge e = new Edge(dest, message);
        ArrayList<Edge> list;
        list = edges.get(from);
        if (list == null) {
            list = new ArrayList<Edge>();
        }
        list.add(e);
        edges.put(from, list);
    }

    public ArrayDeque<String> generateTrace() {
        int pos = root;
        ArrayDeque<String> trace = new ArrayDeque<String>(edges.size());
        ArrayList<Edge> choices = edges.get(pos);
        int index = random.nextInt(choices.size());
        Edge chosen = choices.get(index);
        trace.add(chosen.message);
        int next = chosen.id;
        while (next != pos) {
            pos = next;
            choices = edges.get(pos);
            if (choices == null) {
                return trace;
            }
            index = random.nextInt(choices.size());
            chosen = choices.get(index);
            if (chosen.id == pos) {
                index = random.nextInt(choices.size());
                chosen = choices.get(index);
            }
            trace.add(chosen.message);
            next = chosen.id;
        }

        return trace;
    }

}
