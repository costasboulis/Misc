package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealEstate {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public static String ITEM_TYPE = "ITEM_TYPE";
	public static String AREA_ID = "AREA_ID";
	public static String PRICE = "PRICE";
	public static String TRANSACTION_TYPE = "TRANSACTION_TYPE";
	public static String REAL_ESTATE_TYPE = "TYPE";
	public static String REAL_ESTATE_SUBTYPE = "SUBTYPE";
	public static String FLOOR = "FLOOR";
	public static String AREA = "SIZE";
	public static String STATE = "STATE";
	public static String SUBSTATE = "SUBSTATE";
	public static String CONSTRUCTION_YEAR = "CONSTRUCTION_YEAR";
	public static String IS_AGENT = "FROM_AGENT";
	public static String HAS_GARDEN = "GARDEN";
	public static String HAS_PARKING = "PARKING";
	public static String HAS_AUTONOMOUS_HEATING = "INDEPENDENT_HEATING";
	public static String HAS_NATURAL_GAS = "NATURAL_GAS";
	public static String HAS_SUN_VISORS = "SUN_VISORS";
	public static String HAS_AIR_CONDITIONING = "AIR_CONDITION";
	public static String HAS_PRIVATE_ROOF = "PRIVATE_ROOF";
	public static String HAS_SUN_BOILER = "SUN_BOILER";
	public static String HAS_STORAGE = "STORAGE";
	public static String IS_NEAR_METRO = "NEAR_METRO";
	public static String HAS_PISINA = "POOL";
	public static String HAS_VIEW = "VIEW";
	public static String PUB_YEAR = "PUB_YEAR";
	public static String PUB_MONTH = "PUB_MONTH";
	public static String ADV_ID = "ADVID";
	public static String TELEPHONE = "TELEPHONE";
	public static String BEDROOMS = "BEDROOMS";
	
	private String itemType;
	private int advID;
	private static String[] transactionTypeValues = {"SELL", "LET"};
	private HashSet<String> validTransactionTypeValues = new HashSet<String>(Arrays.asList(transactionTypeValues));
	private String transactionType;
	
	private static String[] stateValues = {"NEWBUILT", "UNDER_CONSTRUCTION", "UNFINISHED", "USED"};
	private HashSet<String> validStateValues = new HashSet<String>(Arrays.asList(stateValues));
	private String state;
	
	private static String[] substateValues = {"PERFECT", "REFURBISHED", "GOOD", "NEEDS_REPAIR"};
	private HashSet<String> validSubstateValues = new HashSet<String>(Arrays.asList(substateValues));
	private String subState;
	
	public static String[] floorValues = {"SH", "S1", "LH", "LHH", "L0", "L1", "L2", "L3", "L4", "L5", "L6", "L7", "L8"};
	private HashSet<String> validFloorValues = new HashSet<String>(Arrays.asList(floorValues));
	private List<String> floors;
	
	private static String[] realEstateTypeValues = {"APARTMENT", "HOUSE", "SPLIT_LEVEL", "œ… ≈…¡", "BUILDING", "OTHER"};
	private HashSet<String> validRealEstateTypeValues = new HashSet<String>(Arrays.asList(realEstateTypeValues));
	private String realEstateType;
	
	private static String[] realEstateSubtypeValues = {"FLOORFLAT", "PENTHOUSE", "SINGLEROOM", "LOFT"};
	private HashSet<String> validRealEstateSubtypeValues = new HashSet<String>(Arrays.asList(realEstateSubtypeValues));
	private String realEstateSubtype;
	
	private float price;
	private float size;
	private int constructionYear;
	private boolean newlyBuilt;
	private int publicationYear;
	private int publicationMonth;
	private long telephone;
	private int numBedrooms;
	
	
	private String areaID;
	private boolean hasGarden;
	private boolean hasParking;
	private boolean hasAutonomousHeating;
	private boolean hasPisina; 
	private boolean hasView;
	private boolean isAgent;
	private boolean hasSunVisors;
	private boolean hasSunBoiler;
	private boolean hasNaturalGas;
	private boolean hasAC;
	private boolean isNearMetro;
	private boolean hasPrivateRoof;
	
	private boolean incomplete;  // If true the information in RealEstate here is incomplete
	
	
	public RealEstate(String areaID, String transactionType, String realEstateType,
					  String floor, float size, boolean newlyBuilt, boolean hasParking, boolean hasGarden, float price) {
		this.price = price;
		this.newlyBuilt = newlyBuilt;
		this.size = size;
		this.realEstateType = realEstateType;
		this.transactionType = transactionType;
		this.floors = new LinkedList<String>();
		this.floors.add(floor);
		this.areaID = areaID;
		this.hasGarden = hasGarden;
	}
	
	public RealEstate(String logEntryLine, HashMap<String, Integer> names2Index) {
		String[] fields = logEntryLine.split("\\t");
		
		this.itemType = names2Index.get(ITEM_TYPE) == null ? "RE_RESIDENCE" : fields[names2Index.get(ITEM_TYPE)];
		
		this.areaID = fields[names2Index.get(AREA_ID)];
		if (areaID.equals("NULL")) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no Area_ID");
			this.incomplete = true;
//			return;
		}
		this.transactionType = fields[names2Index.get(TRANSACTION_TYPE)];
		if (!validTransactionTypeValues.contains(this.transactionType)) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no transactionType");
			this.incomplete = true;
			this.transactionType = "N/A";
//			return;
		}
		this.price = 0.0f;
		try {
			String tmp = fields[names2Index.get(PRICE)];
			this.price = Float.parseFloat(tmp);
		}
		catch (Exception ex) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no price");
			this.incomplete = true;
//			return;
		}
		if (this.price <= 0.0f) {
//			logger.debug("Ad \"" + logEntryLine + "\" has non-positive price");
			this.price = 0.0f;
			this.incomplete = true;
//			return;
		}
		
		this.realEstateType = fields[names2Index.get(REAL_ESTATE_TYPE)];
		if (!validRealEstateTypeValues.contains(this.realEstateType)) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no realestatesubtype");
			this.realEstateType = "N/A";
			this.incomplete = true;
//			return;
		}
		this.realEstateSubtype = fields[names2Index.get(REAL_ESTATE_SUBTYPE)];
		if (!validRealEstateSubtypeValues.contains(this.realEstateSubtype)) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no realestatesubtype");
			this.realEstateSubtype = "N/A";
			this.incomplete = true;
//			return;
		}
		
		
		this.state = fields[names2Index.get(STATE)];
		if (!validStateValues.contains(this.state)) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no realestatesubtype");
			this.state = "N/A";
			this.incomplete = true;
//			return;
		}
		
		this.subState = fields[names2Index.get(SUBSTATE)];
		if (!validSubstateValues.contains(this.subState)) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no realestatesubtype");
			this.subState = "N/A";
			this.incomplete = true;
//			return;
		}
		
		
		
		
		String floorString = fields[names2Index.get(FLOOR)];
		this.floors = new LinkedList<String>();
		for (String s : floorString.split(",")) {
			if (validFloorValues.contains(s)) {
				this.floors.add(s);
			}
			else {
				if (!this.floors.contains("N/A")) {
					this.floors.add("N/A");
				}
			}
		}
		String squareMeters = fields[names2Index.get(AREA)];
		this.size = 0.0f;
		try {
			this.size = Float.parseFloat(squareMeters);
		}
		catch (Exception ex) {
//			logger.debug("Ad \"" + logEntryLine + "\" has no size");
			this.incomplete = true;
//			return;
		}
		if (this.size <= 0.0f) {
			this.size = 0.0f;
			this.incomplete = true;
//			return;
		}
		
		this.newlyBuilt = state.equals("NEWBUILT") ? true : false;
		
		
		
		this.hasView = names2Index.get(HAS_VIEW) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_VIEW)]);
		this.hasPisina = names2Index.get(HAS_PISINA) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_PISINA)]);
		this.hasAutonomousHeating = names2Index.get(HAS_AUTONOMOUS_HEATING) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_AUTONOMOUS_HEATING)]);
		this.hasParking = names2Index.get(HAS_PARKING) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_PARKING)]);
		this.hasGarden = names2Index.get(HAS_GARDEN) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_GARDEN)]);
		this.hasSunVisors = names2Index.get(HAS_SUN_VISORS) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_SUN_VISORS)]);
		this.hasAC = names2Index.get(HAS_AIR_CONDITIONING) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_AIR_CONDITIONING)]);
		this.isNearMetro = names2Index.get(IS_NEAR_METRO) == null ? false : Boolean.parseBoolean(fields[names2Index.get(IS_NEAR_METRO)]);
		this.hasPrivateRoof = names2Index.get(HAS_PRIVATE_ROOF) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_PRIVATE_ROOF)]);
		this.hasSunBoiler = names2Index.get(HAS_SUN_BOILER) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_SUN_BOILER)]);
		this.hasNaturalGas = names2Index.get(HAS_NATURAL_GAS) == null ? false : Boolean.parseBoolean(fields[names2Index.get(HAS_NATURAL_GAS)]);
		
		this.constructionYear = 0;
		try {
			this.constructionYear = Integer.parseInt(fields[names2Index.get(CONSTRUCTION_YEAR)]);
		}
		catch (Exception ex) {
		}
		if (this.constructionYear <= 1875 && this.constructionYear != 0) {
			this.constructionYear = 0;
		}
		
		
		this.isAgent = fields[names2Index.get(IS_AGENT)].equalsIgnoreCase("true") ? true : false;
		
		
		
		this.publicationYear = 0;
		try {
			this.publicationYear = Integer.parseInt(fields[names2Index.get(PUB_YEAR)]);
			if (this.publicationYear < 2005 || this.publicationYear > 2011) {
				logger.warn("Cannot parse value for year " + fields[names2Index.get(PUB_YEAR)]);
				this.publicationYear = -1;
			}
		}
		catch (Exception ex) {
			logger.warn("Cannot parse value for year " + fields[names2Index.get(PUB_YEAR)]);
//			continue;
		}
		
		this.publicationMonth = -1;
		try {
			this.publicationMonth = Integer.parseInt(fields[names2Index.get(PUB_MONTH)]);
			if (this.publicationMonth < 0 || this.publicationMonth > 12) {
				logger.warn("Cannot parse value for month " + fields[names2Index.get(PUB_MONTH)]);
				this.publicationMonth = -1;
			}
		}
		catch (Exception ex) {
			logger.warn("Cannot parse value for month " + fields[names2Index.get(PUB_MONTH)]);
//			continue;
		}
		
		this.telephone = 0;
		try {
			this.telephone = Long.parseLong(fields[names2Index.get(TELEPHONE)]);
			if (this.telephone <= 2000000000l || this.telephone >= 7000000000l) {
//				logger.warn("Cannot parse value for telephone " + fields[names2Index.get(TELEPHONE)]);
				this.telephone = 0;
			}
		}
		catch (Exception ex) {
//			logger.warn("Cannot parse telephone " + fields[names2Index.get(TELEPHONE)]);
//			continue;
		}
		
		this.advID = 0;
		try {
			this.advID = Integer.parseInt(fields[names2Index.get(ADV_ID)]);
		}
		catch (Exception ex) {
			logger.warn("Cannot parse value for advID " + fields[names2Index.get(ADV_ID)]);
//			continue;
		}
		
		this.numBedrooms = 0;
		try {
			this.numBedrooms = Integer.parseInt(fields[names2Index.get(BEDROOMS)]);
			if (this.numBedrooms < 0) {
				this.numBedrooms = 0;
			}
			else if (this.numBedrooms > 10) {
				this.numBedrooms = 10;
			}
		}
		catch (Exception ex) {
			this.numBedrooms = 0;
		}
		
		this.incomplete = false;
	}
	
	public boolean isIncomplete() {
		return this.incomplete;
	}
	
	public String getItemType() {
		return this.itemType;
	}
	
	public float getPrice() {
		return this.price;
	}
	
	public float getSize() {
		return this.size;
	}
	
	public boolean getNewlyBuilt() {
		return this.newlyBuilt;
	}
	
	public String getTransactionType() {
		return this.transactionType;
	}
	
	public String getAreaID() {
		return this.areaID;
	}
	
	public String getFloor() {
		return this.floors.get(0);
	}
	
	public List<String> getFloorList() {
		return this.floors;
	}
	
	public String getRealEstateType() {
		return this.realEstateType;
	}
	
	public String getRealEstateSubtype() {
		return this.realEstateSubtype;
	}
	
	public String getState() {
		return this.state;
	}
	
	public String getSubstate() {
		return this.subState;
	}
	
	public boolean hasGarden() {
		return this.hasGarden;
	}
	
	public boolean hasParking() {
		return this.hasParking;
	}
	
	public boolean hasAutonomousHeating() {
		return this.hasAutonomousHeating;
	}
	
	public boolean hasPisina() {
		return this.hasPisina;
	}
	
	public boolean hasView() {
		return this.hasView;
	}
	
	public int getNumberOfYears() {
		int numYears = this.publicationYear - this.constructionYear;
		return numYears < 0 ? 0 : numYears;
	}
	
	public boolean isAgent() {
		return this.isAgent;
	}
	
	public boolean hasSunVisors() {
		return this.hasSunVisors;
	}
	
	public boolean hasSunBoiler() {
		return this.hasSunBoiler;
	}
	
	public boolean hasNaturalGas() {
		return this.hasNaturalGas;
	}
	
	public boolean hasAC() {
		return this.hasAC;
	}
	
	public boolean isNearMetro() {
		return this.isNearMetro;
	}
	
	public boolean hasPrivateRoof() {
		return this.hasPrivateRoof;
	}
	
	public int getPublicationYear() {
		return this.publicationYear;
	}
	
	public int getPublicationMonth() {
		return this.publicationMonth;
	}
	
	public int getID() {
		return this.advID;
	}
	
	public long getTelephone() {
		return this.telephone;
	}
	
	public int getBedrooms() {
		return this.numBedrooms;
	}
	
	public HashSet<String> getFloorValues() {
		return validFloorValues;
	}
	
	public static String getMappedRealEstateType(String realEstateType) {
		if (realEstateType.equals("OIKEIA") || realEstateType.equals("BUILDING") || realEstateType.equals("OTHER")) {
			return "HOUSE";
		}
		else {
			return realEstateType;
		}
	}
	
	
	public static String getMappedFloor(String floor) {
		if (floor.equals("L8") || floor.equals("L7") || floor.equals("L6") ) {
			return "L6+";
		}
		else if (floor.equals("SH") || floor.equals("S1") || floor.equals("LH") || floor.equals("LHH")){
			return "S";
		}
		else if (floor.equals("L0") || floor.equals("L1") || floor.equals("L2") || floor.equals("L3")
				|| floor.equals("L4") || floor.equals("L5")){
			return floor;
		}
		else {
			return "N/A";
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(areaID); sb.append("_");
		sb.append(transactionType); sb.append("_");
		sb.append(realEstateType); sb.append("_");
		for (int i = 0; i < floors.size() - 1; i ++) {
			sb.append(floors.get(i)); sb.append(","); 
		}
		sb.append(floors.get(floors.size()-1)); sb.append("_");
		sb.append(size); sb.append("_");
		sb.append(newlyBuilt); sb.append("_");
		sb.append(hasParking); sb.append("_");
		sb.append(hasGarden); sb.append("_");
		sb.append(hasAutonomousHeating); sb.append("_");
		sb.append(hasPisina); sb.append("_");
		sb.append(hasView); sb.append("_");
		sb.append(isAgent); sb.append("_");
		sb.append(state.toString()); sb.append("_");
		sb.append(constructionYear); sb.append("_");
		sb.append(price); 
		
		
		return sb.toString();
	}
}
