package com.upstream.datamining.evaluation;

public class PrecisionRecallPoint {
    private float precision;
    private float recall;
    private float threshold;
    private int numBulkMTSent;
    
    
    public PrecisionRecallPoint(float p, float r, float t, int n) {
        precision = p;
        recall = r;
        threshold = t;
        numBulkMTSent = n;
    }
    
    public float getPrecision() {
        return precision;
    }
    
    public float getRecall() {
        return recall;
    }
    
    public float getThreshold() {
    	return this.threshold;
    }
    
    public int getNumBulkMTSent() {
        return numBulkMTSent;
    }
    
    public String toString() {
  //      String s = "P: " + precision + " R: " + recall + " NumBulkMTs: " + numBulkMTSent + " Threshold: " + threshold;
    	String s = "P: " + precision + " R: " + recall + " Threshold: " + threshold;
    	return s;
    }
}
