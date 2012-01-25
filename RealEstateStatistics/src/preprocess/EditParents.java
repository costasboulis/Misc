package preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;

// Currently adds parents to the area ID tree structure


public class EditParents {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private HashMap<String, String> addParents;
	
	public EditParents() {
		addParents = new HashMap<String, String>();
		/*
		addParents.put("94", "GR-Rest");
		addParents.put("97", "GR-Rest");
		addParents.put("88", "GR-Rest");
		addParents.put("95", "GR-Rest");
		addParents.put("89", "GR-Rest");
		addParents.put("96", "GR-Rest");
		addParents.put("98", "GR-Rest");
		addParents.put("99", "GR-Rest");
		addParents.put("36", "GR-Rest");
		addParents.put("50", "GR-Rest");
		addParents.put("64", "63");
		*/
	}
	
	public void edit(File origParents, File newParents) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(newParents));
			try {
				BufferedReader in = new BufferedReader(new FileReader(origParents));
				String logEntryLine = in.readLine();
				String[] fieldNames = logEntryLine.split("\\t");
				HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
				for (int i = 0; i < fieldNames.length; i ++) {
					names2Index.put(fieldNames[i], new Integer(i));
				}
				HashSet<String> hs = new HashSet<String>();
				while ((logEntryLine = in.readLine()) != null) {
					String[] fields = logEntryLine.split(",");
					
					String newParent = addParents.get(fields[1]);
					if (newParent != null) {
						out.write(fields[0] + "," + newParent + newline);
					}
					hs.add(fields[0]);
					
					out.write(logEntryLine + newline);
				}
				in.close();
				
				for (String s : hs) {
					out.write(s + ",0" + newline);
				}
			}
			catch (Exception ex) {
				logger.error("Cannot read  file " + origParents.getAbsolutePath());
				System.exit(-1);
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to file " + newParents.getAbsolutePath());
			System.exit(-1);
		}
	}
	
 	public static void main(String[] argv) {
		String oldParents = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\path.txt";
		String newParents = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\parents_forStats.txt";
		
		EditParents ep = new EditParents();
		ep.edit(new File(oldParents), new File(newParents));
	}
}
