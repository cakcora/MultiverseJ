package core;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static Utils.Utils.saveIntegerListToFile;

public class Dataset {
    private final List<DataPoint> dataPoints;
    private String[] featureNames;
    private Map<Integer, Integer> featureMap;

    public Dataset(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public Dataset(){
        this.dataPoints = new ArrayList<>();
    }

    public void setFeatureNames(String[] featureNames) {
        this.featureNames = featureNames;
    }

    public String[] getFeatureNames() {

        return featureNames;
    }

    public void setFeatureParents(Map<Integer, Integer> featureMap) {
        this.featureMap = featureMap;
    }

    public Map<Integer, Integer> getFeatureMap() {
        return featureMap;
    }

    public List<DataPoint> getDatapoints() {
        return dataPoints;
    }

    public void add(DataPoint datapoint) {
        this.dataPoints.add(datapoint);
    }

    /**
     * Split the dataset into training and test sets. In order to have verifiable execution, we do not sample
     * from the dataset. Instead we pick the training data from the first data points.
     *
     * @param percentageOfDataPointsTobeSelected size of the split
     * @return two datasets: training and test
     */
    public Dataset[] split(int percentageOfDataPointsTobeSelected) {
        Dataset d1 = new Dataset();
        Dataset d2 = new Dataset();
        int v = (int) Math.ceil(dataPoints.size() * percentageOfDataPointsTobeSelected / 100);
        for (int i = 0; i < v; i++) {
            DataPoint datapoint = this.dataPoints.get(i);
            datapoint.setID(i);
            d1.add(datapoint);
        }
        for (int j = v; j < this.dataPoints.size(); j++) {
            DataPoint datapoint = this.dataPoints.get(j);
            datapoint.setID(j);
            d2.add(datapoint);
        }
        d1.setFeatureParents(this.getFeatureMap());
        d2.setFeatureParents(this.getFeatureMap());
        d1.setFeatureNames(this.getFeatureNames());
        d2.setFeatureNames(this.getFeatureNames());
        return new Dataset[]{d1, d2};
    }


    /**
     * Split the dataset into training and test sets. For better sampling this method use random sample selection. Random samples can
     * be reselected for further uses if there is a valid previous randomIndex file existed. In order to have verifiable execution, we do not sample
     * from the dataset. Instead we pick the training data from the first data points.
     *
     * @param percentageOfDataPointsTobeSelectedForTest size of the split
     * @param freshRun flag for using previous random indexes (false), or a fresh sampling (true)
     *
     *
     * @return two datasets: training and test
     */

    public Dataset[] randomSplit(int percentageOfDataPointsTobeSelectedForTest, boolean freshRun)
    {
        Dataset test = new Dataset();
        Dataset train = new Dataset();
        List<DataPoint> randomTrainDataPoints = new ArrayList<DataPoint>(this.dataPoints) ;
        List<Integer> randomIndexes = new ArrayList<>() ;
        int numberOfSections = 4 ;
        int numberOfSamplesFromEachSection;
        int sectionSize ;
        boolean readRandomIndexesFromFile = false ;
        int numberOfSamples = (int) Math.ceil(dataPoints.size() * percentageOfDataPointsTobeSelectedForTest / 100);
        // sectioning DataSet
        if (dataPoints.size() > 100) { numberOfSections = dataPoints.size() / numberOfSamples ; }
        sectionSize = dataPoints.size() / numberOfSections;
        numberOfSamplesFromEachSection = numberOfSamples / numberOfSections ;
        int remainder = numberOfSamples % numberOfSections ;

        // checking if RandomIndex.txt is exists
        File f = new File("randomIndexes.txt");
        if ( f.exists()){
            // remove past Indexes for a fresh sampling
            if (freshRun){f.delete();}
            // load previous random Indexes
            else {
                try {
                    Scanner scanner = new Scanner(f);
                    while(scanner.hasNextLine()){
                        String line = scanner.nextLine();
                        randomIndexes.add(Integer.parseInt(line));
                        readRandomIndexesFromFile = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        for (int i=0 ; i < numberOfSections ; i++)
        {
                // section one includes remainder
                int numberOfSectionSelection = (i==0) ? numberOfSamplesFromEachSection+remainder : numberOfSamplesFromEachSection ;
                int randomOfSection = 0 ;
                // fresh random selection
                if (!readRandomIndexesFromFile) {
                    int min = ((i)*sectionSize);
                    int max = ((i+1)*sectionSize - numberOfSectionSelection) ;
                    randomOfSection = (int)(Math.random()*(max-min+1)+min);
                    randomIndexes.add(randomOfSection);
                }
                // reading from file
                else {
                    randomOfSection = randomIndexes.get(i);
                }


                for (int j = 0 ; j < (numberOfSectionSelection); j ++)
                {
                    int index = randomOfSection+j ;
                    DataPoint datapoint = this.dataPoints.get(index);
                    datapoint.setID(index);
                    test.add(datapoint);
                    randomTrainDataPoints.set(index,null);
                }
        }
        randomTrainDataPoints.removeAll(Collections.singleton(null));
        for (int j = 0; j < randomTrainDataPoints.size(); j++) {
            DataPoint datapoint = randomTrainDataPoints.get(j);
            datapoint.setID(j);
            train.add(datapoint);
        }

        test.setFeatureParents(this.getFeatureMap());
        train.setFeatureParents(this.getFeatureMap());
        test.setFeatureNames(this.getFeatureNames());
        train.setFeatureNames(this.getFeatureNames());

        saveIntegerListToFile("randomIndexes.txt" , randomIndexes);

        return new Dataset[]{train, test};

    }

    /**
     * Shuffle the index of data points in a dataset. We need this function to choose
     * the same training/test split in multiple python/java files. Without shuffling, the data may have a hidden order
     * that can bias results. After shuffling, we can always use the first 80% of data points as the training set.
     * This solution is better than storing the test dataset in a file.
     */
    public void shuffleDataPoints() {
        Collections.shuffle(this.dataPoints);
    }


}
