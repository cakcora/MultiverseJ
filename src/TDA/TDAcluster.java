package TDA;

import core.DecisionTree;
import metrics.SingleEval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TDAcluster {

    private final ArrayList<DecisionTree> trees;
    private String clusterID;
    private int treeCount;
    private final int fn = 0;
    private final Map<String, SingleEval> evals;
    private double auc;

    public TDAcluster() {
        this.evals = new HashMap<>();
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


    public void addToEvals(String dp, SingleEval eval) {
        evals.put(dp, eval);
    }

    public Map<String, SingleEval> getEvals() {
        return evals;
    }

    public double getAUC() {
        return this.auc;
    }

    public void setAUC(double auc) {
        this.auc = auc;
    }

    public List<SingleEval> getEvalProbs() {
        return new ArrayList<>(evals.values());
    }
}
