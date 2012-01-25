package com.upstream.datamining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Class used to sort in chronological order every MO and MT message
 * 
 * Writes temporary files to the same directory where MT and MO files are located. The size of the temporary files
 * is the same as the original ones. Make sure that a) write-permission is enabled in the current directory 
 * and b) there is enough free space on your hard drive
 * 
 * @author k.boulis
 *
 */
public class Preprocessor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private File moFile;
    private File mtFile;
    private List<MobileOriginatedMessage> sortedMOMessages;
    private List<MobileTerminatedMessage> sortedMTMessages;
    public final static String MO_HEADER = "REQUEST_CTX_ID,REQUEST_CNT,REQUEST_DATE,REQUEST_TYPE,USER_ID,QUESTION_ID,OUTCOME,POINTS";
    public final static String MT_HEADER = "RESPONSE_DATE,REQUEST_CTX_ID,BULK_ID,USER_ID,RESPONSE_TYPE,QUESTION_ID,POTENTIAL_POINTS";
    	
    	
    private void init(File moLog, File mtLog) throws IOException {
        if (!moLog.exists()) {
            logger.error("Cannot find file " + moLog.getAbsolutePath());
            throw new IOException();
        }
        if (!moLog.canRead()) {
            logger.error("Cannot read file " + moLog.getAbsolutePath());
            throw new IOException();
        }
        if (moLog.isDirectory()) {
            logger.error("Expecting file but encountered directory " + moLog.getAbsolutePath());
            throw new IOException();
        }
        if (!mtLog.exists()) {
            logger.error("Cannot find file " + mtLog.getAbsolutePath());
            throw new IOException();
        }
        if (!mtLog.canRead()) {
            logger.error("Cannot read file " + mtLog.getAbsolutePath());
            throw new IOException();
        }
        if (mtLog.isDirectory()) {
            logger.error("Expecting file but encountered directory " + mtLog.getAbsolutePath());
            throw new IOException();
        }
        moFile = moLog;
        mtFile = mtLog;
    }
    
    public Preprocessor(File moLog, File mtLog) throws IOException {
        init(moLog, mtLog);
    }
    
    /**
     * Read from the single unsorted MO file from line beginLine to line endLine and sort the segment chronologically
     * @param beginLine Begin line of single unsorted MO file
     * @param endLine End line of single unsorted MO file
     */
	public void readMO() {
		sortedMOMessages = new LinkedList<MobileOriginatedMessage>();
        try {
            logger.info("Reading MO logs");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.moFile)));
            String lineStr;
            lineStr = br.readLine();
            int numLines = 1;
            int numSkippedLines = 0;
            while ((lineStr = br.readLine()) != null) {
                if (numLines % 100000 == 0) {
                    logger.info("Read " + numLines + " lines of MO logs, skipped " + numSkippedLines);
                }
                String[] st = lineStr.split(",");
                if (st.length != 8) {
                    logger.error("Could not parse line " + lineStr);
                    numLines ++;
                    numSkippedLines ++;
                	continue;
                }
                
                String dateString = st[2];
                dateString = dateString.substring(1);
                dateString = dateString.substring(0, dateString.length()-1);
                Date date = null;
                try {
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    date = (Date)formatter.parse(dateString);
                }
                catch (ParseException ex) {
                	logger.error("Cannot parse date " + dateString);
                	numLines ++;
                	numSkippedLines ++;
                	continue;
                }
                
                
                MobileOriginatedMessage mo = new MobileOriginatedMessage(date, null, 0, lineStr);
                sortedMOMessages.add(mo);
                
                numLines ++;
            }
            br.close();
            logger.info("Finished reading MO log...started sorting");
            Collections.sort(sortedMOMessages);
            logger.info("Finished sorting MO logs");
        }
        catch (FileNotFoundException ex) {
            logger.error("Could not find file " + moFile.getAbsolutePath());
        }
        catch (IOException ex) {
            logger.error("Could not read file " + moFile.getAbsolutePath());
        }
    }
	
	private MobileTerminatedMessage extractMT(String lineStr, int indx) {
		String[] st = lineStr.split(",");
        if (st.length != 7) {
            logger.error("Could not parse line " + lineStr);
            return null;
        }
        String dateString = st[0];
        dateString = dateString.substring(1);
        dateString = dateString.substring(0, dateString.length()-1);
        Date date = null;
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            date = (Date)formatter.parse(dateString);
        }
        catch (ParseException ex) {
            logger.error("Cannot parse date " + dateString);
            return null;
        }
        
        MobileTerminatedMessage mt = new MobileTerminatedMessage(date, "", indx, lineStr);	
        
        return mt;
	}
	
	/*
	 * Read from the single unsorted MT file from line beginLine to line endLine and sort the segment chronologically
	 * 
	 */
	private int readMT(int beginLine, int endLine) {
		int n = 0;
		sortedMTMessages = new LinkedList<MobileTerminatedMessage>();
        try {
            logger.info("Reading MT logs");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.mtFile)));
            String lineStr;
            lineStr = br.readLine();
            int numLines = 1;
            int numSkippedMT = 0;
            while ((lineStr = br.readLine()) != null) {
            	if (numLines <= beginLine) {
            		numLines ++;
            		continue;
            	}
  //              if (numLines % 100000 == 0) {
  //                  logger.info("MT logs lines read: " + numLines + ", skipped: " + numSkippedMT);
  //              }
                
                MobileTerminatedMessage mt = extractMT(lineStr, 0);
                if (mt != null) {
                	sortedMTMessages.add(mt);
                }
                
                if (numLines == endLine) {
                	break;
                }
                numLines ++;
            }
            br.close();
            logger.info("Finished reading MT logs...started sorting");
            Collections.sort(sortedMTMessages);
            n = sortedMTMessages.size();
            logger.info("Finished sorting MT logs between lines " + beginLine + " and " + endLine);
        }
        catch (FileNotFoundException ex) {
            logger.error("Could not find file " + mtFile.getAbsolutePath());
            System.exit(-1);
        }
        catch (IOException ex) {
            logger.error("Could not read file " + mtFile.getAbsolutePath());
            System.exit(-1);
        }
        return n;
    }
    
	public void writeMO(File newMOFile) {
		try {
			logger.info("Started writing sorted MO logs");
    		PrintWriter out = new PrintWriter(new FileWriter(newMOFile));
    		out.println(MO_HEADER);
    		for (MobileOriginatedMessage m : sortedMOMessages) {
    			out.println(m.getComments());
    		}
    		out.close();
    		logger.info("Finished writing sorted MO logs");
    		sortedMOMessages = null;
		}
		catch (Exception e){
    		logger.error("Could not write to file " + newMOFile.getAbsolutePath());
    	}
	}
	
	public void writeMT(File newMTFile) {
		try {
			logger.info("Started writing sorted MT logs " + newMTFile.getAbsolutePath());
    		PrintWriter out = new PrintWriter(new FileWriter(newMTFile));
    		out.println(MT_HEADER);
    		for (MobileTerminatedMessage m : sortedMTMessages) {
    			out.println(m.getComments());
    		}
    		out.close();
    		logger.info("Finished writing sorted MT logs " + newMTFile.getAbsolutePath());
    		sortedMTMessages = null;
		}
		catch (Exception e){
    		logger.error("Could not write to file " + newMTFile.getAbsolutePath());
    	}
	}
	
	/**
	 * Receives as input a list of chronologically sorted MT files and merges them into one sorted file
	 * 
	 * @param files List of files to be merged. Each file is chronologically sorted
	 * @param mergedFile Output of the merging process
	 */
	public void mergeMT(File[] files, File mergedFile) {
		BufferedReader[] br = new BufferedReader[files.length];
		for (int i = 0; i < br.length; i ++) {
			br[i] = null;
			try {
				br[i] = new BufferedReader(new InputStreamReader(new FileInputStream(files[i])));
			}
			catch (IOException ex) {
				logger.error("Cannot read from " + files[i].getAbsolutePath());
				System.exit(-1);
			}
		}
		
		PrintWriter out = null;
		try {
			logger.info("Started writing sorted MT logs");
			out = new PrintWriter(new FileWriter(mergedFile));	
			out.println(MT_HEADER);	
		}
		catch (Exception e){
    		logger.error("Could not write to file " + mergedFile.getAbsolutePath());
    	}
		
		try {
			String lineStr = null;
			for (int i = 0; i < files.length; i ++) {
				lineStr = br[i].readLine();
			}
			
			LinkedList<MobileTerminatedMessage> list = new LinkedList<MobileTerminatedMessage>();
			for (int i = 0; i < files.length; i ++) {
				lineStr = br[i].readLine();
				MobileTerminatedMessage mt = extractMT(lineStr, i);
				list.add(mt);
			}
			Collections.sort(list);
			int numLines = 1;
			while (true) {
				if (numLines % 100000 == 0) {
					logger.info("MT logs lines merged: " + numLines);
				}


				MobileTerminatedMessage mtOut = list.get(0);
				int indx = mtOut.getPotentialPoints();
				out.println(mtOut.getComments());
				list.remove(0);
				
				
				lineStr = br[indx].readLine();
				if (lineStr != null) {
					MobileTerminatedMessage mt = extractMT(lineStr, indx);
					list.add(mt);
					Collections.sort(list);
				}
				else {
					if (list.size() == 0) {
						break;
					}
				}
				
				numLines ++;
			}
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		
		try {
			for (int i = 0; i < br.length; i ++) {
				br[i].close();
			}
			out.close();
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		sortedMTMessages = new LinkedList<MobileTerminatedMessage>();
	}
	
	public void sortMTandMO() {
		// MT file is too big to fit in memory, break in chunks, sort each chunk and then merge all chunks in one
		int offset  = 2000000;
		int n = 0;
		int cnt = 1;
		while (true) {
			int numLines = readMT(n, n + offset);
			writeMT(new File(mtFile.getAbsolutePath() + "_" + cnt));
			if (numLines != offset) {
				break;
			}
			cnt ++;
			n += offset;
		}
		File[] mtFiles = new File[cnt];
		for (int i = 0; i < cnt; i++){
			int j = i + 1;
			mtFiles[i] = new File(mtFile.getAbsolutePath() + "_" + j);
		}
		mergeMT(mtFiles, new File(mtFile.getAbsolutePath() + "_sorted"));
		
		for (int i = 0; i < cnt; i++){
			mtFiles[i].delete();
		}
		
		// MO file is usually much smaller than MT so it can be loaded all in memory
		readMO();
		writeMO(new File(moFile.getAbsolutePath() + "_sorted"));
	}
	
	public static void main(String[] argv) {
		String mo = argv[0];
        String mt = argv[1];
        System.out.println("MO file entered: " + mo);
        System.out.println("MT file entered: " + mt);
        try {
        	Preprocessor pr = new Preprocessor(new File(mo), new File(mt));
        	pr.sortMTandMO();
        }
        catch (IOException ex) {
        	System.exit(-1);
        }
		
	}
}
