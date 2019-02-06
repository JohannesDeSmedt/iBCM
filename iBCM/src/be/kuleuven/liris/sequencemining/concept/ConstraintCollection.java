package be.kuleuven.liris.sequencemining.concept;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConstraintCollection extends HashMap<ConstraintFeature,Integer>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3822376066899389433L;
	private int label;
	private List<Character> classAlphabet;
	
	public int getLabel(){
		return label;
	}
	
	public List<Character> getAlphabet(){
		return classAlphabet;
	}
	
	public ConstraintCollection(int l, List<Character> al){
		super();
		this.label = l;
		this.classAlphabet = al;
	}
		
	public void addConstraints(Collection<ConstraintFeature> fs){
		for(ConstraintFeature cf: fs)
			addConstraint(cf);
	}
	
	public void addConstraint(ConstraintFeature f){
		if(this.containsKey(f)) {
			this.put(f, this.get(f) + 1);
		} else {
			this.put(f, 1);
		}
	}
	
	public int getSupportConstraint(ConstraintFeature f){
		if(this.containsKey(f))
			return this.get(f);
		else
			return 0;
	}
	
	public void removeConstraintsBelowSupport(double minSup){
		Collection<ConstraintFeature> cf = new HashSet<ConstraintFeature>();
		for(ConstraintFeature c: this.keySet())
			if(this.get(c)<minSup)
				cf.add(c);
		this.keySet().removeAll(cf);
	}
	
	public Set<ConstraintFeature> getFeatures(){
		return this.keySet();
	}
	
	public void reduceFeatureSpace(boolean VERBOSE) {
		Collection<ConstraintFeature> features = this.keySet();
		if(VERBOSE)
			System.out.println("Begin: " + features.size());
		Collection<ConstraintFeature> toRemove = new HashSet<ConstraintFeature>();
		Collection<ConstraintFeature> toAdd = new HashSet<ConstraintFeature>();
		
		Collection<ConstraintFeature> lookAt = new HashSet<ConstraintFeature>();
		Collection<ConstraintFeature> lookOut = new HashSet<ConstraintFeature>();
		for(ConstraintFeature cf: features){
			if(cf.getConstraintName().contains("Succession"))
				lookAt.add(cf);
			if(cf.getConstraintName().contains("Response")||cf.getConstraintName().contains("Precedence") ||
					cf.getConstraintName().contains("Succession"))
				lookOut.add(cf);
		}
		
		for (ConstraintFeature cf : lookAt) {
			for (ConstraintFeature i : lookOut) {
				if (cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
					if (cf.getConstraintName().equals("Succession") 
							&& (i.getConstraintName().equals("Response") || i.getConstraintName().equals("Precedence"))) {				
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
					if (cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Reponse")
								|| i.getConstraintName().equals("AlternateResponse"))
							toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("ChainPrecedence")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Precedence")
								|| i.getConstraintName().equals("AlternatePrecedence"))
							toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("AlternateResponse")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Reponse"))
							toRemove.add(i);
					}
				}
			}
			if (cf.getConstraintName().equals("AlternatePrecedence")) {
				for (ConstraintFeature i : lookAt) {
					if (cf.getAntecedent() == i.getAntecedent() && cf.getConsequent() == i.getConsequent()) {
						if (i.getConstraintName().equals("Precedence"))
							toRemove.add(i);
					}
				}
			}
		}
		features.removeAll(toRemove);
		features.addAll(toAdd);
		if(VERBOSE)
			System.out.println("End: " + features.size());
	}
		
}
