package experiments;

import core.DataPoint;
import core.Dataset;
import core.DecisionTree;
import core.RandomForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import graphcore.GraphExtractor;
import graphcore.GraphMetrics;
import loader.CSVLoader;
import loader.LoaderOptions;
import mlcore.FeatureImportance;
import poisoner.LabelFlippingPoisoner;

import java.util.*;

public class PoisonedLabelExperiment {
	public static void main(String[] args) throws Exception {
		LoaderOptions options = new LoaderOptions();

		options.setQuoter(args[1].charAt(0));
		options.setSep(args[2].charAt(0));
		// run params: C:/Downloads/adult.data " " "," C:/Downloads/trees/ C:/adultMetrics.txt C://adultGraphs.txt
		String csvFile = args[0];
		String outputPath = args[3];
		String metricFile = args[4];
		String graphFile = args[5];
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

		Dataset[] split = dataset.split(0.8, 0.20);
		Dataset training = split[0];

		System.out.println(Arrays.toString(csvLoader.getFeatureNames()));

		for (String message : csvLoader.getInformation()) {
			System.out.println(message);
		}
		System.out.println("Dataset has " + training.getDatapoints().size() + " data points");
		String[] featureNames = training.getFeatureNames();
		System.out.println("After encoding, each data point has " + (featureNames.length) + " features:");
		//poisoning starts
		Random random = new Random(151);
		Dataset secondLevelDataset = new Dataset();
		Map<Integer, Graph<Integer, Integer>> sampleGraphs = new HashMap<>();
		long treeId = 0;
		for (int poisonLevel = 0; poisonLevel <= 45; poisonLevel += 5) {
			LabelFlippingPoisoner poisoner = new LabelFlippingPoisoner(random);
			Dataset posionedDataset = poisoner.poison(training, poisonLevel);
			int pos = 0;
			for (DataPoint dp : posionedDataset.getDatapoints()) {
				if (dp.getLabel() == DataPoint.POSITIVE_LABEL) {
					pos++;
				}
			}
			System.out.println(poisonLevel + "-level dataset has " + pos + " positive labeled data points.");
			RandomForest rf = new RandomForest(random);
			rf.setNumTrees(300);
			rf.setSampleSize(2000);
			var featureSize = new HashSet(training.getFeatureMap().values()).size();
			int splitFeatureSize = 7;//(int) Math.ceil(Math.sqrt(featureSize));
			rf.setNumFeaturesToConsiderWhenSplitting(splitFeatureSize);
			rf.setMaxTreeDepth(100);
			rf.setMinLeafPopulation(3);
			rf.train(posionedDataset);


			List<DecisionTree> decisionTrees = rf.getDecisionTrees();
			GraphExtractor extractor = new GraphExtractor(decisionTrees.get(0));
			sampleGraphs.put(poisonLevel, extractor.getGraph());

			for (DecisionTree dt : decisionTrees) {
				// extract a graph from the tree

				dt.setID(treeId);
				dt.setPoison(poisonLevel);
				dt.writeTree(outputPath);
				GraphMetrics metric = computeGraphMetrics(dt);

				if (metric.getVertexCount() > 0) {
					DataPoint secLvlDataPoint = metric.convert2DataPoint();
					secLvlDataPoint.setLabel(poisonLevel);
					secLvlDataPoint.setID(treeId);
					secondLevelDataset.add(secLvlDataPoint);
				}
				secondLevelDataset.setFeatureNames(metric.getMetricNames());
				treeId++;
			}
		}
		int featureSize = secondLevelDataset.getFeatureNames().length;
		Map<Integer, Integer> featureMap = new HashMap<>();
		for (int i = 0; i < featureSize; i++) {
			featureMap.put(i, i);
		}
		secondLevelDataset.setFeatureParents(featureMap);
		Utils.Utils.save(metricFile, secondLevelDataset);
		Utils.Utils.saveGraphs(graphFile, sampleGraphs,
				training.getFeatureNames());

		split = secondLevelDataset.split(0.8, 0.20);
		training = split[0];

		// variable importance detection - on 2nd level random forest
		System.out.println("Second level random forest has " + training.getDatapoints().size() + " data points");

		// in the second level we do not have any one hot encoding, so every feature is derived from itself only.

		RandomForest rfSecondLevel = new RandomForest(random);
		rfSecondLevel.setNumTrees(500);
		rfSecondLevel.setSampleSize(1000);
		rfSecondLevel.setNumFeaturesToConsiderWhenSplitting(10);
		rfSecondLevel.setMaxTreeDepth(6);
		rfSecondLevel.setMinLeafPopulation(1);
		rfSecondLevel.train(training);

		Dataset test = split[1];
		FeatureImportance.computeFeatureImportance(rfSecondLevel,test);
	}

	private static GraphMetrics computeGraphMetrics(DecisionTree dt) {
		GraphExtractor extractor = new GraphExtractor(dt);
		DirectedSparseMultigraph<Integer, Integer> graph = extractor.getGraph();
		GraphMetrics metric = new GraphMetrics();
		if (graph.getVertexCount() > 1) {
			metric.computeAllMetrices(graph);
		}

		return metric;
	}
}
