package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.clusterers.SimpleKMeans;

public class ClusterAreaID {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static final float GLOBAL_PRICE_PER_SQUARE_METER = 2258.0f;
	private HashMap<String, HashMap<String, Float>> SS0;
	private HashMap<String, HashMap<String, Float>> SS1;
	private HashMap<String, String> areaNames;
	private HashMap<String, HashSet<String>> parents;
	private HashMap<String, String> firstParent;
	public static String newline = System.getProperty("line.separator");
	
	private class AreaID implements Comparable<AreaID>{
		private String id;
		private double distance;
		
		public AreaID(String areaID, double sq) {
			id = areaID;
			distance = sq;
		}
		
		public double getDistance() {
			return distance;
		}
		
		public String getId() {
			return id;
		}
		
		public int compareTo(AreaID d) {
			if (distance - d.getDistance() > 0.0f) {
				return 1;
			}
			else if (distance - d.getDistance() < 0.0f) {
				return -1;
			}
			else {
				return 0;
			}
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
	
	
	public void createAreaIDVectors(File advFile) {
		SS0 = new HashMap<String, HashMap<String, Float>>();
		SS1 = new HashMap<String, HashMap<String, Float>>();
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
					logger.debug("Ignoring ad \"" + re.toString() + "\"");
					continue;
				}
				
				if (!transactionType.equals("SELL.NORMAL")) {
					continue;
				}
				
				float price = re.getPrice();
				float size = re.getSize();
				
				float pricePerSqMet = price / size;
				
				String realestatesubtype = re.getRealEstateType();
				String mappedRealEstateSubtype = RealEstate.getMappedRealEstateType(realestatesubtype);
				String floor = re.getFloor();
				String mappedFloor = RealEstate.getMappedFloor(floor);
				String newlyBuilt = Boolean.toString(re.getNewlyBuilt());
				
				StringBuffer sb = new StringBuffer();
//				sb.append(transactionType); sb.append("_");
				sb.append(mappedRealEstateSubtype); sb.append("_");
				sb.append(mappedFloor); sb.append("_"); 
				sb.append(newlyBuilt);
				String key = sb.toString();
				
				HashMap<String, Float> hm = SS1.get(areaID);
				if (hm == null) {
					hm = new HashMap<String, Float>();
					SS1.put(areaID, hm);
					SS0.put(areaID, new HashMap<String, Float>());
				}
				
				Float f = hm.get(key);
				if (f == null) {
					hm.put(key, new Float(pricePerSqMet));
					HashMap<String, Float> hm2 = SS0.get(areaID);
					hm2.put(key, new Float(1.0f));
				}
				else {
					hm.put(key, new Float(pricePerSqMet + f.floatValue()));
					HashMap<String, Float> hm2 = SS0.get(areaID);
					hm2.put(key, new Float(hm2.get(key).floatValue() + 1.0f));
				}
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}	
		
		Set<String> aIDs = SS1.keySet();
		for (String s : aIDs) {
			HashMap<String, Float> hmSS1 = SS1.get(s);
			HashMap<String, Float> hmSS0 = SS0.get(s);
			
			Set<String> vecs = hmSS1.keySet();
			for (String g : vecs) {
				float denom = hmSS0.get(g);
				
				float nom = hmSS1.get(g);
				hmSS1.put(g, nom / denom);
			}
		}
		
//		for (Map.Entry<String, Float> e : SS1.get("800").entrySet()) {
//			logger.info(e.getKey() + " : " + e.getValue());
//		}
	}
	
	public void clusterAreaIDs(File outputFile, int numberOfClusters) {
		// Get all the attributes of the vector
		int indx = 0;
		HashMap<String, Integer> attributeNames = new HashMap<String, Integer>();
		for (Map.Entry<String, HashMap<String, Float>> e : SS1.entrySet()) {
			for (String s : e.getValue().keySet()) {
				Integer i = attributeNames.get(s);
				if (i != null) {
					continue;
				}
				
				attributeNames.put(s, indx);
				indx ++;
			}
		}
		
		int len = attributeNames.size();
		FastVector fv = new FastVector(len);
		for (String s : attributeNames.keySet()) {
			fv.addElement(new Attribute(s));
		}
		
		// Create the instances so that you can apply a clusterer
		HashMap<String , Instance> instanceNames = new HashMap<String, Instance>();
		Instances instances = new Instances("AreaIDs", fv, 10000);
		for (Map.Entry<String, HashMap<String, Float>> e : SS1.entrySet()) {
			Instance instance = new Instance(len);
			for (int i = 0; i < instance.numAttributes(); i++) {
				instance.setValue(i, GLOBAL_PRICE_PER_SQUARE_METER);
			}
			instance.setDataset(instances);
			for (Map.Entry<String, Float> att : e.getValue().entrySet()) {
				int idx = attributeNames.get(att.getKey());
				float value = att.getValue();
				
				instance.setValue(idx, value);
			}
			instances.add(instance);
			
			instanceNames.put(e.getKey(), instance);
		}
		
		// Do the clustering
		SimpleKMeans clusterer = new SimpleKMeans();
		try {
			clusterer.setNumClusters(numberOfClusters);
			clusterer.buildClusterer(instances);
		}
		catch (Exception ex) {
			logger.error("Cannot create clusters");
			System.exit(-1);
		}
		
		HashMap<Integer, List<String>> l = new HashMap<Integer, List<String>>();
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outputFile));
			for (Map.Entry<String, Instance> inst : instanceNames.entrySet()) {
				String areaID = inst.getKey();
				Instance instance = inst.getValue();
				int clusterId = -1;
				try {
					clusterId = clusterer.clusterInstance(instance);
				}
				catch (Exception ex) {
					logger.error("Cannot assign instance to cluster");
					System.exit(-1);
				}
				out.write("\"" + areaID + "\",\"" + clusterId + "\"" + newline);
				
				List<String> clusteredAreaIds = l.get(clusterId);
				if (clusteredAreaIds == null) {
					clusteredAreaIds = new LinkedList<String>();
					l.put(clusterId, clusteredAreaIds);
				}
				clusteredAreaIds.add(areaID);
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to file " + outputFile.getAbsolutePath());
			System.exit(-1);
		}
		
		for (Map.Entry<Integer, List<String>> es : l.entrySet()) {
			StringBuffer sb = new StringBuffer();
			for (String s : es.getValue()) {
				sb.append("\""); sb.append(areaNames.get(s)); sb.append("\",");
			}
			System.out.println(sb.toString());
		}
		
	}
	
	public void readAreaNames(File areaNamesFile) {
		areaNames = new HashMap<String, String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(areaNamesFile));
			String logEntryLine;
			while ((logEntryLine = in.readLine()) != null) {
				String[] fields = logEntryLine.split("\",\"");
				String id = fields[0].replaceAll("\"", "");
				String name = fields[1].replaceAll("\"", "");
				
				areaNames.put(id, name);
			}
			in.close();
			
			Set<String> ids = areaNames.keySet();
			for (String id : ids) {
				String[] parentAreaIDs = id.split("-");
				if (parentAreaIDs.length == 2) {
					String parent = areaNames.get(parentAreaIDs[0]);
					String name = areaNames.get(id);
					
					String newName = parent + " - " + name;
					areaNames.put(id, newName);
				}
				else if (parentAreaIDs.length == 3) {
					String grandParent = areaNames.get(parentAreaIDs[0]);
					String tmpId = parentAreaIDs[0] + "-" + parentAreaIDs[1]; 
					String parent = areaNames.get(tmpId);
					String name = areaNames.get(id);
					
					String newName = grandParent + " - " + parent + " - " + name;
					areaNames.put(id, newName);
				}
			}
		}
		catch (Exception ex) {
			logger.error("Cannot read from file " + areaNamesFile.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	/* 
	 * For a given geo area calculate the topN areas. If a target area is the parent of another target area or 
	 * of the source area then remove it
	 * 
	 */
	public List<String> calculateTopDistance(String areaID, int topN) {
		HashMap<String, Float> areaIDVector = SS1.get(areaID);
		List<String> topAreas = new LinkedList<String>();
		List<AreaID> topAreasSortable = new LinkedList<AreaID>();
		
		if (areaIDVector == null) {
			return topAreas;
		}
		
		for (Map.Entry<String, HashMap<String, Float>> e : SS1.entrySet()) {
			String currentArea = e.getKey();
			
			if (isParent(areaID, currentArea) || isParent(currentArea, areaID) || currentArea.equals(areaID)) {
				continue;
			}
			
			HashMap<String, Float> hm = e.getValue();
			
			
			float dist = 0.0f;
			for (Map.Entry<String, Float> h : areaIDVector.entrySet()) {
				float fA = h.getValue();
				
				Float f = hm.get(h.getKey());
				float fB = f == null ? 2258.0f : f.floatValue();
				
				float tmp = fB - fA;
				dist += tmp * tmp;
			}
			for (Map.Entry<String, Float> h : hm.entrySet()) {
				Float f = areaIDVector.get(h.getKey());
				
				if (f == null) {
					float tmp = (2258.0f - h.getValue());
					dist += tmp * tmp;
				}
			}
			
			topAreasSortable.add(new AreaID(currentArea, dist));
		}
		
		Collections.sort(topAreasSortable);
		
		String srcFirstParent = firstParent.get(areaID);
		
		List<AreaID> a = topAreasSortable.subList(0, topN);
		HashSet<String> rejected = new HashSet<String>();
		for (int i = 0; i < a.size(); i ++) {
			for (int j = i + 1; j < a.size(); j ++) {
				if (isParent(a.get(i).getId(), a.get(j).getId()) || isParent(a.get(j).getId(), a.get(i).getId())) {
					rejected.add(a.get(j).getId());
				}
			}
			String tgtFirstParent = firstParent.get(a.get(i).getId());
			if (srcFirstParent != null && tgtFirstParent != null && !srcFirstParent.equals(tgtFirstParent)) {
				rejected.add(a.get(i).getId());
			}
		}
		
		for (int i = 0; i < a.size(); i ++) {
			String id = a.get(i).getId();
			
			if (rejected.contains(id)) {
				continue;
			}
			
	//		String name = areaNames.get(id);
	//		if (name == null) {
	//			logger.error("Cannot find name for id \"" + id + "\"");
	//			continue;
	//		}
			String name = id;
			topAreas.add(name);
		}
		
		
		return topAreas;
	}
	
	/*
	 * Returns true is areaB is the parent of areaA
	 */
	private boolean isParent(String areaA, String areaB) {
		HashSet<String> prnts = parents.get(areaA);
		
		if (prnts == null) {
			return false;
		}
		else {
			return prnts.contains(areaB);
		}
	}
	
	
	public void calculateTopDistance(int topN, File outFile) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			for (String s : areaNames.keySet()) {
				List<String> l = calculateTopDistance(s, topN);
				if (l.size() == 0) {
					continue;
				}
				StringBuffer sb = new StringBuffer();
	//			sb.append("\""); sb.append(areaNames.get(s)); sb.append("\"");
				for (String tgt : l) {
	//				sb.append(",\""); sb.append(tgt); sb.append("\"");
					sb.append(s); sb.append(","); sb.append(tgt);
					sb.append(newline);
				}
//				sb.append(newline);
				
				out.write(sb.toString());
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to file " + outFile.getAbsolutePath());
			System.exit(-1);
		}
		
		
	}
	
	public static void main(String[] argv) {
		String trainFilename = "c:\\Data\\002_Deduped_RealEstate_residence_2010_eksamino_2010_1.txt";
		String areaNamesFilename = "c:\\Data\\areaIDs.txt";
		String clusteredAreaIDsFilename = "c:\\Data\\clusteredAreaIDs.txt";
		String parentsFile = "c:\\Data\\areas-forOrbit.txt";
		ClusterAreaID cluster = new ClusterAreaID();
		
		cluster.createParents(new File(parentsFile));
		cluster.createAreaIDVectors(new File(trainFilename));
		cluster.readAreaNames(new File(areaNamesFilename));
//		cluster.clusterAreaIDs(new File(clusteredAreaIDsFilename), 20);
		
		cluster.calculateTopDistance(20, new File(clusteredAreaIDsFilename));
//		List<String> topAreas = cluster.calculateTopDistance("800-477", 20);
//		for (String s : topAreas) {
//			System.out.println(s);
//		}
		
		
//		List<String> topAreas = cluster.calculateTopDistance("800-477", 20);
//		for (String s : topAreas) {
//			System.out.println(s);
//		}
	}
	
}
