

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ResidenceModel {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void process(File input, File ouput) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(ouput));
			out.write("ID" + "\t" + Residence.getFieldNames());
			try {
				BufferedReader in = new BufferedReader(new FileReader(input));
				String logEntryLine;
				while ((logEntryLine = in.readLine()) != null) {
					String[] flds = logEntryLine.split("\t");
					out.write(flds[0] + "\t" + process(flds[1]).toString());
				}
				in.close();
			}
			catch (Exception ex) {
				logger.error("ERROR: Cannot read file " + input.getAbsolutePath());
				System.exit(-1);
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("ERROR: Cannot write to file " + ouput.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	public abstract Residence process(String text);
}
