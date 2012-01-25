import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Land extends RealEstate {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	
	private String type;
	private String utilities;
	private String cityPlan;
	private String dimensions;
	private String contains;
	private String tilt;
	private String view;
	private boolean hasBuildLicense;
	private boolean isWhole;
	private boolean isBuildable;
	private boolean hasBuilding;
	private boolean canDrill;
	private boolean isCorner;
	private boolean isFenced;
	private boolean through;
	private float buildFactor;
	private float coverFactor;
	private float front;
	private float buildingArea;
	private float buildableArea;
	private float area;
	private int distanceToShore;
	
	private static final String TRANSACTION_PRICE = "Transaction.Price";
	private static final String NEGOTIABLE = "Transaction.is_negotiable";
	private static final String GEO_AREA_ID = "Geo.area_id_new";
	private static final String AREA = "Item.area";
	private static final String TYPE = "Item.type";
	private static final String FRONT_DIMENSION = "Item.front_dimension";
	private static final String THROUGH = "Item.is_see_through";
	private static final String CORNER = "Item.is_corner";
	private static final String BUILDING = "Item.contains_building";
	private static final String BUILDING_AREA = "Item.building_area";
	private static final String DRILL = "Item.can_drill";
	private static final String UTILITIES = "Item.utilities";
	private static final String FENCED = "Item.fenced";
	private static final String TILT = "Item.tilt";
	private static final String VIEW = "Item.view";
	private static final String DIMENSIONS = "Item.dimensions";
	private static final String DISTANCE_TO_SHORE = "Item.distance_to_shore";
	private static final String BUILD_FACTOR = "Item.build_factor";
	private static final String BUILDABLE_AREA = "Item.buildable_area";
	private static final String BUILD_LICENSE = "Item.build_license";
	private static final String WHOLE = "Item.whole";
	private static final String BUILDABLE = "Item.buildable";
	private static final String COVER_FACTOR = "Item.cover_factor";
	private static final String CITY_PLAN = "Item.city_plan";
	private static final String CONTAINS = "Item.contains";
	private static final String AGENTS_ACCEPTED = "Item.agents_accepted";
	private static final String IS_AGENT = "Item.by_agent";
	private static final String ODOS = "ODOS";
	private static final String PERIOXH = "perioxh";
	private static final String YPOPERIOXH = "ypoperioxh";
	private static final String ORIOTHETISI = "oriothetisi";
	private static final String TEXT = "text"; 
	private static final String EMBADON = "embadon";
	private static final String ADDON_TEXT_INTERNET = "CustomText.extra_text_internet";
	private static final String ADDON_TEXT_PAPER = "CustomText.extra_text_paper";
	private static final String LOCATION = "Item.location";
	
	private static final Map<String, String> fieldNamesMapper;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("τιμή", TRANSACTION_PRICE);
        aMap.put("τιμ", TRANSACTION_PRICE);
        aMap.put("_πε", GEO_AREA_ID);
        aMap.put("μεσίτες_δεκτοί", AGENTS_ACCEPTED);
        aMap.put("εμβαδόν", AREA);
        aMap.put("είδος", TYPE);
        aMap.put("bodyPhone", USER_ENTERED_TEXT);
        aMap.put("*", ADDON_TEXT_PAPER);
        aMap.put("itxt", ADDON_TEXT_INTERNET);
        aMap.put("txt", ADDON_TEXT_PAPER);
        aMap.put("μεσιτικό", IS_AGENT);
        aMap.put("συζητήσιμη", NEGOTIABLE);
        aMap.put("phone", PHONE);
        aMap.put("συντελεστής_δόμησης", BUILD_FACTOR);
        aMap.put("με_άδεια_οικοδομής", BUILD_LICENSE);
        aMap.put("άρτιο", WHOLE);
        aMap.put("οικοδομήσιμο", BUILDABLE);
        aMap.put("συντελεστής_κάλυψης", COVER_FACTOR);
        aMap.put("σχέδιο_πόλης", CITY_PLAN);
        aMap.put("κτίζει", BUILDABLE_AREA);
        aMap.put("απόσταση_από_θάλασσα", DISTANCE_TO_SHORE);
        aMap.put("διαστάσεις", DIMENSIONS);
        aMap.put("κλίση", TILT);
        aMap.put("περίφραξη", FENCED);
        aMap.put("παροχές", UTILITIES);
        aMap.put("γεώτρηση", DRILL);
        aMap.put("κτίσμα", BUILDING);
        aMap.put("θέα", VIEW);
        aMap.put("εμβαδόν_κτίσματος", BUILDING_AREA);
        aMap.put("περιέχει", CONTAINS);
        aMap.put("γωνιακό", CORNER);
        aMap.put("πρόσοψη", FRONT_DIMENSION);
        aMap.put("διαμπερές", THROUGH);
        aMap.put("περιοχή", PERIOXH);
        aMap.put("υποπεριοχή", YPOPERIOXH);
        aMap.put("οριοθέτηση", ORIOTHETISI);
        aMap.put("_εμ", EMBADON);
        aMap.put("οδός", ODOS);
        aMap.put("τοποθεσία", LOCATION);
        aMap.put("text", TEXT);
        fieldNamesMapper = Collections.unmodifiableMap(aMap);
    }
	
	public Land(String logEntryLine) throws Exception {
		String[] fields = logEntryLine.split(";");
		super.numberOfNonEmptyFields = fields.length;
		
		String[] secondaryFields = fields[0].split("\\^");
		if (secondaryFields.length < 2) {
			logger.error("Cannot parse line " + logEntryLine);
			throw new Exception();
		}
		String[] tt = secondaryFields[1].split(":");
		String type = tt[0];
		if (type.equals("Ενοικιάσεις Κατοικιών") || type.equals("Πωλήσεις Κατοικιών")) {
			throw new Exception();
		}
		else if (type.equals("Ενοικιάσεις Οικοπέδων") || type.equals("Πωλήσεις Οικοπέδων")) {
			this.itemType = "re_land";
		}
		else if (type.equals("Ενοικιάσεις Καταστημάτων") || type.equals("Πωλήσεις Καταστημάτων")) {
			throw new Exception();
		}
		else if (type.equals("Πωλ. Εξοχικών Κατοικιών") || type.equals("Ενοικ. Εξοχικών Κατοικιών")) {
			throw new Exception();
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
							logger.warn("Cannot identify area unit for \"" + parsableText + "\"");
							throw new Exception();
						}
						parsableText = mArea.group(1);
					}
					try {
						this.area = nf.parse(parsableText).floatValue();
						if (!isInSquareMeters) {
							this.area *= 1000.0f;
						}
					}
					catch (Exception ex) {
						logger.error("Cannot parse land_area (" + parsableText + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(FRONT_DIMENSION)) {
					try {
						this.front = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse front_dimension (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(BUILD_FACTOR)) {
					try {
						this.buildFactor = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse buildFactor (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(COVER_FACTOR)) {
					try {
						this.coverFactor = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse coverFactor (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(BUILDING_AREA)) {
					try {
						this.buildingArea = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse building_area (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(BUILDABLE_AREA)) {
					try {
						this.buildableArea = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse buildable_area (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(BUILD_LICENSE)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse build_license (" + v + ")");
						throw new Exception();
					}
					this.hasBuildLicense = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(WHOLE)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse whole (" + v + ")");
						throw new Exception();
					}
					this.isWhole = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(BUILDABLE)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse buildable (" + v + ")");
						throw new Exception();
					}
					this.isBuildable = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(DRILL)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse can_drill (" + v + ")");
						throw new Exception();
					}
					this.canDrill = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(BUILDING)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse contains_building (" + v + ")");
						throw new Exception();
					}
					this.hasBuilding = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(THROUGH)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse is_see_through (" + v + ")");
						throw new Exception();
					}
					this.through = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(DISTANCE_TO_SHORE)) {
					try {
						this.distanceToShore  = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse distance_to_shore (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(FENCED)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse fenced (" + v + ")");
						throw new Exception();
					}
					this.isFenced = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(CORNER)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse is_corner (" + v + ")");
						throw new Exception();
					}
					this.isCorner = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(TYPE)) {
					this.type = v;
				}
				else if (domainFieldName.equals(CITY_PLAN)) {
					this.cityPlan = v;
				}
				else if (domainFieldName.equals(DIMENSIONS)) {
					this.dimensions = v;
				}
				else if (domainFieldName.equals(UTILITIES)) {
					this.utilities = v;
				}
				else if (domainFieldName.equals(CONTAINS)) {
					this.contains = v;
				}
				else if (domainFieldName.equals(TILT)) {
					this.tilt = v;
				}
				else if (domainFieldName.equals(TYPE)) {
					this.type = v;
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
				else if (domainFieldName.equals(ODOS)) {
					this.odos = v;
				}
				else if (domainFieldName.equals(LOCATION)) {
					this.topothesia = v;
				}
				else if (domainFieldName.equals(VIEW)) {
					this.view = v;
				}
				else if (domainFieldName.equals(USER_ENTERED_TEXT)) {
					this.userEnteredText = v;
				}
				else if (domainFieldName.equals(ADDON_TEXT_INTERNET)) {
					this.extraTextInternet = v;
				}
				else if (domainFieldName.equals(TEXT)) {
					this.text = v;
				}
				else if (domainFieldName.equals(ADDON_TEXT_PAPER)) {
					this.extraTextPaper = v;
				}
				else if (domainFieldName.equals(PHONE)) {
					this.phone = v;
				}
				else if (domainFieldName.equals(GEO_AREA_ID)) {
					this.geoArea = v;
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
				else {
					logger.warn("Unknown field \"" + domainFieldName + "\"");
				}
			}
			
		}
	}
	
	public float getFront() {
		return this.front;
	}
	
	public boolean isCorner() {
		return this.isCorner;
	}
	
	public boolean isThrough() {
		return this.through;
	}
	
	public boolean hasBuilding() {
		return this.hasBuilding;
	}
	
	public float getBuildingArea() {
		return this.buildingArea;
	}
	
	public String getContains() {
		return this.contains;
	}
	
	public boolean canDrill() {
		return this.canDrill;
	}
	
	public String getUtilities() {
		return this.utilities;
	}
	
	public String getTilt() {
		return this.tilt;
	}
	
	public String getDimensions() {
		return this.dimensions;
	}
	
	public int getDistanceToShore() {
		return this.distanceToShore;
	}
	
	public float getBuildableArea() {
		return this.buildableArea;
	}
	
	public float getCoverFactor() {
		return this.coverFactor;
	}
	
	public String getCityPlan() {
		return this.cityPlan;
	}
	
	public boolean isWhole() {
		return this.isWhole;
	}
	
	public boolean isBuildable() {
		return this.isBuildable;
	}
	
	public boolean hasBuildLicense() {
		return this.hasBuildLicense;
	}
	
	public float getBuildFactor() {
		return this.buildFactor;
	}
	
	public float getArea() {
		return this.area;
	}
	
	public boolean isFenced() {
		return this.isFenced;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getView() {
		return this.view;
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
		sb.append(FRONT_DIMENSION); sb.append("\t");
		sb.append(THROUGH); sb.append("\t");
		sb.append(CORNER); sb.append("\t");
		sb.append(BUILDING); sb.append("\t");
		sb.append(BUILDING_AREA); sb.append("\t");
		sb.append(DRILL); sb.append("\t");
		sb.append(UTILITIES); sb.append("\t");
		sb.append(FENCED); sb.append("\t");
		sb.append(TILT); sb.append("\t");
		sb.append(VIEW); sb.append("\t");
		sb.append(DIMENSIONS); sb.append("\t");
		sb.append(DISTANCE_TO_SHORE); sb.append("\t");
		sb.append(BUILD_FACTOR); sb.append("\t");
		sb.append(BUILDABLE_AREA); sb.append("\t");
		sb.append(BUILD_LICENSE); sb.append("\t");
		sb.append(WHOLE); sb.append("\t");
		sb.append(BUILDABLE); sb.append("\t");
		sb.append(COVER_FACTOR); sb.append("\t");
		sb.append(CITY_PLAN); sb.append("\t");
		sb.append(CONTAINS); sb.append("\t");
		sb.append(AGENTS_ACCEPTED); sb.append("\t");
		sb.append(IS_AGENT); sb.append("\t");
		sb.append(LOCATION); sb.append("\t");
		sb.append(PERIOXH); sb.append("\t");
		sb.append(YPOPERIOXH); sb.append("\t");
		sb.append(ORIOTHETISI); sb.append("\t");
		sb.append(ODOS); 
		
		
		
		sb.append(newline);
		return sb.toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.userEnteredText); sb.append("\t");
		sb.append(this.itemType); sb.append("\t");
		sb.append(this.transactionType); sb.append("\t");
		sb.append(this.price == 0 ? "N/A" : this.price); sb.append("\t");
		sb.append(this.isNegotiable ? this.isNegotiable : "N/A"); sb.append("\t");
		sb.append(this.geoArea == null ? "N/A" : this.geoArea); sb.append("\t");
		sb.append(this.area == 0 ? "N/A" : this.area); sb.append("\t");
		sb.append(this.type == null ? "N/A" : this.type); sb.append("\t");
		sb.append(this.front == 0 ? "N/A" : this.front); sb.append("\t");
		sb.append(this.through); sb.append("\t");
		sb.append(this.isCorner); sb.append("\t");
		sb.append(this.hasBuilding); sb.append("\t");
		sb.append(this.buildingArea); sb.append("\t");
		sb.append(this.canDrill); sb.append("\t");
		sb.append(this.utilities == null ? "N/A" : this.utilities); sb.append("\t");
		sb.append(this.isFenced); sb.append("\t");
		sb.append(this.tilt == null ? "N/A" : this.tilt); sb.append("\t");
		sb.append(this.view == null ? "N/A" : this.view); sb.append("\t");
		sb.append(this.dimensions == null ? "N/A" : this.dimensions); sb.append("\t");
		sb.append(this.distanceToShore == 0 ? "N/A" : this.distanceToShore); sb.append("\t");
		sb.append(this.buildFactor == 0.0f ? "N/A" : this.buildFactor); sb.append("\t");
		sb.append(this.buildableArea == 0.0f ? "N/A" : this.buildableArea); sb.append("\t");
		sb.append(this.hasBuildLicense); sb.append("\t");
		sb.append(this.isWhole); sb.append("\t");
		sb.append(this.isBuildable); sb.append("\t");
		sb.append(this.coverFactor == 0.0f ? "N/A" : this.coverFactor); sb.append("\t");
		sb.append(this.cityPlan == null ? "N/A" : this.cityPlan); sb.append("\t");
		sb.append(this.contains == null ? "N/A" : this.contains); sb.append("\t");
		sb.append(this.isAgentsAccepted); sb.append("\t");
		sb.append(this.isAgent); sb.append("\t");
		sb.append(this.topothesia == null ? "N/A" : this.topothesia); sb.append("\t");
		sb.append(this.perioxh == null ? "N/A" : this.perioxh); sb.append("\t");
		sb.append(this.ypoperioxh == null ? "N/A" : this.ypoperioxh); sb.append("\t");
		sb.append(this.oriothetisi == null ? "N/A" : this.oriothetisi); sb.append("\t");
		sb.append(this.odos == null ? "N/A" : this.odos); 
		
		
		sb.append(newline);
		return sb.toString();
	}
	
}
