package model;

import java.util.List;
import java.util.LinkedList;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModelCombiner extends RealEstatePricePredictor {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private List<RealEstatePricePredictor> models;
	
	public ModelCombiner(List<RealEstatePricePredictor> models) throws Exception {
		this.models = models;
		String transactionType = this.models.get(0).transactionType;
		for (int i = 1; i < this.models.size(); i ++) {
			if (!transactionType.equalsIgnoreCase(this.models.get(i).transactionType)) {
				logger.error("Models need to have the same transaction type");
				throw new Exception();
			}
		}
		this.transactionType = transactionType;
		
		String itemType = this.models.get(0).itemType;
		for (int i = 1; i < this.models.size(); i ++) {
			if (!itemType.equalsIgnoreCase(this.models.get(i).itemType)) {
				logger.error("Models need to have the same item type");
				throw new Exception();
			}
		}
		this.itemType = itemType;
		this.discountFactor = 1.0f;
	}
	
	public void readModels() throws Exception {
		for (RealEstatePricePredictor m : this.models) {
			m.readModels();
		}
	}
	
	public void writeModel() throws Exception {
		for (RealEstatePricePredictor m : this.models) {
			m.writeModel();
		}
	}
	
	public void train(File advFile) {
		for (RealEstatePricePredictor model : models) {
			model.train(advFile);
		}
	}
	
	public Prediction predict(RealEstate re) {
		double avgPred = 0.0;
		double count = 0.0;
		float avgConfidence = 0.0f;
		Prediction.Type type = Prediction.Type.MAIN;
		
		List<Prediction> preds = new LinkedList<Prediction>();
		for (RealEstatePricePredictor model : models) {
			Prediction pred = model.predict(re);
			
			preds.add(pred);
		}
		
		for (Prediction pred : preds) {
			if (pred.getType().equals(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION.toString())) {
				type = Prediction.Type.UNABLE_TO_GENERATE_PREDICTION;
				break;
			}
			else if (type != Prediction.Type.GLOBAL && pred.getType().equals(Prediction.Type.FALLBACK.toString())) {
				type = Prediction.Type.FALLBACK;
			}
			else if (pred.getType().equals(Prediction.Type.GLOBAL.toString())) {
				type = Prediction.Type.GLOBAL;
			}
			
			avgPred += pred.getValue();
			avgConfidence += pred.getConfidence();
			count += 1.0f;
		}
		if (count == 0.0 || type == Prediction.Type.UNABLE_TO_GENERATE_PREDICTION) {
			return new Prediction(Prediction.Type.UNABLE_TO_GENERATE_PREDICTION, 0.0f, 0.0f);
		}
		else {
			return new Prediction(type, (float)(avgPred / count), (float) (avgConfidence / count));
		}
	}
}
