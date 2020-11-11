package mlcore;

import java.util.List;

import core.DataPoint;
import core.RandomForest;

public class DecisionTreeLearner {

	public DecisionTreeLearner() {
	}

	/**
	 * Move all dataPoints smaller than pivot to the left of the pivot, Move all
	 * dataPoints larger than pivot to the right of the pivot, Place pivot into the
	 * correct position.
	 * 
	 * @param data
	 * @param pivot
	 * @param featureIndex
	 * 
	 * @return the correct position of pivot inside the list.
	 */
	public int partition(List<DataPoint> data, double pivot, int featureIndex) {
		if (data.size() > 0) {
			int pivotIndex = -1;
			for (int index = 0; index < data.size(); index++) {
				if (data.get(index).getFeature(featureIndex) < pivot) {
					pivotIndex++;
					swap(data, pivotIndex, index);
				}
			}

			if (data.size() > 1) {
				swap(data, (pivotIndex + 1), (data.size() - 1));
			}

			return (pivotIndex + 1);
		} else {
			throw new IllegalArgumentException("List is empty: " + data);
		}
	}

	/**
	 * Swaps two data points that are located in left and right indexes.
	 * 
	 * @param data
	 * @param leftIndex
	 * @param rightIndex
	 */
	private void swap(List<DataPoint> data, int leftIndex, int rightIndex) {
		DataPoint temp = data.get(leftIndex);
		data.set(leftIndex, data.get(rightIndex));
		data.set(rightIndex, temp);
	}

	public RandomForest train(List<DataPoint> data) {
		// TODO(HuseyinCan).
		return null;
	}

}
