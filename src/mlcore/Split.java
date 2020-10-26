package mlcore;

/**
 * Stores information of split operation in decision tree.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class Split {
	
	/**
	 * Feature id for current split. Each split only operates on one feature.
	 */
	private int featureId;
	
	/**
	 * Pivot value for split. All points smaller than pivot will go to left bucket after split,
	 * all points bigger than split will go to right bucket.
	 */
	private double pivot;
	
	/**
	 * Entropy of data before split.
	 */
	private double entropyBeforeSplit;
	
	/**
	 * Entropy of the data after split. This is equivalent to sum of entropies of both right
	 * and left bucket after split.
	 */
	private double entropyAfterSplit;
	
	
	public Split(int featureId) 
	{
		this.featureId = featureId;
	}
	
	
	/**
	 * Returns information gain for current split.
	 */
	public double getInformationGain()
	{
		return (entropyBeforeSplit - entropyAfterSplit);
	}


	public double getPivot() {
		return pivot;
	}


	public void setPivot(double pivot) {
		this.pivot = pivot;
	}


	public double getEntropyBeforeSplit() {
		return entropyBeforeSplit;
	}


	public void setEntropyBeforeSplit(double entropyBeforeSplit) {
		this.entropyBeforeSplit = entropyBeforeSplit;
	}


	public double getEntropyAfterSplit() {
		return entropyAfterSplit;
	}


	public void setEntropyAfterSplit(double entropyAfterSplit) {
		this.entropyAfterSplit = entropyAfterSplit;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(entropyAfterSplit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(entropyBeforeSplit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + featureId;
		temp = Double.doubleToLongBits(pivot);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Split other = (Split) obj;
		if (Double.doubleToLongBits(entropyAfterSplit) != Double.doubleToLongBits(other.entropyAfterSplit))
			return false;
		if (Double.doubleToLongBits(entropyBeforeSplit) != Double.doubleToLongBits(other.entropyBeforeSplit))
			return false;
		if (featureId != other.featureId)
			return false;
		if (Double.doubleToLongBits(pivot) != Double.doubleToLongBits(other.pivot))
			return false;
		return true;
	}
}
