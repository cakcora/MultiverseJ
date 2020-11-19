package loader;

public class LoaderOptions {

    /*
    Flag to mark a feature as categorical
     */
    public static final boolean CATEGORICAL = false;

    /*
        Flag to mark a feature as real valued
     */
    public static final boolean REAL_VALUED = true;

    /*
    Used in one-hot encoding, if a categorical feature has more than ignoreThreshold unique values,
    we exclude the feature from the dataset
     */
    public int featureIgnoreThresholdOnUniqueVals = 20;

    /*
        Column separator in the csv file
         */
    private char separator = ',';


    private char quoter = '\\';


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
}
