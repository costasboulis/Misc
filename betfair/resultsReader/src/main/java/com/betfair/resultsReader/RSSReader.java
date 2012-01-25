package com.betfair.resultsReader;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Date;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO: problem with 12:00 PM and time zones. It is interpreting it as 12:00 AM
// TODO: Some events do not have Match Odds while they do have other markets. Need to check for this


// Fire up every 24 hours

public class RSSReader extends DefaultHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private boolean newItem;
	private boolean newLink;
	private boolean newMarket;
	private boolean newWinner;
	private StringBuffer text;
	private String event;
	private String market;
	private String winner;
	public static String newline = System.getProperty("line.separator");
	private static Pattern matchOddsPattern = Pattern.compile("(.+) Match Odds (.*)settled");
	private static Pattern matchOddsHTPattern = Pattern.compile("(.+) Half Time settled");
	private static Pattern correctScorePattern = Pattern.compile("(.+) Correct Score settled");
	private static Pattern correctScoreHTPattern = Pattern.compile("(.+) Half Time Score settled");
	private static Pattern sendingOffPattern = Pattern.compile("(.+) Sending Off.? settled");
	private static Pattern winnerPattern = Pattern.compile("Winner\\(s\\): (.+)");
//	private static Pattern timePattern = Pattern.compile("Fixtures (.+) / (.+) v (.+) - (\\d\\d?:\\d\\d (AM|PM))$");
	private static Pattern timePattern = Pattern.compile("Fixtures (.+) / (.+) v (.+) - (\\d\\d?:\\d\\d)");
	private URL link;
	private HashMap<String, List<MarketResult>> results;
	
	
	private class MarketResult {
		private String marketName;
		private String winner;
		private URL link;
		
		public MarketResult(URL u, String m, String w) {
			link = u;
			marketName = m;
			winner = w;
		}
		
		public String getMarketName() {
			return marketName;
		}
		
		public String getWinner() {
			return winner;
		}
		
		public URL getLink() {
			return link;
		}
	}
	
	public RSSReader() {
		newItem = false;
		newLink = false;
		newMarket = false;
		text = new StringBuffer();
		results = new HashMap<String, List<MarketResult>>();
	}
	
	public void parseDocument(URL u){
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			
			InputStream raw = u.openStream();
			InputSource in = new InputSource(raw);
			sp.parse(in, this);
			
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
			System.exit(1);
		}
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("item")){
			newItem = true;
		}
		else if ((newItem) && (qName.equals("link"))){
			newLink = true;
		}
		else if ((newItem) && (qName.equals("title"))){
			newMarket = true;
		}
		else if ((newItem) && (qName.equals("description"))){
			newWinner = true;
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		text.append(ch,start,length);
	}
	
	private String getCharacters(){
		String retval = text.toString ();
		text.setLength (0);
		return retval;
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String localText = getCharacters().trim();
		if (qName.equals("item")){
			newItem = false;
			if (market == null) {
				return;
			}
			MarketResult mr = new MarketResult(link, market, winner);
			List<MarketResult> mrList = results.get(event);
			if (mrList == null) {
			    mrList = new LinkedList<MarketResult>();
			    mrList.add(mr);
			    results.put(event, mrList);
			}
			else {
			    mrList.add(mr);
			}
			link = null;
			market = null;
			event = null;
			winner = null;
		}
		else if ((newItem) && (qName.equals("link"))){
			try {
				link = new URL(localText);
			}
			catch (MalformedURLException e){
				e.printStackTrace();
				link = null;
			}
			newLink = false;
		}
		else if ((newItem) && (qName.equals("title"))){
			newMarket = false;
			Matcher m = matchOddsPattern.matcher(localText);
			if (m.find()){
			    event = m.group(1);
				market = "Match Odds";
				return;
			}
			else {
			    m = matchOddsHTPattern.matcher(localText);
	            if (m.find()){
	                event = m.group(1);
	                market = "Match Odds (HT)";
	                return;
	            }
	            else {
	                m = correctScorePattern.matcher(localText);
	                if (m.find()){
	                    event = m.group(1);
	                    market = "Correct Score";
	                    return;
	                }
	                else {
	                    m = sendingOffPattern.matcher(localText);
	                    if (m.find()){
	                        event = m.group(1);
	                        market = "Sending Off";
	                        return;
	                    }
	                    m = correctScoreHTPattern.matcher(localText);
	                    if (m.find()){
	                        event = m.group(1);
	                        market = "Correct Score (HT)";
	                        return;
	                    }
	                    else {
	                        market = null;
	                    }
	                }
	            }
			    
			}
			
		}
		else if ((newItem) && (qName.equals("description"))){
			newWinner = false;
			Matcher m = winnerPattern.matcher(localText);
			if (m.find()){
				winner = m.group(1);
			}
			else {
				winner = "UNKNOWN_WINNER";
			}
		}
	}
	
	public String writeResults() {
	    StringBuffer sb = new StringBuffer();
	    for (String eventName: results.keySet()) {
	    	Matcher m = timePattern.matcher(eventName);
	    	String day = null;
	    	String homeTeam = null;
	    	String awayTeam = null;
	    	String time = null;
	    	sb.append("\""); sb.append(eventName); sb.append("\"");
	    	if (m.find()){
				day = m.group(1);
				homeTeam = m.group(2);
				awayTeam = m.group(3);
				time = m.group(4);
				String[] tmpTime = time.split(" ");
				String[] tmpTime2 = tmpTime[0].split(":");
				int hour = Integer.parseInt(tmpTime2[0]);
/*				if (tmpTime[1].equalsIgnoreCase("PM") && hour != 12) {
					hour += 12;
				}
				if (tmpTime[1].equalsIgnoreCase("AM") && hour == 12) {
					hour = 0;
				}
	*/			
				int minutes = Integer.parseInt(tmpTime2[1]);
				String[] tmpDay = day.split(" ");
				int d = Integer.parseInt(tmpDay[0]);
				int month = 0;
				if (tmpDay[1].equalsIgnoreCase("January")){
					month = 0;
				}
				else if (tmpDay[1].equalsIgnoreCase("February")){
					month = 1;
				}
				else if (tmpDay[1].equalsIgnoreCase("March")){
					month = 2;
				}
				else if (tmpDay[1].equalsIgnoreCase("April")){
					month = 3;
				}
				else if (tmpDay[1].equalsIgnoreCase("May")){
					month = 4;
				}
				else if (tmpDay[1].equalsIgnoreCase("June")){
					month = 5;
				}
				else if (tmpDay[1].equalsIgnoreCase("July")){
					month = 6;
				}
				else if (tmpDay[1].equalsIgnoreCase("August")){
					month = 7;
				}
				else if (tmpDay[1].equalsIgnoreCase("September")){
					month = 8;
				}
				else if (tmpDay[1].equalsIgnoreCase("October")){
					month = 9;
				}
				else if (tmpDay[1].equalsIgnoreCase("November")){
					month = 10;
				}
				else if (tmpDay[1].equalsIgnoreCase("December")){
					month = 11;
				}
				Calendar cldr = new GregorianCalendar(TimeZone.getTimeZone("Etc/Greenwich"));
				cldr.set(110 + 1900, month, d, hour, minutes, 0);
				sb.append(",\""); sb.append(cldr.getTime().toString()); sb.append("\"");
		    	sb.append(",\""); sb.append(homeTeam); sb.append("\"");
		    	sb.append(",\""); sb.append(awayTeam); sb.append("\"");
			}
	    	else {
	    		sb.append(",\"\",\"\",\"\",\"\"");
	    	}
	    	
	    	boolean matchOddsMarketFound = false;
	    	boolean correctScoreFound = false;
	    	String cScore = null;
	    	for (MarketResult mr : results.get(eventName)) {
	    		if (mr.getMarketName().equals("Match Odds")){
	    			matchOddsMarketFound = true;
	    		}
	    		else if (mr.getMarketName().equals("Correct Score")){
	    			correctScoreFound = true;
	    			cScore = mr.getWinner();
	    		}
	            sb.append(",\"" + mr.getMarketName() + "\",\"" + mr.getWinner() + "\"");
	        }
	       
	        
	        if (!matchOddsMarketFound && !correctScoreFound){
	        	logger.error("Could not find \"Match Odds\" market for " + eventName);
	        }
	        else if (!matchOddsMarketFound && correctScoreFound) {
	        	logger.warn("No \"Match Odds\" market but \"Correct Score\" present for " + eventName);
	        	if (cScore.equals("Any Unquoted")) {
	        		logger.error("Cannot deduce winner from \"Any Unquoted\" correct score result for " + eventName);
	        		sb.append(newline);
	        		continue;
	        	}
	        	String[] tms = cScore.split(" - ");
	        	int htGoals = Integer.parseInt(tms[0]);
	        	int aGoals = Integer.parseInt(tms[1]);
	        	String winner = "UNKNOWN_WINNER";
	        	if (htGoals == aGoals) {
	        		winner = "The Draw";
	        	}
	        	else if (htGoals > aGoals) {
	        		winner = homeTeam;
	        	}
	        	else {
	        		winner = awayTeam;
	        	}
	        	sb.append(",\"Match Odds\",\"" + winner + "\"");
	        }
	        
	        sb.append(newline);
	    }
	    return sb.toString();
    }
	
    public static void main( String[] args ) {
    	while (true) {
    		// Soccer events only
        	String path = "http://rss.betfair.com/RSS.aspx?format=rss&sportID=1";
        	URL u = null;
        	try {
        		u = new URL(path);
        	}
        	catch (MalformedURLException e){
    			e.printStackTrace();
    			System.exit(-1);
    		}
        	
        	RSSReader kd = new RSSReader();
        	kd.parseDocument(u);
        	Date currentDate = new Date();
        	String resultsFilename = "c:\\Betfair\\Results\\" + "results_" + 
        								currentDate.toString().replace(' ', '_').replace(':', '_') + ".txt";
        	File f = new File(resultsFilename);
        	try {
        		PrintWriter out = new PrintWriter(new FileWriter(f));
        		out.println(kd.writeResults());
        		out.close();
        		System.out.println("Wrote results for " + currentDate.toString());
        	}
        	catch (Exception e){
        		e.printStackTrace();
        		break;
        	}
        	try {
        		Thread.sleep(24 * 3600 * 1000);
        	}
        	catch (Exception e) {
        		break;
        	}
    	}
    	
    }
}
