package coreTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import core.DecisionTree;
import core.TreeNode;

/**
 * Tests functions of Decision Tree object.
 * 
 * @author Huseyincan Kaynak
 *
 */
@Testable
class DecisionTreeTest {

	/**
	 * Tests tree that contains 3 intermediate nodes and 4 leaf nodes.
	 */
	@Test
	void predictTest() {
		var decisionTree = new DecisionTree();
		double[] features = { 2.0d, 5.1d, 3.0d };
		// intermediate nodes.
		for(int i = 0; i < 3; i++) {
			var intermediate = new TreeNode(i, 1);
			intermediate.setSplitValue(1.3d);
			intermediate.setLeftChild(i * 2 + 1);
			intermediate.setRightChild(i * 2 + 2);
			decisionTree.addNode(intermediate);
		}
		// leaf nodes.
		for(int i = 3; i < 7; i++) {
			var leaf = new TreeNode(i, 1);
			leaf.setValue((float) i);
			decisionTree.addNode(leaf);
		}
		assertEquals(decisionTree.getNode(6).getId(), decisionTree.predict(features));
	}
	

	@Test
	void testPredictOneNode() {
		double[] features = { 1.0d };
		var root = new TreeNode(0, 0);
		root.setSplitValue(2.0d);
		root.setLeftChild(1);
		root.setRightChild(2);
		var leftLeaf = new TreeNode(1, 0);
		leftLeaf.setValue(10.0d);
		var rightLeaf = new TreeNode(2, 0);
		rightLeaf.setValue(20.0d);
		var decisionTree = new DecisionTree();
		decisionTree.addNode(root);
		decisionTree.addNode(leftLeaf);
		decisionTree.addNode(rightLeaf);
		assertEquals(leftLeaf.getValue(), decisionTree.predict(features));
	}
	
	@Test
	void testNoNodes() {
		var decisionTree = new DecisionTree();
		double[] features = { 1.0d, 2.0d, 3.0d };
		Assertions.assertThrows(IllegalStateException.class, () -> {
		    decisionTree.predict(features);
		  });
	}
	
	@Test
	void testInvalidFeatureId() {
		double[] features = { 1.0d };
		var root = new TreeNode(0, 1);
		var decisionTree = new DecisionTree();
		Assertions.assertThrows(IllegalStateException.class, () -> {
		    decisionTree.predict(features);
		  });	
	}
}
