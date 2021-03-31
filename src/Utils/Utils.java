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
        wr.write("treeID" + "\t");
        for(String s:featureNames){
            wr.write(s+"\t");
        }
        wr.write("label\r\n");
        for (DataPoint dp : dataset.getDatapoints()) {
            wr.write(dp.getID() + "\t");
            for (double f : dp.getFeatures()) {
                wr.write(f + "\t");
            }
            wr.write(dp.getLabel() + "\r\n");
        }
        wr.close();
    }

    public static void saveGraphs(String filePath, Map<Integer, Graph<Integer, Integer>> graphs, String[] featureNames) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(filePath));
        wr.write("poison\tfrom\tto\r\n");
        for (Integer poison : graphs.keySet()) {
            Graph<Integer, Integer> graph = graphs.get(poison);
            for (int edge : graph.getEdges()) {
                Pair<Integer> endpoints = graph.getEndpoints(edge);
                Integer first = endpoints.getFirst();
                Integer second = endpoints.getSecond();
                String n1;
                if (featureNames[first].contains("_"))
                    n1 = featureNames[first].substring(0, featureNames[first].indexOf("_"));
                else
                    n1 = featureNames[first];
                String n2;
                if (featureNames[second].contains("_"))
                    n2 = featureNames[second].substring(0, featureNames[second].indexOf("_"));
                else n2 = featureNames[second];
                wr.write(poison + "\t" + n1 + "\t" + n2 + "\r\n");
            }
        }
        wr.close();
    }
}
