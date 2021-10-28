package experiments;

import TDA.TDAcluster;
import core.DataPoint;
import core.Dataset;
import core.DecisionTree;
import core.RandomForest;
import loader.CSVLoader;
import loader.LoaderOptions;

import java.io.*;
import java.util.*;

/**
 * Assumes that we have computed the tdamapper graph from decision tree metrics.
 * The graph is written into clusterNodes.csv and clusterLinks.csv files
 */
public class TDAMapperSelectionExperiment {
    public static void main(String[] args) throws Exception {


        /*  run params:
         C:/multiverse/clusterNodes.csv
C:/multiverse/clusterLinks.csv
C:/multiverse/trees/
C://multiverse/clusternodeIDs.csv
"C:/data\Multiverse Data Research\03 The knote Authentication Dataset\data_banknote_authentication.txt"
" "
","
C:/multiverse/clusteroutput.txt
0
45
         */
        String nodeFile = args[0];
        String edgeFile = args[1];
        String treeDir = args[2];
        String idFile = args[3];
        String csvFile = args[4];
        char quoteChar = args[5].charAt(0);
        char sepChar = args[6].charAt(0);
        String clusterPredictionOutputFile = args[7];
        int targetPoison1 = Integer.parseInt(args[8]);
        int targetPoison2 = Integer.parseInt(args[9]);


        // load tdamapper clusters defined by tdamapper
        System.out.println("Loading tda mapper results...");
        Map<Integer, Integer> decisionTreeIDMap = readIds(idFile);
        List<TDAcluster> clusters = getClusters(treeDir, decisionTreeIDMap, nodeFile);

        // load dataset
        System.out.println("Loading dataset...");
        LoaderOptions options = new LoaderOptions();
        options.setQuoter(quoteChar);
        options.setSep(sepChar);


        options.featureIgnoreThreshold(20);
        options.convertRealToFactorThreshold(4);
        char separator = options.getSeparator();
        if (separator != ' ' && separator != ',' && separator != '\t') {
            System.out.println("Column separator is not set as a comma, space or tab character. Are you sure about that?");
        }
        var csvLoader = new CSVLoader(options);
        List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile);
        Dataset dataset = new Dataset(dataPoints);
        dataset.setFeatureNames(csvLoader.getFeatureNames());
        dataset.setFeatureParents(csvLoader.getFeatureMap());

        Random random = new Random(151);
        RandomForest rf = new RandomForest(random);
        rf.setNumTrees(300);
        var featureSize = new HashSet(dataset.getFeatureMap().values()).size();
        int splitFeatureSize = (int) Math.ceil(Math.sqrt(featureSize));
        rf.setNumFeaturesToConsiderWhenSplitting(splitFeatureSize);
        rf.setMaxTreeDepth(100);
        rf.setMinLeafPopulation(3);

        Dataset[] split = dataset.randomSplit(20 , false);
        Dataset test = split[1];

        System.out.println("Test dataset contains " + test.getDatapoints().size() + " datapoints");
        BufferedWriter out = new BufferedWriter(new FileWriter(clusterPredictionOutputFile));

        for (TDAcluster cls : clusters) {
            ArrayList<DecisionTree> treesOfACluster = cls.getTrees();
            HashMap<Integer, Integer> treePoisons = new HashMap<>();
            for (DecisionTree tree : treesOfACluster) {
                int poison = tree.getPoisonLevel();

                if (!treePoisons.containsKey(poison)) treePoisons.put(poison, 0);
                treePoisons.put(poison, treePoisons.get(poison) + 1);
            }

            for (DataPoint dp : test.getDatapoints()) {
                double prob = 0d;
                for (DecisionTree tree : treesOfACluster) {
                    prob += tree.predict(dp.getFeatures());
                }
                double yhat = prob / treesOfACluster.size();
                double y = dp.getLabel();
                if (Double.isNaN(yhat)) {
                    System.out.println(" Data point leads to Nan probability.");
                    continue;
                }
                int dTreesWithPoison1 = 0;
                int dTreesWithPoison2 = 0;
                if (treePoisons.containsKey(targetPoison1)) dTreesWithPoison1 = treePoisons.get(targetPoison1);
                if (treePoisons.containsKey(targetPoison2)) dTreesWithPoison2 = treePoisons.get(targetPoison2);
                out.write(cls.getID() + "\t" + dp.getID() + "\t" + dTreesWithPoison2 + "\t" + dTreesWithPoison1 + "\t" + yhat + "\t" + y + "\r\n");
            }
        }

        out.close();
    }

    private static List getClusters(String treeDir, Map<Integer, Integer> ids, String nodeFile) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(nodeFile));
        List clusters = new ArrayList<TDAcluster>();
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                var arr = line.split("\t");
                String clusterID = arr[0];
                String treeIDs = arr[1].replaceAll("\\[", "");
                treeIDs = treeIDs.replaceAll("\\]", "");
                TDAcluster cluster = new TDAcluster();
                cluster.setId(clusterID);
                for (String i : treeIDs.split(",")) {
                    DecisionTree dt = new DecisionTree();
                    int id = Integer.parseInt(i.trim());
                    Integer j = ids.get(id);
                    dt.readTree(treeDir, j + "");
                    dt.setID(j);
                    cluster.addTree(dt);
                }
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    static Map<Integer, Integer> readIds(String idFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(idFile));
        String line = "";
        Map<Integer, Integer> ids = new HashMap<>();
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] arr = line.split(",");
            ids.put(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
        }
        return ids;
    }
}
