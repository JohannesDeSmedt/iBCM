package be.kuleuven.liris.sequencemining.chunks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import be.kuleuven.liris.sequencemining.concept.Trace;

public class TraceChunk<T> extends Chunk{

	private Map<Integer, HashSet<Trace<T>>> traces = new HashMap<Integer, HashSet<Trace<T>>>();
	
	public void addTrace(Trace<T> t){
		if(traces.containsKey(t.getLabel()))
			traces.get(t.getLabel()).add(t);
		else{
			HashSet<Trace<T>> tracesCollection = new HashSet<Trace<T>>();
			tracesCollection.add(t);
			traces.put(t.getLabel(), tracesCollection);
		}
	}
	
	public Collection<Trace<T>> getAllTraces(){
		Collection<Trace<T>> allTraces = new HashSet<Trace<T>>();
		for(Integer i: traces.keySet())
			allTraces.addAll(traces.get(i));		
		return allTraces;
	}
	
	public Collection<Trace<T>> getTracesWithLabel(int i){
		return traces.get(i);
	}	
	
	public Collection<T> getConstraintsWithLabel(int i){
		Collection<T> constraints = new HashSet<T>();
		for(Trace<T> t: getTracesWithLabel(i)){
			constraints.addAll(t.getConstraints());
		}
		return constraints;
	}
	
}
