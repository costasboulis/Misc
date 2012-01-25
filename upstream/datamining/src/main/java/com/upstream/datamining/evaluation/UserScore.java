package com.upstream.datamining.evaluation;

public class UserScore implements Comparable<UserScore>{
	private int userId;
	private float score;
	
	public UserScore(int u, float s) {
		userId = u;
		score = s;
	}
	
	public float getScore() {
		return score;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public int compareTo(UserScore us) {
		float diff = this.score - us.getScore();
		if (diff < 0) {
			return 1;
		}
		else if (diff > 0) {
			return -1;
		}
		return 0;
	}
}
