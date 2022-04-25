package experiments;

import TDA.TDAcluster;
import TDA.TFEvaluationOutput;
import core.DataPoint;
import core.Dataset;
import core.DecisionTree;
import core.RandomForest;
import loader.CSVLoader;
import loader.LoaderOptions;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.io.*;
import java.util.*;

import static java.lang.Math.abs;

/**
 * Assumes that we have computed the tdamapper graph from decision tree metrics.
 * The graph is written into clusterNodes.csv and clusterLinks.csv files
 */
public class TopologicalForestPerformanceExperiment {
    private static final double DEFAULT_VALUE = 0d;
    private static boolean EVALUATE_ON_VALIDATION = false;
    static double countTrue,sumTrue;
    static double countFalse,sumFalse;
    static double countNeutralT,countNeutralF,sumNeutral;
    // 0 -> True 1-> False 2-> Neutral
    static double [] overallValidationSumAvgParams = {0.0 , 0.0 , 0.0};
    static int validationCounter = 0,validationNeutralCounter = 0;
    static double [] overallTestSumAvgParams = {0.0 , 0.0 , 0.0};
    static int testCounter = 0, testNeutralCounter = 0;
    static Map<String, Double> clusterQualityIndexHashMap = new HashMap<>();
    // ClusterID -> List of cluster's tree Map{treeID -> Score}
    static Map<String, Map<String , Double>> treeOfClusterQualityIndexHashMap = new HashMap<>();
    static Map<String, String> finalOutput = new HashMap<>();
    static TFEvaluationOutput returnObject;
    static int [] topKOptions ={1, 2, 5};

    // for tree evaluation
    static ArrayList<Long> visitedTress = new ArrayList<>();


    public static TFEvaluationOutput main(String[] args) throws Exception {


        /*  run params:
         C:/multiverse/clusterNodes.csv
C:/multiverse/clusterLinks.csv
C:/multiverse/trees/
C://multiverse/clusternodeIDs.csv
"C:/data\Multiverse Data Research\03 The knote Authentication Dataset\data_banknote_authentication.txt"
" "
","
C:/multiverse/clusteroutput.txt
0
45
seed
         */
        String nodeFile = args[0];
        String edgeFile = args[1];
        String treeDir = args[2];
        String idFile = args[3];
        String csvFile = args[4];
        char quoteChar = args[5].charAt(0);
        char sepChar = args[6].charAt(0);
        String clusterPredictionOutputFile = args[7];
        int targetPoison1 = Integer.parseInt(args[8]);
        int targetPoison2 = Integer.parseInt(args[9]);
        int seed = Integer.parseInt(args[10]);
        int replica = Integer.parseInt(args[11]);
        returnObject = new TFEvaluationOutput();
        clusterQualityIndexHashMap.clear();
        treeOfClusterQualityIndexHashMap.clear();
        finalOutput.clear();

        // evaluation variables


        // load tdamapper clusters defined by tdamapper
        System.out.println("Loading tda mapper results...");
        Map<Integer, Integer> decisionTreeIDMap = readIds(idFile);
        List<TDAcluster> clusters = getClusters(treeDir, decisionTreeIDMap, nodeFile);

        // load dataset
        System.out.println("Loading dataset...");
        LoaderOptions options = new LoaderOptions();
        options.setQuoter(quoteChar);
        options.setSep(sepChar);


        options.featureIgnoreThreshold(20);
        options.convertRealToFactorThreshold(4);
        char separator = options.getSeparator();
        if (separator != ' ' && separator != ',' && separator != '\t') {
            System.out.println("Column separator is not set as a comma, space or tab character. Are you sure about that?");
        }
        var csvLoader = new CSVLoader(options);
        List<DataPoint> dataPoints = csvLoader.loadCSV(csvFile);
        Dataset dataset = new Dataset(dataPoints);
        dataset.setFeatureNames(csvLoader.getFeatureNames());
        dataset.setFeatureParents(csvLoader.getFeatureMap());

        Random random = new Random(151);
        RandomForest rf = new RandomForest(random);
        rf.setNumTrees(300);
        var featureSize = new HashSet(dataset.getFeatureMap().values()).size();
        int splitFeatureSize = (int) Math.ceil(Math.sqrt(featureSize));
        rf.setNumFeaturesToConsiderWhenSplitting(splitFeatureSize);
        rf.setMaxTreeDepth(100);
        rf.setMinLeafPopulation(3);
        Random rnd = new Random(seed);
        dataset.shuffleDataPoints(rnd);
        Dataset[] split = dataset.split(80, 10, 10);
        Dataset validation = split[1];
        Dataset test = split[2];
        System.out.println("Validation dataset contains " + validation.getDatapoints().size() + " datapoints");
        BufferedWriter out = new BufferedWriter(new FileWriter(clusterPredictionOutputFile));
        BufferedWriter treeEvalOut = new BufferedWriter(new FileWriter(clusterPredictionOutputFile.split("\\.")[0] + "TreeEval.csv"));
        treeEvalOut.write("TreeID,Mean,STD\n");



        for (TDAcluster cls : clusters) {
            finalOutput.clear();
            treeOfClusterQualityIndexHashMap.put(cls.getID() , new HashMap<String,Double>());
            ArrayList<DecisionTree> treesOfACluster = cls.getTrees();
            HashMap<Integer, Integer> treePoisons = new HashMap<>();
            for (DecisionTree tree : treesOfACluster) {
                int poison = tree.getPoisonLevel();

                if (!treePoisons.containsKey(poison)) treePoisons.put(poison, 0);
                treePoisons.put(poison, treePoisons.get(poison) + 1);
            }


            // using trees on validation data
            if (EVALUATE_ON_VALIDATION) {
                initiateEvaluationVariables();
                for (DataPoint dp : validation.getDatapoints()) {
                    double prob = 0d;
                    int truePredict = 0;
                    int falsePredict = 0;
                    double treePredict = 0;
                    for (DecisionTree tree : treesOfACluster) {
                        prob += tree.predict(dp.getFeatures());
                        if (tree.predict(dp.getFeatures()) > 0.5)
                        {
                            // Tree true prediction
                            truePredict += 1;
                            treePredict = 1.0;
                        }
                        else
                        {
                            // Tree false prediction
                            falsePredict += 1 ;
                            treePredict = 0 ;
                        }
                        updateTreeScore(cls.getID(), String.valueOf(tree.getID()), treePredict , dp.getLabel());
                    }
                    double yhat = prob / treesOfACluster.size();
                    double y = dp.getLabel();
                    if (Double.isNaN(yhat)) {
                        System.out.println(" Data point leads to Nan probability.");
                        continue;
                    }


                    evaluatePerCluster(truePredict, falsePredict, y, treesOfACluster.size());
                    int dTreesWithPoison1 = 0;
                    int dTreesWithPoison2 = 0;
                    if (treePoisons.containsKey(targetPoison1)) dTreesWithPoison1 = treePoisons.get(targetPoison1);
                    if (treePoisons.containsKey(targetPoison2)) dTreesWithPoison2 = treePoisons.get(targetPoison2);
                    String output = cls.getID() + "\tvalidation\t" + dp.getID() + "\t" + dTreesWithPoison2 + "\t" + dTreesWithPoison1 + "\t" + yhat + "\t" + y + "\t" + falsePredict + "\t" + truePredict + "\t" + treesOfACluster.size() + "\t";
                    finalOutput.put(String.valueOf(dp.getID()), output);
                }
                // Evaluate for tree selection prediction on each data set
                for (DataPoint dp : test.getDatapoints()) {
                    topKTreeEvaluation(cls, dp);
                    out.write(finalOutput.get(String.valueOf(dp.getID())));
                }
                writeClusterEvaluationResult("VALIDATION", cls.getID(), clusterPredictionOutputFile, treesOfACluster.size(), replica);
            }

            // using trees on test data
            initiateEvaluationVariables();
            for (DataPoint dp : test.getDatapoints()) {
                double prob = 0d;
                int truePredict = 0;
                int falsePredict = 0;
                double treePredict = 0;
                for (DecisionTree tree : treesOfACluster)
                {
                    // eval tree
                    evalTree(test.getDatapoints() , tree, treeEvalOut);
                    prob += tree.predict(dp.getFeatures());
                    if (tree.predict(dp.getFeatures()) > 0.5)
                    {
                        // Tree true prediction
                        truePredict += 1;
                        treePredict = 1.0;
                    }
                    else
                    {
                        // Tree false prediction
                        falsePredict += 1 ;
                        treePredict = 0;

                    }
                    updateTreeScore(cls.getID(), String.valueOf(tree.getID()), treePredict , dp.getLabel());
                }
                double yhat = prob / treesOfACluster.size();
                double y = dp.getLabel();
                if (Double.isNaN(yhat)) {
                    System.out.println(" Data point leads to Nan probability.");
                    continue;
                }

                evaluatePerCluster(truePredict,falsePredict,y, treesOfACluster.size());
                int dTreesWithPoison1 = 0;
                int dTreesWithPoison2 = 0;
                if (treePoisons.containsKey(targetPoison1)) dTreesWithPoison1 = treePoisons.get(targetPoison1);
                if (treePoisons.containsKey(targetPoison2)) dTreesWithPoison2 = treePoisons.get(targetPoison2);
                String output = cls.getID() + "\ttest\t" + dp.getID() + "\t" + dTreesWithPoison2 + "\t" + dTreesWithPoison1 + "\t" + yhat + "\t" + y + "\t" + falsePredict + "\t" + truePredict + "\t" + treesOfACluster.size() + "\t";
                finalOutput.put(String.valueOf(dp.getID()), output);
            }
            // Evaluate for tree selection prediction on each data set
            for (DataPoint dp : test.getDatapoints()) {
                topKTreeEvaluation(cls, dp);
                out.write(finalOutput.get(String.valueOf(dp.getID())));
            }

            writeClusterEvaluationResult("TEST",  cls.getID()  ,clusterPredictionOutputFile,  treesOfACluster.size(),replica);
        }

        out.close();
        treeEvalOut.close();
        writeTotalEvaluationResult(clusterPredictionOutputFile, replica);
        returnObject.setClusterQualityIndexHashMap(clusterQualityIndexHashMap);
        returnObject.setTreeOfClusterQualityIndexHashMap(treeOfClusterQualityIndexHashMap);
        returnObject.setTopKTreeSelection(topKOptions);
        return returnObject;
    }

    private static void evalTree(List<DataPoint> Dataset , DecisionTree tree, BufferedWriter treeEvalOut) {
        if (!visitedTress.contains(tree.getID()))
        {
            // new tree visited
            visitedTress.add(tree.getID());
            DescriptiveStatistics ds = new DescriptiveStatistics();
            for (DataPoint dp : Dataset)
            {
                double error = abs(dp.getLabel() - tree.predict(dp.getFeatures()));
                ds.addValue(error);
            }
            double mean = ds.getMean();
            double std = ds.getStandardDeviation();
            try {
                treeEvalOut.write(String.valueOf(tree.getID()) + "," + String.valueOf(mean) + "," + String.valueOf(std) + "\r\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
    }

    private static void topKTreeEvaluation(TDAcluster cls, DataPoint dp)
    {
        Map<String, Double> tempTreeOfClusterQualityIndexHashMap = new HashMap<>();
        // get tree scores of this cluster for re assessment
        tempTreeOfClusterQualityIndexHashMap = sortMapByValue(treeOfClusterQualityIndexHashMap.get(cls.getID()));
        Map<String, Double> eachTopKSelectionScore = new HashMap<>();
        for (int treeSelectionTopK : topKOptions)
        {
            int treeSelectionTopKIndex = treeSelectionTopK ;
            // ASk for alternatives if cluster has less than K trees
            eachTopKSelectionScore.put(("Top" + String.valueOf(treeSelectionTopK) + "TreePrediction"), 0d);
            double topKProb = 0d;
            if (treeSelectionTopK > cls.getTrees().size())
            {
                treeSelectionTopKIndex = cls.getTrees().size();

            }
            // sort the list
            Set<String> selectedTrees = new HashSet<>();
            List<String> treeIDs = new ArrayList<String>(tempTreeOfClusterQualityIndexHashMap.keySet());
            Collections.reverse(treeIDs);
            for (int n = 0 ; n < treeSelectionTopKIndex ; n++)
            {
                // select Top K trees
                selectedTrees.add(treeIDs.get(n));
            }
            for (DecisionTree tree : cls.getTrees()) {
                if (selectedTrees.contains(String.valueOf(tree.getID())))
                    topKProb += tree.predict(dp.getFeatures());
            }
            double prediction = topKProb / selectedTrees.size();
            eachTopKSelectionScore.put(("Top" + String.valueOf(treeSelectionTopK) + "TreePrediction"), prediction);
        }
        StringBuilder output = new StringBuilder(finalOutput.get(String.valueOf(dp.getID())));
        for (int n : topKOptions)
        {

            output.append(String.valueOf(eachTopKSelectionScore.get(("Top" + String.valueOf(n) + "TreePrediction"))));
            output.append("\t");
        }
        output.append("\r\n");
        finalOutput.put(String.valueOf(dp.getID()),output.toString());

    }

    private static void updateTreeScore(String clusterID, String treeID, double treePredict, double label) {

        if (treePredict == label) {
            // tree participated as True homogeneity
            if (treeOfClusterQualityIndexHashMap.get(clusterID).containsKey(String.valueOf(treeID))) {
                // key exist
                treeOfClusterQualityIndexHashMap.get(clusterID).computeIfPresent(String.valueOf(treeID), (k, v) -> v + 1);
            } else {
                treeOfClusterQualityIndexHashMap.get(clusterID).put(String.valueOf(treeID), 1.0);
            }
        }
        else
        {
            // tree participated as False homogeneity
            if (treeOfClusterQualityIndexHashMap.get(clusterID).containsKey(String.valueOf(treeID)))
            {
                // key exist
                treeOfClusterQualityIndexHashMap.get(clusterID).computeIfPresent(String.valueOf(treeID), (k, v) -> v - 1);
            }
            else
            {
                treeOfClusterQualityIndexHashMap.get(clusterID).put(String.valueOf(treeID) , -1.0) ;
            }

        }


    }

    private static List getClusters(String treeDir, Map<Integer, Integer> ids, String nodeFile) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(nodeFile));
        List clusters = new ArrayList<TDAcluster>();
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                var arr = line.split("\t");
                String clusterID = arr[0];
                String treeIDs = arr[1].replaceAll("\\[", "");
                treeIDs = treeIDs.replaceAll("\\]", "");
                TDAcluster cluster = new TDAcluster();
                cluster.setId(clusterID);
                for (String i : treeIDs.split(",")) {
                    DecisionTree dt = new DecisionTree();
                    int id = Integer.parseInt(i.trim());
                    Integer j = ids.get(id);
                    dt.readTree(treeDir, j + "");
                    dt.setID(j);
                    cluster.addTree(dt);
                }
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    static Map<Integer, Integer> readIds(String idFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(idFile));
        String line = "";
        Map<Integer, Integer> ids = new HashMap<>();
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] arr = line.split(",");
            ids.put(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
        }
        return ids;
    }

    private static void evaluatePerCluster(double truePredict, double falsePredict, double y, int size)
    {
        if (truePredict > falsePredict)
        {
            // the model prediction is true
            if (y == 1.0)
            {
                // TP
                sumTrue += (double) truePredict / size;
                countTrue += 1;
            }
            else
            {
                // FP
                sumFalse += (double) truePredict / size;
                countFalse += 1;
            }
        }
        else if (truePredict < falsePredict)
        {
            // the model prediction is false
            if (y == 1.0)
            {
                // FN
                sumFalse += (double) falsePredict / size;
                countFalse += 1;
            }
            else
            {
                // TN
                sumTrue += (double) falsePredict / size;
                countTrue += 1;
            }

        }
        else
        {
            // Neutral Condition
            if (y == 1.0) countNeutralT+=1;
            else countNeutralF +=1;
            sumNeutral += 0.5;

        }
    }

    private static void evaluatePerTest(String evaluationType)
    {
        switch (evaluationType){
            case "TEST" :
                overallTestSumAvgParams[0] += (sumTrue / countTrue) ;
                overallTestSumAvgParams[1] += (sumFalse / countFalse);
                if ((countNeutralF+countNeutralT) != 0) {
                    overallTestSumAvgParams[2] += (sumNeutral / (countNeutralF + countNeutralT));
                    testNeutralCounter += 1;
                }
                testCounter += 1;
                break;
            case "VALIDATION" :
                overallValidationSumAvgParams[0] += (sumTrue / countTrue) ;
                overallValidationSumAvgParams[1] += (sumFalse / countFalse);
                if ((countNeutralF+countNeutralT) != 0) {
                    overallValidationSumAvgParams[2] += (sumNeutral / (countNeutralF + countNeutralT));
                    validationNeutralCounter += 1;
                }
                validationCounter += 1;
                break;
        }
    }

    private static void initiateEvaluationVariables(){
        countTrue = DEFAULT_VALUE;
        sumTrue = DEFAULT_VALUE;
        countFalse= DEFAULT_VALUE;
        sumFalse = DEFAULT_VALUE;
        countNeutralT=DEFAULT_VALUE;
        countNeutralF=DEFAULT_VALUE;
        sumNeutral = DEFAULT_VALUE ;
    }


    private static void writeClusterEvaluationResult(String evaluationType, String clsID ,String clusterPredictionOutputFile, int clusterSize, int replica){

        try
        {
            String header = "";
            // double total size for division in the next step
            if (!new File(clusterPredictionOutputFile.split("\\.")[0]+"perClusterEval.csv").exists()) {
                header = "Replica" + "," +"ClusterID" + "," + "ClusterSize" +  "," + "EvaluationType" + "," + "TruePredictionCount" +"," + "TruePredictionHomogeneity" +"," + "FalsePredictionCount" +"," + "FalsePredictionHomogeneity" +"," + "NeutralPredictionCountT" +"," + "NeutralPredictionCountF" + "," + "ClusterQualityIndex" +  "\n" ;
            }
            FileWriter evaluationOut = new FileWriter(clusterPredictionOutputFile.split("\\.")[0]+"perClusterEval.csv",true);
            evaluationOut.write(header);
            double clusterQualityIndex = calculateClusterQualityIndex();
            clusterQualityIndexHashMap.put(clsID,clusterQualityIndex);
            evaluationOut.write(replica + "," + clsID + ","+ clusterSize +  "," + evaluationType +  "," + countTrue + "," + (sumTrue / countTrue) + "," + countFalse + "," + (sumFalse / countFalse) + "," + countNeutralT + "," + countNeutralF + "," + (clusterQualityIndex) + "\n" );
            evaluationOut.close();
            evaluatePerTest(evaluationType);
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    private static double calculateClusterQualityIndex()
    {
        double totalTestSize = countTrue + countFalse + countNeutralT + countNeutralF;
        return ((countTrue/totalTestSize) * (sumTrue / countTrue)) - ((countFalse/totalTestSize) * (sumFalse / countFalse));

    }

    private static void writeTotalEvaluationResult(String clusterPredictionOutputFile, int replica){

        try
        {
            String header = "";
            if (!new File(clusterPredictionOutputFile.split("\\.")[0]+"OverallEval.csv").exists()) {
                header = "Replica" + "," + "Type" + "," + "truePredictionHomogeneity" +  "," + "falsePredictionHomogeneity" + "," + "neutralPredictionAvg"  + "\n" ;
            }
            FileWriter evaluationOut = new FileWriter(clusterPredictionOutputFile.split("\\.")[0]+"OverallEval.csv",true); //the true will append the new data
            evaluationOut.write(header);
            if (EVALUATE_ON_VALIDATION)
            evaluationOut.write(replica + "," + "VALIDATION" + "," + (overallValidationSumAvgParams[0] / validationCounter) + "," + (overallValidationSumAvgParams[1] / validationCounter) +  "," + (overallValidationSumAvgParams[2] / validationNeutralCounter) +"\n" );
            evaluationOut.write(replica + "," + "TEST" + "," + (overallTestSumAvgParams[0] / testCounter) + "," + (overallTestSumAvgParams[1] / testCounter) + "," + (overallTestSumAvgParams[2] / testNeutralCounter) + "\n" );
            evaluationOut.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
