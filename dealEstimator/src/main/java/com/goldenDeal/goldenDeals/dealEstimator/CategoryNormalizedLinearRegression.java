package com.goldenDeal.goldenDeals.dealEstimator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;

/**
 * 1) Calculate mean per category
 * 2) Remove mean for each category
 * 3) Train a single linear regression model
 * @author kboulis
 *
 */
public class CategoryNormalizedLinearRegression extends Classifier {
	private Logger logger = LoggerFactory.getLogger(getClass());
	static final long serialVersionUID = -3364580862046573746L;
	static final double THRESHOLD = 2.0;
	static final String PREFIX = "CategoryBinary";
	private static final String TMP_PREFIX = "c:\\code\\dealEstimator\\normalized.txt";
	private double[] categoryMean;
	private double[] categoryCount;
	private double overallMean;
	private double overallCount;
	private int numCategories;
	private LinearRegression normalizedModel = new LinearRegression();
	
	public void buildClassifier(Instances instances) throws Exception {
		// Find the number of categories by looking in the data
		numCategories = 0;
		Instance instance = instances.instance(0);
		for (int j = 0; j < instance.numAttributes(); j++) {
			Attribute att = instance.attribute(j);
			if (att.name().startsWith(PREFIX)) {
				numCategories ++;
			}
		}
		if (numCategories <= 0) {
			logger.error("Found " + numCategories);
			System.exit(-1);
			
		}
		categoryMean = new double[numCategories];
		categoryCount = new double[numCategories];
		
		// Estimate mean per category
		for (int i = 0; i < categoryMean.length; i ++) {
			categoryMean[i] = 0.0;
			categoryCount[i] = 0.0;
		}
		overallMean = 0.0;
		overallCount = 0.0;
		for (int i = 0; i < instances.numInstances(); i++) {
			instance = instances.instance(i);
			Attribute attClass = instance.attribute(instance.numAttributes() - 1);
			double val = instance.value(attClass);
			overallMean += val;
			overallCount += 1.0;
			for (int j = 0; j < instance.numAttributes(); j++) {
				Attribute att = instance.attribute(j);
				for (int k = 0; k < categoryMean.length; k++) {
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
		
		// Subtract the category mean in data
		File file = new File(TMP_PREFIX);
		PrintWriter normalizedARFF = new PrintWriter(new FileWriter(file));
		writeHeader(normalizedARFF, instances.instance(0));
		for (int i = 0; i < instances.numInstances(); i++) {
			instance = instances.instance(i);
			Attribute attClass = instance.attribute(instance.numAttributes() - 1);
			double val = instance.value(attClass);
			for (int j = 0; j < instance.numAttributes(); j++) {
				Attribute att = instance.attribute(j);
				for (int k = 0; k < categoryMean.length; k++) {
					String categoryName = PREFIX + Integer.toString(k);
					if (att.name().equals(categoryName) && att.isNumeric()) {
						double indicator = instance.value(att);
						if (indicator == 1.0) {
							instance.setValue(attClass, val - categoryMean[k]);
							writeInstance(normalizedARFF, instance);
							break;
						}
					}
				}
				
			}
		}
		normalizedARFF.close();
		
		
		// Estimate linear regression model in the normalized model
		Instances normalizedInstances = null;
		try {
			Reader reader = new FileReader(file);
			normalizedInstances = new Instances(reader); 
		}
		catch (IOException ex) {
			logger.error("Could not read from file " + file.getAbsolutePath());
			System.exit(-1);
		}
		
		normalizedModel.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
		normalizedModel.setRidge(0.0);
		normalizedModel.setEliminateColinearAttributes(false);
		normalizedModel.buildClassifier(normalizedInstances);
		
//		if (! file.delete()) {
//			logger.warn("File " + file.getAbsolutePath() + " could not be deleted");
//		}
		
	}
	
	private void writeHeader(PrintWriter pr, Instance instance) {
		StringBuffer sb = new StringBuffer();
		sb.append("@RELATION NormalizedRevenuePerDay" + FeatureBuilder.newline + FeatureBuilder.newline);
		for (int j = 0; j < instance.numAttributes(); j++) {
			Attribute att = instance.attribute(j);
			if (att.name().startsWith(PREFIX)) {
				continue;
			}
			sb.append("@ATTRIBUTE "); sb.append(att.name()); sb.append(" ");
			if (att.isNumeric()) {
				sb.append("NUMERIC" + FeatureBuilder.newline);
			}
		}
		sb.append(FeatureBuilder.newline + "@DATA" + FeatureBuilder.newline);
		pr.print(sb.toString());
		pr.flush();
	}
	
	private void writeInstance(PrintWriter pr, Instance instance) {
		StringBuffer sb = new StringBuffer();
		Attribute att = instance.attribute(0);
		double val = instance.value(att);
		sb.append(val);
		for (int j = 1; j < instance.numAttributes(); j++) {
			att = instance.attribute(j);
			if (att.name().startsWith(PREFIX)) {
				continue;
			}
			val = instance.value(att);
			sb.append("," + val);
		}
		sb.append(FeatureBuilder.newline);
		pr.print(sb.toString());
		pr.flush();
	}
	
	public double classifyInstance(Instance instance) throws Exception {
		int cat = -1;
		Instance normalizedInstance = new Instance(instance);
		for (int j = 0; j < instance.numAttributes()-1; j++) {
			Attribute att = instance.attribute(j);
			if (att.name().startsWith(PREFIX)) {
				double val = instance.value(j);
				if (val == 1.0) { 
					cat = Integer.parseInt(att.name().substring(PREFIX.length()));
				}
				normalizedInstance.deleteAttributeAt(j);
			}
		}
		if (cat == -1){
			logger.error("Could not detect category for instance " + instance.toString());
			System.exit(-1);
		}
		double cv = normalizedInstance.classValue();
		normalizedInstance.setClassValue(cv - categoryMean[cat]);
		double v = normalizedModel.classifyInstance(normalizedInstance) + categoryMean[cat];
		return v < 0.0 ? 0.0 : v;
	}
}
