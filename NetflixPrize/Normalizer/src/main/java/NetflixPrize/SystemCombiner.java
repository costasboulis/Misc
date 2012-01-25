package NetflixPrize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.util.LinkedList;

public class SystemCombiner {
	private HashMap<String, Integer> indx; /* user+movie -> consecutive integer */
	private ArrayList<HashMap<String, Integer>> movieIndx; /* for each movie : user -> consecutive integer */
	private final int NUMBEROF_MOVIES=17770;
	private double[][] A;
	private double[] b;
	private double[] w;
	private LinkedList<File> systemPredictionsList;
	private int numberOfSystems;
	
	public SystemCombiner(){
		indx = new HashMap<String, Integer>();
	}
	public void train(File metaFile, File probeSet){
		if (metaFile == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!metaFile.exists()) {
			System.err.println("File " +  metaFile.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!metaFile.isFile()) {
	    	System.err.println("File " +  metaFile.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!metaFile.canRead()) {
			System.err.println("File " +  metaFile.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(metaFile));
			String lineStr;
			systemPredictionsList = new LinkedList<File>();
			while ((lineStr = br.readLine()) != null) {
				systemPredictionsList.add(new File(lineStr));
			}
			br.close();
			
			numberOfSystems = systemPredictionsList.size();
			System.out.println("Combining " + numberOfSystems + " systems");
			int ns=0;
			for (File s : systemPredictionsList){
				for (ProfileEntry pe : readSystemPredictions(s)){
					A[pe.getIndex()][ns] = pe.getValue();
				}
				ns ++;
			}
			
			for (ProfileEntry pe : readSystemPredictions(probeSet)){
				b[pe.getIndex()] = pe.getValue();
			}
		}
		catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + metaFile + " data\n");
        	System.exit(-1);
        } 	
		
		/* Now solve the linear system A*w=b */
		double[][] Barray = new double[b.length][];
		for (int i=0; i<b.length; i++){
			Barray[i] = new double[1];
			Barray[i][0] = b[i];
		}
		Matrix Amatrix = new Matrix(A);
		Matrix W = Amatrix.solve(new Matrix(Barray));
		for (int i=0; i<numberOfSystems; i++)
			w[i] = W.get(i, 0);
		int r=0;
		for (File s : systemPredictionsList){
			System.out.println("System : " + s.getAbsolutePath() + " -> " + w[r]);
			r ++;
		}
		/* Compute RMSE on the probe set */
		double rmse=0.0;
		for (int i=0; i<A.length; i++){
			double pred=0.0;
			for (int j=0; j<numberOfSystems; j++)
				pred += A[i][j] * w[j] ;
				
			double diff = pred - b[i];
			rmse += diff * diff;
		}
		rmse /=(double)A.length;
		rmse = Math.sqrt(rmse);
		System.out.println("Probe RMSE : " + rmse);
	}
	
	/* 
	 * After the combination weights have been estimated on the probe set, apply
	 * combination on the quiz set
	 */
	public void applySystemCombination(File metaQuizFile, File combinedPredictions){
		if (metaQuizFile == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!metaQuizFile.exists()) {
			System.err.println("File " +  metaQuizFile.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!metaQuizFile.isFile()) {
	    	System.err.println("File " +  metaQuizFile.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!metaQuizFile.canRead()) {
			System.err.println("File " +  metaQuizFile.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(metaQuizFile));
			String lineStr;
			/* The order of the files in the quiz file must be the same as in the metaProbeFile */
			systemPredictionsList = new LinkedList<File>();
			while ((lineStr = br.readLine()) != null) {
				systemPredictionsList.add(new File(lineStr));
			}
			br.close();
			
			int ns=0;
			for (File s : systemPredictionsList){
				for (ProfileEntry pe : readSystemPredictions(s)){
					A[pe.getIndex()][ns] = pe.getValue();
				}
				ns ++;
			}
			
			/* The combinbation is A*w */
		}
		catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + metaQuizFile + " data\n");
        	System.exit(-1);
        } 	
	}
	private LinkedList<ProfileEntry> readSystemPredictions(File predictions){
		if (predictions == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!predictions.exists()) {
			System.err.println("File " +  predictions.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!predictions.isFile()) {
	    	System.err.println("File " +  predictions.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!predictions.canRead()) {
			System.err.println("File " +  predictions.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
    	
		System.out.println("Reading ASCII text file " + predictions.getAbsolutePath());
		System.out.flush();
    	String lineStr;
        try { 
        	BufferedReader br = new BufferedReader(new FileReader(predictions));
        	LinkedList<ProfileEntry> l = new LinkedList<ProfileEntry>();
        	while ((lineStr = br.readLine()) != null) {
        		String[] st = lineStr.split("\\s");
        		
        		String user = st[0];
        		for (int i=1; i<st.length-1; i=i+2){
        			String movie = st[i];
        			float pred = Float.parseFloat(st[i+1]);
        			
        			String s = user + "_" + movie;
        			Integer g = indx.get(s);
        			int r=0;
        			if (g == null){
        				int len = indx.size();
        				indx.put(s, new Integer(len));
        				r = len;
        			}
        			else
        				r = g.intValue();
        			
        			l.add(new ProfileEntry(r, pred));
        		}
       
        	}
        	br.close();
        	
        	if (A == null){
        		A = new double[l.size()][];
        		for (int i=0; i<l.size(); i++)
        			A[i] = new double[numberOfSystems];
        		
        		w = new double[numberOfSystems];
        		b = new double[l.size()];
        	}
        	
        	return l;
        }
        catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + predictions + " data\n");
        	System.exit(-1);
        } 	
 
        return null;
	}
	
	public static void main(String[] argv) {
    	CommandLineParser parser = new CommandLineParser(argv);
    	if (parser.hasErrors(new String[]{}, new String[]{
    							 "probeSystems",
                                 "quizSystems",
                                 "probeSet",
                                 "writeCombinedPrediction"})) {
        		 System.err.println("Usage: SystemCombiner\n" +
                                 "-probeSystemsn" +
                                 "-quizSystems\n" +
                                 "-writeCombinedPrediction\n");
        		 System.exit(1);
        	 }
        	 HashMap<String, Object> arguments;
        	 arguments = parser.getArguments();
        	 String probeSystems = CommandLineParser.getStringParameter(arguments, "probeSystems", true);
        	 String probeSet = CommandLineParser.getStringParameter(arguments, "probeSet", true);
        	 String quizSystems = CommandLineParser.getStringParameter(arguments, "quizSystems", false);
        	 String writeCombinedPrediction = CommandLineParser.getStringParameter(arguments, "writeCombinedPrediction", false);
        	 
        	 SystemCombiner sc = new SystemCombiner();
        	 sc.train(new File(probeSystems), new File(probeSet));
	}       	 
}
