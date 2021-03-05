package experiments;

import core.DataPoint;
import core.Dataset;
import core.DecisionTree;
import core.RandomForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import graphcore.GraphExtractor;
import graphcore.GraphMetrics;
import loader.CSVLoader;
import loader.LoaderOptions;
import poisoner.LabelFlippingPoisoner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PoisonedLabelExperiment {
	public static void main(String[] args) throws Exception {
		LoaderOptions options = new LoaderOptions();
		if (args.length == 3) {
			options.setQuoter(args[1].charAt(0));
			options.setSep(args[2].charAt(0));

		} else{
			System.out.println(
					"Error: Expecting filename, separator char and quote char");
			System.exit(2);
		}
		String csvFile = args[0];
		options.featureIgnoreThreshold(20);
		options.convertRealToFactorThreshold(4);
		char separator = options.getSeparator();
		if (separator != ' ' && separator != ',' && separator != '\t')
			System.out.println("Column separator is not set as a comma, space or tab character. Are you sure about that?");
		var csvLoader = new CSVLoader(options);
		List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile);
		Dataset dataset = new Dataset(dataPoints);
		dataset.setFeatureNames(csvLoader.getFeatureNames());
		dataset.setFeatureParents(csvLoader.getFeatureMap());


		for (String message : csvLoader.getInformation()) {
			System.out.println(message);
		}
		System.out.println("Dataset has " + dataPoints.size() + " data points");
		String[] featureNames = dataset.getFeatureNames();
		System.out.println("After encoding, each data point has " + (featureNames.length) + " features:");
		for (String feature : featureNames) {
			System.out.print(" [" + feature+"]");
		}
		System.out.println();
//
//		for(DataPoint s:dataPoints){
//			for(double f: s.getFeatures()){
//				System.out.print(f+" ");
//			}
//			System.out.print("["+s.getLabel()+"]");
//			System.out.println();
//		}
		//poisoning starts
		Random random = new Random(151);
		Dataset secondLevelDataset = new Dataset();
		for (int poisonLevel = 0; poisonLevel <= 5; poisonLevel+=5) {
			LabelFlippingPoisoner poisoner = new LabelFlippingPoisoner(random);
			Dataset posionedDataset = poisoner.poison(dataset,poisonLevel);
			RandomForest rf = new RandomForest(random);
			rf.setNumTrees(2);
			rf.setSampleSize(1000);
			rf.setNumFeaturesToConsiderWhenSplitting(10);
			rf.setMaxTreeDepth(6);
			rf.setMinLeafPopulation(1);
			rf.train(posionedDataset);
			for (String message : rf.getInfoMessages()) {
				System.out.println(message);
			}


			for (DecisionTree dt : rf.getDecisionTrees()) {
				// extract a graph from the tree
				GraphMetrics metric = computeGraphMetrics(dt);

				if (metric.getVertexCount() > 0) {
					DataPoint secLvlDataPoint = metric.convert2DataPoint();
					secLvlDataPoint.setLabel(poisonLevel);
					secondLevelDataset.add(secLvlDataPoint);

				}
			}
		}
		// variable importance detection - on 2nd level random forest
		secondLevelDataset.setFeatureNames(GraphMetrics.getMetricNames());
		for(String f:GraphMetrics.getMetricNames()){
			System.out.println(f);
		}
	}

	private static GraphMetrics computeGraphMetrics(DecisionTree dt) {
		GraphExtractor extractor = new GraphExtractor(dt);
		DirectedSparseMultigraph<Integer, Integer> graph = extractor.getGraph();

		GraphMetrics metric = new GraphMetrics();
		if(graph.getVertexCount()<=1){
			System.out.println("Graph of the decision tree does not have enough nodes");
		}
		else {
			metric.computeAllMetrices(graph);
		}

		return metric;
	}
}
