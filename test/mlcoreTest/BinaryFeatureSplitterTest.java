package mlcoreTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import core.DataPoint;
import mlcore.BinaryFeatureSplitter;

/**
 * Tests Binary Feature Splitter class.
 * 
 * @author Murat Ali Bayir
 */
@Testable
class BinaryFeatureSplitterTest {

	
	private List<DataPoint> generateData()
	{
		var random = new Random("Seed".hashCode());
		var dataPoints = new ArrayList<DataPoint>();
		for (int i = 0; i < 100;  i++)
		{
			var isNegative = random.nextInt(100) < 50;
			var label = isNegative ? 0.0d : 1.0d;
			var point = new DataPoint();
			point.setLabel(label);
			if (isNegative)
			{
				var features = random.nextInt(100) < 90 ?
						new double[] {0.0d} : new double[] {1.0};
				point.setFeatures(features);
			} else {
				var features = random.nextInt(100) >= 90 ?
						new double[] {0.0d} : new double[] {1.0};
				point.setFeatures(features);
			}
			dataPoints.add(point);
		}
		return dataPoints;
	}

	@Test
	@DisplayName("Improve Entropy")
	void testDataWithTwoFeatureValues() {
		var dataPoints = generateData();
		var splitter = new BinaryFeatureSplitter(1);
		var result = splitter.FindBestSplit(0, dataPoints);
		assertNotNull(result);
		assertTrue(result.getInformationGain() > 0.0d);
	}
	
	@Test
	@DisplayName("One Label")
	void testDataWithOneLabel() {
		var random = new Random("Seed".hashCode());
		var dataPoints = new ArrayList<DataPoint>();
		
		for (int i = 0; i < 100;  i++)
		{
			var point = new DataPoint();
			point.setLabel(1.0d);
			var features = random.nextInt(100) < 90 ?
					new double[] {0.0d} : new double[] {1.0};
			point.setFeatures(features);
			dataPoints.add(point);
		}
		
		var splitter = new BinaryFeatureSplitter(1);
		var result = splitter.FindBestSplit(0, dataPoints);
		assertNull(result);
	}
	
	@Test
	@DisplayName("Capacity-Problem")
	void testNoSplitCapacity() {
		var dataPoints = generateData();
		var splitter = new BinaryFeatureSplitter(1000);
		var result = splitter.FindBestSplit(0, dataPoints);
		assertNull(result);
	}
}
