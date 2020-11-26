package mlcoreTest;

import core.DataPoint;
import mlcore.DecisionTreeLearner;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

/**
 * Tests functions of Decision Tree learner object.
 * 
 * @author Huseyincan Kaynak
 */
@Testable
class DecisionTreeLearnerTests {

	/**
	 * Generate random data points For given data point size and feature size.
	 * 
	 * @param dataPointSize
	 * @param featureSize
	 * @return
	 */
	private List<DataPoint> generateDataPoints(int dataPointSize, int featureSize) {
		var random = new Random("Seed".hashCode());
		var dataPoints = new ArrayList<DataPoint>();
		for (int i = 0; i < dataPointSize; i++) {
			var featureArray = new double[featureSize];
			for (int j = 0; j < featureSize; j++) {
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
			learner.partition(data, 0, data.size() - 1, pivot, featureIndex);
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
			learner.partition(data, 0, data.size() - 1, data.get(pivot).getFeature(featureIndex), featureIndex);
		});
	}

	@Test
	void testPivotPosition() {
		List<DataPoint> data = generateDataPoints(1000, 100);
		List<DataPoint> dataSorted = data;
		var learner = new DecisionTreeLearner();
		var random = new Random("Seed".hashCode());
		var pivot = random.nextInt(1000);
		var pivotReference = data.get(pivot);
		var featureIndex = random.nextInt(100);
		var sortType = new Comparator<DataPoint>() {
			@Override
			public int compare(DataPoint dataLeft, DataPoint dataRight) {
				return Double.compare(dataLeft.getFeature(featureIndex), dataRight.getFeature(featureIndex));
			}
		};
		int pivotPositionActual = learner.partition(data, 0, data.size() - 1, data.get(pivot).getFeature(featureIndex),
				featureIndex);
		int pivotPositionExpected = 0;
		Collections.sort(dataSorted, sortType);
		for (int i = 0; i < dataSorted.size(); i++) {
			if (dataSorted.get(i) == pivotReference) {
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
		double[] features = new double[] { 0.5d };
		dataPoint.setFeatures(features);
		List<DataPoint> data = new ArrayList<DataPoint>();
		data.add(dataPoint);
		var learner = new DecisionTreeLearner();
		double pivot = 0.5d;
		int featureIndex = 0;
		int pivotPositionActual = learner.partition(data, 0, data.size() - 1, pivot, featureIndex);
		assertEquals(0, pivotPositionActual);
	}

	/**
	 * Tests left side of the pivot position smaller than pivot's value, right side
	 * of the pivot position larger than pivot's value.
	 */
	@Test
	void testSmallSizeData() {
		List<DataPoint> data = generateDataPoints(5, 10);
		var learner = new DecisionTreeLearner();
		var random = new Random("Seed".hashCode());
		int pivot = 3;
		var featureIndex = random.nextInt(5);
		int pivotPositionActual = learner.partition(data, 0, data.size() - 1, data.get(pivot).getFeature(featureIndex),
				featureIndex);
		for (int i = 0; i < data.size(); i++) {
			if (i < pivotPositionActual) {
				assertTrue(
						data.get(i).getFeature(featureIndex) <= data.get(pivotPositionActual).getFeature(featureIndex));
			} else if (i > pivotPositionActual) {
				assertTrue(
						data.get(i).getFeature(featureIndex) > data.get(pivotPositionActual).getFeature(featureIndex));
			}
		}
	}

	@Test
	void testSmallSizeDataRepeatPivot() {
		List<DataPoint> data = generateDataPoints(5, 10);
		var learner = new DecisionTreeLearner();
		var random = new Random("Seed".hashCode());
		int pivot = 3;
		var repeatPivot = new DataPoint(data.get(pivot).getFeatures());
		data.add(repeatPivot);
		var featureIndex = random.nextInt(5);
		int pivotPositionActual = learner.partition(data, 0, data.size() - 1, data.get(pivot).getFeature(featureIndex),
				featureIndex);
		for (int i = 0; i < data.size(); i++) {
			if (i < pivotPositionActual) {
				assertTrue(
						data.get(i).getFeature(featureIndex) <= data.get(pivotPositionActual).getFeature(featureIndex));
			} else if (i > pivotPositionActual) {
				assertTrue(
						data.get(i).getFeature(featureIndex) > data.get(pivotPositionActual).getFeature(featureIndex));
			}
		}

	}

	@Test
	void testRepeatedItems() {
		List<DataPoint> data = new ArrayList<DataPoint>();
		data.add(new DataPoint(new double[] { 0.7d }));
		data.add(new DataPoint(new double[] { 0.7d }));
		data.add(new DataPoint(new double[] { 0.7d }));
		data.add(new DataPoint(new double[] { 0.5d }));
		data.add(new DataPoint(new double[] { 0.5d }));
		data.add(new DataPoint(new double[] { 0.5d }));
		var learner = new DecisionTreeLearner();
		int featureIndex = 0;
		int pivotPosition = 1;
		int pivotPositionActual = learner.partition(data, 0, data.size() - 1,
				data.get(pivotPosition).getFeature(featureIndex), featureIndex);
		int pivotPositionExpected = 5;
		assertEquals(pivotPositionExpected, pivotPositionActual);
	}

	@Test
	void testOnlySameRepeatedItems() {
		List<DataPoint> data = new ArrayList<DataPoint>();
		data.add(new DataPoint(new double[] { 0.7d }));
		data.add(new DataPoint(new double[] { 0.7d }));
		data.add(new DataPoint(new double[] { 0.7d }));
		var learner = new DecisionTreeLearner();
		int featureIndex = 0;
		int pivotPosition = 1;
		int pivotPositionActual = learner.partition(data, 0, data.size() - 1,
				data.get(pivotPosition).getFeature(featureIndex), featureIndex);
		int pivotPositionExpected = 2;
		assertEquals(pivotPositionExpected, pivotPositionActual);
	}

	/**
	 * Helper function to call partition function with original arrayList
	 * and compare it with expected list for a given pivot and range.
	 */
	void runTestPartition(ArrayList<Double> original, ArrayList<Double> expected, double pivot,
			int starIndex, int endIndex)
	{
		var data = new ArrayList<DataPoint>();
		for (var point : original) {
			data.add(new DataPoint(new double[] { point }));
		}
		var learner = new DecisionTreeLearner();
		int featureIndex = 0;
		learner.partition(data, starIndex, endIndex, pivot, featureIndex);
		for (int i = 0; i < expected.size(); i++)
		{
			assertEquals(expected.get(i),
					data.get(i).getFeature(featureIndex));
		}
	}

	@Test
	void testSpecificArray_StartIndexAsStartOfList() {
		var testPoints = new ArrayList<Double>(
				Arrays.asList(10d, 5d, 3d, 1d, -3d, 9d, 6d, 17d));
		var expectedPoints = new ArrayList<Double>(
				Arrays.asList(1d, 3d, 5d, 10d, -3d, 9d, 6d, 17d));
		// Pivot = 5.0, startIndex = 0, endIndex = 3.
		runTestPartition(testPoints, expectedPoints, 5.0d, 0, 3);
	}
	
	@Test
	void testSpecificArray_StartAndEndIndexMiddle() {
		var testPoints = new ArrayList<Double>(
				Arrays.asList(10d, 5d, 3d, 1d, -3d, 9d, 6d, 17d));
		var expectedPoints = new ArrayList<Double>(
				Arrays.asList(10d, -3d, 1d, 5d, 3d, 9d, 6d, 17d));
		// Pivot = 1.0, startIndex = 1, endIndex = 4.
		runTestPartition(testPoints, expectedPoints, 1.0d, 1, 4);
	}
	
	@Test
	void testSpecificArray_TwoDuplicates() {
		var testPoints = new ArrayList<Double>(
				Arrays.asList(10d, -1d, -1d, -1d, 2d, 2d, 2d, 8d));
		var expectedPoints = new ArrayList<Double>(
				Arrays.asList(10d, -1d, -1d, -1d, 2d, 2d, 2d, 8d));
		// Pivot = -1.0, startIndex = 1, endIndex = 6.
		runTestPartition(testPoints, expectedPoints, -1.0d, 1, 6);
	}
	
	@Test
	void testSpecificArray_ThreeDuplicates() {
		var testPoints = new ArrayList<Double>(
				Arrays.asList(10d, -1d, -1d, 2d, 2d, -3d, -3d, 8d));
		var expectedPoints = new ArrayList<Double>(
				Arrays.asList(10d, -1d, -3d, -3d, -1d, 2d, 2d, 8d));
		// Pivot = -1.0, startIndex = 1, endIndex = 6.
		runTestPartition(testPoints, expectedPoints, -1.0d, 1, 6);
	}
}
