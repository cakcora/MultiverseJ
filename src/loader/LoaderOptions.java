package loader;

public class LoaderOptions {


	/*
	 * Used in one-hot encoding, if a categorical feature has more than
	 * ignoreThreshold unique values, we exclude the feature from the dataset
	 */
	public int featureIgnoreThresholdOnUniqueVals = 20;

	/*
	 * Column separator in the csv file
	 */
	private char separator = ',';

	private char quoter = '\\';

	/*
	A threshold to convert real-valued features to categorical.
	 */
	private int realToFactorThreshold;

	public char getQuoter() {
		return quoter;
	}

	public void setQuoter(char quoteChar) {
		quoter = quoteChar;
	}

	public char getSeparator() {
		return separator;
	}

	public void setSep(char sepChar) {
		separator = sepChar;
	}

	public void featureIgnoreThreshold(int ignoreAbove) {
		featureIgnoreThresholdOnUniqueVals = ignoreAbove;
	}

	public int getIgnoreThreshold() {
		return featureIgnoreThresholdOnUniqueVals;
	}

	/**
	 * Convert a continuous feature if it has less than factorThreshold unique values.
	 * @param factorThreshold  convert if the feature has this or less values
	 */
    public void convertRealToFactorThreshold(int factorThreshold) {
		realToFactorThreshold=factorThreshold;
    }

	public int getFactorThreshold() {
		return realToFactorThreshold;
	}
}
