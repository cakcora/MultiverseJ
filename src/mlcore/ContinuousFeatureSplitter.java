package mlcore;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.DataPoint;
import core.MLContants;

/**
 * Implementation of Continuous Feature Splitter. This class has method to split
 * a vector of data points that has continuous feature into two bucket that
 * maximizes the information gain.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class ContinuousFeatureSplitter extends BaseSplitter {

	public ContinuousFeatureSplitter(int minimumPopulation) {
		super(minimumPopulation);
	}

	/**
	 * Returns comparator for DataPoint objects based on selected feature.
	 * 
	 * @param featureIndex the feature index to be sorted.
	 * @return comparator to sort collection of data points based on input feature.
	 */
	private Comparator<DataPoint> getComparator(int featureIndex) {
		return new Comparator<DataPoint>() {

			public int compare(DataPoint left, DataPoint right) {
				return Double.compare(left.getFeature(featureIndex), right.getFeature(featureIndex));

			}
		};
	}

	private LabelCount getLabelStats(List<DataPoint> data) {
		int numberOfPositiveSamples = 0;
		int numberOfNegativeSamples = 0;
		for (DataPoint point : data) {
			if (point.IsPositive()) {
				numberOfPositiveSamples++;
			} else {
				numberOfNegativeSamples++;
			}
		}
		return new LabelCount(numberOfPositiveSamples, numberOfNegativeSamples);
	}

	private double computeEntropy(LabelCount labelStat) {
		return entropyComputer.computeEntropy(labelStat.getNumberOfPositiveSamples(),
				labelStat.getNumberOfNegativeSamples());
	}

	private double getWeightedEntropy(LabelCount left, LabelCount right) {
		double totalSum = (left.getTotal() + right.getTotal()) * 1.0d;
		if (totalSum < MLContants.EPSILON) {
			throw new IllegalArgumentException("Sum of label stat can not be zero or lower!");
		}
		double leftWeight = (left.getTotal() * 1.0d) / totalSum;
		double rightWeight = (right.getTotal() * 1.0d) / totalSum;

		return (leftWeight * computeEntropy(left)) + (rightWeight * computeEntropy(right));
	}

	@Override
	public Split findBestSplit(int featureIndex, List<DataPoint> dataSet) {
		if (dataSet.size() < 2 * minimumPopulation) {
			return null;
		}

		if (isHomogenous(featureIndex, dataSet)) {
			return null;
		}

		Collections.sort(dataSet, getComparator(featureIndex));

		var labelStat = getLabelStats(dataSet);
		var initialEntropy = computeEntropy(labelStat);

		var currentLabels = new LabelCount(0, 0);
		double maxGain = MLContants.EPSILON;
		boolean isSplitFound = false;
		double pivot = Double.MAX_VALUE;
		double minEntroy = Double.MAX_VALUE;

		for (int i = 0; i < (dataSet.size() - minimumPopulation); i++) {
			currentLabels.addLabel(dataSet.get(i));
			if (i >= minimumPopulation) {
				double diff = dataSet.get(i).getFeature(featureIndex) - dataSet.get(i + 1).getFeature(featureIndex);
				// We need to split at least one time minimum.
				if (i == minimumPopulation || (Math.abs(diff) > MLContants.EPSILON)) {
					double newEntropy = getWeightedEntropy(currentLabels, labelStat.subtract(currentLabels));
					if ((initialEntropy - newEntropy) > maxGain) {
						maxGain = initialEntropy - newEntropy;
						pivot = dataSet.get(i).getFeature(featureIndex);
						minEntroy = newEntropy;
						isSplitFound = true;
					}
				}
			}
		}

		if (isSplitFound) {
			Split result = new Split(featureIndex);
			result.setEntropyBeforeSplit(initialEntropy);
			result.setEntropyAfterSplit(minEntroy);
			result.setPivot(pivot);
			return result;
		} else {
			return null;
		}
	}
}
