package loader;

import core.DataPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/*
Loads CSV files with header into a list of data points.
 */
public class CsvLoader {
    private FeatureStat featureStats = new FeatureStat();

    /**
     * reads a csv file, identifies feature types and
     * one hot encodes categorical features.
     * *
     *
     * @param csvFile is the csv file being read.
     * @param options contains information about parsing characters
     * @return list of data points from the csv file
     * @throws FileNotFoundException
     */
    public List<DataPoint> loadCsv(String csvFile, int labelIndex, LoaderOptions options) throws Exception {
        List<List<String>> lines = loadCSV(csvFile, options.getQuoter(), options.getSeparator());
        findFeatureTypes(lines, labelIndex);
        List<DataPoint> dataPoints = encodeDataPoints(lines, labelIndex);
        return dataPoints;
    }


    /**
     * Reads rows from a file and returns them as a list
     *
     * @param csvFile    is the csv file being read.
     * @param quoteChar: the character that surround feature values in the file
     * @param sepChar:   the character that separates feature values in the file
     * @return a list of rows in the file
     * @throws FileNotFoundException
     */

    private List<List<String>> loadCSV(String csvFile, char quoteChar, char sepChar) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(csvFile));
        // Is the first row the data header in all CSV files?
        String headerString = scanner.nextLine().replaceAll(String.valueOf(quoteChar), "");
        String[] headerArray = headerString.split(String.valueOf(sepChar));
        this.featureStats.addHeader(headerArray);
        var lines = new ArrayList<List<String>>();
        while (scanner.hasNext()) {
            List<String> line = readLine(scanner.nextLine(), sepChar, quoteChar);
            lines.add(line);
        }
        scanner.close();
        return lines;
    }

    /**
     * Converts a list of data lines to a list of data points.
     * One-hot encodes categorical data, ignores a categorical feature
     * if there are more than a threshold of unique values in the feature.
     *
     * @param lines      a list of data points as rows
     * @param labelIndex index of the label column
     * @return an array list of data points
     */
    private void findFeatureTypes(List<List<String>> lines, int labelIndex) throws Exception {
        int nOfDataPoints = lines.size();
        int nOfFeatures = ((nOfDataPoints == 0 ? 0 : lines.get(0).size()));
        if (nOfDataPoints < 1 || nOfFeatures < 1) {
            throw new Exception("There are no data points to shape.");
        }
        this.featureStats.setValArray(nOfFeatures);
        this.featureStats.setTypeArray(nOfFeatures);
        for (int featureIndex = 0; featureIndex < nOfFeatures; featureIndex++) {
            if (featureIndex == labelIndex) continue;
            Set<String> uniqueVals = new HashSet<>();
            for (int dataIndex = 0; dataIndex < nOfDataPoints; dataIndex++) {
                uniqueVals.add(lines.get(dataIndex).get(featureIndex));
            }
            this.featureStats.setFeatureNumUniqueVal(featureIndex, uniqueVals.size());
            this.featureStats.setFeatureType(featureIndex, LoaderOptions.REAL_VALUED);
            try {
                for (String v : uniqueVals) {
                    Double.parseDouble(v);
                }
            } catch (NumberFormatException e) {
                this.featureStats.setFeatureType(featureIndex, LoaderOptions.CATEGORICAL);
            }
            if (this.featureStats.getFeatureType(featureIndex) == LoaderOptions.CATEGORICAL) {
                Map<String, Integer> encodedValues = new HashMap<>();
                int oneHotIndex = 0;
                for (String uniqueFeatureVal : uniqueVals) {
                    encodedValues.put(uniqueFeatureVal, oneHotIndex++);
                }
                featureStats.saveEncoding(featureIndex, encodedValues);
            }
        }
    }

    /**
     * One hot encodes categorical feature values of all data points.
     *
     * @param lines:     features of data points
     * @param labelIndex column id of the label feature
     * @return a list of one hot encoded data points
     */

    private List<DataPoint> encodeDataPoints(List<List<String>> lines, int labelIndex) {
        List<DataPoint> points = new ArrayList<>();
        int nOfDataPoints = lines.size();
        int nOfFeatures = this.featureStats.getNOfFeatures();
        double oneHotExists = 1.0;
        double oneHotNotExists = 1.0;
        for (int dataIndex = 0; dataIndex < nOfDataPoints; dataIndex++) {
            List<Boolean> featureTypes = new ArrayList<>();
            ArrayList<Double> featureValueList = new ArrayList<>();
            for (int featureIndex = 0; featureIndex < nOfFeatures; featureIndex++) {
                if (featureIndex == labelIndex) continue;

                // one hot encoding for categorical features
                if (this.featureStats.getFeatureType(featureIndex) == LoaderOptions.CATEGORICAL) {
                    Map<String, Integer> featureMap = featureStats.getEncodedVals(featureIndex);
                    int oneHotCols = featureMap.size();
                    if (featureStats.getFeatureVal(featureIndex) < LoaderOptions.ignoreThreshold) {

                        int ind = featureMap.get(lines.get(dataIndex).get(featureIndex));
                        for (int k = 0; k < oneHotCols; k++) {
                            featureTypes.add(true);
                            if (k == ind) {
                                featureValueList.add(oneHotExists);
                            } else {
                                featureValueList.add(oneHotNotExists);
                            }
                        }
                    }
                } else {
                    featureValueList.add(Double.parseDouble(lines.get(dataIndex).get(featureIndex)));
                    featureTypes.add(false);
                }
            }
            double[] featureVector = featureValueList.stream().mapToDouble(i -> i).toArray();
            DataPoint dataPoint = new DataPoint(featureVector);
            dataPoint.setLabel(Double.parseDouble(lines.get(dataIndex).get(labelIndex)));
            dataPoint.setFeatureTypes(featureTypes.toArray(new Boolean[featureTypes.size()]));
            points.add(dataPoint);
        }
        return points;
    }


    /**
     * Parses a line and returns feature values in the line as a list.
     *
     * @param line      features of a data point in a row
     * @param sepChar   that separates feature values
     * @param quoteChar that encloses feature values
     * @return feature values in the given line as a list.
     */
    private List<String> readLine(String line, char sepChar, char quoteChar) {

        List<String> values = new ArrayList<>();
        if (line == null && line.isEmpty()) {
            return values;
        }
        String[] features = line.split(String.valueOf(sepChar));
        for (String s : features) {
            values.add(s.replaceAll(String.valueOf(quoteChar), ""));
        }
        return values;
    }

    public String[] getFeatureNames() {
        return this.featureStats.featureNames;
    }

    /**
     * A helper class to store feature information to be used in one hot encoding.
     */

    private class FeatureStat {
        //valArr will record the number of unique values for the column
        int[] valArr;
        //typeArr will record the nature of this column: false for categorical true for real valued
        boolean[] typeArr;
        // we will need the number of features frequently; this caches it
        int nOfFeatures;
        // a map that holds one hot encoded values of each feature (if one-hot encoded)
        Map<Integer, Map<String, Integer>> encoder;

        // names of features
        private String[] featureNames;

        public void setValArray(int nOfFeatures) {
            valArr = new int[nOfFeatures];
            encoder = new HashMap<>();
            this.nOfFeatures = nOfFeatures;
        }

        public void setFeatureNumUniqueVal(int featureInd, int size) {
            valArr[featureInd] = size;
        }

        public int getFeatureVal(int featureIndex) {
            return valArr[featureIndex];
        }

        public void setTypeArray(int nOfFeatures) {
            typeArr = new boolean[nOfFeatures];
        }

        public void setFeatureType(int featureInd, boolean featureType) {
            typeArr[featureInd] = featureType;
        }

        public boolean getFeatureType(int featureIndex) {
            return typeArr[featureIndex];
        }

        public void saveEncoding(int featureIndex, Map<String, Integer> fEncoder) {
            encoder.put(featureIndex, fEncoder);
        }

        public Map<String, Integer> getEncodedVals(int featureIndex) {
            return encoder.get(featureIndex);
        }

        public int getNOfFeatures() {
            return nOfFeatures;
        }

        public void addHeader(String[] headerArray) {
            this.featureNames = headerArray;
        }
    }
}