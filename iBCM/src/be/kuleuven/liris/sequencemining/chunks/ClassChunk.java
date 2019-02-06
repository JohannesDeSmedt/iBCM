package be.kuleuven.liris.sequencemining.chunks;

import java.util.ArrayList;

import be.kuleuven.liris.sequencemining.concept.Trace;

public class ClassChunk<T> extends Chunk{

	private int label;
	private ArrayList<Trace<T>> traces = new ArrayList<Trace<T>>();
	
	public void setLabel(int i){
		this.label= i;
	}
	
	public int getLabel(){
		return label;
	}
	
	public void addTraces(ArrayList<Trace<T>> traces){
		this.traces = traces;
	}
	
	public void addTrace(Trace<T> trace){
		traces.add(trace);
	}
	
	public ArrayList<Trace<T>> getTraces(){
		return traces;
	}
	
	public ArrayList<String> getTracesAsStrings() {
		ArrayList<String> tracesStrings = new ArrayList<String>();
		for(Trace<T> t: traces)
			tracesStrings.add(t.toString());
		return tracesStrings;
	}
		
}
