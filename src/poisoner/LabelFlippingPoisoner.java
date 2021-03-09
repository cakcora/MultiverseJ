package poisoner;

import core.DataPoint;
import core.Dataset;

import java.util.*;

public class LabelFlippingPoisoner {
    private final Random random;
    public LabelFlippingPoisoner(Random random){
        this.random = random;
    }
    /**
     * A function to inject poison to the data set. the poison is untargeted label flipping.
     * @param dataset to be poisoned
     * @param poisonLevel a number between 0 -- 50. We will poison this percentage of data points
     * @return a newly created, poisoned data set
     */
    public Dataset poison(Dataset dataset, int poisonLevel) {
        if(poisonLevel<0||poisonLevel>100){
            return null;
        }
        // compute how many data points we should poison
        List<DataPoint> dataPoints = dataset.getDatapoints();
        int poisonSize = (int) Math.ceil(dataPoints.size() * poisonLevel / 100.0);
        ArrayList<DataPoint> poisonedDataPoints = new ArrayList<>();
        //create a new dataset to be poisoned
        for (DataPoint dataPoint : dataPoints) {
            //Add the object clones
            poisonedDataPoints.add(dataPoint.clone());
        }

        Set<Integer> poisoned = new HashSet<>();
        int pos2neg = 0;
        int neg2pos = 0;
        for (int i = 0; i < poisonSize; i++) {
            int indexToBePoisoned = random.nextInt(poisonSize);
            while (!poisoned.add(indexToBePoisoned)) {//was that data point poisoned before?
                indexToBePoisoned = random.nextInt(poisonSize);
            }
            // we will poison the data point at the indexToBePoisoned
            DataPoint poisonedDataPoint = poisonedDataPoints.get(indexToBePoisoned);
            double label = poisonedDataPoint.getLabel();
            if (label == DataPoint.POSITIVE_LABEL) {// change the label from + to -
                poisonedDataPoint.setLabel(DataPoint.NEGATIVE_LABEL);
                pos2neg++;
            } else {
                poisonedDataPoint.setLabel(DataPoint.POSITIVE_LABEL);
                neg2pos++;
            }
        }
        Dataset pdataset = new Dataset(poisonedDataPoints);
        pdataset.setFeatureParents(dataset.getFeatureMap());
        pdataset.setFeatureNames(dataset.getFeatureNames());
        return pdataset;
    }

}
