package core;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public Dataset[] split(double perc1, double perc2) {
        Dataset d1 = new Dataset();
        Dataset d2 = new Dataset();
        for(int i=0;i<perc1*this.dataPoints.size();i++){
            d1.add(this.dataPoints.get(i));
        }
        for(int j=0;j<perc2*this.dataPoints.size();j++){
            d2.add(this.dataPoints.get(j));
        }
        d1.setFeatureParents(this.getFeatureMap());
        d2.setFeatureParents(this.getFeatureMap());
        d1.setFeatureNames(this.getFeatureNames());
        d2.setFeatureNames(this.getFeatureNames());
        return new Dataset[]{d1,d2};
    }
}
