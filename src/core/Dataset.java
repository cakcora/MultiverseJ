package core;

import java.util.*;

public class Dataset {
    private final List<DataPoint> dataPoints;
    private String[] featureNames;
    private Map<Integer, Integer> featureMap;

    public Dataset(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public Dataset(){
        this.dataPoints = new ArrayList<>();
    }

    public void setFeatureNames(String[] featureNames) {
        this.featureNames = featureNames;
    }

    public String[] getFeatureNames() {

        return featureNames;
    }

    public void setFeatureParents(Map<Integer, Integer> featureMap) {
        this.featureMap = featureMap;
    }

    public Map<Integer, Integer> getFeatureMap() {
        return featureMap;
    }

    public List<DataPoint> getDatapoints() {
        return dataPoints;
    }

    public void add(DataPoint datapoint) {
        this.dataPoints.add(datapoint);
    }

    /**
     * Split the dataset into training and test sets. In order to have verifiable execution, we do not sample
     * from the dataset. Instead we pick the training data from the first data points.
     *
     * @param training   size of the split
     * @param validation size of the split
     * @param test       size of the split
     * @return two datasets: training and test
     */
    public Dataset[] split(int training, int validation, int test) {
        if ((training + validation + test) != 100) {
            return null;
        }
        Dataset trainingData = new Dataset();
        Dataset validationData = new Dataset();
        Dataset testData = new Dataset();
        int v1 = (int) Math.ceil(dataPoints.size() * training / 100);
        int v2 = (int) Math.ceil(dataPoints.size() * validation / 100);
        for (int i = 0; i < v1; i++) {
            DataPoint datapoint = this.dataPoints.get(i);
            datapoint.setID(i);
            trainingData.add(datapoint);
        }
        for (int j = v1; j < (v1 + v2); j++) {
            DataPoint datapoint = this.dataPoints.get(j);
            datapoint.setID(j);
            validationData.add(datapoint);
        }
        for (int k = (v1 + v2); k < this.dataPoints.size(); k++) {
            DataPoint datapoint = this.dataPoints.get(k);
            datapoint.setID(k);
            testData.add(datapoint);
        }
        trainingData.setFeatureParents(this.getFeatureMap());
        validationData.setFeatureParents(this.getFeatureMap());
        testData.setFeatureParents(this.getFeatureMap());
        trainingData.setFeatureNames(this.getFeatureNames());
        validationData.setFeatureNames(this.getFeatureNames());
        testData.setFeatureNames(this.getFeatureNames());
        return new Dataset[]{trainingData, validationData, testData};
    }


    /**
     * Shuffle the index of data points in a dataset. We need this function to choose
     * the same training/test split in multiple python/java files. Without shuffling, the data may have a hidden order
     * that can bias results. After shuffling, we can always use the first 80% of data points as the training set.
     * This solution is better than storing the test dataset in a file.
     *
     * @param rnd randomness seed to select the same training and test set
     */
    public void shuffleDataPoints(Random rnd) {
        Collections.shuffle(this.dataPoints, rnd);
    }


}
