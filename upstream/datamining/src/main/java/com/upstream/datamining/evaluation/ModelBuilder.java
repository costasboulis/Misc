package com.upstream.datamining.evaluation;
/**
 * Builds and evaluates a model for a defined time window 
 * The process involves building a model for days <code>fromDate</code> to <code>toDate</code>-1 and
 * evaluating the model for day <code>toDate</code>
 * 
 * @param mo File with MO messages, chronologically sorted
 * @param mt File with MT messages, chronologically sorted
 * @param fromDate Begin date of model
 * @param toDate End date of model
 */



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upstream.datamining.FeatureBuilder;

import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.Instance;


public class ModelBuilder {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private List<TestDataPoint> testData;
    public static String newline = System.getProperty("line.separator");
    
    
    public ModelBuilder() {
        
    }
    
    public void evaluateExistingCampaign(String mo, String mt, String toDateString, String baseDir) {
    	Date toDate = null;
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            toDate = (Date)formatter.parse(toDateString);
        }
        catch (ParseException ex) {
            logger.error("Cannot parse date \"" + toDateString + "\"");
            System.exit(-1);
        }
        
        long d = toDate.getTime() - (24 * 3600 * 1000);
        Date dayBeforeToDate = new Date(d);
        
        FeatureBuilder pr = null;
        try {
            pr = new FeatureBuilder(new File(mo), new File(mt), dayBeforeToDate);
        }
        catch (IOException ex) {
            System.exit(-1);
        }
        
        pr.load();
        
        File trainingARFF = new File(baseDir + "TrainingFeatures.arff");
        if (trainingARFF.exists()) {
            logger.warn("File " + trainingARFF.getAbsolutePath() + " exists...deleting");
            trainingARFF.delete();
        }
        pr.writeFeatures(trainingARFF, pr.createFeatures(false));
        
        
        Instances instances = null;
        try {
            Reader reader = new FileReader(trainingARFF);
            instances = new Instances(reader); 
            instances.setClassIndex(instances.numAttributes() - 1); 
        }
        catch (IOException ex) {
            logger.error("Could not read from file " + trainingARFF.getAbsolutePath());
            System.exit(-1);
        }
        
        Logistic logistic = new Logistic();
        logistic.setRidge(1.0e-08);
        logistic.setMaxIts(-1);
        
        
        try {
        	logistic.buildClassifier(instances);
        }
        catch (Exception ex) {
            logger.error("Could not train classifier");
            System.exit(-1);
        }
        
        logger.info("Created classifier");
        // Now do the scoring
        pr = null;
        try {
            pr = new FeatureBuilder(new File(mo), new File(mt), toDate);
        }
        catch (IOException ex) {
            System.exit(-1);
        }
        
        pr.load();
        
        File evaluationARFF = new File(baseDir + "EvaluationFeatures.arff");
        if (evaluationARFF.exists()) {
            logger.warn("File " + evaluationARFF.getAbsolutePath() + " exists...deleting");
            evaluationARFF.delete();
        }
        pr.writeFeatures(evaluationARFF, pr.createFeatures(false));
       
       
       
        instances = null;
        try {
            Reader reader = new FileReader(evaluationARFF);
            instances = new Instances(reader); 
            instances.setClassIndex(instances.numAttributes() - 1); 
        }
        catch (IOException ex) {
            logger.error("Could not read from file " + evaluationARFF.getAbsolutePath());
            System.exit(-1);
        }
        
        
        testData = new LinkedList<TestDataPoint>();
        for (int i = 0; i < instances.numInstances(); i ++) {
            Instance instance = instances.instance(i);
            
            double[] probs = null;
            try {
                probs = logistic.distributionForInstance(instance);
                double cl = instance.classValue();
                boolean isClass = cl == 1.0 ? true : false;
                
                TestDataPoint tdp = new TestDataPoint((float)probs[1], isClass);
                testData.add(tdp);
            }
            catch (Exception ex) {
                logger.error("Cannot create class distribution for instance " + instance.toString());
                System.exit(-1);
            }
        }
        
        // Clean up
        trainingARFF.delete();
        evaluationARFF.delete();
        logger.info("Finished");
    }
    
    
    /**
     * Ranks users according to p(MO will send at least one MO the next day | profile, Bulk MT sent the next day)
     * @param mo
     * @param mt
     * @param toDateString
     * @param baseDir
     * @param output
     */
    public void rankUsers(String mo, String mt, String toDateString, String baseDir, String output) {
    	Date toDate = null;
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            toDate = (Date)formatter.parse(toDateString);
        }
        catch (ParseException ex) {
            logger.error("Cannot parse date \"" + toDateString + "\"");
            System.exit(-1);
        }
        
        long d = toDate.getTime() - (24 * 3600 * 1000);
        Date dayBeforeToDate = new Date(d);
        
        FeatureBuilder pr = null;
        try {
            pr = new FeatureBuilder(new File(mo), new File(mt), dayBeforeToDate);
        }
        catch (IOException ex) {
            System.exit(-1);
        }
        
        pr.load();
        
        File trainingARFF = new File(baseDir + "TrainingFeatures.arff");
        if (trainingARFF.exists()) {
            logger.warn("File " + trainingARFF.getAbsolutePath() + " exists...deleting");
            trainingARFF.delete();
        }
        pr.writeFeatures(trainingARFF, pr.createFeatures(false));
        
        
        Instances instances = null;
        try {
            Reader reader = new FileReader(trainingARFF);
            instances = new Instances(reader); 
            instances.setClassIndex(instances.numAttributes() - 1); 
        }
        catch (IOException ex) {
            logger.error("Could not read from file " + trainingARFF.getAbsolutePath());
            System.exit(-1);
        }
        
        Logistic logistic = new Logistic();
        logistic.setRidge(1.0e-08);
        logistic.setMaxIts(-1);
        
        
        try {
        	logistic.buildClassifier(instances);
        }
        catch (Exception ex) {
            logger.error("Could not train classifier");
            System.exit(-1);
        }
        
        logger.info("Created classifier");
        // Now do the scoring
        pr = null;
        try {
            pr = new FeatureBuilder(new File(mo), new File(mt), toDate);
        }
        catch (IOException ex) {
            System.exit(-1);
        }
        
        pr.load();
        
        File evaluationARFF = new File(baseDir + "EvaluationFeatures.arff");
        if (evaluationARFF.exists()) {
            logger.warn("File " + evaluationARFF.getAbsolutePath() + " exists...deleting");
            evaluationARFF.delete();
        }
        pr.writeFeatures(evaluationARFF, pr.createFeatures(true));
       
       
       LinkedList<Integer> userIdList = new LinkedList<Integer>();
        instances = null;
        try {
            Reader reader = new FileReader(evaluationARFF);
            instances = new Instances(reader); 
            instances.setClassIndex(instances.numAttributes() - 1); 
            for (int i = 0; i < instances.numInstances(); i ++) {
                Instance instance = instances.instance(i);
                int userId = (int)instance.value(0);
                userIdList.add(userId);
            }
            instances.deleteAttributeAt(0);
        }
        catch (IOException ex) {
            logger.error("Could not read from file " + evaluationARFF.getAbsolutePath());
            System.exit(-1);
        }
        
        LinkedList<UserScore> userScoreList = new LinkedList<UserScore>();
        testData = new LinkedList<TestDataPoint>();
        for (int i = 0; i < instances.numInstances(); i ++) {
            Instance instance = instances.instance(i);
            
            double[] probs = null;
            try {
            	int userId = userIdList.get(i);
            	instance.setClassMissing();
                probs = logistic.distributionForInstance(instance);
                // probs[1] is the prob we want, we also need the userId
                // Sort and write
                UserScore us = new UserScore(userId, (float)probs[1]);
                
                userScoreList.add(us);
            }
            catch (Exception ex) {
                logger.error("Cannot create class distribution for instance " + instance.toString());
                System.exit(-1);
            }
        }
        Collections.sort(userScoreList);
        
        // Write ranked list of users
        File outputFile = new File(output);
        try {
    		PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    		for (UserScore us : userScoreList) {
    			out.println("\"" + us.getUserId() + "\",\"" + us.getScore());
    		}
    		out.close();
        }
        catch (Exception e){
    		logger.error("Could not write to file " + outputFile.getAbsolutePath());
    		System.exit(-1);
    	}
        
        
        
        // Clean up
        trainingARFF.delete();
        evaluationARFF.delete();
        logger.info("Finished");
    }
    
    public  List<TestDataPoint> getTestData() {
    	return testData;
    }
    
   
    public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{"inOperationMode"}, new String[]{
    							 "MO",
                                 "MT",
                                 "toDay",
                                 "baseDir",
                                 "results"})) {
        		 System.err.println("Trains a Logistic Regression model for days 1..D-1 and then evaluates the model for day D" + newline +
        				 		"Usage: ModelBuilder\n" +
        				 		"-MO : Full pathname of file with MO messages sorted in chronological order (oldest first)" + newline + 
        				 		"-MT : Full pathname of file with MO messages sorted in chronological order (oldest first)" + newline +
        				 		"-toDay : Day for which evaluation will hold. Needs to be in the following format DD/MM/YYYY" + newline +
        				 		"[-baseDir] : Base directory where necessary internal files will be written. If not specified then it is current directory" + newline +
        		 				"-results : Full pathname of where the results are written" + newline +
        		 				"-inOperationMode : If specified then a ranked list of users is produced that are the best targets for MTs" + newline);
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String mo = CommandLineParser.getStringParameter(arguments, "MO", true);
        	 String mt = CommandLineParser.getStringParameter(arguments, "MT", true);
        	 String toDay = CommandLineParser.getStringParameter(arguments, "toDay", true);
        	 String baseDir = CommandLineParser.getStringParameter(arguments, "baseDir", false);
        	 boolean inOperationMode = CommandLineParser.getBooleanParameter(arguments, "inOperationMode", false, false);
        	 
        	 if (baseDir == null) {
        		 File dir1 = new File (".");
        		 try {
        			 baseDir = dir1.getCanonicalPath();
        		 }
        		 catch(Exception e) {
        			 e.printStackTrace();
        		 }
        	 }
        	 String results = CommandLineParser.getStringParameter(arguments, "results", true);
        	 
        	 String toDate = toDay + " " + "23:59:59";
        	 ModelBuilder baseline = new ModelBuilder();
        	 if (!inOperationMode) {
        		 baseline.evaluateExistingCampaign(mo, mt, toDate, baseDir);
        		 try {
        			 PrintWriter out = new PrintWriter(new FileWriter(results));
        			 out.println("Date: " + toDate);
    				 out.println("==================");
    				 // Calculate Precision-Recall curve
    				 PrecisionRecallEvaluator gg = new PrecisionRecallEvaluator();
    				 gg.setTestData(baseline.getTestData());
    				 for (PrecisionRecallPoint prp : gg.createGraph()) {
    					 out.println(prp.toString());
    				 }
    				 out.println(newline);
    	  
    				 //Calculate Confusion Matrix
    				 ConfusionMatrixEvaluator cm = new ConfusionMatrixEvaluator();
    				 cm.setTestData(baseline.getTestData());
    				 int[][] confMatrix = cm.calculateConfusionMatrix();
    				 out.println("Confusion Matrix");
    				 out.println("[1][1]: " + confMatrix[1][1] + " [1][0]: " + confMatrix[1][0]);
    				 out.println("[0][1]: " + confMatrix[0][1] + " [0][0]: " + confMatrix[0][0]);
    				 out.println(newline + newline);
    				 out.flush();
        		 }
        		 catch (Exception e){
        			 System.err.println("I/O error");
        			 System.exit(-1);
        		 }
        	 }
        	 else {
        		 baseline.rankUsers(mo, mt, toDate, baseDir, results);
        	 }
        	 
    }
    
}
