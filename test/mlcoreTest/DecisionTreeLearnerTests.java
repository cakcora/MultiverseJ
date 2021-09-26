package mlcoreTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import core.DataPoint;
import core.MLContants;
import junit.framework.TestCase;
import metrics.MetricComputer;
import metrics.SingleEval;
import mlcore.DecisionTreeLearner;

/**
 * Tests functions of Decision Tree learner object.
 * 
 * @author Murat Ali Bayir.
 * @author Huseyincan Kaynak
 */
@Testable
class DecisionTreeLearnerTests extends TestCase {
	
	private MetricComputer computer;
	
	@BeforeEach
	public void setUp() {
		computer = new MetricComputer();
		System.out.println("===================");
		System.out.println("Start of test Case");
	}
	
	@AfterEach
	public void tearDown() {
		System.out.println("End of test Case");
		System.out.println("===================");
	}
	
	private List<DataPoint> generateIrisDataPoints() {
		
		var dataPoints = new ArrayList<DataPoint>();
		try {
		      File myObj = new File("sklearn/irisSampleTrain.txt");
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        if (!data.equals("")) {
		        	String[] dataLine = data.split(",");
			        var features = new double[4];
			        for (int i = 0; i < dataLine.length - 1; i++) {
			        	features[i] = Double.parseDouble(dataLine[i]);
			        }
			        var dataPoint = new DataPoint(features,Double.parseDouble(dataLine[dataLine.length - 1]));
			        dataPoint.setFeatureTypes(new boolean[] {false, false, false, false});
			        dataPoints.add(dataPoint);
		        }
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		return dataPoints;
	}
	

	//@Test
	void IrisDataset() {
		var dataPoints = generateIrisDataPoints();
		
		var learner = new DecisionTreeLearner(5,5, new int[] {0, 1, 2, 3});
		var tree = learner.train(dataPoints);
	}
	
	
	@Test
	@DisplayName("F(x) = (sin(x) + 0.5) >= 0 ? 1 : 0 ")
	void function1_Test() {		
		var components = new ArrayList<Component>();
		components.add(new Component(FunctionOperator.SINUS, 1.0d, 1));
		components.add(new Component(FunctionOperator.IDENTITY, 0.5, 1));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(components));
		var function = new MathFunction(1, allComponents);
		// Generate training and test data.
		Random random = new Random(20210121);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 0.0d, 1);
		var trainingData = generator.generate(function, 1000);
		var testData = generator.generate(function, 1000);
		generator.writeDataPointsToFile(trainingData, "func1_training");
		generator.writeDataPointsToFile(testData, "func1_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10, 10, new int[] {0});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		assertTrue(computer.computeBias(metrics) < 0.01d);
	}
	
	

	@Test
	@DisplayName("F(x) = (sin(x) + cos(y) + 0.5) >= 0 ? 1 : 0 ")
	void function2_Test() {		
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.SINUS, 1.0d, 1));
		firstComponent.add(new Component(FunctionOperator.IDENTITY, 0.5d, 1));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.COSINUS, 1.0d, 1));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent));
		var function = new MathFunction(2, allComponents);
		
		// Generate training and test data.
		Random random = new Random(20210124);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 1.0d, 2);
		var trainingData = generator.generate(function, 10000);
		var testData = generator.generate(function, 1000);
		generator.writeDataPointsToFile(trainingData, "func2_training");
		generator.writeDataPointsToFile(testData, "func2_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		assertTrue(computer.computeBias(metrics) < 0.015d);
	}

	@Test
	@DisplayName("F(x) = (x^2 + y^2) >= 1.0 ? 1 : 0 ")
	void function3_Test() 
	{
		// Build the function.
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.LINEAR, 1.0d, 2));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.LINEAR, 1.0d, 2));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent));
		var function = new MathFunction(2, allComponents);
		// Generate training and test data.
		Random random = new Random(20210124);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 1.0d, 2);
		var trainingData = generator.generate(function, 10000);
		var testData = generator.generate(function, 1000);
		generator.writeDataPointsToFile(trainingData, "func3_training");
		generator.writeDataPointsToFile(testData, "func3_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		assertTrue(computer.computeBias(metrics) < 0.01d);
	}
	
	@Test
	@DisplayName("F(x) = (x^2 + y^2) >= 0.0 ? 1 : 0 ")
	void function4_Test() 
	{
		// Build the function.
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.LINEAR, 1.0d, 2));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.LINEAR, 1.0d, 2));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent));
		var function = new MathFunction(2, allComponents);
		// Generate training and test data.
		Random random = new Random(20210123);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 0.0d, 2);
		var trainingData = generator.generate(function, 1000);
		var testData = generator.generate(function, 100);
		generator.writeDataPointsToFile(trainingData, "func4_training");
		generator.writeDataPointsToFile(testData, "func4_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		// %100 accuracy and %0.0 Bias
		assertEquals(0.0d, computer.computeBias(metrics), MLContants.PRECISE_EPSILON);
	}

	@Test
	@DisplayName("F(x) = (x^2 - y^2) >= 0.0 ? 1 : 0 ")
	void function5_Test() 
	{
		// Build the function.
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.LINEAR, 1.0d, 2));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.LINEAR, -1.0d, 2));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent));
		var function = new MathFunction(2, allComponents);
		// Generate training and test data.
		Random random = new Random(20210123);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 0.0d, 2);
		var trainingData = generator.generate(function, 1000);
		var testData = generator.generate(function, 100);
		generator.writeDataPointsToFile(trainingData, "func5_training");
		generator.writeDataPointsToFile(testData, "func5_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1});
		var tree = learner.train(trainingData);
		tree.Print();
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		// %100 accuracy and %0.0 Bias
		assertTrue(computer.computeBias(metrics) < 0.015d);
	}
	
	private void mutateArray(boolean[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			// Invert all values;
			values[i] = !values[i];
		}
	}
	
	@Test
	@DisplayName("Test Mutate Array")
	public void testArrayValues()
	{
		var shouldSplit = new boolean[] { false, false, true };
		System.out.println(Arrays.toString(shouldSplit));
		mutateArray(shouldSplit);
		System.out.println(Arrays.toString(shouldSplit));
		assertTrue(shouldSplit[0]);
		assertTrue(shouldSplit[1]);
		assertFalse(shouldSplit[2]);
	}
	
	@Test
	@DisplayName("F(x,y,z) = (sin(x) + cos(y) - sin^2(z)) >= 1.0 ? 1 : 0 ")
	void function6_Test() 
	{
		// Build the function.
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.SINUS, 1.0d, 1));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.COSINUS, 1.0d, 1));
		var thirdComponent = new ArrayList<Component>();
		thirdComponent.add(new Component(FunctionOperator.SINUS, -1.0d, 2));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent, thirdComponent));
		var function = new MathFunction(3, allComponents);
		// Generate training and test data.
		Random random = new Random(20210128);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 1.0d, 3);
		var trainingData = generator.generate(function, 30000);
		var testData = generator.generate(function, 1000);
		generator.writeDataPointsToFile(trainingData, "func6_training");
		generator.writeDataPointsToFile(testData, "func6_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1, 2});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Function 6");
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		assertTrue(computer.computeBias(metrics) < 0.015d);
	}
	
	@Test
	@DisplayName("F(x,y) = (sin^3(x) - cos^2(y) -sin(x) + 1/2y) >= 0.0 ? 1 : 0 ")
	void function7_Test() 
	{
		// Build the function.
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.SINUS, 1.0d, 3));
		firstComponent.add(new Component(FunctionOperator.SINUS, -1.0d, 1));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.COSINUS, -1.0d, 2));
		secondComponent.add(new Component(FunctionOperator.LINEAR, 0.5d, 1));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent));
		var function = new MathFunction(2, allComponents);
		// Generate training and test data.
		Random random = new Random(20210128);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 0.0d, 2);
		var trainingData = generator.generate(function, 10000);
		var testData = generator.generate(function, 1000);
		generator.writeDataPointsToFile(trainingData, "func7_training");
		generator.writeDataPointsToFile(testData, "func7_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Function 7");
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		assertTrue(computer.computeBias(metrics) < 0.015d);
	}
	
	@Test
	@DisplayName("F(x,y) = (-x^3 + 3y^2 -2) >= 0.0 ? 1 : 0 ")
	void function8_Test() 
	{
		// Build the function.
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.LINEAR, -1.0d, 3));
		firstComponent.add(new Component(FunctionOperator.IDENTITY, -2.0d, 1));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.LINEAR, 3.0d, 2));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent));
		var function = new MathFunction(2, allComponents);
		// Generate training and test data.
		Random random = new Random(20210128);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 0.0d, 2);
		var trainingData = generator.generate(function, 1000);
		var testData = generator.generate(function, 100);
		generator.writeDataPointsToFile(trainingData, "func8_training");
		generator.writeDataPointsToFile(testData, "func8_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Function 8");
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		assertTrue(computer.computeBias(metrics) < 0.015d);
	}
	
	@Test
	@DisplayName("F(x,y) = (e^x + sin(x) -cos(y)) >= 0.0 ? 1 : 0 ")
	void function9_Test() 
	{
		// Build the function.
		var firstComponent = new ArrayList<Component>();
		firstComponent.add(new Component(FunctionOperator.EXP, 1.0d, 1));
		firstComponent.add(new Component(FunctionOperator.SINUS, 1.0d, 1));
		var secondComponent = new ArrayList<Component>();
		secondComponent.add(new Component(FunctionOperator.COSINUS, -1.0d, 1));
		var allComponents = new ArrayList<List<Component>>(
				Arrays.asList(firstComponent, secondComponent));
		var function = new MathFunction(2, allComponents);
		// Generate training and test data.
		Random random = new Random(20210123);
		var generator = new DataPointGenerator(random, -2.0d, 2.0d, 0.0d, 2);
		var trainingData = generator.generate(function, 1000);
		var testData = generator.generate(function, 100);
		generator.writeDataPointsToFile(trainingData, "func9_training");
		generator.writeDataPointsToFile(testData, "func9_test");
		// Learn the model.
		var learner = new DecisionTreeLearner(10,10, new int[] {0, 1});
		var tree = learner.train(trainingData);
		// Compute the metrics.
		var metrics = new ArrayList<SingleEval>();
		for (int i = 0; i < testData.size(); i++) {
			var point = testData.get(i);
			var predicted = tree.predict(point.getFeatures());
			metrics.add(new SingleEval(predicted, point.getLabel()));
		}
		System.out.println("Function 9");
		System.out.println("Bias: " + computer.computeBias(metrics));
		System.out.println("Accuracy: " + (1.0d - computer.computeBias(metrics)));
		System.out.println("AUC: " + computer.computeAUC(metrics));
		System.out.println("Log-Loss: " + computer.computeLogLoss(metrics));
		assertTrue(computer.computeBias(metrics) < 0.015d);
	}
}
