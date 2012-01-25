import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Professional {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private static final Map<String, String> fieldNamesMapper;
	
	private int parkingSlots;
	private float parkingArea;
	
	private static final String AREA = "Item.total_area";
	private static final String RAMP = "Item.has_loading_ramp";
	private static final String CABLING = "Item.has_structured_cabling";
	private static final String HEIGHT = "Item.height";
	private static final String PARKING_AREA = "Item.parking_area";
	private static final String PARKING_SLOTS = "Item.parking_slots";
	
    static {
    	Map<String, String> aMap = new HashMap<String, String>();
    	aMap.put("ράμπα", RAMP);
    	aMap.put("δομημένη_καλωδίωση", CABLING);
        aMap.put("ύψος", HEIGHT);
        aMap.put("εμβαδόν_πάρκιν", PARKING_AREA);
        aMap.put("θέσεις_πάρκιν", PARKING_SLOTS);
        fieldNamesMapper = Collections.unmodifiableMap(aMap);
    }
    
    public Professional(String logEntryLine) throws Exception {
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
				
				
				if (domainFieldName.equals(PARKING_AREA)) {
					try {
					this.parkingArea = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse parking_area (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(HEIGHT)) {
					try {
						this.height = nf.parse(v).floatValue();
					}
					catch (Exception ex) {
						logger.error("Cannot parse height (" + v + ")");
						throw new Exception();
					}
				}
				else if (domainFieldName.equals(RAMP)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse ramp (" + v + ")");
						throw new Exception();
					}
					this.hasLoadingRamp = c == 1 ? true : false;
				}
				else if (domainFieldName.equals(CABLING)) {
					int c;
					try {
						c = Integer.parseInt(v);
					}
					catch (Exception ex) {
						logger.error("Cannot parse has_cabling (" + v + ")");
						throw new Exception();
					}
					this.hasCabling = c == 1 ? true : false;
				}
			}
		}
    }
}
