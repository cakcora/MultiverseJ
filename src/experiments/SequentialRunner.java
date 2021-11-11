package experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


public class SequentialRunner {
    public static void main(String[] args) throws Exception {
        String projectPath = args[0];
        String treePath = projectPath + "results/trees/";
        File directory = new File(treePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String dataset = "adult";
        // 1 - poisoner experiment
        String dataPath = projectPath + "data/" + dataset + "/" + dataset + ".DATA";

        String resultsPath = projectPath + "results/";
        String metricPath = projectPath + "results/metrics.txt";
        String graphsPath = projectPath + "results/graphs.txt";
        String quoter = " ";
        String sep = ",";
        int seed = 27;
        int poisonFirst = 0;
        int poisonLast = 45;
        int poisonIncrementBy = 45;


        String[] poisonerArgs = new String[]{dataPath, quoter, sep, treePath, metricPath,
                graphsPath, String.valueOf(seed), String.valueOf(poisonFirst), String.valueOf(poisonLast), String.valueOf(poisonIncrementBy)};
        poisonedLabelExp(poisonerArgs);

        //2 - Mapper clustering experiment
        // first got to python (offline) and install pandas, numpy, sklearn
        String clusterNodes = projectPath + "results/" + dataset + "clusterNodes.csv";
        String clusterLinks = projectPath + "results/" + dataset + "clusterLinks.csv";
        String nodeIDS = projectPath + "results/" + dataset + "clusternodeIDs.csv";

        new File(clusterNodes).delete();
        new File(clusterLinks).delete();
        new File(nodeIDS).delete();

        String command = "python " + projectPath + "python/MultiverseBinaryCode.py " +
                resultsPath + " " + metricPath + " " +
                poisonFirst + " " + poisonLast + " " + dataset;
        // Python execution: System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String result = in.readLine();
        System.out.println("result is : " + result);

        //3 - Mapper cluster selection experiment

        String output = projectPath + "clusterOutPut/" + dataset + "clusteroutput.txt";
        new File(output).delete();
        String[] mapperArgs = new String[]{clusterNodes, clusterLinks, treePath, nodeIDS, dataPath,
                quoter, sep, output, String.valueOf(poisonFirst), String.valueOf(poisonLast), String.valueOf(seed)};
        mapperClusterExp(mapperArgs);

        //4 - Cluster performance experiment
        String expResults = projectPath + "results/" + dataset + "finalResults.txt";
        new File(expResults).delete();
        String[] clusterArgs = new String[]{output, clusterLinks, expResults};
        mapperClusterSectionExp(clusterArgs);
    }

    public static void poisonedLabelExp(String[] argArray) {
        System.out.print("\n Running PLE .. \n");
        try {

            PoisonedLabelExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  PLE ... \n");
        }
    }

    public static void mapperClusterExp(String[] argArray) {
        System.out.print("\n Running TDAMap ... \n");
        try {
            TDAMapperSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  TDAMap ... \n");
        }
    }

    public static void mapperClusterSectionExp(String[] argArray) {
        System.out.print("\n Running ClusterSelectionExperiment ... \n");
        try {
            ClusterSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  ClusterSelectionExperiment ... \n");
        }
    }
}

