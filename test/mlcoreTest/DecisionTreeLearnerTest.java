package mlcoreTest;

import core.DataPoint;
import mlcore.DecisionTreeLearner;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;


/**
 * Tests functions of Decision Tree learner object.
 * 
 * @author Hüseyincan Kaynak
 */
@Testable
class DecisionTreeLearnerTest {

	/**
	 * Generate random data points
	 * For given data point size and 
	 * feature size.
	 * 
	 * @param dataPointSize
	 * @param featureSize
	 * @return
	 */
	@Test
	private List<DataPoint> generateDataPoints(int dataPointSize, int featureSize) 
	{
		var random = new Random("Seed".hashCode());
		var dataPoints = new ArrayList<DataPoint>();
		for (int i = 0; i < dataPointSize;  i++)
		{
			var featureArray = new double[featureSize];
			for (int j = 0; j < featureSize; j++) 
			{
				featureArray[j] = random.nextDouble();
			}
			var point = new DataPoint(featureArray);
			dataPoints.add(point);
		}
		return dataPoints;
	}
	
	
	
	@Test
	void testEmptyList() {
		List<DataPoint> data = generateDataPoints(0, 10);
		var learner = new DecisionTreeLearner();
		var random = new Random("Seed".hashCode());
		var pivot = random.nextDouble();
		var featureIndex = random.nextInt(10);
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
		    learner.partition(data, pivot, featureIndex);
		  });
	}
	
	
	@Test
	void testOutOfRangeFeatureIndex() {
		List<DataPoint> data = generateDataPoints(100, 10);
		var learner = new DecisionTreeLearner();
		var random = new Random("Seed".hashCode());
		var pivot = random.nextInt(100);
		var featureIndex = -1;
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
		    learner.partition(data, data.get(pivot).getFeature(featureIndex), featureIndex);
		  });
	}
	
	@Test
	void testPivotPosition() {
		List<DataPoint> data = generateDataPoints(1000, 100);
		List<DataPoint> dataSorted = data;
		var learner = new DecisionTreeLearner();
		var random = new Random("Seed".hashCode());
		var pivot = random.nextInt(1000);
		var featureIndex = random.nextInt(100);
		var sortType = new Comparator<DataPoint>() {
			@Override
			public int compare(DataPoint dataLeft, DataPoint dataRight) {
			      return Double.compare(dataLeft.getFeature(featureIndex), dataRight.getFeature(featureIndex));
			   }
		};
		int pivotPositionActual = learner.partition(data, data.get(pivot).getFeature(featureIndex), featureIndex);
		int pivotPositionExpected = 0;
		Collections.sort(dataSorted, sortType);
		for(int i = 0; i < dataSorted.size(); i++) {
			if (dataSorted.get(i).getFeature(featureIndex) == dataSorted.get(pivot).getFeature(featureIndex)) {
				pivotPositionExpected = i;
				break;
			}
		}
		assertEquals(pivotPositionExpected, pivotPositionActual);
	}
	
	/**
	 * Tests list contain one Data Point and it's feature value smaller than pivot.
	 */
	@Test
	void testOneItemList() {
		DataPoint dataPoint = new DataPoint();
		double[] features = new double[] {0.5d};
		dataPoint.setFeatures(features);
		List<DataPoint> data = new ArrayList<DataPoint>();
		data.add(dataPoint);
		var learner = new DecisionTreeLearner();
		double pivot = 1.5d;
		int featureIndex = 0;
		int pivotPositionActual = learner.partition(data, pivot, featureIndex);
		assertEquals(1, pivotPositionActual);
	}
	
	/**
	 * Tests left side of the pivot position smaller than pivot's value,
	 * right side of the pivot position larger than pivot's value.
	 */
	@Test
	void testSmallSizeData() {
		List<DataPoint> data = generateDataPoints(5, 10);
		var learner = new DecisionTreeLearner();
		var random = new Random("Seed".hashCode());
		int pivot = 3;
		var featureIndex = random.nextInt(5);
		int pivotPositionActual = learner.partition(data, data.get(pivot).getFeature(featureIndex), featureIndex);
		for(int i = 0; i < data.size(); i++) {
			if(i < pivotPositionActual) {
				assertTrue(data.get(i).getFeature(featureIndex) < data.get(pivotPositionActual).getFeature(featureIndex));
			}
			else if (i > pivotPositionActual) {
				assertTrue(data.get(i).getFeature(featureIndex) > data.get(pivotPositionActual).getFeature(featureIndex));
			}
		}
	}
}
