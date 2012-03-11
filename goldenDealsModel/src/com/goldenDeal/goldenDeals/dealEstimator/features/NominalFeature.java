package com.goldenDeal.goldenDeals.dealEstimator.features;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.goldenDeal.goldenDeals.dealEstimator.GoldenDealsNormalized;

public class NominalFeature extends Feature {
	private short nominal;
	private String[] categories;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public NominalFeature(String[] categories, short i) {
		this.categories = categories;
		this.nominal = i;
		super.name = null;
	}
	
	public NominalFeature(String[] categories, short i, String name) {
		this.categories = categories;
		this.nominal = i;
		super.name = name;
	}
	
	public NominalFeature(String[] categories, String category, String name) throws Exception {
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
		for (int i = 0; i < categories.length - 1; i ++) {
			sb.append("@ATTRIBUTE "); sb.append(name); sb.append(Integer.toString(i)); sb.append(" NUMERIC"); 
			sb.append(GoldenDealsNormalized.newline); 
		}
		sb.append("@ATTRIBUTE "); sb.append(name); sb.append(Integer.toString(categories.length-1));  sb.append(" NUMERIC");
		return sb.toString();
	}
	
	
	public String getValue() {
		if (nominal < 0 || nominal >= categories.length) {
			logger.error("Out of Bounds when creating nominal feature");
			return "?";
		}
		StringBuffer sb = new StringBuffer();
		if (nominal == 0) {
			sb.append("1.0");
		}
		else {
			sb.append("0.0");
		}
		for (int i = 1 ; i < categories.length; i ++) {
			if (i == nominal) {
				sb.append(",1.0");
			}
			else {
				sb.append(",0.0");
			}
		}
		return sb.toString();
	}
	
	
}
