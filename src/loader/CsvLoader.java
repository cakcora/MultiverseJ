package loader;

import core.DataPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Convert CSV file with header into a list of data points.
 */
public class CSVLoader {

	/**
	 * TODO(Cuneyt): Explain what this state is used for?
	 */
	private FeatureStat featureStats;

	/**
	 * TODO(Cuneyt): Explain what this state is used for?
	 */
	private LoaderOptions options;

	/**
	 * The column index of the of the y-label in char seperated input training file.
	 */
	private int labelIndex;

	public CSVLoader(int labelIndex, LoaderOptions options) {
		this.featureStats = new FeatureStat();
		this.options = options;
		this.labelIndex = labelIndex;
	}

	/**
	 * Reads a csv file, identifies feature types and generates one hot encodes for
	 * categorical features.
	 *
	 * @param csvFile is the csv file being read.
	 * @param options contains information about parsing characters
	 * @return list of data points from the csv file
	 * @throws FileNotFoundException if the file does not exist
	 */
	public List<DataPoint> loadCSV(String csvFile) throws Exception {
		List<List<String>> lines = readLinesAsString(csvFile);
		populateFeatureMetaData(lines);
		return encodeDataPoints(lines);
	}

	/**
	 * Reads rows from a file and returns as list of string of list where inner list
	 * represents value of all features in each row.
	 *
	 * @param csvFile is the csv file being read.
	 * @return a list of rows in the file
	 * @throws FileNotFoundException if the file does not exist
	 */

	private List<List<String>> readLinesAsString(String csvFile) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(csvFile));
		// Is the first row the data header in all CSV files?
		String headerString = scanner.nextLine().replaceAll(String.valueOf(options.getQuoter()), "");
		String[] headerArray = headerString.split(String.valueOf(options.getSeparator()));
		featureStats.featureNames = headerArray;
		var lines = new ArrayList<List<String>>();
		while (scanner.hasNext()) {
			List<String> line = readLine(scanner.nextLine(), options.getSeparator(), options.getQuoter());
			lines.add(line);
		}
		scanner.close();
		return lines;
	}

	/**
	 * Populate feature statistics and encodings for categorical features.
	 *
	 * @param lines is the list of list strings that corresponds to lines of input
	 *              file.
	 */
	private void populateFeatureMetaData(List<List<String>> lines) throws Exception {
		var numberOfDataPoints = lines.size();
		int numberOfFeatures = ((numberOfDataPoints == 0 ? 0 : lines.get(0).size()));
		if (numberOfDataPoints < 1 || numberOfFeatures < 1) {
			throw new Exception("There are no data points to shape.");
		}
		featureStats.initialize(numberOfFeatures);

		for (int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++) {
			if (featureIndex == labelIndex)
				continue;
			Set<String> uniqueVals = new HashSet<>();
			for (List<String> line : lines) {
				uniqueVals.add(line.get(featureIndex));
			}
			featureStats.numberOfUniqueValuesPerFeature[featureIndex] = uniqueVals.size();
			featureStats.isContinuousFeature[featureIndex] = LoaderOptions.REAL_VALUED;
			try {
				for (String v : uniqueVals) {
					Double.parseDouble(v);
				}
			} catch (NumberFormatException e) {
				featureStats.isContinuousFeature[featureIndex] = LoaderOptions.CATEGORICAL;
			}
			if (featureStats.isContinuousFeature[featureIndex] == LoaderOptions.CATEGORICAL) {
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
	 * @param lines: features of data points
	 * @return a list of one hot encoded data points
	 */

	private List<DataPoint> encodeDataPoints(List<List<String>> lines) {
		List<DataPoint> points = new ArrayList<>();
		int numberOfFeatures = featureStats.getNumberOfFeatures();
		double oneHotExists = 1.0;
		double oneHotNotExists = 0.0;
		int numberOfTotalFeatures = featureStats.getNumberOfFinalFeatures();
		for (List<String> line : lines) {
			boolean[] featureTypes = new boolean[numberOfTotalFeatures];
			ArrayList<Double> featureValueList = new ArrayList<>();
			int globalIndex = 0;
			for (int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++) {
				if (featureIndex == labelIndex) {
					continue;
				}

				// Handle categorical features.
				if (!featureStats.isContinuousFeature[featureIndex]) {
					Map<String, Integer> featureMap = featureStats.getEncodedValues(featureIndex);
					int numberOfOneHotColumns = featureMap.size();
					int numberOfFeatureValues = featureStats.numberOfUniqueValuesPerFeature[featureIndex];
					if (numberOfFeatureValues < options.getIgnoreThreshold()) {
						// TODO(Cuneyt): Add comment to explain logic below.
						var categoricalFeatureValue = line.get(featureIndex);
						int relativeIndex = featureMap.get(categoricalFeatureValue);
						for (int i = 0; i < numberOfOneHotColumns; i++) {
							featureTypes[globalIndex] = true;
							if (i == relativeIndex) {
								featureValueList.add(oneHotExists);
							} else {
								featureValueList.add(oneHotNotExists);
							}
							globalIndex++;
						}
					}
				} else { // Continuous Feature.
					featureValueList.add(Double.parseDouble(line.get(featureIndex)));
					featureTypes[globalIndex] = false;
					globalIndex++;
				}
			}
			double[] featureVector = featureValueList.stream().mapToDouble(i -> i).toArray();
			DataPoint dataPoint = new DataPoint(featureVector);
			dataPoint.setLabel(Double.parseDouble(line.get(labelIndex)));
			dataPoint.setFeatureTypes(featureTypes);
			points.add(dataPoint);
		}
		// we will create new feature names for all on hot encoded features
		var newFeatureNames = new ArrayList<String>();
		for (int i = 0; i < numberOfFeatures; i++) {
			if (featureStats.isEncoded(i)) {
				Map<String, Integer> encodedValues = featureStats.getEncodedValues(i);
				for (String s : encodedValues.keySet()) {
					newFeatureNames.add(featureStats.featureNames[i] + "_" + s);
				}
			} else
				newFeatureNames.add(featureStats.featureNames[i]);
		}
		featureStats.featureNames = newFeatureNames.toArray(new String[0]);

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
		if (line == null || line.isEmpty()) {
			return values;
		}
		String[] features = line.split(String.valueOf(sepChar));
		for (String s : features) {
			values.add(s.replaceAll(String.valueOf(quoteChar), ""));
		}
		return values;
	}

	public String[] getFeatureNames() {
		return featureStats.featureNames;
	}

	/**
	 * A helper class to store feature information to be used in one hot encoding.
	 */
	private class FeatureStat {

		/**
		 * The number of unique values per feature.
		 */
		private int[] numberOfUniqueValuesPerFeature;

		/**
		 * Stores whether feature is continuous or categorical.
		 */
		private boolean[] isContinuousFeature;

		/**
		 * Stores the number of features.
		 */
		private int numberOfFeatures;

		/**
		 * Stores encodings of one hot encoded features. The outer key is the feature
		 * index in the original file. The inner map stores feature value to relative
		 * index of new one hot feature in the same raw feature.
		 * 
		 * TODO(Cuneyt): Simple Example is needed. 2 Raw feature. First is continuous
		 * and second is category.
		 */
		private Map<Integer, Map<String, Integer>> oneHotEncodings;

		public String[] getFeatureNames() {
			return featureNames;
		}

		// names of features
		private String[] featureNames;

		public void initialize(int numberOfFeatures) {
			this.numberOfUniqueValuesPerFeature = new int[numberOfFeatures];
			this.isContinuousFeature = new boolean[numberOfFeatures];
			this.oneHotEncodings = new HashMap<>();
			this.numberOfFeatures = numberOfFeatures;
		}

		public void saveEncoding(int featureIndex, Map<String, Integer> fEncoder) {
			oneHotEncodings.put(featureIndex, fEncoder);
		}

		public Map<String, Integer> getEncodedValues(int featureIndex) {
			return oneHotEncodings.get(featureIndex);
		}

		public int getNumberOfFeatures() {
			return numberOfFeatures;
		}

		/**
		 * TODO(Cuneyt): Add comment.
		 * 
		 * @param featureIndex
		 * @return
		 */
		public boolean isEncoded(int featureIndex) {
			return !oneHotEncodings.isEmpty() ? oneHotEncodings.containsKey(featureIndex) : false;
		}

		public int getNumberOfFinalFeatures() {
			int total = isContinuousFeature.length;
			for (Integer key : oneHotEncodings.keySet()) {
				total += oneHotEncodings.get(key).size() - 1;
			}
			return total;
		}
	}
}