package be.kuleuven.liris.sequencemining.concept;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class ConstraintConversions {
	
	private HashMap<String,String> conType = new HashMap<String,String>();

	public ConstraintConversions(){
		conType = new HashMap<String,String>();
		
		conType.put("Exactly", "unary");
		conType.put("Exactly2", "unary");
		conType.put("Existence", "unary");
		conType.put("Existence2", "unary");
		conType.put("Existence3", "unary");
		conType.put("Absence", "absence");
		conType.put("Last", "unary");
		conType.put("Init", "unary");
		conType.put("CoExistence", "exist");
		conType.put("RespondedExistence", "exist");
		conType.put("Succession", "simple");
		conType.put("Response", "simple");
		conType.put("Precedence", "simple");
		conType.put("AlternateSuccession", "alt");
		conType.put("AlternateResponse", "alt");
		conType.put("AlternatePrecedence", "alt");
		conType.put("ChainSuccession", "chain");
		conType.put("ChainResponse", "chain");
		conType.put("ChainPrecedence", "chain");
		conType.put("NotSuccession", "neg");
		conType.put("NotChainSuccession", "neg");
		conType.put("ExclusiveChoice", "neg");
	}
	
	public Collection<String> getTypes(){
		return conType.values();
	}
	
	public Set<String> getSingles(){
		return conType.keySet();
	}
	
	public String singleToType(String con){
		return conType.get(con);
	}
	
}	
