package core;

import java.io.Serializable;

/**
 * Represents single node in the Decision Tree.
 * 
 * @author Murat Ali Bayir
 * @author Cuneyt Akcora
 * @author Huseyincan Kaynak
 */

public class TreeNode implements Serializable{

	/**
	 * Id of the current Node.
	 */
	private int id;

	/**
	 * Id of the feature for intermediate node. This feature is used to split data
	 * for the current node. This value is -1 for leaf level nodes.
	 */
	private int featureID;

	/**
	 * Name of the current feature. Null for leaf level node.
	 */
	private String featureName;

	/**
	 * Split value of the current node.
	 */
	private double splitValue;

	/**
	 * Id of the left child. -1 for leaf level.
	 */
	private int leftChild;

	/**
	 * Id of the right child. -1 for leaf level.
	 */
	private int rightChild;

	/**
	 * Number data samples for leaf level node. This value is 0 for intermediate
	 * node.
	 */
	private int population;

	/**
	 * Total sum of positive labels. This value is 0 for intermediate node.
	 */
	private int sumOfPositiveLabels;

	/**
	 * Prediction value stored in leaf nodes;
	 */
	private double value;

	/**
	 * Construct TreeNode object.
	 * 
	 * @param id
	 * @param featureID
	 */
	public TreeNode(int id, int featureID) {
		this.id = id;
		this.featureID = featureID;
		this.population = 0;
		this.leftChild = -1;
		this.rightChild = -1;
		this.sumOfPositiveLabels = 0;
		this.value = -1.0d;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getSplitValue() {
		return splitValue;
	}

	public void setSplitValue(double splitValue) {
		this.splitValue = splitValue;
	}

	public int getLeftChild() {
		return leftChild;
	}

	public void setLeftChild(int leftChild) {
		this.leftChild = leftChild;
	}

	public int getRightChild() {
		return rightChild;
	}

	public void setRightChild(int rightChild) {
		this.rightChild = rightChild;
	}

	public int getPopulation() {
		return population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

	public int getSumOfPositiveLabels() {
		return sumOfPositiveLabels;
	}

	public void setSumOfPositiveLabels(int sumOfPositiveLabels) {
		this.sumOfPositiveLabels = sumOfPositiveLabels;
	}

	public int getId() {
		return id;
	}

	public int getFeatureID() {
		return featureID;
	}

	public String getFeatureName() {
		return featureName;
	}

	public boolean isLeafNode() {
		return leftChild == -1 && rightChild == -1;
	}

	/**
	 * Gets id of the child the subtree of which contains current data.
	 * 
	 * @param features
	 * @return child node id.
	 */
	public int getChildren(double[] features) {
		if (isLeafNode()) {
			return id;
		} else {
			if (featureID >= 0 && featureID < features.length) {
				if (features[featureID] > splitValue) {
					return rightChild;
				} else {
					return leftChild;
				}
			} else {
				throw new IllegalStateException("Feature array length: " + Integer.toString(features.length)
						+ " featureID: " + Integer.toString(featureID) + " are invalid!");
			}
		}

	}
	
	public String indentation(int depth)
	{
		var result = new StringBuffer("");
		for (int i = 0; i < depth; i++)
		{
			// Add 2 spaces for each level.
			result.append("  ");
		}
		return result.toString();
	}

	public String Print(int depth)
	{
		var result = new StringBuffer("");
		result.append(indentation(depth) + "{");
		result.append(indentation(depth) + "}");
		return result.toString();
	}
	
	public String PrintFields(int depth) {
		var result = new StringBuffer();
		result.append(indentation(depth) + "  ");
		result.append(String.format("Id: %d\n", this.id));
		result.append(indentation(depth) + "  ");
		result.append(String.format("FeatureId: %d\n", this.featureID));
		result.append(indentation(depth) + "  ");
		result.append(String.format("FeatureName: %s\n", this.featureName));
		result.append(indentation(depth) + "  ");
		result.append(String.format("SplitValue: %f\n", this.splitValue));
		result.append(indentation(depth) + "  ");
		result.append(String.format("Population: %d\n", this.population));
		result.append(indentation(depth) + "  ");
		result.append(String.format("SumOfPositiveLabels: %d\n", this.sumOfPositiveLabels));
		result.append(indentation(depth) + "  ");
		result.append(String.format("Value: %f\n", this.value));
		return result.toString();
	}
}
