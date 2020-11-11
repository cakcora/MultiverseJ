package mlcoreTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import core.DataPoint;
import mlcore.ContinuousFeatureSplitter;

/**
 * Tests functions of Continuous Feature Splitter.
 * 
 * @author Murat Ali Bayir
 */
@Testable
public class ContinuousFeatureSplitterTests {

	@Test
	@DisplayName("Typical Split")
	void testDataWithOneFeatureValues() {

		var dataPoints = new ArrayList<DataPoint>();
		dataPoints.add(new DataPoint(new double[] { 1.2d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 0.9d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 0.7d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.7d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 5.9d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 2.2d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 2.5d }, 0.0d));

		var splitter = new ContinuousFeatureSplitter(1);
		var result = splitter.findBestSplit(0, dataPoints);
		assertNotNull(result);
		assertEquals(1.7d, result.getPivot(), Math.pow(10, -9));
	}

	@Test
	@DisplayName("Cluster Split")
	void testDataWithOneFeatureValuesCluster() {

		var dataPoints = new ArrayList<DataPoint>();
		dataPoints.add(new DataPoint(new double[] { 1.2d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.2d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.2d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.2d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 5.9d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 5.9d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 5.9d }, 0.0d));

		var splitter = new ContinuousFeatureSplitter(1);
		var result = splitter.findBestSplit(0, dataPoints);
		assertNotNull(result);
		assertEquals(1.2d, result.getPivot(), Math.pow(10, -9));
	}

	@Test
	@DisplayName("Odd One Out")
	void testDataOddOneOut() {

		var dataPoints = new ArrayList<DataPoint>();
		dataPoints.add(new DataPoint(new double[] { 1.2d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.3d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.4d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.5d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.6d }, 0.0d));

		var splitter = new ContinuousFeatureSplitter(1);
		var result = splitter.findBestSplit(0, dataPoints);
		assertNotNull(result);
		assertEquals(1.5d, result.getPivot(), Math.pow(10, -9));
	}

}
