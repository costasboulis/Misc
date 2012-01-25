package com.upstream.datamining.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public String getType() {
		StringBuffer sb = new StringBuffer();
		sb.append("{"); 
		for (int i=0; i<categories.length-1; i++) {
			sb.append(categories[i]); sb.append(",");
		}
		sb.append(categories[categories.length-1]); sb.append("}");
		return sb.toString();
	}
	
	public String getValue() {
		if (nominal < 0 || nominal >= categories.length) {
			logger.error("Out of Bounds when creating nominal feature");
			return "?";
		}
		return categories[nominal];
	}

}
