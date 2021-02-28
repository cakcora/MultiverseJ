package loaderTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import core.DataPoint;
import core.DecisionTree;
import core.MLContants;
import core.RandomForest;
import core.TreeNode;
import loader.CSVLoader;
import loader.LoaderOptions;
import metrics.MetricComputer;
import metrics.SingleEval;
import mlcore.DecisionTreeLearner;

class CSVLoaderTest {

	// @Test
	@DisplayName("Load Census Data")
	void loadCensusData() throws Exception {

		LoaderOptions options = new LoaderOptions();

		String csvFile = "census-income.data";
		int labelIndex = 41;
		options.featureIgnoreThreshold(20);
		options.setQuoter(' ');
		options.setSep(',');
		var csvLoader = new CSVLoader (options);
		List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile);
		String[] featureNames = csvLoader.getFeatureNames();
		String[] names = csvLoader.getFeatureNames();

		for (String message : csvLoader.getInformation()) {
			System.out.println(message);
		}
		System.out.println("Dataset has " + dataPoints.size() + " data points");
		// System.out.println("Each data point has " + featureNames.length + "
		// features:");
		// for (String feature : featureNames) {
		// System.out.print(" " + feature);
		// }
		System.out.println();
//		var learner = new DecisionTreeLearner(10, 250, new int[] { 0, 1, 2, 3 });
//		long startTime = System.nanoTime();
//		var tree = learner.train(dataPoints.subList(0, 5000));
//		System.out.println(tree.predict(dataPoints.get(12000).getFeatures()));
//		System.out.println(dataPoints.get(12000).getLabel());
//		long endTime = System.nanoTime();
//		long totalTime = endTime - startTime;
//		System.out.println(totalTime);

//		RandomForest rf = new RandomForest(dataPoints.subList(0, 1000));
//		rf.setNumTrees(500);
//		rf.setSampleSize(100);
//		rf.setNumFeatures(5);
////		for (String message : rf.getInfoMessages()) {
////			System.out.println(message);
////		}
//		// miraculously learn a decision tree
//		DecisionTree dt = new DecisionTree();
//		TreeNode node = new TreeNode(0, 1);
//		TreeNode nodeL = new TreeNode(1, 2);
//		TreeNode nodeR = new TreeNode(2, 3);
//		node.setLeftChild(1);
//		node.setRightChild(2);
//		dt.addNode(node);
//		dt.addNode(nodeL);
//		dt.addNode(nodeR);
//		dt.addNode(node);
		/*
		 * // extract a graph from the tree GraphExtractor extractor = new
		 * GraphExtractor(dt); DirectedSparseMultigraph<Integer, Integer> graph =
		 * extractor.getGraph();
		 * 
		 * GraphMetrics metric = new GraphMetrics(); metric.computeAllMetrices(graph);
		 * long[] counts = metric.getTriadicCounts(graph); for (int i = 0; i <
		 * counts.length; i++) { if (counts[i] > 0) System.out.println("Graph motif " +
		 * i + ": " + counts[i]); } double diameter = metric.getDiamater();
		 * System.out.println("Graph diameter is " + diameter); double medianDegree =
		 * metric.getMedianDegree(); System.out.println("Graph median degree is " +
		 * medianDegree);
		 * 
		 * double avgInDegree = metric.getAvgInDegree(); double avgOutDegree =
		 * metric.getAvgOutDegree(); System.out.println("avg in degree: " + avgInDegree
		 * + " avg out degree:" + avgOutDegree);
		 * 
		 * double avgClusCoeff = metric.getAvgClusteringCoeff(); double avgBetweenness =
		 * metric.getAvgBetweenness(); System.out.println("avg clustering coefficient: "
		 * + avgClusCoeff + " avg betweenness: " + avgBetweenness);
		 */
	}

	@Test
	@DisplayName("Load Census Data with 13 features")
	void loadCensusData_13() throws Exception {
		LoaderOptions options = new LoaderOptions();

		String csvFile = "adult.csv";
		int labelIndex = 14;
		options.featureIgnoreThreshold(20);
		options.setQuoter(' ');
		options.setSep(',');
		var csvLoader = new CSVLoader (options);
		List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile);
		String[] featureNames = csvLoader.getFeatureNames();
		String[] names = csvLoader.getFeatureNames();

		for (String message : csvLoader.getInformation()) {
			System.out.println(message);
		}
		System.out.println("Dataset has " + dataPoints.size() + " data points");
		ArrayList<Integer> featuresIndex = new ArrayList<Integer>();
		for (int i = 0; i < 66; i++) {
			featuresIndex.add(i);
		}
		var learner = new DecisionTreeLearner(10, 10, featuresIndex);
		long startTime = System.nanoTime();
		var tree = learner.train(dataPoints.subList(0, 10000));
		var testData = dataPoints.subList(20000, 21000);
		var metrics = new ArrayList<SingleEval>();
		var computer = new MetricComputer();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
			System.out.println("Predicted: " + predicted + "Actual: " + point.getLabel());
		}
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		// %100 accuracy and %0.0 Bias
		assertEquals(0.0d, computer.computeBias(metrics), MLContants.PRECISE_EPSILON);

		long endTime = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println(totalTime);
	}

}
