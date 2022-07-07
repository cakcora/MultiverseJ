package experimentsDataShift;

import TDA.TDAcluster;
import core.DecisionTree;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import metrics.MetricComputer;
import metrics.SingleEval;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TopologicalForestClusterSelectionExperiment {
    public static void main(String[] args, Map<String, Double> clusterQualityIndexHashMap,  Map<String, Map<String , Double>> treeOfClusterQualityIndexHashMap, int [] topKTresSelection) throws IOException {
        String ID = "";
        String clusterPredictionOutputFile = args[0];
        String edgeFile = args[1];
        String resultFile = args[2];
        String nodeFile = args[3];
        String Replica = args[5];
        int numClusterSelectionK = Integer.parseInt(args[4]);
        BufferedWriter wr = new BufferedWriter(new FileWriter(resultFile, true));
        BufferedWriter csvWr = new BufferedWriter(new FileWriter(resultFile.split("\\.")[0]+".csv", true));
        // TODO: get the replicat number sequential runner
        if ( Integer.parseInt(Replica) == 30 )
        csvWr.write("ID,Replica,Method,ClusterNo,NN,Auc,Bias,Loss,TreeNo,RunTime\n");
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

        rd.close();
        BufferedReader br = new BufferedReader(new FileReader(clusterPredictionOutputFile));
        Map<String, TDAcluster> clusters = new HashMap<>();

        while ((line = br.readLine()) != null) {
            analyzeLine(line, clusters, clusterQualityIndexHashMap, topKTresSelection);
        }

        br.close();
        BufferedReader rdNodes = new BufferedReader(new FileReader(nodeFile));
        while ((line = rdNodes.readLine()) != null) {
            if (!line.isEmpty()) {
                var arr = line.split("\t");
                String clusterID = arr[0];
                String treeIDs = arr[1].replaceAll("\\[", "");
                treeIDs = treeIDs.replaceAll("\\]", "");
                TDAcluster cluster = clusters.get(clusterID);
                if (cluster != null) {
                    for (String i : treeIDs.split(",")) {
                        DecisionTree dt = new DecisionTree();
                        int id = Integer.parseInt(i.trim());
                        dt.setID(id);
                        cluster.addTree(dt);
                    }
                }
                else {
                    System.out.println("Cluster Null Error : " + clusterID + "\n" );
                }
            }
        }
        rdNodes.close();

        MetricComputer metricComputer = new MetricComputer();
        for (String c : clusters.keySet()) {
            TDAcluster tdAcluster = clusters.get(c);
            List<SingleEval> evals = tdAcluster.getTestEvalProbs();
            double auc_validation = metricComputer.computeAUC(evals);
            tdAcluster.setValidationAUC(auc_validation);

            evals = tdAcluster.getTestEvalProbs();
            double auc_test = metricComputer.computeAUC(evals);
            tdAcluster.setTestAUC(auc_test);
        }

        for (int selectThisManyClusters : new int[]{1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 100, 500}) {

            MetricComputer metric = metricComputer;
            int size = clusters.size();
            if (selectThisManyClusters > size) break;

            // option 1 selectThisManyClusters greedily

            PriorityQueue<Double> queue = new PriorityQueue<>(size, Collections.reverseOrder());

            for (TDAcluster cls : clusters.values()) {
                queue.add(metricComputer.computeAUC(cls.getTestEvalProbs()));
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
            //System.out.println("Greedy clusters: " + selectedGreedy.toString());
            long start = System.currentTimeMillis();
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
            long end = System.currentTimeMillis();
            NumberFormat formatter = new DecimalFormat("#0.00000000");
            ID = "Greedy_" + String.valueOf(selectThisManyClusters);
            wr.write("Greedy" + "\t" + selectThisManyClusters + "\t\t" + greedyAUC + "\t" + bias + "\t" + logloss + "\t" + greedyCLusterTreeCount + "\r\n");
            csvWr.write(ID + "," + Replica + "," +"Greedy" + "," + selectThisManyClusters + ",," + greedyAUC + "," + bias + "," + logloss + "," + greedyCLusterTreeCount + "," +  formatter.format((end - start) / 1000d) + "\r\n");




            // option 2 selectThisManyClusters randomly

            Set<String> selectedRandom = new HashSet<>();
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
            //System.out.println("Random clusters: " + selectedRandom.toString());
            start = System.currentTimeMillis();
            List<SingleEval> evaluationsRandom = evaluateWithSelected(selectedRandom, clusters);
            double randomAUC = metric.computeAUC(evaluationsRandom);
            bias = metric.computeBias(evaluationsRandom);
            logloss = metric.computeLogLoss(evaluationsRandom);
            gh = new HashSet();
            for (String clust : selectedRandom) {
                gh.addAll(clusters.get(clust).getTrees());
            }
            int randomCLusterTreeCount = gh.size();
            end = System.currentTimeMillis();
            formatter = new DecimalFormat("#0.00000000");
            //System.out.println("Random" + "\t" + selectThisManyClusters + "\t\t" + randomAUC + "\t" + bias + "\t" + logloss);
            ID = "Random_" + String.valueOf(selectThisManyClusters);
            wr.write("Random" + "\t" + selectThisManyClusters + "\t\t" + randomAUC + "\t" + bias + "\t" + logloss + "\t" + randomCLusterTreeCount + "\r\n");
            csvWr.write(ID + "," + Replica + "," + "Random" + "," + selectThisManyClusters + ",," + randomAUC + "," + bias + "," + logloss + "," + randomCLusterTreeCount + "," +  formatter.format((end - start) / 1000d) + "\r\n");


            // option 3 select Top-K based on clusterQualityIndex without tree selection strategy
            // sort clusterQualityIndex

            long qualitySelectionStart = System.currentTimeMillis();
            Set<String> selectedClusterQuality = new HashSet<>();
            Map<String,Double> sortedMap = new LinkedHashMap<String, Double>();
            sortedMap = sortMapByValue(clusterQualityIndexHashMap);
            List<String> clusterIDs = new ArrayList<String>(sortedMap.keySet());
            Collections.reverse(clusterIDs);
            for (int n = 0; n < selectThisManyClusters ; n++)
            {
                selectedClusterQuality.add(clusterIDs.get(n));
            }

            long endQualitySelection = System.currentTimeMillis();
            long qualitySelectionTime = endQualitySelection - qualitySelectionStart ;

            start = System.currentTimeMillis();
            List<SingleEval> evaluationsClusterQuality = evaluateWithSelected(selectedClusterQuality, clusters);
            double clusterQualityAUC = metric.computeAUC(evaluationsClusterQuality);
            bias = metric.computeBias(evaluationsClusterQuality);
            logloss = metric.computeLogLoss(evaluationsClusterQuality);
            gh = new HashSet();
            for (String clust : selectedClusterQuality) {
                gh.addAll(clusters.get(clust).getTrees());
            }
            int qualityCLusterTreeCount = gh.size();
            end = System.currentTimeMillis();
            formatter = new DecimalFormat("#0.00000000");
            ID = "Quality_" + String.valueOf(selectThisManyClusters);
            wr.write("Quality" + "\t" + selectThisManyClusters + "\t\t" + clusterQualityAUC + "\t" + bias + "\t" + logloss + "\t" + qualityCLusterTreeCount + "\r\n");
            csvWr.write(ID + "," + Replica + "," + "Quality" + "," + selectThisManyClusters + ",," + clusterQualityAUC + "," + bias + "," + logloss + "," + qualityCLusterTreeCount + "," +  formatter.format((end - start) / 1000d) +  "\r\n");



            // Option 4 clusterQualityIndex with top K tree
            // We need just to change evaluation part in the next steps
            // for Option 4

            Set<String> totalSelectedTrees = new HashSet<>();
            for (int n : topKTresSelection)
            {
                start = System.currentTimeMillis();
                totalSelectedTrees.clear();
                List<SingleEval> evaluationsClusterQualityWithTreeSelection = evaluateWithSelectedForTreeSelection(selectedClusterQuality, clusters, String.valueOf(n));
                double clusterQualityWithTreeSelectionAUC = metric.computeAUC(evaluationsClusterQualityWithTreeSelection);
                bias = metric.computeBias(evaluationsClusterQualityWithTreeSelection);
                logloss = metric.computeLogLoss(evaluationsClusterQualityWithTreeSelection);
                for (String clust : selectedClusterQuality) {
                    // change here for cluster number of unique trees
                    totalSelectedTrees.addAll(getTopKTreesPerCluster(treeOfClusterQualityIndexHashMap, clusters.get(clust), n));
                }
                end = System.currentTimeMillis();
                formatter = new DecimalFormat("#0.00000000");
                ID = "QualityWithTreeSelection_Top" + String.valueOf(n) + "_" + String.valueOf(selectThisManyClusters);
                wr.write("QualityWithTreeSelection_Top" + String.valueOf(n) + "\t" + selectThisManyClusters + "\t\t" + clusterQualityWithTreeSelectionAUC + "\t" + bias + "\t" + logloss + "\t" + totalSelectedTrees.size() + "\r\n");
                //csvWr.write(ID + "," + Replica + "," + "QualityWithTreeSelection_Top" + String.valueOf(n) + "," + selectThisManyClusters + ",," + clusterQualityWithTreeSelectionAUC + "," + bias + "," + logloss + "," + totalSelectedTrees.size() + "," +  formatter.format(((end - start) + qualitySelectionTime) / 1000d)+ "\r\n");
                csvWr.write(ID + "," + Replica + "," + "QualityWithTreeSelection_Top" + String.valueOf(n) + "," + selectThisManyClusters + ",," + clusterQualityWithTreeSelectionAUC + "," + bias + "," + logloss + "," + totalSelectedTrees.size() + "," +  formatter.format(((end - start)) / 1000d)+ "\r\n");






            }


            //option 5 network selection
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
                    ID = "Network_" + String.valueOf(id) + "_" + String.valueOf(selectThisManyClusters);
                    wr.write("Network " + id + "\t" + selectThisManyClusters + "\t" + selectedTDA.size() + "\t" + tdaAUC + "\t" + bias + "\t" + logloss + "\t" + tdaCLusterTreeCount + "\r\n");
                    csvWr.write(ID + "," + Replica + ","  + "Network " + id + "," + selectThisManyClusters + "," + selectedTDA.size() + "," + tdaAUC + "," + bias + "," + logloss + "," + tdaCLusterTreeCount + "\r\n");

                }
            }



        }
        wr.close();
        csvWr.close();

    }

    private static void analyzeLine(String line,  Map<String, TDAcluster> clusters ,  Map<String, Double> clusterQualityIndexHashMap ,  int [] topKTresSelection) {
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
        // add top k tree selection results
        // set to the last index before top k tree scores
        int i = 10 ;
        double topKTreePrediction = 0;
        for (int k : topKTresSelection) {
            topKTreePrediction = Double.parseDouble(arr[i]);
            SingleEval treeEval = new SingleEval(topKTreePrediction, label);
            treeEval.setK(k);
            // for counting the number of unique trees in this experiment
            if (type.equalsIgnoreCase("test")) {
                cls.addToTestTopKTreeEvals((dp+"-"+String.valueOf(k)),treeEval);
            }
            i ++;
        }
        clusters.put(cluster, cls);
    }

    private static Set<String> getTopKTreesPerCluster(Map<String, Map<String, Double>> treeOfClusterQualityIndexHashMap, TDAcluster cluster, int n) {
        Set<String> selectedTrees = new HashSet<>();
        Map<String, Double> tempTreeOfClusterQualityIndexHashMap = new HashMap<>();
        tempTreeOfClusterQualityIndexHashMap = sortMapByValue(treeOfClusterQualityIndexHashMap.get(cluster.getID()));
        int treeSelectionTopKIndex = n;
        if (n > cluster.getTrees().size())
        {
            treeSelectionTopKIndex = cluster.getTrees().size();

        }
        List<String> treeIDs = new ArrayList<String>(tempTreeOfClusterQualityIndexHashMap.keySet());
        Collections.reverse(treeIDs);
        for (int i = 0 ; i < treeSelectionTopKIndex ; i++)
        {
            // select Top K trees
            selectedTrees.add(treeIDs.get(i));
        }
        return selectedTrees;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
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

    private static List<SingleEval> evaluateWithSelectedForTreeSelection(Set<String> selected, Map<String, TDAcluster> clusters, String treeSelectionK) throws IOException {
        String line = "";
        Map<String, SingleEval> probs = new HashMap<>();
        for (String c : selected) {
            TDAcluster tda = clusters.get(c);
            Map<String, SingleEval> evals = tda.getTestTopKTreeEvals();
            for (String dp : evals.keySet()) {
                if (dp.split("-")[1].equals(treeSelectionK)) {
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
