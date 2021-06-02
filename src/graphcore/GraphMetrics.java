package graphcore;

import Utils.Utils;
import com.google.common.base.Function;
import core.DataPoint;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.metrics.TriadicCensus;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

import java.util.*;

import static java.lang.Boolean.FALSE;

public class GraphMetrics {
    //Vladimir Batagelj and Andrej Mrvar,
    // A subquadratic triad census algorithm for large sparse networks with small maximum degree,
    // University of Ljubljana, http://vlado.fmf.uni-lj.si/pub/networks/doc/triads/triads.pdf
    TreeMap<String, Double> metrics = new TreeMap<String, Double>();

    public void computeAllMetrices(DirectedSparseMultigraph<Integer, Integer> graph) {
        // 1- avInDegree: average in degree of $G_j$ vertices
        // 2- mavgOutDegre: average out degree of $G_j$ vertices
        metrics = new TreeMap<>();
        int vertexCount = graph.getVertexCount();
        metrics.put("vertexCount", (double) vertexCount);
        int edgeCount = graph.getEdgeCount();
        metrics.put("edgeCount", (double) edgeCount);
        var degrees = new int[vertexCount];
        int index = 0;
        double avgInDegree = 0;
        double avgOutDegree = 0;
        for (int node : graph.getVertices()) {
            int inDegree = graph.getPredecessorCount(node);
            avgInDegree += inDegree;
            int outDegree = graph.getSuccessorCount(node);
            avgOutDegree += outDegree;
            degrees[index] = inDegree + outDegree;
            index++;
        }
        avgInDegree = avgInDegree / vertexCount;
        metrics.put("avgInDegree", avgInDegree);
        avgOutDegree = avgOutDegree / vertexCount;
        metrics.put("avgOutDegree", avgOutDegree);
        Arrays.sort(degrees);
        double evenCaseValue = (degrees[vertexCount / 2] + degrees[(vertexCount - 1) / 2]) / 2d;
        double oddCaseValue = degrees[(vertexCount) / 2];
        double medianDegree = vertexCount % 2 != 0 ? oddCaseValue : evenCaseValue;
        metrics.put("medianDegree", medianDegree);
        // 3- diameter: diameter of $G_j$
        Function<Integer, Double> distances = DistanceStatistics.averageDistances(graph);
        var distanceMap = new HashMap<Integer, Double>();
        for (Integer node : graph.getVertices()) {
            double dist = distances.apply(node);
            if (!Double.isNaN(dist))
                distanceMap.put(node, dist);
        }
        metrics.put("diameter", Collections.max(distanceMap.values()));


        // 6- numStrongCluster: number of strongly connected components on $G_j$

        // 7- avgStrCompSize: average size of strong connected components on $G_j$
        // 8- meanDist: mean distance between node pairs on the directed graph $G_j$
        double meanDistance = Utils.getAverage(distanceMap.values());
        metrics.put("meanDistance",Utils.getAverage(distanceMap.values()));

        // 9- medHub: mean hub scores of nodes on the undirected graph $G_j$
        // pass
        // 10- medAuth: median hub scores of nodes on the undirected graph $G_j$
        // pass

        // 11 TriadicCensus is a standard social network tool that counts, for each of the different possible
        // configurations of three vertices, the number of times that that configuration occurs in the given graph
        // index 0 is useless in triadic counts
        long[] counts = TriadicCensus.getCounts(graph);
        for(int i = 1; i< counts.length;i++){
            metrics.put("triad"+i, (double) counts[i]);
        }
        //12 Betweennes centrality
        var ranker = new BetweennessCentrality<Integer, Integer>(graph);
        ranker.evaluate();
        List<Double> betweennessVals = ranker.getRankScores(vertexCount);
        metrics.put("avgBetweenness", Utils.getAverage(betweennessVals));
        //13 Clustering coefficient
        Map<Integer, Double> clusteringCoeff = Metrics.clusteringCoefficients(graph);
        metrics.put("avgClusteringCoeff", Utils.getAverage(clusteringCoeff.values()));

    }

    /**
     * Converts metrics of a graph to a data point.
     *
     * @return a data point
     */
    public DataPoint convert2DataPoint() {
        DataPoint dp = new DataPoint();

        //set all metric types as non-categorical
        int metricFeatureCount = metrics.size();
        boolean[] arr = new boolean[metricFeatureCount];
        Arrays.fill(arr, FALSE);
        dp.setFeatureTypes(arr);


        double [] features = new double[metricFeatureCount];
        Map<Integer, Integer> featureMap = new HashMap<>();
        int i=0;
        for(String s:metrics.keySet()){
            featureMap.put(i,i);
            features[i]=metrics.get(s);
            i++;
        }

        dp.setFeatures(features);

        return dp;
    }

    public String[] getMetricNames() {
        String[] featureNames = new String[metrics.size()];
        int i = 0;
        for (String s : metrics.keySet()) {
            featureNames[i++] = s;
        }
        return featureNames;
    }

    public int getVertexCount() {
        double vertexCount = metrics.get("vertexCount");
        return (int) vertexCount;
    }
}
