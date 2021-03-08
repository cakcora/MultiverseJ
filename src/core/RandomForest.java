package core;

import mlcore.DecisionTreeLearner;

import java.util.*;

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
    private int maxFeatures=0;
    private int minPopulation;

    // random seed
    Random random;

    public RandomForest(Random random) {
        this.random = random;
    }

    /**
     * Trains numTrees decision trees
     *
     * @param dataset a set of data points
     */
    public void train(Dataset dataset) {
        List<DataPoint> dataPoints = dataset.getDatapoints();
        //sanity checks on the data
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new RuntimeException("Dataset has no data points");
        }


        Map<Integer, Integer> featureMap = dataset.getFeatureMap();
        HashSet origFeatures = new HashSet(featureMap.values());
        int featureCount = origFeatures.size();
        // number of features to use in each bootstrapping step
        if(maxFeatures==0) {
            //maxFeatures has not been set. In that case we will use the sqrt number of features
            maxFeatures = (featureCount > 1) ? (int) Math.sqrt(featureCount) : 1;
        }

        for (int tree = 0; tree < numTrees; tree++) {
            //sample data points
            List<DataPoint> baggedDataset = sampleData(dataPoints);
            //sample data features to be used in the DT
            ArrayList<Integer> sampledFeatures = sampleFeatures(maxFeatures,featureCount,featureMap);
            DecisionTreeLearner dt = new DecisionTreeLearner(this.maxDepth,this.minPopulation,sampledFeatures);
            DecisionTree decisionTree = dt.train(baggedDataset);
            //save the tree
            decisionTrees.add(decisionTree);
        }
    }



    /**
     * Sample a sampleThisMany number of features to be used in a decision tree.
     * @param sampleThisMany we will return this many features
     * @param fromThisMany is the number of all available features
     * @param featureMap
     * @return ids of features
     */


    private ArrayList<Integer> sampleFeatures(int sampleThisMany, int fromThisMany, Map<Integer, Integer> featureMap) {
        HashSet<Integer> features = new HashSet<>();
        if(sampleThisMany>fromThisMany){

            information.add("DT requested "+sampleThisMany+" but we have "+fromThisMany+" features overall.");
            sampleThisMany = fromThisMany;

        }
        while (features.size() != sampleThisMany) {
            features.add(random.nextInt(fromThisMany));
        }
        var arr = new ArrayList<Integer>();
        for(int i:features) {
                for (int ch : featureMap.keySet()) {
                    if (featureMap.get(ch) == i) {
                        arr.add(ch);
                    }
                }
        }
        return arr;
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
            this.addInfoMessage("Dataset does not contain " + maxSamples + " data points to bag, will use " + size + " instead");
            maxSamples = size;
        }

        List<DataPoint> baggedDataset = new ArrayList<>();
        Random r = new Random();
        // add data points with replacement
        for (int t = 0; t < maxSamples; t++) {
            int index = r.nextInt(size);
            baggedDataset.add(dataPoints.get(index));
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

    /**
     * Set the number of trees in the random forest
     *
     * @param treeCount number of trees
     */
    public void setNumTrees(int treeCount) {
        this.numTrees = treeCount;
        this.decisionTrees = new ArrayList<>();
    }

    // set the number of data points to sample for bagging at each decision tree
    public void setSampleSize(int sampleSize) {
        this.maxSamples = sampleSize;
    }

    // set the number of features to sample at each step
    public void setNumFeaturesToConsiderWhenSplitting(int featureSize) {
        this.maxFeatures = featureSize;
    }

    public List<DecisionTree> getDecisionTrees() {
        return decisionTrees;
    }

    public void setMaxTreeDepth(int depth) {
        this.maxDepth =depth;
    }

    public void setMinLeafPopulation(int population) {
        this.minPopulation=population;
    }
}
