package NetflixPrize;


public class CorrelationsRanker<E> implements Comparable<CorrelationsRanker<E>>{
	private E index;
	private double value;

	public CorrelationsRanker(E i, double v){
		index = i;
		value = v;
	}

	public E getIndex(){
		return index;
	}
	public double getValue(){
		return value;
	}
	
	public int compareTo(CorrelationsRanker<E> av){
		if (Math.abs(value) - Math.abs(av.getValue()) < 0)
			return 1;
		else if (Math.abs(value) - Math.abs(av.getValue()) > 0)
			return -1;
		return 0;
	}
}
