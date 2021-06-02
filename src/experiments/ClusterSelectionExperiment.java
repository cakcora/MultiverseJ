package experiments;

import TDA.TDAcluster;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import metrics.MetricComputer;
import metrics.SingleEval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class ClusterSelectionExperiment {
    public static void main(String[] args) throws IOException {
        String clusterPredictionOutputFile = args[0];
        String edgeFile = args[1];

        BufferedReader rd = new BufferedReader(new FileReader(edgeFile));
        UndirectedSparseGraph<String, Integer> graph = new UndirectedSparseGraph();
        String line = "";
        while ((line = rd.readLine()) != null) {
            if (line.isEmpty()) continue;
            String[] arr = line.split("\t");
            String clus = arr[0].trim();
            String edges = arr[1].replaceAll("\\[", "");
            edges = edges.replaceAll("\\]", "");
            edges = edges.replaceAll("'", "");
            String[] a = edges.split(",");
            graph.addVertex(clus.trim());
            for (String cl2 : a) {
                cl2 = cl2.trim();
                graph.addVertex(cl2.trim());
                graph.addEdge(graph.getEdgeCount(), clus, cl2);
            }
        }
        for (int select : new int[]{1, 2, 3, 4, 5, 10})
            for (int targetEpsilon : new int[]{5}) {
                BufferedReader br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
                line = "";

                Map<String, TDAcluster> clusters = new HashMap<>();

                while ((line = br.readLine()) != null) {
                    String[] arr = line.split("\t");

                    String cluster = arr[0];
                    int vote0 = Integer.parseInt(arr[2]);
                    int vote1 = Integer.parseInt(arr[3]);
                    double predicted = Double.parseDouble(arr[4]);
                    double label = (int) Double.parseDouble(arr[5]);
                    TDAcluster cls = new TDAcluster();
                    cls.setId(cluster);
                    cls.setTreeCount(vote0 + vote1);

                    SingleEval eval = new SingleEval(predicted, label);
                    clusters.put(cluster, cls);

                }
                // option 1 select randomly
                Set<String> selectedRandom = new HashSet<>();
                int size = clusters.size();
                for (int n = 0; n < select; n++) {

                    double random = ThreadLocalRandom.current().nextInt(0, size);
                    Iterator<TDAcluster> iterator = clusters.values().iterator();
                    while (iterator.hasNext() && random > 1) {
                        iterator.next();
                        random--;
                    }
                    TDAcluster next = iterator.next();
                    selectedRandom.add(next.getID());
                }

                // option 2 select greedily
//                PriorityQueue<Double> queue = new PriorityQueue<>(size, Collections.reverseOrder());
//                for (TDAcluster cls : clusters.values()) {
//                    queue.add(cls.getAccuracy());
//                }
//                Set<String> selectedGreedy = new HashSet<>();
//                double last = 0d;
//                for (int n = 0; n < select; n++) {
//                    last = queue.poll();
//                }
//                for (String cluster : clusters.keySet()) {
//                    if (clusters.get(cluster).getAccuracy() >= last)
//                        selectedGreedy.add(clusters.get(cluster).getID());
//                }
//                System.out.println("Greedy clusters: " + selectedGreedy.toString());
                System.out.println("Random clusters: " + selectedRandom.toString());

                //option 3 network selection


                for (TDAcluster cls : clusters.values()) {
                    Collection<String> selectedNeig = graph.getNeighbors(cls.getID());
                    Set<String> selectedClusters = new HashSet<>();
                    selectedClusters.add(cls.getID());
                    Iterator<String> iterator = selectedNeig.iterator();
                    for (int j = 0; j < select - 1; j++) {
                        if (iterator.hasNext()) {
                            selectedClusters.add(iterator.next());
                        }
                    }
                    br = new BufferedReader(new FileReader(clusterPredictionOutputFile));// this code reads the file too many times
                    List<SingleEval> evaluations = evaluateWithSelected(selectedClusters, br);
                    MetricComputer metric = new MetricComputer();
                    double auc = metric.computeAUC(evaluations);
                    double bias = metric.computeBias(evaluations);
                    double logloss = metric.computeLogLoss(evaluations);
                    System.out.println("Network " + cls.getID() + "\t" + select + "\t" + selectedClusters.size() + "\t" + targetEpsilon / 10.0 + "\t" + auc + "\t" + bias + "\t" + logloss);
                }


//                br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
//                int[] perfArray = evaluateWithSelected(selectedGreedy, br, targetEpsilon);
//                System.out.println("Greedy" + "\t" + select + "\t\t" + targetEpsilon / 10.0 + "\t" + (perfArray[0] + perfArray[2] + 0.0) / Arrays.stream(perfArray).sum() + "\t" + perfArray[0] + "\t" + perfArray[1] + "\t" + perfArray[2] + "\t" + perfArray[3] + "\t" + perfArray[4]);
//
//                br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
//                perfArray = evaluateWithSelected(selectedRandom, br, targetEpsilon);
//                System.out.println("Random" + "\t" + select + "\t\t" + targetEpsilon / 10.0 + "\t" + (perfArray[0] + perfArray[2] + 0.0) / Arrays.stream(perfArray).sum() + "\t" + perfArray[0] + "\t" + perfArray[1] + "\t" + perfArray[2] + "\t" + perfArray[3] + "\t" + perfArray[4]);
            }
    }

    private static List<SingleEval> evaluateWithSelected(Set<String> selected, BufferedReader br) throws IOException {
        String line = "";
        List<SingleEval> evaluations = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            String[] arr = line.split("\t");
            String cluster = arr[0];
            if (!selected.contains(cluster)) continue;
            double predicted = Double.parseDouble(arr[4]);
            double label = Double.parseDouble(arr[5]);
            if (Double.isNaN(predicted) || Double.isNaN(label)) {
                System.out.println("Error: nan probability values exist in the cluster output file");
            }
            SingleEval eval = new SingleEval(predicted, label);
            evaluations.add(eval);

        }
        return evaluations;
    }


}
