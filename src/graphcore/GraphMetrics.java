package graphcore;

import com.google.common.base.Function;
import core.DataPoint;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.metrics.TriadicCensus;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import Utils.Utils;
import java.util.*;

public class GraphMetrics {
    // average distances from each node to the rest of the graph
    Map<Integer, Double> distanceMap;
    // how many metrics do we extract?
    static final int metricFeatureCount = 27;
    // counts of 16 triadic motifs
    //Vladimir Batagelj and Andrej Mrvar,
    // A subquadratic triad census algorithm for large sparse networks with small maximum degree,
    // University of Ljubljana, http://vlado.fmf.uni-lj.si/pub/networks/doc/triads/triads.pdf
    private long[] triadCounts = new long[17];
    // Betweenness centrality of nodes
    //diameter of the graph
    private double diameter;


    // average number of incoming edges per node in the graph
    private double avgInDegree;
    // average number of outgoing edges per node in the graph
    private double avgOutDegree;
    //median node degree in the graph
    private double medianDegree;

    //    Average node betweenness value
    private double avgBetweenness;
    // Average node clustering coefficient
    private double avgClusteringCoeff;
    // Average distance between node pairs
    private double meanDistance;
    private int numberOfWeaklyConndComps;
    private double avgSizeOfWeaklyConndComps;
    private int vertexCount;


    public void computeAllMetrices(DirectedSparseMultigraph<Integer, Integer> graph) {
        // 1- avInDegree: average in degree of $G_j$ vertices
        // 2- mavgOutDegre: average out degree of $G_j$ vertices

        this.vertexCount = graph.getVertexCount();
        var degrees = new int[vertexCount];
        int index = 0;
        avgInDegree = 0;
        avgOutDegree = 0;
        for (int node : graph.getVertices()) {
            int inDegree = graph.getPredecessorCount(node);
            avgInDegree += inDegree;
            int outDegree = graph.getSuccessorCount(node);
            avgOutDegree += outDegree;
            degrees[index] = inDegree + outDegree;
            index++;
        }
        avgInDegree = avgInDegree / vertexCount;
        avgOutDegree = avgOutDegree / vertexCount;
        Arrays.sort(degrees);
        double evenCaseValue = (degrees[vertexCount / 2] + degrees[(vertexCount - 1) / 2]) / 2d;
        double oddCaseValue = degrees[(vertexCount) / 2];
        medianDegree = vertexCount % 2 != 0 ? oddCaseValue : evenCaseValue;

        // 3- diameter: diameter of $G_j$
        Function<Integer, Double> distances = DistanceStatistics.averageDistances(graph);
        distanceMap = new HashMap<Integer, Double>();
        for (Integer node : graph.getVertices()) {
            double dist = distances.apply(node);
            if (!Double.isNaN(dist))
                distanceMap.put(node, dist);
        }
        diameter = Collections.max(distanceMap.values());

        // 4- numWeakCluster: number of weakly connected components on $G_j$
        var wc = new WeakComponentClusterer<Integer, Integer>();
        Set<Set<Integer>> components = wc.apply(graph);
        numberOfWeaklyConndComps = components.size();
        // 5- avgWeakCompSize: average size of weakly connected components on $G_j$
        avgSizeOfWeaklyConndComps = 0d;
        for (Set component : components) {
            avgSizeOfWeaklyConndComps += component.size();
        }
        avgSizeOfWeaklyConndComps = avgSizeOfWeaklyConndComps / numberOfWeaklyConndComps;
        // 6- numStrongCluster: number of strongly connected components on $G_j$

        // 7- avgStrCompSize: average size of strong connected components on $G_j$
        // 8- meanDist: mean distance between node pairs on the directed graph $G_j$
        meanDistance = Utils.getAverage(distanceMap.values());
        // 9- medHub: mean hub scores of nodes on the undirected graph $G_j$
        // pass
        // 10- medAuth: median hub scores of nodes on the undirected graph $G_j$
        // pass

        // 11 TriadicCensus is a standard social network tool that counts, for each of the different possible
        // configurations of three vertices, the number of times that that configuration occurs in the given graph
        // index 0 is useless in triadic counts
        int numberOfMotifs = 16;
        triadCounts = Arrays.copyOfRange(TriadicCensus.getCounts(graph), 1, 1 + numberOfMotifs);

        //12 Betweennes centrality
        var ranker = new BetweennessCentrality<Integer, Integer>(graph);
        ranker.evaluate();
        List<Double> betweennessVals = ranker.getRankScores(vertexCount);
        avgBetweenness = Utils.getAverage(betweennessVals);

        //13 Clustering coefficient
        Map<Integer, Double> clusteringCoeff = Metrics.clusteringCoefficients(graph);
        avgClusteringCoeff = Utils.getAverage(clusteringCoeff.values());

        long[] counts = getTriadicCounts(graph);

    }



    public long[] getTriadicCounts(DirectedSparseMultigraph<Integer, Integer> graph) {
        return triadCounts;
    }







    @Override
    public String toString() {
        return "GraphMetrics{" +
                "diameter=" + diameter +
                ", avgInDegree=" + avgInDegree +
                ", avgOutDegree=" + avgOutDegree +
                ", medianDegree=" + medianDegree +
                ", avgBetweenness=" + avgBetweenness +
                ", avgClusteringCoeff=" + avgClusteringCoeff +
                ", meanDistance=" + meanDistance +
                ", numberOfWeaklyConndComps=" + numberOfWeaklyConndComps +
                ", avgSizeOfWeaklyConndComps=" + avgSizeOfWeaklyConndComps +
                '}';
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public DataPoint convert2DataPoint() {
        DataPoint dp = new DataPoint();

        double [] features = new double[metricFeatureCount];
        String[] featureNames = new String[metricFeatureCount];
        Map<Integer, Integer> featureMap = new HashMap<>();
        int i=0;
        features[i++]=this.avgBetweenness;
        featureNames[i]="betweenness";
        features[i++]=this.avgInDegree;
        featureNames[i]="avgInDegree";
        features[i++]=this.avgOutDegree;
        featureNames[i]="avgOutDegree";
        features[i++]=this.medianDegree;
        featureNames[i]="medianDegree";
        features[i++]=this.diameter;
        featureNames[i]="diameter";
        features[i++]= this.avgClusteringCoeff;
        featureNames[i]="avgClusteringCoeff";
        features[i++]=this.avgSizeOfWeaklyConndComps;
        featureNames[i]="avgSizeOfWeaklyConndComps";
        features[i++]=this.meanDistance;
        featureNames[i]="meanDistance";
        features[i++]=this.numberOfWeaklyConndComps;
        featureNames[i]="numberOfWeaklyConndComps";
        long[] temp = this.triadCounts;
        for(long c:temp){
            features[i++] = (double) c;
            featureNames[i]= "triad"+c;
        }
        features[i++]= this.vertexCount;
        featureNames[i]="vertexCount";

        for(int j=0;j<features.length;j++){
            featureMap.put(j,j);
        }

        return dp;
    }

    public static String[] getMetricNames() {
        String[] featureNames = new String[metricFeatureCount];
        int i=0;
        featureNames[i++]="betweenness";
        featureNames[i++]="avgInDegree";
        featureNames[i++]="avgOutDegree";
        featureNames[i++]="medianDegree";
        featureNames[i++]="diameter";
        featureNames[i++]="avgClusteringCoeff";
        featureNames[i++]="avgSizeOfWeaklyConndComps";
        featureNames[i++]="meanDistance";
        featureNames[i++]="numberOfWeaklyConndComps";

        for(int temp=1;temp<=17;temp++){
            featureNames[i++]= "triad"+temp;
        }
        featureNames[i++]="vertexCount";
        return featureNames;
    }
}
