package mlcore;

import java.util.Hashtable;
import java.util.List;

import core.DataPoint;
import core.MLContants;

/**
 * Implementation of Binary Feature splitter. This class has method to split a vector of data
 * points that has binary feature into two bucket that maximizes the information gain.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class BinaryFeatureSplitter extends BaseSplitter {

	
	public BinaryFeatureSplitter(int minimumPopulation)
	{
		super(minimumPopulation);
	}

	@Override
	public Split FindBestSplit(int featureIndex, List<DataPoint> dataSet) {
		
		if (dataSet.size() < 2 * minimumPopulation)
		{
			return null;
		}

		if (isHomogenous(featureIndex, dataSet)) {
			return null;
		}
		
		var countTable = new Hashtable<String, Integer>();
		countTable.put("On-Positive", 0);
		countTable.put("On-Negative", 0);
		countTable.put("Off-Positive", 0);
		countTable.put("Off-Negative", 0);
		
		for (int i = 0; i < dataSet.size(); i++)
		{
			var currentPoint = dataSet.get(i);
			var feature = Math.abs(currentPoint.getFeature(featureIndex) - 1.0d)
					< MLContants.EPSILON ? "On-" : "Off";
			var label = Math.abs(currentPoint.getLabel() - 1.0d)
				< MLContants.EPSILON ? "Positive" : "Negative";
			var key = feature + label;
			countTable.put(key, countTable.get(key) + 1);
		}
		
		var originalEntropy = entropyComputer.computeEntropy(
				countTable.get("On-Positive") + countTable.get("Off-Positive"),
				countTable.get("On-Negative") + countTable.get("Off-Negative"));
		
		var onRatio = 1.0d * (countTable.get("On-Positive") + countTable.get("OnNegative"));
		onRatio = onRatio / (dataSet.size() * 1.0d);
		var offRatio = 1.0d * (countTable.get("Off-Positive") + countTable.get("OffNegative"));
		offRatio = offRatio / (dataSet.size() * 1.0d);
		
		var newEntropy = entropyComputer.computeEntropy();
		
		
		return null;
	}
	
}
