package mlcore;

import java.util.List;

import core.DataPoint;

/**
 * Implementation of Continuous Feature Splitter. This class has method to split a vector of data
 * points that has continuous feature into two bucket that maximizes the information gain.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class ContinuousFeatureSplitter extends BaseSplitter {
	
	public ContinuousFeatureSplitter(int minimumPopulation)
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
		
		// TODO(Murat): Murat will implement this part.
		return null;
	}

}
