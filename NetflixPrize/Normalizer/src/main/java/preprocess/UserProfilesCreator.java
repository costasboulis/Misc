package preprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Set;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;
import NetflixPrize.CommandLineParser;

/**
 * Creates user profiles from the raw, Netflix data. Each line has all the information for a
 * particular user : movie, date, rating
 * 
 * @author kboulis
 *
 */
public class UserProfilesCreator {
	private HashMap<String, HashSet<Short>> probeSet;
	private static final Pattern moviePattern = Pattern.compile("(.+):");
	private static final int NUMBER_OF_MOVIES = 17770; 
	private static final int NUMBER_OF_USERS = 480189; 
	private HashSet<String> users;
	private HashMap<String, ArrayList<Rating>> userProfiles;
	private HashMap<String, ArrayList<Rating>> probeUserProfiles;
	
	public UserProfilesCreator(){
		probeSet = null;
		users = new HashSet<String>();
		userProfiles = new HashMap<String, ArrayList<Rating>>();
		probeUserProfiles = new HashMap<String, ArrayList<Rating>>();
	}
	public void readUsers(File usersFile){
		if (usersFile == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!usersFile.exists()) {
			System.err.println("File " +  usersFile.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!usersFile.isFile()) {
	    	System.err.println("File " +  usersFile.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!usersFile.canRead()) {
			System.err.println("File " +  usersFile.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		try { 
			BufferedReader br = new BufferedReader(new FileReader(usersFile));
			String lineStr;
			while ((lineStr = br.readLine()) != null) {
				users.add(lineStr);
			}
		}
		catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + usersFile.getAbsolutePath() + " data\n");
        	System.exit(-1);
        } 
	}
	
	public void readProbeSet(File prSet){
		if (prSet == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!prSet.exists()) {
			System.err.println("File " +  prSet.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!prSet.isFile()) {
	    	System.err.println("File " +  prSet.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!prSet.canRead()) {
			System.err.println("File " +  prSet.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		probeSet = new HashMap<String, HashSet<Short>>();
		try { 
			BufferedReader br = new BufferedReader(new FileReader(prSet));
			String lineStr;
			short movie = 1;
			while ((lineStr = br.readLine()) != null) {
				Matcher movieMatcher = moviePattern.matcher(lineStr);
				if (movieMatcher.matches()){
					movie = Short.parseShort(movieMatcher.group(1));
				}
				else {
					HashSet<Short> hs = probeSet.get(lineStr);
					if (hs == null){
						hs = new HashSet<Short>();
						probeSet.put(lineStr, hs);
					}
					hs.add(movie);
				}
			}
		}
		catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + prSet.getAbsolutePath() + " data\n");
        	System.exit(-1);
        } 
	}
	
	public void readTrainingData(File moviesList){
		if (moviesList == null){
			System.err.println("Null file pointer");
			System.exit(1);
		}
		if (!moviesList.exists()) {
			System.err.println("File " +  moviesList.getAbsolutePath() + " does not exist");
    		System.exit(1);
	    }
	    if (!moviesList.isFile()) {
	    	System.err.println("File " +  moviesList.getAbsolutePath() + " is not a file");
	    	System.exit(1);
		}
		if (!moviesList.canRead()) {
			System.err.println("File " +  moviesList.getAbsolutePath() + " cannot be read");
			System.exit(1);
		}
		
	
		try { 
			String pathName = "C:/NetflixPrize/data/download/training_set/training_set/";
			BufferedReader br = new BufferedReader(new FileReader(moviesList));
			String moviesFilename;
			int nMovies=1;
			while ((moviesFilename = br.readLine()) != null) {
				File movieFile = new File(pathName + moviesFilename);
				if (nMovies % 300 == 0){
					long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
					System.out.println("Read ratings for " + nMovies + " movies, Memory: " + memory/(1024*1024));
					System.out.flush();
				}
				
				if (!movieFile.exists()) {
					System.err.println("File " +  movieFile.getAbsolutePath() + " does not exist");
		    		System.exit(1);
			    }
			    if (!movieFile.isFile()) {
			    	System.err.println("File " +  movieFile.getAbsolutePath() + " is not a file");
			    	System.exit(1);
				}
				if (!movieFile.canRead()) {
					System.err.println("File " +  movieFile.getAbsolutePath() + " cannot be read");
					System.exit(1);
				}
				try {
					BufferedReader mbr = new BufferedReader(new FileReader(movieFile));
					String lineStr= mbr.readLine();
					short movie = Short.parseShort(lineStr.substring(0,lineStr.length()-1));
					while ((lineStr = mbr.readLine()) != null) {
						String[] st = lineStr.split(",");
						String user = st[0];
						
						if (! users.contains(user)){
							continue;
						}
						
						byte rating = Byte.parseByte(st[1]);
						String[] date = st[2].split("-");
						int intYear = Integer.parseInt(date[0])-1998;
						byte year = (byte)intYear;
						byte month = Byte.parseByte(date[1]);
						byte day = Byte.parseByte(date[2]);
						Rating r = new Rating(movie, rating, year, month, day);
						
						HashSet<Short> probeRatings = probeSet.get(user);
						boolean probeRating = probeRatings == null ? false : probeRatings.contains(movie);
						if (probeRating){
							ArrayList<Rating> ur = probeUserProfiles.get(user);
							if (ur == null){
								ur = new ArrayList<Rating>();
								probeUserProfiles.put(user, ur);
							}
							ur.add(r);
						}
						else {
							ArrayList<Rating> ur = userProfiles.get(user);
							if (ur == null){
								ur = new ArrayList<Rating>();
								userProfiles.put(user, ur);
							}
							ur.add(r);
						}
						
					}
					mbr.close();
				}
				catch (IOException e) { 
		        	e.printStackTrace();
		        	System.err.println("Error while reading " + movieFile.getAbsolutePath() + " data\n");
		        	System.exit(-1);
		        } 
				nMovies ++;
			}
			br.close();
		}
		catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while reading " + moviesList.getAbsolutePath() + " data\n");
        	System.exit(-1);
        } 
	}
	
	private void writeUserProfiles(File writeProfiles, HashMap<String, ArrayList<Rating>> profiles){
		try {
			Writer output = new BufferedWriter( new FileWriter(writeProfiles) );
			for (Iterator<String> it=profiles.keySet().iterator(); it.hasNext();){
				String user = it.next();
				ArrayList<Rating> ratingsList = profiles.get(user);
				StringBuffer sb = new StringBuffer();
				sb.append(user);
				for (Rating r: ratingsList){
					sb.append(" "); sb.append(r.getMovie()); sb.append(" ");
					sb.append(r.getDate()); sb.append(" ");
					sb.append(r.getRating());
				}
				sb.append("\n");
				output.write(sb.toString());
			}
			output.close();
		}
		catch (IOException e) { 
        	e.printStackTrace();
        	System.err.println("Error while writing to " + writeProfiles.getAbsolutePath());
        	System.exit(-1);
        }
	}
	
	private void writeBinaryUserProfiles(File writeProfiles, HashMap<String, ArrayList<Rating>> profiles){
		try { 
    		FileOutputStream file_output = new FileOutputStream (writeProfiles);
		    DataOutputStream data_out = new DataOutputStream (file_output);
		    int shortSize = Short.SIZE/8;
		    int floatSize = Float.SIZE/8;
		    
		    byte[] lengthsBuffer = new byte[shortSize * profiles.size()];
		    int offset = 0;
		    Set<String> profilesKeys = profiles.keySet();
		    for (Iterator<String> it = profilesKeys.iterator(); it.hasNext();){
		    	String key = it.next();
		    	data_out.writeUTF(key);
		    	
		    	ArrayList<Rating> ratingsList = profiles.get(key);
		    	short length = (short)ratingsList.size();
		    	lengthsBuffer[offset] = (byte)(length >>> 8);
		    	lengthsBuffer[offset + 1] = (byte)length;
    		
		    	offset += shortSize;
		    }
		    data_out.write(lengthsBuffer);
		    
		    for (Iterator<String> it = profilesKeys.iterator(); it.hasNext();){
		    	String key = it.next();
		    	ArrayList<Rating> ratingsList = profiles.get(key);
		    	int length = ratingsList.size();
		    	byte[] dateBuffer = new byte[3 * length];
		    	offset = 0;
		    	for (Rating r : ratingsList){
		    		dateBuffer[offset] = r.getYear();
		    		dateBuffer[offset + 1] = r.getMonth();
		    		dateBuffer[offset + 2] = r.getDay();
		    		offset += 3;
		    	}
		    	data_out.write(dateBuffer);
		    	
		    	byte[] indxBuffer = new byte[shortSize * length];
		    	offset = 0;
		    	for (Rating r : ratingsList){
		    		short movie = r.getMovie();
		    		indxBuffer[offset] = (byte)(movie >>> 8);
		    		indxBuffer[offset + 1] = (byte)movie;
	    		
			    	offset += shortSize;
		    	}
		    	data_out.write(indxBuffer);
		    	
		    	
		    	byte[] valueBuffer = new byte[floatSize * length];
		    	offset=0;
		    	for (Rating r : ratingsList){
		    		float rating = r.getRating();
		    		int intBits = Float.floatToRawIntBits(rating);
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
			System.err.println("Error while writing " + writeProfiles.getAbsolutePath() + " data\n");
			System.exit(-1);
		}
	}
	
	public void writeTrainingProfiles(File writeProfiles, boolean binary){
		if (! binary)
			writeUserProfiles(writeProfiles, userProfiles);
		else
			writeBinaryUserProfiles(writeProfiles, userProfiles);
	}
	
	public void writeProbeProfiles(File writeProfiles, boolean binary){
		if (! binary)
			writeUserProfiles(writeProfiles, probeUserProfiles);
		else
			writeBinaryUserProfiles(writeProfiles, probeUserProfiles);
	}
	
	public static void main(String[] argv) {
		
		for (int u=1; u<=3; u++){
		UserProfilesCreator up = new UserProfilesCreator();
		
		String userFilename = "C:/NetflixPrize/data/users_list." + u;
		up.readUsers(new File(userFilename));
		
		String filename = "C:/NetflixPrize/data/download/probe.txt";
		up.readProbeSet(new File(filename));
		long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Read probe set, Memory: " + memory/(1024*1024));
		
		String movieList = "C:/NetflixPrize/data/movies_list";
		up.readTrainingData(new File(movieList));
		
		boolean writeBinary = true;
		up.writeTrainingProfiles(new File("C:/NetflixPrize/data/training_data.withDates." + u), writeBinary);
		
		up.writeProbeProfiles(new File("C:/NetflixPrize/data/probe.withDates." + u), writeBinary);
		}
	}
}
