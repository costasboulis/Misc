package com.goldenDeal.goldenDeals.dealEstimator.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NominalFeature2 extends Feature {
	private short nominal;
	private String[] categories;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public NominalFeature2(String[] categories, short i) {
		this.categories = categories;
		this.nominal = i;
		super.name = null;
	}
	
	public NominalFeature2(String[] categories, short i, String name) {
		this.categories = categories;
		this.nominal = i;
		super.name = name;
	}
	
	public NominalFeature2(String[] categories, String category, String name) throws Exception {
		this.categories = categories;
		boolean found = false;
		for (short i = 0; i < categories.length; i ++) {
			if (categories[i].equalsIgnoreCase(category)) {
				this.nominal = i;
				found = true;
				break;
			}
		}
		if (! found) {
			logger.error("Could not find category \"" + category + "\"");
			throw new Exception();
		}
		super.name = name;
	}
	

	public String getType() {
		StringBuffer sb = new StringBuffer();
		sb.append("@ATTRIBUTE "); sb.append(name); sb.append(" {");
		for (int i = 0; i < categories.length - 1; i ++) {
			sb.append(categories[i]); sb.append(",");
		}
		sb.append(categories[categories.length - 1]); sb.append("}");
		return sb.toString();
	}
	
	
	public String getValue() {
		return categories[nominal];
	}


}
