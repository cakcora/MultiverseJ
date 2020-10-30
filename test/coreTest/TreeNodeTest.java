package coreTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import core.TreeNode;

/**
 * Tests functions of Tree Node object.
 * 
 * @author Huseyincan Kaynak
 *
 */
@Testable
class TreeNodeTest {

	/**
	 * One node tree, get children for root.
	 * Should return itself.
	 * 
	 */
	@Test
	void getRootChildren() {
		TreeNode root = new TreeNode(0,0);
		double[] features = {1.0};
		assertEquals(0, root.getChildren(features));
	}
	
	/**
	 * 3 node tree, get children for root.
	 * root split value: 3.0, given feature 1.0
	 * should return left child id.
	 */
	@Test
	@DisplayName("Get-Children")
	void getChildren() {
		TreeNode root = new TreeNode(0,0);
		root.setSplitValue(3.0);
		TreeNode leftNode = new TreeNode(1, 1);
		TreeNode rightNode = new TreeNode(2, 1);
		root.setLeftChild(leftNode.getId());
		root.setRightChild(rightNode.getId());
		double[] features = {1.0, 0.874};
		assertEquals(1, root.getChildren(features));
	}

}
