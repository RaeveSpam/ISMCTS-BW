package ISMCTS;

import java.util.ArrayList;

import actions.Action;

public class Edge {
	public int wins;
	
	public int visits;
	
	public String fuck;
	
	public Action action;
	
	public ArrayList<Node> children;
	
	public Edge(Action action){
		this.action = action;
		wins = 0;
		visits = 0;
		children = new ArrayList<Node>();
	}
	
	public Edge(){
		wins = 0;
		visits = 0;
		children = new ArrayList<Node>();
	}

	public void backPropogate(boolean win) {
		if(win){
			wins++;
		}
		visits++;
	}
	
	public boolean contains(InformationSet set){
		for(Node child : children){
			if(set.equals(child.getInformationSet())){
				return true;
			}
		}
		return false;
	}
	
	public Node getNodeFromSet(InformationSet set){
		for(Node child : children){
			if(set.equals(child.getInformationSet())){
				System.out.println("Existing node");
				return child;
			}
		}
		//System.out.println("Create new node");
		// new information set. Create a new node
		Node newNode = new Node(set);
		children.add(newNode);
		return newNode;
	}
	
	public Action getAction(){
		return action;
	}
	
	public boolean visited(){
		return (visits > 0);
	}
	
	public double getScore(){
		return 0.0;
	}
	
	public void addChild(Node node){
		children.add(node);
	}
	
	/*public void addChild(InformationSet informationSet){
		// Check if children contains node where node.infoSet = infoSet;
	}*/
	
	public double UCBScore(int parentVisits){
		if(visits == 0){
			return Double.POSITIVE_INFINITY;
		}
		
		// wins / visits + sqrt(2)/2 * sqrt(2 ln(parent.visits)/visits)
		return (double)wins/(double)visits + 0.71 * Math.sqrt(2.0 * Math.log((double)parentVisits/(double)visits));
	}
	
}
