package com.goldenDeal.goldenDeals.dealEstimator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.goldenDeal.goldenDeals.dealEstimator.features.*;

//TODO: NORMALIZE REVENUEPERDAY TO MONTH, TO ADJUST THE GROWING USER BASE
public class FeatureBuilder {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private List<List<Variable>> rawData;
	private List<List<Feature>> features;
	/* The value when the binary variable is true */
	public static float BINARY_VARIABLE_POSITIVE = 1.0f;
	
	/* The value when the binary variable is false */
	public static float BINARY_VARIABLE_NEGATIVE = 0.0f;
	public static final String[] categoryNames = {"Restaurants - Greek", "Restaurants - Non Greek", "Spa - Beauty",
												  "Outdoor Activities", "Hotel", "Gym / Exercise / Fit", "Hair Salons", "Family Activities / Kid",
												  "Leisure / Hanging out", "Car / Computer", "Education / Learning"}; 
	
	public FeatureBuilder() {
		features = new LinkedList<List<Feature>>();
	}
	
	public void readRawData(File rawDataFile) throws IOException {
		rawData = new LinkedList<List<Variable>>();
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
        String[] varNames = lineStr.split(";");
        while ((lineStr = br.readLine()) != null) {
            List<Variable> varList = new LinkedList<Variable>();
            
            String[] st = lineStr.split(";");
            if (st.length != varNames.length) {
            	logger.error("Could not process line \"" + lineStr + "\" Expecting to see " + varNames.length + " fields but seen " + st.length);
            	continue;
            }
            try {
            	DateVariable var = new DateVariable(varNames[0], st[0], false);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[0] + " \"" + st[0] + "\"");
            	System.exit(-1);
            }
            
            try {
            	DateVariable var = new DateVariable(varNames[1], st[1], true);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[1] + " \"" + st[1] + "\"");
            	System.exit(-1);
            }
            
            try {
            	NonNegativeIntegerVariable var = new NonNegativeIntegerVariable(varNames[2], st[2]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[2] + " \"" + st[2] + "\"");
            	System.exit(-1);
            }
            
            try {
            	FloatVariable var = new FloatVariable(varNames[3], st[3]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[3] + " \"" + st[3] + "\"");
            	System.exit(-1);
            }
            
            try {
            	FloatVariable var = new FloatVariable(varNames[4], st[4]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[4] + " \"" + st[4] + "\"");
            	System.exit(-1);
            }
            
            try {
            	StringVariable var = new StringVariable(varNames[5], st[5]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[5] + " \"" + st[5] + "\"");
            	System.exit(-1);
            }
            
            try {
            	StringVariable var = new StringVariable(varNames[6], st[6]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[6] + " \"" + st[6] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[7], st[7]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[7] + " \"" + st[7] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[8], st[8]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[8] + " \"" + st[8] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[9], st[9]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[9] + " \"" + st[9] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[10], st[10]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[10] + " \"" + st[10] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[11], st[11]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[11] + " \"" + st[11] + "\"");
            	System.exit(-1);
            }
            
            try {
            	NonNegativeIntegerVariable var = new NonNegativeIntegerVariable(varNames[12], st[12]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[12] + " \"" + st[12] + "\"");
            	System.exit(-1);
            }
            
            try {
            	NonNegativeIntegerVariable var = new NonNegativeIntegerVariable(varNames[13], st[13]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[13] + " \"" + st[13] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[16], st[16]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[16] + " \"" + st[16] + "\" line " +lineStr);
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[17], st[17]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[17] + " \"" + st[17] + "\" line " +lineStr);
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[18], st[18]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[18] + " \"" + st[18] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[19], st[19]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[19] + " \"" + st[19] + "\"");
            	System.exit(-1);
            }
            
            try {
            	YesNoVariable var = new YesNoVariable(varNames[20], st[20]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[20] + " \"" + st[20] + "\"");
            	System.exit(-1);
            }
            
            try {
            	DateVariable var = new DateVariable(varNames[21], st[21], false);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[21] + " \"" + st[21] + "\"");
            	System.exit(-1);
            }
            
            try {
            	DateVariable var = new DateVariable(varNames[22], st[22], true);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[22] + " \"" + st[22] + "\"");
            	System.exit(-1);
            }
            
            try {
            	StringVariable var = new StringVariable(varNames[23], st[23]);
            	varList.add(var);
            }
            catch (Exception ex) {
            	logger.error("Could not process variable " + varNames[23] + " \"" + st[23] + "\"");
            	System.exit(-1);
            }
            
            rawData.add(varList);
            features.add(new LinkedList<Feature>());
        }
        br.close();
	}
	
	public void calculateDiscount() throws Exception{
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			float discount = 0.0f;
			float initialPrice = -1.0f;
			boolean initialPriceFound = false;
			float priceAfterDiscount = -1.0f;
			boolean priceAfterDiscountFound = false;
			for (Variable var : dealRawData) {
				if (! var.getName().equals("InitialPrice") &&
						! var.getName().equals("PriceAfterDiscount")) {
					continue;
				}
				if (var.getName().equals("InitialPrice")) {
					FloatVariable floatVar = (FloatVariable)var;
					initialPrice = floatVar.getValue();
					initialPriceFound = true;
				}
				else if (var.getName().equals("PriceAfterDiscount")) {
					FloatVariable floatVar = (FloatVariable)var;
					priceAfterDiscount = floatVar.getValue();
					priceAfterDiscountFound = true;
				}
				
				if (initialPriceFound && priceAfterDiscountFound) {
					if (initialPrice == 0.0f) {
						logger.error("Zero initial price for line " + dealNumber);
						throw new Exception();
					}
					if (initialPrice < 0.0f) {
						logger.error("Negative initial price for line " + dealNumber);
						throw new Exception();
					}
					if (priceAfterDiscount == 0.0f) {
						logger.error("Zero price after discount for line " + dealNumber);
						throw new Exception();
					}
					if (priceAfterDiscount < 0.0f) {
						logger.error("Negative price after discount for line " + dealNumber);
						throw new Exception();
					}
					discount = (initialPrice - priceAfterDiscount) / initialPrice;
					discount = Math.round((double)discount * 100) / 100.0f;
					break;
				}
			}
			if (discount == 0.0f) {
				logger.error("Zero discount for line " + dealNumber);
				throw new Exception();
			}
			if (discount < 0.0f) {
				logger.error("Negative discount for line " + dealNumber);
				throw new Exception();
			}
			
			Feature discountFeature = new NumericFeature(discount, "Discount");
			List<Feature> featList = features.get(dealNumber);
			if (featList == null) {
				featList = new LinkedList<Feature>();
				features.add(dealNumber, featList);
			}
			featList.add(discountFeature);
			
			dealNumber ++;
		}
		
	}
	
	public void calculateDealDays() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			long fromCouponDateMs = 0;
			long toCouponDateInMs = 0;
			float numDays = 0.0f;
			boolean fromCouponDateMsFound = false;
			boolean toCouponDateMsFound = false;
			for (Variable var : dealRawData) {
				if (! var.getName().equals("FromCouponDate") &&
						! var.getName().equals("ToCouponDate")) {
					continue;
				}
				if (var.getName().equals("FromCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					fromCouponDateMs = dateVar.getDate().getTime();
					fromCouponDateMsFound = true;
				}
				else if (var.getName().equals("ToCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					toCouponDateInMs = dateVar.getDate().getTime();
					toCouponDateMsFound = true;
				}
				
				if (fromCouponDateMsFound && toCouponDateMsFound) {
					numDays = (float)(toCouponDateInMs - fromCouponDateMs) / (float)(1000 * 60 * 60 * 24);
					numDays = Math.round((double)numDays * 100) / 100.0f;
					if (numDays <= 0.0f) {
						Date startDay = new Date(fromCouponDateMs);
						Date lastDay = new Date(toCouponDateInMs);
						logger.error("Invalid number of deal days (" + numDays + ") START: " + startDay.toString() 
								+ " END: " + lastDay.toString() + " for line " + dealNumber);
						throw new Exception();
					}
					break;
				}
			}
			if (numDays <= 0.0f) {
				Date startDay = new Date(fromCouponDateMs);
				Date lastDay = new Date(toCouponDateInMs);
				logger.error("Invalid number of deal days (" + numDays + ") START: " + startDay.toString() 
						+ " END: " + lastDay.toString() + " for line " + dealNumber);
				throw new Exception();
			}
			Feature numDaysFeature = new NumericFeature(numDays, "NumberOfDaysDeal");
			List<Feature> featList = features.get(dealNumber);
			if (featList == null) {
				featList = new LinkedList<Feature>();
				features.add(dealNumber, featList);
			}
			featList.add(numDaysFeature);
			dealNumber ++;
		}
	}
	
	public void calculateCouponDuration() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			long fromDealDateMs = 0;
			long toDealDateInMs = 0;
			float numDays = 0.0f;
			boolean fromDealDateMsFound = false;
			boolean toDealDateMsFound = false;
			for (Variable var : dealRawData) {
				if (! var.getName().equals("DealStartDate") &&
						! var.getName().equals("DealEndDate")) {
					continue;
				}
				if (var.getName().equals("DealStartDate")) {
					DateVariable dateVar = (DateVariable)var;
					fromDealDateMs = dateVar.getDate().getTime();
					fromDealDateMsFound = true;
				}
				else if (var.getName().equals("DealEndDate")) {
					DateVariable dateVar = (DateVariable)var;
					toDealDateInMs = dateVar.getDate().getTime();
					toDealDateMsFound = true;
				}
				
				if (fromDealDateMsFound && toDealDateMsFound) {
					numDays = (float)(toDealDateInMs - fromDealDateMs) / (float)(1000 * 60 * 60 * 24);
					numDays = Math.round((double)numDays * 100) / 100.0f;
					if (numDays <= 0.0f) {
						Date startDay = new Date(fromDealDateMs);
						Date lastDay = new Date(toDealDateInMs);
						logger.error("Invalid number of deal days (" + numDays + ") START: " + startDay.toString() 
								+ " END: " + lastDay.toString() + " for line " + dealNumber);
						throw new Exception();
					}
					break;
				}
			}
			if (numDays <= 0.0f) {
				Date startDay = new Date(fromDealDateMs);
				Date lastDay = new Date(toDealDateInMs);
				logger.error("Invalid number of deal days (" + numDays + ") START: " + startDay.toString() 
						+ " END: " + lastDay.toString() + " for line " + dealNumber);
				throw new Exception();
			}
			Feature numDaysFeature = new NumericFeature(numDays, "NumberOfDaysCoupon");
			List<Feature> featList = features.get(dealNumber);
			if (featList == null) {
				featList = new LinkedList<Feature>();
				features.add(dealNumber, featList);
			}
			featList.add(numDaysFeature);
			dealNumber ++;
		}
	}
	
	/**
	 * This is 1 if deal has at least one day in a weekend and 0 otherwise
	 * @throws Exception
	 */
	// If it ends on a Saturday or Sunday 23:59:59 then TRUE
	// If it starts on a Saturday or Sunday 00:00:00 then TRUE
	public void isDealInWeekend() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			boolean isDealInWeekend = false;
			for (Variable var : dealRawData) {
				if (! var.getName().equals("FromCouponDate") && 
						! var.getName().equals("ToCouponDate")) {
					continue;
				}
				
				if (var.getName().equals("FromCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					Date startDate = dateVar.getDate();
					if (startDate.getDay() == 0 || startDate.getDay() == 6) {
						isDealInWeekend = true;
						break;
					}
				}
				else if (var.getName().equals("ToCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					Date endDate = dateVar.getDate();
					if (endDate.getDay() == 0 || endDate.getDay() == 6) {
						isDealInWeekend = true;
						break;
					}
				}
			}
			
			Feature isDealInWeekendFeature = isDealInWeekend ? new NumericFeature(BINARY_VARIABLE_POSITIVE, "IsDealInWeekend") :
															   new NumericFeature(BINARY_VARIABLE_NEGATIVE, "IsDealInWeekend");
			List<Feature> featList = features.get(dealNumber);
			if (featList == null) {
				featList = new LinkedList<Feature>();
				features.add(dealNumber, featList);
			}
			featList.add(isDealInWeekendFeature);
			dealNumber ++;
		}
	}
	
	public void getDealPrice() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			float priceAfterDiscount = 0.0f;
			for (Variable var : dealRawData) {
				if (! var.getName().equals("PriceAfterDiscount") ) {
					continue;
				}
				
				if (var.getName().equals("PriceAfterDiscount")) {
					FloatVariable floatVar = (FloatVariable)var;
					priceAfterDiscount = floatVar.getValue();
					break;
				}
			}
			
			Feature priceAfterDiscountFeature = new NumericFeature(priceAfterDiscount, "PriceAfterDiscount");
			List<Feature> featList = features.get(dealNumber);
			if (featList == null) {
				featList = new LinkedList<Feature>();
				features.add(dealNumber, featList);
			}
			featList.add(priceAfterDiscountFeature);
			dealNumber ++;
		}
	}
	
	
	public void getCategoryNominal() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("Category")) {
					continue;
				}
				
				NonNegativeIntegerVariable intVar = (NonNegativeIntegerVariable)var;
				int indx = intVar.getValue();
				Feature categoryFeature = new NominalFeature(FeatureBuilder.categoryNames,(short)indx, "Category");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(categoryFeature);
				dealNumber ++;
			}
			
			
		}
	}
	
	public void hasMultipleStores() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("MultipleStores")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature hasMultipleStoresFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasMultipleStores") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasMultipleStores");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(hasMultipleStoresFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void isPopularBrand() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("PopularBrand")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature isPopularBrandFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "IsPopularBrand") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "IsPopularBrand");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(isPopularBrandFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void isValidForEveryDay() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("ValidForEveryDay")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature isPopularBrandFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "IsValidForEveryDay") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "IsValidForEveryDay");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(isPopularBrandFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void isValidForEveryService() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("ValidForEveryService")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature isPopularBrandFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "IsValidForEveryService") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "IsValidForEveryService");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(isPopularBrandFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void needsPhoneReservation() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("PhoneReservation")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature isPopularBrandFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "NeedsPhoneReservation") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "NeedsPhoneReservation");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(isPopularBrandFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void hasLatinName() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("LatinName")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature hasLatinNameFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasLatinName") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasLatinName");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(hasLatinNameFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void hasVideo() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("Video")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature hasVideoFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasVideo") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasVideo");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(hasVideoFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void hasBottomImage() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("BottomImage")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature hasBottomImageFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "HasBottomImage") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "HasBottomImage");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(hasBottomImageFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void isSideDeal() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("SideDeal")) {
					continue;
				}
				
				YesNoVariable ynvar = (YesNoVariable)var;
				Feature isSideDealFeature = ynvar.getValue().toString().equalsIgnoreCase("yes") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "IsSideDeal") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "IsSideDeal");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(isSideDealFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	public void isAthensOrThessaloniki() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("Location")) {
					continue;
				}
				
				StringVariable svar = (StringVariable)var;
				Feature isAthensOrThessalonikiFeature = svar.getValue().toString().equalsIgnoreCase("Athens") ? 
						new NumericFeature(BINARY_VARIABLE_POSITIVE, "IsLocationAthens") :
					    new NumericFeature(BINARY_VARIABLE_NEGATIVE, "IsLocationAthens");
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				featList.add(isAthensOrThessalonikiFeature);
				
				break;
			}
			dealNumber ++;
		}
	}
	
	/**
	 * Output nominal variable based on discretized values
	 * @throws Exception
	 */
	public void getMaxNumCoupons() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("MaxCouponsPerPerson")) {
					continue;
				}
				
				NonNegativeIntegerVariable intVar = (NonNegativeIntegerVariable)var;
				int indx = intVar.getValue();
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				
				int numCouponsCategory = 0;
				if (indx == 1) {
					numCouponsCategory = 1;
				}
				else if (indx == 2) {
					numCouponsCategory = 2;
				}
				else if (indx == 3 || indx == 4) {
					numCouponsCategory = 3;
				}
				else if (indx <= 10) {
					numCouponsCategory = 4;
				}
				else {
					numCouponsCategory = 5;
				}
				for (int i=1; i <= 5; i++) {
					String name = "MaxNumCouponsBinary" + Integer.toString(i);
					Feature categoryFeature = i == numCouponsCategory ? new NumericFeature(BINARY_VARIABLE_POSITIVE, name) : 
						                                                new NumericFeature(BINARY_VARIABLE_NEGATIVE, name);
					
					featList.add(categoryFeature);
				}
			}
			dealNumber ++;
		}
	}
	
	public void getCategory() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			for (Variable var : dealRawData) {
				if (! var.getName().equals("Category")) {
					continue;
				}
				
				StringVariable intVar = (StringVariable)var;
				String indx = intVar.getValue();
				List<Feature> featList = features.get(dealNumber);
				if (featList == null) {
					featList = new LinkedList<Feature>();
					features.add(dealNumber, featList);
				}
				for (int i=0; i < FeatureBuilder.categoryNames.length; i++) {
					String name = "CategoryBinary" + Integer.toString(i);
					Feature categoryFeature = FeatureBuilder.categoryNames[i].equals(indx) ? new NumericFeature(BINARY_VARIABLE_POSITIVE, name) : 
						                                  new NumericFeature(BINARY_VARIABLE_NEGATIVE, name);
					
					featList.add(categoryFeature);
				}
			}
			dealNumber ++;
		}
	}
	
	public void calculateRevenuePerDay() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			float priceAfterDiscount = 0.0f;
			int numCoupons = 0;
			int minNumCoupons = 0;
			float revenue = 0.0f;
			long fromCouponDateMs = 0;
			long toCouponDateInMs = 0;
			float numDays = 0.0f;
			boolean fromCouponDateMsFound = false;
			boolean toCouponDateMsFound = false;
			boolean priceAfterDiscountFound = false;
			boolean numCouponsFound = false;
			boolean minNumCouponsFound = false;
			for (Variable var : dealRawData) {
				if (! var.getName().equals("NumberOfCoupons") &&
						! var.getName().equals("PriceAfterDiscount") &&
						! var.getName().equals("FromCouponDate") &&
						! var.getName().equals("ToCouponDate") &&
						! var.getName().equals("MinCoupons")) {
					continue;
				}
				
				if (var.getName().equals("NumberOfCoupons")) {
					NonNegativeIntegerVariable intVar = (NonNegativeIntegerVariable)var;
					numCoupons = intVar.getValue();
					numCouponsFound = true;
				}
				else if (var.getName().equals("PriceAfterDiscount")) {
					FloatVariable floatVar = (FloatVariable)var;
					priceAfterDiscount = floatVar.getValue();
					priceAfterDiscountFound = true;
				}
				else if (var.getName().equals("FromCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					fromCouponDateMs = dateVar.getDate().getTime();
					fromCouponDateMsFound = true;
				}
				else if (var.getName().equals("ToCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					toCouponDateInMs = dateVar.getDate().getTime();
					toCouponDateMsFound = true;
				}
				else if (var.getName().equals("MinCoupons")) {
					NonNegativeIntegerVariable intVar = (NonNegativeIntegerVariable)var;
					minNumCoupons = intVar.getValue();
					minNumCouponsFound = true;
				}
				
				
				if (numCouponsFound && priceAfterDiscountFound &&
						fromCouponDateMsFound && toCouponDateMsFound && minNumCouponsFound) {
					if (numCoupons < minNumCoupons) {
						revenue = 0.0f;
						break;
					}
					numDays = (float)(toCouponDateInMs - fromCouponDateMs) / (float)(1000 * 60 * 60 * 24);
					if (numDays <= 0.0f) {
						Date startDay = new Date(fromCouponDateMs);
						Date lastDay = new Date(toCouponDateInMs);
						logger.error("Invalid number of deal days (" + numDays + ") START: " + startDay.toString() 
								+ " END: " + lastDay.toString() + " for line " + dealNumber);
						throw new Exception();
					}
					revenue = (float)numCoupons * priceAfterDiscount;
					if (revenue < 0.0f) {
						logger.error("Negative revenue for line " + dealNumber + " NUM_COUPONS: " + 
								numCoupons + " PRICE_AFTER_DISCOUNT: " + priceAfterDiscount);
						throw new Exception();
					}
					revenue /= numDays;
					revenue = Math.round((double)revenue * 100) / 100.0f;
					break;
				}
			}
			if (revenue < 0.0f) {
				logger.error("Negative revenue for line " + dealNumber + " NUM_COUPONS: " + 
						numCoupons + " PRICE_AFTER_DISCOUNT: " + priceAfterDiscount);
				throw new Exception();
			}
			Feature revenuePerDayFeature = new NumericFeature(revenue, "RevenuePerDay");
			List<Feature> featList = features.get(dealNumber);
			if (featList == null) {
				featList = new LinkedList<Feature>();
				features.add(dealNumber, featList);
			}
			featList.add(revenuePerDayFeature);
			dealNumber ++;
		}
	}
	
	public void calculateNumberOfCouponsPerDay() throws Exception {
		int dealNumber = 0;
		for (List<Variable> dealRawData : rawData) {
			int numCoupons = 0;
			int minNumCoupons = 0;
			float numCouponsPerDay = 0.0f;
			long fromCouponDateMs = 0;
			long toCouponDateInMs = 0;
			float numDays = 0.0f;
			boolean fromCouponDateMsFound = false;
			boolean toCouponDateMsFound = false;
			boolean numCouponsFound = false;
			boolean minNumCouponsFound = false;
			for (Variable var : dealRawData) {
				if (! var.getName().equals("NumberOfCoupons") &&
						! var.getName().equals("FromCouponDate") &&
						! var.getName().equals("ToCouponDate") &&
						! var.getName().equals("MinCoupons")) {
					continue;
				}
				
				if (var.getName().equals("NumberOfCoupons")) {
					NonNegativeIntegerVariable intVar = (NonNegativeIntegerVariable)var;
					numCoupons = intVar.getValue();
					numCouponsFound = true;
				}
				else if (var.getName().equals("FromCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					fromCouponDateMs = dateVar.getDate().getTime();
					fromCouponDateMsFound = true;
				}
				else if (var.getName().equals("ToCouponDate")) {
					DateVariable dateVar = (DateVariable)var;
					toCouponDateInMs = dateVar.getDate().getTime();
					toCouponDateMsFound = true;
				}
				else if (var.getName().equals("MinCoupons")) {
					NonNegativeIntegerVariable intVar = (NonNegativeIntegerVariable)var;
					minNumCoupons = intVar.getValue();
					minNumCouponsFound = true;
				}
				
				
				if (numCouponsFound &&
						fromCouponDateMsFound && toCouponDateMsFound && minNumCouponsFound) {
					if (numCoupons < minNumCoupons) {
						numCouponsPerDay = 0.0f;
						break;
					}
					numDays = (float)(toCouponDateInMs - fromCouponDateMs) / (float)(1000 * 60 * 60 * 24);
					if (numDays <= 0.0f) {
						Date startDay = new Date(fromCouponDateMs);
						Date lastDay = new Date(toCouponDateInMs);
						logger.error("Invalid number of deal days (" + numDays + ") START: " + startDay.toString() 
								+ " END: " + lastDay.toString() + " for line " + dealNumber);
						throw new Exception();
					}
					numCouponsPerDay = (float)numCoupons;
					if (numCouponsPerDay < 0.0f) {
						logger.error("Negative numCouponsPerDay for line " + dealNumber + " NUM_COUPONS: " + 
								numCoupons);
						throw new Exception();
					}
					numCouponsPerDay /= numDays;
					numCouponsPerDay = Math.round((double)numCouponsPerDay * 100) / 100.0f;
					break;
				}
			}
			if (numCouponsPerDay < 0.0f) {
				logger.error("Negative revenue for line " + dealNumber + " NUM_COUPONS: " + 
						numCoupons);
				throw new Exception();
			}
			Feature revenuePerDayFeature = new NumericFeature(numCouponsPerDay, "NumCouponsPerDay");
			List<Feature> featList = features.get(dealNumber);
			if (featList == null) {
				featList = new LinkedList<Feature>();
				features.add(dealNumber, featList);
			}
			featList.add(revenuePerDayFeature);
			dealNumber ++;
		}
	}
	
	public void writeFeatures(File file) {
    	try {
    		PrintWriter out = new PrintWriter(new FileWriter(file));
    		out.println("@RELATION RevenuePerDay" + newline);
    		
    		// Write the @ATTRIBUTES part
    		for (Feature f : features.get(0)) {
    			StringBuffer sb = new StringBuffer();
    			sb.append("@ATTRIBUTE ");
    			sb.append(f.getName()); sb.append(" ");
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
        FeatureBuilder fb = new FeatureBuilder();
        try {
        	fb.readRawData(new File("c:\\code\\dealEstimator\\GoldenDeals.csv"));
        }
        catch (IOException ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.getDealPrice();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.calculateDiscount();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        
        try {
        	fb.calculateDealDays();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.calculateCouponDuration();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.isDealInWeekend();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.hasMultipleStores();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.isSideDeal();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.isPopularBrand();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.needsPhoneReservation();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.isValidForEveryDay();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.isValidForEveryService();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.hasLatinName();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.hasVideo();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.hasBottomImage();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.isAthensOrThessaloniki();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.getMaxNumCoupons();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        try {
        	fb.getCategory();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        
        
        try {
        	fb.calculateRevenuePerDay();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        /*
        try {
        	fb.calculateNumberOfCouponsPerDay();
        }
        catch (Exception ex) {
        	System.exit(-1);
        }
        */
        fb.writeFeatures(new File("c:\\code\\dealEstimator\\features.arff"));
        
    }
    
}
