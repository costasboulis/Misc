package com.upstream.datamining;

import java.util.Date;

public class MobileOriginatedMessage extends Message{
    private Question q;
    private int points;
    private String comments;
    
    public MobileOriginatedMessage(Date d, Question q, int points) {
        super(d);
        this.q = q;
        this.points = points;
        comments = null;
    }
    
    public MobileOriginatedMessage(Date d, Question q, int points, String comments) {
        super(d);
        this.q = q;
        this.points = points;
        this.comments = comments;
    }
    
    public int getPoints() {
        return this.points;
    }
    
    public Question getQuestion() {
        return this.q;
    }
    
    public String getComments() {
    	return this.comments;
    }
    
    public String toString() {
    	String qs = q == null ? "NO_QUESTION" : q.toString();
    	String s = "MO_MSG Date: " + date.toString() + " " + qs + " Points: " + points;
    	
    	return s;
    }
}
