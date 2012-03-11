package com.goldenDeal.goldenDeals.dealEstimator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.List;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deal {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static final Locale locale = new Locale("el", "GR"); 
	private final String SITE = "Provider";
	private final String INITIAL_PRICE = "InitialPrice";
	private final String PRICE_AFTER_DISCOUNT = "PriceAfterDiscount";
	private final String NUMBER_OF_COUPONS = "NumberOfCoupons";
	private final String AMOUNT_PER_DAY = "AmountPerDay";
	private final String BUSINESS_NAME = "NameOfBusiness";
	private final String DISCOUNT = "Discount";
	private final String FROM_COUPON_DATE = "FromCouponDate";
	private final String TO_COUPON_DATE = "ToCouponDate";
	private final String MORE_TO_PAY = "MoreToPay";
	private final String COUPON_DURATION = "CouponDuration";
	private final String QUANTIZED_COUPON_DURATION = "RoundedCouponDuration";
	private final String DEAL_DURATION = "DealDuration";
	private final String CATEGORY = "Category";
	private final String KEYWORDS = "Keywords";
	private final String GROUPON_CATEGORY = "GrouponCategory";
	private final String GROUPON_SUBCATEGORY = "GrouponSubCategory";
	private final String VISITS = "NumberOfVisits";
	private final String LOCATION = "MappedLocation";
	private final String MAX_COUPONS_PER_PERSON = "MaxCouponsPerPerson";
	private final String MIN_COUPONS = "MinCoupons";
	private final String PROMINENT_IN_NEWSLETTER = "ProminentInNewsletter";
	private final String EXTRA_DEAL = "ExtraDeal";
	private final String REQUIRES_PHYSICAL_VISIT = "RequiresPhysicalVisit";
	private final String MULTIPLE_STORES = "MultipleStores";
	private final String LATIN_NAME = "LatinName";
	private final String POPULAR_BRAND = "PopularBrand";
//	private final String HAS_VIDEO = "Video";
//	private final String HAS_BOTTOM_IMAGE = "BottomImage";
	private final String REQUIRES_PHONE_RESERVATION = "PhoneReservation";
	private final String VALID_FOR_SATURDAYS = "ValidForSaturdays";
	private final String VALID_FOR_SUNDAYS = "ValidForSundays";
	private final String VALID_FOR_WEEKDAYS = "ValidForEveryWeekday";
	private final String VALID_WITHOUT_EXCEPTIONS = "ValidWithoutTimeExceptions";
	private final String ONE_PERSON_COUPON = "OnePersonCoupon";
	private final String EXTRA_DISCOUNTS = "ExtraDiscounts";
	private final String COMBO_DEAL = "ComboDeal";
	private final String HAS_OPTIONS = "Has Options";
	private final String WEEKEND_DEAL = "WeekendDeal";
	private final String MULTIPLE_PRICES = "MultiplePrices";
	private final String GEOGRAPHICAL_AREA = "GeographicalArea";
	private final String DAYS_FOR_ACTIVATION = "DaysForActivation";
	private final String QUANTIZED_DAYS_FOR_ACTIVATION = "QuantizedDaysForActivation";
	private final String MONTH_YEAR = "MonthYear";
	private final String LUXURY_HOTEL = "LuxuryHotel";
	private final String VALID_FOR_NEXT_MAJOR_VACATION = "ValidForNextMajorVacations";
	private final String NUMBER_OF_NIGHTS = "NumberOfNightsInHotel";
	private final String ID = "ID";
	private float initialPrice;
	private float priceAfterDiscount;
	private float amountPerDay;
	private float discount;
	private float rank;
	private float lastRankOfSameMerchant;
	private Date fromCouponDate;
	private Date toCouponDate;
	private int numberOfCoupons;
	private int numberOfNightsInHotel;
	private int couponDuration;
	private String quantizedCouponDuration;
	private int dealDuration;
	public static final String[] categoryNames = {"Tasting", "Beauty", 
		  "Outdoor Activities", "Gym / Exercise / Fit", "Family Activities / Kid", "Leisure",
		  "Education / Learning", "Products"}; 
	public static final String[] grouponCategoryNames = {"ArtsandEntertainment", "Automotive", "BeautySpas",
		"Education", "FinancialServices", "FoodDrink", "HealthFitness", "HomeServices", "LegalServices", 
		"Nightlife", "Pets", "ProfessionalServices", "PublicServicesGovernment", "Real Estate", 
		"ReligiousOrganizations", "Restaurants", "Shopping", "Travel"};
	public static final String[] numberOfVisits = {"Single", "Multiple", "Unlimited"};
	public static final String[] locs = {"Athens", "Thessaloniki", "Special Deal"};
	public static final String[] bin = {"Yes", "No"};
	public static final String[] triStates = {"Yes", "No", "Don't know"};
	public static final String[] daysForActivation = {"0", "1", "2", "3", "4", "5"};
	public static final String[] couponDurationGroups = {"0", "1", "2", "3", "4"};
	public static final String[] geographicalAreasNames = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
	private String site;
	private String category;
	private String grouponCategory;
	private String grouponSubCategory;
	private String numVisits;
	private String location;
	private boolean prominentInNewsletter;
	private boolean isExtraDeal;
	private boolean requiresPhysicalVisit;
	private boolean hasMultipleStores;
	private boolean hasLatinName;
	private boolean isPopularBrand;
	private int maxCouponsPerPerson;
	private int minCoupons;
	private int daysBeforeActivation;
	private int numDaysSinceLastSimilarDeal;
	private String quantizedDaysBeforeActivation;
	private String greatDeal;
	private boolean hasCompetingDeal;
	private boolean hasVideo;
	private boolean hasBottomImage;
	private boolean hasMoreToPay;
	private boolean hasMultiplePrices;
	private boolean requiresPhoneReservation;
	private boolean validForSaturdays;
	private boolean validForSundays;
	private boolean validForEveryWeekday;
	private boolean validWithoutTimeExceptions;
	private boolean isOnePersonCoupon;
	private boolean hasExtraDiscounts;
	private boolean isComboDeal;
	private boolean hasOptions;
	private boolean isWeekendDeal;
	private boolean hasDemand;
	private boolean hasLongerWait;
	private String validForNextMajorVacation;
	private String luxuryHotel;
	private String month;
	private String businessName;
	private int numberOfActiveDeals;
	private String id;
	private List<String> geographicalAreas;
	private List<String> keywords;
	
	
	public Deal(String logEntryLine, HashMap<String, Integer> names2Index) {
		HashSet<String> categories = new HashSet<String>(Arrays.asList(categoryNames));
		HashSet<String> grouponCategories = new HashSet<String>(Arrays.asList(grouponCategoryNames));
		HashSet<String> visits = new HashSet<String>(Arrays.asList(numberOfVisits));
		HashSet<String> locations = new HashSet<String>(Arrays.asList(locs));
		HashSet<String> binary = new HashSet<String>(Arrays.asList(bin));
		NumberFormat doubleNumberFormat = NumberFormat.getNumberInstance(locale); 
		doubleNumberFormat.setMaximumFractionDigits(2);
		doubleNumberFormat.setMinimumFractionDigits(0);
		
		String[] fields = logEntryLine.split(";");
		
		initialPrice = 0.0f;
		String fld = INITIAL_PRICE;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	initialPrice = doubleNumberFormat.parse(fields[names2Index.get(fld)]).floatValue();
        }
        catch (ParseException ex) {
        	logger.error("Could not parse field \"" + fld + "\" value : \"" + fields[names2Index.get(fld)] + "\"");
        	System.exit(-1);
        }
        
        priceAfterDiscount = 0.0f;
        fld = PRICE_AFTER_DISCOUNT;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	priceAfterDiscount = doubleNumberFormat.parse(fields[names2Index.get(fld)]).floatValue();
        }
        catch (ParseException ex) {
        	logger.error("Could not parse \"" + fields[names2Index.get(fld)] + "\" for deal \"" + logEntryLine + "\"");
        	System.exit(-1);
        }
        
        amountPerDay = 0.0f;
        fld = AMOUNT_PER_DAY;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	amountPerDay = doubleNumberFormat.parse(fields[names2Index.get(fld)]).floatValue();
        }
        catch (ParseException ex) {
        	logger.error("Could not parse \"" + fields[names2Index.get(fld)] + "\" for deal \"" + logEntryLine + "\"");
        	System.exit(-1);
        }
        
        discount = 0.0f;
        fld = DISCOUNT;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	discount = doubleNumberFormat.parse(fields[names2Index.get(fld)]).floatValue();
        }
        catch (ParseException ex) {
        	logger.error("Could not parse field \"" + fld + "\" value : \"" + fields[names2Index.get(fld)] + "\"");
        	System.exit(-1);
        }
        
        fromCouponDate = new Date();
        fld = FROM_COUPON_DATE;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        SimpleDateFormat format =
            new SimpleDateFormat("d/M/yy H:m");

		String dateString = fields[names2Index.get(fld)];
        try {
        	fromCouponDate = format.parse(dateString);
        }
        catch(ParseException pe) {
            logger.error("Cannot parse date \"" + dateString + "\"");
            System.exit(-1);
        }
        
        toCouponDate = new Date();
        fld = TO_COUPON_DATE;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}

		dateString = fields[names2Index.get(fld)];
        try {
        	toCouponDate = format.parse(dateString);
        }
        catch(ParseException pe) {
            logger.error("Cannot parse date \"" + dateString + "\"");
            System.exit(-1);
        }
        
        numberOfCoupons = 0;
        fld = NUMBER_OF_COUPONS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	numberOfCoupons = Integer.parseInt((fields[names2Index.get(fld)]));
        }
        catch (Exception ex) {
        	logger.error("Could not parse \"" + fields[names2Index.get(fld)] + "\" for deal \"" + logEntryLine + "\" ");
        	System.exit(-1);
        }
        
        numberOfNightsInHotel = 0;
        fld = NUMBER_OF_NIGHTS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	numberOfNightsInHotel = Integer.parseInt((fields[names2Index.get(fld)]));
        }
        catch (Exception ex) {
//        	logger.error("Could not parse \"" + fields[names2Index.get(fld)] + "\" for deal \"" + logEntryLine + "\" ");
//        	System.exit(-1);
        	numberOfNightsInHotel = 0;
        }
        
        couponDuration = 0;
        fld = COUPON_DURATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	couponDuration = Integer.parseInt((fields[names2Index.get(fld)]));
        }
        catch (Exception ex) {
        	logger.error("Could not parse field \"" + fld + "\" value : \"" + fields[names2Index.get(fld)] + "\"");
  //      	System.exit(-1);
        }
        
        fld = QUANTIZED_COUPON_DURATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		quantizedCouponDuration = fields[names2Index.get(fld)];
        
        
        dealDuration = 0;
        fld = DEAL_DURATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	dealDuration = Integer.parseInt((fields[names2Index.get(fld)]));
        }
        catch (Exception ex) {
        	logger.error("Could not parse field \"" + fld + "\" value : \"" + fields[names2Index.get(fld)] + "\" at deal \"" + logEntryLine + "\"");
        	System.exit(-1);
        }
        String tmp;
        
        /*
        fld = CATEGORY;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! categories.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			category = tmp;
		}
		*/
        
		fld = GROUPON_CATEGORY;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! grouponCategories.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp + " at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		else {
			grouponCategory = tmp;
		}
		
		fld = GROUPON_SUBCATEGORY;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		grouponSubCategory = fields[names2Index.get(fld)];
		
		fld = BUSINESS_NAME;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		businessName = fields[names2Index.get(fld)];
			
		
		keywords = new LinkedList<String>();
		fld = KEYWORDS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		for (String keyword : tmp.split(",")) {
			keywords.add(keyword);
		}
		
		
		fld = VISITS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! visits.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			numVisits = tmp;
		}
		
		fld = LOCATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! locations.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			location = tmp;
		}
		
		maxCouponsPerPerson = 0;
        fld = MAX_COUPONS_PER_PERSON;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	maxCouponsPerPerson = Integer.parseInt((fields[names2Index.get(fld)]));
        }
        catch (Exception ex) {
        	logger.error("Could not parse \"" + fields[names2Index.get(fld)] + "\" for deal \"" + logEntryLine + "\" ");
        	System.exit(-1);
        }
        
        minCoupons = 0;
        fld = MIN_COUPONS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	minCoupons = Integer.parseInt((fields[names2Index.get(fld)]));
        }
        catch (Exception ex) {
        	logger.error("Could not parse \"" + fields[names2Index.get(fld)] + "\" for deal \"" + logEntryLine + "\"");
        	System.exit(-1);
        }
        
        fld = MORE_TO_PAY;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasMoreToPay = tmp.equals("Yes") ? true : false;
		}
		
		fld = MULTIPLE_PRICES;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasMultiplePrices = tmp.equals("Yes") ? true : false;
		}
		
		fld = LUXURY_HOTEL;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (tmp.equals("Yes") || tmp.equals("No")) {
			luxuryHotel = tmp;
		}
		else {
			luxuryHotel = "Don't know";
		}
		
		fld = VALID_FOR_NEXT_MAJOR_VACATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (tmp.equals("Yes") || tmp.equals("No")) {
			validForNextMajorVacation = tmp;
		}
		else {
			validForNextMajorVacation = "Don't know";
		}
		
        fld = PROMINENT_IN_NEWSLETTER;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			prominentInNewsletter = tmp.equals("Yes") ? true : false;
		}
		
		fld = EXTRA_DEAL;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			isExtraDeal = tmp.equals("Yes") ? true : false;
		}
		
		fld = REQUIRES_PHYSICAL_VISIT;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			requiresPhysicalVisit = tmp.equals("Yes") ? true : false;
		}
		
		fld = MULTIPLE_STORES;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasMultipleStores = tmp.equals("Yes") ? true : false;
		}
		
		fld = LATIN_NAME;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasLatinName = tmp.equals("Yes") ? true : false;
		}
		
		fld = POPULAR_BRAND;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			isPopularBrand = tmp.equals("Yes") ? true : false;
		}
/*		
		fld = HAS_VIDEO;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasVideo = tmp.equals("Yes") ? true : false;
		}
		
		fld = HAS_BOTTOM_IMAGE;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasBottomImage = tmp.equals("Yes") ? true : false;
		}
	*/	
		fld = REQUIRES_PHONE_RESERVATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			requiresPhoneReservation = tmp.equals("Yes") ? true : false;
		}
		
		fld = VALID_FOR_SATURDAYS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			validForSaturdays = tmp.equals("Yes") ? true : false;
		}
		
		fld = VALID_FOR_SUNDAYS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			validForSundays = tmp.equals("Yes") ? true : false;
		}
		
		fld = VALID_FOR_WEEKDAYS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			validForEveryWeekday = tmp.equals("Yes") ? true : false;
		}
		
		fld = VALID_WITHOUT_EXCEPTIONS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			validWithoutTimeExceptions = tmp.equals("Yes") ? true : false;
		}
		
		fld = ONE_PERSON_COUPON;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			isOnePersonCoupon = tmp.equals("Yes") ? true : false;
		}
		
		fld = EXTRA_DISCOUNTS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasExtraDiscounts = tmp.equals("Yes") ? true : false;
		}
		
		fld = COMBO_DEAL;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			isComboDeal = tmp.equals("Yes") ? true : false;
		}
		
		fld = HAS_OPTIONS;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			hasOptions = tmp.equals("Yes") ? true : false;
		}
		
		fld = WEEKEND_DEAL;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		if (! binary.contains(tmp)) {
			logger.error("Invalid value for field " + fld + " : " + tmp);
			System.exit(-1);
		}
		else {
			isWeekendDeal = tmp.equals("Yes") ? true : false;
		}
		
		fld = MONTH_YEAR;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		month = fields[names2Index.get(fld)];
		
		
		fld = ID;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		id = tmp;
		
		geographicalAreas = new LinkedList<String>();
		fld = GEOGRAPHICAL_AREA;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		for (String geo : tmp.split(",")) {
			geographicalAreas.add(geo.trim());
		}
		
		daysBeforeActivation = 0;
        fld = DAYS_FOR_ACTIVATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
        try {
        	daysBeforeActivation = Integer.parseInt((fields[names2Index.get(fld)]));
        }
        catch (Exception ex) {
        	logger.warn("Could not parse field \"" + fld + " value : \"" + fields[names2Index.get(fld)] + "\" ... skipping");
 //       	System.exit(-1);
        }
        
        fld = QUANTIZED_DAYS_FOR_ACTIVATION;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		quantizedDaysBeforeActivation = fields[names2Index.get(fld)];
        
        
        fld = SITE;
		if (names2Index.get(fld) == null) {
			logger.error("Could not find index of field \"" + fld + "\"");
			System.exit(-1);
		}
		if (names2Index.get(fld) >= fields.length) {
			logger.error("Could not find field \"" + fld + "\" at deal \"" + logEntryLine + "\"");
			System.exit(-1);
		}
		tmp = fields[names2Index.get(fld)];
		site = tmp;
	}
	
	public float getAmountPerDay() {
		return this.amountPerDay;
	}
	
	public float getInitialPrice() {
		return this.initialPrice;
	}
	
	public float getPriceAfterDiscount() {
		return this.priceAfterDiscount;
	}
	
	public float getDiscount() {
		return this.discount;
	}
	
	public int getNumberOfCoupons() {
		return this.numberOfCoupons;
	}
	
	public int getCouponDuration() {
		return this.couponDuration;
	}
	
	public String getQuantizedCouponDuration() {
		return this.quantizedCouponDuration;
	}
	
	public int getDealDuration() {
		return this.dealDuration;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public String getGrouponCategory() {
		return this.grouponCategory;
	}
	
	public String getGrouponSubCategory() {
		return this.grouponSubCategory;
	}
	
	public String getLocation() {
		return this.location;
	}
	
	public String getVisits() {
		return this.numVisits;
	}
	
	public int getMaxCouponsPerPerson() {
		return this.maxCouponsPerPerson;
	}
	
	public int minCoupons() {
		return this.minCoupons;
	}
	
	public boolean isProminentInNewsletter() {
		return this.prominentInNewsletter;
	}
	
	public boolean isExtraDeal() {
		return this.isExtraDeal;
	}
	
	public boolean hasMultipleStores() {
		return this.hasMultipleStores;
	}
	
	public boolean requiresPhysicalVisit() {
		return this.requiresPhysicalVisit;
	}
	
	public boolean isPopularBrand() {
		return this.isPopularBrand;
	}
	
	public boolean hasLatinName() {
		return this.hasLatinName;
	}
	
	public boolean hasVideo() {
		return this.hasVideo;
	}
	
	public boolean hasBottomImage() {
		return this.hasBottomImage;
	}
	
	public boolean hasMoreToPay() {
		return this.hasMoreToPay;
	}
	
	public boolean requiresPhoneReservation() {
		return this.requiresPhoneReservation;
	}
	
	public boolean isValidForSaturdays() {
		return this.validForSaturdays;
	}
	
	public boolean isValidForSundays() {
		return this.validForSundays;
	}
	
	public boolean isValidForWeekdays() {
		return this.validForEveryWeekday;
	}
	
	public boolean isValidWithoutTimeExceptions() {
		return this.validWithoutTimeExceptions;
	}
	
	public boolean isOnePersonCoupon() {
		return this.isOnePersonCoupon;
	}
	
	public boolean hasOptions() {
		return this.hasOptions;
	}
	
	public boolean hasExtraDiscounts() {
		return this.hasExtraDiscounts;
	}
	
	public boolean isComboDeal() {
		return this.isComboDeal;
	}
	
	public boolean hasMultiplePrices() {
		return this.hasMultiplePrices;
	}
	
	public boolean isWeekendDeal() {
		return this.isWeekendDeal;
	}
	
	public String getMonth() {
		return this.month;
	}
	
	public String getID() {
		return this.id;
	}
	
	public Date getFromDealDate() {
		return this.fromCouponDate;
	}
	
	public Date getToDealDate() {
		return this.toCouponDate;
	}
	
	public int getNumDaysBeforeActivation() {
		return this.daysBeforeActivation;
	}
	
	public String getQuantizedNumDaysBeforeActivation() {
		return this.quantizedDaysBeforeActivation;
	}
	
	public List<String> getGeographicalArea() {
		return this.geographicalAreas;
	}
	
	public List<String> getKeywords() {
		return this.keywords;
	}
	
	public String getSite() {
		return this.site;
	}
	
	public void setGreatDeal(String gd) {
		this.greatDeal = gd;
	}
	
	public String getGreatDeal() {
		return this.greatDeal;
	}
	
	public void setCompetingDeal(boolean b) {
		this.hasCompetingDeal = b;
	}
	
	public boolean hasCompetingDeal() {
		return this.hasCompetingDeal;
	}
	
	public void setNumberOfActiveDeals(int c) {
		this.numberOfActiveDeals = c;
	}
	
	public float getNumberOfActiveDeals() {
		return (float)this.numberOfActiveDeals;
	}
	
	public void setHasDemand(boolean b) {
		this.hasDemand = b;
	}
	
	public boolean hasDemand() {
		return this.hasDemand;
	}
	
	public void setRank(float r) {
		this.rank = r;
	}
	
	public float getRank() {
		return this.rank;
	}
	
	public void setNumDaysSinceLastSimilarDeal(int d) {
		this.numDaysSinceLastSimilarDeal = d;
	}
	
	public int getNumDaysSinceLastSimilarDeal() {
		return this.numDaysSinceLastSimilarDeal;
	}
	
	public boolean hasUnlimitedCouponsPerPerson() {
		return this.maxCouponsPerPerson >= 100 ? true : false;
	}
	
	public void setLongerWaitSinceSimilarDeal(boolean b) {
		this.hasLongerWait = b;
	}
	
	public boolean hasLongerWaitSinceSimilarDeal() {
		return this.hasLongerWait;
	}
	
	public String getLuxuryHotel() {
		return this.luxuryHotel;
	}
	
	public String getValidForNextMajorVacation() {
		return this.validForNextMajorVacation;
	}
	
	public int getNumberOfNightsInHotel() {
		return this.numberOfNightsInHotel;
	}
	
	public String getBusinessName() {
		return this.businessName;
	}
	
	public void setLastRankOfSameMerchant(float r) {
		this.lastRankOfSameMerchant = r;
	}
	
	public float getLastRankOfSameMerchant() {
		return this.lastRankOfSameMerchant;
	}
	
	public String toString() {
		return this.getID();
	}
}
