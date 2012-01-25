package com.upstream.datamining.evaluation;


import java.util.List;
import java.util.LinkedList;

public class PrecisionRecallEvaluator extends Evaluator{
	
	
	public List<PrecisionRecallPoint> createGraph() {
		List<PrecisionRecallPoint> prpList = new LinkedList<PrecisionRecallPoint>();
		float[] thresholdValues = {0.0f, 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.1f, 0.15f, 0.20f, 0.25f, 0.30f, 0.35f, 0.40f, 0.45f, 0.50f, 0.55f, 0.60f, 0.65f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1.0f};
		
		int hyp = 0;
		int ref = 0;
		int[][] conf = new int[2][2];
		for (float threshold : thresholdValues) {
			conf[0][0] = 0; conf[0][1] = 0; conf[1][0] = 0; conf[1][1] = 0;
			int numBulkMTSent = 0;
			for (TestDataPoint tdp : super.testData) {
				if (tdp.getProb() >= threshold) {
					hyp = 1;
					
					numBulkMTSent ++;
				}
				else {
					hyp = 0;
				}
				
				ref = tdp.isClass() == true ? 1 : 0;
				
				conf[hyp][ref] ++;
			}
			float precision = conf[1][0] + conf[1][1] > 0 ? (float)conf[1][1] / (float)(conf[1][0] + conf[1][1]) : 0.0f;
			float recall = conf[0][1] + conf[1][1] > 0 ? (float)conf[1][1] / (float)(conf[0][1] + conf[1][1]) : 0.0f;
			float roundedPrecision = Math.round(precision * 1000.0f) / 10.0f;
			float roundedRecall = Math.round(recall * 1000.0f) / 10.0f;
			PrecisionRecallPoint prp = new PrecisionRecallPoint(roundedPrecision, roundedRecall, threshold, numBulkMTSent);
//			System.out.println(prp.toString());
			prpList.add(prp);
		}
		return prpList;
		
	}
	
	
}
