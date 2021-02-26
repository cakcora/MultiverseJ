package graphcore;

import core.DecisionTree;
import core.TreeNode;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

import java.util.List;

public class GraphExtractor {

    DirectedSparseMultigraph<Integer, Integer> graph = new DirectedSparseMultigraph<>();

    public GraphExtractor(DecisionTree dt) {
        int root = 0;

        TreeNode node = dt.getNode(root);
        if (node != null) {
            graph.addVertex(node.getFeatureID());
            extractGraph(dt, root);
        }else{
            System.out.println("Error: Decision Tree has no root!");
        }
    }

    private void extractGraph(DecisionTree dt, int nodeIndex) {

        int leftChild = dt.getNode(nodeIndex).getLeftChild();
        int rightChild = dt.getNode(nodeIndex).getRightChild();
        int parentFeature = dt.getNode(nodeIndex).getFeatureID();
        if (leftChild != -1) {
            int leftFeature = dt.getNode(leftChild).getFeatureID();
            if(leftFeature!=-1) {
                System.out.println("left edge is " + parentFeature + "\t" + leftFeature);
                graph.addVertex(leftFeature);
                graph.addEdge(graph.getEdgeCount(), parentFeature, leftFeature);
                extractGraph(dt, leftChild);
            }
        }
        if (rightChild != -1) {
            int rightFeature = dt.getNode(rightChild).getFeatureID();
            if (rightFeature != -1) {
                System.out.println("right edge is " + parentFeature + "\t" + rightFeature);
                graph.addVertex(rightFeature);
                graph.addEdge(graph.getEdgeCount(), parentFeature, rightFeature);
                extractGraph(dt, rightChild);
            }
        }

    }

    public DirectedSparseMultigraph<Integer, Integer> getGraph() {
        return graph;
    }
}
