import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Evaluate {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private String EMPTY = "N/A";
	
	
	private class RefHypPair {
		String ref;
		String hyp;
		
		public RefHypPair(String r, String h) {
			this.ref = r;
			this.hyp = h;
		}
		
		public String getRef(){
			return this.ref;
		}
		
		public String getHyp() {
			return this.hyp;
		}
	}
	
	private HashMap<String, HashMap<String, String>> readFile(File f) {
		HashMap<String, HashMap<String, String>> hs = new HashMap<String, HashMap<String, String>>();
		if (!f.exists()) {
			logger.error("ERROR: File " + f.getAbsolutePath() + " does not exist");
			System.exit(-1);
		}
		if (!f.canRead()) {
			logger.error("ERROR: Cannot read file " + f.getAbsolutePath());
			System.exit(-1);
		}
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String logEntryLine;
			logEntryLine = in.readLine();
			String[] fieldNames = logEntryLine.split("\\t");
			
			while ((logEntryLine = in.readLine()) != null) {
				String[] fields = logEntryLine.split("\\t");
				if (fields.length != fieldNames.length) {
					logger.error("Wrong number of fields for line " + logEntryLine);
					continue;
				}
				String id = fields[0];
				
				if (!fieldNames[0].equals("ID")) {
					logger.error("Cannot find ID for line " + logEntryLine);
					continue;
				}
				if (hs.containsKey(id)) {
					logger.error("ID " + id + " was found before..ignoring");
					continue;
				}
				if (id.isEmpty()) {
					logger.error("Empty ID for line " + logEntryLine);
					continue;
				}
				
				HashMap<String, String> ff = new HashMap<String, String>();
				for (int i = 1; i < fields.length; i ++) {
					String value = fields[i];
					String name = fieldNames[i];
					
					ff.put(name, value);
				}
				hs.put(id, ff);
			}
			in.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot read file " + f.getAbsolutePath());
			System.exit(-1);
		}
		
		return hs;
	}
	
	public void computeMetrics(File refFile, File hypFile) {
		HashMap<String, HashMap<String, String>> ref = readFile(refFile);
		HashMap<String, HashMap<String, String>> hyp = readFile(hypFile);
		
		HashMap<String, List<RefHypPair>> fields = new HashMap<String, List<RefHypPair>>();
		if (ref.size() != hyp.size()) {
			logger.error("Different sizes for ref (" + ref.size() + ") and hyp (" + hyp.size() + ")");
			System.exit(-1);
		}
		
		for (String s : ref.keySet()) {
			if (!hyp.containsKey(s)) {
				logger.error("Cannot find id " + s + " in hyp");
				System.exit(-1);
			}
			
			HashMap<String, String> refFields = ref.get(s);
			HashMap<String, String> hypFields = hyp.get(s);
			
			if (refFields.size() != hypFields.size()) {
				logger.error("Different number of fields for ID " + s + " ref (" + refFields.size() + ") and hyp (" + hypFields.size() + ")");
				System.exit(-1);
			}
			
			for (String field : refFields.keySet()) {
				if (!hypFields.containsKey(field)) {
					logger.error("ID " + s + " has field \"" + field + "\" for ref but not for hyp");
					System.exit(-1);
				}
				
				List<RefHypPair> fieldsList = fields.get(field);
				if (fieldsList == null) {
					fieldsList = new LinkedList<RefHypPair>();
					fields.put(field, fieldsList);
				}
				RefHypPair pair = new RefHypPair(refFields.get(field), hypFields.get(field));
				
				fieldsList.add(pair);
			}
			
		}
		
		// Now compute precision, recall, accuracy for every field
		for (String field : fields.keySet()) {
			float[][] counts = { {0, 0}, {0, 0}};
			float correct = 0.0f;
			float tot = 0.0f;
			for (RefHypPair pair : fields.get(field)) {
				String r = pair.getRef();
				String h = pair.getHyp();
				
				int rc = r.equals(EMPTY) || r.equalsIgnoreCase("FALSE") ? 0 : 1;
				int hc = h.equals(EMPTY) || h.equalsIgnoreCase("FALSE") ? 0 : 1;
				
				counts[rc][hc] += 1.0f;
				
				if (rc == 1 && hc == 1) {
					if (r.equalsIgnoreCase(h)) {
						correct += 1.0f;
					}
					tot += 1.0f;
				}
			}
			
			float precision = counts[1][1] / (counts[1][1] + counts[0][1]);
			float recall = counts[1][1] / (counts[1][1] + counts[1][0]);
			float accuracy = correct / tot;
			logger.info("Field " + field + " P : " + precision + " R : " + recall + " A: " + accuracy + " [1][1]: " + counts[1][1] + " [1][0]: " + counts[1][0] + " [0][1]: " + counts[0][1] + " [0][0]: " + counts[0][0]);
		}
	}
	
	public static void main(String[] argv) {
		Evaluate e = new Evaluate();
		String ref = "c:\\Data\\fieldExtractor\\residence_informationExtraction.csv";
		String hyp = "c:\\Data\\fieldExtractor\\out.csv";
		
		e.computeMetrics(new File(ref), new File(hyp));
	}
}
