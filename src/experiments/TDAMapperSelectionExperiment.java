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
 * Assumes that we have compute tdamapper graph from decision tree metrics.
 * The graph is written into clusterNodes.csv and clusterLinks.csv files
 */
public class TDAMapperSelectionExperiment {
    public static void main(String[] args) throws Exception {
        String nodeFile = args[0];
        String edgeFile = args[1];
        String treeDir = args[2];
        String idFile = args[3];

        // load tdamapper clusters defined by tdamapper
        System.out.println("Loading tda mapper results...");
        Map<Integer, Integer> ids = readIds(idFile);
        List<TDAcluster> clusters = getClusters(treeDir, ids, nodeFile);

        // load dataset
        System.out.println("Loading dataset...");
        LoaderOptions options = new LoaderOptions();
        String csvFile = args[4];
        options.setQuoter(args[5].charAt(0));
        options.setSep(args[6].charAt(0));

        options.featureIgnoreThreshold(20);
        options.convertRealToFactorThreshold(4);
        char separator = options.getSeparator();
        if (separator != ' ' && separator != ',' && separator != '\t')
            System.out.println("Column separator is not set as a comma, space or tab character. Are you sure about that?");
        var csvLoader = new CSVLoader(options);
        List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile);
        Dataset dataset = new Dataset(dataPoints);
        dataset.setFeatureNames(csvLoader.getFeatureNames());
        dataset.setFeatureParents(csvLoader.getFeatureMap());
        System.out.println(Arrays.toString(csvLoader.getFeatureNames()));

        Random random = new Random(151);
        RandomForest rf = new RandomForest(random);
        rf.setNumTrees(300);
        rf.setSampleSize(2000);
        var featureSize = new HashSet(dataset.getFeatureMap().values()).size();
        int splitFeatureSize = 7;//(int) Math.ceil(Math.sqrt(featureSize));
        rf.setNumFeaturesToConsiderWhenSplitting(splitFeatureSize);
        rf.setMaxTreeDepth(100);
        rf.setMinLeafPopulation(3);
        Dataset[] split = dataset.split(0.8, 0.20);
        Dataset test = split[1];
        System.out.println("test dataset contains " + test.getDatapoints().size() + " datapoints");
        System.out.println("Running tda nodes on test data points...");
        BufferedWriter out = new BufferedWriter(new FileWriter("dummy.csv"));
        for (TDAcluster cls : clusters) {
            ArrayList<DecisionTree> trees1 = cls.getTrees();
            HashMap<Integer, Integer> treePoisons = new HashMap<>();
            for (DecisionTree tree : trees1) {
                int poison = tree.getPoisonLevel();//does not work
                int id = (int) tree.getID();
                if (id > 600) {
                    poison = 45;
                }
                if (!treePoisons.containsKey(poison)) treePoisons.put(poison, 0);
                treePoisons.put(poison, treePoisons.get(poison) + 1);
            }

            for (DataPoint dp : test.getDatapoints()) {
                int[] labels = new int[2];
                for (DecisionTree tree : trees1) {
                    double yhat = tree.predict(dp.getFeatures());
                    // softmax
                    labels[(int) yhat]++;
                }
                double y = dp.getLabel();
                Integer dTreesWith0Poison = 0;
                Integer dTreesWith45Poison = 0;
                if (treePoisons.containsKey(0)) dTreesWith0Poison = treePoisons.get(0);
                if (treePoisons.containsKey(45)) dTreesWith45Poison = treePoisons.get(45);
                // System.out.println(dTreesWith45Poison + "\t" + dTreesWith0Poison + "\t" + labels[0] + "\t" + labels[1] + "\t" + y);
                out.write(dp.getID() + "\t" + dTreesWith45Poison + "\t" + dTreesWith0Poison + "\t" + labels[0] + "\t" + labels[1] + "\t" + y + "\r\n");
            }
        }
        out.close();
//        for(int binaryPoisonLevel=0;binaryPoisonLevel<45;binaryPoisonLevel++){
//            for(TDAcluster cls :clusters){
//                ArrayList<DecisionTree> trees1 = cls.getTrees();
//                int majClass,minClass=0;
//                for(DecisionTree tree: trees1){
//                    int lvl=tree.getPoisonLevel();
//                }
//            }
//        }


    }

    private static List getClusters(String treeDir, Map<Integer, Integer> ids, String nodeFile) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(nodeFile));
        List clusters = new ArrayList<TDAcluster>();
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                var arr = line.split("\t");
                String treeIDs = arr[1].replaceAll("\\[", "");
                treeIDs = treeIDs.replaceAll("\\]", "");
                TDAcluster cluster = new TDAcluster();
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

    private static Map<Integer, Integer> readIds(String idFile) throws IOException {
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