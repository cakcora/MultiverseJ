package experiments;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Random;

public class SequentialRunner {
    public static void main(String[] args) throws Exception {
        String projectPath = args[0];

        //Excluded datasets because AUC is strangely 1 everywhere. "Mushroom",
        for (String datasetName : new String[]{"Breast-cancer", "spambase", "credit", "adult", "LR",
                "Poker", "Nursery", "Connect-4", "Diabetes", "News-popularity"}) {


            int poisonFirst = 0;
            for (int poisonLast : new int[]{0, 2, 4, 6, 8, 10, 20, 40}) {
                System.out.println("############ DATASET " + datasetName + "########################################################");

                System.out.println("Poisons: " + poisonFirst + " and " + poisonLast);
                String resultsPath = projectPath + "results/" + poisonFirst + "_" + poisonLast + "/";
                String treePath = resultsPath + "trees/";
                FileUtils.deleteDirectory(new File(treePath));

                File directory = new File(treePath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                // 1 - poisoner experiment
                String dataPath = projectPath + "data/" + datasetName + "/" + datasetName + ".DATA";
                String expResultsFile = resultsPath + datasetName + "finalResults.txt";
                new File(expResultsFile).delete();

                String metricPath = resultsPath + datasetName + "metrics.txt";
                String graphsPath = resultsPath + datasetName + "graphs.txt";
                String quoter = " ";
                String sep = ",";
                int poisonIncrementBy = ((poisonLast == 0) ? 10 : poisonLast);
                int replicate = 5;
                String aucFile = resultsPath + datasetName + "VanillaAucOnTestData.txt";
                new File(aucFile).delete();
                // replicate experiments
                Random r = new Random();
                while (--replicate > 0) {
                    int seed = r.nextInt(100);

                    String[] poisonerArgs = new String[]{dataPath, quoter, sep, treePath, metricPath,
                            graphsPath, String.valueOf(seed), String.valueOf(poisonFirst),
                            String.valueOf(poisonLast), String.valueOf(poisonIncrementBy),
                            datasetName, aucFile};
                    poisonedLabelExp(poisonerArgs);

                    //2 - Mapper clustering experiment
                    // first got to python (offline) and install pandas, numpy, sklearn
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
                            quoter, sep, output, String.valueOf(poisonFirst), String.valueOf(poisonLast), String.valueOf(seed)};
                    mapperClusterExp(mapperArgs);

                    //4 - Cluster performance experiment

                    String[] clusterArgs = new String[]{output, clusterLinks, expResultsFile, clusterNodes};
                    mapperClusterSectionExp(clusterArgs);

                    //new File(clusterNodes).delete();
                    new File(clusterLinks).delete();
                    //new File(nodeIDS).delete();
                    new File(output).delete();
                    new File(graphsPath).delete();
                    new File(metricPath).delete();

                }

                //delete aux files (todo)
            }
        }
    }

    public static void poisonedLabelExp(String[] argArray) {
        System.out.print("Running PLE .. \n");
        try {

            PoisonedLabelExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  PLE ... \n");
        }
    }

    public static void mapperClusterExp(String[] argArray) {
        System.out.print("Running TDAMap ... \n");
        try {
            TDAMapperSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  TDAMap ... \n");
        }
    }

    public static void mapperClusterSectionExp(String[] argArray) {
        System.out.print("Running ClusterSelectionExperiment ... \n");
        try {
            ClusterSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  ClusterSelectionExperiment ... \n");
        }
    }
}

