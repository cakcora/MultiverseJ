package core;

import java.util.Arrays;

/**
 * Represents Single Data Point in source data.
 * 
 * @author Murat Ali Bayir
 *
 */
public class DataPoint {

	/**
	 * Constant value of the positive label.
     */
    public static final double POSITIVE_LABEL = 1.0d;

    /**
     * Constant value of the negative label.
     */
    public static final double NEGATIVE_LABEL = 0.0d;

    private double[] features;
    private double label;
    private boolean[] isCategorical;

    public DataPoint(double[] features, double label) {
        this.features = features;
        this.label = label;
    }


    public DataPoint(double[] features) {
        this.features = features;
    }

	public DataPoint() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(features);
		result = prime * result + Arrays.hashCode(isCategorical);
		long temp;
		temp = Double.doubleToLongBits(label);
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
		DataPoint other = (DataPoint) obj;
		if (!Arrays.equals(features, other.features))
			return false;
		if (!Arrays.equals(isCategorical, other.isCategorical))
			return false;
		if (Double.doubleToLongBits(label) != Double.doubleToLongBits(other.label))
			return false;
		return true;
	}


	/**
	 * Returns true if current data point has positive label. Otherwise, returns
	 * false.
	 */
	public boolean IsPositive() {
		return (Math.abs(this.label - DataPoint.POSITIVE_LABEL) < MLContants.EPSILON);
	}

	/**
	 * Returns true if current data point has negative label. Otherwise, returns
	 * false.
	 */
	public boolean IsNegative() {
		return (Math.abs(this.label - DataPoint.NEGATIVE_LABEL) < MLContants.EPSILON);
	}

	/**
	 * Returns corresponding feature value on index @featureIndex.
	 *
	 * @param featureIndex is the index of corresponding feature in the sample data.
	 */
	public double getFeature(int featureIndex) {
		if (featureIndex < 0 || featureIndex >= this.features.length) {
			throw new IllegalArgumentException("Feature Id: " + featureIndex + " is out of range!");
		}
		return this.features[featureIndex];
	}

	@Override
	public String toString() {
		return "DataPoint {" + "features= " + Arrays.toString(features) + ", label= " + label + '}';
	}

	public void setFeatureTypes(boolean[] isCategorical) {
		this.isCategorical = isCategorical;
	}
	
	public boolean isCategorical(int featureIndex)
	{
		return this.isCategorical[featureIndex];
	}

	public boolean[] getCategorical() {
		return isCategorical;
	}


}
