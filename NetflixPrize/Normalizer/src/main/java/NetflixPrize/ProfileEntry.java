package NetflixPrize;


public class ProfileEntry implements Comparable<ProfileEntry>{

    private int index;
    private float value;

    public ProfileEntry(int i, float v) {
        index = i;
        value = v;
    }

    public int getIndex() {
        return index;
    }

    public float getValue() {
        return value;
    }
    public int compareTo(ProfileEntry av){
		if (index - av.getIndex() < 0)
			return -1;
		else if (index - av.getIndex() > 0)
			return 1;
		return 0;
	}
}
