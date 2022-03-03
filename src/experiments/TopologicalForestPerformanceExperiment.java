package experiments;

import TDA.TDAcluster;
import core.DataPoint;
import core.Dataset;
import core.DecisionTree;
import core.RandomForest;
import loader.CSVLoader;
import loader.LoaderOptions;

import java.io.*;
import java.util.*;

/**
 * Assumes that we have computed the tdamapper graph from decision tree metrics.
 * The graph is written into clusterNodes.csv and clusterLinks.csv files
 */
public class TopologicalForestPerformanceExperiment {
    private static final double DEFAULT_VALUE = 0d;
    static double countTrue,sumTrue;
    static double countFalse,sumFalse;
    static double countNeutralT,countNeutralF,sumNeutral;
    // storing TRUE FALSE NEUTRAL for overall assessment
    List<Double> overallValidationCountParams = new ArrayList<>();
    // 0 -> True 1-> False 2-> Neutral
    static double [] overallValidationSumAvgParams = {0.0 , 0.0 , 0.0};
    static int validationCounter = 0,validationNeutralCounter = 0;
    static double [] overallTestSumAvgParams = {0.0 , 0.0 , 0.0};
    static int testCounter = 0, testNeutralCounter = 0;


    public static void main(String[] args) throws Exception {


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


        for (TDAcluster cls : clusters) {
            ArrayList<DecisionTree> treesOfACluster = cls.getTrees();
            HashMap<Integer, Integer> treePoisons = new HashMap<>();
            for (DecisionTree tree : treesOfACluster) {
                int poison = tree.getPoisonLevel();

                if (!treePoisons.containsKey(poison)) treePoisons.put(poison, 0);
                treePoisons.put(poison, treePoisons.get(poison) + 1);
            }


            // using trees on validation data
            initiateEvaluationVariables();
            for (DataPoint dp : validation.getDatapoints()) {
                double prob = 0d;
                int truePredict = 0;
                int falsePredict = 0;

                for (DecisionTree tree : treesOfACluster) {
                    prob += tree.predict(dp.getFeatures());
                    if (prob > 0.5) truePredict += 1;
                    else falsePredict += 1 ;
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
                out.write(cls.getID() + "\tvalidation\t" + dp.getID() + "\t" + dTreesWithPoison2 + "\t" + dTreesWithPoison1 + "\t" + yhat + "\t" + y + "\t" + falsePredict + "\t" + truePredict + "\t" +  treesOfACluster.size() + "\r\n");

            }
            writeClusterEvaluationResult("VALIDATION",  cls.getID()  ,clusterPredictionOutputFile);


            // using trees on test data

            initiateEvaluationVariables();
            for (DataPoint dp : test.getDatapoints()) {
                double prob = 0d;
                int truePredict = 0;
                int falsePredict = 0;
                for (DecisionTree tree : treesOfACluster) {
                    prob += tree.predict(dp.getFeatures());
                    if (prob > 0.5) truePredict += 1;
                    else falsePredict += 1 ;
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
                out.write(cls.getID() + "\ttest\t" + dp.getID() + "\t" + dTreesWithPoison2 + "\t" + dTreesWithPoison1 + "\t" + yhat + "\t" + y + "\t" + falsePredict + "\t" + truePredict +  "\t" +  treesOfACluster.size() + "\r\n");
            }
            writeClusterEvaluationResult("TEST",  cls.getID()  ,clusterPredictionOutputFile);
        }

        out.close();
        writeTotalEvaluationResult(clusterPredictionOutputFile);
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


    private static void writeClusterEvaluationResult(String evaluationType, String clsID ,String clusterPredictionOutputFile){

        try
        {
            FileWriter evaluationOut = new FileWriter(clusterPredictionOutputFile.split("\\.")[0]+"perClusterEval.txt",true); //the true will append the new data
            evaluationOut.write(clsID + "\t"+ evaluationType +  "\ttruePredictionCount:" + countTrue + "\ttruePredictionAvg:" + (sumTrue / countTrue) + "\tfalsePredictionCount:" + countFalse + "\tfalsePredictionAvg:" + (sumFalse / countFalse) + "\tneutralPredictionCountT:" + countNeutralT + "\tneutralPredictionCountF:" + countNeutralF + "\n" );
            evaluationOut.close();
            evaluatePerTest(evaluationType);
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    private static void writeTotalEvaluationResult(String clusterPredictionOutputFile){

        try
        {
            FileWriter evaluationOut = new FileWriter(clusterPredictionOutputFile.split("\\.")[0]+"OverallEval.txt",true); //the true will append the new data
            evaluationOut.write("TYPE:" + "VALIDATION" + "\ttruePredictionAvg:" + (overallValidationSumAvgParams[0] / validationCounter) + "\tfalsePredictionAvg:" + (overallValidationSumAvgParams[1] / validationCounter) +  "\tneutralPredictionAvg:" + (overallValidationSumAvgParams[2] / validationNeutralCounter) +"\n" );
            evaluationOut.write("TYPE:" + "TEST" + "\ttruePredictionAvg:" + (overallTestSumAvgParams[0] / testCounter) + "\tfalsePredictionAvg:" + (overallTestSumAvgParams[1] / testCounter) + "\tneutralPredictionAvg:" + (overallTestSumAvgParams[2] / testNeutralCounter) + "\n" );
            evaluationOut.write("## END OF EVALUATION ##\n");
            evaluationOut.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

}
