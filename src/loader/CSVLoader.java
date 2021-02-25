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
	 * We use featureStats to create one hot encoded data features.
	 * Each feature will have unique values, and we will expand the feature
	 * into multiple one hot encoded features based on these unique values.
	 */
	private final FeatureStat featureStats;

	/**
	 * options will define file reading parameters, such as the character
	 * that separates features in a line. At least two options are required: the separating character
	 * and the quoting character used in the data file.
	 */
	private final LoaderOptions options;

	/**
	 * The column index of the of the y-label in char separated input training file.
	 */
	private final int labelIndex;

	// a set of information messages generated during loading the file
	private final HashSet<String> information = new HashSet<>();

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
	 * @return list of data points from the csv file
	 * @throws FileNotFoundException if the file does not exist
	 */
	public List<DataPoint> loadCSV(String csvFile) throws Exception {
		List<List<String>> lines = readLinesAsString(csvFile);
		populateFeatureMetaData(lines);
		return encodeAllDataPointsForAllIndices(lines);
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
		featureStats.featureNames = headerString.split(String.valueOf(options.getSeparator()));

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
		int numberOfFeatures = ((numberOfDataPoints == 0 ? 0 : lines.get(0).size()-1));
		if (numberOfDataPoints <= 1 || numberOfFeatures <= 1) {
			throw new Exception("There are no data points to shape.");
		}
		featureStats.initialize(numberOfFeatures);
		var labelEncodingMap = new HashMap<String, Double>();
		for (int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++) {
			if (featureIndex == labelIndex) {
				// this feature is the label
				for (List<String> line : lines) {
					String originalLabel = line.get(featureIndex);
					if (!labelEncodingMap.containsKey(originalLabel)) {
						labelEncodingMap.put(originalLabel, (double) labelEncodingMap.size());
					}
				}
				featureStats.setLabelEncoding(labelEncodingMap);
				this.addInformationMessage("Label has " + labelEncodingMap.size() + " unique values");
			}
			else encodeFeatureForAllDataPoints(lines, featureIndex);
		}
	}

	private void encodeFeatureForAllDataPoints(List<List<String>> lines, int featureIndex) {
		Set<String> uniqueVals = new HashSet<>();
		for (List<String> line : lines) {
			uniqueVals.add(line.get(featureIndex));
		}

		boolean isContinuous = discoverFeatureNature(uniqueVals);
		featureStats.isContinuousFeature[featureIndex] = isContinuous;

		if (!isContinuous) {
			Map<String, Integer> encodedValues = new HashMap<>();
			int oneHotIndex = 0;
			for (String uniqueFeatureVal : uniqueVals) {
				encodedValues.put(uniqueFeatureVal, oneHotIndex++);
			}
			featureStats.saveEncoding(featureIndex, encodedValues);
			featureStats.finalFeatureCount +=encodedValues.size();
		}
		else{
			featureStats.finalFeatureCount +=1;
		}
	}

	/**
	 * Find whether a feature has all continuous values or not.
	 * @param uniqueVals are the values of the feature
	 * @return true is all feature values are continuous, false otherwise
	 */
	private boolean discoverFeatureNature(Set<String> uniqueVals) {

		try {
			for (String v : uniqueVals) {
				Double.parseDouble(v);
			}
		} catch (NumberFormatException e) {
			//feature has at least one categorical value
			return false;
		}
		return uniqueVals.size() >= options.getFactorThreshold();
	}

	/**
	 * One hot encodes categorical feature values of all data points.
	 *
	 * @param lines: features of data points
	 * @return a list of one hot encoded data points
	 */

	private List<DataPoint> encodeAllDataPointsForAllIndices(List<List<String>> lines) {
		List<DataPoint> points = new ArrayList<>();
		var labelMap = featureStats.getLabelEncoding();
		for (List<String> line : lines) {
			boolean[] featureTypes = new boolean[featureStats.finalFeatureCount];
			ArrayList<Double> featureValueList = new ArrayList<>();
			double encodedlabel = -1;
			// we use a globalIndex param to keep track of how original feature indices
			// change after we insert one-hot encoded features
			int globalIndex = 0;
			for (int featureIndex = 0; featureIndex < featureStats.initialFeatureCount; featureIndex++) {
				if (featureIndex == labelIndex) {
					// this is the encodedlabel
					String originalLabel = line.get(featureIndex);
					encodedlabel = labelMap.get(originalLabel);
				} else {
					//this is a feature: CONTINUOUS OR CATEGORICAL
					if (featureStats.isContinuousFeature[featureIndex]){
						// this is a continuous feature that does not require one-hot encoding.
						featureValueList.add(Double.parseDouble(line.get(featureIndex)));
						// set the feature type as non-categorical
						featureTypes[globalIndex] = false;
						globalIndex++;
					}
					else {
						Map<String, Integer> featureMap = featureStats.getEncodedValues(featureIndex);
						int numberOfFeatureValues = featureMap.size();

						//if the feature has too many unique values, one hot encoding would create too many
						// new features. We should ignore such features.
						if (numberOfFeatureValues < options.getIgnoreThreshold()){
							// encode this categorical feature

							double[] arr = encodeFeatureForSingleDataPoint(line, featureIndex);
							for(int i=0;i<arr.length;i++){
								featureValueList.add(arr[i]);
								featureTypes[globalIndex+i]=true;
							}
							globalIndex+=arr.length;
						} else {
							this.addInformationMessage("feature index " + featureIndex +
									" is ignored because it has too many (" + numberOfFeatureValues + ") unique values");
						}
					}
				}
			}
			double[] featureVector = featureValueList.stream().mapToDouble(i -> i).toArray();
			DataPoint dataPoint = new DataPoint(featureVector);
			dataPoint.setLabel(encodedlabel);
			dataPoint.setFeatureTypes(featureTypes);

			points.add(dataPoint);
		}
		// we will create new feature names for all on hot encoded features
		var newFeatureNames = new ArrayList<String>();
		for (int i = 0; i < featureStats.initialFeatureCount; i++) {
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



	private double[] encodeFeatureForSingleDataPoint(List<String> line, int featureIndex) {
		double oneHot = 1;
		double oneCold = 0;
		// one-hot encode categorical features of data points.

		Map<String, Integer> featureMap = featureStats.getEncodedValues(featureIndex);
		int numberOfFeatureValues = featureMap.size();

		double [] arr = new double[numberOfFeatureValues];

		//get the value of this line
		var categoricalFeatureValue = line.get(featureIndex);
		// get the relative index of the feature value in one-hot encoding
		int relativeIndex = featureMap.get(categoricalFeatureValue);
		//we will set the relative index as 1 and all other indices as 0
		for (int oneHotIndex = 0; oneHotIndex < numberOfFeatureValues; oneHotIndex++) {
			if (oneHotIndex == relativeIndex) {
				arr[oneHotIndex]=oneHot;
			} else {
				arr[oneHotIndex]=oneCold;
			}
		}
		return arr;
	}

	/**
	 * Adds an info message while loading the file.
	 *
	 * @param message a string description of the message
	 */
	private void addInformationMessage(String message) {
		this.information.add(message);
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
			values.add(s.replaceAll(String.valueOf(quoteChar), "").trim());
		}
		return values;
	}

	public String[] getFeatureNames() {
		return featureStats.featureNames;
	}

	/**
	 * Retrieve warnings that were generated while loading the file
	 *
	 * @return a set of warning messages
	 */
	public HashSet<String> getInformation() {
		return this.information;
	}

	/**
	 * A helper class to store feature information to be used in one hot encoding.
	 */
	private class FeatureStat {
		/**
		 * Number of final features after one hot encoding.
		 */
		public int finalFeatureCount;

		/**
		 * Stores whether feature is continuous or categorical.
		 */
		private boolean[] isContinuousFeature;

		/**
		 * Stores the number of features before one hot encoding
		 */
		private int initialFeatureCount;

		/**
		 * Stores encodings of one hot encoded features. The outer key is the feature
		 * index in the original file. The inner map stores feature value to relative
		 * index of new one hot feature in the same raw feature.
		 * <p>
		 * |city|state|age|label|
		 * |Ardahan|DoguAnadolu|39|0|
		 * |Seattle|Washington|40|1|
		 * <p>
		 * is encoded to
		 * |city:Ardahan|city:Seattle|state:DoguAnadolu|state:Washington|age|label|
		 * |1|0|1|0|39|0|
		 * |0|1|0|1|40|1|
		 */
		private Map<Integer, Map<String, Integer>> oneHotFeatureEncodings;

		// labels are encoded to double values and stored in labelEncodingMap
		private HashMap<String, Double> labelEncodingMap;


		// names of features
		private String[] featureNames;

		public void initialize(int numberOfFeatures) {
			this.isContinuousFeature = new boolean[numberOfFeatures];
			this.oneHotFeatureEncodings = new HashMap<>();
			this.initialFeatureCount = numberOfFeatures;
			this.finalFeatureCount =0;
		}

		public void saveEncoding(int featureIndex, Map<String, Integer> fEncoder) {
			oneHotFeatureEncodings.put(featureIndex, fEncoder);
		}

		public Map<String, Integer> getEncodedValues(int featureIndex) {
			return oneHotFeatureEncodings.get(featureIndex);
		}



		/**
		 * Function to query if a feature has been encoded.
		 * @param featureIndex index of the feature
		 * @return true if the feature has been encoded, alse otherwise
		 */
		public boolean isEncoded(int featureIndex) {
			return !oneHotFeatureEncodings.isEmpty() && oneHotFeatureEncodings.containsKey(featureIndex);
		}

		public int getNumberOfFinalFeatures() {
			return finalFeatureCount;
		}

		/**
		 * Retrieve label encodings
		 *
		 * @return a map of original label values and their encoded values
		 */
		public HashMap<String, Double> getLabelEncoding() {
			return this.labelEncodingMap;
		}

		/**
		 * save encodings of label values in labelEncodingMap
		 * @param labelMap original label value-> encoded label value
		 */
		public void setLabelEncoding( HashMap<String, Double> labelMap) {
			this.labelEncodingMap=labelMap;
		}



	}

}