package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.io.PrintWriter;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.functions.LinearRegression;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.FastVector;


public class AreaLinearRegression2 extends RealEstatePricePredictor {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static int REGRESSION_THRESHOLD = 1;
	private static String TRANSACTION_TYPE = "SELL";
	private final static String GLOBAL_AREA_ID = "ALL";
	private final static String KEY_SEPARATOR = ";";
	private HashMap<String, Float> normalizationSS0;
	private HashMap<String, Float> normalizationSS1;
	private HashMap<String, Float> mean;
	private Instances instances;
	private Classifier model;
	
	public AreaLinearRegression2() {
		
	}
	
	public void readModels() throws Exception {
		for (File f : models) {
			if (!f.exists()) {
				logger.error("Model file " + f.getAbsolutePath() + " does not exist");
				throw new Exception();
			}
			if (!f.canRead()) {
				logger.error("Model file " + f.getAbsolutePath() + " cannot be read");
				throw new Exception();
			}
		}
		readClusterModel(models.get(0));
		model = (Classifier) weka.core.SerializationHelper.read(models.get(1).getAbsolutePath());
	}
	
	private void readClusterModel(File f) throws Exception {
		mean = new HashMap<String, Float>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(f));
			String logEntryLine;
			while ((logEntryLine = in.readLine()) != null) {
				String[] fieldNames = logEntryLine.split("\\t");
				
				String key = fieldNames[0];
				
				float m = 0.0f;
				try {
					m = Float.parseFloat(fieldNames[1]);
				}
				catch (Exception ex) {
					logger.error("Cannot read value " + fieldNames[1] + " of key \"" + key + "\"");
					throw new Exception();
				}
				
				if (mean.containsKey(key)) {
					logger.error("Already seen key \"" + key + "\"");
					throw new Exception();
				}
				
				mean.put(key, m);
			}
		}
		catch (Exception ex) {
			logger.error("Cannot read file " + f.getAbsolutePath());
			throw new Exception();
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
		
	}
	
	
	private void estimateNormalization(File advFile) {
		normalizationSS0 = new HashMap<String, Float>();
		normalizationSS1 = new HashMap<String, Float>();
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
			
			while ((logEntryLine = in.readLine()) != null) {
				RealEstate re = new RealEstate(logEntryLine, names2Index);
				
				if (re.getPrice() <= 0.0f || re.getSize() <= 0.0f || re.getAreaID().equalsIgnoreCase("N/A") ||
						!re.getTransactionType().equalsIgnoreCase(this.transactionType)) {
					continue;
				}
				float price = re.getPrice();
				float size = re.getSize();
				
				String areaID = re.getAreaID();
				String transactionType = re.getTransactionType();
				String realestatesubtype = re.getRealEstateType();
				String mappedRealEstateSubtype = RealEstate.getMappedRealEstateType(realestatesubtype);
				
				StringBuffer sb = new StringBuffer();
				sb.append(transactionType); sb.append(KEY_SEPARATOR);
				sb.append(mappedRealEstateSubtype); sb.append(KEY_SEPARATOR);
				sb.append(re.getState()); 
				String keyWithoutAreaID = sb.toString();
				
				sb = new StringBuffer();
				sb.append(areaID); sb.append(KEY_SEPARATOR);
				sb.append(keyWithoutAreaID);
				
				String key = sb.toString();
				float normPrice = price / size;
				
				// Update main statistics
				updateClusterStatistics(key, normPrice);
				
				// Update parent areas
				for (String parentAreaID : getParents(areaID)) {
					sb = new StringBuffer();
					sb.append(parentAreaID); sb.append(KEY_SEPARATOR);
					sb.append(keyWithoutAreaID);
					
					String parentKey = sb.toString();
					updateClusterStatistics(parentKey, normPrice);
				}		
				
				// Update for global area ID
				sb = new StringBuffer();
				sb.append(GLOBAL_AREA_ID); sb.append(KEY_SEPARATOR);
				sb.append(keyWithoutAreaID);
				String globalKey = sb.toString();
				
				updateClusterStatistics(globalKey, normPrice);
			}
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	
	private void updateClusterStatistics(String key, float value) {
		Float f0 = normalizationSS0.get(key);
		if (f0 == null) {
			normalizationSS0.put(key, new Float(1));
		}
		else {
			normalizationSS0.put(key, new Float(f0.floatValue() + 1.0f));
		}
		
		Float f1 = normalizationSS1.get(key);
		if (f1 == null) {
			normalizationSS1.put(key, new Float(value));
		}
		else {
			normalizationSS1.put(key, new Float(f1.floatValue() + value));
		}
	}
	
	private void writeClusterModel(File f) throws Exception {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(f));
			for (String key : normalizationSS0.keySet()) {
				float x = normalizationSS0.get(key);
				if (!normalizationSS1.containsKey(key)) {
					logger.error("Error calculating the cluster value for key \"" + key + "\"");
					throw new Exception();
				}
				float y = normalizationSS1.get(key);
				
				float mean = y / x;
				float roundedMean = (float)Math.floor(mean * 100.0f) / 100.0f;
				
				StringBuffer sb = new StringBuffer();
				sb.append(key); sb.append("\t"); sb.append(roundedMean); sb.append("\t"); sb.append(Math.round(x));
				sb.append(newline);
				
				out.write(sb.toString());
			}
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + f.getAbsolutePath());
			throw new Exception();
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	private void writeRegressionModel(File f) throws Exception {
		if (model == null) {
			logger.error("No regression model exists to write");
			throw new Exception();
		}
		weka.core.SerializationHelper.write(f.getAbsolutePath(), model);
	}
	
	public void writeModel() throws Exception {
		if (models.size() != 2) {
			logger.error("Expecting 2 models but " + models.size() + " seen");
			throw new Exception();
		}
		
		writeClusterModel(models.get(0));
		writeRegressionModel(models.get(1));
	}
	
	private void createModel(File advFile) {
		if (!advFile.exists()) {
			logger.error("ERROR: File " + advFile.getAbsolutePath() + " does not exist");
			System.exit(-1);
		}
		if (!advFile.canRead()) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		logger.info("Reading data " + advFile.getAbsolutePath());
		
		Attribute att1 = new Attribute("PricePerSquareMeter");
		Attribute att2 = new Attribute("SquareMeters");
//		Attribute att3 = new Attribute("IsNewlyBuilt");
		Attribute att4 = new Attribute("HasParking");
		Attribute att5 = new Attribute("HasGarden");
		Attribute att6 = new Attribute("HasAutonomousHeating");
		Attribute att7 = new Attribute("HasPisina");
		Attribute att8 = new Attribute("NumberOfYears");
		Attribute att9 = new Attribute("HasView");
		Attribute att10 = new Attribute("IsAgent");
		Attribute att11 = new Attribute("HasSunVisors");
		Attribute att12 = new Attribute("HasAC");
		Attribute att13 = new Attribute("HasSunBoiler");
		Attribute att14 = new Attribute("IsNearMetro");
		Attribute att15 = new Attribute("HasNaturalGas");
		Attribute att16 = new Attribute("HasPrivateRoof");
		Attribute att17 = new Attribute("REFURBISHED"); 
		Attribute att18 = new Attribute("PERFECT"); 
		Attribute att19 = new Attribute("GOOD");
		Attribute att20 = new Attribute("FLOORFLAT"); 
		Attribute att21 = new Attribute("PENTHOUSE"); 
		Attribute att22 = new Attribute("SINGLEROOM"); 
		Attribute att23 = new Attribute("LOFT"); 
		Attribute att24 = new Attribute("BEDROOMS"); 
		Attribute[] att = new Attribute[RealEstate.floorValues.length];
		for (int i = 0; i < RealEstate.floorValues.length; i ++) {
			att[i] = new Attribute("FLOOR_" + RealEstate.floorValues[i]);
		}
		
		FastVector fv = new FastVector(23 + RealEstate.floorValues.length);
		fv.addElement(att1);
		fv.addElement(att2);
//		fv.addElement(att3);
		fv.addElement(att4);
		fv.addElement(att5);
		fv.addElement(att6);
		fv.addElement(att7);
		fv.addElement(att8);
		fv.addElement(att9);
		fv.addElement(att10);
		fv.addElement(att11);
		fv.addElement(att12);
		fv.addElement(att13);
		fv.addElement(att14);
		fv.addElement(att15);
		fv.addElement(att16);
		fv.addElement(att17);
		fv.addElement(att18);
		fv.addElement(att19);
		fv.addElement(att20);
		fv.addElement(att21);
		fv.addElement(att22);
		fv.addElement(att23);
		fv.addElement(att24);
		for (int i = 0; i < RealEstate.floorValues.length; i ++) {
			fv.addElement(att[i]);
		}
		
		instances = new Instances("price-size-data", fv, 100000);
		instances.setClassIndex(0);
		
		estimateNormalization(advFile);
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
				
				if (re.getPrice() <= 0.0f || re.getSize() <= 0.0f || re.getAreaID().equalsIgnoreCase("N/A") ||
						!super.areas.contains(re.getAreaID()) || re.getTransactionType().equalsIgnoreCase("N/A")) {
					continue;
				}
				if (!re.getTransactionType().equals(TRANSACTION_TYPE)) {
					continue;
				}
				float price = re.getPrice();
				float size = re.getSize();
				
				String areaID = re.getAreaID();
				String transactionType = re.getTransactionType();
				StringBuffer sb = new StringBuffer();
				sb.append(areaID); sb.append(KEY_SEPARATOR);
				sb.append(transactionType); sb.append(KEY_SEPARATOR);
				String mappedType = RealEstate.getMappedRealEstateType(re.getRealEstateType());
				sb.append(mappedType); sb.append(KEY_SEPARATOR);
				sb.append(re.getState()); 
				
				String key = sb.toString();
				
				Float ss1 = normalizationSS1.get(key);
				if (ss1 == null) {
					continue;
				}
				float ss0 = normalizationSS0.get(key);
				
				float normPrice = (price / size) - (ss1 / ss0);
//				float normPrice = (float)Math.log((price / size)) - (float)Math.log((ss1 / ss0));
				addInstance(normPrice, re);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		logger.info("Finished reading data " + advFile.getAbsolutePath());
		
		model = new LinearRegression();
		try {
			model.buildClassifier(instances);
		}
		catch (Exception ex) {
			logger.error("Error when training the model");
			System.exit(-1);
		}
		
		logger.debug(model.toString());
		
	}
	
	private Instance createInstance(RealEstate re, float val) {
		Instance inst = new Instance(23 + RealEstate.floorValues.length);
		
		inst.setValue(0, val);
		inst.setValue(1, re.getSize() > 150.0f ? 150.0f : re.getSize());
		inst.setValue(2, re.hasParking() ? 1 : 0);
		inst.setValue(3, re.hasGarden() ? 1 : 0);
		inst.setValue(4, re.hasAutonomousHeating() ? 1 : 0);
		inst.setValue(5, re.hasPisina() ? 1 : 0);
		inst.setValue(6, re.getNumberOfYears() >= 100 ? 100 : re.getNumberOfYears());
		if (re.getNumberOfYears() == 0) {
			inst.setMissing(6);
		}
		inst.setValue(7, re.hasView() ? 1 : 0);
		inst.setValue(8, re.isAgent() ? 1 : 0);
		inst.setValue(9, re.hasSunVisors() ? 1 : 0);
		inst.setValue(10, re.hasAC() ? 1 : 0);
		inst.setValue(11, re.hasSunBoiler() ? 1 : 0);
		inst.setValue(12, re.isNearMetro() ? 1 : 0);
		inst.setValue(13, re.hasNaturalGas() ? 1 : 0);
		inst.setValue(14, re.hasPrivateRoof() ? 1 : 0);
		inst.setValue(15, re.getSubstate().equals("REFURBISHED") ? 1 : 0); 
		inst.setValue(16, re.getSubstate().equals("PERFECT") ? 1 : 0);
		inst.setValue(17, re.getSubstate().equals("GOOD") ? 1 : 0);
		inst.setValue(18, re.getRealEstateSubtype().equals("FLOORFLAT") ? 1 : 0);
		inst.setValue(19, re.getRealEstateSubtype().equals("PENTHOUSE") ? 1 : 0);
		inst.setValue(20, re.getRealEstateSubtype().equals("SINGLEROOM") ? 1 : 0);
		inst.setValue(21, re.getRealEstateSubtype().equals("LOFT") ? 1 : 0);
		inst.setValue(22, re.getBedrooms());
		if (re.getBedrooms() == 0) {
			inst.setMissing(22);
		}
		for (int i = 0; i < RealEstate.floorValues.length; i ++) {
			inst.setValue(23 + i, RealEstate.floorValues[i].equals(re.getFloor()) ? 1.0f : 0.0f);
		}
		
		return inst;
	}
	
	private void addInstance(float valueA, RealEstate re) {
		Instance inst = createInstance(re, valueA);
		inst.setDataset(instances);
		
		instances.add(inst);
	}
	
	public Prediction predict(RealEstate re) {
		if (!re.getTransactionType().equalsIgnoreCase(this.transactionType) || 
				!re.getItemType().equalsIgnoreCase(this.itemType)) {
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, 0.0f, 0.0f);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(re.getTransactionType()); sb.append(KEY_SEPARATOR);
		String reType = RealEstate.getMappedRealEstateType(re.getRealEstateType());
		sb.append(reType); sb.append(KEY_SEPARATOR);
		sb.append(re.getState()); 
		String keyWithoutAreaID = sb.toString();
		
		sb = new StringBuffer();	
		sb.append(re.getAreaID()); sb.append(KEY_SEPARATOR);
		sb.append(keyWithoutAreaID);
		String key = sb.toString();
		
		Prediction.Type type;
		float confidence = 0.0f;
		
		Float f = mean.get(key);
		if (f == null) {
			// Use parent areaID
			List<String> parentAreas = getParents(re.getAreaID());
			if (parentAreas.size() == 0) {
//				logger.debug("Using global cluster for " + re.toString());
				sb = new StringBuffer();
				sb.append(GLOBAL_AREA_ID); sb.append(KEY_SEPARATOR);
				sb.append(keyWithoutAreaID);
				key = sb.toString();
				type = Prediction.Type.GLOBAL;
				confidence = 0.0f;
			}
			else {
				String parentAreaID = parentAreas.get(0);
				sb = new StringBuffer();
				sb.append(parentAreaID); sb.append(KEY_SEPARATOR); 
				sb.append(keyWithoutAreaID);
				key = sb.toString();
				
				f = mean.get(key);
				
				if (f == null) {
//					logger.debug("Using global cluster for " + re.toString());
					sb = new StringBuffer();
					sb.append(GLOBAL_AREA_ID); sb.append(KEY_SEPARATOR);
					sb.append(keyWithoutAreaID);
					key = sb.toString();
					type = Prediction.Type.GLOBAL;
					confidence = 0.0f;
				}
				else {
//					logger.debug("Using fallback cluster for " + re.toString());
					type = Prediction.Type.FALLBACK;
					confidence = 0.4f;
				}
			}
		}
		else {
			type = Prediction.Type.MAIN;
			confidence = re.getRealEstateType().equalsIgnoreCase("APARTMENT") ? 1.0f : 0.4f;
		}
		
		f = mean.get(key);
		if (f == null) {
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, 0.0f, 0.0f);
		}
		
		Instance inst = createInstance(re, 0.0f);
		
		float tmp = 0.0f;
		try {
			tmp = (float)model.classifyInstance(inst);
		}
		catch (Exception ex) {
			logger.error("Cannot make prediction for " + re.toString());
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, f, 0.0f);
		}
		
		float prediction = this.discountFactor * (tmp + f);
		
//		float confidence = normalizationSS0.get(key) >= 100 ? 1.0f : normalizationSS0.get(key) / 100.0f;
		
		return new Prediction(type, prediction, confidence);
	}
	
	public void train(File advFile) {
		createModel(advFile);
	}
	
}
