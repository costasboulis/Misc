package com.goldenDeal.goldenDeals.dealEstimator;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateVariable extends Variable{
	private Logger logger = LoggerFactory.getLogger(DateVariable.class);
	private Date date;
	
	public DateVariable(String name, String value, boolean endOfDay) throws Exception {
		super.varName = name;
		
		SimpleDateFormat format =
            new SimpleDateFormat("d/M/yyyy H:m:s");

		String dateString = endOfDay ? value + " 23:59:59" : value + " 00:00:00";
        try {
            Date parsed = format.parse(dateString);
            this.date = parsed;
        }
        catch(ParseException pe) {
            logger.error("Cannot parse \"" + dateString + "\"");
            throw new Exception();
        }
	}
	
	public Date getDate() {
		return this.date;
	}
}
