package mlcore;

import java.util.Arrays;
import java.util.List;

import core.DataPoint;
import core.DecisionTree;
import core.TreeNode;

/**
 * Learns single decision tree from a given set of DataPoints and feature set.
 * This class only learns tree for binary classification problem based on
 * entropy.
 * 
 * @author Huseyincan Kaynak
 * @author Murat Ali Bayir.
 *
 */
public class DecisionTreeLearner {

	/**
	 * DecisionTree TODO: maybe create a getter function for this.
	 */
	private DecisionTree tree;

	/**
	 * Maximum depth for Decision Tree.
	 */
	private int maxDepth;

	/**
	 * Splitter for binary data.
	 */
	private BinaryFeatureSplitter binarySplitter;

	/**
	 * Splitter for continuous data.
	 */
	private ContinuousFeatureSplitter continuousSplitter;

	/**
	 * Sorting utility for current learner.
	 */
	private QuickSort quickSorter;

	/**
	 * Indexes of features that current tree is using.
	 */
	private int[] features;

	/**
	 * minimum population for split.
	 */
	private int minPopulation;

	public DecisionTreeLearner() {
		this.quickSorter = new QuickSort();
	}

	public DecisionTreeLearner(int maxDepth, int minPopulation, int[] features) {
		this();
		this.maxDepth = maxDepth;
		this.tree = new DecisionTree();
		this.binarySplitter = new BinaryFeatureSplitter(minPopulation);
		this.continuousSplitter = new ContinuousFeatureSplitter(minPopulation);
		this.features = features;
		this.minPopulation = minPopulation;
	}

	/**
	 * Trains decision tree from given data.
	 * 
	 * @param data
	 * @return
	 */
	public DecisionTree train(List<DataPoint> data) {
		dfs(data);
		return tree;
	}

	/**
	 * DFS Based learner, building Decision Tree with DFS algorithm.
	 * 
	 * @param dataSet is the input data set.
	 */
	public void dfs(List<DataPoint> dataSet) {
		boolean[] shouldSplit = new boolean[dataSet.get(0).getFeatures().length];
		Arrays.fill(shouldSplit, Boolean.FALSE);
		for (int index : features) {
			shouldSplit[index] = true;
		}

		dfsRecursion(dataSet, 0, dataSet.size() - 1, 0, shouldSplit);
	}

	/**
	 * Recursive function to build decision tree in DF manner. Returns the index of
	 * newly generated node that represents the sublist from startIndex to endIndex.
	 * 
	 * @param dataSet     is the input data set.
	 * @param startIndex  the start index of the sub list that is considered.
	 * @param endIndex    the end index of the sub list that is considered.
	 * @param depth       is the current depth that this function is called for.
	 * @param shouldSplit stores homogeneity of the feature set for remaining data
	 *                    in [startIndex, endIndex].
	 */
	private int dfsRecursion(List<DataPoint> dataSet, int startIndex, int endIndex, int depth, boolean[] shouldSplit) {
		int size = endIndex - startIndex + 1;
		if ((depth < this.maxDepth) && (size > this.minPopulation)) {
			var bestSplit = findBestSplit(dataSet, startIndex, endIndex, shouldSplit);
			if (bestSplit == null) {
				// Can not split due to feature and data related condition.
				return makeLeafNode(dataSet, startIndex, endIndex);
			} else {
				// Already have a split. We need to call recursively.
				var pivotPosition = quickSorter.partition(dataSet, startIndex, endIndex, bestSplit.getPivot(),
						bestSplit.getFeatureId());
				var currentNode = makeIntermediateNode(bestSplit);
				// Checking min population to call recursively.
				currentNode.setLeftChild(dfsRecursion(dataSet, startIndex, pivotPosition, depth + 1, shouldSplit.clone()));
				currentNode.setRightChild(dfsRecursion(dataSet, pivotPosition + 1, endIndex, depth + 1, shouldSplit.clone()));
				return currentNode.getId();
			}
		} else {
			// Max depth is reached or population lower than min population. Therefore, creating leaf node.
			return makeLeafNode(dataSet, startIndex, endIndex);
		}
	}

	/**
	 * Creates an intermediate node. Then, sets node id, feature id and split value
	 * of the node.
	 * 
	 * @param bestSplit
	 * @return the newly created intermediate node.
	 */
	private TreeNode makeIntermediateNode(Split bestSplit) {
		var node = new TreeNode(this.tree.getSize(), bestSplit.getFeatureId());
		node.setSplitValue(bestSplit.getPivot());
		this.tree.addNode(node);
		return node;
	}

	/**
	 * Makes the current node as leaf node as it can not be splitted further.
	 * 
	 * @param data       the input list of data point.
	 * @param startIndex the start index of the sub list that is considered.
	 * @param endIndex   the end index of the sub list that is considered.
	 * @return the index of newly created leaf node.
	 */
	private int makeLeafNode(List<DataPoint> dataSet, int startIndex, int endIndex) {
		// Leaf nodes are always appended at the end of the tree.
		int indexOfCurrentNode = this.tree.getSize();
		// Leaf nodes does not have any feature index.
		// Hereby, we're setting feature index to -1.
		var leafNode = new TreeNode(indexOfCurrentNode, -1);
		// Leaf nodes does not have any child.
		leafNode.setRightChild(-1);
		leafNode.setLeftChild(-1);
		double sumOfLabels = 0.0d;
		for (int dataIndex = startIndex; dataIndex <= endIndex; dataIndex++) {
			sumOfLabels += dataSet.get(dataIndex).getLabel();
		}
		double leafLabel = sumOfLabels / (endIndex - startIndex + 1 * 1.0d);
		leafNode.setValue(leafLabel);
		leafNode.setPopulation((endIndex - startIndex + 1));
		leafNode.setSumOfPositiveLabels((int) sumOfLabels);
		this.tree.addNode(leafNode);
		return indexOfCurrentNode;
	}

	/**
	 * Finds the best split for features.
	 * 
	 * @param dataSet     the input list of data point.
	 * @param startIndex  the start index of the sub list that is considered.
	 * @param endIndex    the end index of the sub list that is considered.
	 * @param shouldSplit stores homogeneity of the feature set for remaining data
	 *                    in [startIndex, endIndex].
	 * @return
	 */
	private Split findBestSplit(List<DataPoint> dataSet, int startIndex, int endIndex, boolean[] shouldSplit) {
		Split bestSplit = null;
		Split split;
		double bestInformationGainSoFar = 0.0d;
		for (int featureIndex : this.features) {
			if (shouldSplit[featureIndex]) {
				if (dataSet.get(startIndex).isCategorical(featureIndex)) {
					split = binarySplitter.findBestSplit(featureIndex, dataSet, startIndex, endIndex);
				} else {
					split = continuousSplitter.findBestSplit(featureIndex, dataSet, startIndex, endIndex);
				}
				// Check if the current at featureIndex can be splitted.
				if (split != null) {
					if (bestInformationGainSoFar < split.getInformationGain()) {
						bestInformationGainSoFar = split.getInformationGain();
						bestSplit = split;
					}
				} else {
					// This feature can not be splitted.
					shouldSplit[featureIndex] = false;
				}
			} else {
				continue;
			}
		}

		return bestSplit;
	}
}
