import experiments.ClusterSelectionExperiment;
import experiments.PoisonedLabelExperiment;
import experiments.TDAMapperSelectionExperiment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created By Kiarash Shamsi
 * Kiarash.Shamsi@gmail.com
 * Batch Running for different samples
 */

public class BatchRunner {
    public static void main(String[] args) throws Exception {
        // Change before run
        // ATTENTION: Dont Forget to change the Path in Python File
        String projectPath = "C:\\Users\\etr\\IdeaProjects";
        int sampleSize = 1;

        // List of variables will place here

        //TODO: Clean the variables section for final batch run
        HashMap<Integer, String[]> sampleVariablesPoisoner = new HashMap<Integer, String[]>();
        // Variables for Poisoned Function Run
        String[] argArrayPoisoned = new String[]{
                projectPath + "data\\Sonar(Small)\\test.data", " ",
                ",", projectPath + "\\trees\\trees"
                , projectPath + "Results\\metrics.txt"
                , projectPath +"Results\\graphs.txt"};
        sampleVariablesPoisoner.put(1,argArrayPoisoned);

        // Variables for TDAMap Function Run
        HashMap  <Integer , String[]> sampleVariablesTDAMapper = new HashMap<Integer, String[]>();
        String[] argArrayTDAMapper = new String[] {
                projectPath +"Results\\clusterNodes.csv",
                projectPath +"Results\\clusterLinks.csv",
                projectPath +"\\trees\\trees",
                projectPath +"Results\\clusternodeIDs.csv",
                projectPath +"data\\Sonar(Small)\\test.data",
                " ",
                ",",
                projectPath +"clusterOutPut\\clusteroutput.txt",
                "0",
                "45"
        };
        sampleVariablesTDAMapper.put(1,argArrayTDAMapper);

        HashMap  <Integer , String[]> sampleVariablesClusterSelection= new HashMap<Integer, String[]>();
        String[] argArrayClusterSelection = new String[] {
                projectPath +"clusterOutPut\\clusteroutput.txt",
                projectPath +"Results\\clusterLinks.csv",

        };
        sampleVariablesClusterSelection.put(1,argArrayClusterSelection);


        for(int i =0 ; i < sampleSize ; i ++ ) {
            // for running Poisoned uncomment it
            System.out.print("\n ---- RUNNING POISONER ----");
            RunPoisonedLabelExperiment(sampleVariablesPoisoner.get(i+1));
            System.out.print("\n ---- POISONER FINISHED ----");

            // for running Python MultiverseBinaryCode.py
            System.out.print("\n ---- RUNNING Python BINARY CODE ----");

            Process p = Runtime.getRuntime().exec("python " + projectPath+"python\\MultiverseBinaryCode.py" );
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ret =  in.readLine();
            System.out.println("value is : "+ret);

//            ProcessBuilder pb = new ProcessBuilder("python", projectPath+"python\\MultiverseBinaryCode.py");
//            pb.redirectOutput(new File("output.txt"));
//            Process process = pb.start();
//            process.waitFor();
//            System.out.println("exitValue()>>"+process.exitValue());
//            ProcessBuilder.Redirect output = pb.redirectOutput();
//            File outputFile = output.file();
//            BufferedReader br = new BufferedReader(new FileReader(outputFile));
//            String st;
//            while ((st = br.readLine()) != null) {
//                System.out.println(st);
//            }

            System.out.print("\n ---- Python BINARY CODE FINISHED ----");

            // for running TDAMap uncomment it
            System.out.print("\n ---- RUNNING TDAMapper ----");
            RunTDAMapper(sampleVariablesTDAMapper.get(i+1));
            System.out.print("\n ---- TDAMapper FINISHED ----");

            // for running ClusterSelectionExperiment uncomment it
            System.out.print("\n ---- RUNNING Cluster Selection ----");
            RunClusterSelectionExperiment(sampleVariablesClusterSelection.get(i+1));
            System.out.print("\n ---- Cluster Selection FINISHED :) ----");
        }


    }

    public static void RunPoisonedLabelExperiment(String[] argArray) {
        System.out.print("\n Running PLE ... \n");
        try {
            PoisonedLabelExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  PLE ... \n");
        }
    }

    public static void RunTDAMapper(String[] argArray) {
        System.out.print("\n Running TDAMap ... \n");
        try {
            TDAMapperSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  TDAMap ... \n");
        }
    }

    public static void RunClusterSelectionExperiment(String[] argArray) {
        System.out.print("\n Running ClusterSelectionExperiment ... \n");
        try {
            ClusterSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  ClusterSelectionExperiment ... \n");
        }
    }
}

