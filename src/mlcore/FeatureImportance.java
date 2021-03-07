package mlcore;

import core.Dataset;
import core.RandomForest;

import java.util.Map;

public class FeatureImportance {

    public static void computeFeatureImportance(RandomForest rfSecondLevel, Dataset dataset) {
        Map<Integer, Integer> featureMap = dataset.getFeatureMap();
        int size = dataset.getDatapoints().size();
        double [][] array1OverDataPoints = new double[size][featureMap.size()];
        for(int i = 0;i<featureMap.size();i++){

            for(int j=i+1;j<featureMap.size();j++){
                double [] array2OverDataPoints = new double[size];

            }
        }
    }
}
