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

/**
 * Estimates MAP user bias on the movieMeans residual predictor. On the probe set, it gets
 * an RMSE of 0.9863. With ML estimation it is 0.9877
 * 
 */
public class UserBiases extends BaselineModel{
	private double[] movieMeans;
	private double[] userBiases;
	
	
	public UserBiases(){
		super();
		userBiases = new double[NUMBER_OF_USERS];
		movieMeans = new double[NUMBER_OF_MOVIES];
	}
	
	public void writeModels(File biasesFile){
		try {
	    	Writer output = new BufferedWriter( new FileWriter(biasesFile) );
	    	for (int i=0; i<NUMBER_OF_USERS; i++) {
	    		output.write(reverseUserNames[i] + " " + userBiases[i] + "\n");
	    	}
	    	output.flush();
	    	output.close();
		}
		catch (IOException e){
			System.err.println("Cannot write to " + biasesFile.getAbsolutePath());
			System.exit(1);
		}
    }
    
    
    public void readModels(File userBiasesFile){
    	if (userBiasesFile == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!userBiasesFile.exists()) {
			System.err.println("File " +  userBiasesFile.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!userBiasesFile.isFile()) {
	    	System.err.println("File " +  userBiasesFile.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!userBiasesFile.canRead()) {
			System.err.println("File " +  userBiasesFile.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(userBiasesFile));
			String lineStr;
			while ((lineStr = br.readLine()) != null){
				String[] st = lineStr.split(" ");
				if (st.length != 2){
					System.err.println("Cannot parse line \"" + lineStr + "\"");
					System.exit(1);
				}
				userBiases[userNames.get(st[0]).intValue()] = Double.parseDouble(st[1]);
			}
			br.close();
		}
		catch (FileNotFoundException e) { 
        	System.err.println("Cannot find filename " + userBiasesFile.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
        }	
        catch (IOException e) { 
        	System.err.println("Error while reading filename " + userBiasesFile.getAbsolutePath());
           	e.printStackTrace();
           	System.exit(1);
        }	
    }
    
    public void train(){
    	double[] ms1 = new double[NUMBER_OF_MOVIES];
		double[] ms0 = new double[NUMBER_OF_MOVIES];
		for (int m=0; m<NUMBER_OF_MOVIES; m++){
			ms1[m] = 0.0; ms0[m]=0.0;
		}
				
		for (int i=0; i<NUMBER_OF_USERS; i++){
			for (int m=0; m<dataIndex[i].length; m++){
				short movie = dataIndex[i][m];
				float rating = dataValue[i][m];
				
				ms1[movie] += rating;
				ms0[movie] += 1.0;
			}
		}
		   	
		for (int m=0; m<NUMBER_OF_MOVIES; m++)
			movieMeans[m] = ms1[m] / ms0[m];
        	
		
				
		double[] S1 = new double[NUMBER_OF_USERS];
		double[] S2 = new double[NUMBER_OF_USERS];
		for (int i=0; i<NUMBER_OF_USERS; i++){
			S1[i] = 0.0;
			S2[i] = 0.0;
		}
		double summedS0=0.0;
		double summedS1=0.0;
		double summedS2=0.0;
		for (int i=0; i<NUMBER_OF_USERS; i++){
			for (int m=0; m<dataIndex[i].length; m++){
				short movie = dataIndex[i][m];
				float rating = dataValue[i][m];
				
				double mean = movieMeans[movie];
    			double diff = rating - mean;
    			S1[i] += diff;
    			S2[i] += diff * diff;
			}
    		summedS1 += S1[i];
    		summedS2 += S2[i];
    		summedS0 += (double)dataValue[i].length;
		}
		
		// Estimate hyperparameters
		double priorBias = summedS1 / summedS0;
		double priorVariance = (summedS2 / summedS0) - priorBias * priorBias;
		
		// Now do the MAP estimation
		for (int i=0; i<NUMBER_OF_USERS; i++){
			double n = (double)dataValue[i].length;
			if (n <= 1.0){
				userBiases[i] = priorBias;
				continue;
			}
			double MLBias = S1[i] / n;
			double MLvariance = (S2[i] / (n-1.0)) - (n/(n-1.0))*MLBias*MLBias;
			double tmp = MLvariance / (n * priorVariance);
			double weight = 1.0 / (1.0 + tmp);
			
			if (weight > 1.0 || weight < 0.0){
				System.err.println("MAP weight for user: " + reverseUserNames[i] + " is " + weight);
				System.exit(1);
			}
			userBiases[i] = weight * MLBias + (1.0 - weight) * priorBias;
//			userBiases[i] =  MLBias ;
		}
		
		System.out.println("RMSE: " + predictProbeSet());
    }
    
    public double predictRaw(int userId, short movie){
    	return userBiases[userId] + movieMeans[movie];
    }
    
    public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{"verbose","decrementByOne"}, new String[]{
    							 "data",
                                 "probeSet",
                                 "writePredictions",
                                 "writeResiduals",
                                 "outputModels",
                                 "probeResiduals"})) {
        		 System.err.println("Usage: UserBiases\n" +
                                 "-data\tString Training data\n" +
                                 "-probeSet\tString\n" +
                                 "-writePredictions\tString\n" +
                                 "[-outputModels]\tString\n" +
                                 "[-writeResiduals]\tString\n" +
                                 "-probeResiduals\tFile residuals for the probe set, they are added" +
                                 " to the predictions before calculating RMSE\n" +
                                 "[-decrementByOne]\n" +
                                 "[-verbose]\n");
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String data = CommandLineParser.getStringParameter(arguments, "data", true);
        	 String probeSet = CommandLineParser.getStringParameter(arguments, "probeSet", true);
        	 String writePredictions = CommandLineParser.getStringParameter(arguments, "writePredictions", true);
        	 boolean verbose = CommandLineParser.getBooleanParameter(arguments, "verbose", false, false);
        	 boolean decrementByOne = CommandLineParser.getBooleanParameter(arguments, "decrementByOne", false, true);
        	 String outputModels = CommandLineParser.getStringParameter(arguments, "outputModels", false);
        	 String probeResiduals = CommandLineParser.getStringParameter(arguments, "probeResiduals", true);
        	 String writeResiduals = CommandLineParser.getStringParameter(arguments, "writeResiduals", false);
        	 boolean readBinary = false;
        	 
        	 
        	 UserBiases userBiases = new UserBiases();
        	 userBiases.readTrainingData(new File(data), readBinary);
        	 userBiases.readProbeSet(new File(probeSet));
        	 long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read reference ratings in probe set, Memory: " + memory/(1024*1024));
 			
        	 userBiases.readResiduals(new File(probeResiduals));
        	 memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read residuals in probe set, Memory: " + memory/(1024*1024));
        	 
        	 userBiases.train();
        	 
        	 userBiases.writePredictions(new File(writePredictions));
        	 
        	 if (outputModels != null)
        		 userBiases.writeModels(new File(outputModels));
        	 
        	 if (writeResiduals != null)
        		 userBiases.writeTrainingError(new File(writeResiduals));
        	 
    }  	 
}
