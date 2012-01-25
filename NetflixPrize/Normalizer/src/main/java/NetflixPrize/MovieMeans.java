package NetflixPrize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;



public class MovieMeans extends BaselineModel{
	protected double[] movieMeans;
	
	
	public MovieMeans(){
		super();
		movieMeans = new double[NUMBER_OF_MOVIES];
	}

	public void readModels(File moviesFile){
    	if (moviesFile == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!moviesFile.exists()) {
			System.err.println("File " +  moviesFile.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!moviesFile.isFile()) {
	    	System.err.println("File " +  moviesFile.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!moviesFile.canRead()) {
			System.err.println("File " +  moviesFile.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(moviesFile));
			String lineStr;
			while ((lineStr = br.readLine()) != null){
				String[] st = lineStr.split(" ");
				
				int movie = decrementByOne ? Integer.parseInt(st[0])-1 : Integer.parseInt(st[0]);
				float m = Float.parseFloat(st[1]);
				
				movieMeans[movie] = m;
			}
			br.close();
			
			System.out.println("RMSE: " + predictProbeSet());
		}
		catch (FileNotFoundException e) { 
        	System.err.println("Cannot find filename " + moviesFile.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
        }	
        catch (IOException e) { 
        	System.err.println("Error while reading filename " + moviesFile.getAbsolutePath());
           	e.printStackTrace();
           	System.exit(1);
        }	
    }
    
	public void writeModels(File modelsFile){
		try {
	    	Writer output = new BufferedWriter( new FileWriter(modelsFile) );
	    	for (int m=0; m<NUMBER_OF_MOVIES; m++) {
	    		int movie = decrementByOne ? m+1 : m;
	    		output.write(movie + " " + movieMeans[m] + "\n");
	    	}
	    	output.close();
		}
		catch (IOException e){
			System.err.println("Cannot write to " + modelsFile.getAbsolutePath());
			System.exit(1);
		}
	}
	
	public void train(){
		double[] s1 = new double[NUMBER_OF_MOVIES];
		double[] s0 = new double[NUMBER_OF_MOVIES];
		for (int m=0; m<NUMBER_OF_MOVIES; m++){
			s1[m] = 0.0; s0[m]=0.0;
		}
				
		double globalMean = 0.0; double summedS0=0.0;
		for (int i=0; i<NUMBER_OF_USERS; i++){
			for (int m=0; m<dataIndex[i].length; m++){
				int movie = dataIndex[i][m];
				float rating = dataValue[i][m];
				
				s1[movie] += rating;
				s0[movie] += 1.0;
				
				globalMean += rating;
			}
			summedS0 += (double)dataIndex[i].length;
		}
		globalMean /= summedS0;
		
		for (int m=0; m<NUMBER_OF_MOVIES; m++){
			if (s0[m] == 0.0){
				System.err.println("ERROR: Cannot estimate mean for movie " + m);
				movieMeans[m] = globalMean;
			}
			else
				movieMeans[m] = s1[m] / s0[m];
		}
		System.out.println("RMSE: " + predictProbeSet());
	}
	
	public double predictRaw(int userId, short movie){
    		return movieMeans[movie];
	//	return 0.0;
    }
	
	public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{"decrementByOne"}, new String[]{
    							 "data",
                                 "probeSet",
                                 "writePredictions",
                                 "writeResiduals",
                                 "outputModels",
                                 "readModels",
                                 "probeResiduals"})) {
        		 System.err.println("Usage: MovieMeans\n" +
                                 "-data\tString Training data\n" +
                                 "-probeSet\tString\n" +
                                 "-writePredictions\tString\n" +
                                 "[-outputModels]\tString\n" +
                                 "-probeResiduals\tFile residuals for the probe set, they are added" +
                                 " to the predictions before calculating RMSE\n" +
                                 "[-readModels]\tFile\n" +
                                 "[-writeResiduals]\tString\n" +
                                 "[-decrementByOne]\n");
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String data = CommandLineParser.getStringParameter(arguments, "data", true);
        	 String probeSet = CommandLineParser.getStringParameter(arguments, "probeSet", true);
        	 String writePredictions = CommandLineParser.getStringParameter(arguments, "writePredictions", true);
        	 boolean decrementByOne = CommandLineParser.getBooleanParameter(arguments, "decrementByOne", false, true);
        	 String outputModels = CommandLineParser.getStringParameter(arguments, "outputModels", false);
        	 String probeResiduals = CommandLineParser.getStringParameter(arguments, "probeResiduals", true);
        	 String writeResiduals = CommandLineParser.getStringParameter(arguments, "writeResiduals", false);
        	 String readModels = CommandLineParser.getStringParameter(arguments, "readModels", false);
        	 boolean readBinary = false;
        	 
        	 
        	 MovieMeans movMeans = new MovieMeans();
        	 movMeans.readTrainingData(new File(data), readBinary);
        	 movMeans.readProbeSet(new File(probeSet));
        	 long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read reference ratings in probe set, Memory: " + memory/(1024*1024));
 			
        	 movMeans.readResiduals(new File(probeResiduals));
        	 memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read residuals in probe set, Memory: " + memory/(1024*1024));
        	 
        	 if (readModels != null)
        		 movMeans.readModels(new File(readModels));
        	 else
        		 movMeans.train();
        	 
        	 movMeans.writePredictions(new File(writePredictions));
        	 
        	 if (outputModels != null)
        		 movMeans.writeModels(new File(outputModels));
        	 
        	 if (writeResiduals != null)
        		 movMeans.writeTrainingError(new File(writeResiduals));
    }  	 
}
