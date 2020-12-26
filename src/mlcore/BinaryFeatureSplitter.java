package mlcore;

import java.util.Hashtable;
import java.util.List;

import core.DataPoint;
import core.MLContants;

/**
 * Implementation of Binary Feature splitter. This class has method to split a
 * vector of data points that has binary feature into two bucket that maximizes
 * the information gain.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class BinaryFeatureSplitter extends BaseSplitter {

	private enum CountTableKey {
		ON_POSITIVE, ON_NEGATIVE, OFF_POSITIVE, OFF_NEGATIVE;
	}

	public BinaryFeatureSplitter(int minimumPopulation) {
		super(minimumPopulation);
	}

	private CountTableKey getKey(DataPoint point, int featureIndex) {
		var feature = Math.abs(point.getFeature(featureIndex) - 1.0d);
		var label = Math.abs(point.getLabel() - 1.0d);

		if (feature < MLContants.EPSILON) { // On Case.
			if (label < MLContants.EPSILON) {
				return CountTableKey.ON_POSITIVE;
			} else {
				return CountTableKey.ON_NEGATIVE;
			}
		} else { // Off Case.
			if (label < MLContants.EPSILON) {
				return CountTableKey.OFF_POSITIVE;
			} else {
				return CountTableKey.OFF_NEGATIVE;
			}
		}
	}

	private Hashtable<CountTableKey, Integer> getCountTable() {
		var countTable = new Hashtable<CountTableKey, Integer>();
		countTable.put(CountTableKey.ON_POSITIVE, 0);
		countTable.put(CountTableKey.ON_NEGATIVE, 0);
		countTable.put(CountTableKey.OFF_POSITIVE, 0);
		countTable.put(CountTableKey.OFF_NEGATIVE, 0);
		return countTable;
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

		var countTable = getCountTable();

		for (int i = startIndex; i <= endIndex; i++) {
			var currentPoint = dataSet.get(i);
			var key = getKey(currentPoint, featureIndex);
			countTable.put(key, countTable.get(key) + 1);
		}

		var originalEntropy = entropyComputer.computeEntropy(
				countTable.get(CountTableKey.ON_POSITIVE) + countTable.get(CountTableKey.OFF_POSITIVE),
				countTable.get(CountTableKey.ON_NEGATIVE) + countTable.get(CountTableKey.OFF_NEGATIVE));

		// Compute the ratio of the 'ON' features.
		var onRatio = 1.0d * (countTable.get(CountTableKey.ON_POSITIVE) + countTable.get(CountTableKey.ON_NEGATIVE));
		onRatio = onRatio / (dataSet.size() * 1.0d);
		// Compute the entropy of data set that has 'ON' Features by using positive and negative labels.
		var onEntropy = entropyComputer.computeEntropy(countTable.get(CountTableKey.ON_POSITIVE),
				countTable.get(CountTableKey.ON_NEGATIVE));

		// Compute the ratio of the 'OFF' features.
		var offRatio = 1.0d * (countTable.get(CountTableKey.OFF_POSITIVE) + countTable.get(CountTableKey.OFF_NEGATIVE));
		offRatio = offRatio / (dataSet.size() * 1.0d);
		// Compute the entropy of data set that has 'OFF' Features by using positive and negative labels.
		var offEntropy = entropyComputer.computeEntropy(countTable.get(CountTableKey.OFF_POSITIVE),
				countTable.get(CountTableKey.OFF_NEGATIVE));

		// Compute the new Entropy.
		var newEntropy = (onRatio * onEntropy) + (offRatio * offEntropy);

		var result = new Split(featureIndex);
		result.setEntropyAfterSplit(newEntropy);
		result.setEntropyBeforeSplit(originalEntropy);
		// For binary features (on vs off).
		// We always treat 0.0d is smaller value that represent off value.
		result.setPivot(0.0d);
		return result;
	}

}
