package ISMCTS;

import java.util.ArrayList;

import actions.Action;
import bwapi.UnitType;

public class State {
	
	public ArrayList<UnitType> buildings;
	public ArrayList<Integer> bases;
	public ArrayList<UnitType> army;
	public int supply;
	
	public ArrayList<UnitType> enemyBuildings;
	public ArrayList<Integer> enemyBases;
	public ArrayList<UnitType> enemyArmy;
	public int enemySupply;
	
	public ArrayList<Action> getActions(){
		return null;
	}
	
	public ArrayList<Action> getEnemyActions(){
		return null;
	}
	
	
}
