package preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import model.RealEstate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ErrorRemover {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	
	
	public ErrorRemover() {
	}
	
	public void removeErrors(File advFile, File outFile) {
		int rejectedAds = 0;
		if (!advFile.exists()) {
			logger.error("ERROR: File " + advFile.getAbsolutePath() + " does not exist");
			System.exit(-1);
		}
		if (!advFile.canRead()) {
			logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
			System.exit(-1);
		}
		int totalAds = 0;
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
					totalAds ++;
					RealEstate re = new RealEstate(logEntryLine, names2Index);
					
					float price = re.getPrice();
					float size = re.getSize();
					String transactionType = re.getTransactionType();
					
					if (price == 0.0f || size == 0.0f) {
						out.print(logEntryLine + newline);
						continue;
					}
					
					if (re.getTelephone() == 2109091335) {
						logger.debug("Phone is TEST . Removing ad " + logEntryLine);
						rejectedAds ++;
						continue;
					}
					
					if (transactionType.equals("SELL") && price <= 5000.0f) {
						logger.debug("Removing ad " + logEntryLine);
						rejectedAds ++;
						continue;
					}
					else if (transactionType.equals("LET") && price >= 10000.0f && size <= 130.0f) {
						logger.debug("Removing ad " + logEntryLine);
						rejectedAds ++;
						continue;
					}
					else if (transactionType.equals("SELL") && price > 1000000.0f && size <= 100.0f) {
						logger.debug("Removing ad " + logEntryLine);
						rejectedAds ++;
						continue;
					}
					else if (transactionType.equals("SELL") && price > 2000000.0f) {
						logger.debug("Removing ad " + logEntryLine);
						rejectedAds ++;
						continue;
					}
					else if (transactionType.equals("SELL") && (price / size) > 10000.0f) {
						logger.debug("Removing ad " + logEntryLine);
						rejectedAds ++;
						continue;
					}
					else if (transactionType.equals("SELL") && (price / size) < 100.0f) {
						logger.debug("Removing ad " + logEntryLine);
						rejectedAds ++;
						continue;
					}
					out.print(logEntryLine + newline);
					
				}
				in.close();
			}
			catch (Exception ex) {
				logger.error("ERROR: Cannot read file " + advFile.getAbsolutePath());
				System.exit(-1);
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot write to file " + outFile.getAbsolutePath());
			System.exit(-1);
		}
		logger.info("Total : " + totalAds + " Rejected : " + rejectedAds);
	}
	
	
	public static void main(String[] argv) {
		ErrorRemover er = new ErrorRemover();
		String advData = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\residences_2005-2009_withPriceAndSizeOnly_uniqueAds.txt";
//		String advData = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\Residences_2010_SAS_elab.txt";
		String outFilename = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\2005-2010\\001_residences_2005-2009_withPriceAndSizeOnly_noErrors.txt";
//		String filename = "\\\\x8\\internet\\Projects\\XE\\Statistics\\New base\\Real10eksamino_nodup_fixed.txt";
//		String outFilename = "\\\\x8\\internet\\Projects\\XE\\Statistics\\New base\\001_RealEstate_residence_2010_eksamino_noErrors.txt";
		
//		String filename = "c:\\Data\\Real10eksamino_nodup_fixed.txt";
//		String outFilename = "c:\\Data\\001_RealEstate_residence_2010_eksamino_noErrors.txt";
		er.removeErrors(new File(advData), new File(outFilename));
			
	}
}


