package Utils;

import core.DataPoint;

import java.util.Collection;

public class Utils {
    public static <T> double getAverage(Collection<T> myList) {
        double avgVal = 0d;
        int valCount = myList.size();
        for (T b : myList) {
            avgVal += (Double) b;
        }
        return avgVal / valCount;
    }
}
