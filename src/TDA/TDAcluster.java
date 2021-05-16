package TDA;

import core.DecisionTree;

import java.util.ArrayList;

public class TDAcluster {

    private final ArrayList<DecisionTree> trees;
    private String clusterID;
    private int treeCount;
    private int tp, fp, tn, fn = 0;
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

    public void add(int label, boolean b, int quorum) {
        if (label == 1) {
            if (b)
                tp++;
            else
                fn++;
        } else if (label == 0) {
            if (b) tn++;
            else fp++;
        }
        this.quorum = quorum;
    }

    public Double getAccuracy() {
        return (tp + tn) * 1.0 / (fp + tp + fn + tn);
    }
}
