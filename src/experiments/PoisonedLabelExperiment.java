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
import metrics.MetricComputer;
import poisoner.LabelFlippingPoisoner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class PoisonedLabelExperiment {
	public static void main(String[] args) throws Exception {
		/* param list
		 * "C:\data\Multiverse Data Research\03 The knote Authentication Dataset\data_banknote_authentication.txt"
		 * " "
		 * ","
		 * C://multiverse/trees/
		 * "C://multiverse/metrics.txt"
		 * "C://multiverse/graphs.txt"
		 * * seed (such as 27)
		 */
		LoaderOptions options = new LoaderOptions();

		options.setQuoter(args[1].charAt(0));
		options.setSep(args[2].charAt(0));
		String csvFile = args[0];
		String outputPath = args[3];
		String metricFile = args[4];
		String graphFile = args[5];
		int seed = Integer.parseInt(args[6]);
		int poisonFirst = Integer.parseInt(args[7]);
		int poisonLast = Integer.parseInt(args[8]);
		int poisonInc = Integer.parseInt(args[9]);
		String datasetName = args[10];
		String VFAUCFile = args[11];// to save AUC of the VF on test data
		int numTree = Integer.parseInt(args[12]);
		int replica = Integer.parseInt(args[13]);
		options.featureIgnoreThreshold(20);
		options.convertRealToFactorThreshold(4);
		char separator = options.getSeparator();
		if (separator != ' ' && separator != ',' && separator != '\t')
			System.out.println("Column separator is not set as a comma, space or tab character. Are you sure about that?");
		var csvLoader = new CSVLoader(options);
		List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile).get(0);
		Dataset dataset = new Dataset(dataPoints);
		dataset.setFeatureNames(csvLoader.getFeatureNames());
		dataset.setFeatureParents(csvLoader.getFeatureMap());

		Random rnd = new Random(seed);
		dataset.shuffleDataPoints(rnd);
		Dataset[] partsOfData = dataset.split(80, 10, 10);

		Dataset training = partsOfData[0];
		Dataset validation = partsOfData[1];
		Dataset test = partsOfData[2];

		System.out.println("The fanned-out dataset has these features: " + Arrays.toString(csvLoader.getFeatureNames()));

		for (String message : csvLoader.getInformation()) {
			System.out.println(message);
		}
		System.out.println("Dataset has " + training.getDatapoints().size() + "/" + validation.getDatapoints().size() + "/" + test.getDatapoints().size() + " data points for training/validation/test");
		String[] featureNames = training.getFeatureNames();
		System.out.println("After encoding, each data point has " + (featureNames.length) + " features");
		//poisoning starts
		Random random = new Random(151);
		Dataset secondLevelDataset = new Dataset();
		// we will store sample graphs from each poison level to visualize some results in the paper
		Map<Integer, Graph<Integer, Integer>> sampleGraphs = new HashMap<>();
		long treeId = 0;
		BufferedWriter wr = new BufferedWriter(new FileWriter(VFAUCFile, true));
		for (int poisonLevel = poisonFirst; poisonLevel <= poisonLast; poisonLevel += poisonInc) {
			LabelFlippingPoisoner poisoner = new LabelFlippingPoisoner(random);
			Dataset poisonedDataset = poisoner.poison(training, poisonLevel);
			int pos = 0;
			for (DataPoint dp : poisonedDataset.getDatapoints()) {
				if (dp.getLabel() == DataPoint.POSITIVE_LABEL) {
					pos++;
				}
			}
			System.out.println(poisonLevel + "% poisoned training dataset has " + pos + " positive labeled data points.");
			long startTrain = System.currentTimeMillis();
			RandomForest rf = new RandomForest(random);
			rf.setNumTrees(numTree);
			var featureSize = new HashSet(training.getFeatureMap().values()).size();
			int splitFeatureSize = (int) Math.ceil(Math.sqrt(featureSize));
			rf.setNumFeaturesToConsiderWhenSplitting(splitFeatureSize);
			rf.setMaxTreeDepth(100);
			rf.setMinLeafPopulation(3);
			rf.train(poisonedDataset);
			long endTrain = System.currentTimeMillis();
			NumberFormat formatterTrain = new DecimalFormat("#0.00000");
			System.out.print("VF Train Execution time is " + formatterTrain.format((endTrain - startTrain) / 1000d) + " seconds\n");
			String finalTrainTime = formatterTrain.format((endTrain - startTrain) / 1000d) ;

			long startInfer = System.currentTimeMillis();
			List evaluations = rf.evaluate(test);
			MetricComputer metricComputer = new MetricComputer();
			double auc = metricComputer.computeAUC(evaluations);
			double bias = metricComputer.computeBias(evaluations);
			double logloss = metricComputer.computeLogLoss(evaluations);
			System.out.println( " \n Confusion Matrix : " + metricComputer.getConfusionMatrix(evaluations , 0.5));

			long endInfer = System.currentTimeMillis();
			NumberFormat formatterInfer = new DecimalFormat("#0.00000");
			System.out.print("VF Infer Execution time is " + formatterInfer.format((endInfer - startInfer) / 1000d) + " seconds\n");
			String finalInferTime = formatterInfer.format((endInfer - startInfer) / 1000d) ;

			System.out.println("\tRF auc on test data is " + auc);
			wr.write("FullTree" + "\t" + replica  + "\t" + datasetName + "\t" + poisonLevel + "\t" + auc + "\t" + bias + "\t" + logloss + "\t" + System.currentTimeMillis() + "\t" + finalTrainTime + "\t" + finalInferTime + "\t" + numTree + "\r\n");

			// evaluation for random subtrees
			for (int numberOfTrees : new int[]{15, 30 , 60}) {
				List randomSubTreesEvaluations = rf.evaluateRandomSubTree(test,numberOfTrees);
				MetricComputer metricComputerSubTrees = new MetricComputer();
				double aucSubTrees = metricComputerSubTrees.computeAUC(randomSubTreesEvaluations);
				double biasSubTrees = metricComputerSubTrees.computeBias(randomSubTreesEvaluations);
				double loglossSubTrees = metricComputerSubTrees.computeLogLoss(randomSubTreesEvaluations);
				wr.write("RandomSubTree" + "\t" + replica  + "\t" + datasetName + "\t" + poisonLevel + "\t" + aucSubTrees + "\t" + biasSubTrees + "\t" + loglossSubTrees + "\t" + System.currentTimeMillis() + "\t" + finalTrainTime + "\t" + finalInferTime + "\t" + numberOfTrees +"\r\n");
			}




			List<DecisionTree> decisionTrees = rf.getDecisionTrees();
			GraphExtractor extractor = new GraphExtractor(decisionTrees.get(0));
			sampleGraphs.put(poisonLevel, extractor.getGraph());

			for (DecisionTree dt : decisionTrees) {
				// extract a graph from the tree

				dt.setID(treeId);
				dt.setPoison(poisonLevel);
				dt.writeTree(outputPath);
				GraphMetrics metric = computeGraphMetrics(dt);

				int vertexCount = metric.getVertexCount();
				if (vertexCount > 1) {
					DataPoint secLvlDataPoint = metric.convert2DataPoint();
					secLvlDataPoint.setLabel(poisonLevel);
					secLvlDataPoint.setID(treeId);
					secondLevelDataset.add(secLvlDataPoint);
				} else {
					System.out.println("Error: Tree " + treeId + " in poison level " + poisonLevel + " has " + vertexCount + " node(s)?");
				}
				secondLevelDataset.setFeatureNames(metric.getMetricNames());
				treeId++;
			}
		}
		wr.close();
		int featureSize = secondLevelDataset.getFeatureNames().length;
		System.out.println(featureSize + " metrics have been extracted from each decision tree");
		// we have no fan-out for metric based features, but we still need to record feature parentage.
		Map<Integer, Integer> featureMap = new HashMap<>();
		// in the second level we do not have any one hot encoding, so every feature is derived from itself only.
		for (int i = 0; i < featureSize; i++) {
			featureMap.put(i, i);
		}
		secondLevelDataset.setFeatureParents(featureMap);// end of feature parentage

		// we will save some files for future usage
		Utils.Utils.save(metricFile, secondLevelDataset);
		Utils.Utils.saveGraphs(graphFile, sampleGraphs, training.getFeatureNames());
	}


	private static GraphMetrics computeGraphMetrics(DecisionTree dt) {
		GraphExtractor extractor = new GraphExtractor(dt);
		DirectedSparseMultigraph<Integer, Integer> graph = extractor.getGraph();
		GraphMetrics metric = new GraphMetrics();

		metric.computeAllMetrices(graph);


		return metric;
	}
}
