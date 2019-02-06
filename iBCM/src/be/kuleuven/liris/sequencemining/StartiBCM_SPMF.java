package be.kuleuven.liris.sequencemining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import be.kuleuven.liris.sequencemining.concept.ConstraintConversions;
import be.kuleuven.liris.sequencemining.concept.ConstraintFeature;
import be.kuleuven.liris.sequencemining.concept.Trace;
import be.kuleuven.liris.sequencemining.run.ConstraintMiningThreadWindowed;
import be.kuleuven.liris.sequencemining.run.ExperimentRun;
import be.kuleuven.liris.sequencemining.run.WekaRun;
import be.kuleuven.liris.sequencemining.run.WekaRun.Classifier;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import be.kuleuven.liris.sequencemining.run.WekaRunCV;

public class StartiBCM_SPMF {

	final static String sep = File.separator;
	final static boolean VERBOSE = false;

	private static int noThreads = 8;

	private static int nFold = 10;
	private static int noClasses;
	private static String mainDir = System.getProperty("user.dir") + sep + "logs";
	private static String[] datasets;
	private static List<Double> supports;
	private static int compression = 1;
	private static double minSup = 0.8;
	private static int oversamplingRate = 1;
	private static long time = 0;
	private static boolean absence = true;

	private static HashMap<String, Character> itemMapping = new HashMap<String, Character>();
	private static HashMap<Character, String> reverseMapping = new HashMap<Character, String>();

	private static HashMap<Integer,Collection<ConstraintFeature>> claFeatures 
		= new HashMap<Integer,Collection<ConstraintFeature>>();
	private static Collection<ConstraintFeature> features;
	
	public static void main(String[] args) throws Exception {
		mainDir = mainDir + sep + "ISM_data";
		
		parseOptions(args);
		
		run("pioneer", 0.001, 1, 10, 10, null);
	}
	
	public static Pair<Double, Integer> run(String dataset, double minSupport
			, int noRuns, int noVal, int noWin, BufferedWriter bw) throws Exception {
		minSup = minSupport;
		File f1 = new File(mainDir + sep + dataset+ ".dat");
		File f2 = new File(mainDir + sep + dataset+ ".lab");
		
		long localTime = 0;
		
		List<Trace<ConstraintFeature>> orgTraces = new ArrayList<Trace<ConstraintFeature>>();
		for(int noR=0;noR<noRuns;noR++){
			orgTraces = new ArrayList<Trace<ConstraintFeature>>();
			orgTraces = readFileAndLabelTraces(f1, f2, noWin);
			localTime+= time;
		}
		time = localTime/noRuns;
		System.out.println("Mined in "+(time/1000));

		//This is just for printing
		if(VERBOSE){
			for (ConstraintFeature f : features) {
				String print = f.toString().replace(Character.toString(f.getAntecedent()),
						reverseMapping.get(f.getAntecedent()));
				if (f.getConsequent() != ' ')
					print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
				System.out.print(", " + print);
			}
		}
		//System.out.println("\n");
		
		// Retain only joint features (not guaranteed by set?)
		Collection<ConstraintFeature> jointFeatures = new HashSet<ConstraintFeature>();
		boolean fir = true;
		for(Integer i: claFeatures.keySet()){
			if(fir){
				for(ConstraintFeature f: claFeatures.get(i))
					jointFeatures.add(f);
				fir = false;
			}
			jointFeatures.retainAll(claFeatures.get(i));
		}
		//System.out.println("\nRedundants: ");
		for(ConstraintFeature f: jointFeatures){
			String print = f.toString().replace(Character.toString(f.getAntecedent()),
					reverseMapping.get(f.getAntecedent()));
			if (f.getConsequent() != ' ')
				print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
			//System.out.println(print);
		}
		//System.out.println("Joint Features: "+jointFeatures.size());
		for(Integer i: claFeatures.keySet())
			claFeatures.get(i).removeAll(jointFeatures);
			
		
		features = new HashSet<ConstraintFeature>();
		for(Integer cla: claFeatures.keySet()){
			features.addAll(claFeatures.get(cla));
			//System.out.println("\n#Features: " + claFeatures.get(cla).size()+" for "+cla);
			for(ConstraintFeature f: claFeatures.get(cla)){
				String print = f.toString().replace(Character.toString(f.getAntecedent()),
						reverseMapping.get(f.getAntecedent()));
				if (f.getConsequent() != ' ')
					print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
				//System.out.println(print);
			}
			//System.out.println("-\n");
		}
		System.out.println("-------\nTotal #features: "+features.size()+"\n");
		
		System.out.println("\nFeatures before reduction:");
		if(VERBOSE){
			for(ConstraintFeature f: features){
			String print = f.toString().replace(Character.toString(f.getAntecedent()),
					reverseMapping.get(f.getAntecedent()));
			if (f.getConsequent() != ' ')
				print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
				System.out.println(print);
			}	
		}
		System.out.println("______");
			
		long time_reduce_b = System.currentTimeMillis();
		reduceFeatureSpace();
		long time_reduce = System.currentTimeMillis()-time_reduce_b;
		
		if(VERBOSE){
			for(ConstraintFeature f: features){
			String print = f.toString().replace(Character.toString(f.getAntecedent()),
					reverseMapping.get(f.getAntecedent()));
			if (f.getConsequent() != ' ')
				print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
				System.out.println(print);
			}	
		}
		System.out.println("______");
		
		// Constraint pruning
		ArrayList<Trace<ConstraintFeature>> traces = new ArrayList<Trace<ConstraintFeature>>();
		for (Trace<ConstraintFeature> t : orgTraces) {
			traces.add(new Trace<ConstraintFeature>(1, t.getLabel(), t.getConstraints(), t.getTraceAsString()));
		}
		
//		if(bw!=null){
//		// Check information gain
//			bw.append(dataset+","+minSupport);
//			attributeScoring(traces, bw);
//			bw.append("\n");
//		}
		
		double accuracy = 0.0;
		if(noVal>0){
			for(Classifier c: WekaRun.Classifier.values()){
				if((features.size()*orgTraces.size())<=70_000_000){
					System.out.println("Classifier: "+c);
					WekaRun.c = c;
					ExperimentRun<ConstraintFeature> exRun = new WekaRunCV<ConstraintFeature>(traces, features, nFold, noClasses,
							noVal, "");
					accuracy = exRun.runTests().get("accuracy");
					System.out.println("Average accuracy: " + accuracy);
				}else{
					accuracy = 0.0;
				}
				if(bw!=null)
					bw.append(dataset + "," + c + "," + minSupport + "," + accuracy + ","
						+ features.size() + "," + time + ","+ noWin +","+ time_reduce);//getConstraintString() + "\n");
			}
		}
		if(bw!=null)
			bw.append(dataset + "," + "NA" + "," + minSupport + "," + accuracy + ","
				+ features.size() + "," + time + ","+ noWin +","+ time_reduce + "\n");
		//System.out.println("Average accuracy: " + accuracy);
		return new ImmutablePair<Double, Integer>(accuracy, features.size());
	}

	@SuppressWarnings("resource")
	private static List<Trace<ConstraintFeature>> readFileAndLabelTraces(File fD, File fL, int win)
			throws InterruptedException, ExecutionException, IOException {
		int i = 0;
		time = 0;
		List<Trace<ConstraintFeature>> traces = new ArrayList<Trace<ConstraintFeature>>();

		int c = 1000;
		HashMap<Character, Integer> charCount = new HashMap<Character, Integer>();

		ArrayList<String> logTraces = new ArrayList<String>();
		itemMapping = new HashMap<String,Character>();
		features = new HashSet<ConstraintFeature>();
		claFeatures = new HashMap<Integer,Collection<ConstraintFeature>>();
		
		List<String> lines = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		Set<String> labelSet = new HashSet<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(fD));
		
		String line = "";
		while((line=br.readLine())!= null)
			lines.add(line);		
		br = new BufferedReader(new FileReader(fL));
		line = "";
		while((line=br.readLine())!= null){
			labels.add(line);
			labelSet.add(line);
		}
		noClasses = labelSet.size();
		
		for(String lab: labelSet){
			for(Character ch: charCount.keySet())
				charCount.put(ch, 0);
			logTraces = new ArrayList<String>();
			System.out.println("****\n\nLabel:" + lab);
			
			for(int ls=0;ls<labels.size();ls++){
				if(labels.get(ls).equals(lab) && ls<lines.size()){					
					line = lines.get(ls);
					line = line.replace(" -1 -2", "");
					
					String[] items1 = line.split(" -1 ");
					String[] items = Arrays.copyOfRange(items1, 0, items1.length-1);

					//String[] items = line.split(" -1 ");
					
					String charLine = "";
					for (String item : items) {
						char itemSTC;
						if (itemMapping.containsKey(item)) {
							itemSTC = itemMapping.get(item);
						} else {
							itemSTC = (char) c;
							c++;
							itemMapping.put(item, itemSTC);
							reverseMapping.put(itemSTC, item);
						}
						charLine += itemSTC;
					}
					for(int cI=0;cI<charLine.length();cI++){
						Character cc = charLine.charAt(cI);
						if(charCount.containsKey(cc))
							charCount.put(cc,charCount.get(cc)+1);
						else
							charCount.put(cc, 1);
					}
					logTraces.add(charLine);		
				}
			}
			List<Character> nonRedundantActivities = new ArrayList<Character>();
			for (Character cc : charCount.keySet()) {
				if (charCount.get(cc) >= (logTraces.size() * minSup)){
					nonRedundantActivities.add(cc);
				}
			}
			Collections.sort(nonRedundantActivities);
			if(VERBOSE)
				System.out.println("Alphabetsize: " + itemMapping.keySet().size());
				System.out.println("#NonRedundantActivities: "+nonRedundantActivities.size());
				System.out.println("Trace size: " + logTraces.size());
			traces.addAll(minePhased(logTraces, nonRedundantActivities, i, win));
			i++;
			System.out.println("#Features "+features.size());
		}
		return traces;
	}

	private static List<Trace<ConstraintFeature>> minePhased(ArrayList<String> logTraces,
			List<Character> nonRedundantActivities, int label, int win) throws InterruptedException, ExecutionException {
		List<Trace<ConstraintFeature>> traces = new ArrayList<Trace<ConstraintFeature>>();

		long begin = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(noThreads);
		List<ConstraintMiningThreadWindowed> tasks = new ArrayList<ConstraintMiningThreadWindowed>();

		//features = new HashSet<ConstraintFeature>();
		HashMap<ConstraintFeature, Integer> featCoun = new HashMap<ConstraintFeature, Integer>();

		int tooShort = 0;
		int noPhases = 1;
		Collection<ConstraintFeature> classFeatures = new HashSet<ConstraintFeature>();
		int chunkSize = logTraces.size() / noPhases;
		for (int phase = 0; phase < noPhases; phase++) {
			executor = Executors.newFixedThreadPool(noThreads);
			tasks = new ArrayList<ConstraintMiningThreadWindowed>();
			for (int tt = (chunkSize * phase); tt < chunkSize * (phase + 1); tt++) {
				if (tt % compression == 0) {
					String trace = logTraces.get(tt);
					if(trace.length()>=win)
						tasks.add(new ConstraintMiningThreadWindowed(trace, label, nonRedundantActivities,  absence, win));
					else
						tooShort++;
				}
			}
			for (int osr = 0; osr < oversamplingRate; osr++) {
				for (Future<Trace<ConstraintFeature>> future : executor.invokeAll(tasks)) {
					Trace<ConstraintFeature> t = future.get();
					traces.add(t);
					for (ConstraintFeature f : t.getConstraints()) {
						if (featCoun.containsKey(f)) {
							featCoun.put(f, featCoun.get(f) + 1);
						} else {
							featCoun.put(f, 1);
						}
					}
					classFeatures.addAll(t.getConstraints());
				}
			}
		}
		time += (System.currentTimeMillis()-begin);
		System.out.println("Too short: "+tooShort);
		//System.out.println("Time spent: " + ((System.currentTimeMillis() - begin)) / 1000);
		executor.shutdown();

		Collection<ConstraintFeature> tR = new HashSet<ConstraintFeature>();
		for (ConstraintFeature f : classFeatures) {
			if (featCoun.get(f) < (traces.size() * minSup))
				tR.add(f);
		}
		classFeatures.removeAll(tR);
		if(VERBOSE){
			for(ConstraintFeature f: classFeatures){
				String print = f.toString().replace(Character.toString(f.getAntecedent()),
						reverseMapping.get(f.getAntecedent()));
				if (f.getConsequent() != ' ')
					print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
				System.out.println(print);
			}		
		}
		//if(VERBOSE)
			System.out.println("Features " + classFeatures.size() + " for " + label);
		claFeatures.put(label, classFeatures);
		features.addAll(classFeatures);
		return traces;
	}
	
	public static <T> void attributeScoring(ArrayList<Trace<T>> traces, BufferedWriter bw) throws Exception{
		List<String> labels = new ArrayList<String>();
		HashMap<String,Double> gainPerConstraintType = new HashMap<String,Double>();
		HashMap<String, Integer> constraintTypeSupport = calculateConstraintSupport(true);
		
		ConstraintConversions cc = new ConstraintConversions();
		for(String s: cc.getSingles())
			gainPerConstraintType.put(s, 0.0);
		
		HashMap<Attribute,ConstraintFeature> attot = new HashMap<Attribute,ConstraintFeature>(); 
		
		for (Trace<T> t : traces) 
			if (!labels.contains(Integer.toString(t.getLabel())))
				labels.add(Integer.toString(t.getLabel()));
				
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		ArrayList<String> levels = new ArrayList<String>();
		levels.add("1");
		levels.add("0");
		for (ConstraintFeature con : features) {
			Attribute a = new Attribute(con.toString());
			attributes.add(a);
			attot.put(a, con);
		}
		attributes.add(new Attribute("label", labels));

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
			instance.setValue(attributes.get(attributes.size() - 1),
					Integer.toString(traces.get(t).getLabel()));
			trainingSet.add(instance);
		}
			
		double e1=0.0;
		InfoGainAttributeEval as = new InfoGainAttributeEval();

		as.buildEvaluator(trainingSet);
		for(int i=0;i<trainingSet.numAttributes()-1;i++){
			e1= as.evaluateAttribute(i);
			String constraint = attot.get(trainingSet.attribute(i)).getConstraintName();
			gainPerConstraintType.put(constraint, e1+gainPerConstraintType.get(constraint));			
			//System.out.println(constraint+" -> "+e1);
		}
		for(String con: gainPerConstraintType.keySet()){
			double score = gainPerConstraintType.get(con)/(double) constraintTypeSupport.get(con);
			gainPerConstraintType.put(con, score);
			//if(gainPerConstraintType.get(con)>0)
			//System.out.println("Con "+con+" "+score);
			bw.append(","+score);
		}
	}

	private static String getConstraintString() {		
		HashMap<String, Integer> constraintTypeSupport = calculateConstraintSupport(false);

		String output = "";
		for (String s : constraintTypeSupport.keySet()){
			if(VERBOSE)
				System.out.println(s + " appears " + constraintTypeSupport.get(s) + " times");
			output+=","+ constraintTypeSupport.get(s);
		}
//		System.out.println("Output: "+output);
		return output;
	}
	
	private static HashMap<String, Integer> calculateConstraintSupport(boolean single){
		HashMap<String, Integer> constraintTypeSupport = new HashMap<String, Integer>();
		HashMap<String, Integer> constraintSupport = new HashMap<String, Integer>();
		
		ConstraintConversions cc = new ConstraintConversions();
		
		if(single)
			for(String s: cc.getSingles())
				constraintSupport.put(s, 0);
		else
			for(String s: cc.getTypes())
				constraintSupport.put(s, 0);
				
		for (ConstraintFeature cf : features) {
			String constraint = cf.getConstraintName();
			if(single)
				constraintSupport.put(constraint, constraintSupport.get(constraint)+1);
			else
				constraintSupport.put(cc.singleToType(constraint), 
					constraintSupport.get(cc.singleToType(constraint)) + 1);
		}
		return constraintSupport;
	}

	private static void reduceFeatureSpace() {
		System.out.println("Begin: " + features.size());
		Collection<ConstraintFeature> toRemove = new HashSet<ConstraintFeature>();
		Collection<ConstraintFeature> toAdd = new HashSet<ConstraintFeature>();
		
		for(ConstraintFeature f: features){
		String print = f.toString().replace(Character.toString(f.getAntecedent()),
				reverseMapping.get(f.getAntecedent()));
		if (f.getConsequent() != ' ')
			print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
			//System.out.println(print);
		}
		
		Collection<ConstraintFeature> lookAt = new HashSet<ConstraintFeature>();
		Collection<ConstraintFeature> lookOut = new HashSet<ConstraintFeature>();
		for(ConstraintFeature cf: features){
			if(cf.getConstraintName().contains("Succession"))
				lookAt.add(cf);
			if(cf.getConstraintName().contains("Response")||cf.getConstraintName().contains("Precedence") ||
					cf.getConstraintName().contains("Succession") || cf.getConstraintName().contains("CoExistence"))
				lookOut.add(cf);
		}
		
		for (ConstraintFeature cf : lookAt) {
			for (ConstraintFeature i : lookOut) {
				if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
					if (cf.getConstraintName().equals("Succession") 
							&& (i.getConstraintName().equals("Response") || i.getConstraintName().equals("Precedence")
									/*|| i.getConstraintName().equals("CoExistence")*/)) {				
						toRemove.add(i);
					}
					if (cf.getConstraintName().equals("AlternateSuccession") && !cf.getConstraintName().contains("Chain") &&
							(i.getConstraintName().contains("Response") || i.getConstraintName().contains("Precedence") ||
									i.getConstraintName().equals("Succession"))) {				
						toRemove.add(i);
					}
					if (cf.getConstraintName().equals("ChainSuccession") 
							&& (i.getConstraintName().contains("Response") || i.getConstraintName().contains("Precedence") ||
									i.getConstraintName().equals("Succession") || i.getConstraintName().equals("AlternateSuccession"))) {				
						toRemove.add(i);
					}
				}
				if (cf.getWindow() == i.getWindow() && (cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent() ||
						cf.getConsequent() == i.getAntecedent() && cf.getAntecedent() == i.getConsequent())) {
					if (cf.getConstraintName().equals("Succession") && i.getConstraintName().equals("CoExistence")) {
						toRemove.add(i);
					}
				}
			}
		}
		features.removeAll(toRemove);
		features.addAll(toAdd);
		toRemove.clear();
		toAdd.clear();
		
		lookAt = new HashSet<ConstraintFeature>();
		for(ConstraintFeature cf: features){
			if(cf.getConstraintName().contains("Response") || cf.getConstraintName().contains("Precedence"))
				lookAt.add(cf);
		}		
		for (ConstraintFeature cf : lookAt) {
			if (cf.getConstraintName().equals("ChainResponse")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Reponse")
								|| i.getConstraintName().equals("AlternateResponse"))
							toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("ChainPrecedence")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Precedence")
								|| i.getConstraintName().equals("AlternatePrecedence"))
							toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("AlternateResponse")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Reponse"))
							toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("AlternatePrecedence")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Precedence"))
							toRemove.add(i);
					}
				}
			}
		}
		lookAt = new HashSet<ConstraintFeature>();
		for(ConstraintFeature cf: features){
			if(cf.getConstraintName().contains("Exactly") || cf.getConstraintName().contains("Existence"))
				lookAt.add(cf);
		}	
		for (ConstraintFeature cf : lookAt) {
			if (cf.getConstraintName().equals("Existence3")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() 
							&& (i.getConstraintName().equals("Existence2") || i.getConstraintName().equals("Existence"))) {
						toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("Existence2")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() 
							&& (i.getConstraintName().equals("Existence"))) {
						toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("Exactly2")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() 
							&& (i.getConstraintName().equals("Exactly")
									|| i.getConstraintName().equals("Existence") || i.getConstraintName().equals("Existence2"))) {
						toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("Existence") || cf.getConstraintName().equals("Existence2")
					||cf.getConstraintName().equals("Existence3") || cf.getConstraintName().equals("Exactly")  
					|| cf.getConstraintName().equals("Exactly2")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getWindow() == i.getWindow() && cf.getAntecedent() == i.getAntecedent() 
							&& (i.getConstraintName().equals("Exactly") || i.getConstraintName().equals("Exactly2")
									|| i.getConstraintName().equals("Existence") || i.getConstraintName().equals("Existence2")
									|| i.getConstraintName().equals("Existence3"))) {
						for(ConstraintFeature cf2: features){
							if(cf2.getConstraintName().equals("CoExistence") && cf.getWindow()==cf2.getWindow()  && (
									(cf.getAntecedent()==cf2.getAntecedent() && cf.getConsequent()== cf2.getConsequent()) || 
									(cf.getAntecedent()==cf2.getConsequent() && cf.getConsequent()== cf2.getAntecedent())))
								toRemove.add(cf2);
						}				
					}
				}
			}
		}
		features.removeAll(toRemove);
		features.addAll(toAdd);
		System.out.println("End: " + features.size());
		
		if(VERBOSE){
			for(ConstraintFeature f: features){
			String print = f.toString().replace(Character.toString(f.getAntecedent()),
					reverseMapping.get(f.getAntecedent()));
			if (f.getConsequent() != ' ')
				print = print.replace(Character.toString(f.getConsequent()), reverseMapping.get(f.getConsequent()));
				//System.out.println(print);
			}
		}
	}
	
	private static void parseOptions(String[] args) throws ParseException {
		CommandLineParser parser = new BasicParser();

		Options options = new Options();

		Option help = new Option("h", "help", false, "help");
		Option datasetsOption = new Option("d", "datasets", true, "datasets processed");
		Option supportOption = new Option("s", "support", true, "support values to be tested");
		Option compressionOption = new Option("c", "compression", true, "compression rate for datasets");
		Option oversamplingOption = new Option("o", "oversamplingRate", true, "oversampling rate for datasets");

		options.addOption(help);
		options.addOption(datasetsOption);
		options.addOption(supportOption);
		options.addOption(compressionOption);
		options.addOption(oversamplingOption);

		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("h")) {
				HelpFormatter formater = new HelpFormatter();
				formater.printHelp("Sequence Miner", options);
			}
			if (line.hasOption("d"))
				datasets = line.getOptionValue("d").split(",");
			if (line.hasOption("s")){
				supports = new ArrayList<Double>();
				for(String sup: line.getOptionValue("s").split(","))
					supports.add(Double.parseDouble(sup));			
			}if (line.hasOption("c"))
				compression = Integer.parseInt(line.getOptionValue("c"));
			if (line.hasOption("o"))
				oversamplingRate = Integer.parseInt(line.getOptionValue("o"));
		} catch (ParseException parseException) {
			System.err.println("Exception while parsing:\n" + parseException.getMessage());
		}
	}

}
