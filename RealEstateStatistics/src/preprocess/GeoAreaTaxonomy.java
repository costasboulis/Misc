package preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class GeoAreaTaxonomy {
	public static String newline = System.getProperty("line.separator");
	
	public void create(File file, File outFile) {
//		String[] geoExceptionsList = {"59-15", "59-23", "59-26", "59-3", "59-44", "47", "65", "66", "67", "68", "58", "59", "60", "61", "62", "63", "64", "13", "14", "36", "48", "50", "56", "88", "89", "91", "94", "95", "96", "97", "98", "99", "100000"};
		String[] geoExceptionsList = {""};
		HashSet<String> geoExceptions = new HashSet<String>(Arrays.asList(geoExceptionsList));
		HashSet<String> alteredIDs = new HashSet<String>();
		HashMap<String, String> parents = new HashMap<String, String>();
		HashMap<String, String> geoIds = new HashMap<String, String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String logEntryLine = in.readLine();
			while ((logEntryLine = in.readLine()) != null) {
				String [] fieldNames = logEntryLine.split("\\t");
				
				String geoAreaID = fieldNames[0].trim();
				String areaID = fieldNames[1].trim();
				String parentAreaID = fieldNames[2].trim();
				
				
				
				String key = areaID + "_" + geoAreaID;
				parents.put(key, parentAreaID);
				if (!areaID.equals(parentAreaID)) {
					parents.put(areaID, parentAreaID);
				}
				
				geoIds.put(key, geoAreaID);
			}
			in.close();
			System.out.println("NUMBER OF INPUT AREAS : " + parents.size());
		}
		catch (Exception ex) {
			System.err.println("Cannot read from file " + file.getAbsolutePath());
			System.exit(-1);
		}
		
		try {
			int cnt = 0;
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			for (Map.Entry<String, String> es : parents.entrySet()) {
				String key = es.getKey();
				
				
				cnt ++;
				String[] flds = key.split("_");
				String totalKey = flds[0];
				String parent = es.getValue();
				while (parent != null && parent.length() > 0) {
					totalKey = parent + "," + totalKey;
					parent = parents.get(parent);
				}
				String geoID = geoIds.get(key);
				if (geoID == null) {
					continue;
				}
				
				if (geoExceptions.contains(geoID)) {
					continue;
				}
				
				out.write(geoID + "," + totalKey + newline);
			}
			out.close();
			
			System.out.println("NUMBER OF OUTPUT AREAS : " + cnt);
		}
		catch (Exception ex) {
			System.err.println("Cannot write to file " + outFile.getAbsolutePath());
			System.exit(-1);
		}
		
	}
	
	public static void main(String[] argv) {
		String advDir = "c:\\Data\\areas.txt";
		String outFile = "c:\\Data\\areas-full-path.txt";
		
		GeoAreaTaxonomy gt = new GeoAreaTaxonomy();
		gt.create(new File(advDir), new File(outFile));
	}
}
