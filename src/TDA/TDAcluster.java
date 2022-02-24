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
    private final Map<String, SingleEval> validationEvals;
    private final Map<String, SingleEval> testEvals;
    private double aucValidation;
    private double aucTest;

    public TDAcluster() {
        this.validationEvals = new HashMap<>();
        this.testEvals = new HashMap<>();
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

    public int getTreeCount() {
        return (this.treeCount);
    }

    public void addToValidationEvals(String dp, SingleEval eval) {
        validationEvals.put(dp, eval);
    }

    public void addToTestEvals(String dp, SingleEval eval) {
        testEvals.put(dp, eval);
    }

    public Map<String, SingleEval> getValidationEvals() {
        return validationEvals;
    }

    public Map<String, SingleEval> getTestEvals() {
        return testEvals;
    }

    public double getValidationAUC() {
        return this.aucValidation;
    }

    public void setValidationAUC(double auc) {
        this.aucValidation = auc;
    }

    public void setTestAUC(double auc) {
        this.aucTest = auc;
    }

    public List<SingleEval> getValidationEvalProbs() {
        return new ArrayList<>(validationEvals.values());
    }

    public List<SingleEval> getTestEvalProbs() {
        return new ArrayList<>(testEvals.values());
    }
}
