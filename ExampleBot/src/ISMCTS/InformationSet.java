package ISMCTS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bots.ISMCTSBot;
import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import stateInformation.Memory;

public class InformationSet {
	
	//private ArrayList<Action> enemyActions;
	public ArrayList<Entity> buildings;
	public ArrayList<Integer> bases;
	public ArrayList<Entity> army;
	public ArrayList<Tech> upgrades;
	//public int supply;
	//public Memory memory;
	//public int attacking;

	public InformationSet(){
		buildings = new ArrayList<Entity>();
		bases = new ArrayList<Integer>();
		army = new ArrayList<Entity>();
		upgrades = new ArrayList<Tech>();
	}
	
	public InformationSet(Game game, Memory memory, List<BaseLocation> baseLocations){
		collectInformation(game, baseLocations);
		//this.memory = memory;
	} 
	
	public static InformationSet getDefault(){
		InformationSet result = new InformationSet();
		result.bases.add(0);
		result.buildings.add(Entity.Nexus);
	//	result.attacking = -1;
	//	result.supply = 5;
		return result;
	}
	
	public void collectInformation(Game game, List<BaseLocation> baseLocations){
		// collect units
		for(Unit u : game.self().getUnits()){
			if(u.getType().isBuilding()){
				if(u.getType() == UnitType.Protoss_Nexus){
					for(BaseLocation b : baseLocations){
						BaseLocation closest = BWTA.getNearestBaseLocation(u.getPosition());
						if(b.getX() == closest.getX() && b.getY() == closest.getY()){
							bases.add(baseLocations.indexOf(b));
							break;
						}
					}
				}
				buildings.add(ISMCTS.typeToEntity(u.getType()));	
			} else if(!u.getType().isWorker() && u.getType() != UnitType.Protoss_Scarab && u.getType() != UnitType.Protoss_Interceptor){
				army.add(ISMCTS.typeToEntity(u.getType()));
			}
		}
		//supply = game.self().supplyUsed();
		
		// collect upgrades
		for(Tech t : Tech.getAllTech()){
			if(game.self().getUpgradeLevel(ISMCTS.techToUpgrade(t)) > 0){
				t.level = game.self().getUpgradeLevel(ISMCTS.techToUpgrade(t));
				upgrades.add(t);
			}
		}
		// collect bases
		
	}
	
	public Memory getMemory(){
		return null; //memory;
	}
	
	public ArrayList<Tech> getUpgrades(){
		return upgrades;
	}
	
	public ArrayList<Entity> getArmy(){
		return army;
	}
	
	public ArrayList<Entity> getBuildings(){
		//System.out.println(buildings);
		return buildings;
	}
	
	/*
	public void setAttackTarget(int targetBase){
		//attacking = targetBase;
	}*/
	
	public void setMemory(Memory memory){
	//	this.memory = memory;
	}
	/*
	public int getAttackTarget(){
		return attacking;
	}*/
	
	public List<Integer> getBases(){
		return bases;
	}
	
	public boolean equals(InformationSet other){
	/*	if(supply != other.supply){
			return false;
		} */
		/*if(!memory.equals(other.memory)){
			return false;
		}*/
		
		// compare bases
		if(bases.size() != other.bases.size()){
			return false;
		}
		bases.sort(null);
		other.bases.sort(null);
		for(int i = 0; i < bases.size(); i++){
			if(bases.get(i) != other.bases.get(i)){
				return false;
			}
		}
		
		// compare buildings
		if(buildings.size() != other.buildings.size()){
			return false;
		}
		buildings.sort(null);
		other.buildings.sort(null);
		for(int i = 0; i < buildings.size(); i++){
			if(buildings.get(i) != other.buildings.get(i)){
				return false;
			}
		}
		// compare armies
		if(army.size() != other.army.size()){
			return false;
		}
		army.sort(null);
		other.army.sort(null);
		for(int i = 0; i < army.size(); i++){
			if(army.get(i) != other.army.get(i)){
				return false;
			}
		}
		return true;
	}
	
}
