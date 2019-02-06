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

public class ConstraintMiningThreadWindowed implements Callable<Trace<ConstraintFeature>> {

	private int i;
	private List<Character> activities = new ArrayList<Character>();
	private String s;
	
	private Set<Character> alphabet = new HashSet<Character>();
	private HashSet<ConstraintFeature> localConstraints = new HashSet<ConstraintFeature>();
	
	private boolean ab = true;
	//private int w;
	private int winSize;
	private ArrayList<Integer> windowPos;
	
	private HashMap<Character,ArrayList<Integer>> actPositions = new HashMap<Character,ArrayList<Integer>>();
	
	public ConstraintMiningThreadWindowed(String s, Integer i, List<Character> activities, 
			boolean ab, int w){
		this.s = s;
		this.i = i;
		this.activities = activities;
		this.ab = ab;
		
		windowPos = new ArrayList<Integer>();
		winSize = s.length()/w;
		
		//System.out.println("String: "+s);
		//if(winSize>0 && (winSize>w))
		if(winSize>0)
			for(int win=1;win<w;win++)
				windowPos.add(winSize*win);
		windowPos.add(s.length());
	}

	@Override
	public Trace<ConstraintFeature> call() throws Exception {	
		//System.out.println("Window pos: "+windowPos+" trace "+s.length()+" "+i+" ws "+winSize);
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

			for(int win=0; win<windowPos.size(); win++){
			//System.out.println("A: "+reverseMapping.get(a)+" "+actPositions.get(a));
				int lB = win*winSize;
				int uB = windowPos.get(win);
				//System.out.println("Boundaries: ["+lB+","+uB+"]");
				ArrayList<Integer> aList = getWindow(actPositions.get(a), lB , uB);
						//= actPositions.get(a);

				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				// 13/11 changed consequent of unary constraints to a instead of ' '
				
				if(aList.size()==0 && ab){
					localConstraints.add(new ConstraintFeature("Absence", a, a, win));
				}else if(aList.size()==1){
					localConstraints.add(new ConstraintFeature("Exactly", a, a, win));
					//localConstraints.add(new ConstraintFeature("Existence", a, ' ', win));
				}else if(aList.size()==2){
					localConstraints.add(new ConstraintFeature("Exactly2", a, a, win));
					//localConstraints.add(new ConstraintFeature("Existence", a, ' ', win));
					//localConstraints.add(new ConstraintFeature("Existence2", a, ' ', win));
				}else{
					//localConstraints.add(new ConstraintFeature("Existence", a, ' ', win));
					//localConstraints.add(new ConstraintFeature("Existence2", a, ' ', win));
					localConstraints.add(new ConstraintFeature("Existence3", a, a, win));
				}
				if(aList.size() > 0) {
					if (aList.get(0) == lB)
						localConstraints.add(new ConstraintFeature("Init", a, a, win));
					if (aList.get(aList.size()-1)==uB)
						localConstraints.add(new ConstraintFeature("Last", a, a, win));
					for (int j = i + 1; j < activities.size(); j++){
						//System.out.println("B: "+reverseMapping.get(activities.get(j)));
						char b = activities.get(j);
						ArrayList<Integer> bList = getWindow(actPositions.get(b), lB, uB);
						if(bList.size()>0){
							localConstraints.add(new ConstraintFeature("CoExistence",a,b, win));
							mineBinaries(a, b, aList, bList, win);
							mineBinaries(b, a, bList, aList, win);
						}else{
							//localConstraints.add(new ConstraintFeature("ExclusiveChoice",a, b, win));
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
		}		
		//System.out.println(localConstraints.toString());
		return new Trace<ConstraintFeature>(1, i, localConstraints, s);
	}
	
	private void mineBinaries(Character a, Character b, ArrayList<Integer> aList, 
			ArrayList<Integer> bList, int win){		
		//System.out.println("A: "+aList.toString());
		//System.out.println("B: "+bList.toString());
		
		boolean p=false,ap=false,r=false,ar=false,cp=false,cr=false;
		
		if (bList.get(0) > aList.get(0)) {
			if (aList.get(aList.size() - 1) < bList.get(0))
				localConstraints.add(new ConstraintFeature("NotSuccession", b, a, win));

			localConstraints.add(new ConstraintFeature("Precedence", a, b, win));
			p = true;

			Iterator<Integer> iB = bList.iterator();
			int previous = iB.next();
			int next = previous;
			boolean goOn = true;
			boolean chain = (aList.contains(bList.get(0) - 1));
			while (iB.hasNext() && goOn) {
				next = iB.next();
				if (next - previous > 1) {
					for (int i = previous + 1; i < next; i++) {
						if (aList.contains(i)) {
							goOn = true;
							if (!aList.contains(next - 1))
								chain = false;
							break;
						}
						goOn = false;
					}
					previous = next;
				} else {
					goOn = false;
				}
			}
//			if (next == bList.get(bList.size() - 1) && goOn) {
//				localConstraints.add(new ConstraintFeature("AlternatePrecedence", a, b, win));
//				ap = true;
//				if (chain) {
//					localConstraints.add(new ConstraintFeature("ChainPrecedence", a, b, win));
//					cp = true;
//				}
//			}
			if (next == bList.get(bList.size() - 1) && goOn) {
				if (bList.size() > 1) {
					localConstraints.add(new ConstraintFeature("AlternatePrecedence", a, b, win));
					ap = true;
				}
				if (chain) {
					localConstraints.add(new ConstraintFeature("ChainPrecedence", a, b, win));
					cp = true;
				}
			}
		}
		if (bList.get(bList.size() - 1) > aList.get(aList.size() - 1)) {
			localConstraints.add(new ConstraintFeature("Response", a, b, win));
			r = true;

			Iterator<Integer> iA = aList.iterator();
			int previous = iA.next();
			int next = previous;
			boolean goOn = true;
			boolean chain = (bList.contains(aList.get(aList.size() - 1) + 1));
			while (iA.hasNext() && goOn) {
				next = iA.next();
				if (next - previous > 1) {
					for (int i = previous + 1; i < next; i++) {
						if (bList.contains(i)) {
							goOn = true;
							if (!bList.contains(previous + 1))
								chain = false;
							break;
						}
						goOn = false;
					}
					previous = next;
				} else {
					goOn = false;
				}
			}
			if(next==aList.get(aList.size()-1) && goOn){
				if(aList.size()>1){
					localConstraints.add(new ConstraintFeature("AlternateResponse",a, b, win));
					ar=true;
				}
				if(chain){
					localConstraints.add(new ConstraintFeature("ChainResponse",a, b, win));
					cr=true;
				}
			}
//			if (next == aList.get(aList.size() - 1) && goOn) {
//				localConstraints.add(new ConstraintFeature("AlternateResponse", a, b, win));
//				ar = true;
//				if (chain) {
//					localConstraints.add(new ConstraintFeature("ChainResponse", a, b, win));
//					cr = true;
//				}
//			}
		}		
		
		if(p&&r)
			localConstraints.add(new ConstraintFeature("Succession",a, b, win));
		if(ap&&ar)
			localConstraints.add(new ConstraintFeature("AlternateSuccession",a, b, win));
		if(cp&&cr)
			localConstraints.add(new ConstraintFeature("ChainSuccession",a, b, win));
	}	

	private ArrayList<Integer> getWindow(ArrayList<Integer> aL, int winL, int winR){
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for(int i=winL;i<winR;i++)
			if(aL.contains(i))
				newList.add(i);		
		return newList;
	}
	
}