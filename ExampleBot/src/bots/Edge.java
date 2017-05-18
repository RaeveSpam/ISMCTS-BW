package bots;

import java.util.ArrayList;

public class Edge {
	public int value;
	
	public ArrayList<Node> children;
	
	public Edge(){
		children = new ArrayList<Node>();
	}
}
