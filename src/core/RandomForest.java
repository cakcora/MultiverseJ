package core;

import mlcore.DecisionTreeLearner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Represents Random Forest object.
 *
 * @author Murat Ali Bayir
 * @author Cuneyt Akcora
 */
public class RandomForest {

    // The maximum depth of the tree. If unspecified, nodes are expanded until
    // all leaves are pure or until all leaves contain less than minSamplesLeaf samples.
    public int maxDepth;
    private List<DecisionTree> decisionTrees;
    // Randomness seed to be used
    public long randomState = 57;
    //  The number of data points to be used in bagging
    public int maxSamples = 1000;

    // Minimum number of samples for each node.
    // If reached the minimum, we mark the node as
    // leaf node without further splitting.
    public static final int TREE_MIN_SIZE = 1;
    private int numTrees = 500;
    //The minimum number of samples required to split an internal node.
    private int minSamplesSplit = 2;
    //The minimum number of samples required to be at a leaf node
    private int minSamplesLeaf = 1;
    private HashSet<String> information = new HashSet<String>();

    // size of sampling for each bootstrap step.
    private int maxFeatures;

    /**
     * Trains numTrees decision trees
     *
     * @param dataPoints a set of data points
     */
    public RandomForest(List<DataPoint> dataPoints) {
        //sanity checks on the data
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new RuntimeException("Dataset has no data points");
        }

        int numFeatures = dataPoints.get(0).getFeatures().length;
        // number of features to use in each bootstrapping step
        maxFeatures = (numFeatures > 1) ? (int) Math.sqrt(numFeatures) : 1;


        for (int tree = 0; tree < numTrees; tree++) {
            List<DataPoint> baggedDataset = sampleData(dataPoints);
            DecisionTreeLearner dt = new DecisionTreeLearner();
            dt.train(baggedDataset);
            //decisionTrees.add(dt.getTree()); missing method
        }
    }

    /**
     * Create a bagged dataset by sampling from the original dataset with replacement.
     *
     * @param dataPoints the original dataset
     * @return a bagged set of data points which may contain duplicates
     */
    List<DataPoint> sampleData(List<DataPoint> dataPoints) {

        int size = dataPoints.size();
        if (maxSamples > size) {
            maxSamples = size;
            this.addInfoMessage("Dataset does not contain " + maxSamples + " data points to bag, will use " + size + " instead");
        }

        List<DataPoint> baggedDataset = new ArrayList<>();
        Random r = new Random();
        // add data points with replacement
        for (int t = 0; t < maxSamples; t++) {
            baggedDataset.add(dataPoints.get(r.nextInt(size)));
        }
        // checking how many duplicates we have sampled from the data
        HashSet<DataPoint> uniques = new HashSet<>();
        uniques.addAll(baggedDataset);
        double size1 = uniques.size();
        addInfoMessage(size1 / maxSamples + " uniqueness in bagging");
        return baggedDataset;
    }

    // adds a message to this class
    void addInfoMessage(String message) {
        information.add(message);
    }

    // returns a list of generated information messages
    public HashSet<String> getInfoMessages() {
        return information;
    }
}
