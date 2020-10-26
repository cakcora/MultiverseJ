package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Decision Tree object.
 * 
 * @author Murat Ali Bayir
 * @author Cuneyt Akcora
 *
 */
public class DecisionTree {

	/**
	 * Contains nodes of current decision tree.
	 */
    private List<TreeNode> nodes;
    
    public DecisionTree()
    {
    	nodes = new ArrayList<TreeNode>();
    }
    
    /**
     * Gets tree node specified by node index.
     * 
     * @param nodeIndex
     * @return corresponding tree node.
     */
    public TreeNode getNode(int nodeIndex)
    {
    	if (nodeIndex >= 0 && nodeIndex < nodes.size())
    	{
    		return nodes.get(nodeIndex);
    	}
    	return null;
    }
    
    /**
	 * @TODO(HuseyinCan): To be implemented by Huseyin Can.
	 * 
	 * @param features
	 * @return
	 */
    public double Predict(double[] features)
	{
    	return 0.0;

	}
	
}
