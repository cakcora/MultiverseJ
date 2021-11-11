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
     *
     * @param percentageOfDataPointsTobeSelected size of the split
     * @return two datasets: training and test
     */
    public Dataset[] split(int percentageOfDataPointsTobeSelected) {
        Dataset d1 = new Dataset();
        Dataset d2 = new Dataset();
        int v = (int) Math.ceil(dataPoints.size() * percentageOfDataPointsTobeSelected / 100);

        for (int i = 0; i < v; i++) {
            DataPoint datapoint = this.dataPoints.get(i);
            datapoint.setID(i);
            d1.add(datapoint);
        }
        for (int j = v; j < this.dataPoints.size(); j++) {
            DataPoint datapoint = this.dataPoints.get(j);
            datapoint.setID(j);
            d2.add(datapoint);
        }
        d1.setFeatureParents(this.getFeatureMap());
        d2.setFeatureParents(this.getFeatureMap());
        d1.setFeatureNames(this.getFeatureNames());
        d2.setFeatureNames(this.getFeatureNames());
        return new Dataset[]{d2, d1};
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
