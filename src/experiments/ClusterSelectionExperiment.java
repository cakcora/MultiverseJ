package experiments;

import TDA.TDAcluster;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

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
                    int epsilon = Integer.parseInt((arr[0]));
                    if (epsilon != targetEpsilon) continue;
                    String cluster = arr[1];
                    int vote0 = Integer.parseInt(arr[5]);
                    int vote1 = Integer.parseInt(arr[6]);
                    int predicted = vote0 > vote1 ? 0 : 1;
                    int label = (int) Double.parseDouble(arr[7]);
                    TDAcluster cls = new TDAcluster();
                    cls.setId(cluster);
                    cls.setTreeCount(vote0 + vote1);
                    if (clusters.containsKey(cluster)) {
                        cls = clusters.get(cluster);
                    }
                    cls.add(label, predicted == label, (vote0 + vote1));
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
                PriorityQueue<Double> queue = new PriorityQueue<>(size, Collections.reverseOrder());
                for (TDAcluster cls : clusters.values()) {
                    queue.add(cls.getAccuracy());
                }
                Set<String> selectedGreedy = new HashSet<>();
                double last = 0d;
                for (int n = 0; n < select; n++) {
                    last = queue.poll();
                }
                for (String cluster : clusters.keySet()) {
                    if (clusters.get(cluster).getAccuracy() >= last)
                        selectedGreedy.add(clusters.get(cluster).getID());
                }
                System.out.println("Greedy clusters: " + selectedGreedy.toString());
                System.out.println("Random clusters: " + selectedRandom.toString());

                //option 3 network selection


                for (TDAcluster cls : clusters.values()) {
                    Collection<String> selectedNeig = graph.getNeighbors(cls.getID());
                    Set<String> selectedNeighbors = new HashSet<>();
                    selectedNeighbors.add(cls.getID());
                    Iterator<String> iterator = selectedNeig.iterator();
                    for (int j = 0; j < select - 1; j++) {
                        if (iterator.hasNext()) {
                            selectedNeighbors.add(iterator.next());
                        }
                    }
                    br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
                    int[] perfArray = evaluateWithSelected(selectedNeighbors, br, targetEpsilon);
                    System.out.println("Network " + cls.getID() + "\t" + select + "\t" + selectedNeighbors.size() + "\t" + targetEpsilon / 10.0 + "\t" + (perfArray[0] + perfArray[2] + 0.0) / Arrays.stream(perfArray).sum() + "\t" + perfArray[0] + "\t" + perfArray[1] + "\t" + perfArray[2] + "\t" + perfArray[3] + "\t" + perfArray[4]);
                }


                br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
                int[] perfArray = evaluateWithSelected(selectedGreedy, br, targetEpsilon);
                System.out.println("Greedy" + "\t" + select + "\t\t" + targetEpsilon / 10.0 + "\t" + (perfArray[0] + perfArray[2] + 0.0) / Arrays.stream(perfArray).sum() + "\t" + perfArray[0] + "\t" + perfArray[1] + "\t" + perfArray[2] + "\t" + perfArray[3] + "\t" + perfArray[4]);

                br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
                perfArray = evaluateWithSelected(selectedRandom, br, targetEpsilon);
                System.out.println("Random" + "\t" + select + "\t\t" + targetEpsilon / 10.0 + "\t" + (perfArray[0] + perfArray[2] + 0.0) / Arrays.stream(perfArray).sum() + "\t" + perfArray[0] + "\t" + perfArray[1] + "\t" + perfArray[2] + "\t" + perfArray[3] + "\t" + perfArray[4]);
            }
    }

    private static int[] evaluateWithSelected(Set<String> selected, BufferedReader br, int targetEpsilon) throws IOException {
        String line = "";
        HashMap<Long, Integer> labels = new HashMap<>();
        HashMap<Long, int[]> dpVotes = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] arr = line.split("\t");
            int epsilon = Integer.parseInt((arr[0]));
            if (epsilon != targetEpsilon) continue;
            String cluster = arr[1];
            if (!selected.contains(cluster)) continue;
            long dpID = Long.parseLong(arr[2]);
            int vote0 = Integer.parseInt(arr[5]);
            int vote1 = Integer.parseInt(arr[6]);
            int predicted = vote0 >= vote1 ? 0 : 1;
            int label = (int) Double.parseDouble(arr[7]);
            TDAcluster cls = new TDAcluster();
            cls.setId(cluster);
            cls.setTreeCount(vote0 + vote1);
            int[] array = new int[2];
            if (dpVotes.containsKey(dpID)) {
                array = dpVotes.get(dpID);
            }
            // strategy 1 all cluster trees vote the same
            array[predicted] += (vote0 + vote1);
            // strategy 2 cluster votes as its trees vote
            //array[0]+=vote0;
            //array[1]+=vote1;
            dpVotes.put(dpID, array);
            labels.put(dpID, label);
        }
        int[] perfArray = new int[5];
        int tp = 0, fp = 1, tn = 2, fn = 3, votes = 4;
        for (long dp : dpVotes.keySet()) {
            int label = labels.get(dp);
            int vote0 = dpVotes.get(dp)[0];
            int vote1 = dpVotes.get(dp)[1];
            perfArray[votes] = vote0 + vote1;
            int predicted = vote0 >= vote1 ? 0 : 1;
            if (label == 1) {
                if (predicted == 1) {
                    perfArray[tp]++;
                } else perfArray[fn]++;
            } else if (predicted == 1) {
                perfArray[fp]++;
            } else perfArray[tn]++;
        }
        return perfArray;
    }


}
