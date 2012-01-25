import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class RegExResidenceModel extends ResidenceModel {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	protected static NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);  // Parse numbers according to locale
	
	private Pattern multipleRE = Pattern.compile(".+1\\).+2\\).+");
	private Pattern areaLeft = Pattern.compile("(.*) (\\d+) τ\\.?μ(.*)");
	private Pattern areaLeftB = Pattern.compile("(\\d+) τ\\.?μ(.*)");
	
	private Pattern landArea = Pattern.compile(".+ σε οικόπεδο (\\d+) τ\\.?μ.(.*)");
	private Pattern landAreaStremmata = Pattern.compile(".+ σε οικόπεδο (\\d+) στρεμμάτων(.*)");
	
	private Pattern apartmentTypeA = Pattern.compile(".+ διαμέρισμα .+");
	private Pattern apartmentTypeB = Pattern.compile(".+οροφοδιαμέρισμα.+");
	private Pattern apartmentTypeC = Pattern.compile(".+γκαρσονιέρα.+");
	private Pattern apartmentTypeD = Pattern.compile(".+ ρετιρέ .+");
	private Pattern apartmentTypeE = Pattern.compile(".+ δυάρι[,\\.]?.+");
	private Pattern apartmentTypeF = Pattern.compile(".+ τριάρι[,\\.]?.+");
	private Pattern apartmentTypeG = Pattern.compile(".+ τεσσάρι[,\\.]?.+");
	private Pattern apartmentTypeH = Pattern.compile(".+ 2άρι[,\\.].+");
	private Pattern apartmentTypeI = Pattern.compile(".+ 3άρι[,\\.].+");
	private Pattern apartmentTypeJ = Pattern.compile(".+ 4άρι[,\\.].+");
	private Pattern mezonetaType = Pattern.compile(".+ μεζονέτα .+");
	private Pattern ktirioType = Pattern.compile(".+\\Wκτίριο\\W.+");
	private Pattern oikiaTypeA = Pattern.compile(".+\\Wοικία.+");
	private Pattern oikiaTypeB = Pattern.compile(".+\\Wκατοικία.+");
	private Pattern oikiaTypeC = Pattern.compile(".+\\Wβίλα\\W.+");
	private Pattern monokatoikiaType = Pattern.compile(".+μονοκατοικία.+");
	
	private Pattern conditionA = Pattern.compile(".+ νεόδμητ.+");
	private Pattern conditionB = Pattern.compile(".+ημιτελ[ήέ]ς.+");
	private Pattern conditionC = Pattern.compile(".+υπό κατασκευή.+");
	private Pattern conditionD = Pattern.compile(".+άριστη[ς]? κατάσταση[ς]?.+");
	private Pattern conditionE = Pattern.compile(".+καλή[ς]? κατάσταση[ς]?.+");
	private Pattern conditionF = Pattern.compile(".+ανακαινισμέν[οη].+");
	private Pattern conditionG = Pattern.compile(".+χρήζει ανακαίνισης.+");
	private Pattern conditionH = Pattern.compile(".+καινούρ[γ]?ιο.+");
	private Pattern conditionI = Pattern.compile(".+ (\\d+)ετίας.+");
	
	private Pattern hasLoftA = Pattern.compile(".+ δώμα.+");
	
	private Pattern loftArea = Pattern.compile(".+ με δώμα (\\d+) τ\\.?μ..+");
	
	private Pattern bedroomsΑ = Pattern.compile(".+(\\d+) υ/δ.+");
	private Pattern bedroomsΒ = Pattern.compile(".+(\\d+) υπνοδωμάτια.+");
	
	private Pattern bathroomsA = Pattern.compile(".+(\\d+) μπάνια.+");
	private Pattern bathroomsB = Pattern.compile(".+μπάνιο.+");
	
	private Pattern wcA = Pattern.compile(".+(\\d+) wc[\\W].+");
	private Pattern wcB = Pattern.compile(".+ wc[\\W].+");
	
	private Pattern masterBedroomsA = Pattern.compile(".+ (\\d+) master\\W.+");
	
	private Pattern floorA = Pattern.compile(".+ισ[οό]γε[ίι].+");
	private Pattern floorB = Pattern.compile(".+ ημιυπ[όο]γει.+");
	private Pattern floorC = Pattern.compile(".+ 1ο[υς].+");
	private Pattern floorD = Pattern.compile(".+ 2ο[υς].+");
	private Pattern floorE = Pattern.compile(".+ 3ο[υς].+");
	private Pattern floorF = Pattern.compile(".+ 4ο[υς].+");
	private Pattern floorG = Pattern.compile(".+ 5ο[υς].+");
	private Pattern floorH = Pattern.compile(".+ 6ο[υς].+");
	private Pattern floorI = Pattern.compile(".+ 7ο[υς].+");
	private Pattern floorJ = Pattern.compile(".+ 8ο[υς].+");
	private Pattern floorK = Pattern.compile(".+ ημιώροφ.+");
	private Pattern floorL = Pattern.compile(".+ πρώτου.+");
	private Pattern floorM = Pattern.compile(".+ δευτέρου.+");
	private Pattern floorN = Pattern.compile(".+ τρίτου.+");
	private Pattern floorO = Pattern.compile(".+ τετάρτου.+");
	private Pattern floorP = Pattern.compile(".+ πέμπτου.+");
	private Pattern floorQ = Pattern.compile(".+ έκτου.+");
	private Pattern floorR = Pattern.compile(".+ εβδόμου.+");
	private Pattern floorS = Pattern.compile(".+ ογδόου.+");
	private Pattern floorT = Pattern.compile(".+ υπ[οό]γει.+");
	private Pattern floorU = Pattern.compile(".+ υπερυψωμένο ισόγειο.+");
	
	private Pattern priceA = Pattern.compile(".+ (\\d+(\\.\\d+)*) ευρώ(.*)");
	private Pattern priceB = Pattern.compile("(.*)τιμή (\\d+(\\.\\d+)*)");
	
	private Pattern solarBoiler = Pattern.compile(".+ηλιακός.+");
	
	private Pattern pool = Pattern.compile(".+πισίνα.+");
	
	private Pattern garden = Pattern.compile(".+\\Wκήπο[ς\\s,\\.].+");
	
	private Pattern solarVisors = Pattern.compile(".+\\Wτέντες.+");
	
	private Pattern fireplace = Pattern.compile(".+\\Wτζάκι.+");
	
	private Pattern storage = Pattern.compile(".+\\Wαποθήκη.+");
	
	private Pattern storageArea = Pattern.compile(".+\\Wαποθήκη (\\d+) τ\\.?μ\\.?.+");
	
	
	private Pattern constructionYearA = Pattern.compile(".+κατασκευή[ς]? [']?(\\d{2,4})\\W.+");
	
	private Pattern parkingA = Pattern.compile(".+ πάρκιν\\W.+");
	private Pattern parkingB = Pattern.compile(".+ γκαράζ\\W.+");
	private Pattern parkingC = Pattern.compile(".+ χώρος στάθμευσης\\W.+");
	private Pattern parkingD = Pattern.compile(".+ parking\\W.+");
	
	private Pattern autonomousHeatingA = Pattern.compile(".+αυτ[όο]νομ.+ θέρμανση[ς]?\\W.+");
	
	private Pattern secureDoorA = Pattern.compile(".+πόρτα ασφαλείας\\W.+");
	
	private Pattern acA = Pattern.compile(".+a/?c\\W.+");
	private Pattern acB = Pattern.compile(".+air[ ]?condition\\W.+");
	private Pattern acC = Pattern.compile(".+κλιματισμό[ς]?\\W.+");
	
	private Pattern naturalGas = Pattern.compile(".+φυσικ.+ α[έε]ριο.+");
	
	private Pattern levelsA = Pattern.compile(".+(\\d+) επ[ίι]π[εέ]δ.+");
	
	private Pattern negotiableA = Pattern.compile(".+συζητήσιμη.+");
	
	
	public Residence process(String text) {
		Residence residence = new Residence();
		
		residence.setUserEnteredText(text);
		if (isMultipleRE(text)) {
			return residence;
		}
		
		int area = getArea(text);
		residence.setArea(area);
		
		int landArea = getLandArea(text);
		residence.setLandArea(landArea);
		
		int price = getPrice(text);
		residence.setPrice(price);
		
		boolean isNegotiable = getNegotiable(text);
		residence.setNegotiable(isNegotiable);
		
		String type = getType(text);
		residence.setType(type);
		
		String subtype = getSubtype(text);
		residence.setSubtype(subtype);
		
		boolean hasLoft = getLoft(text);
		residence.setHasLoft(hasLoft);
		
		int loftArea = getLoftArea(text);
		residence.setLoftArea(loftArea);
		
		String condition = getCondition(text);
		residence.setCondition(condition);
		
		String conditionUsed = getSubcondition(text);
		residence.setSubcondition(conditionUsed);
		
		int bedrooms = getBedrooms(text);
		residence.setBedrooms(bedrooms);
		
		int masterBedrooms = getMasterBedrooms(text);
		residence.setMasterBedrooms(masterBedrooms);
		
		int bathrooms = getBathrooms(text);
		residence.setBathrooms(bathrooms);
		
		int wc = getWC(text);
		residence.setWC(wc);
		
		String floor = getFloor(text);
		residence.setFloor(floor);
		
		boolean hasSolarBoiler = getSolarBoiler(text);
		residence.setSolarBoiler(hasSolarBoiler);
		
		boolean hasPool = getPool(text);
		residence.setPool(hasPool);
		
		boolean hasGarden = getPool(text);
		residence.setGarden(hasGarden);
		
		boolean hasSolarVisors = getSolarVisors(text);
		residence.setSolarVisors(hasSolarVisors);
		
		boolean hasFireplace = getFireplace(text);
		residence.setFireplace(hasFireplace);
		
		boolean hasStorage = getStorage(text);
		residence.setStorage(hasStorage);
		
		int storageArea = getStorageArea(text);
		residence.setStorageArea(storageArea);
		
		int constructionYear = getConstructionYear(text);
		residence.setConstructionYear(constructionYear);
		
		boolean hasParking = getParking(text);
		residence.setParking(hasParking);
		
		boolean hasAutonomousHeating = getAutonomousHeating(text);
		residence.setAutonomousHeating(hasAutonomousHeating);
		
		boolean hasSecureDoor = getSecureDoor(text);
		residence.setSecureDoor(hasSecureDoor);
		
		boolean hasAC = getAC(text);
		residence.setAC(hasAC);
		
		boolean hasNaturalGas = getNaturalGas(text);
		residence.setNaturalGas(hasNaturalGas);

		int levels = getLevels(text);
		residence.setLevels(levels);
		
		return residence;
	}
	
	
	public boolean isMultipleRE(String text) {
		Matcher m = multipleRE.matcher(text);
		
		if (m.matches()) {
//			logger.info(text);
			return true;
		}
		
		return false;
	}
	
	public int getArea(String text) {
		String[] segments = text.split(",");
		int area = 0;
		for (String s : segments) {
			Matcher m = areaLeft.matcher(s.trim());
			if (!m.matches()){
				continue;
			}
			String context = m.group(1);
			if (context.contains("σε οικόπεδο")) {
//				logger.info(text);
				continue;
			}
			String areaString = m.group(2);
			try {
				area = nf.parse(areaString).intValue();
				break;
			}
			catch (Exception ex) {
//				logger.error("Cannot parse area (" + areaString + ")");
//				return 0.0f;
			}
		}
		
		
		// If you haven't found an area try with a different pattern
		if (area == 0) {
			for (String s : segments) {
				Matcher m = areaLeftB.matcher(s.trim());
				if (!m.matches()){
					continue;
				}
				try {
					area = nf.parse(m.group(1)).intValue();
					break;
				}
				catch (Exception ex) {

				}
			}
		}
		return area;
	}
	
	public int getLandArea(String text) {
		int area = 0;
		Matcher m = landArea.matcher(text.trim());
		if (!m.matches()){
			// Try to match the stremmata regex
			m = landAreaStremmata.matcher(text.trim());
			if (!m.matches()){
				return 0;
			}
			try {
				area = nf.parse(m.group(1)).intValue();
				area *= 1000;
			}
			catch (Exception ex) {
				return 0;
			}
		}
		try {
			area = nf.parse(m.group(1)).intValue();
		}
		catch (Exception ex) {

		}
		return area;
	}
	
	public int getPrice(String text) {
		Matcher m = priceA.matcher(text);
		if (!m.matches()) {
			// Search for another pattern
			m = priceB.matcher(text);
			if (!m.matches()) {
				return 0;
			}
			int price = 0;
			try {
				price = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {

			}
			
			return price;
		}
		
		int price = 0;
		try {
			price = nf.parse(m.group(1)).intValue();
		}
		catch (Exception ex) {

		}
		
		return price;
	}
	
	public String getType(String text) {
		Matcher m = apartmentTypeA.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeB.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeC.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeD.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeE.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeF.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeG.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeH.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeI.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = apartmentTypeJ.matcher(text);
		if (m.matches()) {
			return "διαμέρισμα";
		}
		m = mezonetaType.matcher(text);
		if (m.matches()) {
			return "μεζονέτα";
		}
		m = ktirioType.matcher(text);
		if (m.matches()) {
			return "κτίριο";
		}
		m = oikiaTypeA.matcher(text);
		if (m.matches()) {
			return "οικία";
		}
		m = oikiaTypeB.matcher(text);
		if (m.matches()) {
			return "οικία";
		}
		m = oikiaTypeC.matcher(text);
		if (m.matches()) {
			return "οικία";
		}
		m = monokatoikiaType.matcher(text);
		if (m.matches()) {
			return "μονοκατοικία";
		}
		return null;
	}
	
	public String getSubtype(String text) {
		Matcher m = apartmentTypeB.matcher(text);
		if (m.matches()) {
			return "οροφοδιαμέρισμα";
		}
		m = apartmentTypeC.matcher(text);
		if (m.matches()) {
			return "γκαρσονιέρα";
		}
		m = apartmentTypeD.matcher(text);
		if (m.matches()) {
			return "ρετιρέ";
		}
		
		return null;
	}
	
	public boolean getLoft(String text) {
		Matcher m = hasLoftA.matcher(text);
		if (m.matches()) {
			return true;
		}
		return false;
	}
	
	public int getLoftArea(String text) {
		Matcher m = loftArea.matcher(text);
		int area = 0;
		if (m.matches()) {
			try {
				area = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}
		return area;
	}
	
	public String getCondition(String text) {
		Matcher m = conditionA.matcher(text);
		if (m.matches()) {
			return "νεόδμητο";
		}
		m = conditionB.matcher(text);
		if (m.matches()) {
			return "ημιτελές";
		}
		m = conditionC.matcher(text);
		if (m.matches()) {
			return "υπό κατασκευή";
		}
		m = conditionD.matcher(text);
		if (m.matches()) {
			return "μεταχειρισμένο";
		}
		m = conditionE.matcher(text);
		if (m.matches()) {
			return "μεταχειρισμένο";
		}
		m = conditionF.matcher(text);
		if (m.matches()) {
			return "μεταχειρισμένο";
		}
		m = conditionG.matcher(text);
		if (m.matches()) {
			return "μεταχειρισμένο";
		}
		m = conditionH.matcher(text);
		if (m.matches()) {
			return "μεταχειρισμένο";
		}
		m = conditionI.matcher(text);
		if (m.matches()) {
			return "μεταχειρισμένο";
		}
		
		return null;
	}
	
	public String getSubcondition(String text) {
		Matcher m = conditionD.matcher(text);
		if (m.matches()) {
			return "άριστη κατάσταση";
		}
		m = conditionE.matcher(text);
		if (m.matches()) {
			return "καλή κατάσταση";
		}
		m = conditionF.matcher(text);
		if (m.matches()) {
			return "ανακαινισμένο";
		}
		return null;
	}
	
	public int getBedrooms(String text) {
		Matcher m = bedroomsΑ.matcher(text);
		int bdr = 0;
		if (m.matches()) {
			try {
				bdr = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}
		else {
			m = bedroomsΒ.matcher(text);
			bdr = 0;
			if (m.matches()) {
				try {
					bdr = nf.parse(m.group(1)).intValue();
				}
				catch (Exception ex) {
					return 0;
				}
			}
		}
		
		return bdr;
	}
	
	public int getBathrooms(String text) {
		Matcher m = bathroomsA.matcher(text);
		int btr = 0;
		if (m.matches()) {
			try {
				btr = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}
		else {
			m = bathroomsB.matcher(text);
			if (m.matches()) {
				return 1;
			}
			
		}
		return btr;
	}
	
	public int getWC(String text) {
		Matcher m = wcA.matcher(text);
		int wc = 0;
		if (m.matches()) {
			try {
				wc = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}
		else {
			m = wcB.matcher(text);
			if (m.matches()) {
				return 1;
			}
			
		}
		return wc;
	}
	
	public boolean getNegotiable(String text) {
		Matcher m = negotiableA.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public String getFloor(String text) {
		Matcher m = floorA.matcher(text);
		if (m.matches()) {
			return "L0";
		}
		m = floorK.matcher(text);
		if (m.matches()) {
			return "LH";
		}
		m = floorT.matcher(text);
		if (m.matches()) {
			return "S1";
		}
		m = floorB.matcher(text);
		if (m.matches()) {
			return "SH";
		}
		m = floorU.matcher(text);
		if (m.matches()) {
			return "LHH";
		}
		m = floorC.matcher(text);
		if (m.matches()) {
			return "L1";
		}
		m = floorD.matcher(text);
		if (m.matches()) {
			return "L2";
		}
		m = floorE.matcher(text);
		if (m.matches()) {
			return "L3";
		}
		m = floorF.matcher(text);
		if (m.matches()) {
			return "L4";
		}
		m = floorG.matcher(text);
		if (m.matches()) {
			return "L5";
		}
		m = floorH.matcher(text);
		if (m.matches()) {
			return "L6";
		}
		m = floorI.matcher(text);
		if (m.matches()) {
			return "L7";
		}
		m = floorJ.matcher(text);
		if (m.matches()) {
			return "L8";
		}
		m = floorL.matcher(text);
		if (m.matches()) {
			return "L1";
		}
		m = floorM.matcher(text);
		if (m.matches()) {
			return "L2";
		}
		m = floorN.matcher(text);
		if (m.matches()) {
			return "L3";
		}
		m = floorO.matcher(text);
		if (m.matches()) {
			return "L4";
		}
		m = floorQ.matcher(text);
		if (m.matches()) {
			return "L5";
		}
		m = floorP.matcher(text);
		if (m.matches()) {
			return "L5";
		}
		m = floorQ.matcher(text);
		if (m.matches()) {
			return "L6";
		}
		m = floorR.matcher(text);
		if (m.matches()) {
			return "L7";
		}
		m = floorS.matcher(text);
		if (m.matches()) {
			return "L8";
		}
		return null;
	}
	
	public boolean getSolarBoiler(String text) {
		Matcher m = solarBoiler.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getPool(String text) {
		Matcher m = pool.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getGarden(String text) {
		Matcher m = garden.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getSolarVisors(String text) {
		Matcher m = solarVisors.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getFireplace(String text) {
		Matcher m = fireplace.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getStorage(String text) {
		Matcher m = storage.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public int getStorageArea(String text) {
		Matcher m = storageArea.matcher(text);
		int area = 0;
		if (m.matches()) {
			try {
				area = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}
		return area;
	}
	
	public int getConstructionYear(String text) {
		Matcher m = constructionYearA.matcher(text);
		int year = 0;
		if (!m.matches()) {
			m = conditionI.matcher(text);
			if (!m.matches()){
				return year;
			}
			try {
				year = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
			return CURRENT_YEAR - year;
		}
		
		
		try {
			year = nf.parse(m.group(1)).intValue();
		}
		catch (Exception ex) {
			return 0;
		}
		if (year <= 20) {
			year += 2000;
		}
		else if (year <= 100){
			year += 1900;
		}
		return year;
	}
	
	public int getMasterBedrooms(String text) {
		Matcher m = masterBedroomsA.matcher(text);
		int masterBedrooms = 0;
		if (m.matches()) {
			try {
				masterBedrooms = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}
		return masterBedrooms;
	}
	
	public boolean getParking(String text) {
		Matcher m = parkingA.matcher(text);
		if (m.matches()) {
			return true;
		}
		m = parkingB.matcher(text);
		if (m.matches()) {
			return true;
		}
		m = parkingC.matcher(text);
		if (m.matches()) {
			return true;
		}
		m = parkingD.matcher(text);
		if (m.matches()) {
			return true;
		}
		return false;
	}
	
	public boolean getAutonomousHeating(String text) {
		Matcher m = autonomousHeatingA.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getSecureDoor(String text) {
		Matcher m = secureDoorA.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getAC(String text) {
		Matcher m = acA.matcher(text);
		if (m.matches()) {
			return true;
		}
		m = acB.matcher(text);
		if (m.matches()) {
			return true;
		}
		m = acC.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public boolean getNaturalGas(String text) {
		Matcher m = naturalGas.matcher(text);
		if (m.matches()) {
			return true;
		}
		
		return false;
	}
	
	public int getLevels(String text) {
		Matcher m = levelsA.matcher(text);
		int levels = 0;
		if (m.matches()) {
			try {
				levels = nf.parse(m.group(1)).intValue();
			}
			catch (Exception ex) {
				return 0;
			}
		}
		return levels;
	}
	
}
