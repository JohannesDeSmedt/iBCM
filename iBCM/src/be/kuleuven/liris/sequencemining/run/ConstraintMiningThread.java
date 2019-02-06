package be.kuleuven.liris.sequencemining.run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import be.kuleuven.liris.sequencemining.concept.ConstraintFeature;
import be.kuleuven.liris.sequencemining.concept.Trace;

public class ConstraintMiningThread implements Callable<Trace<ConstraintFeature>> {

	private int i;
	private List<Character> activities = new ArrayList<Character>();
	private String s;
	private Set<Character> alphabet = new HashSet<Character>();
	private HashSet<ConstraintFeature> localConstraints = new HashSet<ConstraintFeature>();
	private boolean ab = true;
	private HashMap<Character,ArrayList<Integer>> actPositions = new HashMap<Character,ArrayList<Integer>>();
	
	public ConstraintMiningThread(String s, Integer i, List<Character> activities, 
			 boolean ab){
		this.s = s;
		this.i = i;
		this.activities = activities;
		this.ab = ab;
	}

	@Override
	public Trace<ConstraintFeature> call() throws Exception {	
		for(Character c: activities)
			actPositions.put(c, new ArrayList<Integer>());
			
		//String trace = "";
		for(int cha=0;cha<s.length();cha++){
			char c = s.charAt(cha);
			alphabet.add(c);
			if(actPositions.containsKey(c))
				actPositions.get(c).add(cha);
			//trace+=reverseMapping.get(c)+" - ";
		}
		//System.out.println("Trace: "+trace);
		for (int i = 0; i < activities.size(); i++) {
			char a = activities.get(i);
			//System.out.println("A: "+reverseMapping.get(a));
			ArrayList<Integer> aList = actPositions.get(a);
			
			if(aList.size()==0 && ab){
				localConstraints.add(new ConstraintFeature("Absence", a, ' '));
			}else if(aList.size()==1){
				localConstraints.add(new ConstraintFeature("Exactly", a, ' '));
				localConstraints.add(new ConstraintFeature("Existence", a, ' '));
			}else if(aList.size()==2){
				localConstraints.add(new ConstraintFeature("Exactly2", a, ' '));
				localConstraints.add(new ConstraintFeature("Existence", a, ' '));
				localConstraints.add(new ConstraintFeature("Existence2", a, ' '));
			}else{
				localConstraints.add(new ConstraintFeature("Existence", a, ' '));
				localConstraints.add(new ConstraintFeature("Existence2", a, ' '));
				localConstraints.add(new ConstraintFeature("Existence3", a, ' '));
			}
			if(aList.size() > 0) {
				if (aList.get(0) == 0)
					localConstraints.add(new ConstraintFeature("Init", a, ' '));
				if (aList.get(aList.size()-1)==s.length()-1)
					localConstraints.add(new ConstraintFeature("Last", a, ' '));
				for (int j = i + 1; j < activities.size(); j++){
					//System.out.println("B: "+reverseMapping.get(activities.get(j)));
					char b = activities.get(j);
					if(actPositions.get(b).size()>0){
						mineBinaries(a, b, actPositions.get(a), actPositions.get(b), true);
						mineBinaries(b, a, actPositions.get(b), actPositions.get(a), false);
					}else{
						//localConstraints.add(new ConstraintFeature("ExclusiveChoice",a, b));
					}
				}
			}else{
				for (int j = i + 1; j < activities.size(); j++){
					char b = activities.get(j);
					if(actPositions.get(b).size()>0){
						//localConstraints.add(new ConstraintFeature("ExclusiveChoice",a, b));
					}
				}
			}
		}		
		return new Trace<ConstraintFeature>(1, i, localConstraints, s);
	}
	
	private void mineBinaries(Character a, Character b, ArrayList<Integer> aList, 
			ArrayList<Integer> bList, boolean direction){		
		
		boolean p=false,ap=false,r=false,ar=false,cp=false,cr=false;
		
		//if(bList.size()>0){
			if(direction)
				localConstraints.add(new ConstraintFeature("CoExistence",a,b));
			if(bList.get(0)>aList.get(0)){
				if(aList.get(aList.size()-1)<bList.get(0))
					localConstraints.add(new ConstraintFeature("NotSuccession",b,a));
				
				localConstraints.add(new ConstraintFeature("Precedence",a, b));
				p=true;
				
				Iterator<Integer> iB = bList.iterator();
				int previous = iB.next();
				int next = previous;
				boolean goOn = true;
				boolean chain = (aList.contains(bList.get(0)-1));
				while(iB.hasNext() && goOn){
					next = iB.next();
					if(next-previous>1){
						for(int i=previous+1;i<next;i++){
							if(aList.contains(i)){
								goOn=true;
								if(!aList.contains(next-1))
									chain=false;
								break;
							}
							goOn=false;
						}
						previous=next;
					}else{
						goOn=false;
					}
				}
				if(next==bList.get(bList.size()-1) && goOn){
					if(bList.size()>1){
						localConstraints.add(new ConstraintFeature("AlternatePrecedence",a, b));
						ap=true;
					}
					if(chain){
						localConstraints.add(new ConstraintFeature("ChainPrecedence",a, b));
						cp=true;
					}
				}
			}
			if(bList.get(bList.size()-1)>aList.get(aList.size()-1)){
				localConstraints.add(new ConstraintFeature("Response",a, b));
				r=true;
				
				Iterator<Integer> iA = aList.iterator();
				int previous = iA.next();
				int next = previous;
				boolean goOn = true;
				boolean chain = (bList.contains(aList.get(aList.size()-1)+1));
				while(iA.hasNext() && goOn){
					next = iA.next();
					if(next-previous>1){
						for(int i=previous+1;i<next;i++){
							if(bList.contains(i)){
								goOn=true;
								if(!bList.contains(next+1))
									chain=false;
								break;
							}
							goOn=false;
						}
						previous=next;
					}else{
						goOn=false;
					}
				}
				if(next==aList.get(aList.size()-1) && goOn){
					if(aList.size()>1){
						localConstraints.add(new ConstraintFeature("AlternateResponse",a, b));
						ar=true;
					}
					if(chain){
						localConstraints.add(new ConstraintFeature("ChainResponse",a, b));
						cr=true;
					}
				}
			}			
		//}
		if(p&&r)
			localConstraints.add(new ConstraintFeature("Succession",a, b));
		if(ap&&ar)
			localConstraints.add(new ConstraintFeature("AlternateSuccession",a, b));
		if(cp&&cr)
			localConstraints.add(new ConstraintFeature("ChainSuccession",a, b));
	}	
}