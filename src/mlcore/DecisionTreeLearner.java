package mlcore;

import java.util.List;

import core.DataPoint;
import core.RandomForest;

/**
 * Learns single decision tree from a given set of DataPoints and feature set.
 * This class only learns tree for binary classification problem based on entropy.
 * 
 * @author Huseyincan Kaynak
 * @author Murat Ali Bayir.
 *
 */
public class DecisionTreeLearner {

	public DecisionTreeLearner() {
	}

	/**
	 * Moves all dataPoints smaller than pivot to the left of the pivot. Also, moves all
	 * dataPoints larger than pivot to the right of the pivot, Then places pivot into the
	 * correct position. This function only mutates the positions inside the list that
	 * lies between startIndex and endIndex namely the following sublist.
	 * [0... (startIndex) ... (endIdex) ... (data.size()-1)].
	 * 
	 * @param data the input list of data point.
	 * @param startIndex the start index of the sub list that is considered.
	 * @param endIndex the end index of the sub list that is considered.
	 * @param pivot the specific value that is used for partitioning data.
	 * @param featureIndex the index of current feature (the dimension) that we're considering
	 *        for this function. 
	 * @return the correct position of pivot inside the list.
	 */
	public int partition(List<DataPoint> data, int startIndex, int endIndex, double pivot, int featureIndex) {
		// Check for base conditions.
		if (data.size() > 0) {
			// If range is very small where sub array only 1 element.
			if (startIndex == endIndex) {
				return startIndex;
			}
			else {
				// Typical case where array list is not empty and have enough data
				// between startIndex and endIndex.
				int pivotOriginalIndex = findPivotIndex(data, pivot, featureIndex);
				swap(data, pivotOriginalIndex, endIndex);
				int pivotIndex = startIndex;
				for (int index = startIndex; index < endIndex; index++) {
					if (data.get(index).getFeature(featureIndex) <= pivot) {
						swap(data, pivotIndex, index);
						pivotIndex++;
					}
				}
				swap(data, pivotIndex, endIndex);
				
				return pivotIndex;
			}
		}
		else {
			throw new IllegalArgumentException("List is empty: " + data);
		}
	}

	/**
	 * Swaps two data points that are located in left and right indexes.
	 */
	private void swap(List<DataPoint> data, int leftIndex, int rightIndex) {
		DataPoint temp = data.get(leftIndex);
		data.set(leftIndex, data.get(rightIndex));
		data.set(rightIndex, temp);
	}
	
	/**
	 * 
	 * Finds the index of pivot among data which is list of data points.
	 * 
	 * @param data the input list of data point.
	 * @param pivot the specific value that we're looking for.
	 * @param featureIndex the index of current feature (the dimension) that we're considering
	 *        for this function.
	 * @return the index of the pivot inside the list.
	 */
	private int findPivotIndex(List<DataPoint> data, double pivot, int featureIndex) {
		int pivotIndex = 0;
		for (int index = 0; index < data.size(); index++) {
			if (data.get(index).getFeature(featureIndex) == pivot) {
				pivotIndex = index;
			}
		}
		return pivotIndex;
	}
	
	public RandomForest train(List<DataPoint> data)
	{
		// TODO(HuseyinCan).
		return null;
	}

}
