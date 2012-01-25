package com.goldenDeal.goldenDeals.dealEstimator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YesNoVariable extends Variable {
	private Logger logger = LoggerFactory.getLogger(YesNoVariable.class);
	public enum YesNoValues {YES, NO}
	private YesNoValues value;
	
	public YesNoVariable(String name, String value) throws Exception {
		super.varName = name;
		if (value.equalsIgnoreCase("YES")) {
			this.value = YesNoValues.YES;
		}
		else if (value.equalsIgnoreCase("NO")) {
			this.value = YesNoValues.NO;
		}
		else {
			logger.error("Expecting Yes/No value but seen \"" + value + "\"");
			throw new Exception();
		}
	}
	
	public YesNoValues getValue() {
		return this.value;
	}

}
