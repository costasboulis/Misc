package com.upstream.datamining;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProfileTest extends TestCase {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private String className;
	private String dataDir;
	private final String campaignStartString = "02/10/2009 00:00:00";
    private final String fromDateString = "08/10/2009 23:59:59";
    private final String toDateString = "09/10/2009 23:59:59";
    private String mo = "motlog_maxStreak.txt";
    private String mt = "mttlog_maxStreak.txt";
    private Date campaignStart;
    private Date fromDate;
    private Date toDate;
    private FeatureBuilder profiles;
    
    public ProfileTest( String testName ) {
        super( testName );
        
        campaignStart = null;
        fromDate = null;
        toDate = null;
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            campaignStart = (Date)formatter.parse(campaignStartString);
            fromDate = (Date)formatter.parse(fromDateString);
            toDate = (Date)formatter.parse(toDateString);
        }
        catch (ParseException ex) {
            logger.error("Cannot parse date");
            System.exit(-1);
        }
        
        className = getClass().toString().replaceFirst("class ", "");
        String fs = File.separatorChar == '\\' ? "\\\\" : File.separator;
        className = className.replaceAll("\\.", fs);
        dataDir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator 
        + "resources" + File.separator;
        
        
        String fullPathMT = dataDir + mt;
        String fullPathMO = dataDir + mo;
        profiles = load(fullPathMO, fullPathMT);
    }
    
    private FeatureBuilder load(String mo, String mt) {
        FeatureBuilder pr = null;
        try {
            pr = new FeatureBuilder(new File(mo), new File(mt), toDate);
        }
        catch (IOException ex) {
            System.exit(-1);
        }
        
        pr.load();
        
        return pr;
    }
    
    public void testMaxStreak() {
        for (Map.Entry<Integer, List<Message>> entry : profiles.profiles.entrySet()) {
        	List<Message> messages = entry.getValue();      	
        	for (Message m : messages) {
        	    System.out.println(m.toString());
        	}
        	float maxStreak = profiles.getMaxStreak(messages, campaignStart, fromDate);
        	
        	assertEquals(maxStreak, 1.0f);
        }
    }
    
    public void testGetDiffNumCorrectMinusNumWrongDays() {
        for (Map.Entry<Integer, List<Message>> entry : profiles.profiles.entrySet()) {
            List<Message> messages = entry.getValue();    
            int[] diff = profiles.getDiffNumCorrectMinusNumWrongDays(messages, campaignStart, fromDate);
            
            assertEquals(diff.length, 7);
            assertEquals(diff[0], 15);
            assertEquals(diff[1], 0);
            assertEquals(diff[2], 0);
            assertEquals(diff[3], 0);
            assertEquals(diff[4], 0);
            assertEquals(diff[5], 0);
            assertEquals(diff[6], 0);
        }
    }
    
    public void testGetStreakBeforeCurrentDay() {
        for (Map.Entry<Integer, List<Message>> entry : profiles.profiles.entrySet()) {
            List<Message> messages = entry.getValue();
            
            float streak = profiles.getStreakSinceYesterday(messages, campaignStart, fromDate);
            
            assertEquals(streak, 0.0f);
        }
    }
   /* 
    public void testGetNumBulkMTSentSinceLastMO() {
        for (Map.Entry<Integer, List<Message>> entry : profiles.profiles.entrySet()) {
            List<Message> messages = entry.getValue();
            
            float x = profiles.getNumBulkMTSentSinceLastMO(messages, campaignStart, fromDate);
            
            assertEquals(x, 3.0f);
        }
    }
    */
    public void testGetNumDaysDiffNumCorrectMinusNumWrongMOAboveThreshold() {
    	for (Map.Entry<Integer, List<Message>> entry : profiles.profiles.entrySet()) {
            List<Message> messages = entry.getValue();
            
            float x = profiles.getNumDaysDiffNumCorrectMinusNumWrongMOAboveThreshold(messages, campaignStart, fromDate, 1);
            
            assertEquals(x, 1.0f);
        }
    	
    }
}
