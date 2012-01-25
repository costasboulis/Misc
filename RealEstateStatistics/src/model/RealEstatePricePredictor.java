package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.LinkedList;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class RealEstatePricePredictor {
	private Logger logger = LoggerFactory.getLogger(getClass());
	protected String modelName;
	
	// Names of the config variables
	private static String TRANSACTION_TYPE = "Transaction.type";
	private static String ITEM_TYPE = "Item.item_type";
	private static String DISCOUNT_FACTOR = "DISCOUNT_FACTOR";
	private static String MODELS = "MODELS";
	private static String AREAS = "AREAS";
	
	protected String transactionType;
	protected String itemType;
	protected List<File> models;
	protected float discountFactor;
	public static String newline = System.getProperty("line.separator");
	protected HashSet<String> areas;
	protected HashMap<String, List<String>> parentAreas;
	
	private class PredictionError implements Comparable<PredictionError>{
		private int id;
		private double value;
		
		public PredictionError(int p, double sq) {
			id = p;
			value = sq;
		}
		
		public double getValue() {
			return value;
		}
		
		public int getId() {
			return id;
		}
		
		public int compareTo(PredictionError d) {
			if (value - d.getValue() < 0.0f) {
				return 1;
			}
			else if (value - d.getValue() > 0.0f) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	
	public void readAreas(File advFile) throws Exception {
		areas = new HashSet<String>();
		HashMap<String, String> areaNames = new HashMap<String, String>();
		parentAreas = new HashMap<String, List<String>>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(advFile));
			String logEntryLine;
			while ((logEntryLine = in.readLine()) != null) {
				String[] fields = logEntryLine.split(",");
				
				String areaID = fields[0];
				if (areaID == null || areaID.length() == 0) {
					continue;
				}
				areas.add(areaID);
				StringBuffer sb = new StringBuffer();
				for (int i = 1; i < fields.length - 1; i ++) {
					sb.append(fields[i]); sb.append(",");
				}
				sb.append(fields[fields.length-1]);
				String name = sb.toString().trim();
				areaNames.put(name, areaID);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			throw new Exception();
		}
		
		for (Map.Entry<String, String> me : areaNames.entrySet()) {
			List<String> parentsList = new LinkedList<String>();
			String[] fields = me.getKey().split(",");
			for (int i = fields.length - 2; i >= 0 ; i --) {
				String name = fields[i];
				for (int j = i - 1; j >= 0; j --) {
					name = fields[j] + "," + name;
				}
				String parentID = areaNames.get(name);
				if (parentID == null) {
					logger.error("Cannot find id for \"" + name + "\"");
					continue;
				}
				parentsList.add(parentID);
			}
			parentAreas.put(me.getValue(), parentsList);
		}
		
		logger.info("Read " + areas.size() + " areas");
	}
	
	public List<String> getParents(String areaID) {
		return areaID == null || parentAreas.get(areaID) == null ? new LinkedList<String>() : parentAreas.get(areaID);
	}
	
	public void calculateError(File testSet, File outFile) {
		HashMap<String, List<Float>> types = new HashMap<String, List<Float>>();
		int numberOfIgnored = 0;
		int numberOfDataPoints = 0;
		double mae = 0.0;
		double avgActual = 0.0;
		List<Float> medianPricePerSquareMeter = new LinkedList<Float>();
		LinkedList<PredictionError> predList = new LinkedList<PredictionError>();
		String logEntryLine = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(testSet));
			logEntryLine = in.readLine();
			HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
			String[] fieldNames = logEntryLine.split("\\t");
			for (int i = 0; i < fieldNames.length; i ++) {
				names2Index.put(fieldNames[i], new Integer(i));
			}
			
			try {
				NumberFormat doubleNumberFormat = NumberFormat.getNumberInstance(new Locale("el", "GR")); 
				doubleNumberFormat.setMaximumFractionDigits(2);
				doubleNumberFormat.setMinimumFractionDigits(0);
				PrintWriter out = new PrintWriter(new FileWriter(outFile));
				out.write("AreaID\tType\tState\tMedianError" + newline);
				while ((logEntryLine = in.readLine()) != null) {
					RealEstate re = new RealEstate(logEntryLine, names2Index);
					
					String transactionType = re.getTransactionType();
					if (!transactionType.equalsIgnoreCase(this.transactionType) || re.getPrice() <= 0.0f || re.getSize() <= 0.0f || 
							re.getRealEstateType().equals("N/A")) {
						numberOfIgnored ++;
						continue;
					}

					
					
					Prediction prediction = predict(re);
					float predictionValue = prediction.getValue();
					String predictionType = prediction.getType();
					float confidence = prediction.getConfidence();
					
					if (predictionValue <= 0.0f || confidence < 0.7f) {
						numberOfIgnored ++;
						continue;
					}
					
					if (prediction.getType().equals(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION.toString())) {
						numberOfIgnored ++;
						continue;
					}
					
					float actual = re.getPrice() / re.getSize();
					double tmp = predictionValue - actual;
					float absError = (float)Math.abs(tmp);
					
					
					StringBuffer sb = new StringBuffer();
					sb.append(re.getAreaID()); sb.append("\t");
					sb.append(re.getRealEstateType()); sb.append("\t");
					sb.append(re.getState()); sb.append("\t");
					
					sb.append(Math.round(absError)); sb.append(newline);
					
					out.write(sb.toString());
//					if (confidence == 1.0f) {
//						numberOfIgnored ++;
//						continue;
//					}
					
					
					List<Float> typeCnt = types.get(predictionType);
					if (typeCnt == null) {
						typeCnt = new LinkedList<Float>();
						typeCnt.add(absError);
						types.put(predictionType, typeCnt);
					}
					else {
						typeCnt.add(absError);
					}
					
					
					PredictionError pe = new PredictionError(numberOfDataPoints, absError);
					predList.add(pe);
					avgActual += actual;
					medianPricePerSquareMeter.add(actual);
					if (tmp > 5000.0f || tmp < -5000.0f) {
//						logger.warn(prediction.getType() + " PRED: " + predictionValue + " ACTUAL: " + actual);
//						logger.warn(re.toString());
					}
		//			System.out.println("PRED: " + prediction + " ACTUAL: " + actual);
					mae += absError;
					numberOfDataPoints ++;
				}
				out.close();
			}
			catch (Exception ex) {
				logger.error("Cannot write to " + outFile.getAbsolutePath());
				System.exit(-1);
			}
			
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + testSet.getAbsolutePath() + " " + logEntryLine);
			System.exit(-1);
		}
		
		for (Map.Entry<String, List<Float>> es : types.entrySet()) {
			if (es.getValue().size()  > 1) {
				Collections.sort(es.getValue());
				
				int m = (int)Math.ceil((double)es.getValue().size() / (double)2);
				float f = es.getValue().get(m);
				float mean = 0.0f;
				for (Float fl : es.getValue()) {
					mean += fl;
				}
				mean /= (float)es.getValue().size();
				logger.info(es.getKey() + " : " + es.getValue().size() + " MEDIAN : " + f + " MEAN : " + mean);
			}
			else {
				logger.info(es.getKey() + " : " + es.getValue().size() );
			}
		}
		logger.info("Number of Ignored : " + numberOfIgnored);
		logger.info("Number of Data Points : " + numberOfDataPoints);
//		rmse = Math.sqrt(rmse / (double)numberOfDataPoints);
		mae /= (double)numberOfDataPoints;
		logger.info("Mean Absolute Error : " + mae);
		avgActual /= (double)numberOfDataPoints;
		logger.info("Average actual : " + avgActual);
		Collections.sort(medianPricePerSquareMeter);
		int m = (int)Math.ceil((double)medianPricePerSquareMeter.size() / (double)2);
		logger.info("Median actual : " + medianPricePerSquareMeter.get(m));
		
		Collections.sort(predList);
		int cnt = 0;
		List<List<Float>> dist = new LinkedList<List<Float>>();
		for (int i = 0; i < 10; i ++) {
			dist.add(i, new LinkedList<Float>());
		}
		for (PredictionError pe : predList) {
			float pct = 10.0f * (float)cnt / (float)predList.size();
			
			int key = (int)Math.floor(pct);
			float value = (float)pe.getValue();
			
			List<Float> l = dist.get(key);
			if (l == null) {
				l = new LinkedList<Float>();
				dist.add(key, l);
			}
			l.add(value);
			
			cnt ++;	
		}
		for (int i = 0; i < dist.size(); i ++) {
			List<Float> l = dist.get(i);
			int middle = (int)Math.ceil((double)l.size() / (double)2);
			logger.info("PCT : " + i + " MEDIAN : " + l.get(middle));
		}
		int middle = (int)Math.ceil((double)predList.size() / (double)2);
//		double medianError = Math.sqrt(predList.get(middle).getSquaredError());
		double medianError = predList.get(middle).getValue();
		logger.info("MEDIAN ERROR : " + medianError);
	}
	
	
	public void readConfig(File configFile) throws Exception {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(configFile));
			String logEntryLine;
			while ((logEntryLine = in.readLine()) != null) {
				String[] fieldNames = logEntryLine.split("\\t");
				
				
				String var = fieldNames[0];
				if (!var.equalsIgnoreCase(TRANSACTION_TYPE) && !var.equalsIgnoreCase(ITEM_TYPE) 
						&& !var.equalsIgnoreCase(DISCOUNT_FACTOR) && !var.equalsIgnoreCase(MODELS)
						&& !var.equalsIgnoreCase(AREAS)) {
					logger.error("Unknown config variable \"" + var + "\"");
					throw new Exception();
				}
				
				if (var.equalsIgnoreCase(TRANSACTION_TYPE)) {
					transactionType = fieldNames[1];
					if (!transactionType.equalsIgnoreCase("SELL") && !transactionType.equalsIgnoreCase("LET")) {
						logger.error("Unknown transaction type value \"" + transactionType + "\" must be one of SELL or LET");
						throw new Exception();
					}
				}
				else if (var.equalsIgnoreCase(ITEM_TYPE)) {
					itemType = fieldNames[1];
					if (!itemType.equalsIgnoreCase("RE_RESIDENCE")) {
						logger.error("Unknown Item.item_type value \"" + itemType + "\" currently only RE_RESIDENCE supported");
						throw new Exception();
					}
				}
				else if (var.equalsIgnoreCase(DISCOUNT_FACTOR)) {
					try {
						discountFactor = Float.parseFloat(fieldNames[1]);
					}
					catch (Exception ex) {
						logger.error("Cannot parse value \"" + fieldNames[1] + "\" for DISCOUNT_FACTOR");
						throw new Exception();
					}
				}
				else if (var.equalsIgnoreCase(MODELS)) {
					models = new LinkedList<File>();
					for (int i = 1; i < fieldNames.length; i ++) {
						File f = new File(fieldNames[i]);
						
						models.add(f);
					}
				}
				else if (var.equalsIgnoreCase(AREAS)) {
					String parentsFilename = fieldNames[1];
					File f = new File(parentsFilename);
					if (!f.exists()) {
						logger.error("Areas file " + f.getAbsolutePath() + " does not exist");
						throw new Exception();
					}
					if (!f.canRead()) {
						logger.error("Areas file " + f.getAbsolutePath() + " cannot be read");
						throw new Exception();
					}
					readAreas(f);
				}
			}
			if (models.size() == 0) {
				logger.error("No models encountered in the config file");
				throw new Exception();
			}
			
		}
		catch (FileNotFoundException ex) {
			logger.error("Cannot find file " + configFile.getAbsolutePath());
			throw new Exception();
		}
		catch (Exception ex) {
			logger.error("Problem while reading config file " + configFile.getAbsolutePath());
			throw new Exception();
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public void setModelName(String name) {
		this.modelName = name;
	}
	public abstract void train(File advFile);
	public abstract Prediction predict(RealEstate re);
	public abstract void readModels() throws Exception;
	public abstract void writeModel() throws Exception;
}
