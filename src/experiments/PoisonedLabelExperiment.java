package experiments;

import core.DataPoint;
import core.DecisionTree;
import graphcore.GraphExtractor;
import loader.CsvLoader;
import loader.LoaderOptions;

import java.util.List;

public class PoisonedLabelExperiment {
    public static void main(String[] args) throws Exception {

        LoaderOptions options = new LoaderOptions();
        char quoteChar = options.getQuoter();
        char sepChar = options.getSeparator();
        if (args.length < 3) {
            System.out.println("Expecting filename, index of the label column, separator char and quote char");
            System.exit(1);
        } else if (args.length == 4) {
            quoteChar = args[2].charAt(0);
            options.setQuoter(quoteChar);
            sepChar = args[3].charAt(0);
            options.setSep(sepChar);

        } else if (args.length > 4) {
            System.out.println("Too many parameters: expecting filename, index of the label column, separator char and quote char");
            System.exit(2);
        }
        String csvFile = args[0];
        int labelIndex = Integer.parseInt(args[1]);
        CsvLoader csvLoader = new CsvLoader();
        List<DataPoint> dataPoints = csvLoader.loadCsv(csvFile, labelIndex, options);
        String[] featureNames = csvLoader.getFeatureNames();
        // miraculously learn a decision tree

        DecisionTree dt = new DecisionTree();
        GraphExtractor extractor = new GraphExtractor(dt);

    }
}
