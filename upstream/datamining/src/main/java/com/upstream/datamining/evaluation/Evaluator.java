package com.upstream.datamining.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides base functionality to every evaluator, sets the test data points or reads them from disk
 * @author kboulis
 *
 */
public abstract class Evaluator {
	protected List<TestDataPoint> testData;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void setTestData(List<TestDataPoint> tdpl) {
	    testData = tdpl;
	}
	
	/**
	 * Reads ARFF file with predictions along with references
	 * @param modelOutputFile File in ARFF format containing predictions and references
	 */
	public void readTestData(File modelOutputFile) {
		testData = new LinkedList<TestDataPoint>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(modelOutputFile)));
			String lineStr;
			while ((lineStr = br.readLine()) != null) {
				lineStr = lineStr.replace('*', ' ');
				lineStr = lineStr.replace('+', ' ');
				String[] st = lineStr.split("\\s");
				float prob = 0.0f;
				boolean isClass = false;
				if (st.length < 4) {
					continue;
				}
				boolean skipLine = false;
				int k = 0;
				for (int i = 0; i < st.length; i ++) {
					if (st[i].length() == 0) {
						continue;
					}
					if (k == 0) {
						try {
		                    int id = Integer.parseInt(st[i]);
		                }
		                catch (NumberFormatException ex) {
		                	skipLine = true;
		                    break;
		                }
					}
					else if (k == 4) {
						try {
							prob = Float.parseFloat(st[i]);
						}
						catch (NumberFormatException ex) {
							skipLine = true;
							break;
						}
						
					}
					else if (k == 1) {
						if (st[i].equals("2:MOSent")) {
							isClass = true;
						}
						else if (st[i].equals("1:NoMOSent")) {
							isClass = false;
						}
						else {
							skipLine = true;
							break;
						}
					}
					
					k ++;
				}
				if (!skipLine) {
					TestDataPoint tdp = new TestDataPoint(prob, isClass);
					testData.add(tdp);
				}
			}
			
			br.close();
		}
		catch (FileNotFoundException ex) {
            logger.error("Could not find file " + modelOutputFile.getAbsolutePath());
            System.exit(-1);
        }
        catch (IOException ex) {
            logger.error("Could not read file " + modelOutputFile.getAbsolutePath());
            System.exit(-1);
        }
	}
}
