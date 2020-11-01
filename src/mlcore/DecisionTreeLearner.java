package mlcore;

import java.util.List;

import core.DataPoint;
import core.RandomForest;

public class DecisionTreeLearner {
	
	public DecisionTreeLearner() {}
	
	
	/**
	 * Move all dataPoints smaller than pivot to the right of the pivot,
	 * Move all dataPoints larger than pivot to the right of the pivot,
	 * Place pivot into the correct position.
	 * 
	 * @param data
	 * @param pivot
	 * @param featureIndex
	 * 
	 * @return the correct position of pivot inside the list.
	 */
	public int Partition(List<DataPoint> data, double pivot, int featureIndex)
	{
		// TODO(HuseyinCan).
		return -1;
	}
	
	public RandomForest train(List<DataPoint> data)
	{
		// TODO(HuseyinCan).
		return null;
	}

}
