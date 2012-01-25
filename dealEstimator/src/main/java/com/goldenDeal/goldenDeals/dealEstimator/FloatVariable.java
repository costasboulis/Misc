package com.goldenDeal.goldenDeals.dealEstimator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloatVariable extends Variable{
	private Logger logger = LoggerFactory.getLogger(FloatVariable.class);
	private float value;
	
	public FloatVariable(String name, String value) throws Exception {
		super.varName = name;
		
		try {
			this.value = Float.parseFloat(value);
		}
		catch (NumberFormatException ex) {
			logger.error("Cannot parse \"" + value + "\"");
			throw new Exception();
		}
	}
	
	public float getValue() {
		return this.value;
	}
}
