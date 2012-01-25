package com.goldenDeal.goldenDeals.dealEstimator;

//import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.Instance;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ZeroR;
import weka.core.SelectedTag;


public class ModelBuilder {
	 private final Logger logger = LoggerFactory.getLogger(getClass());
	 private CategoryNormalizedLinearRegression model;
	 
	 
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
	     
		 eval.crossValidateModel(new CategoryNormalizedLinearRegression(), instances, instances.numInstances(), new java.util.Random());
		 return eval.rootMeanSquaredError();
	 }
	 
	 
	 public void trainModel(File trainingData)  {
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

	     model = new CategoryNormalizedLinearRegression();
	     /*	     
		 model = new LinearRegression();
		 model.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
		 model.setRidge(0.0);
		 model.setEliminateColinearAttributes(false);
		 */
		 try {
			 model.buildClassifier(instances);
		 }
		 catch (Exception ex) {
			 logger.error("Cannot train model");
			 System.exit(-1);
		 }
	 }
	 
	 
	 
	 /**
	  * Outputs percentage of change in revenue between the two data points. Usually the difference between the two data points should be a single
	  * attribute, e.g. changing discount level from 60% to 80% but it can accommodate many changes as well
	  * 
	  * The first and the last line of the two 
	  * @param whatIfData
	  * @return
	  */
	 public double[] whatIf(File whatIfData) {
		 Instances instances = null;
	     try {
	         Reader reader = new FileReader(whatIfData);
	         instances = new Instances(reader); 
	         instances.setClassIndex(instances.numAttributes() - 1);
	         reader.close();
	     }
	     catch (IOException ex) {
	         logger.error("Could not read from file " + whatIfData.getAbsolutePath());
	         System.exit(-1);
	     }
	     Instance beforeInstance = instances.firstInstance();
	     double revenueBefore = 0.0;
		 try {
			 revenueBefore = model.classifyInstance(beforeInstance);
		 }
		 catch (Exception ex) {
			 logger.error("Cannot produce before revenue value");
			 System.exit(-1);
		 }
		 
		 
	     Instance afterInstance = instances.lastInstance();
		 double revenueAfter = 0.0;
		 try {
			 revenueAfter = model.classifyInstance(afterInstance);
		 }
		 catch (Exception ex) {
			 logger.error("Cannot produce after revenue value");
			 System.exit(-1);
		 }
		 double pct = 100.0 * (revenueAfter - revenueBefore) / revenueBefore;
		 
		 double[] ret = {pct, revenueBefore, revenueAfter};
		 return ret;
	 }
	 
	 public double getBaselineRMSE(File trainingData) throws Exception {
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
	     
		 eval.crossValidateModel(new ZeroR(), instances, instances.numInstances(), new java.util.Random());
		 return eval.rootMeanSquaredError();
	 }
	 
	 
	 
	 public static void main( String[] args ) {
		 ModelBuilder mb = new ModelBuilder();
		 try {
			 double val = mb.getRMSE(new File("c:\\code\\dealEstimator\\features.arff"));
			 System.out.println(val);
		 }
		 catch (Exception ex){
			 System.exit(-1);
		 }
		 
		 try {
			 double val = mb.getBaselineRMSE(new File("c:\\code\\dealEstimator\\features.arff"));
			 System.out.println(val);
		 }
		 catch (Exception ex){
			 System.exit(-1);
		 }
		 System.exit(-1);
		 
		 mb.trainModel(new File("c:\\code\\dealEstimator\\features.arff"));
		 
		// Spa that has a 80% discount moving it up to 80%
		 double[] ret = mb.whatIf(new File("c:\\code\\dealEstimator\\SpaDiscount80to90.txt"));
		 System.out.println("Spa from 80% discount to 90% discount -> " + ret[0] + " " + ret[1] + " " + ret[2]);
		 
		 
		// Restaurant - International that has 4 max coupons per person, moving it to Inf
		 ret = mb.whatIf(new File("c:\\code\\dealEstimator\\RestaurantIntMaxCouponsFrom2to4.txt"));
		 System.out.println("Restaurant-International from 2 coupons to 4 -> " + ret[0] + " " + ret[1] + " " + ret[2]);
		 	 
		// Restaurant - Greek removing time constraints 
		 ret = mb.whatIf(new File("c:\\code\\dealEstimator\\RestaurantGreekNoTimeConstraints.txt"));
		 System.out.println("Restaurant-Greek removing time constraints -> " + ret[0] + " " + ret[1] + " " + ret[2]);
				 
	 // Restaurant - Greek that has coupon duration for 30 days moving it to 90 days
				 
	 
				 	 
	 }
}
