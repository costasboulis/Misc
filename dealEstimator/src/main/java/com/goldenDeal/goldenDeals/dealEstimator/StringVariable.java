package com.goldenDeal.goldenDeals.dealEstimator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringVariable extends Variable{
	private Logger logger = LoggerFactory.getLogger(getClass());
	private String value;
	
	public StringVariable(String name, String value) throws Exception {
		super.varName = name;
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}