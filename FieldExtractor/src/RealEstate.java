import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RealEstate {
	private Logger logger = LoggerFactory.getLogger(getClass());
	protected static NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);  // Parse numbers according to locale
	
	
	public Pattern p = Pattern.compile("(.+)=(.+)");
	public Pattern pArea = Pattern.compile("(\\d+[,.\\d]*)(\\D+)");
	protected String transactionType;
	protected String itemType;
	protected String geoArea;
	
	private String type;
	private String subtype;
	private String condition;
	private String subcondition;
	protected String phone;
	private String orientation;
	protected String userEnteredText;
	protected String extraTextInternet;
	protected String extraTextPaper;
	protected String text;
	private String view;
	private String areaPlan;
	private String parkingType;
	private String availableFrom;
	private String style;
	protected String perioxh;
	protected String ypoperioxh;
	protected String oriothetisi;
	private String embadon;
	private String availability;
	private String aircondition;
	protected String odos;
	protected String topothesia;
	private boolean hasNoElevator;
	private boolean hasGarden;
	private boolean hasIndividualHeating;
	private boolean hasCentralHeating;
	private boolean hasFireplace;
	private boolean hasStorage;
	private boolean hasParking;
	private boolean hasSecureDoor;
	private boolean hasSolarBoiler;
	private boolean hasSolarVisors;
	private boolean hasNaturalGas;
	private boolean hasLoft;
	private boolean hasPool;
	protected boolean isNegotiable;
	protected boolean isAgentsAccepted;
	protected boolean isAgent;
	private boolean isFurnished;
	private boolean hasNoCommunal;
	private boolean isSuitableForProfUse;
	private boolean isOpenPlan;
	private boolean hasLoadingRamp;
	private boolean hasPrivateTerrace;
	private boolean hasCabling;
	private float land;
	protected int price;
	private float gardenArea;
	private float parkingArea;
	private float storageArea;
	private float levelsArea;
	private float loftArea;
	private float height;
	private int parkingSlots;
	private int masterBedrooms;
	private int levels;
	private int constructionYear;
	private int refurbishmentYear;
	protected int numberOfNonEmptyFields;
	private static final String TRANSACTION_PRICE = "Transaction.Price";
	private static final String NEGOTIABLE = "Transaction.is_negotiable";
	private static final String GEO_AREA_ID = "Geo.area_id_new";
	
	
	private static final String AREA = "Item.total_area";
	private static final String BEDROOMS = "Item.bedrooms";
	private static final String BATHROOMS = "Item.bathrooms";
	private static final String WC = "Item.wc";
	private static final String LAND = "Item.land";
	private static final String TYPE = "Item.type";
	private static final String SUBTYPE = "Item.subtype";
	private static final String CONDITION = "Item.condition";
	private static final String SUBCONDITION = "Item.condition_used";
	private static final String AGENTS_ACCEPTED = "Item.agents_accepted";
	private static final String FLOOR = "Item.level";
	private static final String STORAGE = "Item.has_storage";
	private static final String INDIVIDUAL_HEATING = "Item.has_autonomous_heating";
	private static final String FIREPLACE = "Item.has_fireplace";
	private static final String GARDEN = "Item.has_garden";
	private static final String ORIENTATION = "Item.orientation";
	private static final String VIEW = "Item.view";
	private static final String HAS_PARKING = "Item.has_parking";
	private static final String AIRCONDITION = "Item.has_aircon";
	private static final String IS_AGENT = "Item.by_agent";
	private static final String SECURITY_DOOR = "Item.has_secure_door";
	private static final String MASTER_BEDROOMS = "Item.master_bedrooms";
	private static final String LEVELS = "Item.floors";
	private static final String PARKING_TYPE = "Item.parking_type";
	private static final String CONSTRUCTION_YEAR = "Item.construction_year";
	private static final String SOLAR_BOILER = "Item.has_solar_boiler";
	private static final String SOLAR_VISORS = "Item.has_tents";
	private static final String LOCATION = "Item.location";
	private static final String AREA_PLAN = "Item.area_plan";
	private static final String NATURAL_GAS = "Item.has_natural_gas";
	private static final String GARDEN_AREA = "Item.garden_area";
	private static final String POOL = "Item.has_pool";
	private static final String AVAILABLE_FROM = "Item.available_from";
	private static final String FURNISHED = "Item.is_furnished";
	private static final String STYLE = "Item.style";
	private static final String WITHOUT_ELEVATOR = "Item.without_elevator";
	private static final String STORAGE_AREA = "Item.storage_area";
	private static final String LOFT_AREA = "Item.loft_area";
	private static final String LEVELS_AREA = "Item.floors_area";
	private static final String CENTRAL_HEATING = "Item.has_central_heating";
	private static final String LOFT = "Item.has_loft";
	private static final String COMMUNAL = "Item.with_no_communal_charge";
	private static final String REFURBISHED_YEAR = "Item.refurbishment_year";
	private static final String PARKING_SLOTS = "Item.parking_slots";
	private static final String SUITABLE_FOR_PROF_USE = "Item.suitable_for_prof_use";
	private static final String OPEN_PLAN = "Item.is_open_plan";
	private static final String PRIVATE_TERRACE = "Item.has_private_terrace";
	private static final String AVAILABILITY = "Item.availability";
	

	
	
	protected static final String PHONE = "Ad.mainphone";
	
	protected static final String USER_ENTERED_TEXT = "bodyPhone";
	
	private static final String ODOS = "ODOS";
	private static final String PERIOXH = "perioxh";
	private static final String YPOPERIOXH = "ypoperioxh";
	private static final String ORIOTHETISI = "oriothetisi";
	private static final String ROOMS = "rooms";
	private static final String TEXT = "text"; 
	private static final String EMBADON = "embadon";
	private static final String ADDON_TEXT_INTERNET = "CustomText.extra_text_internet";
	private static final String ADDON_TEXT_PAPER = "CustomText.extra_text_paper";
	protected static final Map<String, String> fieldNamesMapper;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("τιμή", TRANSACTION_PRICE);
        aMap.put("τιμ", TRANSACTION_PRICE);
        aMap.put("_πε", GEO_AREA_ID);
        aMap.put("μεσίτες_δεκτοί", AGENTS_ACCEPTED);
        aMap.put("εμβαδόν", AREA);
        aMap.put("υπνοδωμάτια", BEDROOMS);
        aMap.put("μπάνια", BATHROOMS);
        aMap.put("wc", WC);
        aMap.put("κατάσταση", CONDITION);
        aMap.put("υποκατάσταση", SUBCONDITION);
        aMap.put("σε_οικόπεδο", LAND);
        aMap.put("είδος", TYPE);
        aMap.put("υποείδος", SUBTYPE);
        aMap.put("όροφος", FLOOR);
        aMap.put("bodyPhone", USER_ENTERED_TEXT);
        aMap.put("*", ADDON_TEXT_PAPER);
        aMap.put("itxt", ADDON_TEXT_INTERNET);
        aMap.put("txt", ADDON_TEXT_PAPER);
        aMap.put("μεσιτικό", IS_AGENT);
        aMap.put("αυτόνομη_θέρμανση", INDIVIDUAL_HEATING);
        aMap.put("κήπος", GARDEN);
        aMap.put("τζάκι", FIREPLACE);
        aMap.put("αποθήκη", STORAGE);
        aMap.put("πάρκιν", HAS_PARKING);
        aMap.put("συζητήσιμη", NEGOTIABLE);
        aMap.put("αριθμός_επιπέδων", LEVELS);
        aMap.put("είδος_πάρκιν", PARKING_TYPE);
        aMap.put("phone", PHONE);
        aMap.put("θέα", VIEW);
        aMap.put("πόρτα_ασφαλείας", SECURITY_DOOR);
        aMap.put("προσανατολισμός", ORIENTATION);
        aMap.put("κλιματισμός", AIRCONDITION);
        aMap.put("υπνοδωμάτια_master", MASTER_BEDROOMS);
        aMap.put("έτος_κατασκευής", CONSTRUCTION_YEAR);
        aMap.put("ηλιακός_θερμοσίφωνας", SOLAR_BOILER);
        aMap.put("τέντες", SOLAR_VISORS);
        aMap.put("διαμόρφωση_χώρων", AREA_PLAN);
        aMap.put("φυσικό_αέριο", NATURAL_GAS);
        aMap.put("εμβαδόν_κήπου", GARDEN_AREA);
        aMap.put("διαθέσιμο_από", AVAILABLE_FROM);
        aMap.put("πισίνα", POOL);
        aMap.put("επιπλωμένο", FURNISHED);
        aMap.put("τύπος", STYLE);
        aMap.put("χωρίς_ασανσέρ", WITHOUT_ELEVATOR);
        aMap.put("εμβαδόν_αποθήκης", STORAGE_AREA);
        aMap.put("κεντρική_θέρμανση", CENTRAL_HEATING);
        aMap.put("εμβαδόν_ορόφων", LEVELS_AREA);
        aMap.put("πατάρι", LOFT);
        aMap.put("εμβαδόν_παταριού", LOFT_AREA);
        aMap.put("χωρίς_κοινόχρηστα", COMMUNAL);
        aMap.put("έτος_ανακαίνισης", REFURBISHED_YEAR);
        aMap.put("θέσεις_πάρκιν", PARKING_SLOTS);
        aMap.put("κατάλληλο_και_για_επαγγελματική_χρήση", SUITABLE_FOR_PROF_USE);
        aMap.put("και_για_επαγγελματική_χρήση", SUITABLE_FOR_PROF_USE);
        aMap.put("ενιαίος_χώρος", OPEN_PLAN);
        aMap.put("ιδιόκτητη_ταράτσα", PRIVATE_TERRACE);
        aMap.put("διαθεσιμότητα", AVAILABILITY);
        aMap.put("περιοχή", PERIOXH);
        aMap.put("υποπεριοχή", YPOPERIOXH);
        aMap.put("οριοθέτηση", ORIOTHETISI);
        aMap.put("_δω", ROOMS);
        aMap.put("_εμ", EMBADON);
        aMap.put("οδός", ODOS);
        aMap.put("τοποθεσία", LOCATION);
        aMap.put("text", TEXT);
        fieldNamesMapper = Collections.unmodifiableMap(aMap);
    }
	
    public RealEstate() {
    	
    }
	public RealEstate(String logEntryLine) throws Exception {
		String[] fields = logEntryLine.split(";");
		this.numberOfNonEmptyFields = fields.length;
		
		String[] secondaryFields = fields[0].split("\\^");
		if (secondaryFields.length < 2) {
			logger.error("Cannot parse line " + logEntryLine);
			throw new Exception();
		}
		String[] tt = secondaryFields[1].split(":");
		String type = tt[0];
		if (type.equals("Ενοικιάσεις Κατοικιών") || type.equals("Πωλήσεις Κατοικιών")) {
			this.itemType = "re_residence";
		}
		else if (type.equals("Ενοικιάσεις Οικοπέδων") || type.equals("Πωλήσεις Οικοπέδων")) {
			this.itemType = "re_land";
		}
		else if (type.equals("Ενοικιάσεις Καταστημάτων") || type.equals("Πωλήσεις Καταστημάτων")) {
			this.itemType = "re_prof";
		}
		else if (type.equals("Πωλ. Εξοχικών Κατοικιών") || type.equals("Ενοικ. Εξοχικών Κατοικιών")) {
			this.itemType = "re_residence_hol";
		}
		else {
			logger.error("Cannot find item type for \"" + type + "\"");
			throw new Exception();
		}

		
		String type2 = tt[1].trim();
		if ((type.startsWith("Πωλήσεις") || type.startsWith("Πωλ.")) && type2.equals("Προσφορά")) {
			this.transactionType = "SELL";
		}
		else if ((type.startsWith("Πωλήσεις")  || type.startsWith("Πωλ.")) && type2.equals("Ζήτηση")) {
			this.transactionType = "BUY";
		}
		else if ((type.startsWith("Ενοικιάσεις") || type.startsWith("Ενοικ.")) && type2.equals("Προσφορά")) {
			this.transactionType = "LET";
		}
		else if ((type.startsWith("Ενοικιάσεις") || type.startsWith("Ενοικ.")) && type2.equals("Ζήτηση")) {
			this.transactionType = "RENT";
		}
		else {
			logger.error("Cannot find transaction type for \"" + type + "\"");
			throw new Exception();
		}
		
		for (int i = 1; i < this.getNumberOfNonEmptyFields(); i ++) {
			fields[i] = fields[i].replaceAll("\\^", "");
			Matcher m = p.matcher(fields[i]);
			if (!m.matches()) {
				if (!fields[i].matches("\\d+")) {
					logger.warn("Skipping " + fields[i]);
				}
				continue;
			}
			String f = m.group(1);
			String domainFieldName = fieldNamesMapper.get(f);
			if (domainFieldName == null) {
				logger.warn("Cannot map field \"" + f + "\"");
				continue;
			}
			else {
				String v = m.group(2);
				if (v == null) {
					logger.warn("Cannot parse value for field \"" + domainFieldName + "\"");
					continue;
				}
				
				
				
				else if (domainFieldName.equals(GARDEN_AREA)) {
					try {
						this.gardenArea = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse garden_area (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(LOFT_AREA)) {
					try {
						this.loftArea = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse loft_area (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(LEVELS_AREA)) {
					try {
						this.levelsArea = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse levels_area (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(STORAGE_AREA)) {
					try {
						this.storageArea = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse storageArea (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(GEO_AREA_ID)) {
					this.geoArea = v;
				}
				else if (domainFieldName.equals(AGENTS_ACCEPTED)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse agents_accepted (" + v + ")");
						throw new Exception();
					}
					this.isAgentsAccepted = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(IS_AGENT)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse is_agent (" + v + ")");
						throw new Exception();
					}
					this.isAgent = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(GARDEN)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_garden (" + v + ")");
						throw new Exception();
					}
					this.hasGarden = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(INDIVIDUAL_HEATING)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse individual_heating (" + v + ")");
						throw new Exception();
					}
					this.hasIndividualHeating = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(CENTRAL_HEATING)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse central_heating (" + v + ")");
						throw new Exception();
					}
					this.hasCentralHeating = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(FIREPLACE)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_fireplace (" + v + ")");
						throw new Exception();
					}
					this.hasFireplace = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(HAS_PARKING)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_parking (" + v + ")");
						throw new Exception();
					}
					this.hasParking = c == 1 ? true : false;
				}
				
				else if (domainFieldName.equals(STORAGE)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_storage (" + v + ")");
						throw new Exception();
					}
					this.hasStorage = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(NEGOTIABLE)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse is_negotiable (" + v + ")");
						throw new Exception();
					}
					this.isNegotiable = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(SECURITY_DOOR)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_secure_door (" + v + ")");
						throw new Exception();
					}
					this.hasSecureDoor = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(NATURAL_GAS)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse natural_gas (" + v + ")");
						throw new Exception();
					}
					this.hasNaturalGas = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(POOL)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse pool (" + v + ")");
						throw new Exception();
					}
					this.hasPool = c == 1 ? true : false;
				}
				
				else if (domainFieldName.equals(FURNISHED)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse furnished (" + v + ")");
						throw new Exception();
					}
					this.isFurnished = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(LOFT)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_loft (" + v + ")");
						throw new Exception();
					}
					this.hasLoft = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(WITHOUT_ELEVATOR)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse elevator (" + v + ")");
						throw new Exception();
					}
					this.hasNoElevator = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(SUITABLE_FOR_PROF_USE)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse suitable_for_prof_use (" + v + ")");
						throw new Exception();
					}
					this.isSuitableForProfUse = c == 1 ? true : false;
				}
				
				
				else if (domainFieldName.equals(MASTER_BEDROOMS)) {
					try {
						this.masterBedrooms  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse master_bedrooms (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(LEVELS)) {
					try {
						this.levels  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse levels (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(CONSTRUCTION_YEAR)) {
					try {
						this.constructionYear  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse construction_year (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(REFURBISHED_YEAR)) {
					try {
						this.refurbishmentYear  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse refurbishment_year (" + v + ")");
						throw new Exception();
					}
				}
				
				else if (domainFieldName.equals(SOLAR_BOILER)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_solar_boiler (" + v + ")");
						throw new Exception();
					}
					this.hasSolarBoiler = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(SOLAR_VISORS)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_solar_visors (" + v + ")");
						throw new Exception();
					}
					this.hasSolarVisors = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(COMMUNAL)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse no_communal_charges (" + v + ")");
						throw new Exception();
					}
					this.hasNoCommunal = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(OPEN_PLAN)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse open_plan (" + v + ")");
						throw new Exception();
					}
					this.isOpenPlan = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(STYLE)) {
					this.style = v;
				}
				else if (domainFieldName.equals(PARKING_TYPE)) {
					this.parkingType = v;
				}
				else if (domainFieldName.equals(AVAILABLE_FROM)) {
					this.availableFrom = v;
				}
				else if (domainFieldName.equals(VIEW)) {
					this.view = v;
				}
				else if (domainFieldName.equals(AREA_PLAN)) {
					this.areaPlan = v;
				}
				else if (domainFieldName.equals(AIRCONDITION)) {
					this.aircondition = v;
				}
				else if (domainFieldName.equals(AVAILABILITY)) {
					this.availability = v;
				}
				else if (domainFieldName.equals(USER_ENTERED_TEXT)) {
					this.userEnteredText = v;
				}
				else if (domainFieldName.equals(ADDON_TEXT_INTERNET)) {
					this.extraTextInternet = v;
				}
				else if (domainFieldName.equals(ADDON_TEXT_PAPER)) {
					this.extraTextPaper = v;
				}
				else if (domainFieldName.equals(PHONE)) {
					this.phone = v;
				}
				else if (domainFieldName.equals(ORIENTATION)) {
					this.orientation = v;
				}
				else if (domainFieldName.equals(PERIOXH)) {
					this.perioxh = v;
				}
				else if (domainFieldName.equals(YPOPERIOXH)) {
					this.ypoperioxh = v;
				}
				else if (domainFieldName.equals(ORIOTHETISI)) {
					this.oriothetisi = v;
				}
				else if (domainFieldName.equals(EMBADON)) {
					this.embadon = v;
				}
				else if (domainFieldName.equals(ODOS)) {
					this.odos = v;
				}
				else if (domainFieldName.equals(LOCATION)) {
					this.topothesia = v;
				}
				else if (domainFieldName.equals(LAND)) {
					String parsableText = v;
					boolean isInSquareMeters = true;
					Matcher mArea = pArea.matcher(v);
					if (mArea.matches()) {
						if (mArea.group(2).equals("τ") || mArea.group(2).equals("Τ") || mArea.group(2).equals("t")) {
							isInSquareMeters = true;
						}
						else if (mArea.group(2).equals("σ") || mArea.group(2).equals("s")) {
							isInSquareMeters = false;
						}
						else {
							logger.warn("Cannot identify land area unit for \"" + parsableText + "\"");
							throw new Exception();
						}
						parsableText = mArea.group(1);
					}
					try {
						this.land = Float.parseFloat(parsableText);
						if (!isInSquareMeters) {
							this.land *= 1000.0f;
						}
					}
					catch (Exception ex) {
						logger.error("Cannot parse land_area (" + parsableText + ")");
						throw new Exception();
					}
				}
			}
			
			
		}

	}
	
	
	public float getPrice() {
		return this.price;
	}
	
	public String getTransactionType() {
		return this.transactionType;
	}
	
	public String getItemType() {
		return this.itemType;
	}
	
	public String getGeoArea() {
		return this.geoArea;
	}
	
	public String getUserEnteredText() {
		return this.userEnteredText;
	}
	
	public String getAddOnTextInternet() {
		return this.extraTextInternet;
	}
	
	public String getAddOnTextPaper() {
		return this.extraTextPaper;
	}
	
	public String getOrientation() {
		return this.orientation;
	}
	
	public int getNumberOfNonEmptyFields() {
		return this.numberOfNonEmptyFields;
	}
	
	public boolean isAgentsAccepted() {
		return this.isAgentsAccepted;
	}
	
	public boolean hasGarden() {
		return this.hasGarden;
	}
	
	public boolean hasIndividualHeating() {
		return this.hasIndividualHeating;
	}
	
	public boolean hasFireplace() {
		return this.hasFireplace;
	}
	
	public boolean hasParking() {
		return this.hasParking;
	}
	
	public boolean isNegotiable() {
		return this.isNegotiable;
	}
	
	public boolean hasStorage() {
		return this.hasStorage;
	}
	
	public boolean hasSecureDoor() {
		return this.hasSecureDoor;
	}
	
	public boolean isAgent() {
		return this.isAgent;
	}
	
	public String getPrimaryPhone() {
		return this.phone;
	}
	
	public String getPerioxh() {
		return this.perioxh;
	}
	
	public String getYpoperioxh() {
		return this.ypoperioxh;
	}
	
	public String getOriothetisi() {
		return this.oriothetisi;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getSubtype() {
		return this.subtype;
	}
	
	public String getCondition() {
		return this.condition;
	}
	
	public String getSubcondition() {
		return this.subcondition;
	}
	
	public String getEmbadon() {
		return this.embadon;
	}
	
	public String getLocation() {
		return this.topothesia;
	}
	
	public String getView() {
		return this.view;
	}
	
	public String getAircondition() {
		return this.aircondition;
	}
	
	public float getLandArea() {
		return this.land;
	}
	
	public int getMasterBedrooms() {
		return this.masterBedrooms;
	}
	
	public int getLevels() {
		return this.levels;
	}
	
	public int getConstructionYear() {
		return this.constructionYear;
	}
	
	public String getParkingType() {
		return this.parkingType;
	}
	
	public boolean getSolarBoiler() {
		return this.hasSolarBoiler;
	}
	
	public boolean getSolarVisors() {
		return this.hasSolarVisors;
	}
	
	public String getAreaPlan() {
		return this.areaPlan;
	}
	
	
	
	public float getGardenArea() {
		return this.gardenArea;
	}
	
	
	public boolean hasPool() {
		return this.hasPool;
	}
	
	public boolean hasNaturalGas() {
		return this.hasNaturalGas;
	}
	
	public String getAvailableFrom() {
		return this.availableFrom;
	}
	
	
	
	public boolean isFurnished() {
		return this.isFurnished;
	}
	
	public boolean hasNoElevator() {
		return this.hasNoElevator;
	}
	
	public String getStyle() {
		return this.style;
	}
	
	public String getStreet() {
		return this.odos;
	} 
	
	
	
	public float getStorageArea() {
		return this.storageArea;
	}
	
	public float getLoftArea() {
		return this.loftArea;
	}
	
	public float getLevelsArea() {
		return this.levelsArea;
	}
	
	public boolean hasCentralHeating() {
		return this.hasCentralHeating;
	}
	
	public boolean hasLoft() {
		return this.hasLoft;
	}
	
	
	
	public boolean hasNoCommunal() {
		return this.hasNoCommunal;
	}
	
	public int getRefurbishmentYear() {
		return this.refurbishmentYear;
	}
	
	
	
	public int getParkingSlots() {
		return this.parkingSlots;
	}
	
	
	
	public boolean getSuitableForProfUse() {
		return this.isSuitableForProfUse;
	}
	
	public boolean isOpenPlan() {
		return this.isOpenPlan;
	}
	
	
	public boolean hasLoadingRamp() {
		return this.hasLoadingRamp;
	}
	
	public boolean hasPrivateTerrace() {
		return this.hasPrivateTerrace;
	}
	
	public String getAvailability() {
		return this.availability;
	}
	
	
	
	public boolean hasCabling() {
		return this.hasCabling;
	}
	
	
	
	public float getParkingArea() {
		return this.parkingArea;
	}
	
	public float getHeight() {
		return this.height;
	}
}
