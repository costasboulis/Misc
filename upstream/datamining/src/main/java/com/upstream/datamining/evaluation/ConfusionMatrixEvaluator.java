package com.upstream.datamining.evaluation;

public class ConfusionMatrixEvaluator extends Evaluator{
	public int[][] calculateConfusionMatrix() {
		int[][] confMatrix = new int[2][2];
		int hyp=0,ref=0;
		for (TestDataPoint tdp : super.testData) {
			if (tdp.getProb() >= 0.5f) {
				hyp = 1;
			}
			else {
				hyp = 0;
			}
			
			ref = tdp.isClass() == true ? 1 : 0;
			
			confMatrix[hyp][ref] ++;
		}
		return confMatrix;
	}
}
