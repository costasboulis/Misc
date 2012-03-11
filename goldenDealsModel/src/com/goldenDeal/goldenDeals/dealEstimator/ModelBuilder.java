package com.goldenDeal.goldenDeals.dealEstimator;

import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.Instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Evaluation;
import weka.core.SelectedTag;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;


public class ModelBuilder {
	 private final Logger logger = LoggerFactory.getLogger(getClass());
	 private NonNegativeLinearRegression model;
//	 private Classifier model;
	 private Instances instances;

	 
	 public double getRMSE(File trainingData) throws Exception {
		 Instances instances = null;
	     try {
	         Reader reader = new FileReader(trainingData);
	         instances = new Instances(reader); 
	         instances.setClassIndex(instances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + trainingData.getAbsolutePath());
	         System.exit(-1);
	     }
	     
		 Evaluation eval = new Evaluation(instances);
	     
		 NonNegativeLinearRegression m = new NonNegativeLinearRegression();
	     m.setMaximum(GoldenDealsNormalized.NUMBER_OF_QUANTILES);
	     m.setMinimum(1.0);
	     LinearRegression rmodel = new LinearRegression();
//	     rmodel.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
//	     rmodel.setRidge(0.0);
//	     rmodel.setEliminateColinearAttributes(false);
	     m.setRegressor(rmodel);
	     
		 eval.crossValidateModel(m, instances, 10, new java.util.Random());
		 System.out.println(eval.rootMeanPriorSquaredError());
		 return eval.rootMeanSquaredError();
	 }
	 
	 public void getRMSE(File trainingData, File testingData) throws Exception {
		 Instances trainInstances = null;
	     try {
	         Reader reader = new FileReader(trainingData);
	         trainInstances = new Instances(reader); 
	         trainInstances.setClassIndex(trainInstances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + trainingData.getAbsolutePath());
	         System.exit(-1);
	     }
	     
	     model = new NonNegativeLinearRegression();
	     model.setMaximum(GoldenDealsNormalized.NUMBER_OF_QUANTILES);
	     model.setMinimum(1.0);
	     
	     LinearRegression rmodel = new LinearRegression();
//	     rmodel.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
//	     rmodel.setRidge(0.0);
//	     rmodel.setEliminateColinearAttributes(false);
	     
	     model.setRegressor(rmodel);
		 
	     
	     Instances testInstances = null;
	     try {
	         Reader reader = new FileReader(testingData);
	         testInstances = new Instances(reader); 
	         testInstances.setClassIndex(testInstances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + testingData.getAbsolutePath());
	         System.exit(-1);
	     }
		 
		 
		 
		 Evaluation eval = new Evaluation(testInstances);
		 for (double d : eval.evaluateModel(model, testInstances)) {
			 System.out.println(d);
		 }
	 }
	 
	 public void trainModelNormalized(File trainingData, File testingData) {
		 Instances instances = null;
	     try {
	         Reader reader = new FileReader(trainingData);
	         instances = new Instances(reader); 
	         instances.setClassIndex(instances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + trainingData.getAbsolutePath());
	         System.exit(-1);
	     }
	     
	     model = new NonNegativeLinearRegression();
	     model.setMaximum(GoldenDealsNormalized.NUMBER_OF_QUANTILES);
	     model.setMinimum(1.0);
	     
	     LinearRegression rmodel = new LinearRegression();
//	     rmodel.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
//	     rmodel.setRidge(0.0);
//	     rmodel.setEliminateColinearAttributes(false);
	     
	     model.setRegressor(rmodel);
		 
		 try {
			 model.buildClassifier(instances);
		 }
		 catch (Exception ex) {
			 logger.error("Cannot train model");
			 System.exit(-1);
		 }
		 
		 instances = null;
	     try {
	         Reader reader = new FileReader(testingData);
	         instances = new Instances(reader); 
	         instances.setClassIndex(instances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + testingData.getAbsolutePath());
	         System.exit(-1);
	     }
		 
	     double avg = 0.0;
	     try {
	    	 for (int i = 0; i < instances.numInstances(); i ++) {
	    		 Instance instance = instances.instance(i);
	    		 double s = model.classifyInstance(instance);
	    		 if (s < 5.0) {
	    			 System.out.println("Est. : " + s + " " + instance.toString());
	    		 }
	    		 avg += s;
	    	 }
	     }
	     catch (Exception ex) {
			 logger.error("Cannot generate estimation");
			 System.exit(-1);
		 }
	     
	     avg /= (double)(instances.numInstances());
	     System.out.println(avg);
	 }
	 
	 
	 public void trainModelNormalized(File trainingData) {
		 this.instances = null;
	     try {
	         Reader reader = new FileReader(trainingData);
	         this.instances = new Instances(reader); 
	         this.instances.setClassIndex(this.instances.numAttributes() - 1);
//	         double[] monthAvg = this.instances.attributeToDoubleArray(this.instances.numAttributes() - 2);
//	         this.instances.deleteAttributeAt(this.instances.numAttributes() - 2);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + trainingData.getAbsolutePath());
	         System.exit(-1);
	     }
	     
	     model = new NonNegativeLinearRegression();
	     model.setMaximum(GoldenDealsNormalized.NUMBER_OF_QUANTILES);
	     model.setMinimum(1.0);
	     
	     LinearRegression rmodel = new LinearRegression();
//	     rmodel.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
//	     rmodel.setRidge(0.0);
//	     rmodel.setEliminateColinearAttributes(false);
	     
	     model.setRegressor(rmodel);
		 
		 try {
			 model.buildClassifier(this.instances);
		 }
		 catch (Exception ex) {
			 logger.error("Cannot train model");
			 System.exit(-1);
		 }
		 
		 System.out.println(model.toString());

	 }
	 
	 public void trainLogisticRegression(File trainingData) {
		 Instances instances = null;
	     try {
	         Reader reader = new FileReader(trainingData);
	         instances = new Instances(reader); 
	         instances.setClassIndex(instances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + trainingData.getAbsolutePath());
	         System.exit(-1);
	     }
	     
	     Classifier m = new Logistic();
	     
	     try {
			 m.buildClassifier(instances);
		 }
		 catch (Exception ex) {
			 logger.error("Cannot train model");
			 System.exit(-1);
		 }
		 
		 System.out.println(m.toString());
		 
		 try {
			 Evaluation eval = new Evaluation(instances);
			 eval.crossValidateModel(m, instances, 10, new java.util.Random());
			 System.out.println(eval.errorRate());
			 System.out.println(eval.meanPriorAbsoluteError());
		 }
		 catch (Exception ex) {
			 
		 }
		 
		 
	 }
	 
	 public void whatIf(File rankAndAmountData, File initialData, File whatIfData) {
		 if (!rankAndAmountData.exists()) {
	            logger.error("Cannot find file " + rankAndAmountData.getAbsolutePath());
	            System.exit(-1);
	        }
	        if (!rankAndAmountData.canRead()) {
	            logger.error("Cannot read file " + rankAndAmountData.getAbsolutePath());
	            System.exit(-1);
	        }
	        if (rankAndAmountData.isDirectory()) {
	            logger.error("Expecting file but encountered directory " + rankAndAmountData.getAbsolutePath());
	            System.exit(-1);
	        }
	        HashMap<Integer, Float> rankAndAmount = new HashMap<Integer, Float>();
	        String lineStr;
	        try {
	        	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rankAndAmountData)));
	        	while ((lineStr = br.readLine()) != null) {
					String[] fieldNames = lineStr.split(";");
					
					int rank = Integer.parseInt(fieldNames[0]);
					float amount = Float.parseFloat(fieldNames[1]);
					
					rankAndAmount.put(rank, amount);
				}
	        	br.close();
	        }
	        catch (Exception ex) {
	        	logger.error("Cannot read from file " + rankAndAmountData.getAbsolutePath());
	            System.exit(-1);
	        }
			
	            
		 Instances instances = null;
	     try {
	         Reader reader = new FileReader(initialData);
	         instances = new Instances(reader); 
	         instances.setClassIndex(instances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + initialData.getAbsolutePath());
	         System.exit(-1);
	     }
	     
	     
	     // Calculate model
	     model = new NonNegativeLinearRegression();
	     model.setMaximum(GoldenDealsNormalized.NUMBER_OF_QUANTILES);
	     model.setMinimum(1.0);
	     
	     LinearRegression rmodel = new LinearRegression();
//	     rmodel.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
//	     rmodel.setRidge(0.0);
//	     rmodel.setEliminateColinearAttributes(false);
	     
	     model.setRegressor(rmodel);
		 
		 try {
			 model.buildClassifier(instances);
		 }
		 catch (Exception ex) {
			 logger.error("Cannot train model");
			 System.exit(-1);
		 }
		 
		 // Read the whatIf instances
		 Instances whatIfInstances = null;
	     try {
	         Reader reader = new FileReader(whatIfData);
	         whatIfInstances = new Instances(reader); 
	         whatIfInstances.setClassIndex(whatIfInstances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + whatIfData.getAbsolutePath());
	         System.exit(-1);
	     }
	     if (whatIfInstances.numInstances() != instances.numInstances()) {
	    	 logger.error("Different number of instances between original and whatIf");
	    	 System.exit(-1);
	     }
		 
		 // Calculate rank by the model
	     double avgEstimatedInitialRank = 0.0;
	     double avgDiff = 0.0;
	     double totalInitialAmount = 0.0;
	     double totalFinalAmount = 0.0;
		 for (int i = 0 ; i < instances.numInstances(); i ++) {
			 try {
				 double estimatedInitialRank = model.classifyInstance(instances.instance(i));
				 int r = (int)Math.round(estimatedInitialRank);
				 double initialAmount = rankAndAmount.get(r < 1.0 ? 1 : r > 20 ? 20 : r);
				 totalInitialAmount += initialAmount;
				 
				 double estimatedFinalRank = model.classifyInstance(whatIfInstances.instance(i));
				 r = (int)Math.round(estimatedFinalRank);
				 double finalAmount = rankAndAmount.get(r < 1.0 ? 1 : r > 20 ? 20 : r);
				 totalFinalAmount += finalAmount;
				 
				 avgEstimatedInitialRank += estimatedInitialRank;
				 avgDiff += estimatedFinalRank - estimatedInitialRank;
			 }
			 catch (Exception ex) {
				 logger.error("Cannot produce estimate for instance " + instances.instance(i).toString());
				 System.exit(-1);
			 }
		 }
		 
		 avgDiff /= (double)instances.numInstances();
		 avgEstimatedInitialRank /= (double)instances.numInstances();
		 logger.info("Average estimated initial rank : " + avgEstimatedInitialRank);
		 logger.info("Average rank difference : " + avgDiff);
		 logger.info("Average relative rank difference : " + avgDiff / avgEstimatedInitialRank);
		 logger.info("Relative difference in amount per day : " + (totalFinalAmount - totalInitialAmount) / totalInitialAmount);
	 }
		
	 
	 
	 public static void main( String[] args ) {
		 String featuresFilename = "C:\\Users\\kboulis\\Desktop\\group buying sites\\features_binaryClassifier.arff";
		 ModelBuilder mb = new ModelBuilder();
//		 mb.trainModelNormalized(new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\features_NOT_Jan2011_GoldenDeals_Athens.arff"), 
//				 new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\features_Jan2011_GoldenDeals_Athens.arff"));
//		 mb.trainModelNormalized(new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\features_NOT_Feb2011_GoldenDeals_Athens.arff"), 
//				 new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\features_Feb2011_Groupon_Athens.arff"));
		 mb.trainLogisticRegression(new File(featuresFilename));
	
		 
		 mb.trainModelNormalized(new File(featuresFilename));
		 try {
			 double x = mb.getRMSE(new File(featuresFilename));
			 System.out.println(x);
		 }
		 catch (Exception ex) {
			 
		 }
		
		 ScenarioBuilder sb = new ScenarioBuilder();
		 File rankAmountData = new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\rank_and_amount_per_day.csv");
		 File whatIfData = new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\WhatIfFeatures.arff");
		 sb.scenarioB(new File(featuresFilename), whatIfData);
		 mb.whatIf(rankAmountData, new File(featuresFilename), whatIfData);
		 
		
	 }
}
