package TDA;

import java.util.HashMap;
import java.util.Map;

public class TFEvaluationOutput {
    private Map<String, Double> clusterQualityIndexHashMap = new HashMap<>();
    // ClusterID -> List of cluster's tree Map{treeID -> Score}
    private Map<String, Map<String , Double>> treeOfClusterQualityIndexHashMap = new HashMap<>();
    private int [] topKTreeSelection ;

    public Map<String, Double> getClusterQualityIndexHashMap() {
        return clusterQualityIndexHashMap;
    }

    public void setClusterQualityIndexHashMap(Map<String, Double> clusterQualityIndexHashMap) {
        this.clusterQualityIndexHashMap = clusterQualityIndexHashMap;
    }

    public Map<String, Map<String, Double>> getTreeOfClusterQualityIndexHashMap() {
        return treeOfClusterQualityIndexHashMap;
    }

    public void setTreeOfClusterQualityIndexHashMap(Map<String, Map<String, Double>> treeOfClusterQualityIndexHashMap) {
        this.treeOfClusterQualityIndexHashMap = treeOfClusterQualityIndexHashMap;
    }

    public int[] getTopKTreeSelection() {
        return topKTreeSelection;
    }

    public void setTopKTreeSelection(int[] topKTreeSelection) {
        this.topKTreeSelection = topKTreeSelection;
    }
}
