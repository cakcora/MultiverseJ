package graphcore;

import core.DecisionTree;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class GraphExtractor {
    public static void main(String[] args) {
        DirectedSparseMultigraph graph = new DirectedSparseMultigraph();
        GraphExtractor ge = new GraphExtractor();
        DecisionTree dt = new DecisionTree();
        ge.extractFrom(dt);
    }

    private void extractFrom(DecisionTree dt) {
    }


}
