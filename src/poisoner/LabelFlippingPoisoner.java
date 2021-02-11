package poisoner;

import Utils.Utils;
import core.DataPoint;

import java.util.*;

public class LabelFlippingPoisoner {

    /**
     * A function to inject poison to the data set. the poison is untargeted label flipping.
     * @param dataPoints to be poisoned
     * @param poisonLevel a number between 0 -- 50. We will poison this percentage of data points
     * @return a newly created, poisoned data set
     */
    public List poison(List<DataPoint> dataPoints, int poisonLevel) {
        if(poisonLevel<0||poisonLevel>100){
            return null;
        }
        // compute how many data points we should poison
        int poisonSize = (int) Math.ceil(dataPoints.size() * poisonLevel/100.0);
        Random random = new Random();
        ArrayList<DataPoint> poisonedDataPoints = new ArrayList<>();
        //create a new dataset to be poisoned
        for (DataPoint dataPoint : dataPoints) {
            //Add the object clones
            poisonedDataPoints.add(Utils.clone(dataPoint));
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
        return poisonedDataPoints;
    }

}
