package com.goldenDeal.goldenDeals.dealEstimator;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.SelectedTag;
import weka.core.Instances;
import weka.core.Instance;

public class NonNegativeLinearRegression extends Classifier{
	private LinearRegression model;
	static final long serialVersionUID = -3364580862046573748L;
	
	
	public void buildClassifier(Instances instances) throws Exception{
		model = new LinearRegression();
		model.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
		model.setRidge(0.0);
		model.setEliminateColinearAttributes(false);
		
		model.buildClassifier(instances);
	}

	public double classifyInstance(Instance instance) throws Exception {
		double value = model.classifyInstance(instance);
//		System.out.println(value + " " + instance.classValue());
		return value < 0.0 ? 0.0 : value;
	}
}
