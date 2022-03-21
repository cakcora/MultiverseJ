package experiments;

import TDA.TDAcluster;
import core.DecisionTree;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import metrics.MetricComputer;
import metrics.SingleEval;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TopologicalForestClusterSelectionExperiment {
    public static void main(String[] args, Map<String, Double> clusterQualityIndexHashMap,  Map<String, Map<String , Double>> treeOfClusterQualityIndexHashMap) throws IOException {

        String clusterPredictionOutputFile = args[0];
        String edgeFile = args[1];
        String resultFile = args[2];
        String nodeFile = args[3];
        int numClusterSelectionK = Integer.parseInt(args[4]);
        BufferedWriter wr = new BufferedWriter(new FileWriter(resultFile, true));
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
            String type = arr[1];

            String dp = arr[2];
            int vote1 = Integer.parseInt(arr[4]);
            int vote2 = Integer.parseInt(arr[3]);
            double predicted = Double.parseDouble(arr[5]);
            double label = (int) Double.parseDouble(arr[6]);
            TDAcluster cls;
            if (clusters.containsKey(cluster))
                cls = clusters.get(cluster);
            else cls = new TDAcluster();
            cls.setId(cluster);
            cls.setTreeCount(Integer.parseInt(arr[9]));
            cls.setQualityIndex(clusterQualityIndexHashMap.get(cluster));
            SingleEval eval = new SingleEval(predicted, label);
            if (type.equalsIgnoreCase("validation")) {
                cls.addToValidationEvals(dp, eval);
            } else if (type.equalsIgnoreCase("test")) {
                cls.addToTestEvals(dp, eval);
            }
            clusters.put(cluster, cls);
        }

        BufferedReader rdNodes = new BufferedReader(new FileReader(nodeFile));
        while ((line = rdNodes.readLine()) != null) {
            if (!line.isEmpty()) {
                var arr = line.split("\t");
                String clusterID = arr[0];
                String treeIDs = arr[1].replaceAll("\\[", "");
                treeIDs = treeIDs.replaceAll("\\]", "");
                TDAcluster cluster = clusters.get(clusterID);
                for (String i : treeIDs.split(",")) {
                    DecisionTree dt = new DecisionTree();
                    int id = Integer.parseInt(i.trim());
                    dt.setID(id);
                    cluster.addTree(dt);
                }
            }
        }

        MetricComputer metricComputer = new MetricComputer();
        for (String c : clusters.keySet()) {
            TDAcluster tdAcluster = clusters.get(c);
            List<SingleEval> evals = tdAcluster.getValidationEvalProbs();
            double auc_validation = metricComputer.computeAUC(evals);
            tdAcluster.setValidationAUC(auc_validation);

            evals = tdAcluster.getTestEvalProbs();
            double auc_test = metricComputer.computeAUC(evals);
            tdAcluster.setTestAUC(auc_test);
        }

        for (int selectThisManyClusters : new int[]{1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 100, 500}) {

            MetricComputer metric = metricComputer;
            // option 1 selectThisManyClusters randomly

            Set<String> selectedRandom = new HashSet<>();
            int size = clusters.size();
            if (selectThisManyClusters > size) break;
            ThreadLocalRandom current = ThreadLocalRandom.current();
            for (int n = 0; n < selectThisManyClusters; n++) {
                int random = current.nextInt(0, size);
                Iterator<TDAcluster> iterator = clusters.values().iterator();
                while (iterator.hasNext() && random-- > 1) {
                    iterator.next();
                }
                TDAcluster next = iterator.next();
                selectedRandom.add(next.getID());
            }


            // option 2 selectThisManyClusters greedily
            PriorityQueue<Double> queue = new PriorityQueue<>(size, Collections.reverseOrder());

            for (TDAcluster cls : clusters.values()) {
                queue.add(metricComputer.computeAUC(cls.getValidationEvalProbs()));
            }
            Set<String> selectedGreedy = new HashSet<>();
            double last = 0d;
            for (int n = 0; n < selectThisManyClusters; n++) {
                last = queue.poll();
            }
            for (String cluster : clusters.keySet()) {
                TDAcluster tdAcluster = clusters.get(cluster);
                if (tdAcluster.getValidationAUC() >= last) {
                    selectedGreedy.add(tdAcluster.getID());

                }
            }

            // option 3 select Top-K based on clusterQualityIndex
            // sort clusterQualityIndex
            Set<String> selectedClusterQuality = new HashSet<>();
            Map<String,Double> sortedMap = new LinkedHashMap<String, Double>();
            sortedMap = sortByValue(clusterQualityIndexHashMap);
            List<String> clusterIDs = new ArrayList<String>(sortedMap.keySet());
            Collections.reverse(clusterIDs);
            for (int n = 0; n < selectThisManyClusters ; n++)
            {
                selectedClusterQuality.add(clusterIDs.get(n));
            }


            //System.out.println("Greedy clusters: " + selectedGreedy.toString());
            List<SingleEval> evaluationsGreedy = evaluateWithSelected(selectedGreedy, clusters);
            double greedyAUC = metric.computeAUC(evaluationsGreedy);
            double bias = metric.computeBias(evaluationsGreedy);
            double logloss = metric.computeLogLoss(evaluationsGreedy);

            Set gh = new HashSet();
            for (String clust : selectedGreedy) {
                gh.addAll(clusters.get(clust).getTrees());
            }
            int greedyCLusterTreeCount = gh.size();
            //System.out.println("Greedy" + "\t" + selectThisManyClusters + "\t\t" + greedyAUC + "\t" + bias + "\t" + logloss);
            wr.write("Greedy" + "\t" + selectThisManyClusters + "\t\t" + greedyAUC + "\t" + bias + "\t" + logloss + "\t" + greedyCLusterTreeCount + "\r\n");

            //System.out.println("Random clusters: " + selectedRandom.toString());
            List<SingleEval> evaluationsRandom = evaluateWithSelected(selectedRandom, clusters);
            double randomAUC = metric.computeAUC(evaluationsRandom);
            bias = metric.computeBias(evaluationsRandom);
            logloss = metric.computeLogLoss(evaluationsRandom);
            gh = new HashSet();
            for (String clust : selectedRandom) {
                gh.addAll(clusters.get(clust).getTrees());
            }
            int randomCLusterTreeCount = gh.size();
            //System.out.println("Random" + "\t" + selectThisManyClusters + "\t\t" + randomAUC + "\t" + bias + "\t" + logloss);
            wr.write("Random" + "\t" + selectThisManyClusters + "\t\t" + randomAUC + "\t" + bias + "\t" + logloss + "\t" + randomCLusterTreeCount + "\r\n");

            List<SingleEval> evaluationsClusterQuality = evaluateWithSelected(selectedClusterQuality, clusters);
            double clusterQualityAUC = metric.computeAUC(evaluationsClusterQuality);
            bias = metric.computeBias(evaluationsClusterQuality);
            logloss = metric.computeLogLoss(evaluationsClusterQuality);
            gh = new HashSet();
            for (String clust : selectedClusterQuality) {
                gh.addAll(clusters.get(clust).getTrees());
            }
            int qualityCLusterTreeCount = gh.size();
            wr.write("Quality" + "\t" + numClusterSelectionK + "\t\t" + clusterQualityAUC + "\t" + bias + "\t" + logloss + "\t" + qualityCLusterTreeCount + "\r\n");

            //option 4 network selection
            double maxTDA = 0;
            for (TDAcluster cls : clusters.values()) {
                String id = cls.getID();
                Set<String> selectedTDA = new HashSet<>();
                selectedTDA.add(id);
                Collection<String> selectedNeig = graph.getNeighbors(id);
                if (selectedNeig != null) {
                    Iterator<String> iterator = selectedNeig.iterator();
                    for (int j = 0; j < selectThisManyClusters - 1; j++) {
                        if (iterator.hasNext()) {
                            String nextCluster = iterator.next();
                            selectedTDA.add(nextCluster);
                        }
                    }
                }

                List<SingleEval> evaluations = evaluateWithSelected(selectedTDA, clusters);

                double tdaAUC = metric.computeAUC(evaluations);
                bias = metric.computeBias(evaluations);
                logloss = metric.computeLogLoss(evaluations);
                gh = new HashSet();
                for (String clust : selectedTDA) {
                    gh.addAll(clusters.get(clust).getTrees());
                }
                int tdaCLusterTreeCount = gh.size();
                if (tdaAUC > maxTDA) {
                    maxTDA = tdaAUC;
                    //System.out.println("Network " + id + "\t" + selectThisManyClusters + "\t" + selectedTDA.size() + "\t" + tdaAUC + "\t" + bias + "\t" + logloss);
                    wr.write("Network " + id + "\t" + selectThisManyClusters + "\t" + selectedTDA.size() + "\t" + tdaAUC + "\t" + bias + "\t" + logloss + "\t" + tdaCLusterTreeCount + "\r\n");
                }
            }



        }
        wr.close();

    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
    private static List<SingleEval> evaluateWithSelected(Set<String> selected, Map<String, TDAcluster> clusters) throws IOException {
        String line = "";
        Map<String, SingleEval> probs = new HashMap<>();
        for (String c : selected) {
            TDAcluster tda = clusters.get(c);
            Map<String, SingleEval> evals = tda.getTestEvals();
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
