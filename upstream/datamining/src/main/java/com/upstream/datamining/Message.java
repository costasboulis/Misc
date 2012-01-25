package com.upstream.datamining;

import java.util.Date;

public abstract class Message implements Comparable<Message>{
    protected Date date;
    
    public Message(Date d) {
        date = d;
    }
    
    public Date getDate() {
        return date;
    }
    
    public int compareTo(Message m) {
        Date d1 = this.date;
        return d1.compareTo(m.getDate());
    }
}
