package model;

public class Prediction {
	public enum Type {MAIN, FALLBACK, GLOBAL, UNABLE_TO_GENERATE_PREDICTION}
	Type type;
	float predictedValue;
	float confidence;
	
	public Prediction(Type t, float v) {
		type = t;
		predictedValue = v;
		confidence = 1.0f;
	}
	
	public Prediction(Type t, float v, float c) {
		type = t;
		predictedValue = v;
		confidence = c;
	}
	
	public float getValue() {
		return predictedValue;
	}
	
	public float getConfidence() {
		return confidence;
	}
	
	public String getType() {
		return type.toString();
	}
	
}
