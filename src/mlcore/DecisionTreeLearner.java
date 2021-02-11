package mlcore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private DecisionTree tree = new DecisionTree();

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
	 * Features that will be used to train this decision tree
	 */
	private Set<Integer> features;
	/**
	 * Sorting utility for current learner.
	 */
	private QuickSort quickSorter;

	public DecisionTreeLearner() {
		this.quickSorter = new QuickSort();
	}

	public DecisionTreeLearner(int maxDepth, int minPopulation) {
		this();
		this.maxDepth = maxDepth;
		this.tree = new DecisionTree();
		this.binarySplitter = new BinaryFeatureSplitter(minPopulation);
		this.continuousSplitter = new ContinuousFeatureSplitter(minPopulation);
	}

	/**
	 * Trains decision tree from given data.
	 * 
	 * @param data is a set of data points
	 * @return a decision tree
	 */
	public DecisionTree train(List<DataPoint> data) {
		// a boolean array to tell the DT that this feature can be used in a split
		boolean[] shouldSplit = new boolean[data.get(0).getFeatures().length];

		if(this.features!=null){
			//we must be learning a DT in a random forest
			Arrays.fill(shouldSplit, Boolean.FALSE);
			 for(int featureId: this.features){
			 	shouldSplit[featureId]=true;//use this feature
			 }
		} else {
			//we are learning a standard DT that uses all features.
			Arrays.fill(shouldSplit, Boolean.TRUE);
		}
		dfs(data,shouldSplit);
		return tree;
	}

	/**
	 * DFS Based learner, building Decision Tree with DFS algorithm.
	 *
	 * @param dataSet is the input data set.
	 * @param shouldSplit indicates whether a feature can be used in learning this DT
	 */
	public void dfs(List<DataPoint> dataSet, boolean[] shouldSplit) {
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

		if (depth < this.maxDepth) {
			var bestSplit = findBestSplit(dataSet, startIndex, endIndex, shouldSplit);
			if (bestSplit == null) {
				// Can not split due to feature and data related condition.
				return makeLeafNode(dataSet, startIndex, endIndex);
			} else {
				// Already have a split. We need to call recursively.
				var pivotPosition = quickSorter.partition(dataSet, startIndex, endIndex, bestSplit.getPivot(),
						bestSplit.getFeatureId());
				var currentNode = makeIntermediateNode(bestSplit);
				currentNode
						.setLeftChild(dfsRecursion(dataSet, startIndex, pivotPosition, depth + 1, shouldSplit.clone()));
				currentNode.setRightChild(
						dfsRecursion(dataSet, pivotPosition + 1, endIndex, depth + 1, shouldSplit.clone()));
				return currentNode.getId();
			}
		} else { // Max depth is reached. Therefore, creating leaf node.
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
	 * @param dataSet       the input list of data point.
	 * @param startIndex the start index of the sub list that is considered.
	 * @param endIndex   the end index of the sub list that is considered.
	 * @return the index of newly created leaf node.
	 */
	private int makeLeafNode(List<DataPoint> dataSet, int startIndex, int endIndex) {
		// Leaf nodes are always appended at the end of the tree.
		int indexOfCurrentNode = this.tree.getSize();
		// Leaf nodes does not have any feature index.
		// Hereby, we're setting feature index to -1.
		this.tree.addNode(new TreeNode(indexOfCurrentNode, -1));
		// Leaf nodes does not have any child.
		this.tree.getNode(indexOfCurrentNode).setRightChild(-1);
		this.tree.getNode(indexOfCurrentNode).setLeftChild(-1);
		double sumOfLabels = 0.0d;
		for (int dataIndex = startIndex; dataIndex <= endIndex; dataIndex++) {
			sumOfLabels += dataSet.get(dataIndex).getLabel();
		}
		double leafLabel = sumOfLabels / (endIndex - startIndex + 1 * 1.0d);
		this.tree.getNode(indexOfCurrentNode).setValue(leafLabel);
		this.tree.getNode(indexOfCurrentNode).setPopulation((endIndex - startIndex + 1));
		this.tree.getNode(indexOfCurrentNode).setSumOfPositiveLabels((int) sumOfLabels);
		return indexOfCurrentNode;
	}

	/**
	 * Set the subset of features that can be used by this decision tree.
	 * @param features integer ids of features
	 */
	public void setFeatures(Set<Integer> features){
		this.features = features;
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
		for (int featureIndex = 0; featureIndex < dataSet.get(0).getFeatures().length; featureIndex++) {
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
