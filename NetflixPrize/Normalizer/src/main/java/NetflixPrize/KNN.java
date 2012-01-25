package NetflixPrize;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;


public class KNN extends BaselineModel{
	/* Sufficient statistics calculated over the data, necessary for calculating correlations */
	private double[][] sumOfRatingbyRating;
	private double[] sumOfRatingsSquared;
	private short[][] numberOfRaters;
	private double alpha;
	private int numberOfNeighbors;
	private double[][] correlations;
	
	public KNN(int k, double a){
		super();
		numberOfNeighbors = k;
		alpha = a;
	}
	
	public void writeModels(File models){
		try {
			FileOutputStream file_output = new FileOutputStream (models);
		    DataOutputStream data_out = new DataOutputStream (file_output);
		    
		    int doubleSize = Double.SIZE/8;
		    double[] tmp = new double[NUMBER_OF_MOVIES];
			for (int i=0; i<NUMBER_OF_MOVIES; i++){
				tmp[i] = (float)Math.sqrt((double)sumOfRatingsSquared[i]);
				if (tmp[i] == 0.0)
					tmp[i] = 0.001;
			}
			for (int i=0; i<NUMBER_OF_MOVIES; i++){
				int len = NUMBER_OF_MOVIES-(i+1);
				double[] movieCorrelations = new double[len];
				for (int j=0; j<len; j++){
					if (sumOfRatingbyRating[i][j] == 0.0f)
						continue;
					
					double corrML = sumOfRatingbyRating[i][j] / (tmp[i] * tmp[i+j+1]);
					double n = (double)numberOfRaters[i][j];
					double corr = corrML *  n / (n + alpha);
					movieCorrelations[j] = corr;
				}
				byte[] valueBuffer = new byte[doubleSize * len];
		    	int offset=0;
		    	for (int j=0; j<len; j++){
		    		long intBits = Double.doubleToLongBits(movieCorrelations[j]);
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
			System.err.println("Error while writing to " + models.getAbsolutePath());
			System.exit(-1);
		}	
	}
	/*
	public void writeModelsInText(File models){
		try{
			Writer output = new BufferedWriter(new FileWriter(models));
			float[] tmp = new float[NUMBER_OF_MOVIES];
			for (int i=0; i<NUMBER_OF_MOVIES; i++){
				tmp[i] = (float)Math.sqrt((double)sumOfRatingsSquared[i]);
			}
			for (int i=0; i<NUMBER_OF_MOVIES; i++){
				StringBuffer sb = new StringBuffer();
				sb.append(i);
				List<CorrelationsRanker<Integer>> l = 
					new LinkedList<CorrelationsRanker<Integer>>();
				for (int j=0; j<NUMBER_OF_MOVIES-(i+1); j++){
					if (sumOfRatingbyRating[i][j] == 0.0f)
						continue;
					
					float corr = sumOfRatingbyRating[i][j] / (tmp[i] * tmp[i+j+1]);
					l.add(new CorrelationsRanker<Integer>(new Integer(i+j+1), corr));
				}
				
				for (int j=0; j<i; j++){
					if (sumOfRatingbyRating[j][i-j-1] == 0.0f)
						continue;
					
					float corr = sumOfRatingbyRating[j][i-j-1] / (tmp[j] * tmp[i]);
					l.add(new CorrelationsRanker<Integer>(new Integer(j), corr));
				}
				Collections.sort(l);
				
				int nnbr = numberOfNeighbors > l.size() ? l.size() : numberOfNeighbors;
				if (nnbr != numberOfNeighbors){
					System.out.println("Number of neighbors for attribute : " + i + 
							" is " + nnbr);
				}
				for (CorrelationsRanker<Integer> cr : l.subList(0,nnbr)){
					sb.append(","); sb.append(cr.getIndex().shortValue());
					sb.append(","); sb.append(cr.getValue());
				}
				sb.append("\n");
				output.write(sb.toString());
			}
			output.close();
		}
		catch (IOException e){
			System.err.println("Could not write to file " + models.getAbsolutePath());
			System.exit(-1);
		}
		
	}
	*/
	public void readModels(File models){
		if (models == null) {
		      System.err.println("File should not be null.");
		      System.exit(-1);
		}
		if (!models.exists()) {
			System.err.println("File does not exist: " + models.getAbsolutePath());
			System.exit(-1);
	    } 
	    if (!models.isFile()) {
	    	System.err.println("Should not be a directory: " + models.getAbsolutePath());
	    	System.exit(-1);
		}
		if (!models.canRead()) {
			System.err.println("File cannot be read: " + models.getAbsolutePath());
			System.exit(-1);
		}
		
		correlations = new double[NUMBER_OF_MOVIES][];
		for (int i=0; i<NUMBER_OF_MOVIES; i++){
			correlations[i] = new double[NUMBER_OF_MOVIES-(i+1)];
		}
		try { 
			FileInputStream file_input = new FileInputStream (models);
		    DataInputStream data_in    = new DataInputStream (file_input);
		    
		    int doubleSize = Double.SIZE/8;
		    for (int i=0; i<NUMBER_OF_MOVIES; i++){
		    	int len = NUMBER_OF_MOVIES-(i+1);
		    	byte[] doublebuf = new byte[doubleSize * len];
		    	data_in.read(doublebuf);
		    	int offset = 0;
		    	for (int j=0; j<len; j++){
		    		long value = 0;
		    		for (int r = 0; r < doubleSize; r++) {
		    			int shift = (doubleSize - 1 - r) * 8;
		    			value += ( (long)(doublebuf[r + offset] & 0x000000FF)) << shift;
		    		}
		    		correlations[i][j] = Double.longBitsToDouble(value);
		    		offset += doubleSize;
		    	}
		    }
		    		  
		    
		    file_input.close();
		}
		catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while reading " + models.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
	}
	
	/*
	public void readModelsInText(File models){
		correlations = new ArrayList<HashMap<Short, Float>>(NUMBER_OF_MOVIES);
		for (int i=0; i<NUMBER_OF_MOVIES; i++){
			correlations.add(i, new HashMap<Short, Float>());
		}
    	String lineStr;
        try {
        	if (models == null){
    			System.err.println("Null file pointer");
    			throw new IOException();
    		}
    		if (!models.exists()) {
    			System.err.println("File " +  models.getAbsolutePath() + " does not exist");
    			throw new IOException();
    	    }
    	    if (!models.isFile()) {
    	    	System.err.println("File " +  models.getAbsolutePath() + " is not a file");
    	    	throw new IOException();
    		}
    		if (!models.canRead()) {
    			System.err.println("File " +  models.getAbsolutePath() + " cannot be read");
    			throw new IOException();
    		}
        	BufferedReader br = new BufferedReader(new FileReader(models));
        	while ((lineStr = br.readLine()) != null) {
        		String[] st = lineStr.split(",");
        		
        		if ((st.length-1) % 2 != 0){
        			System.err.println("Could not parse line " + lineStr);
        			throw new IOException();
        		}
        		int nnbr = numberOfNeighbors > (st.length-1)/2 ? (st.length-1)/2 : numberOfNeighbors;
        		int i = Integer.parseInt(st[0]);
        		int n=1;
        		HashMap<Short, Float> hm = correlations.get(i);
        		for (int j=1; j<st.length-1; j=j+2){
        			short r =0;
        			try{
        			 r = Short.parseShort(st[j]);
        			}
        			catch (NumberFormatException e){
        				System.err.println(lineStr);
        				throw new IOException();
        			}
        			float c = Float.parseFloat(st[j+1]);
        			
        			hm.put(new Short(r), new Float(c));
        			if (n >= nnbr)
        				break;
        			
        			n ++;
        		}
        		
        	}
        	br.close();
        }
        catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + models + " data\n");
        	System.exit(-1);
        } 
	}
	*/
	public void calculateSufficientStatisticsFromBinary(File profiles){
		System.out.println("Reading binary file " + profiles.getAbsolutePath());
    	try { 
			FileInputStream file_input = new FileInputStream (profiles);
		    DataInputStream data_in    = new DataInputStream (file_input);
		    
		    short[] userProfileLength = new short[NUMBER_OF_USERS];
		    int shortSize = Short.SIZE/8;	    
		    byte[] lenbuf = new byte[shortSize * NUMBER_OF_USERS];
		    data_in.read(lenbuf);
		    int offset = 0;
		    for (int k=0; k<NUMBER_OF_USERS; k++){
		    	short len = 0;
		    	for (int r = 0; r < shortSize; r++) {
		    		int shift = (shortSize - 1 - r) * 8;
		    		len += (lenbuf[r + offset] & 0x000000FF) << shift;
		    	}
		    	offset += shortSize;
        			 
		    	userProfileLength[k] = len;
		    }
		    
		    int floatSize = Float.SIZE/8;
		    for (int i=0; i<NUMBER_OF_USERS; i++){
		    	String user = data_in.readUTF();
       
		    	int len = userProfileLength[i];
		    	short[] movieIndx = new short[len];
		    	float[] movieValue = new float[len];
    			byte[] intbuf = new byte[shortSize * len];
        		data_in.read(intbuf);
        		offset = 0;
        		for (int k=0; k<len; k++){
        			short value = 0;
        			 for (int r = 0; r < shortSize; r++) {
        				 int shift = (shortSize - 1 - r) * 8;
        				 value += (short)(intbuf[r + offset] & 0x000000FF) << shift;
        			 }
        			 movieIndx[k] = value;
        			 offset += shortSize;
        		}
        		
        		
        		
        		byte[] floatbuf = new byte[floatSize * len];
        		data_in.read(floatbuf);
        		offset = 0;
        		for (int k=0; k<len; k++){
        			int value = 0;
        			 for (int r = 0; r < floatSize; r++) {
        				 int shift = (floatSize - 1 - r) * 8;
        				 value += (floatbuf[r + offset] & 0x000000FF) << shift;
        			 }
        			 movieValue[k] = Float.intBitsToFloat(value);
        			 offset += floatSize;
        		}
        		
        		calculateSufficientStatisticsForUser(movieIndx, movieValue);
        		
        		
        		if ((i+1) % 10000 == 0){
        			int it = i + 1;
        			long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        			System.out.println("Read: " + it + " Memory: " + memory/(1024*1024));
        		}
		    }
		    		
		    file_input.close();
		}
		catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while reading " + profiles.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
    }
    

	public void calculateSufficientStatisticsFromText(File profiles){
		
		if (profiles == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!profiles.exists()) {
			System.err.println("File " +  profiles.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!profiles.isFile()) {
	    	System.err.println("File " +  profiles.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!profiles.canRead()) {
			System.err.println("File " +  profiles.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
    	
		System.out.println("Reading ASCII text file " + profiles.getAbsolutePath());
    	int itemCnt=0;
    	String lineStr;
        try { 
        	BufferedReader br = new BufferedReader(new FileReader(profiles));
        	while ((lineStr = br.readLine()) != null) {
        		String[] st = lineStr.split("\\s+");
  /*     		
        		List<ProfileEntry> prf = new ArrayList<ProfileEntry>((st.length-1)/2);
        		for (int k=1; k<st.length-1; k=k+2){
        			short indx = decrementByOne ? (short)(Short.parseShort(st[k])-1) : Short.parseShort(st[k]); 
        			float value = Float.parseFloat(st[k+1]); 	
        			
        			prf.add(new ProfileEntry(indx, value));
        		}
        		*/
        		// Already sorted in the training data, if it wasn't it would be necessary
        		// to sort
        		//Collections.sort(prf);
        		
        		short[] movieIndx = new short[(st.length-1) / 2];
        		float[] value = new float[(st.length-1) / 2];
        		int r=0;
        		for (int k=1; k<st.length-1; k=k+2){
        			short indx = decrementByOne ? (short)(Short.parseShort(st[k])-1) : Short.parseShort(st[k]); 
        			float v = Float.parseFloat(st[k+1]); 	
        			
        			movieIndx[r] = indx;
        			value[r] = v;
        			
        			r ++;
        		}
        		    		
        		calculateSufficientStatisticsForUser(movieIndx, value);
        		
        		
        		if (itemCnt % 10000 == 0){
        			long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        			System.out.println("Read: " + itemCnt + " Memory: " + memory/(1024*1024));
        		}
        		itemCnt ++;
        	}
        	br.close();
        	
        }
        catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + profiles + " data\n");
        	System.exit(-1);
        } 	
	}
	
	private void calculateSufficientStatisticsForUser(short[] movieIndx, float[] value){
		for (int i=0; i<movieIndx.length; i++){
			short indxA = movieIndx[i];
			double valueA = (double)value[i];
			
			sumOfRatingsSquared[indxA] += valueA * valueA;
			for (int j=i+1; j<movieIndx.length; j++){
    			short indxB = movieIndx[j];
    			double valueB = (double)value[j] ;
    			
    			sumOfRatingbyRating[indxA][indxB - indxA - 1] += 
    												valueA * valueB;
    			
    			numberOfRaters[indxA][indxB - indxA - 1] += 1;
			}
		}
	}
	
	public void calculateSufficientStatistics(File profiles, boolean binary){
		sumOfRatingbyRating = new double[NUMBER_OF_MOVIES-1][];
		for (int i=0; i<NUMBER_OF_MOVIES-1; i++){
			sumOfRatingbyRating[i] = new double[NUMBER_OF_MOVIES - (i+1)];
		}
		sumOfRatingsSquared = new double[NUMBER_OF_MOVIES];
		numberOfRaters = new short[NUMBER_OF_MOVIES-1][];
		for (int i=0; i<NUMBER_OF_MOVIES-1; i++){
			numberOfRaters[i] = new short[NUMBER_OF_MOVIES - (i+1)];
		}
		
		if (!binary)
			calculateSufficientStatisticsFromText(profiles);
		else
			calculateSufficientStatisticsFromBinary(profiles);
	}
	
	public double predictRaw(int userId, short movie){
		double nominator=0.0;
		double denominator=0.0;
		List<CorrelationsRanker<Integer>> l = 
			new LinkedList<CorrelationsRanker<Integer>>();
		for (int m=0; m<dataIndex[userId].length; m++){
			short m1 = dataIndex[userId][m];
			double corr=0.0;
			if (m1 < movie)
				corr = correlations[m1][movie-m1-1];
			else
				corr = correlations[movie][m1-movie-1];
			
			l.add(new CorrelationsRanker<Integer>(new Integer(m), (float)corr));
		}
		Collections.sort(l);
		if (numberOfNeighbors != -1){  // Use prediction on a fixed number of neighbors
			int n = numberOfNeighbors > l.size() ? l.size() : numberOfNeighbors;
			for (CorrelationsRanker<Integer> cr : l.subList(0,n)){
				double corr = (double)cr.getValue();
				int m = cr.getIndex();
				nominator += corr * dataValue[userId][m];
				denominator += Math.abs(corr);
			}
		}
		else {// Use threshold-based prediction
			for (CorrelationsRanker<Integer> cr : l){
				double corr = (double)cr.getValue();
				int m = cr.getIndex();
				
				double d = Math.abs(corr);
				if (d < alpha)
					break;
				
				nominator += corr * dataValue[userId][m];
				denominator += d;
			}
		}
		
		if (denominator != 0.0)
			return nominator/denominator;
		else{
			System.out.println("No correlations for user " + userId + " movie " + movie);
			return 0.0;
		}
	}
	
	public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{"decrementByOne"}, new String[]{
    							 "data",
                                 "numberOfNeighbors",
                                 "probeSet",
                                 "writePredictions",
                                 "outputModels",
                                 "inputModels",
                                 "alpha",
                                 "writeResiduals",
                                 "probeResiduals"})) {
        		 System.err.println("Usage: kNN\n" +
                                 "-data\tString\n" +
                                 "-numberOfNeighbors\tInteger\n" +
                                 "-probeSet\tString\n" +
                                 "-writePredictions\tString\n" +
                                 "[-outputModels]\tString\n" +
                                 "[-alpha]\tDouble MAP smoothing parameter\n" +
                                 "-probeResiduals\tFile\n" +
                                 "[-writeResiduals]\tString\n" +
                                 "[-inputModels]\tString\n" +
                                 "[-decrementByOne]\n");
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String data = CommandLineParser.getStringParameter(arguments, "data", true);
        	 String probeSet = CommandLineParser.getStringParameter(arguments, "probeSet", false);
        	 int numberOfNeighbors = CommandLineParser.getIntegerParameter(arguments, "numberOfNeighbors",false, 10);
        	 double alpha = CommandLineParser.getDoubleParameter(arguments, "alpha",false, 0.0f);
        	 String outputModels = CommandLineParser.getStringParameter(arguments, "outputModels", false);
        	 String inputModels = CommandLineParser.getStringParameter(arguments, "inputModels", false);
        	 String writePredictions = CommandLineParser.getStringParameter(arguments, "writePredictions", false);
        	 boolean decrementByOne = CommandLineParser.getBooleanParameter(arguments, "decrementByOne", false, true);
        	 String probeResiduals = CommandLineParser.getStringParameter(arguments, "probeResiduals", false);
        	 
        	 boolean binary = true;
        	 
        	 KNN kNN = new KNN(numberOfNeighbors, alpha);
        	 if (outputModels != null){
        		 kNN.calculateSufficientStatistics(new File(data), binary);
        		 kNN.writeModels(new File(outputModels));
        	 }
        	 else {
        		 kNN.readTrainingData(new File(data), binary);
        		 kNN.readModels(new File(inputModels));
        		 kNN.readProbeSet(new File(probeSet));
        		 kNN.readResiduals(new File(probeResiduals));
        		 kNN.predictProbeSet();
        		 System.out.println("RMSE: " + kNN.calculateRMSE());
        		 if (writePredictions != null)
        			 kNN.writePredictions(new File(writePredictions)); 	
        	 }
	}
}
