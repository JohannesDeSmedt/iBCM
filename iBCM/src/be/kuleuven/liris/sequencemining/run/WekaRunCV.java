package be.kuleuven.liris.sequencemining.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

public class WekaRunCV<T> extends ExperimentRun<T> {

	public enum Classifier {
		NB, RandomForest, SVM// ,DecisionTree,Logistic
	}

	public static Classifier c = Classifier.NB;

	public WekaRunCV(ArrayList<Trace<T>> traces, Collection<T> features, int nFold, int noClasses, int noRuns, String name) {
		super(traces, features, nFold, noClasses, noRuns, name);
	}

	public Map<String,Double> call() throws Exception {
		List<String> labels = new ArrayList<String>();

		for (Trace<T> t : traces) {
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
		int classIndex = attributes.size() - 1;
		Instances trainingSet = new Instances("TrainingSet", attributes, traces.size());
		trainingSet.setClassIndex(classIndex);
		
		for (int t = 0; t < traces.size(); t++) {
			Instance instance = new DenseInstance(attributes.size());
			Collection<String> strings = traces.get(t).getConstraintsAsStrings();
			for (int a = 0; a < attributes.size() - 1; a++) {
				if (strings.contains(attributes.get(a).name()))
					instance.setValue(attributes.get(a), 0);
				else
					instance.setValue(attributes.get(a), 1);
			}
			instance.setValue(attributes.get(attributes.size() - 1), Integer.toString(traces.get(t).getLabel()));
			trainingSet.add(instance);
		}
		
		// Print data
		String sep = File.separator;
		String outputLocation = System.getProperty("user.dir") + sep + "logs" + sep + "Featurized" + sep + name + ".csv";
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputLocation));
		for(int at = 0;at<attributes.size()-1;at++)
			bw.write("f"+(int) at+",");
		bw.write("label");
		bw.write("\n");
		
		for(int ti=0;ti<trainingSet.size();ti++){
			Instance i = trainingSet.get(ti);
			for(int ai=0;ai<attributes.size();ai++){
				Attribute a = attributes.get(ai);
				if(ai<attributes.size()-1)
					bw.write((int) i.value(a)+",");
				else
					bw.write((int) i.value(a)+"");
			}			
			if(ti<trainingSet.size()-1)
				bw.write("\n");
		}
		
//		for(Instance i: trainingSet){
//			for(Attribute a: attributes)
//				bw.write(i.value(a)+",");
//			bw.write("\n");
//		}
		bw.flush();
		bw.close();
		
		//Map<String,Double> result = new HashMap<String,Double>();
		//result.put("accuracy",0.0);
		
		//System.out.println(trainingSet.attribute(classIndex).toString());
		


		WekaPackageManager.loadPackages(false, true, false);
		AbstractClassifier classifier = null;
		String options = "";
		switch (c) {
		case NB:
			classifier = new NaiveBayes();// new LibSVM();
			options = "";
			break;
		case RandomForest:
			classifier = new RandomForest();
			options = "";
			break;
		// case DecisionTree:
		// classifier = new J48();
		// options = "";
		// break;
		case SVM:
			classifier = new LibSVM();
			// K: 0=linear, 1=polynomial, 2=radial basis, 3=sigmoid
			options = "-K,3,-W,";
			for (int i = 0; i < labels.size(); i++)
				options += "1 ";
			break;
		}
		String[] optionsArray = options.split(",");
		classifier.setOptions(optionsArray);
		classifier.buildClassifier(trainingSet);

		
		Map<String,Double> result = new HashMap<String,Double>();
		
		// attributeScoring(trainingSet);
		// System.out.println(classifier.toSummaryString());
		// System.out.println(classifier.toString());

		Evaluation eTest = new Evaluation(trainingSet);
		eTest.crossValidateModel(classifier, trainingSet, nFold, new Random());
		//eTest.evaluateModel(classifier);

		// double auc = eTest.areaUnderROC(classIndex);
		// System.out.println("AUC: "+auc);
		double accuracy = (double) eTest.correct() / (double) trainingSet.size();
		result.put("accuracy", accuracy);
		//result.put("f-measure", eTest.fMeasure(classIndex));
		
		
		return result;
		
	}

	public static void attributeScoring(Instances tr) {
		try {
			double e1, e2, e3;
			InfoGainAttributeEval as = new InfoGainAttributeEval();
			// AttributeEvaluator as2 = new GainRatioAttributeEval();
			// AttributeEvaluator as3 = new ChiSquaredAttributeEval();

			as.buildEvaluator(tr);
			for (int i = 0; i < tr.numAttributes(); i++) {
				e1 = as.evaluateAttribute(i);
				// e2= as2.evaluateAttribute(i);
				// e3= as3.evaluateAttribute(i);
				System.out.println(tr.attribute(i).name() + "," + tr.attribute(i).toString() + e1);
			}
		} catch (Exception e) {
			System.out.println("Exception thrown in attributeScoring = " + e.toString());
		}
	}

}
