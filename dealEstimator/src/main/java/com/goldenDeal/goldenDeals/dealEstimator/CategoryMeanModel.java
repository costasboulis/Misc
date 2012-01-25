package com.goldenDeal.goldenDeals.dealEstimator;


import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;


public class CategoryMeanModel extends Classifier {
	static final long serialVersionUID = -3364580862046573743L;
	static final double THRESHOLD = 2.0;
	double[] categoryMean = new double[FeatureBuilder.categoryNames.length];
	double[] categoryCount = new double[FeatureBuilder.categoryNames.length];
	double overallMean;
	double overallCount;
	
	public void buildClassifier(Instances instances) throws Exception {
		for (int i = 0; i < categoryMean.length; i ++) {
			categoryMean[i] = 0.0;
			categoryCount[i] = 0.0;
		}
		overallMean = 0.0;
		overallCount = 0.0;
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			Attribute attClass = instance.attribute(instance.numAttributes() - 1);
			double val = instance.value(attClass);
			overallMean += val;
			overallCount += 1.0;
			for (int j = 0; j < instance.numAttributes(); j++) {
				Attribute att = instance.attribute(j);
				for (int k = 1; k < categoryMean.length; k++) {
					String categoryName = "CategoryBinary" + Integer.toString(k);
					if (att.name().equals(categoryName) && att.isNumeric()) {
						double v = instance.value(attClass);
						double indicator = instance.value(att);
						if (indicator == 1.0) {
							categoryMean[k] += v;
							categoryCount[k] += 1.0;
						}
					}
				}
				
			}
		}
		for (int k = 0 ; k < categoryMean.length; k++) {
			categoryMean[k] = categoryCount[k] < THRESHOLD ? overallMean / overallCount : categoryMean[k] / categoryCount[k];
		}
	}
	
	public double classifyInstance(Instance instance) throws Exception {
		for (int j = 0; j < instance.numAttributes(); j++) {
			Attribute att = instance.attribute(j);
			for (int k = 1; k < categoryMean.length; k++) {
				String categoryName = "CategoryBinary" + Integer.toString(k);
				if (att.name().equals(categoryName) && att.isNumeric()) {
					double v = instance.value(att);
					if (v == 1.0) {
						return categoryMean[k];
					}
				}
			}
			
		}
		return 0.0;
	}
}
