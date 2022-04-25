package experiments;

import TDA.TFEvaluationOutput;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class SequentialRunner {
    static TFEvaluationOutput returnObj = new TFEvaluationOutput();

    public static void main(String[] args) throws Exception {
        //

        String projectPath = args[0];
        int numTree = 300;
        // K variable for Top K Cluster Selection
        int numClusterSelectionK = 5;

        //Excluded datasets because AUC is strangely 1 everywhere. "Mushroom",
        for (String datasetName : new String[]{"adult"}) {
        // , "adult","Diabetes", "Breast-cancer", "spambase", "credit", "LR",
            //                "Poker", "Nursery", "C4", "Diabetes", "News-popularity , "Poker" , "Nursery""

            int poisonFirst = 0;
            for (int poisonLast : new int[]{0/*, 2, 4, 6, 8, 10, 20, 40*/}) {
                System.out.println("############ DATASET " + datasetName + "########################################################");

                System.out.println("Poisons: " + poisonFirst + " and " + poisonLast);
                String resultsPath = projectPath + "results/" + poisonFirst + "_" + poisonLast + "/";
                String treePath = resultsPath + datasetName + "trees/";
                FileUtils.deleteDirectory(new File(treePath));

                File directory = new File(treePath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                // 1 - poisoner experiment
                String dataPath = projectPath + "data/" + datasetName + "/" + datasetName + ".DATA";
                String TFFinalResultsAUCFile = resultsPath + datasetName + "TFfinalResults.txt";
                new File(TFFinalResultsAUCFile).delete();

                String metricPath = resultsPath + datasetName + "metrics.txt";
                String graphsPath = resultsPath + datasetName + "graphs.txt";
                String quoter = " ";
                String sep = ",";
                int poisonIncrementBy = ((poisonLast == 0) ? 10 : poisonLast);
                int replicate = 2;
                // We used firstAucFile to save the AUC score of the vanilla forest on the test data.
                // this is kind of redundant now because experiment 5-2 can now compute the same auc value
                String firstAucFile = resultsPath + datasetName + "VanillaAucOnTestData.txt";
                new File(firstAucFile).delete();
                String VFFinalResultsAUCFile = resultsPath + datasetName + "VFfinalResults.txt";
                new File(VFFinalResultsAUCFile).delete();
                // replicate experiments
                Random r = new Random();
                while (--replicate > 0) {
                    int seed = r.nextInt(100);

                    String[] poisonerArgs = new String[]{dataPath, quoter, sep, treePath, metricPath,
                            graphsPath, String.valueOf(seed), String.valueOf(poisonFirst),
                            String.valueOf(poisonLast), String.valueOf(poisonIncrementBy),
                            datasetName, firstAucFile, String.valueOf(numTree)};
                    poisonedLabelExp(poisonerArgs);

                    //2 - Mapper clustering experiment
                    // first go to python (offline) and install pandas, numpy, sklearn
                    String clusterNodes = resultsPath + datasetName + "clusterNodes.csv";
                    String clusterLinks = resultsPath + datasetName + "clusterLinks.csv";
                    String nodeIDS = resultsPath + datasetName + "clusternodeIDs.csv";

                    String command = "python " + projectPath + "python/MultiverseBinaryCode.py " +
                            resultsPath + " " + metricPath + " " +
                            poisonFirst + " " + poisonLast + " " + datasetName;
                    // Python execution: System.out.println(command);
                    Process p = Runtime.getRuntime().exec(command);
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String result = in.readLine();
                    System.out.println("result is : " + result);

                    //3 - Mapper cluster selection experiment

                    String output = resultsPath + datasetName + "clusteroutput.txt";
                    String[] mapperArgs = new String[]{clusterNodes, clusterLinks, treePath, nodeIDS, dataPath,
                            quoter, sep, output, String.valueOf(poisonFirst), String.valueOf(poisonLast), String.valueOf(seed), String.valueOf(replicate) };
                    mapperClusterExp(mapperArgs);

                    //4 - Cluster performance experiment

                    String[] clusterArgs = new String[]{output, clusterLinks, TFFinalResultsAUCFile, clusterNodes, String.valueOf(numClusterSelectionK) , String.valueOf(replicate)};
                    mapperClusterSectionExp(clusterArgs);

                    //5-1 Vanilla forest performance experiments
                    String kPredictionOutputFile = resultsPath + "adultVFK.csv";

                    String[] vanillaPerfArgs = new String[]{
                            String.valueOf(numTree), treePath, dataPath, quoter, sep, kPredictionOutputFile, String.valueOf(seed)};
                    VanillaForestPerformanceExperiment.main(vanillaPerfArgs);
                    //5-2 Vanilla forest tree selection experiments
                    String[] vanillaAucArgs = new String[]{kPredictionOutputFile, VFFinalResultsAUCFile};
                    VanillaForestTreeSelectionExperiment.main(vanillaAucArgs);

                    // Should we keep result files for future analysis: yes
                    //new File(clusterLinks).delete();
                    //new File(output).delete();
                    //new File(graphsPath).delete();
                    //new File(metricPath).delete();

                }

                //delete aux files (may be)
            }
        }
    }

    public static void poisonedLabelExp(String[] argArray) {
        System.out.print("Running PLE .. \n");
        try {

            PoisonedLabelExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("error in   PLE ... \n");
        }
    }

    public static void mapperClusterExp(String[] argArray) {
        System.out.print("Running TDAMap ... \n");
        try {

            returnObj = TopologicalForestPerformanceExperiment.main(argArray);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("error in   TDAMap ... \n");
        }
    }

    public static void mapperClusterSectionExp(String[] argArray) {
        System.out.print("Running ClusterSelectionExperiment ... \n");
        try {
            TopologicalForestClusterSelectionExperiment.main(argArray, returnObj.getClusterQualityIndexHashMap(), returnObj.getTreeOfClusterQualityIndexHashMap(), returnObj.getTopKTreeSelection());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("error in   ClusterSelectionExperiment ... \n");
        }
    }
}

