package be.kuleuven.liris.sequencemining.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import com.google.common.collect.Sets;

import be.kuleuven.liris.sequencemining.chunks.TraceChunk;
import be.kuleuven.liris.sequencemining.concept.Trace;

public class CrossValidationRun<T> extends ExperimentRun<T>{
	
	public CrossValidationRun(ArrayList<Trace<T>> traces, Collection<T> features, int nFold, int noClasses, int noRuns) {
		super(traces, features, nFold, noClasses, noRuns, "");
	}
		
	public Double call() throws Exception{
		int chunkSize = traces.size() / nFold;
		ArrayList<TraceChunk<T>> chunks = new ArrayList<TraceChunk<T>>();
		for (int n = 0; n < nFold; n++) {
			TraceChunk<T> chunk = new TraceChunk<T>();
			for (int c = 0; c < chunkSize; c++)
				chunk.addTrace(traces.get(n * chunkSize + c));
			chunks.add(chunk);
			//System.out.println("Chunk size: "+chunk.getAllTraces().size());
//			for(int cla=1;cla<=noClasses;cla++){
//				System.out.println("Chunk "+n+" -cla: "+cla+" "+chunk.getTracesWithLabel(cla).size());
//				System.out.println("Chunk "+n+" -cla: "+cla+" "+chunk.getConstraintsWithLabel(cla).size());
//			}
		}								

		double totalAcc = 0.0;
		for (int n = 0; n < nFold; n++) {
			HashMap<Integer, HashSet<Pair<Integer, String>>> trainSet2 = new HashMap<Integer, HashSet<Pair<Integer, String>>>();

			Set<Pair<Integer, String>> commonConstraints = new HashSet<Pair<Integer, String>>();

			for (int cla = 1; cla <= noClasses; cla++) {
				HashMap<String, Integer> constraintTally = new HashMap<String, Integer>();
				trainSet2.put(cla,
						new HashSet<Pair<Integer, String>>());
				for (int ch = 0; ch < nFold; ch++) {
					if (ch != n) {
						for (T constraintS : chunks
								.get(ch).getConstraintsWithLabel(cla)) {
							if (constraintTally.containsKey(constraintS.toString()))
								constraintTally
										.put(constraintS.toString(),
												constraintTally
														.get(constraintS.toString()) + 1);
							else
								constraintTally.put(
										constraintS.toString(), 1);
						}
					}
				}				
				for (String s : constraintTally.keySet()) {
					if (constraintTally.get(s) > 0){
						//System.out.println("ConstraintTally: "+s+" - "+constraintTally.get(s));
						trainSet2.get(cla).add(
								new Pair<Integer, String>(constraintTally.get(s), s));
					}
				}
				//System.out.println("#Constraints for class "+ cla + " is "+ trainSet2.get(cla).size());
			}

			for (int cla = 1; cla <= noClasses - 1; cla++) {
				if (cla == 1)
					commonConstraints.addAll(Sets.intersection(
							trainSet2.get(cla),
							trainSet2.get(cla + 1)));
				else
					commonConstraints.retainAll(Sets.intersection(commonConstraints, trainSet2.get(cla + 1)));
			}

			System.out.println("Common constraints: "
					+ commonConstraints.size());
			for (int cla = 1; cla <= noClasses; cla++)
				trainSet2.get(cla).removeAll(commonConstraints);

			TraceChunk<T> testSet = chunks.get(n);
			double acc = 0.0;
			int correct = 0;
			for (Trace<T> t : testSet.getAllTraces()) {
				int predLabel = classifyTrace(t, trainSet2);
				if (t.getLabel() == predLabel)
					correct++;
			}
			acc = (double) correct / (double) chunkSize;
			System.out.println("Accuracy: " + acc);
			totalAcc += acc;
		}
		return (totalAcc / (double) nFold);
	}
	
	private int classifyTrace(Trace<T> t,
			HashMap<Integer, HashSet<Pair<Integer, String>>> constraintsClass)
			throws Exception {
		int classNumber = 0;
		int maxSize = 0;

		for (Integer i : constraintsClass.keySet()) {
			// Set<String> matches = Sets.intersection(t.getConstraints(),
			// constraintsClass.get(i));
			// int setSize = Sets.intersection(t.getConstraints(),
			// constraintsClass.get(i)).size();
			int setSize = 0;
			Set<String> constraintsOfT = t.constraintsAsStrings();
			for (Pair<Integer, String> pa : constraintsClass.get(i)) {
				if (constraintsOfT.contains(pa.getSecond().toString())) {
					setSize +=pa.getFirst();
				}
			}
			if (setSize > maxSize) {
				classNumber = i;
				maxSize = setSize;
			}
		}
		return classNumber;
	}

	
	
}
