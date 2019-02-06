package be.kuleuven.liris.sequencemining.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.kuleuven.liris.sequencemining.concept.Trace;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaPackageManager;

public class WekaRun<T> extends ExperimentRun<T>{

	public enum Classifier {
		NB,RandomForest,SVM//,DecisionTree,Logistic
	}
	
	public static Classifier c = Classifier.NB;
	
	public WekaRun(ArrayList<Trace<T>> traces, Collection<T> features, int nFold, int noClasses, int noRuns, String name) {
		super(traces, features, nFold, noClasses, noRuns, name);
	}

	public Map<String,Double> call()
			throws Exception {
//		HashSet<T> allConstraints = new HashSet<T>();
		List<String> labels = new ArrayList<String>();
				
		for (Trace<T> t : traces) {
//			Set<T> toAdd = new HashSet<T>();
//			for (T s : t.getConstraints())
//				toAdd.add(s);
//			t.setConstraints(toAdd);
//			for (T c : toAdd)
//				allConstraints.add(c);
			if (!labels.contains(Integer.toString(t.getLabel())))
				labels.add(Integer.toString(t.getLabel()));
		}
		
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		ArrayList<String> levels = new ArrayList<String>();
		levels.add("1");
		levels.add("0");
		for (T con : features) {
			Attribute a = new Attribute(con.toString());
			attributes.add(a);
		}
		attributes.add(new Attribute("label", labels));

		// Crossvalidate with Weka
		Instances trainingSet = new Instances("TrainingSet", attributes, traces.size());
		trainingSet.setClassIndex(attributes.size() - 1);
		Instances testSet = new Instances("TrainingSet", attributes, traces.size());
		testSet.setClassIndex(attributes.size() - 1);
		
		for (int t = 0; t < traces.size(); t++) {
			if (t % nFold != 0) {
				Instance instance = new DenseInstance(attributes.size());
				Collection<String> strings = traces.get(t).getConstraintsAsStrings();
				for (int a = 0; a < attributes.size() - 1; a++) {
					if (strings.contains(attributes.get(a).name())) {
						instance.setValue(attributes.get(a), 0);
					} else{
						instance.setValue(attributes.get(a), 1);
					}
				}
				instance.setValue(attributes.get(attributes.size() - 1),
						Integer.toString(traces.get(t).getLabel()));
				trainingSet.add(instance);
			} else {
				Instance instance = new DenseInstance(attributes.size());
				Collection<String> strings = traces.get(t).getConstraintsAsStrings();
				for (int a = 0; a < attributes.size() - 1; a++) {
					if (strings.contains(attributes.get(a).name())){
						instance.setValue(attributes.get(a), 0);
					}else{
						instance.setValue(attributes.get(a), 1);
					}
				}
				instance.setValue(attributes.get(attributes.size() - 1),
						Integer.toString(traces.get(t).getLabel()));
				testSet.add(instance);
			}
		}
		
		//System.out.println("Reasoning");
		WekaPackageManager.loadPackages(false, true, false);
		AbstractClassifier classifier = null;
		String options = "";
		switch(c){
			case NB:
				classifier = new NaiveBayes();//new LibSVM();
				options = "";
				break;
			case RandomForest:
				classifier = new RandomForest();
				options = "";
				break;
			case SVM:
				classifier = new LibSVM();
				//K: 0=linear, 1=polynomial, 2=radial basis, 3=sigmoid
				options = "-K,1,-W,";
				for(int i =0;i<labels.size();i++)
					options+= "1 ";
				break;
		}		
		String[] optionsArray = options.split(",");
		classifier.setOptions(optionsArray);
		classifier.buildClassifier(trainingSet);
		
		Map<String,Double> result = new HashMap<String,Double>();
		
		//attributeScoring(trainingSet);
		//System.out.println(classifier.toSummaryString());
		//System.out.println(classifier.toString());
		
		Evaluation eTest = new Evaluation(trainingSet);
		eTest.evaluateModel(classifier, testSet);
		
		//double auc = eTest.areaUnderROC(classIndex);
		//System.out.println("AUC: "+auc);
		double accuracy = (double) eTest.correct() / (double) trainingSet.size();
		result.put("accuracy", accuracy);
		return result;
	}

	public static void attributeScoring(Instances tr)
	{
		try{
			double e1,e2,e3;
			InfoGainAttributeEval as = new InfoGainAttributeEval();
			//AttributeEvaluator as2 = new GainRatioAttributeEval();
			//AttributeEvaluator as3 = new ChiSquaredAttributeEval();

			as.buildEvaluator(tr);
			for(int i=0;i<tr.numAttributes();i++)
			{
				e1= as.evaluateAttribute(i);
				//e2= as2.evaluateAttribute(i);
				//e3= as3.evaluateAttribute(i);
				System.out.println(tr.attribute(i).name()+","+tr.attribute(i).toString()+e1);
			}
		}catch(Exception e)
		{
			System.out.println("Exception thrown in attributeScoring = "+e.toString());
		}
	}
	
}
