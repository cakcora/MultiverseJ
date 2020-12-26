package metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Contains functionality to compute area under ROC curve.
 * AUC stands for Area Under the Curve, the curve here refers to ROC curve.
 * https://developers.google.com/machine-learning/crash-course/classification/roc-and-auc
 * 
 * @author Murat Ali Bayir.
 */
public class MetricComputer {

	public MetricComputer() {}
	
	
	private void updateBin(Hashtable<Integer, BinData> table, int binId, boolean isPositive)
	{
		var data = table.get(binId);
		var currenValue = isPositive ? data.getNumberOfPositiveLabels()
				: data.getNumberOfNegativeLabels();
		if (isPositive)
		{
			data.setNumberOfPositiveLabels(currenValue + 1);
		} else {
			data.setNumberOfNegativeLabels(currenValue + 1);
		}
	}
	
	private List<BinData> computeBins(List<SingleEval> evaluationData)
	{
		var tabularBins = new Hashtable<Integer, BinData>();
		for (SingleEval data : evaluationData)
		{
			if (!tabularBins.containsKey(data.getBinId()))
			{
				tabularBins.put(data.getBinId(), new BinData(data.getBinId(), 0, 0));
			}
			updateBin(tabularBins, data.getBinId(), data.IsPositive());
		}
		
		var result = new ArrayList<BinData>();
		for (int i = 0; i <= 1000; i++)
		{
			if (tabularBins.containsKey(i))
			{
				result.add(tabularBins.get(i));
			}
		}
		return result;
	}
	
	/**
	 * Compute AUC for a given data.
	 * 
	 * @return computed AUC.
	 */
	public double computeAUC(List<SingleEval> evaluationData) {
		
		Collections.sort(evaluationData);
		var sortedBins = computeBins(evaluationData);
		int totalPositive = 0;
		int totalNegative = 0;
		// Compute cumulative distribution numbers.
		for (int i = sortedBins.size() - 1; i >=0; i--) {
			var bin = sortedBins.get(i);
			totalNegative += bin.getNumberOfNegativeLabels();
			totalPositive += bin.getNumberOfPositiveLabels();
			bin.setNumberOfNegativeLabels(totalNegative);
			bin.setNumberOfPositiveLabels(totalPositive);
		}
		
		// Apply trapezoid rule to approximate integral operation
		// in order to compute area under the curve.
		double result = 0.0d;
		for (int i = 0; i < sortedBins.size() - 1; i++) {
			var current = sortedBins.get(i);
			var next = sortedBins.get(i + 1);
			var tprCurrent = current.getTruePositiveRatio(totalPositive);
			var fprCurrent = current.getFalsePositiveRatio(totalNegative);
			var tprNext = next.getTruePositiveRatio(totalPositive);
			var fprNext = next.getFalsePositiveRatio(totalNegative);
			// Compute the area under the trapezoid.
			result += 0.5d * (tprCurrent + tprNext) * (fprNext - fprCurrent);
		}
		return result;
	}
	
	/**
	 * Compute Bias for a given data set that contains predicted and actual label.
	 * 
	 * @param evaluationData is the input data that contains predicted and actual label.
	 * @return computed log loss.
	 */
	public double computeLogLoss(List<SingleEval> evaluationData) {
		double logLoss = 0.0d;
		
		for (var data : evaluationData) {
			if (data.IsPositive()) {
				logLoss += Math.log(data.getPredicted());
			} else {
				logLoss += Math.log(1.0d - data.getPredicted());
			}
		}
		
		logLoss = (-1) * logLoss / (1.0d * evaluationData.size());
		return logLoss;
	}
	
	/**
	 * Compute Bias for a given data set that contains predicted and actual label.
	 * 
	 * @param evaluationData is the input data that contains predicted and actual label.
	 * @return computed log loss.
	 */
	public double computeBias(List<SingleEval> evaluationData) {
		double totalTruePositive = 0.0d;
		double totalPredictedPositive = 0.0d;
		
		for (var data : evaluationData) {
			if (data.IsPositive()) {
				totalTruePositive += 1.0d;
			}
			totalPredictedPositive += data.getPredicted();
		}
		return Math.abs(totalPredictedPositive - totalTruePositive) / totalTruePositive;
	}
	
	
	class BinData {
		/**
		 * The number of positive labels in this bin.
		 */
		private int numberOfPositiveLabels;
		
		/**
		 * The number of negative labels in this bin.
		 */
		private int numberOfNegativeLabels;
		
		
		/**
		 * The bin number for AUC between [0, 999]
		 */
		private int binId;
		
		public BinData(int binId, int numberOfPositiveLabels, int numberOfNegativeLabels)
		{
			this.binId = binId;
			this.numberOfPositiveLabels = numberOfPositiveLabels;
			this.numberOfNegativeLabels = numberOfNegativeLabels;
		}

		public int getNumberOfPositiveLabels() {
			return numberOfPositiveLabels;
		}

		public void setNumberOfPositiveLabels(int numberOfPositiveLabels) {
			this.numberOfPositiveLabels = numberOfPositiveLabels;
		}

		public int getNumberOfNegativeLabels() {
			return numberOfNegativeLabels;
		}

		public void setNumberOfNegativeLabels(int numberOfNegativeLabels) {
			this.numberOfNegativeLabels = numberOfNegativeLabels;
		}

		public int getBinId() {
			return binId;
		}

		public void setBinId(int binId) {
			this.binId = binId;
		}
		
		public double getTruePositiveRatio(int totalPositiveLabels)
		{
			// Every point that is included in current bin or bigger bins counted as positive.
			// number of negative labels are equal to false positives
			// TPR = (NumberOfPositiveLabels in current Bin and right side) / (TotalPositiveLabels).
			// TotalPositiveLabels = TotalTruePositiveLabels + TotalFalseNegativeLabels.
			return (this.numberOfPositiveLabels * 1.0d) / (totalPositiveLabels * 1.0d);
		}
		
		public double getFalsePositiveRatio(int totalNegativeLabels)
		{
			// Every point that is included in current bin or bigger bins counted as positive.
			// The number of negative labels are equal to false positives.
			// FPR = (NumberOfNegativeLabels in current bin and right side) / (TotalNegativeLabels).
			// TotalNegativeLabels = TotalFalsePositiveLabels + TotalTrueNegativeLabels.
			return (this.numberOfNegativeLabels * 1.0d) / (totalNegativeLabels * 1.0d);
		}
	}
}

