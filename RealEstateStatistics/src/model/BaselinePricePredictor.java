package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaselinePricePredictor extends RealEstatePricePredictor{
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private static String GLOBAL_AREA = "ALL";
	private HashMap<String, Float> SS0;
	private HashMap<String, Float> SS1;
	private float THRESHOLD = 1.0f;
	
	private String getAreaBand(float sqMeters) {
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
	
	public void readModels() throws Exception {
		
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
			readAreas(advFile);
		}
		catch (Exception ex) {
			
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
				
				if (areaID.equalsIgnoreCase("N/A") || transactionType.equalsIgnoreCase("N/A") || 
						re.getPrice() <= 0.0f || re.getSize() <= 0.0f) {
					logger.debug("Ignoring ad \"" + re.toString() + "\"");
					continue;
				}

				float price = re.getPrice();
				String realestatesubtype = re.getRealEstateType();
				String mappedRealEstateSubtype = RealEstate.getMappedRealEstateType(realestatesubtype);
				String floor = re.getFloor();
				String mappedFloor = RealEstate.getMappedFloor(floor);
				String areaBand = getAreaBand(re.getSize());
				
				
				StringBuffer sb = new StringBuffer();
				sb.append(transactionType); sb.append("_");
				sb.append(mappedRealEstateSubtype); sb.append("_");
				sb.append(mappedFloor); sb.append("_");
				sb.append(areaBand); sb.append("_");
				sb.append(re.getNewlyBuilt());
				
				String keyWithoutAreaID = sb.toString();
				
				sb = new StringBuffer();
				sb.append(areaID); sb.append("_");
				sb.append(keyWithoutAreaID);
				
				String key = sb.toString();
				float normPrice = price / re.getSize();
				updateSufficientStatistics(key, normPrice);
				

				// Update global size band cluster
				sb = new StringBuffer();
				sb.append(areaID); sb.append("_");
				sb.append(transactionType); sb.append("_");
				sb.append(mappedRealEstateSubtype); sb.append("_");
				sb.append(mappedFloor); sb.append("_");
				sb.append("ALL"); sb.append("_");
				sb.append(re.getNewlyBuilt());
				
				key = sb.toString();
				updateSufficientStatistics(key, normPrice);
				
				
				// Update parent area statistics
				for (String parentAreaID : getParents(areaID)) {
					sb = new StringBuffer();
					sb.append(parentAreaID); sb.append("_");
					sb.append(transactionType); sb.append("_");
					sb.append(mappedRealEstateSubtype); sb.append("_");
					sb.append(mappedFloor); sb.append("_");
					sb.append("ALL"); sb.append("_");
					sb.append(re.getNewlyBuilt());
					
					String parentKey = sb.toString();
					updateSufficientStatistics(parentKey, normPrice);
				}			
				
				// Update global area statistics
				sb = new StringBuffer();
				sb.append(GLOBAL_AREA); sb.append("_");
				sb.append(transactionType); sb.append("_");
				sb.append(mappedRealEstateSubtype); sb.append("_");
				sb.append(mappedFloor); sb.append("_");
				sb.append("ALL"); sb.append("_");
				sb.append(re.getNewlyBuilt());
				key = sb.toString();
				updateSufficientStatistics(key, normPrice);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		logger.info("Finished training");
		
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
	
	
	private Prediction getFallbackPredictionByParentAreaID(RealEstate re) {
		List<String> parentAreas = getParents(re.getAreaID());
		if (parentAreas.size() == 0) {
			return getFallbackPredictionByGlobalAreaID(re);
		}
		String parentAreaID = parentAreas.get(0);
		StringBuffer sb = new StringBuffer();
		sb.append(parentAreaID); sb.append("_"); 
		sb.append(re.getTransactionType()); sb.append("_");
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append("_");
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append("_");
		String areaBand = getAreaBand(re.getSize()); 
		sb.append(areaBand); sb.append("_");
		sb.append(re.getNewlyBuilt());
		String fallbackKey = sb.toString();
		
		Float ss1 = SS1.get(fallbackKey);
		Float ss0 = SS0.get(fallbackKey);
		if (ss1 == null || ss0 == null || ss0 < THRESHOLD) {
			return getFallbackPredictionByGlobalAreaID(re);
		}
		if (ss0.floatValue() <= 0.0f) {
			return getFallbackPredictionByGlobalAreaID(re);
		}
		else {
			float a = ss1.floatValue();
			float b = ss0.floatValue();
			float predictionValue = a / b;
			return new Prediction(Prediction.Type.FALLBACK, predictionValue);
		}
	}
	
	private Prediction getFallbackPredictionByGlobalAreaID(RealEstate re) {
		StringBuffer sb = new StringBuffer();
		sb.append(GLOBAL_AREA); sb.append("_"); 
		sb.append(re.getTransactionType()); sb.append("_");
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append("_");
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append("_");
		String areaBand = getAreaBand(re.getSize()); 
		sb.append(areaBand); sb.append("_");
		sb.append(re.getNewlyBuilt());
		String fallbackKey = sb.toString();
		
		Float ss1 = SS1.get(fallbackKey);
		Float ss0 = SS0.get(fallbackKey);
		if (ss1 == null || ss0 == null || ss0 < THRESHOLD) {
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, 0.0f);
		}
		if (ss0.floatValue() <= 0.0f) {
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, 0.0f);
		}
		else {
			float a = ss1.floatValue();
			float b = ss0.floatValue();
			float predictionValue = a / b;
			return new Prediction(Prediction.Type.GLOBAL, predictionValue);
		}
	}
	
	private Prediction getFallbackPredictionByGlobalSizeBand(RealEstate re) {
		StringBuffer sb = new StringBuffer();
		sb.append(re.getAreaID()); sb.append("_"); 
		sb.append(re.getTransactionType()); sb.append("_");
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append("_");
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append("_");
		String areaBand = "ALL";
		sb.append(areaBand); sb.append("_");
		sb.append(re.getNewlyBuilt());
		String fallbackKey = sb.toString();
		
		Float ss1 = SS1.get(fallbackKey);
		Float ss0 = SS0.get(fallbackKey);
		if (ss1 == null || ss0 == null || ss0 < THRESHOLD) {
			return getFallbackPredictionByParentAreaID(re);
		}
		if (ss0.floatValue() <= 0.0f) {
			return getFallbackPredictionByParentAreaID(re);
		}
		else {
			float a = ss1.floatValue();
			float b = ss0.floatValue();
			float predictionValue = a / b;
			return new Prediction(Prediction.Type.FALLBACK, predictionValue);
		}
	}
	
	private String determineFallbackKey(String areaID, String transactionType, String realEstateType,
										String floor, String sizeBand, String newBuilt) {
		if (areaID.equals(GLOBAL_AREA)) {
			// No further fallback
			return null;
		}
		
		String fallbackKey = null;
		Float f = null;
		StringBuffer sb = new StringBuffer();
		if (!sizeBand.equals("ALL")) {
			// Fallback A : Use global size band
			sb = new StringBuffer();
			sb.append(areaID); sb.append("_");
			sb.append(transactionType); sb.append("_");
			sb.append(realEstateType); sb.append("_");
			sb.append(floor); sb.append("_");
			sb.append("ALL"); sb.append("_");
			sb.append(newBuilt);
			fallbackKey = sb.toString();
			f = SS0.get(fallbackKey);
		}
		
		if ( sizeBand.equals("ALL") || f == null || f < THRESHOLD) {
			// Fallback B : Use parent area band
			List<String> parentAreas = getParents(areaID);
			if (parentAreas.size() != 0) {
				String parentAreaID = parentAreas.get(0);
				sb = new StringBuffer();
				sb.append(parentAreaID); sb.append("_");
				sb.append(transactionType); sb.append("_");
				sb.append(realEstateType); sb.append("_");
				sb.append(floor); sb.append("_");
				sb.append("ALL"); sb.append("_");
				sb.append(newBuilt);
				fallbackKey = sb.toString();
			
				f = SS0.get(fallbackKey);
			}
			if ( f == null || f < THRESHOLD) {
				// Fallback C : Use global areaID
				sb = new StringBuffer();
				sb.append("ALL"); sb.append("_");
				sb.append(transactionType); sb.append("_");
				sb.append(realEstateType); sb.append("_");
				sb.append(floor); sb.append("_");
				sb.append("ALL"); sb.append("_");
				sb.append(newBuilt);
				fallbackKey = sb.toString();
				
				f = SS0.get(fallbackKey);
				
				if (f == null || f < THRESHOLD) {
					// Cannot make prediction
					fallbackKey = null;
				}
			}
		}
		
		sb = new StringBuffer();
		sb.append(areaID); sb.append("_");
		sb.append(transactionType); sb.append("_");
		sb.append(realEstateType); sb.append("_");
		sb.append(floor); sb.append("_");
		sb.append(sizeBand); sb.append("_");
		sb.append(newBuilt);
		String origKey = sb.toString();
		
		if (fallbackKey == null) {
			return fallbackKey;
		}
		if (fallbackKey.equals(origKey)) {
			return null;
		}
		return fallbackKey;
	}
	
	public void writeModel() throws Exception {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(this.models.get(0)));
			String header = "CLUSTERID,CLUSTERKEY,DOFALLBACK,FALLBACKID,CANMAKEPREDICTION,AREAID_TRANSACTIONTYPE_REALESTATETYPE_FLOOR_SIZEBAND_NEWLYBUILT,MEAN,COUNT";
			out.write(header + newline);
			int clusterID = 0;
			// Determine the set of areaIDs + parentAreaIDs + global area
			HashSet<String> allAreas = new HashSet<String>();
			allAreas.addAll(areas);
			allAreas.add(GLOBAL_AREA);
			
			String[] globalSizeBand = {"ALL"};
			String[] otherSizeBands = {"1-54", "55-74", "75-100", ">100", "ALL"};
			for (String areaID : allAreas) {
				String[] transactionTypes = {"LET.NORMAL", "SELL.NORMAL"};
				for (String transactionType : transactionTypes) {
					String[] realEstateTypes = {"diamerisma", "monokatoikia", "mezoneta"};
					for (String realEstateType : realEstateTypes) {
						for (String floor : RealEstate.floorValues) {
							String[] sizeBands = areaID.equals(GLOBAL_AREA) ? globalSizeBand : otherSizeBands;
							for (String sizeBand : sizeBands) {
								String[] newBuiltStates = {"true", "false"};
								for (String newBuilt : newBuiltStates) {
									StringBuffer sb = new StringBuffer();
									sb.append(areaID); sb.append("_");
									sb.append(transactionType); sb.append("_");
									sb.append(realEstateType); sb.append("_");
									sb.append(floor); sb.append("_");
									sb.append(sizeBand); sb.append("_");
									sb.append(newBuilt); 
									
									String key = sb.toString();
									
									sb.append(",");
									
									Float f = SS1.get(key);
									Float g = SS0.get(key);
									float roundedMean = 0.0f;
									int cnt = 0;
									if (f == null || g == null || g < THRESHOLD) {
										// Fallback
										String fallbackKey = determineFallbackKey(areaID, transactionType, 
												realEstateType, floor, sizeBand, newBuilt);
										roundedMean = f == null || g == null ? 0.0f : (float)Math.floor(f / g * 100.0f) / 100.0f;
										cnt = g == null ? 0 : Math.round(g);
										boolean doFallback = fallbackKey == null ? false : true;
										sb.append(doFallback); sb.append(",");
										sb.append(fallbackKey == null ? "" : fallbackKey); sb.append(",");
										boolean canMakePrediction = false;
										sb.append(canMakePrediction); sb.append(",");
									}
									else {
										float mean = f / g;
										roundedMean = (float)Math.floor(mean * 100.0f) / 100.0f;
										cnt = Math.round(g);
										boolean doFallback = false;
										sb.append(doFallback); sb.append(",");
										sb.append(""); sb.append(",");
										boolean canMakePrediction = true;
										sb.append(canMakePrediction); sb.append(",");
									}
									
									StringBuffer sb2 = new StringBuffer();
									sb2.append(clusterID); sb2.append(",");
									sb2.append(sb.toString());
									sb2.append(roundedMean); sb2.append(",");
									sb2.append(cnt); 
									sb2.append(newline);
									out.write(sb2.toString());
									
									clusterID ++;
								}
							}
						}
						
					}
				}
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + this.models.get(0).getAbsolutePath());
			throw new Exception();
		}
	}
	
	public Prediction predict(RealEstate re) {
		StringBuffer sb = new StringBuffer();
		sb.append(re.getTransactionType()); sb.append("_");
		sb.append(RealEstate.getMappedRealEstateType(re.getRealEstateType())); sb.append("_");
		String floor = re.getFloor();
		String mappedFloor = RealEstate.getMappedFloor(floor);
		sb.append(mappedFloor); sb.append("_");
		String areaBand = getAreaBand(re.getSize()); 
		sb.append(areaBand); sb.append("_");
		sb.append(re.getNewlyBuilt());
		String keyWithoutAreaId = sb.toString();
		
		sb = new StringBuffer();
		sb.append(re.getAreaID()); sb.append("_");
		sb.append(keyWithoutAreaId);
		String key = sb.toString();
		
		Float ss1 = SS1.get(key);
		Float ss0 = SS0.get(key);
		if (ss1 == null || ss0 == null || ss0 < THRESHOLD) {
			return getFallbackPredictionByGlobalSizeBand(re);
//			return getFallbackPredictionByParentAreaID(re);
		}
		if (ss0 > 0.0f) {
			float predictionValue = ss1 / ss0;
			return new Prediction(Prediction.Type.MAIN, predictionValue);
		}
		else {
			return getFallbackPredictionByGlobalSizeBand(re);
//			return getFallbackPredictionByParentAreaID(re);
		}
	}
}
