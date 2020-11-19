package graphcore;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.metrics.TriadicCensus;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

import java.util.*;

public class GraphMetrics {
    // average distances from each node to the rest of the graph
    Map<Integer, Double> distanceMap;
    //clustering coefficients of nodes
    Map<Integer, Double> clusteringCoeff;
    // counts of 16 triadic motifs
    //Vladimir Batagelj and Andrej Mrvar,
    // A subquadratic triad census algorithm for large sparse networks with small maximum degree,
    // University of Ljubljana, http://vlado.fmf.uni-lj.si/pub/networks/doc/triads/triads.pdf
    private long[] triadCounts = new long[17];
    // Betweenness centrality of nodes
    private List<Double> betweennessVals;
    //diameter of the graph
    private double diameter;

    public void computerAllMetrices(DirectedSparseMultigraph<Integer, Integer> graph) {
        //1- avDegree: average degree of $G_j$ vertices
        // 3- medDegre: median degree of $G_j$ vertices
        Function<Integer, Double> distances = DistanceStatistics.averageDistances(graph);
        distanceMap = new HashMap<Integer, Double>();
        for (Integer node : graph.getVertices()) {
            double dist = distances.apply(node);
            if (!Double.isNaN(dist))
                distanceMap.put(node, dist);
        }

        // 2- diameter: diameter of $G_j$
        diameter = Collections.max(distanceMap.values());


        // 4- numWeakCluster: number of weakly connected components on $G_j$
        // 5- avgWeakCompSize: average size of weakly connected components on $G_j$
        // 6- numWeakCluster: number of strongly connected components on $G_j$
        // 7- avgStrCompSize: average size of strong connected components on $G_j$
        // 8- meanDist: mean distance between node pairs on the directed graph $G_j$
        // 9- medHub: mean hub scores of nodes on the undirected graph $G_j$
        // 10- medAuth: median hub scores of nodes on the undirected graph $G_j$

        //11 TriadicCensus is a standard social network tool that counts, for each of the different possible
        // configurations of three vertices, the number of times that that configuration occurs in the given graph
        // index 0 is useless in triadic counts
        triadCounts = Arrays.copyOfRange(TriadicCensus.getCounts(graph), 1, 17);

        //12 Betweennes centrality
        var ranker = new BetweennessCentrality<Integer, Integer>(graph);
        ranker.evaluate();
        betweennessVals = ranker.getRankScores(graph.getVertexCount());

        //13 Clustering coefficient
        clusteringCoeff = Metrics.clusteringCoefficients(graph);
    }

    public long[] getTriadicCounts(DirectedSparseMultigraph<Integer, Integer> graph) {
        return triadCounts;
    }

    public double getDiamater() {
        return diameter;
    }
}
