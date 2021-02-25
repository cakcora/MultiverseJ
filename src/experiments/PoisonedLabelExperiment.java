package experiments;

import core.DataPoint;
import core.DecisionTree;
import core.RandomForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import graphcore.GraphExtractor;
import graphcore.GraphMetrics;
import loader.CSVLoader;
import loader.LoaderOptions;
import poisoner.LabelFlippingPoisoner;

import java.util.List;
import java.util.Random;

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
		options.convertRealTofactorThreshold(4);
		char separator = options.getSeparator();
		if (separator != ' ' && separator != ',' && separator != '\t')
			System.out.println("Column separator is not set as a comma, space or tab character. Are you sure about that?");
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
			System.out.print(" [" + feature+"]");
		}
		System.out.println();

		//poisoning starts
		Random random = new Random(151);
		for (int poisonLevel = 0; poisonLevel <= 49; poisonLevel++) {
			LabelFlippingPoisoner poisoner = new LabelFlippingPoisoner(random);
			List posionedDataPoints = poisoner.poison(dataPoints,poisonLevel);
			RandomForest rf = new RandomForest(random);
			rf.setNumTrees(100);
			rf.setSampleSize(100);
			rf.setNumFeaturesToConsiderWhenSplitting(5);
			rf.setMaxTreeDepth(6);
			rf.setMinLeafPopulation(10);
			rf.train(posionedDataPoints);
			for (String message : rf.getInfoMessages()) {
				System.out.println(message);
			}
			for (DecisionTree dt : rf.getDecisionTrees()) {
				// extract a graph from the tree
				GraphMetrics metric = computeGraphMetrics(dt);
				if (metric.getVertexCount() > 0)
					System.out.println(metric.toString());


			}
		}
	}

	private static GraphMetrics computeGraphMetrics(DecisionTree dt) {
		GraphExtractor extractor = new GraphExtractor(dt);
		DirectedSparseMultigraph<Integer, Integer> graph = extractor.getGraph();
		GraphMetrics metric = new GraphMetrics();
		if(graph.getVertexCount()<=1){
			System.out.println("Graph of the decision tree does not have enough nodes");
		}
		else metric.computeAllMetrices(graph);
		return metric;
	}
}
