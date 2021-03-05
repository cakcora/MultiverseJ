package poisoner;

import Utils.Utils;
import core.DataPoint;
import core.Dataset;

import java.util.*;

public class LabelFlippingPoisoner {
    private Random random;
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
        List<DataPoint> dataPoints =  dataset.getDatapoints();
        int poisonSize = (int) Math.ceil(dataPoints.size() * poisonLevel/100.0);
        ArrayList<DataPoint> poisonedDataPoints = new ArrayList<>();
        //create a new dataset to be poisoned
        for (DataPoint dataPoint : dataPoints) {
            //Add the object clones
            poisonedDataPoints.add(dataPoint.clone());
        }

        Set<Integer> poisoned = new HashSet<>();
        for(int i=0;i<poisonSize;i++){
            int indexToBePosioned=random.nextInt(poisonSize);
            while(!poisoned.add(indexToBePosioned)){//was that data point poisoned before?
                indexToBePosioned=random.nextInt(poisonSize);
            }
            // we will poison the data point at the indexToBePosioned
            DataPoint poisonedDataPoint = poisonedDataPoints.get(indexToBePosioned);
            double label = poisonedDataPoint.getLabel();
            if(label==DataPoint.POSITIVE_LABEL){// change the label from + to -
                poisonedDataPoint.setLabel(DataPoint.NEGATIVE_LABEL);
            }
            else poisonedDataPoint.setLabel(DataPoint.POSITIVE_LABEL);
        }
        Dataset pdataset = new Dataset(poisonedDataPoints);
        pdataset.setFeatureParents(dataset.getFeatureMap());
        pdataset.setFeatureNames(dataset.getFeatureNames());
        return pdataset;
    }

}
