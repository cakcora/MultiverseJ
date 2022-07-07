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

	// a set of information messages generated during loading the file
	private final HashSet<String> information = new HashSet<>();
	private Map<Integer,Integer> parentMap;

	public CSVLoader(LoaderOptions options) {
		this.featureStats = new FeatureStat();
		this.options = options;
		this.parentMap = new HashMap<>();
	}

	/**
	 * Reads a csv file, identifies feature types and generates one hot encodes for
	 * categorical features.
	 *
	 * @param csvFile is the csv file being read.
	 * @return list of data points from the csv file
	 * @throws FileNotFoundException if the file does not exist
	 */
	public List<List<DataPoint>> loadCSV(String csvFile) throws Exception {
		List<List<String>> lines = readLinesAsString(csvFile);
		extractMetadataForAllFeaturesAndLabel(lines);
		return encodeAllDataPointsForAllFeaturesAndLabel(lines);
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
	 * Populate feature statistics and encodings for all features+label.
	 *
	 * @param lines is the list of list strings that corresponds to lines of input
	 *              file.
	 */
	private void extractMetadataForAllFeaturesAndLabel(List<List<String>> lines) throws Exception {
		var numberOfDataPoints = lines.size();
		int numberOfFeatures = ((numberOfDataPoints == 0 ? 0 : lines.get(0).size()-1));
		if (numberOfDataPoints <= 1 || numberOfFeatures <= 1) {
			throw new Exception("There are no data points to shape.");
		}
		featureStats.initialize(numberOfFeatures);
		var labelEncodingMap = new HashMap<String, Double>();
		for (int colIndex = 0; colIndex < numberOfFeatures; colIndex++) {
			extractMetadataForSingleFeature(lines, colIndex);
		}
		int labelIndex = lines.get(0).size()-1;
		// encode label
		for (List<String> line : lines) {
			String originalLabel = line.get(labelIndex);
			if (!labelEncodingMap.containsKey(originalLabel)) {
				labelEncodingMap.put(originalLabel, (double) labelEncodingMap.size());
			}
		}
		featureStats.setLabelEncoding(labelEncodingMap);
		this.addInformationMessage("Label has " + labelEncodingMap.size() + " unique values");

	}

	private void extractMetadataForSingleFeature(List<List<String>> lines, int featureIndex) {
		Set<String> uniqueVals = new TreeSet<>();
		for (List<String> line : lines) {
			uniqueVals.add(line.get(featureIndex));
		}

		boolean isContinuous = discoverFeatureNature(uniqueVals);
		featureStats.isContinuousFeature[featureIndex] = isContinuous;
		int countSoFar = featureStats.finalFeatureCount;
		if (isContinuous) {
			//feature is continuous
			setFeatureParent(countSoFar,featureIndex);
			featureStats.finalFeatureCount +=1;
		}else {
			//feature is categorical
			if(uniqueVals.size()< options.getIgnoreThreshold()) {
				// we should not ignore this feature
				var encodedValues = new TreeMap<String,Integer>();
				int oneHotIndex = 0;
				int newFeatureIndex =0;
				for (String uniqueFeatureVal : uniqueVals) {
					encodedValues.put(uniqueFeatureVal, oneHotIndex++);
					setFeatureParent(countSoFar+newFeatureIndex,featureIndex);
					newFeatureIndex++;
					featureStats.finalFeatureCount +=1;
				}
				featureStats.saveEncoding(featureIndex, encodedValues);
			}else{
				information.add("Ignoring feature at col "+featureIndex+" because it has too many values.");
			}
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

	private List<List<DataPoint>> encodeAllDataPointsForAllFeaturesAndLabel(List<List<String>> lines) {
		List<DataPoint> points = new ArrayList<>();
		List<DataPoint> onlyPositivePoints = new ArrayList<>();
		List<DataPoint> onlyNegativePoints = new ArrayList<>();
		List<DataPoint> equalPositiveNegative = new ArrayList<>();
		List<List<DataPoint>> results =  new ArrayList<>();
		var labelMap = featureStats.getLabelEncoding();
		var usedFeatures = featureStats.getUsedFeatures();
		//label is the last column in the file
		int labelIndex = lines.get(0).size()-1;
		for (List<String> line : lines) {
			ArrayList<Boolean> featureTypesList = new ArrayList<>();
			ArrayList<Double> featureValuesList = new ArrayList<>();

			for (int featureIndex:usedFeatures) {
				if (featureStats.isContinuousFeature[featureIndex]){
					// this is a continuous feature that does not require one-hot encoding.
					featureValuesList.add(Double.parseDouble(line.get(featureIndex)));
					// set the feature type as non-categorical
					featureTypesList.add(false);
				}
				else {
					//get the value of this line
					var categoricalFeatureValue = line.get(featureIndex);
					encodeForFeature(categoricalFeatureValue, featureTypesList, featureValuesList, featureIndex);
				}
			}
			double[] featureVector = featureValuesList.stream().mapToDouble(i -> i).toArray();
			DataPoint dataPoint = new DataPoint(featureVector);
			//label encoding
			String originalLabel = line.get(labelIndex);
			double encodedLabel = labelMap.get(originalLabel);
			dataPoint.setLabel(encodedLabel);

			boolean[] op = new boolean[featureTypesList.size()];
			for(int n = 0; n < op.length; n++){
				op[n] = featureTypesList.get(n);
			}
			dataPoint.setFeatureTypes(op);

			if (dataPoint.IsPositive())
			{
				onlyPositivePoints.add(dataPoint);
			}
			else
			{
				onlyNegativePoints.add(dataPoint);
			}

			points.add(dataPoint);
		}
		if (onlyPositivePoints.size() >= onlyNegativePoints.size())
		{
			equalPositiveNegative.addAll(onlyNegativePoints);
			equalPositiveNegative.addAll(onlyPositivePoints.subList(0,onlyNegativePoints.size()));

		}
		else
		{
			equalPositiveNegative.addAll(onlyPositivePoints);
			equalPositiveNegative.addAll(onlyNegativePoints.subList(0,onlyPositivePoints.size()));
		}

		updateFeatureNames();
		results.add(points);
		results.add(onlyPositivePoints);
		results.add(onlyNegativePoints);
		results.add(equalPositiveNegative);
		return results;
	}

	/**
	 * keeps feature names of real valued features, and gives new names to one hot encoded features.
	 */
	private void updateFeatureNames() {
		// we will create new feature names for all one hot encoded features
		var newFeatureNames = new ArrayList<String>();
		for (int featureIndex : featureStats.getUsedFeatures() ){
			if (featureStats.isEncoded(featureIndex)) {
				Map<String, Integer> encodedValues = featureStats.getEncodedValues(featureIndex);
				for (String s : encodedValues.keySet()) {
					newFeatureNames.add(featureStats.featureNames[featureIndex] + "_" + s);
				}
			} else
				newFeatureNames.add(featureStats.featureNames[featureIndex]);
		}
		featureStats.featureNames = newFeatureNames.toArray(new String[0]);
	}

	private void encodeForFeature(String categoricalFeatureValue, ArrayList<Boolean> featureTypes, ArrayList<Double> featureValueList, int featureIndex) {
		// encode this categorical feature
		double[] arr = encodeFeatureForSingleDataPoint(categoricalFeatureValue, featureIndex);
		int currIndex = featureTypes.size();
		for(int i=0;i<arr.length;i++){
			featureValueList.add(arr[i]);
			featureTypes.add(true);
		}
	}

	private void setFeatureParent(int featureIndex, int parentIndex) {
		parentMap.put(featureIndex,parentIndex);
	}
	public Map<Integer, Integer> getFeatureMap() {
		return parentMap;
	}



	private double[] encodeFeatureForSingleDataPoint(String categoricalFeatureValue, int featureIndex) {
		double oneHot = 1;
		double oneCold = 0;
		// one-hot encode categorical features of data points.

		Map<String, Integer> featureMap = featureStats.getEncodedValues(featureIndex);
		int numberOfFeatureValues = featureMap.size();

		double [] arr = new double[numberOfFeatureValues];

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
		private Map<Integer, TreeMap<String, Integer>> oneHotFeatureEncodings;

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

		public void saveEncoding(int featureIndex, TreeMap<String, Integer> fEncoder) {
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
			return !oneHotFeatureEncodings.isEmpty() &&
					oneHotFeatureEncodings.containsKey(featureIndex);
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


		public Set<Integer> getUsedFeatures() {
			TreeSet<Integer> integers = new TreeSet<>(oneHotFeatureEncodings.keySet());
			for(int i =0;i<isContinuousFeature.length;i++){
				if(isContinuousFeature[i])
					integers.add(i);
			}
			return integers;
		}
	}

}