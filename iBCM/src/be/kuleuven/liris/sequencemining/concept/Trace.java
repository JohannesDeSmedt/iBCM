package be.kuleuven.liris.sequencemining.concept;

import java.util.HashSet;
import java.util.Set;


public class Trace<T>{

	private Set<T> constraints = new HashSet<T>();
	private Integer id;
	private Integer label;
	private String traceString;
	
	public Trace(Integer id, Integer label, Set<T> constraints, String traceString){
		this.constraints = constraints;
		this.id=id;
		this.label=label;
		this.traceString = traceString;
	}
		
	public String getTraceAsString(){
		return traceString;
	}
	
//	public boolean hasConstraint(Constraint s){
//		return constraints.contains(s);
//	}
	
	public Set<String> constraintsAsStrings(){
		Set<String> constraintsAsStrings = new HashSet<String>();
		for(T c: constraints)
			constraintsAsStrings.add(c.toString());
		return constraintsAsStrings;		
	}
	
	public Set<T> getConstraints(){
		return constraints;
	}
		
	public void removeConstraints(Set<T> toRemove){
		constraints.removeAll(toRemove);
	}
	
	public Set<String> getConstraintsAsStrings(){
		Set<String> strings = new HashSet<String>();
		for(T c: constraints)
			strings.add(c.toString());
		return strings;
	}
	
	public void setConstraints(Set<T> newConstraints){
		constraints = newConstraints;
	}
	
	public int getNumberOfConstraints(){
		return constraints.size();
	}
	
	public int getLabel(){
		return label;
	}
	
	public Integer getId(){
		return id;
	}
	
	public String toString(){
		return constraints.toString();
	}
	
}
