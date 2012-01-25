package com.upstream.datamining.evaluation;

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
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Counts fraction of users that were sent a Bulk MT. Used for informational purposes
 * For each day D the number of users that have sent at least one MO in the previous days 1..D-1,
 * and the number of users that were sent a Bulk MT in day D are calculated
 * 
 * @author kboulis
 *
 */
public class StatisticsCalculator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private HashSet<Integer> activeUsers;
    private HashSet<Integer> usersWithBulkMTsInLastDay;
    public static String newline = System.getProperty("line.separator");
    
    public StatisticsCalculator(String mo, String mt, String toDateString) {
    	usersWithBulkMTsInLastDay = new HashSet<Integer>();
    	activeUsers = new HashSet<Integer>();
        Date toDate = null;
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            toDate = (Date)formatter.parse(toDateString);
        }
        catch (ParseException ex) {
            logger.error("Cannot parse date \"" + toDateString + "\"");
            System.exit(-1);
        }
        
        long d = toDate.getTime() - (24 * 3600 * 1000);
        Date dayBeforeToDate = new Date(d);
        
        loadMO(new File(mo), dayBeforeToDate);
        loadMT(new File(mt), toDate);
    }
    
    private void loadMO(File moFile, Date toDate) {
        try {
            logger.info("Reading MO logs");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(moFile)));
            String lineStr;
            lineStr = br.readLine();
            int numLines = 1;
            int numSkippedLines = 0;
            Date prevDate = null;
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
                
                if (date.after(toDate)) {
                	break;
                }
                if (st[3].equals("\"DEFAULT\"")) {
//              	logger.info("Skipping MO " + lineStr);
                	numLines ++;
                	numSkippedLines ++;
                	continue;
                }
                
                if (prevDate != null && prevDate.after(date)) {
                	logger.error("MO file " + moFile.getAbsolutePath() + " is not sorted");
                	System.exit(-1);
                }
                prevDate = date;
                
                int userId = Integer.parseInt(st[4]);
                
                if (activeUsers.contains(userId)) {
                	continue;
                }
                else {
                	activeUsers.add(userId);
                }
                
                numLines ++;
            }
            br.close();
        }
        catch (FileNotFoundException ex) {
            logger.error("Could not find file " + moFile.getAbsolutePath());
        }
        catch (IOException ex) {
            logger.error("Could not read file " + moFile.getAbsolutePath());
        }
    }
    
    /*
     * Loads the list of raw MT messages. Assumes a chronologically ordered MT file
     */
    private void loadMT(File mtFile, Date toDate) {
        try {
            logger.info("Reading MT logs");
            long d = toDate.getTime() - (24 * 3600 * 1000);
            Date dayBeforeToDate = new Date(d);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mtFile)));
            String lineStr;
            lineStr = br.readLine();
            int numLines = 1;
            int numSkippedMT = 0;
            Date prevDate = null;
            while ((lineStr = br.readLine()) != null) {
                
                String[] st = lineStr.split(",");
                if (st.length != 7) {
                    logger.error("Could not parse line " + lineStr);
                    numLines ++;
                    continue;
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
                    numLines ++;
                    numSkippedMT ++;
                    continue;
                }

                if (date.before(dayBeforeToDate)) {
                	continue;
                }
                if (date.after(toDate)) {
                	break;
                }
                
                if (prevDate != null && prevDate.after(date)) {
                	logger.error("MT file " + mtFile.getAbsolutePath() + " is not sorted");
                	System.exit(-1);
                }
                prevDate = date;
                
                
                int userId = Integer.parseInt(st[3]);
                
                if (usersWithBulkMTsInLastDay.contains(userId)){
                	continue;
                }
                else {
                	usersWithBulkMTsInLastDay.add(userId);
                }
                
                numLines ++;
            }
            br.close();
        }
        catch (FileNotFoundException ex) {
            logger.error("Could not find file " + mtFile.getAbsolutePath());
        }
        catch (IOException ex) {
            logger.error("Could not read file " + mtFile.getAbsolutePath());
        }
    }
    
    public int getNumberOfActiveUsersThatWereAlsoSentBulkMT(){
    	int numActiveUsersAndSentBulkMT = 0;
    	for (Integer userId: usersWithBulkMTsInLastDay) {
    		if (activeUsers.contains(userId)) {
    			numActiveUsersAndSentBulkMT ++;
    		}
    	}
    	return numActiveUsersAndSentBulkMT;
    }
    
    public int getNumberOfActiveUsers() {
    	return activeUsers.size();
    }
    
    public static void main(String[] argv) {
    	File file = new File("c:\\Upstream\\FractionOfUsersSentMTPerDay.txt");
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            String mo = "c:\\Upstream\\motlog_sorted.txt";
            String mt = "c:\\Upstream\\mttlog_sorted.txt";
            for (int m = 10; m <= 12; m ++) {
            	for (int d = 1; d <= 31; d ++) {
            		if (d <= 3 && m == 10) {
            			continue;
            		}
            		if (d == 31 && m == 11) {
            			continue;
            		}
            		String tmp = Integer.toString(d);
            		String prefix = d < 10 ? "0" + tmp : tmp;
            		String toDateString = prefix + "/" + Integer.toString(m) + "/2009 23:59:59";
            		StatisticsCalculator baseline = new StatisticsCalculator(mo, mt, toDateString);
                
            		out.println("Date: " + toDateString);
            		out.println("========");
            		int numberOfUsersWithBulkMT = baseline.getNumberOfActiveUsersThatWereAlsoSentBulkMT();
            		int numberOfActiveUsers = baseline.getNumberOfActiveUsers();
            		out.println("Number of Users That Have Sent At least One MO until the Previous Day And " +
            				"Received At Least One Bulk MT on This Day: " + numberOfUsersWithBulkMT);
            		out.println("Number of Users That Have Sent At Least One MO until the Previous Day: " 
            				+ numberOfActiveUsers);
            		float f = (float)numberOfUsersWithBulkMT / (float)numberOfActiveUsers;
            		out.println("Fraction: " + f + newline + newline);
        			out.flush();
            		
            	}
            }
        }
        catch (Exception e){
        	System.err.println("I/O error");
            System.exit(-1);
        }
    }
}
