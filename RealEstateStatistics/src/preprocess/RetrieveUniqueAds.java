package preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import model.RealEstate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* 
 * Output one unique ad per time period
 * 
 * Keep the first publication per time period
 */
public class RetrieveUniqueAds {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");

	private String getSemester(int pubYear, int pubMonth) {
		String semester;
		if (pubMonth >= 1 && pubMonth <= 6) {
			semester = "A";
		}
		else if (pubMonth > 6 && pubMonth <= 12) {
			semester = "B";
		}
		else {
			logger.error("Cannot get semester for month " + pubMonth);
			semester = "NULL";
			System.exit(-1);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(semester); sb.append("_"); sb.append(pubYear);
		return sb.toString();
	}
	
	public void getUniqueAds(File pubsFile, File adsFile) {
		HashSet<String> uniqueAds = new HashSet<String>();
		try {
			PrintWriter out = new PrintWriter(new FileWriter(adsFile));
			try {
				BufferedReader in = new BufferedReader(new FileReader(pubsFile));
				String logEntryLine = in.readLine();
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
					int advID = re.getID();
					
					StringBuffer sb = new StringBuffer();
					String timePeriod = getSemester(pubYear, pubMonth);
					
					sb.append(advID); sb.append("_"); sb.append(timePeriod);
					
					String key = sb.toString();
					
					if (uniqueAds.contains(key)) {
						continue;
					}
					
					out.print(logEntryLine + newline);
					uniqueAds.add(key);
				}
				in.close();
			}
			catch (Exception ex) {
				logger.error("ERROR: Cannot read file " + pubsFile.getAbsolutePath());
				System.exit(-1);
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot write to file " + adsFile.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	public static void main(String[] argv) {
		String pubData = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\residences_2005-2009_withPriceAndSizeOnly.txt";
		String advData = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\residences_2005-2009_withPriceAndSizeOnly_uniqueAds.txt";
		
		RetrieveUniqueAds rua = new RetrieveUniqueAds();
		rua.getUniqueAds(new File(pubData), new File(advData));
	}
}
