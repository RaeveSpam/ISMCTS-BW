package bots;

import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import ISMCTS.Edge;
import ISMCTS.Entity;
import ISMCTS.ISMCTS;
import ISMCTS.Node;
import ISMCTS.Persistence;
import actions.Action;
import actions.BuildAction;
import actions.BuildBuilding;
import actions.BuildUnit;
import actions.Withdraw;
import bwapi.UnitType;
import stateInformation.EnemyBuilding;

import java.beans.XMLDecoder;


public class XMLTest {

	public static void main(String[] args) {
		

		
		// TODO Auto-generated method stub
		//UnitType bla = UnitType.Protoss_Arbiter;
		//System.out.println(bla);
		Node n0 = new Node();
		n0.suckIt = "node0";
		Action a0 = new Withdraw();
		Edge e0 = new Edge(a0);
		e0.fuck = "edge0";
		Node n1 = new Node();
		n1.suckIt = "node1";
		e0.children.add(n1);
		n0.children.add(e0);
		System.out.println(n0.children.get(0).children.size());
		
		//BuildAction<Entity> test = new BuildBuilding();
		
		//test.type = Entity.Arbiter;
		//test.suckIt = "suck my ****";
		
		//System.out.println(Persistence.getHostName());
		//Persistence.saveTree();
		// Save Object
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("XMLTEST")));
			encoder.writeObject(n0);
			encoder.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		Node test = new Node();
		// Load Object
		try {
			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream("XMLTEST")));
			test = (Node)decoder.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		System.out.println(test.children.get(0).children.size());
		//System.out.println(test.type);
	}

	public static boolean saveBuilding(EnemyBuilding building){

		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("TEST")));
			encoder.writeObject(building);
			encoder.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static EnemyBuilding loadTree(){
		EnemyBuilding result = null;

		try {
			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream("TEST")));
			result = (EnemyBuilding)decoder.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return result;
	}
	
	public static int randomTest(int range){
		Random random = new Random();
		return random.nextInt(range);
	}
}
