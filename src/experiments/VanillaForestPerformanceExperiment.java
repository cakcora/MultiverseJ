package experiments;

import core.DataPoint;
import core.Dataset;
import core.DecisionTree;
import core.RandomForest;
import loader.CSVLoader;
import loader.LoaderOptions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Assumes that we have computed a vanilla random forest by using PoisonedlabelExperiment.java and stored its trees.
 */
public class VanillaForestPerformanceExperiment {
    public static void main(String[] args) throws Exception {


        /*  run params:
        300
C:/multiverse/trees/
"C:/data/adult.data"
" "
","
0
seed
         */
        int numTree = Integer.parseInt(args[0]);
        String treeDir = args[1];
        String csvFile = args[2];
        char quoteChar = args[3].charAt(0);
        char sepChar = args[4].charAt(0);
        String kPredictionOutputFile = args[5];
        int seed = Integer.parseInt(args[6]);

        // load tdamapper clusters defined by tdamapper
        List<DecisionTree> trees = loadTrees(treeDir, numTree);

        // load dataset
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
        List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile).get(0);
        Dataset dataset = new Dataset(dataPoints);
        dataset.setFeatureNames(csvLoader.getFeatureNames());
        dataset.setFeatureParents(csvLoader.getFeatureMap());

        Random random = new Random(151);
        RandomForest rf = new RandomForest(random);
        rf.setNumTrees(numTree);
        var featureSize = new HashSet(dataset.getFeatureMap().values()).size();
        int splitFeatureSize = (int) Math.ceil(Math.sqrt(featureSize));
        rf.setNumFeaturesToConsiderWhenSplitting(splitFeatureSize);
        rf.setMaxTreeDepth(100);
        rf.setMinLeafPopulation(3);
        Random rnd = new Random(seed);
        dataset.shuffleDataPoints(rnd);
        Dataset[] split = dataset.split(80, 10, 10);
        Dataset validation = split[1];
        Dataset test = split[2];
        System.out.println("Validation dataset contains " + validation.getDatapoints().size() + " datapoints");
        BufferedWriter out = new BufferedWriter(new FileWriter(kPredictionOutputFile));


        // using trees on validation data
        for (DataPoint dp : validation.getDatapoints()) {
            for (DecisionTree tree : trees) {
                double yhat = tree.predict(dp.getFeatures());
                double y = dp.getLabel();
                if (Double.isNaN(yhat)) {
                    System.out.println(" Data point leads to Nan probability.");
                    continue;
                }
                out.write(tree.getID() + "\tvalidation\t" + dp.getID() + "\t" + yhat + "\t" + y + "\r\n");
            }
        }
        // using trees on test data
        for (DataPoint dp : test.getDatapoints()) {
            for (DecisionTree tree : trees) {
                double yhat = tree.predict(dp.getFeatures());

                double y = dp.getLabel();
                if (Double.isNaN(yhat)) {
                    System.out.println(" Data point leads to Nan probability.");
                    continue;
                }
                out.write(tree.getID() + "\ttest\t" + dp.getID() + "\t" + yhat + "\t" + y + "\r\n");
            }
        }
        out.close();
    }

    private static List loadTrees(String treeDir, int numTrees) {
        List<DecisionTree> trees = new ArrayList<>();
        for (int id = 0; id < numTrees; id++) {
            DecisionTree dt = new DecisionTree();
            dt.readTree(treeDir, id + "");
            dt.setID(id);
            trees.add(dt);
        }
        return trees;
    }
}
