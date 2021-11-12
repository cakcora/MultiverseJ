package experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class SequentialRunner {
    public static void main(String[] args) throws Exception {
        String projectPath = args[0];

        String resultsPath = projectPath + "results/";
        String treePath = projectPath + "results/trees/";

        File directory = new File(treePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (String datasetName : new String[]{"Bank-note", "adult", "LR", "Poker", "Mushroom", "Nursery",
                "Breast-Cancer", "Connect-4", "Diabetes", "News-popularity"}) {
            // 1 - poisoner experiment
            System.out.println("############ DATASET " + datasetName + "##############");
            String dataPath = projectPath + "data/" + datasetName + "/" + datasetName + ".DATA";
            String expResultsFile = projectPath + "results/" + datasetName + "finalResults.txt";
            new File(expResultsFile).delete();

            String metricPath = projectPath + "results/" + datasetName + "metrics.txt";
            String graphsPath = projectPath + "results/" + datasetName + "graphs.txt";
            String quoter = " ";
            String sep = ",";
            int poisonFirst = 0;
            int poisonLast = 45;
            int poisonIncrementBy = 45;
            int replicate = 0;
            String aucFile = resultsPath + datasetName + "VanillaAucOnTestData.txt";
            new File(aucFile).delete();
            // replicate experiments
            while (++replicate < 5) {
                int seed = (int) (System.currentTimeMillis() / 100000);

                String[] poisonerArgs = new String[]{dataPath, quoter, sep, treePath, metricPath,
                        graphsPath, String.valueOf(seed), String.valueOf(poisonFirst),
                        String.valueOf(poisonLast), String.valueOf(poisonIncrementBy),
                        datasetName, aucFile};
                poisonedLabelExp(poisonerArgs);

                //2 - Mapper clustering experiment
                // first got to python (offline) and install pandas, numpy, sklearn
                String clusterNodes = projectPath + "results/" + datasetName + "clusterNodes.csv";
                String clusterLinks = projectPath + "results/" + datasetName + "clusterLinks.csv";
                String nodeIDS = projectPath + "results/" + datasetName + "clusternodeIDs.csv";

                new File(clusterNodes).delete();
                new File(clusterLinks).delete();
                new File(nodeIDS).delete();

                String command = "python " + projectPath + "python/MultiverseBinaryCode.py " +
                        resultsPath + " " + metricPath + " " +
                        poisonFirst + " " + poisonLast + " " + datasetName;
                // Python execution: System.out.println(command);
                Process p = Runtime.getRuntime().exec(command);
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String result = in.readLine();
                System.out.println("result is : " + result);

                //3 - Mapper cluster selection experiment

                String output = projectPath + "clusterOutPut/" + datasetName + "clusteroutput.txt";
                new File(output).delete();
                String[] mapperArgs = new String[]{clusterNodes, clusterLinks, treePath, nodeIDS, dataPath,
                        quoter, sep, output, String.valueOf(poisonFirst), String.valueOf(poisonLast), String.valueOf(seed)};
                mapperClusterExp(mapperArgs);

                //4 - Cluster performance experiment

                String[] clusterArgs = new String[]{output, clusterLinks, expResultsFile};
                mapperClusterSectionExp(clusterArgs);
            }
            //delete aux files (todo)
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

