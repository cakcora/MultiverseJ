package mlcoreTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import core.DataPoint;
import mlcore.DecisionTreeLearner;

/**
 * Tests functions of Decision Tree learner object.
 * 
 * @author Huseyincan Kaynak
 */
@Testable
class DecisionTreeLearnerTests {

	private DataPoint polynomial(double x1) {
		double y = (x1 - 2) * (x1 + 2);
		var dataPoint = new DataPoint(new double[]{x1}, mapValue(y));
		dataPoint.setFeatureTypes(new boolean[] {false});
		return dataPoint;
	}
	
	private DataPoint polynomial2(double x1) {
		double y = x1 * Math.sin(x1);
		var dataPoint = new DataPoint(new double[]{x1}, mapValue(y));
		dataPoint.setFeatureTypes(new boolean[] {false});
		return dataPoint;
	}

	private double mapValue(double value) {
		if (value > 0) {
			return 1.0d;
		}
		else {
			return 0.0d;
		}
	}
	
	@Test
	void dataPointWithBasicCategorical() {
		var dataPoints = new ArrayList<DataPoint>();
		dataPoints.add(new DataPoint(new double[] { 1.0d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.0d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.0d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.0d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 0.0d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 0.0d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 0.0d }, 0.0d));
		for (int i = 0; i < dataPoints.size(); i++) {
			dataPoints.get(i).setFeatureTypes(new boolean[] {true});
		}

		var learner = new DecisionTreeLearner(5, 1);
		var tree = learner.train(dataPoints);;
		assertEquals(0.0d, tree.predict(new double[] { 0.0d }));
		assertEquals(1.0d, tree.predict(new double[] { 1.0d }));
		
	}
	
	@Test
	void dataPointwithBasicnotCategorical() {
		var dataPoints = new ArrayList<DataPoint>();
		dataPoints.add(new DataPoint(new double[] { 1.2d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 0.9d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 0.7d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 1.7d }, 1.0d));
		dataPoints.add(new DataPoint(new double[] { 5.9d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 2.2d }, 0.0d));
		dataPoints.add(new DataPoint(new double[] { 2.5d }, 0.0d));
		
		for (int i = 0; i < dataPoints.size(); i++) {
			dataPoints.get(i).setFeatureTypes(new boolean[] {false});
		}
		
		var learner = new DecisionTreeLearner(5,1);
		var tree = learner.train(dataPoints);
		assertEquals(1.0d, tree.predict(new double[] { 0.0d }));
		assertEquals(1.0d, tree.predict(new double[] { 1.0d }));
		assertEquals(0.0d, tree.predict(new double[] { 10.0d }));
		
	}
	
	@Test
	@DisplayName("x^2 - 4")
	void dataPointWithPolynomial() {
		var dataPoints = new ArrayList<DataPoint>();
		double rangeMin = -100.0d;
		double rangeMax = 100.0d;
		double randomValue = 0.0d;
		Random r = new Random("Seed".hashCode());
		for (int i = 0; i < 1000; i++) {
			randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
			dataPoints.add(polynomial(randomValue));
		}
		
		var learner = new DecisionTreeLearner(10,1);
		var tree = learner.train(dataPoints);
		assertEquals(0.0d, tree.predict(new double[] { -1.0d }));
		assertEquals(1.0d, tree.predict(new double[] { 15.0d }));
	}
	
	@Test
	@DisplayName("x * sin(x)")
	void dataPointWithPolynomial2() {
		var dataPoints = new ArrayList<DataPoint>();
		double rangeMin = -100.0d;
		double rangeMax = 100.0d;
		double randomValue = 0.0d;
		Random r = new Random("Seed".hashCode());
		for (int i = 0; i < 10000; i++) {
			randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
			dataPoints.add(polynomial2(randomValue));
		}
		
		var learner = new DecisionTreeLearner(100,1);
		var tree = learner.train(dataPoints);
		System.out.println(tree.getSize());
		assertEquals(1.0d, tree.predict(new double[] { 13.34d }));
		assertEquals(0.0d, tree.predict(new double[] { 17.28d }));
		assertEquals(0.0d, tree.predict(new double[] { 4.85d }));
		assertEquals(1.0d, tree.predict(new double[] { -14.05d }));
		assertEquals(0.0d, tree.predict(new double[] { -18.25d }));
		assertEquals(0.0d, tree.predict(new double[] { -99.25d }));
		
	}

	// TODO(HuseyinCan): Add Test for Decision Tree Learner.
	// Create Real Life True Model that only test function knows.
	// True model could be polynomial function that takes 3-4 parameter.
	// Then, create a policy that maps result of polynomial function to 0 or 1 
	// (binary classification value.). Then Generate some features and labels from
	// true for model for training.
	// Once you create your training data. Call DecisionTree Learner
	// to learn a Tree. Then create test data from same true model.
	// Finally assert correctness of test data with small epsilon.
}
