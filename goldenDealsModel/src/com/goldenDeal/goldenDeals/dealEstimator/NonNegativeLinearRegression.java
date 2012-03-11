package com.goldenDeal.goldenDeals.dealEstimator;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.SelectedTag;
import weka.core.Instances;
import weka.core.Instance;

public class NonNegativeLinearRegression extends Classifier{
	private LinearRegression model;
	private double minimum;
	private double maximum;
	static final long serialVersionUID = -3364580862046573748L;
	
	
	public void buildClassifier(Instances instances) throws Exception{
		model.buildClassifier(instances);
	}

	public void setRegressor(LinearRegression rmodel) {
		this.model = rmodel;
	}
	
	public void setMinimum(double min) {
		this.minimum = min;
	}
	
	public void setMaximum(double max) {
		this.maximum = max;
	}
	
	public double classifyInstance(Instance instance) throws Exception {
		double value = model.classifyInstance(instance);
		return value < minimum ? minimum : value > maximum ? maximum : value;
	}
	
	public String toString() {
		return model.toString();
	}
}
