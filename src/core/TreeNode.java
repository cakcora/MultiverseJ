package core;

/**
 * Represents single node in the Decision Tree.
 * 
 * @author Murat Ali Bayir
 * @author Cuneyt Akcora
 */

public class TreeNode {

    /**
     * Id of the current Node.
     */
    private int id;

    /**
     * Id of the feature for intermediate node. This feature is used
     * to split data for the current node. This value is -1 for leaf level
     * nodes.
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
     * Number data samples for leaf level node.
     * This value is 0 for intermediate node.
     */
    private int population;
    
    /**
     * Total sum of positive labels.
     * This value is 0 for intermediate node.
     */
    private int sumOfPositiveLabels;
    
    
    /**
     * Construct TreeNode object.
     * @param id
     * @param featureID
     */
    public TreeNode(int id, int featureID)
    {
    	this.id = id;
    	this.featureID = featureID;
    	this.population = 0;
    	this.leftChild = -1;
    	this.rightChild = -1;
    	this.sumOfPositiveLabels = 0;
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
	
	/**
	 * @TODO(HuseyinCan): To be implemented by Huseyin Can.
	 * 
	 * @param features
	 * @return
	 */
	public int GetChildren(float[] features)
	{
		return -1;
	}
    
    
    
}
