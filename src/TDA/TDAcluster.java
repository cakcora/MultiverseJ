package TDA;

import core.DecisionTree;

import java.util.ArrayList;

public class TDAcluster {

    private final ArrayList<DecisionTree> trees;
    private String clusterID;
    private int treeCount;
    private final int fn = 0;
    private int tp;
    private int fp;
    private int tn;
    private int quorum;

    public TDAcluster() {
        this.trees = new ArrayList();
    }

    public void addTree(DecisionTree dt) {
        this.trees.add(dt);
    }

    public ArrayList<DecisionTree> getTrees() {
        return this.trees;
    }

    public void setId(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getID() {
        return this.clusterID;
    }

    public void setTreeCount(int size) {
        this.treeCount = size;
    }


}
