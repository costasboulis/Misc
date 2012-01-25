package com.goldenDeal.goldenDeals.dealEstimator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;

import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.Reader;

public class CategoryDependentLinearRegressionModel extends Classifier {
	private Logger logger = LoggerFactory.getLogger(getClass());
	static final long serialVersionUID = -3364580862046573748L;
	static final double CATEGORY_THRESHOLD = 5.0;
	static final String PREFIX = "CategoryBinary";
	private static final String TMP_PREFIX = "c:\\code\\dealEstimator\\tmp_ARFF_";
	private LinearRegression[] models = new LinearRegression[FeatureBuilder.categoryNames.length];
	private LinearRegression backOffModel;
	
	/**
	 * Write an ARFF file for each category (minus the CategoryBinary variables)
	 */
	public void buildClassifier(Instances instances) throws Exception {
		backOffModel = new LinearRegression();
		backOffModel.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
		backOffModel.setRidge(0.0);
		backOffModel.setEliminateColinearAttributes(false);
		for (int i = 0; i < models.length; i ++) {
			models[i] = new LinearRegression();
			models[i].setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
			models[i].setRidge(0.0);
			models[i].setEliminateColinearAttributes(false);
		}
		PrintWriter[] categoryARFF = new PrintWriter[models.length];
		for (int i = 0; i < models.length; i++) {
			File file = new File(TMP_PREFIX + i + ".txt");
			categoryARFF[i] = new PrintWriter(new FileWriter(file));
			writeHeader(categoryARFF[i], instances.instance(0));
		}
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			for (int j = 0; j < instance.numAttributes(); j++) {
				Attribute att = instance.attribute(j);
				if (att.name().startsWith(PREFIX)) {
					double val = instance.value(att);
					if (val == 1.0) { 
						int cat = Integer.parseInt(att.name().substring(PREFIX.length()));
						writeInstance(categoryARFF[cat], instance);
						break;
					}
				}
			}
		}
		for (int i = 0; i < models.length; i++) {
			categoryARFF[i].close();
		}
		
		try {
			backOffModel.buildClassifier(instances);
		}
		catch (Exception e) {
			logger.error("Cannot train backOff model");
			System.exit(-1);
		}
		
		// Now train a separate linear regression model for each category
		// Remove variables with single value using instances[i].numDistinctValues(j) == 1
		for (int i = 0 ; i < models.length; i ++) {
			File file = new File(TMP_PREFIX + i + ".txt");
			try {
				Reader reader = new FileReader(file);
				Instances categoryInstances = new Instances(reader); 
				if (categoryInstances.numInstances() <= CATEGORY_THRESHOLD) {
					logger.warn("Not enough data for category " + i + " using backOff model");
					models[i] = backOffModel;
					reader.close();
					continue;
				}
				for (int k = 0; k < categoryInstances.numAttributes(); k ++) {
					if (categoryInstances.numDistinctValues(k) == 1) {
						logger.warn("Single distinct value for data set " + FeatureBuilder.categoryNames[i] + 
								" attribute " + categoryInstances.attribute(k).toString());
					}
				}
				categoryInstances.setClassIndex(categoryInstances.numAttributes() - 1);
				try {
					logger.info("Training model with " + categoryInstances.numInstances() + " instances " + categoryInstances.numAttributes() + " attributes");
					models[i].buildClassifier(categoryInstances);
				}
				catch (Exception e) {
					logger.warn("Cannot train model for category " + i + " using backoff");
					models[i] = backOffModel;
				}
				reader.close();
			}
			catch (IOException ex) {
				logger.error("Could not read from file " + file.getAbsolutePath());
				System.exit(-1);
			}
		}
		
		// Delete the category ARFF files
		for (int i = 0 ; i < models.length; i++) {
			File file = new File(TMP_PREFIX + i + ".txt");
			if (! file.delete()) {
				logger.warn("File " + file.getAbsolutePath() + " could not be deleted");
			}
		}
	}
	
	private void writeHeader(PrintWriter pr, Instance instance) {
		StringBuffer sb = new StringBuffer();
		sb.append("@RELATION RevenuePerDay" + FeatureBuilder.newline + FeatureBuilder.newline);
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
		for (int j = 0; j < instance.numAttributes(); j++) {
			Attribute att = instance.attribute(j);
			if (att.name().startsWith(PREFIX)) {
				double val = instance.value(att);
				if (val == 1.0) { 
					int cat = Integer.parseInt(att.name().substring(PREFIX.length()));
					return models[cat].classifyInstance(instance);
				}
			}
		}
		logger.error("Could not find any category for instance " + instance.toString());
		return 0.0;
	}
}
