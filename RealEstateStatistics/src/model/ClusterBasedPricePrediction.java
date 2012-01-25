package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterBasedPricePrediction extends RealEstatePricePredictor {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private HashMap<String, Float> SS0;
	private HashMap<String, Float> SS1;
	private HashMap<String, Float> mean;
	private HashMap<String, String> fallbackAreas;
	private HashMap<String, Float> conversion;
	private float THRESHOLD = 1.0f;
	private final static String GLOBAL_AREA_ID = "ALL";
	private final static String GLOBAL_SIZE = "ALL";
	private final static String GLOBAL_FLOOR = "ALL";
	private final static String KEY_SEPARATOR = ";";
	
	private String getAreaBand2(float sqMeters) {
		if (sqMeters <= 54) {
			return "1-54";
		}
		else if (sqMeters <= 74) {
			return "55-74";
		}
		else if (sqMeters <= 100) {
			return "75-100";
		}
		else {
			return ">100";
		}
	}
	
	private String getAreaBand(float sqMeters) {
		if (sqMeters <= 35) {
			return "1-35";
		}
		else if (sqMeters <= 54) {
			return "36-54";
		}
		else if (sqMeters <= 70) {
			return "55-70";
		}
		else if (sqMeters <= 85) {
			return "71-85";
		}
		else if (sqMeters <= 100) {
			return "86-100";
		}
		else if (sqMeters <= 120) {
			return "101-120";
		}
		else {
			return ">121";
		}
	}
	
	public void readFallbackAreaIds(File fallbackFilename) {
		fallbackAreas = new HashMap<String, String>();
		logger.debug("Initiating training using " + fallbackFilename.getAbsolutePath());
		if (!fallbackFilename.exists()) {
			logger.error("ERROR: File " + fallbackFilename.getAbsolutePath() + " does not exist");
			System.exit(-1);
		}
		if (!fallbackFilename.canRead()) {
			logger.error("ERROR: Cannot read file " + fallbackFilename.getAbsolutePath());
			System.exit(-1);
		}
		try {
			BufferedReader in = new BufferedReader(new FileReader(fallbackFilename));
			String logEntryLine;
			SS0 = new HashMap<String, Float>();
			SS1 = new HashMap<String, Float>();
			while ((logEntryLine = in.readLine()) != null) {
				String[] fields = logEntryLine.split("\",\"");
				String areaID = fields[0].replaceAll("\"", "");
				String fallbackAreaID = "FALLBACK" + fields[1].replaceAll("\"", "");
				
				fallbackAreas.put(areaID, fallbackAreaID);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("Cannot read file " + fallbackFilename.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	private void calculateRentToSellConversion(File advFile) {
		conversion = new HashMap<String, Float>();
		if (!advFile.exists()) {
			logger.error("ERROR: File " + advFile.getAbsolutePath() + " does not exist");
			System.exit(-1);
		}
		if (!advFile.canRead()) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		HashMap<String, Float> rentSS0 = new HashMap<String, Float>();
		HashMap<String, Float> rentSS1 = new HashMap<String, Float>();
		HashMap<String, Float> sellSS0 = new HashMap<String, Float>();
		HashMap<String, Float> sellSS1 = new HashMap<String, Float>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(advFile));
			String logEntryLine;
			logEntryLine = in.readLine();
			HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
			String[] fieldNames = logEntryLine.split("\\t");
			for (int i = 0; i < fieldNames.length; i ++) {
				names2Index.put(fieldNames[i], new Integer(i));
			}
			
			while ((logEntryLine = in.readLine()) != null) {
				RealEstate re = new RealEstate(logEntryLine, names2Index);

				String areaID = re.getAreaID();
				String transactionType = re.getTransactionType();
				
				if (areaID.equalsIgnoreCase("NULL") || transactionType.equalsIgnoreCase("NULL") || 
						re.getPrice() <= 0.0f || re.getSize() <= 0.0f) {
					continue;
				}
				float normPrice = re.getPrice() / re.getSize();
				StringBuffer sb = new StringBuffer();
				sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append(KEY_SEPARATOR);
				sb.append(re.getFloor()); sb.append(KEY_SEPARATOR);
				sb.append(re.getNewlyBuilt());
				
				String key = sb.toString();
				
				if (transactionType.equals("SELL")) {
					Float f = sellSS1.get(areaID);
					if (f == null) {
						sellSS1.put(key, normPrice);
						sellSS0.put(key, 1.0f);
					}
					else {
						sellSS1.put(key, new Float(f.floatValue() + normPrice));
						Float g = sellSS0.get(key);
						sellSS0.put(key, new Float(g.floatValue() + 1.0f));
					}
				}
				
				if (transactionType.equals("RENT")) {
					Float f = rentSS1.get(key);
					if (f == null) {
						rentSS1.put(key, normPrice);
						rentSS0.put(key, 1.0f);
					}
					else {
						rentSS1.put(key, new Float(f.floatValue() + normPrice));
						Float g = rentSS0.get(key);
						rentSS0.put(key, new Float(g.floatValue() + 1.0f));
					}
				}
				
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		
		for (Map.Entry<String, Float> es : sellSS1.entrySet()) {
			String key = es.getKey();
			
			float avgSellPrice = es.getValue() / sellSS0.get(key);
			
			Float f = rentSS1.get(key);
			if (f == null) {
				conversion.put(key, 0.0f);
			}
			else {
				float avgRentPrice = f.floatValue() / rentSS0.get(key);
				
				float rate = avgSellPrice / avgRentPrice;
				conversion.put(key, rate);
			}
		}
	}
	
	public void train(File advFile) {
		logger.debug("Initiating training using " + advFile.getAbsolutePath());
		if (!advFile.exists()) {
			logger.error("ERROR: File " + advFile.getAbsolutePath() + " does not exist");
			System.exit(-1);
		}
		if (!advFile.canRead()) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(advFile));
			String logEntryLine;
			logEntryLine = in.readLine();
			HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
			String[] fieldNames = logEntryLine.split("\\t");
			for (int i = 0; i < fieldNames.length; i ++) {
				names2Index.put(fieldNames[i], new Integer(i));
			}
			SS0 = new HashMap<String, Float>();
			SS1 = new HashMap<String, Float>();
			while ((logEntryLine = in.readLine()) != null) {
				RealEstate re = new RealEstate(logEntryLine, names2Index);

				String areaID = re.getAreaID();
				String transactionType = re.getTransactionType();
				
				if (areaID.equalsIgnoreCase("NULL") || !transactionType.equalsIgnoreCase(this.transactionType) || 
						re.getPrice() <= 0.0f || re.getSize() <= 0.0f) {
//					logger.debug("Ignoring ad \"" + re.toString() + "\"");
					continue;
				}
/*				
				if (conversion.get(areaID) == null && transactionType.equals("RENT")) {
					continue;
				}
				float price = transactionType.equals("RENT") ? re.getPrice() * conversion.get(areaID) : re.getPrice();
				transactionType = "SELL";
*/
				float price = re.getPrice();
				String realestatesubtype = re.getRealEstateType();
				String mappedRealEstateSubtype = RealEstate.getMappedRealEstateType(realestatesubtype);
				String floor = re.getFloor();
				String mappedFloor = RealEstate.getMappedFloor(floor);
				String areaBand = getAreaBand(re.getSize());
				String newlyBuilt = Boolean.toString(re.getNewlyBuilt());
				String hasParking = Boolean.toString(re.hasParking());
				String hasGarden = Boolean.toString(re.hasGarden());
				String hasAutonomousHeating = Boolean.toString(re.hasAutonomousHeating());
				String hasPisina = Boolean.toString(re.hasPisina());
				
				StringBuffer sb = new StringBuffer();
				sb.append(transactionType); sb.append(KEY_SEPARATOR);
				sb.append(mappedRealEstateSubtype); sb.append(KEY_SEPARATOR);
				sb.append(mappedFloor); sb.append(KEY_SEPARATOR);
				sb.append(areaBand); sb.append(KEY_SEPARATOR); 
				sb.append(newlyBuilt); sb.append(KEY_SEPARATOR);
				sb.append(hasParking); sb.append(KEY_SEPARATOR);
				sb.append(hasGarden); sb.append(KEY_SEPARATOR);
				sb.append(hasAutonomousHeating); sb.append(KEY_SEPARATOR);
				sb.append(hasPisina);
				String keyWithoutAreaID = sb.toString();
				
				sb = new StringBuffer();
				sb.append(areaID); sb.append(KEY_SEPARATOR);
				sb.append(keyWithoutAreaID);
				
				String key = sb.toString();
				float normPrice = price / re.getSize();
				updateSufficientStatistics(key, normPrice);
				
				// Update global area statistics
				sb = new StringBuffer();
				sb.append(GLOBAL_AREA_ID); sb.append(KEY_SEPARATOR);
				sb.append(keyWithoutAreaID);
				String globalKey = sb.toString();
				updateSufficientStatistics(globalKey, normPrice);

				// Update parent area statistics
				for (String parentAreaID : getParents(areaID)) {
					sb = new StringBuffer();
					sb.append(parentAreaID); sb.append(KEY_SEPARATOR);
					sb.append(keyWithoutAreaID);
					
					String parentKey = sb.toString();
					updateSufficientStatistics(parentKey, normPrice);
				}			
				
				// Update fallback area statistics - generated by k-means or other way
				if (fallbackAreas != null) {
					String fallbackAreaID = fallbackAreas.get(areaID);
					if (fallbackAreaID != null) {
						sb = new StringBuffer();
						sb.append(fallbackAreaID); sb.append(KEY_SEPARATOR);
						sb.append(keyWithoutAreaID);
						
						String parentKey = sb.toString();
						updateSufficientStatistics(parentKey, normPrice);
					}
				}
				
				// Update statistics for the global area band cluster
				sb = new StringBuffer();
				sb.append(areaID); sb.append(KEY_SEPARATOR);
				sb.append(transactionType); sb.append(KEY_SEPARATOR);
				sb.append(mappedRealEstateSubtype); sb.append(KEY_SEPARATOR);
				sb.append(mappedFloor); sb.append(KEY_SEPARATOR);
				sb.append(GLOBAL_SIZE); sb.append(KEY_SEPARATOR); 
				sb.append(newlyBuilt); sb.append(KEY_SEPARATOR);
				sb.append(hasParking); sb.append(KEY_SEPARATOR);
				sb.append(hasGarden); sb.append(KEY_SEPARATOR);
				sb.append(hasAutonomousHeating); sb.append(KEY_SEPARATOR);
				sb.append(hasPisina);
				String fallbackKey = sb.toString();
				updateSufficientStatistics(fallbackKey, normPrice);
				
				// Update statistics for the global area band + global floor cluster
				sb = new StringBuffer();
				sb.append(areaID); sb.append(KEY_SEPARATOR);
				sb.append(transactionType); sb.append(KEY_SEPARATOR);
				sb.append(mappedRealEstateSubtype); sb.append(KEY_SEPARATOR);
				sb.append(GLOBAL_FLOOR); sb.append(KEY_SEPARATOR);
				sb.append(GLOBAL_SIZE); sb.append(KEY_SEPARATOR); 
				sb.append(newlyBuilt); sb.append(KEY_SEPARATOR);
				sb.append(hasParking); sb.append(KEY_SEPARATOR);
				sb.append(hasGarden); sb.append(KEY_SEPARATOR);
				sb.append(hasAutonomousHeating); sb.append(KEY_SEPARATOR);
				sb.append(hasPisina);
				fallbackKey = sb.toString();
				updateSufficientStatistics(fallbackKey, normPrice);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		logger.info("Finished training");
		
//		for (Map.Entry<String, Float> e : SS0.entrySet()) {
//			if (e.getKey().contains(("ALL"))) {
//				logger.debug(e.getKey() + " : " + e.getValue());
//			}
//		}
	}
	
	private void updateSufficientStatistics(String key, float value) {
		Float f0 = SS0.get(key);
		if (f0 == null) {
			SS0.put(key, new Float(1.0f));
		}
		else {
			SS0.put(key, new Float(f0.floatValue() + 1.0f));
		}
		
		Float f1 = SS1.get(key);
		
		if (f1 == null) {
			SS1.put(key, new Float(value));
		}
		else {
			SS1.put(key, new Float(f1.floatValue() + value));
		}	
	}
	
	private Prediction getFallbackPredictionByFallbackAreaID(RealEstate re) {
		if (fallbackAreas == null || fallbackAreas.get(re.getAreaID()) == null) {
			return getFallbackPredictionByGlobalAreaID(re); 
		}
		String fallbackAreaID = fallbackAreas.get(re.getAreaID());
		StringBuffer sb = new StringBuffer();
		sb.append(fallbackAreaID); sb.append(KEY_SEPARATOR); 
		sb.append(re.getTransactionType()); sb.append(KEY_SEPARATOR);
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append(KEY_SEPARATOR);
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append(KEY_SEPARATOR);
		String areaBand = getAreaBand(re.getSize()); 
		sb.append(areaBand); sb.append(KEY_SEPARATOR);  
		sb.append(re.getNewlyBuilt()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasParking());  sb.append(KEY_SEPARATOR);
		sb.append(re.hasGarden());
		String fallbackKey = sb.toString();
		
		Float m = mean.get(fallbackKey);
		if (m == null) {
			return getFallbackPredictionByGlobalAreaID(re);
		}
		float predictionValue = m * this.discountFactor;
		float confidence = 0.0f;
		
		return new Prediction(Prediction.Type.FALLBACK, predictionValue, confidence);
	}
	
	private Prediction getFallbackPredictionByParentAreaID(RealEstate re) {
		List<String> parentAreas = getParents(re.getAreaID());
		if (parentAreas.size() == 0) {
			return getFallbackPredictionByGlobalAreaID(re); 
		}
		String parentAreaID = parentAreas.get(0);
		StringBuffer sb = new StringBuffer();
		sb.append(parentAreaID); sb.append(KEY_SEPARATOR); 
		sb.append(re.getTransactionType()); sb.append(KEY_SEPARATOR);
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append(KEY_SEPARATOR);
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append(KEY_SEPARATOR);
		String areaBand = getAreaBand(re.getSize()); 
		sb.append(areaBand); sb.append(KEY_SEPARATOR);  
		sb.append(re.getNewlyBuilt()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasParking()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasGarden()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasAutonomousHeating()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasPisina());
		String fallbackKey = sb.toString();
		
		Float m = mean.get(fallbackKey);
		if (m == null) {
			return getFallbackPredictionByGlobalAreaID(re);
		}
		float predictionValue = m * this.discountFactor;
		float confidence = 0.2f;
		
		return new Prediction(Prediction.Type.FALLBACK, predictionValue, confidence);
	}
	
	private Prediction getFallbackPredictionByGlobalAreaID(RealEstate re) {
		StringBuffer sb = new StringBuffer();
		sb.append(GLOBAL_AREA_ID); sb.append(KEY_SEPARATOR); 
		sb.append(re.getTransactionType()); sb.append(KEY_SEPARATOR);
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append(KEY_SEPARATOR);
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append(KEY_SEPARATOR);
		String areaBand = getAreaBand(re.getSize()); 
		sb.append(areaBand); sb.append(KEY_SEPARATOR);  
		sb.append(re.getNewlyBuilt()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasParking()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasGarden()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasAutonomousHeating()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasPisina());
		String globalKey = sb.toString();
		
		Float m = mean.get(globalKey);
		if (m == null) {
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, 0.0f);
		}
		float predictionValue = m * this.discountFactor;
		float confidence = 0.0f;
		
		return new Prediction(Prediction.Type.GLOBAL, predictionValue, confidence);
	}
	
	private Prediction getFallbackPredictionBySize(RealEstate re) {
		StringBuffer sb = new StringBuffer();
		sb.append(re.getAreaID()); sb.append(KEY_SEPARATOR); 
		sb.append(re.getTransactionType()); sb.append(KEY_SEPARATOR);
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append(KEY_SEPARATOR);
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append(KEY_SEPARATOR);
		sb.append(GLOBAL_SIZE); sb.append(KEY_SEPARATOR); 
		sb.append(re.getNewlyBuilt()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasParking()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasGarden()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasAutonomousHeating()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasPisina());
		String fallbackKey = sb.toString();
		
		Float m = mean.get(fallbackKey);
		if (m == null) {
//			return getFallbackPredictionByParentAreaID(re);
			return getFallbackPredictionBySizeAndFloor(re);
		}
		float predictionValue = m * this.discountFactor;
		float confidence = 0.7f;
		
		return new Prediction(Prediction.Type.FALLBACK, predictionValue, confidence);
	}
	
	private Prediction getFallbackPredictionBySizeAndFloor(RealEstate re) {
		StringBuffer sb = new StringBuffer();
		sb.append(re.getAreaID()); sb.append(KEY_SEPARATOR); 
		sb.append(re.getTransactionType()); sb.append(KEY_SEPARATOR);
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append(KEY_SEPARATOR);
		sb.append(GLOBAL_FLOOR); sb.append(KEY_SEPARATOR);
		sb.append(GLOBAL_SIZE); sb.append(KEY_SEPARATOR); 
		sb.append(re.getNewlyBuilt()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasParking()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasGarden()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasAutonomousHeating()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasPisina());
		String fallbackKey = sb.toString();
		
		Float m = mean.get(fallbackKey);
		if (m == null) {
//			return getFallbackPredictionByParentAreaID(re);
			return getFallbackPredictionByFallbackAreaID(re);
		}
		float predictionValue = m * this.discountFactor;
		float confidence = 0.4f;
		
		return new Prediction(Prediction.Type.FALLBACK, predictionValue, confidence);
	}
	
	public void writeModel() throws Exception {
		File modelFile = models.get(0);
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(modelFile));
			String header = "CLUSTERID\tAREAID" + KEY_SEPARATOR + "TRANSACTIONTYPE" + KEY_SEPARATOR + "REALESTATETYPE" +
					KEY_SEPARATOR + "FLOOR" + KEY_SEPARATOR + "SIZEBAND" + KEY_SEPARATOR + "NEWLYBUILT" + KEY_SEPARATOR + 
					"PARKING" + KEY_SEPARATOR + "GARDEN" + KEY_SEPARATOR + "INDEPENDENTHEATING" + KEY_SEPARATOR + "POOL\tMEAN\tCOUNT";
			out.write(header + newline);
			int clusterId = 0;
			for (Map.Entry<String, Float> entry : SS1.entrySet()) {
				String key = entry.getKey();
				Float value = entry.getValue();
				
				Float count = SS0.get(key);
				if (count == null) {
					logger.error("Could not find count for \"" + key + "\" skipping");
					continue;
				}
				
				StringBuffer sb = new StringBuffer();
				sb.append(clusterId); sb.append("\t");
				sb.append(key); sb.append("\t");
				float mean = value / count.intValue();
				float roundedMean = (float)Math.floor(mean * 100.0f) / 100.0f;
				sb.append(roundedMean); sb.append("\t");
				int cnt = Math.round(count);
				sb.append(cnt); 
				/*
				sb.append("\t");
				sb.append(""); sb.append("\t");
				boolean doFallback = count < THRESHOLD ? true : false;
				sb.append(doFallback);
				*/
				sb.append(newline);
				
				out.write(sb.toString());
				clusterId ++;
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + modelFile.getAbsolutePath());
			throw new Exception();
		}
	}
	
	public void readModels() throws Exception {
		File modelsFile = models.get(0);
		if (!modelsFile.exists()) {
			logger.error("Model file " + modelsFile.getAbsolutePath() + " does not exist");
			throw new Exception();
		}
		if (!modelsFile.canRead()) {
			logger.error("Model file " + modelsFile.getAbsolutePath() + " cannot be read");
			throw new Exception();
		}
		mean = new HashMap<String, Float>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(modelsFile));
			String logEntryLine = in.readLine();
			while ((logEntryLine = in.readLine()) != null) {
				String[] fieldNames = logEntryLine.split("\\t");
				
				String key = fieldNames[1];
				
				if (mean.containsKey(key)) {
					logger.error("Key \"" + key + "\" has been encountered before in the model");
					throw new Exception();
				}
				
				float value = 0.0f;
				try {
					value = Float.parseFloat(fieldNames[2]);
				}
				catch (Exception ex) {
					logger.error("Cannot read value for key \"" + key + "\"");
					throw new Exception();
				}
				mean.put(key, value);
			}
		}
		catch (Exception ex) {
			logger.error("Problem while reading models file " + modelsFile.getAbsolutePath());
			throw new Exception();
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public Prediction predict(RealEstate re) {
		if (!re.getTransactionType().equalsIgnoreCase(this.transactionType) || 
				!re.getItemType().equalsIgnoreCase(this.itemType)) {
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, 0.0f, 0.0f);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(re.getTransactionType()); sb.append(KEY_SEPARATOR);
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append(KEY_SEPARATOR);
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append(KEY_SEPARATOR);
		String areaBand = getAreaBand(re.getSize()); 
		sb.append(areaBand); sb.append(KEY_SEPARATOR); 
		sb.append(re.getNewlyBuilt()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasParking()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasGarden()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasAutonomousHeating()); sb.append(KEY_SEPARATOR);
		sb.append(re.hasPisina());
		String keyWithoutAreaId = sb.toString();
		
		sb = new StringBuffer();
		sb.append(re.getAreaID()); sb.append(KEY_SEPARATOR);
		sb.append(keyWithoutAreaId);
		String key = sb.toString();
		
		Float m = mean.get(key);
		if (m == null) {
//			return getFallbackPredictionByParentAreaID(re);
//			return getFallbackPredictionByFallbackAreaID(re);
			return getFallbackPredictionBySize(re);
		}
		float predictionValue = m * this.discountFactor;
		float confidence = re.getRealEstateType().equalsIgnoreCase("APARTMENT") ? 1.0f : 0.4f;
		
		return new Prediction(Prediction.Type.MAIN, predictionValue, confidence);
		
	}
	
}
