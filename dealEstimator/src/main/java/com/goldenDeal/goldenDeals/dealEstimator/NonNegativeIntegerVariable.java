package com.goldenDeal.goldenDeals.dealEstimator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonNegativeIntegerVariable extends Variable{
	private Logger logger = LoggerFactory.getLogger(NonNegativeIntegerVariable.class);
	private int value;
	
	public NonNegativeIntegerVariable(String name, String value) throws Exception {
		super.varName = name;
		
		if (value.equalsIgnoreCase("Inf")) {
			this.value = Integer.MAX_VALUE;
			return;
		}
		try {
			this.value = Integer.parseInt(value);
		}
		catch (NumberFormatException ex) {
			logger.error("Cannot parse \"" + value + "\"");
			throw new Exception();
		}
		
		if (this.value < 0) {
			logger.error("Negative value");
			throw new Exception();
		}
	}
	
	public int getValue() {
		return this.value;
	}
}
