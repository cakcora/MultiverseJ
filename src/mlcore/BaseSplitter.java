package mlcore;

import java.util.List;

import core.DataPoint;
import core.MLContants;

/***
 * Base Class for split operation. Contains shared methods and parameters for
 * different split types.
 * 
 * @author Murat Ali Bayir.
 *
 */
public abstract class BaseSplitter {

	/**
	 * The minimum input population size for split. If input data has less than
	 * minimumPopulation items, then we don't split.
	 */
	protected int minimumPopulation;

	/**
	 * Used for computing entropy for a given data set.
	 */
	protected Entropy entropyComputer;

	public BaseSplitter(int minimumPopulation) {
		this.minimumPopulation = minimumPopulation;
		this.entropyComputer = new Entropy();
	}

	public BaseSplitter() {
		this.entropyComputer = new Entropy();
	}

	/**
	 * Finds the best split for a given feature on the given data set.
	 * 
	 * @param featureIndex the index of the current feature this function operating on.
	 * @param dataSet is the input dataset.
	 * @param startIndex the start index of the sub list that is considered.
	 * @param endIndex the end index of the sub list that is considered.
	 */
	public abstract Split findBestSplit(int featureIndex, List<DataPoint> dataSet, int startIndex, int endIndex);

	/**
	 * Check if given data is homogeneous. This function returns true if all feature
	 * values or all labels are equivalent in Epsilon proximity.
	 * 
	 * @param featureIndex the index of the current feature this function operating on.
	 * @param dataSet is the input dataset.
	 * @param startIndex the start index of the sub list that is considered.
	 * @param endIndex the end index of the sub list that is considered.
	 * @return
	 */
	protected boolean isHomogenous(int featureIndex, List<DataPoint> dataSet, int startIndex, int endIndex) {
		int size = endIndex - startIndex + 1;
		if (dataSet == null || size < 1) {
			return true;
		}
		double initialFeatureValue = dataSet.get(startIndex).getFeature(featureIndex);
		double initialLabel = dataSet.get(startIndex).getLabel();

		boolean isHomogenousFeatures = true;
		boolean isHomogenousLabels = true;
		boolean isHomogenousSoFar = isHomogenousFeatures || isHomogenousLabels;

		for (int i = startIndex; i <= endIndex; i++) {
			var currentPoint = dataSet.get(i);
			isHomogenousFeatures = isHomogenousFeatures
					&& (Math.abs(currentPoint.getFeature(featureIndex) - initialFeatureValue) < MLContants.EPSILON);
			isHomogenousLabels = isHomogenousLabels
					&& (Math.abs(currentPoint.getLabel() - initialLabel) < MLContants.EPSILON);
			isHomogenousSoFar = isHomogenousFeatures || isHomogenousLabels;
			if (!isHomogenousSoFar) {
				return false;
			}
		}
		return true;
	}

	public int getMinimumPopulation() {
		return minimumPopulation;
	}

	public void setMinimumPopulation(int minimumPopulation) {
		this.minimumPopulation = minimumPopulation;
	}
}
