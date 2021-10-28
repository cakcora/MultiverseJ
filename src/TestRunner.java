import experiments.PoisonedLabelExperiment;
import experiments.TDAMapperSelectionExperiment;
import experiments.ClusterSelectionExperiment;
public class TestRunner {
    public static void main(String[] args) throws Exception {
        // Change before run
        String projectPath = "C:\\Users\\win_10\\Desktop\\MyProject\\Multiverse\\";

        // Variables for Poisoned Function Run
        String[] argArrayPoisoned =new String[] {
                projectPath +"data\\Diabetes\\diabetes.data", " ",
                "," , projectPath +"\\trees\\trees"
                , projectPath +"Results\\metrics.txt"
                , projectPath +"Results\\graphs.txt"};

        // Variables for TDAMap Function Run
        String[] argArrayTDAMapper = new String[] {
                projectPath +"Results\\clusterNodes.csv",
                projectPath +"Results\\clusterLinks.csv",
                projectPath +"\\trees\\trees",
                projectPath +"Results\\clusternodeIDs.csv",
                projectPath +"data\\adult.data",
                " ",
                ",",
                projectPath +"clusterOutPut\\clusteroutput.txt",
                "0",
                "45"
        };

        String[] argArrayClusterSelection = new String[] {
                projectPath +"clusterOutPut\\clusteroutput.txt",
                projectPath +"Results\\clusterLinks.csv",



        };


        // for running Poisoned uncomment it
        RunPoisonedLabelExperiment(argArrayPoisoned);

        // for running TDAMap uncomment it
        //RunTDAMapper(argArrayTDAMapper);

        // for running ClusterSelectionExperiment uncomment it
        //RunClusterSelectionExperiment(argArrayClusterSelection);
    }

    public static void RunPoisonedLabelExperiment(String[] argArray)
    {
        System.out.print("Running PLE ... \n");
        try {
            PoisonedLabelExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  PLE ... \n");
        }
    }

    public static void RunTDAMapper(String[] argArray)
    {
        System.out.print("Running TDAMap ... \n");
        try {
            TDAMapperSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  TDAMap ... \n");
        }
    }

    public static void RunClusterSelectionExperiment(String[] argArray)
    {
        System.out.print("Running ClusterSelectionExperiment ... \n");
        try {
            ClusterSelectionExperiment.main(argArray);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Exception Occurred in running  ClusterSelectionExperiment ... \n");
        }
    }
}

