package com.upstream.datamining.features;


public class NumericFeature extends Feature{
	private float f;
	
	public NumericFeature(float f) {
		super.type = FeatureType.NUMERIC;
		this.f = f;
		super.name = null;
	}
	
	public NumericFeature(float f, String name) {
		super.type = FeatureType.NUMERIC;
		this.f = f;
		super.name = name;
	}
	
	public String getType() {
		return super.type.toString();
	}
	
	public String getValue() {
		return Float.toString(this.f);
	}
}
