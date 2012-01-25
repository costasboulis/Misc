package com.upstream.datamining.evaluation;

/**
 * Returns the average accuracy on a set of data points
 * @author kboulis
 *
 */
public class AccuracyEvaluator extends Evaluator{

	/**
	 * Calculates the average accuracy on the test set
	 * @return
	 */
		public float calculateSoftAccuracy() {
			float error = 0.0f;
			for (TestDataPoint tdp : super.testData) {
				float ref = tdp.isClass() == true ? 1.0f : 0.0f;
				error += Math.abs(tdp.getProb() - ref);
			}
			return (1.0f - (error / (float)super.testData.size()));
		}
		
		/**
		 * Converts the probability to 0 or 1 based on the threshold of 0.5 and calculates accuracy
		 * @return
		 */
		public float calculateHardAccuracy() {
			float error = 0.0f;
			for (TestDataPoint tdp : super.testData) {
				float ref = tdp.isClass() == true ? 1.0f : 0.0f;
				float binaryScore = tdp.getProb() >= 0.5f ? 1.0f : 0.0f;
				error += Math.abs(binaryScore - ref);
			}
			return (1.0f - (error / (float)super.testData.size()));
		}
		
		/**
		 * Generates score of the trivial system that outputs NoMOSent for every user
		 * @return
		 */
		public float calculateBaselineScore() {
			float error = 0.0f;
			for (TestDataPoint tdp : super.testData) {
				float ref = tdp.isClass() == true ? 1.0f : 0.0f;
				error += ref;
			}
			return (1.0f - (error / (float)super.testData.size()));
		}
}
