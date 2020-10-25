package mlcore;

import java.util.ArrayList;

import core.DataPoint;

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
	

	/**
	 * Finds the best split for a given feature on the given data set.
	 * 
	 * @param featureId is the id of the current feature. It is used for accessing feature
	 * 		values in the data set.
	 * @param dataSet is the input data.
	 */
    public abstract void FindBestSplit(int featureId, ArrayList<DataPoint> dataSet);
}
