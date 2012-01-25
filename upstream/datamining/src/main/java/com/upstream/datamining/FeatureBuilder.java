package com.upstream.datamining;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upstream.datamining.features.*;

/**
 * Builds the user profile with features from the list of MOs and MTs and writes an ARFF file to be processed by Weka 
 * @author kboulis
 *
 */
public class FeatureBuilder {
    public HashMap<Integer, List<Message>> profiles;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private File moFile;
    private File mtFile;
    private Date toDate;
    public static final int PROFILE_SPAN = 210;
    
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
        profiles = new HashMap<Integer, List<Message>>();
    }
    
    public FeatureBuilder(File moLog, File mtLog, Date td) throws IOException {
        init(moLog, mtLog);
        toDate = td;
    }
    
    /*
     * Loads the raw MO messages in memory. Assumes a chronologically sorted MO file
     */
    private void loadMO() {
        try {
            logger.info("Reading MO logs");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.moFile)));
            String lineStr;
            lineStr = br.readLine();
            int numLines = 1;
            int numSkippedLines = 0;
            Date prevDate = null;
            while ((lineStr = br.readLine()) != null) {
   //             if (numLines % 1000000 == 0) {
   //                 logger.info("Read " + numLines + " lines of MO logs, skipped " + numSkippedLines);
   //             }
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
                /*
                if (fromDate != null && toDate != null) {
                    if (date.before(fromDate) || date.after(toDate)) {
                        numLines ++;
                        numSkippedLines ++;
                        continue;
                    }
                }
                */
                /*
                if (date.before(fromDate)) {
                    numLines ++;
                    numSkippedLines ++;
                    continue;
                }
                */
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
                	logger.error("MO file " + this.moFile.getAbsolutePath() + " is not sorted, offending line " + lineStr);
                	System.exit(-1);
                }
                prevDate = date;
                
                int userId = Integer.parseInt(st[4]);
                
                Question question = null;
                try {
                    int questionId = Integer.parseInt(st[5]);
                    String outcomeString = st[6];
                    question = new Question(questionId, outcomeString);
                }
                catch (NumberFormatException ex) {
                    question = null; // No question-related MO
                }
                
                int points = Integer.parseInt(st[7]);
                
                List<Message> messageList = profiles.get(userId);
                if (messageList == null) {
                    messageList = new LinkedList<Message>();
                    profiles.put(userId, messageList);
                }
                
             // Do not store messages prior to the profile span
              /*  
                long d2 = toDate.getTime() - ((PROFILE_SPAN + 2) * 24 * 3600 * 1000);
                Date beginProfileDate = new Date(d2);
                if (date.before(beginProfileDate) && messageList.size() > 0) {
                	numSkippedLines ++;
                	numLines ++;
                	continue;
                }
                */
                MobileOriginatedMessage mo = new MobileOriginatedMessage(date, question, points);
                messageList.add(mo);
                
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
    private void loadMT() {
        try {
            logger.info("Reading MT logs");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.mtFile)));
            String lineStr;
            lineStr = br.readLine();
            int numLines = 1;
            int numSkippedMT = 0;
            Date prevDate = null;
            while ((lineStr = br.readLine()) != null) {
  //              if (numLines % 1000000 == 0) {
  //                  logger.info("MT logs lines read: " + numLines + ", skipped: " + numSkippedMT + 
  //                          " Number of users: " + profiles.size());
  //              }
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
/*
                if (date.before(fromDate)) {
                    numLines ++;
                    numSkippedMT ++;
                    continue;
                }
                */
                if (date.after(toDate)) {
                	break;
                }
                
                if (prevDate != null && prevDate.after(date)) {
                	logger.error("MT file " + this.mtFile.getAbsolutePath() + " is not sorted, offending line " + lineStr);
                	System.exit(-1);
                }
                prevDate = date;
                
                int bulkId = -1;
                try {
                    bulkId = Integer.parseInt(st[2]);
                }
                catch (NumberFormatException ex) {
                    bulkId = -1; 
                }
                int userId = Integer.parseInt(st[3]);
                
                Question question = null;
                try {
                    int questionId = Integer.parseInt(st[5]);
                    question = new Question(questionId, "");
                }
                catch (NumberFormatException ex) {
                    question = null; // No question-related MT
                }
                
                int potentialPoints = Integer.parseInt(st[6]);
                
                List<Message> messageList = profiles.get(userId);
                if (messageList == null) {
                    messageList = new LinkedList<Message>();
                    profiles.put(userId, messageList);
                }
                
                // Do not store messages prior to the profile span
                /*
                long d2 = toDate.getTime() - ((PROFILE_SPAN + 2) * 24 * 3600 * 1000);
                Date beginProfileDate = new Date(d2);
                if (date.before(beginProfileDate) && messageList.size() > 0) {
                	logger.info("Skipping " + lineStr);
                	numSkippedMT ++;
                	numLines ++;
                	continue;
                }
                */
                MobileTerminatedMessage mt = new MobileTerminatedMessage(date, st[4], potentialPoints);
                if (!mt.isReplyMT() && !mt.isBulkQuestionMT()){
                    messageList.add(mt);
                }
                else {
                    numSkippedMT ++;
                }
                
                numLines ++;
            }
            logger.info(profiles.size() + " players");
            br.close();
        }
        catch (FileNotFoundException ex) {
            logger.error("Could not find file " + mtFile.getAbsolutePath());
        }
        catch (IOException ex) {
            logger.error("Could not read file " + mtFile.getAbsolutePath());
        }
    }
    
    
    protected boolean[] getMOSentDays(List<Message> messages, Date fr, Date to) {
        float f = (float)(to.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
        int numDays = (int)Math.floor(f);
        boolean[] isMOSentInEachDay = new boolean[numDays + 1];
        for (int i = 0 ; i< isMOSentInEachDay.length ; i ++) {
            isMOSentInEachDay[i] = false;
        }
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
            if (m instanceof MobileOriginatedMessage) {
                float df = (float)(date.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
                int d = (int)Math.floor(df);
                isMOSentInEachDay[d] = true;
            }
        }
        return isMOSentInEachDay;
    }
   
    /* The following code is valid only for campaigns with lives
    private int[] getNumCorrectDays(List<Message> messages, Date fr, Date to) {
        float f = (float)(to.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
        int numDays = (int)Math.floor(f);
        int[] numCorrectEachDay = new int[numDays + 1];
        int[] numWrongEachDay = new int[numDays + 1];
        for (int i = 0 ; i< numCorrectEachDay.length ; i ++) {
        	numCorrectEachDay[i] = 0;
        	numWrongEachDay[i] = 0;
        }
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
            if (m instanceof MobileOriginatedMessage) {
                float df = (float)(date.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
                int d = (int)Math.floor(df);
                MobileOriginatedMessage moMsg = (MobileOriginatedMessage)m;
                if (moMsg.getQuestion() != null) {
        			if (moMsg.getQuestion().getAnswer() == Question.Answer.CORRECT) {
        				numCorrectEachDay[d] ++;
            		}
            		else if (moMsg.getQuestion().getAnswer() == Question.Answer.WRONG) {
            			numWrongEachDay[d] ++;
            			if (numCorrectEachDay[d] >= 5 && numWrongEachDay[d] >= 4) {
                			numWrongEachDay[d] = 0;
                			numCorrectEachDay[d] = 0;
                		}
            			else if (numCorrectEachDay[d] < 5 && numWrongEachDay[d] >= 1) {
                			numWrongEachDay[d] = 0;
                			numCorrectEachDay[d] = 0;
                		}
            		}
        		}
        		else {
        			numCorrectEachDay[d] ++;
        		}
        		
            }
        }
        return numCorrectEachDay;
    }
    */
    
    /**
     * For each day since the start of the profile, get the difference of correct minus wrong answers
     * 
     */
    protected int[] getDiffNumCorrectMinusNumWrongDays(List<Message> messages, Date fr, Date to) {
        float f = (float)((to.getTime() - fr.getTime())) / (3600.0f * 1000.0f * 24.0f);
        int numDays = (int)Math.floor(f);
        int[] numCorrectEachDay = new int[numDays + 1];
        int[] numWrongEachDay = new int[numDays + 1];
        for (int i = 0 ; i< numCorrectEachDay.length ; i ++) {
        	numCorrectEachDay[i] = 0;
        	numWrongEachDay[i] = 0;
        }
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
            if (m instanceof MobileOriginatedMessage) {
                float df = (float)((date.getTime() - fr.getTime())) / (3600.0f * 1000.0f * 24.0f);
                int d = (int)Math.floor(df);
                MobileOriginatedMessage moMsg = (MobileOriginatedMessage)m;
                if (moMsg.getQuestion() != null) {
        			if (moMsg.getQuestion().getAnswer() == Question.Answer.CORRECT) {
        				numCorrectEachDay[d] ++;
            		}
        			else if (moMsg.getQuestion().getAnswer() == Question.Answer.WRONG) {
        				numCorrectEachDay[d] --;
        			}
        		}
        		else {
        			numCorrectEachDay[d] ++;
        		}
        		
            }
        }
        return numCorrectEachDay;
    }
    /**
     * Get maximum number of correct answers over all days
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public int getMaxDiffNumCorrectMinusNumWrongMO(List<Message> messages, Date fr, Date to) {
    	int[] numCorrectMOsPerDay = getDiffNumCorrectMinusNumWrongDays(messages, fr, to);
    	int maxNumCorrect = numCorrectMOsPerDay[0];
    	for (int i = 1; i<numCorrectMOsPerDay.length; i++) {
    		if (maxNumCorrect < numCorrectMOsPerDay[i]) {
    			maxNumCorrect = numCorrectMOsPerDay[i];
    		}
    	}
    	return maxNumCorrect < 10 ? maxNumCorrect : 10;
    }
    
    public int getNumDaysDiffNumCorrectMinusNumWrongMOAboveThreshold(List<Message> messages, Date fr, Date to, int thr) {
    	int[] numCorrectMOsPerDay = getDiffNumCorrectMinusNumWrongDays(messages, fr, to);
    	int numDays = 0;
    	for (int i = 0; i < numCorrectMOsPerDay.length; i ++) {
    		if (numCorrectMOsPerDay[i] >= thr) {
    			numDays ++;
    		}
    	}
    	return numDays;
    }
    
    /**
     * Get the binned number of correct answers in the last day
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public int getDiffNumCorrectMinusNumWrongBinned(List<Message> messages, Date fr, Date to, int lowThreshold, int highThreshold) {
    	int[] numCorrectMOsPerDay = getDiffNumCorrectMinusNumWrongDays(messages, fr, to);
    	int nCorr = numCorrectMOsPerDay[numCorrectMOsPerDay.length-1];
    	return  nCorr > highThreshold || nCorr <= lowThreshold ? 0 : 1;
    }
    
    /**
     * Get the day of the maximum number of correct MOs
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public int getDayOfMaxDiffNumCorrectMinusNumWrongMO(List<Message> messages, Date fr, Date to) {
    	int[] numCorrectMOsPerDay = getDiffNumCorrectMinusNumWrongDays(messages, fr, to);
    	int maxNumCorrect = numCorrectMOsPerDay[0];
    	int maxCorrectDay = 0;
    	for (int i = 1; i<numCorrectMOsPerDay.length; i++) {
    		if (maxNumCorrect < numCorrectMOsPerDay[i]) {
    			maxNumCorrect = numCorrectMOsPerDay[i];
    			
    			maxCorrectDay = i;
    		}
    	}
    	return maxCorrectDay ;
    }
    
   
    /**
     * Returns the maximum number of consecutive days that the player sent MO, from the beginning of the campaign
     * 
     * @param messages
     * @return
     */
    public int getMaxStreak(List<Message> messages, Date fr, Date to) {
        boolean[] isMOSentInEachDay = getMOSentDays(messages, fr, to);
        
        int maxStreak = 0;
        for (int i = 0; i < isMOSentInEachDay.length; i ++) {
            int streak = 0;
            if (!isMOSentInEachDay[i]) {
                continue;
            }
            for (int j = i; j < isMOSentInEachDay.length; j ++) {
                if (isMOSentInEachDay[j]) {
                    streak ++;
                    if (maxStreak < streak) {
                        maxStreak = streak;
                    }
                }
                else {
                	i = j;
                    break;
                }
            }
        }
        
        return maxStreak;
    }
    
    
    public int getStreakSinceYesterday(List<Message> messages, Date fr, Date to) {
        boolean[] isMOSentInEachDay = getMOSentDays(messages, fr, to);
        int streak = 0;
        for (int i = isMOSentInEachDay.length - 1; i >= 0; i --) {
            if (isMOSentInEachDay[i]) {
                streak ++;
            }
            else {
                break;
            }
        }
        
        return streak;
    }
    
    public int getNumberOfMO(List<Message> messages, Date fr, Date to) {
        int numMOSent = 0;
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
            if (m instanceof MobileOriginatedMessage) {
                numMOSent ++;
            }
        }
        return numMOSent;
    }
    
    public boolean getNumberOfMOBinned(List<Message> messages, Date fr, Date to, int lowThreshold, int highThreshold) {
        int numMOSent = 0;
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
            if (m instanceof MobileOriginatedMessage) {
                numMOSent ++;
            }
        }
        return numMOSent > highThreshold || numMOSent <= lowThreshold ? false : true;
    }
    
    /**
     * Get total number of Bulk MTs sent within the specified time period
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public int getNumberOfBulkMT(List<Message> messages, Date fr, Date to) {
        int numMTSent = 0;
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
            if (m instanceof MobileTerminatedMessage) {  	
            	MobileTerminatedMessage mtMsg = (MobileTerminatedMessage)m;
            	if (mtMsg.getType() == MobileTerminatedMessage.RequestType.Bulk) {
            		numMTSent ++;
            	}
            }
        }
        return numMTSent;
    }
    
    /**
     * Get number of days until last sent MO
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public float getDaysSinceLastMO(List<Message> messages, Date fr, Date to) {
    	float numDays = (float)(to.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);;
        for (Message m : messages) {
            if (m instanceof MobileOriginatedMessage) {
            	Date date = m.getDate();
            	if (date.before(fr) || date.after(to)) {
            		continue;
            	}
            	numDays = (float)(to.getTime() - date.getTime()) / (3600.0f * 1000.0f * 24.0f);
            }
        }
        return numDays;
    }
    
    /**
     * Get number of days until last sent Bulk MT
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public float getDaysSinceLastBulkMT(List<Message> messages, Date fr, Date to) {
    	float numDays = (float)(to.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
            if (m instanceof MobileTerminatedMessage) {
            	MobileTerminatedMessage mtMsg = (MobileTerminatedMessage)m;
            	if (mtMsg.getType() == MobileTerminatedMessage.RequestType.Bulk) {
            		numDays = (float)(to.getTime() - date.getTime()) / (3600.0f * 1000.0f * 24.0f);
            	}
            	
            }
        }
        return numDays;
    }
    
    /**
     * Get day of first MO
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public float getDayOfFirstMO(List<Message> messages, Date fr, Date to) {
    	float firstDayMO = 0.0f;
        for (Message m : messages) {
        	Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
            if (m instanceof MobileOriginatedMessage) {
            	firstDayMO = (float)(date.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
            	
            	break;
            }
        }
        return firstDayMO;
    }
    
    /**
     * Returns the number of times the player has continued answering correct questions after reaching 0 points
     * Valid only for campaigns with lives
     * @return
     */
    public int getNumReactivations(List<Message> messages, Date fr, Date to) {
    	int numReactivations = 0;
    	
    	float df = (float)(to.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
    	int numDays = (int)Math.floor(df);
    	int[] numWrongs = new int[numDays + 1];
    	int[] numCorrect = new int[numDays + 1];
    	boolean[] backToSquareOne = new boolean[numDays + 1];
    	for (int i = 0; i < numWrongs.length; i ++) {
    		numWrongs[i] = 0;
    		numCorrect[i] = 0;
    		backToSquareOne[i] = false;
    	}
    	for (Message m : messages) {
    		Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileOriginatedMessage) {
        		MobileOriginatedMessage moMsg = (MobileOriginatedMessage)m;
        		df = (float)(date.getTime() - fr.getTime()) / (3600.0f * 1000.0f * 24.0f);
        		int d = (int)Math.floor(df);
//        		int points = moMsg.getPoints();
        		
        		if (backToSquareOne[d]) {
        			numReactivations ++;
        			backToSquareOne[d] = false;
        			numWrongs[d] = 0;
        			numCorrect[d] = 0;
        		}
        		
        		if (moMsg.getQuestion() != null) {
        			if (moMsg.getQuestion().getAnswer() == Question.Answer.CORRECT) {
            			numCorrect[d] ++;
            		}
            		else if (moMsg.getQuestion().getAnswer() == Question.Answer.WRONG) {
            			numWrongs[d] ++;
            			if (numCorrect[d] >= 5 && numWrongs[d] >= 4) {
                			backToSquareOne[d] = true;
                		}
            			if (numCorrect[d] > 0 && numCorrect[d] < 5 && numWrongs[d] >= 1) {
                			backToSquareOne[d] = true;
                		}
            		}
        		}
        		else {
        			numCorrect[d] ++;
        		}
        		
        	}
    	}
    	return numReactivations > 10 ? 10 : numReactivations;
    }
    
   
    public boolean isLastMOWrongOrInvalid(List<Message> messages, Date fr, Date to) {
    	boolean isWrongMO = false;
    	for (Message m : messages) {
    		Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileOriginatedMessage) {
        		MobileOriginatedMessage moMsg = (MobileOriginatedMessage)m;
        		
        		if (moMsg.getQuestion() != null) {
        			if (moMsg.getQuestion().getAnswer() == Question.Answer.CORRECT) {
        				isWrongMO = false;
            		}
        			else if (moMsg.getQuestion().getAnswer() == Question.Answer.WRONG || 
        					moMsg.getQuestion().getAnswer() == Question.Answer.INVALID) {
            			isWrongMO= true;
            		}
        		}
        		else {
        			isWrongMO = false;
        		}
        	}
    	}
    	return isWrongMO;
    }
   
    /**
     * Returns true if the player did not sent any MOs after the last backToLevelOne
     * Valid only for campaigns with lives
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public boolean isBackToSquareOne(List<Message> messages, Date fr, Date to) {
    	int numWrongs = 0;
    	int numCorrect = 0;
    	boolean backToSquareOne = false;
    	for (Message m : messages) {
    		Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileOriginatedMessage) {
        		MobileOriginatedMessage moMsg = (MobileOriginatedMessage)m;
        		
        		if (backToSquareOne) {
        			backToSquareOne = false;
        			numWrongs = 0;
        			numCorrect = 0;
        		}
        		
        		if (moMsg.getQuestion() != null) {
        			if (moMsg.getQuestion().getAnswer() == Question.Answer.CORRECT) {
            			numCorrect ++;
            		}
            		else if (moMsg.getQuestion().getAnswer() == Question.Answer.WRONG) {
            			numWrongs ++;
            			if (numCorrect >= 5 && numWrongs >= 4) {
                			backToSquareOne = true;
                		}
            			if (numCorrect < 5 && numWrongs >= 1) {
                			backToSquareOne = true;
                		}
            		}
        		}
        		else {
        			numCorrect ++;
        		}
        		
        	}
    	}
    	return backToSquareOne;
    }
    
    public float getNumBulkMTSentSinceLastMO(List<Message> messages, Date fr, Date to) {
    	int streakBulks = 0;
    	for (int i = messages.size()-1 ; i >= 0; i--) {
    		Message m = messages.get(i);
    		Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileOriginatedMessage) {
        		for (int j = i + 1; j < messages.size(); j ++) {
        			Message mg = messages.get(j);
            		Date dg = mg.getDate();
                	if (dg.before(fr) || dg.after(to)) {
                		continue;
                	}
                	
                	if (mg instanceof MobileTerminatedMessage) {
                		MobileTerminatedMessage mtMsg = (MobileTerminatedMessage) mg;
                		if (mtMsg.getType() == MobileTerminatedMessage.RequestType.Bulk) {
                			streakBulks ++;
                		}
                	}
                	else {
                		return (float)streakBulks;
                	}
        		}
        		return (float)streakBulks;
        	}
    	}
    	return (float)streakBulks;
    }
    
    public float getMaxNumBulkMTSentSinceLastMO(List<Message> messages, Date fr, Date to) {
    	int maxStreakBulks = 0;
    	int currentStreakBulks = 0;
    	for (int i = 0 ; i < messages.size(); i++) {
    		Message m = messages.get(i);
    		Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileOriginatedMessage) {
        		for (int j = i + 1; j < messages.size(); j ++) {
        			Message mg = messages.get(j);
            		Date dg = mg.getDate();
                	if (dg.before(fr) || dg.after(to)) {
                		continue;
                	}
                	
                	if (mg instanceof MobileTerminatedMessage) {
                		MobileTerminatedMessage mtMsg = (MobileTerminatedMessage) mg;
                		if (mtMsg.getType() == MobileTerminatedMessage.RequestType.Bulk) {
                			currentStreakBulks ++;
                		}
                	}
                	else {
                		if (currentStreakBulks > maxStreakBulks) {
                			maxStreakBulks = currentStreakBulks;
                		}
                		currentStreakBulks = 0;
                		i = j;
                		break;
                	}
        		}
        	}
    	}
    	return (float)maxStreakBulks;
    }
    
    /**
     * Get the maximum time without a MT or MO
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public float getMaxNumDaysOfInactivity(List<Message> messages, Date fr, Date to) {
    	return 0.0f;
    }
    
    /**
     * Get number of days that the player sent at least one MOs
     * @param messages
     * @param fr
     * @param to
     * @return
     */
    public float getNumDaysMO(List<Message> messages, Date fr, Date to) {
    	boolean[] days = getMOSentDays(messages, fr, to);
    	int numDays = 0;
    	for (boolean b : days) {
    		if (b) {
    			numDays ++;
    		}
    	}
    	return (float)numDays;
    }
    
    
    public float getNumMOSinceLastBulkMT(List<Message> messages, Date fr, Date to) {
    	int streakMO = 0;
    	for (int i = messages.size()-1 ; i >= 0; i--) {
    		Message m = messages.get(i);
    		Date date = m.getDate();
        	if (date.before(fr) || date.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileTerminatedMessage) {
        		MobileTerminatedMessage mtMsg = (MobileTerminatedMessage) m;
        		if (mtMsg.getType() != MobileTerminatedMessage.RequestType.Bulk) {
        			continue;
        		}
        		for (int j = i + 1; j < messages.size(); j ++) {
        			Message mg = messages.get(j);
            		Date dg = mg.getDate();
                	if (dg.before(fr) || dg.after(to)) {
                		continue;
                	}
                	
                	if (mg instanceof MobileOriginatedMessage) {
                		streakMO ++;
                	}
                	
        		}
        		break;
        	}
    	}
    	return streakMO > 10 ? 10.0f : (float)streakMO;
    }
    
    public boolean isTimeOfLastBulkMTWithinTimeWindowOfFirstMO(List<Message> messages, Date fr, Date to, int hoursWindow) {
    	int firstMOHours = 0;
    	int firstMOSeconds = 0;
    	Date moDate;
    	for (int i = 0 ; i < messages.size(); i++) {
    		Message m = messages.get(i);
    		moDate = m.getDate();
        	if (moDate.before(fr) || moDate.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileOriginatedMessage) {
        		firstMOHours = moDate.getHours();
        		firstMOSeconds = moDate.getSeconds();
        		
        		break;
        	}
    	}
    	
    	int lastBulkMTHours = 0;
    	int lastBulkMTSeconds = 0;
    	Date mtDate;
    	for (int i = messages.size()-1 ; i >= 0; i--) {
    		Message m = messages.get(i);
    		mtDate = m.getDate();
        	if (mtDate.before(fr) || mtDate.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileTerminatedMessage) {
        		MobileTerminatedMessage mtMsg = (MobileTerminatedMessage) m;
        		if (mtMsg.getType() != MobileTerminatedMessage.RequestType.Bulk) {
        			continue;
        		}
        		
        		lastBulkMTHours = mtDate.getHours();
        		lastBulkMTSeconds = mtDate.getSeconds();
        		
        		break;
        	}
    	}
    	
    	if (Math.abs(firstMOHours - lastBulkMTHours) < hoursWindow) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    public boolean isTimeOfLastBulkMTWithinTimeWindowOfLastMO(List<Message> messages, Date fr, Date to, int hoursWindow) {
    	int lastMOHours = 0;
    	int lastMOSeconds = 0;
    	Date moDate;
    	for (int i = messages.size()-1 ; i >= 0; i--) {
    		Message m = messages.get(i);
    		moDate = m.getDate();
        	if (moDate.before(fr) || moDate.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileOriginatedMessage) {
        		lastMOHours = moDate.getHours();
        		lastMOSeconds = moDate.getSeconds();
        		
        		break;
        	}
    	}
    	
    	int lastBulkMTHours = 0;
    	int lastBulkMTSeconds = 0;
    	Date mtDate;
    	for (int i = messages.size()-1 ; i >= 0; i--) {
    		Message m = messages.get(i);
    		mtDate = m.getDate();
        	if (mtDate.before(fr) || mtDate.after(to)) {
        		continue;
        	}
        	
        	if (m instanceof MobileTerminatedMessage) {
        		MobileTerminatedMessage mtMsg = (MobileTerminatedMessage) m;
        		if (mtMsg.getType() != MobileTerminatedMessage.RequestType.Bulk) {
        			continue;
        		}
        		
        		lastBulkMTHours = mtDate.getHours();
        		lastBulkMTSeconds = mtDate.getSeconds();
        		
        		break;
        	}
    	}
    	
    	if (Math.abs(lastMOHours - lastBulkMTHours) < hoursWindow) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    /**
     * Returns <code>true</code> if there is at least one Bulk MT sent to the user in the specified day
     * @param messages
     * @param date
     * @return
     */
    public boolean isMTSentInDay(List<Message> messages, Date d) {
    	long d2 = d.getTime() - (24 * 3600 * 1000);
        Date startDate = new Date(d2);
    	for (int i = messages.size()-1 ; i >= 0; i--) {
    		Message m = messages.get(i);
    		Date date = m.getDate();
        	if (date.after(d)) {
        		continue;
        	}
        	if (date.before(startDate)) {
        		break;
        	}
        	if (m instanceof MobileTerminatedMessage) {
        		MobileTerminatedMessage mtMsg = (MobileTerminatedMessage) m;
        		if (mtMsg.getType() != MobileTerminatedMessage.RequestType.Bulk) {
        			continue;
        		}
        		
        		return true;
        	}
    	}
    	return false;
    }
    
    /**
     * Creates the features for every user. If <code>isOperationMode</code> is true then the Bulk MT is 
     * considered to be sent to every user at the next day
     * @param isOperationMode If <code>true</code> then information about the next day is not present, so 
     * Bulk MT is considered to be sent to every user. If <code>false</code> then look at the provided messages
     * @return For every user return the list of features
     */
    public List<List<Feature>> createFeatures(boolean isOperationMode) {
    	String[] labels =  {"NoMOSent", "MOSent"};
        List<List<Feature>> features = new ArrayList<List<Feature>>(profiles.size());
        for (Map.Entry<Integer, List<Message>> entry : profiles.entrySet()) {
            List<Message> messages = entry.getValue();
            long d = toDate.getTime() - (24 * 3600 * 1000);
            Date fDate = new Date(d);
            long d2 = d - (PROFILE_SPAN * 24 * 3600 * 1000);
            Date beginDate = new Date(d2);
            
            List<Feature> featuresList = new LinkedList<Feature>();
            // Extract features here, one feature at a time
            
            if (isOperationMode) {
            	int userId = entry.getKey();
            	Feature userIdFeat = features.size() == 0 ? new NumericFeature(userId, "UserID") : new NumericFeature(userId);
                featuresList.add(userIdFeat);
            }
            
            float maxStreak = getMaxStreak(messages, beginDate, fDate);
            Feature maxStreakFeat = features.size() == 0 ? new NumericFeature(maxStreak, "MaxStreak") : new NumericFeature(maxStreak);
            featuresList.add(maxStreakFeat);
            
            float streak = getStreakSinceYesterday(messages, beginDate, fDate);
            Feature streakFeat = features.size() == 0 ? new NumericFeature(streak, "StreakSinceYesterday") : new NumericFeature(streak);
            featuresList.add(streakFeat);
           
 
            float totalMOBetween0And1 = getNumberOfMOBinned(messages, beginDate, fDate, 0, 1) ? 1.0f : 0.0f;
            Feature totalMOBetween0And1Feat = features.size() == 0 ? new NumericFeature(totalMOBetween0And1, "NumMOBetween0And1") : 
            														 new NumericFeature(totalMOBetween0And1);
            featuresList.add(totalMOBetween0And1Feat);
            
            float totalMOBetween1And3 = getNumberOfMOBinned(messages, beginDate, fDate, 1, 3) ? 1.0f : 0.0f;
            Feature totalMOBetween1And3Feat = features.size() == 0 ? new NumericFeature(totalMOBetween1And3, "NumMOBetween1And3") :
            	                                                     new NumericFeature(totalMOBetween1And3);
            featuresList.add(totalMOBetween1And3Feat);
            
            float totalMOBetween3And7 = getNumberOfMOBinned(messages, beginDate, fDate, 3, 7) ? 1.0f : 0.0f;
            Feature totalMOBetween3And7Feat = features.size() == 0 ? new NumericFeature(totalMOBetween3And7, "NumMOBetween3And7") :
            														 new NumericFeature(totalMOBetween3And7);
            featuresList.add(totalMOBetween3And7Feat);
            
            float totalMOBetween7And15 = getNumberOfMOBinned(messages, beginDate, fDate, 7, 15) ? 1.0f : 0.0f;
            Feature totalMOBetween7And15Feat = features.size() == 0 ? new NumericFeature(totalMOBetween7And15, "NumMOBetween7And15") :
            														  new NumericFeature(totalMOBetween7And15);
            featuresList.add(totalMOBetween7And15Feat);
            
            float totalMOBetween15And30 = getNumberOfMOBinned(messages, beginDate, fDate, 15, 30) ? 1.0f : 0.0f;
            Feature totalMOBetween15And30Feat = features.size() == 0 ? new NumericFeature(totalMOBetween15And30, "NumMOBetween15And30") :
            														   new NumericFeature(totalMOBetween15And30);
            featuresList.add(totalMOBetween15And30Feat);
            
            float totalMOBetween30And60 = getNumberOfMOBinned(messages, beginDate, fDate, 30, 60) ? 1.0f : 0.0f;
            Feature totalMOBetween30And60Feat = features.size() == 0 ? new NumericFeature(totalMOBetween30And60, "NumMOBetween30And60") :
            														   new NumericFeature(totalMOBetween30And60);
            featuresList.add(totalMOBetween30And60Feat);
            
            float totalMOBetween60And100 = getNumberOfMOBinned(messages, beginDate, fDate, 60, 100) ? 1.0f : 0.0f;
            Feature totalMOBetween60And100Feat = features.size() == 0 ? new NumericFeature(totalMOBetween60And100, "NumMOBetween60And100") :
            															new NumericFeature(totalMOBetween60And100);
            featuresList.add(totalMOBetween60And100Feat);
            
            float totalMOBetween100AndInf = getNumberOfMOBinned(messages, beginDate, fDate, 100, 1000000) ? 1.0f : 0.0f;
            Feature totalMOBetween100AndInfFeat = features.size() == 0 ? new NumericFeature(totalMOBetween100AndInf, "NumMOBetween100AndInf") :
            															 new NumericFeature(totalMOBetween100AndInf);
            featuresList.add(totalMOBetween100AndInfFeat);
            
            
            long y = d - (24 * 3600 * 1000);
            Date yesterday = new Date(y);
 
            float noMOYesterday = getNumberOfMOBinned(messages, yesterday, fDate, -1, 0) ? 1.0f : 0.0f;
            Feature noMOYesterdayFeat = features.size() == 0 ? new NumericFeature(noMOYesterday, "NumMOIsZeroYesterday") : 
            												   new NumericFeature(noMOYesterday);
            featuresList.add(noMOYesterdayFeat);
            
            float yesterdayMOBetween0And1 = getNumberOfMOBinned(messages, yesterday, fDate, 0, 1) ? 1.0f : 0.0f;
            Feature yesterdayMOBetween0And1Feat = features.size() == 0 ? new NumericFeature(yesterdayMOBetween0And1, "NumMOBetween0And1Yesterday") : 
            															 new NumericFeature(yesterdayMOBetween0And1);
            featuresList.add(yesterdayMOBetween0And1Feat);
            
            float yesterdayMOBetween1And3 = getNumberOfMOBinned(messages, yesterday, fDate, 1, 3) ? 1.0f : 0.0f;
            Feature yesterdayMOBetween1And3Feat = features.size() == 0 ? new NumericFeature(yesterdayMOBetween1And3, "NumMOBetween1And3Yesterday") : 
            															 new NumericFeature(yesterdayMOBetween1And3);
            featuresList.add(yesterdayMOBetween1And3Feat);
            
            float yesterdayMOBetween3And7 = getNumberOfMOBinned(messages, yesterday, fDate, 3, 7) ? 1.0f : 0.0f;
            Feature yesterdayMOBetween3And7Feat = features.size() == 0 ? new NumericFeature(yesterdayMOBetween3And7, "NumMOBetween3And7Yesterday") :
            															 new NumericFeature(yesterdayMOBetween3And7);
            featuresList.add(yesterdayMOBetween3And7Feat);
            
            float yesterdayMOBetween15And30 = getNumberOfMOBinned(messages, yesterday, fDate, 15, 30) ? 1.0f : 0.0f;
            Feature yesterdayMOBetween15And30Feat = features.size() == 0 ? new NumericFeature(yesterdayMOBetween15And30, "NumMOBetween15And30Yesterday") :
            															   new NumericFeature(yesterdayMOBetween15And30);
            featuresList.add(yesterdayMOBetween15And30Feat);
            
            float yesterdayMOBetween30And60 = getNumberOfMOBinned(messages, yesterday, fDate, 30, 60) ? 1.0f : 0.0f;
            Feature yesterdayMOBetween30And60Feat = features.size() == 0 ? new NumericFeature(yesterdayMOBetween30And60, "NumMOBetween30And60Yesterday") :
            															   new NumericFeature(yesterdayMOBetween30And60);
            featuresList.add(yesterdayMOBetween30And60Feat);
            
            float yesterdayMOBetween60And100 = getNumberOfMOBinned(messages, yesterday, fDate, 60, 100) ? 1.0f : 0.0f;
            Feature yesterdayMOBetween60And100Feat = features.size() == 0 ? new NumericFeature(yesterdayMOBetween60And100, "NumMOBetween60And100Yesterday") :
            																new NumericFeature(yesterdayMOBetween60And100);
            featuresList.add(yesterdayMOBetween60And100Feat);
            
            float yesterdayMOBetween100AndInf = getNumberOfMOBinned(messages, yesterday, fDate, 100, 1000000) ? 1.0f : 0.0f;
            Feature yesterdayMOBetween100AndInfFeat = features.size() == 0 ? new NumericFeature(yesterdayMOBetween100AndInf, "NumMOBetween100AndInfYesterday") :
            																 new NumericFeature(yesterdayMOBetween100AndInf);
            featuresList.add(yesterdayMOBetween100AndInfFeat);
            
            float numBulkMT = getNumberOfBulkMT(messages, beginDate, fDate);
            Feature numBulkMTFeat = features.size() == 0 ? new NumericFeature(numBulkMT, "TotalNumBulkMT") :
            											   new NumericFeature(numBulkMT);
            featuresList.add(numBulkMTFeat);
            
            float numBulkMTYesterday = getNumberOfBulkMT(messages, yesterday, fDate);
            Feature numBulkMTYesterdayFeat = features.size() == 0 ? new NumericFeature(numBulkMTYesterday, "NumBulkMTYesterday") :
            														new NumericFeature(numBulkMTYesterday);
            featuresList.add(numBulkMTYesterdayFeat);
            
            float numDaysSinceLastMO = getDaysSinceLastMO(messages, beginDate, fDate);
            Feature numDaysSinceLastMOFeat = features.size() == 0 ? new NumericFeature(numDaysSinceLastMO, "NumDaysSinceLastMO") : 
            														new NumericFeature(numDaysSinceLastMO);
            featuresList.add(numDaysSinceLastMOFeat);
           
            float numDaysSinceLastBulkMT = getDaysSinceLastBulkMT(messages, beginDate, fDate);
            Feature numDaysSinceLastBulkMTFeat = features.size() == 0 ? new NumericFeature(numDaysSinceLastBulkMT, "NumDaysSinceLastBulkMT") : 
            															new NumericFeature(numDaysSinceLastBulkMT);
            featuresList.add(numDaysSinceLastBulkMTFeat);
            
            float getDayOfFirstMO = getDayOfFirstMO(messages, beginDate, fDate);
            Feature getDayOfFirstMOFeat = features.size() == 0 ? new NumericFeature(getDayOfFirstMO, "DayOfFirstMO") : 
            													 new NumericFeature(getDayOfFirstMO);
            featuresList.add(getDayOfFirstMOFeat);
            
            float getNumCorrectBetweenInfAnd0 = getDiffNumCorrectMinusNumWrongBinned(messages, beginDate, fDate, -1, 0);
            Feature getNumCorrectBetweenInfAnd0Feat = features.size() == 0 ? new NumericFeature(getNumCorrectBetweenInfAnd0, "DiffNumCorrectMinusNumWrongBetweenInfAnd0") : 
            																 new NumericFeature(getNumCorrectBetweenInfAnd0);
            featuresList.add(getNumCorrectBetweenInfAnd0Feat);
            
            float getNumCorrectBetween0And1 = getDiffNumCorrectMinusNumWrongBinned(messages, beginDate, fDate, 0, 1);
            Feature getNumCorrectBetween0And1Feat = features.size() == 0 ? new NumericFeature(getNumCorrectBetween0And1, "DiffNumCorrectMinusNumWrongBetween0And1") : 
            															   new NumericFeature(getNumCorrectBetween0And1);
            featuresList.add(getNumCorrectBetween0And1Feat);
            
            float getNumCorrectBetween1And5 = getDiffNumCorrectMinusNumWrongBinned(messages, beginDate, fDate, 1, 5);
            Feature getNumCorrectBetween1And5Feat = features.size() == 0 ? new NumericFeature(getNumCorrectBetween1And5, "DiffNumCorrectMinusNumWrongBetween1And5") :
            															   new NumericFeature(getNumCorrectBetween1And5);
            featuresList.add(getNumCorrectBetween1And5Feat);
            
            float getNumCorrectBetween5And9 = getDiffNumCorrectMinusNumWrongBinned(messages, beginDate, fDate, 5, 9);
            Feature getNumCorrectBetween5And9Feat = features.size() == 0 ? new NumericFeature(getNumCorrectBetween5And9, "DiffNumCorrectMinusNumWrongBetween5And9") :
            															   new NumericFeature(getNumCorrectBetween5And9);
            featuresList.add(getNumCorrectBetween5And9Feat);
            
            float getNumCorrectBetween10AndInf = getDiffNumCorrectMinusNumWrongBinned(messages, beginDate, fDate, 9, 100000);
            Feature getNumCorrectBetween10AndInfFeat = features.size() == 0 ? new NumericFeature(getNumCorrectBetween10AndInf, "DiffNumCorrectMinusNumWrongBetween10AndInf") :
            																  new NumericFeature(getNumCorrectBetween10AndInf);
            featuresList.add(getNumCorrectBetween10AndInfFeat);
            
            float getDayOfMaxNumCorrectMO = getDayOfMaxDiffNumCorrectMinusNumWrongMO(messages, beginDate, fDate);
            Feature getDayOfMaxNumCorrectMOFeat = features.size() == 0 ? new NumericFeature(getDayOfMaxNumCorrectMO, "DayOfMaxDiffNumCorrectMOMinusNumWrong") : 
            															 new NumericFeature(getDayOfMaxNumCorrectMO);
            featuresList.add(getDayOfMaxNumCorrectMOFeat);
            
            float getMaxNumCorrectMO = getMaxDiffNumCorrectMinusNumWrongMO(messages, beginDate, fDate);
            Feature getMaxNumCorrectMOFeat = features.size() == 0 ? new NumericFeature(getMaxNumCorrectMO, "MaxDiffNumCorrectMinusNumWrongMO") :
            														new NumericFeature(getMaxNumCorrectMO);
            featuresList.add(getMaxNumCorrectMOFeat);
            
            float getNumBulkMTSentSinceLastMO = getNumBulkMTSentSinceLastMO(messages, beginDate, fDate);
            Feature getNumBulkMTSentSinceLastMOFeat = features.size() == 0 ? new NumericFeature(getNumBulkMTSentSinceLastMO, "NumBulkMTSinceLastMO") : 
            																 new NumericFeature(getNumBulkMTSentSinceLastMO);
            featuresList.add(getNumBulkMTSentSinceLastMOFeat);
            
            float getMaxNumBulkMTSentSinceLastMO = getMaxNumBulkMTSentSinceLastMO(messages, beginDate, fDate);
            Feature getMaxNumBulkMTSentSinceLastMOFeat = features.size() == 0 ? new NumericFeature(getMaxNumBulkMTSentSinceLastMO, "MaxNumBulkMTSinceLastMO") :
            																	new NumericFeature(getMaxNumBulkMTSentSinceLastMO);
            featuresList.add(getMaxNumBulkMTSentSinceLastMOFeat);
            
            float getNumDaysMO = getNumDaysMO(messages, beginDate, fDate);
            Feature getNumDaysMOFeat = features.size() == 0 ? new NumericFeature(getNumDaysMO, "NumDaysMO") : 
            												  new NumericFeature(getNumDaysMO);
            featuresList.add(getNumDaysMOFeat);
            
            float getNumMOSinceLastBulkMT = getNumMOSinceLastBulkMT(messages, beginDate, fDate);
            Feature getNumMOSinceLastBulkMTFeat = features.size() == 0 ? new NumericFeature(getNumMOSinceLastBulkMT, "NumMOSinceLastBulkMT") :
            															 new NumericFeature(getNumMOSinceLastBulkMT);
            featuresList.add(getNumMOSinceLastBulkMTFeat);
            
            float isTimeOfLastBulkMTWithinTimeWindowOfFirstMO = isTimeOfLastBulkMTWithinTimeWindowOfFirstMO(messages, beginDate, fDate, 2) ? 1.0f : 0.0f;
            Feature isTimeOfLastBulkMTWithinTimeWindowOfFirstMOFeat = features.size() == 0 ? new NumericFeature(isTimeOfLastBulkMTWithinTimeWindowOfFirstMO, "IsTimeOfLastBulkMTWithinTwoHoursOfFirstMO") :
            																				 new NumericFeature(isTimeOfLastBulkMTWithinTimeWindowOfFirstMO);
            featuresList.add(isTimeOfLastBulkMTWithinTimeWindowOfFirstMOFeat);
            
            float isTimeOfLastBulkMTWithinTimeWindowOfLastMO = isTimeOfLastBulkMTWithinTimeWindowOfLastMO(messages, beginDate, fDate, 2) ? 1.0f : 0.0f;
            Feature isTimeOfLastBulkMTWithinTimeWindowOfLastMOFeat = features.size() == 0 ? new NumericFeature(isTimeOfLastBulkMTWithinTimeWindowOfLastMO, "IsTimeOfLastBulkMTWithinTwoHoursOfLastMO") :
            																				new NumericFeature(isTimeOfLastBulkMTWithinTimeWindowOfLastMO);
            featuresList.add(isTimeOfLastBulkMTWithinTimeWindowOfLastMOFeat);
            
            
            float getNumDaysDiffNumCorrectMinusNumWrongMOAbove3 = getNumDaysDiffNumCorrectMinusNumWrongMOAboveThreshold(messages, beginDate, fDate, 3);
            Feature getNumDaysDiffNumCorrectMinusNumWrongMOAbove3Feat = features.size() == 0 ? new NumericFeature(getNumDaysDiffNumCorrectMinusNumWrongMOAbove3, "NumDaysDiffNumCorrectMinusNumWrongMOAbove3") :
            																				   new NumericFeature(getNumDaysDiffNumCorrectMinusNumWrongMOAbove3);
            featuresList.add(getNumDaysDiffNumCorrectMinusNumWrongMOAbove3Feat);
            
            float isLastMOWrongOrInvalid = isLastMOWrongOrInvalid(messages, yesterday, fDate) ? 1.0f : 0.0f;
            Feature isLastMOWrongOrInvalidFeat = features.size() == 0 ? new NumericFeature(isLastMOWrongOrInvalid, "IsLastMOWrongOrInvalid") :
            															new NumericFeature(isLastMOWrongOrInvalid);
            featuresList.add(isLastMOWrongOrInvalidFeat);
            
            float isMTSentInLastDay = 0.0f;
            if (!isOperationMode) {
            	isMTSentInLastDay = isMTSentInDay(messages, toDate) ? 1.0f : 0.0f;
            }
            else {
            	isMTSentInLastDay = 1.0f;
            }
            Feature isMTSentInLastDayFeat = features.size() == 0 ? new NumericFeature(isMTSentInLastDay, "IsMTSentInLastDay") :
				   new NumericFeature(isMTSentInLastDay);
            featuresList.add(isMTSentInLastDayFeat);
            
            if (featuresList.size() == 0) {
                logger.warn("No features could be created for userId " + entry.getKey());
                continue;
            }
            
            if (!isOperationMode) {
            	// Add label
                // Label is 1 if there is an MO at any point in the toDate day
                   short isMOSent = 0;
                   for (Message m : messages) {
                   	Date mDate = m.getDate();
                   	if (mDate.before(fDate) || mDate.after(toDate)) {
                   		continue;
                   	}
                   	if (m instanceof MobileOriginatedMessage) {
               	          isMOSent = 1;
               	          break;
                   	}
                   }
                   Feature label = new NominalFeature(labels, isMOSent, "Label");
                   featuresList.add(label);
            }
            else {
            	Feature label = new NominalFeature(labels, (short)0, "Label");
                featuresList.add(label);
            }
            
            features.add(featuresList);
        }
        
        return features;
    }
    
    
    
    public void writeFeatures(File file, List<List<Feature>> features) {
    	try {
    		PrintWriter out = new PrintWriter(new FileWriter(file));
    		out.println("@RELATION MOSentVSnoMOSent\n");
    		
    		// Write the @ATTRIBUTES part
    		for (Feature f : features.get(0)) {
    			StringBuffer sb = new StringBuffer();
    			sb.append("@ATTRIBUTE ");
    			sb.append(f.getName()); sb.append(" ");
    			sb.append(f.getType());
    			out.println(sb.toString());
    		}
    		// Write the @DATA part
    		out.println("\n@DATA");
    		for (List<Feature> fv : features) {
            	StringBuffer sb = new StringBuffer();
            	for (int i=0; i<fv.size()-1; i++) {
            		Feature f = fv.get(i);
            		sb.append(f.getValue()); sb.append(",");
            	}
            	sb.append(fv.get(fv.size()-1).getValue());  sb.append("\n");
            	
            	out.print(sb.toString());
            }
    		out.close();
    		logger.info("Wrote features in " + file.getAbsolutePath());
    	}
    	catch (Exception e){
    		logger.error("Could not write to file " + file.getAbsolutePath());
    		System.exit(-1);
    	}
        
    }
    
    public static Date[] findDateOfFirstAndLastMessageOfCampaign(File mtFile, File moFile) {
    	Date minDate = null;
    	Date maxDate = null;
    	try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mtFile)));
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
            	String[] st = lineStr.split(",");
                String dateString = st[0];
                dateString = dateString.substring(1);
                dateString = dateString.substring(0, dateString.length()-1);
                Date date = null;
                try {
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    date = (Date)formatter.parse(dateString);
                }
                catch (ParseException ex) {
                	continue;
                }
                if (minDate == null) {
                	minDate = date;
                	maxDate = date;
                }
                
                if (date.before(minDate)) {
                	minDate = date;
                }
                else if (date.after(maxDate)) {
                	maxDate = date;
                }
            }
            br.close();
    	}
    	catch (FileNotFoundException ex) {
        }
        catch (IOException ex) {
        }
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(moFile)));
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
            	String[] st = lineStr.split(",");
                String dateString = st[0];
                dateString = dateString.substring(1);
                dateString = dateString.substring(0, dateString.length()-1);
                Date date = null;
                try {
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    date = (Date)formatter.parse(dateString);
                }
                catch (ParseException ex) {
                	continue;
                }
                if (minDate == null) {
                	minDate = date;
                	maxDate = date;
                }
                
                if (date.before(minDate)) {
                	minDate = date;
                }
                else if (date.after(maxDate)) {
                	maxDate = date;
                }
            }
            br.close();
    	}
    	catch (FileNotFoundException ex) {
        }
        catch (IOException ex) {
        }
        
        Date[] dates = {minDate, maxDate};
        return dates;
    }
    
    /**
     * Loads the MO and MT messages in memory
     */
    public void load() {
        loadMO();
        loadMT();
        logger.info("Sorting message lists by date");
        for (List<Message> l : profiles.values()) {
            Collections.sort(l);
        }
    }
    
    /**
     * For every user returns true if the user was sent a Bulk MT in the specified time period
     * @param fromDate
     * @param toDate
     * @return
     */
    public HashMap<Integer, Boolean> getUsersThatReceivedBulkMT(Date fromDate, Date toDate) {
        HashMap<Integer, Boolean> hm = new HashMap<Integer, Boolean>();
        int numReceived = 0;
        for (Map.Entry<Integer, List<Message>> entry : profiles.entrySet()) {
            Date mtDate = null;
            for (Message m : entry.getValue()) {
                Date mDate = m.getDate();
                if (mDate.before(fromDate) || mDate.after(toDate)) {
                    continue;
                }
               
                if (m instanceof MobileTerminatedMessage) {
                    MobileTerminatedMessage mtMsg = (MobileTerminatedMessage)m;
                    if (mtMsg.getType() == MobileTerminatedMessage.RequestType.Bulk) {
                        mtDate = mDate;
                        
                        break;
                    }
                }
                
            }
            
            if (mtDate == null) {
                hm.put(entry.getKey(), false);
            }
            else {
                hm.put(entry.getKey(), true);
                numReceived ++;
            }
        }
        logger.info("Number of users that received a Bulk MT between " + fromDate.toString() + 
                " and " + toDate.toString() + " : " + numReceived + " Total number of users : " + hm.size());
        return hm;
    }
    
    /**
     * For every available user, returns true if the user has sent an MO after the last Bulk MT of the 
     * specified period OR if there is an MO sent anytime of the specified period if there is no Bulk MT
     * @param fromDate
     * @param toDate
     * @return
     */
    public HashMap<Integer, Boolean> getUsersThatSentMO(Date fromDate, Date toDate) {
        HashMap<Integer, Boolean> hm = new HashMap<Integer, Boolean>();
        int numSent = 0;
        for (Map.Entry<Integer, List<Message>> entry : profiles.entrySet()) {
            Date moDate = null;
            Date mtDate = null;
            for (Message m : entry.getValue()) {
                Date mDate = m.getDate();
                if (mDate.before(fromDate) || mDate.after(toDate)) {
                    continue;
                }
               
                if (m instanceof MobileTerminatedMessage) {
                    MobileTerminatedMessage mtMsg = (MobileTerminatedMessage)m;
                    if (mtMsg.getType() == MobileTerminatedMessage.RequestType.Bulk) {
                        mtDate = mDate;
                    }
                }
                else if (m instanceof MobileOriginatedMessage) {
//                    MobileOriginatedMessage moMsg = (MobileOriginatedMessage)m;
                    moDate = mDate;
                }
                
            }
            
            if (moDate == null) {
                hm.put(entry.getKey(), false);
            }
            else {
                if (mtDate == null) {
                    numSent ++;
                    hm.put(entry.getKey(), true);
                }
                else {
                    if (mtDate.before(moDate)) {
                        numSent ++;
                        hm.put(entry.getKey(), true);
                    }
                    else {
                        hm.put(entry.getKey(), false);
                    }
                }
            }
        }
        logger.info("Number of users that sent an MO between " + fromDate.toString() + 
                " and " + toDate.toString() + " : " + numSent + " Total number of users : " + hm.size());
        return hm;
    }
    
    public void print() {
    	for (Map.Entry<Integer, List<Message>> entry : profiles.entrySet()) {
    		logger.info("USER: " + entry.getKey());
            for (Message m : entry.getValue()) {
            	logger.info(m.toString());
            }
        }
    }
    
}
