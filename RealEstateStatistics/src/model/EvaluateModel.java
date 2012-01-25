package model;

import java.io.File;
import java.util.List;
import java.util.LinkedList;


public class EvaluateModel {
	public static void main(String[] argv) {
//		String trainFilename = "\\\\x8\\internet\\Projects\\XE\\Statistics\\New base\\002_Deduped_RealEstate_residence_2010_eksamino_2010_1_TRAIN.txt";
//		String testFilename = "\\\\x8\\internet\\Projects\\XE\\Statistics\\New base\\002_Deduped_RealEstate_residence_2010_eksamino_2010_1_TEST_SELL.txt";
		
		String trainFilename = "c:\\Data\\002_residences_deduped_2010_1.txt";
//		String trainFilename = "c:\\Data\\002_residences_2010_1_TRAIN.txt";
//		String testFilename = "c:\\Data\\002_residences_2010_1_TEST_SELL.txt";
		String configFilenameA = "c:\\Data\\deliverables\\config_REGRESSION.txt";
		String configFilenameB = "c:\\Data\\deliverables\\config_CLUSTERS.txt";
		String outFilename = "c:\\Data\\out.csv";
		
		
		AreaLinearRegression2 modelA = new AreaLinearRegression2();
		try {
			modelA.readConfig(new File(configFilenameA));
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		
		
		modelA.train(new File(trainFilename));
		
		try {
			modelA.writeModel();
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		
/*	
		try {
			modelA.readModels();
		}
		catch (Exception ex) {
			System.exit(-1);
		}
	*/	
		
		ClusterBasedPricePrediction modelB = new ClusterBasedPricePrediction();
		try {
			modelB.readConfig(new File(configFilenameB));
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		
		modelB.train(new File(trainFilename));
		
		try {
			modelB.writeModel();
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		
		/*
		try {
			modelB.readModels();
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		*/
		
		/*
		List<RealEstatePricePredictor> l = new LinkedList<RealEstatePricePredictor>();
		l.add(modelA); l.add(modelB);
		ModelCombiner mc = null;
		try {
			mc = new ModelCombiner(l);
		}
		catch (Exception ex) {
			System.exit(-1);
		}
		
		mc.calculateError(new File(testFilename), new File(outFilename));
		
		*/
		
//		BaselinePricePredictor modelC = new BaselinePricePredictor();
//		modelC.readAreas(new File(parentsFilename));
//		AreaLinearRegression2 modelA = new AreaLinearRegression2();
//		modelA.readAreas(new File(parentsFilename));
//		modelA.readFallbackAreaIds(new File(fallbackAreaIDsFilename));
//		AreaLinearRegression2 modelB = new AreaLinearRegression2();
//		modelB.readAreas(new File(parentsFilename));
//		AreaIDByTypeModel modelC = new AreaIDByTypeModel();
//		modelA.calculateError(new File(testFilename), new File(outFilename));
//		modelA.writeModel(new File(modelFilename));
//		List<RealEstatePricePredictor> models = new LinkedList<RealEstatePricePredictor>();
//		models.add(modelA);
//		models.add(modelB);
// 		models.add(modelC);
//		ModelCombiner mc = new ModelCombiner(models);
		
//		mc.train(new File(trainFilename));
//		mc.calculateRMSE(new File(testFilename));
	}
}
