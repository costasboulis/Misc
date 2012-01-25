package com.upstream.datamining.evaluation;

public class TestDataPoint {
	private float prob;
	private boolean isClass;
	
	public TestDataPoint(float pr, boolean b) {
		this.prob = pr;
		this.isClass = b;
	}
	
	public float getProb() {
		return this.prob;
	}
	
	public boolean isClass() {
		return this.isClass;
	}
}
