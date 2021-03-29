package core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
public class DecisionTree implements Serializable {
	
	/**
	 * ID for tree. May use select tree from random forest. Also used for writing tree object to file.
	 */
	private int id;

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
	
	
	public String Print(int depth, TreeNode node)
	{
		var result = new StringBuffer("");
		result.append(node.indentation(depth) + "{" + "\n");
		result.append(node.PrintFields(depth));
		if (node.isLeafNode())
		{
			result.append(node.indentation(depth) + "  Left: {}\n");
			result.append(node.indentation(depth) + "  Right: {}\n");
		} else {
			result.append(node.indentation(depth));
			result.append("  Left: \n");
			var leftChild = getNode(node.getLeftChild());
			result.append(Print(depth+1, leftChild));
			result.append(node.indentation(depth));
			result.append("  Right: \n");
			var rightChild = getNode(node.getRightChild());
			result.append(Print(depth+1, rightChild));
		}
		result.append(node.indentation(depth) + "}" + "\n");
		return result.toString();
	}
	
	public String Print()
	{
		return Print(0, nodes.get(0));
	}
	
	public void writeTree() {
		try {
			FileOutputStream fileOutputStream
		      = new FileOutputStream(this.id + "");
		    ObjectOutputStream objectOutputStream 
		      = new ObjectOutputStream(fileOutputStream);
		    objectOutputStream.writeObject(this);
		    objectOutputStream.flush();
		    objectOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void readTree(String fileName) {
		try {
			FileInputStream fileInputStream
		      = new FileInputStream(fileName);
		    ObjectInputStream objectInputStream
		      = new ObjectInputStream(fileInputStream);
		    DecisionTree readTree = (DecisionTree) objectInputStream.readObject();
		    this.id = readTree.id;
		    this.nodes = readTree.getNodes();
		    objectInputStream.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return this.id;
	}
	
	public List<TreeNode> getNodes() {
		return this.nodes;
	}
}
