package ISMCTS;

import java.util.ArrayList;

public class Node {
	
	public boolean isRoot;
	
	public int visits;
	
	public int wins;
	
	public InformationSet informationSet;
	
	public ArrayList<Edge> children;
	
	public String suckIt;
	
	public Node(InformationSet informationSet, boolean isRoot){
		this.isRoot = isRoot;
		children = new ArrayList<Edge>();
		this.informationSet = informationSet;
		visits = 0;
		wins = 0;
	}	
	
	public Node(InformationSet informationSet){
		isRoot = false;
		children = new ArrayList<Edge>();
		this.informationSet = informationSet;
		visits = 0;
		wins = 0;
	}

	
	/**
	 * DO NOT USE. Only used for serialization.
	 */
	public Node(){
		children = new ArrayList<Edge>();
	}
	
	public InformationSet getInformationSet(){
	//	System.out.println(informationSet);
		return informationSet;
	}
	
	public void addChild(Edge child){
		children.add(child);
	}
	
	public double getScore(){
		// Algo
		return 0.0;
	}
	
	public String getName(){
		return "ISMCTS-" + visits;
	}
	
	public boolean isFullyExpanded(){
		if(visits == 0){
			//System.out.println("fully expanded false");
			return false;
		}
		for(Edge e : children){
			if(!e.visited()){
				return false;
			}
		}
		return true;
	}
	
	public int getVisits(){
		return visits;
	}
	
	public void backPropogate(boolean win){
		if(win){
			wins++;
		}
		
		visits++;
		
	}
}
