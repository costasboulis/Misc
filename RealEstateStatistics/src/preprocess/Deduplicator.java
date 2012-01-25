package preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;

import model.RealEstate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Remove advIds on the same phone at the last semester

public class Deduplicator {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private HashMap<Long, HashSet<String>> adv;
	public static String newline = System.getProperty("line.separator");
	
	private class Duped implements Comparable<Duped>{
		private long phone;
		private int numberOfAds;
		
		public Duped(long p, int n) {
			phone = p;
			numberOfAds = n;
		}
		
		public int getNumberOfAds() {
			return numberOfAds;
		}
		
		public long getPhone() {
			return phone;
		}
		
		public int compareTo(Duped d) {
			return numberOfAds - d.getNumberOfAds();
		}
	}
	public Deduplicator() {
		adv = new HashMap<Long, HashSet<String>>();
	}
	
	public void dedup(File advFile, Calendar fromCalendar, Calendar toCalendar, File outFile) {
		int rejectedAds = 0;
		int totalAds = 0;
		HashMap<Long, Integer> rejectedAdsPerPhone = new HashMap<Long, Integer>();
		adv = new HashMap<Long, HashSet<String>>();
		if (!advFile.exists()) {
			logger.error("File " + advFile.getAbsolutePath() + " does not exist");
			System.exit(-1);
		}
		if (!advFile.canRead()) {
			logger.error("No read access to file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			
			try {
				BufferedReader in = new BufferedReader(new FileReader(advFile));
				String logEntryLine;
				logEntryLine = in.readLine();
				HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
				String[] fieldNames = logEntryLine.split("\\t");
				for (int i = 0; i < fieldNames.length; i ++) {
					names2Index.put(fieldNames[i], new Integer(i));
				}
				out.print(logEntryLine + newline);
				while ((logEntryLine = in.readLine()) != null) {
					RealEstate re = new RealEstate(logEntryLine, names2Index);
					
					int pubYear = re.getPublicationYear();
					int pubMonth = re.getPublicationMonth();
					long telephone = re.getTelephone();
					float price = re.getPrice();
					float size = re.getSize();
					String reType = re.getRealEstateType();
					String transType = re.getTransactionType();
					
//					if (pubYear == -1 || pubMonth == -1 || telephone == 0 || price == 0.0f || 
//							size == 0.0f || reType.equals("N/A") || transType.equals("N/A")) {
//						continue;
//					}
					
					if (pubYear == -1 || pubMonth == -1 || telephone == 0 || 
							 reType.equals("N/A") || transType.equals("N/A")) {
						continue;
					}
					
					GregorianCalendar gc = new GregorianCalendar(pubYear, pubMonth, 1);
					if (gc.compareTo(fromCalendar) >= 0 && gc.compareTo(toCalendar) <= 0 ) {
						HashSet<String> hs = adv.get(telephone);
						if (hs == null) {
							hs = new HashSet<String>();
						}
						
						StringBuffer sb = new StringBuffer(); 
						sb.append(transType); sb.append("_");
						sb.append(re.getAreaID()); sb.append("_");
						sb.append(reType); sb.append("_");
						sb.append(size <= 0.0f ? "N/A" : size); sb.append("_");
						String floor = re.getFloor();
						sb.append(floor); sb.append("_");
						sb.append(price <= 0.0f ? "N/A" : price); 
						
						boolean doNotDedup = false;
						if (reType.equals("APARTMENT") && floor.equals("N/A")) {
							doNotDedup = true; 
						}
						if (price <= 0.0f || size <= 0.0f) {
							doNotDedup = true; 
						}
						String key = sb.toString();
						
						if (!doNotDedup && hs.contains(key)) {
							rejectedAds ++;
							int cnt = rejectedAdsPerPhone.get(telephone) == null ? 0 : rejectedAdsPerPhone.get(telephone);
							rejectedAdsPerPhone.put(telephone, new Integer(cnt + 1));
			//				System.out.println(key);
						}
						else {
							hs.add(key);
							out.print(logEntryLine + newline);
						}
						adv.put(telephone, hs);
						
						totalAds ++;
					}
				}
				in.close();
			}
			catch (Exception ex) {
				logger.error("Cannot read file " + advFile.getAbsolutePath());
				System.exit(-1);
			}
			ArrayList<Duped> al = new ArrayList<Duped>();
			for (Entry<Long, Integer> en : rejectedAdsPerPhone.entrySet()) {
				Duped d = new Duped(en.getKey(), en.getValue());
				al.add(d);
			}
			Collections.sort(al);
			for (Duped d : al) {
				System.out.println(d.getPhone() + " " + d.getNumberOfAds());
			}
			logger.info("Rejected ads: " + rejectedAds + " Total ads: " + totalAds);
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to file " + outFile.getAbsolutePath());
			System.exit(-1);
		}
		
		
	}
	
	
	public static void main(String[] argv) {
		Deduplicator dd = new Deduplicator();
//		String filename = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\2005-2010\\001_residences_2005-2009_withPriceAndSizeOnly_noErrors.txt";
//		String outFilename = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\2005-2010\\002_residences_deduped";
		
		String filename = "c:\\Data\\001_RealEstate_residence_2010_eksamino_noErrors.txt";
		String outFilename = "c:\\data\\002_Deduped_RealEstate_residence_2010_eksamino";
		
		int monthStep = 6;
		for (int year = 2010; year <= 2010; year ++) {
			for (int j = 0; j <= 1; j ++) {
				GregorianCalendar fromCalendar = new GregorianCalendar(year, 1 + j * monthStep, 1);
				GregorianCalendar toCalendar = new GregorianCalendar(year, monthStep + j * monthStep, 30);
				String f = outFilename + "_" + year + "_" + j + ".txt";
				dd.dedup(new File(filename), fromCalendar, toCalendar, new File(f));
			}
		}
			
		
	}
}
