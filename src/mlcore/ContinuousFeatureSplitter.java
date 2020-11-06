package mlcore;

import java.util.Collections;
import java.util.Comparator;
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
	
	
	/**
	 * Returns comparator for DataPoint objects based on selected feature.
	 * @param featureIndex
	 * @return
	 */
	private Comparator<DataPoint> getComparator(int featureIndex)
	{
		return new Comparator<DataPoint>() {

			public int compare(DataPoint left, DataPoint right) {
			   return Double.compare(left.getFeature(featureIndex),
					   right.getFeature(featureIndex));
				   
		    }};
	}

	@Override
	public Split findBestSplit(int featureIndex, List<DataPoint> dataSet) {
		if (dataSet.size() < 2 * minimumPopulation)
		{
			return null;
		}

		if (isHomogenous(featureIndex, dataSet)) {
			return null;
		}
		
		Collections.sort(dataSet, getComparator(featureIndex));
		// TODO(Murat): Murat will implement this part.
		return null;
	}

}
