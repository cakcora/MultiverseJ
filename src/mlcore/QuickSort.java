package mlcore;

import java.util.List;

import core.DataPoint;

public class QuickSort {
	
	public QuickSort() {}
	
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
		int size = endIndex - startIndex + 1;
		// Check for base conditions.
		if (size > 0) {
			// If range is very small where sub array only 1 element.
			if (startIndex == endIndex) {
				return startIndex;
			}
			else {
				// Typical case where array list is not empty and have enough data
				// between startIndex and endIndex.
				int pivotOriginalIndex = findPivotIndex(data, pivot, featureIndex, startIndex, endIndex);
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
	
	private int quickSortPartition(List<DataPoint> data, int startIndex, int endIndex, int featureIndex) {
		// We can have better algorithm to select initial pivot.
		double pivot = data.get(endIndex).getFeature(featureIndex);
		return partition(data, startIndex, endIndex, pivot, featureIndex);
	}
	
	public void sort(List<DataPoint> data, int startIndex, int endIndex, int featureIndex) {
		if (endIndex > startIndex) {
			int pivotIndex = quickSortPartition(data, startIndex, endIndex, featureIndex);
			sort(data, startIndex, pivotIndex - 1, featureIndex);
			sort(data, pivotIndex + 1, endIndex, featureIndex);
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
	 * Finds the index of pivot among data which is list of data points.
	 * 
	 * @param data the input list of data point.
	 * @param pivot the specific value that we're looking for.
	 * @param featureIndex the index of current feature (the dimension) that we're considering
	 *        for this function.       
	 * @param startIndex the start index of the sub list that is considered.
	 * @param endIndex the end index of the sub list that is considered.
	 * @return the index of the pivot inside the list.
	 */
	private int findPivotIndex(List<DataPoint> data, double pivot, int featureIndex, int startIndex, int endIndex) {
		int pivotIndex = 0;
		for (int index = startIndex; index <= endIndex; index++) {
			if (data.get(index).getFeature(featureIndex) == pivot) {
				pivotIndex = index;
			}
		}
		return pivotIndex;
	}

}
