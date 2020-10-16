package org;

import java.util.List;

public class RandomForest {

    private int numTrees;
    private List<DecisionTree> decisionTrees;

    // size of sampling for each bootstrap step.
    private int maxFeatures;

    private int minSamplesLeaf;

    // Minimum number of samples for each node.
    // If reached the minimum, we mark the node as
    // leaf node without further splitting.
    public static final int TREE_MIN_SIZE = 1;

}
