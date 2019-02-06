package be.kuleuven.liris.sequencemining.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import be.kuleuven.liris.sequencemining.concept.Trace;

public abstract class ExperimentRun<T> implements Callable<Map<String,Double>>{

	protected ArrayList<Trace<T>> traces;
	protected Collection<T> features;
	protected int nFold;
	protected int noClasses;
	protected int noRuns = 0;
	protected String name;
	
	public ExperimentRun(ArrayList<Trace<T>> traces, Collection<T> features, int nFold , int noClasses, int noRuns, String name){
		this.traces = traces;
		this.features = features;
		this.nFold = nFold;
		this.noClasses = noClasses;
		this.noRuns = noRuns;
		this.name = name;
	}
		
	public Map<String,Double> runTests() throws Exception{		
		Map<String,Double> result = new HashMap<String,Double>();
		
		for(int i=0;i<noRuns;i++){
			Collections.shuffle(traces);
			Map<String,Double> outcome = call();
			for(String s: outcome.keySet()){
				if(!result.containsKey(s))
					result.put(s, 0.0);
				result.put(s, result.get(s)+outcome.get(s));
			}
			//runAcc += accuracy;
		}
		
		//double accuracy = runAcc
		//		/ ((double) noRuns);
		for(String s: result.keySet())
			result.put(s, result.get(s)/(double) noRuns);
		
		return result;
	}
	
	
}
