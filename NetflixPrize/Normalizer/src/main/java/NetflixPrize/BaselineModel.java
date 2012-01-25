package NetflixPrize;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;



// TODO: Need to be adding multiple residuals

public abstract class BaselineModel {
	protected short[][] probeSetIndex;
	protected float[][] probeSetRating;
	protected short[][] probeSetYear;
	protected byte[][] probeSetMonth;
	protected byte[][] probeSetDay;
	
	protected short[][] residualsIndex;
	protected float[][] residualsValue;
	protected short[][] predictionsIndex;
	protected float[][] predictionsValue;
    protected String [] attributeName;
    
    protected short[][] dataIndex;
    protected float[][] dataValue;
    protected short[][] dataYear;
    protected byte[][] dataMonth;
    protected byte[][] dataDay;
    
    protected String[] reverseUserNames;
    protected HashMap<String, Integer> userNames;
    protected boolean decrementByOne;
    protected static final int NUMBER_OF_MOVIES = 17770;
    protected static final int NUMBER_OF_USERS = 480189;
    protected Writer logWriter;
    
    public BaselineModel(){
    	reverseUserNames = new String[NUMBER_OF_USERS];
    	userNames = new HashMap<String, Integer>();
    	decrementByOne = true;
    	logWriter = null;
    }
    
    public BaselineModel(File log){
    	reverseUserNames = new String[NUMBER_OF_USERS];
    	userNames = new HashMap<String, Integer>();
    	decrementByOne = true;
    	try {
    		logWriter = new BufferedWriter( new FileWriter(log) );
    	}
    	catch (IOException e){
    		System.err.println("ERROR: Cannot open log file for writing");
    		System.exit(1);
    	}
    }
    
    private void readBinaryUserProfiles(File file){
    	System.out.println("Reading binary file " + file.getAbsolutePath());
    	try { 
			FileInputStream file_input = new FileInputStream (file);
		    DataInputStream data_in    = new DataInputStream (file_input);
		    
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
        			 
		    	dataIndex[k] = new short[len];
		    	dataValue[k] = new float[len];
		    }
		    
		    int floatSize = Float.SIZE/8;
		    for (int i=0; i<NUMBER_OF_USERS; i++){
		    	String user = data_in.readUTF();
		    	
		    	userNames.put(new String(user), new Integer(i));
    			reverseUserNames[i] = new String(user);
		    	
       
    			int len = dataIndex[i].length;
    			byte[] intbuf = new byte[shortSize * len];
        		data_in.read(intbuf);
        		offset = 0;
        		for (int k=0; k<len; k++){
        			short value = 0;
        			 for (int r = 0; r < shortSize; r++) {
        				 int shift = (shortSize - 1 - r) * 8;
        				 value += (short)(intbuf[r + offset] & 0x000000FF) << shift;
        			 }
        			 dataIndex[i][k] = value;
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
        			 dataValue[i][k] = Float.intBitsToFloat(value);
        			 offset += floatSize;
        		}
        		
        		
        		
        		
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
			System.err.println("Error while reading " + file.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
    }
    
    private void readUserProfilesWithTime(File file, 
    		short[][] indexMatrix, float[][] valueMatrix, short[][] year, byte[][] month, byte[][] day){
    	if (file == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!file.exists()) {
			System.err.println("File " +  file.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!file.isFile()) {
	    	System.err.println("File " +  file.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!file.canRead()) {
			System.err.println("File " +  file.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
    	
		System.out.println("Reading ASCII text file " + file.getAbsolutePath());
    	int itemCnt=-1;
    	String lineStr;
        try { 
        	BufferedReader br = new BufferedReader(new FileReader(file));
        	while ((lineStr = br.readLine()) != null) {
        		String[] st = lineStr.split("\\s");
        		
        		if (! userNames.containsKey(st[0])){
        			itemCnt ++;
        			userNames.put(new String(st[0]), new Integer(itemCnt));
        			reverseUserNames[itemCnt] = new String(st[0]);
        		}
        		else 
        			itemCnt = userNames.get(st[0]).intValue();
        		
        		
        		indexMatrix[itemCnt] = new short[(st.length-1)/4];
        		valueMatrix[itemCnt] = new float[(st.length-1)/4];
        		year[itemCnt] = new short[(st.length-1)/4];
        		month[itemCnt] = new byte[(st.length-1)/4];
        		day[itemCnt] = new byte[(st.length-1)/4];
       
        		for (int k=1; k<st.length-1; k=k+4){
        			short indx = decrementByOne ? (short)(Short.parseShort(st[k])-1) : Short.parseShort(st[k]); 
        				
        			indexMatrix[itemCnt][(k-1)/2] = indx;
        			valueMatrix[itemCnt][(k-1)/2] = Float.parseFloat(st[k+1]);
        			year[itemCnt][(k-1)/2] = Short.parseShort(st[k+2]);
        			month[itemCnt][(k-1)/2] = Byte.parseByte(st[k+3]);
        			day[itemCnt][(k-1)/2] = Byte.parseByte(st[k+4]);
        		}
        		
        		
        		if ((itemCnt+1) % 10000 == 0){
        			int it = itemCnt + 1;
        			long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        			System.out.println("Read: " + it + " Memory: " + memory/(1024*1024));
        		}
        	}
        	br.close();
        	
        }
        catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + file + " data\n");
        	System.exit(-1);
        } 	
    }
    
    
    private void readUserProfiles(File file, short[][] indexMatrix, float[][] valueMatrix){
    	if (file == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!file.exists()) {
			System.err.println("File " +  file.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!file.isFile()) {
	    	System.err.println("File " +  file.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!file.canRead()) {
			System.err.println("File " +  file.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
    	
		System.out.println("Reading ASCII text file " + file.getAbsolutePath());
    	int itemCnt=-1;
    	String lineStr;
        try { 
        	BufferedReader br = new BufferedReader(new FileReader(file));
        	while ((lineStr = br.readLine()) != null) {
        		String[] st = lineStr.split("\\s");
        		
        		if (! userNames.containsKey(st[0])){
        			itemCnt ++;
        			userNames.put(new String(st[0]), new Integer(itemCnt));
        			reverseUserNames[itemCnt] = new String(st[0]);
        		}
        		else 
        			itemCnt = userNames.get(st[0]).intValue();
        		
        		indexMatrix[itemCnt] = new short[(st.length-1)/2];
        		valueMatrix[itemCnt] = new float[(st.length-1)/2];
        		
       
        		for (int k=1; k<st.length-1; k=k+2){
        			short indx = decrementByOne ? (short)(Short.parseShort(st[k])-1) : Short.parseShort(st[k]);
        			   
        				
        			indexMatrix[itemCnt][(k-1)/2] = indx;
        			valueMatrix[itemCnt][(k-1)/2] = Float.parseFloat(st[k+1]);     
        		}
        		
        		
        		if ((itemCnt+1) % 10000 == 0){
        			int it = itemCnt + 1;
        			long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        			System.out.println("Read: " + it + " Memory: " + memory/(1024*1024));
        		}
        	}
        	br.close();
        	
        }
        catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + file + " data\n");
        	System.exit(-1);
        } 	
    }
    
    public void readTrainingDataWithTime(File trainingDataFile){
    	dataIndex = new short[NUMBER_OF_USERS][];
    	dataValue = new float[NUMBER_OF_USERS][];
    	dataYear = new short[NUMBER_OF_USERS][];
    	dataMonth = new byte[NUMBER_OF_USERS][];
    	dataDay = new byte[NUMBER_OF_USERS][];
    	readUserProfilesWithTime(trainingDataFile, dataIndex, dataValue, dataYear, dataMonth, dataDay);
    }
    
    public void readTrainingData(File trainingDataFile, boolean binary){
    	dataIndex = new short[NUMBER_OF_USERS][];
    	dataValue = new float[NUMBER_OF_USERS][];
    	if (binary)
    		readBinaryUserProfiles(trainingDataFile);
    	else
    		readUserProfiles(trainingDataFile, dataIndex, dataValue);
    }
    
    public void readProbeSetWithTime(File probeSetFile){
    	probeSetIndex = new short[NUMBER_OF_USERS][];
    	probeSetRating = new float[NUMBER_OF_USERS][];
    	probeSetYear = new short[NUMBER_OF_USERS][];
    	probeSetMonth = new byte[NUMBER_OF_USERS][];
    	probeSetDay = new byte[NUMBER_OF_USERS][];
    	readUserProfilesWithTime(probeSetFile, probeSetIndex, probeSetRating, probeSetYear, probeSetMonth, probeSetDay);
    	predictionsIndex = probeSetIndex;
    	predictionsValue = new float[NUMBER_OF_USERS][];
    	for (int i=0; i<NUMBER_OF_USERS; i++){
    		if (probeSetIndex[i] == null)
    			continue;
    		predictionsValue[i] = new float[probeSetRating[i].length];
    	}
    }
    
    public void readProbeSet(File probeSetFile){
    	probeSetIndex = new short[NUMBER_OF_USERS][];
    	probeSetRating = new float[NUMBER_OF_USERS][];
    	readUserProfiles(probeSetFile, probeSetIndex, probeSetRating);
    	predictionsIndex = probeSetIndex;
    	predictionsValue = new float[NUMBER_OF_USERS][];
    	for (int i=0; i<NUMBER_OF_USERS; i++){
    		if (probeSetIndex[i] == null)
    			continue;
    		predictionsValue[i] = new float[probeSetRating[i].length];
    	}
    }
    
    public void readResiduals(File resFile){
    	residualsIndex = new short[NUMBER_OF_USERS][];
    	residualsValue = new float[NUMBER_OF_USERS][];
    	readUserProfiles(resFile, residualsIndex, residualsValue);
    }
    
    public void readPredictions(File predFile){
    	predictionsIndex = new short[NUMBER_OF_USERS][];
    	predictionsValue = new float[NUMBER_OF_USERS][];
    	readUserProfiles(predFile, predictionsIndex, predictionsValue);
    }
    
    private void addResiduals(){
    	for (int i=0; i<probeSetIndex.length; i++){
    		if (probeSetIndex[i] == null)
    			continue;
    		for (int k=0; k<probeSetIndex[i].length; k++){
    			float prediction = predictionsValue[i][k] + residualsValue[i][k];
    			if (prediction > 5.0f)
    				predictionsValue[i][k] = 5.0f;
    			else if (prediction < 1.0f)
    				predictionsValue[i][k] = 1.0f;
    			else
    				predictionsValue[i][k] = prediction;
    		}
    	}
    }
    public void readAttributeNames(String attributesFilename){
    	attributeName = new String[NUMBER_OF_MOVIES];
    	String lineStr;
    	File aFile = new File(attributesFilename);
    	if (aFile == null) {
    		System.err.println("File " + attributesFilename + " should not be null.");
			System.exit(-1);
    	}
    	if (!aFile.exists()) {
    		System.err.println("File " + attributesFilename + " should not be null.");
			System.exit(-1);
    	}
    	if (!aFile.isFile()) {
    		System.err.println("File " + attributesFilename + " should not be null.");
			System.exit(-1);
    	}
    	if (!aFile.canRead()) {
    		System.err.println("File " + attributesFilename + " should not be null.");
			System.exit(-1);
    	}
	
    	
    	int i=0;
    	try { 
    		FileReader fr     = new FileReader(aFile);
        	BufferedReader br = new BufferedReader(fr);
    		while ((i < NUMBER_OF_MOVIES) && ((lineStr = br.readLine()) != null)){
    			String[] st = lineStr.split("\\t");
    			int indx = decrementByOne ? Integer.parseInt(st[0])-1 : Integer.parseInt(st[0]);
    			
    			attributeName[indx] = new String(st[1]);
    			i ++; 
    		} 
    		br.close();
    	} 
    	catch (IOException e) { 
    		System.err.println("Error while reading filename " + attributesFilename);
    		e.printStackTrace();
    	}	
    }
    
    
    
    
    /* Returns the raw prediction (no residuals added) of the userId and movie according 
     * to the current model. If the 
     * (userId, movie) pair is not in the training data then there is prediction else
     * it can be used to assess the error from the real data
     */
    public abstract double predictRaw(int userId, short movie);
    
    public abstract void writeModels(File wModels);
    
    public abstract void readModels(File rModels);
    
    /*
     * Returns the full prediction (raw + residuals) 
     */
    public double predict(int userId, short movie){
    	double rawPrediction = predictRaw(userId, movie);
    	
    	int k;
    	for (k=0; k<residualsIndex[userId].length; k++)
    		if (residualsIndex[userId][k] == movie)
    			break;
    	
    	double residual = residualsValue[userId][k];
    	// Clip to 1-5 range
		double prediction = rawPrediction + residual; 
		if (prediction > 5.0) 
			prediction = 5.0;
		else if (prediction < 1.0)
			prediction = 1.0;
		
		return prediction;
    }
    
    public void writeTrainingError(File resFile) {
    	try { 
    		FileOutputStream file_output = new FileOutputStream (resFile);
		    DataOutputStream data_out = new DataOutputStream (file_output);
		
		    int shortSize = Short.SIZE/8;
		    byte[] lenBuffer = new byte[shortSize * NUMBER_OF_USERS];
		    int offset=0;
		    for (int i=0; i<NUMBER_OF_USERS; i++){
		    	short len = (short)dataIndex[i].length;
		    	lenBuffer[offset] = (byte)(len >>> 8);
		    	lenBuffer[offset + 1] = (byte)(len);
		    	
		    	offset += shortSize;
		    }
		    data_out.write(lenBuffer);
		    
		    int floatSize = Float.SIZE/8;
		    for (int i=0; i<NUMBER_OF_USERS; i++){
		    	data_out.writeUTF(reverseUserNames[i]);
		    	
		    	byte[] indxBuffer = new byte[shortSize * dataIndex[i].length];
		    	offset=0;
		    	for (int j=0; j<dataValue[i].length; j++){
		    		short movie = dataIndex[i][j];
		    		indxBuffer[offset] = (byte)(movie >>> 8);
		    		indxBuffer[offset + 1] = (byte)movie;
	    		
			    	offset += shortSize;
		    	}
		    	data_out.write(indxBuffer);
		    	
		    	
		    	byte[] valueBuffer = new byte[floatSize * dataValue[i].length];
		    	offset=0;
		    	for (int j=0; j<dataValue[i].length; j++){
		    		short movie = dataIndex[i][j];
		    		double residual = dataValue[i][j] - predictRaw(i,movie);
		    		int intBits = Float.floatToRawIntBits((float)residual);
		    		valueBuffer[offset] = (byte)(intBits >>> 24);
		    		valueBuffer[offset + 1] = (byte)(intBits >>> 16);
		    		valueBuffer[offset + 2] = (byte)(intBits >>> 8);
		    		valueBuffer[offset + 3] = (byte)intBits;
			    	offset += floatSize;
		    	}
		    	data_out.write(valueBuffer);
		    }
		    data_out.close();
    	}
    	catch (IOException e) { 
			e.printStackTrace();
			System.err.println("Error while writing " + resFile.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
    }
    
    /*
     * Make predictions on the probe set and output the RMSE
     */
    public double predictProbeSet(){
    	for (int i=0; i<probeSetIndex.length; i++){
    		if (probeSetIndex[i] == null)
    			continue;
    		for (int k=0; k<probeSetIndex[i].length; k++){
    			short movie = probeSetIndex[i][k];
    			predictionsValue[i][k] = (float)predictRaw(i, movie);
    		}
    	}
    	
    	// Now add back the offset that has been removed initially
    	addResiduals();
    	
    	return(calculateRMSE());
    }
    
    protected double calculateRMSE(){
    	double rmse = 0.0;
    	double n=0.0;
    	for (int i=0; i<probeSetIndex.length; i++){
    		if (probeSetIndex[i] == null)
    			continue;
    		for (int k=0; k<probeSetIndex[i].length; k++){
    			short movie = probeSetIndex[i][k];
    			if (predictionsIndex[i][k] != probeSetIndex[i][k]){
    				System.err.println("ERROR: Lack of agreement between the " + i +
    						" row " + k + " column of predictions and probe set");
    				System.exit(1);
    			}
    			double prediction = predictionsValue[i][k];
    			if (prediction < 1.0 || prediction > 5.0){
    				System.err.println("Invalid prediction for user " + reverseUserNames[i] + 
    						" movie " + movie);
    				System.exit(1);
    			}
    			double ref = probeSetRating[i][k];
    			if (ref != 1.0 && ref != 2.0 && ref != 3.0 && ref !=4.0 && ref !=5.0){
    				System.err.println("Invalid reference value: " + ref);
    				System.exit(1);
    			}
    			double diff = prediction - ref;
    			rmse += diff * diff;
    			n += 1.0;
    		}
    	}
    	return Math.sqrt(rmse/n);
    }

    public void writePredictions(File predFile){
    	try {
	    	Writer output = new BufferedWriter( new FileWriter(predFile) );
	    	for (int i=0; i<NUMBER_OF_USERS; i++){
	    		if (predictionsIndex[i] == null)
	    			continue;
	    		StringBuffer sb = new StringBuffer();
	    		sb.append(reverseUserNames[i]);
	    		for (int k=0; k<predictionsIndex[i].length; k++){
	    			short indx = decrementByOne ? (short)(predictionsIndex[i][k]+1) : predictionsIndex[i][k];
	    			sb.append(" "); sb.append(indx);
	    			sb.append(" "); sb.append(predictionsValue[i][k]);
	    		}
	    		sb.append("\n");
	    		
	    		output.write(sb.toString());
	    	}
	    	output.close();
		}
		catch (IOException e){
			System.err.println("Cannot write to " + predFile.getAbsolutePath());
			System.exit(1);
		}
    }

}
