package com.goldenDeal.goldenDeals.dealEstimator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.goldenDeal.goldenDeals.dealEstimator.features.*;


/**
 * This class gets as input the csv file with the raw data and produces an arff file ready to be processed
 * by Weka or other. 
 * 
 * Normalizations performed in the dependent variable. 
 * A) Gets all the values of the AmountPerDay variable and computes averages per Site, Location, Month & Year
 * B) Computes n = AmountPerDay / NormalizedAmountPerDay and estimates M equal-sized quantiles, thus computing
 * the function f(n) = quantile
 * C) The independent variable is the quantile
 * 
 *  
 * @author kboulis
 *
 */

public class GoldenDealsNormalized {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private List<Deal> deals;
	private List<List<Feature>> features;
	private HashMap<String, List<Float>> quantiles;
	private HashMap<String, Float> grouponCategories;
	private static final int MIN_DEALS_FOR_GREATDEAL_FEATURE = 10;
	private static final String GREATDEAL_DONT_KNOW = "Don't know";
	private static final String GREATDEAL_YES = "Yes";
	private static final String GREATDEAL_NO = "No";
	public static final int NUMBER_OF_QUANTILES = 20;
	
	/* The value when the binary variable is true */
	public static float BINARY_VARIABLE_POSITIVE = 1.0f;
	
	/* The value when the binary variable is false */
	public static float BINARY_VARIABLE_NEGATIVE = 0.0f;
	
	
	
	public GoldenDealsNormalized() {
		features = new LinkedList<List<Feature>>();
	}
	
	private void loadDeals(File rawDataFile) throws IOException {
		if (!rawDataFile.exists()) {
            logger.error("Cannot find file " + rawDataFile.getAbsolutePath());
            throw new IOException();
        }
        if (!rawDataFile.canRead()) {
            logger.error("Cannot read file " + rawDataFile.getAbsolutePath());
            throw new IOException();
        }
        if (rawDataFile.isDirectory()) {
            logger.error("Expecting file but encountered directory " + rawDataFile.getAbsolutePath());
            throw new IOException();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rawDataFile)));
        String lineStr;
        lineStr = br.readLine();
		HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
		String[] fieldNames = lineStr.split(";");
		for (int i = 0; i < fieldNames.length; i ++) {
			names2Index.put(fieldNames[i], new Integer(i));
		}
		deals = new LinkedList<Deal>();
		while ((lineStr = br.readLine()) != null) {
            Deal deal = new Deal(lineStr, names2Index);
            
            deals.add(deal);
		}
		br.close();
	}
	
	private void calculateNormalization() {
        quantiles = new HashMap<String, List<Float>>();
        for (Deal deal : deals) {
            StringBuffer sb = new StringBuffer();
            sb.append(deal.getSite()); sb.append("_");
            sb.append(deal.getLocation()); sb.append("_");
            sb.append(deal.getMonth()); 
            
            String key = sb.toString();
            
            List<Float> f = quantiles.get(key);
            if (f == null) {
            	f = new LinkedList<Float>();
            	quantiles.put(key, f);
            }
            
        	f.add(deal.getAmountPerDay());
        }
        
        
        Set<String> keySet = quantiles.keySet();
        for (String s : keySet) {
        	List<Float> f = quantiles.get(s);
        	Collections.sort(f);
        }
	}
	
	private void calculateAverageRankPerCategory() {
        // Now calculate averages for each category / subcategory
        grouponCategories = new HashMap<String, Float>();
        HashMap<String, Float> grouponCategoriesSS1 = new HashMap<String, Float>();
        HashMap<String, Float> grouponCategoriesSS0 = new HashMap<String, Float>();
        HashMap<String, HashMap<String,Float>> grouponSubCategoriesSS1 = new HashMap<String, HashMap<String, Float>>();
        HashMap<String, HashMap<String,Float>> grouponSubCategoriesSS0 = new HashMap<String, HashMap<String, Float>>();
        for (Deal deal : deals) {
            String grouponCategory = deal.getGrouponCategory();
            String grouponSubCategory = deal.getGrouponSubCategory();
            
            float normAmount = deal.getRank();
            
            Float f = grouponCategoriesSS1.get(grouponCategory);
            if (f == null) {
            	grouponCategoriesSS1.put(grouponCategory, normAmount);
            	grouponCategoriesSS0.put(grouponCategory, 1.0f);
            	
            	HashMap<String, Float> hmSS1 = new HashMap<String, Float>();
            	hmSS1.put(grouponSubCategory, normAmount);
            	grouponSubCategoriesSS1.put(grouponCategory, hmSS1);
            	HashMap<String, Float> hmSS0 = new HashMap<String, Float>();
            	hmSS0.put(grouponSubCategory, 1.0f);
            	grouponSubCategoriesSS0.put(grouponCategory, hmSS0);
            }
            else {
            	float d = grouponCategoriesSS0.get(grouponCategory).floatValue();
            	grouponCategoriesSS1.put(grouponCategory, f.floatValue() + normAmount);
            	grouponCategoriesSS0.put(grouponCategory, d + 1.0f);
            	
            	Float fs = grouponSubCategoriesSS1.get(grouponCategory).get(grouponSubCategory);
            	if (fs == null) {
            		grouponSubCategoriesSS1.get(grouponCategory).put(grouponSubCategory, normAmount);
                	grouponSubCategoriesSS0.get(grouponCategory).put(grouponSubCategory, 1.0f);
            	}
            	else {
            		grouponSubCategoriesSS1.get(grouponCategory).put(grouponSubCategory, fs.floatValue() + normAmount);
                	float ds = grouponSubCategoriesSS0.get(grouponCategory).get(grouponSubCategory);
                	grouponSubCategoriesSS0.get(grouponCategory).put(grouponSubCategory, ds + 1.0f);
            	}
            }
        }
        
        for (String grouponCategory : grouponCategoriesSS1.keySet()) {
        	Float f = grouponCategoriesSS1.get(grouponCategory);
        	Float d = grouponCategoriesSS0.get(grouponCategory);
        	
        	grouponCategories.put(grouponCategory, new Float(f / d));
        	System.out.println(grouponCategory + " -> " + f / d + " (" + d + ")");
        	
        	for (String grouponSubCategory : grouponSubCategoriesSS1.get(grouponCategory).keySet()) {
        		Float fs = grouponSubCategoriesSS1.get(grouponCategory).get(grouponSubCategory);
            	Float ds = grouponSubCategoriesSS0.get(grouponCategory).get(grouponSubCategory);
            	
            	System.out.println("\t\t" + grouponSubCategory + " -> " + fs / ds + " (" + ds + ")");
        	}
        }
	}
	
	// Computes the average number of days between deals of the same category/subcategory
	private void createLongerWaitSinceLastSimilarDealFeature() {
		HashMap<String, Float> averageSubCategoryIntervalSS1 = new HashMap<String, Float>();
        HashMap<String, Float> averageSubCategoryIntervalSS0 = new HashMap<String, Float>();
        for (Deal deal : deals) {
        	Date fromDate = deal.getFromDealDate();
//			Date toDate = deal.getToDealDate();
			String grouponCategory = deal.getGrouponCategory();
			String grouponSubCategory = deal.getGrouponSubCategory();
			Date minDate = new Date();
			minDate.setTime(1);
			Deal lastDeal = null;
			int interval = 0;
			for (Deal otherDeal : deals) {	
				Date fromDateOther = otherDeal.getFromDealDate();
				if (deal.getID().equals(otherDeal.getID()) || fromDate.before(fromDateOther) || 
						!otherDeal.getGrouponCategory().equals(grouponCategory) || 
						!otherDeal.getGrouponSubCategory().equals(grouponSubCategory) || 
						!otherDeal.getSite().equals(deal.getSite())) {
					continue;
				}
				
				if (fromDateOther.after(minDate)) {
					minDate = fromDateOther;
					lastDeal = otherDeal;
				}
			}
			if (lastDeal != null) {
				Date f = lastDeal.getFromDealDate();
				double d = ((double)deal.getFromDealDate().getTime() - (double)f.getTime()) / (1000.0 * 60 * 60 * 24);
				interval = (int)Math.round(d);
			}
			else {
				interval = 365;
			}
			
			String key = grouponCategory + "_" + grouponSubCategory;
            Float f = averageSubCategoryIntervalSS1.get(key);
            if (f == null) {
            	averageSubCategoryIntervalSS1.put(key, (float)interval);
            	averageSubCategoryIntervalSS0.put(key, 1.0f);
            }
            else {
            	float d = averageSubCategoryIntervalSS0.get(key);
            	averageSubCategoryIntervalSS1.put(key, f.floatValue() + (float)interval);
            	averageSubCategoryIntervalSS0.put(key, d + 1.0f);
            }
        }
        
        // Now go over the deals again and set the interval feature 
        for (Deal deal : deals) {
        	int interval = getNumDaysSinceLastSimilarDeal(deal);
        	
        	if (interval == -1) {
        		deal.setLongerWaitSinceSimilarDeal(false);
        	}
        	else {
        		String grouponCategory = deal.getGrouponCategory();
    			String grouponSubCategory = deal.getGrouponSubCategory();
            	String key = grouponCategory + "_" + grouponSubCategory;
            	Float f = averageSubCategoryIntervalSS1.get(key);
            	Float d = averageSubCategoryIntervalSS0.get(key);
            	
            	if (d >= 10) {
            		boolean b = interval - (f / d) > 0.0f ? true : false;
            		deal.setLongerWaitSinceSimilarDeal(b);
            	}
            	else {
            		deal.setLongerWaitSinceSimilarDeal(false);
            	}
        	}
        	
        	
			
        }
	}
	
	private int getNumDaysSinceLastSimilarDeal(Deal deal) {	
		Date minDate = new Date(); 
		minDate.setTime(1);
		Deal lastDeal = null;
		Date fromDate = deal.getFromDealDate();
		for (Deal otherDeal : deals) {
			Date fromDateOther = otherDeal.getFromDealDate();
			if (deal.getID().equals(otherDeal.getID()) || fromDate.before(fromDateOther) || 
					!otherDeal.getGrouponCategory().equals(deal.getGrouponCategory()) || 
					!otherDeal.getSite().equals(deal.getSite())) {
				continue;
			}
					
			if (fromDateOther.after(minDate)) {
				minDate = fromDateOther;
				lastDeal = otherDeal;
			}
		}
		if (lastDeal == null) {
			return(-1);
		}
		else {
			double days = ( (double)deal.getFromDealDate().getTime() - (double)lastDeal.getFromDealDate().getTime() ) 
			/ (1000.0 * 60.0 * 60.0 * 24.0); 
					
			int d = (int)Math.round(days);
			return d;
		}
	}
	 
	private void createGreatDealFeature() {
		// Now calculate average price of a subcategory
        HashMap<String, Float> averageSubCategoryPricesSS1 = new HashMap<String, Float>();
        HashMap<String, Float> averageSubCategoryPricesSS0 = new HashMap<String, Float>();
        for (Deal deal : deals) {
            float price = deal.getPriceAfterDiscount();
            
            String grouponCategory = deal.getGrouponCategory();
            String grouponSubCategory = deal.getGrouponSubCategory();
           
            
            String key = grouponCategory + "_" + grouponSubCategory;
            
            float v = deal.isOnePersonCoupon() ? 1.0f : 0.5f;
            float s = deal.getLuxuryHotel().equals("Yes") ? 5.0f : deal.getLuxuryHotel().equals("No") ? 4.0f : -1.0f;
            float value = key.equals("Travel_Hotels") ? price * v * (1.0f / deal.getNumberOfNightsInHotel()) * (1.0f /s ): price * v;
            
            Float f = averageSubCategoryPricesSS1.get(key);
            if (f == null) {
            	averageSubCategoryPricesSS1.put(key, value);
            	averageSubCategoryPricesSS0.put(key, 1.0f);
            }
            else {
            	float d = averageSubCategoryPricesSS0.get(key);
            	averageSubCategoryPricesSS1.put(key, f.floatValue() + value);
            	averageSubCategoryPricesSS0.put(key, d + 1.0f);
            }
        }
        
        
        for (Deal deal : deals) {
        	
            
            String grouponCategory = deal.getGrouponCategory();
            String grouponSubCategory = deal.getGrouponSubCategory();
            String key = grouponCategory + "_" + grouponSubCategory;
            
            float price = deal.getPriceAfterDiscount();
            float v = deal.isOnePersonCoupon() ? 1.0f : 0.5f;
            float s = deal.getLuxuryHotel().equals("Yes") ? 5.0f : deal.getLuxuryHotel().equals("No") ? 4.0f : -1.0f;
        	float normPrice = key.equals("Travel_Hotels") ? price * v * (1.0f / deal.getNumberOfNightsInHotel()) * (1.0f /s ): price * v;
        	
            float d = averageSubCategoryPricesSS0.get(key);
            float avgPrice = averageSubCategoryPricesSS1.get(key) / d;
            
            if (d <= MIN_DEALS_FOR_GREATDEAL_FEATURE) {
            	deal.setGreatDeal(GREATDEAL_DONT_KNOW);
            }
            else {
            	if (normPrice < avgPrice) {
            		deal.setGreatDeal(GREATDEAL_YES);
            	}
            	else {
            		deal.setGreatDeal(GREATDEAL_NO);
            	}
            }
        }
	}
	
	/*
	 * YES if there is at least one deal with the same Groupon category / subcategory active at the same time period
	 */
	private void createCompetingDealsFeature() {
		for (Deal deal : deals) {
			Date fromDate = deal.getFromDealDate();
//			Date toDate = deal.getToDealDate();
			String grouponCategory = deal.getGrouponCategory();
			String grouponSubCategory = deal.getGrouponSubCategory();
			deal.setCompetingDeal(false);
			for (Deal competingDeal : deals) {
				if (deal.getID().equals(competingDeal.getID())) {
					continue;
				}
				
				Date fromDateCompeting = competingDeal.getFromDealDate();
				Date toDateCompeting = competingDeal.getToDealDate();
				
				if (fromDateCompeting.equals(fromDate) &&
						grouponCategory.equals(competingDeal.getGrouponCategory()) &&
						grouponSubCategory.equals(competingDeal.getGrouponSubCategory()) &&
						deal.getSite().equals(competingDeal.getSite())) {
					deal.setCompetingDeal(true);
					break;
				}
				
				if (fromDateCompeting.before(fromDate) && toDateCompeting.after(fromDate) &&
						grouponCategory.equals(competingDeal.getGrouponCategory()) &&
						grouponSubCategory.equals(competingDeal.getGrouponSubCategory()) &&
								deal.getSite().equals(competingDeal.getSite())) {
					deal.setCompetingDeal(true);
					break;
				}
			}
		}
    }
	
	/*
	 * Number of deals that are active for a given provider, irrespective of the category
	 */
	private void createNumberOfActiveDealsFeature() {
		for (Deal deal : deals) {
			Date fromDate = deal.getFromDealDate();
			Date toDate = deal.getToDealDate();
			int c = 0;
			for (Deal otherDeal : deals) {
				if (deal.getID().equals(otherDeal.getID()) || !deal.getSite().equals(otherDeal.getSite())) {
					continue;
				}
				Date fromDateOther = otherDeal.getFromDealDate();
				if (fromDateOther.equals(fromDate)) {
					c ++;
				}
				else if (fromDateOther.after(fromDate) && fromDateOther.before(toDate)) {
					c ++;
				}
			}
			deal.setNumberOfActiveDeals(c);
		}
	}
	
	private void createDemandFeature() {
		for (Deal deal : deals) {
			Date minDate = new Date(); 
			minDate.setTime(1);
			Deal lastDeal = null;
			Date fromDate = deal.getFromDealDate();
			for (Deal otherDeal : deals) {
				Date fromDateOther = otherDeal.getFromDealDate();
				if (deal.getID().equals(otherDeal.getID()) || fromDate.before(fromDateOther) || 
						!otherDeal.getGrouponCategory().equals(deal.getGrouponCategory()) || 
						!otherDeal.getSite().equals(deal.getSite())) {
					continue;
				}
				
				if (fromDateOther.after(minDate)) {
					minDate = fromDateOther;
					lastDeal = otherDeal;
				}
			}
			if (lastDeal == null) {
				deal.setHasDemand(true);
			}
			else {
				float lastRank = lastDeal.getRank();
				Float avgRank = grouponCategories.get(deal.getGrouponCategory());
				if (avgRank == null) {
					logger.error("Cannot find category \"" + deal.getGrouponCategory() + "\"");
					System.exit(-1);
				}
				boolean hasDemand = lastRank - avgRank < 0.0f ? true : false;
				deal.setHasDemand(hasDemand);
			}
		}
	}
	
	private void createSameMerchantFeature() {
		for (Deal deal : deals) {
			Date minDate = new Date(); 
			minDate.setTime(1);
			Deal lastDeal = null;
			Date fromDate = deal.getFromDealDate();
			for (Deal otherDeal : deals) {
				Date fromDateOther = otherDeal.getFromDealDate();
				if (deal.getID().equals(otherDeal.getID()) || fromDate.before(fromDateOther) || 
						!deal.getBusinessName().equals(otherDeal.getBusinessName())) {
					continue;
				}
				
				if (fromDateOther.after(minDate)) {
					minDate = fromDateOther;
					lastDeal = otherDeal;
				}
			}
			if (lastDeal == null) {
				deal.setLastRankOfSameMerchant(-1.0f);
			}
			else {
				deal.setLastRankOfSameMerchant(lastDeal.getRank());
			}
		}
	}
	
	private void createRankFeature() {
		for (Deal deal : deals) {
			StringBuffer sb = new StringBuffer();
	        sb.append(deal.getSite()); sb.append("_");
	        sb.append(deal.getLocation()); sb.append("_");
	        sb.append(deal.getMonth()); 
	        
	        String key = sb.toString();
	        
	        List<Float> qnt = quantiles.get(key);
	        if (qnt == null) {
	        	logger.error("Cannot retrieve quantile for key \"" + key + "\"");
	        	System.exit(-1);
	        }
	        
	        float quantile = 1;
	        float amountPerDay = deal.getAmountPerDay();
	        int sz = qnt.size();
	        for (int i = sz - 1 ; i >= 0; i --) {
	        	float amnt = qnt.get(i);
	        	if (amnt <= amountPerDay) {
	        		float f = (float) (i + 1) / sz;
	        		quantile = (float)Math.ceil((double)f * (double)NUMBER_OF_QUANTILES);
	        		break;
	        	}
	        }
	        deal.setRank(quantile);
		}
	}
	
	
	public void readRawData(File rawDataFile) throws IOException {
        loadDeals(rawDataFile);
        calculateNormalization();
        createRankFeature();
        calculateAverageRankPerCategory();
        createGreatDealFeature();
        createCompetingDealsFeature();
        createNumberOfActiveDealsFeature();
        createDemandFeature();
        createLongerWaitSinceLastSimilarDealFeature();
        createSameMerchantFeature();
        for (Deal deal : deals) {
            
       	
        	if (deal.getRank() > 5 && deal.getRank() < 15) {
        		continue;
        	}
       
        	
            LinkedList<Feature> featList = new LinkedList<Feature>();
            
            
            Feature discountFeature = new NumericFeature(deal.getDiscount(), "Discount");
            featList.add(discountFeature);
            
            
            Feature priceAfterDiscountFeature = new NumericFeature(deal.getPriceAfterDiscount(), "PriceAfterDiscount");
            featList.add(priceAfterDiscountFeature);
            
            Feature lastRankFeature = deal.getLastRankOfSameMerchant() < 0.0f ? new NumericFeature("LastRank") :
            								new NumericFeature(deal.getLastRankOfSameMerchant(), "LastRank");
            featList.add(lastRankFeature);
            
            Feature dealDurationFeature = new NumericFeature(deal.getDealDuration(), "DealDuration");
            featList.add(dealDurationFeature);
            
            
            try {
            	Feature categoryFeature = new NominalFeature(Deal.grouponCategoryNames, deal.getGrouponCategory(), "GrouponCategory");
            	featList.add(categoryFeature);
            }
            catch (Exception ex) {
            	logger.error("Could not extract category feature for line \"" + deal.toString() + "\"");
            	System.exit(-1);
            }
            
            Feature hasMoreToPayFeature = deal.hasMoreToPay() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasMoreToPay") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasMoreToPay");
			featList.add(hasMoreToPayFeature);
			
			Feature hasMultiplePricesFeature = deal.hasMultiplePrices() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasMutliplePrices") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasMutliplePrices");
			featList.add(hasMultiplePricesFeature);
			
			Feature hasUnlimitedCouponsPerPersonFeature = deal.hasUnlimitedCouponsPerPerson() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasUnlimitedCouponsPerPerson") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasUnlimitedCouponsPerPerson");
			featList.add(hasUnlimitedCouponsPerPersonFeature);
			
			Feature prominentInNewsletterFeature = deal.isProminentInNewsletter() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "ProminentInNewsletter") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "ProminentInNewsletter");
			featList.add(prominentInNewsletterFeature);
			
            Feature requiresPhysicalPresenceFeature = deal.requiresPhysicalVisit() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "RequiresPhysicalVisit") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "RequiresPhysicalVisit");
			featList.add(requiresPhysicalPresenceFeature);
			
			Feature multipleStoresFeature = deal.hasMultipleStores() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "MultipleStores") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "MultipleStores");
			featList.add(multipleStoresFeature);
			
			Feature latinNameFeature = deal.hasLatinName() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "LatinName") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "LatinName");
			featList.add(latinNameFeature);
			
			Feature popularBrandFeature = deal.isPopularBrand() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "PopularBrand") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "PopularBrand");
			featList.add(popularBrandFeature);
			
			
			Feature phoneReservationFeature = deal.requiresPhoneReservation() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "RequiresPhoneReservation") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "RequiresPhoneReservation");
			featList.add(phoneReservationFeature);
			
			Feature validForSaturdayFeature = deal.isValidForSaturdays() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "ValidForSaturdays") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "ValidForSaturdays");
			featList.add(validForSaturdayFeature);
			
			Feature validForSundayFeature = deal.isValidForSundays() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "ValidForSundays") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "ValidForSundays");
			featList.add(validForSundayFeature);
			
			Feature validForEveryWeekdayFeature = deal.isValidForWeekdays() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "ValidForWeekdays") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "ValidForWeekdays");
			featList.add(validForEveryWeekdayFeature);
			
			Feature validWithoutExceptionsFeature = deal.isValidWithoutTimeExceptions() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "ValidWithoutTimeExceptions") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "ValidWithoutTimeExceptions");
			featList.add(validWithoutExceptionsFeature);
			
			Feature onePersonCouponFeature = deal.isOnePersonCoupon() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "OnePersonCoupon") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "OnePersonCoupon");
			featList.add(onePersonCouponFeature);
			
			Feature extraDiscountsFeature = deal.hasExtraDiscounts() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "ExtraDiscounts") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "ExtraDiscounts");
			featList.add(extraDiscountsFeature);
			
			Feature comboDealFeature = deal.isComboDeal() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "ComboDeal") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "ComboDeal");
			featList.add(comboDealFeature);
			
			Feature hasOptionsFeature = deal.hasOptions() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasOptions") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasOptions");
			featList.add(hasOptionsFeature);
			
			try {
				Feature numberOfVisitsFeature = new NominalFeature(Deal.numberOfVisits, deal.getVisits(), "VisitsPerCoupon");
				featList.add(numberOfVisitsFeature);
			}
			catch (Exception ex) {
            	logger.error("Could not extract number of visits feature for line \"" + deal.toString() + "\"");
            	System.exit(-1);
            }
			

 //           try {
 //           	Feature locationFeature = new NominalFeature(Deal.locs, deal.getLocation(), "LocationOffered");
//            	featList.add(locationFeature);
 //           }
 //           catch (Exception ex) {
 //           	logger.error("Could not extract location feature for line \"" + lineStr + "\"");
 //           	System.exit(-1);
 //           }
            
            
            Feature weekendDealFeature = deal.isWeekendDeal() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "WeekendDeal") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "WeekendDeal");
			featList.add(weekendDealFeature);
			
			
            try {
				Feature quantizedDaysBeforeActivationFeature = new NominalFeature(Deal.daysForActivation, deal.getQuantizedNumDaysBeforeActivation(), "DaysForActivationQuantile");
				featList.add(quantizedDaysBeforeActivationFeature);
			}
			catch (Exception ex) {
            	logger.error("Could not extract quantized days before activation feature for line \"" + deal.toString() + "\"");
            	System.exit(-1);
            }
			
			try {
				Feature roundedCouponDurationFeature = new NominalFeature(Deal.couponDurationGroups, deal.getQuantizedCouponDuration(), "CouponDurationQuantile");
				featList.add(roundedCouponDurationFeature);
			}
			catch (Exception ex) {
            	logger.error("Could not extract rounded coupons duration feature for line \"" + deal.toString() + "\"");
            	System.exit(-1);
            }
			
            
			 try {
				 Feature greatDealFeature = new NominalFeature(Deal.triStates, deal.getGreatDeal(), "GreatDeal");
				 featList.add(greatDealFeature);
			 }
			 catch (Exception ex) {
				 logger.error("Could not extract great deal feature for line \"" + deal.toString() + "\"");
				 System.exit(-1);
			 }
			 
			 try {
				 Feature geographicalAreaFeature = new NominalFeature(Deal.geographicalAreasNames, deal.getGeographicalArea().get(0), "GeographicalArea");
				 featList.add(geographicalAreaFeature);
			 }
			 catch (Exception ex) {
				 logger.error("Could not extract geographical area feature for line \"" + deal.toString() + "\"");
				 System.exit(-1);
			 }
			
			Feature numberOfActiveDealsFeature = new NumericFeature(deal.getNumberOfActiveDeals(), "ActiveDeals");
            featList.add(numberOfActiveDealsFeature);
            
            
			Feature hasDemandFeature = deal.hasDemand() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasDemand") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasDemand");
			featList.add(hasDemandFeature);
			
			
			Feature hasLongerWaitFeature = deal.hasLongerWaitSinceSimilarDeal() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasLongerIntervalForSimilarDeals") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasLongerIntervalForSimilarDeals");
			featList.add(hasLongerWaitFeature);
            
			
			Feature competingDealFeature = deal.hasCompetingDeal() ? 
					new NumericFeature(BINARY_VARIABLE_POSITIVE, "CompetingDeal") :
				    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "CompetingDeal");
			featList.add(competingDealFeature);
		
			try {
				Feature luxuryHotelFeature = new NominalFeature(Deal.triStates, deal.getLuxuryHotel(), "LuxuryHotel");
				featList.add(luxuryHotelFeature);
			}
			catch (Exception ex) {
				logger.error("Could not extract luxury hotel feature for line \"" + deal.toString() + "\"");
				System.exit(-1);
			}
			
			try {
				Feature validForNextMajorVacationFeature = new NominalFeature(Deal.triStates, deal.getValidForNextMajorVacation(), "ValidForNextMajorVacation");
				featList.add(validForNextMajorVacationFeature);
			}
			catch (Exception ex) {
				logger.error("Could not extract valid for next major vacation feature for line \"" + deal.toString() + "\"");
				System.exit(-1);
			}
			
			int c = deal.getNumberOfNightsInHotel();
			try {
				Feature numberOfNightsInHotel = new NominalFeature(Deal.couponDurationGroups, c >= 4 ? "4" : Integer.toString(c), "NumberOfNights");
				featList.add(numberOfNightsInHotel);
			}
			catch (Exception ex) {
            	logger.error("Could not extract number of nights feature for line \"" + deal.toString() + "\"");
            	System.exit(-1);
            }
			
			
            /* Last feature is always the independent variable */
//			Feature normalizedFeature = new NumericFeature(deal.getRank(), "NormalizedQuantileAmountPerDay");
//			featList.add(normalizedFeature);
   
            
            
            try {
				 Feature goodBadDealFeature = new NominalFeature2(Deal.bin, deal.getRank() >= 15 ? "Yes" : "No", "GoodBadDeal");
				 featList.add(goodBadDealFeature);
			 }
			 catch (Exception ex) {
				 logger.error("Could not extract good/bad feature for line \"" + deal.toString() + "\"");
				 System.exit(-1);
			 }
            
			features.add(featList);
        }
	}
	
	
	public void writeFeatures(File file) {
    	try {
    		PrintWriter out = new PrintWriter(new FileWriter(file));
    		out.println("@RELATION RevenuePerDay" + newline);
    		
    		// Write the @ATTRIBUTES part
    		for (Feature f : features.get(0)) {
    			StringBuffer sb = new StringBuffer();
    			sb.append(f.getType());
    			out.println(sb.toString());
    		}
    		// Write the @DATA part
    		out.println(newline + "@DATA");
    		for (List<Feature> fv : features) {
            	StringBuffer sb = new StringBuffer();
            	for (int i = 0; i < fv.size() - 1; i ++) {
            		Feature f = fv.get(i);
            		sb.append(f.getValue()); sb.append(",");
            	}
            	sb.append(fv.get(fv.size()-1).getValue());  sb.append(newline);
            	
            	out.print(sb.toString());
            }
    		out.close();
    		logger.info("Wrote features in " + file.getAbsolutePath());
    	}
    	catch (Exception e){
    		logger.error("Could not write to file " + file.getAbsolutePath());
    		System.exit(-1);
    	}
        
    }
	
	
	
	public static void main( String[] args ) {
        GoldenDealsNormalized fb = new GoldenDealsNormalized();
        try {
        	fb.readRawData(new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\Filtered_All_Deals.csv"));
        }
        catch (IOException ex) {
        	System.exit(-1);
        }
        
        
        fb.writeFeatures(new File("C:\\Users\\kboulis\\Desktop\\group buying sites\\features_binaryClassifier.arff"));
        
    }
    
}
