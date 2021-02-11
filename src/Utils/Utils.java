package Utils;

import core.DataPoint;

public class Utils {
    public static DataPoint clone(DataPoint dataPoint) {
        DataPoint newDp = new DataPoint();
        double[] features = dataPoint.getFeatures();
        double label = dataPoint.getLabel();
        boolean[] isCategorical = dataPoint.getCategorical();

        double[] newFeatures = new double[features.length];
        boolean[] newIsCategorical = new boolean[isCategorical.length];

        for (int i = 0; i < newFeatures.length; i++) {
            newFeatures[i]=features[i];
            newIsCategorical[i]=isCategorical[i];
        }
        newDp.setFeatures(newFeatures);
        newDp.setFeatureTypes(newIsCategorical);
        return newDp;

    }
}
