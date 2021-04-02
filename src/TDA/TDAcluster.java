package TDA;

import core.DecisionTree;

import java.util.ArrayList;

public class TDAcluster {

    private final ArrayList<DecisionTree> trees;

    public TDAcluster() {
        this.trees = new ArrayList();
    }

    public void addTree(DecisionTree dt) {
        this.trees.add(dt);
    }

    public ArrayList<DecisionTree> getTrees() {
        return this.trees;
    }
}
