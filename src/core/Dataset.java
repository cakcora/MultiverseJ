package core;

import java.util.List;
import java.util.Map;

public class Dataset {
    private final List<DataPoint> dataPoints;
    private String[] feeatureNames;
    private Map<Integer, Integer> featureMap;

    public Dataset(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;

    }

    public void setFeatureNames(String[] featureNames) {
        this.feeatureNames= featureNames;
    }

    public String[] getFeatureNames() {
        return feeatureNames;
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
