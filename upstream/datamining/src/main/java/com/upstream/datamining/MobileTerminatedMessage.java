package com.upstream.datamining;

import java.util.Date;


public class MobileTerminatedMessage extends Message {
    public static enum RequestType {Question, Bulk_Question, Bulk, Information, Reply, UNKNOWN};
    private RequestType type;
    private int potentialPoints;
    private String comments;
    
    public MobileTerminatedMessage(Date d, RequestType rt, int pp) {
        super(d);
        this.type = rt;
        this.potentialPoints = pp;
    }
    
    public MobileTerminatedMessage(Date d, String rtString, int pp) {
        super(d);
        this.potentialPoints = pp;
        if ((rtString.equalsIgnoreCase("Question MT")) || (rtString.equalsIgnoreCase("\"Question MT\""))) {
            type = RequestType.Question;
        }
        else if ((rtString.equalsIgnoreCase("Bulk Question MT")) || (rtString.equalsIgnoreCase("\"Bulk Question MT\""))) {
            type = RequestType.Bulk_Question;
        }
        else if ((rtString.equalsIgnoreCase("Bulk MT")) || (rtString.equalsIgnoreCase("\"Bulk MT\""))) {
            type = RequestType.Bulk;
        }
        else if ((rtString.equalsIgnoreCase("Reply MT")) || (rtString.equalsIgnoreCase("\"Reply MT\""))) {
            type = RequestType.Reply;
        }
        else if ((rtString.equalsIgnoreCase("Information MT")) || (rtString.equalsIgnoreCase("\"Information MT\""))) {
            type = RequestType.Information;
        }
        else {
        	type = RequestType.UNKNOWN;
        }
        this.comments = null;
    }
    
    public MobileTerminatedMessage(Date d, String rtString, int pp, String comments) {
        super(d);
        this.potentialPoints = pp;
        if ((rtString.equalsIgnoreCase("Question MT")) || (rtString.equalsIgnoreCase("\"Question MT\""))) {
            type = RequestType.Question;
        }
        else if ((rtString.equalsIgnoreCase("Bulk Question MT")) || (rtString.equalsIgnoreCase("\"Bulk Question MT\""))) {
            type = RequestType.Bulk_Question;
        }
        else if ((rtString.equalsIgnoreCase("Bulk MT")) || (rtString.equalsIgnoreCase("\"Bulk MT\""))) {
            type = RequestType.Bulk;
        }
        else if ((rtString.equalsIgnoreCase("Reply MT")) || (rtString.equalsIgnoreCase("\"Reply MT\""))) {
            type = RequestType.Reply;
        }
        else if ((rtString.equalsIgnoreCase("Information MT")) || (rtString.equalsIgnoreCase("\"Information MT\""))) {
            type = RequestType.Information;
        }
        else {
        	type = RequestType.UNKNOWN;
        }
        
        this.comments = comments;
    }
    
    public int getPotentialPoints() {
        return this.potentialPoints;
    }
    
    public RequestType getType() {
        return this.type;
    }
    
    public String getComments() {
    	return this.comments;
    }
    
    public boolean isReplyMT() {
    	return type == RequestType.Reply;
    }
    
    public boolean isBulkQuestionMT() {
        return type == RequestType.Bulk_Question;
    }
    
    public String toString() {
    	String s = "MT_MSG Date: " + date.toString() + " " + type + " Potential_Points: " + potentialPoints;
    	
    	return s;
    }
}
