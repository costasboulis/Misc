
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Normalizer {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	public void normalize(File inFile, File outFile) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			out.write(Residence.getFieldNames());
			try {
				BufferedReader in = new BufferedReader(new FileReader(inFile));
				String logEntryLine = in.readLine();
				while ((logEntryLine = in.readLine()) != null) {
					Residence re = null;
					try {
						re = new Residence(logEntryLine);
					}
					catch (Exception ex) {
//						logger.error("Cannot create Residence object from \"" + logEntryLine + "\"");
//						System.exit(-1);
						continue;
					}
					
					
					out.write(re.toString());
				
				}
				in.close();
				
				
			}
			catch (Exception ex) {
				logger.error("ERROR: Cannot read file " + inFile.getAbsolutePath());
				System.exit(-1);
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot write to file " + outFile.getAbsolutePath());
			System.exit(-1);
		}
		
	}
	
	public static void main(String[] argv) {
		Normalizer norm = new Normalizer();
		String raw = "c:\\Data\\fieldExtractor\\pediopoihsh.csv";
		String outFilename = "c:\\Data\\fieldExtractor\\processed.csv";
		
		norm.normalize(new File(raw), new File(outFilename));
	}
}
