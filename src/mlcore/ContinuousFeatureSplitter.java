package mlcore;

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

	/**
	 * Sorting utility for current learner.
	 */
	private QuickSort quickSorter;

	public ContinuousFeatureSplitter(int minimumPopulation) {
		super(minimumPopulation);
		quickSorter = new QuickSort();
	}

	private LabelCount getLabelStats(List<DataPoint> data, int startIndex, int endIndex) {
		int numberOfPositiveSamples = 0;
		int numberOfNegativeSamples = 0;
		for (int index = startIndex; startIndex <= endIndex; startIndex ++) {
			if (data.get(startIndex).IsPositive()) {
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
	public Split findBestSplit(int featureIndex, List<DataPoint> dataSet, int startIndex, int endIndex) {
		int size = endIndex - startIndex + 1;
		if (size < 2 * minimumPopulation) {
			return null;
		}

		if (isHomogenous(featureIndex, dataSet, startIndex, endIndex)) {
			return null;
		}
		
		quickSorter.sort(dataSet, startIndex, endIndex, featureIndex);

		var labelStat = getLabelStats(dataSet, startIndex, endIndex);
		var initialEntropy = computeEntropy(labelStat);

		var currentLabels = new LabelCount(0, 0);
		double maxGain = MLContants.EPSILON;
		boolean isSplitFound = false;
		double pivot = Double.MAX_VALUE;
		double minEntroy = Double.MAX_VALUE;

		// We need to start with startIndex to make sure that
		// currentLabels are computed properly.
		for (int i = startIndex; i <= (endIndex - minimumPopulation); i++) {
			currentLabels.addLabel(dataSet.get(i));
			// We can only split if i >= (startIndex + minimumPopulation) since population
			// of left split should be more than size of minimumPopulation.
			if (i >= (startIndex + minimumPopulation)) {
				// Check the difference for neighbor positions.
				double diff = dataSet.get(i).getFeature(featureIndex) - dataSet.get(i + 1).getFeature(featureIndex);
				// We need to split at least one time minimum.
				if (i == (startIndex + minimumPopulation) || (Math.abs(diff) > MLContants.EPSILON)) {
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
