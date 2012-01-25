package NetflixPrize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

//TODO: Generalized backoff mechanism

public class MixtureOfGaussians extends BaselineModel{
	private int numberOfClusters;
	private double[][] clusterMeans;
	private double[][] clusterVariances;
	private double[][] logClusterVariances;
	private double[] logClusterWeights;
	private double[][] userMembershipsValues;
	private int[][] userMembershipsIndexes;
	private double S2[][];
	private double S1[][];
	private double S0[][];
	private double S00[];
	private double threshold;
	private double[] globalMeans;
	private double[] globalVariances;
	private double[] globalMeansDenom;
	
	public MixtureOfGaussians(int _nCl, double t){
		super();
		numberOfClusters = _nCl;
		clusterMeans = new double[numberOfClusters][];
		for (int c=0; c<numberOfClusters; c++)
			clusterMeans[c] = new double[NUMBER_OF_MOVIES];
		clusterVariances = new double[numberOfClusters][];
		for (int c=0; c<numberOfClusters; c++)
			clusterVariances[c] = new double[NUMBER_OF_MOVIES];
		logClusterVariances = new double[numberOfClusters][];
		for (int c=0; c<numberOfClusters; c++)
			logClusterVariances[c] = new double[NUMBER_OF_MOVIES];
		logClusterWeights = new double[numberOfClusters];
		userMembershipsValues = new double[NUMBER_OF_USERS][];
		userMembershipsIndexes = new int[NUMBER_OF_USERS][];
		S2 = new double[numberOfClusters][];
		for (int c=0; c<numberOfClusters; c++)
			S2[c] = new double[NUMBER_OF_MOVIES];
		S1 = new double[numberOfClusters][];
		for (int c=0; c<numberOfClusters; c++)
			S1[c] = new double[NUMBER_OF_MOVIES];
		S00 = new double[numberOfClusters];
		S0 = new double[numberOfClusters][];
		for (int c=0; c<numberOfClusters; c++)
			S0[c] = new double[NUMBER_OF_MOVIES];
		threshold = t;
		globalMeans = new double[NUMBER_OF_MOVIES];
		globalVariances = new double[NUMBER_OF_MOVIES];
		globalMeansDenom = new double[NUMBER_OF_MOVIES];
		for (int m=0; m<NUMBER_OF_MOVIES; m++){
			globalMeans[m] = 0.0f;
			globalMeansDenom[m] = 0.0f;
			globalVariances[m] = 0.0f;
		}
	}
	
	private void zeroSufficientStatistics(){
		for (int c=0; c<numberOfClusters; c++){
			S00[c] = 0.0;
			for (int m=0; m<NUMBER_OF_MOVIES; m++){
				S0[c][m] = 0.0;
				S1[c][m] = 0.0;
				S2[c][m] = 0.0;
			}
		}
	}
	
	
	
	public double predictRaw(int userId, short movie){
		double prediction = 0.0;
		for (int c=0; c<userMembershipsIndexes[userId].length; c++){
			int clusterId = userMembershipsIndexes[userId][c];
			double membership = userMembershipsValues[userId][c];
			
			prediction += membership * clusterMeans[clusterId][movie];
		}
		return prediction;
	}
	
	public void randomInitialization(){
		zeroSufficientStatistics();
		Random rand = new Random();
		for (int i=0; i<NUMBER_OF_USERS; i++){
			int clust = rand.nextInt(numberOfClusters);
			userMembershipsIndexes[i] = new int[1];
			userMembershipsValues[i] = new double[1];
			userMembershipsIndexes[i][0] = clust;
			userMembershipsValues[i][0] = 1.0;
			S00[clust] += 1.0;
			for (int m=0; m<dataIndex[i].length; m++){
				short movie = dataIndex[i][m];
				float rating = dataValue[i][m];
				
				S0[clust][movie] += 1.0;
				S1[clust][movie] += rating;
				S2[clust][movie] += rating * rating;
				
				
				globalMeansDenom[movie] += 1.0;
				globalMeans[movie] += rating;
				globalVariances[movie] += rating * rating;
			}
		}
		
		for (int m=0; m<NUMBER_OF_MOVIES; m++){
			globalMeans[m] /= globalMeansDenom[m];
			globalVariances[m] /= globalMeansDenom[m];
			globalVariances[m] -= globalMeans[m] * globalMeans[m];
		}
		Mstep();
		
		System.out.println("RMSE: " + predictProbeSet());
	}
	
	private void Mstep(){
		double tot=0.0;
		for (int c=0; c<numberOfClusters; c++)
			tot += S00[c];
		for (int c=0; c<numberOfClusters; c++)
			logClusterWeights[c] = Math.log(S00[c] / tot);
		
		for (int c=0; c<numberOfClusters; c++){
			for (int m=0; m<NUMBER_OF_MOVIES; m++){
				double weight = S0[c][m] > threshold ? 1.0 : S0[c][m] / threshold;
				if (weight == 0.0)
					clusterMeans[c][m] = globalMeans[m];
				else {
					double MLmean = S1[c][m] / S0[c][m];
					clusterMeans[c][m] = weight * MLmean + (1.0 - weight) * globalMeans[m];
				}
			}
			for (int m=0; m<NUMBER_OF_MOVIES; m++){
				double weight = S0[c][m] > threshold ? 1.0 : S0[c][m] / threshold;
				if (weight == 0.0 || S0[c][m] <= 1.0)
					clusterVariances[c][m] = globalVariances[m];
				else {
					double MLvariance = (S2[c][m] / (S0[c][m]-1.0)) - 
				(S0[c][m]/(S0[c][m]-1.0))*clusterMeans[c][m] * clusterMeans[c][m];
					clusterVariances[c][m] = weight * MLvariance + (1.0 - weight) * globalVariances[m];
				}
				
				if (clusterVariances[c][m] <= 0.0)
					clusterVariances[c][m] = 0.001;
				
				logClusterVariances[c][m] = Math.log(clusterVariances[c][m]);
			}
		}
			
	}
	
	private void Estep(){
		zeroSufficientStatistics();
		double[] logProbCluster = new double[numberOfClusters];
		double[] userProbability = new double[numberOfClusters];
		for (int i=0; i<NUMBER_OF_USERS; i++){
			for (int c=0; c<numberOfClusters; c++){
				logProbCluster[c] = logClusterWeights[c];
				double partialSum = 0.0;
				for (int m=0; m<dataIndex[i].length; m++){
					short movie = dataIndex[i][m];
					double rating = dataValue[i][m];
					
					double diff = rating - clusterMeans[c][movie];
					
					partialSum -= logClusterVariances[c][movie] + 
					((diff * diff) / clusterVariances[c][movie]);
				}
				partialSum *= 0.5;
				logProbCluster[c] += partialSum;
			}
			
			// Now calculate p(c|u)
			double maxLogProb = logProbCluster[0];
			for (int c=1; c<numberOfClusters; c++)
				if (logProbCluster[c] > maxLogProb)
					maxLogProb = logProbCluster[c];
			double sum = 0.0; int nnz=0;
			for (int c=0; c<numberOfClusters; c++){
				double denom=0.0; 
				if (maxLogProb - logProbCluster[c] >= 10.0){
					userProbability[c] = 0.0;
					continue;
				}
				boolean found = false;
				for (int k=0; k<numberOfClusters; k++){
					double diff = logProbCluster[k] - logProbCluster[c];
					if (diff < -10.0)
						continue;
					
					denom += diff == 0 ? 1.0 : Math.exp(diff);
					if (denom >= 1e+4){
						userProbability[c] = 0.0;
						found = true;
						break;
					}
				}
				
				if (found)
					continue;
				
			
				userProbability[c] = 1.0 / denom;
				nnz ++;
				sum += userProbability[c];	
			}
			if (sum <= 0.8){
				System.err.println("ERROR: Invalid assignments of user to clusters " + 
						reverseUserNames[i]);
				System.err.flush();
				System.exit(1);
			}
			// If the sum does not equal 1, make sure it does
			if (sum <= 0.999)
				for (int c=0; c<numberOfClusters; c++)
					userProbability[c] /= sum;
			
			userMembershipsValues[i] = new double[nnz];
			userMembershipsIndexes[i] = new int[nnz];
			int g=0;
			// Now calculate sufficient statistics
			for (int c=0; c<numberOfClusters; c++){
				if (userProbability[c] > 0.0){
					S00[c] += userProbability[c];
					for (int m=0; m<dataIndex[i].length; m++){
						short movie = dataIndex[i][m];
						double rating = dataValue[i][m];
						S1[c][movie] += userProbability[c] * rating;
						S2[c][movie] += userProbability[c] * rating * rating;
						S0[c][movie] += userProbability[c];
					}
					userMembershipsIndexes[i][g] = c;
					userMembershipsValues[i][g] = userProbability[c];
					g ++;
				}
			}
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
		
		    for (int c=0; c<numberOfClusters; c++)
		    	logClusterWeights[c] = data_in.readDouble();
		    
		    for (int c=0; c<numberOfClusters; c++)
		    	for (int i=0; i<NUMBER_OF_MOVIES; i++)
		    		clusterMeans[c][i] = data_in.readDouble();
		    
		    for (int c=0; c<numberOfClusters; c++)
		    	for (int i=0; i<NUMBER_OF_MOVIES; i++)
		    		clusterVariances[c][i] = data_in.readDouble();
		    
		    for (int c=0; c<numberOfClusters; c++)
		    	for (int i=0; i<NUMBER_OF_MOVIES; i++)
		    		logClusterVariances[c][i] = data_in.readDouble();
		    		
		    file_input.close();
		}
		catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while reading " + modelsFile.getAbsolutePath() + " data\n");
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
		    
		    for (int c=0; c<numberOfClusters; c++)
		    	data_out.writeDouble(logClusterWeights[c]);
		    
		    for (int c=0; c<numberOfClusters; c++)
		    	for (int i=0; i<NUMBER_OF_MOVIES; i++)
		    		data_out.writeDouble(clusterMeans[c][i]);
		    
		    for (int c=0; c<numberOfClusters; c++)
		    	for (int i=0; i<NUMBER_OF_MOVIES; i++)
		    		data_out.writeDouble(clusterVariances[c][i]);
		    
		    for (int c=0; c<numberOfClusters; c++)
		    	for (int i=0; i<NUMBER_OF_MOVIES; i++)
		    		data_out.writeDouble(logClusterVariances[c][i]);
		    
		    file_output.close();
		}
		catch (IOException e){
			e.printStackTrace();
			System.err.println("Error while writing to " + modelsFile.getAbsolutePath());
			System.exit(-1);
		}	
	}
	
	public int train(double delta){
		int iter=1;
        double previousRmse = 1000.0;
		while (true){
			
			Estep();
			Mstep();
			double rmse = predictProbeSet();
    	
    	
			// Delta is overloaded. If smaller than unity, it is used as the diffence in RMSE to stop training
			// If bigger than 1 then it is the number of iterations that will be applied. The first semantics will be used
			// when the probe set is available and the second when the training data includes the probe set
			if (delta < 1.0){   	
				System.out.println("RMSE: " + rmse);
				if (previousRmse - rmse < delta)
					break;
			}
			else if (iter >= Math.round(delta))
					break;
    	

			iter ++; 
			previousRmse = rmse;
		}
		return(iter);
	}
	
	
	public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{"decrementByOne"}, new String[]{
    							 "data",
                                 "numberOfClusters",
                                 "probeSet",
                                 "writePredictions",
                                 "outputModels",
                                 "inputModels",
                                 "threshold",
                                 "writeResiduals",
                                 "delta",
                                 "probeResiduals"})) {
        		 System.err.println("Usage: MixtureOfGaussians\n" +
                                 "-data\tString\n" +
                                 "-numberOfClusters\tInteger\n" +
                                 "-probeSet\tString\n" +
                                 "-writePredictions\tString\n" +
                                 "[-outputModels]\tString\n" +
                                 "-delta\tDouble" +
                                 "[-threshold]\tDouble MAP threshold\n" +
                                 "-probeResiduals\tFile\n" +
                                 "[-writeResiduals]\tString\n" +
                                 "[-inputModels]\tString\n" +
                                 "[-decrementByOne]\n");
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String data = CommandLineParser.getStringParameter(arguments, "data", true);
        	 int numberOfClusters = CommandLineParser.getIntegerParameter(arguments, "numberOfClusters",true, 0);
        	 String probeSet = CommandLineParser.getStringParameter(arguments, "probeSet", true);
        	 String writePredictions = CommandLineParser.getStringParameter(arguments, "writePredictions", true);
        	 boolean decrementByOne = CommandLineParser.getBooleanParameter(arguments, "decrementByOne", false, true);
        	 String inputModels = CommandLineParser.getStringParameter(arguments, "inputModels", false);
        	 String outputModels = CommandLineParser.getStringParameter(arguments, "outputModels", false);
        	 String probeResiduals = CommandLineParser.getStringParameter(arguments, "probeResiduals", true);
        	 double threshold = CommandLineParser.getDoubleParameter(arguments, "threshold",false, 1.0f);
        	 double delta = CommandLineParser.getDoubleParameter(arguments, "delta",true, 0.0001);
        	 String writeResiduals = CommandLineParser.getStringParameter(arguments, "writeResiduals", false);
        	 boolean readBinary = true;
        	 
        	 
        	 MixtureOfGaussians mixGauss = new MixtureOfGaussians(numberOfClusters, (float)threshold);
        	 
        	 mixGauss.readTrainingData(new File(data), readBinary);
        	 mixGauss.readProbeSet(new File(probeSet));
        	 long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read reference ratings in probe set, Memory: " + memory/(1024*1024));
        	 
        	 mixGauss.readResiduals(new File(probeResiduals));
        	 memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        	 System.out.println("Read residuals in probe set, Memory: " + memory/(1024*1024));
        		 
        	 if (inputModels == null)
        		 mixGauss.randomInitialization();
        	 else 
        		 mixGauss.readModels(new File(inputModels));
        	 
        	 
        	
        	 int nIters = mixGauss.train(delta);
        	 System.out.println("Number of iterations: " + nIters);
        	 
        	 if (outputModels != null) {
        		 String modelsFilename = outputModels + ".nClusters" + numberOfClusters + ".MAP" + 
        		 threshold + ".nIters" + nIters;
        		 mixGauss.writeModels(new File(modelsFilename));
        	 }
        	 
        	 String predFilename = writePredictions + ".nClusters" + numberOfClusters + ".MAP" + threshold + ".nIters" + nIters;
        	 mixGauss.writePredictions(new File(predFilename)); 	 
        	 
        	 if (writeResiduals != null)
        		 mixGauss.writeTrainingError(new File(writeResiduals));
        	 
        	 
	}
}
