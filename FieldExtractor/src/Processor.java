import java.io.File;


public class Processor {

	public static void main(String[] argv) {
		RegExResidenceModel m = new RegExResidenceModel();
		String inputFilename = "c:\\Data\\fieldExtractor\\inputResidence.txt";
//		String inputFilename = "c:\\Data\\fieldExtractor\\test.txt";
		String outFilename = "c:\\Data\\fieldExtractor\\out.csv";
		
		m.process(new File(inputFilename), new File(outFilename));
	}
}
