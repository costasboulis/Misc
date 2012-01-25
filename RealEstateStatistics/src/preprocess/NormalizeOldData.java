package preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;
import java.util.Map;
import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Map Panos' 2005-2009 data to a common format
 * 
 * @author user
 *
 */


public class NormalizeOldData {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private HashMap<String, String> floorMapper;
	private HashMap<String, String> typeMapper;
	private HashMap<String, String> subTypeMapper;
	private HashMap<String, String> stateMapper;
	private HashMap<String, String> substateMapper;
	private HashMap<Integer, HashMap<String, Integer>> rejectionMsgs;
	public static String newline = System.getProperty("line.separator");
	
	public NormalizeOldData() {
		floorMapper = new HashMap<String, String>();
		floorMapper.put("1", "L1"); floorMapper.put("2", "L2");
		floorMapper.put("3", "L3"); floorMapper.put("4", "L4");
		floorMapper.put("5", "L5"); floorMapper.put("6", "L6");
		floorMapper.put("7", "L7"); floorMapper.put("8", "L8");
		floorMapper.put("1ος", "L1"); floorMapper.put("2ος", "L2");
		floorMapper.put("3ος", "L3"); floorMapper.put("4ος", "L4");
		floorMapper.put("5ος", "L5"); floorMapper.put("6ος", "L6");
		floorMapper.put("7ος", "L7"); floorMapper.put("8ος", "L8");
		floorMapper.put("1ου", "L1"); floorMapper.put("2ου", "L2");
		floorMapper.put("3ου", "L3"); floorMapper.put("4ου", "L4");
		floorMapper.put("5ου", "L5"); floorMapper.put("6ου", "L6");
		floorMapper.put("7ου", "L7"); floorMapper.put("8ου", "L8");
		floorMapper.put("ισόγειο", "L0");
		floorMapper.put("ημιώροφος", "LH"); floorMapper.put("ημιώροφο", "LH"); floorMapper.put("ημιώροφη", "LH");
		floorMapper.put("υπερυψωμένο", "LHH"); floorMapper.put("υπερυψωμένη", "LHH"); floorMapper.put("υπερυψωμένος", "LHH");
		floorMapper.put("υπερυψωμένο ισόγειο", "LHH"); floorMapper.put("υπερυψωμένη ισόγεια", "LHH");
		floorMapper.put("υπόγειο", "S1"); floorMapper.put("υπόγεια", "S1"); floorMapper.put("υπόγειος", "S1");
		floorMapper.put("ημιυπόγειο", "SH"); floorMapper.put("ημιυπόγεια", "SH"); floorMapper.put("ημιυπόγειος", "SH");
		
		typeMapper = new HashMap<String, String>();
		typeMapper.put("διαμέρισμα", "APARTMENT");
		typeMapper.put("Διαμέρισμα", "APARTMENT");
		typeMapper.put("μονοκατοικία", "HOUSE");
		typeMapper.put("Μονοκατοικία", "HOUSE");
		typeMapper.put("μεζονέτα", "SPLIT_LEVEL");
		typeMapper.put("Μεζονέτα", "SPLIT_LEVEL");
		typeMapper.put("οικία", "ΟΙΚΕΙΑ");
		typeMapper.put("βίλα", "ΟΙΚΕΙΑ");
		typeMapper.put("Βίλα", "ΟΙΚΕΙΑ");
		typeMapper.put("κατoικία", "ΟΙΚΕΙΑ");
		typeMapper.put("κτίριο", "BUILDING");
		typeMapper.put("Κτίριο", "BUILDING");
		typeMapper.put("οροφοδιαμέρισμα", "APARTMENT");
		typeMapper.put("Οροφοδιαμέρισμα", "APARTMENT");
		typeMapper.put("ρετιρέ", "APARTMENT");
		typeMapper.put("Ρετιρέ", "APARTMENT");
		typeMapper.put("γκαρσονιέρα", "APARTMENT");
		typeMapper.put("Γκαρσονιέρα", "APARTMENT");
		typeMapper.put("δώμα", "APARTMENT");
		typeMapper.put("δυάρι", "APARTMENT");
		typeMapper.put("Δυάρι", "APARTMENT");
		typeMapper.put("τριάρι", "APARTMENT");
		typeMapper.put("Τριάρι", "APARTMENT");
		typeMapper.put("τεσσάρι", "APARTMENT");
		typeMapper.put("Τεσσάρι", "APARTMENT");
		typeMapper.put("studio", "APARTMENT");
		typeMapper.put("στούντιο", "APARTMENT");
		typeMapper.put("Στούντιο", "APARTMENT");
		typeMapper.put("Άλλο", "OTHER");
		typeMapper.put("άλλο", "OTHER");
		
		
		subTypeMapper = new HashMap<String, String>();
		subTypeMapper.put("οροφοδιαμέρισμα", "FLOORFLAT");
		subTypeMapper.put("Οροφοδιαμέρισμα", "FLOORFLAT");
		subTypeMapper.put("ρετιρέ", "PENTHOUSE");
		subTypeMapper.put("Ρετιρέ", "PENTHOUSE");
		subTypeMapper.put("γκαρσονιέρα", "SINGLEROOM");
		subTypeMapper.put("Γκαρσονιέρα", "SINGLEROOM");
		subTypeMapper.put("studio", "SINGLEROOM");
		subTypeMapper.put("στούντιο", "SINGLEROOM");
		subTypeMapper.put("Στούντιο", "SINGLEROOM");
		subTypeMapper.put("δώμα", "LOFT");
		
		stateMapper = new HashMap<String, String>();
		stateMapper.put("νεόδμητο", "NEWBUILT");
		stateMapper.put("καινούργιο", "NEWBUILT");
		stateMapper.put("καινούριο", "NEWBUILT");
		stateMapper.put("υπό κατασκευή", "UNDER_CONSTRUCTION");
		stateMapper.put("υπό ανέγερση", "UNDER_CONSTRUCTION");
		stateMapper.put("στα θεμέλια", "UNDER_CONSTRUCTION");
		stateMapper.put("στα τούβλα", "UNDER_CONSTRUCTION");
		stateMapper.put("ημιτελές", "UNFINISHED");
		stateMapper.put("ημιτελής", "UNFINISHED");
		stateMapper.put("ημιτελή", "UNFINISHED");
		stateMapper.put("μπετά", "UNDER_CONSTRUCTION");
		stateMapper.put("άλλη κατάσταση", "USED");
		stateMapper.put("παλαιό", "USED");
		stateMapper.put("άριστη κατάσταση", "USED");
		stateMapper.put("ανακαινισμένο", "USED");
		stateMapper.put("ανακαινισμένη", "USED");
		stateMapper.put("καλή κατάσταση", "USED");
		stateMapper.put("χρήζει ανακαίνισης", "USED");
		
		
		substateMapper = new HashMap<String, String>();
		substateMapper.put("άριστη κατάσταση", "PERFECT");
		substateMapper.put("ανακαινισμένο", "REFURBISHED");
		substateMapper.put("ανακαινισμένη", "REFURBISHED");
		substateMapper.put("καλή κατάσταση", "GOOD");
		substateMapper.put("χρήζει ανακαίνισης", "NEEDS_REPAIR");
	}
	
	private void addRejectionMsg(int year, String msg) {
		HashMap<String, Integer> hm = rejectionMsgs.get(year);
		if (hm == null) {
			hm = new HashMap<String, Integer>();
			rejectionMsgs.put(year, hm);
		}
		Integer cnt = hm.get(msg);
		if (cnt == null) {
			hm.put(msg, new Integer(0));
		}
		else {
			hm.put(msg, new Integer(cnt.intValue() + 1));
		}
	}
	
	public void normalize(File dir, File caCategMapping, File outFile) {
		HashMap<String, String> caCat2TransactionType = new HashMap<String, String>();
		HashMap<String, String> caCat2ItemType = new HashMap<String, String>();
		HashMap<String, String> caCat2GeoArea = new HashMap<String, String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(caCategMapping));
			logger.info("Reading CA categories mapping file " + caCategMapping.getAbsolutePath());
			String logEntryLine = in.readLine();
			while ((logEntryLine = in.readLine()) != null) {
				String [] fieldNames = logEntryLine.split(",");
				
				caCat2TransactionType.put(fieldNames[0], fieldNames[2]);
				caCat2ItemType.put(fieldNames[0], fieldNames[1]);
				caCat2GeoArea.put(fieldNames[0], fieldNames[3]);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("Cannot read from file " + caCategMapping.getAbsolutePath());
			System.exit(-1);
		}
		
		HashMap<String, Integer> numAds = new HashMap<String, Integer>();
		numAds.put("2005", new Integer(0));
		numAds.put("2006", new Integer(0));
		numAds.put("2007", new Integer(0));
		numAds.put("2008", new Integer(0));
		numAds.put("2009", new Integer(0));
		numAds.put("2010", new Integer(0));
		
		HashMap<String, Integer> numAdsParsed = new HashMap<String, Integer>();
		numAdsParsed.put("2005", new Integer(0));
		numAdsParsed.put("2006", new Integer(0));
		numAdsParsed.put("2007", new Integer(0));
		numAdsParsed.put("2008", new Integer(0));
		numAdsParsed.put("2009", new Integer(0));
		numAdsParsed.put("2010", new Integer(0));
		
		rejectionMsgs = new HashMap<Integer, HashMap<String, Integer>>();
		NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
		int numberOfAdsParsed = 0;
		int numberOfAds = 0;
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			
			if (!dir.isDirectory()) {
				logger.error("Expecting directory for " + dir.getAbsolutePath());
				System.exit(-1);
			}
			
			out.write("ADVID\tCREATION_DAY\tCREATION_MONTH\tCREATION_YEAR\tPUB_DAY\tPUB_MONTH\tPUB_YEAR\tTELEPHONE\tTRANSACTION_TYPE\tAREA_ID\tSIZE\tBEDROOMS\tFLOOR\tTYPE\tSUBTYPE\tSTATE\tSUBSTATE\tPARKING\tCONSTRUCTION_YEAR\tFROM_AGENT\tPRICE" + newline);
//			out.write("0\t0\t0\t0\t0\t0\t0\t0\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\t0\t0\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tPARKING\t0\tAAAAAAAAAAAAAAAAAAAAAAAAA\t0" + newline);
			for (File advFile : dir.listFiles()) {
				String filename = advFile.getName();
				if (!filename.contains("parsed_")) {
					logger.warn("Skipping file " + advFile.getAbsolutePath());
					continue;
				}
				String[] fields = filename.split("_");
				if (fields.length != 4) {
					logger.warn("Cannot parse filename, skipping file " + advFile.getAbsolutePath());
					continue;
				}
				String publicationDay = fields[1];
				String publicationMonth = fields[2];
				String publicationYear = fields[3].replaceAll(".txt", "");
				
				int pubYear = 0;
				try {
					pubYear = Integer.parseInt(publicationYear);
					if (pubYear < 2005 || pubYear > 2010) {
						continue;
					}
				}
				catch (Exception ex) {
					logger.warn("Cannot parse year for file " + advFile.getAbsolutePath());
					continue;
				}
				
				try {
					BufferedReader in = new BufferedReader(new FileReader(advFile));
					logger.info("Reading file " + advFile.getAbsolutePath());
					String logEntryLine;
					logEntryLine = in.readLine();
					logEntryLine = "ID	timestamp	advertCategory	areaTreeCode	offerType	type	areaCode	area	subArea	size	numOfBedRooms	floor	description	subtype	buildingState	price	parking	year	availability	rooms	inplan	buildingFactor	lettingAvailability	road	houseNumber	x	y	accuracy	telephone	free	publishDate";
					HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
					String[] fieldNames = logEntryLine.split("\\t");
					for (int i = 0; i < fieldNames.length; i ++) {
						names2Index.put(fieldNames[i], new Integer(i));
					}
					
					
					while ((logEntryLine = in.readLine()) != null) {
						numberOfAds = numAds.get(publicationYear);
						numAds.put(publicationYear, new Integer(numberOfAds + 1));
						
						fieldNames = logEntryLine.split("\\t");
						
						int indx = 0;
						try {
							indx = Integer.parseInt(fieldNames[names2Index.get("ID")]);
						}
						catch (Exception ex) {
//							logger.warn("INVALID ADV_ID . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "INVALID_ADV_ID");
							continue;
						}
						long creationTimestamp = Long.parseLong(fieldNames[names2Index.get("timestamp")]);
						Date creationDate = new Date(creationTimestamp);
						int creationDay = creationDate.getDate();
						int creationMonth = creationDate.getMonth() + 1;
						int creationYear = creationDate.getYear() + 1900;
						
						String caCategory = fieldNames[names2Index.get("areaCode")];
						String itemType = caCat2ItemType.get(caCategory);
						String transactionType = caCat2TransactionType.get(caCategory);
						String geoAreaID = caCat2GeoArea.get(caCategory);
						
						if (caCategory == null) {
//							logger.warn("NO_CA_CATEGORY . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_CA_CATEGORY");
							continue;
						}
						else if (itemType == null || !itemType.equals("re_residence")) {
//							logger.warn("NO_ITEM_TYPE or NO_GEO_AREA_ID . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_OR_WRONG_ITEM_TYPE");
							continue;
						}
						else if (transactionType == null) {
//							logger.warn("NO_TRANSACTION_TYPE . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_TRANSACTION_TYPE");
							continue;
						}
						else if (geoAreaID == null) {
//							logger.warn("NO_GEO_AREA_ID . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_GEO_AREA_ID");
							continue;
						}
						String size = fieldNames[names2Index.get("size")];
						float sizeFloat = 0.0f;
						if (!size.equals("null")) {
							Number n = null;
							try {
								n = nf.parse(size);
							}
							catch (Exception ex) {
//								logger.warn("INVALID_SIZE " + size + " . Rejecting line \"" + logEntryLine + "\"");
								continue;
							}
							if (n == null || n.floatValue() <= 0.0f) {
								size = "N/A";
							}
							else {
								sizeFloat = n.floatValue();
							}
						}
						else {
							size = "N/A";
						}
						
						if (size.equals("N/A")) {
//							logger.warn("INVALID_OR_MISSING_SIZE . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_OR_INVALID_SIZE");
							continue;
						}
						
						String numBedrooms = fieldNames[names2Index.get("numOfBedRooms")];
						if (!numBedrooms.equals("null")) {
							int numOfBedRooms = 0;
							try {
								numOfBedRooms = Integer.parseInt(fieldNames[names2Index.get("numOfBedRooms")]);
							}
							catch (Exception ex) {
//								logger.debug("INVALID_NUMBER_OF_BEDROOMS . Rejecting line \"" + logEntryLine + "\"");
								continue;
							}
							if (numOfBedRooms <= 0) {
								numBedrooms = "N/A";
							}
						}
						else {
							numBedrooms = "N/A";
						}
						
						String price = fieldNames[names2Index.get("price")];
						float priceFloat = 0.0f;
						if (!price.equals("null") && price.length() != 0) {
							Number n = null;
							try {
								n = nf.parse(price);
							}
							catch (Exception ex) {
//								logger.debug("INVALID_PRICE " + price + " . Rejecting line \"" + logEntryLine + "\"");
//								continue;
							}
							if (n == null || n.floatValue() <= 0.0f) {
								price = "N/A";
							}
							else {
								priceFloat = n.floatValue();
							}
						}
						else {
							price = "N/A";
						}
						if (price.equals("N/A")) {
//							logger.debug("INVALID_OR_MISSING_PRICE . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_OR_INVALID_PRICE");
							continue;
						}
						String telephone = fieldNames[names2Index.get("telephone")].trim();
						if (telephone.equals("null") || telephone.length() < 10 || telephone.length() > 15) {
//							logger.warn("INVALID_TELEPHONE " + telephone + " . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_OR_INVALID_TELEPHONE");
							continue;
						}
						
						String buildingState = fieldNames[names2Index.get("buildingState")].trim();
						String normState = stateMapper.get(buildingState) == null ? "N/A" : stateMapper.get(buildingState);
						String normSubstate = substateMapper.get(buildingState) == null ? "N/A" : substateMapper.get(buildingState);
						
						String floor = fieldNames[names2Index.get("floor")].trim();
						HashSet<String> t = new HashSet<String>();
						for (String f : floor.split("-")) {
							String fl = f.trim();
							String normFloor = floorMapper.get(fl) == null ? "N/A" : floorMapper.get(fl);
							t.add(normFloor);
						}
						
						int i = 0;
						StringBuffer normFloorList = new StringBuffer();
						for (String f : t) {
							if (t.size() > 1 && f.equals("N/A")) {
								continue;
							}
							normFloorList.append(f); 
							if (i < t.size()-1) {
								normFloorList.append(",");
							}
							i ++;
						}
						
						
						String type = fieldNames[names2Index.get("subtype")].trim();
						String normType = typeMapper.get(type) == null ? "N/A" : typeMapper.get(type);
						if (normType.equals("N/A")) {
//							logger.warn("INVALID_OR_MISSING_TYPE . Rejecting line \"" + logEntryLine + "\"");
							addRejectionMsg(pubYear, "NO_OR_INVALID_REAL_ESTATE_TYPE");
							continue;
						}
						String normSubtype = subTypeMapper.get(type) == null ? "N/A" : subTypeMapper.get(type);
						
						String tmp = fieldNames[names2Index.get("parking")];
						boolean parking = tmp.equals("1") ? true : false;
						String year = fieldNames[names2Index.get("year")].equals("null") ? "N/A" : fieldNames[names2Index.get("year")];
						if (year.length() < 2 || year.length() > 4) {
							year = "N/A";
						}
						if (year.length() == 2) {
							if (year.charAt(0) == '0') {
								year = "20" + year;
							}
							else {
								year = "19" + year;
							}
						}
						
						
						StringBuffer sb = new StringBuffer();
						sb.append(indx); sb.append("\t");
						sb.append(creationDay); sb.append("\t");
						sb.append(creationMonth); sb.append("\t");
						sb.append(creationYear); sb.append("\t");
						sb.append(publicationDay); sb.append("\t");
						sb.append(publicationMonth); sb.append("\t");
						sb.append(publicationYear); sb.append("\t");
						sb.append(telephone); sb.append("\t");
						sb.append(transactionType); sb.append("\t");
						sb.append(geoAreaID); sb.append("\t");
						sb.append(sizeFloat <= 0.0f ? "N/A" : sizeFloat); sb.append("\t");
						sb.append(numBedrooms); sb.append("\t");
						sb.append(normFloorList.toString()); sb.append("\t");
						sb.append(normType); sb.append("\t");
						sb.append(normSubtype); sb.append("\t");
						sb.append(normState); sb.append("\t");
						sb.append(normSubstate); sb.append("\t");
						sb.append(parking); sb.append("\t");
						sb.append(year); sb.append("\t");
						boolean isAgent = Boolean.parseBoolean(fieldNames[names2Index.get("free")]) ? true : false; // Panos has made an error here and "free" is "agent"
						sb.append(isAgent); sb.append("\t");
						sb.append(priceFloat <= 0.0f ? "N/A" : priceFloat);
						sb.append(newline);
						
						out.write(sb.toString());
						
						numberOfAdsParsed = numAdsParsed.get(publicationYear);
						numAdsParsed.put(publicationYear, new Integer(numberOfAdsParsed + 1));
					}
					in.close();
				}
				catch (Exception ex) {
					logger.error("Cannot read from file " + advFile.getAbsolutePath());
					System.exit(-1);
				}
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to file " + outFile.getAbsolutePath());
			System.exit(-1);
		}
		
		String[] years = {"2005", "2006", "2007", "2008", "2009"};
		for (String year : years) {
			logger.info("Total number of pubs for " + year + " : " + numAds.get(year));
			logger.info("Number of parsed pubs for " + year + " : " + numAdsParsed.get(year));
		}
		
		for (int year = 2005; year <= 2009; year ++) {
			HashMap<String, Integer> msgs = rejectionMsgs.get(year);
			for (Map.Entry<String, Integer> es : msgs.entrySet()) {
				String rejMsg = es.getKey();
				int cnt = es.getValue();
				
				logger.info("YEAR : " + year + " MESSAGE : " + rejMsg + " COUNT : " + cnt);
			}
		}
		
	}
	public static void main(String[] argv) {
		String advDir = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\fromPanos\\parsed";
		String catCategories = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\old2New.csv";
		String newData = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\residences_2005_2009.txt";
		
		NormalizeOldData ad = new NormalizeOldData();
		ad.normalize(new File(advDir), new File(catCategories), new File(newData));
		
	}
}
