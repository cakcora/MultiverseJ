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

        BufferedReader br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
        Map<String, TDAcluster> clusters = new HashMap<>();

        while ((line = br.readLine()) != null) {
            String[] arr = line.split("\t");

            String cluster = arr[0];
            String dp = arr[1];
            int vote1 = Integer.parseInt(arr[2]);
            int vote2 = Integer.parseInt(arr[3]);
            double predicted = Double.parseDouble(arr[4]);
            double label = (int) Double.parseDouble(arr[5]);
            TDAcluster cls;
            if (clusters.containsKey(cluster))
                cls = clusters.get(cluster);
            else cls = new TDAcluster();
            cls.setId(cluster);
            cls.setTreeCount(vote1 + vote2);
            SingleEval eval = new SingleEval(predicted, label);
            cls.addToEvals(dp, eval);
            clusters.put(cluster, cls);
        }
        MetricComputer metricComputer = new MetricComputer();
        for (String c : clusters.keySet()) {
            TDAcluster tdAcluster = clusters.get(c);
            List<SingleEval> evals = tdAcluster.getEvalProbs();
            double auc = metricComputer.computeAUC(evals);
            tdAcluster.setAUC(auc);
        }

        for (int selectThisManyClusters : new int[]{1, 2, 3, 4, 5, 10, 20, 50}) {
            MetricComputer metric = metricComputer;
            // option 1 selectThisManyClusters randomly
            Set<String> selectedRandom = new HashSet<>();
            int size = clusters.size();
            for (int n = 0; n < selectThisManyClusters; n++) {

                double random = ThreadLocalRandom.current().nextInt(0, size);
                Iterator<TDAcluster> iterator = clusters.values().iterator();
                while (iterator.hasNext() && random > 1) {
                    iterator.next();
                    random--;
                }
                TDAcluster next = iterator.next();
                selectedRandom.add(next.getID());
            }

            // option 2 selectThisManyClusters greedily
            PriorityQueue<Double> queue = new PriorityQueue<>(size, Collections.reverseOrder());

            for (TDAcluster cls : clusters.values()) {
                queue.add(metricComputer.computeAUC(cls.getEvalProbs()));
            }
            Set<String> selectedGreedy = new HashSet<>();
            double last = 0d;
            for (int n = 0; n < selectThisManyClusters; n++) {
                last = queue.poll();
            }
            for (String cluster : clusters.keySet()) {
                TDAcluster tdAcluster = clusters.get(cluster);
                if (tdAcluster.getAUC() >= last)
                    selectedGreedy.add(tdAcluster.getID());
            }
            //System.out.println("Greedy clusters: " + selectedGreedy.toString());
            List<SingleEval> evaluationsGreedy = evaluateWithSelected(selectedGreedy, clusters);
            double greedyAUC = metric.computeAUC(evaluationsGreedy);
            double bias = metric.computeBias(evaluationsGreedy);
            double logloss = metric.computeLogLoss(evaluationsGreedy);
            System.out.println("Greedy" + "\t" + selectThisManyClusters + "\t\t" + greedyAUC + "\t" + bias + "\t" + logloss);

            //System.out.println("Random clusters: " + selectedRandom.toString());
            List<SingleEval> evaluationsRandom = evaluateWithSelected(selectedRandom, clusters);
            double randomAUC = metric.computeAUC(evaluationsRandom);
            bias = metric.computeBias(evaluationsRandom);
            logloss = metric.computeLogLoss(evaluationsRandom);
            System.out.println("Random" + "\t" + selectThisManyClusters + "\t\t" + randomAUC + "\t" + bias + "\t" + logloss);

            //option 3 network selection
            double maxTDA = 0;
            for (TDAcluster cls : clusters.values()) {
                String id = cls.getID();
                Set<String> selectedClusters = new HashSet<>();
                selectedClusters.add(id);
                Collection<String> selectedNeig = graph.getNeighbors(id);
                if (selectedNeig != null) {
                    Iterator<String> iterator = selectedNeig.iterator();
                    for (int j = 0; j < selectThisManyClusters - 1; j++) {
                        if (iterator.hasNext()) {
                            selectedClusters.add(iterator.next());
                        }
                    }
                }
                List<SingleEval> evaluations = evaluateWithSelected(selectedClusters, clusters);

                double tdaAUC = metric.computeAUC(evaluations);
                bias = metric.computeBias(evaluations);
                logloss = metric.computeLogLoss(evaluations);
                if (tdaAUC > maxTDA) {
                    maxTDA = tdaAUC;
                    System.out.println("Network " + id + "\t" + selectThisManyClusters + "\t" + selectedClusters.size() + "\t" + tdaAUC + "\t" + bias + "\t" + logloss);
                }
            }

        }

    }

    private static List<SingleEval> evaluateWithSelected(Set<String> selected, Map<String, TDAcluster> clusters) throws IOException {
        String line = "";
        Map<String, SingleEval> probs = new HashMap<>();
        for (String c : selected) {
            TDAcluster tda = clusters.get(c);
            Map<String, SingleEval> evals = tda.getEvals();
            for (String dp : evals.keySet()) {
                SingleEval eval = evals.get(dp);
                if (!probs.containsKey(dp)) {
                    probs.put(dp, eval);
                } else {
                    SingleEval eval2 = probs.get(dp);
                    double runningSum = eval.getPredicted() + eval2.getPredicted();
                    probs.put(dp, new SingleEval(runningSum, eval2.getActual()));
                }
            }
        }
        // recompute mean probabilities
        int size = selected.size();
        for (String dp : probs.keySet()) {
            SingleEval value = probs.get(dp);
            double prob = value.getPredicted() / size;
            double label = value.getActual();
            probs.put(dp, new SingleEval(prob, label));
        }
        return new ArrayList<>(probs.values());
    }


}
