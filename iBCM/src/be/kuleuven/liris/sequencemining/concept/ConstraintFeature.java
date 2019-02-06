package be.kuleuven.liris.sequencemining.concept;

import java.util.HashMap;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class ConstraintFeature extends Object{

	private char antecedent, consequent;
	private String constraintName;
	private int window = 0;
	
	public ConstraintFeature(String constraintName, char antecedent, char consequent){
		this.constraintName = constraintName;
		this.antecedent = antecedent;
		this.consequent = consequent;
	}
	
	public ConstraintFeature(String constraintName, char antecedent, char consequent, int window){
		this.constraintName = constraintName;
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.window = window;
	}
	
	@Override
	public int hashCode(){
		return new HashCodeBuilder().append(constraintName).append(antecedent).append(consequent).toHashCode();
	}
	
	public String getConstraintName(){
		return constraintName;
	}
	
	public char getAntecedent(){
		return antecedent;
	}
	
	public char getConsequent(){
		return consequent;
	}
	
	public int getWindow(){
		return window;
	}
	
	public String toString(){
		return constraintName+"("+antecedent+","+consequent+")_"+window;
	}
	
	public static String getConstraintName(int in) {
		HashMap<Integer,String> names = new HashMap<Integer,String>();
		names.put(0, "NoConnection");
		names.put(1, "NotSuccession");
		names.put(2, "Precedence");
		names.put(3, "AlternatePrecedence");
		names.put(4, "ChainPrecedence");
		names.put(5, "Response");
		names.put(6, "AlternateResponse");
		names.put(7, "ChainResponse");
		
		return names.get(in);
	}
	
	@Override
	public boolean equals(Object o){
		ConstraintFeature c2 = (ConstraintFeature) o;
		if(c2.getConstraintName() == constraintName &&
				c2.getAntecedent()==antecedent && c2.getConsequent()==consequent
				&& c2.getWindow()==window)
			return true;
		else
			return false;
	}
	
}
