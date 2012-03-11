package com.goldenDeal.goldenDeals.dealEstimator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;
import weka.core.converters.ArffSaver;


public class ScenarioBuilder {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	public void scenarioA(File initialData, File whatIfData) {
		Random rand = new Random();
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
	     
	     for (int i = 0 ; i < instances.numInstances(); i ++) {
	    	 Instance instance = instances.instance(i);
	    	 for (int att = 0 ; att < instance.numAttributes(); att ++) {
	    		 Attribute attribute = instance.attribute(att);
	    		 
	    		 String name = attribute.name();
	    		 
	    		 if (name.equals("Discount")) {   // Increase discount by an average 10%, e.g. 50% -> 55%, 90% -> 91%
	    			 double v = instance.value(attribute);
	    			 double newValue = (1.0 - v) * 0.1 + v;
	    			 instance.setValue(attribute, newValue);
	    		 }
	    		 else if (name.equals("PriceAfterDiscount")) { // Increase price after discount by 10% by offering combo deals
	    			 double v = instance.value(attribute);
	    			 double newValue = v * 1.1;
	    			 instance.setValue(attribute, newValue);
	    		 }
	    		 else if (name.equals("HasMoreToPay")) {  // Cancel HasMoreToPay deals
	    			 double v = instance.value(attribute);
	    			 double newValue = 0.0;
	    			 if (v == 1.0) {
	    				 newValue = 0.0;
	    			 }
	    			 instance.setValue(attribute, newValue);
	    		 }
	    		 else if (name.equals("MultipleStores")) { // Increase by 10% the stores with multiple locations
	    			 double v = instance.value(attribute); 
	    			 double newValue = v;
	    			 if (v == 0.0) {
	    				 if (rand.nextFloat() <= 0.1f) {  
	    					 newValue = 1.0;
	    				 }
	    			 }
	    			 instance.setValue(attribute, newValue);
	    		 }
	    		 else if (name.equals("DealDuration")) {  // Decrease deal duration by one day for 30% of deals with 3 or more days
	    			 double v = instance.value(attribute); 
	    			 double newValue = v;
	    			 if (v >= 3.0 && rand.nextFloat() <= 0.3f) {  
	    				 newValue = v - 1.0f;
	    			 }
	    			 instance.setValue(attribute, newValue);
	    		 }
	    		 else if (name.equals("DaysForActivationQuantile5")) { // 50% decrease in deals that are redeemable in the last quantile
	    			 double v = instance.value(attribute); 
	    			 if (v == 1.0 && rand.nextFloat() <= 0.5f) { 
	    				 instance.setValue(attribute, 0.0);
		    			 
		    			 for (int att2 = 0 ; att2 < instance.numAttributes(); att2 ++) {
		    	    		 Attribute attribute2 = instance.attribute(att2);
		    	    		 
		    	    		 if (attribute2.name().equals("DaysForActivationQuantile0")) {
		    	    			 instance.setValue(attribute2, 1.0);
		    	    			 break;
		    	    		 }
		    			 }
	    			 }
	    		 }
	    		 else if (name.equals("ValidForWeekdays")) { // 50% decrease of deals that are not active for every weekday
	    			 double v = instance.value(attribute); 
	    			 if (v == 0.0 && rand.nextFloat() <= 0.5f) {
	    				 instance.setValue(attribute, 1.0);
	    			 }
	    		 }
	    		 else if (name.equals("MultiplePrices")) { // Cancel deals with multiple prices
	    			 instance.setValue(attribute, 0.0);
	    		 }
	    		 else if (name.equals("NumberOfNights1")) {  // One night stay hotels become two-night stays
	    			 double v = instance.value(attribute); 
	    			 if (v == 1.0) {
	    				 instance.setValue(attribute, 0.0);
		    			 for (int att2 = 0 ; att2 < instance.numAttributes(); att2 ++) {
		    	    		 Attribute attribute2 = instance.attribute(att2);
		    	    		 
		    	    		 if (attribute2.name().equals("NumberOfNights2")) {
		    	    			 instance.setValue(attribute2, 1.0);
		    	    		 }
		    	    		 
		    	    		 if (attribute2.name().equals("PriceAfterDiscount")) {
		    	    			 double p = instance.value(attribute2); 
		    	    			 instance.setValue(attribute2, p * 2.0);
		    	    		 }
		    			 }
		    			 
	    			 }
	    		 }
	    		 else if (name.equals("NumberOfNights4")) {  // Four night stay hotels become two-night stays
	    			 double v = instance.value(attribute); 
	    			 if (v == 1.0) {
	    				 instance.setValue(attribute, 0.0);
		    			 for (int att2 = 0 ; att2 < instance.numAttributes(); att2 ++) {
		    	    		 Attribute attribute2 = instance.attribute(att2);
		    	    		 
		    	    		 if (attribute2.name().equals("NumberOfNights2")) {
		    	    			 instance.setValue(attribute2, 1.0);
		    	    		 }
		    	    		 
		    	    		 if (attribute2.name().equals("PriceAfterDiscount")) {
		    	    			 double p = instance.value(attribute2); 
		    	    			 instance.setValue(attribute2, p * 0.5);
		    	    		 }
		    			 }
		    			 
	    			 }
	    		 }
	    		 else if (name.equals("LuxuryHotel1")) {  // Decrease by 30% the amount of non-luxury hotels
	    			 double v = instance.value(attribute);
	    			 if (v == 1.0 && rand.nextFloat() <= 0.3f) {
	    				 instance.setValue(attribute, 0.0);
	    				 for (int att2 = 0 ; att2 < instance.numAttributes(); att2 ++) {
		    	    		 Attribute attribute2 = instance.attribute(att2);
		    	    		 
		    	    		 if (attribute2.name().equals("LuxuryHotel0")) {
		    	    			 instance.setValue(attribute2, 1.0);
		    	    		 }
		    	    		 
		    	    		 if (attribute2.name().equals("PriceAfterDiscount")) {
		    	    			 double p = instance.value(attribute2); 
		    	    			 instance.setValue(attribute2, p * 1.31);
		    	    		 }
		    			 }
	    			 }
	    		 }
	    		 else if (name.equals("GeographicalArea3") || name.equals("GeographicalArea4") 
	    				 || name.equals("GeographicalArea13")) {  // Decrease by 30% the deals that are offered in the areas 4, 5, 14
	    			 double v = instance.value(attribute);
	    			 if (v == 1.0 && rand.nextFloat() <= 0.3f) {
	    				 instance.setValue(attribute, 0.0);
	    				 for (int att2 = 0 ; att2 < instance.numAttributes(); att2 ++) {
		    	    		 Attribute attribute2 = instance.attribute(att2);
		    	    		 
		    	    		 if (attribute2.name().equals("GeographicalArea0")) {
		    	    			 instance.setValue(attribute2, 1.0);
		    	    		 }
	    				 }
	    			 }
	    		 }
	    	 }
	     }
	     
	     ArffSaver saver = new ArffSaver();
	     saver.setInstances(instances);
	     try {
	    	 saver.setFile(whatIfData);
	    	 saver.writeBatch();
	     }
	     catch (Exception ex) {
	    	 logger.error("Cannot write what-If data");
	    	 System.exit(-1);
	     }
	}
	
	public void scenarioB(File initialData, File whatIfData) {
		Random rand = new Random();
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
	     
	     for (int i = 0 ; i < instances.numInstances(); i ++) {
	    	 Instance instance = instances.instance(i);
	    	 for (int att = 0 ; att < instance.numAttributes(); att ++) {
	    		 Attribute attribute = instance.attribute(att);
	    		 
	    		 String name = attribute.name();
	    		 
	    		 if (name.equals("DealDuration")) {  // Decrease deal duration by one day for 20% of deals with 3 or more days
	    			 double v = instance.value(attribute); 
	    			 double newValue = v;
	    			 if (v >= 3.0 && rand.nextFloat() <= 0.2f) {  
	    				 newValue = v - 1.0f;
	    			 }
	    			 instance.setValue(attribute, newValue);
	    		 }
	    		 else if (name.equals("HasLongerIntervalForSimilarDeals")) { // Decrease by 10% the number of deals that do not have long time intervals since similar deals
	    			 double v = instance.value(attribute); 
	    			 double newValue = v;
	    			 if (v == 0.0 && rand.nextFloat() <= 0.1f) {
	    				 newValue = 1.0f;
	    			 }
	    			 instance.setValue(attribute, newValue);
	    		 }
	    		 else if (name.equals("WeekendDeal")) {  // Decrease Travel, Beauty and Restaurant deals in the weekend by 50%
	    			 double v = instance.value(attribute); 
	    			 if (v == 1.0 && rand.nextFloat() <= 0.5f) {
	    				 boolean found = false;
	    				 for (int att2 = 0 ; att2 < instance.numAttributes(); att2 ++) {
		    	    		 Attribute attribute2 = instance.attribute(att2);
		    	    		 
		    	    		 if (attribute2.name().equals("GrouponCategory2") || attribute2.name().equals("GrouponCategory17") || attribute2.name().equals("GrouponCategory15")) {
		    	    			 double v2 = instance.value(attribute2);
		    	    			 
		    	    			 if (v2 == 1.0) {
		    	    				 instance.setValue(attribute, 0.0);
		    	    				 found = true;
		    	    				 break;
		    	    			 }
		    	    		 }
	    				 }
	    				 if (found) { // Swap with a non-weekend deal
	    					 boolean swapped = false;
	    					 for (int j = 0 ; j < instances.numInstances(); j ++) {
	    				    	 Instance instance2 = instances.instance(j);
	    				    	 
	    				    	 for (int att3 = 0 ; att3 < instance.numAttributes(); att3 ++) {
	    				    		 Attribute attribute3 = instance.attribute(att);
	    				    		
	    				    		 if (attribute3.name().equals("WeekendDeal")) {
	    				    			 double v3 = instance2.value(attribute3); 
	    				    			 
	    				    			 if (v3 == 0.0) {
	    				    				 instance2.setValue(attribute3, 1.0);
	    				    				 swapped = true;
	    				    				 break;
	    				    			 }
	    				    		 }
	    				    	 }
	    				    	 if (swapped) {
	    				    		 break;
	    				    	 }
	    					 }
	    				 }
	    			 }
	    		 }
	    	 }
	     }
	     
	     ArffSaver saver = new ArffSaver();
	     saver.setInstances(instances);
	     try {
	    	 saver.setFile(whatIfData);
	    	 saver.writeBatch();
	     }
	     catch (Exception ex) {
	    	 logger.error("Cannot write what-If data");
	    	 System.exit(-1);
	     }
	}
}
