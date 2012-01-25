package preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalizeNewData {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String newline = System.getProperty("line.separator");
	private HashMap<String, String> typeMapper;
	private HashMap<String, String> subTypeMapper;
	private HashMap<String, String> stateMapper;
	private HashMap<String, String> substateMapper;
	
	
	public NormalizeNewData() {
		typeMapper = new HashMap<String, String>();
		typeMapper.put("diamerisma", "APARTMENT");
		typeMapper.put("orofodiamerisma", "APARTMENT");
		typeMapper.put("gkarsoniera", "APARTMENT");
		typeMapper.put("retire", "APARTMENT");
		typeMapper.put("doma", "APARTMENT");
		typeMapper.put("stoyntio", "APARTMENT");
		typeMapper.put("monokatoikia", "HOUSE");
		typeMapper.put("mezoneta", "SPLIT_LEVEL");
		typeMapper.put("bila", "œ… ≈…¡");
		typeMapper.put("oikia", "œ… ≈…¡");
		typeMapper.put("ktirio", "BUILDING");
		typeMapper.put("allo", "OTHER");
		
		subTypeMapper = new HashMap<String, String>();
		subTypeMapper.put("orofodiamerisma", "FLOORFLAT");
		subTypeMapper.put("retire", "PENTHOUSE");
		subTypeMapper.put("gkarsoniera", "SINGLEROOM");
		subTypeMapper.put("stoyntio", "SINGLEROOM");
		subTypeMapper.put("doma", "LOFT");
		
		stateMapper = new HashMap<String, String>();
		stateMapper.put("neodmito", "NEWBUILT");
		stateMapper.put("upo kataskeui", "UNDER_CONSTRUCTION");
		stateMapper.put("imiteles", "UNFINISHED");
		stateMapper.put("metaxeirismeno", "USED");
		stateMapper.put("metaxeirismeno arsisti katastas", "USED");
		stateMapper.put("metaxeirismeno anakainismeno", "USED");
		stateMapper.put("metaxeirismeno kali katastasi", "USED");
		
		substateMapper = new HashMap<String, String>();
		substateMapper.put("metaxeirismeno arsisti katastas", "PERFECT");
		substateMapper.put("metaxeirismeno anakainismeno", "REFURBISHED");
		substateMapper.put("metaxeirismeno kali katastasi", "GOOD");
//		substateMapper.put("˜ÒﬁÊÂÈ ·Ì·Í·ﬂÌÈÛÁÚ", "NEEDS_REPAIR");
	}
	
	public void normalize(File advFile, File outFile) {
		String[] floorValues = {"SH", "S1", "LH", "LHH", "L0", "L1", "L2", "L3", "L4", "L5", "L6", "L7", "L8"};
		HashSet<String> validFloorValues = new HashSet<String>(Arrays.asList(floorValues));
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			out.write("ADVID\tCREATION_DAY\tCREATION_MONTH\tCREATION_YEAR\tPUB_DAY\tPUB_MONTH\tPUB_YEAR\tTELEPHONE\tTRANSACTION_TYPE\t" + 
					"AREA_ID\tSIZE\tBEDROOMS\tFLOOR\tTYPE\tSUBTYPE\tSTATE\tSUBSTATE\tCONSTRUCTION_YEAR\tFROM_AGENT\tPARKING\t" + 
					"POOL\tGARDEN\tVIEW\tNEAR_METRO\tFIREPLACE\tSUN_VISORS\tSTORAGE\tINDEPENDENT_HEATING\tSUN_BOILER\tFURNISHED\tNATURAL_GAS\t" +
					"AIR_CONDITION\tPRIVATE_ROOF" +
					"\tPRICE" + newline);
//			out.write("0\t0\t0\t0\t0\t0\t0\t0\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\t0\t0\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tAAAAAAAAAAAAAAAAAAAAAAAAA\tPARKING\t0\tAAAAAAAAAAAAAAAAAAAAAAAAA\t0" + newline);
			NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
			try {
				BufferedReader in = new BufferedReader(new FileReader(advFile));
				logger.info("Reading file " + advFile.getAbsolutePath());
				String logEntryLine = in.readLine();
				String[] fieldNames = logEntryLine.split("\\t");
				HashMap<String, Integer> names2Index = new HashMap<String, Integer>();
				for (int i = 0; i < fieldNames.length; i ++) {
					names2Index.put(fieldNames[i], new Integer(i));
				}
				while ((logEntryLine = in.readLine()) != null) {
					fieldNames = logEntryLine.split("\\t");
					
					int indx = 0;
					try {
						indx = Integer.parseInt(fieldNames[names2Index.get("AID")]);
					}
					catch (Exception ex) {
						logger.debug("INVALID ADV_ID . Rejecting line \"" + logEntryLine + "\"");
					}
					
					int creationYear = 0;
					try {
						creationYear = Integer.parseInt(fieldNames[names2Index.get("CREATION_YEAR")]);
					}
					catch (Exception ex) {
						logger.debug("INVALID CREATION_YEAR . Rejecting line \"" + logEntryLine + "\"");
					}
					
					int creationMonth = 0;
					try {
						creationMonth = Integer.parseInt(fieldNames[names2Index.get("CREATION_MONTH")]);
					}
					catch (Exception ex) {
						logger.debug("INVALID CREATION_MONTH . Rejecting line \"" + logEntryLine + "\"");
					}
					
					int publicationYear = 0;
					try {
						publicationYear = Integer.parseInt(fieldNames[names2Index.get("PUB_YEAR")]);
					}
					catch (Exception ex) {
						logger.debug("INVALID PUBLICATION_YEAR . Rejecting line \"" + logEntryLine + "\"");
						continue;
					}
					
					if (publicationYear > 2010 || publicationYear < 2010) {
						logger.debug("OTHER PUBLICATION_YEAR . Rejecting line \"" + logEntryLine + "\"");
						continue;
					}
					int publicationMonth = 0;
					try {
						publicationMonth = Integer.parseInt(fieldNames[names2Index.get("PUB_MONTH")]);
					}
					catch (Exception ex) {
						logger.debug("INVALID PUBLICATION_MONTH . Rejecting line \"" + logEntryLine + "\"");
						continue;
					}
					
					String telephone = fieldNames[names2Index.get("Tilefono")].trim();
					if (telephone.equals("null") || telephone.length() < 10 || telephone.length() > 15) {
						telephone = fieldNames[names2Index.get("TEL")].trim().replace("...", "").replace(".", "");
						if (telephone.equals("null") || telephone.length() < 10 || telephone.length() > 15) {
							logger.debug("INVALID_TELEPHONE " + telephone + " . Rejecting line \"" + logEntryLine + "\"");
							continue;
						}
					}
					
					String transactionType = fieldNames[names2Index.get("TransactionType")].trim();
					if (transactionType.equals("RENT")) {
						transactionType = "LET";
					}
					else if (!transactionType.equals("SELL")) {
						logger.debug("INVALID_TRANSACTION_TYPE " + transactionType + " . Rejecting line \"" + logEntryLine + "\"");
						continue;
					}
					
					String areaID = fieldNames[names2Index.get("Area_ID")].trim();
					
					String size = fieldNames[names2Index.get("size")];
					Number n = null;
					try {
						n = nf.parse(size);
					}
					catch (Exception ex) {
//						logger.debug("INVALID_SIZE " + size + " . Rejecting line \"" + logEntryLine + "\"");
					}
					if (n == null || n.floatValue() <= 0.0f) {
						size = "N/A";
					}
					if (size.equals("N/A")) {
						continue;
					}
					
					String bedrooms = fieldNames[names2Index.get("bedrooms")];
					n = null;
					try {
						n = nf.parse(bedrooms);
					}
					catch (Exception ex) {
//						logger.debug("INVALID_NUMBER_OF_BEDROOMS " + size + " . Rejecting line \"" + logEntryLine + "\"");
					}
					if (n == null || n.intValue() <= 0) {
						bedrooms = "N/A";
					}
					
					String floor = fieldNames[names2Index.get("floor")];
					floor = validFloorValues.contains(floor) ? floor : "N/A";
					
					String tmp = typeMapper.get(fieldNames[names2Index.get("realestatesubtype")]);
					String type = tmp == null ? "N/A" : tmp;
					
					tmp = subTypeMapper.get(fieldNames[names2Index.get("realestatesubtype")]);
					String subType = tmp == null ? "N/A" : tmp;
					
					tmp = fieldNames[names2Index.get("katastasi")];
					String g = stateMapper.get(tmp);
					String state = g == null ? "N/A" : g;
					
					tmp = fieldNames[names2Index.get("katastasi")];
					g = substateMapper.get(tmp);
					String substate = g == null ? "N/A" : g;
					
					String neodm = fieldNames[names2Index.get("Flag_neodmito")];
					if (neodm.equals("1")) {
						state = "NEWBUILT";
						substate = "N/A";
					}
					
					String constructionYear = fieldNames[names2Index.get("contructionyear")];
					int conYear = 0;
					try {
						conYear = Integer.parseInt(constructionYear);
					}
					catch (Exception ex) {
//						logger.debug("INVALID_CONSTRUCTION_YEAR . Rejecting line \"" + logEntryLine + "\"");
					}
					if (conYear <= 0) {
						constructionYear = "N/A";
					}
					
					String price = fieldNames[names2Index.get("price")];
					n = null;
					try {
						n = nf.parse(price);
					}
					catch (Exception ex) {
//						logger.debug("INVALID_PRICE " + price + " . Rejecting line \"" + logEntryLine + "\"");
					}
					if (n == null || n.floatValue() <= 0.0f) {
						price = "N/A";
					}
					if (price.equals("N/A")) {
						continue;
					}
					
					boolean isAgent = fieldNames[names2Index.get("AGNT_ID")].startsWith("1") ? true : false;
					boolean parking = fieldNames[names2Index.get("parking")].equals("1") ? true : false;
					boolean pisina = fieldNames[names2Index.get("pisina")].equals("1") ? true : false;
					boolean kipos = fieldNames[names2Index.get("kipos")].equals("1") ? true : false;
					boolean thea;
					String view = fieldNames[names2Index.get("thea")];
					if (view.contains("thea thalassa") || view.contains("thea vouno") || view.contains("aperioristi thea")) {
						thea = true;
					}
					else {
						thea = false;
					}
					boolean metro = fieldNames[names2Index.get("metro")].equals("1") ? true : false;
					boolean tzaki = fieldNames[names2Index.get("tzaki")].equals("1") ? true : false;
					boolean tentes = fieldNames[names2Index.get("tentes")].equals("1") ? true : false;
					boolean apothiki = fieldNames[names2Index.get("apothiki")].equals("1") ? true : false;
					boolean autonomi_thermansi = fieldNames[names2Index.get("autonomi_thermansi")].equals("1") ? true : false;
					boolean iliakos = fieldNames[names2Index.get("iliakos")].equals("1") ? true : false;
					boolean epiplomeno = fieldNames[names2Index.get("epiplomeno")].equals("1") ? true : false;
					boolean fusiko_aerio = fieldNames[names2Index.get("fusiko_aerio")].equals("1") ? true : false;
					boolean klimatismos = fieldNames[names2Index.get("klimatismos")].equals("1") ? true : false;
					boolean idioktiti_taratsa = fieldNames[names2Index.get("idioktiti_taratsa")].equals("1") ? true : false;
								
					
					
					StringBuffer sb = new StringBuffer();
					sb.append(indx); sb.append("\t");
					sb.append("N/A"); sb.append("\t");
					sb.append(creationMonth); sb.append("\t");
					sb.append(creationYear); sb.append("\t");
					sb.append("N/A"); sb.append("\t");
					sb.append(publicationMonth); sb.append("\t");
					sb.append(publicationYear); sb.append("\t");
					sb.append(telephone); sb.append("\t");
					sb.append(transactionType); sb.append("\t");
					sb.append(areaID); sb.append("\t");
					sb.append(size); sb.append("\t");
					sb.append(bedrooms); sb.append("\t");
					sb.append(floor); sb.append("\t");
					sb.append(type); sb.append("\t");
					sb.append(subType); sb.append("\t");
					sb.append(state); sb.append("\t");
					sb.append(substate); sb.append("\t");
					sb.append(constructionYear); sb.append("\t");
					sb.append(isAgent); sb.append("\t");
					sb.append(parking); sb.append("\t");
					sb.append(pisina); sb.append("\t");
					sb.append(kipos); sb.append("\t");
					sb.append(thea); sb.append("\t");
					sb.append(metro); sb.append("\t");
					sb.append(tzaki); sb.append("\t");
					sb.append(tentes); sb.append("\t");
					sb.append(apothiki); sb.append("\t");
					sb.append(autonomi_thermansi); sb.append("\t");
					sb.append(iliakos); sb.append("\t");
					sb.append(epiplomeno); sb.append("\t");
					sb.append(fusiko_aerio); sb.append("\t");
					sb.append(klimatismos); sb.append("\t");
					sb.append(idioktiti_taratsa); sb.append("\t");
					sb.append(price);
					sb.append(newline);
					
					out.write(sb.toString());
					
				}
				in.close();
			}
			catch (Exception ex) {
				logger.error("Cannot read from file " + advFile.getAbsolutePath());
				System.exit(-1);
			}
			out.close();
		}
		catch (Exception ex) {
			logger.error("Cannot write to file " + outFile.getAbsolutePath());
			System.exit(-1);
		}
	}
	
	public static void main(String[] argv) {
		String advDir = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\New base\\residences_2010.txt";
		String newData = "\\\\x8\\internet\\Projects\\XE Property\\Statistics\\residences_2010.txt";
		
		NormalizeNewData ad = new NormalizeNewData();
		ad.normalize(new File(advDir), new File(newData));
		
	}
}
