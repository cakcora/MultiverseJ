package mlcore;

/**
 * Contains Simple arithmetic utility for computing Entropy.
 * 
 * @author Murat Ali Bayir.
 *
 */
public class Entropy {
	
	private static double Epsilon = Math.pow(10, -6);
	
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
		if ((Math.abs(probability) < Epsilon) || (Math.abs(1.0d -probability) < Epsilon))
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
		double total = (numberOfNegativeSamples + numberOfNegativeSamples) * 1.0d;
		return getLogLoss((numberOfPositiveSamples * 1.0d) / total) +
				getLogLoss((numberOfNegativeSamples * 1.0d) / total);
	}
}
