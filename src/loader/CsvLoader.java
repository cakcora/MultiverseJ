package loader;

import core.DataPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/*
Loads CSV files with header into a list of data points.
 */
public class CsvLoader {


    public static void main(String[] args) throws FileNotFoundException {
        CsvLoader csvLoader = new CsvLoader();
        LoaderOptions op = new LoaderOptions();
        char quoteChar = op.getQuoter();
        char sepChar = op.getSeparator();
        if (args.length < 3) {
            System.out.println("Expecting filename, index of the label column, separator char and quote char");
            System.exit(1);
        } else if (args.length == 4) {
            quoteChar = args[2].charAt(0);
            op.setQuoter(quoteChar);
            sepChar = args[3].charAt(0);
            op.setSep(sepChar);

        } else if (args.length > 4) {
            System.out.println("Too many parameters: expecting filename, index of the label column, separator char and quote char");
            System.exit(2);
        }
        String csvFile = args[0];
        int labelIndex = Integer.parseInt(args[1]);

        List<List<String>> lines = csvLoader.loadCSV(csvFile, quoteChar, sepChar);
        List<DataPoint> dataPoints = csvLoader.reshapeData(lines, labelIndex);
        for (boolean x : dataPoints.get(0).getTypes()) {
            System.out.println(x);
        }
        for (DataPoint dp : dataPoints) {
            System.out.println(dp.toString());
        }
    }


    /**
     * @param csvFile   is the csv file being read.
     * @param quoteChar is the
     * @param sepChar   is the separating character of row values
     * @return list of data points from the csv file
     * @throws FileNotFoundException
     */
    public List<List<String>> loadCSV(String csvFile, char quoteChar, char sepChar) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(csvFile));
        // Is the first row the data header in all CSV files?
        scanner.nextLine();
        List<List<String>> lines = new ArrayList();
        while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine(), sepChar, quoteChar);
            lines.add(line);
        }
        scanner.close();
        return lines;
    }

    /**
     * @param lines      a list of data points as rows
     * @param labelIndex index of the label column
     * @return an array list of data points
     */
    private List<DataPoint> reshapeData(List<List<String>> lines, int labelIndex) {
        int nOfDataPoints = lines.size();
        int nOfFeatures = ((nOfDataPoints == 0 ? 0 : lines.get(0).size()));

        //valArr will record the number of unique values for the column
        int[] valArr = new int[nOfFeatures];
        //typeArr will record the nature of this column: false for categorical true for real valued
        boolean[] typeArr = new boolean[nOfFeatures];

        Map<Integer, Map<String, Integer>> encoder = new HashMap();
        for (int featureInd = 0; featureInd < nOfFeatures; featureInd++) {
            if (featureInd == labelIndex) continue;
            Set<String> uniqueVals = new HashSet<>();
            for (int dataInd = 0; dataInd < nOfDataPoints; dataInd++) {
                uniqueVals.add(lines.get(dataInd).get(featureInd));
            }
            valArr[featureInd] = uniqueVals.size();
            typeArr[featureInd] = LoaderOptions.REAL_VALUED;
            try {
                for (String v : uniqueVals) {
                    Double.parseDouble(v);
                }
            } catch (NumberFormatException e) {
                typeArr[featureInd] = LoaderOptions.CATEGORICAL;
            }
            if (typeArr[featureInd] == LoaderOptions.CATEGORICAL) {
                Map<String, Integer> fEncoder = new HashMap<>();
                int ind = 0;
                for (String s : uniqueVals) {
                    fEncoder.put(s, ind++);
                }
                encoder.put(featureInd, fEncoder);
            }
        }

        List<DataPoint> points = new ArrayList<>();

        for (int dataInd = 0; dataInd < nOfDataPoints; dataInd++) {
            List<Boolean> types = new ArrayList<>();
            ArrayList<Double> vec = new ArrayList<>();
            for (int featureInd = 0; featureInd < nOfFeatures; featureInd++) {
                if (featureInd == labelIndex) continue;

                // one hot encoding for categorical features
                if (typeArr[featureInd] == LoaderOptions.CATEGORICAL) {
                    Map<String, Integer> featureMap = encoder.get(featureInd);
                    int oneHotCols = featureMap.size();
                    if (valArr[featureInd] < LoaderOptions.ignoreThreshold) {

                        int ind = featureMap.get(lines.get(dataInd).get(featureInd));
                        for (int k = 0; k < oneHotCols; k++) {
                            types.add(true);
                            if (k == ind) {
                                vec.add(1.0);
                            } else {
                                vec.add(0.0);
                            }
                        }
                    }
                } else {
                    vec.add(Double.parseDouble(lines.get(dataInd).get(featureInd)));
                    types.add(false);
                }
            }
            double[] arr = vec.stream().mapToDouble(i -> i).toArray();
            DataPoint dp = new DataPoint(arr);
            dp.setLabel(Double.parseDouble(lines.get(dataInd).get(labelIndex)));
            dp.setFeatureTypes(types.toArray(new Boolean[types.size()]));
            points.add(dp);
        }
        return points;
    }

    /**
     * @param line      is one row of values
     * @param sepChar   is the separating character of row values
     * @param quoteChar is the quotation character that surrounds a value
     * @return data point values in the given line as a list.
     */

    public List<String> parseLine(String line, char sepChar, char quoteChar) {

        List<String> values = new ArrayList<>();
        if (line == null && line.isEmpty()) {
            return values;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = line.toCharArray();

        for (char ch : chars) {
            if (inQuotes) {
                startCollectChar = true;
                if (ch == quoteChar) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }
                }
            } else {
                if (ch == quoteChar) {
                    inQuotes = true;
                    if (chars[0] != '"' && quoteChar == '\"') {
                        curVal.append('"');
                    }
                    if (startCollectChar) {
                        curVal.append('"');
                    }
                } else if (ch == sepChar) {
                    values.add(curVal.toString());
                    curVal = new StringBuffer();
                    startCollectChar = false;
                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }
        }
        values.add(curVal.toString());
        return values;
    }
}