package mlcore;

import java.util.List;

import core.DataPoint;
import core.MLContants;

/**
 * Contains Simple arithmetic utility for computing Entropy.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class Entropy {
	
	public Entropy()
	{
	}
	
	/**
	 * Returns logarithmic loss for given probability which is: -p*log[p]
	 * 
	 * @param probability is the input probability.
	 */
	private double getLogLoss(double probability)
	{
		if ((Math.abs(probability) < MLContants.EPSILON) || (Math.abs(1.0d -probability) < MLContants.EPSILON))
		{
			return 0;
		} else {
			return (-1) * probability * Math.log(probability);
		}
	}
	
	/**
	 * Computes entropy for the given set of items that are specified by
	 * then number of positive and negative labels.
	 */
	public double computeEntropy(int numberOfPositiveSamples, int numberOfNegativeSamples)
	{
		double total = (numberOfPositiveSamples + numberOfNegativeSamples) * 1.0d;
		return total != 0 ? 
				getLogLoss((numberOfPositiveSamples * 1.0d) / total) +
				getLogLoss((numberOfNegativeSamples * 1.0d) / total)
				: 0.0;
	}
}
