package NetflixPrize;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * User similarities are computed in a dense space, usually coming from latent-based methods
 * while rating prediction happens on the original rating-based space
 */
public class DenseProfileUserBasedKNN extends BaselineModel{
	private DenseProfile[] profiles;
	private double[] norm;
	private int userAssociationsIndx[][];
	private float userAssociationsScore[][];
	private int numberOfNeighbors;  /* Number of neighbors that will be used for prediction */
	private int maxNeighbors; /* Number of neighbors stored in the model */
	private int numberOfFactors;
	
	public DenseProfileUserBasedKNN(){
		
	}
	public void setNumberOfNeighbors(int k){
		numberOfNeighbors = k;
	}
	public void setMaxNeighbors(int k){
		maxNeighbors = k;
	}
	private class DenseProfile {
		private String user;
		private float[] value;
		
		public DenseProfile(String u, float[] v){
			user = u;
			value = v;
		}
		public float[] getValues(){
			return value;
		}
		public String getUser(){
			return user;
		}
	}
	public void readDenseProfiles(File denseProfilesFile){
		/* Read dense user profiles */
		if (denseProfilesFile == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!denseProfilesFile.exists()) {
			System.err.println("File " +  denseProfilesFile.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!denseProfilesFile.isFile()) {
	    	System.err.println("File " +  denseProfilesFile.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!denseProfilesFile.canRead()) {
			System.err.println("File " +  denseProfilesFile.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		profiles = new DenseProfile[NUMBER_OF_USERS];
		norm = new double[NUMBER_OF_USERS];
		try { 
			FileInputStream file_input = new FileInputStream (denseProfilesFile);
		    DataInputStream data_in    = new DataInputStream (file_input);
		    
		    int shortSize = Short.SIZE/8;
		    byte[] intbuf = new byte[shortSize];
    		data_in.read(intbuf);
    		int value = 0;
    		for (int r = 0; r < shortSize; r++) {
    			int shift = (shortSize - 1 - r) * 8;
    			value += (int)(intbuf[r] & 0x000000FF) << shift;
    		}
    		numberOfFactors = (int)value;
    		System.out.println("Dense profile length : " + numberOfFactors);
    		
		    int floatSize = Float.SIZE/8;
		    for (int i=0; i<NUMBER_OF_USERS; i++){
		    	String user = data_in.readUTF();    	
       
    				
        		byte[] floatbuf = new byte[floatSize * numberOfFactors];
        		data_in.read(floatbuf);
        		int offset = 0;
        		float[] prf = new float[numberOfFactors];
        		for (int k=0; k<numberOfFactors; k++){
        			value = 0;
        			for (int r = 0; r < floatSize; r++) {
        				int shift = (floatSize - 1 - r) * 8;
        				value += (floatbuf[r + offset] & 0x000000FF) << shift;
        			}
        			prf[k] = Float.intBitsToFloat(value);
        			offset += floatSize;
        		}
        		profiles[i] = new DenseProfile(user, prf);
 /*       		
        		if ((i+1) % 10000 == 0){
        			int it = i + 1;
        			long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        			System.out.println("Dense profiles read: " + it + " Memory: " + memory/(1024*1024));
        		}
        		*/
		    }
		    		
		    file_input.close();
		    data_in.close();
		}
		catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while reading " + denseProfilesFile.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
        
		/* After dense profiles have been read, estimate factor mean */
		System.out.println("Calculating user norms");
		double[] mean = new double[numberOfFactors];
		for (int f=0; f<numberOfFactors; f++){
			mean[f] = 0.0;
		}
		for (int p=0; p<profiles.length; p++){
			float[] profile = profiles[p].getValues();
			for (int v=0; v<numberOfFactors; v++)
				mean[v] += profile[v];
		}
		for (int v=0; v<numberOfFactors; v++)
			mean[v] /= (double)profiles.length;
		
		/* Go through the data and remove the mean */
		for (int p=0; p<profiles.length; p++){
			float[] profile = profiles[p].getValues();
			for (int v=0; v<numberOfFactors; v++)
				profile[v] -= mean[v]; 
		}
		
		/* Now estimate user norm */
		for (int p=0; p<profiles.length; p++){
			float[] profile = profiles[p].getValues();
			double var=0.0;
			for (int v=0; v<numberOfFactors; v++)
				var += profile[v] * profile[v];
			if (var == 0.0){
				System.err.println("Zero norm for user " + profiles[p].getUser());
				System.exit(-1);
			}
			norm[p] = 1.0 / Math.sqrt(var);
		}
	}
	/*
	 * Need to normalize the dense data first, need to compute the mean of each factor and 
	 * the norm
	 */
	public void findNeighbors(int startUser, int endUser, String neighborsFilePrefix){
		int sus = startUser > 0 ? startUser : 0;
		int eus = endUser < profiles.length ? endUser : profiles.length;
		if (eus < sus){
			System.err.println("Invalid arguments for range of users, START: " + sus + " END: " + eus);
			System.exit(1);
		}
		File neighborsFile = new File(neighborsFilePrefix + "_" + sus + "_" + eus + ".bin");
		try { 
    		FileOutputStream file_output = new FileOutputStream (neighborsFile);
		    DataOutputStream data_out = new DataOutputStream (file_output);
		    
		    for (int i=sus; i<eus; i++){
		    	String user = profiles[i].getUser();
		    	
		    	List<CorrelationsRanker<String>> userList = 
		    		new LinkedList<CorrelationsRanker<String>>();
		    	double minimumScore;
		    	float[] prfA = profiles[i].getValues();
		    	/* Add the first numberOfNeighbors users */
		    	for (int j=0; j<numberOfNeighbors; j++){
		    		float[] prfB = profiles[j].getValues();
		    		double score=0.0;
		    		for (int f=0; f<prfB.length; f++){
		    			score += prfA[f] * prfB[f];
		    		}
		    		score *= (norm[i] * norm[j]);
				
		    		
		    		if (i != j){
		    			if (score > 1.0 || score < -1.0){
			    			System.err.println("Illegal score (" + score + ") for users " + 
			    					profiles[i].getUser() + " and " + profiles[j].getUser());
			    			System.exit(-1);
			    		}
		    			userList.add(new CorrelationsRanker<String>(profiles[j].getUser(), score));
		    		}
				}
		    	Collections.sort(userList);
				minimumScore=Math.abs(userList.get(userList.size()-1).getValue());
			
				/* For the rest, check whether to add or not */
				for (int j=numberOfNeighbors; j<profiles.length; j++){
					float[] prfB = profiles[j].getValues();
					double score=0.0;
					for (int f=0; f<prfB.length; f++){
						score += prfA[f] * prfB[f];
					}
					score *= (norm[i] * norm[j]);
					
					
					if (Math.abs(score) < minimumScore){
						continue;
					}
					else {
						if (i == j)
							continue;
						
						if (score > 1.0 || score < -1.0){
			    			System.err.println("Illegal score (" + score + ") for users " + 
			    					profiles[i].getUser() + " and " + profiles[j].getUser());
			    			System.exit(-1);
			    		}
						
						userList.add(new CorrelationsRanker<String>(profiles[j].getUser(), score));
						if (userList.size() > 2 * numberOfNeighbors){
							Collections.sort(userList);
							userList = userList.subList(0, numberOfNeighbors);
							minimumScore=Math.abs(userList.get(userList.size()-1).getValue());
						}					
					}
				}
				Collections.sort(userList);
				userList = userList.subList(0, numberOfNeighbors);
			
				/* Neighbors are determined */
				// Write neighbors here
				int floatSize = Float.SIZE/8;
				byte[] valueBuffer = new byte[floatSize * numberOfNeighbors];
		    	int offset=0;
		    	data_out.writeUTF(user);
		    	for (CorrelationsRanker<String> cr : userList){
		    		String neighbor = cr.getIndex();
		    		data_out.writeUTF(neighbor);
		    		double score = cr.getValue();
		    		int intBits = Float.floatToRawIntBits((float)score);
		    		valueBuffer[offset] = (byte)(intBits >>> 24);
		    		valueBuffer[offset + 1] = (byte)(intBits >>> 16);
		    		valueBuffer[offset + 2] = (byte)(intBits >>> 8);
		    		valueBuffer[offset + 3] = (byte)intBits;
			    	offset += floatSize;
		    	}
		    	data_out.write(valueBuffer);
		    	
		    	if ((i+1) % 100 == 0){
        			int it = i + 1;
        			
        			System.out.println("Calculated user neighbors for: " + it + " users");
        		}
		    }
		    data_out.close();
		}
		catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while writing " + neighborsFile.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
	}
	
	public void readModels(File models){
		if (models == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!models.exists()) {
			System.err.println("File " +  models.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!models.isFile()) {
	    	System.err.println("File " +  models.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!models.canRead()) {
			System.err.println("File " +  models.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		try { 
			FileInputStream file_input = new FileInputStream (models);
		    DataInputStream data_in    = new DataInputStream (file_input);
	/*	    
		    int intSize = Integer.SIZE/8;
		    byte[] intbuf = new byte[intSize];
    		data_in.read(intbuf);
    		int value = 0;
    		for (int r = 0; r < intSize; r++) {
    			int shift = (intSize - 1 - r) * 8;
    			value += (int)(intbuf[r] & 0x000000FF) << shift;
    		}
    		maxNeighbors = value;
    	*/	
    		userAssociationsIndx = new int[NUMBER_OF_USERS][];
    		userAssociationsScore = new float[NUMBER_OF_USERS][];
    		for (int u=0; u<NUMBER_OF_USERS; u++){
    			userAssociationsIndx[u] = new int[numberOfNeighbors];
    			userAssociationsScore[u] = new float[numberOfNeighbors];
    		}
    		int floatSize = Float.SIZE/8;
		    for (int i=0; i<NUMBER_OF_USERS; i++){
		    	String user = data_in.readUTF();    	
		    	Integer indx = userNames.get(user);
	    		if (indx == null){
	    			System.err.println("Cannot retrieve index for user " + user);
	    			System.exit(1);
	    		}
	    		int u = indx.intValue();
	    		
	    		/* Read the user similarity indices */
		    	for (int k=0; k<numberOfNeighbors; k++){
		    		String userId = data_in.readUTF();
		    		indx = userNames.get(userId);
		    		if (indx == null){
		    			System.err.println("Cannot retrieve index for user " + userId);
		    			System.exit(1);
		    		}
		    		userAssociationsIndx[u][k] = indx.intValue();
		    	}
		    	/* Ignore the rest of neighbors */
		    	for (int k=numberOfNeighbors; k<maxNeighbors; k++){
		    		String userId = data_in.readUTF();
		    	}
		    	
		    	/* Read the user similarity scores */
        		byte[] floatbuf = new byte[floatSize * maxNeighbors];
        		data_in.read(floatbuf);
        		int offset = 0;
        		for (int k=0; k<numberOfNeighbors; k++){
        			int value = 0;
        			for (int r = 0; r < floatSize; r++) {
        				int shift = (floatSize - 1 - r) * 8;
        				value += (floatbuf[r + offset] & 0x000000FF) << shift;
        			}
        			userAssociationsScore[u][k] = Float.intBitsToFloat(value);
        			offset += floatSize;
        		}
        		/* Ignore the rest of neighbors */
        		for (int k=numberOfNeighbors; k<maxNeighbors; k++){
        			int value = 0;
        			for (int r = 0; r < floatSize; r++) {
        				int shift = (floatSize - 1 - r) * 8;
        				value += (floatbuf[r + offset] & 0x000000FF) << shift;
        			}
        			offset += floatSize;
        		}
        		
        		if ((i+1) % 10000 == 0){
        			int it = i + 1;
        			long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        			System.out.println("Read neighbors for " + it + " users, Memory: " + memory/(1024*1024));
        		}
		    }
		}
		catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while reading " + models.getAbsolutePath());
			System.exit(-1);
		}
        
	}
	public void writeModels(File models){
		// Need to write the numberOfNeighbors first */
	}
	public double predictRaw(int userId, short movie){
		double predNom=0.0;
		double predDenom=0.0;
		int predCount=0;
		int[] neighbors = userAssociationsIndx[userId];
		int nnbr = numberOfNeighbors > userAssociationsIndx[userId].length ?
				 userAssociationsIndx[userId].length : numberOfNeighbors;
		for (int n=0; n<nnbr; n++){
			short[] neighborMovies = dataIndex[neighbors[n]];
			for (short s=0; s<neighborMovies.length; s++){
				if (s == movie){
					double score = userAssociationsScore[userId][n];
					predNom += score * dataValue[neighbors[n]][s];
					predDenom += Math.abs(score);
					predCount ++;
					break;
				}
			}
		}
		
		double MLpred = predDenom > 0.0 ? predNom / predDenom : 0.0;
		
		return MLpred;
	}
	
	public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{"decrementByOne"}, new String[]{
    							 "denseProfiles",
    							 "ratingProfiles",
                                 "numberOfNeighbors",
                                 "probeSet",
                                 "writePredictions",
                                 "outputModels",
                                 "userAffinityStart",
                                 "userAffinityEnd",
                                 "userAffinityScoresFile",
                                 "inputModels",
                                 "alpha",
                                 "writeResiduals",
                                 "probeResiduals"})) {
        		 System.err.println("Usage: DenseProfileUserBasedkNN\n" +
                                 "-denseProfiles\tString\n" +
                                 "-ratingProfiles\tString\n" +
                                 "-userAffinityScoresFile\tString\n" +
                                 "-userAffinityStart\n"+
                                 "-userAffinityEnd\n" +
                                 "-numberOfNeighbors\tInteger\n" +
                                 "-probeSet\tString\n" +
                                 "-writePredictions\tString\n" +
                                 "[-alpha]\tDouble MAP smoothing parameter\n" +
                                 "-probeResiduals\tFile\n" +
                                 "[-writeResiduals]\tString\n" +
                                 "[-inputModels]\tString\n" +
                                 "[-decrementByOne]\n");
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String denseProfiles = CommandLineParser.getStringParameter(arguments, "denseProfiles", false);
        	 String ratingProfiles = CommandLineParser.getStringParameter(arguments, "ratingProfiles", false);
        	 String userAffinityScoresFile = CommandLineParser.getStringParameter(arguments, "userAffinityScoresFile", false);
        	 int userAffinityStart = CommandLineParser.getIntegerParameter(arguments, "userAffinityStart", false, 10);
        	 int userAffinityEnd = CommandLineParser.getIntegerParameter(arguments, "userAffinityEnd", false, 10);
        	 String probeSet = CommandLineParser.getStringParameter(arguments, "probeSet", false);
        	 int numberOfNeighbors = CommandLineParser.getIntegerParameter(arguments, "numberOfNeighbors", true, 10);
        	 double alpha = CommandLineParser.getDoubleParameter(arguments, "alpha",false, 0.0f);
        	 String inputModels = CommandLineParser.getStringParameter(arguments, "inputModels", false);
        	 String writePredictions = CommandLineParser.getStringParameter(arguments, "writePredictions", false);
        	 boolean decrementByOne = CommandLineParser.getBooleanParameter(arguments, "decrementByOne", false, true);
        	 String probeResiduals = CommandLineParser.getStringParameter(arguments, "probeResiduals", false);
        	 
        	 DenseProfileUserBasedKNN userKNN = new DenseProfileUserBasedKNN();
        	 
        	 if (denseProfiles != null){
        		 userKNN.readDenseProfiles(new File(denseProfiles));
        		 userKNN.setNumberOfNeighbors(numberOfNeighbors);
        		 userKNN.findNeighbors(userAffinityStart, userAffinityEnd, userAffinityScoresFile);
        	 }
        	 else {
        		 userKNN.readTrainingData(new File(ratingProfiles), true);
        		 userKNN.setMaxNeighbors(1000);
        		 userKNN.setNumberOfNeighbors(numberOfNeighbors);
        		 userKNN.readModels(new File(inputModels));
        		 userKNN.readProbeSet(new File(probeSet));
        		 userKNN.readResiduals(new File(probeResiduals));
        		 System.out.println("RMSE: " + userKNN.predictProbeSet());
        		 userKNN.writePredictions(new File(writePredictions));
        	 }
	}
}
