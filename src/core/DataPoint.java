package core;

/**
 * Represents Single Data Point in source data.
 * 
 * @author Murat Ali Bayir
 *
 */
public class DataPoint {
	
	private double[] features;
	
	private double label;
	
	public DataPoint(double[] features, double label)
	{
		this.features = features;
		this.label = label;
	}

	public DataPoint(double[] features)
	{
		this.features = features;
	}

	public double[] getFeatures() {
		return features;
	}

	public void setFeatures(double[] features) {
		this.features = features;
	}

	public double getLabel() {
		return label;
	}

	public void setLabel(double label) {
		this.label = label;
	}
}
