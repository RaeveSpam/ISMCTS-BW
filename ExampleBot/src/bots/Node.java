package bots;

import java.util.ArrayList;

public class Node {
	
	public int value;
	
	public ArrayList<Edge> children;
	
	public Node(){
		children = new ArrayList<Edge>();
	}
}
