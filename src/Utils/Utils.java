package Utils;

import core.DataPoint;
import core.Dataset;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class Utils {
    public static <T> double getAverage(Collection<T> myList) {
        double avgVal = 0d;
        int valCount = myList.size();
        for (T b : myList) {
            avgVal += (Double) b;
        }
        return avgVal / valCount;
    }

    public static void save(String filePath, Dataset dataset) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(filePath));
        String[] featureNames = dataset.getFeatureNames();
        for(String s:featureNames){
            wr.write(s+"\t");
        }
        wr.write("label\r\n");
        for (DataPoint dp : dataset.getDatapoints()) {
            for (double f : dp.getFeatures()) {
                wr.write(f + "\t");
            }
            wr.write(dp.getLabel() + "\r\n");
        }
        wr.close();
    }

    public static void saveGraphs(String filePath, Map<Integer, Graph<Integer, Integer>> graphs) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(filePath));
        wr.write("poison\tfrom\tto\r\n");
        for (Integer poison : graphs.keySet()) {
            Graph<Integer, Integer> graph = graphs.get(poison);
            for (int edge : graph.getEdges()) {
                Pair<Integer> endpoints = graph.getEndpoints(edge);
                wr.write(poison + "\t" + endpoints.getFirst() + "\t" + endpoints.getSecond() + "\r\n");
            }
        }
        wr.close();
    }
}
