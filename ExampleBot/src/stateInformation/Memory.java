package stateInformation;

import java.util.ArrayList;

import ISMCTS.Entity;
import ISMCTS.Tech;
import bwapi.UnitType;

public class Memory {
	public ArrayList<Entity> enemyBuildings;
	public ArrayList<Integer> enemyBases;
	public  ArrayList<Entity> enemyArmy;
	/**
	 * DO NOT USE
	 */
	public ArrayList<Tech> enemyUpgrades;
	
	public Memory(){
		enemyBuildings = new ArrayList<Entity>();
		enemyBases = new ArrayList<Integer>();
		enemyArmy = new ArrayList<Entity>();
		enemyUpgrades = new ArrayList<Tech>();
	}
	
	public boolean equals(Memory other){
		if(enemyBuildings.size() != other.enemyBuildings.size()){
			return false;
		}
		// Compare buildings
		enemyBuildings.sort(null);
		other.enemyBuildings.sort(null);
		for(int i = 0; i < enemyBuildings.size(); i++){
			if(enemyBuildings.get(i) != other.enemyBuildings.get(i)){
				return false;
			}
		}
		
		// compare bases
		if(enemyBases.size() != other.enemyBases.size()){
			return false;
		}
		enemyBases.sort(null);
		other.enemyBases.sort(null);
		for(int i = 0; i < enemyBases.size(); i++){
			if(enemyBases.get(i) != other.enemyBases.get(i)){
				return false;
			}
		}
		
		// compare armies
		if(enemyArmy.size() != other.enemyArmy.size()){
			return false;
		}
		enemyArmy.sort(null);
		other.enemyArmy.sort(null);
		for(int i = 0; i < enemyArmy.size(); i++){
			if(enemyArmy.get(i) != other.enemyArmy.get(i)){
				return false;
			}
		}
		return true;
	}
	
	public void addBuilding(Entity building){
		enemyBuildings.add(building);
	}
	
	public void addBases(int base){
		enemyBases.add(base);
	}
	
	public void addArmy(Entity unit){
		enemyArmy.add(unit);
	}
}
