package preprocess;

/**
 * A single reference rating
 * 
 * @author kboulis
 *
 */
public class Rating {
	private short movie;
	private byte rating;  // Reference rating takes integer values between 1-5
	private byte year;	  // Only a few years are present
	private byte month;
	private byte day;
	
	
	public Rating(short m, byte r, byte y, byte mo, byte d){
		movie = m;
		rating = r;
		year = y;
		month = mo;
		day = d;
	}

	public short getMovie(){
		return movie;
	}
	
	public byte getRating(){
		return rating;
	}
	
	public byte getYear(){
		return year;
	}
	
	public byte getMonth(){
		return month;
	}
	
	public byte getDay(){
		return day;
	}
	
	public String getDate(){
		StringBuffer sb = new StringBuffer();
		int intYear = (int) year + 1998;
		sb.append(intYear); sb.append("-"); sb.append(month);
		sb.append("-"); sb.append(day);
		return sb.toString();
	}
}
