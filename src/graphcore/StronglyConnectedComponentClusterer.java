package graphcore;

import edu.uci.ics.jung.graph.DirectedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StronglyConnectedComponentClusterer {

    int index = 0;
    List<Integer> stack = new ArrayList<Integer>();
    List<ArrayList<Integer>> SCC = new
            ArrayList<ArrayList<Integer>>();

    Map<Integer, Integer> indexHash = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer> lowlinkHash = new HashMap<Integer, Integer>();

    private List<ArrayList<Integer>> compute(Integer v, DirectedGraph<Integer, Integer> graph) {
        for (Integer node : graph.getVertices()) {
            indexHash.put(node, -1);
            lowlinkHash.put(node, -1);
        }
        return start(v, graph);
    }

    private List<ArrayList<Integer>> start(Integer vertex, DirectedGraph<Integer, Integer> source) {
        indexHash.put(vertex, index);
        lowlinkHash.put(vertex, index);
        index++;
        stack.add(0, vertex);

        for (Integer outNeighbor : source.getSuccessors(vertex)) {
            if (indexHash.get(outNeighbor) == -1) {
                start(outNeighbor, source);
                lowlinkHash.put(vertex, Math.min(lowlinkHash.get(vertex), lowlinkHash.get(outNeighbor)));
            } else if (stack.contains(outNeighbor)) {
                lowlinkHash.put(vertex, Math.min(lowlinkHash.get(vertex), indexHash.get(outNeighbor)));
            }
        }
        if (lowlinkHash.get(vertex).equals(indexHash.get(vertex))) {
            Integer n;
            ArrayList<Integer> component = new ArrayList<Integer>();
            do {
                n = stack.remove(0);
                component.add(n);
            } while (n != vertex);
            SCC.add(component);
        }
        return SCC;
    }
}

