import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Residence extends RealEstate{
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	
	private String type;
	private String subtype;
	private String condition;
	private String subcondition;
	private String view;
	private String areaPlan;
	private String parkingType;
	private String availableFrom;
	private String availability;
	private String style;
	private String embadon;
	private String orientation;
	private String floor;
	private boolean hasAircondition;
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
	private boolean hasNoCommunal;
	private boolean isSuitableForProfUse;
	private boolean isOpenPlan;
	private boolean isFurnished;
	private boolean hasPrivateTerrace;
	private int area;
	private int land;
	private float gardenArea;
	private int storageArea;
	private float levelsArea;
	private float loftArea;
	private int masterBedrooms;
	private int bedrooms;
	private int bathrooms;
	private int wc;
	private int rooms;
	private int levels;
	private int constructionYear;
	private int refurbishmentYear;
	
	
	private static final String TRANSACTION_PRICE = "Transaction.Price";
	private static final String NEGOTIABLE = "Transaction.is_negotiable";
	private static final String GEO_AREA_ID = "Geo.area_id_new";
	
	
	private static final String AREA = "Item.area";
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
	private static final String SUITABLE_FOR_PROF_USE = "Item.suitable_for_prof_use";
	private static final String OPEN_PLAN = "Item.is_open_plan";
	private static final String PRIVATE_TERRACE = "Item.has_private_terrace";
	private static final String AVAILABILITY = "Item.availability";
	private static final String ODOS = "ODOS";
	private static final String PERIOXH = "perioxh";
	private static final String YPOPERIOXH = "ypoperioxh";
	private static final String ORIOTHETISI = "oriothetisi";
	private static final String ROOMS = "rooms";
	private static final String TEXT = "text"; 
	private static final String EMBADON = "embadon";
	private static final String ADDON_TEXT_INTERNET = "CustomText.extra_text_internet";
	private static final String ADDON_TEXT_PAPER = "CustomText.extra_text_paper";
	
	private static final Map<String, String> fieldNamesMapper;
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
    
    public Residence() {
    	
    }
    
    public Residence(String logEntryLine) throws Exception {
		String[] fields = logEntryLine.split(";");
		this.numberOfNonEmptyFields = fields.length;
		
		String[] secondaryFields = fields[0].split("\\^");
		if (secondaryFields.length < 2) {
			logger.error("Cannot parse line " + logEntryLine);
			throw new Exception();
		}
		String[] tt = secondaryFields[1].split(":");
		String tmp = tt[0];
		if (tmp.equals("Ενοικιάσεις Κατοικιών") || tmp.equals("Πωλήσεις Κατοικιών")) {
			this.itemType = "re_residence";
		}
		else if (tmp.equals("Ενοικιάσεις Οικοπέδων") || tmp.equals("Πωλήσεις Οικοπέδων")) {
			throw new Exception();
		}
		else if (tmp.equals("Ενοικιάσεις Καταστημάτων") || tmp.equals("Πωλήσεις Καταστημάτων")) {
			throw new Exception();
		}
		else if (tmp.equals("Πωλ. Εξοχικών Κατοικιών") || tmp.equals("Ενοικ. Εξοχικών Κατοικιών")) {
			throw new Exception();
		}
		else {
			logger.error("Cannot find item type for \"" + tmp + "\"");
			throw new Exception();
		}

		
		String type2 = tt[1].trim();
		if ((tmp.startsWith("Πωλήσεις") || tmp.startsWith("Πωλ.")) && type2.equals("Προσφορά")) {
			this.transactionType = "SELL";
		}
		else if ((tmp.startsWith("Πωλήσεις")  || tmp.startsWith("Πωλ.")) && type2.equals("Ζήτηση")) {
			this.transactionType = "BUY";
		}
		else if ((type.startsWith("Ενοικιάσεις") || type.startsWith("Ενοικ.")) && type2.equals("Προσφορά")) {
			this.transactionType = "LET";
		}
		else if ((type.startsWith("Ενοικιάσεις") || type.startsWith("Ενοικ.")) && type2.equals("Ζήτηση")) {
			this.transactionType = "RENT";
		}
		else {
			logger.error("Cannot find transaction type for \"" + tmp + "\"");
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
				if (domainFieldName.equals(TRANSACTION_PRICE)) {
					try {
						this.price = nf.parse(v).intValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse price (" + v + ")");
						throw new Exception();
					}
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
				else if (domainFieldName.equals(AREA)) {
					try {
						this.area = nf.parse(v).intValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse area (" + v + ")");
						throw new Exception();
					}
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
						this.storageArea = nf.parse(v).intValue();
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
				else if (domainFieldName.equals(BEDROOMS)) {
					try {
						this.bedrooms  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse bedrooms (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(BATHROOMS)) {
					try {
						this.bathrooms  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse bathrooms (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(WC)) {
					try {
						this.wc  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse wc (" + v + ")");
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
				else if (domainFieldName.equals(ROOMS)) {
					try {
						this.rooms  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse rooms (" + v + ")");
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
				else if (domainFieldName.equals(AIRCONDITION)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse aircondition (" + v + ")");
						throw new Exception();
					}
					this.hasAircondition = c == 1 ? true : false;
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
				else if (domainFieldName.equals(TYPE)) {
					this.type = v;
				}
				else if (domainFieldName.equals(SUBTYPE)) {
					this.subtype = v;
				}
				else if (domainFieldName.equals(CONDITION)) {
					this.condition = v;
					String[] parsedCondition = this.condition.split("\\.");
					if (parsedCondition.length == 2) {
						this.subcondition = parsedCondition[1];
						this.condition = parsedCondition[0];
					}
				}
	//			else if (domainFieldName.equals(SUBCONDITION)) {
//					
	//			}
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
						float tmpLand = Float.parseFloat(parsableText);
						if (!isInSquareMeters) {
							this.land = Math.round(1000.0f * tmpLand);
						}
						else {
							this.land = Math.round(tmpLand);
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
    
    public static String getFieldNames() {
		StringBuffer sb = new StringBuffer();
		sb.append(USER_ENTERED_TEXT); sb.append("\t");
		sb.append("Item.item_type"); sb.append("\t");
		sb.append("Transaction.type"); sb.append("\t");
		sb.append(TRANSACTION_PRICE); sb.append("\t");
		sb.append(NEGOTIABLE); sb.append("\t");
		sb.append(GEO_AREA_ID); sb.append("\t");
		sb.append(AREA); sb.append("\t");
		sb.append(TYPE); sb.append("\t");
		sb.append(SUBTYPE); sb.append("\t");
		sb.append(CONDITION); sb.append("\t");
		sb.append(SUBCONDITION); sb.append("\t");
		sb.append(BEDROOMS); sb.append("\t");
		sb.append(MASTER_BEDROOMS); sb.append("\t");
		sb.append(BATHROOMS); sb.append("\t");
		sb.append(WC); sb.append("\t");
		sb.append(LEVELS); sb.append("\t");
		sb.append(ROOMS); sb.append("\t");
		sb.append(LAND); sb.append("\t");
		sb.append(FLOOR); sb.append("\t");
		sb.append(STORAGE); sb.append("\t");
		sb.append(INDIVIDUAL_HEATING); sb.append("\t");
		sb.append(CENTRAL_HEATING); sb.append("\t");
		sb.append(FIREPLACE); sb.append("\t");
		sb.append(GARDEN); sb.append("\t");
		sb.append(VIEW); sb.append("\t");
		sb.append(HAS_PARKING); sb.append("\t");
		sb.append(AIRCONDITION); sb.append("\t");
		sb.append(SECURITY_DOOR); sb.append("\t");
		sb.append(SOLAR_BOILER); sb.append("\t");
		sb.append(SOLAR_VISORS); sb.append("\t");
		sb.append(NATURAL_GAS); sb.append("\t");
		sb.append(POOL); sb.append("\t");
		sb.append(FURNISHED); sb.append("\t");
		sb.append(WITHOUT_ELEVATOR); sb.append("\t");
		sb.append(LOFT); sb.append("\t");
		sb.append(COMMUNAL); sb.append("\t");
		sb.append(SUITABLE_FOR_PROF_USE); sb.append("\t");
		sb.append(OPEN_PLAN); sb.append("\t");
		sb.append(PRIVATE_TERRACE); sb.append("\t");
		sb.append(AGENTS_ACCEPTED); sb.append("\t");
		sb.append(IS_AGENT); sb.append("\t");
		sb.append(ORIENTATION); sb.append("\t");
		sb.append(CONSTRUCTION_YEAR); sb.append("\t");
		sb.append(REFURBISHED_YEAR); sb.append("\t");
		sb.append(PERIOXH); sb.append("\t");
		sb.append(YPOPERIOXH); sb.append("\t");
		sb.append(ORIOTHETISI); sb.append("\t");
		sb.append(ODOS); sb.append("\t");
		sb.append(AVAILABILITY); sb.append("\t");
		sb.append(GARDEN_AREA); sb.append("\t");
		sb.append(STORAGE_AREA); sb.append("\t");
		sb.append(LOFT_AREA); sb.append("\t");
		sb.append(LEVELS_AREA); sb.append("\t");
		sb.append(LOCATION); sb.append("\t");
		sb.append(STYLE); sb.append("\t");
		sb.append(PARKING_TYPE); sb.append("\t");
		sb.append(AVAILABLE_FROM); sb.append("\t");
		sb.append(AREA_PLAN); sb.append("\t");
		sb.append(EMBADON); sb.append("\t");
		sb.append(TEXT); 
		
		sb.append(newline);
		return sb.toString();
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(this.userEnteredText); sb.append("\t");
    	sb.append(this.itemType); sb.append("\t");
		sb.append(this.transactionType); sb.append("\t");
		sb.append(this.price == 0 ? "N/A" : this.price); sb.append("\t");
		sb.append(this.isNegotiable); sb.append("\t");
		sb.append(this.geoArea == null ? "N/A" : this.geoArea); sb.append("\t");
		sb.append(this.area == 0.0f ? "N/A" : this.area); sb.append("\t");
		sb.append(this.type == null ? "N/A" : this.type); sb.append("\t");
		sb.append(this.subtype == null ? "N/A" : this.subtype); sb.append("\t");
		sb.append(this.condition == null ? "N/A" : this.condition); sb.append("\t");
		sb.append(this.subcondition == null ? "N/A" : this.subcondition); sb.append("\t");
		sb.append(this.bedrooms == 0 ? "N/A" : this.bedrooms); sb.append("\t");
		sb.append(this.masterBedrooms == 0 ? "N/A" : this.masterBedrooms); sb.append("\t");
		sb.append(this.bathrooms == 0? "N/A" : this.bathrooms); sb.append("\t");
		sb.append(this.wc == 0 ? "N/A" : this.wc); sb.append("\t");
		sb.append(this.levels == 0 ? "N/A" : this.levels); sb.append("\t");
		sb.append(this.rooms == 0 ? "N/A" : this.rooms); sb.append("\t");
		sb.append(this.land == 0 ? "N/A" : this.land); sb.append("\t");
		sb.append(this.floor == null ? "N/A" : this.floor); sb.append("\t");
		sb.append(this.hasStorage); sb.append("\t");
		sb.append(this.hasIndividualHeating); sb.append("\t");
		sb.append(this.hasCentralHeating); sb.append("\t");
		sb.append(this.hasFireplace); sb.append("\t");
		sb.append(this.hasGarden); sb.append("\t");
		sb.append(this.view == null ? "N/A" : this.view); sb.append("\t");
		sb.append(this.hasParking); sb.append("\t");
		sb.append(this.hasAircondition); sb.append("\t");
		sb.append(this.hasSecureDoor); sb.append("\t");
		sb.append(this.hasSolarBoiler); sb.append("\t");
		sb.append(this.hasSolarVisors); sb.append("\t");
		sb.append(this.hasNaturalGas); sb.append("\t");
		sb.append(this.hasPool); sb.append("\t");
		sb.append(this.isFurnished); sb.append("\t");
		sb.append(this.hasNoElevator); sb.append("\t");
		sb.append(this.hasLoft); sb.append("\t");
		sb.append(this.hasNoCommunal); sb.append("\t");
		sb.append(this.isSuitableForProfUse); sb.append("\t");
		sb.append(this.isOpenPlan); sb.append("\t");
		sb.append(this.hasPrivateTerrace); sb.append("\t");
		sb.append(this.isAgentsAccepted); sb.append("\t");
		sb.append(this.isAgent); sb.append("\t");
		sb.append(this.orientation == null ? "N/A" : this.orientation); sb.append("\t");
		sb.append(this.constructionYear == 0 ? "N/A" : this.constructionYear); sb.append("\t");
		sb.append(this.refurbishmentYear == 0 ? "N/A" : this.refurbishmentYear); sb.append("\t");
		sb.append(this.perioxh == null ? "N/A" : this.perioxh); sb.append("\t");
		sb.append(this.ypoperioxh == null ? "N/A" : this.ypoperioxh); sb.append("\t");
		sb.append(this.oriothetisi == null ? "N/A" : this.oriothetisi); sb.append("\t");
		sb.append(this.odos == null ? "N/A" : this.odos);  sb.append("\t");
		sb.append(this.availability == null ? "N/A" : this.availability); sb.append("\t");
		sb.append(this.gardenArea == 0.0f ? "N/A" : this.gardenArea); sb.append("\t");
		sb.append(this.storageArea == 0.0f ? "N/A" : this.storageArea); sb.append("\t");
		sb.append(this.loftArea == 0.0f ? "N/A" : this.loftArea); sb.append("\t");
		sb.append(this.levelsArea == 0.0f ? "N/A" : this.levelsArea); sb.append("\t");
		sb.append(this.topothesia == null ? "N/A" : this.topothesia); sb.append("\t");
		sb.append(this.style == null ? "N/A" : this.style); sb.append("\t");
		sb.append(this.parkingType == null ? "N/A" : this.parkingType); sb.append("\t");
		sb.append(this.availableFrom == null ? "N/A" : this.availableFrom); sb.append("\t");
		sb.append(this.areaPlan == null ? "N/A" : this.areaPlan); sb.append("\t");
		sb.append(this.embadon == null ? "N/A" : this.embadon); sb.append("\t");
		sb.append(this.text == null ? "N/A" : this.text); 
		
    	sb.append(newline);
		return sb.toString();
    }
    
    public void setUserEnteredText(String userEnteredText) {
		this.userEnteredText = userEnteredText;
	}
    
    public void setItemType(String itemType) {
		this.itemType = itemType;
	}
    
    public void setTransactionType(String tranType) {
		this.transactionType = tranType;
	}
    
    public void setType(String type) {
		this.type = type;
	}
    
    public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
    
    public void setCondition(String condition) {
		this.condition = condition;
	}
    
    public void setSubcondition(String subcondition) {
		this.subcondition = subcondition;
	}
    
	public void setArea(int area) {
		this.area = area;
	}
	
	public void setLandArea(int area) {
		this.land = area;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public void setBedrooms(int bedrooms) {
		this.bedrooms = bedrooms;
	}
	
	public void setMasterBedrooms(int masterBedrooms) {
		this.masterBedrooms = masterBedrooms;
	}
	
	public void setBathrooms(int bathrooms) {
		this.bathrooms = bathrooms;
	}
	
	public void setWC(int wc) {
		this.wc = wc;
	}
	
	public void setHasLoft(boolean hasLoft) {
		this.hasLoft = hasLoft;
	}
	
	public void setLoftArea(int loftArea) {
		this.loftArea = loftArea;
	}
	
	public void setNegotiable(boolean isNegotiable) {
		this.isNegotiable = isNegotiable;
	}
	
	public void setFloor(String floor) {
		this.floor = floor;
	}
	
	public void setSolarBoiler(boolean hasSolarBoiler) {
		this.hasSolarBoiler = hasSolarBoiler;
	}
	
	public void setPool(boolean hasPool) {
		this.hasPool = hasPool;
	}
	
	public void setGarden(boolean hasGarden) {
		this.hasGarden = hasGarden;
	}
	
	public void setSolarVisors(boolean hasSolarVisors) {
		this.hasSolarVisors = hasSolarVisors;
	}
	
	public void setFireplace(boolean hasFireplace) {
		this.hasFireplace = hasFireplace;
	}
	
	public void setStorage(boolean hasStorage) {
		this.hasStorage = hasStorage;
	}
	
	public void setStorageArea(int area) {
		this.storageArea = area;
	}
	
	public void setConstructionYear(int year) {
		this.constructionYear = year;
	}
	
	public void setParking(boolean parking) {
		this.hasParking = parking;
	}
	
	public void setAutonomousHeating(boolean hasAutonomousHeating) {
		this.hasIndividualHeating = hasAutonomousHeating;
	}
	
	public void setSecureDoor(boolean hasSecureDoor) {
		this.hasSecureDoor = hasSecureDoor;
	}
	
	public void setAC(boolean hasAC) {
		this.hasAircondition = hasAC;
	}
	
	public void setNaturalGas(boolean hasNaturalGas) {
		this.hasNaturalGas = hasNaturalGas;
	}
	
	public void setLevels(int levels) {
		this.levels = levels;
	}
}
