package Utils;

import core.DataPoint;
import core.Dataset;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

    public static void save(String filePath, Dataset dataset) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(filePath));
        String featureNames [] = dataset.getFeatureNames();
        for(String s:featureNames){
            wr.write(s+"\t");
        }
        wr.write("label\r\n");
        for(DataPoint dp:dataset.getDatapoints()){
            for(double f:dp.getFeatures()){
                wr.write(f+"\t");
            }
            wr.write(dp.getLabel()+"\r\n");
        }
        wr.close();
    }
}
