package mlcore;

import java.util.List;

import core.DataPoint;

/**
 * Implementation of Binary Feature splitter. This class has method to split a vector of data
 * points that has binary feature into two bucket that maximizes the information gain.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class BinaryFeatureSplitter extends BaseSplitter {

	
	
	public BinaryFeatureSplitter(int minimumSplitSize, int minimumPopulation)
	{
		super(minimumSplitSize, minimumPopulation);
	}

	@Override
	public void FindBestSplit(int featureId, List<DataPoint> dataSet) {
		// TODO Auto-generated method stub
		
	}
	
}
