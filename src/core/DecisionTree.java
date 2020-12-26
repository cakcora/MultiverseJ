package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Decision Tree object.
 * 
 * @author Murat Ali Bayir
 * @author Cuneyt Akcora
 * @author Huseyincan Kaynak
 *
 */
public class DecisionTree {

	/**
	 * Contains nodes of current decision tree.
	 */
	private List<TreeNode> nodes;

	public DecisionTree() {
		nodes = new ArrayList<TreeNode>();
	}

	/**
	 * Add node to the decision Tree.
	 * 
	 * @param node the node to be added.
	 */
	public void addNode(TreeNode node) {
		this.nodes.add(node);
	}

	/**
	 * Gets tree node specified by node index.
	 * 
	 * @param nodeIndex
	 * @return corresponding tree node.
	 */
	public TreeNode getNode(int nodeIndex) {
		if (nodeIndex >= 0 && nodeIndex < nodes.size()) {
			return nodes.get(nodeIndex);
		}
		return null;
	}
	
	/**
	 * Gets size of the nodes list.
	 * 
	 * @return tree size.
	 */
	public int getSize() {
		return nodes.size();
	}

	/**
	 * Predicts the probability for given features.
	 * 
	 * @param features is the input array of features.
	 * @return
	 */
	public double predict(double[] features) {
		if (nodes.size() > 0) {
			var root = nodes.get(0);
			return predictRecursive(root, features);
		} else {
			throw new IllegalStateException("Nodes should have at least one item!");
		}
	}

	/**
	 * 
	 * Predict the value of y-label for the given tree.
	 * 
	 * @param node     is the current tree node.
	 * @param features
	 * @return Predicted probability.
	 */
	private double predictRecursive(TreeNode node, double[] features) {
		if (node.isLeafNode()) {
			return node.getValue();
		}

		var childIndex = node.getChildren(features);
		var childNode = getNode(childIndex);
		if (childNode != null) {
			return predictRecursive(childNode, features);
		}
		return 0.0;
	}

}
