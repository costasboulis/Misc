package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StatisticsCalculator {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private static int THRESHOLD = 30;   // threshold to calculate median variance
	private HashMap<String, HashMap<String, List<Float>>> data;  // AREAID_TRANSACTIONTYPE_NEWBUILT -> {MEASURE , list of data points}
	private HashMap<String, HashSet<String>> parents;
	private HashMap<String, String> firstParent;
	
	public void loadParents(File parentsFile) {
		parents = new HashMap<String, HashSet<String>>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(parentsFile));
			String logEntryLine;
			while ((logEntryLine = in.readLine()) != null) {
				String[] fields = logEntryLine.split(",");
				
				String srcArea = fields[0];
				String tgtArea = fields[1];
				
				HashSet<String> hs = parents.get(srcArea);
				if (hs == null) {
					hs = new HashSet<String>();
					parents.put(srcArea, hs);
				}
				hs.add(tgtArea);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("Cannot read from " + parentsFile.getAbsolutePath());
			System.exit(-1);
		}
		
	}
	
	public void createParents(File areasFile) {
		HashMap<String, String> geoIDs = new HashMap<String, String>();
		parents = new HashMap<String, HashSet<String>>();
		firstParent = new HashMap<String, String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(areasFile));
			String logEntryLine;
			while ((logEntryLine = in.readLine()) != null) {
				String[] fields = logEntryLine.split(",");
				
				String geoID = fields[0];
				
				StringBuffer sb = new StringBuffer();
				for (int i = 1; i < fields.length - 1; i ++) {
					sb.append(fields[i]); sb.append(",");
				}
				sb.append(fields[fields.length - 1]);
				
				geoIDs.put(sb.toString(), geoID);
				parents.put(geoID, new HashSet<String>());
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("Cannot read from " + areasFile.getAbsolutePath());
			System.exit(-1);
		}
		
		for (String s : geoIDs.keySet()) {
			String[] fields = s.split(",");
			String srcGeoKey = geoIDs.get(s);
			HashSet<String> hs = parents.get(srcGeoKey);
			
			if (fields.length <= 1) {
				continue;
			}
			
			String fParent = fields[0];
			firstParent.put(geoIDs.get(s), fParent);
			
			for (int i = fields.length - 2; i >= 0; i --) {
				String geoKey = fields[i];
				for (int j = i - 1; j >= 0; j --) {
					geoKey = fields[j] + "," + geoKey;
				}
				String tgtGeoKey = geoIDs.get(geoKey);
				if (tgtGeoKey == null || tgtGeoKey.equals(srcGeoKey)) {
					continue;
				}
				hs.add(tgtGeoKey);
			}
		}
	}
	
	public void writeParents(File outFile) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			for (Map.Entry<String, HashSet<String>> prnts : parents.entrySet()) {
				for (String p : prnts.getValue()) {
					out.write(prnts.getKey() + "," + p + newline);
				}
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + outFile.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	private void updateStatistics(String areaID, String transactionType, String newBuilt, String isAgent, float pricePerSquareMeter, float price) {		
		StringBuffer sb = new StringBuffer();
		sb.append(areaID); sb.append("_");
		sb.append(transactionType); sb.append("_");
		sb.append(newBuilt); sb.append("_");
		sb.append(isAgent);
		
		String key = sb.toString();
		
		String l = "PricePerSquareMeter";
		HashMap<String, List<Float>> m = data.get(key);
		if (m == null) {
			m = new HashMap<String, List<Float>>();
			data.put(key, m);
		}
		List<Float> d = m.get(l);
		if (d == null) {
			d = new LinkedList<Float>();
			m.put(l, d);
		}
		d.add(pricePerSquareMeter);	
		
		
		l = "Price";
		m = data.get(key);
		if (m == null) {
			m = new HashMap<String, List<Float>>();
			data.put(key, m);
		}
		d = m.get(l);
		if (d == null) {
			d = new LinkedList<Float>();
			m.put(l, d);
		}
		d.add(price);
	}
	
	private void calculateMeasures(List<Float> data, List<Float> measures) {
		Collections.sort(data);
		
		int middle = (int)Math.floor((float)data.size() / 2.0f);
		float median = data.get(middle);
		
		measures.add(median);
		float mean = 0.0f;
		for (Float f : data) {
			mean += f;
		}
		mean /= (float)data.size();
		float rndMean = (float)Math.floor(mean * 100.0f) / 100.0f;
		measures.add(rndMean);
		
		if (data.size() >= THRESHOLD) {
			List<Float> topList = data.subList((int)Math.floor(data.size()*0.8f), data.size());
			middle = (int)Math.floor((float)topList.size() / 2.0f);
			float topMedian = topList.get(middle);
			measures.add(topMedian);
			
			List<Float> bottomList = data.subList(0, (int)Math.floor(data.size()*0.2f));
			middle = (int)Math.floor((float)bottomList.size() / 2.0f);
			float bottomMedian = bottomList.get(middle);
			measures.add(bottomMedian);
		}
		else {
			measures.add(0.0f);
			measures.add(0.0f);
		}
	}
	
	public void calculate(File advFile, File outFile, String timePeriod) {
		data = new HashMap<String, HashMap<String, List<Float>>>();
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
				if (areaID.equalsIgnoreCase("NULL") || areaID.equals("мо") || 
						transactionType.equalsIgnoreCase("N/A") || re.getSize() <= 0.0f || re.getPrice() <= 0.0f) {
					continue;
				}

				/*
				if (areaID.equalsIgnoreCase("NULL") || areaID.equals("мо") || 
						transactionType.equalsIgnoreCase("N/A") ) {
					continue;
				}
				*/
				float price = re.getPrice();
				float size = re.getSize() <= 0.0f ? 1.0f : re.getSize();
				float normPrice = (float)Math.floor(price / size * 100.0f) / 100.0f;
				
				String isAgent = Boolean.toString(re.isAgent());
				String newlyBuilt = Boolean.toString(re.getNewlyBuilt());
				updateStatistics(areaID, transactionType, newlyBuilt, isAgent, normPrice, price);
				updateStatistics(areaID, transactionType, "ALL", isAgent, normPrice, price);
				updateStatistics(areaID, transactionType, newlyBuilt, "ALL", normPrice, price);
				updateStatistics(areaID, transactionType, "ALL", "ALL", normPrice, price);
				
				if (parents.get(areaID) != null) {
					for (String area : parents.get(areaID)) {
						updateStatistics(area, transactionType, newlyBuilt, isAgent, normPrice, price);
						updateStatistics(area, transactionType, "ALL", isAgent, normPrice, price);
						updateStatistics(area, transactionType, newlyBuilt, "ALL", normPrice, price);
						updateStatistics(area, transactionType, "ALL", "ALL", normPrice, price);
					}
				}
				
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			out.write("TIME_PERIOD\tAREAID\tTRANSACTION_TYPE\tNEWBUILT\tIS_AGENT\tCAN-CALCULATE-STATISTICS\tMEDIAN-PRICE-PER-SQUARE-METER\tMEAN-PRICE-PER-SQUARE-METER\tMEDIAN_TOP20PCT-PRICE-PER-SQUARE-METER\tMEDIAN_BOTTOM20PCT-PRICE-PER-SQUARE-METER\t" + 
					"MEDIAN-PRICE\tMEAN-PRICE\tMEDIAN_TOP20PCT-PRICE\tMEDIAN_BOTTOM20PCT-PRICE" +
							"\tCOUNT" + newline);
			for (String k : data.keySet()) {
				List<Float> d = data.get(k).get("PricePerSquareMeter");
				List<Float> dPrice = data.get(k).get("Price");
				logger.debug("KEY : " + k + " SIZE : " + d.size());
				
				List<Float> m = new LinkedList<Float>();
				calculateMeasures(d, m);
				calculateMeasures(dPrice, m);
				
				boolean canDoStats = d.size() >= THRESHOLD ? true : false;
				
				StringBuffer sb = new StringBuffer();
				sb.append(timePeriod); sb.append("\t"); 
				for (String kk : k.split("_")) {
					sb.append(kk); sb.append("\t"); 
				}
				sb.append(canDoStats); sb.append("\t");
				if (canDoStats) {
					for (Float f : m) {
						sb.append(f); sb.append("\t");
					}
					sb.append(d.size()); sb.append(newline);
				}
				else {
					for (Float f : m) {
						sb.append("N/A"); sb.append("\t");
					}
					sb.append(d.size()); sb.append(newline);
				}
				
				out.write(sb.toString());
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to " + outFile.getAbsolutePath());
			System.exit(-1);
		}
		
	}
	
	public static void main(String[] argv) {	
	/*
		String parentsFile = "c:\\Data\\areas-full-path.txt";
		StatisticsCalculator sc = new StatisticsCalculator();
		sc.createParents(new File(parentsFile));
		sc.writeParents(new File("c:\\Data\\parents.txt"));
		*/
		
		String parentsFile = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\parents_forStats.txt";
		String[] timePeriods = {"2005_0", "2005_1", "2006_0", "2006_1", "2007_0", "2007_1", "2008_0", "2008_1", "2009_0", "2009_1", "2010_0", "2010_1"};
		for (String timePeriod : timePeriods) {
			String dataFilename = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\2005-2010\\002_residences_deduped_" + timePeriod + ".txt";
			String stats = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\aggregations\\stats_withPriceAndSizeOnly_" + timePeriod + ".txt";
			
			StatisticsCalculator sc = new StatisticsCalculator();
			sc.loadParents(new File(parentsFile));
			sc.calculate(new File(dataFilename), new File(stats), timePeriod);
		}
	
		
	}
}
