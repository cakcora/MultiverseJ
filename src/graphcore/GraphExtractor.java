package graphcore;

import core.DecisionTree;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class GraphExtractor {

    DirectedSparseMultigraph<Integer, Integer> graph = new DirectedSparseMultigraph<>();

    public GraphExtractor(DecisionTree dt) {
        int nodeIndex = 0;
        if (dt.getNode(nodeIndex) != null) {
            graph.addVertex(nodeIndex);
            extractGraph(dt, nodeIndex);
        }
    }

    private void extractGraph(DecisionTree dt, int nodeIndex) {

        int leftChild = dt.getNode(nodeIndex).getLeftChild();
        int rightChild = dt.getNode(nodeIndex).getRightChild();
        if (leftChild != -1) {
            graph.addVertex(leftChild);
            graph.addEdge(graph.getEdgeCount(), nodeIndex, leftChild);
            extractGraph(dt, leftChild);
        }
        if (rightChild != -1) {
            graph.addVertex(rightChild);
            graph.addEdge(graph.getEdgeCount(), nodeIndex, rightChild);
            extractGraph(dt, rightChild);
        }


    }

    public DirectedSparseMultigraph<Integer, Integer> getGraph() {
        return graph;
    }
}
