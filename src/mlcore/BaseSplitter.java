package mlcore;

import java.util.List;

import core.DataPoint;
import core.MLContants;

/***
 * Base Class for split operation. Contains shared methods and parameters
 * for different split types.
 * 
 * @author Murat Ali Bayir.
 *
 */
public abstract class BaseSplitter {

	/**
	 * The minimum size of any set 'after split'.
	 * Example: If minimum split size is 40 and total population is 90.
	 * 			Split-1) 42 + 48 is valid split
	 * 			Split-2) 35 + 65 is invalid split.
	 */
	protected int minimumSplitSize;
	
	/**
	 * The minimum input population size for split.
	 * If input data has less than minimumPopulation items, then
	 * we don't split.
	 */
	protected int minimumPopulation;
	
	public BaseSplitter(int minimumSplitSize, int minimumPopulation)
	{
		this.minimumSplitSize = minimumSplitSize;
		this.minimumPopulation = minimumPopulation;
	}
	
	public BaseSplitter()
	{
		
	}
	

	/**
	 * Finds the best split for a given feature on the given data set.
	 * 
	 * @param featureIndex is the id of the current feature. It is used for accessing feature
	 * 		values in the data set.
	 * @param dataSet is the input data.
	 */
    public abstract void FindBestSplit(int featureIndex, List<DataPoint> dataSet);
    

    /**
     * Check if given data is homogeneous. This function returns true if all feature values or 
     * all labels are equivalent in Epsilon proximity.
     * 
     * @param featureIndex the index of the current feature this function operating on.
     * @param dataSet is the input dataset.
     * @return
     */
    protected boolean isHomogenous(int featureIndex, List<DataPoint> dataSet)
    {
    	if (dataSet == null || dataSet.size() < 1)
    	{
    		return true;    		
    	}
    	double initialFeatureValue = dataSet.get(0).getFeature(featureIndex);
    	double initialLabel = dataSet.get(0).getLabel();
    	
    	boolean isHomogenousFeatures = true;
    	boolean isHomogenousLabels = true;
    	boolean isHomogenousSoFar = isHomogenousFeatures || isHomogenousLabels;
    	
    	for (int i = 1; i < dataSet.size(); i++) {
    		var currentPoint = dataSet.get(i);
    		isHomogenousFeatures = isHomogenousFeatures && 
    				(Math.abs(currentPoint.getFeature(featureIndex) - initialFeatureValue) < MLContants.EPSILON);
    		isHomogenousLabels = isHomogenousLabels && 
    				(Math.abs(currentPoint.getLabel() - initialLabel) < MLContants.EPSILON);
    		isHomogenousSoFar = isHomogenousFeatures || isHomogenousLabels;
    		if (!isHomogenousSoFar)
    		{
    			return false;
    		}
    	}
    	return true;
    }

	public int getMinimumSplitSize() {
		return minimumSplitSize;
	}


	public void setMinimumSplitSize(int minimumSplitSize) {
		this.minimumSplitSize = minimumSplitSize;
	}


	public int getMinimumPopulation() {
		return minimumPopulation;
	}


	public void setMinimumPopulation(int minimumPopulation) {
		this.minimumPopulation = minimumPopulation;
	}
}
