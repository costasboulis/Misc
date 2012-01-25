package NetflixPrize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class SVD extends BaselineModel{
	int numberOfFactors;
    double[][] U;
    double[][] V;
    
    
    
    public SVD(int _numberOfFactors, File log){
    	super(log);
    	numberOfFactors = _numberOfFactors;
        U = new double[NUMBER_OF_USERS][];
        for (int k=0; k<NUMBER_OF_USERS; k++)
        	U[k] = new double[numberOfFactors];
        V = new double[numberOfFactors][];
        for (int k=0; k<numberOfFactors; k++)
        	V[k] = new double[NUMBER_OF_MOVIES];
    }

    public SVD(int _numberOfFactors){
    	super();
    	numberOfFactors = _numberOfFactors;
        U = new double[NUMBER_OF_USERS][];
        for (int k=0; k<NUMBER_OF_USERS; k++)
        	U[k] = new double[numberOfFactors];
        V = new double[numberOfFactors][];
        for (int k=0; k<numberOfFactors; k++)
        	V[k] = new double[NUMBER_OF_MOVIES];
    }
    
    public double predictRaw(int userId, short movie){
    	double prediction = 0.0;
		for (int z=0; z<numberOfFactors; z++)
			prediction += U[userId][z] * V[z][movie];	
		return (prediction);
    }
    
    private void randomInitialization(){
    	Random rand = new Random();
    	for (int row=0; row<NUMBER_OF_USERS; row++)
    		for (int z=0; z<numberOfFactors; z++)
    			U[row][z] = (0.2*(rand.nextDouble() - 0.5)) ;
    	for (int att=0; att<NUMBER_OF_MOVIES; att++)
    		for (int z=0; z<numberOfFactors; z++)
    			V[z][att] = (0.2*(rand.nextDouble() - 0.5));
    }
    
    public int train(double lambda, double lRate, double delta, int stepSize){
        double[] tmpU = new double[numberOfFactors];
        double[] tmpV = new double[numberOfFactors];
        
        int iter=1;
        double previousRmse = 1000.0;
        double constant = (1.0 - (lambda*lRate));
        double constant2 = (2.0 * lRate);
        while (true){
        	for (int row=0; row<NUMBER_OF_USERS; row ++){
        		for (int i=0; i<dataIndex[row].length; i++){
        			int att = dataIndex[row][i];
        			
        			double prediction=0.0;
        			for (int z=0; z<numberOfFactors; z++)
        				prediction += U[row][z] * V[z][att];
        			double error = dataValue[row][i] - prediction;
        			
        			
        			for (int z=0; z<numberOfFactors; z++){
        				tmpU[z] = error * V[z][att];
        				tmpV[z] = error * U[row][z];
        			}

        			for (int z=0; z<numberOfFactors; z++){
            			U[row][z] = (constant * U[row][z]) + (constant2 * tmpU[z]);
            			V[z][att] = (constant * V[z][att]) + (constant2 * tmpV[z]);
            		}
        			
        		}
        	}
        	
        	double rmse = predictProbeSet();
        	
        	
        	// Delta is overloaded. If smaller than unity, it is used as the difference in RMSE to stop training
        	// If bigger than 1 then it is the number of iterations that will be applied. The first semantics will be used
        	// when the probe set is available and the second when the training data includes the probe set
        	if (delta < 1.0){   	
        		System.out.println("RMSE: " + rmse);
        		if (logWriter != null){
        			try {
        				logWriter.write("RMSE: " + rmse + "\n");
        				logWriter.flush();
        			}
        			catch (IOException e){
        				System.err.println("ERROR: Cannot write RMSE to log file");
        				System.exit(1);
        			}
        		}
        		if (previousRmse - rmse < delta && iter >= 4)
        			break;
        	}
        	else if (iter >= Math.round(delta))
        		break;
     
        	
        	if (stepSize > 0){
        		if (iter % stepSize == 0){
        			int prevIter = iter - stepSize;
        			String prevFilename = "models.SVD.tmp.nFactors" + numberOfFactors + ".lamda" + lambda + ".lRate" + lRate +
        			".nIters" + prevIter;
        			File f = new File(prevFilename);
        			f.delete();
        			String filename = "models.SVD.tmp.nFactors" + numberOfFactors + ".lamda" + lambda + ".lRate" + lRate +
        			".nIters" + iter;
        			writeModels(new File(filename));
        		}
        	}
        	
        	iter ++; 
        	previousRmse = rmse;
        }
        if (logWriter != null){
        	try{
        		logWriter.close();
        	}
        	catch (IOException e){
				System.err.println("ERROR: Cannot close log file");
				System.exit(1);
			}
        }
        return(iter);
    }
    
    
    public void writeUserProjections(File file){
    	try {
			FileOutputStream file_output = new FileOutputStream (file);
		    DataOutputStream data_out = new DataOutputStream (file_output);
		    
		    /* Write the length of each vector */
		    int shortSize = Short.SIZE/8;
		    byte[] lenBuffer = new byte[shortSize];
		    short len = (short)numberOfFactors;
		    int offset=0;
		    lenBuffer[offset] = (byte)(len >>> 8);
		    lenBuffer[offset + 1] = (byte)(len);
		    data_out.write(lenBuffer);
		    
		    /* For each user, write the userId and then the dense profile (U Matrix) */
		    int floatSize = Float.SIZE/8;
			byte[] valueBuffer = new byte[floatSize * numberOfFactors];
		    for (int i=0; i<NUMBER_OF_USERS; i++){
		    	data_out.writeUTF(reverseUserNames[i]);
		    	
		    	offset=0;
		    	for (int z=0; z<numberOfFactors; z++){
		    		int intBits = Float.floatToRawIntBits((float)U[i][z]);
		    		valueBuffer[offset] = (byte)(intBits >>> 24);
		    		valueBuffer[offset + 1] = (byte)(intBits >>> 16);
		    		valueBuffer[offset + 2] = (byte)(intBits >>> 8);
		    		valueBuffer[offset + 3] = (byte)intBits;
			    	offset += floatSize;
		    	}
		    	data_out.write(valueBuffer);
		    }
		    file_output.flush();	
		    file_output.close();
    	}
    	catch (IOException e){
			e.printStackTrace();
			System.err.println("Error while writing to " + file.getAbsolutePath());
			System.exit(-1);
		}	
    }
    
    public void writeModels(File modelsFile){
		if (modelsFile == null) {
			System.err.println("File pointer is null.");
			System.exit(-1);
		}
		
		try {
			FileOutputStream file_output = new FileOutputStream (modelsFile);
		    DataOutputStream data_out = new DataOutputStream (file_output);
		    
		    int doubleSize = Double.SIZE/8;
		    for (int z=0; z<numberOfFactors; z++){
		    	byte[] valueBuffer = new byte[doubleSize * NUMBER_OF_USERS];
		    	int offset=0;
		    	for (int i=0; i<NUMBER_OF_USERS; i++){
		    		long intBits = Double.doubleToLongBits(U[i][z]);
		    		valueBuffer[offset] = (byte)(intBits >>> 56);
		    		valueBuffer[offset + 1] = (byte)(intBits >>> 48);
		    		valueBuffer[offset + 2] = (byte)(intBits >>> 40);
		    		valueBuffer[offset + 3] = (byte)(intBits >>> 32);
		    		valueBuffer[offset + 4] = (byte)(intBits >>> 24);
		    		valueBuffer[offset + 5] = (byte)(intBits >>> 16);
		    		valueBuffer[offset + 6] = (byte)(intBits >>> 8);
		    		valueBuffer[offset + 7] = (byte)intBits;
			    	offset += doubleSize;
		    	}
		    	data_out.write(valueBuffer);
		    }
		    
		    for (int z=0; z<numberOfFactors; z++){
		    	byte[] valueBuffer = new byte[doubleSize * NUMBER_OF_MOVIES];
		    	int offset=0;
		    	for (int i=0; i<NUMBER_OF_MOVIES; i++){
		    		long intBits = Double.doubleToLongBits(V[z][i]);
		    		valueBuffer[offset] = (byte)(intBits >>> 56);
		    		valueBuffer[offset + 1] = (byte)(intBits >>> 48);
		    		valueBuffer[offset + 2] = (byte)(intBits >>> 40);
		    		valueBuffer[offset + 3] = (byte)(intBits >>> 32);
		    		valueBuffer[offset + 4] = (byte)(intBits >>> 24);
		    		valueBuffer[offset + 5] = (byte)(intBits >>> 16);
		    		valueBuffer[offset + 6] = (byte)(intBits >>> 8);
		    		valueBuffer[offset + 7] = (byte)intBits;
			    	offset += doubleSize;
		    	}
		    	data_out.write(valueBuffer);
		    }
		    
		    	
		    
		    file_output.flush();	
		    file_output.close();
		}
		catch (IOException e){
			e.printStackTrace();
			System.err.println("Error while writing to " + modelsFile.getAbsolutePath());
			System.exit(-1);
		}	
    }
    
    public void readModels(File modelsFile){
		if (modelsFile == null) {
		      System.err.println("File should not be null.");
		      System.exit(-1);
		}
		if (!modelsFile.exists()) {
			System.err.println("File does not exist: " + modelsFile.getAbsolutePath());
			System.exit(-1);
	    } 
	    if (!modelsFile.isFile()) {
	    	System.err.println("Should not be a directory: " + modelsFile.getAbsolutePath());
	    	System.exit(-1);
		}
		if (!modelsFile.canRead()) {
			System.err.println("File cannot be read: " + modelsFile.getAbsolutePath());
			System.exit(-1);
		}
		
		try { 
			FileInputStream file_input = new FileInputStream (modelsFile);
		    DataInputStream data_in    = new DataInputStream (file_input);
		    
		    int doubleSize = Double.SIZE/8;
		    for (int z=0; z<numberOfFactors; z++){
		    	byte[] doublebuf = new byte[doubleSize * NUMBER_OF_USERS];
        		data_in.read(doublebuf);
        		int offset = 0;
        		for (int i=0; i<NUMBER_OF_USERS; i++){
        			long value = 0;
        			 for (int r = 0; r < doubleSize; r++) {
        				 int shift = (doubleSize - 1 - r) * 8;
        				 value += ( (long)(doublebuf[r + offset] & 0x000000FF)) << shift;
        			 }
        			 U[i][z] = Double.longBitsToDouble(value);
        			 offset += doubleSize;
        		}
		    }
		    		
		    
		    for (int z=0; z<numberOfFactors; z++){
		    	byte[] doublebuf = new byte[doubleSize * NUMBER_OF_MOVIES];
        		data_in.read(doublebuf);
        		int offset = 0;
        		for (int i=0; i<NUMBER_OF_MOVIES; i++){
        			long value = 0;
        			 for (int r = 0; r < doubleSize; r++) {
        				 int shift = (doubleSize - 1 - r) * 8;
        				 value += ( (long)(doublebuf[r + offset] & 0x000000FF)) << shift;
        			 }
        			 V[z][i] = Double.longBitsToDouble(value);
        			 offset += doubleSize;
        		}
		    }
		    
		    file_input.close();
		}
		catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while reading " + modelsFile.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
	}
	
    
    
         
    public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{"decrementByOne"}, new String[]{
    							 "data",
                                 "numberOfFactors",
                                 "probeSet",
                                 "writePredictions",
                                 "readPredictions",
                                 "outputModels",
                                 "inputModels",
                                 "writeUserProjections",
                                 "lambda",
                                 "learningRate",
                                 "probeResiduals",
                                 "log",
                                 "stepSize",
                                 "writeResiduals",
        	 					 "delta"})) {
        		 System.err.println("Usage: SVD\n" +
                                 "-data\tString Training data\n" +
                                 "-numberOfFactors\tInteger\n" +
                                 "-probeSet\tString\n" +
                                 "-writePredictions\tString\n" +
                                 "[-readPredictions]\tString" + 
                                 "[-outputModels]\tString\n" +
                                 "[-writeUserProjections]\tString\n" +
                                 "-lambda\tDouble\n" +
                                 "-learningRate\tDouble\n" +
                                 "-probeResiduals\tFile residuals for the probe set, they are added" +
                                 " to the predictions before calculating RMSE\n" +
                                 "[-log]\tFile where RMSEs are written\n" +
                                 "[-stepSize]\tFile, save models every stepSize iterations\n" +
                                 "[-inputModels]\tString\n" +
                                 "[-writeResiduals]\tString\n" +
                                 "[-decrementByOne]\n");
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String data = CommandLineParser.getStringParameter(arguments, "data", true);
        	 int numberOfFactors = CommandLineParser.getIntegerParameter(arguments, "numberOfFactors",true, 0);
        	 String probeSet = CommandLineParser.getStringParameter(arguments, "probeSet", true);
        	 String writePredictions = CommandLineParser.getStringParameter(arguments, "writePredictions", true);
        	 boolean decrementByOne = CommandLineParser.getBooleanParameter(arguments, "decrementByOne", false, true);
        	 double lambda = CommandLineParser.getDoubleParameter(arguments, "lambda", true, 0.01);
        	 double learningRate = CommandLineParser.getDoubleParameter(arguments, "learningRate", true, 0.001);
        	 double delta = CommandLineParser.getDoubleParameter(arguments, "delta",true, 0.0001);
        	 String inputModels = CommandLineParser.getStringParameter(arguments, "inputModels", false);
        	 String outputModels = CommandLineParser.getStringParameter(arguments, "outputModels", false);
        	 String readPredictions = CommandLineParser.getStringParameter(arguments, "readPredictions", false);
        	 String probeResiduals = CommandLineParser.getStringParameter(arguments, "probeResiduals", true);
        	 String writeResiduals = CommandLineParser.getStringParameter(arguments, "writeResiduals", false);
        	 String writeUserProjections = CommandLineParser.getStringParameter(arguments, "writeUserProjections", false);
        	 String log = CommandLineParser.getStringParameter(arguments, "log", false);
        	 int stepSize = CommandLineParser.getIntegerParameter(arguments, "stepSize",false, 0);
        	 boolean readBinary = true;
        	 
        	 
        	 SVD svd = null;
        	 if (log != null){
        		 String logFilename = log + ".nFactors" +  numberOfFactors + ".lambda" + lambda + ".lRate" + learningRate;
        		 svd = new SVD(numberOfFactors, new File(logFilename));
        	 }
        	 else
        		 svd = new SVD(numberOfFactors);
        	 
        	 svd.readTrainingData(new File(data), readBinary);
        	 
        	
        	 svd.readProbeSet(new File(probeSet));
        	 long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read reference ratings in probe set, Memory: " + memory/(1024*1024));
        	 
        	 svd.readResiduals(new File(probeResiduals));
        	 memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read residuals in probe set, Memory: " + memory/(1024*1024));
        	 
        	 if (readPredictions != null){
        		 svd.readPredictions(new File(readPredictions));
        		 System.out.println(svd.calculateRMSE());
        		 System.exit(1);
        	 }
        		 
        	 if (inputModels == null)
        		 svd.randomInitialization();
        	 else {
        		 System.out.print("Reading models...");
        		 svd.readModels(new File(inputModels));
        		 System.out.println("done");
        	 }
        	 
        	 
        	 int nIters = svd.train(lambda, learningRate, delta, stepSize);
        	 System.out.println("Number of iterations: " + nIters);
        	 
        	 if (outputModels != null) {
        		 String modelsFilename = outputModels + ".nFactors" +  numberOfFactors + ".lambda" + lambda + ".lRate" + learningRate +
        		 ".nIters" + nIters;
        		 svd.writeModels(new File(modelsFilename));
        	 }
        	 
        	 String predFilename = writePredictions + ".nFactors" +  numberOfFactors + ".lambda" + lambda + ".lRate" + learningRate +
        	 ".nIters" + nIters;
        	 svd.writePredictions(new File(predFilename)); 	 
        	 
        	 if (writeResiduals != null)
        		 svd.writeTrainingError(new File(writeResiduals));
        	 
        	 if (writeUserProjections != null)
        		 svd.writeUserProjections(new File(writeUserProjections));
        	 
    }	
}


