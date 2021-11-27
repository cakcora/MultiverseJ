package experiments;

import TDA.TDAcluster;
import metrics.MetricComputer;
import metrics.SingleEval;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//requires results from the VanillaForestTreeSelectionExperiment
public class VanillaForestTreeSelectionExperiment {
    public static void main(String[] args) throws IOException {
        String vanillaPredictionOutputFile = args[0];
        String vanillaAUCresultFile = args[1];
        BufferedWriter wr = new BufferedWriter(new FileWriter(vanillaAUCresultFile, true));


        BufferedReader br = new BufferedReader(new FileReader(vanillaPredictionOutputFile));
        Map<String, TDAcluster> clusters = new HashMap<>();
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] arr = line.split("\t");

            String treeID = arr[0];
            String type = arr[1];

            String dp = arr[2];
            double predicted = Double.parseDouble(arr[3]);
            double label = (int) Double.parseDouble(arr[4]);
            TDAcluster cls;
            if (clusters.containsKey(treeID))
                cls = clusters.get(treeID);
            else cls = new TDAcluster();
            cls.setId(treeID);
            cls.setTreeCount(1);//we have a single decision tree in the cluster
            SingleEval eval = new SingleEval(predicted, label);
            if (type.equalsIgnoreCase("validation")) {
                cls.addToValidationEvals(dp, eval);
            } else if (type.equalsIgnoreCase("test")) {
                cls.addToTestEvals(dp, eval);
            }
            clusters.put(treeID, cls);
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
            List<SingleEval> evaluationsGreedy = evaluateWithSelected(selectedGreedy, clusters);
            double greedyAUC = metric.computeAUC(evaluationsGreedy);
            double bias = metric.computeBias(evaluationsGreedy);
            double logloss = metric.computeLogLoss(evaluationsGreedy);

            Set gh = new HashSet();
            for (String clust : selectedGreedy) {
                gh.addAll(clusters.get(clust).getTrees());
            }
            int greedyCLusterTreeCount = gh.size();
            wr.write("Greedy" + "\t" + selectThisManyClusters + "\t\t" + greedyAUC + "\t" + bias + "\t" + logloss + "\r\n");

            List<SingleEval> evaluationsRandom = evaluateWithSelected(selectedRandom, clusters);
            double randomAUC = metric.computeAUC(evaluationsRandom);
            bias = metric.computeBias(evaluationsRandom);
            logloss = metric.computeLogLoss(evaluationsRandom);
            gh = new HashSet();
            for (String clust : selectedRandom) {
                gh.addAll(clusters.get(clust).getTrees());
            }
            int randomCLusterTreeCount = gh.size();
            wr.write("Random" + "\t" + selectThisManyClusters + "\t\t" + randomAUC + "\t" + bias + "\t" + logloss + "\r\n");
        }
        wr.close();

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
