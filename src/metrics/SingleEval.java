package metrics;

import core.DataPoint;
import core.MLContants;

/**
 * Contains single evaluation data with actual and predicted labels.
 * 
 * @author Murat Ali Bayir
 *
 */
public class SingleEval implements Comparable<SingleEval>{
	/**
	 * Predicted value for binary classification problem.
	 */
	private double predicted;
	
	/**
	 * Actual value for binary classification problem.
	 */
	private double actual;
	
	
	/**
	 * The bin number for AUC between [0, 999]
	 */
	private int binId;
	
	public SingleEval(double predicted, double actual)
	{
		this.predicted = predicted;
		this.actual = actual;
		this.binId = (int)(predicted * 1000.0d);
	}

	public double getPredicted() {
		return predicted;
	}

	public double getActual() {
		return actual;
	}
	
	public int getBinId() {
		return binId;
	}

	/**
	 * Ascending Order.
	 */
	@Override
	public int compareTo(SingleEval that) {
		if (Math.abs(this.predicted - that.getPredicted()) < MLContants.PRECISE_EPSILON)
		{
			return 0;
		}
		if (this.predicted < that.getPredicted())
		{
			return -1;
		} else {
			return 1;
		}
	}
	
	@Override
	public String toString()
	{
		return String.format("Predicted: %f, Actual: %f, Bin: %d",
				predicted, actual, binId);
	}
	
	/**
	 * Returns true if current data point has positive label. Otherwise, returns
	 * false.
	 */
	public boolean IsPositive() {
		return (Math.abs(this.actual - DataPoint.POSITIVE_LABEL) < MLContants.EPSILON);
	}

	/**
	 * Returns true if current data point has negative label. Otherwise, returns
	 * false.
	 */
	public boolean IsNegative() {
		return (Math.abs(this.actual - DataPoint.NEGATIVE_LABEL) < MLContants.EPSILON);
	}


}
