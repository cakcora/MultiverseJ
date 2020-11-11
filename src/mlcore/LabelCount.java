package mlcore;

import core.DataPoint;

/**
 * Contains statistics about positive and negative labels.
 * 
 * @author Murat Ali Bayir
 *
 */
 public class LabelCount {
	
	private int numberOfPositiveSamples;
	
	private int numberOfNegativeSamples;
	
	
	public LabelCount(int numberOfPositiveLabels, int numberOfNegativeLabels)
	{
		this.numberOfPositiveSamples = numberOfPositiveLabels;
		this.numberOfNegativeSamples = numberOfNegativeLabels;
	}


	public int getNumberOfPositiveSamples() {
		return numberOfPositiveSamples;
	}


	public int getNumberOfNegativeSamples() {
		return numberOfNegativeSamples;
	}
	
	public int getTotal()
	{
		return numberOfPositiveSamples + numberOfNegativeSamples;
	}
	
	public void addLabel(DataPoint point)
	{
		if (point.IsPositive())
		{
			this.numberOfPositiveSamples++;
		} else {
			this.numberOfNegativeSamples++;
		}
	}
	
	/**
	 * Creates new LabelCount object by subtracting input label count.
	 * 
	 * @param other the input argument
	 * @return label count as diff.
	 */
	public LabelCount subtract(LabelCount other)
	{
		return new LabelCount(this.numberOfPositiveSamples - other.numberOfPositiveSamples,
		   this.numberOfNegativeSamples - other.numberOfNegativeSamples);
	}
}