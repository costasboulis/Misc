package com.upstream.datamining.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Convenience class for running ModelBuilder many times, useful for evaluating all the days of a campaign
 * @author kboulis
 *
 */
public class BatchModelBuilder {
	 public static String newline = System.getProperty("line.separator");
	 
	 
	 public static void main(String[] argv) {
		 String mo = "c:\\Upstream\\motlog_sorted.txt";
		 String mt = "c:\\Upstream\\mttlog_sorted.txt";
//		 String beginDate = "02/10/2009 00:00:00";
		 File file = new File("c:\\Upstream\\results.txt");
		 try {
			 PrintWriter out = new PrintWriter(new FileWriter(file));
		        
			 for (int m = 10; m <= 10; m ++) {
				 for (int d = 1; d <= 31; d ++) {
					 if (d <= 3 && m == 10) {
						 continue;
					 }
					 if (d == 31 && m == 11) {
						 continue;
					 }
					 String tmp = Integer.toString(d);
					 String prefix = d < 10 ? "0" + tmp : tmp;
					 String toDateString = prefix + "/" + Integer.toString(m) + "/2009 23:59:59";
					 ModelBuilder baseline = new ModelBuilder();
					 baseline.evaluateExistingCampaign(mo, mt, toDateString, "c:\\Upstream\\");
			//		 baseline.rankUsers(mo, mt, toDateString, "c:\\Upstream\\", "c:\\Upstream\\users.txt"); 
			//		 System.exit(-1);
					 
					 out.println("Date: " + toDateString);
					 out.println("==================");
					 // Calculate Precision-Recall curve
					 PrecisionRecallEvaluator gg = new PrecisionRecallEvaluator();
					 gg.setTestData(baseline.getTestData());
					 for (PrecisionRecallPoint prp : gg.createGraph()) {
						 out.println(prp.toString());
					 }
					 out.println(newline);
		                
					 // Calculate accuracy score
					 AccuracyEvaluator ac = new AccuracyEvaluator();
					 ac.setTestData(baseline.getTestData());
					 float accuracy = ac.calculateHardAccuracy();
					 out.println("Base Accuracy: " + ac.calculateBaselineScore());
					 out.println("Accuracy: " + accuracy + newline);
		                
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
			 }
		        
		        
		 }
		 catch (Exception e){
			 System.err.println("I/O error");
			 System.exit(-1);
		 }
		    
	 }
   
}
