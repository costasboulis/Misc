package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SetSplitter {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private Random r;
	
	public SetSplitter() {
		r = new Random();
	}
	
	/*
	 * Select for testing all the ads of a particular month
	 */
	public void splitByTime(File original, File train, File test, int month, int year, String transactionType) {
		try {
			PrintWriter outTrain = new PrintWriter(new FileWriter(train));
			PrintWriter outTest = new PrintWriter(new FileWriter(test));
			BufferedReader in = new BufferedReader(new FileReader(original));
			String logEntryLine = in.readLine();
			String[] fieldNames = logEntryLine.split("\\t");
			HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
			for (int i = 0; i < fieldNames.length; i ++) {
				names2Index.put(fieldNames[i], new Integer(i));
			}
			outTrain.write(logEntryLine + newline);
			outTest.write(logEntryLine + newline);
			while ((logEntryLine = in.readLine()) != null) {
				RealEstate re = new RealEstate(logEntryLine, names2Index);
				
				if (re.getTransactionType().equals(transactionType) && 
						re.getPublicationMonth() == month && re.getPublicationYear() == year &&
						re.getPrice() > 0.0f && re.getSize() > 0.0f) {
					outTest.write(logEntryLine + newline);
				}
				else {
					outTrain.write(logEntryLine + newline);
				}
			}
			in.close();
			outTrain.close();
			outTest.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + original.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	/*
	 * Select for testing a random sample of the original set
	 */
	public void split(File original, File train, File test, float fraction, String transactionType) {
		try {
			PrintWriter outTrain = new PrintWriter(new FileWriter(train));
			PrintWriter outTest = new PrintWriter(new FileWriter(test));
			BufferedReader in = new BufferedReader(new FileReader(original));
			String logEntryLine = in.readLine();
			String[] fieldNames = logEntryLine.split("\\t");
			HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
			for (int i = 0; i < fieldNames.length; i ++) {
				names2Index.put(fieldNames[i], new Integer(i));
			}
			outTrain.write(logEntryLine + newline);
			outTest.write(logEntryLine + newline);
			while ((logEntryLine = in.readLine()) != null) {
				RealEstate re = new RealEstate(logEntryLine, names2Index);
				
				if (r.nextFloat() <= fraction || !re.getTransactionType().equals(transactionType)) {
					outTrain.write(logEntryLine + newline);
				}
				else {
					outTest.write(logEntryLine + newline);
				}
			}
			in.close();
			outTrain.close();
			outTest.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + original.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	public static void main(String[] argv) {
//		String original = "\\\\x8\\internet\\Projects\\XE\\Statistics\\New base\\002_Deduped_RealEstate_residence_2010_eksamino_2010_1.txt";
//		String transactionType = "SELL";
//		String train = "\\\\x8\\internet\\Projects\\XE\\Statistics\\New base\\002_Deduped_RealEstate_residence_2010_eksamino_2010_1_TRAIN.txt";
//		String test = "\\\\x8\\internet\\Projects\\XE\\Statistics\\New base\\002_Deduped_RealEstate_residence_2010_eksamino_2010_1_TEST_" 
//			+ transactionType + ".txt"; 
		
//		String original = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\2005-2010\\002_residences_deduped_2010_1.txt";
		String original = "c:\\Data\\001_residences_2010_noErrors.txt";
		String transactionType = "SELL";
		String train = "c:\\Data\\residences_2010_1_TRAIN.txt";
		String test = "c:\\Data\\residences_2010_1_TEST_" 
			+ transactionType + ".txt"; 
		float fraction = 0.9f;
		
		
		SetSplitter ss = new SetSplitter();
		ss.splitByTime(new File(original), new File(train), new File(test), 12, 2010, transactionType);
	}
}
