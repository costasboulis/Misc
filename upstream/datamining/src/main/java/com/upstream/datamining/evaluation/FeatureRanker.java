package com.upstream.datamining.evaluation;
/**
 * Ranks features according to p(MO=1|F=1). Useful for assessing the importance of each feature
 * 
 */
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instance;
import weka.core.Instances;

public class FeatureRanker {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void rank(File arffFile) {
		Instances instances = null;
        try {
            Reader reader = new FileReader(arffFile);
            instances = new Instances(reader); 
            instances.setClassIndex(instances.numAttributes() - 1); 
        }
        catch (IOException ex) {
            logger.error("Could not read from file " + arffFile.getAbsolutePath());
            System.exit(-1);
        }
        
        int numAttributes = instances.numAttributes();
        int[][][] counts = new int[numAttributes][2][2];
        int[] prior = new int[2];
        for (int att = 0; att < numAttributes; att ++) {
        	counts[att][0][0] = counts[att][0][1] = counts[att][1][0] = counts[att][1][1] = 0;
        }
        for (int i = 0; i < instances.numInstances(); i ++) {
            Instance instance = instances.instance(i);
            
            double cl = instance.classValue();
            int classVal = cl == 1.0 ? 1 : 0;
            
            prior[classVal] ++;
            
            for (int att = 0; att < numAttributes; att ++) {
            	double f = instance.value(att);
            	int featureVal = f > 0.0 ? 1 : 0;
            	
            	counts[att][classVal][featureVal] ++;
            }
        }
        LinkedList<UserScore> features = new LinkedList<UserScore>();
        for (int att = 0; att < numAttributes; att ++) {
        	float score = (float)counts[att][1][1] / ((float)counts[att][1][1] + (float)counts[att][0][1]);
        	
        	// Overloading the use of UserScore to include features
            UserScore feat = new UserScore(att, score);
            
            features.add(feat);
        }
        Collections.sort(features);
        
        for (UserScore feat : features) {
        	String desc = instances.attribute(feat.getUserId()).toString();
        	logger.info(desc + " : " + feat.getScore());
        }
        float p = (float)prior[1] / ((float)prior[1] + (float)prior[0]);
        logger.info("Class Prior: " + p);
	}
	
	public static void main(String[] argv) {
		FeatureRanker fr = new FeatureRanker();
		fr.rank(new File("c:\\Upstream\\EvaluationFeatures_17112009.arff"));
	}
}
