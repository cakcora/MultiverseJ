import core.DataPoint;
import core.DecisionTree;
import core.RandomForest;
import core.TreeNode;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import graphcore.GraphExtractor;
import graphcore.GraphMetrics;
import loader.CSVLoader;
import loader.LoaderOptions;

import java.util.List;

public class PoisonedLabelExperiment {
	public static void main(String[] args) throws Exception {

		LoaderOptions options = new LoaderOptions();
		if (args.length < 3) {
			System.out.println("Expecting filename, index of the label column, separator char and quote char");
			System.exit(1);
		} else if (args.length == 4) {
			options.setQuoter(args[2].charAt(0));
			options.setSep(args[3].charAt(0));

		} else if (args.length > 4) {
			System.out.println(
					"Too many parameters: expecting filename, index of the label column, separator char and quote char");
			System.exit(2);
		}
		String csvFile = args[0];
		int labelIndex = Integer.parseInt(args[1]);
		options.featureIgnoreThreshold(20);

		var csvLoader = new CSVLoader(labelIndex, options);
		List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile);
		String[] featureNames = csvLoader.getFeatureNames();
		String[] names = csvLoader.getFeatureNames();

		for (String message : csvLoader.getInformation()) {
			System.out.println(message);
		}
		System.out.println("Dataset has " + dataPoints.size() + " data points");
		System.out.println("Each data point has " + featureNames.length + " features:");
		for (String feature : featureNames) {
			System.out.print(" " + feature);
		}
		System.out.println();

		RandomForest rf = new RandomForest(dataPoints);
		rf.setNumTrees(100);
		rf.setSampleSize(100);
		rf.setNumFeatures(5);
		for (String message : rf.getInfoMessages()) {
			System.out.println(message);
		}
		// miraculously learn a decision tree
		DecisionTree dt = new DecisionTree();
		TreeNode node = new TreeNode(0, 1);
		TreeNode nodeL = new TreeNode(1, 2);
		TreeNode nodeR = new TreeNode(2, 3);
		node.setLeftChild(1);
		node.setRightChild(2);
		dt.addNode(node);
		dt.addNode(nodeL);
		dt.addNode(nodeR);
		dt.addNode(node);

		// extract a graph from the tree
		GraphExtractor extractor = new GraphExtractor(dt);
		DirectedSparseMultigraph<Integer, Integer> graph = extractor.getGraph();

		GraphMetrics metric = new GraphMetrics();
		metric.computeAllMetrices(graph);
		long[] counts = metric.getTriadicCounts(graph);
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > 0)
				System.out.println("Graph motif " + i + ": " + counts[i]);
		}
		double diameter = metric.getDiamater();
		System.out.println("Graph diameter is " + diameter);
		double medianDegree = metric.getMedianDegree();
		System.out.println("Graph median degree is " + medianDegree);

		double avgInDegree = metric.getAvgInDegree();
		double avgOutDegree = metric.getAvgOutDegree();
		System.out.println("avg in degree: " + avgInDegree + " avg out degree:" + avgOutDegree);

		double avgClusCoeff = metric.getAvgClusteringCoeff();
		double avgBetweenness = metric.getAvgBetweenness();
		System.out.println("avg clustering coefficient: " + avgClusCoeff + " avg betweenness: " + avgBetweenness);

	}
}
