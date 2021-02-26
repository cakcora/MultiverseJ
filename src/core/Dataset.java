package core;

import java.util.List;
import java.util.Map;

public class Dataset {
    private final List<DataPoint> dataPoints;
    private String[] featureNames;
    private Map<Integer, Integer> featureMap;

    public Dataset(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;

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
}
